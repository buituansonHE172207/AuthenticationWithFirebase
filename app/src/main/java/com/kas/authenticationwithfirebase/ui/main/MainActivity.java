package com.kas.authenticationwithfirebase.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kas.authenticationwithfirebase.R;
import com.kas.authenticationwithfirebase.ui.auth.AuthViewModel;
import com.kas.authenticationwithfirebase.ui.chatRoom.ChatRoomAdapter;
import com.kas.authenticationwithfirebase.ui.chatRoom.ChatRoomViewmodel;
import com.kas.authenticationwithfirebase.ui.login.LoginActivity;
import com.kas.authenticationwithfirebase.utility.Resource;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private Button btnLogout;
    private ChatRoomViewmodel chatRoomViewmodel;
    private RecyclerView rvChatRooms;
    private Button btnCreateChatRoom;

    private LinearLayout dropdownLayout;
    private ImageButton profileIcon;

    AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //btnLogout = findViewById(R.id.btnLogout);
        rvChatRooms = findViewById(R.id.rvChatRooms);
        profileIcon = findViewById(R.id.profile_icon);
        chatRoomViewmodel = new ViewModelProvider(this).get(ChatRoomViewmodel.class);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        ChatRoomAdapter chatRoomAdapter = new ChatRoomAdapter();

        rvChatRooms.setAdapter(chatRoomAdapter);
        rvChatRooms.setLayoutManager(new LinearLayoutManager(this));
        /*
        btnLogout.setOnClickListener(v -> {
            AuthViewModel authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
            authViewModel.logoutUser();
            //redirect to login

        });
        */
        chatRoomViewmodel.getChatRooms().observe(this, resource -> {
            if (resource.getStatus() == Resource.Status.SUCCESS) {
                chatRoomAdapter.setChatRooms(resource.getData());
            } else if (resource.getStatus() == Resource.Status.ERROR) {
                // Handle error
            } else if (resource.getStatus() == Resource.Status.LOADING) {
                // Show loading
            }
        });
        chatRoomAdapter.setOnChatRoomClickListener(chatRoom -> {
            //show the log
            Log.d("This is the chat room", chatRoom.getChatRoomId());
        });

        btnCreateChatRoom = findViewById(R.id.btnStartNewChat);

        //create chat room
        btnCreateChatRoom.setOnClickListener(v -> {
            chatRoomViewmodel.createChatRoom("81gHkQDfPbaifFqw4wz7HBweL8O2");
        });

        profileIcon.setOnClickListener(view -> {
            showPopupMenu(view);
        });

    }
    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.profile_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.btnLogout) {
                    authViewModel.logoutUser();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    // Clear the back stack to prevent returning to MainActivity
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                    // Finish MainActivity
                    finish();
                    return true;
                } else if (itemId == R.id.btnSettings) {
                    // Handle settings action, e.g., open settings activity
                    return true;
                }
                return false;
            }
        });

        popupMenu.show();
    }

}