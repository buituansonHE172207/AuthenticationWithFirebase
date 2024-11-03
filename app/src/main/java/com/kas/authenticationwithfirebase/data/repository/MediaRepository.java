package com.kas.authenticationwithfirebase.data.repository;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kas.authenticationwithfirebase.data.entity.Media;
import com.kas.authenticationwithfirebase.utility.Resource;

import javax.inject.Inject;

public class MediaRepository {

    private final FirebaseStorage firebaseStorage;
    private final DatabaseReference mediaRef;

    private static final String DEFAULT_FOLDER_NAME = "chat_images"; // Thư mục mặc định cho ảnh

    @Inject
    public MediaRepository(FirebaseStorage firebaseStorage, FirebaseDatabase firebaseDatabase) {
        this.firebaseStorage = firebaseStorage;
        this.mediaRef = firebaseDatabase.getReference("media");
    }

    public LiveData<Resource<String>> uploadMedia(String messageId, Uri fileUri, Media media) {
        MutableLiveData<Resource<String>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        // Sử dụng `messageId` làm `mediaId` và lưu trữ ảnh trong thư mục mặc định
        StorageReference fileRef = firebaseStorage.getReference().child(DEFAULT_FOLDER_NAME + "/" + messageId);

        fileRef.putFile(fileUri)
                .continueWithTask(task -> fileRef.getDownloadUrl()) // Nhận URL sau khi tải lên thành công
                .addOnSuccessListener(uri -> {
                    media.setUrl(uri.toString());
                    media.setMediaId(messageId); // Dùng `messageId` làm `mediaId`

                    mediaRef.child(messageId).setValue(media)
                            .addOnSuccessListener(aVoid -> result.setValue(Resource.success(uri.toString())))
                            .addOnFailureListener(e -> result.setValue(Resource.error("Failed to save metadata: " + e.getMessage(), null)));
                })
                .addOnFailureListener(e -> result.setValue(Resource.error("Failed to upload file: " + e.getMessage(), null)));

        return result;
    }
}
