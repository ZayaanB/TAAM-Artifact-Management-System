package com.example.b07taam2026;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ArtifactAdapter extends RecyclerView.Adapter<ArtifactAdapter.ArtifactViewHolder> {

    private final List<Artifact> artifacts;
    private final LikeManager likeManager = new LikeManager();
    private final String uid = new AuthManager().getCurrentUid();
    private final Map<String, Long> likeCounts = new HashMap<>();
    private final Set<String> likedByMe = new HashSet<>();

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

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        likeManager.startLive(uid, (counts, mine) -> {
            likeCounts.clear();
            likeCounts.putAll(counts);
            likedByMe.clear();
            likedByMe.addAll(mine);
            notifyDataSetChanged();
        });
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        likeManager.stopLive();
    }

    class ArtifactViewHolder extends RecyclerView.ViewHolder {
        private final TextView textName;
        private final TextView textLotNumber;
        private final TextView textCategory;
        private final TextView textMaterial;
        private final TextView textDynastyPeriod;
        private final TextView textDescription;
        private final ImageButton buttonLike;
        private final TextView textLikeCount;
        private final ImageView imageArtifact;

        ArtifactViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textArtifactName);
            textLotNumber = itemView.findViewById(R.id.textLotNumber);
            textCategory = itemView.findViewById(R.id.textCategory);
            textMaterial = itemView.findViewById(R.id.textMaterial);
            textDynastyPeriod = itemView.findViewById(R.id.textDynastyPeriod);
            textDescription = itemView.findViewById(R.id.textDescription);
            buttonLike = itemView.findViewById(R.id.buttonLike);
            textLikeCount = itemView.findViewById(R.id.textLikeCount);
            imageArtifact = itemView.findViewById(R.id.imageArtifact);
        }

        void bind(Artifact artifact) {
            textName.setText(artifact.getName());
            textLotNumber.setText("Lot Number: " + artifact.getLotNumber());
            textCategory.setText("Category: " + artifact.getCategory());
            textMaterial.setText("Material: " + artifact.getMaterial());
            textDynastyPeriod.setText("Dynasty Period: " + artifact.getDynasty());
            textDescription.setText(artifact.getDescription());

            String url = artifact.getImageUrl();

            if (url != null && !url.isEmpty()) {
                Glide.with(imageArtifact.getContext())
                        .load(url)
                        .centerCrop()
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground).into(imageArtifact);
            } else {
                imageArtifact.setImageResource(R.drawable.ic_launcher_foreground);
            }

            String lot = artifact.getLotNumber();
            Long count = likeCounts.get(lot);
            boolean liked = likedByMe.contains(lot);

            textLikeCount.setText(String.valueOf(count == null ? 0 : count));
            buttonLike.setImageResource(liked
                    ? R.drawable.ic_heart_filled
                    : R.drawable.ic_heart_outline);
            buttonLike.setOnClickListener(v -> {
                if (uid != null) likeManager.setLike(lot, uid, !liked);
            });
        }
    }
}