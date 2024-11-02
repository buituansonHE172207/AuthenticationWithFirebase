package com.kas.authenticationwithfirebase.data.repository;


import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kas.authenticationwithfirebase.utility.Resource;

import java.io.ByteArrayOutputStream;

import javax.inject.Inject;

public class CloudStorageRepository {
    private final FirebaseStorage firebaseStorage;

    @Inject
    public CloudStorageRepository(FirebaseStorage firebaseStorage) {
        this.firebaseStorage = firebaseStorage;
    }

    public LiveData<Resource<String>> uploadFile(String folderName, String fileName, Uri fileUri) {
        MutableLiveData<Resource<String>> result = new MutableLiveData<>();
        //log the input
        Log.d("CloudStorageRepository", "Uploading file: " + fileName + " to folder: " + folderName);


        StorageReference fileRef = firebaseStorage.getReference().child(folderName + "/" + fileName);
        fileRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    result.setValue(Resource.success(uri.toString()));
                }))
                .addOnFailureListener(e -> result.setValue(Resource.error(e.getMessage(), null)));
        // log if failed
        if (result.getValue() != null && result.getValue().getStatus() == Resource.Status.ERROR) {
            Log.d("CloudStorageRepository", "Failed to upload file: " + result.getValue().getMessage());
        }
        // log if success]
        if (result.getValue() != null && result.getValue().getStatus() == Resource.Status.SUCCESS) {
            Log.d("CloudStorageRepository", "File uploaded successfully: " + result.getValue().getData());
        }
        return result;
    }

    public LiveData<Resource<String>> getFileUrl(String folderName, String fileName) {
        MutableLiveData<Resource<String>> result = new MutableLiveData<>();

        StorageReference fileRef = firebaseStorage.getReference().child(folderName + "/" + fileName);
        fileRef.getDownloadUrl()
                .addOnSuccessListener(uri -> result.setValue(Resource.success(uri.toString())))
                .addOnFailureListener(e -> result.setValue(Resource.error(e.getMessage(), null)));

        return result;
    }
}
