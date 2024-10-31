package com.kas.authenticationwithfirebase.ui.message;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.kas.authenticationwithfirebase.R;
import com.kas.authenticationwithfirebase.data.entity.Message;
import com.kas.authenticationwithfirebase.data.model.MessageWithUserDetail;
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
    private FloatingActionButton sendButton;
    private List<MessageWithUserDetail> messages = new ArrayList<>();
    private String chatRoomId;
    private Observer<Resource<List<MessageWithUserDetail>>> messagesObserver;

    private ImageButton extraIcon;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        chatRoomId = getIntent().getStringExtra("chatRoomId");
        recyclerView = findViewById(R.id.recycler_view_messages);
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);

        messageViewModel = new ViewModelProvider(this).get(MessageViewModel.class);
        messageAdapter = new MessageAdapter(messages, messageViewModel.getCurrentUserId());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageAdapter);

        // Initialize the observer
        messagesObserver = resource -> {
            if (resource.getStatus() == Resource.Status.SUCCESS) {
                messages.clear();
                messages.addAll(resource.getData());
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
}
