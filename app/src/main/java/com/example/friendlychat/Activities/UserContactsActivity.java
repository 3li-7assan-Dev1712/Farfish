package com.example.friendlychat.Activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.friendlychat.Adapters.ContactsAdapter;
import com.example.friendlychat.Module.FullMessage;
import com.example.friendlychat.Module.MessagesPreference;
import com.example.friendlychat.Module.SharedPreferenceUtils;
import com.example.friendlychat.Module.User;
import com.example.friendlychat.R;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.invoke.ConstantCallSite;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class UserContactsActivity extends AppCompatActivity implements ContactsAdapter.OnChatClicked {
    private static final String TAG = ContactsActivity.class.getSimpleName();
    private FirebaseAuth mAuth;
    private List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.EmailBuilder().build(),
            new AuthUI.IdpConfig.GoogleBuilder().build());
    private ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            this::onSignInResult
    );
    private List<FullMessage> fullMessages;
    private ContactsAdapter contactsAdapter;
    private FirebaseFirestore mFirestore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_contacts);
        mFirestore = FirebaseFirestore.getInstance();
        RecyclerView contactsRecycler = findViewById(R.id.userContactsRecyclerView);
        fullMessages = new ArrayList<>();
        contactsAdapter = new ContactsAdapter(this, fullMessages, this, null);
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
        mFirestore.collection("rooms").document(mAuth.getUid())
                .collection("chats").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int num = task.getResult().getDocumentChanges().size();
                        Toast.makeText(this, "change ", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "change in the console");
                        String name = task.getResult().getDocumentChanges().get(0).getDocument().toObject(FullMessage.class).getTargetUserName();
                        Toast.makeText(this, "change from " + name, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "change in the console from " + name);
                        Log.d(TAG, "going to loop");
                        for (DocumentSnapshot ds: task.getResult().getDocuments()){
                            fullMessages.add(ds.toObject(FullMessage.class));
                            Log.d(TAG, "add full message in the loop");
                        }
                        if (fullMessages.size() != 0) {
                            contactsAdapter.setFullMessages(fullMessages);
                            contactsAdapter.notifyDataSetChanged();

                            Log.d(TAG, "set the data to the adapter");
                            Toast.makeText(this, "data is " + fullMessages.size(), Toast.LENGTH_SHORT).show();
                            Log.d(TAG, " there is " + fullMessages.size() + " contacts should appear");
                        }
                        else {
                            Toast.makeText(this, "no data to send", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "no data to show");
                        }
                    }
                });
                /*.addOnSuccessListener(queryDocumentSnapshots -> {
                    int num = queryDocumentSnapshots.getDocumentChanges().size();
                    Toast.makeText(this, "changes in " + num+ " chats", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "there are " + num + " changes");
                    for (DocumentChange dc: queryDocumentSnapshots.getDocumentChanges()){
                        String changeType = dc.getType().name();
                        *//*if the type is added than means there's a new user*//*
                        if (changeType.equals(DocumentChange.Type.ADDED)) {
                           fullMessages.add(dc.getDocument().toObject(FullMessage.class));
                            contactsAdapter.setFullMessages(fullMessages);
                        }

                        *//*if the change type is modified that means that that the user is writing or sent a new message
                        * and will be handled after a while*//*
                        else if (changeType.equals(DocumentChange.Type.MODIFIED)){
                            Log.d(TAG, "modification");
                            Toast.makeText(this, "Modification happened will be done in the future", Toast.LENGTH_SHORT).show();
                        }
                    }*/

                /*});*/


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
       switch (id) {
           case R.id.sign_out:
               mAuth.signOut();
               Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show();
               SharedPreferenceUtils.saveUserSignOut(this);
               fullMessages.clear();
               launchFirebaseUI();
               break;
           case R.id.see_all_users:
               Intent seeAllUsersIntent = new Intent(this, ContactsActivity.class);
               startActivity(seeAllUsersIntent);
               break;
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
                SharedPreferenceUtils.saveUserId(this, userId);
                MessagesPreference.saveUserPhotoUrl(this, photoUrl);
                long lastTimeSeen = new Date().getTime();
                User newUser = new User(userName, phoneNumber, photoUrl, userId, true, false,lastTimeSeen);
                assert userId != null;
                mFirestore.collection("rooms").document(userId).set(newUser).addOnCompleteListener(task ->
                        Toast.makeText(UserContactsActivity.this, "saved new user successfully", Toast.LENGTH_SHORT).show()
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
        Toast.makeText(this, fullMessages.get(position).getTargetUserName(), Toast.LENGTH_SHORT).show();
        String chatTitle = fullMessages.get(position).getTargetUserName();
        String photoUrl= fullMessages.get(position).getTargetUserPhotoUrl();
        Intent chatsIntent = new Intent(this, ChatsActivity.class);
        chatsIntent.putExtra(getResources().getString(R.string.photoUrl), photoUrl);
        chatsIntent.putExtra(getResources().getString(R.string.chat_title), chatTitle);
        String targetUserId = fullMessages.get(position).getTargetUserId();
        chatsIntent.putExtra(getResources().getString(R.string.targetUidKey), targetUserId);
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


}