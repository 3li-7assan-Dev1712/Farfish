package com.example.farfish.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.farfish.Adapters.ContactsListAdapter;
import com.example.farfish.Module.Connection;
import com.example.farfish.Module.FilterPreferenceUtils;
import com.example.farfish.Module.SharedPreferenceUtils;
import com.example.farfish.Module.User;
import com.example.farfish.R;
import com.example.farfish.data.MainViewModel;
import com.example.farfish.databinding.UsersFragmentBinding;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

public class UsersFragment extends Fragment implements ContactsListAdapter.OnChatClicked,
        SharedPreferences.OnSharedPreferenceChangeListener, MainViewModel.InvokeObservers {
    private static final String TAG = UsersFragment.class.getSimpleName();
    // users view model
    private MainViewModel mModel;
    private UsersFragmentBinding mBinding;
    private FirebaseAuth mAuth;
    /*private ContactsAdapter usersAdapter;*/
    private ContactsListAdapter mUserListAdapter;
    private NavController mNavController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        /*usersAdapter = new ContactsAdapter(requireContext(), publicUsers, this);*/
        mUserListAdapter = new ContactsListAdapter(this);
        /*firebase database & auth*/
        mAuth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
    }

    /* request permission*/
    private ActivityResultLauncher<String> requestPermissionToReadContacts =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    mModel.getAllUsers().observe(getViewLifecycleOwner(), users ->
                            mUserListAdapter.submitList(users));
                } else {
                    Toast.makeText(requireContext(),
                            getString(R.string.access_contacts_permission_msg), Toast.LENGTH_LONG).show();
                    mBinding.loadUsersProgressBar.setVisibility(View.GONE);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = UsersFragmentBinding.inflate(inflater, container, false);
        requireActivity().findViewById(R.id.bottom_nav).setVisibility(View.VISIBLE);
        View view = mBinding.getRoot();
        mModel = new ViewModelProvider(this).get(MainViewModel.class);
        mModel.setObservers(this);
        if (mBinding != null) mBinding.loadUsersProgressBar.setVisibility(View.VISIBLE);
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.READ_CONTACTS) ==
                PackageManager.PERMISSION_GRANTED) {
            // check if we have the phone numbers already
            mModel.getAllUsers().observe(getViewLifecycleOwner(), users -> {
                Log.d(TAG, "onCreateView: users size is: " + users.size());
                mUserListAdapter.submitList(users);
            });
        } else {
            requestPermissionToReadContacts.launch(Manifest.permission.READ_CONTACTS);
        }

        requireActivity().getSharedPreferences("filter_utils", Activity.MODE_PRIVATE).
                registerOnSharedPreferenceChangeListener(this);
        mNavController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        ((AppCompatActivity) requireActivity())
                .setSupportActionBar(mBinding.mainToolbarFrag);
        updateFilterImageResoucre();
        mBinding.usersToolbar.filterImageView.setOnClickListener(filterListener -> {

            if (getFilterState())
                FilterPreferenceUtils.disableUsersFilter(requireContext());
            else
                FilterPreferenceUtils.enableUsersFilter(requireContext());
            updateFilterImageResoucre();
        });
        RecyclerView usersRecycler = view.findViewById(R.id.usersRecyclerView);
        // migrating from the old normal RecyclerView adapter to the new ListRecyclerView Adapter (With ListAdapter)
        // to get benefit from the DiffUtil class

        usersRecycler.setAdapter(mUserListAdapter);

        checkUserConnection();
        return view;
    }


    private void updateFilterImageResoucre() {
        if (getFilterState())
            mBinding.usersToolbar.filterImageView.setImageResource(R.drawable.ic_filter_list_yellow_24);
        else
            mBinding.usersToolbar.filterImageView.setImageResource(R.drawable.ic_filter_list_24);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        requireActivity().getSharedPreferences("filter_utils", Activity.MODE_PRIVATE).
                unregisterOnSharedPreferenceChangeListener(this);
        mBinding = null;
    }


    @Override
    public void onChatClicked(int position) {

        String targetUserName;
        String photoUrl;
        String targetUserEmail;
        String userStatus;
        String targetUserId;
        long lastTimeSeen;
        User selectedUser = mModel.getUserInPosition(position, getFilterState());
        targetUserName = selectedUser.getUserName();
        photoUrl = selectedUser.getPhotoUrl();
        targetUserEmail = selectedUser.getEmail();
        userStatus = selectedUser.getStatus();
        targetUserId = selectedUser.getUserId();
        lastTimeSeen = selectedUser.getLastTimeSeen();
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
        switch (id) {
            case R.id.sign_out:
                mAuth.signOut();
                Toast.makeText(requireContext(), requireContext().getString(R.string.sign_out_msg), Toast.LENGTH_SHORT).show();
                SharedPreferenceUtils.saveUserSignOut(requireContext());
                mNavController.navigate(R.id.action_usersFragment_to_fragmentSignIn);
                break;
            case R.id.go_to_profile:
                mNavController.navigate(R.id.action_usersFragment_to_userProfileFragment);
                break;
            case R.id.report_issue:
                sendEmailIssue();
                break;
        }
        return true;
    }

    private void sendEmailIssue() {
        final Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/plain")
                .putExtra(Intent.EXTRA_EMAIL, "alihassan17122002@gmail.com")
                .putExtra(Intent.EXTRA_SUBJECT, getString(R.string.report_issue_email))
                .putExtra(Intent.EXTRA_TEXT, getString(R.string.type_your_issue));
        try {
            startActivity(Intent.createChooser(emailIntent, getString(R.string.choose_app_to_send_emial)));
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(requireActivity(), getString(R.string.no_app_found), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        boolean activeFilter = sharedPreferences.getBoolean(key, true);
        Log.d(TAG, "onSharedPreferenceChanged: filter state: " + activeFilter);
        mModel.updateUsers(activeFilter);
    }

    private Boolean getFilterState() {
        return FilterPreferenceUtils.isFilterActive(requireContext());
    }

    private void checkUserConnection() {
        if (!Connection.isUserConnected(requireContext())) {
            Snackbar.make(requireActivity().findViewById(R.id.bottom_nav), R.string.user_offline_msg, BaseTransientBottomBar.LENGTH_LONG)
                    .setAnchorView(R.id.bottom_nav).show();
        }
    }

    @Override
    public void invokeObservers() {
        mModel.deviceContactsObserver.observe(getViewLifecycleOwner(), workInfo -> {
            if (workInfo != null && workInfo.getState().isFinished()) {
                String[] deviceContacts = workInfo.getOutputData().getStringArray("contacts");
                mModel.readContactsWorkerEnd(deviceContacts);
            }
        });

    }

    @Override
    public void observeCommonContacts() {
        Log.d(TAG, "observeCommonContacts: ");
        mModel.commonContactsObserver.observe(getViewLifecycleOwner(), commonWorkInfo -> {
            if (commonWorkInfo != null && commonWorkInfo.getState().isFinished()) {
                String[] commonContacts = commonWorkInfo.getOutputData().getStringArray("common_phone_numbers");
                if (commonContacts != null)
                    mModel.prepareUserUserKnowList(commonContacts);
                else
                    Log.d(TAG, "observeCommonContacts: common contacts is null");
            }
        });
    }

    @Override
    public void prepareDataFinished() {
        mModel.updateUsers(getFilterState());
        mBinding.loadUsersProgressBar.setVisibility(View.GONE);
    }

}
