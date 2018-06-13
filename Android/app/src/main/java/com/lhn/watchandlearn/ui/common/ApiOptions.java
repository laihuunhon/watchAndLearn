package com.lhn.watchandlearn.ui.common;

import java.io.IOException;
import java.io.StringWriter;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lhn.watchandlearn.WatchAndLearnConfig;
import com.lhn.watchandlearn.api.response.config.ApiConfig;
import com.lhn.watchandlearn.api.response.config.ErrorCodeMessages;
import com.lhn.watchandlearn.api.response.config.ServersApiConfig;

/**
 * Copyright (c) 2013 , Saritasa LLC (www.saritasa.com) . All rights reserved.
 * 
 * @author An
 */
public class ApiOptions{
    
    public static final String BASE64_ECONDED_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAix+vmxLQaoqddrS27Z2ocupnDjdzQ3/tx2aWSafYXx5dXwEh9fOO77gNIB4MMOBBw96i7iKFdzlum/D3ozLJ30vipWvdw5CfpU9O3ZjZCH+QOFcfTYYHkZEJZYbJXwqeypSClU9ZGU6SIM+23Io/wVOjSBSM8qmET40swnfX9SuwBxKL+wp49B1gc2MrhyMSIDJGpvViV4TMrix8IMG3ha0vaScXfxEaqwwsHkCph89lg6AF/sR5el8a4gtWtZ8VM++sgd7J8FNcc2n9E59YMhtkHmTdJyeD51rqiF//AiQYMQhS4LK25X71CMx1AVpRqXuimr2R1PbqKmGpjC1LPQIDAQAB";
    
    private final static ObjectMapper mapper = new ObjectMapper();
    private static final String API_PREFERENCES = "fantasysuite.api.preferences";
    private static final String PREF_SERVERS = "api.servers";
    private static final String PREF_ERRORS = "api.errors";
    
    private static final String TAG = ApiOptions.class.getName();

    public static void saveServers(final ServersApiConfig aServers, Context aContext) throws IOException{
        StringWriter serversAsString = new StringWriter();
        mapper.writeValue(serversAsString, aServers);
        prefs(aContext).edit().putString(PREF_SERVERS, serversAsString.toString()).apply();
    }

    public static void saveMessages(final ErrorCodeMessages aMessages, Context aContext) throws IOException{
        prefs(aContext).edit().putString(PREF_ERRORS, aMessages.asString()).apply();
    }

    public static ApiConfig loadApi(WatchAndLearnConfig.State aState, Context aContext){
        try{
            String servers = prefs(aContext).getString(PREF_SERVERS, "");
            ServersApiConfig serversApiConfig = mapper.readValue(servers, ServersApiConfig.class);
            switch(aState){
                case Dev:
                    return serversApiConfig.getDev();
                case Prod:
                    return serversApiConfig.getProd();
                default:
                    return serversApiConfig.getStaging();
            }
        } catch(Exception e){
            Log.e(TAG, "Failed to load api config", e);
            
        }
        
        return null;
    }
    
    public static ErrorCodeMessages loadMessages(Context aContext){
        String errorsString = prefs(aContext).getString(PREF_ERRORS, "");
        return ErrorCodeMessages.fromString(errorsString);
    }

    private static SharedPreferences prefs(final Context aContext){
        return aContext.getSharedPreferences(API_PREFERENCES, Context.MODE_PRIVATE);
    }

}
