package com.kas.authenticationwithfirebase.ui.main;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.kas.authenticationwithfirebase.R;
import com.kas.authenticationwithfirebase.data.entity.ChatRoom;
import com.kas.authenticationwithfirebase.data.entity.User;
import com.kas.authenticationwithfirebase.ui.chatRoom.ChatRoomAdapter;
import com.kas.authenticationwithfirebase.ui.chatRoom.ChatRoomViewModel;
import com.kas.authenticationwithfirebase.ui.friend.FriendAdapter;
import com.kas.authenticationwithfirebase.ui.friend.FriendViewModel;
import com.kas.authenticationwithfirebase.ui.message.MessageActivity;
import com.kas.authenticationwithfirebase.ui.userProfile.UserProfileActivity;
import com.kas.authenticationwithfirebase.utility.Resource;

import java.util.List;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainFragment extends Fragment {

    private ChatRoomViewModel chatRoomViewModel;
    private FriendViewModel friendViewModel;
    private RecyclerView rvChatRooms;
    private RecyclerView rvFriends;
    private SharedPreferences sharedPreferences;
    private ChatRoomAdapter chatRoomAdapter;
    private MainFriendAdapter friendAdapter;

    public MainFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = requireActivity().getSharedPreferences("AppPreferences", MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupDarkMode();
        initializeViewModels();
        setupRecyclerViews(view);
        observeChatRooms();
        observeFriendsList();
        getFCMToken();
    }

    private void setupDarkMode() {
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    private void initializeViewModels() {
        chatRoomViewModel = new ViewModelProvider(this).get(ChatRoomViewModel.class);
        friendViewModel = new ViewModelProvider(this).get(FriendViewModel.class);
    }

    private void setupRecyclerViews(View view) {
        // Setup chat rooms RecyclerView
        chatRoomAdapter = new ChatRoomAdapter();
        rvChatRooms = view.findViewById(R.id.rvChatRooms);
        rvChatRooms.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvChatRooms.setAdapter(chatRoomAdapter);

        chatRoomAdapter.setOnChatRoomClickListener(chatRoom -> {
            Intent intent = new Intent(requireActivity(), MessageActivity.class);
            intent.putExtra("chatRoomId", chatRoom.getChatRoomId());
            intent.putExtra("chatRoomName", chatRoom.getChatRoomName());
            startActivity(intent);
        });

        // Setup friends RecyclerView
        friendAdapter = new MainFriendAdapter();
        rvFriends = view.findViewById(R.id.dropdownLayout); // Ensure this ID matches your layout
        rvFriends.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        rvFriends.setAdapter(friendAdapter);

        friendAdapter.setOnFriendClickListener(friend -> {
            Intent intent = new Intent(requireActivity(), UserProfileActivity.class);
            intent.putExtra("friendId", friend.getUserId());
            startActivity(intent);
        });
    }

    private void observeChatRooms() {
        chatRoomViewModel.getChatRooms().observe(getViewLifecycleOwner(), resource -> {
            if (resource.getStatus() == Resource.Status.SUCCESS) {
                chatRoomAdapter.setChatRooms(resource.getData());
                for (ChatRoom chatRoom : resource.getData()) {
                    chatRoomViewModel.getUnreadMessagesCount(chatRoom.getChatRoomId()).observe(getViewLifecycleOwner(), unreadResource -> {
                        if (unreadResource.getStatus() == Resource.Status.SUCCESS) {
                            chatRoomAdapter.updateUnreadCount(chatRoom.getChatRoomId(), unreadResource.getData());
                        }
                    });
                }
            }
        });
    }

    private void observeFriendsList() {
        friendViewModel.getFriendsList().observe(getViewLifecycleOwner(), resource -> {
            if (resource.getStatus() == Resource.Status.SUCCESS && resource.getData() != null) {
                populateFriendList(resource.getData());
            }
        });
    }

    private void populateFriendList(List<User> friendsList) {
        friendAdapter.setFriendList(friendsList);
        rvFriends.setVisibility(View.VISIBLE); // Optionally show the RecyclerView
    }

    private void getFCMToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String token = task.getResult();
                String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                FirebaseFirestore.getInstance().collection("users")
                        .document(uid)
                        .update("token", token)
                        .addOnSuccessListener(aVoid -> {})
                        .addOnFailureListener(e -> {});
            }
        });
    }
}
