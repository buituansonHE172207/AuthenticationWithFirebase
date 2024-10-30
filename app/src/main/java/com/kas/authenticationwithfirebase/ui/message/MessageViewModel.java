package com.kas.authenticationwithfirebase.ui.message;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.kas.authenticationwithfirebase.data.model.Message;
import com.kas.authenticationwithfirebase.data.repository.AuthRepository;
import com.kas.authenticationwithfirebase.data.repository.ChatRoomRepository;
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
    private ChatRoomRepository chatRoomRepository;

    @Inject
    public MessageViewModel(MessageRepository messageRepository, AuthRepository authRepository, ChatRoomRepository chatRoomRepository) {
        this.messageRepository = messageRepository;
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

    // Observe messages in a chat room
    public LiveData<Resource<List<Message>>> observeMessages(String chatRoomId) {
        if (messages == null) {
            messages = new MutableLiveData<>();
            messages.setValue(Resource.loading(null));

            // Observe messages from the repository
            LiveData<Resource<List<Message>>> observedMessages = messageRepository.observeMessages(chatRoomId);

            observedMessages.observeForever(resource -> {
                if (resource.getData() != null) {
                    // Mark each unread message as read
                    for (Message message : resource.getData()) {
                        List<String> readBy = message.getReadBy();
                        if (readBy != null) {
                            Log.d("countUnreadMessages", "Read by: " + readBy.toString());
                            if (!readBy.contains(currentUserId)) {
                                Log.d("countUnreadMessages", "set readby" + message.getMessageId());
                                markMessageAsRead(chatRoomId, message.getMessageId());
                            }
                        }
                    }
                    // Update the live data with the observed resource
                    messages.setValue(resource);
                }
            });
        }
        return messages; // Move the return statement outside the if block
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

    // Get current user ID
    public String getCurrentUserId() {
        return currentUserId;
    }

    public void removeMessagesListener(String chatRoomId) {
        messageRepository.removeMessageListener(chatRoomId);
    }
}
