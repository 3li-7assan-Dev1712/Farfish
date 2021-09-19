package com.example.friendlychat.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.friendlychat.Module.MessagesPreference;
import com.example.friendlychat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class UserProfileFragment extends Fragment {

    private static final String TAG = UserProfileFragment.class.getSimpleName();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        requireActivity().findViewById(R.id.bottom_nav).setVisibility(View.GONE);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_profile_fragment, container, false);
        Toolbar toolbar = view.findViewById(R.id.toolbar_user_profile);
        NavController controller = Navigation.findNavController(view);
        toolbar.setNavigationOnClickListener(clickListener -> {
            controller.navigateUp();
        });
        requireActivity().findViewById(R.id.bottom_nav).setVisibility(View.GONE);
        // init views
        ImageView profileImage = view.findViewById(R.id.userProfileImageView);
        ProgressBar progressBar = view.findViewById(R.id.userProfileProgressBar);
        TextView userNameTextView = view.findViewById(R.id.userNameProfileTextView);
        TextView statusTextView = view.findViewById(R.id.statusOfUserTextVIew);
        TextView emailTextView = view.findViewById(R.id.userEmailProfileTextView);
        TextView userIdTextView = view.findViewById(R.id.userIdTextView);
        /*------------------------------------------------------------------------------*/

        // user information
        Context context = requireContext();
        String userName = MessagesPreference.getUserName(context);
        String userPhotoUrl = MessagesPreference.getUsePhoto(context);
        String userId = MessagesPreference.getUserId(context);
        String status = MessagesPreference.getUseStatus(context);
        String phoneNumber = MessagesPreference.getUsePhoneNumber(context);
        String email = "";
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            email = currentUser.getEmail();

        } else {
            email = "no email";
        }
        // populate the UI with the data
        Picasso.get().load(userPhotoUrl).placeholder(R.drawable.ic_round_person_24)
                .into(profileImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d(TAG, "onError: " + e.getMessage());
                    }
                });
        userNameTextView.setText(userName);
        emailTextView.setText(email);
        statusTextView.setText(status);
        userIdTextView.setText(userId);
        /*------------------------------------------------------------------------------*/

        // invoke listeners
        Button edit = view.findViewById(R.id.editProfileButton);
        edit.setVisibility(View.VISIBLE);
        edit.setOnClickListener(editProfile -> {
            // prepare data in bundle to send to the destination
            Bundle userData = new Bundle();
            userData.putString("photo_url", userPhotoUrl);
            userData.putString("user_name", userName);
            userData.putString("user_status", status);
            userData.putString("phone_number", phoneNumber);
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
                    .navigate(R.id.action_userProfileFragment_to_editProfileFragment, userData);
        });

        Button logout = view.findViewById(R.id.layoutButton);
        logout.setVisibility(View.VISIBLE);
        logout.setOnClickListener(logoutOnClickListener -> {
            FirebaseAuth.getInstance().signOut();
            controller.navigate(R.id.action_userProfileFragment_to_fragmentSignIn);
        });
        return view;
    }
}
