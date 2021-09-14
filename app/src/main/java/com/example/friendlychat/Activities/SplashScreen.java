package com.example.friendlychat.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.friendlychat.R;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Handler().postDelayed( () -> {
            startActivity(new Intent(SplashScreen.this, MainActivity.class));
            finish();
        }, 1000);
    }
}