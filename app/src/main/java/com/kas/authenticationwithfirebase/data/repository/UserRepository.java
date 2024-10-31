package com.kas.authenticationwithfirebase.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kas.authenticationwithfirebase.data.entity.ChatRoom;
import com.kas.authenticationwithfirebase.data.entity.User;
import com.kas.authenticationwithfirebase.utility.Resource;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class UserRepository {
    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firebaseFirestore;
    private final FirebaseDatabase firebaseDatabase;
    private final String USERS_COLLECTION = "users";
    private final String CHAT_ROOMS_COLLECTION = "chatRooms";

    @Inject
    public UserRepository(FirebaseAuth firebaseAuth,
                          FirebaseFirestore firebaseFirestore,
                          FirebaseDatabase firebaseDatabase) {
        this.firebaseAuth = firebaseAuth;
        this.firebaseFirestore = firebaseFirestore;
        this.firebaseDatabase = firebaseDatabase;
    }

    public LiveData<Resource<User>> getUserProfile(String userId) {
        MutableLiveData<Resource<User>> result = new MutableLiveData<>();

        DocumentReference userRef = firebaseFirestore.collection("users").document(userId);
        userRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        result.setValue(Resource.success(user));
                    } else {
                        result.setValue(Resource.error("User not found", null));
                    }
                })
                .addOnFailureListener(e -> {
                    result.setValue(Resource.error(e.getMessage(), null));
                });

        return result;
    }

    public LiveData<Resource<Boolean>> updateUserProfile(User updatedUser) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            result.setValue(Resource.error("User not found", null));
            return result;
        }
        String userId = user.getUid();
        DocumentReference userRef = firebaseFirestore.collection(USERS_COLLECTION).document(userId);

        userRef.set(updatedUser)
                .addOnSuccessListener(aVoid -> result.setValue(Resource.success(true)))
                .addOnFailureListener(e ->
                        result.setValue(Resource.error(e.getMessage(), false)));

        return result;
    }

    //Update user image
    public LiveData<Resource<String>> updateUserImage(String imageUrl) {
        MutableLiveData<Resource<String>> result = new MutableLiveData<>();

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            result.setValue(Resource.error("User not found", null));
            return result;
        }
        String userId = user.getUid();
        DocumentReference userRef = firebaseFirestore.collection(USERS_COLLECTION).document(userId);

        userRef.update("imageUrl", imageUrl)
                .addOnSuccessListener(aVoid -> result.setValue(Resource.success(imageUrl)))
                .addOnFailureListener(e ->
                        result.setValue(Resource.error(e.getMessage(), null)));

        return result;
    }

    public LiveData<Resource<Boolean>> updateUserStatus(String status) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            result.setValue(Resource.error("User not found", null));
            return result;
        }
        String userId = user.getUid();
        DocumentReference userRef = firebaseFirestore.collection(USERS_COLLECTION).document(userId);

        userRef.update("status", status)
                .addOnSuccessListener(aVoid -> result.setValue(Resource.success(true)))
                .addOnFailureListener(e ->
                        result.setValue(Resource.error(e.getMessage(), false)));

        return result;
    }

    public LiveData<Resource<List<User>>> getUsersInChatRoom(String chatRoomId) {
        MutableLiveData<Resource<List<User>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        // Step 1: Fetch chat room to get the list of user IDs
        firebaseDatabase.getReference(CHAT_ROOMS_COLLECTION).child(chatRoomId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            ChatRoom chatRoom = snapshot.getValue(ChatRoom.class);
                            List<String> userIds = chatRoom != null ? chatRoom.getUserIds() : new ArrayList<>();

                            if (userIds.isEmpty()) {
                                result.setValue(Resource.success(new ArrayList<>())); // No users in chat room
                                return;
                            }

                            // Step 2: Fetch user details in a single batch using whereIn
                            firebaseFirestore.collection(USERS_COLLECTION)
                                    .whereIn("userId", userIds)
                                    .get()
                                    .addOnSuccessListener(userSnapshot -> {
                                        List<User> userList = new ArrayList<>();
                                        for (DocumentSnapshot doc : userSnapshot.getDocuments()) {
                                            User user = doc.toObject(User.class);
                                            if (user != null) {
                                                userList.add(user);
                                            }
                                        }
                                        result.setValue(Resource.success(userList));
                                    })
                                    .addOnFailureListener(e -> {
                                        result.setValue(Resource.error("Error fetching user details: " + e.getMessage(), null));
                                        Log.e("UserRepository", "Failed to fetch user data: " + e.getMessage());
                                    });
                        } else {
                            result.setValue(Resource.error("Chat room not found", null));
                            Log.e("UserRepository", "Chat room not found");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        result.setValue(Resource.error(error.getMessage(), null));
                        Log.e("UserRepository", "Failed to fetch chat room data: " + error.getMessage());
                    }
                });

        return result;
    }


}
