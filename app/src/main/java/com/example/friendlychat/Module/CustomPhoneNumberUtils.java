package com.example.friendlychat.Module;

import android.telephony.PhoneNumberUtils;

import androidx.annotation.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CustomPhoneNumberUtils {
    private static Set<String> mContactsPhoneNumbers = new HashSet<>();
    private  String val;

    public CustomPhoneNumberUtils (String val){
        this.val = val;
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

        CustomPhoneNumberUtils number =  (CustomPhoneNumberUtils) obj;
        return PhoneNumberUtils.compare(this.getVal(), number.getVal());
    }

    public static Set<CustomPhoneNumberUtils> getCommonPhoneNumbers (List<String> phoneNumbersFromServer,
                                                               List<String> phoneNumberFromContactContactProvider) {
        Set<CustomPhoneNumberUtils> common = new HashSet<>();
        Set<CustomPhoneNumberUtils> phoneNumbers = new HashSet<>();
        for (String s: phoneNumbersFromServer){
            phoneNumbers.add(new CustomPhoneNumberUtils(s));
        }
        for (String s: phoneNumberFromContactContactProvider){
            CustomPhoneNumberUtils customPhoneNumberUtils = new CustomPhoneNumberUtils(s);
            if (phoneNumbers.contains(customPhoneNumberUtils)){
                common.add(customPhoneNumberUtils);
                mContactsPhoneNumbers.add(s);
            }
        }
        return common;
    }
}
