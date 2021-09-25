package com.example.friendlychat.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.friendlychat.Adapters.ContactsAdapter;
import com.example.friendlychat.Module.CustomPhoneNumberUtils;
import com.example.friendlychat.Module.FilterPreferenceUtils;
import com.example.friendlychat.Module.SharedPreferenceUtils;
import com.example.friendlychat.Module.User;
import com.example.friendlychat.R;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class UsersFragment extends Fragment implements  ContactsAdapter.OnChatClicked,
        SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = UsersFragment.class.getSimpleName();
    private FirebaseAuth mAuth;
    private List<User> users;
    private List<User> usersUserKnow;
    private ContactsAdapter usersAdapter;
    private FirebaseFirestore mFirestore;
    private NavController mNavController;

    private ImageView mFilterImageView;
    private List<String> mPhoneNumbersFromContacts = new ArrayList<>();
    private List<String> mPhonNumbersFromServer = new ArrayList<>();

    private View snackbarView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mFirestore = FirebaseFirestore.getInstance();
        users = new ArrayList<>();
        usersUserKnow = new ArrayList<>();
        usersAdapter = new ContactsAdapter(requireContext(), users, this);
        /*firebase database & auth*/
        mAuth = FirebaseAuth.getInstance();

        super.onCreate(savedInstanceState);
    }
    /* request permission*/
    private ActivityResultLauncher<String> requestPermissionToReadContacts =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    initializeUserAndData();
                } else {
                    Toast.makeText(requireContext(),
                            "In order to display for you the users that you might you" +
                                    "the app needs to read you contacts", Toast.LENGTH_LONG).show();
                }
            });
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        requireActivity().findViewById(R.id.bottom_nav).setVisibility(View.VISIBLE);
        View view =  inflater.inflate(R.layout.users_fragment, container, false);
        // check for the contacts permission if it's granted or not

        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.READ_CONTACTS) ==
                PackageManager.PERMISSION_GRANTED) {
            insertUserContacts();
        }else{
            requestPermissionToReadContacts.launch(Manifest.permission.READ_CONTACTS);
        }

        requireActivity().getSharedPreferences("filter_utils", Activity.MODE_PRIVATE).
                registerOnSharedPreferenceChangeListener(this);
        mNavController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        Toolbar tb = view.findViewById(R.id.mainToolbar_frag);
        ((AppCompatActivity) requireActivity())
                .setSupportActionBar(tb);
        mFilterImageView = tb.findViewById(R.id.filterImageView);
        updateFilterImageResoucre();
        mFilterImageView.setOnClickListener(filterListener -> {

            if (getFilterState())
                FilterPreferenceUtils.disableUsersFilter(requireContext());
            else
                FilterPreferenceUtils.enableUsersFilter(requireContext());
            updateFilterImageResoucre();
        });
        RecyclerView usersRecycler = view.findViewById(R.id.usersRecyclerView);
        usersRecycler.setAdapter(usersAdapter);

        snackbarView = view;
        checkUserConnection();
        return view;
    }

    private void updateFilterImageResoucre() {
        if (getFilterState())
            mFilterImageView.setImageResource(R.drawable.ic_filter_list_yellow_24);
        else
            mFilterImageView.setImageResource(R.drawable.ic_filter_list_24);
    }

    private void insertUserContacts() {
        /*---------------------------*/
        Cursor contactsCursor = requireContext().getContentResolver()
                .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        new String[] {ContactsContract.CommonDataKinds.Phone.NUMBER },
                        ContactsContract.CommonDataKinds.Phone.NUMBER + " != ?",
                        new String[] {" "},null);
        if (contactsCursor != null){
            while (contactsCursor.moveToNext()) {
                String phoneNumber = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                mPhoneNumbersFromContacts.add(phoneNumber);
            }
            initializeUserAndData();
            contactsCursor.close();
        }
        /*---------------------------*/
    }

    @Override
    public void onDestroyView() {
        requireActivity().getSharedPreferences("filter_utils", Activity.MODE_PRIVATE).
                unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroyView();
    }

    private void initializeUserAndData() {
        /*makeUserActive();*/
        Log.d(TAG, "initializeUserAndData: start getting data");
        mFirestore.collection("rooms")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // this method will fetch the primary data (insert all users inside users list)
                    fetchPrimaryData(queryDocumentSnapshots);
                    // this method will use the users list from above and filter it to users whom current user have in contacts
                    fetchDataInUsersUserKnowList();
                    // save it in a SharedPreference


                    // check for the filter state then populate the ui
                    if (getFilterState()) usersAdapter.setUsers(usersUserKnow);
                    else usersAdapter.setUsers(users);
                }).addOnFailureListener(exception -> Log.d(TAG, "initializeUserAndData: the exception in the new query caused by" +
                        exception.getMessage()));

    }

    private void fetchDataInUsersUserKnowList() {
        if (usersUserKnow.size() == 0) {
            Set<CustomPhoneNumberUtils> data =
                    CustomPhoneNumberUtils.getCommonPhoneNumbers(mPhonNumbersFromServer, mPhoneNumbersFromContacts, requireContext());
            Log.d(TAG, "initializeUserAndData: common number size " + data.size());
            Log.d(TAG, "initializeUserAndData: fianl common : " + data.toString());
            for (CustomPhoneNumberUtils datum : data) {
                String commonPhoneNumber = datum.getVal();
                Log.d(TAG, "initializeUserAndData: common phone number is: " +
                        commonPhoneNumber);
                for (User userUserKnow : users) {
                    String localUserPhoneNumber = userUserKnow.getPhoneNumber();
                    if (PhoneNumberUtils.compare(commonPhoneNumber, localUserPhoneNumber)) {
                        usersUserKnow.add(userUserKnow);
                    }
                }
            }
        }
    }

    private void fetchPrimaryData(QuerySnapshot queryDocumentSnapshots) {
        if (users.size() == 0) {
            for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                User user = dc.getDocument().toObject(User.class);
                String currentUserId = mAuth.getUid();
                Log.d(TAG, "initializeUserAndData: mildle");
                String phoneNumber = user.getPhoneNumber();
                Log.d(TAG, "initializeUserAndData: phoneNumberSever: " + phoneNumber);
                if (phoneNumber != null) {
                    if (!phoneNumber.equals(""))
                        mPhonNumbersFromServer.add(phoneNumber);
                }
                assert currentUserId != null;
                if (!currentUserId.equals(user.getUserId()))
                    users.add(user);
            }
        }
        Log.d(TAG, "fetchPrimaryData: uses list size: " + users.size());
    }


    @Override
    public void onChatClicked(int position) {

        String targetUserName;
        String photoUrl;
        String targetUserEmail;
        String userStatus;
        String targetUserId;
        long lastTimeSeen;

        if (getFilterState()){
            targetUserName= usersUserKnow.get(position).getUserName();
            photoUrl= usersUserKnow.get(position).getPhotoUrl();
            targetUserEmail= usersUserKnow.get(position).getEmail();
            userStatus= usersUserKnow.get(position).getStatus();
            targetUserId = usersUserKnow.get(position).getUserId();
            lastTimeSeen= usersUserKnow.get(position).getLastTimeSeen();
        }else{
             targetUserName= users.get(position).getUserName();
             photoUrl= users.get(position).getPhotoUrl();
             targetUserEmail= users.get(position).getEmail();
             userStatus= users.get(position).getStatus();
             lastTimeSeen= users.get(position).getLastTimeSeen();
             targetUserId = users.get(position).getUserId();
        }
        Bundle primaryDataBundle = new Bundle();
        primaryDataBundle.putString("target_user_name", targetUserName);
        primaryDataBundle.putString("target_user_photo_url", photoUrl);
        primaryDataBundle.putString("target_user_id", targetUserId);
        primaryDataBundle.putString("target_user_email", targetUserEmail);
        primaryDataBundle.putString("target_user_status", userStatus);
        primaryDataBundle.putLong("target_user_last_time_seen", lastTimeSeen);

        NavController controller = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        controller.navigate(R.id.action_usersFragment_to_chatsFragment, primaryDataBundle);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.sign_out:
                mAuth.signOut();
                Toast.makeText(requireContext(), "Signed out successfully", Toast.LENGTH_SHORT).show();
                SharedPreferenceUtils.saveUserSignOut(requireContext());
                mNavController.navigate(R.id.action_usersFragment_to_fragmentSignIn);
                break;
            case R.id.go_to_profile:
                mNavController.navigate(R.id.action_usersFragment_to_userProfileFragment);
                break;
            case R.id.report_issue:
                // will be implemented...
                break;
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
       boolean activeFilter = sharedPreferences.getBoolean(key, true);
        Log.d(TAG, "onSharedPreferenceChanged: filter state: " + activeFilter);
        if (activeFilter) {
            usersAdapter.setUsers(usersUserKnow);
        }else{
            usersAdapter.setUsers(users);
            Log.d(TAG, "onSharedPreferenceChanged: list of users size is: " + users.size());
        }
    }

    private Boolean getFilterState () {
        return FilterPreferenceUtils.isFilterActive(requireContext());
    }

    private void checkUserConnection() {

        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    Log.d(TAG, "connected");
                } else {
                    Log.d(TAG, "not connected");
                    Snackbar.make(snackbarView, R.string.user_ofline_msg, BaseTransientBottomBar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Listener was cancelled");
            }
        });
    }
}
