package com.kas.authenticationwithfirebase.utility;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CameraManager {
    private final Context context;
    private final CamaraCallBack callBack;
    private String currentMediaPath;
    private static final int REQUEST_CODE_IMAGE_CAPTURE = 2001;
    private static final int REQUEST_CODE_VIDEO_CAPTURE = 2002;

    public interface CamaraCallBack {
        void onMediaCaptured(Uri mediaUri, boolean isVideo);
        void onError(String error);
    }

    public CameraManager(Context context, CamaraCallBack callBack) {
        this.context = context;
        this.callBack = callBack;
    }

    public void openCameraForPhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
            File photoFile = createMediaFile("IMG_", ".jpg");
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(context, "com.kas.authenticationwithfirebase.imageprovier", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                if (context instanceof Activity) {
                    ((Activity) context).startActivityForResult(takePictureIntent, REQUEST_CODE_IMAGE_CAPTURE);
                }
            } else {
                callBack.onError("Error creating media file");
            }
        }
    }

    public void openCameraForVideo() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            File videoFile = createMediaFile("VID_", ".mp4");
            if (videoFile != null) {
                Uri videoURI = FileProvider.getUriForFile(context, "com.kas.authenticationwithfirebase.videoprovider", videoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, videoURI);
                if (context instanceof Activity) {
                    ((Activity) context).startActivityForResult(intent, REQUEST_CODE_VIDEO_CAPTURE);
                }
            } else {
                callBack.onError("Could not create file for capturing video");
            }
        }
    }

    private File createMediaFile(String prefix, String suffix) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = prefix + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File mediaFile = null;
        try {
            mediaFile = File.createTempFile(fileName, suffix, storageDir);
            currentMediaPath = mediaFile.getAbsolutePath();
        } catch (IOException e) {
            callBack.onError("Error creating media file: " + e.getMessage());
        }
        return mediaFile;
    }
}
