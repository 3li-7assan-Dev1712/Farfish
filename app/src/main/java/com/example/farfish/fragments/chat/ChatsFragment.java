package com.example.farfish.fragments.chat;

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
import androidx.hilt.navigation.HiltViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aghajari.emojiview.view.AXEmojiPopupLayout;
import com.aghajari.emojiview.view.AXEmojiView;
import com.example.farfish.Adapters.MessagesListAdapter;
import com.example.farfish.Module.dataclasses.FullImageData;
import com.example.farfish.Module.dataclasses.Message;
import com.example.farfish.Module.dataclasses.User;
import com.example.farfish.Module.preferences.MessagesPreference;
import com.example.farfish.Module.util.Connection;
import com.example.farfish.R;
import com.example.farfish.data.MainViewModel;
import com.example.farfish.data.repositories.MessagingRepository;
import com.example.farfish.databinding.ChatsFragmentBinding;
import com.example.farfish.databinding.ToolbarConversationBinding;
import com.example.farfish.fragments.dialogs.InternetConnectionDialog;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ChatsFragment extends Fragment implements MessagesListAdapter.MessageClick, MessagingRepository.MessagingInterface {
    // max number of characters with a single message.
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    private static final String ORIENTATION_CHANGE = "orientation_change";
    @Inject
    public MessagesListAdapter messagesListAdapter;
    public MainViewModel mModel;
    // root class
    private ChatsFragmentBinding mBinding;
    private ToolbarConversationBinding mToolbarBinding;
    private String mUsername;
    // this tracker is used to invoke the method of the realtime database to update the user is writing once
    private int tracker = 0;

    private NavController navController;

    // toolbar values
    private String targetUserId;

    // for target user profile in detail
    private Bundle targetUserData;

    private String currentUserId;
    private String currentUserName;
    private String currentPhotoUrl;

    private RecyclerView.AdapterDataObserver mObserver;
    public boolean USER_EXPECT_TO_RETURN;
    private ActivityResultLauncher<String> pickPic = registerForActivityResult(
            new ActivityResultContracts.GetContent() {
                @NonNull
                @Override
                public Intent createIntent(@NonNull Context context, @NonNull String input) {
                    return super.createIntent(context, "image/*");// filter the gallery output, so the user can send a photo as they expects
                }
            },
            this::putIntoImage);
    /*real time permission*/
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    pickImageFromGallery();
                } else {
                    Toast.makeText(requireContext(), requireContext().getString(R.string.grant_access_media_permission), Toast.LENGTH_SHORT).show();
                }
            });

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
        setHasOptionsMenu(true);



        /*app UI functionality*/
        mUsername = MessagesPreference.getUserName(requireContext());

        targetUserData = getArguments();
        if (targetUserData != null) {
            targetUserId = targetUserData.getString("target_user_id", "id for target user");
            // rooms references
            currentUserId = MessagesPreference.getUserId(requireContext());

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
        mBinding.progressBar.setVisibility(View.VISIBLE);
        Log.d("TAG", "USER_EXPECT_TO_RETURN" + USER_EXPECT_TO_RETURN);
        View view = mBinding.getRoot();
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setStackFromEnd(true);
        mObserver = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                Log.d("TAG", "onItemRangeInserted");
                scrollToLastMessage(positionStart, layoutManager);
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
                super.onItemRangeChanged(positionStart, itemCount, payload);
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                Log.d("TAG", "background thread 3");
                boolean b = savedInstanceState != null || !USER_EXPECT_TO_RETURN;
                Log.d("TAG", "onItemRangeChanged user expect to return: " + USER_EXPECT_TO_RETURN + " result: " + b);
                if (savedInstanceState != null) {
                    scrollToLastMessage(mModel.messagingRepository.getMessages().size() - 1, layoutManager);
                    Log.d("TAG", "onItemRangeChanged executed");
                }

            }


        };
        USER_EXPECT_TO_RETURN = false;
        mBinding.messageRecyclerView.setLayoutManager(layoutManager);
        messagesListAdapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);
        Log.d("TAG", "policy is: " +  messagesListAdapter.getStateRestorationPolicy());
      /*  if (USER_EXPECT_TO_RETURN)
            populateToolbar();*/
      Log.d("TAG", "main thread 1");

        requireActivity().findViewById(R.id.bottom_nav).setVisibility(View.GONE);

        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        Toolbar tb = mBinding.toolbarFrag;
        ((AppCompatActivity) requireActivity()).setSupportActionBar(tb);
        mToolbarBinding = mBinding.toolbarTargetUserInfo;

        LinearLayout layout = view.findViewById(R.id.go_back);
        layout.setOnClickListener(v -> navController.navigateUp());

        mToolbarBinding.conversationToolbarUserInfo.setOnClickListener(targetUserLayoutListener -> {

            if (mModel.getMessagingRepository().getTargetUserData().getParcelable("user") == null)
                return;
            USER_EXPECT_TO_RETURN = true;
            navController.navigate(R.id.action_chatsFragment_to_userProfileFragment,
                    mModel.getMessagingRepository().getTargetUserData());

        });

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
            }
        });
        mBinding.messageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

        mBinding.sendButton.setOnClickListener(v -> {
            if (mModel.getMessagingRepository().getTargetUserData().getParcelable("user") == null)
            {
                Toast.makeText(requireContext(), getResources().getString(R.string.try_again), Toast.LENGTH_SHORT).show();
                return;
            }
            if (Connection.isUserConnected(requireContext())) {

                User targetUser = mModel.getMessagingRepository().getTargetUserData().getParcelable("user");
                long dateFromDateClass = new Date().getTime();
                String text = Objects.requireNonNull(mBinding.messageEditText.getText()).toString();
                Message currentUserMsg = new Message(text, "", dateFromDateClass, currentUserId, currentUserId,
                        mUsername, currentUserName, currentPhotoUrl, false);
                assert targetUser != null;
                Message targetUserMsg = new Message(text, "", dateFromDateClass, currentUserId, targetUserId,
                        mUsername, targetUser.getUserName(),
                        targetUser.getPhotoUrl(), false);
                mModel.getMessagingRepository().sendMessage(currentUserMsg, targetUserMsg);
                mBinding.messageEditText.setText("");
            } else {
                new InternetConnectionDialog().show(requireActivity().getSupportFragmentManager(), "internet_dialog");
            }

        });

        messagesListAdapter.registerAdapterDataObserver(mObserver);


        NavBackStackEntry backStackEntry = navController.getBackStackEntry(R.id.nav_graph);
        mModel = new ViewModelProvider(
                backStackEntry,
                HiltViewModelFactory.create(requireContext(), backStackEntry)
        ).get(MainViewModel.class);
        mModel.getMessagingRepository().setMessagingInterface(this);
        mModel.getMessagingRepository().setTargetUserId(targetUserId);
        mBinding.messageRecyclerView.setAdapter(messagesListAdapter);
        messagesListAdapter.setMessageInterface(this);
        messagesListAdapter.setIsGeneral(false);

        mModel.getChatMessages().observe(getViewLifecycleOwner(), chatMessages -> {
            messagesListAdapter.submitList(chatMessages);
            mBinding.progressBar.setVisibility(View.GONE);
        });
        if (mModel.getMessagingRepository().getMessages().size() == 0)
            mBinding.progressBar.setVisibility(View.GONE);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(ORIENTATION_CHANGE))
                populateToolbar();
        }

        mBinding.scrollBottomFab.setOnClickListener(scrollFabListener -> {
            mBinding.scrollBottomFab.hide();
            mBinding.messageRecyclerView.scrollToPosition(mModel.getMessagingRepository().getMessages().size() - 1);
        });
        mBinding.messageRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && mBinding.scrollBottomFab.getVisibility() == View.VISIBLE) {
                    mBinding.scrollBottomFab.hide();
                } else if (dy < 0 && mBinding.scrollBottomFab.getVisibility() != View.VISIBLE) {
                    mBinding.scrollBottomFab.show();
                }
            }
        });
        checkUserConnection();
        Log.d("TAG", "main thread 2");
        return view;
    }

    private void scrollToLastMessage(int positionStart, LinearLayoutManager layoutManager) {
        if (positionStart > 0) {
            layoutManager.scrollToPosition(positionStart);
        }
    }

    private void checkUserConnection() {
        if (!Connection.isUserConnected(requireContext())) {
            Snackbar.make(requireActivity().findViewById(R.id.bottom_nav), R.string.user_offline_msg, BaseTransientBottomBar.LENGTH_LONG)
                    .setAnchorView(R.id.bottom_nav).show();
        }
    }

    private void setUserIsNotWriting() {
        mModel.getMessagingRepository().setUserIsNotWriting();
    }

    private void setUserIsWriting() {
        if (tracker == 0) {
            mModel.getMessagingRepository().setUserIsWriting();
            tracker++;
        }
    }


    /*this method will update the chat info int the toolbar in real time!*/
    public void updateChatInfo() {
        if (mToolbarBinding == null) return;
        if (getContext() != null) {
            if (mModel.getMessagingRepository().isWriting()) {
                mToolbarBinding.chatLastSeen.setText(getResources().getString(R.string.isWriting));
                mToolbarBinding.chatLastSeen.setTextColor(getResources().getColor(R.color.colorAccent));
            } else if (mModel.getMessagingRepository().isActive()) {

                mToolbarBinding.chatLastSeen.setText(getResources().getString(R.string.online));
                mToolbarBinding.chatLastSeen.setTextColor(getResources().getColor(R.color.colorTitle));
            } else {
                long lastSeen = mModel.getMessagingRepository().getLastTimeSeen();
                SimpleDateFormat df = new SimpleDateFormat("EEE, MMM d", Locale.getDefault());
                String lastTimeSeenText = df.format(lastSeen);
                SimpleDateFormat df2 = new SimpleDateFormat("h:mm a", Locale.getDefault());
                String text2 = df2.format(lastSeen);
                String lastTimeSeenToDisplay = lastTimeSeenText + ", " + text2;
                mToolbarBinding.chatLastSeen.setText(lastTimeSeenToDisplay);
            }
        }
    }

    @Override
    public void refreshMessages() {
        mBinding.progressBar.setVisibility(View.GONE);
        mModel.updateMessages();
        if (mModel.getMessagingRepository().getMessages().size() <= 10) {
            mBinding.scrollBottomFab.hide();
        }

    }

    @Override
    public void refreshChatInfo() {
        updateChatInfo();
    }

    @Override
    public void populateToolbar() {
        if (mBinding != null) {
            User targetUser = mModel.getMessagingRepository().getTargetUserData().getParcelable("user");
            if (targetUser == null){
                Log.d("TAG", "target user data is null");
                return;
            }else
                Log.d("TAG", "target user data is not null targetName: " + targetUser.getUserName());
            mToolbarBinding.chatTitle.setText(targetUser.getUserName());
            Picasso.get().load(targetUser.getPhotoUrl()).placeholder(R.drawable.time_background).into(mToolbarBinding.chatConversationProfile);
            updateChatInfo();
        }
    }

    private void putIntoImage(Uri uri) {

        mBinding.progressBar.setVisibility(View.VISIBLE);
        if (uri != null) {
            mModel.getMessagingRepository().compressAndSendImage(uri);
        } else {
            Toast.makeText(requireContext(), requireContext().getString(R.string.cancel_sending_img), Toast.LENGTH_SHORT).show();
            mBinding.progressBar.setVisibility(View.GONE);
        }

    }


    // these overriding methods for debugging only and will be cleaned up in the future.
    @Override
    public void onDestroyView() {

        Log.d("TAG", "USER_EXPECT_TO_RETURN" + USER_EXPECT_TO_RETURN);
        if (!USER_EXPECT_TO_RETURN) {
            mModel.getMessagingRepository().removeListeners();
            messagesListAdapter.unregisterAdapterDataObserver(mObserver);
            mObserver = null;
            mModel.getMessagingRepository().getMessages().clear();
            // in the case the user is writing and move to another destination before sending their message.
            if (mModel.getMessagingRepository().isWriting())
                setUserIsNotWriting();
            // remove the listener when the view is no longer visible for the user
            mBinding = null;
            mToolbarBinding = null;
            targetUserData.clear();
        }
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        /*USER_EXPECT_TO_RETURN = true;*/
        outState.putBoolean(ORIENTATION_CHANGE, true);
    }

    private void pickImageFromGallery() {
        ConnectivityManager cm = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        InternetConnectionDialog internetDialog = new InternetConnectionDialog();
        if (!isConnected)
            internetDialog.show(requireActivity().getSupportFragmentManager(), "internet_alert");
        else pickPic.launch("image/*");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.profile:
                USER_EXPECT_TO_RETURN = true;
                Navigation.findNavController(mBinding.getRoot()).navigate(R.id.action_chatsFragment_to_userProfileFragment, mModel.getMessagingRepository().getTargetUserData());
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
        Toast.makeText(requireContext(), requireContext().getString(R.string.future_msg), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMessageClick(View view, int position) {
        Message message = mModel.getMessagingRepository().getMessageInPosition(position);
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
            navController.navigate(actionToFullImageFragment, extras);
        }
    }

}
