package com.example.farfish.data;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.farfish.Module.Message;
import com.example.farfish.Module.Status;
import com.example.farfish.Module.User;
import com.example.farfish.data.repositories.ChatsRepository;
import com.example.farfish.data.repositories.MessagingRepository;
import com.example.farfish.data.repositories.StatusRepository;
import com.example.farfish.data.repositories.UsersRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MainViewModel extends ViewModel {

    private static final String TAG = MainViewModel.class.getSimpleName();
    // LiveData
    private MutableLiveData<List<User>> allUsers;
    private MutableLiveData<List<Message>> userChats;
    private MutableLiveData<List<Message>> userMessages;
    private MutableLiveData<List<List<Status>>> statuesLists;
    // repositories
     public UsersRepository usersRepository;
     public ChatsRepository chatsRepository;
     public MessagingRepository messagingRepository;
     public StatusRepository statusRepository;



    @Inject
    public MainViewModel
            (
                    UsersRepository usersRepository,
                    ChatsRepository chatsRepository,
                    MessagingRepository messagingRepository,
                    StatusRepository statusRepository

            ) {
//        Context context = application.getApplicationContext();
        this.usersRepository = usersRepository;
        this.chatsRepository = chatsRepository;
        this.messagingRepository = messagingRepository;
        this.statusRepository = statusRepository;
    }

    public void init(Context context) {
        this.usersRepository = new UsersRepository(context);
        this.chatsRepository = new ChatsRepository(context);
        this.messagingRepository = new MessagingRepository(context);
        this.statusRepository = new StatusRepository(context);
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

    public LiveData<List<Message>> getChatMessages() {
        if (userMessages == null) {
            userMessages = new MutableLiveData<>(new ArrayList<>());
            messagingRepository.loadMessages();
        }
        return userMessages;
    }

    public LiveData<List<List<Status>>> getStatusLiveData() {
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
        int i = 0;
        for (Message message : messagingRepository.getMessages()) {
            if (!message.getIsRead()) {
                Log.d(TAG, "updateMessages: message is not read ");
                i++;
            }
        }
        Log.d(TAG, "updateMessages: number of unread messages is: " + i);
        userMessages.setValue(messagingRepository.getMessages());
    }

    public void clearChats() {
        userChats.setValue(new ArrayList<>());
    }

    public void updateStatues() {
        statuesLists.setValue(statusRepository.getStatusLists());
    }
}
