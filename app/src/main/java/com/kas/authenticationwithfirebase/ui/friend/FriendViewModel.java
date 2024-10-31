package com.kas.authenticationwithfirebase.ui.friend;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.kas.authenticationwithfirebase.data.entity.ChatRoom;
import com.kas.authenticationwithfirebase.data.entity.User;
import com.kas.authenticationwithfirebase.data.repository.AuthRepository;
import com.kas.authenticationwithfirebase.data.repository.ChatRoomRepository;
import com.kas.authenticationwithfirebase.data.repository.FriendRepository;
import com.kas.authenticationwithfirebase.utility.Resource;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class FriendViewModel extends ViewModel {
    private final FriendRepository friendRepository;
    private final String currentUserId;
    private final ChatRoomRepository chatRoomRepository;

    @Inject
    public FriendViewModel(FriendRepository friendRepository, AuthRepository authRepository, ChatRoomRepository chatRoomRepository) {
        this.friendRepository = friendRepository;
        this.currentUserId = authRepository.getCurrentUserId();
        this.chatRoomRepository = chatRoomRepository;
    }

    private <T> LiveData<Resource<T>> checkUserLoggedIn(LiveData<Resource<T>> successLiveData) {
        if (currentUserId == null) {
            MutableLiveData<Resource<T>> errorResult = new MutableLiveData<>();
            errorResult.setValue(Resource.error("User not logged in", null));
            return errorResult;
        }
        return successLiveData;
    }

    // Add a friend
    public LiveData<Resource<Boolean>> addFriend(String friendUserId) {
        return checkUserLoggedIn(
                friendRepository.addFriend(currentUserId, friendUserId)
        );
    }

    // Remove a friend
    public LiveData<Resource<Boolean>> removeFriend(String friendUserId) {
        return checkUserLoggedIn(
                friendRepository.removeFriend(currentUserId, friendUserId)
        );
    }

    // Get friends list
    public LiveData<Resource<List<User>>> getFriendsList() {
        return checkUserLoggedIn(
                friendRepository.getFriendsList(currentUserId)
        );
    }

    // Search for friends by username or email
    public LiveData<Resource<List<User>>> searchFriends(String query) {
        return friendRepository.searchFriends(query);
    }

    // Create a chat room with a friend
    public LiveData<Resource<ChatRoom>> createChatRoom(String friendUserId) {
        return checkUserLoggedIn(
                chatRoomRepository.startNewChat(friendUserId, currentUserId)
        );
    }
}
