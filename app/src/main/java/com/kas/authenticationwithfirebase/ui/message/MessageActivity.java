package com.kas.authenticationwithfirebase.ui.message;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.kas.authenticationwithfirebase.R;
import com.kas.authenticationwithfirebase.data.entity.Message;
import com.kas.authenticationwithfirebase.utility.CameraManager;
import com.kas.authenticationwithfirebase.utility.FileManager;
import com.kas.authenticationwithfirebase.utility.RequestCodeManager;
import com.kas.authenticationwithfirebase.data.model.MessageWithUserDetail;
import com.kas.authenticationwithfirebase.utility.Resource;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MessageActivity extends AppCompatActivity implements CameraManager.CamaraCallBack, FileManager.FilePickerCallback {

    private MessageViewModel messageViewModel;
    private MessageAdapter messageAdapter;
    private RecyclerView recyclerView;
    private TextInputEditText messageInput;
    private ImageButton sendButton, backButton, extraIcon;
    private List<MessageWithUserDetail> messages = new ArrayList<>();
    private String chatRoomId;
    private Observer<Resource<List<MessageWithUserDetail>>> messagesObserver;

    private CameraManager cameraManager;
    private FileManager fileManager;
    private TextView chatName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        // Initialize UI components
        chatRoomId = getIntent().getStringExtra("chatRoomId");
        recyclerView = findViewById(R.id.recycler_view_messages);
        chatName = findViewById(R.id.chat_name);
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);
        backButton = findViewById(R.id.backButton);
        extraIcon = findViewById(R.id.btnExtra);

        // Set up event listeners
        backButton.setOnClickListener(v -> finish());
        extraIcon.setOnClickListener(this::showPopupMenu);

        // Initialize CameraManager and FileManager
        cameraManager = new CameraManager(this, this);
        fileManager = new FileManager(this, this);

        // ViewModel and Adapter Setup
        messageViewModel = new ViewModelProvider(this).get(MessageViewModel.class);
        messageAdapter = new MessageAdapter(messages, messageViewModel.getCurrentUserId());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageAdapter);

        // Get Chat Room Name
        String chatRoomNameText = getIntent().getStringExtra("chatRoomName");
        chatName.setText(chatRoomNameText);

        // Initialize the observer for messages
        setupMessagesObserver();

        // Send Message
        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void setupMessagesObserver() {
        messagesObserver = resource -> {
            if (resource.getStatus() == Resource.Status.SUCCESS) {
                messages.clear();
                messages.addAll(resource.getData());
                messageAdapter.notifyDataSetChanged();
                if (!messages.isEmpty()) {
                    // Scroll to the last message
                    recyclerView.smoothScrollToPosition(messages.size() - 1);
                }
                markMessagesAsRead();
            } else if (resource.getStatus() == Resource.Status.ERROR) {
                Toast.makeText(this, "Failed to load messages: " + resource.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void markMessagesAsRead() {
        for (MessageWithUserDetail message : messages) {
            messageViewModel.markMessageAsRead(chatRoomId, message.getMessageId());
        }
    }

    private void sendMessage() {
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
            messageViewModel.sendTextMessage(chatRoomId, message);
            messageInput.setText("");
        }
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
        MenuBuilder menuBuilder = new MenuBuilder(this);
        getMenuInflater().inflate(R.menu.chat_extra, menuBuilder);
        MenuPopupHelper menuPopupHelper = new MenuPopupHelper(this, menuBuilder, view);
        menuPopupHelper.setForceShowIcon(true);
        menuBuilder.setCallback(new MenuBuilder.Callback() {
            @Override
            public boolean onMenuItemSelected(@NonNull MenuBuilder menu, @NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.btnCamera) {
                    cameraManager.openCameraForPhoto();
                    return true;
                } else if (itemId == R.id.btnAttachment) {
                    fileManager.pickImageAndVideo();
                    return true;
                }
                return false;
            }

            @Override
            public void onMenuModeChange(MenuBuilder menu) { }
        });
        menuPopupHelper.show();
    }

    @Override
    public void onMediaCaptured(Uri mediaUri, boolean isVideo) {
        String messageType = isVideo ? "video" : "image";
        sendMediaMessage(mediaUri, messageType);
    }

    private void sendMediaMessage(Uri mediaUri, String messageType) {
        if (mediaUri != null) {
            Message message = new Message(
                    null,
                    chatRoomId,
                    messageViewModel.getCurrentUserId(),
                    mediaUri.toString(),
                    messageType,
                    System.currentTimeMillis(),
                    null
            );
            messageViewModel.sendImageMessage(chatRoomId, mediaUri, message);
            Toast.makeText(this, messageType + " sent!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to send " + messageType, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onError(String error) {
        Toast.makeText(this, "Error: " + error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == RequestCodeManager.REQUEST_CODE_IMAGE_CAPTURE) {
                this.onMediaCaptured(cameraManager.getCurrentMediaUri(), false);
            } else if (requestCode == RequestCodeManager.REQUEST_CODE_VIDEO_CAPTURE) {
                this.onMediaCaptured(cameraManager.getCurrentMediaUri(), true);
            }
        } else {
            this.onError("Media capture failed or canceled.");
        }
    }

    @Override
    public void onFilePicked(Uri uri) {
        sendMediaMessage(uri, "image");
    }

    @Override
    public void onNoFileSelected() {
        Toast.makeText(this, "No file selected.", Toast.LENGTH_SHORT).show();
    }
}
