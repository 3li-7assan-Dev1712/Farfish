package com.example.farfish.Module.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.farfish.Module.CustomPhoneNumberUtils;

import java.util.Arrays;
import java.util.Set;

public class ReadDataFromServerWorker extends Worker {
    private static final String TAG = ReadDataFromServerWorker.class.getSimpleName();

    public ReadDataFromServerWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String [] serverData = getInputData().getStringArray("server_contacts");
        String [] deviceData = getInputData().getStringArray("device_contacts");

        Set<CustomPhoneNumberUtils> data =
                CustomPhoneNumberUtils.getCommonPhoneNumbers(Arrays.asList(serverData), Arrays.asList(deviceData), getApplicationContext());
        String [] commonPhoneNumbers = new String[data.size()];
        int index = 0;
        for (CustomPhoneNumberUtils datum : data) {
            String commonPhoneNumber = datum.getVal();
            commonPhoneNumbers[index] = commonPhoneNumber;
            index++;
        }
        Data output = new Data.Builder()
                .putStringArray("common_phone_numbers", commonPhoneNumbers)
                .build();
        return Result.success(output);
    }
}
