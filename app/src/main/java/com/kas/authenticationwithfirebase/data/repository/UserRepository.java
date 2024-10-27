package com.kas.authenticationwithfirebase.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kas.authenticationwithfirebase.data.model.User;
import com.kas.authenticationwithfirebase.utility.Resource;

import javax.inject.Inject;

public class UserRepository {
    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firebaseFirestore;
    private final String USERS_COLLECTION = "users";

    @Inject
    public UserRepository(FirebaseAuth firebaseAuth, FirebaseFirestore firebaseFirestore) {
        this.firebaseAuth = firebaseAuth;
        this.firebaseFirestore = firebaseFirestore;
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

}
