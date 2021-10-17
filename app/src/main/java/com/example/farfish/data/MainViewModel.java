package com.example.farfish.data;

import android.app.Application;
import android.telephony.PhoneNumberUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.example.farfish.Module.FilterPreferenceUtils;
import com.example.farfish.Module.User;
import com.example.farfish.Module.workers.ReadContactsWorker;
import com.example.farfish.Module.workers.ReadDataFromServerWorker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private WorkManager workManager;
    private MutableLiveData<List<User>> allUsers;
    private List<User> allUsersList = new ArrayList<>();
    private List<User> usersUserKnowList = new ArrayList<>();
    private List<User> contactUsers = new ArrayList<>();
    // phone numbers
    private List<String> listServerPhoneNumber = new ArrayList<>();
    private String[] arrayServerPhoneNumbers;

    // observers
    public LiveData<WorkInfo> deviceContactsObserver;
    public LiveData<WorkInfo> commonContactsObserver;

    private InvokeObservers invokeObservers;

    public void setObservers(InvokeObservers observers) {
        this.invokeObservers = observers;
    }
    public MainViewModel(@NonNull Application application) {
        super(application);
        workManager = WorkManager.getInstance(getApplication().getApplicationContext());
    }

    public LiveData<List<User>> getAllUsers() {
        if (allUsers == null) {
            allUsers = new MutableLiveData<>(usersUserKnowList);
            loadUsers();
        }
        return allUsers;
    }

    private void loadUsers() {
        // Do an asynchronous operation to fetch users.
        FirebaseFirestore.getInstance().collection("room").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    fetchPrimaryData(queryDocumentSnapshots);
                    fetchDataInUsersUserKnowList();
                }).addOnCompleteListener(listener -> {
                    invokeObservers.invokeObservers();
        });
    }

    public static interface InvokeObservers{
        void invokeObservers();
    }


    private void fetchPrimaryData(QuerySnapshot queryDocumentSnapshots) {
        if (allUsersList.size() == 0) {
            for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                User user = dc.getDocument().toObject(User.class);
                String currentUserId = FirebaseAuth.getInstance().getUid();
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
            arrayServerPhoneNumbers = new String[listServerPhoneNumber.size()];
            for (int i = 0; i < listServerPhoneNumber.size(); i++) {
                arrayServerPhoneNumbers[i] = listServerPhoneNumber.get(i);
            }
        }
    }

    public void prepareUserUserKnowList(String [] userContacts) {
        assert userContacts != null;
        for (String commonPhoneNumber : userContacts) {
            for (User userUserKnow : contactUsers) {
                String localUserPhoneNumber = userUserKnow.getPhoneNumber();
                if (PhoneNumberUtils.compare(commonPhoneNumber, localUserPhoneNumber)) {
                    usersUserKnowList.add(userUserKnow);
                }
            }
        }
        allUsers.setValue(usersUserKnowList);
    }

    private void fetchDataInUsersUserKnowList() {

        if (usersUserKnowList.size() == 0) {
            // for WorkManager functionality
            OneTimeWorkRequest contactsWork = new OneTimeWorkRequest.Builder(ReadContactsWorker.class)
                    .build();
            workManager.enqueueUniqueWork("read_contacts_work", ExistingWorkPolicy.KEEP, contactsWork);
            deviceContactsObserver = workManager.getWorkInfoByIdLiveData(contactsWork.getId());
        }
    }

    public void readContactsWorkerEnd(String[] deviceContacts) {
        assert deviceContacts != null;
        Data input = new Data.Builder()
                .putStringArray("device_contacts", deviceContacts)
                .putStringArray("server_contacts", arrayServerPhoneNumbers)
                .build();
        WorkRequest commonContactsWorker = new OneTimeWorkRequest.Builder(ReadDataFromServerWorker.class)
                .setInputData(input)
                .build();
        workManager.enqueue(commonContactsWorker);
        commonContactsObserver = workManager.getWorkInfoByIdLiveData(commonContactsWorker.getId());
    }

    public void updateUsers (boolean fromContacts) {
        if (fromContacts)
            allUsers.setValue(usersUserKnowList);
        else
            allUsers.setValue(allUsersList);
    }
}
