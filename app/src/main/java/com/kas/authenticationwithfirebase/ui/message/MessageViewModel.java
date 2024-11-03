package com.kas.authenticationwithfirebase.ui.message;

import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.kas.authenticationwithfirebase.data.entity.Media;
import com.kas.authenticationwithfirebase.data.entity.Message;
import com.kas.authenticationwithfirebase.data.entity.User;
import com.kas.authenticationwithfirebase.data.model.MessageWithUserDetail;
import com.kas.authenticationwithfirebase.data.repository.AuthRepository;
import com.kas.authenticationwithfirebase.data.repository.ChatRoomRepository;
import com.kas.authenticationwithfirebase.data.repository.CloudStorageRepository;
import com.kas.authenticationwithfirebase.data.repository.MediaRepository;
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
    private final CloudStorageRepository cloudStorageRepository;

    private final MutableLiveData<Boolean> isSendingMessage = new MutableLiveData<>(false);

    @Inject
    public MessageViewModel(MessageRepository messageRepository,
                            AuthRepository authRepository,
                            ChatRoomRepository chatRoomRepository,
                            UserRepository userRepository,
                            CloudStorageRepository cloudStorageRepository) {
        this.messageRepository = messageRepository;
        this.currentUserId = authRepository.getCurrentUserId();
        this.chatRoomRepository = chatRoomRepository;
        this.userRepository = userRepository;
        this.cloudStorageRepository = cloudStorageRepository;
    }

    public LiveData<Boolean> isSendingMessage() {
        return isSendingMessage;
    }

    private <T> LiveData<Resource<T>> checkUserLoggedIn(LiveData<Resource<T>> successLiveData) {
        if (currentUserId == null) {
            MutableLiveData<Resource<T>> errorResult = new MutableLiveData<>();
            errorResult.setValue(Resource.error("User not logged in", null));
            return errorResult;
        }
        return successLiveData;
    }

    // Phương thức để gửi tin nhắn văn bản
    public LiveData<Resource<Message>> sendTextMessage(String chatRoomId, Message message) {
        message.setSenderId(currentUserId);
        LiveData<Resource<Message>> result = checkUserLoggedIn(
                messageRepository.sendMessage(chatRoomId, message)
        );
        chatRoomRepository.updateLastMessage(message);
        return result;
    }

    // Phương thức để gửi tin nhắn ảnh
    public LiveData<Resource<Message>> sendImageMessage(String chatRoomId, Uri imageUri, Message message) {
        isSendingMessage.setValue(true);

        MutableLiveData<Resource<Message>> result = new MutableLiveData<>();

        // Upload the image to cloud storage
        cloudStorageRepository.uploadFile("messages_image", imageUri.getLastPathSegment(), imageUri)
                .observeForever(resource -> {
                    if (resource.getStatus() == Resource.Status.SUCCESS) {
                        // Image upload successful, set the message content to the image URL
                        message.setMessageContent(resource.getData());
                        Log.d("MessageViewModel", "Image URL: " + message.getMessageContent());

                        // Now send the text message with the image URL
                        message.setSenderId(currentUserId);
                        messageRepository.sendMessage(chatRoomId, message).observeForever(sendResult -> {
                            isSendingMessage.setValue(false);
                            if (sendResult.getStatus() == Resource.Status.SUCCESS) {
                                result.setValue(Resource.success(sendResult.getData()));

                                // Update the message observer to ensure it triggers for the new message
                                observeMessages(chatRoomId);  // This line ensures real-time updates resume after sending
                            } else {
                                result.setValue(Resource.error(sendResult.getMessage(), null));
                            }
                        });
                    } else {
                        isSendingMessage.setValue(false);
                        result.setValue(Resource.error("Failed to send image message", null));
                    }
                });

        // Update the last message in the chat room
        chatRoomRepository.updateLastMessage(message);
        return result;
    }



    // Quan sát tin nhắn
    public LiveData<Resource<List<MessageWithUserDetail>>> observeMessages(String chatRoomId) {
        if (messagesWithDetails == null) {
            messagesWithDetails = new MediatorLiveData<>();
            messagesWithDetails.setValue(Resource.loading(null));

            LiveData<Resource<List<Message>>> messagesLivedata = messageRepository.observeMessages(chatRoomId, currentUserId);
            LiveData<Resource<List<User>>> usersLivedata = userRepository.getUsersInChatRoom(chatRoomId);

            messagesWithDetails.addSource(messagesLivedata, messagesResource -> {
                if (messagesResource.getStatus() == Resource.Status.SUCCESS
                        && usersLivedata.getValue() != null
                        && usersLivedata.getValue().getStatus() == Resource.Status.SUCCESS) {
                    combineMessagesAndUsers(messagesResource.getData(), usersLivedata.getValue().getData());
                } else if (messagesResource.getStatus() == Resource.Status.ERROR) {
                    messagesWithDetails.setValue(Resource.error(messagesResource.getMessage(), null));
                }
            });

            messagesWithDetails.addSource(usersLivedata, usersResource -> {
                if (usersResource.getStatus() == Resource.Status.SUCCESS
                        && messagesLivedata.getValue() != null
                        && messagesLivedata.getValue().getStatus() == Resource.Status.SUCCESS) {
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
                    sender != null ? sender.getUsername() : "Unknown",
                    sender != null ? sender.getProfileImageUrl() : ""
            ));
        }

        messagesWithDetails.setValue(Resource.success(messagesWithDetailsList));
    }

    private User findUserById(List<User> users, String userId) {
        for (User user : users) {
            if (user.getUserId().equals(userId)) {
                return user;
            }
        }
        return null;
    }

    // Các phương thức khác như đánh dấu đã đọc, xóa tin nhắn, lấy thông tin người dùng
    public LiveData<Resource<Boolean>> markMessageAsRead(String chatRoomId, String messageId) {
        return checkUserLoggedIn(
                messageRepository.markMessageAsRead(chatRoomId, messageId, currentUserId)
        );
    }

    public LiveData<Resource<Boolean>> deleteMessages(String chatRoomId) {
        return messageRepository.deleteMessages(chatRoomId);
    }

    public LiveData<Resource<Boolean>> deleteMessage(String chatRoomId, String messageId) {
        return checkUserLoggedIn(
                messageRepository.deleteMessage(chatRoomId, messageId, currentUserId)
        );
    }

    public LiveData<Resource<User>> getUserProfile(String userId) {
        return userRepository.getUserProfile(userId);
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public void removeMessagesListener(String chatRoomId) {
        messageRepository.removeMessageListener(chatRoomId);
    }
}
