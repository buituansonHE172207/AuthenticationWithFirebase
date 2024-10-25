package com.kas.authenticationwithfirebase.utility;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.Settings;
import android.widget.Toast;

import java.io.OutputStream;

public class FileManager {
    public static final int REQUEST_CODE_MANAGE_STORAGE = 4001;
    public static Uri saveImageToMediaStore(Context context, String displayName, byte[] imageData) {
        Uri imageUri = null;

        // Set up content values for the new image
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, displayName);
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MyApp");

        // Insert the image in the MediaStore and get its URI
        Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        if (uri != null) {
            try (OutputStream outStream = context.getContentResolver().openOutputStream(uri)) {
                outStream.write(imageData);
                imageUri = uri;
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show();
            }
        }

        return imageUri;
    }

    // Request media write permissions if necessary (Android 11+)
    public static void requestWritePermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.setData(Uri.parse("package:" + activity.getPackageName()));
            activity.startActivityForResult(intent, REQUEST_CODE_MANAGE_STORAGE);
        }
    }
}