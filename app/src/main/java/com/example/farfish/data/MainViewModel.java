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

    // return after repairing a broken head
    // Tag for logging
    private final String TAG = MainViewModel.class.getSimpleName();
    // repositories
    public UsersRepository usersRepository;
    public ChatsRepository chatsRepository;
    public MessagingRepository messagingRepository;
    public StatusRepository statusRepository;
    // MutableLiveData
    private MutableLiveData<List<User>> allUsers;
    private MutableLiveData<List<Message>> userChats;
    private MutableLiveData<List<Message>> userMessages;
    private MutableLiveData<List<List<Status>>> statuesLists;

    /**
     * a Hilt injected constructor to instantiate the main repos for the application
     * functionality, to make anything ready for the fragment to just display them
     * in the screen.
     *
     * @param usersRepository     users repo to provide the users logic (getting all users, users user may know etc...)
     * @param chatsRepository     this repos is responsible for providing the logic of the main chats to set the data to the
     *                            Messaging list adapter to display the data in the UserChatsFragment.
     * @param messagingRepository a repo for providing the chats functionality (send and receive messages and photos
     *                            display the target user info in the toolbar etc...)
     * @param statusRepository    a status repo for providing all the status logic (getting the interested statuses upload text and image statuses and
     *                            display them)
     */
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


    /**
     * userRepo getter
     *
     * @return the userRepository object.
     */
    public UsersRepository getUsersRepository() {
        return usersRepository;
    }

    /**
     * chatRepo getter
     *
     * @return the chatsRepository object.
     */
    public ChatsRepository getChatsRepository() {
        return chatsRepository;
    }

    /**
     * messagingRepo getter
     *
     * @return the messagingRepository object.
     */
    public MessagingRepository getMessagingRepository() {
        return messagingRepository;
    }

    /**
     * statusRepo getter
     *
     * @return the statusRepository object.
     */
    public StatusRepository getStatusRepository() {
        return statusRepository;
    }

    /**
     * this method is used in the UsersFragment to keep track of the users and
     * display a list of users in a RecyclerView.
     *
     * @return the allUser live data.
     */
    public LiveData<List<User>> getAllUsers() {
        if (allUsers == null) {
            allUsers = new MutableLiveData<>();
            usersRepository.loadUsers();
        }
        return allUsers;
    }

    /**
     * the UserChatsFragment calls this method to get all the chats to be displayed in its RecyclerView and
     * keep track of any changes happen to them.
     *
     * @return the userChats LiveData.
     */
    public MutableLiveData<List<Message>> getUserChats() {
        Log.d(TAG, "getUserChats: getting chats");
        if (userChats == null) {
            userChats = new MutableLiveData<>();
        }
        chatsRepository.loadAllChats();
        return userChats;
    }

    /**
     * when the user opens the ChatsFragment, this method will be called to get all
     * them messages of ths chat and keep track of any changes happen.
     *
     * @return the userMessages LiveData.
     */
    public MutableLiveData<List<Message>> getChatMessages() {
        if (userMessages == null) {
            userMessages = new MutableLiveData<>();
            messagingRepository.setPostMessagesInterface(this);
            Log.d(TAG, "getChatMessages: set post interface");
        }
        messagingRepository.loadMessages();
        return userMessages;
    }

    /**
     * this method is responsible of getting the statuesLists LiveData
     *
     * @return the statuesLists MutableLiveData.
     */
    public MutableLiveData<List<List<Status>>> getStatusLiveData() {
        if (statuesLists == null) {
            statuesLists = new MutableLiveData<>();
            statusRepository.loadAllStatuses();
        }
        return statuesLists;
    }


    /**
     * when a change is triggered this method is called to update the data.
     *
     * @param fromContacts a boolean type to determine which list will be displayed to
     *                     the user after updating the data.
     */
    public void updateUsers(boolean fromContacts) {
        if (allUsers != null)
            allUsers.setValue(usersRepository.getUsers(fromContacts));
    }

    /**
     * when a change is triggered this method is called to update the data.
     */
    public void updateChats() {
        Log.d(TAG, "updateChats: size after logout: " + chatsRepository.getUserChats().size());
        userChats.setValue(chatsRepository.getUserChats());
    }

    /**
     * when a change is triggered this method is called to update the messages.
     */
    public void updateMessages() {
        Log.d(TAG, "updateChats: size after logout: " + messagingRepository.getMessages().size());
        userMessages.setValue(messagingRepository.getMessages());
    }

    /**
     * when a change is triggered this method is called to update the status
     * they might be a new statues to display or removed.
     */
    public void updateStatues() {
        statuesLists.setValue(statusRepository.getStatusLists());
    }

    /**
     * a callback that called from a background thread to set a list messages in a specific chat
     *
     * @param messages a list of messages.
     */
    @Override
    public void postMessages(List<Message> messages) {
        userMessages.postValue(messages);
    }

}
