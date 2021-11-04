package com.example.farfish.data.repositories;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.example.farfish.Module.util.CustomPhoneNumberUtils;
import com.example.farfish.Module.preferences.MessagesPreference;
import com.example.farfish.Module.dataclasses.User;
import com.example.farfish.Module.workers.ReadContactsWorker;
import com.example.farfish.Module.workers.ReadDataFromServerWorker;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;

public class UsersRepository {
    private static final String TAG = UsersRepository.class.getSimpleName();
    // observers
    public LiveData<WorkInfo> deviceContactsObserver;
    public LiveData<WorkInfo> commonContactsObserver;
    private InvokeObservers invokeObservers;
    private WorkManager workManager;
    private List<User> allUsersList = new ArrayList<>();
    private List<User> usersUserKnowList = new ArrayList<>();
    /*private List<User> contactUsers = new ArrayList<>();*/
    // phone numbers
    private List<String> listServerPhoneNumber = new ArrayList<>();
    private Set<CustomPhoneNumberUtils> setServerPhoneNumber = new HashSet<>();
    private Context mContext;
    private String currentUserId;

    @Inject
    public FirebaseFirestore mFirestore;
    @Inject
    public UsersRepository(@ApplicationContext Context context) {
        workManager = WorkManager.getInstance(context);
        mContext = context;
        currentUserId = MessagesPreference.getUserId(context);
    }

    public void setObservers(UsersRepository.InvokeObservers observers) {
        this.invokeObservers = observers;
    }

    public void loadUsers() {
        // Do an asynchronous operation to fetch users.
        refreshData();
    }


    private void refreshData() {
        boolean contactsIsCached = MessagesPreference.getDeviceContacts(mContext) != null;
        if (!contactsIsCached)
            readTheContactsInTheDevice();

        mFirestore.collection("rooms").get()
                .addOnSuccessListener(this::fetchPrimaryData).addOnCompleteListener(listener -> {
            Log.d(TAG, "refreshData: onCompletion called");
                    invokeObservers.invokeObservers();
                    if (contactsIsCached)
                        readContactsWorkerEnd();
                }
        );

    }

    public void readContactsWorkerEnd() {
        Log.d(TAG, "readContactsWorkerEnd: ");

            /*Data input = new Data.Builder()
                    .putStringArray("device_contacts", deviceContacts)
                    .putStringArray("server_contacts", arrayServerPhoneNumbers)
                    .build();*/
        ReadDataFromServerWorker.setLists(MessagesPreference.getDeviceContacts(mContext), setServerPhoneNumber);
        WorkRequest commonContactsWorker = new OneTimeWorkRequest.Builder(ReadDataFromServerWorker.class)
                .build();
        workManager.enqueue(commonContactsWorker);
        commonContactsObserver = workManager.getWorkInfoByIdLiveData(commonContactsWorker.getId());
        Log.d(TAG, "readContactsWorkerEnd: enqueue the work successfully");
        invokeObservers.observeCommonContacts();

    }

    private void fetchPrimaryData(QuerySnapshot queryDocumentSnapshots) {
        Log.d(TAG, "fetchPrimaryData: ");
       /* contactUsers.clear();*/
        allUsersList.clear();
        for (DocumentSnapshot ds : queryDocumentSnapshots.getDocuments()) {
            User user = ds.toObject(User.class);
            assert user != null;
            String phoneNumber = user.getPhoneNumber();
            setServerPhoneNumber.add(new CustomPhoneNumberUtils(phoneNumber));
            /*if (phoneNumber != null) {
                if (!phoneNumber.equals("")) {
                   *//* listServerPhoneNumber.add(phoneNumber);*//*

                }
            }*/
            assert currentUserId != null;
            if (!currentUserId.equals(user.getUserId()) && user.getIsPublic())
                allUsersList.add(user);
            if (!currentUserId.equals(user.getUserId()))
                CustomPhoneNumberUtils.allUsers.add(user);
        }
        Log.d(TAG, "fetchPrimaryData: done");
    }

    public void prepareUserUserKnowList() {
        /*for (String commonPhoneNumber : MessagesPreference.getUserContacts(mContext)) {
            for (User userUserKnow : contactUsers) {
                String localUserPhoneNumber = userUserKnow.getPhoneNumber();
                if (PhoneNumberUtils.compare(commonPhoneNumber, localUserPhoneNumber)) {
                    Log.d(TAG, "prepareUserUserKnowList: common number: " + commonPhoneNumber);
                    usersUserKnowList.add(userUserKnow);
                }
            }
        }*/
        usersUserKnowList = CustomPhoneNumberUtils.getUsersUserKnow();
        CustomPhoneNumberUtils.clearLists();
        Log.d(TAG, "prepareUserUserKnowList: userUserKnowList size is: " + usersUserKnowList.size());
        invokeObservers.prepareDataFinished();
    }

    private void readTheContactsInTheDevice() {

        // for WorkManager functionality
        OneTimeWorkRequest contactsWork = new OneTimeWorkRequest.Builder(ReadContactsWorker.class)
                .build();
        workManager.enqueueUniqueWork("read_contacts_work", ExistingWorkPolicy.KEEP, contactsWork);
        deviceContactsObserver = workManager.getWorkInfoByIdLiveData(contactsWork.getId());

    }

    public User getUserInPosition(int position, boolean fromContacts) {
        if (fromContacts)
            return usersUserKnowList.get(position);
        else
            return allUsersList.get(position);
    }

    public List<User> getUsers(boolean fromContacts) {
        if (fromContacts)
            return usersUserKnowList;
        else
            return allUsersList;
    }

    public interface InvokeObservers {
        void invokeObservers();

        void observeCommonContacts();

        void prepareDataFinished();
    }
}
