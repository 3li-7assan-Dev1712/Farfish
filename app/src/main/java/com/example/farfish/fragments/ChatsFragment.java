package com.example.farfish.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;


import androidx.activity.OnBackPressedCallback;
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

import com.aghajari.emojiview.view.AXEmojiPopupLayout;
import com.aghajari.emojiview.view.AXEmojiView;
import com.example.farfish.Adapters.MessagesListAdapter;
import com.example.farfish.Module.Connection;
import com.example.farfish.Module.FileUtil;
import com.example.farfish.Module.FullImageData;
import com.example.farfish.Module.Message;
import com.example.farfish.Module.MessagesPreference;
import com.example.farfish.Module.User;
import com.example.farfish.R;
import com.example.farfish.databinding.ChatsFragmentBinding;
import com.example.farfish.databinding.ToolbarConversationBinding;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import id.zelory.compressor.Compressor;

public class ChatsFragment extends Fragment implements MessagesListAdapter.MessageClick {
    // root class
    private ChatsFragmentBinding mBinding;
    private ToolbarConversationBinding mToolbarBinding;
    /*real time permission*/
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                    pickImageFromGallery();
                } else {
                    Toast.makeText(requireContext(), requireContext().getString(R.string.grant_access_media_permission), Toast.LENGTH_SHORT).show();
                }
            });
    /*TAG for logging*/
    private static final String TAG = ChatsFragment.class.getSimpleName();
    // optional for shiny users *_*
    public static final String ANONYMOUS = "anonymous";
    // max number of characters with a single message.
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;

    // functionality
    private List<Message> messages;

    private MessagesListAdapter messagesListAdapter;
    private String mUsername;
    // firestore to get the user state wheater they're active or not
    private FirebaseFirestore mFirebasestore;
    // for sending and receiving photos
    private StorageReference mRootRef;
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


    // firebase realtime database
    private DatabaseReference mCurrentUserRoomReference;
    private DatabaseReference mTargetUserRoomReference;

    private String currentUserId;
    private String currentUserName;
    private String currentPhotoUrl;

    private boolean USER_EXPECT_TO_RETURN = false;
    private ActivityResultLauncher<String> pickPic = registerForActivityResult(
            new ActivityResultContracts.GetContent() {
                @NonNull
                @Override
                public Intent createIntent(@NonNull Context context, @NonNull String input) {
                    return super.createIntent(context, "image/*");// filter the gallery output, so the user can send a photo as they expects
                }
            },
            this::putIntoImage);

    // rooms listeners
    private CurrentRoomListener mCurrentRoomListener;
    private TargetRoomListener mTargetRoomListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!mBinding.emojiKeyboardPopup.onBackPressed())
                    navController.navigateUp();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
        messages = new ArrayList<>();
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

        targetUserData = getArguments();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        if (targetUserData != null) {
            targetUserId = targetUserData.getString("target_user_id", "id for target user");
            // rooms references
            currentUserId = MessagesPreference.getUserId(requireContext());
            mCurrentUserRoomReference = database.getReference("rooms").child(currentUserId)
                    .child(currentUserId + targetUserId);
            mTargetUserRoomReference = database.getReference("rooms").child(targetUserId)
                    .child(targetUserId + currentUserId);
        } else {
            Toast.makeText(requireContext(), "Data is null", Toast.LENGTH_SHORT).show();
        }

        currentUserName = MessagesPreference.getUserName(requireContext());
        currentPhotoUrl = MessagesPreference.getUsePhoto(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = ChatsFragmentBinding.inflate(inflater, container, false);
        View view = mBinding.getRoot();
        Log.d(TAG, "onCreateView: ");
        // listeners
        mCurrentRoomListener = new CurrentRoomListener();
        mTargetRoomListener = new TargetRoomListener();
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        Toolbar tb = mBinding.toolbarFrag;
        ((AppCompatActivity) requireActivity()).setSupportActionBar(tb);
        mToolbarBinding = mBinding.toolbarTargetUserInfo;

        LinearLayout layout = view.findViewById(R.id.go_back);
        layout.setOnClickListener(v -> {
            Log.d(TAG, "onCreateView: navigate to the back stack through the navigation components");
            navController.navigateUp();
        });

        mToolbarBinding.conversationToolbarUserInfo.setOnClickListener(targetUserLayoutListener -> {

            USER_EXPECT_TO_RETURN = true;
            Log.d(TAG, "onCreateView: target photo Url : " + targetUserData.getString("target_user_photo_url"));
            /*messages.clear();*/ // clean the list to ensure it will not contain duplicated data
            navController.navigate(R.id.action_chatsFragment_to_userProfileFragment,
                    targetUserData);

        });
        setChatInfo();

        mBinding.progressBar.setVisibility(ProgressBar.VISIBLE);

        /*implementing Messages Adapter for the RecyclerView*/
        /*messagesAdapter = new MessagesAdapter(requireContext(), messages, this);*/
        messagesListAdapter = new MessagesListAdapter(messages, requireContext(), this, false);
        mBinding.messageRecyclerView.setAdapter(messagesListAdapter);
        mBinding.messageRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // ImagePickerButton shows an image picker to upload a image for a message
        mBinding.photoPickerButton.setOnClickListener(v -> {
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

        AXEmojiView emojiView = new AXEmojiView(requireContext());
        emojiView.setEditText(mBinding.messageEditText);
        AXEmojiPopupLayout emojiPopupLayout = view.findViewById(R.id.emoji_keyboard_popup);
        emojiPopupLayout.initPopupView(emojiView);
        emojiPopupLayout.hideAndOpenKeyboard();
        mBinding.messageEditText.setOnClickListener(editTextView -> {
            emojiPopupLayout.openKeyboard();
            emojiPopupLayout.setVisibility(View.GONE);
            Log.d(TAG, "onCreateView: mMessageEditTextClick");
        });
        mBinding.sendEmojiBtn.setOnClickListener(emojiPopupListener -> {
            if (emojiPopupLayout.isShowing()) {
                emojiPopupLayout.openKeyboard();
                emojiPopupLayout.dismiss();
                emojiPopupLayout.setVisibility(View.GONE);
                mBinding.sendEmojiBtn.setImageResource(R.drawable.ic_baseline_emoji_emotions_24_50_gray);
            } else {
                emojiPopupLayout.setVisibility(View.VISIBLE);
                emojiPopupLayout.show();
                mBinding.sendEmojiBtn.setImageResource(R.drawable.ic_baseline_keyboard_24);
            }
        });

        // Enable Send button when there's text to send
        mBinding.messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d(TAG, "beforeTextChanged");
                setUserIsWriting();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mBinding.sendButton.setEnabled(true);
                } else {
                    mBinding.sendButton.setEnabled(false);
                    setUserIsNotWriting();
                    tracker = 0;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.d(TAG, "afterTextChanged");
            }
        });
        mBinding.messageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

        mBinding.sendButton.setOnClickListener(v -> {

            long dateInLocalTime = System.currentTimeMillis();
            long dateFromDateClass = new Date().getTime();

            Log.d(TAG, "Date in Local (System.currentTimeMillis() ) " + dateInLocalTime);
            Log.d(TAG, "Date in Local (Date().getTime()) " + dateFromDateClass);
            if (dateInLocalTime == dateFromDateClass)
                Log.d(TAG, "Date from System and Date from date are the same");
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMM dd. yyyy. -- H:mm aa zzzz", Locale.getDefault());
            Log.d(TAG, "---------------------------------------------------------------------------");

            Log.d(TAG, "Date in Local (System.currentTimeMillis() ) " + sdf.format(dateInLocalTime));
            Log.d(TAG, "Date in Local (Date().getTime()) " + sdf.format(dateFromDateClass));


            String text = Objects.requireNonNull(mBinding.messageEditText.getText()).toString();
            Message currentUserMsg = new Message(text, "", dateFromDateClass, currentUserId, currentUserId,
                    mUsername, currentUserName, currentPhotoUrl, false);
            Message targetUserMsg = new Message(text, "", dateFromDateClass, currentUserId, targetUserId,
                    mUsername, targetUserName, targetUserPhotoUrl, false);
            sendMessage(currentUserMsg, targetUserMsg);
        });

        if (messages.size() == 0)
            initializeUserAndData();
        else {
            mBinding.progressBar.setVisibility(View.GONE);
            messagesListAdapter.submitList(messages);
        }

        checkUserConnection();
        return view;
    }

    private void checkUserConnection() {
        if (!Connection.isUserConnected(requireContext())) {
            Snackbar.make(requireActivity().findViewById(R.id.bottom_nav), R.string.user_offline_msg, BaseTransientBottomBar.LENGTH_LONG)
                    .setAnchorView(R.id.bottom_nav).show();
        }
    }

    private void setUserIsNotWriting() {
        mCurrentUserRoomReference.child("isWriting").setValue(false);
        // when the user has no internet connection we set the value of the isWriting to be false
        mCurrentUserRoomReference.child("isWriting").onDisconnect().setValue(false);
        Log.d(TAG, "set user is not writing");
    }

    private void setUserIsWriting() {

        Log.d(TAG, "set user is writing");
        if (tracker == 0) {
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
                mToolbarBinding.chatTitle.setText(targetUserName);
                Picasso.get().load(targetUserPhotoUrl).placeholder(R.drawable.ic_baseline_emoji_emotions_24).into(mToolbarBinding.chatConversationProfile);
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
                .addSnapshotListener(((value, error) -> {
                    assert value != null;
                    User user = value.toObject(User.class);
                    String source =
                            value.getMetadata().isFromCache() ?
                                    "local cache" : "server";
                    Log.d(TAG, "Data fetched from " + source);
                    assert user != null;
                    isActive = user.getIsActive();
                    targetUserData.putBoolean("isActive", isActive);
                    lastTimeSeen = user.getLastTimeSeen();
                    try {
                        if (!Connection.isUserConnected(requireContext()))
                            isActive = false;
                    }catch (Exception ex){

                    }
                    updateChatInfo();
                }));

    }

    /*this method will update the chat info int the toolbar in real time!*/
    private void updateChatInfo() {
        Log.d(TAG, "updateChatInfo: ");
        if (getContext() != null) {
            if (isWriting) {
                mToolbarBinding.chatLastSeen.setText(getResources().getString(R.string.isWriting));
                mToolbarBinding.chatLastSeen.setTextColor(getResources().getColor(R.color.colorAccent));
            } else if (isActive) {

                mToolbarBinding.chatLastSeen.setText(getResources().getString(R.string.online));
                mToolbarBinding.chatLastSeen.setTextColor(getResources().getColor(R.color.colorTitle));
            } else {
                SimpleDateFormat df = new SimpleDateFormat("EEE, MMM d", Locale.getDefault());
                String lastTimeSeenText = df.format(lastTimeSeen);
                SimpleDateFormat df2 = new SimpleDateFormat("h:mm a", Locale.getDefault());
                String text2 = df2.format(lastTimeSeen);
                String lastTimeSeenToDisplay = lastTimeSeenText + ", " + text2;
                mToolbarBinding.chatLastSeen.setText(lastTimeSeenToDisplay);
            }
        }
    }

    private void putIntoImage(Uri uri) {

        mBinding.progressBar.setVisibility(View.VISIBLE);
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
                }).addOnSuccessListener(taskSnapshot -> {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    // ...
                    Toast.makeText(getContext(), requireContext().getString(R.string.sending_img_msg), Toast.LENGTH_SHORT).show();
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
        } else {
            Toast.makeText(requireContext(), requireContext().getString(R.string.cancel_sending_img), Toast.LENGTH_SHORT).show();
        }

    }

    // these overriding methods for debugging only and will be cleaned up in the future.
    @Override
    public void onDestroyView() {

        super.onDestroyView();
        Log.d(TAG, "onDestroyView: "); // for logging
        Log.d(TAG, "onDestroyView: expect user to return " + USER_EXPECT_TO_RETURN);

        if (!USER_EXPECT_TO_RETURN) {
            Log.d(TAG, "onDestroyView: going to clean up");
            // remove the listener when the view is no longer visilbe for the user
            mCurrentUserRoomReference.removeEventListener(mCurrentRoomListener);
            mTargetUserRoomReference.removeEventListener(mTargetRoomListener);
            // clean up views
            mBinding = null;
            mToolbarBinding = null;
            messages.clear();
        } else Log.d(TAG, "onDestroyView: should not clean up the data");

    }


    private void sendMessage(Message currentUserMsg, Message targetUserMsg) {

        String key = mCurrentUserRoomReference.push().getKey();
        Log.d(TAG, "sendMessage: the key of the new two messages is: " + key);
        if (key == null)
            throw new NullPointerException("the key of the new messages should not be null");
        mCurrentUserRoomReference.child(key).setValue(targetUserMsg).addOnSuccessListener(success -> {
            if (mBinding.progressBar.getVisibility() == View.VISIBLE)
                mBinding.progressBar.setVisibility(View.GONE);
            mTargetUserRoomReference.child(key).setValue(currentUserMsg);
                }
                ).addOnFailureListener(exception ->
                Log.d(TAG, "sendMessage: exception msg: " + exception.getMessage()));
        mBinding.messageEditText.setText("");

    }


    private void pickImageFromGallery() {
        ConnectivityManager cm = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        InternetConnectionDialog internetDialog = new InternetConnectionDialog();
        if (!isConnected) internetDialog.show(requireActivity().getSupportFragmentManager(), "internet_alert");
        else pickPic.launch("image/*");
    }


    private void initializeUserAndData() {

        /*read all messages form the database and add any new messages with notifying the Adapter after that*/
        mUsername = MessagesPreference.getUserName(requireContext());
        /* mCurrentUserRoomReference.get().addOnSuccessListener(this::insertMessagesInAdapter);*/

        mCurrentUserRoomReference.addChildEventListener(mCurrentRoomListener);
        mTargetUserRoomReference.addChildEventListener(mTargetRoomListener);
        mBinding.progressBar.setVisibility(View.GONE);
    }


    /* this method is used in two functionality, for getting all the messages from a special room
     * and for adding new messages as the user sends. */
    private void addNewMessage(DataSnapshot value) {
        Log.d(TAG, "addNewMessage: ");
        mBinding.progressBar.setVisibility(View.INVISIBLE);
        try {
            Message newMessage = value.getValue(Message.class);
            assert newMessage != null;
            messages.add(newMessage);
            if (messages.size() > 0) {
                Log.d(TAG, "addNewMessage: messges size is: " + messages.size());
                messagesListAdapter.submitList(messages);
                mBinding.messageRecyclerView.scrollToPosition(messages.size() - 1);
                if (!newMessage.getIsRead() && !newMessage.getSenderId().equals(currentUserId))
                    markMessageAsRead(value, newMessage);
            }
        } catch (Exception e) {
            Log.d(TAG, "addNewMessage: exception " + e.getMessage());
        }
    }

    private void markMessageAsRead(DataSnapshot snapshotMessageTobeUpdated, Message messageToUpdate) {


        Log.d(TAG, "markMessageAsRead: ");
        String key = snapshotMessageTobeUpdated.getKey();
        Log.d(TAG, "markMessageAsRead: the key of the message to be updated is: " + key);
        Map<String, Object> originalMessage = messageToUpdate.toMap();
        originalMessage.put("isRead", true);
        snapshotMessageTobeUpdated.getRef().updateChildren(originalMessage).addOnSuccessListener(
                successListener -> {
                    Log.d(TAG, "update message successfully to be read");
                    //  change the message from target message to local message
                    originalMessage.put("targetId", currentUserId);
                    originalMessage.put("targetName", currentUserName);
                    originalMessage.put("targetPhotoUrl", currentPhotoUrl);
                    mTargetUserRoomReference.child(key).updateChildren(originalMessage).addOnFailureListener(fle ->
                            Log.d(TAG, "markMessageAsRead: " + fle.getMessage()));

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
                Navigation.findNavController(mBinding.getRoot()).navigate(R.id.action_chatsFragment_to_userProfileFragment, targetUserData);
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

    private void displayFutureFeature() {
        Toast.makeText(requireContext(),requireContext().getString(R.string.future_msg), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMessageClick(View view, int position) {
        Message message = messages.get(position);
        String senderName = message.getSenderName();
        SimpleDateFormat d = new SimpleDateFormat("EEE, MMM d", Locale.getDefault());
        String formattedDate = d.format(message.getTimestamp());
        ImageView imageView = (ImageView) view;
        /* enable the drawing cache for the image view to derive a bitmap from it*/
        /*imageView.setDrawingCacheEnabled(true);*/
        BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
        if (bitmapDrawable != null) {
            Bitmap bitmap = bitmapDrawable.getBitmap();

            /* after initializing these 3 arguments, let's use them*/
            FullImageData imageData = new FullImageData(senderName, formattedDate, bitmap);
            ChatsFragmentDirections.ActionChatsFragmentToFullImageFragment actionToFullImageFragment =
                    ChatsFragmentDirections.actionChatsFragmentToFullImageFragment(imageData);
            FragmentNavigator.Extras extras = new FragmentNavigator.Extras.Builder()
                    .addSharedElement(view, "root_view_full_image_fragment")
                    .build();

            USER_EXPECT_TO_RETURN = true;
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigate(actionToFullImageFragment, extras);
        }
    }


    private void refreshData() {
        Log.d(TAG, "refreshData: ");
        messages.clear();
        try {
            mCurrentUserRoomReference.get().addOnSuccessListener(sListener -> {
                Iterable<DataSnapshot> meessagesIterable = sListener.getChildren();
                for (DataSnapshot messageSnapShot : meessagesIterable) {
                    if (!messageSnapShot.getKey().equals("isWriting")) {
                        Message msg = messageSnapShot.getValue(Message.class);
                        assert msg != null;
                        Log.d(TAG, "refreshData: isRead: " + msg.getIsRead());
                        messages.add(msg);
                    }
                }
                messagesListAdapter.submitList(messages);
                messagesListAdapter.notifyDataSetChanged();
            });
        } catch (Exception e) {
            Log.d(TAG, "refreshData: " + e.getMessage());
        }
    }

    class CurrentRoomListener implements ChildEventListener {

        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            addNewMessage(snapshot);
            Log.d(TAG, "onChildAdded: ");
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            Log.d(TAG, "onChildChanged: ");
            if (!snapshot.getKey().equals("isWriting"))
                refreshData();
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

    class TargetRoomListener implements ChildEventListener {

        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            Log.d(TAG, "onChildChanged: the key of the changed child is: " + previousChildName);
            if (snapshot.getKey().equals("isWriting")) {
                isWriting = (boolean) snapshot.getValue();
                Log.d(TAG, "onChildChanged: isWriting " + isWriting);
                updateChatInfo();
            }

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

}
