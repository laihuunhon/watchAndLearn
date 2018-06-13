package com.lhn.watchandlearn.api.request.users;

import org.springframework.http.HttpMethod;

import com.lhn.watchandlearn.api.request.base.ApiRequest;
import com.lhn.watchandlearn.api.response.auth.LoginResponse;

public class AddUserRequest extends ApiRequest<LoginResponse> {    
    public AddUserRequest(){
        super(LoginResponse.class);
    }

    public void setEmail(String aEmail){
        setParam("email", aEmail);
    }

    public void setPassword(String aPassword){
        setParam("password", aPassword);
    }
    
    public void setPhone(String aPhone) {
    	setParam("phone", aPhone);
    }
    
    public void setDeviceUUID(String deviceUUID) {
    	setParam("device_uuid", deviceUUID);
    }

    @Override
    protected String getMethodUrl(){
        return "users";
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
