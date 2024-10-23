package com.kas.authenticationwithfirebase.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.kas.authenticationwithfirebase.ui.main.MainActivity;
import com.kas.authenticationwithfirebase.R;
import com.kas.authenticationwithfirebase.ui.signup.SignUpActivity;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView signUpTextView;

    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        emailEditText = findViewById(R.id.login_email);
        passwordEditText = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        signUpTextView = findViewById(R.id.signup_text);

        loginButton.setOnClickListener(view -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            if (email.isEmpty()) {
                emailEditText.setError("Email is required");
                emailEditText.requestFocus();
                return;
            }
            if (password.isEmpty()) {
                passwordEditText.setError("Password is required");
                passwordEditText.requestFocus();
                return;
            }
            viewModel.signIn(email, password).observe(this, firebaseUser -> {
                if (firebaseUser != null) {
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                }
            });
        });

        signUpTextView.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
        });
    }




}