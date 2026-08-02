package com.example.b07taam2026;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class RelatedArtifactAdapter extends RecyclerView.Adapter<RelatedArtifactAdapter.RelatedViewHolder> {

    private final List<Artifact> artifacts = new ArrayList<>();
    private OnArtifactClickListener listener;

    public interface OnArtifactClickListener {
        void onArtifactClick(Artifact artifact);
    }

    public void setOnArtifactClickListener(OnArtifactClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Artifact> fresh) {
        artifacts.clear();
        artifacts.addAll(fresh);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RelatedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.related_artifact_card, parent, false);
        return new RelatedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RelatedViewHolder holder, int position) {
        holder.bind(artifacts.get(position));
    }

    @Override
    public int getItemCount() {
        return artifacts.size();
    }

    class RelatedViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageArtifact;
        private final TextView textName;
        private final TextView textLotNumber;

        RelatedViewHolder(@NonNull View itemView) {
            super(itemView);
            imageArtifact = itemView.findViewById(R.id.imageRelatedArtifact);
            textName = itemView.findViewById(R.id.textRelatedName);
            textLotNumber = itemView.findViewById(R.id.textRelatedLotNumber);
        }

        void bind(Artifact artifact) {
            textName.setText(artifact.getName());
            textLotNumber.setText("Lot Number: " + artifact.getLotNumber());
            String url = artifact.getImageUrl();
            if (url != null && !url.isEmpty()) {
                Glide.with(imageArtifact.getContext())
                        .load(url)
                        .centerCrop()
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(imageArtifact);
            } else {
                imageArtifact.setImageResource(R.drawable.ic_launcher_foreground);
            }
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onArtifactClick(artifact);
            });
        }
    }
}
