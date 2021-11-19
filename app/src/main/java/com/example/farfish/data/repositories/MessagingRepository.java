package com.example.farfish.data.repositories;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.farfish.Module.util.Connection;
import com.example.farfish.Module.util.FileUtil;
import com.example.farfish.Module.dataclasses.Message;
import com.example.farfish.Module.preferences.MessagesPreference;
import com.example.farfish.Module.dataclasses.User;
import com.example.farfish.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;
import id.zelory.compressor.Compressor;

public class MessagingRepository {
    /*TAG for logging*/
    private static final String TAG = MessagingRepository.class.getSimpleName();
    private MessagingInterface messagingInterface;
    // functionality
    private List<Message> messages;
    private StorageReference mRootRef;
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
    private DatabaseReference mTargetUserRoomReference;
    // current user info
    private String currentUserId;
    private String currentUserName;
    private String currentPhotoUrl;
    private Context mContext;
    // rooms listeners
    private CurrentRoomListener mCurrentRoomListener;
    private TargetRoomListener mTargetRoomListener;

    private PostMessagesInterface postMessagesInterface;

    @Inject
    public MessagingRepository(@ApplicationContext Context context) {
        mContext = context;
        targetUserData = new Bundle();
    }

    private void init() {
        mRootRef = FirebaseStorage.getInstance().getReference("images");
        messages = new ArrayList<>();
        currentUserName = MessagesPreference.getUserName(mContext);
        currentPhotoUrl = MessagesPreference.getUsePhoto(mContext);
        mCurrentRoomListener = new CurrentRoomListener();
        mTargetRoomListener = new TargetRoomListener();
    }

    public void setPostMessagesInterface(PostMessagesInterface postMessagesInterface) {
        this.postMessagesInterface = postMessagesInterface;
    }

    public List<Message> getMessages() {
        return this.messages;
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
        init();
        messages.clear();
        prepareToolbarInfo();
        mCurrentUserRoomReference.addChildEventListener(mCurrentRoomListener);
        mTargetUserRoomReference.addChildEventListener(mTargetRoomListener);
    }

    private void prepareToolbarInfo() {

        FirebaseFirestore.getInstance().collection("rooms").document(targetUserId)
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
       /* Log.d(TAG, "populateTargetUserInfo: populate successfully");
        Log.d(TAG, "from populate: the target user photo url : " + user.getPhotoUrl());
        targetUserData.putString("target_user_id", user.getUserId());
        Log.d(TAG, "populateTargetUserInfo: target userId: " + targetUserData.getString("target_user_id"));
        targetUserData.putString("target_user_email", user.getEmail());
        targetUserData.putString("target_user_photo_url", user.getPhotoUrl());
        targetUserData.putString("target_user_status", user.getStatus());
        targetUserData.putString("target_user_name", user.getUserName());
        targetUserData.putBoolean("isActive", user.getIsActive());
        targetUserData.putLong("target_user_last_time_seen", user.getLastTimeSeen());*/
        targetUserData.putParcelable("user", user);
        messagingInterface.populateToolbar();
    }

    public Bundle getTargetUserData() {
        return targetUserData;
    }

    private void listenToChange(String targetUserId) {
        FirebaseFirestore.getInstance().collection("rooms").document(targetUserId)
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
        Log.d(TAG, "removeListeners: ");
        mCurrentUserRoomReference.removeEventListener(mCurrentRoomListener);
        mTargetUserRoomReference.removeEventListener(mTargetRoomListener);
//        cleanUp();
    }

    private void cleanUp() {
        messagingInterface = null;
        messages.clear();
        messages = null;
        targetUserId = null;
        targetUserData.clear();
        mCurrentUserRoomReference = null;
        mTargetUserRoomReference = null;
        currentUserId = null;
        currentUserName = null;
        currentPhotoUrl = null;
        mCurrentRoomListener = null;
        mTargetRoomListener = null;
        lastTimeSeen = 0;
    }

    public boolean isWriting() {
        return isWriting;
    }

    /* this method is used in two functionality, for getting all the messages from a special room
     * and for adding new messages as the user sends. */
    private void addNewMessage(DataSnapshot value) {
        Log.d(TAG, "addNewMessage: ");
        String key = value.getKey();
        if (key == null) return;
        if (key.equals("isWriting")) return;

        Message newMessage = value.getValue(Message.class);
        assert newMessage != null;
        messages.add(newMessage);
        postMessagesInterface.postMessages(messages);
        if (!newMessage.getIsRead() && !newMessage.getSenderId().equals(currentUserId))
            markMessageAsRead(value, newMessage);
    }

    private void refreshData() {
        Log.d(TAG, "refreshData: ");
        messages.clear();
        try {
            mCurrentUserRoomReference.get().addOnSuccessListener(sListener -> {
                Iterable<DataSnapshot> meessagesIterable = sListener.getChildren();
                for (DataSnapshot messageSnapShot : meessagesIterable) {
                    String key = messageSnapShot.getKey();
                    if (key != null) {
                        if (!key.equals("isWriting")) {
                            Message msg = messageSnapShot.getValue(Message.class);
                            assert msg != null;
                            Log.d(TAG, "refreshData: isRead: " + msg.getIsRead());
                            messages.add(msg);
                        }
                    }
                }
                checkForDuplication();
                messagingInterface.refreshMessages();
            });
        } catch (Exception e) {
            Log.d(TAG, "refreshData: " + e.getMessage());
        }
    }

