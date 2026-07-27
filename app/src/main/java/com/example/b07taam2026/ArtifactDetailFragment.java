package com.example.b07taam2026;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

public class ArtifactDetailFragment extends Fragment {

    private static final String ARG_ARTIFACT = "artifact";

    public static ArtifactDetailFragment newInstance(Artifact artifact) {
        ArtifactDetailFragment fragment = new ArtifactDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ARTIFACT, artifact);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_artifact_detail, container, false);

        Artifact artifact = (Artifact) requireArguments().getSerializable(ARG_ARTIFACT);

        ((TextView) view.findViewById(R.id.textArtifactName)).setText(artifact.getName());
        ((TextView) view.findViewById(R.id.textLotNumber)).setText("Lot Number: " + artifact.getLotNumber());
        ((TextView) view.findViewById(R.id.textCategory)).setText("Category: " + artifact.getCategory());
        ((TextView) view.findViewById(R.id.textMaterial)).setText("Material: " + artifact.getMaterial());
        ((TextView) view.findViewById(R.id.textDynastyPeriod)).setText("Dynasty Period: " + artifact.getDynasty());
        ((TextView) view.findViewById(R.id.textDescription)).setText(artifact.getDescription());

        RecyclerView comments = view.findViewById(R.id.commentsRecyclerView);
        comments.setLayoutManager(new LinearLayoutManager(requireContext()));
        comments.setAdapter(new CommentAdapter(mockComments()));

        return view;
    }

    private List<Comment> mockComments() {
        long now = System.currentTimeMillis();
        long hour = 60L * 60 * 1000;
        return Arrays.asList(
                new Comment("c1", "Mei Chen", "Stunning craftsmanship on this piece.", now - 2 * hour),
                new Comment("c2", "Daniel Park", "Provenance details would strengthen the catalog entry.", now - 26 * hour),
                new Comment("c3", "Layla Hassan", "Used as a reference during the conservation workshop.", now - 5L * 24 * hour)
        );
    }
}
