package com.example.farfish.data.repositories;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.farfish.Module.dataclasses.Message;
import com.example.farfish.Module.preferences.MessagesPreference;
import com.example.farfish.Module.util.NotificationUtils;
import com.example.farfish.fragments.chat.ChatsFragment;
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
    private DatabaseReference mChatsReference;
    public static volatile boolean mDataAreTheSame = true;
    private String mCurrentUserId;
    private Context mContext;
    private DataReadyInterface mDataReadyInterface;
    private List<Long> mRoomsSize;
    private static volatile List<Message> mUserChats = new ArrayList<>();

    @Inject
    public ChatsRepository(@ApplicationContext Context context) {
        Log.d("TAG", "constructor called !");
        mContext = context;

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
        boolean shouldReturn = shouldReturn(snapshot);
        List<Message> messages = new ArrayList<>();
        boolean isTheSame = false;
        int index = 0;
        Log.d("TAG", "shouldUpdate" + mDataAreTheSame);
//        if (!shouldReturn || shouldUpdate) {
//            mUserChats.clear();
        mRoomsSize.clear();
        Iterable<DataSnapshot> roomsIterable = snapshot.getChildren();
        for (DataSnapshot roomsSnapshot : roomsIterable) {
            Iterable<DataSnapshot> messagesIterable = roomsSnapshot.getChildren();
            Message lastMessage = null;
            int newMessageCounter = 0;
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
                messages.add(lastMessage);
                Log.d("TAG", "mUserChats size is " + mUserChats.size() + " hash code: " + mUserChats.hashCode());
                if (index < mUserChats.size()) {
                    isTheSame = check(mUserChats.get(index), lastMessage);
                    if (!isTheSame) {
                        mDataAreTheSame = false;
                        ChatsFragment.IS_DATA_THE_SAME = false;
                    }
                    Log.d("TAG", "text: " + lastMessage.getText() + " - " + mUserChats.get(index).getText());
                    Log.d("TAG", "is the same : " + isTheSame);
                }

                index++;
                String senderId = lastMessage.getSenderId();
                if (mCurrentUserId != null) {
                    if (!senderId.equals(mCurrentUserId) && newMessageCounter != 0)
                        lastMessage.setNewMessagesCount(newMessageCounter);
                    if (!senderId.equals(mCurrentUserId) && newMessageCounter != 0 && !isTheSame) {
                        sendNotification(lastMessage);
                    }
                }

//                    mUserChats.add(lastMessage);

            }
//            }

        }
        Log.d("TAG", "isTheSame value is: " + isTheSame);
        Log.d("TAG", "mUserChats size: " + mUserChats.size() + " messages: " + messages.size());
        Log.d("TAG", "final check: " + mDataAreTheSame);
        if (mUserChats.isEmpty() || !mDataAreTheSame || mUserChats.size() != messages.size()) {
            mUserChats.clear();
            mUserChats.addAll(messages);
            // the data is ready now
            Log.d("TAG", "data is ready");
            mDataReadyInterface.dataIsReady();
            /* mDataAreTheSame = false;*/
        }
        mDataAreTheSame = true;
    }

    private boolean check(Message message, Message lastMessage) {

        return message.getTimestamp() == lastMessage.getTimestamp()
                && message.getIsRead() == lastMessage.getIsRead();
    }

    private boolean shouldReturn(DataSnapshot snapshot) {
        if (mRoomsSize.size() == 0 || mRoomsSize.size() == 1)
            return false;
        else {
            Iterable<DataSnapshot> roomsIterable = snapshot.getChildren();
            int index = 0;
            for (DataSnapshot roomsSnapshot : roomsIterable) {
                if (mRoomsSize.get(index) != roomsSnapshot.getChildrenCount()) {
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
        refreshData(snapshot);
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }

    public void removeValueEventListener() {

        Log.d("TAG", "removeValueEventListener: ");
        if (mChatsReference != null) {
            mChatsReference.removeEventListener(this);
            Log.d("TAG", "removeValueEventListener: removed the listener successfully");
        }
    }

    public interface DataReadyInterface {
        void dataIsReady();
    }

}
