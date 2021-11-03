package com.example.farfish.data;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.farfish.Module.dataclasses.Message;
import com.example.farfish.Module.dataclasses.Status;
import com.example.farfish.Module.dataclasses.User;
import com.example.farfish.data.repositories.ChatsRepository;
import com.example.farfish.data.repositories.MessagingRepository;
import com.example.farfish.data.repositories.StatusRepository;
import com.example.farfish.data.repositories.UsersRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MainViewModel extends ViewModel implements MessagingRepository.PostMessagesInterface {

    // Tag for logging
    private static final String TAG = MainViewModel.class.getSimpleName();
    // MutableLiveData
    private MutableLiveData<List<User>> allUsers;
    private MutableLiveData<List<Message>> userChats;
    private MutableLiveData<List<Message>> userMessages;
    private MutableLiveData<List<List<Status>>> statuesLists;
    // repositories
    public UsersRepository usersRepository;
    public ChatsRepository chatsRepository;
    public MessagingRepository messagingRepository;
    public StatusRepository statusRepository;

    // the constructor
    @Inject
    public MainViewModel
    (
            UsersRepository usersRepository,
            ChatsRepository chatsRepository,
            MessagingRepository messagingRepository,
            StatusRepository statusRepository

    ) {
        Log.d(TAG, "MainViewModel: constructor");
        this.usersRepository = usersRepository;
        this.chatsRepository = chatsRepository;
        this.messagingRepository = messagingRepository;
        this.statusRepository = statusRepository;
    }


    // getters
    public UsersRepository getUsersRepository() {
        return usersRepository;
    }

    public ChatsRepository getChatsRepository() {
        return chatsRepository;
    }

    public MessagingRepository getMessagingRepository() {
        return messagingRepository;
    }

    public StatusRepository getStatusRepository() {
        return statusRepository;
    }

    public LiveData<List<User>> getAllUsers() {
        if (allUsers == null) {
            allUsers = new MutableLiveData<>();
            usersRepository.loadUsers();
        }
        return allUsers;
    }

    public MutableLiveData<List<Message>> getUserChats() {
        Log.d(TAG, "getUserChats: getting chats");
        if (userChats == null) {
            userChats = new MutableLiveData<>();
        }
        chatsRepository.loadAllChats();
        return userChats;
    }

    public MutableLiveData<List<Message>> getChatMessages() {
        if (userMessages == null) {
            userMessages = new MutableLiveData<>();
            messagingRepository.setPostMessagesInterface(this);
            Log.d(TAG, "getChatMessages: set post interface");
        }
        messagingRepository.loadMessages();
        return userMessages;
    }

    public MutableLiveData<List<List<Status>>> getStatusLiveData() {
        if (statuesLists == null) {
            statuesLists = new MutableLiveData<>();
            statusRepository.loadAllStatuses();
        }
        return statuesLists;
    }


    public void updateUsers(boolean fromContacts) {
        if (allUsers != null)
            allUsers.setValue(usersRepository.getUsers(fromContacts));
    }

    public void updateChats() {
        Log.d(TAG, "updateChats: size after logout: " + chatsRepository.getUserChats().size());
        userChats.setValue(chatsRepository.getUserChats());
    }

    public void updateMessages() {
        Log.d(TAG, "updateChats: size after logout: " + messagingRepository.getMessages().size());
        userMessages.setValue(messagingRepository.getMessages());
    }

    public void updateStatues() {
        statuesLists.setValue(statusRepository.getStatusLists());
    }

    @Override
    public void postMessages(List<Message> messages) {
        userMessages.postValue(messages);
    }

}
