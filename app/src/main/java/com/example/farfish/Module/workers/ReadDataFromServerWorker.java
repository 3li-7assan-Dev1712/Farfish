package com.example.farfish.Module.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.farfish.Module.CustomPhoneNumberUtils;

import java.util.Set;

public class ReadDataFromServerWorker extends Worker {
    /*    private static List<String>  commonPhoneNumbers;
        private static Set<String>  setCommonPhoneNumbers;*/
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

    /*public static List<String> getCommonPhoneNumbers() {
        return commonPhoneNumbers;
    }*/

    @NonNull
    @Override
    public Result doWork() {

        assert deviceContacts != null;
        assert serverContacts != null;
        CustomPhoneNumberUtils.storeCommonPhoneNumber(deviceContacts, serverContacts, getApplicationContext());
        // free up some space in the memory
        deviceContacts.clear();
        serverContacts.clear();
        /*
        commonPhoneNumbers = new ArrayList<>(data.size());
        for (CustomPhoneNumberUtils datum : data) {
            String commonPhoneNumber = datum.getVal();
            commonPhoneNumbers.add(commonPhoneNumber);
        }*/
        return Result.success();
    }
}
