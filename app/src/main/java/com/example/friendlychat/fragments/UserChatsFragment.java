package com.example.friendlychat.fragments;

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

import com.example.friendlychat.Adapters.ContactsAdapter;
import com.example.friendlychat.Module.Message;
import com.example.friendlychat.Module.MessagesPreference;
import com.example.friendlychat.Module.NotificationUtils;
import com.example.friendlychat.Module.SharedPreferenceUtils;
import com.example.friendlychat.Module.workers.CleanUpOldDataPeriodicWork;
import com.example.friendlychat.R;
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


public class UserChatsFragment extends Fragment implements ContactsAdapter.OnChatClicked, ValueEventListener{

    private FirebaseAuth mAuth;
    private static final String TAG = UserChatsFragment.class.getSimpleName();
    private List<Message> messages;
    private ContactsAdapter contactsAdapter;
    private DatabaseReference mCurrentUserRoomReference;
    private NavController mNavController;
    private String mCurrentUserId;

    public UserChatsFragment() {
        // Required empty public constructor
    }


    private View snackbarView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mCurrentUserId = MessagesPreference.getUserId(requireContext());

        mCurrentUserRoomReference = FirebaseDatabase.getInstance().getReference("rooms")
                .child(mCurrentUserId);
        messages = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        contactsAdapter = new ContactsAdapter(getContext(), messages, this, null);
    }

    private void navigateToSignIn() {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.navigate(R.id.fragmentSignIn);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        requireActivity().findViewById(R.id.bottom_nav).setVisibility(View.VISIBLE);
        Log.d(TAG, "onCreateView: ");
        View view =inflater.inflate(R.layout.fragment_user_chats, container, false);

        mNavController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        if (mAuth.getCurrentUser() == null){
            navigateToSignIn();
        }
        Toolbar tb = view.findViewById(R.id.mainToolbar_frag);
        ((AppCompatActivity) requireActivity())
                .setSupportActionBar(tb);
        /*requireActivity().findViewById(R.id.nav_graph).setVisibility(View.VISIBLE);*/
        RecyclerView contactsRecycler = view.findViewById(R.id.userContactsRecyclerView);


        contactsRecycler.setAdapter(contactsAdapter);
        // start the periodic work
        uniquelyScheduleCleanUPWorker();
        if (mAuth.getCurrentUser() != null && messages.size() == 0)
            initializeUserAndData();
        snackbarView = view;
        checkUserConnection();
        return view;
    }



    private void initializeUserAndData() {

        mCurrentUserRoomReference.addValueEventListener(this);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.sign_out:
                mAuth.signOut();
                Toast.makeText(requireContext(), "Signed out successfully", Toast.LENGTH_SHORT).show();
                SharedPreferenceUtils.saveUserSignOut(requireContext());
                mNavController.navigate(R.id.action_userChatsFragment_to_fragmentSignIn);
                break;
            case R.id.go_to_profile:
                mNavController.navigate(R.id.action_userChatsFragment_to_userProfileFragment);
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
    public void onChatClicked(int position) {

        String targetUserId = messages.get(position).getTargetId();
        Bundle primaryDataBundle = new Bundle();
        primaryDataBundle.putString("target_user_id", targetUserId);
        mNavController.navigate(R.id.chatsFragment, primaryDataBundle);

    }

    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
        messages.clear();
        Iterable<DataSnapshot> roomsIterable = snapshot.getChildren();
        for (DataSnapshot roomsSnapshot : roomsIterable) {

            Iterable<DataSnapshot> messagesIterable = roomsSnapshot.getChildren();
            Message lastMessage = null;
            int newMessageCounter = 0;
            for (DataSnapshot messageSnapShot : messagesIterable){
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
        contactsAdapter.notifyDataSetChanged();

    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }

    @Override
    public void onDestroy() {
        mCurrentUserRoomReference.removeEventListener(this);
        super.onDestroy();
    }

    private void sendNotification(Message message) {
        if (!message.getIsRead() && !message.getSenderId().equals(mCurrentUserId))
            NotificationUtils.notifyUserOfNewMessage(requireContext(), message);
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
                    Snackbar.make(requireActivity().findViewById(R.id.bottom_nav), R.string.user_ofline_msg, BaseTransientBottomBar.LENGTH_LONG)
                            .setAnchorView(R.id.bottom_nav).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Listener was cancelled");
            }
        });
    }

    private void uniquelyScheduleCleanUPWorker() {

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build();

        PeriodicWorkRequest cleanUpRequest =
                new PeriodicWorkRequest.Builder(CleanUpOldDataPeriodicWork.class, 15, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .build();
        WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
                "cleanUpWork",
                ExistingPeriodicWorkPolicy.KEEP,
                cleanUpRequest);


    }
}