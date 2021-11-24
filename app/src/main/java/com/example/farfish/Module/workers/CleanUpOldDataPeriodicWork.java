package com.example.farfish.Module.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.farfish.Module.dataclasses.Message;
import com.example.farfish.Module.dataclasses.Status;
import com.example.farfish.Module.preferences.MessagesPreference;
import com.example.farfish.Module.util.NotificationUtils;
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

    // three months
    private final short MAX_MESSAGE_AGE = 90;
    // max number of story can live
    private final short MAX_STATUS_DURATION = 2; // two days
    // status reference where in the clean up happens
    private DatabaseReference mDatabaseStatusReference = FirebaseDatabase.getInstance().getReference("status");
    private DatabaseReference mDatabaseMessageReference = FirebaseDatabase.getInstance().getReference("rooms");
    // firestore references from stories and messages with images
    private StorageReference mOutdatedStoryImageReference = FirebaseStorage.getInstance().getReference("stories");
    private StorageReference mOutdatedMessageImageReference = FirebaseStorage.getInstance().getReference("images");

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
                            messageSnapshot.getRef().removeValue();
                        } else {
                            newMessage = outdatedMessage;
                        }
                    }
                }
                if (newMessage != null && !newMessage.getIsRead() && !newMessage.getSenderId().equals(currentUserId)) {
                    newMessages.add(newMessage);
                }
            }
        }).addOnCompleteListener(complete -> {
            // after cleaning up the old messages and check for the new one,
            // we need to notify the user of the new messages if so
            if (newMessages.size() > 0) {
                NotificationUtils.notifyUserOfNewMessages(getApplicationContext(), newMessages);
            }
        });


        return Result.success();
    }

    private void removeImageFromFirestore(String outdatedImageUrl, int locationCode) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        String path = storage.getReferenceFromUrl(outdatedImageUrl).getName();
        switch (locationCode) {
            case 1:
                // delete image from story ref
                mOutdatedStoryImageReference.child(path).delete().addOnSuccessListener(aVoid -> {
                });
                break;
            case 2:
                // delete image from message ref
                mOutdatedMessageImageReference.child(path).delete().addOnSuccessListener(aVoid -> {
                });
                break;
        }
    }


}
