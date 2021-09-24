package com.example.friendlychat.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.friendlychat.Adapters.MessagesAdapter;
import com.example.friendlychat.Module.FileUtil;
import com.example.friendlychat.Module.FullImageData;
import com.example.friendlychat.Module.Message;
import com.example.friendlychat.Module.MessagesPreference;
import com.example.friendlychat.Module.User;
import com.example.friendlychat.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.protobuf.MapEntryLite;
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

import id.zelory.compressor.Compressor;

public class ChatsFragment extends Fragment implements MessagesAdapter.MessageClick,
    ChildEventListener{
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
    private static final String TAG = ChatsFragment.class.getSimpleName();
    // optional for shiny users *_*
    public static final String ANONYMOUS = "anonymous";
    // max number of characters with a single message.
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    // views
    private RecyclerView mMessageRecyclerView;
    private EditText mMessageEditText;
    private FloatingActionButton mSendButton;
    // functionality
    private List<Message> messages;
    private MessagesAdapter messagesAdapter;
    private String mUsername;
    // firestore to get the user state wheater they're active or not
    private FirebaseFirestore mFirebasestore;
    // for sending and receiving photos
    private StorageReference mRootRef;
    /*toolbar views to display target user info*/
    private ImageView chat_image;
    private TextView chat_title;
    private TextView chat_last_seen;
    // this tracker is used to invoke the method of the realtime database to update the user is writing once
    private int tracker = 0;

    private NavController navController;

    // toolbar values
    private String targetUserId;
    private String targetUserName;
    private String targetUserPhotoUrl;

    // for target user profile in detail
    private Bundle targetUserData;

    /*chat info in upper toolbar*/
    private boolean isWriting;
    private boolean isActive;
    private long lastTimeSeen;
    /*---------------------*/
    /* progress bar to show loading of messages*/
    private ProgressBar mProgressBar;
    // ok this is a perfi
    /*pick picture via calling picPic.launch() method*/

    // firebase realtime database
    private DatabaseReference mCurrentUserRoomReference;
    private DatabaseReference mTargetUserRoomReference;

    private String currentUserId;
    private String currentUserName;
    private String currentPhotoUrl;

    private ActivityResultLauncher<String> pickPic = registerForActivityResult(
            new ActivityResultContracts.GetContent(){
                @NonNull
                @Override
                public Intent createIntent(@NonNull Context context, @NonNull String input) {
                    return super.createIntent(context, "image/*");// filter the gallery output, so the user can send a photo as they expects
                }
            },
            this::putIntoImage);
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        requireActivity().findViewById(R.id.bottom_nav).setVisibility(View.GONE);
        /*firebase storage and its references*/
        FirebaseStorage mStorage = FirebaseStorage.getInstance();
        // Create a storage reference from our app
        mRootRef = mStorage.getReference("images");
        /*Firestore functionality*/
        mFirebasestore = FirebaseFirestore.getInstance();
        /*app UI functionality*/
        mUsername = ANONYMOUS;
        messages = new ArrayList<>();
        targetUserData = getArguments();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
       if (targetUserData != null) {
           targetUserId = targetUserData.getString("target_user_id", "id for target user");
           // rooms references
           currentUserId =  MessagesPreference.getUserId(requireContext());
           mCurrentUserRoomReference = database.getReference("rooms").child(currentUserId)
                   .child(currentUserId + targetUserId);
           mTargetUserRoomReference = database.getReference("rooms").child(targetUserId)
                   .child(targetUserId + currentUserId);
       }else{
           Toast.makeText(requireContext(), "Data is null", Toast.LENGTH_SHORT).show();
       }

        currentUserName = MessagesPreference.getUserName(requireContext());
        currentPhotoUrl = MessagesPreference.getUsePhoto(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chats_fragment, container, false);
         navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        Toolbar tb = view.findViewById(R.id.toolbar_frag);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(tb);
        
        chat_image = view.findViewById(R.id.chat_conversation_profile);
        chat_title = view.findViewById(R.id.chat_title);
        chat_last_seen = view.findViewById(R.id.chat_last_seen);
        LinearLayout layout = view.findViewById(R.id.go_back);
        layout.setOnClickListener( v -> {
            Log.d(TAG, "onCreateView: navigate to the back stack through the navigation components");
            navController.navigateUp();
        });
        LinearLayout targetUserLayout = view.findViewById(R.id.conversationToolbarUserInfo);
        targetUserLayout.setOnClickListener(targetUserLayoutListener ->{

            Log.d(TAG, "onCreateView: target photo Url : " + targetUserData.getString("target_user_photo_url"));
            /*messages.clear();*/ // clean the list to ensure it will not contain duplicated data
            navController.navigate(R.id.action_chatsFragment_to_userProfileFragment,
                    targetUserData);

        });
        setChatInfo();
        mMessageRecyclerView = view.findViewById(R.id.messageRecyclerView);
        mProgressBar = view.findViewById(R.id.progressBar);
        ImageButton mPhotoPickerButton = view.findViewById(R.id.photoPickerButton);
        mMessageEditText = view.findViewById(R.id.messageEditText);
        mSendButton = view.findViewById(R.id.sendButton);
        mProgressBar.setVisibility(ProgressBar.VISIBLE);

        /*implementing Messages Adapter for the RecyclerView*/
        messagesAdapter = new MessagesAdapter(requireContext(), messages, this);
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

        mSendButton.setOnClickListener( v -> {

            long dateInLocalTime = System.currentTimeMillis();
            long dateFromDateClass = new Date().getTime();

            Log.d(TAG, "Date in Local (System.currentTimeMillis() ) " + dateInLocalTime);
            Log.d(TAG, "Date in Local (Date().getTime()) " + dateFromDateClass);
            if (dateInLocalTime == dateFromDateClass)
                Log.d(TAG, "Date from System and Date from date are the same");
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMM dd. yyyy. -- H:mm aa zzzz" , Locale.getDefault());
            Log.d(TAG, "---------------------------------------------------------------------------");

            Log.d(TAG, "Date in Local (System.currentTimeMillis() ) " + sdf.format(dateInLocalTime));
            Log.d(TAG, "Date in Local (Date().getTime()) " + sdf.format(dateFromDateClass));


            String text = mMessageEditText.getText().toString();
            Message currentUserMsg = new Message(text, "", dateFromDateClass, currentUserId, currentUserId,
                    mUsername, currentUserName, currentPhotoUrl, false);
            Message targetUserMsg = new Message(text, "", dateFromDateClass, currentUserId, targetUserId,
                    mUsername, targetUserName, targetUserPhotoUrl, false);
            sendMessage(currentUserMsg, targetUserMsg);
        });

        if (messages.size() == 0)
            initializeUserAndData();
        else
            mProgressBar.setVisibility(View.GONE);

        return view;
    }

    private void setUserIsNotWriting() {
           mCurrentUserRoomReference.child("isWriting").setValue(false);
            Log.d(TAG, "set user is not writing");
    }

    private void setUserIsWriting() {

        Log.d(TAG, "set user is writing");
        if (tracker == 0){
            mCurrentUserRoomReference.child("isWriting")
                    .setValue(true);
            /*messageSingleRef.document("isWriting")
                    .update("isWriting", true);*/
            tracker++;
            Log.d(TAG, " just one time *_* ");
        }
    }

    private void setChatInfo() {

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
                populateTargetUserInfo(user);
                listenToChange(targetUserId);
            }
        });

    }

    private void populateTargetUserInfo(User user) {
        Log.d(TAG, "populateTargetUserInfo: populate successfully");
        Log.d(TAG, "from populate: the targer user photo url : " + user.getPhotoUrl());
        targetUserData.putString("target_user_id", user.getUserId());
        Log.d(TAG, "populateTargetUserInfo: target userId: " + targetUserData.getString("target_user_id"));
        targetUserData.putString("target_user_email", user.getEmail());
        targetUserData.putString("target_user_photo_url", user.getPhotoUrl());
        targetUserData.putString("target_user_status", user.getStatus());
        targetUserData.putString("target_user_name", user.getUserName());
        targetUserData.putBoolean("isActive", user.getIsActive());
        targetUserData.putLong("target_user_last_time_seen", user.getLastTimeSeen());

    }

    private void listenToChange(String targetUserId) {
        mFirebasestore.collection("rooms").document(targetUserId)
                .addSnapshotListener( ((value, error) -> {
                    assert value != null;
                    User user = value.toObject(User.class);
                    assert user != null;
                    isActive = user.getIsActive();
                    targetUserData.putBoolean("isActive", isActive);
                    lastTimeSeen = user.getLastTimeSeen();
                    updateChatInfo();
                }));

    }

    /*this method will update the chat info int the toolbar in real time!*/
    private void updateChatInfo() {
        Log.d(TAG, "updateChatInfo: ");
        if (getContext() != null){
            if (isWriting){
                chat_last_seen.setText(getResources().getString(R.string.isWriting));
                chat_last_seen.setTextColor(getResources().getColor(R.color.colorAccent));
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
    }

    private void putIntoImage(Uri uri)  {

        if (uri != null) {
            try {
                File galleryFile = FileUtil.from(requireContext(), uri);
                /*compress the file using a special library*/
                File compressedImageFile = new Compressor(requireContext()).compressToFile(galleryFile);
                /*take the file name as a unique identifier*/
                StorageReference imageRef = mRootRef.child(compressedImageFile.getName());
                // finally uploading the file to firebase storage.
                UploadTask uploadTask = imageRef.putFile(Uri.fromFile(compressedImageFile));

                // some logging to track the file size after compressing it with the library.
                Log.d(TAG, "Original file size is: " + galleryFile.length() / 1024 + "KB");
                Log.d(TAG, "Compressed file size is: " + compressedImageFile.length() / 1024 + "KB"); // after compressing the file

                // Register observers to listen for when the download is done or if it fails
                uploadTask.addOnFailureListener(exception -> {
                    // Handle unsuccessful uploads
                    Toast.makeText(requireContext(), "failed to set the image please try again later", Toast.LENGTH_SHORT).show();
                }).addOnSuccessListener(taskSnapshot -> {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    // ...
                    Toast.makeText(getContext(), "Added image to Storage successfully", Toast.LENGTH_SHORT).show();
                    imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        Log.d(TAG, downloadUri.toString());
                        Log.d(TAG, String.valueOf(downloadUri));
                        String downloadUrl = downloadUri.toString();
                        long dateFromDateClass = new Date().getTime();
                         /* if the image sent successfully to the firebase storage send its metadata as a message
                         to the firebase firestore */
                        Message currentUserMsg = new Message("", downloadUrl, dateFromDateClass, currentUserId, currentUserId,
                                mUsername, currentUserName, currentPhotoUrl, false);
                        Message targetUserMsg = new Message("", downloadUrl, dateFromDateClass, currentUserId, targetUserId,
                                mUsername, targetUserName, targetUserPhotoUrl, false);
                        sendMessage(currentUserMsg, targetUserMsg);
                    });

                });

            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "Error compressing the file");
                Toast.makeText(requireContext(), "Error occurs", Toast.LENGTH_SHORT).show();
            }
            // if the user hit the back button before choosing an image to send the code below will be executed.
        }else{
            Toast.makeText(requireContext(), "Sending image operation canceled", Toast.LENGTH_SHORT).show();
        }

    }

    // these overriding methods for debugging only and will be cleaned up in the future.
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // remove the listener when the view is no longer visilbe for the user
        mCurrentUserRoomReference.removeEventListener(this);
        mTargetUserRoomReference = null;
        mCurrentUserRoomReference = null;
        Log.d(TAG, "onDestroyView: ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    private void sendMessage(Message currentUserMsg, Message targetUserMsg) {

        mCurrentUserRoomReference.push().setValue(targetUserMsg).addOnSuccessListener(success ->
                mTargetUserRoomReference.push().setValue(currentUserMsg)).addOnFailureListener(exception ->
                Log.d(TAG, "sendMessage: exception msg: " + exception.getMessage()));
        mMessageEditText.setText("");

    }

