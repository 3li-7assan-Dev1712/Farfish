package com.example.farfish.Module.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.farfish.Module.CustomPhoneNumberUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class ReadDataFromServerWorker extends Worker {
    private static final String TAG = ReadDataFromServerWorker.class.getSimpleName();
    private static List<String> serverContacts, commonPhoneNumbers;
    private static Set<String> deviceContacts;
    public ReadDataFromServerWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public static void setLists(Set<String> _deviceContacts, List<String> _serverContacts){
         deviceContacts= _deviceContacts;
         serverContacts = _serverContacts;
    }

    public static List<String> getCommonPhoneNumbers() {
        return commonPhoneNumbers;
    }

    @NonNull
    @Override
    public Result doWork() {

        assert deviceContacts != null;
        assert serverContacts != null;
        Set<CustomPhoneNumberUtils> data =
                CustomPhoneNumberUtils.getCommonPhoneNumbers(deviceContacts, serverContacts, getApplicationContext());
        commonPhoneNumbers = new ArrayList<>(data.size());
        for (CustomPhoneNumberUtils datum : data) {
            String commonPhoneNumber = datum.getVal();
            commonPhoneNumbers.add(commonPhoneNumber);
        }
        return Result.success();
    }
}
