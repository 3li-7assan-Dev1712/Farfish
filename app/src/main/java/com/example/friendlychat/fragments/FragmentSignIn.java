package com.example.friendlychat.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.friendlychat.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;

public class FragmentSignIn extends Fragment {
    private static final String TAG = FragmentSignIn.class.getSimpleName();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        requireActivity().findViewById(R.id.bottom_nav).setVisibility(View.GONE);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_in, container, false);
        TextView emailSignInTextView = view.findViewById(R.id.editTextEmailSignIn);
        TextView passwordSignInTextView = view.findViewById(R.id.editTextPasswordSignIn);
        TextView forgotPassWord = view.findViewById(R.id.forgotPasswordSignIn);
        forgotPassWord.setOnClickListener( forgotPassWordListener -> {
            // forgot password functionality
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
            Navigation.findNavController(view).navigate(FragmentSignInDirections.actionFragmentSignInToFragmentSignUp());
        });
        return view;
    }

    private void signIn(String email, String password) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
       /* auth.signOut();*/
        AuthCredential credential = EmailAuthProvider.getCredential(email, password);
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                Toast.makeText(requireActivity(), "successfully sin in", Toast.LENGTH_SHORT).show();

                Log.d(TAG, "signIn: sign in successfully " + auth.getUid());
            }else if (task.getException() != null){
                auth.signInWithCredential(credential).addOnCompleteListener( comleteTask ->{
                    String id = comleteTask.getResult().getUser().getIdToken(true).toString();
                    Log.d(TAG, "signIn: id : " + id);
                }).addOnFailureListener(exc -> {
                    Log.d(TAG, "signIn: exception me, esage : " + exc.getMessage());
                });
            }
        }).addOnFailureListener(e -> {
            Log.d(TAG, "signIn: " +e.getMessage());
        });
    }

    private void displayRequiredFieldToast(String message) {
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();
    }

}
