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
import androidx.hilt.navigation.HiltViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.farfish.Adapters.ContactsListAdapter;
import com.example.farfish.Module.Connection;
import com.example.farfish.Module.FilterPreferenceUtils;
import com.example.farfish.Module.SharedPreferenceUtils;
import com.example.farfish.Module.User;
import com.example.farfish.Module.workers.ReadContactsWorker;
import com.example.farfish.Module.workers.ReadDataFromServerWorker;
import com.example.farfish.R;
import com.example.farfish.data.MainViewModel;
import com.example.farfish.data.repositories.UsersRepository;
import com.example.farfish.databinding.UsersFragmentBinding;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class UsersFragment extends Fragment implements ContactsListAdapter.OnChatClicked,
        SharedPreferences.OnSharedPreferenceChangeListener, UsersRepository.InvokeObservers {
    private static final String TAG = UsersFragment.class.getSimpleName();
    private static final String ORIENTATION_CHANGE = "change";
    // users view model

    public MainViewModel mModel;

    private UsersFragmentBinding mBinding;
    private FirebaseAuth mAuth;
    /*private ContactsAdapter usersAdapter;*/
    private ContactsListAdapter mUserListAdapter;
    private NavController mNavController;
    private boolean isProgressBarVisible = true;

    /* request permission*/
    private ActivityResultLauncher<String> requestPermissionToReadContacts =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {

                    mModel.getAllUsers().observe(getViewLifecycleOwner(), users ->
                            mUserListAdapter.customSubmitUserList(users));
                    setProgresBarVisibility();
                } else {
                    Toast.makeText(requireContext(),
                            getString(R.string.access_contacts_permission_msg), Toast.LENGTH_LONG).show();
                    mBinding.loadUsersProgressBar.setVisibility(View.GONE);
                }
            });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        /*usersAdapter = new ContactsAdapter(requireContext(), publicUsers, this);*/
        mUserListAdapter = new ContactsListAdapter(this, false);
        /*firebase database & auth*/
        mAuth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
    }

    private void setProgresBarVisibility() {
        if (isProgressBarVisible)
            mBinding.loadUsersProgressBar.setVisibility(View.VISIBLE);
        else
            mBinding.loadUsersProgressBar.setVisibility(View.GONE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = UsersFragmentBinding.inflate(inflater, container, false);
        requireActivity().findViewById(R.id.bottom_nav).setVisibility(View.VISIBLE);
        View view = mBinding.getRoot();
        mNavController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        NavBackStackEntry backStackEntry = mNavController.getBackStackEntry(R.id.nav_graph);
        mModel = new ViewModelProvider(
                backStackEntry,
                HiltViewModelFactory.create(requireContext(), backStackEntry)
        ).get(MainViewModel.class);
        Log.d(TAG, "onCreateView: mModule has code: " + mModel.hashCode());
        mModel.getUsersRepository().setObservers(this);
        if (mBinding != null) setProgresBarVisibility();
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.READ_CONTACTS) ==
                PackageManager.PERMISSION_GRANTED) {
            // check if we have the phone numbers already
            mModel.getAllUsers().observe(getViewLifecycleOwner(), users -> {
                mUserListAdapter.customSubmitUserList(users);

            });
        } else {
            requestPermissionToReadContacts.launch(Manifest.permission.READ_CONTACTS);
        }
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(ORIENTATION_CHANGE))
                mBinding.loadUsersProgressBar.setVisibility(View.GONE);
        }


        requireActivity().getSharedPreferences("filter_utils", Activity.MODE_PRIVATE).
                registerOnSharedPreferenceChangeListener(this);

        ((AppCompatActivity) requireActivity())
                .setSupportActionBar(mBinding.mainToolbarFrag);
        updateFilterImageResoucre();
        mBinding.usersToolbar.filterImageView.setOnClickListener(filterListener -> {
            if (ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.READ_CONTACTS) ==
                    PackageManager.PERMISSION_GRANTED) {
                if (getFilterState())
                    FilterPreferenceUtils.disableUsersFilter(requireContext());
                else
                    FilterPreferenceUtils.enableUsersFilter(requireContext());
                updateFilterImageResoucre();
            } else
                requestPermissionToReadContacts.launch(Manifest.permission.READ_CONTACTS);

        });
        // migrating from the old normal RecyclerView adapter to the new ListRecyclerView Adapter (With ListAdapter)
        // to get benefit from the DiffUtil class

        mBinding.usersRecyclerView.setAdapter(mUserListAdapter);
        checkUserConnection();
        return view;
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(ORIENTATION_CHANGE, isProgressBarVisible);
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
        isProgressBarVisible = false;
        User selectedUser = mModel.getUsersRepository().getUserInPosition(position, getFilterState());
        String targetUserId = selectedUser.getUserId();
        Log.d(TAG, "onChatClick: target user id: " + targetUserId);
        Bundle primaryDataBundle = new Bundle();
        primaryDataBundle.putString("target_user_id", targetUserId);
        mNavController.navigate(R.id.action_usersFragment_to_chatsFragment, primaryDataBundle);
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
                isProgressBarVisible = false;
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
        getViewLifecycleOwnerLiveData().observe(this, lifecycleOwner -> {
            mModel.getUsersRepository().deviceContactsObserver.observe(lifecycleOwner, workInfo -> {
                if (workInfo != null && workInfo.getState().isFinished()) {
                    List<String> deviceContacts = ReadContactsWorker.contactsList;
                    mModel.getUsersRepository().readContactsWorkerEnd(deviceContacts);
                }
            });
        });


    }

    @Override
    public void observeCommonContacts() {
        Log.d(TAG, "observeCommonContacts: ");
        mModel.getUsersRepository().commonContactsObserver.observe(getViewLifecycleOwner(), commonWorkInfo -> {
            if (commonWorkInfo != null && commonWorkInfo.getState().isFinished()) {
                List<String> commonContacts = ReadDataFromServerWorker.getCommonPhoneNumbers();
                if (commonContacts != null)
                    mModel.getUsersRepository().prepareUserUserKnowList(commonContacts);
                else
                    Log.d(TAG, "observeCommonContacts: common contacts is null");
            }
        });
    }

    @Override
    public void prepareDataFinished() {
        mModel.updateUsers(getFilterState());
        isProgressBarVisible = false;
        setProgresBarVisibility();
    }

}
