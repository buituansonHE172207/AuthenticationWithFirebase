package com.kas.authenticationwithfirebase.ui.userProfile;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.imageview.ShapeableImageView;
import com.kas.authenticationwithfirebase.R;
import com.kas.authenticationwithfirebase.ui.friend.FriendViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class UserProfileActivity extends AppCompatActivity {
    private UserProfileViewModel userProfileViewModel;
    private ShapeableImageView updateAvatar;
    private ShapeableImageView btnAdd;
    private String userId;
    private Button btnEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This will close the current activity and return to the previous one
                finish();
            }
        });

        updateAvatar = findViewById(R.id.updateAvatar);
        btnEdit = findViewById(R.id.btnEdit);
        btnAdd = findViewById(R.id.btnAdd);

        //viewModel
        userProfileViewModel = new ViewModelProvider(this).get(UserProfileViewModel.class);

        if (userId == null) {
            userProfileViewModel.getUserProfile().observe(this, userResource -> {
                if (userResource != null) {
                    // Lấy các TextView
//                    TextView usernameTextView = findViewById(R.id.userDisplayName);
                    TextView emailTextView = findViewById(R.id.userDisplayEmail);
                    TextView statusTextView = findViewById(R.id.userDisplayStatus);
                    TextView username = findViewById(R.id.user_name);
//                    TextView email = findViewById(R.id.user_name);

//                    usernameTextView.setText(userResource.getData().getUsername());
                    emailTextView.setText(userResource.getData().getUsername()); //fix later
                    statusTextView.setText(userResource.getData().getStatus());
                    username.setText(userResource.getData().getUsername());
//                    email.setText(userResource.getData().getEmail());
                }
            });
            btnAdd.setVisibility(View.GONE);
        } else if (userId != null) {
            userProfileViewModel.getUserProfile(userId).observe(this, userResource -> {
                if (userResource != null) {
                    // Lấy các TextView
//                    TextView usernameTextView = findViewById(R.id.userDisplayName);
                    TextView emailTextView = findViewById(R.id.userDisplayEmail);
                    TextView statusTextView = findViewById(R.id.userDisplayStatus);
                    TextView username = findViewById(R.id.user_name);
//                    TextView email = findViewById(R.id.user_name);

//                    usernameTextView.setText(userResource.getData().getUsername());
                    emailTextView.setText(userResource.getData().getUsername()); //fix later
                    statusTextView.setText(userResource.getData().getStatus());
                    username.setText(userResource.getData().getUsername());
//                    email.setText(userResource.getData().getEmail());
                }
                updateAvatar.setVisibility(View.GONE);
                btnEdit.setVisibility(View.GONE);
            });
        }
    }

}