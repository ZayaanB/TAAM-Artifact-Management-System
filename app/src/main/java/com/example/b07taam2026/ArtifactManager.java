package com.example.b07taam2026;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ArtifactManager {
    private final DatabaseReference artifactsRef;
    private ValueEventListener liveListener;

    public ArtifactManager() {
        artifactsRef = FirebaseDatabase
                .getInstance("https://taam-artifact-management-default-rtdb.firebaseio.com")
                .getReference("artifacts");
    }

    // how artifact list results get reported back to the caller
    public interface ArtifactCallback {
        void onResult(List<Artifact> artifacts);
        void onError(String errorMessage);
    }

    // live updates: callback fires whenever artifacts changes
    public void startLive(ArtifactCallback callback) {
        stopLive(); // guard against double subscribing
        liveListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                callback.onResult(parse(snapshot));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        };
        artifactsRef.addValueEventListener(liveListener);
    }

    public void stopLive() {
        if (liveListener != null) {
            artifactsRef.removeEventListener(liveListener);
            liveListener = null;
        }
    }

    // turn snapshot of children into List of Artifact Objects
    private List<Artifact> parse(DataSnapshot snapshot) {
        List<Artifact> artifacts = new ArrayList<>();
        for (DataSnapshot child : snapshot.getChildren()) {
            Artifact a = child.getValue(Artifact.class);

            if (a != null) {
                a.setLotNumber(child.getKey());
                artifacts.add((a));
            }
        }
        return artifacts;
    }

}
