package com.example.friendlychat.Module;

import android.content.Context;
import android.content.SharedPreferences;

public class FilterPreferenceUtils {

    public static void enableUsersFilter(Context context){
        SharedPreferences sp = context.getSharedPreferences("filter_utils", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("filter", true);
        editor.apply();
    }

    public static void disableUsersFilter(Context context){
        SharedPreferences sp = context.getSharedPreferences("filter_utils", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("filter", false);
        editor.apply();
    }

    public static boolean isFilterActive(Context context){
        SharedPreferences sp = context.getSharedPreferences("filter_utils", Context.MODE_PRIVATE);
        return sp.getBoolean("filter", true);
    }

}
