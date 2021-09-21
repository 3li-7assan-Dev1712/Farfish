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
    public static void saveUserStatus(Context context, String  status){
        SharedPreferences sharedPreferences = context.getSharedPreferences("messages", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user-status", status);
        editor.apply();
    }

    public static void saveUserPhoneNumber(Context context, String  phoneNumber){
        SharedPreferences sharedPreferences = context.getSharedPreferences("messages", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user-phone-number", phoneNumber);
        editor.apply();
    }
    public static String getUsePhoneNumber(Context context){
        return context.getSharedPreferences("messages", Context.MODE_PRIVATE).getString("user-phone-number", "----");
    }
    public static String getUsePhoto(Context context){
        return context.getSharedPreferences("messages", Context.MODE_PRIVATE).getString("user-photo", "photo");
    }

    public static String getUseStatus(Context context){
        return context.getSharedPreferences("messages", Context.MODE_PRIVATE).getString("user-status", "اللهم صلي وسلم على محمد");
    }
    public static void saveUserId(Context context, String userId){
        SharedPreferences sp = context.getSharedPreferences("messages", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("user_id", userId);
        editor.apply();
    }

    public static String getUserId(Context context){
        SharedPreferences sp = context.getSharedPreferences("messages", Context.MODE_PRIVATE);
        return sp.getString("user_id", "id");
    }

    public static void enableUsersFilter(Context context){
        SharedPreferences sp = context.getSharedPreferences("messages", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("filter", true);
        editor.apply();
    }

    public static void disableUsersFilter(Context context){
        SharedPreferences sp = context.getSharedPreferences("messages", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("filter", false);
        editor.apply();
    }

    public static boolean isFilterActive(Context context){
        SharedPreferences sp = context.getSharedPreferences("messages", Context.MODE_PRIVATE);
        return sp.getBoolean("filter", true);
    }

}
