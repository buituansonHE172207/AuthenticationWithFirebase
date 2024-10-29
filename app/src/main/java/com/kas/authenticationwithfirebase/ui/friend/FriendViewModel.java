package com.kas.authenticationwithfirebase.ui.friend;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.kas.authenticationwithfirebase.data.model.User;
import com.kas.authenticationwithfirebase.data.repository.AuthRepository;
import com.kas.authenticationwithfirebase.data.repository.FriendRepository;
import com.kas.authenticationwithfirebase.utility.Resource;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class FriendViewModel extends ViewModel {
    private final FriendRepository friendRepository;
    private final String currentUserId;

    @Inject
    public FriendViewModel(FriendRepository friendRepository, AuthRepository authRepository) {
        this.friendRepository = friendRepository;
        this.currentUserId = authRepository.getCurrentUserId();
    }

    private <T> LiveData<Resource<T>> requireCurrentUser(LiveData<Resource<T>> onValidUser, Resource<T> error) {
        if (currentUserId == null) {
            MutableLiveData<Resource<T>> result = new MutableLiveData<>();
            result.setValue(error);
            return result;
        }
        return onValidUser;
    }

    // Add a friend
    public LiveData<Resource<Boolean>> addFriend(String friendUserId) {
        return requireCurrentUser(
                friendRepository.addFriend(currentUserId, friendUserId),
                Resource.error("User not logged in", false)
        );
    }

    // Remove a friend
    public LiveData<Resource<Boolean>> removeFriend(String friendUserId) {
        return requireCurrentUser(
                friendRepository.removeFriend(currentUserId, friendUserId),
                Resource.error("User not logged in", false)
        );
    }

    // Get friends list
    public LiveData<Resource<List<User>>> getFriendsList() {
        return requireCurrentUser(
                friendRepository.getFriendsList(currentUserId),
                Resource.error("User not logged in", null)
        );
    }

    // Search for friends by username or email
    public LiveData<Resource<List<User>>> searchFriends(String query) {
        return friendRepository.searchFriends(query);
    }
}
