package com.example.friendlychat.fragments;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.friendlychat.Activities.ChatsActivity;
import com.example.friendlychat.Activities.UserContactsActivity;
import com.example.friendlychat.Adapters.MessagesAdapter;
import com.example.friendlychat.Module.DateUtils;
import com.example.friendlychat.Module.FileUtil;
import com.example.friendlychat.Module.FullMessage;
import com.example.friendlychat.Module.Message;
import com.example.friendlychat.Module.MessagesPreference;
import com.example.friendlychat.Module.NotificationUtils;
import com.example.friendlychat.Module.User;
import com.example.friendlychat.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import id.zelory.compressor.Compressor;

public class ChatsFragment extends Fragment {
    /*real time permission*/
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                    pickImageFromGallery();
                } else {
                    Toast.makeText(requireContext(), "Ok, if you need to send images please grant the requested permission", Toast.LENGTH_SHORT).show();
                }
            });
    /*TAG for logging*/
    private static final String TAG = ChatsActivity.class.getSimpleName();
    public static final String ANONYMOUS = "anonymous";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    private RecyclerView mMessageRecyclerView;
    private EditText mMessageEditText;
    private FloatingActionButton mSendButton;
    private CollectionReference messagesRef;
    private List<Message> messages;
    private MessagesAdapter messagesAdapter;
    private String mUsername;
    private boolean isGroup;
    private FirebaseFirestore mFirebasestore;
    private CollectionReference messageSingleRef;
    private CollectionReference messageSingleRefTarget;
    private StorageReference mRootRef;
    /*user profile image*/
    private ImageView chat_image;
    private TextView chat_title;
    private TextView chat_last_seen;
    private int tracker = 0;

    /*info for FullMessage class*/
    private String targetUserId;
    private Message lastMessage;
    private String targetUserName;
    private String targetUserPhotoUrl;

    /*chat info in upper toolbar*/
    private boolean isWriting;
    private boolean isActive;
    private long lastTimeSeen;
    /*---------------------*/
    /*pick picture via calling picPic.launch() method*/
    private ActivityResultLauncher<String> pickPic = registerForActivityResult(
            new ActivityResultContracts.GetContent(){
                @NonNull
                @Override
                public Intent createIntent(@NonNull Context context, @NonNull String input) {
                    return super.createIntent(context, "image/*");
                }
            },
            this::putIntoImage);
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*firebase storage and its references*/
        FirebaseStorage mStorage = FirebaseStorage.getInstance();
        // Create a storage reference from our app
        mRootRef = mStorage.getReference("images");
        /*Firestore functionality*/
        mFirebasestore = FirebaseFirestore.getInstance();
        /*app UI functionality*/
        mUsername = ANONYMOUS;
        messages = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chats_fragment, container, false);
        chat_image = view.findViewById(R.id.chat_conversation_profile);
        chat_title = view.findViewById(R.id.chat_title);
        chat_last_seen = view.findViewById(R.id.chat_last_seen);
        LinearLayout layout = view.findViewById(R.id.go_back);
        layout.setOnClickListener(v-> {
            Intent ContactsActivity = new Intent(getContext(), UserContactsActivity.class);
            ContactsActivity.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                            Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(ContactsActivity);
        });

        mMessageRecyclerView = view.findViewById(R.id.messageRecyclerView);
        ProgressBar mProgressBar = view.findViewById(R.id.progressBar);
        ImageButton mPhotoPickerButton = view.findViewById(R.id.photoPickerButton);
        mMessageEditText = view.findViewById(R.id.messageEditText);
        mSendButton = view.findViewById(R.id.sendButton);
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        /*implementing Messages Adapter for the RecyclerView*/
        messagesAdapter = new MessagesAdapter(requireContext(), messages);
        mMessageRecyclerView.setAdapter(messagesAdapter);
        mMessageRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // ImagePickerButton shows an image picker to upload a image for a message
        mPhotoPickerButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) ==
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

        // using the navigation component You should share data between fragment using actions
