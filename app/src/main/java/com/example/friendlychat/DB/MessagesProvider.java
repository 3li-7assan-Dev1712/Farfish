package com.example.friendlychat.DB;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import org.jetbrains.annotations.NotNull;

public class MessagesProvider extends ContentProvider {
    public static final int PEOPLE = 100;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private static final String TAG = MessagesProvider.class.getName();
    private MessagesDbOpenHelper mMessagesDbHelper;
    public static UriMatcher buildUriMatcher() {
        // Initialize a UriMatcher
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        // Add URI matches
        uriMatcher.addURI(PeopleMessagesContract.AUTHORITY, PeopleMessagesContract.PEOPLE_MESSAGES_PATH, PEOPLE);
        return uriMatcher;
    }
    public MessagesProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(@NotNull Uri uri, ContentValues values) {
        final SQLiteDatabase sqLiteDatabase = mMessagesDbHelper.getWritableDatabase();
        final int INSERT_REQUEST = sUriMatcher.match(uri);
        if (INSERT_REQUEST == PEOPLE){
            long id = sqLiteDatabase.insert(PeopleMessagesContract.MessageEntry.TABLE_NAME, null, values);
            if (id > 0) {
                return ContentUris.withAppendedId(PeopleMessagesContract.MessageEntry.CONTENT_URI, id);
            } else {
                throw new android.database.SQLException("Failed to insert row into " + uri);
            }
        }else
            throw new UnsupportedOperationException("Could'nt recognize uri");
    }

    @Override
    public boolean onCreate() {
        mMessagesDbHelper = new MessagesDbOpenHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        final SQLiteDatabase db = mMessagesDbHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        Cursor retCursor;
        if (match == PEOPLE) {
            retCursor = db.query(PeopleMessagesContract.MessageEntry.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    sortOrder);
            return retCursor;
        }else
            throw new UnsupportedOperationException("Could'nt recognize uri");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
