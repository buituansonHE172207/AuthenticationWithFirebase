package com.kas.authenticationwithfirebase.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kas.authenticationwithfirebase.data.model.Message;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class ChatRepository {
    private final DatabaseReference databaseReference;


    @Inject
    public ChatRepository(FirebaseDatabase firebaseDatabase) {
        this.databaseReference = firebaseDatabase.getReference("chats");
    }

    public void sendMessage(Message message) {
        databaseReference.push().setValue(message);
    }

    public LiveData<List<Message>> getMessages() {
        MutableLiveData<List<Message>> messagesLiveData = new MutableLiveData<>();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Message> messages = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);
                    messages.add(message);
                }
                messagesLiveData.setValue(messages);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                messagesLiveData.setValue(null);
            }
        });
        return messagesLiveData;
    }


}
