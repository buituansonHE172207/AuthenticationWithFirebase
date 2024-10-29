package com.kas.authenticationwithfirebase.ui.message;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.kas.authenticationwithfirebase.data.model.Message;
import com.kas.authenticationwithfirebase.data.repository.AuthRepository;
import com.kas.authenticationwithfirebase.data.repository.MessageRepository;
import com.kas.authenticationwithfirebase.utility.Resource;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MessageViewModel extends ViewModel {
    private final MessageRepository messageRepository;
    private MutableLiveData<Resource<List<Message>>> messages;
    private final String currentUserId;

    @Inject
    public MessageViewModel(MessageRepository messageRepository, AuthRepository authRepository) {
        this.messageRepository = messageRepository;
        this.currentUserId = authRepository.getCurrentUserId();
    }

    private <T> LiveData<Resource<T>> requireCurrentUser(LiveData<Resource<T>> onValidUser, Resource<T> error) {
        if (currentUserId == null) {
            return new MutableLiveData<>(error);
        }
        return onValidUser;
    }

    // Observe messages in a chat room
    public LiveData<Resource<List<Message>>> observeMessages(String chatRoomId) {
        if (messages == null) {
            messages = new MutableLiveData<>();
            messages.setValue(Resource.loading(null));
            messages = (MutableLiveData<Resource<List<Message>>>) messageRepository.observeMessages(chatRoomId);
        }
        return messages;
    }

    // Send a new message
    public LiveData<Resource<Message>> sendMessage(String chatRoomId, Message message) {
        message.setSenderId(currentUserId);
        return requireCurrentUser(
                messageRepository.sendMessage(chatRoomId, message),
                Resource.error("User not found", null)
        );
    }

    // Mark message as read
    public LiveData<Resource<Boolean>> markMessageAsRead(String chatRoomId, String messageId) {
        return requireCurrentUser(
                messageRepository.markMessageAsRead(chatRoomId, messageId, currentUserId),
                Resource.error("User not found", false)
        );
    }

    // Delete a message
    public LiveData<Resource<Boolean>> deleteMessage(String chatRoomId, String messageId) {
        return requireCurrentUser(
                messageRepository.deleteMessage(chatRoomId, messageId, currentUserId),
                Resource.error("User not found", false)
        );
    }
}
