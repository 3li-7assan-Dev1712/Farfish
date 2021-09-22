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
import com.example.friendlychat.Module.SharedPreferenceUtils;
import com.example.friendlychat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class UserProfileFragment extends Fragment {

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
        View view = inflater.inflate(R.layout.user_profile_fragment, container, false);
        Toolbar toolbar = view.findViewById(R.id.toolbar_user_profile);
        NavController controller = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
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
        TextView lastTimeSeenTextView = view.findViewById(R.id.userProfileLastTimeSeen);
        Button edit = view.findViewById(R.id.editProfileButton);
        Button logout = view.findViewById(R.id.logoutButtonUserProfile);
        /*------------------------------------------------------------------------------*/


        // user information

        String userName ;
        String userPhotoUrl ;
        String userId ;
        String status ;
        String email;
        String lastTimeSeen = requireContext().getResources().getString(R.string.online);
        Bundle userInfo = getArguments();
        if (userInfo != null){
            edit.setVisibility(View.INVISIBLE);
            logout.setVisibility(View.INVISIBLE);
            userPhotoUrl = userInfo.getString("target_user_photo_url");
            userName = userInfo.getString("target_user_name");
            status = userInfo.getString("target_user_status");
            email = userInfo.getString("target_user_email");
            userId = userInfo.getString("target_user_id");
            boolean isActive = userInfo.getBoolean("isActive");
            if (!isActive)
                lastTimeSeen = getReadableLastTimeSeen(userInfo.getLong("target_user_last_time_seen"));
        }else{
            edit.setVisibility(View.VISIBLE);
            logout.setVisibility(View.VISIBLE);
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
        // user info from bundle, this will override the above fields if it's not null

        userNameTextView.setText(userName);
        emailTextView.setText(email);
        statusTextView.setText(status);
        userIdTextView.setText(userId);
        lastTimeSeenTextView.setText(lastTimeSeen);
        /*------------------------------------------------------------------------------*/

        // invoke listeners
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

        logout.setVisibility(View.VISIBLE);
        logout.setOnClickListener(logoutOnClickListener -> {
            FirebaseAuth.getInstance().signOut();
            SharedPreferenceUtils.saveUserSignOut(requireContext());
            controller.navigate(R.id.action_userProfileFragment_to_fragmentSignIn);
        });
        /*----------------------------------------------------------------------------*/
        return view;
    }

    private String getReadableLastTimeSeen (long lastTimeUserWasActive) {
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
        String and =  context.getResources().getString(R.string.and);

        if (days > 1){
            hours -= days * 24;
            return formattedDiff.append(lastTimeSeenLabel).append(" ").append(days).append(" ")
                    .append(daysLabel).append(" ").append(and).append(" ").append(hours).append(" ")
                    .append(context.getResources().getString(R.string.hours_ago)).toString();
        }
        if (days == 1){
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
        if (hours == 1){
            minutes -= hours * 60;
            return formattedDiff.append(lastTimeSeenLabel).append(" ").append(context.getResources()
                    .getString(R.string.one_hour)).append(and).append(" ").append(minutes).append(" ")
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
