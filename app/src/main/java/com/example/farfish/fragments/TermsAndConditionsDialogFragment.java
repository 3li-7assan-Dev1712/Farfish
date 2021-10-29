package com.example.farfish.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.farfish.R;

public class TermsAndConditionsDialogFragment extends DialogFragment {

    public interface ActionClickListener {
        void onActionClick(boolean isAgree);
    }

    public ActionClickListener mTermsAndConditionsListener;

    public TermsAndConditionsDialogFragment(ActionClickListener _mTermsAndConditionsListener) {
        this.mTermsAndConditionsListener = _mTermsAndConditionsListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(R.string.terms_and_conditions_title)
                .setMessage(R.string.terms_and_conditions)
                .setPositiveButton(R.string.agree, (dialog, which) -> {
                    Toast.makeText(requireContext(), requireContext().getString(R.string.agree_message), Toast.LENGTH_SHORT).show();
                    mTermsAndConditionsListener.onActionClick(true);
                })
                .setNegativeButton(R.string.dont_agree, (dontAgree, id) -> {
                    Toast.makeText(requireContext(), requireContext().getString(R.string.not_agree_message), Toast.LENGTH_SHORT).show();
                    mTermsAndConditionsListener.onActionClick(false);
                });

        return builder.create();
    }
}
