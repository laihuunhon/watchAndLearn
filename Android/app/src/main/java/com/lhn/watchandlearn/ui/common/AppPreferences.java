package com.lhn.watchandlearn.ui.common;

import android.content.Context;
import android.content.SharedPreferences;

public class AppPreferences {

    private static final String TAG = AppPreferences.class.getName();

    private static final String PREF_USERID = "userId";
    private static final String PREF_TOKEN = "token";
//    private static final String PREF_PURCHASE_RESPONSE = "purchase.response";
    private static final String APP_PREFERENCES = "app.preferences";

    public static void clear(Context aContext){
        prefs(aContext).edit().clear().commit();
    }
    
    public static String getToken(Context aContext){
        return prefs(aContext).getString(PREF_TOKEN, null);
    }

    public static void setToken(Context aContext, String aToken){
        prefs(aContext).edit().putString(PREF_TOKEN, aToken).apply();
    }

    public static void setUserId(Context aContext, String aEmail){
        prefs(aContext).edit().putString(PREF_USERID, aEmail).apply();
    }    

    public static String getUserId(Context aContext){
        return prefs(aContext).getString(PREF_USERID, null);
    }    

//    public static void setPurchaseResponse(Context aContext, PurchaseResponse aPurchaseResponse){
//        ObjectMapper mapper = new ObjectMapper();
//        try{
//            String json = mapper.writeValueAsString(aPurchaseResponse);
//
//            SharedPreferences preferences = aContext.getSharedPreferences(LOGIN_PREFERENCES, Context.MODE_PRIVATE);
//            preferences.edit().putString(PREF_PURCHASE_RESPONSE, json).commit();
//        } catch(JsonProcessingException e){
//            Log.e(TAG, "Failed to serialize purchase", e);
//        }
//    }
    
//    public static void removePurchaseResponse(Context aContext){
//        SharedPreferences preferences = aContext.getSharedPreferences(LOGIN_PREFERENCES, Context.MODE_PRIVATE);
//        preferences.edit().remove(PREF_PURCHASE_RESPONSE);
//    }
//
//    public static PurchaseResponse getPurchaseInfo(Context aContext){
//        try{
//            ObjectMapper mapper = new ObjectMapper();
//            SharedPreferences preferences = aContext.getSharedPreferences(LOGIN_PREFERENCES, Context.MODE_PRIVATE);
//
//            String json = preferences.getString(PREF_PURCHASE_RESPONSE, "");
//            return mapper.readValue(json, PurchaseResponse.class);
//        } catch(IOException e){
//            Log.e(TAG, "Failed to serialize purchase", e);
//        }
//
//        return null;
//    }

    private static SharedPreferences prefs(final Context aContext){
        return aContext.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
    }
}
