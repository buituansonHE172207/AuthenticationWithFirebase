package com.kas.authenticationwithfirebase.ui.splash;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.kas.authenticationwithfirebase.R;
import com.kas.authenticationwithfirebase.ui.auth.AuthViewModel;
import com.kas.authenticationwithfirebase.ui.login.LoginActivity;
import com.kas.authenticationwithfirebase.ui.main.MainActivity;

import dagger.hilt.android.AndroidEntryPoint;


@AndroidEntryPoint
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //Get the AuthViewModel instance
        AuthViewModel authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);


        new Handler().postDelayed(() -> {
            //Check if the user is logged in
            if (authViewModel.isUserLoggedIn()) {
                //If the user is logged in, navigate to the MainActivity
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            } else {
                //If the user is not logged in, navigate to the LoginActivity
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
            //Finish the current activity
            finish();
        }, 2000);
    }
}