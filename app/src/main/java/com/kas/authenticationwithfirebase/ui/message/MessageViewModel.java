package com.kas.authenticationwithfirebase.ui.message;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.kas.authenticationwithfirebase.data.model.Message;
import com.kas.authenticationwithfirebase.data.model.User;
import com.kas.authenticationwithfirebase.data.repository.AuthRepository;
import com.kas.authenticationwithfirebase.data.repository.ChatRoomRepository;
import com.kas.authenticationwithfirebase.data.repository.MessageRepository;
import com.kas.authenticationwithfirebase.data.repository.UserRepository;
import com.kas.authenticationwithfirebase.utility.Resource;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MessageViewModel extends ViewModel {
    private final MessageRepository messageRepository;
    private MutableLiveData<Resource<List<Message>>> messages;
    private final String currentUserId;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    @Inject
    public MessageViewModel(MessageRepository messageRepository,
                            AuthRepository authRepository,
                            ChatRoomRepository chatRoomRepository,
                            UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.currentUserId = authRepository.getCurrentUserId();
        this.chatRoomRepository = chatRoomRepository;
        this.userRepository = userRepository;
    }

    private <T> LiveData<Resource<T>> checkUserLoggedIn(LiveData<Resource<T>> successLiveData) {
        if (currentUserId == null) {
            MutableLiveData<Resource<T>> errorResult = new MutableLiveData<>();
            errorResult.setValue(Resource.error("User not logged in", null));
            return errorResult;
        }
        return successLiveData;
    }

    // Observe messages in a chat room
    public LiveData<Resource<List<Message>>> observeMessages(String chatRoomId) {
        if (messages == null) {
            messages = new MutableLiveData<>();
            messages.setValue(Resource.loading(null));
            messages = (MutableLiveData<Resource<List<Message>>>) messageRepository.observeMessages(chatRoomId, currentUserId);
        }
        return messages;
    }

    // Send a new message
    public LiveData<Resource<Message>> sendMessage(String chatRoomId, Message message) {
        message.setSenderId(currentUserId);
        LiveData<Resource<Message>> result = checkUserLoggedIn(
                messageRepository.sendMessage(chatRoomId, message)
        );
        // Update last message in chat room
        chatRoomRepository.updateLastMessage(message);
        return result;
    }

    // Mark message as read
    public LiveData<Resource<Boolean>> markMessageAsRead(String chatRoomId, String messageId) {
        return checkUserLoggedIn(
                messageRepository.markMessageAsRead(chatRoomId, messageId, currentUserId)
        );
    }

    // Delete a message
    public LiveData<Resource<Boolean>> deleteMessage(String chatRoomId, String messageId) {
        return checkUserLoggedIn(
                messageRepository.deleteMessage(chatRoomId, messageId, currentUserId)
        );
    }

    // Get user profile
    public LiveData<Resource<User>> getUserProfile(String userId) {
        return userRepository.getUserProfile(userId);
    }

    // Get current user ID
    public String getCurrentUserId() {
        return currentUserId;
    }

    // Remove message listener
    public void removeMessagesListener(String chatRoomId) {
        messageRepository.removeMessageListener(chatRoomId);
    }


}
