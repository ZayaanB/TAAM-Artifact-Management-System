package com.example.b07taam2026;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class SignUpPage extends AppCompatActivity {
    private EditText editEmail, editUsername, editPass, editConfirm;
    private Button buttonCreateAccount, buttonGoToLogin;

    private FirebaseAuth auth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // views from activity_signup.xml
        editEmail = findViewById(R.id.editEmail);
        editUsername = findViewById(R.id.editUsername);
        editPass = findViewById(R.id.editPass);
        editConfirm = findViewById(R.id.editConfirm);
        buttonCreateAccount = findViewById(R.id.buttonCreateAccount);
        buttonGoToLogin = findViewById(R.id.buttonGoToLogin);

        // firebase Auth and users database
        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase
                .getInstance("https://taam-artifact-management-default-rtdb.firebaseio.com")
                .getReference("users");

        buttonCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSignUp();
            }
        });

        // login page
        buttonGoToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    // validate and create Firebase Auth account
    public void handleSignUp() {
        String email = editEmail.getText().toString().trim();
        String username = editUsername.getText().toString().trim();
        String pass = editPass.getText().toString();
        String confirm = editConfirm.getText().toString();

        if (email.isEmpty() || username.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (pass.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!pass.equals(confirm)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // disable button to stop duplicate requests
        buttonCreateAccount.setEnabled(false);
        auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        buttonCreateAccount.setEnabled(true);
                        Toast.makeText(this, "Sign up failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    if (firebaseUser == null) {
                        buttonCreateAccount.setEnabled(true);
                        Toast.makeText(this, "Sign up failed. Please try again.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    saveProfile(firebaseUser.getUid(), username);
                });
    }

    private void saveProfile(String uid, String username) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("username", username);
        profile.put("role", "user"); // new accounts are always regular users

        usersRef.child(uid).updateChildren(profile).addOnCompleteListener(task -> {
            buttonCreateAccount.setEnabled(true);
            if (!task.isSuccessful()) {
                Toast.makeText(this, "Failed to save profile: " + task.getException().getMessage(),
                        Toast.LENGTH_LONG).show();
                return;
            }
            // go to home after signing in
            // wipes login/signup off the back stack.
            Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(LoginPage.EXTRA_IS_ADMIN, false);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
