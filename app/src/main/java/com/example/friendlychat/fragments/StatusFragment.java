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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.friendlychat.Module.FileUtil;
import com.example.friendlychat.Module.MessagesPreference;
import com.example.friendlychat.Module.Status;
import com.example.friendlychat.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;

import id.zelory.compressor.Compressor;

public class StatusFragment extends Fragment {

    private DatabaseReference mDatabaseReference  = FirebaseDatabase.getInstance().getReference("status");
    private DatabaseReference mUserReference  = FirebaseDatabase.getInstance().getReference();
    private StorageReference mRootRef;
    /* request permission*/
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
    private ActivityResultLauncher<String> selectImageToUpload = registerForActivityResult(
            new ActivityResultContracts.GetContent(){
                @NonNull
                @Override
                public Intent createIntent(@NonNull Context context, @NonNull String input) {
                    return super.createIntent(context, "image/*");// filter the gallery output, so the user can send a photo as they expects
                }
            },
            this::putIntoImage);
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.status_fragment, container, false);
        FloatingActionButton uploadImageFab = view.findViewById(R.id.uploadImageStatusFab);
        uploadImageFab.setOnClickListener( uploadImage -> {

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
        FloatingActionButton uploadTextFab = view.findViewById(R.id.uploadTextStatusFab);
        uploadTextFab.setOnClickListener( uploadImage -> {
            Toast.makeText(requireContext(), "Upload text as a status", Toast.LENGTH_SHORT).show();
        });
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mRootRef = FirebaseStorage.getInstance().getReference("images");
        mUserReference = mDatabaseReference.child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
        super.onCreate(savedInstanceState);
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

                 // Register observers to listen for when the download is done or if it fails
                uploadTask.addOnFailureListener(exception -> {
                    // Handle unsuccessful uploads
                    Toast.makeText(requireContext(), "failed to set the image please try again later", Toast.LENGTH_SHORT).show();
                }).addOnSuccessListener(taskSnapshot -> {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    // ...
                    Toast.makeText(getContext(), "Uploaded the image to storage successfully", Toast.LENGTH_SHORT).show();
                    imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        String downloadUrl = downloadUri.toString();
                        long dateFromDateClass = new Date().getTime();
                         /* if the image sent successfully to the firebase storage send its metadata as a message
                         to the firebase firestore */
                         String uploaderName = MessagesPreference.getUserName(requireContext());
                        Status newStatus = new Status(uploaderName, downloadUrl, "", dateFromDateClass, 0);
                        uploadNewStatus(newStatus);
                    });

                });

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), "Error occurs", Toast.LENGTH_SHORT).show();
            }
            // if the user hit the back button before choosing an image to send the code below will be executed.
        }else{
            Toast.makeText(requireContext(), "canceled uploading new image", Toast.LENGTH_SHORT).show();
        }

    }

    private void uploadNewStatus(Status newStatus) {
        mUserReference.push().setValue(newStatus);
    }

    private void pickImageFromGallery() {
        selectImageToUpload.launch("image/*");
    }
}
