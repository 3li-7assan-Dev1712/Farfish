package com.example.friendlychat.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.friendlychat.Activities.ChatsActivity;
import com.example.friendlychat.Adapters.ContactsAdapter;
import com.example.friendlychat.Module.FullMessage;
import com.example.friendlychat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UserChatsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserChatsFragment extends Fragment implements ContactsAdapter.OnChatClicked {
    private static final String TAG = UserChatsFragment.class.getSimpleName();
    private List<FullMessage> fullMessages;
    private ContactsAdapter contactsAdapter;
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public UserChatsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UserChatsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UserChatsFragment newInstance(String param1, String param2) {
        UserChatsFragment fragment = new UserChatsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        mFirestore = FirebaseFirestore.getInstance();
        fullMessages = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        contactsAdapter = new ContactsAdapter(getContext(), fullMessages, this, null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d(TAG, "onCreateView: ");
        View view =inflater.inflate(R.layout.fragment_user_chats, container, false);


        RecyclerView contactsRecycler = view.findViewById(R.id.userContactsRecyclerView);


        contactsRecycler.setAdapter(contactsAdapter);
        initializeUserAndData();
        return view;
    }

    private void initializeUserAndData() {

        mFirestore.collection("rooms").document(mAuth.getUid())
                .collection("chats").addSnapshotListener((value, error) -> {
            if (error != null){
                Toast.makeText(getContext(), "Error reading message", Toast.LENGTH_SHORT).show();
            }else{
                String source = value != null && value.getMetadata().hasPendingWrites()
                        ? "Local" : "Server";
                Log.d(TAG, source);
                Toast.makeText(getContext(), source, Toast.LENGTH_SHORT).show();
                updateUI(value);
            }
        });

    }

    private void updateUI(QuerySnapshot value) {
        if (value != null) {

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
        }
    }

    @Override
    public void onChatClicked(int position) {

        String chatTitle = fullMessages.get(position).getTargetUserName();
        String photoUrl= fullMessages.get(position).getTargetUserPhotoUrl();
        Intent chatsIntent = new Intent(getContext(), ChatsActivity.class);
        chatsIntent.putExtra(getResources().getString(R.string.photoUrl), photoUrl);
        chatsIntent.putExtra(getResources().getString(R.string.chat_title), chatTitle);
        String targetUserId = fullMessages.get(position).getTargetUserId();
        Log.d(TAG, "onChatClicked: targetUid: " + targetUserId);
        chatsIntent.putExtra(getResources().getString(R.string.targetUidKey), targetUserId);
        startActivity(chatsIntent);
    }
}