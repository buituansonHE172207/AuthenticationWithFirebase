package com.kas.authenticationwithfirebase.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.kas.authenticationwithfirebase.R;
import com.kas.authenticationwithfirebase.ui.auth.AuthViewModel;
import com.kas.authenticationwithfirebase.ui.chatRoom.ChatRoomAdapter;
import com.kas.authenticationwithfirebase.ui.chatRoom.ChatRoomViewModel;
import com.kas.authenticationwithfirebase.ui.friend.FriendActivity;
import com.kas.authenticationwithfirebase.ui.login.LoginActivity;
import com.kas.authenticationwithfirebase.ui.message.MessageActivity;
import com.kas.authenticationwithfirebase.ui.settings.SettingsActivity;
import com.kas.authenticationwithfirebase.utility.Resource;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private ChatRoomViewModel chatRoomViewModel;
    private RecyclerView rvChatRooms;
    private BottomNavigationView bottomNavigationView;
    private LinearLayout dropdownLayout;
    private ImageButton profileIcon;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rvChatRooms = findViewById(R.id.rvChatRooms);
        profileIcon = findViewById(R.id.profile_icon);
        chatRoomViewModel = new ViewModelProvider(this).get(ChatRoomViewModel.class);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        ChatRoomAdapter chatRoomAdapter = new ChatRoomAdapter();

        rvChatRooms.setAdapter(chatRoomAdapter);
        rvChatRooms.setLayoutManager(new LinearLayoutManager(this));

        // Observe chat rooms
        chatRoomViewModel.getChatRooms().observe(this, resource -> {
            if (resource.getStatus() == Resource.Status.SUCCESS) {
                chatRoomAdapter.setChatRooms(resource.getData());
            } else if (resource.getStatus() == Resource.Status.ERROR) {
                // Handle error
            } else if (resource.getStatus() == Resource.Status.LOADING) {
                // Show loading
            }
        });

        // Open MessageActivity when a chat room is clicked
        chatRoomAdapter.setOnChatRoomClickListener(chatRoom -> {
            Intent intent = new Intent(MainActivity.this, MessageActivity.class);
            intent.putExtra("chatRoomId", chatRoom.getChatRoomId());
            startActivity(intent);
        });

        // Initialize Bottom Navigation View
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Setup bottom navigation listener
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.new_chat) {
                chatRoomViewModel.createChatRoom("81gHkQDfPbaifFqw4wz7HBweL8O2");
                return true;
            } else if (item.getItemId() == R.id.message ) {
                return true;
            } else if (item.getItemId() == R.id.contact) {
                // Open friends activity
                Intent intent = new Intent(MainActivity.this, FriendActivity.class);
                startActivity(intent);
                return true;
            } else if (item.getItemId() == R.id.settings) {
                // Handle settings action, e.g., open settings activity
                return true;
            }
            return false;
        });

        profileIcon.setOnClickListener(this::showPopupMenu);
    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.profile_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.btnLogout) {
                authViewModel.logoutUser();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.btnSettings) {
                // Open settings activity
                // Handle settings action, e.g., open settings activity
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);

                return true;
            }
            return false;
        });

        popupMenu.show();
    }

}