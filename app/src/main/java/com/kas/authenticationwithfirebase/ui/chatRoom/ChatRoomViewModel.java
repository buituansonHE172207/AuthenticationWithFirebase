package com.kas.authenticationwithfirebase.ui.chatRoom;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.kas.authenticationwithfirebase.data.entity.ChatRoom;
import com.kas.authenticationwithfirebase.data.entity.User;
import com.kas.authenticationwithfirebase.data.repository.AuthRepository;
import com.kas.authenticationwithfirebase.data.repository.ChatRoomRepository;
import com.kas.authenticationwithfirebase.data.repository.FriendRepository;
import com.kas.authenticationwithfirebase.data.repository.MessageRepository;
import com.kas.authenticationwithfirebase.data.repository.UserRepository;
import com.kas.authenticationwithfirebase.utility.Resource;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ChatRoomViewModel extends ViewModel {
    private final ChatRoomRepository chatRoomRepository;
    private final String currentUserId;
    private final FriendRepository friendRepository;

    @Inject
    public ChatRoomViewModel(ChatRoomRepository chatRoomRepository, AuthRepository authRepository,FriendRepository friendRepository) {
        this.chatRoomRepository = chatRoomRepository;
        this.currentUserId = authRepository.getCurrentUserId();
        this.friendRepository = friendRepository;
    }

    private <T> LiveData<Resource<T>> checkUserLoggedIn(LiveData<Resource<T>> successLiveData) {
        if (currentUserId == null) {
            MutableLiveData<Resource<T>> errorResult = new MutableLiveData<>();
            errorResult.setValue(Resource.error("User not logged in", null));
            return errorResult;
        }
        return successLiveData;
    }

    public LiveData<Resource<List<ChatRoom>>> getChatRooms() {
        return chatRoomRepository.observeUserChatRooms(currentUserId);
    }

    public LiveData<Resource<ChatRoom>> createChatRoom(String userId) {
        return checkUserLoggedIn(chatRoomRepository.startNewChat(userId, currentUserId));
    }

    public LiveData<Resource<ChatRoom>> createGroupChatRoom(List<String> userId) {
        return chatRoomRepository.startNewGroupChat(userId);
    }

    public LiveData<Resource<Boolean>> deleteChatRoom(String chatRoomId) {
        return chatRoomRepository.deleteChatRoom(chatRoomId);
    }

    public LiveData<Resource<Integer>> getUnreadMessagesCount(String chatRoomId) {
        return checkUserLoggedIn(
                chatRoomRepository.countUnreadMessages(chatRoomId, currentUserId)
        );
    }

    // Get All Friends
    public LiveData<Resource<List<User>>> getAllFriends() {
        return checkUserLoggedIn(friendRepository.getFriendsList(currentUserId));
    }


    @Override
    protected void onCleared() {
        super.onCleared();
        chatRoomRepository.removeChatRoomsListener();
    }
}
