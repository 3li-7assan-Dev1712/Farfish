package com.example.farfish.data.repositories;

import android.content.Context;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.example.farfish.Module.MessagesPreference;
import com.example.farfish.Module.User;
import com.example.farfish.Module.workers.ReadContactsWorker;
import com.example.farfish.Module.workers.ReadDataFromServerWorker;
import com.example.farfish.data.AppDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class UsersRepository {
    private static final String TAG = UsersRepository.class.getSimpleName();
    // observers
    public LiveData<WorkInfo> deviceContactsObserver;
    public LiveData<WorkInfo> commonContactsObserver;
    private InvokeObservers invokeObservers;
    private WorkManager workManager;
    private List<User> allUsersList = new ArrayList<>();
    private List<User> usersUserKnowList = new ArrayList<>();
    private List<User> contactUsers = new ArrayList<>();
    // phone numbers
    private List<String> listServerPhoneNumber = new ArrayList<>();
    private String[] arrayServerPhoneNumbers;
    private Context context;
    public UsersRepository(Context context) {
        workManager = WorkManager.getInstance(context);
        this.context = context;
    }

    public void setObservers(UsersRepository.InvokeObservers observers) {
        this.invokeObservers = observers;
    }

    public void loadUsers() {
        // Do an asynchronous operation to fetch users.
       /* usersUserKnowList = AppDatabase.getInstance(context).getUserDao().getAllUsersUserMayKnow(MessagesPreference.getUserContacts(context)).getValue();
        allUsersList = AppDatabase.getInstance(context).getUserDao().getAllPublicUser(true).getValue();
        invokeObservers.prepareDataFinished();*/
        refreshData();
    }

    private void refreshData() {
        fetchDataInUsersUserKnowList();
        FirebaseFirestore.getInstance().collection("rooms").get(Source.SERVER)
                .addOnSuccessListener(this::fetchPrimaryData).addOnCompleteListener(listener -> invokeObservers.invokeObservers());
    }

    public void readContactsWorkerEnd(String[] deviceContacts) {
        Log.d(TAG, "readContactsWorkerEnd: ");
        if (deviceContacts != null) {
            Data input = new Data.Builder()
                    .putStringArray("device_contacts", deviceContacts)
                    .putStringArray("server_contacts", arrayServerPhoneNumbers)
                    .build();
            WorkRequest commonContactsWorker = new OneTimeWorkRequest.Builder(ReadDataFromServerWorker.class)
                    .setInputData(input)
                    .build();
            workManager.enqueue(commonContactsWorker);
            commonContactsObserver = workManager.getWorkInfoByIdLiveData(commonContactsWorker.getId());
            Log.d(TAG, "readContactsWorkerEnd: enqueue the work successfully");
            invokeObservers.observeCommonContacts();
        } else Log.d(TAG, "readContactsWorkerEnd: deviceContacts is null");

    }

    private void fetchPrimaryData(QuerySnapshot queryDocumentSnapshots) {
        Log.d(TAG, "fetchPrimaryData: ");
        for (DocumentSnapshot ds : queryDocumentSnapshots.getDocuments()) {
            User user = ds.toObject(User.class);
            String currentUserId = FirebaseAuth.getInstance().getUid();
            assert user != null;
            String phoneNumber = user.getPhoneNumber();
            if (phoneNumber != null) {
                if (!phoneNumber.equals("")) {
                    listServerPhoneNumber.add(phoneNumber);
                }
            }
            assert currentUserId != null;
            if (!currentUserId.equals(user.getUserId()) && user.getIsPublic())
                allUsersList.add(user);
            if (!currentUserId.equals(user.getUserId()))
                contactUsers.add(user);
        }
        // converting from list to array
        AppDatabase.getInstance(context).getUserDao().saveAllUsers(allUsersList);
        arrayServerPhoneNumbers = new String[listServerPhoneNumber.size()];
        for (int i = 0; i < listServerPhoneNumber.size(); i++) {
            arrayServerPhoneNumbers[i] = listServerPhoneNumber.get(i);
        }

    }

    public void prepareUserUserKnowList(String[] userContacts) {
        assert userContacts != null;
        for (String commonPhoneNumber : userContacts) {
            for (User userUserKnow : contactUsers) {
                String localUserPhoneNumber = userUserKnow.getPhoneNumber();
                if (PhoneNumberUtils.compare(commonPhoneNumber, localUserPhoneNumber)) {
                    Log.d(TAG, "prepareUserUserKnowList: common number: " + commonPhoneNumber);
                    usersUserKnowList.add(userUserKnow);
                }
            }
        }
        Log.d(TAG, "prepareUserUserKnowList: userUserKnowList size is: " + usersUserKnowList.size());

        invokeObservers.prepareDataFinished();
    }

    private void fetchDataInUsersUserKnowList() {

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
        Log.d(TAG, "updateUsers: userUserKnowList size: " + usersUserKnowList.size());
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
