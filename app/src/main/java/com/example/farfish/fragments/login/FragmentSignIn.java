package com.example.farfish.fragments.login;

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

import com.example.farfish.Module.dataclasses.User;
import com.example.farfish.Module.preferences.MessagesPreference;
import com.example.farfish.Module.preferences.SharedPreferenceUtils;
import com.example.farfish.Module.util.Connection;
import com.example.farfish.R;
import com.example.farfish.databinding.FragmentSignInBinding;
import com.example.farfish.fragments.dialogs.InternetConnectionDialog;
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

/**
 * The Sign in fragment through it the user login in the app
 */
public class FragmentSignIn extends Fragment {
    private static final String TAG = FragmentSignIn.class.getSimpleName();
    /*FirebaseUI*/
    private List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.EmailBuilder().build(),
            new AuthUI.IdpConfig.GoogleBuilder().build());
    // firebase auth
    private FirebaseAuth mAuth;
    /*Navigation*/
    private NavController mNavController;
    // snackbar view
    private View snackBarView;
    // Root class that contains al the views
    private FragmentSignInBinding mBinding;
    private ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            this::onSignInResult
    );

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().finish();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
        mAuth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentSignInBinding.inflate(inflater, container, false);
        View view = mBinding.getRoot();
        snackBarView = view;
        mNavController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        requireActivity().findViewById(R.id.bottom_nav).setVisibility(View.GONE);
        // this will check the permission first
        InternetConnectionDialog internetDialog = new InternetConnectionDialog();
        mBinding.buttonLogin.setOnClickListener(loginButtonListener -> {

            if (mBinding.signInHorizontalProgressBar.getVisibility() == View.VISIBLE) return;
            String email = mBinding.editTextEmailSignIn.getText().toString();
            String password = mBinding.editTextPasswordSignIn.getText().toString();
            Log.d(TAG, "onCreateView: email is: " + email);
            Log.d(TAG, "onCreateView: password is: " + password);
            if (email.equals(""))
                displayRequiredFieldToast(getString(R.string.required_field_email));
            else if (password.equals(""))
                displayRequiredFieldToast(getString(R.string.required_field_password));
            else if (!Connection.isUserConnected(requireContext()))
                internetDialog.show(requireActivity().getSupportFragmentManager(), "internet_alert");
            else {
                showHorizontalProgressBar(true);
                // sign in functionality
                Toast.makeText(requireContext(), requireContext().getString(R.string.agree_message), Toast.LENGTH_SHORT).show();
                signIn(email, password);
            }

        });

        // when user forgot their password
        mBinding.forgotPasswordSignIn.setOnClickListener(forgotPassWordListener ->
        {
            // for checking internet connection
            // forgot password functionality
            showHorizontalProgressBar(true);
            String email = mBinding.editTextEmailSignIn.getText().toString();
            if (email.equals("")) {
                Snackbar.make(snackBarView, R.string.enter_email, Snackbar.LENGTH_LONG)
                        .setAction(R.string.insert_email, actionFocus -> showKeyboardOnEditText(mBinding.editTextEmailSignIn)).show();
                showHorizontalProgressBar(false);
            } else if (!Connection.isUserConnected(requireContext())) {
                internetDialog.show(requireActivity().getSupportFragmentManager(), "internet_alert");
                showHorizontalProgressBar(false);
            } else {
                mAuth.sendPasswordResetEmail(email)
                        .addOnSuccessListener(message -> {
                            Log.d(TAG, "onCreateView: " + message);
                            showHorizontalProgressBar(false);
                            Toast.makeText(requireActivity(), getString(R.string.foregot_password_msg), Toast.LENGTH_SHORT).show();
                        }).addOnFailureListener(exc -> {
                            showHorizontalProgressBar(false);
                            Log.d(TAG, "onCreateView: forgot password exception: " + exc.getMessage());
                        }
                );
            }
        });
        TextView register = view.findViewById(R.id.register_sign_in);
        register.setOnClickListener(registerTextView ->
                mNavController.navigate(FragmentSignInDirections.actionFragmentSignInToFragmentSignUp()));
        return view;
    }

    /**
     * the method hide or show the horizontal progress bar at the top of the fragment
     *
     * @param showVisibility takes this boolean type to determine whether to hide or show
     *                       the progress bar.
     */
    private void showHorizontalProgressBar(boolean showVisibility) {
        if (showVisibility)
            mBinding.signInHorizontalProgressBar.setVisibility(View.VISIBLE);
        else {
            if (mBinding != null) {
                mBinding.signInHorizontalProgressBar.setVisibility(View.GONE);
            }
        }
    }

    /**
     * this method is called after the user input the required fields in the screen to be able
     * to sign in the app.
     * the method checks for any invalid data and throw an exception of so.
     *
     * @param email    the email of the user that they used to register in the app before.
     * @param password the password they input to register.
     */
    private void signIn(String email, String password) {
        Log.d(TAG, "signIn: ");
        mAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(authResult -> {
            Log.d(TAG, "signIn: user id " + Objects.requireNonNull(authResult.getUser()).getIdToken(true));
            // after checking the user id will be saved the the app flow will be completed
            updateUserInfoAndNavigateBack();
        }).addOnFailureListener(e -> {
            Log.d(TAG, "signIn: exception message: " + e.getMessage());
            if (e.getMessage().equals("There is no user record corresponding to this identifier. The user may have been deleted.")) {
                showSnackBarWithAction(R.string.notRegsitered, R.id.register_button, null);
                showHorizontalProgressBar(false);
            }
            try {
                throw e;
            } catch (FirebaseAuthEmailException emailException) {
                Log.d(TAG, "signIn: email exception " + emailException.getMessage());
                showSnackBarWithAction(R.string.wrong_email, R.string.modify, emailException);
            } catch (FirebaseAuthInvalidCredentialsException invalidCredentialException) {
                Log.d(TAG, "signIn: invalid credential exception " + invalidCredentialException.getMessage());
                showSnackBarWithAction(R.string.wrong_password, R.string.modify, invalidCredentialException);
            } catch (FirebaseAuthUserCollisionException collisionException) {
                Log.d(TAG, "signIn: Collision Exception: " + collisionException.getMessage());
            } catch (FirebaseNoSignedInUserException signInException) {
                Log.d(TAG, "signIn: user should register " + signInException.getMessage());
                showSnackBarWithAction(R.string.notRegsitered, R.string.register, signInException);
            } catch (Exception ex) {
                ex.printStackTrace();
                Log.d(TAG, "signIn: general exception: " + ex.getMessage());
            } finally {
                showHorizontalProgressBar(false);
            }
        });
    }

    /**
     * after ensure the user inserted valid data and successfully login the app this method is called to store and update
     * the user information as well ass navigates to the UserChatsFragment.
     */
    private void updateUserInfoAndNavigateBack() {

        showHorizontalProgressBar(false);
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        SharedPreferenceUtils.saveUserSignIn(requireContext());
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // after the user sign in/up saving their information in the firestore
            firestore.collection("rooms").document(currentUser.getUid())
                    .get().addOnSuccessListener(result -> {
                User user = result.toObject(User.class);
                if (user != null) {
                    String userName = user.getUserName();
                    String photoUrl = user.getPhotoUrl();
                    String userId = currentUser.getUid();
                    String phoneNumber = user.getPhoneNumber();
                    String userStatus = user.getStatus();
                    boolean isPublic = user.getIsPublic();
                    saveUserDataInSharedPreference(userName, photoUrl, userId, userStatus, phoneNumber, isPublic);
                    mNavController.navigate(R.id.action_fragmentSignIn_to_userChatsFragment);
                }
            }).addOnFailureListener(exc -> {
                showHorizontalProgressBar(false);
                Log.d(TAG, "updateUserInfoAndNavigateBack: exception: " + exc.getMessage());
                // save default
                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                String userId = firebaseUser.getUid();
                Uri photoUri = firebaseUser.getPhotoUrl();
                String email = firebaseUser.getEmail();
                String photoUrl = "";
                if (photoUri != null) {
                    photoUrl = photoUri.toString();
                }
                String userName = firebaseUser.getDisplayName();
                String phoneNumber = firebaseUser.getPhoneNumber();
                User user = new User(userName, email, phoneNumber, photoUrl, userId, "اللهم صلي وسلم على محمد", true, false, new Date().getTime());
                String finalPhotoUrl = photoUrl;
                firestore.collection("rooms").document(userId).set(user).addOnSuccessListener(suc -> {
                    saveUserDataInSharedPreference(userName, finalPhotoUrl, userId, "اللهم صلي وسلم على محمد", phoneNumber, false);
                    mNavController.navigateUp();
                }).addOnFailureListener(exception -> Log.d(TAG, "updateUserInfoAndNavigateBack: exception: " + exception.getMessage()));

            });
        }

    }

    /**
     * this method takes the user info and store them in a SharedPreferences to be used
     * later in the app flow.
     *
     * @param userName    the user name that visible to other users.
     * @param photoUrl    the photo url to display for other user and in the ProfileFragment as well.
     * @param userId      the user id to be used in the app logic.
     * @param status      a status a bio the user likes to be in their profile.
     * @param phoneNumber the phone number of the user to be used the UserFragment show them for other users
     *                    may know them.
     * @param isPublic    a boolean type to indicate whether the user likes to visible for all users (public user)
     *                    or just to their contacts (private user).
     */
    private void saveUserDataInSharedPreference(String userName, String photoUrl, String userId, String status, String phoneNumber, boolean isPublic) {
        Context context = requireContext();
        MessagesPreference.saveUserName(context, userName);
        MessagesPreference.saveUserId(context, userId);
        MessagesPreference.saveUserPhotoUrl(context, photoUrl);
        MessagesPreference.saveUserStatus(context, status);
        MessagesPreference.saveUserPhoneNumber(context, phoneNumber);
        MessagesPreference.saveUserPrivacy(context, isPublic);
        SharedPreferenceUtils.saveUserSignIn(context);
    }

    /**
     * when the user ignore a required field this method will be called to notify the user that
     * a specific field is required and must be filled in.
     *
     * @param message takes a message to display it to the user.
     */
    private void displayRequiredFieldToast(String message) {
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * the method is called from the FirebaseUI after the user use it sign in with app.
     *
     * @param result takes a FirebaseAuthUIAuthenticationResult instance as result.
     */
    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {

        if (result.getResultCode() == Activity.RESULT_OK) {
            // Successfully signed in
            Toast.makeText(requireContext(), requireContext().getString(R.string.sign_in_success_msg), Toast.LENGTH_SHORT).show();
            Log.d(TAG, "sign in successfully");

            updateUserInfoAndNavigateBack();
            // ...
        } else {
            Log.d(TAG, "onSignInResult" + " should finish the Activity");
            Log.d(TAG, String.valueOf(result.getResultCode()));
            requireActivity().finish();
        }
    }

    /**
     * launcher method to launch the firebase UI to let the user sign in easily.
     */
    private void launchFirebaseUI() {
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(true)
                .setLogo(R.drawable.ic_icon_round)
                .build();
        signInLauncher.launch(signInIntent);
    }

    /**
     * this method do show th keyboard, it is called when the user try to sigin in without filling all
     * the required fields.
     *
     * @param editText takes it to show on it the keyboard.
     */
    private void showKeyboardOnEditText(EditText editText) {
        editText.requestFocus();
        InputMethodManager manager = (InputMethodManager) requireActivity().
                getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    /**
     * when a exception occurs when the user try to sign in this method will be called
     * to notify the user about the exception that happened.
     *
     * @param label     the label or the title of the message that explains the problem, it's a string resource to
     *                  provide the translation for English and Arabic languages.
     * @param action    the suggested action the user should take it's also a string resource for the same reason eg:
     *                  (R.string.register)
     * @param exception the exception tha has occur which it's used to suggest an action to be taken.
     */
    private void showSnackBarWithAction(int label, int action, Exception exception) {
        Snackbar snackbar = Snackbar.make(snackBarView, label, Snackbar.LENGTH_LONG);
        if (exception != null) {
            if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                snackbar.setAction(action, snackbarListener -> showKeyboardOnEditText(mBinding.editTextPasswordSignIn));
            } else if (exception instanceof FirebaseAuthEmailException) {
                snackbar.setAction(action, snackbarListener -> {
                    showKeyboardOnEditText(mBinding.editTextEmailSignIn);
                });
            } else if (exception instanceof FirebaseNoSignedInUserException) {
                snackbar.setAction(action, snackbarListener -> mNavController.navigate(R.id.action_fragmentSignIn_to_fragmentSignUp));
            }
        } else {
            snackbar.setAction(action, snackBarAction -> mNavController.navigate(R.id.action_fragmentSignIn_to_fragmentSignUp));
        }
        snackbar.show();
    }

    /**
     * this method is override to free up the binding resource when the fragment is destroyed.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}
