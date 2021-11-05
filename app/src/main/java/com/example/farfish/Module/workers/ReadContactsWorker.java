package com.example.farfish.Module.workers;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.farfish.Module.preferences.MessagesPreference;

import java.util.HashSet;
import java.util.Set;

public class ReadContactsWorker extends Worker {

    public static Set<String> contactsSet = new HashSet<>();

    public ReadContactsWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public static void freeUpMemory() {
        contactsSet.clear();
        contactsSet = null;
    }

    @NonNull
    @Override
    public Result doWork() {
        // background thread
        Cursor contactsCursor = getApplicationContext().getContentResolver()
                .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                        ContactsContract.CommonDataKinds.Phone.NUMBER + " != ?",
                        new String[]{" "}, null);
        if (contactsCursor != null) {
            while (contactsCursor.moveToNext()) {
                String phoneNumber = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                /*contactsList.add(phoneNumber);*/
                contactsSet.add(phoneNumber);
            }
            contactsCursor.close();
        }
        MessagesPreference.saveDeviceContacts(getApplicationContext(), contactsSet);
        freeUpMemory();
        return Result.success();
    }

}
