package com.lhn.watchandlearn.ui.utils;

import java.util.regex.Pattern;

import org.apache.commons.lang3.math.NumberUtils;

import android.text.TextUtils;

public class Validator{

    private static final String EMAIL_REGEXP = "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
            + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
            + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
            + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";

    private static Pattern pattern = Pattern.compile(EMAIL_REGEXP, Pattern.CASE_INSENSITIVE);

    public static boolean isValidPassword(String aPassword){
        return (!TextUtils.isEmpty(aPassword) && aPassword.length() >= 6 && aPassword.length() <= 18);
    }

    public static boolean isValidName(String aName){
        return (!TextUtils.isEmpty(aName) && aName.split(" ").length >= 2);
    }

    public static boolean isValidNick(String aName){
        return !TextUtils.isEmpty(aName);
    }

    public static boolean isValidPhone(String aPhone){
        return TextUtils.isEmpty(aPhone) || (NumberUtils.isNumber(aPhone) && aPhone.length() >= 10);
    }

    public static boolean isValidEmail(String aEmail){
        return pattern.matcher(aEmail).matches();
    }
}
