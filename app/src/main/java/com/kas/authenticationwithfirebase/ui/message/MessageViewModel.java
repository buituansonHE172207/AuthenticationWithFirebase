package com.kas.authenticationwithfirebase.ui.message;

import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.auth.FirebaseAuth;
import com.kas.authenticationwithfirebase.data.entity.Message;
import com.kas.authenticationwithfirebase.data.entity.User;
import com.kas.authenticationwithfirebase.data.model.MessageWithUserDetail;
import com.kas.authenticationwithfirebase.data.repository.AuthRepository;
import com.kas.authenticationwithfirebase.data.repository.ChatRoomRepository;
import com.kas.authenticationwithfirebase.data.repository.CloudStorageRepository;
import com.kas.authenticationwithfirebase.data.repository.MessageRepository;
import com.kas.authenticationwithfirebase.data.repository.UserRepository;
import com.kas.authenticationwithfirebase.service.FcmApi;
import com.kas.authenticationwithfirebase.service.NotificationBody;
import com.kas.authenticationwithfirebase.service.SendMessageDto;
import com.kas.authenticationwithfirebase.utility.Resource;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@HiltViewModel
public class MessageViewModel extends ViewModel {
    private static final String MESSAGING_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
    private static final String[] SCOPES = { MESSAGING_SCOPE };
    private final MessageRepository messageRepository;
    private MediatorLiveData<Resource<List<MessageWithUserDetail>>> messagesWithDetails;
    private final String currentUserId;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final CloudStorageRepository cloudStorageRepository;

    private final MutableLiveData<Boolean> isSendingMessage = new MutableLiveData<>(false);


    //private ChatState state = new ChatState();
    private final FcmApi fcmApi;

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

        fcmApi = new Retrofit.Builder()
                //.baseUrl("http://10.0.2.2:8080/") // URL cơ bản cho FCM chi dung dc voi emulator
                .baseUrl("https://fcm.googleapis.com/")  // Dung cai nay thi phai sua URL endpoint trong FcmApi thanh @POST("v1/projects/{project_id}/messages:send")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(FcmApi.class);

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
        result.observeForever(sendResult -> {
            if (sendResult.getStatus() == Resource.Status.SUCCESS) {
                // Gửi thông báo qua FCM sau khi tin nhắn được gửi thành công
                sendNotificationToRecipient(message, chatRoomId);
            }
        });
        chatRoomRepository.updateLastMessage(message);
        return result;
    }
    private void sendNotificationToRecipient(Message message, String chatRoomId) {
        String tokenTest = "cyICIKhsSQStFnga7Co1HR:APA91bG-jZl0ZPN-fq_n5sMIcbBXqx0-fKea1gvCgJp3kDIPw-pr_cRmbFnwwJKmCpoyEWd-diFOzPPRpfI3PuvNWr2Tbdz45solxYMnbgZ7ot_iTg0s54k";
        // Thông tin thông báo
        NotificationBody notificationBody = new NotificationBody(
                "New message!",
                message.getMessageContent()
        );

        // Thiết lập đối tượng `SendMessageDto`
        SendMessageDto sendMessageDto = new SendMessageDto(
                tokenTest, // Thay bằng token của người nhận tin nhắn, lấy từ chatRoomId
                notificationBody
        );

        // Gửi yêu cầu thông qua FcmApi
        new Thread(() -> {
//            try {
//                fcmApi.sendMessage(sendMessageDto).execute();
//                Log.d("MessageViewModel", "Notification sent successfully.");
//            } catch (Exception e) {
//                Log.e("MessageViewModel", "Failed to send notification: " + e.getMessage());
//            }
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
//            String token = null;
//            try {
//                token = getAccessToken();
//            } catch (IOException e) {
//                Log.d("FCM1","GetAccessToken:"+e.getMessage());
//                throw new RuntimeException(e);
//            }
            String token = "ya29.a0AeDClZA_t_NAiPzua1VDBVIYKJ3HPx5wuz89XcjqoJ2yHxwGqqw7JV6OumynK7Sl9nA3_az5do7jy_UHD7nW-Hvx1xwTkE_Yj2WRQk75ixPUeKLqoDjPlIw9gZFFQnr0napGWAxDon0Rs_Q9h_1At1Ysxq0tDSWVx-FwgA4SaCgYKAeUSARESFQHGX2MiogN9J5w280nuCXg6tY4zlw0175";
            String authToken = "Bearer "+ token;

            String projectId = "kas1407";

            // Tạo payload cho FCM
            JSONObject payload = new JSONObject();
            try {
                JSONObject messageObject = new JSONObject();
                messageObject.put("token", tokenTest);
                JSONObject notificationObject = new JSONObject();
                notificationObject.put("title", notificationBody.getTitle());
                notificationObject.put("body", notificationBody.getBody());
                messageObject.put("notification", notificationObject);
                payload.put("message", messageObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            fcmApi.sendMessage(authToken, projectId, sendMessageDto).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Log.d("FCM1", "Message sent successfully!");
                    } else {
                        try {
                            // In ra chi tiết lỗi
                            String errorResponse = response.errorBody().string();
                            Log.e("FCM1", "Failed to send message: " + errorResponse);
                        } catch (IOException e) {
                            Log.e("FCM1", "Failed to read error response: " + e.getMessage());
                        }
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e("FCM1", "Error: " + t.getMessage());
                }
            });
        }).start();
    }
    private static String getAccessToken() throws IOException {
        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new FileInputStream("service-account.json"))
                .createScoped(Arrays.asList(SCOPES));
        googleCredentials.refresh();
        return googleCredentials.getAccessToken().getTokenValue();
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
