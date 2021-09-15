package com.example.friendlychat.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.friendlychat.Module.FileUtil;
import com.example.friendlychat.Module.MessagesPreference;
import com.example.friendlychat.Module.SharedPreferenceUtils;
import com.example.friendlychat.Module.User;
import com.example.friendlychat.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import id.zelory.compressor.Compressor;

public class ProfileImageFragment extends Fragment {
    private static final String TAG = ProfileImageFragment.class.getSimpleName();
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
    private ActivityResultLauncher<String> choosePicture = registerForActivityResult(
            new ActivityResultContracts.GetContent(){
                @NonNull
                @Override
                public Intent createIntent(@NonNull Context context, @NonNull String input) {
                    return super.createIntent(context, "image/*");// filter the gallery output, so the user can send a photo as they expects
                }
            },
            this::putIntoImage);
    private String userId, userName, photoUrl, phoneNumber;
    private NavController controller;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_image_fragment, container, false);
        controller = Navigation.findNavController(view);
        Bundle userData = getArguments();
        if (userData != null) {
            userId = userData.getString("userId");
            userName = userData.getString("userName");
        }
        ImageView image = view.findViewById(R.id.registerImage);
        image.setOnClickListener( imageListener -> {

        });
        EditText phoneNumberEditText = view.findViewById(R.id.profileImagePhoneNumber);
        Button continueButton = view.findViewById(R.id.continueButton);
        continueButton.setOnClickListener(continueButtonListener -> {
            phoneNumber = phoneNumberEditText.getText().toString();
            if (photoUrl == null || photoUrl.equals(""))
                Toast.makeText(requireActivity(), "Please insert an image to continue", Toast.LENGTH_SHORT).show();
            else
                saveUserDataAndNavigateToHomeScreen();
        });

        return view;
    }

    private void saveUserDataAndNavigateToHomeScreen() {
        /*save user data to be used later*/
        MessagesPreference.saveUserPhotoUrl(requireContext(), photoUrl);
        MessagesPreference.saveUserId(requireContext(), userId);
        MessagesPreference.saveUserName(requireContext(), userName);
        /*create a new user*/
        User newUser = new User(userName, phoneNumber, photoUrl, userId, true, new Date().getTime());
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("rooms").document(userId).set(newUser).addOnSuccessListener( result -> {
            Log.d(TAG, "saveUserDataAndNavigateToHomeScreen: successfully register new user");
            SharedPreferenceUtils.saveUserSignIn(requireContext());
            controller.popBackStack(); // return to home screen

        }).addOnFailureListener(exc -> {
            Log.d(TAG, "saveUserDataAndNavigateToHomeScreen: exception: " + exc.getMessage());
        });

    }

    private void pickImageFromGallery() {
        choosePicture.launch("image/*");
    }

    private void putIntoImage(Uri uri) {

        if (uri != null) {
            try {
                File galleryFile = FileUtil.from(requireContext(), uri);
                /*compress the file using a special library*/
                File compressedImageFile = new Compressor(requireContext()).compressToFile(galleryFile);
                /*take the file name as a unique identifier*/
                StorageReference profiles = FirebaseStorage.getInstance().getReference("profiles");
                StorageReference imageRef = profiles.child(compressedImageFile.getName());
                // finally uploading the file to firebase storage.
                UploadTask uploadTask = imageRef.putFile(Uri.fromFile(compressedImageFile));
                // Register observers to listen for when the download is done or if it fails
                uploadTask.addOnFailureListener(exception -> {
                    // Handle unsuccessful uploads
                    Log.d(TAG, "putIntoImage: exc msg: " + exception.getMessage());
                }).addOnSuccessListener(taskSnapshot -> {
                    Log.d(TAG, "putIntoImage: Successfully upload image to firestore");
                    imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        Log.d(TAG, downloadUri.toString());
                        Log.d(TAG, String.valueOf(downloadUri));
                        photoUrl = downloadUri.toString();

                    });

                });

            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "Error compressing the file");
                Toast.makeText(requireContext(), "Error occurs", Toast.LENGTH_SHORT).show();
            }
            // if the user hit the back button before choosing an image to send the code below will be executed.
        } else {
            Toast.makeText(requireContext(), "image operation canceled", Toast.LENGTH_SHORT).show();
        }
    }
}
