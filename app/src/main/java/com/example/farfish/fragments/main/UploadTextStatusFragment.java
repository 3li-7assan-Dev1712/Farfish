package com.example.farfish.fragments.main;

import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.aghajari.emojiview.view.AXEmojiView;
import com.example.farfish.Module.preferences.MessagesPreference;
import com.example.farfish.Module.dataclasses.Status;
import com.example.farfish.R;
import com.example.farfish.data.MainViewModel;
import com.example.farfish.databinding.UploadTextStatusFragmentBinding;

import java.util.Date;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class UploadTextStatusFragment extends Fragment {

    private static final String TAG = UploadTextStatusFragment.class.getSimpleName();
    private static final int DEFAULT_STATUS_LENGTH_LIMIT = 400;
    private UploadTextStatusFragmentBinding mBinding;

    public MainViewModel model;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requireActivity().findViewById(R.id.bottom_nav).setVisibility(View.GONE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = UploadTextStatusFragmentBinding.inflate(inflater, container, false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            requireActivity().getWindow().setStatusBarColor(requireContext().getResources().getColor(R.color.secondaryDarkColor));
        View view = mBinding.getRoot();
        model = new ViewModelProvider(this).get(MainViewModel.class);
        hideSendFab(); // hide it until the user inserts some chars
        mBinding.uploadTextStatusFragmentFab.setOnClickListener(uploadFab -> {
            if (mBinding.editTextUploadStatus.getText().toString().equals("")) {
                Toast.makeText(requireContext(), getString(R.string.input_field_first), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireActivity(), getString(R.string.uploading_text_status), Toast.LENGTH_SHORT).show();
                Status textStatus = new Status(MessagesPreference.getUserName(requireContext()),
                        MessagesPreference.getUsePhoneNumber(requireContext()),
                        "", Objects.requireNonNull(mBinding.editTextUploadStatus.getText()).toString(),
                        new Date().getTime(),
                        0);
                model.getStatusRepository().uploadNewStatus(textStatus);
                Navigation.findNavController(view).navigateUp();
            }
        });
        AXEmojiView emojiView = new AXEmojiView(requireContext());
        emojiView.setEditText(mBinding.editTextUploadStatus);
        mBinding.statusEditTextPoppupLayout.initPopupView(emojiView);
        mBinding.statusEditTextPoppupLayout.hideAndOpenKeyboard();

        mBinding.editTextUploadStatus.setOnClickListener(listener -> {
            mBinding.statusEditTextPoppupLayout.openKeyboard();
            mBinding.statusEditTextPoppupLayout.setVisibility(View.GONE);
        });

        mBinding.editTextUploadStatus.setOnLongClickListener(longListener -> {
            if (mBinding.statusEditTextPoppupLayout.isShowing()) {
                mBinding.statusEditTextPoppupLayout.openKeyboard();
                mBinding.statusEditTextPoppupLayout.dismiss();
                mBinding.statusEditTextPoppupLayout.setVisibility(View.GONE);
            } else {
                mBinding.statusEditTextPoppupLayout.setVisibility(View.VISIBLE);
                mBinding.statusEditTextPoppupLayout.show();
            }
            return true;
        });
        mBinding.editTextUploadStatus.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d(TAG, "beforeTextChanged");
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    displaySendFab();
                } else {
                    hideSendFab();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.d(TAG, "afterTextChanged");
            }
        });
        mBinding.editTextUploadStatus.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_STATUS_LENGTH_LIMIT)});

        return view;
    }

    private void hideSendFab() {
        mBinding.uploadTextStatusFragmentFab.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            requireActivity().getWindow().setStatusBarColor(requireContext().getResources().getColor(R.color.colorPrimaryDark));
    }

    private void displaySendFab() {
        mBinding.uploadTextStatusFragmentFab.setVisibility(View.VISIBLE);
    }
}
