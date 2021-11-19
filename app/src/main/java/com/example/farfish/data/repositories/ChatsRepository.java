package com.example.farfish.data.repositories;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.farfish.Adapters.MessagesListAdapter;
import com.example.farfish.Module.dataclasses.Message;
import com.example.farfish.Module.preferences.MessagesPreference;
import com.example.farfish.Module.util.NotificationUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;

public class ChatsRepository implements ValueEventListener {
    private static final String TAG = ChatsRepository.class.getSimpleName();
    private DatabaseReference mChatsReference;
    private List<Message> mUserChats;
    private String mCurrentUserId;
    private Context mContext;
    private DataReadyInterface mDataReadyInterface;
    private List<Long> mRoomsSize;


    @Inject
    public ChatsRepository(@ApplicationContext Context context) {
        mContext = context;
        mUserChats = new ArrayList<>();
        mRoomsSize = new ArrayList<>();
    }

    public void setDataReadyInterface(DataReadyInterface mDataReadyInterface) {
        this.mDataReadyInterface = mDataReadyInterface;
    }

    public void loadAllChats() {

        mCurrentUserId = MessagesPreference.getUserId(mContext);
        if (mCurrentUserId != null) {
            mChatsReference = FirebaseDatabase.getInstance().getReference("rooms")
                    .child(mCurrentUserId);
        }
        mChatsReference.addValueEventListener(this);
    }

    public void refreshData(@NonNull DataSnapshot snapshot) {
        Log.d(TAG, "refreshData: ");
        boolean shouldReturn = shouldReturn(snapshot);
        if (shouldReturn) {
            Log.d(TAG, "refreshData: should return is true");
            return;
        } else {
            Log.d(TAG, "refreshData: should return is false");
        }
        mUserChats.clear();
        mRoomsSize.clear();
        Iterable<DataSnapshot> roomsIterable = snapshot.getChildren();
        for (DataSnapshot roomsSnapshot : roomsIterable) {
            Iterable<DataSnapshot> messagesIterable = roomsSnapshot.getChildren();
            Message lastMessage = null;
            int newMessageCounter = 0;
            Log.d(TAG, "refreshData: roomsSnapShot key: " + roomsSnapshot.getKey());
            mRoomsSize.add(roomsSnapshot.getChildrenCount());
            for (DataSnapshot messageSnapShot : messagesIterable) {
                if (!messageSnapShot.getKey().equals("isWriting")) {
                    lastMessage = messageSnapShot.getValue(Message.class);
                    if (!lastMessage.getSenderId().equals(mCurrentUserId) && !lastMessage.getIsRead()) {
                        newMessageCounter++;
                    }
                }
            }

            if (lastMessage != null) {
                String senderId = lastMessage.getSenderId();
                if (mCurrentUserId != null) {
                    if (!senderId.equals(mCurrentUserId) && newMessageCounter != 0) {
                        lastMessage.setNewMessagesCount(newMessageCounter);
                        sendNotification(lastMessage);
                    }
                }
                mUserChats.add(lastMessage);

            }
        }
        // the data is ready now
        mDataReadyInterface.dataIsReady();
    }

    private boolean shouldReturn(DataSnapshot snapshot) {
        if (mRoomsSize.size() == 0 || mRoomsSize.size() == 1)
            return false;
        else {
            Iterable<DataSnapshot> roomsIterable = snapshot.getChildren();
            int index = 0;
            for (DataSnapshot roomsSnapshot : roomsIterable) {
                if (mRoomsSize.get(index) != roomsSnapshot.getChildrenCount()) {
                    Log.d(TAG, "shouldReturn: size from Room : " + mRoomsSize.get(index) + " size in current: " + roomsSnapshot.getChildrenCount());
                    return false;
                }
                index++;
            }
            return true;
        }
    }

    public List<Message> getUserChats() {
        return mUserChats;
    }


    public void sendNotification(Message newMessage) {
        NotificationUtils.notifyUserOfNewMessage(mContext, newMessage);
    }

    public Message getMessageInPosition(int position) {
        return mUserChats.get(position);
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
        Log.d(TAG, "onDataChange: ");
        refreshData(snapshot);
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }

    public void removeValueEventListener() {

        if (mChatsReference != null) {
            mChatsReference.removeEventListener(this);
//            cleanUp();
        }
    }

    private void cleanUp() {
        mChatsReference = null;
        mCurrentUserId = null;
        mDataReadyInterface = null;
    }

    public interface DataReadyInterface {
        void dataIsReady();
    }

}
