package com.example.farfish.Module.util;

import android.content.Context;
import android.telephony.PhoneNumberUtils;

import androidx.annotation.Nullable;

import com.example.farfish.Module.preferences.MessagesPreference;
import com.example.farfish.Module.dataclasses.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CustomPhoneNumberUtils {
    private static Set<String> mContactsPhoneNumbers = new HashSet<>();
    private String val;

    public static List<User> allUsers = new ArrayList<>();
    private static List<User> usersUserKnow = new ArrayList<>();
    public CustomPhoneNumberUtils(String val) {
        this.val = val;
    }

    public String getVal() {
        return val;
    }

    @Override
    public int hashCode() {
        return this.getVal().hashCode();
    }

    public static void storeCommonPhoneNumber(Set<String> phoneNumberFromContactContactProvider,
                                              Set<CustomPhoneNumberUtils> phoneNumbersFromServer,
                                              Context context) {

        for (String s : phoneNumberFromContactContactProvider) {
            /*    CustomPhoneNumberUtils customPhoneNumberUtils = ;*/
            if (phoneNumbersFromServer.contains(new CustomPhoneNumberUtils(s))) {
                /*     common.add(customPhoneNumberUtils);*/
                mContactsPhoneNumbers.add(s);
                for (User userUserKnow : allUsers) {
                    String localUserPhoneNumber = userUserKnow.getPhoneNumber();
                    if (PhoneNumberUtils.compare(s, localUserPhoneNumber)) {
                        usersUserKnow.add(userUserKnow);
                    }
                }
            }
        }
        MessagesPreference.saveCommonContacts(context, mContactsPhoneNumbers);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (this.getClass() != obj.getClass())
            return false;

        CustomPhoneNumberUtils number = (CustomPhoneNumberUtils) obj;
        return PhoneNumberUtils.compare(this.getVal(), number.getVal());
    }
    public static List<User> getUsersUserKnow(){
        return usersUserKnow;
    }

    // this function is responsible for clearing up the lists after using them to free up some space.
    public static void clearLists(){
        allUsers.clear();
        usersUserKnow.clear();
        mContactsPhoneNumbers.clear();
    }
}
