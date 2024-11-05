package com.kas.authenticationwithfirebase.ui.forgotPassword;

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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.kas.authenticationwithfirebase.R;
import com.kas.authenticationwithfirebase.ui.auth.AuthViewModel;
import com.kas.authenticationwithfirebase.utility.Resource;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText emailEditText;
    private Button resetPasswordButton;
    private TextView notice;
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
        setContentView(R.layout.activity_forgot_password);
        // Initialize the views
        emailEditText = findViewById(R.id.emailEditText);
        resetPasswordButton = findViewById(R.id.loginButton);
        notice = findViewById(R.id.resetNotice);

        backButton = findViewById(R.id.backButton); // Use the correct ID if it's not "backButton"
        backButton.setOnClickListener(v -> finish());

        // Disable button initially
        resetPasswordButton.setEnabled(false);

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

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        resetPasswordButton.setOnClickListener(view -> {
            String email = emailEditText.getText().toString().trim();
            if (email.isEmpty()) {
                emailEditText.setError("Email is required");
                emailEditText.requestFocus();
                return;
            }
            displayLoadingButton(true);
            viewModel.resetPassword(email).observe(this, resource -> {
                if (resource.getStatus() == Resource.Status.SUCCESS) {
                    displayNotice("Check your email inbox to proceed", "SUCCESS");
                } else {
                    displayNotice("Password reset email failed:" + resource.getMessage(), "FAIL");
                    displayLoadingButton(false);
                }
            });
        });
        emailEditText.addTextChangedListener(textWatcher);
    }

    private void checkFieldsForEmptyValues() {
        String email = emailEditText.getText().toString().trim();
        notice.setVisibility(View.INVISIBLE);
        if (!email.isEmpty()) {
            resetPasswordButton.setEnabled(true);
            resetPasswordButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primary)); // enabled color
            resetPasswordButton.setTextColor(ContextCompat.getColorStateList(this, R.color.white));
        } else {
            resetPasswordButton.setEnabled(false);
            resetPasswordButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.disabled_button)); // disabled color
            resetPasswordButton.setTextColor(ContextCompat.getColorStateList(this, R.color.menu_item_gray));

        }
    }

    private void displayLoadingButton(Boolean isLoading) {
        if (isLoading) {
            // Disable the login button and set "Logging in" text and color
            resetPasswordButton.setEnabled(false);
            resetPasswordButton.setText("Request sent");
            resetPasswordButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.disabled_button)); // Gray background
            resetPasswordButton.setTextColor(ContextCompat.getColorStateList(this, R.color.menu_item_gray));
        } else {
            // Reset the button state after login attempt is complete
            resetPasswordButton.setEnabled(true);
            resetPasswordButton.setText("Request Reset Password");
            resetPasswordButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primary)); // Enabled color
            resetPasswordButton.setTextColor(ContextCompat.getColorStateList(this, R.color.white));
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