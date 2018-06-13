package com.lhn.watchandlearn.api.request.auth;

import org.springframework.http.HttpMethod;

import com.lhn.watchandlearn.api.request.base.ApiRequest;
import com.lhn.watchandlearn.api.response.auth.LoginResponse;

public class LoginRequest extends ApiRequest<LoginResponse> {    
    public LoginRequest(){
        super(LoginResponse.class);
    }

    public void setEmail(String aEmail){
        setParam("email", aEmail);
    }

    public void setPassword(String aPassword){
        setParam("password", aPassword);
    }

    @Override
    protected String getMethodUrl(){
        return "users/login";
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
