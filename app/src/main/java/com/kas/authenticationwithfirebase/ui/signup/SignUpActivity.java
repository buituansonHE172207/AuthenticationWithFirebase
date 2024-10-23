package com.kas.authenticationwithfirebase.ui.signup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.kas.authenticationwithfirebase.R;
import com.kas.authenticationwithfirebase.ui.login.LoginActivity;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button signUpButton;
    private TextView loginTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        emailEditText = findViewById(R.id.signup_email);
        passwordEditText = findViewById(R.id.signup_password);
        signUpButton = findViewById(R.id.signup_button);
        loginTextView = findViewById(R.id.login_text);

        auth = FirebaseAuth.getInstance();

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
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isComplete()) {
                            Toast.makeText(SignUpActivity.this, "User created successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                        } else {
                            Toast.makeText(SignUpActivity.this, "Signup Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


            }
        });
        loginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            }
        });
    }
}