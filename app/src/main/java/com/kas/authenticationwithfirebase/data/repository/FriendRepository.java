package com.kas.authenticationwithfirebase.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kas.authenticationwithfirebase.data.entity.Friend;
import com.kas.authenticationwithfirebase.data.entity.User;
import com.kas.authenticationwithfirebase.utility.Resource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

public class FriendRepository {
    private final FirebaseFirestore firebaseFirestore;
    private final String USERS_COLLECTION = "users";
    private final String FRIENDS_SUB_COLLECTION = "friends";

    @Inject
    public FriendRepository(FirebaseFirestore firebaseFirestore) {
        this.firebaseFirestore = firebaseFirestore;
    }

    // Add Friend
    public LiveData<Resource<Boolean>> addFriend(String currentUserId, String friendUserId) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();

        if (currentUserId.equals(friendUserId)) {
            result.setValue(Resource.error("Cannot add yourself as a friend", false));
            return result;
        }

        CollectionReference currentUserFriendsRef = firebaseFirestore.collection(USERS_COLLECTION)
                .document(currentUserId)
                .collection(FRIENDS_SUB_COLLECTION);

        CollectionReference friendFriendsRef = firebaseFirestore.collection(USERS_COLLECTION)
                .document(friendUserId)
                .collection(FRIENDS_SUB_COLLECTION);

        currentUserFriendsRef.document(friendUserId).set(new Friend())
                .addOnSuccessListener(aVoid -> {
                    friendFriendsRef.document(currentUserId).set(new Friend(friendUserId))
                            .addOnSuccessListener(aVoid1 -> result.setValue(Resource.success(true)))
                            .addOnFailureListener(e -> result.setValue(Resource.error("Failed to add friend on friendUser side: " + e.getMessage(), false)));
                })
                .addOnFailureListener(e -> result.setValue(Resource.error("Failed to add friend on currentUser side: " + e.getMessage(), false)));

        return result;
    }

    // Remove Friend
    public LiveData<Resource<Boolean>> removeFriend(String currentUserId, String friendUserId) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();

        CollectionReference currentUserFriendsRef = firebaseFirestore.collection(USERS_COLLECTION)
                .document(currentUserId)
                .collection(FRIENDS_SUB_COLLECTION);

        CollectionReference friendUserFriendsRef = firebaseFirestore.collection(USERS_COLLECTION)
                .document(friendUserId)
                .collection(FRIENDS_SUB_COLLECTION);

        currentUserFriendsRef.document(friendUserId).delete()
                .addOnSuccessListener(aVoid -> {
                    friendUserFriendsRef.document(currentUserId).delete()
                            .addOnSuccessListener(aVoid2 -> result.setValue(Resource.success(true)))
                            .addOnFailureListener(e -> result.setValue(Resource.error("Failed to remove friend on friendUser side: " + e.getMessage(), false)));
                })
                .addOnFailureListener(e -> result.setValue(Resource.error("Failed to remove friend on currentUser side: " + e.getMessage(), false)));

        return result;
    }

    // Get Friends List
    public LiveData<Resource<List<User>>> getFriendsList(String currentUserId) {
        MutableLiveData<Resource<List<User>>> result = new MutableLiveData<>();

        CollectionReference friendsRef = firebaseFirestore.collection(USERS_COLLECTION)
                .document(currentUserId)
                .collection(FRIENDS_SUB_COLLECTION);

        friendsRef.get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> friendIds = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        friendIds.add(doc.getId());
                    }

                    // Batch get friend details
                    if (!friendIds.isEmpty()) {
                        firebaseFirestore.collection(USERS_COLLECTION)
                                .whereIn("userId", friendIds)
                                .get()
                                .addOnSuccessListener(friendSnapshot -> {
                                    List<User> friendsList = new ArrayList<>();
                                    for (DocumentSnapshot friendDoc : friendSnapshot.getDocuments()) {
                                        friendsList.add(friendDoc.toObject(User.class));
                                    }
                                    result.setValue(Resource.success(friendsList));
                                })
                                .addOnFailureListener(e -> result.setValue(Resource.error("Error fetching friends details: " + e.getMessage(), null)));
                    } else {
                        result.setValue(Resource.success(new ArrayList<>())); // No friends
                    }
                })
                .addOnFailureListener(e -> result.setValue(Resource.error(e.getMessage(), null)));

        return result;
    }

    // Search Friends by username or email
    public LiveData<Resource<List<User>>> searchFriends(String query) {
        MutableLiveData<Resource<List<User>>> result = new MutableLiveData<>();
        Set<User> uniqueUsers = new HashSet<>();

        firebaseFirestore.collection(USERS_COLLECTION)
                .whereGreaterThanOrEqualTo("username", query)
                .whereLessThanOrEqualTo("username", query + "\uf8ff")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        uniqueUsers.add(doc.toObject(User.class));
                    }
                    // Perform second search on email only if username search completed successfully
                    firebaseFirestore.collection(USERS_COLLECTION)
                            .whereGreaterThanOrEqualTo("email", query)
                            .whereLessThanOrEqualTo("email", query + "\uf8ff")
                            .get()
                            .addOnSuccessListener(emailQuerySnapshot -> {
                                for (DocumentSnapshot doc : emailQuerySnapshot.getDocuments()) {
                                    uniqueUsers.add(doc.toObject(User.class));
                                }
                                result.setValue(Resource.success(new ArrayList<>(uniqueUsers)));
                            })
                            .addOnFailureListener(e -> result.setValue(Resource.error(e.getMessage(), null)));
                })
                .addOnFailureListener(e -> result.setValue(Resource.error(e.getMessage(), null)));

        return result;
    }
}