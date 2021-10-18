package com.example.farfish.data;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.farfish.Module.User;
import com.example.farfish.data.repositories.UsersRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainViewModel extends AndroidViewModel {

    private static final String TAG = MainViewModel.class.getSimpleName();
    private MutableLiveData<List<User>> allUsers;
    private UsersRepository usersRepository;

    public MainViewModel(@NonNull Application application) {
        super(application);
        usersRepository = new UsersRepository(application.getApplicationContext());
    }

    public UsersRepository getUsersRepository() {
        return usersRepository;
    }

    public LiveData<List<User>> getAllUsers() {
        if (allUsers == null) {
            allUsers = new MutableLiveData<>(new ArrayList<>());
            usersRepository.loadUsers();
        }
        return allUsers;
    }

    public void updateUsers(boolean fromContacts) {
        List<User> freshData = usersRepository.getUsers(fromContacts);
        if (freshData != null && freshData.size() > 0)
            allUsers.setValue(freshData);
    }

}
