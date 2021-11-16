package com.example.farfish.fragments.main;

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
import androidx.hilt.navigation.HiltViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.farfish.Adapters.ContactsListAdapter;
import com.example.farfish.CustomViews.CustomStatusView;
import com.example.farfish.CustomViews.CustomStory;
import com.example.farfish.Module.dataclasses.Status;
import com.example.farfish.Module.util.Connection;
import com.example.farfish.R;
import com.example.farfish.data.MainViewModel;
import com.example.farfish.data.repositories.StatusRepository;
import com.example.farfish.databinding.StatusFragmentBinding;
import com.example.farfish.fragments.dialogs.InternetConnectionDialog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * The StatusFragment which displays other users statues and let the user
 * upload a status text or image.
 */
@AndroidEntryPoint
public class StatusFragment extends Fragment implements ContactsListAdapter.OnChatClicked, StatusRepository.StatusInterface {

    // the root view
    private StatusFragmentBinding mBinding;
    private static final String TAG = StatusFragment.class.getSimpleName();

    private boolean SHOULD_REMOVE_LISTENER;

    public MainViewModel mModel;
    /* request permission*/
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your app.
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
    @Inject
    public ContactsListAdapter mStatusAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mStatusAdapter.setForStatus(true);
        mStatusAdapter.setOnChatClicked(this);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = StatusFragmentBinding.inflate(inflater, container, false);
        View view = mBinding.getRoot();
        SHOULD_REMOVE_LISTENER = true;
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        requireActivity().findViewById(R.id.bottom_nav).setVisibility(View.VISIBLE);
        mBinding.statusRecycler.setAdapter(mStatusAdapter);
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
            if (!Connection.isUserConnected(requireContext()))
                new InternetConnectionDialog().show(requireActivity().getSupportFragmentManager(), "internet_alert");
            else {
                SHOULD_REMOVE_LISTENER = false;
                navController.navigate(R.id.uploadTextStatusFragment);
            }
        });
        NavBackStackEntry backStackEntry = navController.getBackStackEntry(R.id.nav_graph);
        mModel = new ViewModelProvider(
                backStackEntry,
                HiltViewModelFactory.create(requireContext(), backStackEntry)
        ).get(MainViewModel.class);
        mModel.getStatusRepository().setStatusInterface(this);
        getViewLifecycleOwnerLiveData().observe(getViewLifecycleOwner(), lifecycleOwner ->
                mModel.getStatusLiveData().observe(lifecycleOwner, statuesLists -> {
                    mStatusAdapter.customSubmitStatusList(statuesLists);
                    mBinding.statusProgressBar.setVisibility(View.GONE);
                }));

        if (savedInstanceState != null)
            mBinding.statusProgressBar.setVisibility(View.GONE);
        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /**
     * after the user choose an image from the gallery this method will be called compress the image
     * and store it in the firestorage and finally display it in the status list.
     *
     * @param uri the uri of the selected image from gallery.
     */
    private void putIntoImage(Uri uri) {
        mBinding.statusProgressBar.setVisibility(View.VISIBLE);
        if (uri != null) {
            mModel.getStatusRepository().compressAndUploadImage(uri);
            // if the user hit the back button before choosing an image to send the code below will be executed.
        } else {
            Toast.makeText(requireContext(), getString(R.string.cancel_sending_img), Toast.LENGTH_SHORT).show();
            mBinding.statusProgressBar.setVisibility(View.GONE);
        }

    }

    /**
     * whenever a user try to upload an image as a status this method will be called, it checks the internet
     * connection and then start open the gallery if so.
     */
    private void pickImageFromGallery() {
        if (!Connection.isUserConnected(requireContext()))
            new InternetConnectionDialog().show(requireActivity().getSupportFragmentManager(), "internet_alert");
        else {
            SHOULD_REMOVE_LISTENER = false;
            selectImageToUpload.launch("image/*");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
        if (SHOULD_REMOVE_LISTENER)
            mModel.statusRepository.removeListener();
    }


    /**
     * this method is called when the user tab on a status from the list to display the statues in
     * full screen (just like Whatsapp).
     *
     * @param position takes the position of the pressed status (item in the RecyclerView)
     *                 to be used in the app flow.
     */
    @Override
    public void onChatClicked(int position) {
        Log.d(TAG, "onStatusClicked: Ok, will be completed soon");

        List<Status> userStatuses = mModel.getStatusRepository().getStatusLists().get(position);
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
                .setStoryDuration(2500)
                .setTitleText(userStatuses.get(0).getUploaderName())
                .setTitleLogoUrl(userStatuses.get(0).getUploaderPhotoUrl())
                .setSubtitleText(null) // Default is Hidden
                .build()
                .show();
    }


    /**
     * the StatusRepository calls this method after preparing the data (statues) to notify the
     * fragment to display it to the user.
     */
    @Override
    public void statusesAreReady() {
        mModel.updateStatues();
        mStatusAdapter.notifyDataSetChanged();
        if (mBinding != null)
            mBinding.statusProgressBar.setVisibility(View.GONE);
    }
}
