package com.example.farfish.data;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.farfish.Module.Message;
import com.example.farfish.Module.User;
import com.example.farfish.data.repositories.ChatsRepository;
import com.example.farfish.data.repositories.UsersRepository;

import java.util.ArrayList;
import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private static final String TAG = MainViewModel.class.getSimpleName();
    private MutableLiveData<List<User>> allUsers;
    private MutableLiveData<List<Message>> userChats;
    private UsersRepository usersRepository;
    private ChatsRepository chatsRepository;

    public MainViewModel(@NonNull Application application) {
        super(application);
        Context context = application.getApplicationContext();
        usersRepository = new UsersRepository(context);
        chatsRepository = new ChatsRepository(context);
    }

    public UsersRepository getUsersRepository() {
        return usersRepository;
    }

    public ChatsRepository getChatsRepository() {
        return chatsRepository;
    }

    public LiveData<List<User>> getAllUsers() {
        if (allUsers == null) {
            allUsers = new MutableLiveData<>(new ArrayList<>());
            usersRepository.loadUsers();
        }
        return allUsers;
    }

    public LiveData<List<Message>> getUserChats() {
        Log.d(TAG, "getUserChats: getting chats");
        if (userChats == null) {
            userChats = new MutableLiveData<>(new ArrayList<>());
        }
        chatsRepository.loadAllChats();
        return userChats;
    }

    public void updateUsers(boolean fromContacts) {
        allUsers.setValue(usersRepository.getUsers(fromContacts));
    }

    public void updateChats() {
        userChats.setValue(chatsRepository.getUserChats());
    }
}
