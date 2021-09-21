package com.example.friendlychat.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.service.voice.AlwaysOnHotwordDetector;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.friendlychat.Adapters.ContactsAdapter;
import com.example.friendlychat.Module.CustomPhoneNumberUtils;
import com.example.friendlychat.Module.Message;
import com.example.friendlychat.Module.MessagesPreference;
import com.example.friendlychat.Module.SharedPreferenceUtils;
import com.example.friendlychat.Module.User;
import com.example.friendlychat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class UsersFragment extends Fragment implements  ContactsAdapter.OnChatClicked {
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        requireActivity().findViewById(R.id.bottom_nav).setVisibility(View.VISIBLE);
        View view =  inflater.inflate(R.layout.users_fragment, container, false);
        insertUserContacts();
        mNavController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        Toolbar tb = view.findViewById(R.id.mainToolbar_frag);
        ((AppCompatActivity) requireActivity())
                .setSupportActionBar(tb);
        mFilterImageView = tb.findViewById(R.id.filterImageView);
        updateFilterImageResoucre();
        mFilterImageView.setOnClickListener(filterListener -> {
            if (MessagesPreference.isFilterActive(requireContext()))
                MessagesPreference.disableUsersFilter(requireContext());
            else
                MessagesPreference.enableUsersFilter(requireContext());
            updateFilterImageResoucre();
        });
        RecyclerView usersRecycler = view.findViewById(R.id.usersRecyclerView);
        usersRecycler.setAdapter(usersAdapter);
         return view;
    }

    private void updateFilterImageResoucre() {
        if (MessagesPreference.isFilterActive(requireContext()))
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
            int number = contactsCursor.getCount();
            String demoPhoneNumber = "0115735414";
            String father = "0123749439";
            String mother = "+249122155276";
            int matchNumber =0;
            Log.d(TAG, "updateUI: there are " + number + " contacts in this device");
            while (contactsCursor.moveToNext()) {
                String phoneNumber = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                mPhoneNumbersFromContacts.add(phoneNumber);
                if (PhoneNumberUtils.compare(phoneNumber, demoPhoneNumber) ||PhoneNumberUtils.compare(phoneNumber, father)
                        || PhoneNumberUtils.compare(phoneNumber, mother) ){
                    matchNumber++;
                }

            }
            initializeUserAndData();
            Log.d(TAG, "updateUI: match number is : " + matchNumber);
            contactsCursor.close();
        }
        /*---------------------------*/
    }


    private void initializeUserAndData() {
        /*makeUserActive();*/
        Log.d(TAG, "initializeUserAndData: start getting data");
        mFirestore.collection("rooms")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentChange dc: queryDocumentSnapshots.getDocumentChanges()){
                        User user = dc.getDocument().toObject(User.class);
                        String currentUserId = mAuth.getUid();
                        Log.d(TAG, "initializeUserAndData: mildle");
                        String phoneNumber = user.getPhoneNumber();
                        Log.d(TAG, "initializeUserAndData: phoneNumberSever: " + phoneNumber);
                        if (phoneNumber != null) {
                            if(!phoneNumber.equals(""))
                                mPhonNumbersFromServer.add(phoneNumber);
                        }
                        assert currentUserId != null;
                        if (!currentUserId.equals(user.getUserId()))
                            users.add(user);
                    }
                    Log.d(TAG, "initializeUserAndData: phone number size form the server: " + mPhonNumbersFromServer.size());
                    Set<CustomPhoneNumberUtils> data =
                            CustomPhoneNumberUtils.getCommonPhoneNumbers(mPhonNumbersFromServer, mPhoneNumbersFromContacts);
                    Log.d(TAG, "initializeUserAndData: common number size " + data.size());
                    Log.d(TAG, "initializeUserAndData: fianl common : " + data.toString());
                    for (CustomPhoneNumberUtils datum : data) {
                        String commonPhoneNumber = datum.getVal();
                        Log.d(TAG, "initializeUserAndData: common phone number is: " +
                                commonPhoneNumber);
                        for (User userUserKnow : users){
                            String localUserPhoneNumber = userUserKnow.getPhoneNumber();
                            if (PhoneNumberUtils.compare(commonPhoneNumber, localUserPhoneNumber)) {
                                usersUserKnow.add(userUserKnow);
                            }
                        }
                    }
                    usersAdapter.setUsers(usersUserKnow);
                }).addOnFailureListener(exception -> {
            Log.d(TAG, "initializeUserAndData: the exception in the new query caused by" +
                    exception.getMessage());
        });
        Log.d(TAG, "initializeUserAndData: end getting data");



    }


    @Override
    public void onChatClicked(int position) {

        String chatTitle = users.get(position).getUserName();
        String photoUrl = users.get(position).getPhotoUrl();
        String targetUserEmail = users.get(position).getEmail();
        String userStatus = users.get(position).getStatus();
        long lastTimeSeen = users.get(position).getLastTimeSeen();
        Bundle primaryDataBundle = new Bundle();
        primaryDataBundle.putString("target_user_name", chatTitle);
        primaryDataBundle.putString("target_user_photo_url", photoUrl);
        if (!chatTitle.equals("All people use the app")) {
            String targetUserId = users.get(position).getUserId();
            primaryDataBundle.putString("target_user_id", targetUserId);
            primaryDataBundle.putString("target_user_email", targetUserEmail);
            primaryDataBundle.putString("target_user_status", userStatus);
            primaryDataBundle.putLong("target_user_last_time_seen", lastTimeSeen);
            primaryDataBundle.putBoolean("isGroup", false);

        }else
            primaryDataBundle.putBoolean("isGroup", true);

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
                mNavController.navigate(R.id.fragmentSignIn);
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

}
