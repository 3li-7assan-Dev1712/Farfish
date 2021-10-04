package com.example.farfish.Module.workers;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.ArrayList;
import java.util.List;

public class ReadContactsWorker extends Worker {

    public ReadContactsWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // background thread

        List<String> contactsList = new ArrayList<>();

        Cursor contactsCursor = getApplicationContext().getContentResolver()
                .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        new String[] {ContactsContract.CommonDataKinds.Phone.NUMBER },
                        ContactsContract.CommonDataKinds.Phone.NUMBER + " != ?",
                        new String[] {" "},null);
        if (contactsCursor != null){
            while (contactsCursor.moveToNext()) {
                String phoneNumber = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                contactsList.add(phoneNumber);
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
