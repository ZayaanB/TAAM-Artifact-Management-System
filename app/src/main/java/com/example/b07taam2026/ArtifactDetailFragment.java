package com.example.b07taam2026;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;
import androidx.appcompat.app.AlertDialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

// full detail screen for a single artifact
@SuppressWarnings("deprecation")
public class ArtifactDetailFragment extends Fragment {

    private static final String ARG_ARTIFACT = "artifact";
    private static final String ARG_USERNAME = "username";
    private static final String ARG_UID = "uid";
    private static final String ARG_IS_ADMIN = "isAdmin";

    private CommentManager commentManager;
    private CommentAdapter commentAdapter;
    private LikeManager likeManager;
    private SaveManager saveManager;
    private ArtifactManager artifactManager;
    private boolean likedByMe;
    private boolean savedByMe;
    private final List<Comment> comments = new ArrayList<>();
    private Button buttonSortNewest, buttonSortOldest;
    private static final int COLUMN_GAP_DP = 8;
    private RelatedArtifactAdapter relatedAdapter;

    public static ArtifactDetailFragment newInstance(Artifact artifact, String username, String uid, boolean isAdmin) {
        ArtifactDetailFragment fragment = new ArtifactDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ARTIFACT, artifact);
        args.putString(ARG_USERNAME, username);
        args.putString(ARG_UID, uid);
        args.putBoolean(ARG_IS_ADMIN, isAdmin);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_artifact_detail, container, false);

        Artifact artifact = (Artifact) requireArguments().getSerializable(ARG_ARTIFACT);
        String uid = requireArguments().getString(ARG_UID);
        boolean isAdmin = requireArguments().getBoolean(ARG_IS_ADMIN, false);
        bindArtifact(view, artifact);
        wireActions(view, artifact, uid, isAdmin);

        // comments list setup
        RecyclerView commentsView = view.findViewById(R.id.commentsRecyclerView);
        commentsView.setLayoutManager(new LinearLayoutManager(requireContext()));
        commentAdapter = new CommentAdapter(comments);
        commentsView.setAdapter(commentAdapter);

        commentManager = new CommentManager();
        commentAdapter.setUserContext(uid, artifact.getLotNumber(), commentManager, isAdmin);

        wireSort(view);
        wireBack(view);

        // live comment updates
        commentManager.startLive(artifact.getLotNumber(), new CommentManager.CommentCallback() {
            @Override
            public void onResult(List<Comment> fresh) {
                commentAdapter.submitList(fresh);
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(requireContext(), "Failed to load comments: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        wireComposer(view);
        wireRelated(view, artifact);
        return view;
    }

    private void wireBack(View view) {
        view.findViewById(R.id.buttonBackDetail).setOnClickListener(v -> getParentFragmentManager().popBackStack());
    }

    // newest/oldest sort buttons for comments
    private void wireSort(View view) {
        buttonSortNewest = view.findViewById(R.id.buttonSortNewest);
        buttonSortOldest = view.findViewById(R.id.buttonSortOldest);
        int active = ContextCompat.getColor(requireContext(), R.color.cinnabar);
        int inactive = ContextCompat.getColor(requireContext(), R.color.muted_taupe);
        buttonSortNewest.setTextColor(active);
        buttonSortOldest.setTextColor(inactive);
        buttonSortNewest.setOnClickListener(v -> {
            commentAdapter.setSortMode(CommentAdapter.SortMode.NEWEST);
            buttonSortNewest.setTextColor(active);
            buttonSortOldest.setTextColor(inactive);
        });
        buttonSortOldest.setOnClickListener(v -> {
            commentAdapter.setSortMode(CommentAdapter.SortMode.OLDEST);
            buttonSortOldest.setTextColor(active);
            buttonSortNewest.setTextColor(inactive);
        });
    }
    // like, save and delete controls
    private void wireActions(View view, Artifact artifact, String uid, boolean isAdmin) {
        String lot = artifact.getLotNumber();

        ImageButton buttonLike = view.findViewById(R.id.buttonLikeDetail);
        TextView textLikeCount = view.findViewById(R.id.textLikeCountDetail);
        ImageButton buttonSave = view.findViewById(R.id.buttonSaveDetail);
        ImageButton buttonDelete = view.findViewById(R.id.buttonDeleteDetail);

        likeManager = new LikeManager();
        saveManager = new SaveManager();

        // live like count and curr user like state
        likeManager.startLive(uid, (counts, mine) -> {
            Long count = counts.get(lot);
            likedByMe = mine.contains(lot);
            textLikeCount.setText(String.valueOf(count == null ? 0 : count));
            buttonLike.setImageResource(likedByMe
                    ? R.drawable.ic_heart_filled
                    : R.drawable.ic_heart_outline);
        });
        buttonLike.setOnClickListener(v -> {
            if (uid != null) likeManager.setLike(lot, uid, !likedByMe);
        });

        saveManager.startLive(uid, saved -> {
            savedByMe = saved.contains(lot);
            buttonSave.setImageResource(savedByMe
                    ? R.drawable.ic_bookmark_filled
                    : R.drawable.ic_bookmark_outline);
        });
        buttonSave.setOnClickListener(v -> {
            if (uid != null) saveManager.setSaved(lot, uid, !savedByMe);
        });

        // only admins can delete
        if (isAdmin) {
            buttonDelete.setVisibility(View.VISIBLE);
            buttonDelete.setOnClickListener(v -> confirmDelete(artifact));
        }
    }

    // ask before deleting the artifact
    private void confirmDelete(Artifact artifact) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_dialog_title)
                .setMessage(R.string.delete_dialog_message)
                .setPositiveButton(R.string.delete_dialog_confirm, (d, w) -> {
                    if (artifactManager == null) artifactManager = new ArtifactManager();
                    artifactManager.deleteArtifact(artifact.getLotNumber(), new ArtifactManager.WriteCallback() {
                        @Override
                        public void onSuccess() {
                            if (!isAdded()) return;
                            Toast.makeText(requireContext(), R.string.toast_artifact_deleted, Toast.LENGTH_SHORT).show();
                            getParentFragmentManager().popBackStack();
                        }
                        @Override
                        public void onError(String errorMessage) {
                            if (!isAdded()) return;
                            Toast.makeText(requireContext(), "Delete failed: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton(R.string.delete_dialog_cancel, null)
                .show();
    }
    // show artifact name, image and fields
    private void bindArtifact(View view, Artifact artifact) {
        ((TextView) view.findViewById(R.id.textArtifactName)).setText(artifact.getName());

        ImageView image = view.findViewById(R.id.imageArtifactDetail);
        String url = artifact.getImageUrl();
        if (url != null && !url.isEmpty()) {
            Glide.with(image.getContext())
                    .load(url)
                    .fitCenter()
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(image);
        }
        else {
            image.setImageResource(R.drawable.ic_launcher_foreground);
        }

        bindFields(view, artifact);
    }

    private void bindFields(View view, Artifact artifact) {
        LinearLayout container = view.findViewById(R.id.detailFieldsContainer);
        container.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(container.getContext());

        addHeader(inflater, container, "Description");
        addFull(inflater, container, field(null, artifact.getDescription()));

        addHeader(inflater, container, "Identification");
        addPair(inflater, container,
                field("Lot Number", artifact.getLotNumber()),
                field("Accession Number", artifact.getAccessionNumber())
        );
        addPair(inflater, container,
                field("Category", artifact.getCategory()),
                field("Material", artifact.getMaterial())
        );

        addHeader(inflater, container, "Physical");
        addPair(inflater, container,
                field("Dimensions", artifact.getDimensions()),
                field("Current Location", artifact.getCurrentLocation())
        );
        addFull(inflater, container, field("Condition Report", artifact.getConditionReport()));

        addHeader(inflater, container, "Origin & History");
        addPair(inflater, container,
                field("Dynasty / Period", artifact.getDynasty()),
                field("Cultural Origin", artifact.getCulturalOrigin())
        );
        addFull(inflater, container, field("Provenance", artifact.getProvenance()));

        addHeader(inflater, container, "Collection");
        addFull(inflater, container, field("Acquisition Method", artifact.getAcquisitionMethod()));

        addHeader(inflater, container, "Notes");
        addFull(inflater, container, field(null, artifact.getNotes()));
    }

    private String[] field(String label, String value) {
        return new String[]{label, value};
    }

    private void addHeader(LayoutInflater inflater, LinearLayout container, String title) {
        View header = inflater.inflate(R.layout.row_detail_section, container, false);
        ((TextView) header.findViewById(R.id.textSectionTitle)).setText(title);
        container.addView(header);
    }

    private void addFull(LayoutInflater inflater, LinearLayout container, String[] field) {
        container.addView(buildCell(inflater, container, field));
    }

    private void addPair(LayoutInflater inflater, LinearLayout container, String[] left, String[] right) {
        LinearLayout row = new LinearLayout(container.getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setBaselineAligned(false);

        addWeightedCell(inflater, row, left, 0, COLUMN_GAP_DP);
        addWeightedCell(inflater, row, right, COLUMN_GAP_DP, 0);

        container.addView(row, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void addWeightedCell(LayoutInflater inflater, LinearLayout row, String[] field, int startDp, int endDp) {
        View cell = buildCell(inflater, row, field);

        // equal split for each cell
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        params.setMarginStart(dpToPx(row, startDp));
        params.setMarginEnd(dpToPx(row, endDp));

        row.addView(cell, params);
    }

    private View buildCell(LayoutInflater inflater, ViewGroup parent, String[] field) {
        View cell = inflater.inflate(R.layout.row_detail_field, parent, false);

        TextView label = cell.findViewById(R.id.textFieldLabel);

        if (field[0] != null) label.setText(field[0]);
        else label.setVisibility(View.GONE);

        ((TextView) cell.findViewById(R.id.textFieldValue)).setText(field[1]);
        return cell;
    }

    private int dpToPx(View view, int value) {
        // convert dp to screen pixels
        return Math.round(value * view.getResources().getDisplayMetrics().density);
    }

    private void wireComposer(View view) {
        EditText input = view.findViewById(R.id.editCommentText);
        Button submit = view.findViewById(R.id.buttonSubmitComment);
        submit.setOnClickListener(v -> submitComment(input));
    }

    // validate and post a new comment
    private void submitComment(EditText input) {
        String text = input.getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(requireContext(), "Comment cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        Artifact artifact = (Artifact) requireArguments().getSerializable(ARG_ARTIFACT);
        String username = requireArguments().getString(ARG_USERNAME);
        if (username == null || username.isEmpty()) {
            username = "Guest";
        }
        commentManager.addComment(artifact.getLotNumber(), username, text);
        input.setText("");
    }

    // related artifacts carousel setup
    private void wireRelated(View view, Artifact artifact) {
        RecyclerView relatedView = view.findViewById(R.id.relatedArtifactsRecyclerView);
        relatedView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        relatedAdapter = new RelatedArtifactAdapter();
        relatedAdapter.setOnArtifactClickListener(this::openRelatedArtifact);
        relatedView.setAdapter(relatedAdapter);

        // watch all artifacts and filter related ones
        artifactManager = new ArtifactManager();
        artifactManager.startLive(new ArtifactManager.ArtifactCallback() {
            @Override
            public void onResult(List<Artifact> artifacts) {
                List<Artifact> related = findRelated(artifact, artifacts);
                relatedAdapter.submitList(related);
                view.findViewById(R.id.sectionRelatedArtifacts)
                        .setVisibility(related.isEmpty() ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(requireContext(), "Failed to load related artifacts: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // open detail screen for a related artifact
    private void openRelatedArtifact(Artifact artifact) {
        ArtifactDetailFragment next = ArtifactDetailFragment.newInstance(artifact,
                requireArguments().getString(ARG_USERNAME),
                requireArguments().getString(ARG_UID),
                requireArguments().getBoolean(ARG_IS_ADMIN, false));
        ViewGroup container = (ViewGroup) requireView().getParent();
        FragmentManager fm = getParentFragmentManager();
        // clear old detail screens first
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fm.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .add(container.getId(), next)
                .addToBackStack(null)
                .commit();
    }

    // filter artifacts related to the current one
    static List<Artifact> findRelated(Artifact current, List<Artifact> artifacts) {
        String lot = current.getLotNumber();
        List<Artifact> related = new ArrayList<>();
        for (Artifact other : artifacts) {
            if ((lot == null || !lot.equals(other.getLotNumber())) && isRelated(current, other)) {
                related.add(other);
            }
        }
        return related;
    }

    // same dynasty and location is related
    static boolean isRelated(Artifact current, Artifact other) {
        return equal(current.getDynasty(), other.getDynasty())
                && equal(current.getCurrentLocation(), other.getCurrentLocation());
    }

    private static boolean equal(String a, String b) {
        return a != null && !a.isEmpty() && a.equalsIgnoreCase(b);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (commentManager != null) {
            commentManager.stopLive();
        }
        if (likeManager != null) {
            likeManager.stopLive();
        }
        if (saveManager != null) {
            saveManager.stopLive();
        }
        if (artifactManager != null) {
            artifactManager.stopLive();
        }
    }
}
