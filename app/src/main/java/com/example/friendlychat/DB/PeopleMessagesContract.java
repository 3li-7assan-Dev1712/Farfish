package com.example.friendlychat.DB;

import android.net.Uri;
import android.provider.BaseColumns;

public class PeopleMessagesContract implements BaseColumns {
    public static final String AUTHORITY = "com.example.friendlychat";

    // The base content URI = "content://" + <authority>
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    // Define the possible paths for accessing data in this contract
    // This is the path for the "plants" directory
    public static final String PEOPLE_MESSAGES_PATH = "people";


    public static final class MessageEntry implements BaseColumns {

        // TaskEntry content URI = base content URI + path
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PEOPLE_MESSAGES_PATH).build();

        public static final String TABLE_NAME = "people";
        public static final String TABLE_NAME_INDIVIDUAL = "ALI";
        public static final String COLUMN_MESSAGE = "message";
        public static final String COLUMN_SENDER_NAME = "sender_name";
        public static final String COLUMN_PHOTO_URL = "photo_url";
    }
}
