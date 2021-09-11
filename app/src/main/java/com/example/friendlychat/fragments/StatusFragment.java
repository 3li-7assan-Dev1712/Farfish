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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.friendlychat.Adapters.StatusAdapter;
import com.example.friendlychat.Module.FileUtil;
import com.example.friendlychat.Module.MessagesPreference;
import com.example.friendlychat.Module.Status;
import com.example.friendlychat.Module.StatusLists;
import com.example.friendlychat.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import java.util.function.UnaryOperator;

import id.zelory.compressor.Compressor;

public class StatusFragment extends Fragment implements StatusAdapter.OnStatusClicked{

    private static final String TAG = StatusFragment.class.getSimpleName();
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

    private List<List<Status>> mStatusLists = new ArrayList<>();
    private StatusAdapter mStatusAdapter;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.status_fragment, container, false);
        requireActivity().findViewById(R.id.bottom_nav).setVisibility(View.VISIBLE);
        RecyclerView statusRecycler = view.findViewById(R.id.statusRecycler);
        statusRecycler.setAdapter(mStatusAdapter);
        FloatingActionButton uploadImageFab = view.findViewById(R.id.uploadImageStatusFab);
        uploadImageFab.setClickable(true);
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
        uploadTextFab.setClickable(true);
        uploadTextFab.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Upload text as a status", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).navigate(R.id.uploadTextStatusFragment);
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
                Toast.makeText(getContext(), "onDataChanged generally", Toast.LENGTH_SHORT).show();
                List<Status> statuses = snapshot.child(FirebaseAuth.getInstance().getUid()).getValue(StatusLists.class).getStatusLists();
                mStatusLists.add(statuses);
                Iterable<DataSnapshot> iterable = snapshot.getChildren();
                List<List<Status>> allUsersStatues = new ArrayList<>();
                for (DataSnapshot dataSnapshot : iterable) {
                    Iterator<DataSnapshot> childInterator = dataSnapshot.getChildren().iterator();
                    List<Status> oneUserStatuses = new ArrayList<>();
                    while (childInterator.hasNext()) {
                        oneUserStatuses.add(childInterator.next().getValue(Status.class));
                    }
                    allUsersStatues.add(oneUserStatuses);
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mRootRef = FirebaseStorage.getInstance().getReference("stories");
        mUserReference = mDatabaseReference.child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
        mStatusAdapter = new StatusAdapter(requireContext(), mStatusLists, this);

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

    @Override
    public void onStatusClicked(int position) {
        Log.d(TAG, "onStatusClicked: Ok, will be completed soon");

        List<Status> userStatuses = mStatusLists.get(position);
        StatusFragmentDirections.ActionStatusFragmentToStatusDetailFragment2 acionToDetail =
                StatusFragmentDirections.actionStatusFragmentToStatusDetailFragment2(userStatuses.toArray(new Status[0]));
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.navigate(acionToDetail); // OK, that's it, it's time to pray!
    }
    /*
    class PutStatus implements Consumer<List<Status>> {
        @Override
        public void accept(List<Status> statuses) {
            mStatusLists.add(statuses);
        }
    }*/
}
