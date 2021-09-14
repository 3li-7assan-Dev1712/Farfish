package com.example.friendlychat.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.friendlychat.R;

public class FragmentSignIn extends Fragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        requireActivity().findViewById(R.id.bottom_nav).setVisibility(View.GONE);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_in, container, false);
        TextView register = view.findViewById(R.id.register_sign_in);
        register.setOnClickListener( registerTextView -> {
            Navigation.findNavController(view).navigate(FragmentSignInDirections.actionFragmentSignInToFragmentSignUp());
        });
        return view;
    }
}
