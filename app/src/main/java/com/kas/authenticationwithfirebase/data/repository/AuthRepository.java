package com.kas.authenticationwithfirebase.data.repository;


import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import javax.inject.Inject;

public class AuthRepository {
    private final FirebaseAuth firebaseAuth;

    @Inject
    public AuthRepository(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }

    public LiveData<FirebaseUser> signUp(String email, String password) {
        MutableLiveData<FirebaseUser> userLiveData = new MutableLiveData<>();
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userLiveData.setValue(firebaseAuth.getCurrentUser());
                    } else {
                        Log.e("AuthRepository", "Sign-Up Failed: " + task.getException().getMessage());
                        userLiveData.setValue(null);
                    }
                });
        return userLiveData;
    }

    // Sign In with Email and Password
    public LiveData<FirebaseUser> signIn(String email, String password) {
        MutableLiveData<FirebaseUser> userLiveData = new MutableLiveData<>();
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userLiveData.setValue(firebaseAuth.getCurrentUser());
                    } else {
                        Log.e("AuthRepository", "Sign-In Failed: " + task.getException().getMessage());
                        userLiveData.setValue(null);
                    }
                });
        return userLiveData;
    }

    // Sign Out
    public void signOut() {
        firebaseAuth.signOut();
    }

    // Get currently signed-in user
    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }
}
