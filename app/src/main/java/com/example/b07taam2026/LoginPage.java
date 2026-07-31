package com.example.b07taam2026;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginPage extends AppCompatActivity {
    // key for isAdmin boolean passed to HomeActivity
    public static final String EXTRA_IS_ADMIN = "isAdmin";

    private EditText editUser, editPass; 
    private Button buttonLogin, buttonSignUp;

    private AuthManager authManager;
    private RoleManager roleManager;

    private String user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            com.google.firebase.FirebaseApp.initializeApp(this);
        } catch (Exception ignored) { }
        setContentView(R.layout.activity_login);

        // views from activity_login.xml
        editUser = findViewById(R.id.editUser);
        editPass = findViewById(R.id.editPass);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonSignUp = findViewById(R.id.buttonSignUp);

        // firebase helpers (Ryan's backend classes)
        authManager = new AuthManager();
        roleManager = new RoleManager();
        
        buttonLogin.setOnClickListener(v -> handleLogin());

        // open the sign-up screen
        buttonSignUp.setOnClickListener(v -> startActivity(new Intent(LoginPage.this, SignUpPage.class)));
    }

    // validate input and attempt Firebase sign in
    public void handleLogin() {
        user = editUser.getText().toString().trim();
        String pass = editPass.getText().toString().trim();
        if (user.isEmpty() || pass.isEmpty()){
            Toast.makeText(this, "Enter both fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // disable button to stop duplicate requests
        buttonLogin.setEnabled(false);
        authManager.login(user, pass, new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(String uid) {
                checkRoleAndProceed(uid);
            }

            @Override
            public void onFailure(String errorMessage) {
                buttonLogin.setEnabled(true);
                Toast.makeText(LoginPage.this, "Login failed: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    // read user role and switch to main page
    private void checkRoleAndProceed(String uid) {
        roleManager.isAdmin(uid, new RoleManager.RoleCallback() {
            @Override
            public void onResult(boolean isAdmin) {
                buttonLogin.setEnabled(true);
                Intent intent = new Intent(LoginPage.this, HomeActivity.class);
                intent.putExtra(EXTRA_IS_ADMIN, isAdmin);
                intent.putExtra("UID", uid);
                roleManager.fetchUsername(uid, username -> {
                    intent.putExtra("USER_NAME", username != null ? username : user);
                    startActivity(intent);
                    finish();
                });
            }

            @Override
            public void onError(String errorMessage) {
                buttonLogin.setEnabled(true);
                Toast.makeText(LoginPage.this, "Role error: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
}
