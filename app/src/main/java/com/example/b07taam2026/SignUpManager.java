package com.example.b07taam2026;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.HashMap;
import java.util.Map;

public class SignUpManager {

    public interface SignUpCallback {
        void onSuccess(String uid, String username);
        void onFailure(String errorMessage);
    }

    private final FirebaseAuth auth;
    private final DatabaseReference usersRef;
    private final DatabaseReference usernamesRef;

    public SignUpManager() {
        FirebaseDatabase db = FirebaseDatabase
                .getInstance("https://taam-artifact-management-default-rtdb.firebaseio.com");
        auth = FirebaseAuth.getInstance();
        usersRef = db.getReference("users");
        usernamesRef = db.getReference("usernames");
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
                    claimUsername(user, username, callback);
                });
    }

    private void claimUsername(FirebaseUser user, String username, SignUpCallback callback) {
        String key = username.toLowerCase();
        String uid = user.getUid();
        usernamesRef.child(key).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                if (currentData.getValue() != null) {
                    return Transaction.abort();
                }
                currentData.setValue(uid);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot snapshot) {
                if (error != null) {
                    callback.onFailure(error.getMessage());
                    return;
                }
                if (!committed) {
                    user.delete(); // roll back the account we just created
                    callback.onFailure("Username already taken");
                    return;
                }
                saveProfile(uid, user.getEmail(), username, callback);
            }
        });
    }

    private void saveProfile(String uid, String email, String username, SignUpCallback callback) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("username", username);
        profile.put("email", email);
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