package com.example.farfish.data.repositories;

import android.content.Context;

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
    private static volatile List<Message> mUserChats = new ArrayList<>();

    @Inject
    public ChatsRepository(@ApplicationContext Context context) {
        mContext = context;
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
        List<Message> messages = new ArrayList<>();
        boolean isTheSame = false;
        int index = 0;
        Iterable<DataSnapshot> roomsIterable = snapshot.getChildren();
        for (DataSnapshot roomsSnapshot : roomsIterable) {
            Iterable<DataSnapshot> messagesIterable = roomsSnapshot.getChildren();
            Message lastMessage = null;
            int newMessageCounter = 0;
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
                if (index < mUserChats.size()) {
                    isTheSame = check(mUserChats.get(index), lastMessage);
                    if (!isTheSame) {
                        mDataAreTheSame = false;
                        ChatsFragment.IS_DATA_THE_SAME = false;
                    }
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

            }
        }

        if (mUserChats.isEmpty() || !mDataAreTheSame || mUserChats.size() != messages.size()) {
            mUserChats.clear();
            mUserChats.addAll(messages);
            // the data is ready now
            mDataReadyInterface.dataIsReady();
            /* mDataAreTheSame = false;*/
        }
        mDataAreTheSame = true;
    }

    private boolean check(Message message, Message lastMessage) {

        return message.getTimestamp() == lastMessage.getTimestamp()
                && message.getIsRead() == lastMessage.getIsRead();
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
        if (mChatsReference != null)
            mChatsReference.removeEventListener(this);

    }

    public interface DataReadyInterface {
        void dataIsReady();
    }

}
