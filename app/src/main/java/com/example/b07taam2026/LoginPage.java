package com.example.b07taam2026;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginPage extends AppCompatActivity {
    // key for isAdmin boolean passed to MainActivity
    public static final String EXTRA_IS_ADMIN = "isAdmin";

    private EditText editUser, editPass; 
    private Button buttonLogin, buttonSignUp;

    private AuthManager authManager;
    private RoleManager roleManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // views from activity_login.xml
        editUser = findViewById(R.id.editUser); 
        editPass = findViewById(R.id.editPass); 
        buttonLogin = findViewById(R.id.buttonLogin); 
        buttonSignUp = findViewById(R.id.buttonSignUp);

        // firebase helpers (Ryan's backend classes)
        authManager = new AuthManager();
        roleManager = new RoleManager();
        
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogin();
            }
        });

        // open the sign-up screen
        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginPage.this, SignUpPage.class));
            }
        });
    }

    // validate input and attempt Firebase sign in
    public void handleLogin() {
        String user = editUser.getText().toString().trim();
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

    // read users/{uid}/role from the DB hand off to MainActivity with the admin flag
    private void checkRoleAndProceed(String uid) {
        roleManager.isAdmin(uid, new RoleManager.RoleCallback() {
            @Override
            public void onResult(boolean isAdmin) {
                buttonLogin.setEnabled(true);
                Intent intent = new Intent(LoginPage.this, MainActivity.class);
                intent.putExtra(EXTRA_IS_ADMIN, isAdmin);
                startActivity(intent);
                finish(); // drop LoginPage from the back stack
            }

            @Override
            public void onError(String errorMessage) {
                buttonLogin.setEnabled(true);
                Toast.makeText(LoginPage.this, "Role error: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
}
