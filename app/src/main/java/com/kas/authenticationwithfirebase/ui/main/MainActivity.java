package com.kas.authenticationwithfirebase.ui.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.kas.authenticationwithfirebase.R;
import com.kas.authenticationwithfirebase.data.entity.ChatRoom;
import com.kas.authenticationwithfirebase.data.entity.User;
import com.kas.authenticationwithfirebase.ui.auth.AuthViewModel;
import com.kas.authenticationwithfirebase.ui.chatRoom.ChatRoomAdapter;
import com.kas.authenticationwithfirebase.ui.chatRoom.ChatRoomViewModel;
import com.kas.authenticationwithfirebase.ui.friend.FriendActivity;
import com.kas.authenticationwithfirebase.ui.friend.FriendViewModel;
import com.kas.authenticationwithfirebase.ui.login.LoginActivity;
import com.kas.authenticationwithfirebase.ui.message.MessageActivity;
import com.kas.authenticationwithfirebase.ui.settings.SettingsActivity;
import com.kas.authenticationwithfirebase.ui.userProfile.UserProfileActivity;
import com.kas.authenticationwithfirebase.utility.Resource;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private ChatRoomViewModel chatRoomViewModel;
    private RecyclerView rvChatRooms;
    private BottomNavigationView bottomNavigationView;
    private LinearLayout dropdownLayout;
    private ImageButton profileIcon;
    private AuthViewModel authViewModel;
    private FriendViewModel friendViewModel;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);

        // Load current theme setting
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

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
                // Observe unread message count for each chat room
                for (ChatRoom chatRoom : resource.getData()) {
                    chatRoomViewModel.getUnreadMessagesCount(chatRoom.getChatRoomId()).observe(this, unreadResource -> {
                        if (unreadResource.getStatus() == Resource.Status.SUCCESS) {
                            chatRoomAdapter.updateUnreadCount(chatRoom.getChatRoomId(), unreadResource.getData());
                        }
                    });
                }
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
            intent.putExtra("chatRoomName", chatRoom.getChatRoomName());
            startActivity(intent);
        });

        // Initialize Bottom Navigation View
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        // Set default highlighted item in Bottom Navigation
        bottomNavigationView.setSelectedItemId(R.id.message);

        // Setup bottom navigation listener
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.new_chat) {
                //chatRoomViewModel.createChatRoom("81gHkQDfPbaifFqw4wz7HBweL8O2");
                return true;
            } else if (item.getItemId() == R.id.message) {
                return true;
            } else if (item.getItemId() == R.id.contact) {
                // Open friends activity
                Intent intent = new Intent(MainActivity.this, FriendActivity.class);
                startActivity(intent);
                return true;
            } else if (item.getItemId() == R.id.settings) {
                // Handle settings action, e.g., open settings activity
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });

        profileIcon.setOnClickListener(this::showPopupMenu);


        //load list friends
        dropdownLayout = findViewById(R.id.dropdownLayout);
        // Khởi tạo ViewModel
        friendViewModel = new ViewModelProvider(this).get(FriendViewModel.class);

        // Quan sát danh sách bạn bè và hiển thị
        friendViewModel.getFriendsList().observe(this, resource -> {
            if (resource.getStatus() == Resource.Status.SUCCESS && resource.getData() != null) {
                populateFriendList(resource.getData());
            }
        });
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
            } else if (itemId == R.id.btnProfile) {
                // Open settings activity
                // Handle settings action, e.g., open settings activity
                Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
                startActivity(intent);

                return true;
            }
            return false;
        });

        popupMenu.show();
    }


    private void populateFriendList(List<User> friendsList) {
        dropdownLayout.removeAllViews(); // Xóa các View cũ

        for (User friend : friendsList) {
            // Tạo `LinearLayout` cho từng bạn bè
            LinearLayout friendLayout = new LinearLayout(this);
            friendLayout.setOrientation(LinearLayout.VERTICAL);
            friendLayout.setPadding(8, 8, 8, 8);
            friendLayout.setGravity(Gravity.CENTER);

            // Tạo ImageView cho ảnh đại diện
            ImageView avatar = new ImageView(this);
            avatar.setLayoutParams(new LinearLayout.LayoutParams(120, 120));

//            // Sử dụng Glide để tải ảnh từ `profileImageUrl`
            Glide.with(this)
                    .load(friend.getProfileImageUrl()) // URL của ảnh
                    .placeholder(R.drawable.default_avatar) // Ảnh tạm thời nếu chưa tải xong
                    //.error(R.drawable.error_avatar) // Ảnh lỗi nếu tải thất bại
                    .into(avatar);

            // Tạo TextView cho tên bạn bè
            TextView name = new TextView(this);
            name.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            name.setText(friend.getUsername()); // Sử dụng username từ User
            name.setTextColor(Color.WHITE);
            name.setTextSize(12);
            name.setGravity(Gravity.CENTER);
            name.setPadding(0, 4, 0, 0);

            // Thêm `ImageView` và `TextView` vào `friendLayout`
            friendLayout.addView(avatar);
            friendLayout.addView(name);

            // Thêm `friendLayout` vào `dropdownLayout`
            dropdownLayout.addView(friendLayout);
        }
    }
}