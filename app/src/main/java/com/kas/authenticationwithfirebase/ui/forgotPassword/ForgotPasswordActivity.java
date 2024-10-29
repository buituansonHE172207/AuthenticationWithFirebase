package com.kas.authenticationwithfirebase.ui.forgotPassword;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.kas.authenticationwithfirebase.R;
import com.kas.authenticationwithfirebase.ui.auth.AuthViewModel;
import com.kas.authenticationwithfirebase.utility.Resource;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText emailEditText;
    private Button resetPasswordButton;
    private TextView loginTextView;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        // Initialize the views
        emailEditText = findViewById(R.id.forgot_password_email);
        resetPasswordButton = findViewById(R.id.reset_password_button);
        loginTextView = findViewById(R.id.login_text);

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        resetPasswordButton.setOnClickListener(view -> {
            String email = emailEditText.getText().toString().trim();
            if (email.isEmpty()) {
                emailEditText.setError("Email is required");
                emailEditText.requestFocus();
                return;
            }
            viewModel.resetPassword(email).observe(this, resource -> {
                if (resource.getStatus() == Resource.Status.SUCCESS) {
                    Toast.makeText(ForgotPasswordActivity.this, "Password reset email sent successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "Password reset email failed: " + resource.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}