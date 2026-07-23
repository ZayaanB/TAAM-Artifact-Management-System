package com.example.b07taam2026;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

// wrapper around Firebase Auth (Ryan's backend)
public class AuthManager {

    private final FirebaseAuth memberAuth;

    public AuthManager() {
        memberAuth = FirebaseAuth.getInstance();
    }

    // uid of the signed in user null if nobody is signed in
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

    // how login results get reported back to the caller
    public interface AuthCallback {
        void onSuccess(String uid);
        void onFailure(String errorMessage);
    }

    // sign in with email/password
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
