package com.example.farfish.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.farfish.Module.Message;
import com.example.farfish.Module.MessagesPreference;
import com.example.farfish.Module.SharedPreferenceUtils;
import com.example.farfish.R;
import com.example.farfish.databinding.UserProfileFragmentBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

public class UserProfileFragment extends Fragment {

    private UserProfileFragmentBinding mBinding;

    private static final String TAG = UserProfileFragment.class.getSimpleName();
    private String phoneNumber;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        requireActivity().findViewById(R.id.bottom_nav).setVisibility(View.GONE);

        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = UserProfileFragmentBinding.inflate(inflater, container, false);
        View view = mBinding.getRoot();
        NavController controller = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        mBinding.toolbarUserProfile.setNavigationOnClickListener(clickListener -> controller.navigateUp());
        requireActivity().findViewById(R.id.bottom_nav).setVisibility(View.GONE);

        // user information
        String userName;
        String userPhotoUrl;
        String userId;
        String status;
        String email;
        String lastTimeSeen = requireContext().getResources().getString(R.string.online);
        Bundle userInfo = getArguments();
        if (userInfo != null) {
            mBinding.editProfileButton.setVisibility(View.GONE);
            mBinding.logoutButtonUserProfile.setVisibility(View.GONE);
            userPhotoUrl = userInfo.getString("target_user_photo_url");
            userName = userInfo.getString("target_user_name");
            status = userInfo.getString("target_user_status");
            email = userInfo.getString("target_user_email");
            userId = userInfo.getString("target_user_id");
            boolean isActive = userInfo.getBoolean("isActive");
            if (!isActive)
                lastTimeSeen = getReadableLastTimeSeen(userInfo.getLong("target_user_last_time_seen"));
        } else {
            mBinding.editProfileButton.setVisibility(View.VISIBLE);
            mBinding.logoutButtonUserProfile.setVisibility(View.VISIBLE);
            Context context = requireContext();
            userName = MessagesPreference.getUserName(context);
            userPhotoUrl = MessagesPreference.getUsePhoto(context);
            userId = MessagesPreference.getUserId(context);
            status = MessagesPreference.getUseStatus(context);
            phoneNumber = MessagesPreference.getUsePhoneNumber(context);
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                email = currentUser.getEmail();

            } else {
                email = "no email";
            }
        }

        // populate the UI with the data
        Picasso.get().load(userPhotoUrl).placeholder(R.drawable.place_holder)
                .into(mBinding.userProfileImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        mBinding.userProfileProgressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d(TAG, "onError: " + e.getMessage());
                    }
                });
        // user info from bundle, this will override the above fields if it's not null

        mBinding.userNameProfileTextView.setText(userName);
        mBinding.userEmailProfileTextView.setText(email);
        mBinding.statusOfUserTextVIew.setText(status);
        mBinding.userIdTextView.setText(userId);
        mBinding.userProfileLastTimeSeen.setText(lastTimeSeen);
        /*------------------------------------------------------------------------------*/

        // invoke listeners
        mBinding.editProfileButton.setOnClickListener(editProfile -> {
            // prepare data in bundle to send to the destination
            Bundle userData = new Bundle();
            userData.putString("photo_url", userPhotoUrl);
            userData.putString("user_name", userName);
            userData.putString("user_status", status);
            userData.putString("phone_number", phoneNumber);
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
                    .navigate(R.id.action_userProfileFragment_to_editProfileFragment, userData);
        });

        mBinding.logoutButtonUserProfile.setOnClickListener(logoutOnClickListener -> {
            SharedPreferenceUtils.saveUserSignOut(requireContext());
            FirebaseAuth.getInstance().signOut();
            UserChatsFragment.clearAndRefresh();
            controller.navigate(R.id.action_userProfileFragment_to_fragmentSignIn);
        });
        /*----------------------------------------------------------------------------*/
        return view;
    }

    private String getReadableLastTimeSeen(long lastTimeUserWasActive) {
        long diff = System.currentTimeMillis() - lastTimeUserWasActive;
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        Log.d(TAG, "getReadableLastTimeSeen: days: " + days);
        Log.d(TAG, "getReadableLastTimeSeen: hours: " + hours);
        Log.d(TAG, "getReadableLastTimeSeen: minutes: " + minutes);
        Log.d(TAG, "getReadableLastTimeSeen: seconds: " + seconds);

        Context context = requireContext();
        StringBuilder formattedDiff = new StringBuilder();
        String daysLabel = context.getResources().getString(R.string.days);
        String hoursLabel = context.getString(R.string.hours_label);
        String lastTimeSeenLabel = requireContext().getResources()
                .getString(R.string.last_time_seen);
        String and = context.getResources().getString(R.string.and);

        if (days > 1) {
            hours -= days * 24;
            return formattedDiff.append(lastTimeSeenLabel).append(" ").append(days).append(" ")
                    .append(daysLabel).append(" ").append(and).append(" ").append(hours).append(" ")
                    .append(context.getResources().getString(R.string.hours_ago)).toString();
        }
        if (days == 1) {
            hours -= days * 24;
            return formattedDiff.append(lastTimeSeenLabel).append(" ").append(context.getResources()
                    .getString(R.string.one_day)).append(" ").append(and).append(" ").append(hours).append(" ")
                    .append(context.getResources().getString(R.string.hours_ago)).toString();
        }
        if (hours > 1) {
            minutes -= hours * 60;
            return formattedDiff.append(lastTimeSeenLabel).append(" ").append(hours).append(" ").append(hoursLabel)
                    .append(" ").append(and).append(" ").append(minutes)
                    .append(" ").append(context.getResources().getString(R.string.minutes_ago)).toString();
        }
        if (hours == 1) {
            minutes -= hours * 60;
            return formattedDiff.append(lastTimeSeenLabel).append(" ").append(context.getResources()
                    .getString(R.string.one_hour)).append(" ").append(and).append(" ").append(minutes).append(" ")
                    .append(context.getResources().getString(R.string.minutes_ago)).toString();
        }
        if (minutes > 1) {
            seconds -= minutes * 60;
            return formattedDiff.append(lastTimeSeenLabel).append(" ").append(minutes).append(" ").
                    append(context.getResources().getString(R.string.minutes_label)).append(" ").append(and)
                    .append(" ").append(seconds).append(" ").append(context.getResources().getString(R.string.seconds_ago)
                    ).toString();
        }
        return context.getResources().getString(R.string.last_time_seen_was_seconds_ago);
    }

}
