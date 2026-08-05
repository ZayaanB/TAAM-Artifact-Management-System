package com.example.b07taam2026.auth;

import com.example.b07taam2026.data.RoleManager;

public class LoginPresenter {

    // what the login screen must implement
    public interface View {
        void showError(String message);
        void setLoginEnabled(boolean enabled);
        void persistKeepSignedIn(boolean keep);
        void navigateToHome(String role, String uid, String username);
    }

    private View view;
    private final AuthManager authManager;
    private final RoleManager roleManager;

    public LoginPresenter(View view, AuthManager authManager, RoleManager roleManager) {
        this.view = view;
        this.authManager = authManager;
        this.roleManager = roleManager;
    }

    // skip the login form if already signed in
    public void tryAutoLogin(boolean keepSignedInPref) {
        if (keepSignedInPref && authManager.isLoggedIn()) {
            if (view != null) view.setLoginEnabled(false);
            proceedWithRole(authManager.getCurrentUid(), null);
        }
    }

    // validate input then attempt sign in
    public void login(String username, String password, boolean keepSignedIn) {
        if (username.isEmpty() || password.isEmpty()) {
            if (view != null) view.showError("Enter both fields");
            return;
        }
        if (view != null) view.setLoginEnabled(false);
        // attempt firebase sign in
        authManager.login(username, password, new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(String uid) {
                if (view != null) view.persistKeepSignedIn(keepSignedIn);
                proceedWithRole(uid, username);
            }
            @Override
            public void onFailure(String errorMessage) {
                if (view == null) return;
                view.setLoginEnabled(true);
                view.showError("Login failed: " + errorMessage);
            }
        });
    }

    // fetch role and username and go home
    private void proceedWithRole(String uid, String fallbackName) {
        roleManager.fetchRole(uid, new RoleManager.RoleStringCallback() {
            @Override
            public void onResult(String role) {
                roleManager.fetchUsername(uid, username -> {
                    if (view == null){
                        return;
                    }
                    view.setLoginEnabled(true);
                    view.navigateToHome(role, uid, username != null ? username:fallbackName);
                });
            }

            @Override
            public void onError(String errorMessage){
                if(view == null){
                    return;
                }
                view.setLoginEnabled(true);
                view.showError("Role error: " + errorMessage);
            }
        });

        //TODO: Will be replaced by fetchRole
        /*
        roleManager.isAdmin(uid, new RoleManager.RoleCallback() {
            @Override
            public void onResult(boolean isAdmin) {
                roleManager.fetchUsername(uid, username -> {
                    if (view == null) return;
                    view.setLoginEnabled(true);
                    view.navigateToHome(isAdmin, uid, username != null ? username : fallbackName);
                });
            }
            @Override
            public void onError(String errorMessage) {
                if (view == null) return;
                view.setLoginEnabled(true);
                view.showError("Role error: " + errorMessage);
            }
        });
        */
    }

    public void detachView() { this.view = null; }
}