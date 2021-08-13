package com.example.friendlychat.Module;

import android.content.Context;
import android.content.SharedPreferences;

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


    public static void saveUserPhotoUrl(Context context, String  photoUrl){
        SharedPreferences sharedPreferences = context.getSharedPreferences("messages", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user-photo", photoUrl);
        editor.apply();
    }

    public static String getUsePhoto(Context context){
        return context.getSharedPreferences("messages", Context.MODE_PRIVATE).getString("user-photo", "photo");
    }

}
