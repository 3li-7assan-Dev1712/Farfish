package com.example.friendlychat.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.friendlychat.Adapters.ContactsAdapter;
import com.example.friendlychat.Module.MessagesPreference;
import com.example.friendlychat.Module.SharedPreferenceUtils;
import com.example.friendlychat.R;
import com.example.friendlychat.Module.User;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class ContactsActivity extends AppCompatActivity implements ContactsAdapter.OnChatClicked{
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
    private FirebaseFirestore mFirestore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        mFirestore = FirebaseFirestore.getInstance();
        RecyclerView contactsRecycler = findViewById(R.id.contactsRecyclerView);
        users = new ArrayList<>();
        contactsAdapter = new ContactsAdapter(this, users, this);
        contactsRecycler.setAdapter(contactsAdapter);

        /*firebase database & auth*/
        mAuth = FirebaseAuth.getInstance();
        /*firstly check if the user is signed in or not and interact accordingly*/
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            // there's a user go and sign in
            String userName = currentUser.getDisplayName();
            MessagesPreference.saveUserName(this, userName);
            initializeUserAndData();
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

    private void initializeUserAndData() {


        /*makeUserActive();*/
        mFirestore.collection("rooms").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                   for (DocumentChange dc: queryDocumentSnapshots.getDocumentChanges()){
                       User user = dc.getDocument().toObject(User.class);
                       String currentUserId = mAuth.getUid();
                       assert currentUserId != null;
                       /*if (!currentUserId.equals(user.getUserId()))*/
                       users.add(user);
                   }
                   contactsAdapter.notifyDataSetChanged();
                });

    }

    private void makeUserActive() {
        String userId = mAuth.getUid();
        assert userId != null;
        mFirestore.collection("rooms").document(userId)
                .update("isActive", true).addOnCompleteListener(task -> {
                    Log.d(TAG, "made user active");
                    Toast.makeText(ContactsActivity.this, "User activated successfully", Toast.LENGTH_SHORT).show();
                });

    }

    private void makeUserInActive() {
        String userId = mAuth.getUid();
        long lastTimeSeen = new Date().getTime();
        assert userId != null;
        mFirestore.collection("rooms").document(userId)
                .update(
                        "isActive", false,
                        "lastTimeSeen", lastTimeSeen
                );
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       getMenuInflater().inflate(R.menu.main, menu);
       return true;

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.sign_out){
            mAuth.signOut();
            Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show();
            SharedPreferenceUtils.saveUserSignOut(this);
            launchFirebaseUI();
        }
        return true;
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        /*IdpResponse response = result.getIdpResponse();*/
        Log.d(TAG, "result: " + result.getResultCode());
        Log.d(TAG, "onSignInResult");
        if (result.getResultCode() == RESULT_OK) {
            // Successfully signed in
            Toast.makeText(this, "You've signed in successfully", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "sign in successfully");
            SharedPreferenceUtils.saveUserSignIn(this);
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                /*after the user sign in/up saving their information in the firestore*/
                String userName = currentUser.getDisplayName();
                MessagesPreference.saveUserName(this, userName);
                String phoneNumber = currentUser.getPhoneNumber();
                String photoUrl = Objects.requireNonNull(currentUser.getPhotoUrl()).toString();
                String userId = mAuth.getUid();

                long lastTimeSeen = new Date().getTime();
                User newUser = new User(userName, phoneNumber, photoUrl, userId, true, false,lastTimeSeen);
                assert userId != null;
                mFirestore.collection("rooms").document(userId).set(newUser).addOnCompleteListener(task ->
                        Toast.makeText(ContactsActivity.this, "saved new user successfully", Toast.LENGTH_SHORT).show()
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

            finish();
        }
    }

    @Override
    public void onChatClicked(int position) {
        Toast.makeText(this, users.get(position).getUserName(), Toast.LENGTH_SHORT).show();
        String chatTitle = users.get(position).getUserName();
        Intent chatsIntent = new Intent(this, ChatsActivity.class);
        chatsIntent.putExtra(getResources().getString(R.string.chat_title), chatTitle);
        if (!chatTitle.equals("All people use the app")) {
            String targetUserId = users.get(position).getUserId();
            chatsIntent.putExtra(getResources().getString(R.string.targetUidKey), targetUserId);
        }
        startActivity(chatsIntent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");

    }

    @Override
    protected void onDestroy() {
      /*  makeUserInActive();*/
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        /*when the user navigate into another app or just close their phone
         * then they are no longer active *_-  */

    }
}