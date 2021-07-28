package com.example.friendlychat;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    public static final String ANONYMOUS = "anonymous";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;

    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private RecyclerView mMessageRecyclerView;
    private ProgressBar mProgressBar;
    private ImageButton mPhotoPickerButton;
    private EditText mMessageEditText;
    private Button mSendButton;

    private List<Message> messages;
    private MessagesAdapter messagesAdapter;
    private DatabaseReference mDatabaseReference;
    private FirebaseDatabase mFirebaseDatabase;
    private String mUsername;
    private FirebaseAuth mAuth;
    private List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.EmailBuilder().build(),
            new AuthUI.IdpConfig.GoogleBuilder().build());
    private ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            this::onSignInResult
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate");
        /*firebase database & auth*/
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference= mFirebaseDatabase.getReference().child("messages");

        /*app UI functionality*/
        mUsername = ANONYMOUS;
        mMessageRecyclerView = findViewById(R.id.messageRecyclerView);
        mProgressBar = findViewById(R.id.progressBar);
        mPhotoPickerButton = findViewById(R.id.photoPickerButton);
        mMessageEditText = findViewById(R.id.messageEditText);
        mSendButton = findViewById(R.id.sendButton);
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);
        messages = new ArrayList<>();
        /*implementing Messages Adapter for the RecyclerView*/
        messagesAdapter = new MessagesAdapter(this, messages);
        mMessageRecyclerView.setAdapter(messagesAdapter);
        mMessageRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // ImagePickerButton shows an image picker to upload a image for a message
        mPhotoPickerButton.setOnClickListener(v -> {
           // will be implemented in the next commits  if Allah wills :)
        });

        // Enable Send button when there's text to send
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

        // Send button sends a message and clears the EditText
        mSendButton.setOnClickListener( view -> {

            String messageToBeSentFromUser = mMessageEditText.getText().toString();
            mDatabaseReference.push().setValue(new Message(messageToBeSentFromUser, mUsername, ""));

            // Clear input box
            mMessageEditText.setText("");
        });


        /*firstly check if the user is signed in or not and interact accourdingly*/
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            // there's a user go and sign in
            initializeUserAndData(currentUser.getDisplayName());
        }else{
            // three's no user, ge and sign up
            launchFirebaseUI();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.sign_out){
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener(task -> {
                        // ...
                        Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show();
                        launchFirebaseUI();
                    });
        }
        return true;
    }

    private void launchFirebaseUI(){
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(false)
                .build();
        signInLauncher.launch(signInIntent);

    }

    private void initializeUserAndData(String userName) {

        /*read all messages form the database and add any new messages with notifying the Adapter after that*/
        Toast.makeText(this, "Welcome " + userName + "!", Toast.LENGTH_SHORT).show();
        mUsername = userName;
        /* after make sure the user singed in successfully we get the data from the database*/
        mDatabaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                Message newMessage = snapshot.getValue(Message.class);
                if (newMessage != null) {
                    messages.add(newMessage);
                    messagesAdapter.notifyDataSetChanged();
                    mMessageRecyclerView.smoothScrollToPosition(messages.size()-1);
                }
                else {
                    Log.w(TAG, "the newMessage is null!");
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        /*IdpResponse response = result.getIdpResponse();*/
        Log.d(TAG, "result: " + result.getResultCode());
        Log.d(TAG, "onSignInResult");
        if (result.getResultCode() == RESULT_OK) {
            // Successfully signed in
            Toast.makeText(this, "You've signed in successfully", Toast.LENGTH_SHORT).show();
            FirebaseUser currentUser = mAuth.getCurrentUser();
            initializeUserAndData(currentUser.getDisplayName());
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