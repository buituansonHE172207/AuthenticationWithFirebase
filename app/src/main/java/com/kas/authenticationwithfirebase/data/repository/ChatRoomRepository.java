package com.kas.authenticationwithfirebase.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kas.authenticationwithfirebase.data.model.ChatRoom;
import com.kas.authenticationwithfirebase.data.model.Message;
import com.kas.authenticationwithfirebase.utility.Resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class ChatRoomRepository {
    private final DatabaseReference chatRoomsRef;
    private ValueEventListener chatRoomsListener;

    @Inject
    public ChatRoomRepository(FirebaseDatabase firebaseDatabase, FirebaseAuth firebaseAuth) {

        this.chatRoomsRef = firebaseDatabase.getReference("chatRooms");

    }

    public LiveData<Resource<ChatRoom>> startNewChat(String userId1, String userId2) {
        MutableLiveData<Resource<ChatRoom>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        // Kiểm tra nếu đã có phòng chat chứa cả userId1 và userId2
        chatRoomsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ChatRoom existingChatRoom = dataSnapshot.getValue(ChatRoom.class);
                    if (existingChatRoom != null && !existingChatRoom.isGroupChat()) {
                        List<String> users = existingChatRoom.getUserIds();
                        if (users.contains(userId1) && users.contains(userId2)) {
                            result.setValue(Resource.success(existingChatRoom));
                            return;
                        }
                    }
                }
                createNewChatRoom(userId1, userId2, result);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                result.setValue(Resource.error(error.getMessage(), null));
            }
        });

        return result;
    }

    private void createNewChatRoom(String userId1, String userId2, MutableLiveData<Resource<ChatRoom>> result) {
        String chatRoomId = chatRoomsRef.push().getKey();
        ChatRoom newChatRoom = new ChatRoom(
                chatRoomId,
                chatRoomId,
                new ArrayList<>(Arrays.asList(userId1, userId2)),
                System.currentTimeMillis(),
                "",
                0L,
                false);

        if (chatRoomId == null) {
            result.setValue(Resource.error("Failed to create chat room", null));
            return;
        }

        chatRoomsRef.child(chatRoomId).setValue(newChatRoom)
                .addOnSuccessListener(aVoid -> {
                    Log.d("ChatRoomRepository", "Chat room created successfully: " + chatRoomId);
                    result.setValue(Resource.success(newChatRoom));
                })
                .addOnFailureListener(e -> {
                    Log.e("ChatRoomRepository", "Failed to create chat room: " + e.getMessage());
                    result.setValue(Resource.error(e.getMessage(), null));
                });
    }

    public LiveData<Resource<ChatRoom>> startNewGroupChat(List<String> userIds) {
        MutableLiveData<Resource<ChatRoom>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        String chatRoomId = chatRoomsRef.push().getKey();
        ChatRoom newChatRoom = new ChatRoom(
                chatRoomId,
                "Group: " + chatRoomId,
                new ArrayList<>(userIds),
                System.currentTimeMillis(),
                "",
                0L,
                true);

        if (chatRoomId == null) {
            result.setValue(Resource.error("Failed to create chat room id", null));
            return result;
        }

        chatRoomsRef.child(chatRoomId).setValue(newChatRoom)
                .addOnSuccessListener(aVoid -> {
                    Log.d("ChatRoomRepository", "Chat room created successfully: " + chatRoomId);
                    result.setValue(Resource.success(newChatRoom));
                })
                .addOnFailureListener(e -> {
                    Log.e("ChatRoomRepository", "Failed to create chat room: " + e.getMessage());
                    result.setValue(Resource.error(e.getMessage(), null));
                });

        return result;
    }

    public LiveData<Resource<List<ChatRoom>>> observeUserChatRooms(String userId) {
        MutableLiveData<Resource<List<ChatRoom>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));
        removeChatRoomsListener();
        chatRoomsListener = chatRoomsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<ChatRoom> userChatRooms = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ChatRoom chatRoom = dataSnapshot.getValue(ChatRoom.class);
                    if (chatRoom != null && chatRoom.getUserIds().contains(userId)) {
                        userChatRooms.add(chatRoom);
                    }
                }
                result.setValue(Resource.success(userChatRooms));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                result.setValue(Resource.error(error.getMessage(), null));
            }
        });

        return result;
    }

    public void removeChatRoomsListener() {
        if (chatRoomsListener != null) {
            chatRoomsRef.removeEventListener(chatRoomsListener);
            chatRoomsListener = null;
        }
    }

    public LiveData<Resource<Boolean>> updateLastMessage(Message message) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(false));

        Map<String, Object> updates = new HashMap<>();
        updates.put("lastMessage", message.getMessageContent());
        updates.put("lastMessageTimestamp", message.getTimestamp());

        chatRoomsRef.child(message.getChatRoomId()).updateChildren(updates)
                .addOnSuccessListener(aVoid -> result.setValue(Resource.success(true)))
                .addOnFailureListener(e -> result.setValue(Resource.error(e.getMessage(), false)));

        return result;
    }
}
