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

    private List<Long> mRoomsSize;

    private  List<Message> testMessages = new ArrayList<>();;
    public ChatsRepository(Context context) {
        Log.d(TAG, "ChatsRepository: constructor is called");
        mUserChats = new ArrayList<>();
        mContext = context;
        mCurrentUserId = FirebaseAuth.getInstance().getUid();
        if (mCurrentUserId != null) {
            mChatsReference = FirebaseDatabase.getInstance().getReference("rooms")
                    .child(mCurrentUserId);
        }


    }


    public void initList() {
        Log.d(TAG, "initList: called");
        if (this.mRoomsSize == null)
            this.mRoomsSize = new ArrayList<>();
    }

    public void setDataReadyInterface(DataReadyInterface mDataReadyInterface) {
        this.mDataReadyInterface = mDataReadyInterface;
    }

    public void loadAllChats() {
        Log.d(TAG, "loadAllChats: adding event listener");
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
            Log.d(TAG, "refreshData: children count: " + roomsSnapshot.getChildrenCount());
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
        if (mRoomsSize.size() == 0)
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
                /*currentList.add();*/
               /* Iterable<DataSnapshot> messagesIterable = roomsSnapshot.getChildren();
                DataSnapshot snapShot = null;
                for (DataSnapshot messageSnapShot : messagesIterable) {
                    snapShot = messageSnapShot;
                }*/
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

    public interface DataReadyInterface {
        void dataIsReady();
    }

    public Message getMessageInPosition(int position) {
        return mUserChats.get(position);
        /*return testMessages.get(position);*/
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
        mChatsReference.removeEventListener(this);
    }

    public List<Message> getTestData () {
        // String text, String photoUrl, long timestamp, String senderId, String targetId, String senderName, String targetName, String targetPhotoUrl, boolean isRead

        testMessages.add(new Message("Hi there", "no photo", 1938372733,
                "kfkdj83839", "8838kdkfjd8", "Ali Hassan", "Esam Hassan", "no", false ));
        testMessages.add(new Message("Anything just test", "no photo", 1938372733,
                "kfkdj83839", "8838kdkfjd8", "Ali Hassan", "Esam Hassan", "no", true ));
        testMessages.add(new Message("test demo", "no photo", 1938372733,
                "kfkdj83839", "8838kdkfjd8", "Ali Hassan", "Esam Hassan", "no", false ));
        return testMessages;
    }
}
