package com.example.b07demosummer2024;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginPage extends AppCompatActivity {
    private EditText editUser, editPass; 
    private Button buttonLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editUser = findViewById(R.id.editUser); 
        editPass = findViewById(R.id.editPass); 
        buttonLogin = findViewById(R.id.buttonLogin); 
        
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogin();
            }
        });
    }

    public void handleLogin() {
        String user = editTextUsername.getText().toString().trim();
        String pass = editTextPassword.getText().toString().trim();
        if (user.isEmpty() || pass.isEmptyy()){
            Toast.makeText(this, "Enter both fields", Toast.LENGTH_SHORT).show();
            return;
        }
        //here do firebase and search for user and verify pass in func
    }
}
