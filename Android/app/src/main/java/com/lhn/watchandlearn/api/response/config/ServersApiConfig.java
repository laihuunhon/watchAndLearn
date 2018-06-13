package com.lhn.watchandlearn.api.response.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Copyright (c) 2013 , Saritasa LLC (www.saritasa.com) . All rights reserved.
 * 
 * @author ak
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServersApiConfig{

    private ApiConfig mProd;
    private ApiConfig mStaging;
    private ApiConfig mDev;

    public ApiConfig getProd(){
        return mProd;
    }

    @JsonProperty("production")
    public void setProd(final ApiConfig aProd){
        mProd = aProd;
    }

    public ApiConfig getStaging(){
        return mStaging;
    }

    @JsonProperty("staging")
    public void setStaging(final ApiConfig aStaging){
        mStaging = aStaging;
    }

    public ApiConfig getDev(){
        return mDev;
    }

    @JsonProperty("development")
    public void setDev(final ApiConfig aDev){
        mDev = aDev;
    }

    @Override
    public String toString(){
        return "ServersApiConfig{" +
                "mProd=" + mProd +
                ", mStaging=" + mStaging +
                ", mDev=" + mDev +
                '}';
    }
}
