package com.kas.authenticationwithfirebase.ui.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.kas.authenticationwithfirebase.ui.auth.AuthViewModel;
import com.kas.authenticationwithfirebase.ui.forgotPassword.ForgotPasswordActivity;
import com.kas.authenticationwithfirebase.ui.main.MainActivity;
import com.kas.authenticationwithfirebase.R;
import com.kas.authenticationwithfirebase.ui.signup.SignUpActivity;
import com.kas.authenticationwithfirebase.utility.Resource;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView signUpTextView, fogotPasswordTextView, notice;
    private AuthViewModel viewModel;
    private ImageButton backButton;
    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Load current theme setting
        sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
        signUpTextView = findViewById(R.id.signup_text);
        fogotPasswordTextView = findViewById(R.id.fogot_password_text);
        notice = findViewById(R.id.resetNotice);

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkFieldsForEmptyValues();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };

        emailEditText.addTextChangedListener(textWatcher);
        passwordEditText.addTextChangedListener(textWatcher);
        signUpTextView.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
        });
        fogotPasswordTextView.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });

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
            displayLoadingButton(true);
            viewModel.loginUser(email, password).observe(this, resource -> {
                if (resource.getStatus() == Resource.Status.SUCCESS) {
                    displayNotice("Success", "SUCCESS");
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else if (resource.getStatus() == Resource.Status.ERROR) {
                    displayNotice(resource.getMessage(), "FAIL");
                    //Toast.makeText(LoginActivity.this, resource.getMessage(), Toast.LENGTH_SHORT).show();
                    displayLoadingButton(false);
                }
            });
        });

        signUpTextView.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
        });
    }

    private void checkFieldsForEmptyValues() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (!email.isEmpty() && !password.isEmpty()) {
            loginButton.setEnabled(true);
            loginButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primary)); // enabled color
            loginButton.setTextColor(ContextCompat.getColorStateList(this, R.color.white));
        } else {
            loginButton.setEnabled(false);
            loginButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.disabled_button)); // disabled color
            loginButton.setTextColor(ContextCompat.getColorStateList(this, R.color.menu_item_gray));

        }
    }

    private void displayLoadingButton(Boolean isLoading) {
        if (isLoading) {
            // Disable the login button and set "Logging in" text and color
            loginButton.setEnabled(false);
            loginButton.setText("Logging in...");
            loginButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.disabled_button)); // Gray background
            loginButton.setTextColor(ContextCompat.getColorStateList(this, R.color.menu_item_gray));
        } else {
            // Reset the button state after login attempt is complete
            loginButton.setEnabled(true);
            loginButton.setText("Login");
            loginButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primary)); // Enabled color
            loginButton.setTextColor(ContextCompat.getColorStateList(this, R.color.white));
        }
    }

    private void displayNotice(String message, String messageType) {
        notice.setText(message);

        int colorResource;
        switch (messageType) {
            case "SUCCESS":
                colorResource = R.color.primary;
                break;
            case "FAIL":
                colorResource = R.color.warning;
                break;
            default:
                colorResource = R.color.primary;
                break;
        }
        notice.setTextColor(ContextCompat.getColorStateList(this, colorResource));
        notice.setVisibility(View.VISIBLE);
    }
}