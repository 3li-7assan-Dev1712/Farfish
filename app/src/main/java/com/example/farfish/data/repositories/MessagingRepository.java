package com.example.farfish.data.repositories;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.farfish.Module.Connection;
import com.example.farfish.Module.Message;
import com.example.farfish.Module.User;
import com.example.farfish.databinding.ToolbarConversationBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MessagingRepository {
    /*TAG for logging*/
    private static final String TAG = MessagingRepository.class.getSimpleName();
    private MessagingInterface messagingInterface;
    private ToolbarConversationBinding mToolbarBinding;
    // functionality
    private List<Message> messages;
    private String mUsername;
    // firestore to get the user state wheater they're active or not
    private FirebaseFirestore mFirebasestore;
    // for sending and receiving photos
    private StorageReference mRootRef;
    // this tracker is used to invoke the method of the realtime database to update the user is writing once
    private int tracker = 0;
    // toolbar values
    private String targetUserId;
    // for target user profile in detail
    private Bundle targetUserData;
    /*chat info in upper toolbar*/
    private boolean isWriting;
    private boolean isActive;
    private long lastTimeSeen;
    // firebase realtime database
    private DatabaseReference mCurrentUserRoomReference;
    /*---------------------*/
    private DatabaseReference mTargetUserRoomReference;
    // current user info
    private String currentUserId;
    private String currentUserName;
    private String currentPhotoUrl;
    private Context mContext;
    // rooms listeners
    private CurrentRoomListener mCurrentRoomListener;
    private TargetRoomListener mTargetRoomListener;

    public MessagingRepository(Context context) {
        messages = new ArrayList<>();
        targetUserData = new Bundle();
        mContext = context;
        mCurrentRoomListener = new CurrentRoomListener();
        mTargetRoomListener = new TargetRoomListener();
        mFirebasestore = FirebaseFirestore.getInstance();
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setTargetUserId(String targetUserId) {
        this.targetUserId = targetUserId;
        currentUserId = FirebaseAuth.getInstance().getUid();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mCurrentUserRoomReference = database.getReference("rooms").child(currentUserId)
                .child(currentUserId + targetUserId);
        mTargetUserRoomReference = database.getReference("rooms").child(targetUserId)
                .child(targetUserId + currentUserId);
    }

    public void loadMessages() {
        prepareToolbarInfo();
        mCurrentUserRoomReference.addChildEventListener(mCurrentRoomListener);
        mTargetUserRoomReference.addChildEventListener(mTargetRoomListener);
    }

    /* this method is used in two functionality, for getting all the messages from a special room
     * and for adding new messages as the user sends. */
    private void addNewMessage(DataSnapshot value) {
        Log.d(TAG, "addNewMessage: ");

        try {
            Message newMessage = value.getValue(Message.class);
            assert newMessage != null;
            messages.add(newMessage);
            if (messages.size() > 0) {
                Log.d(TAG, "addNewMessage: messges size is: " + messages.size());
                /*messagesListAdapter.submitList(messages);*/
                messagingInterface.refreshMessages();
                if (!newMessage.getIsRead() && !newMessage.getSenderId().equals(currentUserId))
                    markMessageAsRead(value, newMessage);
            }
        } catch (Exception e) {
            Log.d(TAG, "addNewMessage: exception " + e.getMessage());
        }
    }

    private void prepareToolbarInfo() {

        mFirebasestore.collection("rooms").document(targetUserId)
                .get().addOnSuccessListener(documentSnapshot -> {
            User user = documentSnapshot.toObject(User.class);
            if (user != null) {
                isActive = user.getIsActive();
                lastTimeSeen = user.getLastTimeSeen();
                populateTargetUserInfo(user);
                listenToChange(targetUserId);
            }
        });


    }

    public boolean isActive() {
        return isActive;
    }

    public long getLastTimeSeen() {
        return lastTimeSeen;
    }

    private void populateTargetUserInfo(User user) {
        Log.d(TAG, "populateTargetUserInfo: populate successfully");
        Log.d(TAG, "from populate: the targer user photo url : " + user.getPhotoUrl());
        targetUserData.putString("target_user_id", user.getUserId());
        Log.d(TAG, "populateTargetUserInfo: target userId: " + targetUserData.getString("target_user_id"));
        targetUserData.putString("target_user_email", user.getEmail());
        targetUserData.putString("target_user_photo_url", user.getPhotoUrl());
        targetUserData.putString("target_user_status", user.getStatus());
        targetUserData.putString("target_user_name", user.getUserName());
        targetUserData.putBoolean("isActive", user.getIsActive());
        targetUserData.putLong("target_user_last_time_seen", user.getLastTimeSeen());
        messagingInterface.populateToolbar();
    }

    public Bundle getTargetUserData() {
        return targetUserData;
    }

    private void listenToChange(String targetUserId) {
        mFirebasestore.collection("rooms").document(targetUserId)
                .addSnapshotListener(((value, error) -> {
                    assert value != null;
                    User user = value.toObject(User.class);
                    String source =
                            value.getMetadata().isFromCache() ?
                                    "local cache" : "server";
                    Log.d(TAG, "Data fetched from " + source);
                    assert user != null;
                    isActive = user.getIsActive();
                    populateTargetUserInfo(user);
                    lastTimeSeen = user.getLastTimeSeen();
                    if (!Connection.isUserConnected(mContext))
                        isActive = false;
                    messagingInterface.refreshChatInfo();
                }));

    }

    public void removeListeners() {
        mCurrentUserRoomReference.removeEventListener(mCurrentRoomListener);
        mTargetUserRoomReference.removeEventListener(mTargetRoomListener);
    }

    public boolean isWriting() {
        return isWriting;
    }

    private void markMessageAsRead(DataSnapshot snapshotMessageTobeUpdated, Message messageToUpdate) {


        Log.d(TAG, "markMessageAsRead: ");
        String key = snapshotMessageTobeUpdated.getKey();
        Log.d(TAG, "markMessageAsRead: the key of the message to be updated is: " + key);
        Map<String, Object> originalMessage = messageToUpdate.toMap();
        originalMessage.put("isRead", true);
        snapshotMessageTobeUpdated.getRef().updateChildren(originalMessage).addOnSuccessListener(
                successListener -> {
                    Log.d(TAG, "update message successfully to be read");
                    //  change the message from target message to local message
                    originalMessage.put("targetId", currentUserId);
                    originalMessage.put("targetName", currentUserName);
                    originalMessage.put("targetPhotoUrl", currentPhotoUrl);
                    assert key != null;
                    mTargetUserRoomReference.child(key).updateChildren(originalMessage).addOnFailureListener(fle ->
                            Log.d(TAG, "markMessageAsRead: " + fle.getMessage()));

                }

        ).addOnFailureListener(
                exception -> Log.d(TAG, "markMessageAsRead: " + exception.getMessage()
                ));
    }

    private void refreshData() {
        Log.d(TAG, "refreshData: ");
        messages.clear();
        try {
            mCurrentUserRoomReference.get().addOnSuccessListener(sListener -> {
                Iterable<DataSnapshot> meessagesIterable = sListener.getChildren();
                for (DataSnapshot messageSnapShot : meessagesIterable) {
                    if (!messageSnapShot.getKey().equals("isWriting")) {
                        Message msg = messageSnapShot.getValue(Message.class);
                        assert msg != null;
                        Log.d(TAG, "refreshData: isRead: " + msg.getIsRead());
                        messages.add(msg);
                    }
                }
                messagingInterface.refreshMessages();
               /* messagesListAdapter.submitList(messages);
                messagesListAdapter.notifyDataSetChanged();*/
            });
        } catch (Exception e) {
            Log.d(TAG, "refreshData: " + e.getMessage());
        }
    }

    public Message getMessageInPosition(int position) {
        return messages.get(position);
    }

    private void sendMessage(Message currentUserMsg, Message targetUserMsg) {

        String key = mCurrentUserRoomReference.push().getKey();
        Log.d(TAG, "sendMessage: the key of the new two messages is: " + key);
        if (key == null)
            throw new NullPointerException("the key of the new messages should not be null");
        mCurrentUserRoomReference.child(key).setValue(targetUserMsg).addOnSuccessListener(success -> {
                    mTargetUserRoomReference.child(key).setValue(currentUserMsg);
                }
        ).addOnFailureListener(exception ->
                Log.d(TAG, "sendMessage: exception msg: " + exception.getMessage()));
    }

    public void setMessagingInterface(MessagingInterface messagingInterface) {
        this.messagingInterface = messagingInterface;
    }
    public void setUserIsNotWriting() {
        mCurrentUserRoomReference.child("isWriting").setValue(false);
        // when the user has no internet connection we set the value of the isWriting to be false
        mCurrentUserRoomReference.child("isWriting").onDisconnect().setValue(false);
        Log.d(TAG, "set user is not writing");
    }

    public void setUserIsWriting() {
        Log.d(TAG, "set user is writing");
            mCurrentUserRoomReference.child("isWriting")
                    .setValue(true);
            /*messageSingleRef.document("isWriting")
                    .update("isWriting", true);*/
    }
    public interface MessagingInterface {
        void refreshChatInfo();

        void refreshMessages();

        void populateToolbar();
    }

    class CurrentRoomListener implements ChildEventListener {

        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            addNewMessage(snapshot);
            Log.d(TAG, "onChildAdded: ");
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            Log.d(TAG, "onChildChanged: ");
            if (!snapshot.getKey().equals("isWriting"))
                refreshData();
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot snapshot) {

        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    }

    class TargetRoomListener implements ChildEventListener {

        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            Log.d(TAG, "onChildChanged: the key of the changed child is: " + previousChildName);
            if (snapshot.getKey().equals("isWriting")) {
                isWriting = (boolean) snapshot.getValue();
                Log.d(TAG, "onChildChanged: isWriting " + isWriting);
                messagingInterface.refreshChatInfo();
            }

        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot snapshot) {

        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    }


}
