package com.kas.authenticationwithfirebase.data.repository;


import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kas.authenticationwithfirebase.R;
import com.kas.authenticationwithfirebase.data.entity.Notification;
import com.kas.authenticationwithfirebase.data.model.NotificationBody;
import com.kas.authenticationwithfirebase.data.model.SendMessageDto;
import com.kas.authenticationwithfirebase.service.FcmApi;
import com.kas.authenticationwithfirebase.utility.Resource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationRepository {
    private final DatabaseReference databaseReference;
    private final FcmApi fcmApi ;
    private final String projectId = "kas1407";
    private static final String MESSAGING_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
    private final Context context;

    @Inject
    public NotificationRepository(FirebaseDatabase firebaseDatabase, FcmApi fcmApi,@ApplicationContext Context context) {
        this.databaseReference = firebaseDatabase.getReference("notifications");
        this.fcmApi = fcmApi;
        this.context = context;
    }

    public LiveData<Resource<Void>> addNotification(Notification notification) {
        MutableLiveData<Resource<Void>> resultLiveData = new MutableLiveData<>();
        resultLiveData.setValue(Resource.loading(null)); // Set initial loading state

        // Generate a unique ID for the notification
        String notificationId = databaseReference.push().getKey();
        if (notificationId != null) {
            notification.setNotificationId(notificationId);
            databaseReference.child(notificationId).setValue(notification)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("NotificationRepo", "Notification added successfully");
                        resultLiveData.setValue(Resource.success(null)); // Set success state
                    })
                    .addOnFailureListener(e -> {
                        Log.e("NotificationRepo", "Failed to add notification", e);
                        resultLiveData.setValue(Resource.error("Failed to add notification: " + e.getMessage(), null)); // Set error state
                    });
        } else {
            Log.e("NotificationRepo", "Failed to generate unique ID for notification");
            resultLiveData.setValue(Resource.error("Failed to generate unique ID for notification", null)); // Set error state
        }

        return resultLiveData; // Return the LiveData resource
    }

    public LiveData<Resource<List<Notification>>> getUserNotifications(String userId) {
        MutableLiveData<Resource<List<Notification>>> notificationsLiveData = new MutableLiveData<>();
        notificationsLiveData.setValue(Resource.loading(null)); // Set initial loading state

        databaseReference.orderByChild("recipientId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<Notification> notifications = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Notification notification = snapshot.getValue(Notification.class);
                            if (notification != null) {
                                notifications.add(notification);
                            }
                        }
                        notificationsLiveData.setValue(Resource.success(notifications)); // Set success state with data
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        notificationsLiveData.setValue(Resource.error("Failed to fetch notifications: " + databaseError.getMessage(), null));
                        Log.e("NotificationRepo", "Failed to fetch notifications: " + databaseError.getMessage());
                    }
                });

        return notificationsLiveData;
    }

    public LiveData<Resource<Boolean>> hasUserNotifications(String userId) {
        MutableLiveData<Resource<Boolean>> hasNotificationsLiveData = new MutableLiveData<>();
        hasNotificationsLiveData.setValue(Resource.loading(null)); // Set initial loading state

        databaseReference.orderByChild("recipientId").equalTo(userId)
                .limitToFirst(1) // Only check for at least one notification
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boolean hasNotifications = dataSnapshot.exists();
                        hasNotificationsLiveData.setValue(Resource.success(hasNotifications)); // Set success state with data
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        hasNotificationsLiveData.setValue(Resource.error("Failed to check notifications: " + databaseError.getMessage(), null));
                        Log.e("NotificationRepo", "Failed to check notifications: " + databaseError.getMessage());
                    }
                });

        return hasNotificationsLiveData;
    }

    private String getAccessToken() throws IOException {
        try (InputStream serviceAccountStream = context.getResources().openRawResource(R.raw.clould_service_account)) {
            GoogleCredentials googleCredentials = GoogleCredentials
                    .fromStream(serviceAccountStream)
                    .createScoped(Arrays.asList(MESSAGING_SCOPE));
            googleCredentials.refresh();
            return googleCredentials.getAccessToken().getTokenValue();
        }
    }

    // Phương thức gửi notification qua FCM
    public void sendNotification(String recipientToken, NotificationBody notificationBody) {
        new Thread(() -> {
            String token;
            try {
                token = getAccessToken();
            } catch (IOException e) {
                Log.e("NotificationRepository", "Failed to get access token: " + e.getMessage());
                return;
            }

            String authToken = "Bearer " + token;
            SendMessageDto sendMessageDto = new SendMessageDto(recipientToken, notificationBody);

            fcmApi.sendMessage(authToken, projectId, sendMessageDto).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Log.d("NotificationRepository", "Notification sent successfully!");
                    } else {
                        try {
                            String errorResponse = response.errorBody().string();
                            Log.e("NotificationRepository", "Failed to send notification: " + errorResponse);
                        } catch (IOException e) {
                            Log.e("NotificationRepository", "Failed to read error response: " + e.getMessage());
                        }
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e("NotificationRepository", "Error sending notification: " + t.getMessage());
                }
            });
        }).start();
    }

}
