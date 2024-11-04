package com.kas.authenticationwithfirebase.ui.message;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kas.authenticationwithfirebase.R;
import com.kas.authenticationwithfirebase.data.entity.Message;
import com.kas.authenticationwithfirebase.data.entity.User;
import com.kas.authenticationwithfirebase.utility.CameraManager;
import com.kas.authenticationwithfirebase.utility.FileManager;
import com.kas.authenticationwithfirebase.utility.RequestCodeManager;
import com.kas.authenticationwithfirebase.data.model.MessageWithUserDetail;
import com.kas.authenticationwithfirebase.utility.Resource;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@AndroidEntryPoint
public class MessageActivity extends AppCompatActivity implements CameraManager.CamaraCallBack, FileManager.FilePickerCallback {

    private MessageViewModel messageViewModel;
    private MessageAdapter messageAdapter;
    private RecyclerView recyclerView;
    private TextInputEditText messageInput;
    private ImageButton sendButton, backButton, extraIcon;
    private List<MessageWithUserDetail> messages = new ArrayList<>();
    private String chatRoomId;
    private Observer<Resource<List<MessageWithUserDetail>>> messagesObserver;

    private CameraManager cameraManager;
    private FileManager fileManager;
    private TextView chatName;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Log.d("MessageActivity", "Activity created");

