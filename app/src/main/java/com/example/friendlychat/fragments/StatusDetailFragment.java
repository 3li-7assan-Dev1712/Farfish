package com.example.friendlychat.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.friendlychat.R;

public class StatusDetailFragment extends Fragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View statusFragment = inflater.inflate(R.layout.status_detail_fragment, container, false);
        Toolbar statusBar = statusFragment.findViewById(R.id.toolbar_status_detail);
        statusBar.setNavigationOnClickListener( navigationIconView -> {
            Navigation.findNavController(statusFragment).navigateUp();
        });
        View navigateToPreviousStatus = statusFragment.findViewById(R.id.go_to_previous_fragment);
        View navigateToNextStatus = statusFragment.findViewById(R.id.go_to_next_fragment);
        navigateToPreviousStatus.setOnClickListener( goToNext -> {
            Toast.makeText(requireActivity(), "Navigating to previous status", Toast.LENGTH_SHORT).show();
        });
        navigateToNextStatus.setOnClickListener( goToNext -> {
            Toast.makeText(requireActivity(), "Navigating to next status", Toast.LENGTH_SHORT).show();
        });
        return statusFragment;
    }
}
