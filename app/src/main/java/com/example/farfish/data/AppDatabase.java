package com.example.farfish.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.farfish.Module.User;


@Database(entities = {User.class}, version = 1)
abstract public class AppDatabase extends RoomDatabase {
    private static final String DB_NAME = "app.db";

    // creating this abstract class is very expensive that's whey we should use the Single design pattern
    private static volatile AppDatabase instance;

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = create(context);
        }
        return instance;
    };

    private static AppDatabase create(final Context context) {
        return Room.databaseBuilder(
                context,
                AppDatabase.class,
                DB_NAME).build();
    }

    public abstract UsersDao getUserDao();
}
