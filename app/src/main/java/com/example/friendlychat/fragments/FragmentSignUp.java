package com.example.friendlychat.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.friendlychat.R;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

public class FragmentSignUp extends Fragment implements TermsAndConditionsDialogFragment.ActionClickListener{

    private static final String TAG = FragmentSignUp.class.getSimpleName();
    private NavController mNavController;
    private String mUserName;
    private CheckBox mTermsCheck;
    private View snackBarView;
    private  TermsAndConditionsDialogFragment termsAndConditionsDialogFragment;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);
        mNavController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        Toolbar toolbar = view.findViewById(R.id.toolbar_sign_up);
        toolbar.setNavigationOnClickListener( navigationIcon -> {
            navigateUp(); // navigate back using the navigation icon
        });
        TextView loginTextView = view.findViewById(R.id.text_view_login);
        loginTextView.setOnClickListener(login -> navigateUp());
        EditText firstNameTextView = view.findViewById(R.id.edit_text_first_name);
        EditText lastNameTextView = view.findViewById(R.id.edit_text_last_name);
        EditText emailTextView = view.findViewById(R.id.edit_text_email_address_sign_up);
        EditText passwordTextView = view.findViewById(R.id.edit_text_password_sign_up);
        EditText confirmPasswordTextView = view.findViewById(R.id.edit_text_confirm_password);
        Button registerButton = view.findViewById(R.id.register_button);
        termsAndConditionsDialogFragment = new TermsAndConditionsDialogFragment(this);
        mTermsCheck = view.findViewById(R.id.check_box_terms_condition);
        mTermsCheck.setOnClickListener(termsListener -> {
            if (mTermsCheck.isChecked()) {
                termsAndConditionsDialogFragment.
                        show(requireActivity().getSupportFragmentManager(), "terms_conditions");
            }
        });
        registerButton.setOnClickListener( registerButtonListener -> {
            if (firstNameTextView.getText().toString().equals(""))
                displayRequiredFieldSnackBar(firstNameTextView, "please enter you first name to register");
            else if (lastNameTextView.getText().toString().equals(""))
                displayRequiredFieldSnackBar(lastNameTextView, "please enter your last name to register");
            else if (emailTextView.getText().toString().equals(""))
                displayRequiredFieldSnackBar(emailTextView, "please enter your email address to register");
            else if (passwordTextView.getText().toString().equals(""))
                displayRequiredFieldSnackBar(passwordTextView, "please enter a password to register");
            else if (confirmPasswordTextView.getText().toString().equals(""))
                displayRequiredFieldSnackBar(confirmPasswordTextView, "please confirm the password to register");
            else if (!confirmPasswordTextView.getText().toString().equals(passwordTextView.getText().toString()))
                displayRequiredFieldSnackBar(confirmPasswordTextView, "please confirm password is different from the password above");
            else if (!mTermsCheck.isChecked()){
                Snackbar.make(view, R.string.terms_and_conditions, BaseTransientBottomBar.LENGTH_LONG).show();
                termsAndConditionsDialogFragment.show(requireActivity().getSupportFragmentManager(), "terms_conditions");
            }
            else{
                Toast.makeText(requireContext(), "You are ready to register", Toast.LENGTH_SHORT).show();
                String email = emailTextView.getText().toString();
                String password = passwordTextView.getText().toString();
                String firstName = firstNameTextView.getText().toString();
                String lastName = lastNameTextView.getText().toString();
                mUserName = firstName +" "+ lastName;
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

    // this method is replaced by the one below it
/*    private void displayRequiredFieldToast(EditText requiredField, String message) {
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();
        requiredField.requestFocus();
    }*/
    private void showKeyboardOnEditText (EditText editText){
        editText.requestFocus();
        InputMethodManager manager = (InputMethodManager) requireActivity().
                getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }
    private void displayRequiredFieldSnackBar(EditText requiredField, String message){
        Snackbar snackbar = Snackbar.make(snackBarView, message, Snackbar.LENGTH_LONG);
        int action = R.string.fix;
        snackbar.setAction(action, listener-> {
            showKeyboardOnEditText(requiredField);
        });
        snackbar.show();
    }
    @Override
    public void onActionClick(boolean isAgree) {
        mTermsCheck.setChecked(isAgree);
    }
}
