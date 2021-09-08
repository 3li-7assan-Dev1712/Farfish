package com.example.friendlychat.fragments;

import android.os.Build;
import android.os.Bundle;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.friendlychat.Module.FullImageData;
import com.example.friendlychat.R;

public class FullImageFragment extends Fragment {
    private FullImageData mFullImageData;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (getArguments() != null)
            mFullImageData = getArguments().getParcelable("image_info");
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.full_image_fragment, container, false);
        ViewCompat.setTransitionName(view, "root_view_full_image_fragment");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Transition transition = TransitionInflater.from(requireContext())
                    .inflateTransition(R.transition.image_transition);
            setSharedElementEnterTransition(transition);
            setSharedElementReturnTransition(transition);
        }
        Toolbar toolbar = view.findViewById(R.id.toolbar_full_image);
        ImageView fullImage = view.findViewById(R.id.full_bleed_image);
        fullImage.setOnClickListener( v -> {
            Navigation.findNavController(v).navigateUp();
        });
        if (mFullImageData != null) {
            toolbar.setTitle(mFullImageData.getSenderName());
            toolbar.setSubtitle(mFullImageData.getFormattedTime());
            fullImage.setImageBitmap(mFullImageData.getBitmap());
        }else
            Toast.makeText(requireContext(), "data is null", Toast.LENGTH_SHORT).show();
        return view;
    }
}
