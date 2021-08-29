package com.example.friendlychat.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.friendlychat.Adapters.ContactsAdapter;
import com.example.friendlychat.Module.User;
import com.example.friendlychat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ContactsActivity extends AppCompatActivity implements ContactsAdapter.OnChatClicked {
    private static final String TAG = ContactsActivity.class.getSimpleName();
    private FirebaseAuth mAuth;
    private List<User> users;
    private ContactsAdapter contactsAdapter;
    private FirebaseFirestore mFirestore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        mFirestore = FirebaseFirestore.getInstance();
        RecyclerView contactsRecycler = findViewById(R.id.contactsRecyclerView);
        users = new ArrayList<>();
        contactsAdapter = new ContactsAdapter(this, users, this);
        contactsRecycler.setAdapter(contactsAdapter);

        /*firebase database & auth*/
        mAuth = FirebaseAuth.getInstance();
        /*firstly check if the user is signed in or not and interact accordingly*/

        initializeUserAndData();

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }


    private void initializeUserAndData() {


        /*makeUserActive();*/
        mFirestore.collection("rooms").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                   for (DocumentChange dc: queryDocumentSnapshots.getDocumentChanges()){
                       User user = dc.getDocument().toObject(User.class);
                       String currentUserId = mAuth.getUid();
                       assert currentUserId != null;
                       /*if (!currentUserId.equals(user.getUserId()))*/
                       users.add(user);
                   }
                   contactsAdapter.notifyDataSetChanged();
                });

    }



    @Override
    public void onChatClicked(int position) {
        Toast.makeText(this, users.get(position).getUserName(), Toast.LENGTH_SHORT).show();
        String chatTitle = users.get(position).getUserName();
        String photoUrl = users.get(position).getPhotoUrl();
        Intent chatsIntent = new Intent(this, ChatsActivity.class);
        chatsIntent.putExtra(getResources().getString(R.string.chat_title), chatTitle);
        if (!chatTitle.equals("All people use the app")) {
            String targetUserId = users.get(position).getUserId();
            chatsIntent.putExtra(getResources().getString(R.string.targetUidKey), targetUserId);
            chatsIntent.putExtra(getResources().getString(R.string.photoUrl), photoUrl);
        }
        startActivity(chatsIntent);
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