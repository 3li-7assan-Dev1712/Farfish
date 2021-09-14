package com.example.friendlychat.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.friendlychat.R;

public class FragmentSignUp extends Fragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        requireActivity().findViewById(R.id.bottom_nav).setVisibility(View.GONE);
        super.onCreate(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);
        Toolbar toolbar = view.findViewById(R.id.toolbar_sign_up);
        toolbar.setNavigationOnClickListener( navigationIcon -> {
            navigateUp(view); // navigate back using the navigation icon
        });
        TextView loginTextView = view.findViewById(R.id.text_view_login);
        loginTextView.setOnClickListener(login -> {
            navigateUp(view);
        });
        return view;
    }

    private void navigateUp(View view) {
        Navigation.findNavController(view).navigateUp();
    }
}
