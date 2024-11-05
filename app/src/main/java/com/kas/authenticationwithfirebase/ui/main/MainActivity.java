package com.kas.authenticationwithfirebase.ui.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.kas.authenticationwithfirebase.AuthenticationWithFirebase;
import com.kas.authenticationwithfirebase.R;
import com.kas.authenticationwithfirebase.data.entity.ChatRoom;
import com.kas.authenticationwithfirebase.data.entity.User;
import com.kas.authenticationwithfirebase.ui.auth.AuthViewModel;
import com.kas.authenticationwithfirebase.ui.chatRoom.ChatRoomAdapter;
import com.kas.authenticationwithfirebase.ui.chatRoom.ChatRoomViewModel;
import com.kas.authenticationwithfirebase.ui.friend.FriendActivity;
import com.kas.authenticationwithfirebase.ui.friend.FriendAdapter;
import com.kas.authenticationwithfirebase.ui.friend.FriendFragment;
import com.kas.authenticationwithfirebase.ui.friend.FriendViewModel;
import com.kas.authenticationwithfirebase.ui.login.LoginActivity;
import com.kas.authenticationwithfirebase.ui.message.MessageActivity;
import com.kas.authenticationwithfirebase.ui.settings.SettingsActivity;
import com.kas.authenticationwithfirebase.ui.userProfile.UserProfileActivity;
import com.kas.authenticationwithfirebase.ui.userProfile.UserProfileViewModel;
import com.kas.authenticationwithfirebase.utility.Resource;

import java.util.List;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private ImageButton profileIcon;
    private AuthViewModel authViewModel;
    private UserProfileViewModel userViewModel;
    private SharedPreferences sharedPreferences;

    private static final int REQUEST_NOTIFICATION_PERMISSION = 1001;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);

        // Load current theme setting
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Kiểm tra và yêu cầu quyền thông báo nếu cần thiết
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION_PERMISSION);
            }
        }

        profileIcon = findViewById(R.id.profile_icon);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        userViewModel = new ViewModelProvider(this).get(UserProfileViewModel.class);
        userViewModel.getUserProfile().observe(this, resource -> {
            if (resource.getStatus() == Resource.Status.SUCCESS) {
                // Set the profile image directly
                String profileImageUrl = resource.getData().getProfileImageUrl();
                Glide.with(this)
                        .load(profileImageUrl)
                        .placeholder(R.drawable.default_avatar)
                        .into(profileIcon);
            } else if (resource.getStatus() == Resource.Status.ERROR) {
                // Handle error
            } else if (resource.getStatus() == Resource.Status.LOADING) {
                // Show loading
            }
        });

        // Initialize Bottom Navigation View
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        // Set default highlighted item in Bottom Navigation
        bottomNavigationView.setSelectedItemId(R.id.message);

        // Setup bottom navigation listener
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.profile) {
                //chatRoomViewModel.createChatRoom("81gHkQDfPbaifFqw4wz7HBweL8O2");

                Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
                startActivity(intent);
                return true;
            } else if (item.getItemId() == R.id.message) {
                displayFragment(new MainFragment());
                return true;
            } else if (item.getItemId() == R.id.contact) {
                displayFragment(new FriendFragment());
                /*
                // Open friends activity
                Intent intent = new Intent(MainActivity.this, FriendActivity.class);
                startActivity(intent);
                */
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

        getFCMToken();
        // Display MainFragment

        if (savedInstanceState == null) {
            displayFragment(new MainFragment());
        }
    }

    private void getFCMToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                String token = task.getResult();
                Log.d("token", token);
                String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                Log.d("uid",uid);
                FirebaseFirestore.getInstance().collection("users")
                        .document(uid)
                        .update("token",token)
                        .addOnSuccessListener(aVoid -> Log.d("TokenUpdate", "Token updated successfully in Firestore"))
                        .addOnFailureListener(e -> Log.d("TokenUpdate", "Failed to update token", e));

            }
        });
    }

    @SuppressLint("RestrictedApi")

    private void showPopupMenu(View view) {
        // Create a MenuBuilder instance
        MenuBuilder menuBuilder = new MenuBuilder(this);
        // Inflate your menu resource into the MenuBuilder
        getMenuInflater().inflate(R.menu.profile_menu, menuBuilder);

        // Create a MenuPopupHelper to display the menu
        MenuPopupHelper menuPopupHelper = new MenuPopupHelper(this, menuBuilder, view);
        menuPopupHelper.setForceShowIcon(true);  // Force icons to be shown

        // Set a callback to handle menu item clicks
        menuBuilder.setCallback(new MenuBuilder.Callback() {
            @Override
            public boolean onMenuItemSelected(@NonNull MenuBuilder menu, @NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.btnLogout) {
                    FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                authViewModel.logoutUser();
                                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });
//                    authViewModel.logoutUser();
//                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                    startActivity(intent);
//                    finish();
                    return true;
                } else if (itemId == R.id.btnSettings) {
                    Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.btnProfile) {
                    Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
                    startActivity(intent);
                    return true;
                }
                return false;
            }

            @Override
            public void onMenuModeChange(MenuBuilder menu) {
                // No need to implement
            }
        });

        // Show the popup menu
        menuPopupHelper.show();
    }
    private void displayFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }
}