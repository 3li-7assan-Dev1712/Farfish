package com.example.farfish.Module.workers;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReadContactsWorker extends Worker {

    public static List<String> contactsList = new ArrayList<>();
    public static Set<String> contactsSet = new HashSet<>();
    public ReadContactsWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public static void freeUpMemory() {
        contactsSet = null;
    }

    @NonNull
    @Override
    public Result doWork() {
        // background thread



        Cursor contactsCursor = getApplicationContext().getContentResolver()
                .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        new String[] {ContactsContract.CommonDataKinds.Phone.NUMBER },
                        ContactsContract.CommonDataKinds.Phone.NUMBER + " != ?",
                        new String[] {" "},null);
        if (contactsCursor != null){
            while (contactsCursor.moveToNext()) {
                String phoneNumber = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                /*contactsList.add(phoneNumber);*/
                contactsSet.add(phoneNumber);
            }
            contactsCursor.close();
        }
        String [] numbers = new String[contactsList.size()];
        for (int i = 0; i < contactsList.size(); i++ ) {
            numbers[i] = contactsList.get(i);

        }

        Data output = new Data.Builder()
                .putStringArray("contacts", numbers)
                .build();
        return Result.success(output);

        /*
        *
WorkRequest uploadWorkRequest =
   new OneTimeWorkRequest.Builder(UploadWorker.class)
       .build();


*
WorkManager
    .getInstance(myContext)
    .enqueue(uploadWorkRequest);


        * */
    }

}
