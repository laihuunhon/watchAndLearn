package com.lhn.watchandlearn.api.request.users;

import org.springframework.http.HttpMethod;

import com.lhn.watchandlearn.api.request.base.ApiRequest;
import com.lhn.watchandlearn.api.response.auth.LoginResponse;

public class GetUserRequest extends ApiRequest<LoginResponse> {    
    public GetUserRequest(){
        super(LoginResponse.class);
    }

    @Override
    protected String getMethodUrl(){
        return "users/me";
    }

    @Override
    protected HttpMethod getMethod(){
        return HttpMethod.GET;
    }
}
