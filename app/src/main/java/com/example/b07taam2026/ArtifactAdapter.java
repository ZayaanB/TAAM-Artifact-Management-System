package com.example.b07taam2026;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ArtifactAdapter extends RecyclerView.Adapter<ArtifactAdapter.ArtifactViewHolder> {

    private final ArtifactFilter filter = new ArtifactFilter(this::notifyDataSetChanged);
    private final LikeManager likeManager = new LikeManager();
    private final String uid = new AuthManager().getCurrentUid();
    private final Map<String, Long> likeCounts = new HashMap<>();
    private final Set<String> likedByMe = new HashSet<>();
    private final SaveManager saveManager = new SaveManager();
    private final Set<String> savedByMe = new HashSet<>();
    private String query = "";

    @Nullable
    private OnReadMoreClickListener readMoreListener;

    public interface OnReadMoreClickListener {
        void onReadMore(Artifact artifact);
    }

    public ArtifactAdapter(List<Artifact> artifacts) {
        filter.submit(artifacts);
    }

    public void setOnReadMoreClickListener(OnReadMoreClickListener listener) {
        this.readMoreListener = listener;
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
        holder.bind(filter.get(position), readMoreListener);
    }

    @Override
    public int getItemCount() { return filter.size(); }

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

        saveManager.startLive(uid, saved -> {
            savedByMe.clear();
            savedByMe.addAll(saved);
            notifyDataSetChanged();
        });
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        likeManager.stopLive();
        saveManager.stopLive();
    }

    public void submitList(List<Artifact> artifacts) { filter.submit(artifacts); }

    public void setQuery(String q) { filter.setQuery(q); }

    private boolean matches(Artifact a) {
        // check if search text is a substring of these 6 mandatory fields
        return contains(a.getName())
                || contains(a.getLotNumber())
                || contains(a.getCategory())
                || contains(a.getMaterial())
                || contains(a.getDynasty())
                || contains(a.getDescription());
    }

    private boolean contains(String field) {
        // simple substring matching
        return field != null && field.toLowerCase(Locale.ROOT).contains(query);
    }

    class ArtifactViewHolder extends RecyclerView.ViewHolder {
        private final TextView textName;
        private final TextView textLotNumber;
        private final TextView textCategory;
        private final TextView textMaterial;
        private final TextView textDynastyPeriod;
        private final TextView textDescription;
        private final ImageButton buttonLike;
        private final ImageButton buttonSave;
        private final TextView textLikeCount;
        private final ImageView imageArtifact;
        private final Button buttonReadMore;

        ArtifactViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textArtifactName);
            textLotNumber = itemView.findViewById(R.id.textLotNumber);
            textCategory = itemView.findViewById(R.id.textCategory);
            textMaterial = itemView.findViewById(R.id.textMaterial);
            textDynastyPeriod = itemView.findViewById(R.id.textDynastyPeriod);
            textDescription = itemView.findViewById(R.id.textDescription);
            buttonLike = itemView.findViewById(R.id.buttonLike);
            buttonSave = itemView.findViewById(R.id.buttonSave);
            textLikeCount = itemView.findViewById(R.id.textLikeCount);
            imageArtifact = itemView.findViewById(R.id.imageArtifact);
            buttonReadMore = itemView.findViewById(R.id.buttonReadMore);
        }

        void bind(Artifact artifact, @Nullable OnReadMoreClickListener listener) {
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

            boolean saved = savedByMe.contains(lot);
            buttonSave.setImageResource(saved
                    ? R.drawable.ic_bookmark_filled
                    : R.drawable.ic_bookmark_outline);
            buttonSave.setOnClickListener(v -> {
                if (uid != null) saveManager.setSaved(lot, uid, !saved);
            });

            buttonReadMore.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onReadMore(artifact);
                }
            });
            });
        }
    }
}