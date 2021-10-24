package com.example.farfish.fragments;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.farfish.Adapters.MessagesListAdapter;
import com.example.farfish.Module.Connection;
import com.example.farfish.Module.MessagesPreference;
import com.example.farfish.Module.SharedPreferenceUtils;
import com.example.farfish.Module.workers.CleanUpOldDataPeriodicWork;
import com.example.farfish.R;
import com.example.farfish.data.MainViewModel;
import com.example.farfish.data.repositories.ChatsRepository;
import com.example.farfish.databinding.FragmentUserChatsBinding;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


public class UserChatsFragment extends Fragment implements MessagesListAdapter.ChatClick,
        ChatsRepository.DataReadyInterface, UserProfileFragment.CleanViewModel {

    private FirebaseAuth mAuth;
    private static final String TAG = UserChatsFragment.class.getSimpleName();
    private MessagesListAdapter mListAdapter;

    private NavController mNavController;
    private MainViewModel mainViewModel;

    private FragmentUserChatsBinding mBinding;

    public UserChatsFragment() {
        // Required empty public constructor
    }


    private void navigateToSignIn() {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.navigate(R.id.fragmentSignIn);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.sign_out:
                mAuth.signOut();
                Toast.makeText(requireContext(), getString(R.string.sign_out_msg), Toast.LENGTH_SHORT).show();
                SharedPreferenceUtils.saveUserSignOut(requireContext());
                onDestroy();
                mNavController.navigate(R.id.action_userChatsFragment_to_fragmentSignIn);
                break;
            case R.id.go_to_profile:
                mNavController.navigate(R.id.action_userChatsFragment_to_userProfileFragment);
                break;
            case R.id.report_issue:
                sendEmailIssue();
                break;
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        requireActivity().findViewById(R.id.bottom_nav).setVisibility(View.VISIBLE);
        Log.d(TAG, "onCreateView: ");
        mBinding = FragmentUserChatsBinding.inflate(inflater, container, false);
        View view = mBinding.getRoot();
        String mCurrentUserId = MessagesPreference.getUserId(requireContext());
        Log.d(TAG, "onCreate: ");
        mAuth = FirebaseAuth.getInstance();
        mNavController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        if (mAuth.getCurrentUser() == null) {
            navigateToSignIn();
        }
        Toolbar tb = view.findViewById(R.id.mainToolbar_frag);
        ((AppCompatActivity) requireActivity())
                .setSupportActionBar(tb);
        /*requireActivity().findViewById(R.id.nav_graph).setVisibility(View.VISIBLE);*/
        RecyclerView contactsRecycler = view.findViewById(R.id.userContactsRecyclerView);


        contactsRecycler.setAdapter(mListAdapter);
        // start the periodic work
        uniquelyScheduleCleanUPWorker();
        if (mAuth.getCurrentUser() != null) {
            if (mainViewModel == null)
                mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
            mainViewModel.getChatsRepository().setDataReadyInterface(this);
            mainViewModel.getUserChats().observe(getViewLifecycleOwner(), userChats -> {
                mListAdapter.submitList(new ArrayList<>(userChats));
                mBinding.userChatsProgressBar.setVisibility(View.GONE);
            });
        }

        checkUserConnection();

        UserProfileFragment.setCleaner(this);
        return view;
    }


    private void checkUserConnection() {

        if (!Connection.isUserConnected(requireContext())) {
            Snackbar.make(requireActivity().findViewById(R.id.bottom_nav), R.string.user_offline_msg, BaseTransientBottomBar.LENGTH_LONG)
                    .setAnchorView(R.id.bottom_nav).show();
        }

    }

    private void uniquelyScheduleCleanUPWorker() {

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build();

        PeriodicWorkRequest cleanUpRequest =
                new PeriodicWorkRequest.Builder(CleanUpOldDataPeriodicWork.class, 3, TimeUnit.HOURS)
                        .setConstraints(constraints)
                        .build();
        WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
                "cleanUpWork",
                ExistingPeriodicWorkPolicy.KEEP,
                cleanUpRequest);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        if (mainViewModel != null)
            mainViewModel.getChatsRepository().removeValueEventListener();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    @Override
    public void onChatClick(int position) {
        /*mainViewModel.getChatsRepository().removeValueEventListener();*/
        String targetUserId = mainViewModel.getChatsRepository().getMessageInPosition(position).getTargetId();
        Bundle primaryDataBundle = new Bundle();
        primaryDataBundle.putString("target_user_id", targetUserId);
        mNavController.navigate(R.id.chatsFragment, primaryDataBundle);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().finish();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
        mListAdapter = new MessagesListAdapter(new ArrayList<>(), requireContext(), this, true);
    }

    @Override
    public void dataIsReady() {
        mainViewModel.updateChats();
        Log.d(TAG, "dataIsReady: data is ready: " + mainViewModel.getChatsRepository().getUserChats().size());
        Log.d(TAG, "dataIsReady: isDetached: " + isVisible());
        if (mBinding != null) mBinding.userChatsProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void cleanViewModel() {
        this.mainViewModel = null;
    }
}