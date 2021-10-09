package com.example.farfish.Module.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.farfish.Module.Message;
import com.example.farfish.Module.MessagesPreference;
import com.example.farfish.Module.NotificationUtils;
import com.example.farfish.Module.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CleanUpOldDataPeriodicWork extends Worker {

    // tag for logging and debugging
    private static final String TAG = CleanUpOldDataPeriodicWork.class.getSimpleName();
    // three months
    private static final long MAX_MESSAGE_AGE = 90;
    // status reference where in the clean up happens
    private DatabaseReference mDatabaseStatusReference = FirebaseDatabase.getInstance().getReference("status");
    private DatabaseReference mDatabaseMessageReference = FirebaseDatabase.getInstance().getReference("rooms");
    // firestore references from stories and messages with images
    private StorageReference mOutdatedStoryImageReference = FirebaseStorage.getInstance().getReference("stories");
    private StorageReference mOutdatedMessageImageReference = FirebaseStorage.getInstance().getReference("images");
    // max number of story can live
    private static final long MAX_STATUS_DURATION = 2; // two days

    // required constructor
    public CleanUpOldDataPeriodicWork(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }


    @NonNull
    @Override
    public Result doWork() {

        if (FirebaseAuth.getInstance().getCurrentUser() == null)
            return Result.failure();
        String currentUserId = MessagesPreference.getUserId(getApplicationContext());
        long currentTimeInMillis = System.currentTimeMillis();

        Log.d(TAG, "doWork: start cleaning up");
        // clean up old statuses
        DatabaseReference userStatusReference = mDatabaseStatusReference.child(currentUserId);
        userStatusReference.get().addOnSuccessListener(snapshot -> {

            Iterable<DataSnapshot> statusesIterable = snapshot.getChildren();
            for (DataSnapshot status : statusesIterable) {
                Status statusToCheck = status.getValue(Status.class);
                long currentTimeInDays = TimeUnit.MILLISECONDS.toDays(currentTimeInMillis);
                assert statusToCheck != null;
                long statusTimeInDays = TimeUnit.MILLISECONDS.toDays(statusToCheck.getTimestamp());
                long statusAge = currentTimeInDays - statusTimeInDays;
                Log.d(TAG, "doWork: status age is: " + statusAge);
                if (statusAge >= MAX_STATUS_DURATION) {
                    String outdatedImageUrl = statusToCheck.getStatusImage();
                    if (!outdatedImageUrl.equals(""))
                        removeImageFromFirestore(outdatedImageUrl, 1);
                    status.getRef().removeValue();
                }
            }
        });

        // clean up old messages
        List<Message> newMessages = new ArrayList<>();
        DatabaseReference userMessageRooms = mDatabaseMessageReference.child(currentUserId);
        userMessageRooms.get().addOnSuccessListener(snapshot -> {

            Iterable<DataSnapshot> userRooms = snapshot.getChildren();
            for (DataSnapshot singleRoom : userRooms) {
                Iterable<DataSnapshot> messagesIterable = singleRoom.getChildren();
                Message newMessage = null;
                for (DataSnapshot messageSnapshot : messagesIterable) {

                    if (!messageSnapshot.getKey().equals("isWriting")) {
                        Message outdatedMessage = messageSnapshot.getValue(Message.class);
                        // after get the message let's see if it's outdated or not
                        assert outdatedMessage != null;
                        long messageTimeInDays = TimeUnit.MILLISECONDS.toDays(outdatedMessage.getTimestamp());
                        long currentTimeInDays = TimeUnit.MILLISECONDS.toDays(currentTimeInMillis);
                        long messageAgeInDays = currentTimeInDays - messageTimeInDays;
                        if (messageAgeInDays >= MAX_MESSAGE_AGE) {
                            // the message is outdated and should be deleted
                            String outdatedMessagePhotoUrl = outdatedMessage.getPhotoUrl();
                            if (!outdatedMessagePhotoUrl.equals(""))
                                removeImageFromFirestore(outdatedMessagePhotoUrl, 2);
                            messageSnapshot.getRef().removeValue().addOnSuccessListener(successfullRemove -> {
                                Log.d(TAG, "doWork: remove outdated message successfully");
                            });
                        } else {
                            newMessage = outdatedMessage;
                        }
                    }
                }
                 if (newMessage != null && !newMessage.getIsRead() && !newMessage.getSenderId().equals(currentUserId)){
                    Log.d(TAG, "doWork: message text : " + newMessage.getText());
                    newMessages.add(newMessage);
                }else{
                    Log.d(TAG, "doWork: message is null");
                }
            }
        }).addOnCompleteListener(complete -> {
            Log.d(TAG, "doWork: newMessages size is: " + newMessages.size());
            // after cleaning up the old messages and check for the new one,
            // we need to notify the user of the new messages if so
            if (newMessages.size() > 0 ) {
                Log.d(TAG, "doWork: notify the user of the " + newMessages.size() + " new messages *_* ");
                NotificationUtils.notifyUserOfNewMessages(getApplicationContext(), newMessages);
            }
        });



        return Result.success();
    }

    private void removeImageFromFirestore(String outdatedImageUrl, int locationCode) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        String path = storage.getReferenceFromUrl(outdatedImageUrl).getName();
        Log.d(TAG, "removeImageFromStorage: out dated status path to delete " + path);
        switch (locationCode) {
            case 1:
                // delete image from story ref
                mOutdatedStoryImageReference.child(path).delete().addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "removeImageFromFirestore: image deleted successfully");
                });
                break;
            case 2:
                // delete image from message ref
                mOutdatedMessageImageReference.child(path).delete().addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "removeImageFromFirestore: image deleted successfully");
                });
                break;
        }
    }


}