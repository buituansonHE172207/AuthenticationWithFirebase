package com.kas.authenticationwithfirebase.service;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.kas.authenticationwithfirebase.R;

import java.util.Objects;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "message_notification";
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        createNotificationChannel();

        // Lấy tiêu đề và nội dung từ tin nhắn
        String title = remoteMessage.getNotification() != null ? remoteMessage.getNotification().getTitle() : "New Message";
        String message = remoteMessage.getNotification() != null ? remoteMessage.getNotification().getBody() : "You have a new message";

        displayNotification(title, message);
        Log.d("onMessageReceived", "Message received: " + remoteMessage.getData());
    }

    private void displayNotification(String title, String message) {
        // Kiểm tra quyền trước khi hiển thị thông báo
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.message_icon3)  // Thay thế bằng icon của bạn
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        } else {
            Log.d("displayNotification", "Permission for notifications not granted.");
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        // Kiểm tra xem người dùng có đang đăng nhập không
        String userId = FirebaseAuth.getInstance().getUid();

        if (userId != null) {
            // Cập nhật token trong Firestore cho thông báo đẩy
            FirebaseFirestore.getInstance().collection("users")
                    .document(userId)
                    .update("token", token)
                    .addOnSuccessListener(aVoid -> Log.d("FCM", "Token updated successfully"))
                    .addOnFailureListener(e -> Log.e("FCM", "Failed to update token", e));
        } else {
            Log.d("FCM", "User not logged in, token not updated");
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "New Messages";
            String description = "Notifications for new messages";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            } else {
                Log.d("createNotificationChannel", "NotificationManager is null");
            }
        } else {
            Log.d("createNotificationChannel", "Build.VERSION.SDK_INT < Build.VERSION_CODES.O");
        }
    }
}
