package com.example.b07taam2026.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.b07taam2026.R;
import com.example.b07taam2026.data.RoleManager;
import com.example.b07taam2026.ui.HomeActivity;

public class LoginPage extends AppCompatActivity implements LoginPresenter.View {

    public static final String EXTRA_IS_ADMIN = "isAdmin";

    private EditText editUser, editPass;
    private Button buttonLogin, buttonSignUp;
    private CheckBox keepSignedIn;

    private LoginPresenter presenter;

    // set up views presenter and click listeners
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try { com.google.firebase.FirebaseApp.initializeApp(this); } catch (Exception ignored) { }
        setContentView(R.layout.activity_login);

        editUser = findViewById(R.id.editUser);
        editPass = findViewById(R.id.editPass);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonSignUp = findViewById(R.id.buttonSignUp);
        keepSignedIn = findViewById(R.id.checkKeepSignedIn);

        presenter = new LoginPresenter(this, new AuthManager(), new RoleManager());

        // auto login if keep signed in was checked
        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        presenter.tryAutoLogin(prefs.getBoolean("keepLoggedIn", false));

        // forward button clicks to the presenter
        buttonLogin.setOnClickListener(v -> presenter.login(
                editUser.getText().toString().trim(),
                editPass.getText().toString(),
                keepSignedIn.isChecked()));

        buttonSignUp.setOnClickListener(v -> startActivity(new Intent(this, SignUpPage.class)));
    }

    @Override
    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    @Override
    public void setLoginEnabled(boolean enabled) {
        buttonLogin.setEnabled(enabled);
    }
    // save the keep signed in preference
    @Override
    public void persistKeepSignedIn(boolean keep) {
        getSharedPreferences("LoginPrefs", MODE_PRIVATE)
                .edit().putBoolean("keepLoggedIn", keep).apply();
    }

    @Override
    public void navigateToHome(String role, String uid, String username) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("USER_ROLE", role);
        intent.putExtra("UID", uid);
        intent.putExtra("USER_NAME", username);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.detachView();
    }
}