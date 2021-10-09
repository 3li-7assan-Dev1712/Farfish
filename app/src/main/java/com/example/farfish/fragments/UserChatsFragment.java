package com.example.farfish.fragments;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
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
import com.example.farfish.Module.Message;
import com.example.farfish.Module.MessagesPreference;
import com.example.farfish.Module.NotificationUtils;
import com.example.farfish.Module.SharedPreferenceUtils;
import com.example.farfish.Module.workers.CleanUpOldDataPeriodicWork;
import com.example.farfish.R;
import com.example.farfish.databinding.FragmentUserChatsBinding;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class UserChatsFragment extends Fragment implements MessagesListAdapter.ChatClick, ValueEventListener {

    private FirebaseAuth mAuth;
    private static final String TAG = UserChatsFragment.class.getSimpleName();
    private static List<Message> messages;
    private MessagesListAdapter mListAdapter;
    private DatabaseReference mCurrentUserRoomReference;
    private NavController mNavController;
    private String mCurrentUserId;


    private FragmentUserChatsBinding mBinding;

    public UserChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        messages = new ArrayList<>();
        mListAdapter = new MessagesListAdapter(messages, requireContext(), this, true);
    }

    private void navigateToSignIn() {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.navigate(R.id.fragmentSignIn);
    }

    private int tracker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //prevent any orientation changes
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        requireActivity().findViewById(R.id.bottom_nav).setVisibility(View.VISIBLE);
        Log.d(TAG, "onCreateView: ");
        mBinding = FragmentUserChatsBinding.inflate(inflater, container, false);
        View view = mBinding.getRoot();
        mCurrentUserId = MessagesPreference.getUserId(requireContext());
        Log.d(TAG, "onCreate: ");
        mCurrentUserRoomReference = FirebaseDatabase.getInstance().getReference("rooms")
                .child(mCurrentUserId);
        mAuth = FirebaseAuth.getInstance();
        tracker = 1;
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
        Log.d(TAG, "onCreateView: messges size is: " + messages.size());
        if (messages.size() == 0)
            initializeUserAndData();
        else
            mBinding.userChatsProgressBar.setVisibility(View.GONE);
        checkUserConnection();

        return view;
    }


    private void initializeUserAndData() {

        mCurrentUserRoomReference.addValueEventListener(this);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.sign_out:
                mAuth.signOut();
                Toast.makeText(requireContext(), getString(R.string.sign_out_msg), Toast.LENGTH_SHORT).show();
                SharedPreferenceUtils.saveUserSignOut(requireContext());
                messages.clear();
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
    public void onDataChange(@NonNull DataSnapshot snapshot) {
        messages.clear();
        Iterable<DataSnapshot> roomsIterable = snapshot.getChildren();
        for (DataSnapshot roomsSnapshot : roomsIterable) {

            Iterable<DataSnapshot> messagesIterable = roomsSnapshot.getChildren();
            Message lastMessage = null;
            int newMessageCounter = 0;
            for (DataSnapshot messageSnapShot : messagesIterable) {
                if (!messageSnapShot.getKey().equals("isWriting")) {
                    lastMessage = messageSnapShot.getValue(Message.class);
                    if (!lastMessage.getIsRead())
                        newMessageCounter++;
                }
            }
            if (lastMessage != null) {
                String senderId = lastMessage.getSenderId();
                if (!senderId.equals(mCurrentUserId) && newMessageCounter != 0)
                    lastMessage.setNewMessagesCount(newMessageCounter);
                messages.add(lastMessage);
                sendNotification(lastMessage);
            }
        }
        if (mBinding != null)
            mBinding.userChatsProgressBar.setVisibility(View.GONE);
        mListAdapter.submitList(new ArrayList<>(messages));

    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        mCurrentUserRoomReference.removeEventListener(this);
    }


    private void sendNotification(Message message) {
        if (tracker == 0) {
            Log.d(TAG, "sendNotification: " + tracker);
            if (!message.getIsRead() && !message.getSenderId().equals(mCurrentUserId))
                NotificationUtils.notifyUserOfNewMessage(requireContext(), message);
        } else
            tracker = 0;
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
    public void onDestroyView() {
        super.onDestroyView();
        tracker = 1;
        mBinding = null;

    }

    @Override
    public void onChatClick(int position) {
        String targetUserId = messages.get(position).getTargetId();
        Bundle primaryDataBundle = new Bundle();
        primaryDataBundle.putString("target_user_id", targetUserId);
        mNavController.navigate(R.id.chatsFragment, primaryDataBundle);
    }

    public static List<Message> getMessages() {
        return messages;
    }
}