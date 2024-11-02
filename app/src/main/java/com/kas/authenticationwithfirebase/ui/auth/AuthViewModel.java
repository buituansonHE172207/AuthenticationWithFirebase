package com.kas.authenticationwithfirebase.ui.auth;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.kas.authenticationwithfirebase.data.entity.User;
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

    // Change Password
    public LiveData<Resource<Boolean>> changePassword(String newPassword) {
        return authRepository.changePassword(newPassword);
    }

    // Is User Logged In
    public boolean isUserLoggedIn() {
        return authRepository.isUserLoggedIn();
    }


}
