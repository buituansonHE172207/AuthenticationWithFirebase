package com.kas.authenticationwithfirebase.ui.auth;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.kas.authenticationwithfirebase.data.model.User;
import com.kas.authenticationwithfirebase.data.repository.AuthRepository;
import com.kas.authenticationwithfirebase.utility.Resource;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AuthViewModel extends ViewModel {
    private final AuthRepository authRepository;

    @Inject
    public AuthViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    // Register new User
    public LiveData<Resource<User>> registerUser(String email, String password) {
        return authRepository.registerUser(email, password);
    }

    // Login existing User
    public LiveData<Resource<User>> loginUser(String email, String password) {
        return authRepository.loginUser(email, password);
    }

    // Logout User
    public void logoutUser() {
        authRepository.logoutUser();
    }

    // Reset Password
    public LiveData<Resource<Boolean>> resetPassword(String email) {
        return authRepository.resetPassword(email);
    }

    // Get Current User
    public FirebaseUser getCurrentUser() {
        return authRepository.getCurrentUser();
    }

    // Is User Logged In
    public boolean isUserLoggedIn() {
        return authRepository.isUserLoggedIn();
    }


}
