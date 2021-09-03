package com.example.friendlychat.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.friendlychat.Adapters.ContactsAdapter;
import com.example.friendlychat.Module.FullMessage;
import com.example.friendlychat.Module.MessagesPreference;
import com.example.friendlychat.Module.SharedPreferenceUtils;
import com.example.friendlychat.Module.User;
import com.example.friendlychat.R;
import com.example.friendlychat.SignUpActivity;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class UserContactsActivity extends AppCompatActivity  {
    private static final String TAG = ContactsActivity.class.getSimpleName();
    private FirebaseAuth mAuth;
    private List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.EmailBuilder().build(),
            new AuthUI.IdpConfig.GoogleBuilder().build());
    private ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            this::onSignInResult
    );
    private List<FullMessage> fullMessages;
    private ContactsAdapter contactsAdapter;
    private FirebaseFirestore mFirestore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_contacts);
        mFirestore = FirebaseFirestore.getInstance();
        setSupportActionBar(findViewById(R.id.mainToolbar));
       /* RecyclerView contactsRecycler = findViewById(R.id.userContactsRecyclerView);*/

       /* contactsRecycler.setAdapter(contactsAdapter);*/

        /*firebase database & auth*/
        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    protected void onStart() {
        super.onStart();
        /*firstly check if the user is signed in or not and interact accordingly*/
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            // there's a user go and sign in
            /*String userName = currentUser.getDisplayName();
            MessagesPreference.saveUserName(this, userName);

            */
            /*NavHostFragment navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment);*/
            //NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
            NavHostFragment f = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
            if ( f != null ){
                NavController navController = f.getNavController();
                BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
                NavigationUI.setupWithNavController(bottomNav, navController);

            }else{
                Toast.makeText(this, "Masorah !", Toast.LENGTH_SHORT).show();
            }
        }else{
            // three's no user, ge and sign up
            launchFirebaseUI();
        }
    }

    private void launchFirebaseUI() {
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(false)
                .setLogo(R.drawable.ui_logo)
                .build();
        signInLauncher.launch(signInIntent);
    }

   /* private void initializeUserAndData() {

        mFirestore.collection("rooms").document(mAuth.getUid())
                .collection("chats").addSnapshotListener((value, error) -> {
            if (error != null){
                Toast.makeText(this, "Error reading message", Toast.LENGTH_SHORT).show();
            }else{
                String source = value != null && value.getMetadata().hasPendingWrites()
                        ? "Local" : "Server";
                Log.d(TAG, source);
                Toast.makeText(UserContactsActivity.this, source, Toast.LENGTH_SHORT).show();
                updateUI(value);
            }
        });

    }

    private void updateUI(QuerySnapshot value) {
        if (value != null) {

            for (DocumentChange dc : value.getDocumentChanges()) {
                Toast.makeText(this, "document has changed ", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "document change in User Contacts Activity");
                FullMessage fullMessage = dc.getDocument().toObject(FullMessage.class);
                String upComingId = fullMessage.getTargetUserId();
                for (int i = 0 ; i < fullMessages.size(); i ++){
                    String toBeReplaceId = fullMessages.get(i).getTargetUserId();
                    if (toBeReplaceId.equals(upComingId)) fullMessages.remove(i);
                }


                fullMessages.add(fullMessage);

            }
            contactsAdapter.notifyDataSetChanged();
        }
    }*/


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
       switch (id) {
           case R.id.sign_out:
               mAuth.signOut();
               Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show();
               SharedPreferenceUtils.saveUserSignOut(this);
               fullMessages.clear();
               launchFirebaseUI();
               break;
           case R.id.see_all_users:
               Intent seeAllUsersIntent = new Intent(this, ContactsActivity.class);
               startActivity(seeAllUsersIntent);

               break;
           case R.id.action_see_sign_in:
               Intent seeSignUpActivity = new Intent(this, SignUpActivity.class);
               startActivity(seeSignUpActivity);
               break;

       }
        return true;
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        /*IdpResponse response = result.getIdpResponse();*/
        Log.d(TAG, "result: " + result.getResultCode());
        Log.d(TAG, "onSignInResult");
        if (result.getResultCode() == RESULT_OK) {
            // Successfully signed in
            Toast.makeText(this, "You've signed in successfully", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "sign in successfully");
            SharedPreferenceUtils.saveUserSignIn(this);
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                /*after the user sign in/up saving their information in the firestore*/
                String userName = currentUser.getDisplayName();
                MessagesPreference.saveUserName(this, userName);
                String phoneNumber = currentUser.getPhoneNumber();
                String photoUrl = Objects.requireNonNull(currentUser.getPhotoUrl()).toString();
                String userId = mAuth.getUid();
                MessagesPreference.saveUserId(this, userId);
                MessagesPreference.saveUserPhotoUrl(this, photoUrl);
                long lastTimeSeen = new Date().getTime();
                User newUser = new User(userName, phoneNumber, photoUrl, userId, true, false,lastTimeSeen);
                assert userId != null;
                mFirestore.collection("rooms").document(userId).set(newUser).addOnCompleteListener(task ->
                        Toast.makeText(UserContactsActivity.this, "saved new user successfully", Toast.LENGTH_SHORT).show()
                );
               /* initializeUserAndData();*/
            }
            // ...
        } else{
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            // ...
            Log.d(TAG, "onSignInResult"+ " should finish the Activity");
            Log.d(TAG, String.valueOf(result.getResultCode()));

            finish();
        }
    }



    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");

    }

}