package com.kas.authenticationwithfirebase.ui.splash;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kas.authenticationwithfirebase.R;
import com.kas.authenticationwithfirebase.ui.auth.AuthViewModel;
import com.kas.authenticationwithfirebase.ui.friend.FriendActivity;
import com.kas.authenticationwithfirebase.ui.friend.FriendAdapter;
import com.kas.authenticationwithfirebase.ui.friend.FriendViewModel;
import com.kas.authenticationwithfirebase.ui.login.LoginActivity;
import com.kas.authenticationwithfirebase.ui.main.MainActivity;
import com.kas.authenticationwithfirebase.ui.message.MessageActivity;
import com.kas.authenticationwithfirebase.utility.Resource;

import dagger.hilt.android.AndroidEntryPoint;


@AndroidEntryPoint
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        FriendAdapter friendAdapter = new FriendAdapter();
        FriendViewModel friendViewModel = new ViewModelProvider(this).get(FriendViewModel.class); ;
        AuthViewModel authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        if(authViewModel.isUserLoggedIn() && getIntent().getExtras()!=null){
            //from notification
            String userId = getIntent().getExtras().getString("userId");
            FirebaseFirestore.getInstance().collection("users").document(userId).get()
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){

                            Intent mainIntent = new Intent(this,MainActivity.class);
                            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivity(mainIntent);

                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                friendAdapter.setOnFriendClickListener(friend -> {
                                    friendViewModel.createChatRoom(friend.getUserId()).observe(this, resource -> {
                                        if (resource.getStatus() == Resource.Status.SUCCESS) {
                                            // If chat room is created, navigate to MessageActivity
                                            Intent intent = new Intent(this, MessageActivity.class);
                                            intent.putExtra("chatRoomId", resource.getData().getChatRoomId());
                                            intent.putExtra("chatRoomName", resource.getData().getChatRoomName());
                                            startActivity(intent);
                                            finish();
                                        } else if (resource.getStatus() == Resource.Status.ERROR) {
                                            // Handle error if chat room creation fails
                                        } else if (resource.getStatus() == Resource.Status.LOADING) {
                                            // Show loading indication if needed
                                        }
                                    });
                                });
                            } else {
                                // Handle case if user document does not exist
                                Log.d("MainActivity", "User document does not exist.");
                            }
                        } else {
                            // Handle Firestore error
                            Log.d("MainActivity", "Failed to retrieve user data", task.getException());
                        }
                    });

        }else{
            //Get the AuthViewModel instance
            //AuthViewModel authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

            new Handler().postDelayed(() -> {
                //Check if the user is logged in
                if (authViewModel.isUserLoggedIn()) {
                    //If the user is logged in, navigate to the MainActivity
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                } else {
                    //If the user is not logged in, navigate to the LoginActivity
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                }
                //Finish the current activity
                finish();
            }, 2000);
        }

    }
}