/*

        Intent mIntent = getIntent();
        if (mIntent != null){
            setTitle(mIntent.getStringExtra(getResources().getString(R.string.chat_title)));
            isGroup = !mIntent.hasExtra(getResources().getString(R.string.targetUidKey));
        }
*/

        // Enable Send button when there's text to send
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d(TAG, "beforeTextChanged");
                setUserIsWriting();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                    setUserIsNotWriting();
                    tracker = 0;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.d(TAG, "afterTextChanged");
            }
        });
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

        Log.d(TAG, "isGroup " + isGroup);

        mSendButton.setOnClickListener( v -> {
            long dateInUTC = DateUtils.getNormalizedUtcDateForToday();
            long dateInLocalTime = System.currentTimeMillis();
            long dateFromDateClass = new Date().getTime();
            Log.d(TAG, "Date in UTC" + dateInUTC);
            Log.d(TAG, "Date in Local (System.currentTimeMillis() ) " + dateInLocalTime);
            Log.d(TAG, "Date in Local (Date().getTime()) " + dateFromDateClass);
            if (dateInLocalTime == dateFromDateClass)
                Log.d(TAG, "Date from System and Date from date are the same");
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMM dd. yyyy. -- H:mm aa zzzz" , Locale.getDefault());
            Log.d(TAG, "---------------------------------------------------------------------------");
            Log.d(TAG, "Date in UTC" + sdf.format(dateInUTC));
            Log.d(TAG, "Date in Local (System.currentTimeMillis() ) " + sdf.format(dateInLocalTime));
            Log.d(TAG, "Date in Local (Date().getTime()) " + sdf.format(dateFromDateClass));
            Message message = new Message(mMessageEditText.getText().toString(), mUsername, "", MessagesPreference.getUserId(requireContext()), dateFromDateClass);
            sendMessage(message);
        });

/*

        if (!isGroup) {
            assert mIntent != null;
            */
