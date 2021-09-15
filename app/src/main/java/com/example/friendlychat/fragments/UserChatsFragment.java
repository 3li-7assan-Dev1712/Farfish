package com.example.friendlychat.fragments;

import android.app.Activity;
import android.content.Context;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.friendlychat.Adapters.ContactsAdapter;
import com.example.friendlychat.Module.FullMessage;
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
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;


public class UserChatsFragment extends Fragment implements ContactsAdapter.OnChatClicked {

    private FirebaseAuth mAuth;
    private List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.EmailBuilder().build(),
            new AuthUI.IdpConfig.PhoneBuilder().build(),
            new AuthUI.IdpConfig.GoogleBuilder().build());
   /* private ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            this::onSignInResult
    );*/
    private static final String TAG = UserChatsFragment.class.getSimpleName();
    private List<FullMessage> fullMessages;
    private ContactsAdapter contactsAdapter;
    private FirebaseFirestore mFirestore;



    public UserChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mAuth = FirebaseAuth.getInstance();

        mFirestore = FirebaseFirestore.getInstance();
        fullMessages = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        contactsAdapter = new ContactsAdapter(getContext(), fullMessages, this, null);
    }

    private void navigateToSignIn() {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.navigate(R.id.fragmentSignIn);
    }

    /*private void launchFirebaseUI() {
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(false)
                .setLogo(R.drawable.ic_icon_round)
                .build();
        signInLauncher.launch(signInIntent);
    }*/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        requireActivity().findViewById(R.id.bottom_nav).setVisibility(View.VISIBLE);
        Log.d(TAG, "onCreateView: ");
        View view =inflater.inflate(R.layout.fragment_user_chats, container, false);
        if (mAuth.getCurrentUser() == null){
            navigateToSignIn();
        }
        Toolbar tb = view.findViewById(R.id.mainToolbar_frag);
        ((AppCompatActivity) requireActivity())
                .setSupportActionBar(tb);
        /*requireActivity().findViewById(R.id.nav_graph).setVisibility(View.VISIBLE);*/
        RecyclerView contactsRecycler = view.findViewById(R.id.userContactsRecyclerView);


        contactsRecycler.setAdapter(contactsAdapter);
        if (mAuth.getCurrentUser() != null)
            initializeUserAndData();
        return view;
    }

    private void initializeUserAndData() {

        mFirestore.collection("rooms").document(Objects.requireNonNull(mAuth.getUid()))
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

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.sign_out:
                mAuth.signOut();
                navigateToSignIn();
                break;
            case R.id.sign_in_custom:
                navigateToSignIn();
                break;
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
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
        String targetUserId = fullMessages.get(position).getTargetUserId();
        Bundle primaryDataBundle = new Bundle();
        primaryDataBundle.putString("chat_title", chatTitle);
        primaryDataBundle.putString("photo_url", photoUrl);
        primaryDataBundle.putString("target_user_id", targetUserId);
        primaryDataBundle.putBoolean("isGroup", false);
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.navigate(R.id.chatsFragment, primaryDataBundle);

    }
/*
    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        *//*IdpResponse response = result.getIdpResponse();*//*
        Log.d(TAG, "result: " + result.getResultCode());
        Log.d(TAG, "onSignInResult");
        if (result.getResultCode() == Activity.RESULT_OK) {
            // Successfully signed in
            Toast.makeText(requireContext(), "You've signed in successfully", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "sign in successfully");
            SharedPreferenceUtils.saveUserSignIn(requireContext());
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                *//*after the user sign in/up saving their information in the firestore*//*
                String userName = currentUser.getDisplayName();
                MessagesPreference.saveUserName(requireContext(), userName);
                String phoneNumber = currentUser.getPhoneNumber();
                String photoUrl = Objects.requireNonNull(currentUser.getPhotoUrl()).toString();
                String userId = mAuth.getUid();
                MessagesPreference.saveUserId(requireContext(), userId);
                MessagesPreference.saveUserPhotoUrl(requireContext(), photoUrl);
                long lastTimeSeen = new Date().getTime();
                User newUser = new User(userName, phoneNumber, photoUrl, userId, true, false, lastTimeSeen);
                assert userId != null;
                mFirestore.collection("rooms").document(userId).set(newUser).addOnCompleteListener(task ->
                        Toast.makeText(requireContext(), "saved new user successfully", Toast.LENGTH_SHORT).show()
                );
                initializeUserAndData();
            }
            // ...
        } else{
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            // ...
            Log.d(TAG, "onSignInResult"+ " should finish the Activity");
            Log.d(TAG, String.valueOf(result.getResultCode()));
            requireActivity().finish();
        }
    }*/
}