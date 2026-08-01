package com.example.b07taam2026;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArtifactManager {
    private final DatabaseReference rootRef;
    private final DatabaseReference artifactsRef;
    private ValueEventListener liveListener;

    public ArtifactManager() {
        FirebaseDatabase database = FirebaseDatabase
                .getInstance("https://taam-artifact-management-default-rtdb.firebaseio.com");
        rootRef = database.getReference();
        artifactsRef = database.getReference("artifacts");
    }

    // how artifact list results get reported back to the caller
    public interface ArtifactCallback {
        void onResult(List<Artifact> artifacts);
        void onError(String errorMessage);
    }

    // write results reported back
    public interface WriteCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    // create artifacts (unique lot number)
    public void addArtifact(String lotNumber, Artifact artifact, WriteCallback callback) {
        artifactsRef.child(lotNumber).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        callback.onError("Lot #" + lotNumber + " already exists");
                    }
                    else {
                        artifactsRef.child(lotNumber).setValue(artifact)
                                .addOnSuccessListener(unused -> callback.onSuccess())
                                .addOnFailureListener(e -> callback.onError(e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // overwrite artifacts (based on lot number)
    public void updateArtifact(String lotNumber, Artifact artifact, WriteCallback callback) {
        artifactsRef.child(lotNumber).setValue(artifact)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // remove artifact and all fields
    public void deleteArtifact(String lotNumber, WriteCallback callback) {
        rootRef.child("users").get()
                .addOnSuccessListener(snapshot -> {
                    Map<String, Object> updates = baseDeleteUpdates(lotNumber);
                    for (DataSnapshot user : snapshot.getChildren()) {
                        if (user.child("saved").hasChild(lotNumber)) {
                            updates.put("users/" + user.getKey() + "/saved/" + lotNumber, null);
                        }
                    }
                    applyDelete(updates, callback);
                })
                .addOnFailureListener(e -> applyDelete(baseDeleteUpdates(lotNumber), callback));
    }

    // path removed on delete
    private Map<String, Object> baseDeleteUpdates(String lotNumber) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("artifacts/" + lotNumber, null);
        updates.put("likes/" + lotNumber, null);
        updates.put("comments/" + lotNumber, null);
        return updates;
    }

    private void applyDelete(Map<String, Object> updates, WriteCallback callback) {
        rootRef.updateChildren(updates)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    //artifacts changes cause updates
    public void startLive(ArtifactCallback callback) {
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
