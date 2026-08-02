package com.example.b07taam2026;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class SignUpManager {

    public interface SignUpCallback {
        void onSuccess(String uid, String username);
        void onFailure(String errorMessage);
    }

    private final FirebaseAuth auth;
    private final DatabaseReference usersRef;

    public SignUpManager() {
        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase
                .getInstance("https://taam-artifact-management-default-rtdb.firebaseio.com")
                .getReference("users");
    }

    public void register(String email, String username, String password, SignUpCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        callback.onFailure(task.getException() != null
                                ? task.getException().getMessage() : "Sign up failed");
                        return;
                    }
                    FirebaseUser user = auth.getCurrentUser();
                    if (user == null) {
                        callback.onFailure("Sign up failed. Please try again.");
                        return;
                    }
                    saveProfile(user.getUid(), username, callback);
                });
    }

    private void saveProfile(String uid, String username, SignUpCallback callback) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("username", username);
        profile.put("role", "user");
        usersRef.child(uid).updateChildren(profile).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                callback.onFailure(task.getException() != null
                        ? task.getException().getMessage() : "Failed to save profile");
                return;
            }
            callback.onSuccess(uid, username);
        });
    }
}