package com.lhn.watchandlearn.api.request.users;

import org.springframework.http.HttpMethod;

import com.lhn.watchandlearn.api.request.base.ApiRequest;
import com.lhn.watchandlearn.api.response.users.CheckUserEndDateResponse;

public class CheckUserEndDateRequest extends ApiRequest<CheckUserEndDateResponse> {    
    public CheckUserEndDateRequest(){
        super(CheckUserEndDateResponse.class);
    }

    @Override
    protected String getMethodUrl(){
        return "checkEndDate";
    }

    @Override
    protected HttpMethod getMethod(){
        return HttpMethod.GET;
    }
}
