package com.example.b07demosummer2024;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class LoginTestFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.temp_login, container, false);

        EditText editTextEmail = view.findViewById(R.id.editTextEmail);
        EditText editTextPassword = view.findViewById(R.id.editTextPassword);
        Button buttonLogin = view.findViewById(R.id.buttonLogin);

        AuthManager authManager = new AuthManager();
        RoleManager roleManager = new RoleManager();

        buttonLogin.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            authManager.login(email, password, new AuthManager.AuthCallback() {
                @Override
                public void onSuccess(String uid) {
                    Log.d("LoginTest", "SUCCESS uid=" + uid);
                    roleManager.isAdmin(uid, new RoleManager.RoleCallback() {
                        @Override
                        public void onResult(boolean isAdmin) {
                            Log.d("LoginTest", "isAdmin=" + isAdmin);
                            Toast.makeText(getContext(),
                                    "Login OK: " + (isAdmin ? "ADMIN" : "USER"),
                                    Toast.LENGTH_LONG).show();
                        }
                        @Override
                        public void onError(String msg) {
                            Log.d("LoginTest", "role error: " + msg);
                            Toast.makeText(getContext(), "Role error: " + msg, Toast.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void onFailure(String errorMessage) {
                    Log.d("LoginTest", "FAILURE: " + errorMessage);
                    Toast.makeText(getContext(), "Login failed: " + errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        });

        return view;
    }
}