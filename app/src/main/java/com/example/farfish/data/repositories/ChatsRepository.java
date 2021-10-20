package com.example.farfish.data.repositories;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.farfish.Module.Message;
import com.example.farfish.Module.NotificationUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatsRepository implements ValueEventListener {
    private static final String TAG = ChatsRepository.class.getSimpleName();
    private DatabaseReference mChatsReference;
    private List<Message> mUserChats;
    private String mCurrentUserId;
    private Context mContext;
    private DataReadyInterface mDataReadyInterface;
    private boolean userShouldBeNotified = true;

    public ChatsRepository(Context context) {
        mUserChats = new ArrayList<>();
        mContext = context;
        mCurrentUserId = FirebaseAuth.getInstance().getUid();
        if (mCurrentUserId != null) {
            mChatsReference = FirebaseDatabase.getInstance().getReference("rooms")
                    .child(mCurrentUserId);
        }
    }

    public void setDataReadyInterface(DataReadyInterface mDataReadyInterface) {
        this.mDataReadyInterface = mDataReadyInterface;
    }

    public void loadAllChats() {
        Log.d(TAG, "loadAllChats: adding event listener");
        mChatsReference.addValueEventListener(this);
    }

    public void refreshData(@NonNull DataSnapshot snapshot) {
        mUserChats.clear();
        Iterable<DataSnapshot> roomsIterable = snapshot.getChildren();
        for (DataSnapshot roomsSnapshot : roomsIterable) {

            Iterable<DataSnapshot> messagesIterable = roomsSnapshot.getChildren();
            Message lastMessage = null;
            int newMessageCounter = 0;
            for (DataSnapshot messageSnapShot : messagesIterable) {
                if (!messageSnapShot.getKey().equals("isWriting")) {
                    lastMessage = messageSnapShot.getValue(Message.class);
                    if (!lastMessage.getIsRead())
                        newMessageCounter++;
                }
            }
            if (lastMessage != null) {
                String senderId = lastMessage.getSenderId();
                if (mCurrentUserId != null) {
                    if (!senderId.equals(mCurrentUserId) && newMessageCounter != 0)
                        lastMessage.setNewMessagesCount(newMessageCounter);
                }
                mUserChats.add(lastMessage);
                sendNotification(lastMessage);
            }
        }
        // the data is ready now
        mDataReadyInterface.dataIsReady();
    }

    public List<Message> getUserChats() {
        return mUserChats;
    }

    public boolean isUserShouldBeNotified() {
        return userShouldBeNotified;
    }

    public void setUserShouldBeNotified(boolean userShouldBeNotified) {
        this.userShouldBeNotified = userShouldBeNotified;
        Log.d(TAG, "setUserShouldBeNotified: " + userShouldBeNotified);
    }

    public void sendNotification( Message newMessage) {
        Log.d(TAG, "sendNotification: user should be notified: " + userShouldBeNotified);
        if (userShouldBeNotified)
            NotificationUtils.notifyUserOfNewMessage(mContext, newMessage);
    }

    public interface DataReadyInterface {
        void dataIsReady();
    }

    public Message getMessageInPosition(int position) {
        return mUserChats.get(position);
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
        refreshData(snapshot);
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }

    public void removeValueEventListener() {
        mChatsReference.removeEventListener(this);
    }
}
