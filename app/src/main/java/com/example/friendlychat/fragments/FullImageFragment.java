package com.example.friendlychat.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

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
        Toolbar toolbar = view.findViewById(R.id.toolbar_full_image);
        ImageView fullImage = view.findViewById(R.id.full_bleed_image);

        if (mFullImageData != null) {
            toolbar.setTitle(mFullImageData.getSenderName());
            toolbar.setSubtitle(mFullImageData.getFormattedTime());
            fullImage.setImageBitmap(mFullImageData.getBitmap());
        }else
            Toast.makeText(requireContext(), "data is null", Toast.LENGTH_SHORT).show();
        return view;
    }
}
