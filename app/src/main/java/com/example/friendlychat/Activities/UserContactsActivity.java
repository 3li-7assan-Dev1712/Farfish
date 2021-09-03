package com.example.friendlychat.Activities;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.friendlychat.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class UserContactsActivity extends AppCompatActivity  {
    private static final String TAG = ContactsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_contacts);

    }

    @Override
    protected void onStart() {
        super.onStart();

          NavHostFragment f = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if ( f != null ){
            NavController navController = f.getNavController();
            BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
            NavigationUI.setupWithNavController(bottomNav, navController);

        }else{
            Toast.makeText(this, "error in Host fragment !", Toast.LENGTH_SHORT).show();
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