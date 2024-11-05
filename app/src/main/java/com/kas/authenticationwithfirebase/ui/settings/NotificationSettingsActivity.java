package com.kas.authenticationwithfirebase.ui.settings;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationManagerCompat;

import com.kas.authenticationwithfirebase.R;

public class NotificationSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Enable back button
            getSupportActionBar().setTitle("");

            // Set the color of the back button
            Drawable backArrow = getResources().getDrawable(R.drawable.baseline_arrow_back_24); // or use `AppCompatResources.getDrawable()` for better compatibility
            backArrow.setColorFilter(getResources().getColor(R.color.top_nav_text_color), PorterDuff.Mode.SRC_ATOP);
            getSupportActionBar().setHomeAsUpIndicator(backArrow);
        }


        // Find the switch or button in the layout
        Button openSettingsButton = findViewById(R.id.openSettingsButton);

        // Set a click listener for the button
        openSettingsButton.setOnClickListener(v -> redirectToNotificationSettings());
    }

    // Method to redirect the user to the app's notification settings page
    private void redirectToNotificationSettings() {
        Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
        startActivity(intent);
    }
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
