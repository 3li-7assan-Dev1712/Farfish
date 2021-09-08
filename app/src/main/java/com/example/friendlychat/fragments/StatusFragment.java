package com.example.friendlychat.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.friendlychat.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class StatusFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.status_fragment, container, false);
        FloatingActionButton uploadImageFab = view.findViewById(R.id.uploadImageStatusFab);
        uploadImageFab.setOnClickListener( uploadImage -> {
            Toast.makeText(requireContext(), "Upload image as a status", Toast.LENGTH_SHORT).show();
        });
        FloatingActionButton uploadTextFab = view.findViewById(R.id.uploadTextStatusFab);
        uploadTextFab.setOnClickListener( uploadImage -> {
            Toast.makeText(requireContext(), "Upload text as a status", Toast.LENGTH_SHORT).show();
        });
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
