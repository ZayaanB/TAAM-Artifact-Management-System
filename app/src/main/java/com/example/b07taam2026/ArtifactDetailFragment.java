package com.example.b07taam2026;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ArtifactDetailFragment extends Fragment {

    private static final String ARG_ARTIFACT = "artifact";
    private static final String ARG_USERNAME = "username";
    private static final String ARG_UID = "uid";
    private static final String ARG_IS_ADMIN = "isAdmin";

    private CommentManager commentManager;
    private CommentAdapter commentAdapter;
    private final List<Comment> comments = new ArrayList<>();
    private Button buttonSortNewest, buttonSortOldest;

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

        RecyclerView commentsView = view.findViewById(R.id.commentsRecyclerView);
        commentsView.setLayoutManager(new LinearLayoutManager(requireContext()));
        commentAdapter = new CommentAdapter(comments);
        commentsView.setAdapter(commentAdapter);

        commentManager = new CommentManager();
        commentAdapter.setUserContext(uid, artifact.getLotNumber(), commentManager, isAdmin);

        wireSort(view);
        wireBack(view);

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
        return view;
    }

    private void wireBack(View view) {
        view.findViewById(R.id.buttonBackDetail).setOnClickListener(v -> getParentFragmentManager().popBackStack());
    }

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

    private void bindArtifact(View view, Artifact artifact) {
        ((TextView) view.findViewById(R.id.textArtifactName)).setText(artifact.getName());
        ((TextView) view.findViewById(R.id.textLotNumber)).setText("Lot Number: " + artifact.getLotNumber());
        ((TextView) view.findViewById(R.id.textCategory)).setText("Category: " + artifact.getCategory());
        ((TextView) view.findViewById(R.id.textMaterial)).setText("Material: " + artifact.getMaterial());
        ((TextView) view.findViewById(R.id.textDynastyPeriod)).setText("Dynasty Period: " + artifact.getDynasty());
        ((TextView) view.findViewById(R.id.textDescription)).setText(artifact.getDescription());
    }

    private void wireComposer(View view) {
        EditText input = view.findViewById(R.id.editCommentText);
        Button submit = view.findViewById(R.id.buttonSubmitComment);
        submit.setOnClickListener(v -> submitComment(input));
    }

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (commentManager != null) {
            commentManager.stopLive();
        }
    }
}
