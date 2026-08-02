package com.example.b07taam2026;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CommentManager {

    private final DatabaseReference commentsRef;
    private ValueEventListener liveListener;
    private DatabaseReference activeRef;

    public CommentManager() {
        commentsRef = FirebaseDatabase
                .getInstance("https://taam-artifact-management-default-rtdb.firebaseio.com")
                .getReference("comments");
    }

    public interface CommentCallback {
        void onResult(List<Comment> comments);
        void onError(String errorMessage);
    }

    public void startLive(String lotNumber, CommentCallback callback) {
        stopLive();
        activeRef = commentsRef.child(lotNumber);
        liveListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Comment> result = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Comment c = child.getValue(Comment.class);
                    if (c != null) {
                        c.setId(child.getKey());
                        result.add(c);
                    }
                }
                callback.onResult(result);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("CommentManager", "comments read failed", error.toException());
                callback.onError(error.getMessage());
            }
        };
        activeRef.addValueEventListener(liveListener);
    }

    public void stopLive() {
        if (liveListener != null && activeRef != null) {
            activeRef.removeEventListener(liveListener);
            liveListener = null;
            activeRef = null;
        }
    }

    public void addComment(String lotNumber, String author, String text) {
        DatabaseReference lotRef = commentsRef.child(lotNumber);
        String key = lotRef.push().getKey();
        if (key == null) return;
        Comment comment = new Comment(key, author, text, System.currentTimeMillis());
        lotRef.child(key).setValue(comment);
    }

    public void toggleLike(String lotNumber, String commentId, String uid, boolean currentlyLiked) {
        DatabaseReference likeRef = commentsRef.child(lotNumber).child(commentId).child("likes").child(uid);
        if (currentlyLiked) {
            likeRef.removeValue();
        } else {
            likeRef.setValue(true);
        }
    }

    public void addReply(String lotNumber, String commentId, String author, String text) {
        DatabaseReference repliesRef = commentsRef.child(lotNumber).child(commentId).child("replies");
        String key = repliesRef.push().getKey();
        if (key == null) return;
        repliesRef.child(key).setValue(new Comment(key, author, text, System.currentTimeMillis()));
    }

    // admin comment management
    public void deleteComment(String lotNumber, String commentId) {
        commentsRef.child(lotNumber).child(commentId).removeValue();
    }

    public void deleteReply(String lotNumber, String commentId, String replyId) {
        commentsRef.child(lotNumber).child(commentId).child("replies").child(replyId).removeValue();
    }
}
