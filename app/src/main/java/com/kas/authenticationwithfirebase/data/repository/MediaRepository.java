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

    @Inject
    public MediaRepository(FirebaseStorage firebaseStorage) {
        this.firebaseStorage = firebaseStorage;
        this.mediaRef = FirebaseDatabase.getInstance().getReference("media");
    }

    public LiveData<Resource<String>> uploadMedia(String folderName, String fileName, Uri fileUri, Media media) {
        MutableLiveData<Resource<String>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        StorageReference fileRef = firebaseStorage.getReference().child(folderName + "/" + fileName);
        fileRef.putFile(fileUri)
                .continueWithTask(task -> fileRef.getDownloadUrl()) // Get download URL directly after upload
                .addOnSuccessListener(uri -> {
                    media.setUrl(uri.toString());
                    mediaRef.child(media.getMediaId()).setValue(media)
                            .addOnSuccessListener(aVoid -> result.setValue(Resource.success(uri.toString())))
                            .addOnFailureListener(e -> result.setValue(Resource.error("Failed to save metadata: " + e.getMessage(), null)));
                })
                .addOnFailureListener(e -> result.setValue(Resource.error("Failed to upload file: " + e.getMessage(), null)));

        return result;
    }

    public LiveData<Resource<Media>> getMedia(String mediaId) {
        MutableLiveData<Resource<Media>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        mediaRef.child(mediaId).get()
                .addOnSuccessListener(dataSnapshot -> {
                    Media media = dataSnapshot.getValue(Media.class);
                    result.setValue(Resource.success(media));
                })
                .addOnFailureListener(e -> result.setValue(Resource.error("Failed to retrieve media: " + e.getMessage(), null)));
        return result;
    }

    public LiveData<Resource<Boolean>> deleteMedia(String mediaId, String mediaUrl) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(false));

        // Delete media metadata in Database
        mediaRef.child(mediaId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    firebaseStorage.getReferenceFromUrl(mediaUrl).delete()
                            .addOnSuccessListener(aVoid1 -> result.setValue(Resource.success(true)))
                            .addOnFailureListener(e -> result.setValue(Resource.error("Failed to delete file from storage: " + e.getMessage(), false)));
                })
                .addOnFailureListener(e -> result.setValue(Resource.error("Failed to delete metadata: " + e.getMessage(), false)));

        return result;
    }
}
