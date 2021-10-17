package com.example.farfish.data;

import android.app.Application;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

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

import com.example.farfish.Module.User;
import com.example.farfish.Module.workers.ReadContactsWorker;
import com.example.farfish.Module.workers.ReadDataFromServerWorker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainViewModel extends AndroidViewModel {

    private static final String TAG = MainViewModel.class.getSimpleName();
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
            allUsers = new MutableLiveData<>(contactUsers);
            loadUsers();
        }
        Log.d(TAG, "getAllUsers: allUsers: " + Objects.requireNonNull(allUsers.getValue()).size());
        return allUsers;
    }

    public User getUserInPosition(int position, boolean fromContacts) {
        if (fromContacts)
            return usersUserKnowList.get(position);
        else
            return allUsersList.get(position);
    }

    private void loadUsers() {
        // Do an asynchronous operation to fetch users.
        fetchDataInUsersUserKnowList();
        FirebaseFirestore.getInstance().collection("rooms").get()
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

    public void updateUsers(boolean fromContacts) {
        Log.d(TAG, "updateUsers: userUserKnowList size: " + usersUserKnowList.size());
        if (fromContacts)
            allUsers.setValue(usersUserKnowList);
        else
            allUsers.setValue(allUsersList);
    }

    public interface InvokeObservers {
        void invokeObservers();

        void observeCommonContacts();

        void prepareDataFinished();
    }
}
