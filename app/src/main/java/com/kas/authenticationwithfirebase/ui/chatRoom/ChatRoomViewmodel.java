package com.kas.authenticationwithfirebase.ui.chatRoom;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.kas.authenticationwithfirebase.data.model.ChatRoom;
import com.kas.authenticationwithfirebase.data.repository.AuthRepository;
import com.kas.authenticationwithfirebase.data.repository.ChatRoomRepository;
import com.kas.authenticationwithfirebase.utility.Resource;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ChatRoomViewmodel extends ViewModel {
    private final ChatRoomRepository chatRoomRepository;
    private final AuthRepository authRepository;

    @Inject
    public ChatRoomViewmodel(ChatRoomRepository chatRoomRepository, AuthRepository authRepository) {
        this.chatRoomRepository = chatRoomRepository;
        this.authRepository = authRepository;
    }

    private String getCurrentUserId() {
        return authRepository.getCurrentUser().getUid();
    }

    public LiveData<Resource<List<ChatRoom>>> getChatRooms() {
        return chatRoomRepository.observeUserChatRooms(getCurrentUserId());
    }

    public LiveData<Resource<ChatRoom>> createChatRoom(String userId) {
        return chatRoomRepository.startNewChat(userId, getCurrentUserId());
    }

    public LiveData<Resource<ChatRoom>> createGroupChatRoom(List<String> userId) {
        return chatRoomRepository.startNewGroupChat(userId);
    }

    public LiveData<Resource<Boolean>> updateLastMessage(String chatRoomId, String lastMessage, Long lastMessageTimestamp) {
        return chatRoomRepository.updateLastMessage(chatRoomId, lastMessage, lastMessageTimestamp);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        chatRoomRepository.removeChatRoomsListener();
    }


}
