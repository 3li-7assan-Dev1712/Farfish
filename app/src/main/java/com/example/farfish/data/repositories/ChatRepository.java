package com.example.farfish.data.repositories;

import android.content.Context;
import androidx.annotation.NonNull;
import com.example.farfish.Module.Message;
import com.example.farfish.Module.MessagesPreference;
import com.example.farfish.Module.NotificationUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class ChatRepository implements ValueEventListener {
    private DatabaseReference mChatsReference;
    private List<Message> mUserChats;
    private String mCurrentUserId;
    private Context mContext;
    private DataReadyInterface mDataReadyInterface;
    public ChatRepository(Context context, DataReadyInterface dataReadyInterface) {
        mUserChats = new ArrayList<>();
        this.mDataReadyInterface = dataReadyInterface;
        mContext = context;
        mCurrentUserId = MessagesPreference.getUserId(context);
        mChatsReference = FirebaseDatabase.getInstance().getReference("rooms")
                .child(mCurrentUserId);
    }

    public void loadAllChats() {
        mChatsReference.addValueEventListener(this);
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
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
                if (!senderId.equals(mCurrentUserId) && newMessageCounter != 0)
                    lastMessage.setNewMessagesCount(newMessageCounter);
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

    private void sendNotification(Message lastMessage) {
        NotificationUtils.notifyUserOfNewMessage(mContext, lastMessage);
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }

    public interface DataReadyInterface {
        void dataIsReady();
    }
}
