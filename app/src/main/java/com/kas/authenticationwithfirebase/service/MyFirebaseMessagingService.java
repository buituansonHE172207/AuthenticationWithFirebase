package com.kas.authenticationwithfirebase.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
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
//    private static final String CHANNEL_ID = "message_notification";
//    private static final int REQUEST_NOTIFICATION_PERMISSION = 1;
//    @Override
//    public void onMessageReceived(RemoteMessage remoteMessage) {
//        createNotificationChannel();
//        // Parse message and display it in the notification tray or in-app
//        String title = remoteMessage.getNotification().getTitle();
//        String message = remoteMessage.getNotification().getBody();
//        displayNotification(title, message);
//        Log.d("onMessageReceived", "Message received: " + remoteMessage.getData());
//    }
//
//
//    private void displayNotification(String title, String message) {
////        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "YOUR_CHANNEL_ID")
//////                .setSmallIcon()
////                .setContentTitle(title)
////                .setContentText(message)
////                .setPriority(NotificationCompat.PRIORITY_HIGH);
////        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
////        manager.notify(0, builder.build());
////        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
////
//        // Kiểm tra quyền thông báo trước khi hiển thị
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
//                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
//
//            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
//                    .setSmallIcon(R.drawable.message_icon3)
//                    .setContentTitle(title)
//                    .setContentText(message)
//                    .setPriority(NotificationCompat.PRIORITY_HIGH);
//
//            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
//            notificationManager.notify(1, builder.build());
//        } else {
//            Log.d("displayNotification", "Permission for notifications not granted.");
//        }
//
//    }
//
////    @Override
////    public void onNewToken(@NonNull String token) {
////        // Update token in Firestore for push notifications
////        FirebaseFirestore.getInstance().collection("users")
////                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
////                .update("token", token)
////                .addOnSuccessListener(aVoid -> Log.d("FCM", "Token updated successfully"))
////                .addOnFailureListener(e -> Log.e("FCM", "Failed to update token", e));
////    }
@Override
public void onNewToken(@NonNull String token) {
    // Check if the user is logged in
    String userId = FirebaseAuth.getInstance().getUid();

    if (userId != null) {
        // Update token in Firestore for push notifications if user is logged in
        FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .update("token", token)
                .addOnSuccessListener(aVoid -> Log.d("FCM", "Token updated successfully"))
                .addOnFailureListener(e -> Log.e("FCM", "Failed to update token", e));
    } else {
        Log.d("FCM", "User not logged in, token not updated");
    }
}
//
//
//    private void createNotificationChannel() {
//        // Kiểm tra nếu thiết bị chạy Android Oreo trở lên
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            CharSequence name = "New Messages";
//            String description = "Notifications for new messages";
//            int importance = NotificationManager.IMPORTANCE_HIGH;
//
//            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
//            channel.setDescription(description);
//
//            // Lấy NotificationManager từ context hiện tại
//            NotificationManager notificationManager = getSystemService(NotificationManager.class);
//            if (notificationManager != null) {
//                notificationManager.createNotificationChannel(channel);
//            } else {
//                Log.d("createNotificationChannel", "NotificationManager is null");
//            }
//        }
//        else{
//            Log.d("createNotificationChannel","Build.VERSION.SDK_INT < Build.VERSION_CODES.O");
//        }
//    }

}