package com.example.friendlychat.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.friendlychat.Module.MessagesPreference;
import com.example.friendlychat.Module.SharedPreferenceUtils;
import com.example.friendlychat.Module.User;
import com.example.friendlychat.R;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class FragmentSignIn extends Fragment {
    private static final String TAG = FragmentSignIn.class.getSimpleName();
    /*FirebaseUI*/
    private List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.EmailBuilder().build(),
            new AuthUI.IdpConfig.GoogleBuilder().build());
    private ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            this::onSignInResult
    );
    // firebase auth
    private FirebaseAuth mAuth;
    private  TextView tryAnotherWay;
    /*Navigation*/
    private NavController mNavController;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        requireActivity().findViewById(R.id.bottom_nav).setVisibility(View.GONE);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().finish();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_in, container, false);
        mNavController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        TextView emailSignInTextView = view.findViewById(R.id.editTextEmailSignIn);
        TextView passwordSignInTextView = view.findViewById(R.id.editTextPasswordSignIn);
        TextView forgotPassWord = view.findViewById(R.id.forgotPasswordSignIn);
        forgotPassWord.setOnClickListener( forgotPassWordListener -> {
            // forgot password functionality
        });
        tryAnotherWay = view.findViewById(R.id.tryAnotherWay);
        tryAnotherWay.setOnClickListener(tryAnotherWayListener -> {
            launchFirebaseUI();
        });
        Button loginButton = view.findViewById(R.id.buttonLogin);
        loginButton.setOnClickListener( loginButtonListener -> {
            String email = emailSignInTextView.getText().toString();
            String password = passwordSignInTextView.getText().toString();
            if (email.equals(""))
                displayRequiredFieldToast("Please enter you e-mail address to sign in");
            else if (password.equals(""))
                displayRequiredFieldToast("Please enter you password to sign in");
            else{
                // sign in functionality
                Toast.makeText(requireContext(), "You are ready to sign in", Toast.LENGTH_SHORT).show();
                signIn(email, password);
            }
        });
        TextView register = view.findViewById(R.id.register_sign_in);
        register.setOnClickListener( registerTextView -> {
            mNavController.navigate(FragmentSignInDirections.actionFragmentSignInToFragmentSignUp());
        });
        return view;
    }

    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(authResult -> {
            Log.d(TAG, "signIn: user id " + Objects.requireNonNull(authResult.getUser()).getIdToken(true));
            // after checking the user id will be saved the the app flow will be completed
            updateUserInfoAndNavigateBack();
        }).addOnFailureListener(e -> {
            Log.d(TAG, "signIn: exception message: " + e.getMessage());
            tryAnotherWay.setVisibility(View.VISIBLE);
        });
    }

    private void updateUserInfoAndNavigateBack() {

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        SharedPreferenceUtils.saveUserSignIn(requireContext());
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
           // after the user sign in/up saving their information in the firestore
            firestore.collection("rooms").document(currentUser.getUid())
                    .get().addOnSuccessListener( result -> {
                User user = result.toObject(User.class);
                if (user != null){
                    String userName = user.getUserName();
                    String photoUrl = user.getPhotoUrl();
                    String userId = currentUser.getUid();
                    saveUserDataInSharedPreference(userName, photoUrl, userId);
                    mNavController.navigateUp();
                }
            }).addOnFailureListener(exc -> {
                Log.d(TAG, "updateUserInfoAndNavigateBack: exception: " + exc.getMessage());
                Toast.makeText(requireActivity(), "Error sign in", Toast.LENGTH_SHORT).show();
                // save default
                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                String userId = firebaseUser.getUid();
                Uri photoUri = firebaseUser.getPhotoUrl();
                String photoUrl = "";
                if (photoUri != null){
                    photoUrl = photoUri.toString();
                }
                String userName = firebaseUser.getDisplayName();
                String phoneNumber = firebaseUser.getPhoneNumber();
                User user = new User(userName, phoneNumber, photoUrl, userId, true, new Date().getTime());
                String finalPhotoUrl = photoUrl;
                firestore.collection("rooms").document(userId).set(user).addOnSuccessListener(suc -> {
                    saveUserDataInSharedPreference(userName, finalPhotoUrl, userId);
                    mNavController.navigateUp();
                }).addOnFailureListener(exception -> {
                    Log.d(TAG, "updateUserInfoAndNavigateBack: exception: " + exception.getMessage());
                });

            });
        }

    }

    private void saveUserDataInSharedPreference(String userName, String photoUrl, String userId) {
        Context context = requireContext();
        MessagesPreference.saveUserName(context, userName);
        MessagesPreference.saveUserId(context, userId);
        MessagesPreference.saveUserPhotoUrl(context, photoUrl);
        SharedPreferenceUtils.saveUserSignIn(context);
    }

    private void displayRequiredFieldToast(String message) {
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {

        if (result.getResultCode() == Activity.RESULT_OK) {
            // Successfully signed in
            Toast.makeText(requireContext(), "You've signed in successfully", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "sign in successfully");

            updateUserInfoAndNavigateBack();
            // ...
        } else {
            Log.d(TAG, "onSignInResult" + " should finish the Activity");
            Log.d(TAG, String.valueOf(result.getResultCode()));
            requireActivity().finish();
        }
    }

    private void launchFirebaseUI() {
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(true)
                .setLogo(R.drawable.ic_icon_round)
                .build();
        signInLauncher.launch(signInIntent);
    }
}
