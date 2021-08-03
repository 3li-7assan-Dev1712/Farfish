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
import com.example.friendlychat.MessagesPreference;
import com.example.friendlychat.R;
import com.example.friendlychat.User;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import com.example.friendlychat.User;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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
    private FirebaseFirestore mFirestore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        mFirestore = FirebaseFirestore.getInstance();
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
        mFirestore.collection("rooms").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                users.clear();
                Log.d(TAG, "onSuccess");
                /*task.getResult().getDocuments().get(0).getD*/
                for (QueryDocumentSnapshot document: Objects.requireNonNull(task.getResult())){
                    User user = document.toObject(User.class);
                    Log.d(TAG, "userName: " + user.getUserName());
                    users.add(user);
                }
                contactsAdapter.notifyDataSetChanged();
            }
        }).addOnFailureListener(e -> {

            Toast.makeText(this, "error "+ e.toString(), Toast.LENGTH_SHORT).show();
        });
        /*User user = new User("Abdualah AbdAlgalil", "", "no");
        users.add(user);
        contactsAdapter.notifyItemInserted(users.size()-1);*/

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
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                /*after the user sign in/up saving their information in the firestore*/
                String userName = currentUser.getDisplayName();
                MessagesPreference.saveUserName(this, userName);
                String phoneNumber = currentUser.getPhoneNumber();
                String photoUrl = Objects.requireNonNull(currentUser.getPhotoUrl()).toString();
                User newUser = new User(userName, phoneNumber, photoUrl);
                mFirestore.collection("rooms").document(mAuth.getUid()).set(newUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(ContactsActivity.this, "saved new user successfully", Toast.LENGTH_SHORT).show();
                    }
                });
                initializeUserAndData(userName);
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
}