package com.kas.authenticationwithfirebase.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.kas.authenticationwithfirebase.R;

public class StorageAndDataActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_and_data);
        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Enable back button
            getSupportActionBar().setTitle("");
        }

        LinearLayout clearChatContainer = findViewById(R.id.clear_chat_container);

        clearChatContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to ClearChatHistoryActivity
                Intent intent = new Intent(StorageAndDataActivity.this, ClearChatHistoryActivity.class);
                startActivity(intent);
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Close this activity and go back
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
