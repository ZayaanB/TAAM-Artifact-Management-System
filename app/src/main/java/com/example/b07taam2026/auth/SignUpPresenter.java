package com.example.b07taam2026.auth;

// handles sign up logic between the view and manager
public class SignUpPresenter {

    // what the sign up screen must implement
    public interface View {
        void showError(String message);
        void showMessage(String message);
        void setCreateEnabled(boolean enabled);
        void navigateToHome(String uid, String username);
    }

    private View view;
    private final SignUpManager signUpManager;

    public SignUpPresenter(View view, SignUpManager signUpManager) {
        this.view = view;
        this.signUpManager = signUpManager;
    }

    // validate input and create the account
    public void signUp(String email, String username, String password, String confirm) {
        // email and password requirements
        if (email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            if (view != null) view.showError("Please fill out all fields");
            return;
        }
        if (password.length() < 6) {
            if (view != null) view.showError("Password must be at least 6 characters");
            return;
        }
        if (!password.equals(confirm)) {
            if (view != null) view.showError("Passwords do not match");
            return;
        }
        if (view != null) view.setCreateEnabled(false);
        
        //handle user sign up
        signUpManager.register(email, username, password, new SignUpManager.SignUpCallback() {
            @Override
            public void onSuccess(String uid, String createdName) {
                if (view == null) return;
                view.showMessage("Account created!");
                view.navigateToHome(uid, createdName);
            }
            @Override
            public void onFailure(String errorMessage) {
                if (view == null) return;
                view.setCreateEnabled(true);
                view.showError("Sign up failed: " + errorMessage);
            }
        });
    }

    public void detachView() { this.view = null; }
}