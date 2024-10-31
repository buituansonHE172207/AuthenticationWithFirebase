package com.kas.authenticationwithfirebase.data.repository;


import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kas.authenticationwithfirebase.data.entity.Notification;
import com.kas.authenticationwithfirebase.utility.Resource;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class NotificationRepository {
    private final DatabaseReference databaseReference;

    @Inject
    public NotificationRepository(FirebaseDatabase firebaseDatabase) {
        this.databaseReference = firebaseDatabase.getReference("notifications");
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

}
