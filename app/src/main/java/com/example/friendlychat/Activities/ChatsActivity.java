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
import com.example.friendlychat.FileUtil;
import com.example.friendlychat.Message;
import com.example.friendlychat.MessagesPreference;
import com.example.friendlychat.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import id.zelory.compressor.Compressor;

public class ChatsActivity extends AppCompatActivity {
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
    private static final String TAG = ChatsActivity.class.getSimpleName();

    public static final String ANONYMOUS = "anonymous";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    private RecyclerView mMessageRecyclerView;
    private EditText mMessageEditText;
    private Button mSendButton;
    private CollectionReference messagesRef;
    private List<Message> messages;
    private MessagesAdapter messagesAdapter;
    private String mUsername;
    private boolean isGroup;
    private Intent mIntent;
    private FirebaseFirestore mFirebasestore;
    private CollectionReference messageSingleRef;
    private CollectionReference messageSingleRefTarget;
    private StorageReference mRootRef;

    private ActivityResultLauncher<String> pickPic = registerForActivityResult(
            new ActivityResultContracts.GetContent(){
                @NonNull
                @Override
                public Intent createIntent(@NonNull Context context, @NonNull String input) {
                    return super.createIntent(context, "image/*");
                }
            },
            this::putIntoImage);


    private void putIntoImage(Uri uri)  {

        StorageReference imageRef = mRootRef.child(Objects.requireNonNull(uri.getLastPathSegment()));


        try {
            File galeryFile = FileUtil.from(this, uri);
            File compressedImageFile = new Compressor(this).compressToFile(galeryFile);
            UploadTask uploadTask = imageRef.putFile(Uri.fromFile(compressedImageFile));


            String originalFileName = FileUtil.getFileName(this, uri);
            String compressedFileName = compressedImageFile.getName();
            Log.d(TAG, "Original file uri is: " +uri);
            Log.d(TAG, "Compressed file uri is: " + Uri.fromFile(compressedImageFile));

            // Register observers to listen for when the download is done or if it fails
            uploadTask.addOnFailureListener(exception -> {
                // Handle unsuccessful uploads
                Toast.makeText(ChatsActivity.this, "failed to set the image please try again later", Toast.LENGTH_SHORT).show();
            }).addOnSuccessListener(taskSnapshot -> {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...

                Toast.makeText(this, "Added image to Storage successfully", Toast.LENGTH_SHORT).show();

                Date date = new Date();
                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.d(TAG, uri.toString());
                        Log.d(TAG, String.valueOf(uri));
                        String downloadUrl = uri.toString();
                        Message message = new Message("", mUsername, downloadUrl, date.getTime());
                        sendMessage(message);
                    }
                });

            });

            Log.d(TAG, "compressed file into: " + compressedImageFile.getAbsolutePath() );
            Log.d(TAG, "Original file size: " + galeryFile.length() / 1024);
            Log.d(TAG, "Compressed file size: " + compressedImageFile.length() / 1024);
            Toast.makeText(this, "Compressed file into: " + compressedImageFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "Error copressign the file");
            Toast.makeText(this, "Error happended", Toast.LENGTH_SHORT).show();
        }


    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);
        Log.d(TAG, "onCreate");

        /*firebase storage and its references*/
        FirebaseStorage mStorage = FirebaseStorage.getInstance();
        // Create a storage reference from our app
        mRootRef = mStorage.getReference("images");

        /*Firestore functionality*/
        mFirebasestore = FirebaseFirestore.getInstance();



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
        /*message indiviual */
        mIntent= getIntent();
        if (mIntent != null){
            setTitle(mIntent.getStringExtra("chatTitle"));
            isGroup = !mIntent.hasExtra("targetId");
        }

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

        Log.d(TAG, "isGroup" + isGroup);

        mSendButton.setOnClickListener( v -> {
            Date date = new Date();
            Message message = new Message(mMessageEditText.getText().toString(), mUsername, "", date.getTime());
            sendMessage(message);
        });


        if (!isGroup) {
            String targetId = mIntent.getStringExtra("targetId");
            FirebaseAuth auth = FirebaseAuth.getInstance();
            messageSingleRef = mFirebasestore.collection("rooms").document(auth.getUid())
                    .collection("chats")
                    .document(auth.getUid()+targetId)
                    .collection("messages");
            messageSingleRefTarget = mFirebasestore.collection("rooms")
                    .document(targetId)
                    .collection("chats").document(targetId + auth.getUid())
                    .collection("messages");
            initializeUserAndData();
        }
    }

    private void sendMessage(Message message) {

        if (isGroup){
            messagesRef = mFirebasestore.collection("rooms").document("people use the app")
                    .collection("messages");
            messagesRef
                    .add(message)
                    .addOnSuccessListener(
                            documentReference -> {
                                Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                            }

                    )
                    .addOnFailureListener(e -> Log.w(TAG, "Error adding document", e));
            // Clear input box
        }else {



            messageSingleRef
                    .add(message)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Toast.makeText(ChatsActivity.this, "Added new message", Toast.LENGTH_SHORT).show();
                            messageSingleRefTarget.add(message);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ChatsActivity.this, "Error" + e.toString(), Toast.LENGTH_SHORT).show();
                }

            });
        }
        mMessageEditText.setText("");

    }

    private void pickImageFromGallery() {
        pickPic.launch("image/*");
    }


    private void initializeUserAndData() {

        messagesRef = mFirebasestore.collection("rooms").document("people use the app")
                .collection("messages");

        /*read all messages form the database and add any new messages with notifying the Adapter after that*/
        String userName = MessagesPreference.getUserName(this);
        mUsername = userName;
        Toast.makeText(this, "Welcome " + userName + "!", Toast.LENGTH_SHORT).show();


        if (isGroup) {
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
                if (error != null) {
                    Toast.makeText(ChatsActivity.this, "fail", Toast.LENGTH_SHORT).show();
                } else {
                    if (messages.size() == 0) {
                        if (value != null) {
                            for (DocumentSnapshot document : value.getDocuments()) {
                                messages.add(document.toObject(Message.class));
                            }
                            messagesAdapter.notifyDataSetChanged();
                            mMessageRecyclerView.smoothScrollToPosition(messages.size() - 1);
                        } else {
                            Log.d(TAG, "no value to add");
                        }
                    } else {
                        Log.d(TAG, "onEvent");

                        String source = value != null && value.getMetadata().hasPendingWrites()
                                ? "Local" : "Server";
                        Log.d(TAG, source);
                        Toast.makeText(ChatsActivity.this, source, Toast.LENGTH_SHORT).show();
                        Message m = value.getDocuments().get(value.getDocuments().size() - 1).toObject(Message.class);
                        Toast.makeText(ChatsActivity.this, "Message: " + m.getText(), Toast.LENGTH_SHORT).show();
                        messages.add(m);
                        messagesAdapter.notifyItemInserted(messages.size() - 1);
                        mMessageRecyclerView.smoothScrollToPosition(messages.size() - 1);

                    }
                }
            });
        }else{
            Toast.makeText(this, "Make it step by step", Toast.LENGTH_SHORT).show();
            messageSingleRef.orderBy("timestamp").addSnapshotListener((value, error) ->{
                if (error != null){
                    Toast.makeText(this, "Error reading message", Toast.LENGTH_SHORT).show();
                }else{
                    if (messages.size() == 0) { /*check if there's already some data*/
                        if (value != null) {
                            for (DocumentSnapshot document : value.getDocuments()) {
                                messages.add(document.toObject(Message.class));
                            }
                            messagesAdapter.notifyDataSetChanged();
                            mMessageRecyclerView.smoothScrollToPosition(messages.size() - 1);
                        } else {
                            Log.d(TAG, "no value to add");
                        }
                    } else {
                        Log.d(TAG, "onEvent");

                        String source = value != null && value.getMetadata().hasPendingWrites()
                                ? "Local" : "Server";
                        Log.d(TAG, source);
                        Toast.makeText(ChatsActivity.this, source, Toast.LENGTH_SHORT).show();
                        Message m = value.getDocuments().get(value.getDocuments().size() - 1).toObject(Message.class);
                        Toast.makeText(ChatsActivity.this, "Message: " + m.getText(), Toast.LENGTH_SHORT).show();
                        messages.add(m);
                        messagesAdapter.notifyItemInserted(messages.size() - 1);
                        mMessageRecyclerView.smoothScrollToPosition(messages.size() - 1);
                }
            }
        });
        }
    }

}