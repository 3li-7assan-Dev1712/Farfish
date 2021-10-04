package com.example.farfish.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.farfish.R;

public class InternetConnectionDialog extends DialogFragment {

    public InternetConnectionDialog() {

    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(R.string.no_connection)
                .setMessage(R.string.no_connection_msg)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    dialog.dismiss();
                });
        return builder.create();
    }
}
