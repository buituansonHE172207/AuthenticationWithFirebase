package com.kas.authenticationwithfirebase.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kas.authenticationwithfirebase.data.model.UserProfile;

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

    public void saveUserProfile(UserProfile userProfile) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            firebaseFirestore.collection(USERS_COLLECTION)
                    .document(currentUser.getUid())
                    .set(userProfile);
        }
    }

    public LiveData<UserProfile> getUserProfile() {
        MutableLiveData<UserProfile> userProfileLiveData = new MutableLiveData<>();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            firebaseFirestore.collection(USERS_COLLECTION)
                    .document(currentUser.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            UserProfile userProfile = task.getResult().toObject(UserProfile.class);
                            userProfileLiveData.setValue(userProfile);
                        } else {
                            userProfileLiveData.setValue(null);
                        }
                    })
                    .addOnFailureListener(e -> userProfileLiveData.setValue(null));
        }
        return userProfileLiveData;
    }

}
