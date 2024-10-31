package com.kas.authenticationwithfirebase.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.kas.authenticationwithfirebase.R;

public class StorageAndDataActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_and_data);

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
}
