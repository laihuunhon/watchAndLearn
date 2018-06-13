package com.lhn.watchandlearn.api;

import java.lang.reflect.Constructor;

import android.content.Context;
import android.util.Log;

import com.lhn.watchandlearn.WatchAndLearnConfig;
import com.lhn.watchandlearn.api.request.base.ApiRequest;
import com.lhn.watchandlearn.api.response.config.ApiConfig;
import com.lhn.watchandlearn.ui.common.ApiOptions;
import com.lhn.watchandlearn.ui.common.AppPreferences;

public class Requests{

    public static final String LOG_TAG = "fantasy.suite.request.factory";

    public static <T extends ApiRequest> T create(Context aContext, Class<T> aClass){
        try{
            Constructor<T> ctor = aClass.getConstructor();
            T request = ctor.newInstance();
            request.setAuthorization(AppPreferences.getToken(aContext));
            ApiConfig apiConfig = ApiOptions.loadApi(WatchAndLearnConfig.state, aContext);
            request.setApiConfig(apiConfig);
            return request;
        } catch(Exception e){
            Log.e(LOG_TAG, "Request creation failed: class=" + String.valueOf(aClass), e);
            throw new UnsupportedOperationException("Request creation failed", e);
        }
    }
}
