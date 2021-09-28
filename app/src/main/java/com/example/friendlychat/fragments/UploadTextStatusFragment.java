package com.example.friendlychat.fragments;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.aghajari.emojiview.view.AXEmojiPopupLayout;
import com.aghajari.emojiview.view.AXEmojiView;
import com.example.friendlychat.Module.MessagesPreference;
import com.example.friendlychat.Module.Status;
import com.example.friendlychat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;

public class UploadTextStatusFragment extends Fragment {

    private static final String TAG = UploadTextStatusFragment.class.getSimpleName();
    private static final int DEFAULT_STATUS_LENGTH_LIMIT = 400;
    private FloatingActionButton mUploadFab;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requireActivity().findViewById(R.id.bottom_nav).setVisibility(View.GONE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.upload_text_status_fragment, container, false);
        EditText statusEditText = view.findViewById(R.id.editTextUploadStatus);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("status");
        DatabaseReference userRef = reference.child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
        mUploadFab = view.findViewById(R.id.uploadTextStatusFragmentFab);
        mUploadFab.setOnClickListener( uploadFab -> {
            Toast.makeText(requireActivity(), "Upload Text", Toast.LENGTH_SHORT).show();
            Status textStatus = new Status(MessagesPreference.getUserName(requireContext()),
                    MessagesPreference.getUsePhoneNumber(requireContext()),
                    "", statusEditText.getText().toString(),
                    new Date().getTime(),
                    0);

            //
            if (statusEditText.getText().toString().equals("")) {
                Toast.makeText(requireContext(), "Enter text first", Toast.LENGTH_SHORT).show();
            }else{
                userRef.push().setValue(textStatus).addOnCompleteListener(task -> {
                    Navigation.findNavController(view).navigateUp();
                }).addOnFailureListener(exception -> {
                    Log.d(TAG, "onCreateView: upload text status exception " + exception.getMessage());
                    Toast.makeText(requireActivity(), "Error uploading status, check out your internet connection", Toast.LENGTH_SHORT).show();
                });
            }
        });
        AXEmojiView emojiView = new AXEmojiView(requireContext());
        emojiView.setEditText(statusEditText);
        AXEmojiPopupLayout emojiPopupLayout = view.findViewById(R.id.status_edit_text_poppup_layout);
        emojiPopupLayout.initPopupView(emojiView);
        emojiPopupLayout.hideAndOpenKeyboard();
        statusEditText.setOnClickListener(listener-> {
            emojiPopupLayout.openKeyboard();
            emojiPopupLayout.setVisibility(View.GONE);
        });
        statusEditText.setOnLongClickListener(longListener-> {
            if (emojiPopupLayout.isShowing()) {
                emojiPopupLayout.openKeyboard();
                emojiPopupLayout.dismiss();
                emojiPopupLayout.setVisibility(View.GONE);
            }
            else {
                emojiPopupLayout.setVisibility(View.VISIBLE);
                emojiPopupLayout.show();
            }
            return true;
        });
        statusEditText.addTextChangedListener(new TextWatcher() {
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
        statusEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_STATUS_LENGTH_LIMIT)});

        return view;
    }

    private void hideSendFab() {
        mUploadFab.setVisibility(View.INVISIBLE);
    }

    private void displaySendFab() {
        mUploadFab.setVisibility(View.VISIBLE);
    }
}
