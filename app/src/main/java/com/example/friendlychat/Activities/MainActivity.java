package com.example.friendlychat.Activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.friendlychat.Adapters.MessagesAdapter;
import com.example.friendlychat.Message;
import com.example.friendlychat.R;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    // Register the permissions callback, which handles the user's response to the
// system permissions dialog. Save the return value, an instance of
// ActivityResultLauncher, as an instance variable.
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                    pickImageFromGallery();
                } else {
                    Toast.makeText(this, "Ok, if you need to send images please grant the requested permission", Toast.LENGTH_SHORT).show();
                }
            });
    private static final String TAG = "MainActivity";

    public static final String ANONYMOUS = "anonymous";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    private RecyclerView mMessageRecyclerView;
    private EditText mMessageEditText;
    private Button mSendButton;
    private CollectionReference messagesRef;
    private List<Message> messages;
    private MessagesAdapter messagesAdapter;
    private String mUsername;


    private ActivityResultLauncher<String> pickPic = registerForActivityResult(
            new ActivityResultContracts.GetContent(){
                @NonNull
                @Override
                public Intent createIntent(@NonNull Context context, @NonNull String input) {
                    return super.createIntent(context, "image/*");
                }
            },
            this::putIntoImage);


    private void putIntoImage(Uri uri) {

        StorageReference imageRef = mRootRef.child(Objects.requireNonNull(uri.getLastPathSegment()));

        File galeryFile = new File(String.valueOf(uri));
        UploadTask uploadTask = imageRef.putFile(uri);

// Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(exception -> {
            // Handle unsuccessful uploads
            Toast.makeText(MainActivity.this, "failed to set the image please try again later", Toast.LENGTH_SHORT).show();
        }).addOnSuccessListener(taskSnapshot -> {
            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
            // ...

        });

    }

    private StorageReference mRootRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate");

        /*firebase storage and its references*/
        FirebaseStorage mStorage = FirebaseStorage.getInstance();
        // Create a storage reference from our app
        mRootRef = mStorage.getReference("images");

        /*Firestore functionality*/
        FirebaseFirestore mFirebasestore = FirebaseFirestore.getInstance();

        messagesRef = mFirebasestore.collection("rooms").document("people use the app")
                .collection("messages");
        /*app UI functionality*/
        mUsername = ANONYMOUS;
        mMessageRecyclerView = findViewById(R.id.messageRecyclerView);
        ProgressBar mProgressBar = findViewById(R.id.progressBar);
        ImageButton mPhotoPickerButton = findViewById(R.id.photoPickerButton);
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
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED) {
                // You can use the API that requires the permission.
                pickImageFromGallery();
            } else {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher.launch(
                        Manifest.permission.READ_EXTERNAL_STORAGE);
            }

        });

        // Enable Send button when there's text to send
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Toast.makeText(MainActivity.this, "before text change", Toast.LENGTH_SHORT).show();
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

        mSendButton.setOnClickListener( view -> {

            String messageToBeSentFromUser = mMessageEditText.getText().toString();
            messagesRef
                    .add(new Message(messageToBeSentFromUser, mUsername, "", System.currentTimeMillis()))
                    .addOnSuccessListener(
                            documentReference -> {
                                Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                            }

                    )
                    .addOnFailureListener(e -> Log.w(TAG, "Error adding document", e));
            // Clear input box
            mMessageEditText.setText("");
        });



    }

    private void pickImageFromGallery() {
        pickPic.launch("image/*");
    }


    private void initializeUserAndData(String userName) {


        /*read all messages form the database and add any new messages with notifying the Adapter after that*/
        Toast.makeText(this, "Welcome " + userName + "!", Toast.LENGTH_SHORT).show();
        mUsername = userName;

        messagesRef.orderBy("timestamp").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onComplete");
                        messages.clear();
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            Message message = document.toObject(Message.class);
                            messages.add(message);
                        }
                        messagesAdapter.notifyDataSetChanged();
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });

        messagesRef.orderBy("timestamp").addSnapshotListener((value, error) -> {
            if (error != null){
                Toast.makeText(MainActivity.this, "fail", Toast.LENGTH_SHORT).show();
            }else {
                if (messages.size() == 0) {
                    if (value != null) {
                        for (DocumentSnapshot document : value.getDocuments()) {
                            messages.add(document.toObject(Message.class));
                        }
                        messagesAdapter.notifyDataSetChanged();
                        mMessageRecyclerView.smoothScrollToPosition(messages.size()-1);
                    }else{
                        Log.d(TAG, "no value to add");
                    }
                } else {
                    Log.d(TAG, "onEvent");

                    String source = value != null && value.getMetadata().hasPendingWrites()
                            ? "Local" : "Server";
                    Log.d(TAG, source);
                    Toast.makeText(MainActivity.this, source, Toast.LENGTH_SHORT).show();
                    Message m = value.getDocuments().get(value.getDocuments().size() - 1).toObject(Message.class);
                    Toast.makeText(MainActivity.this, "Message: " + m.getText(), Toast.LENGTH_SHORT).show();
                    messages.add(m);
                    messagesAdapter.notifyItemInserted(messages.size() - 1);
                    mMessageRecyclerView.smoothScrollToPosition(messages.size() - 1);

                }
            }
        });

    }

}