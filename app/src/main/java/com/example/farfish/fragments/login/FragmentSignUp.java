package com.example.farfish.fragments.login;

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

import com.example.farfish.R;
import com.example.farfish.databinding.FragmentSignUpBinding;
import com.example.farfish.fragments.dialogs.TermsAndConditionsDialogFragment;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

/**
 * this fragment is for let users sign up when it is the first time to register in the app.
 */
public class FragmentSignUp extends Fragment implements TermsAndConditionsDialogFragment.ActionClickListener {

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
            else if (mBinding.editTextPasswordSignUp.getText().length() < 6)
                displayRequiredFieldSnackBar(mBinding.editTextPasswordSignUp, getString(R.string.short_password));
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

    /**
     * this method is called after ensure that all the required fields are filled by the user
     * the let them sign up (create an account in the Farfish app).
     *
     * @param email    the user's email to be used for sign in on another time.
     * @param password the password for authentication.
     */
    private void signUp(String email, String password) {

        Bundle userData = new Bundle();
        userData.putString("userName", mUserName);
        userData.putString("email", email);
        userData.putString("password", password);
        mNavController.navigate(R.id.action_fragmentSignUp_to_profileImageFragment, userData);
    }

    /**
     * this method is called to navigate back to the Sign in fragment.
     */
    private void navigateUp() {
        mNavController.navigateUp();
    }

    /**
     * this method is responsible for showing the keyboard in a specific EditText to
     * inform the user about a required field to be filled.
     *
     * @param editText the EditText to show on it the keyboard for informing the user that
     *                 this field is required.
     */
    private void showKeyboardOnEditText(EditText editText) {
        editText.requestFocus();
        InputMethodManager manager = (InputMethodManager) requireActivity().
                getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    /**
     * this method displays a snackbar to let the user know they missed filling a required field.
     *
     * @param requiredField the missed filling required filed.
     * @param message       the message which will be used to explain for the user they should
     *                      fill this required field.
     */
    private void displayRequiredFieldSnackBar(EditText requiredField, String message) {
        Snackbar snackbar = Snackbar.make(snackBarView, message, Snackbar.LENGTH_LONG);
        int action = R.string.fix;
        snackbar.setAction(action, listener -> showKeyboardOnEditText(requiredField));
        snackbar.show();
    }

    /**
     * a callback that will be invoked the user choose an action from the TermsAndConditions dialog
     * (agree or disagree).
     *
     * @param isAgree a boolean type which indicates whether agreed on the Terms or not.
     */
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
