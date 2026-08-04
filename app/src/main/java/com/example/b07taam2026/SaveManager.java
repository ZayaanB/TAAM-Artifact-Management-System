package com.example.b07taam2026;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashSet;
import java.util.Set;

// handles saved artifacts in firebase
public class SaveManager {

    private final DatabaseReference usersRef;
    private DatabaseReference savedRef;
    private ValueEventListener liveListener;

    // db instantiation
    public SaveManager() {
        usersRef = FirebaseDatabase
                .getInstance("https://taam-artifact-management-default-rtdb.firebaseio.com")
                .getReference("users");
    }

    public interface SaveCallback {
        void onResult(Set<String> savedByMe);
    }

    public void startLive(String uid, SaveCallback callback) {
        stopLive();
        if (uid == null) {
            callback.onResult(new HashSet<>());
            return;
        }
        // save action button
        savedRef = usersRef.child(uid).child("saved");
        liveListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Set<String> saved = new HashSet<>();
                for (DataSnapshot lot : snapshot.getChildren()) {
                    saved.add(lot.getKey());
                }
                callback.onResult(saved);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("SaveManager", "saved read failed", error.toException());
            }
        };
        savedRef.addValueEventListener(liveListener);
    }

    public void stopLive() {
        if (liveListener != null && savedRef != null) {
            savedRef.removeEventListener(liveListener);
        }
        liveListener = null;
    }

    public void setSaved(String lotNumber, String uid, boolean saved) {
        DatabaseReference ref = usersRef.child(uid).child("saved").child(lotNumber);
        if (saved) {
            ref.setValue(true);
        } else {
            ref.removeValue();
        }
    }
}