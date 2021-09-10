package com.example.friendlychat.fragments;

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

import com.example.friendlychat.Module.MessagesPreference;
import com.example.friendlychat.Module.Status;
import com.example.friendlychat.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Date;

public class UploadTextStatusFragment extends Fragment {

    private static final String TAG = UploadTextStatusFragment.class.getSimpleName();
    private static final int DEFAULT_STATUS_LENGTH_LIMIT = 400;
    private FloatingActionButton mUploadFab;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.upload_text_status_fragment, container, false);
        EditText statusEditText = view.findViewById(R.id.editTextUploadStatus);
        mUploadFab = view.findViewById(R.id.uploadTextStatusFragmentFab);
        mUploadFab.setOnClickListener( uploadFab -> {
            Toast.makeText(requireActivity(), "Upload Text", Toast.LENGTH_SHORT).show();
            Status textStatus = new Status(MessagesPreference.getUserName(requireContext()),
                    "", statusEditText.getText().toString(),
                    new Date().getTime(),
                    0);
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
