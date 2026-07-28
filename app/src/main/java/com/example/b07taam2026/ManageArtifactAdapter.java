package com.example.b07taam2026;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// taken from home pages artifact adapter
public class ManageArtifactAdapter extends RecyclerView.Adapter<ManageArtifactAdapter.ManageViewHolder> {

    // how edit/delete taps get reported back to the activity
    public interface Listener {
        void onEditClicked(Artifact artifact);
        void onDeleteClicked(Artifact artifact);
    }

    private final List<Artifact> artifacts;
    private final Listener listener;

    public ManageArtifactAdapter(List<Artifact> artifacts, Listener listener) {
        this.artifacts = artifacts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ManageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_manage_artifact, parent, false);
        return new ManageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ManageViewHolder holder, int position) {
        holder.bind(artifacts.get(position));
    }

    @Override
    public int getItemCount() {
        return artifacts.size();
    }

    private String safe(String s) {
        return s == null ? "—" : s;
    }

    class ManageViewHolder extends RecyclerView.ViewHolder {
        private final TextView textName;
        private final TextView textLot;
        private final TextView textInfo;
        private final ImageButton buttonEdit;
        private final ImageButton buttonDelete;

        ManageViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textRowName);
            textLot = itemView.findViewById(R.id.textRowLot);
            textInfo = itemView.findViewById(R.id.textRowInfo);
            buttonEdit = itemView.findViewById(R.id.buttonRowEdit);
            buttonDelete = itemView.findViewById(R.id.buttonRowDelete);
        }

        void bind(Artifact artifact) {
            textName.setText(artifact.getName());
            textLot.setText("Lot #" + safe(artifact.getLotNumber()));
            textInfo.setText(safe(artifact.getCategory()) + " · " + safe(artifact.getDynasty()));

            buttonEdit.setOnClickListener(v -> listener.onEditClicked(artifact));
            buttonDelete.setOnClickListener(v -> listener.onDeleteClicked(artifact));
        }
    }
}
