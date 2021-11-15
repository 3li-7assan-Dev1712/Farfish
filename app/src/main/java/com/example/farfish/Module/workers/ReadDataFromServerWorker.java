package com.example.farfish.Module.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.farfish.Module.util.CustomPhoneNumberUtils;

import java.util.Set;

public class ReadDataFromServerWorker extends Worker {

    private static Set<String> deviceContacts;
    private static Set<CustomPhoneNumberUtils> serverContacts;
    private final String TAG = ReadDataFromServerWorker.class.getSimpleName();

    public ReadDataFromServerWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public static void setLists(Set<String> _deviceContacts, Set<CustomPhoneNumberUtils> _serverContacts) {
        deviceContacts = _deviceContacts;
        serverContacts = _serverContacts;
    }


    @NonNull
    @Override
    public Result doWork() {

        assert deviceContacts != null;
        assert serverContacts != null;
        Log.d(TAG, "doWork: " + deviceContacts.size());
        Log.d(TAG, "doWork: server: " + serverContacts.size());
        CustomPhoneNumberUtils.storeCommonPhoneNumber(deviceContacts, serverContacts, getApplicationContext());
        // free up some space in the memory
        deviceContacts.clear();
        serverContacts.clear();
        return Result.success();
    }
}
