package com.kas.authenticationwithfirebase.ui.userProfile;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.kas.authenticationwithfirebase.R;
import com.kas.authenticationwithfirebase.data.entity.User;
import com.kas.authenticationwithfirebase.ui.auth.AuthViewModel;
import com.kas.authenticationwithfirebase.ui.friend.FriendViewModel;
import com.kas.authenticationwithfirebase.utility.CameraManager;
import com.kas.authenticationwithfirebase.utility.Resource;

import dagger.hilt.android.AndroidEntryPoint;

import android.content.Intent;
import android.provider.MediaStore;
import android.app.AlertDialog;
import android.net.Uri;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;

import android.os.Environment;
import android.widget.Toast;

@AndroidEntryPoint
public class UserProfileActivity extends AppCompatActivity implements CameraManager.CamaraCallBack {
    private UserProfileViewModel userProfileViewModel;
    private AuthViewModel authViewModel;
    private CameraManager cameraManager;
    private ShapeableImageView updateAvatar;
    private ShapeableImageView userAvatar;
    private ShapeableImageView btnAdd;
    private ShapeableImageView chatBox;
    private String userId;
    private Button btnEdit;
    private EditText password;
    private TextView userDisplayEmail;
    private EditText userDisplayEmailEdit;
    private TextView userDisplayUsername;
    private EditText userDisplayUsernameEdit;
    private LinearLayout status;
    private LinearLayout pwdLayout;
    private LinearLayout reEnterPwdLayout;
    //Camera
    private static final int REQUEST_IMAGE_CAPTURE = 2001;
    private static final int REQUEST_IMAGE_PICK = 2;
    private Uri photoURI;
    private String imageUri;
    private String imageName;
    private User updateUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        userId = getIntent().getStringExtra("userId");

        updateAvatar = findViewById(R.id.updateAvatar);
        userAvatar = findViewById(R.id.userAvatar);
        btnEdit = findViewById(R.id.btnEdit);
        btnAdd = findViewById(R.id.btnAdd);
        userDisplayUsername = findViewById(R.id.userDisplayName);
        userDisplayUsernameEdit = findViewById(R.id.userDisplayNameEdit);
        status = findViewById(R.id.status);
        chatBox = findViewById(R.id.chatBox);
        password = findViewById(R.id.password);
        pwdLayout = findViewById(R.id.pwdLayout);
        reEnterPwdLayout = findViewById(R.id.reEnterPwdLayout);

        //viewModel
        userProfileViewModel = new ViewModelProvider(this).get(UserProfileViewModel.class);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        //camera
        cameraManager = new CameraManager(this, this);

        if (userId == null) {
            userProfileViewModel.getUserProfile().observe(this, userResource -> {
                if (userResource != null) {
                    updateUser = userResource.getData();

                    EditText emailEditText = findViewById(R.id.userDisplayNameEdit);
                    TextView username = findViewById(R.id.user_name);

                    emailEditText.setText(userResource.getData().getUsername());
                    username.setText(userResource.getData().getUsername());

                    //Image
                    String imageUrlString = userResource.getData().getProfileImageUrl();
                    Glide.with(this)
                            .load(imageUrlString)
                            .placeholder(R.drawable.default_avatar)
                            .error(R.drawable.default_avatar)
                            .into(userAvatar);
                }
            });
            btnAdd.setVisibility(View.GONE);
            userDisplayUsername.setVisibility(View.GONE);
            userDisplayUsernameEdit.setVisibility(View.VISIBLE);
            status.setVisibility(View.GONE);
            chatBox.setVisibility(View.GONE);

        } else if (userId != null) { //Friend Profile
            userProfileViewModel.getUserProfile(userId).observe(this, userResource -> {
                if (userResource != null) {
                    TextView emailTextView = findViewById(R.id.userDisplayName);
                    TextView statusTextView = findViewById(R.id.userDisplayStatus);
                    TextView username = findViewById(R.id.user_name);

                    emailTextView.setText(userResource.getData().getUsername()); //fix later
                    statusTextView.setText(userResource.getData().getStatus());
                    username.setText(userResource.getData().getUsername());
                }
            });//Hide fields
            pwdLayout.setVisibility(View.GONE);
            reEnterPwdLayout.setVisibility(View.GONE);
            updateAvatar.setVisibility(View.GONE);
            btnEdit.setVisibility(View.GONE);
            userDisplayUsernameEdit.setVisibility(View.GONE);
            userDisplayUsername.setVisibility(View.VISIBLE);
        }

        updateAvatar.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select Image Source")
                    .setItems(new CharSequence[]{"Camera", "Gallery"}, (dialog, which) -> {
                        if (which == 0) {
                            // Open Camera
                            cameraManager.openCameraForPhoto();
                        } else {
                            // Open Gallery
                            openGallery();
                        }
                    })
                    .show();
        });
        btnEdit.setOnClickListener(view -> {
            String username = ((EditText) findViewById(R.id.userDisplayNameEdit)).getText().toString().trim();

            updateUser.setUsername(username);
            userProfileViewModel.updateUserProfile(updateUser);

            String password = ((EditText) findViewById(R.id.password)).getText().toString().trim();
            String reenteredPassword = ((EditText) findViewById(R.id.reEnterPassword)).getText().toString().trim();

            if (!password.equals("")) {
                if (!password.equals(reenteredPassword)) {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }
                authViewModel.changePassword(password);
            }

            uploadAndUpdatePhoto();
            Toast.makeText(this, "Successfully", Toast.LENGTH_SHORT).show();
        });
    }

    private void uploadAndUpdatePhoto() {
        if (photoURI != null) {
            userProfileViewModel.updateUserImageCloud("profile_photo", imageName, photoURI)
                    .observe(this, resource -> {
                        if (resource != null) {
                            if (resource.getStatus() == Resource.Status.SUCCESS) {
                                String downloadUrl = resource.getData();
                                imageUri = downloadUrl;
                                userProfileViewModel.updateUserImage(imageUri);
                            }
                            Toast.makeText(this, "Successfully", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void openGallery() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, REQUEST_IMAGE_PICK);
    }

    @Override
    public void onMediaCaptured(Uri mediaUri, boolean isVideo) {
    }

    @Override
    public void onError(String error) {
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                // Use the Uri from CameraManager to set it as the avatar image
                if (cameraManager.getCurrentMediaUri() != null) {
                    userAvatar.setImageURI(cameraManager.getCurrentMediaUri());
                    photoURI = cameraManager.getCurrentMediaUri();
                    // Generate imageName from the URI
                    imageName = generateImageNameFromUri(photoURI);
                } else {
                    // Handle error
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == REQUEST_IMAGE_PICK) {
                // Image selected from gallery
                Uri selectedImage = data.getData();
                userAvatar.setImageURI(selectedImage);
                photoURI = selectedImage;
                // Generate imageName from the URI
                imageName = generateImageNameFromUri(photoURI);
            }
        }
    }

    private String generateImageNameFromUri(Uri uri) {
        // Extract the last segment from the URI
        String lastPathSegment = uri.getLastPathSegment();
        if (lastPathSegment != null) {
            return "IMG_" + lastPathSegment; // Prefix with IMG_
        }
        return "IMG_undefined"; // Fallback name if URI does not have a last segment
    }
}
