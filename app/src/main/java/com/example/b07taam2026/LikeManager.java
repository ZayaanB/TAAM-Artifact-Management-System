package com.example.b07taam2026;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LikeManager {

    private final DatabaseReference likesRef;
    private ValueEventListener liveListener;

    public LikeManager() {
        likesRef = FirebaseDatabase
                .getInstance("https://taam-artifact-management-default-rtdb.firebaseio.com")
                .getReference("likes");
    }

    public interface LikeCallback {
        void onResult(Map<String, Long> likeCounts, Set<String> likedByMe);
    }

    public void startLive(String uid, LikeCallback callback) {
        stopLive();
        liveListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Long> counts = new HashMap<>();
                Set<String> mine = new HashSet<>();

                for (DataSnapshot lot : snapshot.getChildren()) {
                    counts.put(lot.getKey(), lot.getChildrenCount());
                    if (uid != null && lot.hasChild(uid)) {
                        mine.add(lot.getKey());
                    }
                }
                callback.onResult(counts, mine);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("LikeManager", "likes read failed", error.toException());
            }
        };
        likesRef.addValueEventListener(liveListener);
    }

    public void stopLive() {
        if (liveListener != null) {
            likesRef.removeEventListener(liveListener);
            liveListener = null;
        }
    }

    public void setLike(String lotNumber, String uid, boolean liked) {
        DatabaseReference ref = likesRef.child(lotNumber).child(uid);
        if (liked) {
            ref.setValue(true);
        } else {
            ref.removeValue();
        }
    }
}