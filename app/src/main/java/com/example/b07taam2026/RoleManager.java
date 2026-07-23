package com.example.b07taam2026;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

// looks up user roles in the realtime database (Ryan's backend)
public class RoleManager {

    // users node of database
    private final DatabaseReference usersRef;

    public RoleManager() {
        usersRef = FirebaseDatabase
                .getInstance("https://taam-artifact-management-default-rtdb.firebaseio.com")
                .getReference("users");
    }

    // how role results get reported back to the caller
    public interface RoleCallback {
        void onResult(boolean isAdmin);
        void onError(String errorMessage);
    }

    public void isAdmin(String uid, RoleCallback callback) {
        if (uid == null) {
            callback.onError("No user is signed in");
            return;
        }
        usersRef.child(uid).child("role")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String role = snapshot.getValue(String.class);
                        if (role == null) {
                            callback.onError("No role set for this user");
                        } else {
                            callback.onResult("admin".equals(role));
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }
}
