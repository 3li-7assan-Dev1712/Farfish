package com.example.friendlychat.DB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MessagesDbOpenHelper extends SQLiteOpenHelper {

    private static final String peopleMessagesTable = "people_message.db";
    private static final int tableVersion = 1;

    public MessagesDbOpenHelper(Context context){
        super(context, peopleMessagesTable, null, tableVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_PEOPLE_TABLE = "CREATE TABLE " + PeopleMessagesContract.MessageEntry.TABLE_NAME + " (" +
                PeopleMessagesContract.MessageEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                PeopleMessagesContract.MessageEntry.COLUMN_MESSAGE + " TEXT, " +
                PeopleMessagesContract.MessageEntry.COLUMN_PHOTO_URL + " TEXT, " +
                PeopleMessagesContract.MessageEntry.COLUMN_SENDER_NAME + " TEXT NOT NULL, " +
                PeopleMessagesContract.MessageEntry.COLUMN_MESSAGE_TIMESTAMP + " TIMESTAMP NOT NULL)";

        db.execSQL(SQL_CREATE_PEOPLE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PeopleMessagesContract.MessageEntry.TABLE_NAME);
        onCreate(db);
    }
}
