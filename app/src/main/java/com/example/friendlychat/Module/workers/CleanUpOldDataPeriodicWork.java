package com.example.friendlychat.Module.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.friendlychat.Module.MessagesPreference;
import com.example.friendlychat.Module.Status;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.concurrent.TimeUnit;

public class CleanUpOldDataPeriodicWork extends Worker {

    // tag for logging and debugging
    private static final String TAG = CleanUpOldDataPeriodicWork.class.getSimpleName();
    // status reference where in the clean up happens
    private DatabaseReference mDatabaseStatusReference = FirebaseDatabase.getInstance().getReference("status");
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

        String currentUserId = MessagesPreference.getUserId(getApplicationContext());

        // clean up old statuses
        DatabaseReference userStatusReference = mDatabaseStatusReference.child(currentUserId);
        userStatusReference.get().addOnSuccessListener(snapshot -> {

            Iterable<DataSnapshot> statusesIterable = snapshot.getChildren();
            for (DataSnapshot status : statusesIterable) {
                Status statusToCheck = status.getValue(Status.class);
                long currentTimeInDays = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis());
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

        // clean up old messages will be in the next commit *_*

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
