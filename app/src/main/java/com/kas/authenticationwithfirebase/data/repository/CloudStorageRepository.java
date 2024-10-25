package com.kas.authenticationwithfirebase.data.repository;


import android.graphics.Bitmap;
import android.net.Uri;

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

        StorageReference fileRef = firebaseStorage.getReference().child(folderName + "/" + fileName);
        fileRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    result.setValue(Resource.success(uri.toString()));
                }))
                .addOnFailureListener(e -> result.setValue(Resource.error(e.getMessage(), null)));

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
