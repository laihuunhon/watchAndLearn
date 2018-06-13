package com.lhn.watchandlearn.api.request.auth;

import org.springframework.http.HttpMethod;

import com.lhn.watchandlearn.api.request.base.ApiRequest;
import com.lhn.watchandlearn.api.response.auth.LoginResponse;

public class LoginFBRequest extends ApiRequest<LoginResponse> {    
    public LoginFBRequest(){
        super(LoginResponse.class);
    }

    public void setAccessToken(String aAccessToken){
        setParam("access_token", aAccessToken);
    }
    
    public void setDeviceUUID(String deviceUUID) {
    	setParam("device_uuid", deviceUUID);
    }

    @Override
    protected String getMethodUrl(){
        return "users/loginWithFB";
    }

    @Override
    protected HttpMethod getMethod(){
        return HttpMethod.POST;
    }
    
    @Override
    public boolean isRequireAuth() {
    	return false;
    }
}
