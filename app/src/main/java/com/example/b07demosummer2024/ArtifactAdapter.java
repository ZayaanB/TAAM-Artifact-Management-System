package com.example.b07demosummer2024;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ArtifactAdapter extends RecyclerView.Adapter<ArtifactAdapter.ArtifactViewHolder> {
    private final List<Artifact> artifacts;

    public ArtifactAdapter(List<Artifact> artifacts) {
        this.artifacts = artifacts;
    }

    @NonNull
    @Override
    public ArtifactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.artifact_card, parent, false);
        return new ArtifactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtifactViewHolder holder, int position) {
        holder.bind(artifacts.get(position));
    }

    @Override
    public int getItemCount() {
        return artifacts.size();
    }

    static class ArtifactViewHolder extends RecyclerView.ViewHolder {
        private final TextView textName;
        private final TextView textLotNumber;
        private final TextView textCategory;
        private final TextView textMaterial;
        private final TextView textDynastyPeriod;
        private final TextView textDescription;

        ArtifactViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textArtifactName);
            textLotNumber = itemView.findViewById(R.id.textLotNumber);
            textCategory = itemView.findViewById(R.id.textCategory);
            textMaterial = itemView.findViewById(R.id.textMaterial);
            textDynastyPeriod = itemView.findViewById(R.id.textDynastyPeriod);
            textDescription = itemView.findViewById(R.id.textDescription);
        }

        void bind(Artifact artifact) {
            textName.setText(artifact.getName());
            textLotNumber.setText("Lot Number: " + artifact.getLotNumber());
            textCategory.setText("Category: " + artifact.getCategory());
            textMaterial.setText("Material: " + artifact.getMaterial());
            textDynastyPeriod.setText("Dynasty Period: " + artifact.getDynastyPeriod());
            textDescription.setText(artifact.getDescription());
        }
    }
}