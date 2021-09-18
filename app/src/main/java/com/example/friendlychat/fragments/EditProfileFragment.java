package com.example.friendlychat.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.friendlychat.Module.FileUtil;
import com.example.friendlychat.Module.MessagesPreference;
import com.example.friendlychat.R;
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
                    Toast.makeText(requireContext(), "Ok, if you need to send images please grant the requested permission", Toast.LENGTH_SHORT).show();
                }
            });
    private ActivityResultLauncher<String> choosePicture = registerForActivityResult(
            new ActivityResultContracts.GetContent(){
                @NonNull
                @Override
                public Intent createIntent(@NonNull Context context, @NonNull String input) {
                    return super.createIntent(context, "image/*");// filter the gallery output, so the user can send a photo as they expects
                }
            },
            this::putIntoImage);

    private Uri imageUri;
    private ImageView profile;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_profile_fragment, container, false);
        profile = view.findViewById(R.id.editProfileImageVIew);
        EditText userNameEditText = view.findViewById(R.id.editProfileEditTextUserName);
        EditText statusEditText = view.findViewById(R.id.editProfileEditTextStatus);
        EditText phoneNumberEditText = view.findViewById(R.id.editProfilePhoneNumber);
        Button save = view.findViewById(R.id.editProfileSaveButton);
        Bundle userData = getArguments();

        if (userData != null) {
            photoUrl = userData.getString("photo_url");
            userName = userData.getString("user_name");
            userStatus = userData.getString("user_status");
            userPhoneNumber = userData.getString("phone_number");
            // populate the UI
            Picasso.get().load(photoUrl).placeholder(R.drawable.ic_round_person_24)
                    .into(profile);
            userNameEditText.setText(userName, TextView.BufferType.EDITABLE);
            statusEditText.setText(userStatus, TextView.BufferType.EDITABLE);
            phoneNumberEditText.setText(userPhoneNumber, TextView.BufferType.EDITABLE);
        }

        // invoke listeners
        profile.setOnClickListener(profileImageListener -> {
            if (ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED) {
                pickImageFromGallery();
            } else {
                requestPermissionLauncher.launch(
                        Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        });
        save.setOnClickListener(saveListener -> {
            // firstly save the image if the user choose a new one

            List<Map> fieldsToUpdate = new ArrayList<>();
            userNameAfterClick = userNameEditText.getText().toString();
            userStatusAfterClick = statusEditText.getText().toString();
            userPhoneNumberAfterClick = phoneNumberEditText.getText().toString();
            Log.d(TAG, "onCreateView: imageUrl: " + imageUri);
            if (imageUri != null){
                checkIfUserSelectTheSameImageAndContinueIfNot();
            }
            if (!userName.equals(userNameAfterClick)){
                Map<String, String> field = new HashMap<>();
                field.put("userName", userNameAfterClick);
                fieldsToUpdate.add(field);
            }
            if (!userStatus.equals(statusEditText.getText().toString())){
                Map<String, String> field = new HashMap<>();
                field.put("status", userStatusAfterClick);
                fieldsToUpdate.add(field);
            }
            if (!userPhoneNumber.equals(phoneNumberEditText.getText().toString())){
                Map<String, String> field = new HashMap<>();
                field.put("phoneNumber", userPhoneNumberAfterClick);
                fieldsToUpdate.add(field);
            }

            if (fieldsToUpdate.size() == 0 && imageUri == null){
                // there's no any change happened
                Toast.makeText(requireActivity(), "There is no any change to be updated", Toast.LENGTH_SHORT).show();
            }else {

                DocumentReference documentReference =
                        mFirestore.collection("rooms").document(MessagesPreference.getUserId(requireContext()));
                String[] keys = {"userName", "status", "phoneNumber"};
                for (Map field : fieldsToUpdate)
                {
                    for (String key : keys){
                        if (field.containsKey(key)){
                           documentReference.update(key, field.get(key));
                           updateLocalUserData(key, Objects.requireNonNull(field.get(key)).toString());
                        }
                    }
                }
            }
        });
        return view;
    }

    private void updateLocalUserData(String key, String data) {
        Context context = requireContext();
        switch (key) {
            case "userName":
                MessagesPreference.saveUserName(context, data);
                break;
            case "status":
                MessagesPreference.saveUserStatus(context, data);
                break;
            case "phoneNumber":
                MessagesPreference.saveUserPhoneNumber(context, data);
                break;
            case "photoUrl":
                MessagesPreference.saveUserPhotoUrl(context, data);
                break;
        }
    }

    private void checkIfUserSelectTheSameImageAndContinueIfNot() {
        // check if the usr selected the same image
        FirebaseStorage storage = FirebaseStorage.getInstance();
        String imageNameInTheServer = storage.getReferenceFromUrl(photoUrl).getName();
        String imageFromGalleryName = "";
        File compressedFile = null;
        // compress the image from the gallery to get its name
        try {
            File galleryFile = FileUtil.from(requireContext(), imageUri);
            /*compress the file using a special library*/
            File compressedImageFile = new Compressor(requireContext()).compressToFile(galleryFile);
            compressedFile = compressedImageFile;
            imageFromGalleryName = compressedImageFile.getName();
        }catch (IOException exc){
            Log.d(TAG, "checkIfTheUserSeletectTheSameImage: " + exc.getMessage());
        }
        // if there are not the same prepare the downloadable url
        if (!imageFromGalleryName.equals(imageNameInTheServer)){
            uploadImageToServer(compressedFile);

            // delete the old profile
            assert compressedFile != null;
            deleteOldProfile(compressedFile.getName());
            Log.d(TAG, "checkIfUserSelectTheSameImageAndContinueIfNot: photo to be updated" + photoUrlToBeUpdated);
        }

    }

    private void deleteOldProfile(String profileRefToBeDeleted) {
        mStorage.getReference("profiles")
                .child(profileRefToBeDeleted).delete().addOnSuccessListener(deleteListener -> Log.d(TAG, "deleteOldProfile: deleted old password successfully")).addOnFailureListener(faliureListener -> Log.d(TAG, "deleteOldProfile: " + faliureListener.getMessage()));
    }

    private void uploadImageToServer(File compressedFile) {
        if (compressedFile != null) {
            StorageReference profiles = mStorage.getReference("profiles");
            StorageReference imageReference = profiles.child(compressedFile.toString());
            UploadTask uploadTask = imageReference.putFile(Uri.fromFile(compressedFile));
            uploadTask.addOnSuccessListener(successListener -> imageReference.getDownloadUrl().addOnSuccessListener(downloadUrlSuccessListener -> {
               String newProfileUrl  = downloadUrlSuccessListener.toString();
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
                .addOnSuccessListener(sListner -> Log.d(TAG, "saveDataInFirestore: update new profile image successfully")).
                addOnFailureListener(fListener -> Log.d(TAG, "saveDataInFirestore: " + fListener.getMessage()));
    }

    private void pickImageFromGallery() {
        choosePicture.launch("image/*");
    }

    private void putIntoImage(Uri uri) {
        imageUri = uri;
        if (imageUri != null) {
            profile.setImageURI(imageUri);
        }else {
            Toast.makeText(requireActivity(), "operation canceled", Toast.LENGTH_SHORT).show();
        }
    }
}
