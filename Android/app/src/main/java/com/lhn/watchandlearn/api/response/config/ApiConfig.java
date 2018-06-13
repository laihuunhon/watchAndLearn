package com.lhn.watchandlearn.api.response.config;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Copyright (c) 2013 , Saritasa LLC (www.saritasa.com) . All rights reserved.
 * 
 * @author An
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiConfig implements Serializable{

    private static final long serialVersionUID = -1709862898630295047L;

    private String mApiUrl;
    private String mVersion;

    public String getApiUrl() {
		return mApiUrl;
	}

	@JsonProperty("apiUrl")
	public void setApiUrl(String mApiUrl) {
		this.mApiUrl = mApiUrl;
	}

    public String getVersion(){
        return mVersion;
    }

    @JsonProperty("androidVersion")
    public void setVersion(final String aVersion){
        mVersion = aVersion;
    }

    @Override
    public String toString(){
        return "ApiConfig{" +
                "mApiUrl='" + mApiUrl + '\'' +
                ", mVersion=" + mVersion +
                '}';
    }

	
}
