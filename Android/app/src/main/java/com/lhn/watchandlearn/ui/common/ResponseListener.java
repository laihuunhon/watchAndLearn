package com.lhn.watchandlearn.ui.common;

import java.io.IOException;

import org.json.JSONException;

import com.lhn.watchandlearn.api.ServerException;
import com.lhn.watchandlearn.api.request.base.ApiRequest;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

/**
 * Copyright (c) 2013 , Blurr, LLC  . All rights reserved.
 *
 * @author ak
 */
@SuppressWarnings("rawtypes")
public abstract class ResponseListener<RESULT>{
    public static final String INVALID_TOKEN_ERROR_CODE = "E00600";
    public static final String RESTRICT_ERROR_CODE = "E000603";

    protected ApiRequest mApiRequest;

    public void setApiRequest(ApiRequest aApiRequest){
        mApiRequest = aApiRequest;
    }

    private RequestListener<RESULT> mRequestListener = new RequestListener<RESULT>(){

        @Override
        public void onRequestFailure(final SpiceException e){
            onBeforeResult();
            Throwable throwable = e.getCause();
            if(throwable instanceof ServerException){
                ServerException ex = (ServerException) throwable;
                onServerError(ex);
            } else if(throwable instanceof JSONException){
                JSONException ex = (JSONException) throwable;
                onServerError(new ServerException(ex.getMessage()));
            } else {
                onNetworkError(throwable);
            }
        }

        @Override
        public void onRequestSuccess(final RESULT aResult){
            processRequestSuccess(aResult);
        }
    };

    /**
     * Server operation completed successfully.
     *
     * @param aResult operation result returned by server.
     */
    public abstract void onSuccess(final RESULT aResult);

    protected void processUnauthorizedException(IOException ex){
        onNetworkError(ex);
    }

    /**
     * Network error happened while sending/receiving data.
     *
     * @param e error.
     */
    public abstract void onNetworkError(final Throwable e);

    /**
     * Server error occurred while processing request.
     *
     * @param e error message from server.
     */
    public abstract void onServerError(final ServerException e);

    public abstract void onLicenseError(final Throwable e);
    
    /**
     * Called before the onSuccess or onNetworkError or onServerError
     */
    public void onBeforeResult(){
    }

    public RequestListener<RESULT> getRequestListener(){
        return mRequestListener;
    }

    protected void processRequestSuccess(final RESULT aResult){
        onBeforeResult();
        onSuccess(aResult);
    }
}
