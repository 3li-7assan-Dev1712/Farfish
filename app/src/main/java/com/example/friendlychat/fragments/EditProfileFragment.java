package com.example.friendlychat.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.friendlychat.Module.FileUtil;
import com.example.friendlychat.Module.MessagesPreference;
import com.example.friendlychat.R;
import com.example.friendlychat.databinding.EditProfileFragmentBinding;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import id.zelory.compressor.Compressor;

public class EditProfileFragment extends Fragment {

    private FirebaseStorage mStorage = FirebaseStorage.getInstance();
    private static final String TAG = EditProfileFragment.class.getSimpleName();
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    pickImageFromGallery();
                } else {
                    Toast.makeText(requireContext(), requireContext().getString(R.string.grant_access_media_permission),Toast.LENGTH_SHORT).show();
                }
            });
    private ActivityResultLauncher<String> choosePicture = registerForActivityResult(
            new ActivityResultContracts.GetContent() {
                @NonNull
                @Override
                public Intent createIntent(@NonNull Context context, @NonNull String input) {
                    return super.createIntent(context, "image/*");// filter the gallery output, so the user can send a photo as they expects
                }
            },
            this::putIntoImage);

    private Uri imageUri;
    private String photoUrl;
    private String userName;
    private String userStatus;
    private String userPhoneNumber;

    // to be updated
    private String photoUrlToBeUpdated = null;

    // value after click
    private String userNameAfterClick;
    private String userStatusAfterClick;
    private String userPhoneNumberAfterClick;

    FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();

    private boolean mOriginalUserPrivacyState; // the one which saved shared preferences and the server
    private boolean mDynamicPrivacyState; // this will be changed every tiem the user click on the two button above save button

    private EditProfileFragmentBinding mBinding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = EditProfileFragmentBinding.inflate(inflater, container, false);
        View view = mBinding.getRoot();
        mBinding.toolbarEditProfile.setNavigationOnClickListener(navigationClickListener ->
                Navigation.findNavController(view).navigateUp()
        );

        Bundle userData = getArguments();

        if (userData != null) {
            photoUrl = userData.getString("photo_url");
            userName = userData.getString("user_name");
            userStatus = userData.getString("user_status");
            userPhoneNumber = userData.getString("phone_number");
            // populate the UI
            Picasso.get().load(photoUrl).placeholder(R.drawable.ic_round_person_24)
                    .into(mBinding.editProfileImageVIew);
            mBinding.editProfileEditTextUserName.setText(userName, TextView.BufferType.EDITABLE);
            mBinding.editProfileEditTextStatus.setText(userStatus, TextView.BufferType.EDITABLE);
            mBinding.editProfilePhoneNumber.setText(userPhoneNumber, TextView.BufferType.EDITABLE);
        }

        mOriginalUserPrivacyState = MessagesPreference.userIsPublic(requireContext());
        mDynamicPrivacyState = mOriginalUserPrivacyState;
        updateButtonBackground(mOriginalUserPrivacyState);
        mBinding.privateButton.setOnClickListener(privateListener -> {
            mDynamicPrivacyState = false;
            updateButtonBackground(mDynamicPrivacyState);
        });
        mBinding.publicButton.setOnClickListener(publicListener -> {
            mDynamicPrivacyState = true;
            updateButtonBackground(mDynamicPrivacyState);
        });
        // connection check
        ConnectivityManager cm = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        InternetConnectionDialog internetDialog = new InternetConnectionDialog();
        // invoke listeners
        mBinding.editProfileImageVIew.setOnClickListener(profileImageListener -> {
            if (ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED) {
                pickImageFromGallery();
            } else {
                requestPermissionLauncher.launch(
                        Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        });
        mBinding.editProfileSaveButton.setOnClickListener(saveListener -> {
            if (!isConnected) {
                internetDialog.show(requireActivity().getSupportFragmentManager(), "internet_alert");
                return;
            }
            mBinding.editProfileHorizontalProgressBar.setVisibility(View.VISIBLE);
            // firstly save the image if the user choose a new one

            List<Map> fieldsToUpdate = new ArrayList<>();
            userNameAfterClick = mBinding.editProfileEditTextUserName.getText().toString();
            userStatusAfterClick = mBinding.editProfileEditTextStatus.getText().toString();
            userPhoneNumberAfterClick = mBinding.editProfilePhoneNumber.getText().toString();
            Log.d(TAG, "onCreateView: imageUrl: " + imageUri);
            if (imageUri != null) {
                checkIfUserSelectTheSameImageAndContinueIfNot();
            }
            if (!userName.equals(userNameAfterClick)) {
                Map<String, String> field = new HashMap<>();
                field.put("userName", userNameAfterClick);
                fieldsToUpdate.add(field);
            }
            if (!userStatus.equals(mBinding.editProfileEditTextStatus.getText().toString())) {
                Map<String, String> field = new HashMap<>();
                field.put("status", userStatusAfterClick);
                fieldsToUpdate.add(field);
            }
            if (!userPhoneNumber.equals(mBinding.editProfilePhoneNumber.getText().toString())) {
                Map<String, String> field = new HashMap<>();
                field.put("phoneNumber", userPhoneNumberAfterClick);
                fieldsToUpdate.add(field);
            }

            if (mDynamicPrivacyState != mOriginalUserPrivacyState) {
                Map<String, Boolean> field = new HashMap<>();
                field.put("isPublic", mDynamicPrivacyState);
                fieldsToUpdate.add(field);
            }
            if (fieldsToUpdate.size() == 0 && imageUri == null) {
                // there's no any change happened
                mBinding.editProfileHorizontalProgressBar.setVisibility(View.GONE);
                Toast.makeText(requireActivity(), requireContext().getString(R.string.no_update_happened_msg), Toast.LENGTH_SHORT).show();
            } else {

                DocumentReference documentReference =
                        mFirestore.collection("rooms").document(MessagesPreference.getUserId(requireContext()));
                String[] keys = {"userName", "status", "phoneNumber", "isPublic"};
                for (int i = 0; i < fieldsToUpdate.size(); i++) {
                    Map field = fieldsToUpdate.get(i);
                    for (String key : keys) {
                        if (field.containsKey(key)) {
                            int finalI = i;
                            documentReference.update(key, field.get(key)).addOnSuccessListener(sl -> {
                                // check if the this is the last updated item in  the list to hide the progress bar and show the result
                                if (finalI == fieldsToUpdate.size() - 1 && imageUri == null) {
                                    mBinding.editProfileHorizontalProgressBar.setVisibility(View.GONE);
                                    showSnackBar();
                                }
                            });
                            if (key.equals("isPublic")) {
                                MessagesPreference.saveUserPrivacy(requireContext(), mDynamicPrivacyState);
                                // update the original value to prevent overriding the same value
                                mOriginalUserPrivacyState = mDynamicPrivacyState;
                            } else
                                updateLocalUserData(key, Objects.requireNonNull(field.get(key)).toString());
                        }
                    }
                }
            }
        });

        return view;
    }

    private void updateButtonBackground(boolean userIsPublic) {
        if (userIsPublic) {
            mBinding.publicButton.setBackgroundColor(requireContext().getResources().getColor(R.color.red));
            mBinding.privateButton.setBackgroundColor(requireContext().getResources().getColor(R.color.darkerGray));
        } else {
            mBinding.publicButton.setBackgroundColor(requireContext().getResources().getColor(R.color.darkerGray));
            mBinding.privateButton.setBackgroundColor(requireContext().getResources().getColor(R.color.red));
        }
    }

    private void updateLocalUserData(String key, String data) {
        Context context = requireContext();
        switch (key) {
            case "userName":
                MessagesPreference.saveUserName(context, data);
                userName = data;
                break;
            case "status":
                MessagesPreference.saveUserStatus(context, data);
                userStatus = data;
                break;
            case "phoneNumber":
                MessagesPreference.saveUserPhoneNumber(context, data);
                userPhoneNumber = data;
                break;
            case "photoUrl":
                MessagesPreference.saveUserPhotoUrl(context, data);
                photoUrl = data;
                break;

        }
    }

    private void checkIfUserSelectTheSameImageAndContinueIfNot() {
        // check if the usr selected the same image
        FirebaseStorage storage = FirebaseStorage.getInstance();

        String imageNameInTheServer = "";
        StorageReference oldProfileReference = null;
        try {
            imageNameInTheServer = storage.getReferenceFromUrl(photoUrl).getName();
            oldProfileReference = storage.getReferenceFromUrl(photoUrl);
            Log.d(TAG, "checkIfUserSelectTheSameImageAndContinueIfNot: image int the server is: " + imageNameInTheServer);
        } catch (Exception e) {
            Log.d(TAG, "checkIfUserSelectTheSameImageAndContinueIfNot: " + e.getMessage());
        }
        String imageFromGalleryName = "";
        File compressedFile = null;
        // compress the image from the gallery to get its name
        try {
            File galleryFile = FileUtil.from(requireContext(), imageUri);
            /*compress the file using a special library*/
            File compressedImageFile = new Compressor(requireContext()).compressToFile(galleryFile);
            compressedFile = compressedImageFile;
            imageFromGalleryName = compressedImageFile.getName();
        } catch (IOException exc) {
            Log.d(TAG, "checkIfTheUserSeletectTheSameImage: " + exc.getMessage());
        }
        // if there are not the same prepare the downloadable url
        if (!imageFromGalleryName.equals(imageNameInTheServer)) {
            uploadImageToServer(compressedFile);

            // delete the old profile
            if (!imageNameInTheServer.equals("") && oldProfileReference != null)
                deleteOldProfile(oldProfileReference);
        } else
            Log.d(TAG, "checkIfUserSelectTheSameImageAndContinueIfNot: the user seletect the same image");

    }

    private void deleteOldProfile(StorageReference profileRefToBeDeleted) {
        profileRefToBeDeleted.delete().addOnSuccessListener(s -> Log.d(TAG, "deleteOldProfile: old profile deleted successfully")).addOnFailureListener(f -> Log.d(TAG, "deleteOldProfile: " + f.getMessage()));
    }

    private void uploadImageToServer(File compressedFile) {
        if (compressedFile != null) {
            StorageReference profiles = mStorage.getReference("profiles");
            StorageReference imageReference = profiles.child(compressedFile.toString());
            UploadTask uploadTask = imageReference.putFile(Uri.fromFile(compressedFile));
            uploadTask.addOnSuccessListener(successListener -> imageReference.getDownloadUrl().addOnSuccessListener(downloadUrlSuccessListener -> {
                String newProfileUrl = downloadUrlSuccessListener.toString();
                saveDataInFirestore(newProfileUrl);
                Log.d(TAG, "uploadImageToServer: " + photoUrlToBeUpdated);
            })).addOnFailureListener(failureListener -> Log.d(TAG, "uploadImageToServer: " + failureListener.getMessage()));
        }

    }

    private void saveDataInFirestore(String newProfileUrl) {
        updateLocalUserData("photoUrl", newProfileUrl);
        Log.d(TAG, "saveDataInFirestore: " + newProfileUrl);
        mFirestore.collection("rooms")
                .document(MessagesPreference.getUserId(requireContext()))
                .update("photoUrl", newProfileUrl)
                .addOnSuccessListener(sListner -> {
                            Log.d(TAG, "saveDataInFirestore: update new profile image successfully");
                            mBinding.editProfileHorizontalProgressBar.setVisibility(View.GONE);
                            showSnackBar();
                            imageUri = null;
                        }
                ).
                addOnFailureListener(fListener -> Log.d(TAG, "saveDataInFirestore: " + fListener.getMessage()));
    }

    private void showSnackBar() {
        Snackbar.make(mBinding.getRoot(), R.string.profile_updated_successully, BaseTransientBottomBar.LENGTH_SHORT)
                .setAction(R.string.ok, sbl -> {

                }).show();
    }

    private void pickImageFromGallery() {
        choosePicture.launch("image/*");
    }

    private void putIntoImage(Uri uri) {
        imageUri = uri;
        if (imageUri != null) {
            Picasso.get().load(imageUri).fit().centerCrop().into(mBinding.editProfileImageVIew);
        } else {
            Toast.makeText(requireActivity(), requireContext().getString(R.string.grant_access_media_permission), Toast.LENGTH_SHORT).show();
        }
    }
}
