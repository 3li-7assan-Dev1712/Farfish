package com.example.friendlychat.Activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.friendlychat.Adapters.ContactsAdapter;
import com.example.friendlychat.MessagesPreference;
import com.example.friendlychat.R;
import com.example.friendlychat.User;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ContactsActivity extends AppCompatActivity {
    private static final String TAG = ContactsActivity.class.getSimpleName();
    private FirebaseAuth mAuth;
    private List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.EmailBuilder().build(),
            new AuthUI.IdpConfig.GoogleBuilder().build());
    private ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            this::onSignInResult
    );
    private List<User> users;
    private ContactsAdapter contactsAdapter;
    private  RecyclerView contactsRecycler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        contactsRecycler = findViewById(R.id.contactsRecyclerView);
        users = new ArrayList<>();
        contactsAdapter = new ContactsAdapter(this, users);
        contactsRecycler.setAdapter(contactsAdapter);

        /*firebase database & auth*/
        mAuth = FirebaseAuth.getInstance();
        /*firstly check if the user is signed in or not and interact accordingly*/
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            // there's a user go and sign in
            String userName = currentUser.getDisplayName();
            MessagesPreference.saveUserName(this, userName);
            initializeUserAndData(userName);
        }else{
            // three's no user, ge and sign up
            launchFirebaseUI();
        }
    }

    private void launchFirebaseUI() {
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(false)
                .setLogo(R.drawable.ui_logo)
                .build();
        signInLauncher.launch(signInIntent);
    }

    private void initializeUserAndData(String userName) {

        /* here i should read user from the firestore but that will be done tomorrow if Allah wills *_*  */
        /* for checking*/
        User user = new User("Abdualah AbdAlgalil", "", "", "no");
        users.add(user);
        contactsAdapter.notifyItemInserted(users.size()-1);

    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        /*IdpResponse response = result.getIdpResponse();*/
        Log.d(TAG, "result: " + result.getResultCode());
        Log.d(TAG, "onSignInResult");
        if (result.getResultCode() == RESULT_OK) {
            // Successfully signed in
            Toast.makeText(this, "You've signed in successfully", Toast.LENGTH_SHORT).show();
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                MessagesPreference.saveUserName(this, currentUser.getDisplayName());
                initializeUserAndData(currentUser.getDisplayName());
            }
            // ...
        } else{
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            // ...
            Log.d(TAG, "onSignInResult"+ " should finish the Activity");
            finish();
        }
    }
}