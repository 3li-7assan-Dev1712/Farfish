package com.example.farfish.Module.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

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

    public static void saveUserPrivacy(Context context, boolean isPublic){
        SharedPreferences sp = context.getSharedPreferences("messages", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("user_privacy", isPublic);
        editor.apply();
    }
    public static boolean userIsPublic(Context context){
        SharedPreferences sp = context.getSharedPreferences("messages", Context.MODE_PRIVATE);
        return sp.getBoolean("user_privacy", false);
    }

    public static String getUserId(Context context){
        SharedPreferences sp = context.getSharedPreferences("messages", Context.MODE_PRIVATE);
        return sp.getString("user_id", "id");
    }

    public static void saveCommonContacts(Context context, Set<String> contacts){
        SharedPreferences sp = context.getSharedPreferences("messages", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putStringSet("contacts", contacts);
        editor.apply();
    }

    public static void saveDeviceContacts(Context context, Set<String> deviceContacts){
        SharedPreferences sp = context.getSharedPreferences("messages", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putStringSet("device_contacts", deviceContacts);
        editor.apply();
    }


    public static Set<String> getUserContacts(Context context){
        SharedPreferences sp = context.getSharedPreferences("messages", Context.MODE_PRIVATE);
        return sp.getStringSet("contacts", null);
    }

    public static Set<String> getDeviceContacts(Context context) {
        SharedPreferences sp = context.getSharedPreferences("messages", Context.MODE_PRIVATE);
        return sp.getStringSet("device_contacts", null);
    }
}
