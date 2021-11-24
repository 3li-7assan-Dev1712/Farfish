package com.example.farfish.Module.util;

import android.content.Context;
import android.telephony.PhoneNumberUtils;

import androidx.annotation.Nullable;

import com.example.farfish.Module.dataclasses.User;
import com.example.farfish.Module.preferences.MessagesPreference;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CustomPhoneNumberUtils {
    public static List<User> allUsers = new ArrayList<>();
    private static Set<String> mContactsPhoneNumbers = new HashSet<>();
    private static List<User> usersUserKnow = new ArrayList<>();
    private String val;

    public CustomPhoneNumberUtils(String val) {
        this.val = val;
    }

    /**
     * this method is responsible for providing the common phone numbers algorithm, it compares
     * between two Sets one holds data from the server, and the second holds data from the Contacts ContentProvider.
     * takes O(nm)
     * n -=> the size of the Set from the ContactsContentProvider.
     * m -=> the size of the Set from the firestore database.
     *
     * @param phoneNumberFromContactContactProvider from the Contacts ContentProvider.
     * @param phoneNumbersFromServer                from the firestore database.
     * @param context                               the app context to be used for saving the common data in a SharedPreferences.
     */
    public static void storeCommonPhoneNumber(Set<String> phoneNumberFromContactContactProvider,
                                              Set<CustomPhoneNumberUtils> phoneNumbersFromServer,
                                              Context context) {

        for (String s : phoneNumberFromContactContactProvider) {
            if (phoneNumbersFromServer.contains(new CustomPhoneNumberUtils(s))) {
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

    public static List<User> getUsersUserKnow() {
        return usersUserKnow;
    }

    // this function is responsible for clearing up the lists after using them to free up some space.
    public static void clearLists() {
        allUsers.clear();
        usersUserKnow.clear();
        mContactsPhoneNumbers.clear();
    }

    public String getVal() {
        return val;
    }

    @Override
    public int hashCode() {
        return this.getVal().hashCode();
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
}
