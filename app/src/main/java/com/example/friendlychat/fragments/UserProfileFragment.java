package com.example.friendlychat.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.friendlychat.R;

public class UserProfileFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_profile_fragment, container, false);
        ImageView profileImage = view.findViewById(R.id.profileImage);
        TextView emailTextVIew = view.findViewById(R.id.userEmailProfileTextView);
        Button edit = view.findViewById(R.id.editProfileButton);
        edit.setOnClickListener(editProfile -> {

        });
        return view;
    }
}