/*target user Id*//*

            targetUserId = mIntent.getStringExtra(getResources().getString(R.string.targetUidKey));
            targetUserName = mIntent.getStringExtra(getResources().getString(R.string.chat_title));
            targetUserPhotoUrl = mIntent.getStringExtra(getResources().getString(R.string.photoUrl));
            FirebaseAuth auth = FirebaseAuth.getInstance();
            messageSingleRef = mFirebasestore.collection("rooms").document(Objects.requireNonNull(auth.getUid()))
                    .collection("chats")
                    .document(auth.getUid()+ targetUserId)
                    .collection("messages");
            Map<String, Object> data = new HashMap<>();
            data.put("isWriting", false);
            messageSingleRef.document("isWriting").set(data);
            assert targetUserId != null;
            messageSingleRefTarget = mFirebasestore.collection("rooms")
                    .document(targetUserId)
                    .collection("chats").document(targetUserId + auth.getUid())
                    .collection("messages");
            messageSingleRefTarget.document("isWriting").set(data);
            setChatInfo(targetUserId);

        }else{
            messagesRef = mFirebasestore.collection("rooms").document("people use the app")
                    .collection("messages");
        }
        initializeUserAndData();
*/

        return view;
    }

    private void setUserIsNotWriting() {


        if (!isGroup) {
            messageSingleRef.document("isWriting")
                    .update("isWriting", false);

            Log.d(TAG, "set user is not writing");
        }
    }

    private void setUserIsWriting() {
        Log.d(TAG, "set user is writing");
        if (tracker == 0 && !isGroup){
            messageSingleRef.document("isWriting")
                    .update("isWriting", true);
            tracker++;
            Log.d(TAG, " just one time *_* ");
        }
    }

    private void setChatInfo(String targetUserId) {
        /*in the next commit I'll be setting the chat's info*/
        mFirebasestore.collection("rooms").document(targetUserId)
                .get().addOnSuccessListener(documentSnapshot -> {
            User user = documentSnapshot.toObject(User.class);
            if (user != null) {
                targetUserPhotoUrl = user.getPhotoUrl();
                targetUserName = user.getUserName();
                chat_title.setText(targetUserName);
                Picasso.get().load(targetUserPhotoUrl).placeholder(R.drawable.ic_baseline_emoji_emotions_24).into(chat_image);
                isActive = user.getIsActive();
                lastTimeSeen = user.getLastTimeSeen();
                updateChatInfo();
            }
        });
        listenToChange(targetUserId);
    }

    private void listenToChange(String targetUserId) {
        messageSingleRefTarget.document("isWriting")
                .addSnapshotListener( (value, error) -> {
                    assert value != null;
                    isWriting = (boolean) value.get("isWriting");
                    updateChatInfo();
                });
        mFirebasestore.collection("rooms").document(targetUserId)
                .addSnapshotListener( ((value, error) -> {
                    assert value != null;
                    User user = value.toObject(User.class);
                    assert user != null;
                    isActive = user.getIsActive();
                    lastTimeSeen = user.getLastTimeSeen();
                    updateChatInfo();
                }));

    }

    /*this method will update the chat info int the toolbar in real time!*/
    private void updateChatInfo() {

        if (isWriting){
            chat_last_seen.setText(getResources().getString(R.string.isWriting));
            chat_last_seen.setTextColor(getResources().getColor(R.color.colorAccentLight));
        }
        else if (isActive) {
            chat_last_seen.setText(getResources().getString(R.string.online));
            chat_last_seen.setTextColor(getResources().getColor(R.color.colorTitle));
        }
        else {

            SimpleDateFormat df = new SimpleDateFormat("EEE, MMM d", Locale.getDefault());
            String lastTimeSeenText = df.format(lastTimeSeen);
            SimpleDateFormat df2 = new SimpleDateFormat("h:mm a", Locale.getDefault());
            String text2 = df2.format(lastTimeSeen);
            String lastTimeSeenToDisplay = lastTimeSeenText +", "+ text2;
            chat_last_seen.setText(lastTimeSeenToDisplay);
        }
    }

    private void putIntoImage(Uri uri)  {

        if (uri != null) {
            try {
                File galeryFile = FileUtil.from(requireContext(), uri);
                /*compress the file using a special library*/
                File compressedImageFile = new Compressor(requireContext()).compressToFile(galeryFile);
                /*take the file name as a unique identifier*/
                StorageReference imageRef = mRootRef.child(compressedImageFile.getName());
                // finally uploading the file to firebase storage.
                UploadTask uploadTask = imageRef.putFile(Uri.fromFile(compressedImageFile));
                Log.d(TAG, "Original file size is: " + galeryFile.length() / 1024 + "KB");
                Log.d(TAG, "Compressed file size is: " + compressedImageFile.length() / 1024 + "KB");

                // Register observers to listen for when the download is done or if it fails
                uploadTask.addOnFailureListener(exception -> {
                    // Handle unsuccessful uploads
                    Toast.makeText(requireContext(), "failed to set the image please try again later", Toast.LENGTH_SHORT).show();
                }).addOnSuccessListener(taskSnapshot -> {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    // ...
                    Toast.makeText(requireContext(), "Added image to Storage successfully", Toast.LENGTH_SHORT).show();
                    imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        Log.d(TAG, downloadUri.toString());
                        Log.d(TAG, String.valueOf(downloadUri));
                        String downloadUrl = downloadUri.toString();
                        long dateFromDateClass = new Date().getTime();
                        Message message = new Message("", mUsername, downloadUrl, dateFromDateClass);
                        sendMessage(message);
                    });

                });

            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "Error compressing the file");
                Toast.makeText(requireContext(), "Error occurs", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(requireContext(), "Sending image operation canceled", Toast.LENGTH_SHORT).show();
        }

    }




    private void sendMessage(Message message) {

        if (isGroup){

            messagesRef
                    .add(message)
                    .addOnSuccessListener(
                            documentReference -> Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId())

                    )
                    .addOnFailureListener(e -> Log.w(TAG, "Error adding document", e));
            // Clear input box
        }else {
            messageSingleRef
                    .add(message)
                    .addOnSuccessListener(
                            documentReference -> {
                                Toast.makeText(requireContext(), "Added new message", Toast.LENGTH_SHORT).show();
                                messageSingleRefTarget.add(message);
                                lastMessage = message;
                                FullMessage fullMessage = new FullMessage(lastMessage, targetUserName, targetUserPhotoUrl, targetUserId);
                                Objects.requireNonNull(messageSingleRef.getParent()).set(fullMessage);
                                String currentUserName = MessagesPreference.getUserName(requireContext());
                                String currentPhotoUrl = MessagesPreference.getUsePhoto(requireContext());
                                String currentUserId = MessagesPreference.getUserId(requireContext());
                                FullMessage targetFullMessage = new FullMessage(lastMessage, currentUserName, currentPhotoUrl, currentUserId);
                                Objects.requireNonNull(messageSingleRefTarget.getParent()).set(targetFullMessage);
                            }).addOnFailureListener(e ->
                    Toast.makeText(requireContext(), "Error" + e.toString(), Toast.LENGTH_SHORT).show());
        }
        mMessageEditText.setText("");

    }

    private void sendNotification(Message message) {
        String currentUserId = MessagesPreference.getUserId(requireContext());
        String senderId = message.getSenderId();
        if (senderId != null) {
            if (!senderId.equals(currentUserId))
                NotificationUtils.notifyUserOfNewMessage(requireContext(), message);
        }
    }

    private void pickImageFromGallery() {
        pickPic.launch("image/*");
    }


    private void initializeUserAndData() {

        /*read all messages form the database and add any new messages with notifying the Adapter after that*/
        String userName = MessagesPreference.getUserName(requireContext());
        mUsername = userName;
        Toast.makeText(requireContext(), "Welcome " + userName + "!", Toast.LENGTH_SHORT).show();

        Log.d(TAG, "initialize user and data");
        if (isGroup) {
            /*listen to any new new data*/
            messagesRef.orderBy("timestamp").addSnapshotListener((value, error) -> {
                if (error != null) {
                    Toast.makeText(requireContext(), "fail", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "no error should read data properly");
                    String source = value != null && value.getMetadata().hasPendingWrites()
                            ? "Local" : "Server";
                    Log.d(TAG, source);
                    Toast.makeText(requireContext(), source, Toast.LENGTH_SHORT).show();
                    addNewMessage(value);
                }
            });
        }else{
            /*get all the messages, and listen to any up coming one*/
            messageSingleRef.orderBy("timestamp").addSnapshotListener((value, error) -> {
                if (error != null){
                    Toast.makeText(requireContext(), "Error reading message", Toast.LENGTH_SHORT).show();
                }else{
                    String source = value != null && value.getMetadata().hasPendingWrites()
                            ? "Local" : "Server";
                    Log.d(TAG, source);
                    Toast.makeText(requireContext(), source, Toast.LENGTH_SHORT).show();
                    addNewMessage(value);
                }
            });
        }
    }

    private void addNewMessage(QuerySnapshot value) {
        if (value != null) {

            for (DocumentChange dc : value.getDocumentChanges()) {
                Message newMessage = dc.getDocument().toObject(Message.class);
                messages.add(newMessage);
                Log.d(TAG, "document change");

            }
            Log.d(TAG, "the number of messages in this chat is: " + value.getDocumentChanges().size());
            messagesAdapter.notifyDataSetChanged();
            sendNotification(messages.get(messages.size() -1));
            mMessageRecyclerView.smoothScrollToPosition(messages.size() - 1);

        }
    }
}