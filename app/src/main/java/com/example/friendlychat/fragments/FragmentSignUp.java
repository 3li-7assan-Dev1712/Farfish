package com.example.friendlychat.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.friendlychat.R;
import com.example.friendlychat.databinding.FragmentSignUpBinding;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

public class FragmentSignUp extends Fragment implements TermsAndConditionsDialogFragment.ActionClickListener {

    private static final String TAG = FragmentSignUp.class.getSimpleName();
    private NavController mNavController;
    private String mUserName;
    private View snackBarView;
    private FragmentSignUpBinding mBinding;
    private TermsAndConditionsDialogFragment termsAndConditionsDialogFragment;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentSignUpBinding.inflate(inflater, container, false);
        View view = mBinding.getRoot();
        mNavController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        mBinding.toolbarSignUp.setNavigationOnClickListener(navigationIcon -> {
            navigateUp(); // navigate back using the navigation icon
        });
        mBinding.textViewLogin.setOnClickListener(textViewLoginListener -> navigateUp());
        termsAndConditionsDialogFragment = new TermsAndConditionsDialogFragment(this);
        mBinding.checkBoxTermsCondition.setOnClickListener(termsListener -> {
            if (mBinding.checkBoxTermsCondition.isChecked()) {
                termsAndConditionsDialogFragment.
                        show(requireActivity().getSupportFragmentManager(), "terms_conditions");
            }
        });
        mBinding.registerButton.setOnClickListener(registerButtonListener -> {
            if (mBinding.editTextFirstName.getText().toString().equals(""))
                displayRequiredFieldSnackBar(mBinding.editTextFirstName, getString(R.string.input_field_first));
            else if (mBinding.editTextLastName.getText().toString().equals(""))
                displayRequiredFieldSnackBar(mBinding.editTextLastName, getString(R.string.input_field_first));
            else if (mBinding.editTextEmailAddressSignUp.getText().toString().equals(""))
                displayRequiredFieldSnackBar(mBinding.editTextEmailAddressSignUp, getString(R.string.input_field_first));
            else if (mBinding.editTextPasswordSignUp.getText().toString().equals(""))
                displayRequiredFieldSnackBar(mBinding.editTextPasswordSignUp, getString(R.string.input_field_first));
            else if (mBinding.editTextConfirmPassword.getText().toString().equals(""))
                displayRequiredFieldSnackBar(mBinding.editTextConfirmPassword, getString(R.string.input_field_first));
            else if (!mBinding.editTextConfirmPassword.getText().toString().equals(mBinding.editTextPasswordSignUp.getText().toString()))
                displayRequiredFieldSnackBar(mBinding.editTextConfirmPassword, getString(R.string.confirm_password_first));
            else if (!mBinding.checkBoxTermsCondition.isChecked()) {
                Snackbar.make(view, R.string.terms_and_conditions, BaseTransientBottomBar.LENGTH_LONG).show();
                termsAndConditionsDialogFragment.show(requireActivity().getSupportFragmentManager(), "terms_conditions");
            } else {
                Toast.makeText(requireContext(), getString(R.string.agree_message), Toast.LENGTH_SHORT).show();
                String email = mBinding.editTextEmailAddressSignUp.getText().toString();
                String password = mBinding.editTextPasswordSignUp.getText().toString();
                String firstName = mBinding.editTextFirstName.getText().toString();
                String lastName = mBinding.editTextLastName.getText().toString();
                mUserName = firstName + " " + lastName;
                signUp(email, password);
            }
        });
        snackBarView = view;
        return view;
    }

    private void signUp(String email, String password) {

        Bundle userData = new Bundle();
        userData.putString("userName", mUserName);
        userData.putString("email", email);
        userData.putString("password", password);
        mNavController.navigate(R.id.profileImageFragment, userData);
    }

    private void navigateUp() {
        mNavController.navigateUp();
    }

    private void showKeyboardOnEditText(EditText editText) {
        editText.requestFocus();
        InputMethodManager manager = (InputMethodManager) requireActivity().
                getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    private void displayRequiredFieldSnackBar(EditText requiredField, String message) {
        Snackbar snackbar = Snackbar.make(snackBarView, message, Snackbar.LENGTH_LONG);
        int action = R.string.fix;
        snackbar.setAction(action, listener -> {
            showKeyboardOnEditText(requiredField);
        });
        snackbar.show();
    }

    @Override
    public void onActionClick(boolean isAgree) {
        mBinding.checkBoxTermsCondition.setChecked(isAgree);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}
