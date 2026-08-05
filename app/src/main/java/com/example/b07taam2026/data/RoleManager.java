package com.example.b07taam2026.data;

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

    // connect to database
    public RoleManager() {
        usersRef = FirebaseDatabase
                .getInstance("https://taam-artifact-management-default-rtdb.firebaseio.com")
                .getReference("users");
    }
    // inject user for testing
    public RoleManager(DatabaseReference usersRef) {
        this.usersRef = usersRef;
    }

    // how role results get reported back to the caller
    public interface RoleCallback {
        void onResult(boolean isAdmin);
        void onError(String errorMessage);
    }

    public interface UsernameCallback {
        void onResult(String username);
    }

    public interface RoleStringCallback {
        void onResult(String role);
        void onError(String errorMessage);

    }

    // fetch the raw role string from uid
    public void fetchRole(String uid, RoleStringCallback callback){
        if (uid == null){
            callback.onError("No user is signed in");
            return;
        }
        usersRef.child(uid).child("role")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String role = snapshot.getValue(String.class);
                        callback.onResult(role != null ? role: "user");
                    }

                    @Override public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    public void isAdmin(String uid, RoleCallback callback) {
        if (uid == null) {
            callback.onError("No user is signed in");
            return;
        }

        // db role reference check
        usersRef.child(uid).child("role")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String role = snapshot.getValue(String.class);
                        if (role == null) {
                            callback.onError("No role set for this user");
                        } else {
                            callback.onResult("admin".equals(role) || "admin_m".equals(role));
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    // fetch the username for a uid
    public void fetchUsername(String uid, UsernameCallback callback) {
        if (uid == null) { callback.onResult(null); return; }
        usersRef.child(uid).child("username")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        callback.onResult(snapshot.getValue(String.class));
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onResult(null);
                    }
                });
    }
}
