package com.kas.authenticationwithfirebase.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kas.authenticationwithfirebase.data.entity.ChatRoom;
import com.kas.authenticationwithfirebase.data.entity.Message;
import com.kas.authenticationwithfirebase.utility.Resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class ChatRoomRepository {
    private final DatabaseReference chatRoomsRef;
    private final DatabaseReference messagesRef;

    private ValueEventListener chatRoomsListener;
    private final CollectionReference usersRef;

    @Inject
    public ChatRoomRepository(FirebaseDatabase firebaseDatabase, FirebaseFirestore firebaseFirestore) {
        this.chatRoomsRef = firebaseDatabase.getReference("chatRooms");
        this.messagesRef = firebaseDatabase.getReference("messages");

        this.usersRef = firebaseFirestore.collection("users");
    }

    public LiveData<Resource<ChatRoom>> startNewChat(String userId1, String userId2) {
        MutableLiveData<Resource<ChatRoom>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        // Check if chat room already exists
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
        if (chatRoomId == null) {
            result.setValue(Resource.error("Failed to create chat room ID", null));
            return;
        }

        Task<DocumentSnapshot> user1Task = usersRef.document(userId1).get();
        Task<DocumentSnapshot> user2Task = usersRef.document(userId2).get();

        Tasks.whenAllSuccess(user1Task, user2Task).addOnSuccessListener(results -> {
            String userName1 = user1Task.getResult().getString("username");
            String userName2 = user2Task.getResult().getString("username");

            if (userName1 == null || userName2 == null) {
                result.setValue(Resource.error("Failed to fetch user names", null));
                return;
            }

            String chatRoomName = userName1 + " & " + userName2;

            ChatRoom newChatRoom = new ChatRoom(
                    chatRoomId,
                    chatRoomName,
                    new ArrayList<>(Arrays.asList(userId1, userId2)),
                    System.currentTimeMillis(),
                    "",
                    0L,
                    false);

            // Add the chat room to the database
            chatRoomsRef.child(chatRoomId).setValue(newChatRoom)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("ChatRoomRepository", "Chat room created successfully: " + chatRoomId);
                        result.setValue(Resource.success(newChatRoom));
                    })
                    .addOnFailureListener(e -> {
                        Log.e("ChatRoomRepository", "Failed to create chat room: " + e.getMessage());
                        result.setValue(Resource.error(e.getMessage(), null));
                    });

        }).addOnFailureListener(e -> {
            Log.e("ChatRoomRepository", "Failed to fetch user data: " + e.getMessage());
            result.setValue(Resource.error("Failed to fetch user data", null));
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
                        Integer unreadCount = dataSnapshot.child("unreadCounts").child(userId).getValue(Integer.class);
                        if (chatRoom.getUnreadCounts() == null)
                            chatRoom.setUnreadCounts(new HashMap<>());
                        chatRoom.getUnreadCounts().putIfAbsent(userId, unreadCount != null ? unreadCount : 0);
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
    public LiveData<Resource<Integer>> countUnreadMessages(String chatRoomId, String userId) {
        MutableLiveData<Resource<Integer>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        DatabaseReference chatRoomMessagesRef = messagesRef.child(chatRoomId);

        chatRoomMessagesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                int unreadCount = 0;
                Log.d("countUnreadMessages", "Number of messages: " + snapshot.getChildrenCount() + "User: " + userId);

                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    Message message = messageSnapshot.getValue(Message.class);

                    if (message != null) {
                        List<String> readBy = message.getReadBy();

                        // Safely log readBy to avoid NullPointerException
                        if (readBy != null) {
                            Log.d("countUnreadMessages", "Read by: " + readBy.toString());
                            if (!readBy.contains(userId)) {
                                unreadCount++;
                            }
                        } else {
                            Log.d("countUnreadMessages", "Message has no readBy list. Counting as unread.");
                            unreadCount++;
                        }
                    }
                }

                Log.d("countUnreadMessages", "Number of unread messages: " + unreadCount);
                result.setValue(Resource.success(unreadCount));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                result.setValue(Resource.error(error.getMessage(), null));
            }
        });

        return result;
    }



}
