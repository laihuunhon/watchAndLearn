package com.lhn.watchandlearn.api.request.users;

import org.springframework.http.HttpMethod;

import com.lhn.watchandlearn.api.request.base.ApiRequest;
import com.lhn.watchandlearn.api.response.base.Response;

public class ChangePasswordRequest extends ApiRequest<Response> {    
    public ChangePasswordRequest(){
        super(Response.class);
    }

    public void setCurrentPassword(String aCurrentPassword){
        setParam("currentPassword", aCurrentPassword);
    }

    public void setNewPassword(String aNewPassword){
        setParam("newPassword", aNewPassword);
    }

    @Override
    protected String getMethodUrl(){
        return "users/changePassword";
    }

    @Override
    protected HttpMethod getMethod(){
        return HttpMethod.PUT;
    }
}
