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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.friendlychat.Module.FileUtil;
import com.example.friendlychat.Module.MessagesPreference;
import com.example.friendlychat.Module.SharedPreferenceUtils;
import com.example.friendlychat.Module.User;
import com.example.friendlychat.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthEmailException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;

import id.zelory.compressor.Compressor;

public class ProfileImageFragment extends Fragment {
    private Uri imageUriFromGallery;
    private ProgressBar mProgressBar;
    private FrameLayout mBorder;
    private EditText mStatusEditText;
    private static final String TAG = ProfileImageFragment.class.getSimpleName();
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
    private ImageView mImageView;
    private String userId, userName, photoUrl, phoneNumber, email, password;
    private View snackBarView;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_image_fragment, container, false);
        snackBarView = view;
        mBorder = view.findViewById(R.id.profileImageFrameLayout);
        mProgressBar = view.findViewById(R.id.progressBarProfileImage);
        mStatusEditText = view.findViewById(R.id.editTextStatus);
        Bundle userData = getArguments();
        if (userData != null) {
            userName = userData.getString("userName");
            email = userData.getString("email");
            password = userData.getString("password");
        }
        mImageView = view.findViewById(R.id.registerImage);
        mImageView.setOnClickListener( imageListener -> {
            if (ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED) {
                pickImageFromGallery();
            } else {
                requestPermissionLauncher.launch(
                        Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        });
        EditText phoneNumberEditText = view.findViewById(R.id.profileImagePhoneNumber);
        Button continueButton = view.findViewById(R.id.continueButton);
        continueButton.setOnClickListener(continueButtonListener -> {
            phoneNumber = phoneNumberEditText.getText().toString();
            if (imageUriFromGallery == null)
                Toast.makeText(requireActivity(), "Please insert an image to continue", Toast.LENGTH_SHORT).show();
            else {
                mProgressBar.setVisibility(View.VISIBLE);
                saveUserDataAndNavigateToHomeScreen();
            }
        });

        return view;
    }

    private void saveUserDataAndNavigateToHomeScreen() {
        if (email != null) {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(result -> {
                userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();
                compressImageAndStoreInStorage(imageUriFromGallery);
            }).addOnFailureListener(exception -> {
                Log.d(TAG, "saveUserDataAndNavigateToHomeScreen: exception: " + exception.getMessage());
                Log.d(TAG, "saveUserDataAndNavigateToHomeScreen: Error in authenticating new user");
                try{
                    throw exception;
                }catch (FirebaseAuthEmailException emailException){
                    showSnackBarWithAction(R.string.wrong_email, emailException);
                }catch (FirebaseAuthWeakPasswordException weakPasswordException){
                    showSnackBarWithAction(R.string.weak_password, weakPasswordException);
                }catch (FirebaseAuthUserCollisionException collisionException){
                    showSnackBarWithAction(R.string.already_registered, collisionException);
                }catch (Exception generalException){
                    Toast.makeText(requireActivity(), generalException.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "saveUserDataAndNavigateToHomeScreen: general exception: " + generalException.getMessage());

                }
            });
        }else
            Toast.makeText(requireActivity(), "you email is null", Toast.LENGTH_SHORT).show();

    }

    private void pickImageFromGallery() {
        choosePicture.launch("image/*");
    }

    private void putIntoImage(Uri uri) {
        imageUriFromGallery = uri;
        if (imageUriFromGallery != null) {
            mBorder.setBackground(requireContext().getResources().getDrawable(R.drawable.circle_background));
            mImageView.setImageURI(imageUriFromGallery);
        }
    }

    private void compressImageAndStoreInStorage(Uri uri) {
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
                    saveFinalData();
                });
            });

        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "Error compressing the file");
            Toast.makeText(requireContext(), "Error occurs", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    private void saveFinalData() {
        Log.d(TAG, "saveFinalData: photoUrl" + photoUrl);
        String status = mStatusEditText.getText().toString();
        /*save user data to be used later*/
        MessagesPreference.saveUserStatus(requireContext(), status);
        MessagesPreference.saveUserPhotoUrl(requireContext(), photoUrl);
        MessagesPreference.saveUserId(requireContext(), userId);
        MessagesPreference.saveUserName(requireContext(), userName);
        /*create a new user*/
        User newUser = new User(userName, phoneNumber, photoUrl, userId, status,true, new Date().getTime());
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("rooms").document(userId).set(newUser).addOnSuccessListener(data -> {
            Log.d(TAG, "saveUserDataAndNavigateToHomeScreen: successfully register new user");
            SharedPreferenceUtils.saveUserSignIn(requireContext());
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).popBackStack(R.id.userChatsFragment, false); // return to home screen
        }).addOnFailureListener(exc -> {
            Log.d(TAG, "saveUserDataAndNavigateToHomeScreen: exception: " + exc.getMessage());
            Log.d(TAG, "saveUserDataAndNavigateToHomeScreen: user authenticated successfully, the error in firestore");
        });
    }


    private void showSnackBarWithAction(int label, Exception exception){
        Snackbar snackbar = Snackbar.make(snackBarView, label, Snackbar.LENGTH_INDEFINITE);
        int action = R.string.return_fix;
        NavController controller = Navigation.findNavController(snackBarView);
        if (exception != null) {
            if (exception instanceof FirebaseAuthEmailException) {
                snackbar.setAction(action, snackbarListener -> {
                    controller.navigateUp();
                });
            } else if (exception instanceof FirebaseAuthWeakPasswordException) {
                snackbar.setAction(action, snackbarListener -> {
                    controller.navigateUp();
                });
            }else if (exception instanceof FirebaseAuthUserCollisionException){
                action = R.string.sign_in;
               snackbar.setAction(action, snackbarListener -> {
                   controller.popBackStack(R.id.fragmentSignIn, false);
               });
            }
        }
        snackbar.show();
    }
}
