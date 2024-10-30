package com.kas.authenticationwithfirebase.ui.signup;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.kas.authenticationwithfirebase.R;
import com.kas.authenticationwithfirebase.ui.login.LoginActivity;
import com.kas.authenticationwithfirebase.ui.auth.AuthViewModel;
import com.kas.authenticationwithfirebase.utility.Resource;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SignUpActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;
    private EditText nameEditText, emailEditText, passwordEditText, confirmPasswordEditText;

    private Button signUpButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        signUpButton = findViewById(R.id.loginButton);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Set TextWatcher for all fields
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkFieldsForEmptyValues();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        // Add TextWatcher to each EditText
        nameEditText.addTextChangedListener(textWatcher);
        emailEditText.addTextChangedListener(textWatcher);
        passwordEditText.addTextChangedListener(textWatcher);
        confirmPasswordEditText.addTextChangedListener(textWatcher);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                authViewModel.registerUser(email, password).observe(SignUpActivity.this, resource -> {
                    if (resource.getStatus() == Resource.Status.SUCCESS) {
                        Toast.makeText(SignUpActivity.this, "User created successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                    } else {
                        Toast.makeText(SignUpActivity.this, "Signup Failed: " + resource.getMessage(), Toast.LENGTH_SHORT).show();
                        displayLoadingButton(false);
                    }
                });
            }
        });
    }
    // Check if all fields are non-empty
    private void checkFieldsForEmptyValues() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        // Enable button if all fields are filled
        if (!name.isEmpty() && !email.isEmpty() && !password.isEmpty() && !confirmPassword.isEmpty()) {
            signUpButton.setEnabled(true);
            signUpButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primary)); // enabled color
            signUpButton.setTextColor(ContextCompat.getColorStateList(this, R.color.white));
        } else {
            signUpButton.setEnabled(false);
            signUpButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.disabled_button)); // disabled color
            signUpButton.setTextColor(ContextCompat.getColorStateList(this, R.color.menu_item_gray));
        }
    }
    private void displayLoadingButton(Boolean isLoading){
        if (isLoading){
            // Disable the login button and set "Logging in" text and color
            signUpButton.setEnabled(false);
            signUpButton.setText("Logging in...");
            signUpButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.disabled_button)); // Gray background
            signUpButton.setTextColor(ContextCompat.getColorStateList(this, R.color.menu_item_gray));
        } else{
            // Reset the button state after login attempt is complete
            signUpButton.setEnabled(true);
            signUpButton.setText("Login");
            signUpButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primary)); // Enabled color
            signUpButton.setTextColor(ContextCompat.getColorStateList(this, R.color.white));
        }
    }
}