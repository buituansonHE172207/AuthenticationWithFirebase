package com.kas.authenticationwithfirebase.ui.settings;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kas.authenticationwithfirebase.R;
import com.kas.authenticationwithfirebase.data.entity.ChatRoom;
import com.kas.authenticationwithfirebase.ui.chatRoom.ChatRoomAdapter;
import com.kas.authenticationwithfirebase.ui.chatRoom.ChatRoomViewModel;
import com.kas.authenticationwithfirebase.ui.message.MessageViewModel;
import com.kas.authenticationwithfirebase.utility.Resource;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ClearChatHistoryActivity extends AppCompatActivity {

    private ChatRoomViewModel chatRoomViewModel;
    private MessageViewModel messageViewModel;
    private ChatRoomAdapter chatRoomAdapter;
    private RecyclerView rvChatRooms;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clear_chat_history);
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

        // Initialize RecyclerView
        rvChatRooms = findViewById(R.id.rvChatRooms);
        rvChatRooms.setLayoutManager(new LinearLayoutManager(this));

        // Initialize adapter
        chatRoomAdapter = new ChatRoomAdapter();
        chatRoomAdapter.setHideButton(false); // Set the button to be visible in this activity
        rvChatRooms.setAdapter(chatRoomAdapter);

        // Set up ViewModel
        chatRoomViewModel = new ViewModelProvider(this).get(ChatRoomViewModel.class);
        messageViewModel = new ViewModelProvider(this).get(MessageViewModel.class);

        // Observe chat rooms and handle results
        chatRoomViewModel.getChatRooms().observe(this, resource -> {
            if (resource.getStatus() == Resource.Status.SUCCESS) {
                List<ChatRoom> chatRooms = resource.getData();
                chatRoomAdapter.setChatRooms(chatRooms);
                Toast.makeText(this, "Loaded " + (chatRooms != null ? chatRooms.size() : 0) + " chat rooms", Toast.LENGTH_SHORT).show();
            } else if (resource.getStatus() == Resource.Status.ERROR) {
                Toast.makeText(this, "Failed to load chat rooms: " + resource.getMessage(), Toast.LENGTH_SHORT).show();
            } else if (resource.getStatus() == Resource.Status.LOADING) {
                // Optionally show a loading indicator
                Toast.makeText(this, "Loading chat rooms...", Toast.LENGTH_SHORT).show();
            }
        });

// Set up delete listener to use MessageViewModel
        chatRoomAdapter.setOnDeleteChatRoomClickListener(chatRoom -> {
            // Show a confirmation dialog before deletion
            new AlertDialog.Builder(this)
                    .setTitle("Delete Chat Room")
                    .setMessage("Are you sure you want to clear the chat history for this room?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Proceed with deleting messages after confirmation
                        messageViewModel.deleteMessages(chatRoom.getChatRoomId()).observe(this, deleteResult -> {
                            if (deleteResult.getStatus() == Resource.Status.SUCCESS) {
                                // Proceed to delete the chat room if message deletion is successful
                                chatRoomViewModel.deleteChatRoom(chatRoom.getChatRoomId()).observe(this, deleteRoomResult -> {
                                    if (deleteRoomResult.getStatus() == Resource.Status.SUCCESS) {
                                        refreshChatRooms();
                                        Toast.makeText(this, "Chat room and messages cleared successfully", Toast.LENGTH_SHORT).show();
                                    } else if (deleteRoomResult.getStatus() == Resource.Status.ERROR) {
                                        Toast.makeText(this, "Failed to delete chat room: " + deleteRoomResult.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else if (deleteResult.getStatus() == Resource.Status.ERROR) {
                                Toast.makeText(this, "Failed to clear messages: " + deleteResult.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        // Dismiss the dialog if the user cancels the action
                        dialog.dismiss();
                    })
                    .show();
        });

    }

    private void refreshChatRooms() {
        // Refresh the chat rooms after a deletion
        chatRoomViewModel.getChatRooms().observe(this, refreshResult -> {
            if (refreshResult.getStatus() == Resource.Status.SUCCESS) {
                chatRoomAdapter.setChatRooms(refreshResult.getData());
                Toast.makeText(this, "Chat rooms refreshed", Toast.LENGTH_SHORT).show();
            } else if (refreshResult.getStatus() == Resource.Status.ERROR) {
                Toast.makeText(this, "Error refreshing chat rooms: " + refreshResult.getMessage(), Toast.LENGTH_SHORT).show();
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
