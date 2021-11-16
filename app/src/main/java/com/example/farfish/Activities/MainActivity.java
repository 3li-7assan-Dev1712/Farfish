package com.example.farfish.Activities;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.farfish.R;
import com.example.farfish.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * the main activity of the app it contains the host fragment
 * and then to navigate between the different fragments
 * using the navigation component.
 */
@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    private final String TAG = MainActivity.class.getSimpleName();

    private ActivityMainBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        mBinding = LayoutInflater.from(getApplicationContext()).inflate(R.layout.activity_main, mBinding.getRoot());
        setContentView(R.layout.activity_main);


    }

    @Override
    protected void onStart() {
        super.onStart();

        NavHostFragment f = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (f != null) {
            NavController navController = f.getNavController();
            BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
            NavigationUI.setupWithNavController(bottomNav, navController);

        } else {
            Toast.makeText(this, "error in Host fragment !", Toast.LENGTH_SHORT).show();
        }

    }


}