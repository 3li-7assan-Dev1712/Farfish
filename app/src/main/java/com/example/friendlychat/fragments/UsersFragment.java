package com.example.friendlychat.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.friendlychat.Adapters.ContactsAdapter;
import com.example.friendlychat.Module.User;
import com.example.friendlychat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class UsersFragment extends Fragment implements  ContactsAdapter.OnChatClicked {
    private FirebaseAuth mAuth;
    private List<User> users;
    private ContactsAdapter usersAdapter;
    private FirebaseFirestore mFirestore;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        mFirestore = FirebaseFirestore.getInstance();
        users = new ArrayList<>();
        usersAdapter = new ContactsAdapter(requireContext(), users, this);
        /*firebase database & auth*/
        mAuth = FirebaseAuth.getInstance();
        initializeUserAndData();
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
         View view =  inflater.inflate(R.layout.users_fragment, container, false);
        RecyclerView usersRecycler = view.findViewById(R.id.usersRecyclerView);
        usersRecycler.setAdapter(usersAdapter);
         return view;
    }



    private void initializeUserAndData() {


        /*makeUserActive();*/
        mFirestore.collection("rooms").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentChange dc: queryDocumentSnapshots.getDocumentChanges()){
                        User user = dc.getDocument().toObject(User.class);
                        String currentUserId = mAuth.getUid();
                        assert currentUserId != null;
                        if (!currentUserId.equals(user.getUserId()))
                            users.add(user);
                    }
                    usersAdapter.notifyDataSetChanged();
                });

    }



    @Override
    public void onChatClicked(int position) {

        String chatTitle = users.get(position).getUserName();
        String photoUrl = users.get(position).getPhotoUrl();
        if (!chatTitle.equals("All people use the app")) {
            String targetUserId = users.get(position).getUserId();

        }
        // will be completed in the following commits.
    }
}
