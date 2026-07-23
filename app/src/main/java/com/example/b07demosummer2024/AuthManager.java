package com.example.b07demosummer2024;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthManager {

    private final FirebaseAuth memberAuth;

    public AuthManager() {
        memberAuth = FirebaseAuth.getInstance();
    }

    public String getCurrentUid() {
        FirebaseUser user = memberAuth.getCurrentUser();
                if (user == null) {
                    return null;
                }
                return user.getUid();
    }

    public boolean isLoggedIn() {
        return memberAuth.getCurrentUser() != null;
    }

    public void logout() {
        memberAuth.signOut();
    }

    public interface AuthCallback {
        void onSuccess(String uid);
        void onFailure(String errorMessage);
    }

    public void login(String email, String password, AuthCallback callback) {
        if (email.isEmpty() || password.isEmpty()) {
            callback.onFailure("Please enter both email and password");
            return;
        }

        memberAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener( task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(getCurrentUid());
                    } else {
                        callback.onFailure(task.getException().getMessage());
                    }
                });
    }
}