        // Initialize UI components
        chatRoomId = getIntent().getStringExtra("chatRoomId");
        recyclerView = findViewById(R.id.recycler_view_messages);
        chatName = findViewById(R.id.chat_name);
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);
        backButton = findViewById(R.id.backButton);
        extraIcon = findViewById(R.id.btnExtra);

        // Set up event listeners
        backButton.setOnClickListener(v -> finish());
        extraIcon.setOnClickListener(this::showPopupMenu);

        // Initialize CameraManager and FileManager
        cameraManager = new CameraManager(this, this);
        fileManager = new FileManager(this, this);

        // Retrieve and apply text size from SharedPreferences
        sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        float textSize = sharedPreferences.getFloat("text_size", 16.0f); // Default size is 16.0f if not set

        // ViewModel and Adapter Setup
        messageViewModel = new ViewModelProvider(this).get(MessageViewModel.class);
        messageAdapter = new MessageAdapter(messages, messageViewModel.getCurrentUserId(),textSize);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageAdapter);

        // Get Chat Room Name
        String chatRoomNameText = getIntent().getStringExtra("chatRoomName");
        chatName.setText(chatRoomNameText);

        // Initialize the observer for messages
        setupMessagesObserver();


        Log.d("MessageActivity", "Chat Room ID: " + chatRoomId);
        Log.d("MessageActivity", "Chat Name: " + chatRoomNameText);

        // Send Message
        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void setupMessagesObserver() {
        messagesObserver = resource -> {
            if (resource.getStatus() == Resource.Status.SUCCESS) {
                messages.clear();
                messages.addAll(resource.getData());
                messageAdapter.notifyDataSetChanged();
                if (!messages.isEmpty()) {
                    // Scroll to the last message
                    recyclerView.smoothScrollToPosition(messages.size() - 1);
                }
                markMessagesAsRead();
            } else if (resource.getStatus() == Resource.Status.ERROR) {
                Toast.makeText(this, "Failed to load messages: " + resource.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void markMessagesAsRead() {
        for (MessageWithUserDetail message : messages) {
            messageViewModel.markMessageAsRead(chatRoomId, message.getMessageId());
        }
    }

    private void sendMessage() {
        String messageContent = messageInput.getText().toString();
        Log.d("MessageActivity", "Sending message: " + messageContent);
        if (!messageContent.isEmpty()) {
            Message message = new Message(
                    null,
                    chatRoomId,
                    messageViewModel.getCurrentUserId(),
                    messageContent,
                    "text",
                    System.currentTimeMillis(),
                    null);
//            messageViewModel.sendTextMessage(chatRoomId, message);
//            messageInput.setText("");

            // Observe the result of sending the message
            messageViewModel.sendTextMessage(chatRoomId, message).observe(this, resource -> {
                if (resource.getStatus() == Resource.Status.SUCCESS) {
                    Log.d("MessageActivity", "Message sent successfully, triggering notification");
                    // Message was sent successfully, trigger the notification
                    sendNotification(messageContent);
                    messageInput.setText(""); // Clear the input field
                } else if (resource.getStatus() == Resource.Status.ERROR) {
                    // Show an error message to the user if the message send fails
                    Toast.makeText(this, "Failed to send message: " + resource.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    void sendNotification(String message){
        Log.d("MessageActivity", "Preparing to send notification for message: " + message);

        String t = "cyICIKhsSQStFnga7Co1HR:APA91bG-jZl0ZPN-fq_n5sMIcbBXqx0-fKea1gvCgJp3kDIPw-pr_cRmbFnwwJKmCpoyEWd-diFOzPPRpfI3PuvNWr2Tbdz45solxYMnbgZ7ot_iTg0s54k";
        FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getUid())
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        User currentUser = task.getResult().toObject(User.class);
                        Log.d("MessageActivity", "Current user: " + currentUser.getUsername());
                        // Retrieve the FCM token of the other user in the room
//                        fetchOtherUserFcmToken(chatRoomId, token -> {
//                            try {
//                                JSONObject jsonObject = new JSONObject();
//
//                                JSONObject notificationObj = new JSONObject();
//                                notificationObj.put("title", currentUser.getUsername());
//                                notificationObj.put("body", message);
//
//                                JSONObject dataObj = new JSONObject();
//                                dataObj.put("userId", currentUser.getUserId());
//                                dataObj.put("chatRoomId", chatRoomId);
//
//                                jsonObject.put("notification", notificationObj);
//                                jsonObject.put("data", dataObj);
//                                jsonObject.put("to", token); // Use the fetched token
//
//                                callApi(jsonObject);
//
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        });
                        Log.d("MessageActivity", "Using recipient token: " + t);
                        try {
                            JSONObject jsonObject = new JSONObject();

                            JSONObject notificationObj = new JSONObject();
                            notificationObj.put("title", currentUser.getUsername());
                            notificationObj.put("body", message);

                            JSONObject dataObj = new JSONObject();
                            dataObj.put("userId", currentUser.getUserId());

                            jsonObject.put("notification", notificationObj);
                            jsonObject.put("data", dataObj);
                            jsonObject.put("to", t);   //token nguoi nhan o day

                            callApi(jsonObject);
                            Log.d("MessageActivity", "Notification JSON: " + jsonObject.toString());

                        } catch (Exception e) {
                            Log.e("MessageActivity", "Error creating JSON for notification", e);
                        }
                    }else{
                        Log.e("MessageActivity", "Failed to retrieve current user data");
                    }
                });
    }

//    private void fetchOtherUserFcmToken(String chatRoomId, Callback<String> callback)
//    {
//        FirebaseUtil.getChatRoomParticipants(chatRoomId).addOnCompleteListener(task -> {
//            if (task.isSuccessful() && task.getResult() != null) {
//                for (UserModel user : task.getResult().toObjects(UserModel.class)) {
//                    if (!user.getUserId().equals(currentUserId)) {
//                        callback.onSuccess(user.getFcmToken());
//                        return;
//                    }
//                }
//            }
//            callback.onFailure("Failed to retrieve recipient FCM token.");
//        });
//    }

    void callApi(JSONObject jsonObject){
        String serverKey = "BCcunojuEgTx_jOQAkLYO-NHfwygRkmA0S1pOrdRzRb6ohJxwRTdKumX5Kvy7t5wR2BIfX5MFPGxTU6EgMOoD4U";
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();
        String url = "https://fcm.googleapis.com/fcm/send";
        RequestBody body = RequestBody.create(jsonObject.toString(),JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                //.header("Authorization","Bearer "+serverKey)
                .header("Authorization", "key=" +serverKey)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("MessageActivity", "API call failed", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d("MessageActivity", "Notification sent successfully: " + response.body().string());
                } else {
                    Log.e("MessageActivity", "Failed to send notification: " + response.message());
                    Log.e("MessageActivity", "Response body: " + response.body().string());
                }
            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();
        // Observe messages when the activity is resumed
        messageViewModel.observeMessages(chatRoomId).observe(this, messagesObserver);
    }

    @Override
    protected void onPause() {
        super.onPause();
        messageViewModel.removeMessagesListener(chatRoomId);
    }

    @SuppressLint("RestrictedApi")
    private void showPopupMenu(View view) {
        MenuBuilder menuBuilder = new MenuBuilder(this);
        getMenuInflater().inflate(R.menu.chat_extra, menuBuilder);
        MenuPopupHelper menuPopupHelper = new MenuPopupHelper(this, menuBuilder, view);
        menuPopupHelper.setForceShowIcon(true);
        menuBuilder.setCallback(new MenuBuilder.Callback() {
            @Override
            public boolean onMenuItemSelected(@NonNull MenuBuilder menu, @NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.btnCamera) {
                    cameraManager.openCameraForPhoto();
                    return true;
                } else if (itemId == R.id.btnAttachment) {
                    fileManager.pickImageAndVideo();
                    return true;
                }
                return false;
            }

            @Override
            public void onMenuModeChange(MenuBuilder menu) { }
        });
        menuPopupHelper.show();
    }

    @Override
    public void onMediaCaptured(Uri mediaUri, boolean isVideo) {
        String messageType = isVideo ? "video" : "image";
        sendMediaMessage(mediaUri, messageType);
    }

    private void sendMediaMessage(Uri mediaUri, String messageType) {
        if (mediaUri != null) {
            Message message = new Message(
                    null,
                    chatRoomId,
                    messageViewModel.getCurrentUserId(),
                    mediaUri.toString(),
                    messageType,
                    System.currentTimeMillis(),
                    null
            );
            messageViewModel.sendImageMessage(chatRoomId, mediaUri, message);
            Toast.makeText(this, messageType + " sent!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to send " + messageType, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onError(String error) {
        Toast.makeText(this, "Error: " + error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == RequestCodeManager.REQUEST_CODE_IMAGE_CAPTURE) {
                this.onMediaCaptured(cameraManager.getCurrentMediaUri(), false);
            } else if (requestCode == RequestCodeManager.REQUEST_CODE_VIDEO_CAPTURE) {
                this.onMediaCaptured(cameraManager.getCurrentMediaUri(), true);
            }
        } else {
            this.onError("Media capture failed or canceled.");
        }
    }

    @Override
    public void onFilePicked(Uri uri) {
        sendMediaMessage(uri, "image");
    }

    @Override
    public void onNoFileSelected() {
        Toast.makeText(this, "No file selected.", Toast.LENGTH_SHORT).show();
    }
}
