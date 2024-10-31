package com.kas.authenticationwithfirebase.ui.message;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.kas.authenticationwithfirebase.data.entity.Message;
import com.kas.authenticationwithfirebase.data.entity.User;
import com.kas.authenticationwithfirebase.data.model.MessageWithUserDetail;
import com.kas.authenticationwithfirebase.data.repository.AuthRepository;
import com.kas.authenticationwithfirebase.data.repository.ChatRoomRepository;
import com.kas.authenticationwithfirebase.data.repository.MessageRepository;
import com.kas.authenticationwithfirebase.data.repository.UserRepository;
import com.kas.authenticationwithfirebase.utility.Resource;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MessageViewModel extends ViewModel {
    private final MessageRepository messageRepository;
    private MediatorLiveData<Resource<List<MessageWithUserDetail>>> messagesWithDetails;
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
    public LiveData<Resource<List<MessageWithUserDetail>>> observeMessages(String chatRoomId) {
        if (messagesWithDetails == null) {
            messagesWithDetails = new MediatorLiveData<>();
            messagesWithDetails.setValue(Resource.loading(null));

            LiveData<Resource<List<Message>>> messagesLivedata = messageRepository.observeMessages(chatRoomId, currentUserId);
            LiveData<Resource<List<User>>> usersLivedata = userRepository.getUsersInChatRoom(chatRoomId);

            messagesWithDetails.addSource(messagesLivedata, messagesResource -> {
                if (messagesResource.getStatus() == Resource.Status.SUCCESS && usersLivedata.getValue() != null && usersLivedata.getValue().getStatus() == Resource.Status.SUCCESS) {
                    combineMessagesAndUsers(messagesResource.getData(), usersLivedata.getValue().getData());
                } else if (messagesResource.getStatus() == Resource.Status.ERROR) {
                    messagesWithDetails.setValue(Resource.error(messagesResource.getMessage(), null));
                }
            });

            messagesWithDetails.addSource(usersLivedata, usersResource -> {
                if (usersResource.getStatus() == Resource.Status.SUCCESS && messagesLivedata.getValue() != null && messagesLivedata.getValue().getStatus() == Resource.Status.SUCCESS) {
                    combineMessagesAndUsers(messagesLivedata.getValue().getData(), usersResource.getData());
                } else if (usersResource.getStatus() == Resource.Status.ERROR) {
                    messagesWithDetails.setValue(Resource.error(usersResource.getMessage(), null));
                }
            });
        }
        return messagesWithDetails;
    }

    private void combineMessagesAndUsers(List<Message> messages, List<User> users) {
        if (messages == null || users == null) {
            messagesWithDetails.setValue(Resource.error("Data is not available", null));
            return;
        }

        List<MessageWithUserDetail> messagesWithDetailsList = new ArrayList<>();

        for (Message message : messages) {
            User sender = findUserById(users, message.getSenderId());

            messagesWithDetailsList.add(new MessageWithUserDetail(
                    message.getMessageId(),
                    message.getChatRoomId(),
                    message.getSenderId(),
                    message.getMessageContent(),
                    message.getMessageType(),
                    message.getTimestamp(),
                    message.getReadBy(),
                    sender != null ? sender.getUsername() : "Unknown"
            ));
        }

        messagesWithDetails.setValue(Resource.success(messagesWithDetailsList));
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

    private User findUserById(List<User> users, String userId) {
        for (User user : users) {
            if (user.getUserId().equals(userId)) {
                return user;
            }
        }
        return null;
    }


}
