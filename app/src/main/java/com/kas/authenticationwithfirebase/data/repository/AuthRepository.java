package com.kas.authenticationwithfirebase.data.repository;


import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kas.authenticationwithfirebase.data.entity.User;
import com.kas.authenticationwithfirebase.utility.Resource;

import javax.inject.Inject;

public class AuthRepository {
    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firebaseFirestore;
    private final String USERS_COLLECTION = "users";

    @Inject
    public AuthRepository(FirebaseAuth firebaseAuth, FirebaseFirestore firebaseFirestore) {
        this.firebaseAuth = firebaseAuth;
        this.firebaseFirestore = firebaseFirestore;
    }

    public LiveData<Resource<User>> registerUser(String email, String password) {
        MutableLiveData<Resource<User>> result = new MutableLiveData<>();

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = firebaseAuth.getCurrentUser().getUid();
                        User newUser = new User(userId, email, email, "", "", 0, "");

                        firebaseFirestore.collection(USERS_COLLECTION)
                                .document(userId)
                                .set(newUser)
                                .addOnSuccessListener(aVoid -> {
                                    result.setValue(Resource.success(newUser));
                                })
                                .addOnFailureListener(e -> {
                                    result.setValue(Resource.error(e.getMessage(), null));
                                });

                    } else {
                        result.setValue(Resource.error(task.getException().getMessage(), null));
                    }
                });
        return result;
    }

    // Sign In with Email and Password
    public LiveData<Resource<User>> loginUser(String email, String password) {
        MutableLiveData<Resource<User>> result = new MutableLiveData<>();

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = firebaseAuth.getCurrentUser().getUid();

                        DocumentReference userRef = firebaseFirestore
                                .collection(USERS_COLLECTION)
                                .document(userId);
                        // Set the user's online status to online and last seen to current time
                        userRef.update("status", "online", "lastSeen", System.currentTimeMillis())
                                .addOnSuccessListener(aVoid -> {
                                    userRef.get()
                                            .addOnSuccessListener(documentSnapshot -> {
                                                User user = documentSnapshot.toObject(User.class);
                                                result.setValue(Resource.success(user));
                                            })
                                            .addOnFailureListener(e -> {
                                                result.setValue(Resource.error(e.getMessage(), null));
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    result.setValue(Resource.error("Failed to update user status: " + e.getMessage(), null));
                                });
                    } else {
                        result.setValue(Resource.error(task.getException().getMessage(), null));
                    }
                });
        return result;
    }

    // Sign Out
    public void logoutUser() {
        String userId = firebaseAuth.getCurrentUser().getUid();

        // Update the user's online status to offline
        firebaseFirestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .update("status", "offline")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        firebaseAuth.signOut();
                    } else {
                        Log.e("AuthRepository", "Failed to update user status to offline");
                    }
                });
    }

    // Reset password
    public LiveData<Resource<Boolean>> resetPassword(String email) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();

        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        result.setValue(Resource.success(true));
                    } else {
                        result.setValue(Resource.error(task.getException().getMessage(), false));
                    }
                });
        return result;
    }

    // Change password
    public LiveData<Resource<Boolean>> changePassword(String newPassword) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            user.updatePassword(newPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            result.setValue(Resource.success(true));
                        } else {
                            result.setValue(Resource.error(task.getException().getMessage(), false));
                        }
                    });
        } else {
            result.setValue(Resource.error("User not logged in", false));
        }
        return result;
    }

    // Get currently signed-in user Id
    public String getCurrentUserId() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public boolean isUserLoggedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }
}
