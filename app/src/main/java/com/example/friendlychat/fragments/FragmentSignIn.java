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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthEmailException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.internal.api.FirebaseNoSignedInUserException;

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
    /*Navigation*/
    private NavController mNavController;
    // snackbar view
    private View snackBarView;
    // email - password edit texts
    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

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
        snackBarView = view;
        mNavController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        requireActivity().findViewById(R.id.bottom_nav).setVisibility(View.GONE);
        mEmailEditText = view.findViewById(R.id.editTextEmailSignIn);
        mPasswordEditText = view.findViewById(R.id.editTextPasswordSignIn);
        TextView forgotPassWord = view.findViewById(R.id.forgotPasswordSignIn);
        TextView tryAnotherWay = view.findViewById(R.id.tryAnotherWay);
        tryAnotherWay.setOnClickListener(tryAnotherWayListener -> launchFirebaseUI());
        Button loginButton = view.findViewById(R.id.buttonLogin);
        loginButton.setOnClickListener( loginButtonListener -> {
            String email = mEmailEditText.getText().toString();
            String password = mPasswordEditText.getText().toString();
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
        // when user forgot their password
        forgotPassWord.setOnClickListener( forgotPassWordListener -> {
            // forgot password functionality
            String email = mEmailEditText.getText().toString();
            if (email.equals("")){
                Snackbar.make(snackBarView, R.string.enter_email, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.insert_email, actionFocus -> showKeyboardOnEditText(mEmailEditText)).show();
            }else{

                mAuth.sendPasswordResetEmail(email)
                        .addOnSuccessListener(message -> {
                            Log.d(TAG, "onCreateView: " + message);
                            Toast.makeText(requireActivity(), "Send email successfully, now check you email", Toast.LENGTH_SHORT).show();
                        }).addOnFailureListener(exc -> Log.d(TAG, "onCreateView: forgot password exception: " + exc.getMessage()));
            }

        });
        TextView register = view.findViewById(R.id.register_sign_in);
        register.setOnClickListener( registerTextView -> mNavController.navigate(FragmentSignInDirections.actionFragmentSignInToFragmentSignUp()));
        return view;
    }

    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(authResult -> {
            Log.d(TAG, "signIn: user id " + Objects.requireNonNull(authResult.getUser()).getIdToken(true));
            // after checking the user id will be saved the the app flow will be completed
            updateUserInfoAndNavigateBack();
        }).addOnFailureListener(e -> {
            Log.d(TAG, "signIn: exception message: " + e.getMessage());
            if (e.getMessage().equals("There is no user record corresponding to this identifier. The user may have been deleted."))
                showSnackBarWithAction(R.string.notRegsitered, R.id.register_button, null);
            try {
                throw e;
            }catch (FirebaseAuthEmailException emailException){
                Log.d(TAG, "signIn: email exception " + emailException.getMessage());
                showSnackBarWithAction(R.string.wrong_email, R.string.modify, emailException);
            } catch (FirebaseAuthInvalidCredentialsException invalidCredentialException) {
                Log.d(TAG, "signIn: invalid credential exception " + invalidCredentialException.getMessage());
                showSnackBarWithAction( R.string.wrong_password,  R.string.modify, invalidCredentialException);
            }
            catch (FirebaseAuthUserCollisionException collisionException) {
                Log.d(TAG, "signIn: Collision Exception: " + collisionException.getMessage() );
            }catch (FirebaseNoSignedInUserException signInException) {
                Log.d(TAG, "signIn: user should register " + signInException.getMessage());
                showSnackBarWithAction(R.string.notRegsitered, R.string.register, signInException);
            } catch (Exception ex) {
                ex.printStackTrace();
                Log.d(TAG, "signIn: general exception: " +ex.getMessage());
            }
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
                    String phoneNumber = user.getPhoneNumber();
                    String userStatus = user.getStatus();
                    saveUserDataInSharedPreference(userName, photoUrl, userId, userStatus, phoneNumber);
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
                User user = new User(userName, phoneNumber, photoUrl, userId, "اللهم صلي وسلم على محمد" ,true, new Date().getTime());
                String finalPhotoUrl = photoUrl;
                firestore.collection("rooms").document(userId).set(user).addOnSuccessListener(suc -> {
                    saveUserDataInSharedPreference(userName, finalPhotoUrl, userId, "اللهم صلي وسلم على محمد", phoneNumber);
                    mNavController.navigateUp();
                }).addOnFailureListener(exception -> Log.d(TAG, "updateUserInfoAndNavigateBack: exception: " + exception.getMessage()));

            });
        }

    }

    private void saveUserDataInSharedPreference(String userName, String photoUrl, String userId, String status, String phoneNumber) {
        Context context = requireContext();
        MessagesPreference.saveUserName(context, userName);
        MessagesPreference.saveUserId(context, userId);
        MessagesPreference.saveUserPhotoUrl(context, photoUrl);
        MessagesPreference.saveUserStatus(context, status);
        MessagesPreference.saveUserPhoneNumber(context, phoneNumber);

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

    private void showKeyboardOnEditText (EditText editText){
        editText.requestFocus();
        InputMethodManager manager = (InputMethodManager) requireActivity().
                getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    private void showSnackBarWithAction(int label, int action, Exception exception){
        Snackbar snackbar = Snackbar.make(snackBarView, label, Snackbar.LENGTH_INDEFINITE);
        if (exception != null) {
            if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                snackbar.setAction(action, snackbarListener -> showKeyboardOnEditText(mPasswordEditText));
            } else if (exception instanceof FirebaseAuthEmailException) {
                snackbar.setAction(action, snackbarListener -> {
                    showKeyboardOnEditText(mEmailEditText);
                });
            } else if (exception instanceof FirebaseNoSignedInUserException) {
                snackbar.setAction(action, snackbarListener -> mNavController.navigate(R.id.action_fragmentSignIn_to_fragmentSignUp));
            }
        }else{
            snackbar.setAction(action, snackBarAction -> mNavController.navigate(R.id.action_fragmentSignIn_to_fragmentSignUp));
        }
        snackbar.show();
    }
}
