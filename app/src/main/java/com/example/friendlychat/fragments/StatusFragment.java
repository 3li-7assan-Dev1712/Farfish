package com.example.friendlychat.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
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
import androidx.recyclerview.widget.RecyclerView;

import com.example.friendlychat.Adapters.StatusAdapter;
import com.example.friendlychat.CustomViews.CustomStatusView;
import com.example.friendlychat.Module.CustomStory;
import com.example.friendlychat.Module.FileUtil;
import com.example.friendlychat.Module.MessagesPreference;
import com.example.friendlychat.Module.Status;
import com.example.friendlychat.R;
import com.example.friendlychat.databinding.StatusFragmentBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import id.zelory.compressor.Compressor;

public class StatusFragment extends Fragment implements StatusAdapter.OnStatusClicked {

    // the root view
    private StatusFragmentBinding mBinding;
    private static final String TAG = StatusFragment.class.getSimpleName();
    private DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference("status");
    private DatabaseReference mUserReference = FirebaseDatabase.getInstance().getReference();
    private StorageReference mRootRef;
    private Set<String> mContact;

    /* request permission*/
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                    pickImageFromGallery();
                } else {
                    Toast.makeText(requireContext(), getString(R.string.grant_access_media_permission), Toast.LENGTH_SHORT).show();
                }
            });
    private ActivityResultLauncher<String> selectImageToUpload = registerForActivityResult(
            new ActivityResultContracts.GetContent() {
                @NonNull
                @Override
                public Intent createIntent(@NonNull Context context, @NonNull String input) {
                    return super.createIntent(context, "image/*");// filter the gallery output, so the user can send a photo as they expects
                }
            },
            this::putIntoImage);

    private List<List<Status>> mStatusLists = new ArrayList<>();
    private StatusAdapter mStatusAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mRootRef = FirebaseStorage.getInstance().getReference("stories");
        mUserReference = mDatabaseReference.child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
        mStatusAdapter = new StatusAdapter(requireContext(), mStatusLists, this);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = StatusFragmentBinding.inflate(inflater, container, false);
        View view = mBinding.getRoot();
        mContact = MessagesPreference.getUserContacts(requireContext());
        NavController mNavController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        requireActivity().findViewById(R.id.bottom_nav).setVisibility(View.VISIBLE);
        RecyclerView statusRecycler = view.findViewById(R.id.statusRecycler);
        statusRecycler.setAdapter(mStatusAdapter);
        mBinding.uploadImageStatusFab.setOnClickListener(uploadImage -> {

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

        mBinding.uploadTextStatusFab.setOnClickListener(v -> {
            // prevent the user from uploading new statues if they are not connected to the internet *_-
            if (getInternetState())
                new InternetConnectionDialog().show(requireActivity().getSupportFragmentManager(), "internet_alert");
            else Navigation.findNavController(view).navigate(R.id.uploadTextStatusFragment);
        });
        listenToUpComingStatus();
        return view;
    }

    private void listenToUpComingStatus() {

        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            /*@RequiresApi(api = Build.VERSION_CODES.N)*/
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "onDataChange: generally");
              /*  List<Status> statuses = snapshot.child(FirebaseAuth.getInstance().getUid()).getValue(StatusLists.class).getStatusLists();
                mStatusLists.add(statuses);*/
                Iterable<DataSnapshot> iterable = snapshot.getChildren();
                List<List<Status>> allUsersStatues = new ArrayList<>();
                for (DataSnapshot dataSnapshot : iterable) {
                    String rootUserStatusPhoneNumber = "";
                    Iterator<DataSnapshot> childIterator = dataSnapshot.getChildren().iterator();
                    List<Status> oneUserStatuses = new ArrayList<>();
                    while (childIterator.hasNext()) {
                        Status status = childIterator.next().getValue(Status.class);
                        assert status != null;
                        String phoneNum = status.getUploaderPhoneNumber();
                        if (rootUserStatusPhoneNumber.equals(""))
                            rootUserStatusPhoneNumber = phoneNum;
                        oneUserStatuses.add(status);
                    }
                    // just display contacts status
                    if (mContact != null) {
                        for (String contact : mContact) {
                            if (PhoneNumberUtils.compare(contact, rootUserStatusPhoneNumber))
                                allUsersStatues.add(oneUserStatuses);
                        }
                    }

                }
                mStatusLists.clear();
                mStatusLists.addAll(allUsersStatues);
                mStatusAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void putIntoImage(Uri uri) {

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
                }).addOnSuccessListener(taskSnapshot -> {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    // ...
                    Toast.makeText(getContext(), getString(R.string.sending_img_msg), Toast.LENGTH_SHORT).show();
                    imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        String downloadUrl = downloadUri.toString();
                        long dateFromDateClass = new Date().getTime();
                         /* if the image sent successfully to the firebase storage send its metadata as a message
                         to the firebase firestore */
                        String uploaderName = MessagesPreference.getUserName(requireContext());
                        String uploaderPhoneNumber = MessagesPreference.getUsePhoneNumber(requireContext());
                        Status newStatus = new Status(uploaderName, uploaderPhoneNumber, downloadUrl, "", dateFromDateClass, 0);
                        uploadNewStatus(newStatus);
                    });

                });

            } catch (IOException e) {
                e.printStackTrace();
            }
            // if the user hit the back button before choosing an image to send the code below will be executed.
        } else {
            Toast.makeText(requireContext(), getString(R.string.cancel_sending_img), Toast.LENGTH_SHORT).show();
        }

    }

    private void uploadNewStatus(Status newStatus) {
        mUserReference.push().setValue(newStatus);
    }

    private void pickImageFromGallery() {
        if (getInternetState())
            new InternetConnectionDialog().show(requireActivity().getSupportFragmentManager(), "internet_alert");
        else selectImageToUpload.launch("image/*");
    }

    public boolean getInternetState() {
        ConnectivityManager cm = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork == null || !activeNetwork.isConnectedOrConnecting();
    }

    @Override
    public void onStatusClicked(int position) {
        Log.d(TAG, "onStatusClicked: Ok, will be completed soon");

        List<Status> userStatuses = mStatusLists.get(position);
        // test story view library
        ArrayList<CustomStory> myStories = new ArrayList<>();
        for (Status status : userStatuses) {
            myStories.add(new CustomStory(
                            status.getStatusImage(),
                            new Date(status.getTimestamp()),
                            status.getStatusText()
                    )
            );
        }
        new CustomStatusView.Builder(requireActivity().getSupportFragmentManager())
                .setStoriesList(myStories)
                .setStoryDuration(5000)
                .setTitleText(userStatuses.get(0).getUploaderName())
                .setSubtitleText(null) // Default is Hidden
                .build()
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}
