package com.kas.authenticationwithfirebase.utility;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

public class CameraManager {
    private final Context context;
    private final CamaraCallBack callBack;
    private Uri currentMediaUri;


    public interface CamaraCallBack {
        void onMediaCaptured(Uri mediaUri, boolean isVideo);
        void onError(String error);

        void onActivityResult(int requestCode, int resultCode, @Nullable Intent data);
    }

    public CameraManager(Context context, CamaraCallBack callBack) {
        this.context = context;
        this.callBack = callBack;
    }

    public void openCameraForPhoto() {
        if (checkAndRequestPermissions()) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
                currentMediaUri = createMediaStoreUri("IMG_", "image/jpeg", Environment.DIRECTORY_PICTURES);
                if (currentMediaUri != null) {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentMediaUri);
                    if (context instanceof Activity) {
                        ((Activity) context).startActivityForResult(takePictureIntent, RequestCodeManager.REQUEST_CODE_IMAGE_CAPTURE);
                    }
                } else {
                    callBack.onError("Error creating media URI");
                }
            } else {
                callBack.onError("No app found to handle the camera intent");
            }
        }
    }

    public void openCameraForVideo() {
        if (checkAndRequestPermissions()) {
            Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            if (takeVideoIntent.resolveActivity(context.getPackageManager()) != null) {
                currentMediaUri = createMediaStoreUri("VID_", "video/mp4", Environment.DIRECTORY_MOVIES);
                if (currentMediaUri != null) {
                    takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentMediaUri);
                    if (context instanceof Activity) {
                        ((Activity) context).startActivityForResult(takeVideoIntent, RequestCodeManager.REQUEST_CODE_VIDEO_CAPTURE);
                    }
                } else {
                    callBack.onError("Error creating media URI");
                }
            } else {
                callBack.onError("No app found to handle the video intent");
            }
        }
    }

    private Uri createMediaStoreUri(String prefix, String mimeType, String directory) {
        ContentValues values = new ContentValues();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, prefix + timeStamp);
        values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, directory + "/MyApp");

        Uri mediaUri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (mimeType.startsWith("image")) {
                mediaUri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else if (mimeType.startsWith("video")) {
                mediaUri = context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
            }
        }
        return mediaUri;
    }

    private boolean checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.CAMERA}, RequestCodeManager.REQUEST_CODE_CAMERA_PERMISSION);
            return false;
        }
        return true;
    }

    public Uri getCurrentMediaUri() {
        return currentMediaUri;
    }
}
