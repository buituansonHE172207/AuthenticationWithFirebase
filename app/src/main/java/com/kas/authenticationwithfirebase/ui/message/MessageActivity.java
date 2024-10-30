package com.kas.authenticationwithfirebase.ui.message;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.kas.authenticationwithfirebase.R;
import com.kas.authenticationwithfirebase.data.model.Message;
import com.kas.authenticationwithfirebase.utility.Resource;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MessageActivity extends AppCompatActivity {

    private MessageViewModel messageViewModel;
    private MessageAdapter messageAdapter;
    private RecyclerView recyclerView;
    private TextInputEditText messageInput;
    private ImageButton sendButton,backButton;
    private List<Message> messages = new ArrayList<>();
    private String chatRoomId;
    private Observer<Resource<List<Message>>> messagesObserver;

    private ImageButton extraIcon;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        chatRoomId = getIntent().getStringExtra("chatRoomId");
        recyclerView = findViewById(R.id.recycler_view_messages);
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
        extraIcon = (ImageButton) findViewById(R.id.btnExtra);
        extraIcon.setOnClickListener(this::showPopupMenu);

        messageViewModel = new ViewModelProvider(this).get(MessageViewModel.class);
        messageAdapter = new MessageAdapter(messages, messageViewModel.getCurrentUserId());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageAdapter);

        // Initialize the observer
        messagesObserver = resource -> {
            if (resource.getStatus() == Resource.Status.SUCCESS) {
                messages.clear();
                messages.addAll(resource.getData());
                for (Message message : resource.getData()) {
                    List<String> readBy = message.getReadBy();
                    messageViewModel.markMessageAsRead(chatRoomId, message.getMessageId());
                }
                messageAdapter.notifyDataSetChanged();
                if (!messages.isEmpty()) {
                    // Scroll to the last message
                    recyclerView.smoothScrollToPosition(messages.size() - 1);
                }
            }
        };

        // Send Message
        sendButton.setOnClickListener(v -> {
            String messageContent = messageInput.getText().toString();
            if (!messageContent.isEmpty()) {
                Message message = new Message(
                        null,
                        chatRoomId,
                        messageViewModel.getCurrentUserId(),
                        messageContent,
                        "text",
                        System.currentTimeMillis(),
                        null);
                messageViewModel.sendMessage(chatRoomId, message);
                messageInput.setText("");
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Observe messages when the activity is resumed
        messageViewModel.observeMessages(chatRoomId).observe(this, messagesObserver);
    }

    @Override
    protected void onPause() {
        super.onPause();
        messageViewModel.removeMessagesListener(chatRoomId);
    }
    @SuppressLint("RestrictedApi")
    private void showPopupMenu(View view) {
        // Create a MenuBuilder instead of PopupMenu
        MenuBuilder menuBuilder = new MenuBuilder(this);
        getMenuInflater().inflate(R.menu.chat_extra, menuBuilder);

        // Create the MenuPopupHelper with the MenuBuilder
        MenuPopupHelper menuPopupHelper = new MenuPopupHelper(this, menuBuilder, view);
        menuPopupHelper.setForceShowIcon(true);  // Show icons in the popup menu

        // Set the menu item click listener
        menuBuilder.setCallback(new MenuBuilder.Callback() {
            @Override
            public boolean onMenuItemSelected(@NonNull MenuBuilder menu, @NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.btnCamera) {
                    return true;
                } else if (itemId == R.id.btnAttachment) {
                    // Handle settings action, e.g., open settings activity
                    return true;
                } else if (itemId == R.id.btnMic) {
                    // Handle settings action, e.g., open settings activity
                    return true;
                }
                return false;
            }

            @Override
            public void onMenuModeChange(MenuBuilder menu) {
                // Not needed in this case
            }
        });

        // Show the popup menu
        menuPopupHelper.show();
    }
}
