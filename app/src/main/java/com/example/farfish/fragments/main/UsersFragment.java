package com.example.farfish.fragments.main;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
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
import com.example.farfish.Module.dataclasses.User;
import com.example.farfish.Module.preferences.SharedPreferenceUtils;
import com.example.farfish.Module.util.Connection;
import com.example.farfish.Module.util.FilterPreferenceUtils;
import com.example.farfish.R;
import com.example.farfish.data.MainViewModel;
import com.example.farfish.data.repositories.UsersRepository;
import com.example.farfish.databinding.UsersFragmentBinding;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * This fragment is responsible for displaying the users data in the app, the data is displayed
 * in one of two lists, all users list (which contains public all users) and users
 * user may know (the user store their phone numbers in their device)
 */
@AndroidEntryPoint
public class UsersFragment extends Fragment implements ContactsListAdapter.OnChatClicked,
        SharedPreferences.OnSharedPreferenceChangeListener, UsersRepository.InvokeObservers {
    // users view model
    public MainViewModel mModel;
    /*private ContactsAdapter usersAdapter;*/
    @Inject
    public ContactsListAdapter mUserListAdapter;
    private UsersFragmentBinding mBinding;
    private NavController mNavController;

    /*ok this keyboard is really great
    * after I work as ok that's is*/



    /* request permission*/
    private ActivityResultLauncher<String> requestPermissionToReadContacts =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    getViewLifecycleOwnerLiveData().observe(this, lifecycleOwner ->
                            mModel.getAllUsers().observe(lifecycleOwner, users ->
                                    mUserListAdapter.customSubmitUserList(users)));
                } else {
                    Toast.makeText(requireContext(),
                            getString(R.string.access_contacts_permission_msg), Toast.LENGTH_LONG).show();
                    mBinding.loadUsersProgressBar.setVisibility(View.GONE);
                }

            });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mUserListAdapter.setOnChatClicked(this);
        mUserListAdapter.setForStatus(false);
        super.onCreate(savedInstanceState);
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
        mModel.getUsersRepository().setObservers(this);
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.READ_CONTACTS) ==
                PackageManager.PERMISSION_GRANTED) {
            getViewLifecycleOwnerLiveData().observe(getViewLifecycleOwner(), lifecycleOwner ->
                    mModel.getAllUsers().observe(lifecycleOwner, users -> {
                        mUserListAdapter.customSubmitUserList(users);
                        mBinding.loadUsersProgressBar.setVisibility(View.GONE);
                    }));
        } else {
            requestPermissionToReadContacts.launch(Manifest.permission.READ_CONTACTS);
        }
         super.onDestroyView();
        requireActivity().getSharedPreferences("filter_utils", Activity.MODE_PRIVATE).
                unregisterOnSharedPreferenceChangeListener(this);
        mBinding = null;
    }


    /**
     * when the user click in one of the messages (messages that displays images) the method
     * will be called to open the image in a full screen fragment.
     *
     * @param position takes the position of the selected image in the RecyclerView to be used in the
     *                 app flow.
     */
    @Override
    public void onChatClicked(int position) {
        // ok this is one of the most imp
        User selectedUser = mModel.getUsersRepository().getUserInPosition(position, getFilterState());
        String targetUserId = selectedUser.getUserId();
        Bundle primaryDataBundle = new Bundle();
        primaryDataBundle.putString("target_user_id", targetUserId);
        mNavController.navigate(R.id.action_usersFragment_to_chatsFragment, primaryDataBundle);
    }

    /**
     * this method wll be called when the user presses on one of the items
     * in the menu for a specific action.
     *
     * @param item the selected item from the menu bar.
     * @return true to tell the OS (the android operation system) that we handled
     * this method functionality.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.sign_out:
               /* FirebaseAuth.getInstance().signOut();
                Toast.makeText(requireContext(), requireContext().getString(R.string.sign_out_msg), Toast.LENGTH_SHORT).show();*/
                SharedPreferenceUtils.saveUserSignOut(requireContext());
                mModel.getMessagingRepository().resetLastTimeSeen();
                mNavController.navigate(R.id.action_usersFragment_to_fragmentSignIn);
                break;
            case R.id.go_to_profile:
                mModel.getMessagingRepository().resetLastTimeSeen();
                mNavController.navigate(R.id.action_usersFragment_to_userProfileFragment);
                break;
            case R.id.report_issue:
                sendEmailIssue();
                break;
        }
        return true;
    }

    /**
     * called to handle sending an issue as an email to
     * my email account.
     */
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

    /**
     * when any changes occurs in the SharedPreferences the method will be called
     * to update the image resource and display the desired list for the user.
     *
     * @param sharedPreferences the sharedPreferences instance that the change happened inside it.
     * @param key               the key of the data stored in the SharedPreferences and has changes as well.
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        boolean activeFilter = sharedPreferences.getBoolean(key, true);
        mModel.updateUsers(activeFilter);
    }

    /**
     * a simple method to get the filter state (enabled / disabled)
     *
     * @return the filter state true if enable and vice-versa.
     */
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
        if (mModel.getUsersRepository().deviceContactsObserver == null) return;
        getViewLifecycleOwnerLiveData().observe(this, lifecycleOwner ->
                mModel.getUsersRepository().deviceContactsObserver.observe(lifecycleOwner, workInfo -> {
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        mModel.getUsersRepository().readContactsWorkerEnd();
                    }
                }));
    }

    @Override
    public void observeCommonContacts() {

        getViewLifecycleOwnerLiveData().observe(this, lifecycleOwner ->
                mModel.getUsersRepository().commonContactsObserver.observe(getViewLifecycleOwner(), commonWorkInfo -> {
                    if (commonWorkInfo != null && commonWorkInfo.getState().isFinished()) {
                        mModel.getUsersRepository().prepareUserUserKnowList();

                    }
                }));
    }

    /**
     * this callback will be called when the data is ready from the UsersFragment.
     */
    @Override
    public void prepareDataFinished() {
        mModel.updateUsers(getFilterState());
        mBinding.loadUsersProgressBar.setVisibility(View.GONE);
    }

}