/*    private void sendNotification(Message message) {
        String currentUserId = MessagesPreference.getUserId(requireContext());
        String senderId = message.getSenderId();
        if (senderId != null) {
            if (!senderId.equals(currentUserId))
                NotificationUtils.notifyUserOfNewMessage(requireContext(), message);
        }
    }*/

    private void pickImageFromGallery() {
        pickPic.launch("image/*");
    }


    private void initializeUserAndData() {

        /*read all messages form the database and add any new messages with notifying the Adapter after that*/
        mUsername = MessagesPreference.getUserName(requireContext());
       /* mCurrentUserRoomReference.get().addOnSuccessListener(this::insertMessagesInAdapter);*/
        mCurrentUserRoomReference.addChildEventListener(this);
        mTargetUserRoomReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                isWriting = (boolean) snapshot.getValue();
                Log.d(TAG, "onChildChanged: isWriting" + isWriting);
                updateChatInfo();
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
        mProgressBar.setVisibility(View.GONE);
    }

    private void insertMessagesInAdapter(DataSnapshot allMessagesDataSnapshot) {
        // here all the messages will be loaded
        Iterable<DataSnapshot> iterable = allMessagesDataSnapshot.getChildren();
        for (DataSnapshot dataSnapshot : iterable) {
            try {
                messages.add(dataSnapshot.getValue(Message.class));
            }catch (Exception e){
                Log.d(TAG, "insertMessagesInAdapter: " + e.getMessage());
            }
        }
        if (messages.size() > 0) {
            messagesAdapter.notifyDataSetChanged();
            mMessageRecyclerView.scrollToPosition(messages.size() - 1);
        }else
            Toast.makeText(requireContext(), "the size of the messages is 0 or less", Toast.LENGTH_SHORT).show();
    }

    /* this method is used in two functionality, for getting all the messages from a special room
    * and for adding new messages as the user sends. */
    private void addNewMessage(DataSnapshot value) {
        mProgressBar.setVisibility(View.INVISIBLE);
        try {
            Message newMessage = value.getValue(Message.class);
            if (!newMessage.getIsRead() && !newMessage.getSenderId().equals(currentUserId))
                markMessageAsRead(value, newMessage);
            messages.add(newMessage);
            if (messages.size() > 0) {
                messagesAdapter.notifyDataSetChanged();
                mMessageRecyclerView.scrollToPosition(messages.size() - 1);
            }
        }catch (Exception e){
            Log.d(TAG, "addNewMessage: exception " + e.getMessage());
        }
    }

    private void markMessageAsRead(DataSnapshot snapshotMessageTobeUpdated, Message messageToUpdate) {
        Log.d(TAG, "markMessageAsRead: ");
        Map<String, Object> originalMessage = messageToUpdate.toMap();
        originalMessage.put("isRead", true);
        snapshotMessageTobeUpdated.getRef().updateChildren(originalMessage).addOnSuccessListener(
                successListener -> {
                    Log.d(TAG, "update message successfully to be read");
                    // update locally as well
                    messageToUpdate.setIsRead(true);
                    messagesAdapter.notifyDataSetChanged();
                }

        ).addOnFailureListener(
                exception -> Log.d(TAG, "markMessageAsRead: " + exception.getMessage()
                ));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.profile:
                Toast.makeText(requireContext(), "Go to Profile", Toast.LENGTH_SHORT).show(); // will be implemented in the future
                break;
            case R.id.make_video_call:
            case R.id.make_normal_call:
                displayFutureFeature();
                break;
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.chats_menu, menu);
    }
    private void displayFutureFeature(){
        Toast.makeText(requireContext(), "This feature wil be added the next next version of the app", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMessageClick(View view, int position) {
        Toast.makeText(requireContext(), "You click a view", Toast.LENGTH_SHORT).show();
        Message message = messages.get(position);
        String senderName = message.getSenderName();
        SimpleDateFormat d = new SimpleDateFormat("EEE, MMM d", Locale.getDefault());
        String formattedDate = d.format(message.getTimestamp());
        ImageView imageView = (ImageView) view;
        /* enable the drawing cache for the image view to derive a bitmap from it*/
        /*imageView.setDrawingCacheEnabled(true);*/
        BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = bitmapDrawable.getBitmap();
        // checking if the bitmap is correct first by the following code:

        /* after initializing these 3 arguments, let's use them*/
        FullImageData imageData = new FullImageData(senderName, formattedDate, bitmap);
        ChatsFragmentDirections.ActionChatsFragmentToFullImageFragment actionToFullImageFragment =
                ChatsFragmentDirections.actionChatsFragmentToFullImageFragment(imageData);
        FragmentNavigator.Extras extras = new FragmentNavigator.Extras.Builder()
                .addSharedElement(view, "root_view_full_image_fragment")
                .build();


        /*NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);*/
        navController.navigate(actionToFullImageFragment, extras);

    }


    // listeners
    @Override
    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
        addNewMessage(snapshot);
        Log.d(TAG, "onChildAdded: ");
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
}
