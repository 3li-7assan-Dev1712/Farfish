package com.example.farfish.fragments.login;

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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.farfish.Module.FileUtil;
import com.example.farfish.Module.MessagesPreference;
import com.example.farfish.Module.SharedPreferenceUtils;
import com.example.farfish.Module.User;
import com.example.farfish.R;
import com.example.farfish.databinding.ProfileImageFragmentBinding;
import com.example.farfish.fragments.dialogs.InternetConnectionDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthEmailException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;

import id.zelory.compressor.Compressor;

public class ProfileImageFragment extends Fragment {
    private Uri imageUriFromGallery;
    private ProfileImageFragmentBinding mBinding;
    private static final String TAG = ProfileImageFragment.class.getSimpleName();
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    pickImageFromGallery();
                } else {
                    Toast.makeText(requireContext(), getString(R.string.grant_access_media_permission), Toast.LENGTH_SHORT).show();
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

    private String userId, userName, photoUrl, phoneNumber, email, password;
    private View snackBarView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = ProfileImageFragmentBinding.inflate(inflater, container, false);
        View view = mBinding.getRoot();
        snackBarView = view;
        Bundle userData = getArguments();
        if (userData != null) {
            userName = userData.getString("userName");
            email = userData.getString("email");
            password = userData.getString("password");
        }
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("imageUri")) {
                imageUriFromGallery = savedInstanceState.getParcelable("imageUri");
                setImageAndBackground(imageUriFromGallery);
            }
        }
        mBinding.registerImage.setOnClickListener(imageListener -> {
            if (ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED) {
                pickImageFromGallery();
            } else {
                requestPermissionLauncher.launch(
                        Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        });
        // for checking internet connection
        ConnectivityManager cm = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        InternetConnectionDialog internetDialog = new InternetConnectionDialog();
        mBinding.continueButton.setOnClickListener(continueButtonListener -> {
            phoneNumber = mBinding.profileImagePhoneNumber.getText().toString();
            if (imageUriFromGallery == null)
                Toast.makeText(requireActivity(), getString(R.string.img_required_msg), Toast.LENGTH_SHORT).show();
            else if (phoneNumber.equals(""))
                Toast.makeText(requireContext(), getString(R.string.phone_number_required_msg), Toast.LENGTH_SHORT).show();
            else if (phoneNumber.charAt(0) != 0 && phoneNumber.length() != 10)
                Toast.makeText(requireContext(), getString(R.string.phone_number_required_msg), Toast.LENGTH_SHORT).show();
            else if (!isConnected)
                internetDialog.show(requireActivity().getSupportFragmentManager(), "internet_alert");
            else {
                mBinding.progressBarProfileImage.setVisibility(View.VISIBLE);
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
                try {
                    throw exception;
                } catch (FirebaseAuthEmailException emailException) {
                    showSnackBarWithAction(R.string.wrong_email, emailException);
                } catch (FirebaseAuthWeakPasswordException weakPasswordException) {
                    showSnackBarWithAction(R.string.weak_password, weakPasswordException);
                } catch (FirebaseAuthUserCollisionException collisionException) {
                    showSnackBarWithAction(R.string.already_registered, collisionException);
                } catch (Exception generalException) {
                    Toast.makeText(requireActivity(), generalException.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "saveUserDataAndNavigateToHomeScreen: general exception: " + generalException.getMessage());
                    showSnackBarWithAction(R.string.wrong_email, generalException);
                }
            });
        }


    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (imageUriFromGallery != null)
            outState.putParcelable("imageUri", imageUriFromGallery);
    }

    private void pickImageFromGallery() {
        choosePicture.launch("image/*");
    }

    private void putIntoImage(Uri uri) {
        imageUriFromGallery = uri;
        if (imageUriFromGallery != null) {
            setImageAndBackground(uri);

        }
    }

    private void setImageAndBackground(Uri uri) {
        mBinding.profileImageFrameLayout.setBackground(requireContext().getResources().getDrawable(R.drawable.circle_background));
        Picasso.get().load(uri).fit().centerCrop().into(mBinding.registerImage);
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
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void saveFinalData() {
        Log.d(TAG, "saveFinalData: photoUrl" + photoUrl);
        String status = Objects.requireNonNull(mBinding.editTextStatus.getText()).toString();
        /*save user data to be used later*/
        MessagesPreference.saveUserStatus(requireContext(), status);
        MessagesPreference.saveUserPhotoUrl(requireContext(), photoUrl);
        MessagesPreference.saveUserId(requireContext(), userId);
        MessagesPreference.saveUserName(requireContext(), userName);
        MessagesPreference.saveUserPhoneNumber(requireContext(), phoneNumber);
        MessagesPreference.saveUserPrivacy(requireContext(), false);
        /*create a new user*/
        User newUser = new User(userName, email, phoneNumber, photoUrl, userId, status, true, false, new Date().getTime());
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


    private void showSnackBarWithAction(int label, Exception exception) {
        mBinding.progressBarProfileImage.setVisibility(View.GONE);
        Snackbar snackbar = Snackbar.make(snackBarView, label, Snackbar.LENGTH_INDEFINITE);
        int action = R.string.return_fix;
        NavController controller = Navigation.findNavController(snackBarView);
        if (exception != null) {
            if (exception instanceof FirebaseAuthEmailException) {
                snackbar.setAction(action, snackbarListener -> controller.navigateUp());
            } else if (exception instanceof FirebaseAuthWeakPasswordException) {
                snackbar.setAction(action, snackbarListener -> controller.navigateUp());
            } else if (exception instanceof FirebaseAuthUserCollisionException) {
                action = R.string.sign_in;
                snackbar.setAction(action, snackbarListener -> controller.popBackStack(R.id.fragmentSignIn, false));
            } else {
                snackbar.setAction(action, snackbarListener -> controller.navigateUp());
            }
        }
        snackbar.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}
