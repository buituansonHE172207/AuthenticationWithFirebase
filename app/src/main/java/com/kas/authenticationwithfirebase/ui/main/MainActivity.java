package com.kas.authenticationwithfirebase.ui.main;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.kas.authenticationwithfirebase.R;
import com.kas.authenticationwithfirebase.ui.auth.AuthViewModel;
import com.kas.authenticationwithfirebase.ui.login.LoginActivity;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the AuthViewModel instance
        AuthViewModel authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Check if the user is logged in
        if (authViewModel.isUserLoggedIn()) {
            // If the user is logged in, set the main content view
            setContentView(R.layout.activity_main);
        } else {
            // If the user is not logged in, navigate to the LoginActivity
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
    }
}