    private void checkForDuplication() {
        Log.d(TAG, "checkForDuplication: size before solution: " + messages.size());
        int listSize = messages.size();
        if (listSize == 0 || listSize == 1)
            return;
        if (listSize % 2 != 0) {
            listSize++;
        }
        int indexToCheck = listSize / 2;
        Message firstMessage = messages.get(0);
        Message messageMightBeDuplicated = messages.get(indexToCheck);
        if (firstMessage.getTimestamp() == messageMightBeDuplicated.getTimestamp()) {
            Log.d(TAG, "checkForDublication: there is duplication!");
            messages = messages.subList(0, indexToCheck);
            Log.d(TAG, "checkForDuplication: size after solution: " + messages.size());
        }
    }

    public Message getMessageInPosition(int position) {
        return messages.get(position);
        /* return testMessages.get(position);*/
    }

    public void sendMessage(Message currentUserMsg, Message targetUserMsg) {

        String key = mCurrentUserRoomReference.push().getKey();
        Log.d(TAG, "sendMessage: the key of the new two messages is: " + key);
        if (key == null)
            throw new NullPointerException("the key of the new messages should not be null");
        mCurrentUserRoomReference.child(key).setValue(targetUserMsg).addOnSuccessListener(success -> mTargetUserRoomReference.child(key).setValue(currentUserMsg)
        ).addOnFailureListener(exception ->
                Log.d(TAG, "sendMessage: exception msg: " + exception.getMessage()));
    }

    public void setMessagingInterface(MessagingInterface messagingInterface) {
        this.messagingInterface = messagingInterface;
    }

    public void setUserIsNotWriting() {
        isWriting = false;
        mCurrentUserRoomReference.child("isWriting").setValue(false);
        // when the user has no internet connection we set the value of the isWriting to be false
        mCurrentUserRoomReference.child("isWriting").onDisconnect().setValue(false);
        Log.d(TAG, "set user is not writing");
    }

    public void setUserIsWriting() {
        Log.d(TAG, "set user is writing");
        isWriting = true;
        mCurrentUserRoomReference.child("isWriting")
                .setValue(true);
    }

    private void markMessageAsRead(DataSnapshot snapshotMessageTobeUpdated, Message messageToUpdate) {


        Log.d(TAG, "markMessageAsRead: ");
        String key = snapshotMessageTobeUpdated.getKey();
        Log.d(TAG, "markMessageAsRead: the key of the message to be updated is: " + key);
        Map<String, Object> originalMessage = messageToUpdate.toMap();
        originalMessage.put("isRead", true);
        mTargetUserRoomReference.child(key).updateChildren(originalMessage);
        /*
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

         */
    }

    public void compressAndSendImage(Uri uri) {
        try {
            File galleryFile = FileUtil.from(mContext, uri);
            /*compress the file using a special library*/
            File compressedImageFile = new Compressor(mContext).compressToFile(galleryFile);
            /*take the file name as a unique identifier*/
            StorageReference imageRef = mRootRef.child(compressedImageFile.getName());
            // finally uploading the file to firebase storage.
            UploadTask uploadTask = imageRef.putFile(Uri.fromFile(compressedImageFile));

            // some logging to track the file size after compressing it with the library.
            Log.d(TAG, "Original file size is: " + galleryFile.length() / 1024 + "KB");
            Log.d(TAG, "Compressed file size is: " + compressedImageFile.length() / 1024 + "KB"); // after compressing the file

            // Register observers to listen for when the download is done or if it fails
            uploadTask.addOnFailureListener(exception -> {
                // Handle unsuccessful uploads
            }).addOnSuccessListener(taskSnapshot -> {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
                Toast.makeText(mContext, mContext.getString(R.string.sending_img_msg), Toast.LENGTH_SHORT).show();
                imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    Log.d(TAG, downloadUri.toString());
                    Log.d(TAG, String.valueOf(downloadUri));
                    String downloadUrl = downloadUri.toString();
                    long dateFromDateClass = new Date().getTime();
                     /* if the image sent successfully to the firebase storage send its metadata as a message
                     to the firebase firestore */
                    Message currentUserMsg = new Message("", downloadUrl, dateFromDateClass, currentUserId, currentUserId,
                            currentUserName, currentUserName, currentPhotoUrl, false);
                    Message targetUserMsg = new Message("", downloadUrl, dateFromDateClass, currentUserId, targetUserId,
                            currentUserName, getTargetUserData().getString("target_user_name"),
                            getTargetUserData().getString("target_user_photo_url"),
                            false);
                    sendMessage(currentUserMsg, targetUserMsg); //hey mister ViewModel send this new message please *_*
                });

            });

        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "Error compressing the file");
            Toast.makeText(mContext, "Error occurs", Toast.LENGTH_SHORT).show();
        }
    }

    public interface MessagingInterface {
        void refreshChatInfo();

        void refreshMessages();

        void populateToolbar();
    }

    public interface PostMessagesInterface {
        void postMessages(List<Message> messages);
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
            String key = snapshot.getKey();
            if (key != null) {
                if (!key.equals("isWriting")) {
                    messages.clear();
                    Log.d(TAG, "onChildChanged: size after clear: " + messages.size());
                    refreshData();
                }
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

    class TargetRoomListener implements ChildEventListener {

        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            Log.d(TAG, "onChildChanged: the key of the changed child is: " + previousChildName);
            String key = snapshot.getKey();
            if (key != null) {
                if (key.equals("isWriting")) {
                    isWriting = (boolean) snapshot.getValue();
                    Log.d(TAG, "onChildChanged: isWriting " + isWriting);
                    messagingInterface.refreshChatInfo();
                }
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
