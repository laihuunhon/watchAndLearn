package com.lhn.watchandlearn.api.response.base;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Copyright (c) 2013 , Saritasa LLC (www.saritasa.com) . All rights reserved.
 * 
 * @author ak
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Response{

    private int mSuccess;
    private String mCode;
    private String mMessage;
    private Map<String, String> mErrors;

    public int getSuccess(){
        return mSuccess;
    }

    @JsonProperty("success")
    public void setSuccess(final int aSuccess){
        mSuccess = aSuccess;
    }

    public String getCode(){
        return mCode;
    }

    @JsonProperty("code")
    public void setCode(final String aCode){
        mCode = aCode;
    }

    public String getMessage(){
        return mMessage;
    }

    @JsonProperty("message")
    public void setMessage(final String aMessage){
        mMessage = aMessage;
    }

    public Map<String, String> getErrors(){
        return mErrors;
    }
    
    public void setErrors(Map<String, String> aErrors){
        mErrors = aErrors;
    }
}
