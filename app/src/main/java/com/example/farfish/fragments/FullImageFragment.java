package com.example.farfish.fragments;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.farfish.Module.FullImageData;
import com.example.farfish.R;
import com.example.farfish.databinding.FullImageFragmentBinding;

public class FullImageFragment extends Fragment {
    private FullImageData mFullImageData;
    private FullImageFragmentBinding mBinding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        requireActivity().findViewById(R.id.bottom_nav).setVisibility(View.GONE);
        if (getArguments() != null)
            mFullImageData = getArguments().getParcelable("image_info");
        super.onCreate(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FullImageFragmentBinding.inflate(inflater, container, false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            requireActivity().getWindow().setStatusBarColor(requireContext().getResources().getColor(R.color.brown));
        View view = mBinding.getRoot();
        ViewCompat.setTransitionName(view, "root_view_full_image_fragment");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Transition transition = TransitionInflater.from(requireContext())
                    .inflateTransition(R.transition.image_transition);
            setSharedElementEnterTransition(transition);
            setSharedElementReturnTransition(transition);
        }
        Toolbar toolbar = view.findViewById(R.id.toolbar_full_image);
        toolbar.setNavigationOnClickListener(navigationclickListener -> navigateBack());
        ImageView fullImage = view.findViewById(R.id.full_bleed_image);
        fullImage.setOnClickListener(v -> navigateBack());
        if (mFullImageData != null) {
            toolbar.setTitle(mFullImageData.getSenderName());
            toolbar.setSubtitle(mFullImageData.getFormattedTime());
            fullImage.setImageBitmap(mFullImageData.getBitmap());
        }
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            requireActivity().getWindow().setStatusBarColor(requireContext().getResources().getColor(R.color.colorPrimaryDark));
    }

    private void navigateBack() {
        Navigation.findNavController(mBinding.getRoot()).navigateUp();
    }
}
