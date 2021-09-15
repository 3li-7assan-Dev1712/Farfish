package com.example.friendlychat.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.friendlychat.R;

public class ProfileImageFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_image_fragment, container, false);
        ImageView image = view.findViewById(R.id.registerImage);
        EditText phoneNumberEditText = view.findViewById(R.id.profileImagePhoneNumber);
        Button continueButton = view.findViewById(R.id.continueButton);

        return view;
    }
}
