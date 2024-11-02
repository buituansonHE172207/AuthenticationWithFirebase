package com.kas.authenticationwithfirebase.utility;


import android.app.Activity;
import android.net.Uri;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia;
import androidx.appcompat.app.AppCompatActivity;

public class FileManager {
    private final AppCompatActivity activity;
    private final FilePickerCallback callback;
    private final ActivityResultLauncher<PickVisualMediaRequest> pickMediaLauncher;

    // Giao diện callback để truyền URI về component gọi
    public interface FilePickerCallback {
        void onFilePicked(Uri uri);
        void onNoFileSelected();
    }

    // Khởi tạo FileManager với callback
    public FileManager(AppCompatActivity activity, FilePickerCallback callback) {
        this.activity = activity;
        this.callback = callback;
        this.pickMediaLauncher = initFilePicker();
    }

    // Phương thức khởi tạo ActivityResultLauncher
    private ActivityResultLauncher<PickVisualMediaRequest> initFilePicker() {
        return activity.registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                Log.d("FileManager", "Selected URI: " + uri);
                callback.onFilePicked(uri);
            } else {
                Log.d("FileManager", "No media selected");
                callback.onNoFileSelected();
            }
        });
    }

    // Phương thức chọn ảnh và video
    public void pickImageAndVideo() {
        pickMediaLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(PickVisualMedia.ImageAndVideo.INSTANCE)
                .build());
    }

    // Phương thức chọn chỉ ảnh
    public void pickImageOnly() {
        pickMediaLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    // Phương thức chọn chỉ video
    public void pickVideoOnly() {
        pickMediaLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(PickVisualMedia.VideoOnly.INSTANCE)
                .build());
    }

    // Phương thức chọn MIME type cụ thể
    public void pickSpecificMimeType(String mimeType) {
        pickMediaLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(new PickVisualMedia.SingleMimeType(mimeType))
                .build());
    }

    public static String getFileNameFromUri(Uri uri) {
        return uri.getLastPathSegment();
    }
}

