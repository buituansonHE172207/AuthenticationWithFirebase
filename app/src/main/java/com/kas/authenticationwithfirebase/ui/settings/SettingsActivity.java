package com.kas.authenticationwithfirebase.ui.settings;

import android.os.Bundle;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.kas.authenticationwithfirebase.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Enable back button
            getSupportActionBar().setTitle("");
        }
        // Set up click listeners for each settings option
        findViewById(R.id.chat_option).setOnClickListener(view -> openAppearanceSettings());
//        findViewById(R.id.link_notification).setOnClickListener(view -> openNotificationSettings());
//        findViewById(R.id.link_privacy).setOnClickListener(view -> openPrivacySettings());
    }

    private void openAppearanceSettings() {
        startActivity(new Intent(SettingsActivity.this, AppearanceActivity.class));
    }

    //
//    private void openNotificationSettings() {
//        startActivity(new Intent(this, NotificationActivity.class));
//    }
//
//    private void openPrivacySettings() {
//        startActivity(new Intent(this, PrivacyActivity.class));
//    }
// Handle back button in the toolbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Close this activity and go back
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

