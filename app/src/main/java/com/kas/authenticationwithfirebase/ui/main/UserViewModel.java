package com.kas.authenticationwithfirebase.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.kas.authenticationwithfirebase.data.model.UserProfile;
import com.kas.authenticationwithfirebase.data.repository.UserRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class UserViewModel extends ViewModel {
    private final UserRepository repository;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    @Inject
    public UserViewModel(UserRepository userRepository) {
        this.repository = userRepository;
    }

    public void saveUserProfile(String name, String email) {
        isLoading.setValue(true);
        repository.saveUserProfile(new UserProfile(name, email));
        isLoading.setValue(false);
    }

    public LiveData<UserProfile> getUserProfile() {
        return repository.getUserProfile();
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }




}
