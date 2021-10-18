package com.example.farfish.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.farfish.Module.User;

import java.util.List;
import java.util.Set;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface UsersDao {

    @Query("SELECT * FROM User WHERE phoneNumber IN  (:numbers)  ORDER BY lastTimeSeen")
    LiveData<List<User>> getAllUsersUserMayKnow(Set<String> numbers);

    @Query("SELECT * FROM User WHERE (isPublic = :isPublic) ORDER BY lastTimeSeen")
    LiveData<List<User>> getAllPublicUser(boolean isPublic);

    @Insert(onConflict = REPLACE)
    void saveAllUsers(List<User> allUsers);

}
