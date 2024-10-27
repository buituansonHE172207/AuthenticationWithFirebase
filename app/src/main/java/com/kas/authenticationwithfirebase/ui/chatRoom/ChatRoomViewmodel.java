package com.kas.authenticationwithfirebase.ui.chatRoom;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.kas.authenticationwithfirebase.data.model.ChatRoom;
import com.kas.authenticationwithfirebase.data.repository.AuthRepository;
import com.kas.authenticationwithfirebase.data.repository.ChatRoomRepository;
import com.kas.authenticationwithfirebase.utility.Resource;

import java.util.ArrayList;
import java.util.Arrays;
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
    public LiveData<Resource<List<ChatRoom>>> getMockChatRooms() {
        MutableLiveData<Resource<List<ChatRoom>>> mockChatRoomsLiveData = new MutableLiveData<>();

        List<ChatRoom> mockChatRooms = new ArrayList<>();
        mockChatRooms.add(new ChatRoom("1", Arrays.asList("User1", "User2"), System.currentTimeMillis() - 60000, "Hey, how are you?", System.currentTimeMillis() - 60000, false));
        mockChatRooms.add(new ChatRoom("2", Arrays.asList("User1", "User3"), System.currentTimeMillis() - 120000, "See you tomorrow!", System.currentTimeMillis() - 120000, false));
        mockChatRooms.add(new ChatRoom("3", Arrays.asList("User1", "User4", "User5"), System.currentTimeMillis() - 180000, "Group chat started!", System.currentTimeMillis() - 180000, true));
        mockChatRooms.add(new ChatRoom("4", Arrays.asList("User1", "User6"), System.currentTimeMillis() - 240000, "Let's catch up later.", System.currentTimeMillis() - 240000, false));
        mockChatRooms.add(new ChatRoom("4", Arrays.asList("User1", "User6"), System.currentTimeMillis() - 240000, "Let's catch up later.", System.currentTimeMillis() - 240000, false));
        mockChatRooms.add(new ChatRoom("4", Arrays.asList("User1", "User6"), System.currentTimeMillis() - 240000, "Let's catch up later.", System.currentTimeMillis() - 240000, false));
        mockChatRooms.add(new ChatRoom("4", Arrays.asList("User1", "User6"), System.currentTimeMillis() - 240000, "Let's catch up later.", System.currentTimeMillis() - 240000, false));
        mockChatRooms.add(new ChatRoom("4", Arrays.asList("User1", "User6"), System.currentTimeMillis() - 240000, "Let's catch up later.", System.currentTimeMillis() - 240000, false));
        mockChatRooms.add(new ChatRoom("4", Arrays.asList("User1", "User6"), System.currentTimeMillis() - 240000, "Let's catch up later.", System.currentTimeMillis() - 240000, false));
        mockChatRooms.add(new ChatRoom("4", Arrays.asList("User1", "User6"), System.currentTimeMillis() - 240000, "Let's catch up later.", System.currentTimeMillis() - 240000, false));
        mockChatRooms.add(new ChatRoom("4", Arrays.asList("User1", "User6"), System.currentTimeMillis() - 240000, "Let's catch up later.", System.currentTimeMillis() - 240000, false));
        mockChatRooms.add(new ChatRoom("4", Arrays.asList("User1", "User6"), System.currentTimeMillis() - 240000, "Let's catch up later.", System.currentTimeMillis() - 240000, false));
        mockChatRooms.add(new ChatRoom("4", Arrays.asList("User1", "User6"), System.currentTimeMillis() - 240000, "Let's catch up later.", System.currentTimeMillis() - 240000, false));
        mockChatRooms.add(new ChatRoom("4", Arrays.asList("User1", "User6"), System.currentTimeMillis() - 240000, "Let's catch up later.", System.currentTimeMillis() - 240000, false));

        mockChatRoomsLiveData.setValue(Resource.success(mockChatRooms));
        return mockChatRoomsLiveData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        chatRoomRepository.removeChatRoomsListener();
    }


}
