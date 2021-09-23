package com.example.friendlychat.fragments;

import android.content.Context;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.friendlychat.Adapters.ContactsAdapter;
import com.example.friendlychat.Module.FullMessage;
import com.example.friendlychat.Module.Message;
import com.example.friendlychat.Module.MessagesPreference;
import com.example.friendlychat.Module.SharedPreferenceUtils;
import com.example.friendlychat.Module.Status;
import com.example.friendlychat.R;
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
import java.util.EventListener;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;


public class UserChatsFragment extends Fragment implements ContactsAdapter.OnChatClicked, ValueEventListener{

    private FirebaseAuth mAuth;
    private static final String TAG = UserChatsFragment.class.getSimpleName();

    private List<Message> messages;

    private ContactsAdapter contactsAdapter;
    private FirebaseFirestore mFirestore;
    private DatabaseReference mCurrentUserRoomReference;
    private NavController mNavController;


    public UserChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mAuth = FirebaseAuth.getInstance();

        mFirestore = FirebaseFirestore.getInstance();
        String currentUserId = MessagesPreference.getUserId(requireContext());
        mCurrentUserRoomReference = FirebaseDatabase.getInstance().getReference("rooms")
                .child(currentUserId);

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
        if (mAuth.getCurrentUser() != null && messages.size() == 0)
            initializeUserAndData();
        return view;
    }

    private void initializeUserAndData() {

        mCurrentUserRoomReference.addValueEventListener(this);
       /* mCurrentUserRoomReference.get().addOnSuccessListener(successDataSnapshot -> {




        });*/
       /* mFirestore.collection("rooms").document(Objects.requireNonNull(mAuth.getUid()))
                .collection("chats").addSnapshotListener((value, error) -> {
            if (error != null){
                Toast.makeText(getContext(), "Error reading message", Toast.LENGTH_SHORT).show();
            }else{
                String source = value != null && value.getMetadata().hasPendingWrites()
                        ? "Local" : "Server";
                Log.d(TAG, source);
                Toast.makeText(requireActivity(), source, Toast.LENGTH_SHORT).show();
                updateUI(value);
            }
        });*/

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

    private void updateUI(QuerySnapshot value) {
       /* if (value != null) {


            for (DocumentChange dc : value.getDocumentChanges()) {
                Toast.makeText(getContext(), "document has changed ", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "document change in User Contacts Activity");
                FullMessage fullMessage = dc.getDocument().toObject(FullMessage.class);
                String upComingId = fullMessage.getTargetUserId();
                for (int i = 0 ; i < fullMessages.size(); i ++){
                    String toBeReplaceId = fullMessages.get(i).getTargetUserId();
                    if (toBeReplaceId.equals(upComingId)) fullMessages.remove(i);
                }

                fullMessages.add(fullMessage);

            }
            contactsAdapter.notifyDataSetChanged();
        }*/
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
            for (DataSnapshot messageSnapShot : messagesIterable){
                if (!messageSnapShot.getKey().equals("isWriting")) {
                    lastMessage = messageSnapShot.getValue(Message.class);
                }
            }
            if (lastMessage != null)
                messages.add(lastMessage);

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
}