package com.kas.authenticationwithfirebase.ui.main;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kas.authenticationwithfirebase.R;
import com.kas.authenticationwithfirebase.ui.auth.AuthViewModel;
import com.kas.authenticationwithfirebase.ui.chatRoom.ChatRoomAdapter;
import com.kas.authenticationwithfirebase.ui.chatRoom.ChatRoomViewModel;
import com.kas.authenticationwithfirebase.utility.Resource;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private Button btnLogout;
    private ChatRoomViewModel chatRoomViewmodel;
    private RecyclerView rvChatRooms;
    private Button btnCreateChatRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnLogout = findViewById(R.id.btnLogout);
        rvChatRooms = findViewById(R.id.rvChatRooms);
        chatRoomViewmodel = new ViewModelProvider(this).get(ChatRoomViewModel.class);
        ChatRoomAdapter chatRoomAdapter = new ChatRoomAdapter();

        rvChatRooms.setAdapter(chatRoomAdapter);
        rvChatRooms.setLayoutManager(new LinearLayoutManager(this));

        btnLogout.setOnClickListener(v -> {
            AuthViewModel authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
            authViewModel.logoutUser();
            //redirect to login

        });

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
    }

}