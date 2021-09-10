package com.example.friendlychat.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.Navigation;

import com.example.friendlychat.Module.Status;
import com.example.friendlychat.R;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class StatusDetailFragment extends Fragment {

    private static final String TAG = StatusDetailFragment.class.getSimpleName();
    private List<Status> userStatues;
    private int tracker = 0;
    private Toolbar mStatusToolBar;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideBottomNav();
        /* get the argument from the parent fragment*/
        assert getArguments() != null;
        Status[] statuses = (Status[]) getArguments().getParcelableArray("one_user_statuses");
        assert statuses != null;
        userStatues = Arrays.asList(statuses);
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View statusFragment = inflater.inflate(R.layout.status_detail_fragment, container, false);
        mStatusToolBar = statusFragment.findViewById(R.id.toolbar_status_detail);
        displayToolbarInfo(statusFragment);
        View navigateToPreviousStatus = statusFragment.findViewById(R.id.go_to_previous_fragment);
        View navigateToNextStatus = statusFragment.findViewById(R.id.go_to_next_fragment);
        navigateToPreviousStatus.setOnClickListener( goToPrevious -> {
            Toast.makeText(requireActivity(), "Navigating to previous status", Toast.LENGTH_SHORT).show();
            tracker--;
            if (tracker >= 0) displayFragment();
            else navigateBack(statusFragment);
        });
        navigateToNextStatus.setOnClickListener( goToNext -> {
            Toast.makeText(requireActivity(), "Navigating to next status", Toast.LENGTH_SHORT).show();
            tracker++;
            if (tracker < userStatues.size()) displayFragment();
            else navigateBack(statusFragment);
        });
        Status firstStatus = userStatues.get(tracker);
        UserStatus userStatus = new UserStatus(firstStatus.getStatusImage(), firstStatus.getStatusText());
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction().add(R.id.statusDetailFragment, userStatus).commit();
        return statusFragment;
    }

    private void navigateBack(View statusFragment) {
        Navigation.findNavController(statusFragment).navigateUp();
    }

    private void displayFragment() {
        Status nextStatus = userStatues.get(tracker);
        UserStatus userStatus = new UserStatus(nextStatus.getStatusImage(), nextStatus.getStatusText());
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.statusDetailFragment, userStatus).commit();
    }

    private void displayToolbarInfo(View view) {
        mStatusToolBar.setTitle(userStatues.get(0).getUploaderName());
        SimpleDateFormat d = new SimpleDateFormat("h:mm a", Locale.getDefault());
        String readableDate = d.format(userStatues.get(0).getTimestamp());
        mStatusToolBar.setSubtitle(readableDate);
        Log.d(TAG, "onCreateView: statusNumber are " + userStatues.size());
        mStatusToolBar.setNavigationOnClickListener( navigationIconView -> navigateBack(view));
    }

    private void hideBottomNav() {
        requireActivity().findViewById(R.id.bottom_nav).setVisibility(View.GONE);
    }
}
