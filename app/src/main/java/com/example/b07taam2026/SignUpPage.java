package com.example.b07taam2026;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// sign up screen that delegates logic to SignUpPresenter
public class SignUpPage extends AppCompatActivity implements SignUpPresenter.View {

    private EditText editEmail, editUsername, editPass, editConfirm;
    private Button buttonCreateAccount, buttonGoToLogin;

    private SignUpPresenter presenter;

    // page fields and view
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        editEmail = findViewById(R.id.editEmail);
        editUsername = findViewById(R.id.editUsername);
        editPass = findViewById(R.id.editPass);
        editConfirm = findViewById(R.id.editConfirm);
        buttonCreateAccount = findViewById(R.id.buttonCreateAccount);
        buttonGoToLogin = findViewById(R.id.buttonGoToLogin);

        presenter = new SignUpPresenter(this, new SignUpManager());

        buttonCreateAccount.setOnClickListener(v -> presenter.signUp(
                editEmail.getText().toString().trim(),
                editUsername.getText().toString().trim(),
                editPass.getText().toString(),
                editConfirm.getText().toString()));

        buttonGoToLogin.setOnClickListener(v -> finish());
    }

    // error and success messages
    @Override
    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    @Override
    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    @Override
    public void setCreateEnabled(boolean enabled) {
        buttonCreateAccount.setEnabled(enabled);
    }

    // proceed to home page
    @Override
    public void navigateToHome(String uid, String username) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra(LoginPage.EXTRA_IS_ADMIN, false);
        intent.putExtra("USER_NAME", username);
        intent.putExtra("UID", uid);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.detachView();
    }
}