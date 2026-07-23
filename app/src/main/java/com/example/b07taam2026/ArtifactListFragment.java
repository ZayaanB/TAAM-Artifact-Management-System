package com.example.b07taam2026;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

public class ArtifactListFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_artifact_list, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.artifactRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(new ArtifactAdapter(sampleArtifacts()));

        return view;
    }

    private List<Artifact> sampleArtifacts() {
        return Arrays.asList(
                new Artifact(
                        "Jade Bi Disc",
                        "LOT-001",
                        "Ceremonial",
                        "Jade",
                        "Han Dynasty",
                        "A circular jade disc with a central aperture, traditionally associated with ritual and burial practices."
                ),
                new Artifact(
                        "Bronze Ding Vessel",
                        "LOT-002",
                        "Vessel",
                        "Bronze",
                        "Shang Dynasty",
                        "A tripod ritual vessel used for offerings, featuring taotie motifs cast in relief around the body."
                ),
                new Artifact(
                        "Porcelain Vase",
                        "LOT-003",
                        "Decorative",
                        "Porcelain",
                        "Ming Dynasty",
                        "Blue-and-white porcelain vase with floral scrollwork typical of imperial kiln production."
                ),
                new Artifact(
                        "Silk Scroll Painting",
                        "LOT-004",
                        "Painting",
                        "Silk and ink",
                        "Song Dynasty",
                        "Landscape scroll depicting misted mountains and scholars traveling along a mountain path."
                ),
                new Artifact(
                        "Terracotta Horse",
                        "LOT-005",
                        "Sculpture",
                        "Terracotta",
                        "Tang Dynasty",
                        "Painted funerary figure of a horse, reflecting the importance of cavalry and trade along the Silk Road."
                )
        );
    }
}