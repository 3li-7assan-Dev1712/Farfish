package com.example.friendlychat.Module;

import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

/* In my chat app I want to know when the user close the app (navigate to another app, or
* go the home screen so I can save their last time activation my firestore server,
* this class demonstrates the process*/

public class AppStateDetector extends androidx.multidex.MultiDexApplication implements
        LifecycleObserver, SharedPreferences.OnSharedPreferenceChangeListener {

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private static final String TAG = AppStateDetector.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        getSharedPreferences("user_state", MODE_PRIVATE).registerOnSharedPreferenceChangeListener(this);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);

    }


    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void started() {
        Log.d("SampleLifeCycle", "ON_START");
        Toast.makeText(this, "Welcome back", Toast.LENGTH_SHORT).show();
        makeUserActive();
    }



    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void stopped() {
        getSharedPreferences("user_state", MODE_PRIVATE).unregisterOnSharedPreferenceChangeListener(this);
        Log.d("SampleLifeCycle", "ON_STOP");
        Toast.makeText(this, "Goodbye", Toast.LENGTH_SHORT).show();
        makeUserInActive();
    }

    private void makeUserInActive() {
        if (SharedPreferenceUtils.getUserState(this)) {
            String userId = mAuth.getUid();

            long lastTimeSeen = new Date().getTime();
            if (userId != null) {
                mFirestore.collection("rooms").document(userId)
                        .update(
                                "isActive", false,
                                "lastTimeSeen", lastTimeSeen
                        ).addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "made user inactive");
                    Toast.makeText(this, "User inactivated successfully", Toast.LENGTH_SHORT).show();
                });
            }
        }
    }

    private void makeUserActive() {
        if (SharedPreferenceUtils.getUserState(this)) {
            String userId = mAuth.getUid();
            if (userId != null) {
                mFirestore.collection("rooms").document(userId)
                        .update("isActive", true).addOnCompleteListener(task -> {
                    Log.d(TAG, "made user active");
                    Toast.makeText(this, "User activated successfully", Toast.LENGTH_SHORT).show();
                });
            }else
                Toast.makeText(this, "user id is null", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        boolean userIsSignIn = sharedPreferences.getBoolean(key, false);

        Log.d(TAG, "user state is: " + userIsSignIn);
        if (userIsSignIn)
            makeUserActive();
        else {
            forceUserToBeInActive();
        }
    }

    private void forceUserToBeInActive() {
        String userId = MessagesPreference.getUserId(this);
        mFirestore.collection("rooms").document(userId)
                .update("isActive", false).addOnCompleteListener(task -> {
            Log.d(TAG, "forced user to be in Active");
            Toast.makeText(this, "successfully done!", Toast.LENGTH_SHORT).show();
        });
    }



}
