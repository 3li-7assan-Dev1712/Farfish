package com.example.friendlychat.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.friendlychat.Module.MessagesPreference;
import com.example.friendlychat.Module.SharedPreferenceUtils;
import com.example.friendlychat.Module.User;
import com.example.friendlychat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.Objects;

public class FragmentSignUp extends Fragment {

    private static final String TAG = FragmentSignUp.class.getSimpleName();
    private NavController mNavController;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        requireActivity().findViewById(R.id.bottom_nav).setVisibility(View.GONE);
        super.onCreate(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);
        mNavController = Navigation.findNavController(view);
        Toolbar toolbar = view.findViewById(R.id.toolbar_sign_up);
        toolbar.setNavigationOnClickListener( navigationIcon -> {
            navigateUp(); // navigate back using the navigation icon
        });
        TextView loginTextView = view.findViewById(R.id.text_view_login);
        loginTextView.setOnClickListener(login -> {
            navigateUp();
        });
        EditText firstNameTextView = view.findViewById(R.id.edit_text_first_name);
        EditText lastNameTextView = view.findViewById(R.id.edit_text_last_name);
        EditText emailTextView = view.findViewById(R.id.edit_text_email_address_sign_up);
        EditText passwordTextView = view.findViewById(R.id.edit_text_password_sign_up);
        EditText confirmPasswordTextView = view.findViewById(R.id.edit_text_confirm_password);
        Button registerButton = view.findViewById(R.id.register_button);
        registerButton.setOnClickListener( registerButtonListener -> {
            if (firstNameTextView.getText().toString().equals(""))
                displayRequiredFieldToast(firstNameTextView, "please enter you first name to register");
            else if (lastNameTextView.getText().toString().equals(""))
                displayRequiredFieldToast(lastNameTextView, "please enter your last name to register");
            else if (emailTextView.getText().toString().equals(""))
                displayRequiredFieldToast(emailTextView, "please enter your email address to register");
            else if (passwordTextView.getText().toString().equals(""))
                displayRequiredFieldToast(passwordTextView, "please enter a password to register");
            else if (confirmPasswordTextView.getText().toString().equals(""))
                displayRequiredFieldToast(lastNameTextView, "please confirm the password to register");
            else if (!confirmPasswordTextView.getText().toString().equals(passwordTextView.getText().toString()))
                displayRequiredFieldToast(confirmPasswordTextView, "please confirm password is different from the password above");
            else{
                Toast.makeText(requireContext(), "You are ready to register", Toast.LENGTH_SHORT).show();
                String email = emailTextView.getText().toString();
                String password = passwordTextView.getText().toString();
                signUp(email, password);
            }
        });
        return view;
    }

    private void signUp(String email, String password) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(authResult -> {
            Log.d(TAG, "signUp: " +  Objects.requireNonNull(authResult.getUser()).getUid());
            saveUserInfoAndNavigateBack();
        }).addOnFailureListener(exception -> {
            Log.d(TAG, "signUp: exception message: " + exception.getMessage());
            Toast.makeText(requireActivity(), "An error occurred, you can navigate back and try with another way", Toast.LENGTH_SHORT).show();
        });
    }

    private void navigateUp() {
        mNavController.navigateUp();
    }

    private void displayRequiredFieldToast(EditText requiredField, String message) {
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();
        requiredField.requestFocus();
    }
    private void saveUserInfoAndNavigateBack() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
        SharedPreferenceUtils.saveUserSignIn(requireContext());
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            /*after the user sign in/up saving their information in the firestore*/
            String userName = currentUser.getDisplayName();
            MessagesPreference.saveUserName(requireContext(), userName);
            String phoneNumber = currentUser.getPhoneNumber();
            String photoUrl = Objects.requireNonNull(currentUser.getPhotoUrl()).toString();
            String userId = mAuth.getUid();
            MessagesPreference.saveUserId(requireContext(), userId);
            MessagesPreference.saveUserPhotoUrl(requireContext(), photoUrl);
            long lastTimeSeen = new Date().getTime();
            User newUser = new User(userName, phoneNumber, photoUrl, userId, true, false, lastTimeSeen);
            assert userId != null;
            mFirestore.collection("rooms").document(userId).set(newUser);
        }
        mNavController.popBackStack();
    }
}
