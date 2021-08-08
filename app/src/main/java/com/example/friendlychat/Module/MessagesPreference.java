package com.example.friendlychat.Module;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.android.gms.common.util.SharedPreferencesUtils;

public class MessagesPreference {


    public static void saveUserName(Context context, String  userName){
        SharedPreferences sharedPreferences = context.getSharedPreferences("messages", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user-name", userName);
        editor.apply();
    }

    public static String getUserName(Context context){
        return context.getSharedPreferences("messages", Context.MODE_PRIVATE).getString("user-name", "Ali");
    }

}
