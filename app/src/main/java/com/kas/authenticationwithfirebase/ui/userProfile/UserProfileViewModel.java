package com.kas.authenticationwithfirebase.ui.userProfile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.kas.authenticationwithfirebase.data.entity.User;
import com.kas.authenticationwithfirebase.data.repository.AuthRepository;
import com.kas.authenticationwithfirebase.data.repository.UserRepository;
import com.kas.authenticationwithfirebase.utility.Resource;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class UserProfileViewModel extends ViewModel {
    private final UserRepository userRepository;
    private final String userId;

    @Inject
    public UserProfileViewModel(UserRepository userRepository, AuthRepository authRepository) {
        this.userRepository = userRepository;
        this.userId = authRepository.getCurrentUserId();
    }

    private <T> LiveData<Resource<T>> checkUserLoggedIn(LiveData<Resource<T>> successLiveData) {
        if (userId == null) {
            MutableLiveData<Resource<T>> errorResult = new MutableLiveData<>();
            errorResult.setValue(Resource.error("User not logged in", null));
            return errorResult;
        }
        return successLiveData;
    }

    public LiveData<Resource<User>> getUserProfile() {
        return checkUserLoggedIn(userRepository.getUserProfile(userId));
    }

    public LiveData<Resource<Boolean>> updateUserProfile(User user) {
        return checkUserLoggedIn(userRepository.updateUserProfile(user));
    }

    public LiveData<Resource<Boolean>> updateUserStatus(String status) {
        return checkUserLoggedIn(userRepository.updateUserStatus(status));
    }

    public LiveData<Resource<String>> updateUserImage(String imageUrl) {
        return checkUserLoggedIn(userRepository.updateUserImage(imageUrl));
    }
}
