package com.example.farfish.data.repositories;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.farfish.Module.Message;
import com.example.farfish.Module.MessagesPreference;
import com.example.farfish.Module.NotificationUtils;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        mCurrentUserId = MessagesPreference.getUserId(context);
        mChatsReference = FirebaseDatabase.getInstance().getReference("rooms")
                .child(mCurrentUserId);
    }

    public void setDataReadyInterface(DataReadyInterface mDataReadyInterface) {
        this.mDataReadyInterface = mDataReadyInterface;
    }

    public void loadAllChats() {
        mChatsReference.addValueEventListener(this);
    }

/*    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
        refreshData(snapshot);
    }*/

    private void refreshData(@NonNull DataSnapshot snapshot) {
        mUserChats.clear();
        Log.d(TAG, "onDataChange: key: " + snapshot.getKey());
        Log.d(TAG, "onDataChange: reference: " + snapshot.getRef().toString());
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
                if (!senderId.equals(mCurrentUserId) && newMessageCounter != 0)
                    lastMessage.setNewMessagesCount(newMessageCounter);
                mUserChats.add(lastMessage);
                sendNotification(userShouldBeNotified, lastMessage);
            }
        }
        // the data is ready now
        mDataReadyInterface.dataIsReady();
    }

    public List<Message> getUserChats() {
        return mUserChats;
    }

    public void setUserShouldBeNotified(boolean userShouldBeNotified) {
        this.userShouldBeNotified = userShouldBeNotified;
    }

    public void sendNotification(boolean userShouldBeNotified, Message newMessage) {
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
