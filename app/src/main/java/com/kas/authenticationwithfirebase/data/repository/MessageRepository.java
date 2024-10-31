package com.kas.authenticationwithfirebase.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kas.authenticationwithfirebase.data.entity.Message;
import com.kas.authenticationwithfirebase.utility.Resource;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class MessageRepository {
    private final DatabaseReference messagesRef;
    private ValueEventListener messagesListener;

    @Inject
    public MessageRepository(FirebaseDatabase firebaseDatabase) {
        this.messagesRef = firebaseDatabase.getReference("messages");
    }


    // Send a new message
    public LiveData<Resource<Message>> sendMessage(String chatRoomId, Message message) {
        MutableLiveData<Resource<Message>> result = new MutableLiveData<>();

        String messageId = messagesRef.child(chatRoomId).push().getKey();
        message.setMessageId(messageId);

        if (messageId == null) {
            result.setValue(Resource.error("Failed to send message", null));
            return result;
        }
        messagesRef.child(chatRoomId).child(messageId).setValue(message)
                .addOnSuccessListener(aVoid -> result.setValue(Resource.success(message)))
                .addOnFailureListener(e -> result.setValue(Resource.error(e.getMessage(), null)));

        return result;
    }


    // Observe messages in a chat room and update unread counts
    public LiveData<Resource<List<Message>>> observeMessages(String chatRoomId, String userId) {
        MutableLiveData<Resource<List<Message>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        messagesListener = messagesRef.child(chatRoomId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Message> messages = new ArrayList<>();
                int unreadCount = 0;

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Message message = dataSnapshot.getValue(Message.class);
                    if (message != null) {
                        messages.add(message);

                        // Check if message is unread by the current user
                        if (message.getReadBy() == null || !message.getReadBy().contains(userId)) {
                            unreadCount++;
                        }
                    }
                }

                // Update unread count in the chat room collection
                updateUnreadCountInChatRoom(chatRoomId, userId, unreadCount);

                result.setValue(Resource.success(messages));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                result.setValue(Resource.error(error.getMessage(), null));
            }
        });

        return result;
    }

    private void updateUnreadCountInChatRoom(String chatRoomId, String userId, int unreadCount) {
        DatabaseReference chatRoomRef = FirebaseDatabase.getInstance().getReference("chatRooms").child(chatRoomId);

        // Set the unread count specifically for this user in the map
        chatRoomRef.child("unreadCounts").child(userId).setValue(unreadCount)
                .addOnSuccessListener(aVoid -> Log.d("MessageRepository", "Unread count updated successfully"))
                .addOnFailureListener(e -> Log.e("MessageRepository", "Failed to update unread count: " + e.getMessage()));
    }


    // Mark message as read and update unread count
    public LiveData<Resource<Boolean>> markMessageAsRead(String chatRoomId, String messageId, String userId) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(false));

        messagesRef.child(chatRoomId).child(messageId).child("readBy").get().addOnSuccessListener(snapshot -> {
            List<String> readBy = (List<String>) snapshot.getValue();
            if (readBy == null) {
                readBy = new ArrayList<>();
            }

            if (!readBy.contains(userId)) {
                readBy.add(userId);
                messagesRef.child(chatRoomId).child(messageId).child("readBy").setValue(readBy)
                        .addOnSuccessListener(aVoid -> {
                            // After marking as read, update unread count
                            updateUnreadCountAfterRead(chatRoomId, userId);
                            result.setValue(Resource.success(true));
                        })
                        .addOnFailureListener(e -> result.setValue(Resource.error(e.getMessage(), false)));
            } else {
                result.setValue(Resource.success(false));  // User has already read the message
            }
        }).addOnFailureListener(e -> result.setValue(Resource.error(e.getMessage(), false)));

        return result;
    }

    private void updateUnreadCountAfterRead(String chatRoomId, String userId) {
        messagesRef.child(chatRoomId).orderByChild("readBy/" + userId).equalTo(null)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int unreadCount = (int) snapshot.getChildrenCount();
                        updateUnreadCountInChatRoom(chatRoomId, userId, unreadCount);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("MessageRepository", "Failed to fetch unread messages count: " + error.getMessage());
                    }
                });
    }


    // Delete message
    public LiveData<Resource<Boolean>> deleteMessage(String chatRoomId, String messageId, String userId) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(false));

        DatabaseReference messageRef = messagesRef.child(chatRoomId).child(messageId);

        if (userId == null) {
            messageRef.removeValue()
                    .addOnSuccessListener(aVoid -> result.setValue(Resource.success(true)))
                    .addOnFailureListener(e -> result.setValue(Resource.error(e.getMessage(), false)));
        } else {
            messageRef.child("deletedForUsers").child(userId).setValue(true)
                    .addOnSuccessListener(aVoid -> result.setValue(Resource.success(true)))
                    .addOnFailureListener(e -> result.setValue(Resource.error(e.getMessage(), false)));
        }

        return result;
    }

    // Edit message
    public LiveData<Resource<Boolean>> editMessage(String chatRoomId, String messageId, String newContent) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(false));

        messagesRef.child(chatRoomId).child(messageId).child("messageContent").setValue(newContent)
                .addOnSuccessListener(aVoid -> result.setValue(Resource.success(true)))
                .addOnFailureListener(e -> result.setValue(Resource.error(e.getMessage(), false)));

        return result;
    }

    // Remove message listener
    public void removeMessageListener(String chatRoomId) {
        if (messagesListener != null) {
            messagesRef.child(chatRoomId).removeEventListener(messagesListener);
        }
    }
}