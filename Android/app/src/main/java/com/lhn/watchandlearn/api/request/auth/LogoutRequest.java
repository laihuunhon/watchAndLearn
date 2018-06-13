package com.lhn.watchandlearn.api.request.auth;

import org.springframework.http.HttpMethod;

import com.lhn.watchandlearn.api.request.base.ApiRequest;
import com.lhn.watchandlearn.api.response.base.Response;

public class LogoutRequest extends ApiRequest<Response>{

    public LogoutRequest(){
        super(Response.class);
    }

    @Override
    protected String getMethodUrl(){
        return "users/logout";
    }

    @Override
    protected HttpMethod getMethod(){
        return HttpMethod.POST;
    }
}