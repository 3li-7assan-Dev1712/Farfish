package com.example.friendlychat.Module;

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
    }

    public static boolean getUserState(Context context){
        SharedPreferences sp = context.getSharedPreferences("user_state", Context.MODE_PRIVATE);
        return sp.getBoolean("user_sign_in", false);
    }


}
