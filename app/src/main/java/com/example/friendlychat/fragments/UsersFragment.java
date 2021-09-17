package com.example.friendlychat.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.friendlychat.Adapters.ContactsAdapter;
import com.example.friendlychat.Module.MessagesPreference;
import com.example.friendlychat.Module.SharedPreferenceUtils;
import com.example.friendlychat.Module.User;
import com.example.friendlychat.R;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class UsersFragment extends Fragment implements  ContactsAdapter.OnChatClicked {
    private FirebaseAuth mAuth;
    private List<User> users;
    private ContactsAdapter usersAdapter;
    private FirebaseFirestore mFirestore;
    private NavController mNavController;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
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
        requireActivity().findViewById(R.id.bottom_nav).setVisibility(View.VISIBLE);
        View view =  inflater.inflate(R.layout.users_fragment, container, false);
        mNavController = Navigation.findNavController(view);
        Toolbar tb = view.findViewById(R.id.mainToolbar_frag);
        ((AppCompatActivity) requireActivity())
                .setSupportActionBar(tb);
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
        Bundle primaryDataBundle = new Bundle();
        primaryDataBundle.putString("chat_title", chatTitle);
        primaryDataBundle.putString("photo_url", photoUrl);
        if (!chatTitle.equals("All people use the app")) {
            String targetUserId = users.get(position).getUserId();
            primaryDataBundle.putString("target_user_id", targetUserId);
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
