package com.example.friendlychat.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.friendlychat.R;

import java.util.Objects;

public class SignInActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        Objects.requireNonNull(getSupportActionBar()).hide();
    }
}