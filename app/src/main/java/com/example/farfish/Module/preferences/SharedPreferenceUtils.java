package com.example.farfish.Module.preferences;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceUtils {

    public static void saveUserSignIn(Context context){
        SharedPreferences sp = context.getSharedPreferences("user_state", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("user_sign_in", true);
        editor.apply();
    }

    public static void saveUserSignOut(Context context){
        SharedPreferences sp = context.getSharedPreferences("user_state", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("user_sign_in", false);
        editor.apply();
        resetUserInfo(context);
    }

    private static void resetUserInfo(Context context) {
        SharedPreferences sp = context.getSharedPreferences("messages", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("user-name", "none");
        editor.putString("photo-url", "no photo");
        editor.putString("user-status", "no status");
        editor.putString("user_id", "0");
        editor.putString("user-phone-number", "0");
        editor.apply();

    }

    public static boolean getUserState(Context context){
        SharedPreferences sp = context.getSharedPreferences("user_state", Context.MODE_PRIVATE);
        return sp.getBoolean("user_sign_in", false);
    }


}
