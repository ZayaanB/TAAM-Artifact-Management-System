package com.example.b07taam2026;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminManager {
    private final DatabaseReference usersRef;
    private ValueEventListener liveListener;
    private Query liveQuery;

    public AdminManager() {
        FirebaseDatabase database = FirebaseDatabase
                .getInstance("https://taam-artifact-management-default-rtdb.firebaseio.com");
        usersRef = database.getReference("users");
    }
    public interface AdminCallback {
        void onResult(List<User> admins);
        void onError(String errorMessage);
    }

    public interface WriteCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    public void addAdmin(String email, WriteCallback callback) {
        usersRef.orderByChild("email").equalTo(email).get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        callback.onError("User not found");
                        return;
                    }
                    // get the role and update by UID
                    DataSnapshot userSnapshot = snapshot.getChildren().iterator().next();
                    String uid = userSnapshot.getKey();
                    usersRef.child(uid).child("role").setValue("admin")
                            .addOnSuccessListener(a -> callback.onSuccess())
                            .addOnFailureListener(e -> callback.onError(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void removeAdmin(String uid, WriteCallback callback) {
        // change admins role to user
        usersRef.child(uid).child("role").setValue("user")
                .addOnSuccessListener(a -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void startLive(AdminCallback callback) {
        liveQuery = usersRef.orderByChild("role").equalTo("admin");
        liveListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<User> admins = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()){
                    User u = child.getValue(User.class);
                    if(u != null){
                        u.setUid(child.getKey());
                        admins.add(u);
                    }
                }
                callback.onResult(admins);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        };
        liveQuery.addValueEventListener(liveListener);
    }
    public void stopLive() {
        if (liveListener != null && liveQuery != null) {
            liveQuery.removeEventListener(liveListener);
        }
    }

}
