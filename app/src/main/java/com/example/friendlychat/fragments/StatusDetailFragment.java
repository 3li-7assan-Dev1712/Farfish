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

import com.example.friendlychat.Module.Status;
import com.example.friendlychat.R;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class StatusDetailFragment extends Fragment {

    private List<Status> userStatues;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* get the argument from the parent fragment*/
        Status[] statuses = (Status[]) getArguments().getParcelableArray("one_user_statuses");
        userStatues = Arrays.asList(statuses);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View statusFragment = inflater.inflate(R.layout.status_detail_fragment, container, false);
        Toolbar statusBar = statusFragment.findViewById(R.id.toolbar_status_detail);
        statusBar.setTitle(userStatues.get(0).getUploaderName());
        SimpleDateFormat d = new SimpleDateFormat("h:mm a", Locale.getDefault());
        String readableDate = d.format(userStatues.get(0).getTimestamp());
        statusBar.setSubtitle(readableDate);
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
