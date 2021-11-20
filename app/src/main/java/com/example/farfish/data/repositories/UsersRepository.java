package com.example.farfish.data.repositories;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.example.farfish.Module.dataclasses.User;
import com.example.farfish.Module.preferences.MessagesPreference;
import com.example.farfish.Module.util.CustomPhoneNumberUtils;
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
    /**
     * TAG for debug, and will be removed later
     */
    private final String TAG = UsersRepository.class.getSimpleName();

    public LiveData<WorkInfo> deviceContactsObserver;
    public LiveData<WorkInfo> commonContactsObserver;
    private InvokeObservers invokeObservers;
    private WorkManager workManager;
    private List<User> allUsersList = new ArrayList<>();
    private List<User> usersUserKnowList = new ArrayList<>();
    private Set<CustomPhoneNumberUtils> setServerPhoneNumber = new HashSet<>();
    private Context mContext;
    private String currentUserId;

    /**
     * injected constructor to provide an object of this class.
     * constructs the necessary dependencies.
     *
     * @param context takes a context to be used for creating a WorkManager object
     */
    @Inject
    public UsersRepository(@ApplicationContext Context context) {
        workManager = WorkManager.getInstance(context);
        mContext = context;
        currentUserId = MessagesPreference.getUserId(context);
    }

    /**
     * this method is responsible to attach the callbacks of this class to the UsersFragment
     *
     * @param observers takes an interface that holds a set of callbacks to invoke the fragments when the data is
     *                  ready to display it to the user.
     */
    public void setObservers(UsersRepository.InvokeObservers observers) {
        this.invokeObservers = observers;
    }

    /**
     * This method provides the logic of getting the data asynchronously
     */
    public void loadUsers() {
        // Do an asynchronous operation to fetch users.
        refreshData();
    }

    /**
     * this method fetches the data into some lists to display them to the user.
     */
    private void refreshData() {
        boolean contactsIsCached = false;
        if (MessagesPreference.getDeviceContacts(mContext) != null) {
            if (MessagesPreference.getDeviceContacts(mContext).size() > 0)
                contactsIsCached = true;
        }
        if (!contactsIsCached) {
            readTheContactsInTheDevice();
            Log.d(TAG, "refreshData: data is not cached");
        }
        boolean finalContactsIsCached = contactsIsCached;
        FirebaseFirestore.getInstance().collection("rooms").get()
                .addOnSuccessListener(this::fetchPrimaryData).addOnCompleteListener(listener -> {
                    Log.d(TAG, "refreshData: onCompletion called");
                    invokeObservers.invokeObservers();
                    if (finalContactsIsCached) {
                        readContactsWorkerEnd();
                        Log.d(TAG, "refreshData: data is cached!");
                    }
                }
        );

    }

    /**
     * this methods is called from the UsersFragment, UsersFragment observes
     * the WorkManager after it is finished it call this method to
     * complete the app flow.
     */
    public void readContactsWorkerEnd() {
        Log.d(TAG, "readContactsWorkerEnd: ");
        ReadDataFromServerWorker.setLists(MessagesPreference.getDeviceContacts(mContext), setServerPhoneNumber);
        WorkRequest commonContactsWorker = new OneTimeWorkRequest.Builder(ReadDataFromServerWorker.class)
                .build();
        workManager.enqueue(commonContactsWorker);
        commonContactsObserver = workManager.getWorkInfoByIdLiveData(commonContactsWorker.getId());
        Log.d(TAG, "readContactsWorkerEnd: enqueue the work successfully");
        invokeObservers.observeCommonContacts();

    }

    /**
     * as the name suggests the method fetches the users from the
     * Firestore database into a list as well as their phone numbers.
     *
     * @param queryDocumentSnapshots takes an instance of a QuerySnapshot to fetch it
     *                               to a list of users.
     */
    private void fetchPrimaryData(QuerySnapshot queryDocumentSnapshots) {
        Log.d(TAG, "fetchPrimaryData: ");
        allUsersList.clear();
        for (DocumentSnapshot ds : queryDocumentSnapshots.getDocuments()) {
            User user = ds.toObject(User.class);
            assert user != null;
            String phoneNumber = user.getPhoneNumber();
            setServerPhoneNumber.add(new CustomPhoneNumberUtils(phoneNumber));
            assert currentUserId != null;
            if (!currentUserId.equals(user.getUserId()) && user.getIsPublic())
                allUsersList.add(user);
            if (!currentUserId.equals(user.getUserId()))
                CustomPhoneNumberUtils.allUsers.add(user);
        }
        Log.d(TAG, "fetchPrimaryData: done");
    }

    /**
     * this method is called from the UsersFragment, after the WorkManager
     * is finished its work (getting the common phone numbers) the UsersFragment
     * calls this method to start preparing the UsersUserKnow list.
     */
    public void prepareUserUserKnowList() {
        Log.d(TAG, "prepareUserUserKnowList: test size is: " + CustomPhoneNumberUtils.getUsersUserKnow().size());
        usersUserKnowList.addAll(CustomPhoneNumberUtils.getUsersUserKnow());
        CustomPhoneNumberUtils.clearLists();
        Log.d(TAG, "prepareUserUserKnowList: userUserKnowList size is: " + usersUserKnowList.size());
        invokeObservers.prepareDataFinished();
    }

    /**
     * the first time the user opens the app (specifically the UsersFragment)
     * this method will be called to start a work in the background thread
     * to store the user phone numbers in a SharedPreferences.
     */
    private void readTheContactsInTheDevice() {

        // for WorkManager functionality
        OneTimeWorkRequest contactsWork = new OneTimeWorkRequest.Builder(ReadContactsWorker.class)
                .build();
        workManager.enqueueUniqueWork("read_contacts_work", ExistingWorkPolicy.KEEP, contactsWork);
        deviceContactsObserver = workManager.getWorkInfoByIdLiveData(contactsWork.getId());

    }

    /**
     * when the user tabs in an item in the list in the UsersFragment
     * the fragment calls this method to get the user object
     * to be used the ChatsFragment flow.
     *
     * @param position     the position of the item clicked in the UsersFragment RecyclerView.
     * @param fromContacts a boolean type which indicates whether the filter is enabled or not
     *                     when the user clicks on the item, in order to take the object from all users list
     *                     or from the userUserKnow list.
     * @return returns a user object stored at the position in the list.
     */
    public User getUserInPosition(int position, boolean fromContacts) {
        if (fromContacts)
            return usersUserKnowList.get(position);
        else
            return allUsersList.get(position);
    }

    /**
     * get the currently displayed list to the user.
     *
     * @param fromContacts the same param of the method above to determine which method should
     *                     be returned.
     * @return returns the interesting list according to the boolean value.
     */
    public List<User> getUsers(boolean fromContacts) {
        if (fromContacts)
            return usersUserKnowList;
        else
            return allUsersList;
    }

    /**
     * a set of callbacks to provide the functionality of the UsersFragment.
     */
    public interface InvokeObservers {
        void invokeObservers();

        void observeCommonContacts();

        void prepareDataFinished();
    }
}
