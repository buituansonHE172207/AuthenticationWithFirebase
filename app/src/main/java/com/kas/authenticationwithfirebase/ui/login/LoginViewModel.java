package com.kas.authenticationwithfirebase.ui.login;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.kas.authenticationwithfirebase.data.model.UserProfile;
import com.kas.authenticationwithfirebase.data.repository.AuthRepository;
import com.kas.authenticationwithfirebase.data.repository.UserRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class LoginViewModel extends ViewModel {
    private final AuthRepository repository;
    private final UserRepository userRepository;

    @Inject
    public LoginViewModel(AuthRepository authRepository, UserRepository userRepository) {
        this.repository = authRepository;
        this.userRepository = userRepository;
    }

    public LiveData<FirebaseUser> signIn(String email, String password) {
        LiveData<FirebaseUser> user = repository.signIn(email, password);
        // Save user profile
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser != null) {
            UserProfile userProfile = new UserProfile(currentUser.getDisplayName(), currentUser.getEmail());
            userRepository.saveUserProfile(userProfile);
        }
        return user;
    }

    public FirebaseUser getCurrentUser() {
        return repository.getCurrentUser();
    }


}
