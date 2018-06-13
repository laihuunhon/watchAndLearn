package com.lhn.watchandlearn.api.request.auth;

import org.springframework.http.HttpMethod;

import com.lhn.watchandlearn.api.request.base.ApiRequest;
import com.lhn.watchandlearn.api.response.base.Response;

public class ResetPasswordRequest extends ApiRequest<Response> {
	public ResetPasswordRequest() {
		super(Response.class);
	}
	
	public void setEmail(String aEmail){
        setParam("email", aEmail);
    }
	
	public void setPhone(String aPhone){
        setParam("phone", aPhone);
    }

	@Override
	protected String getMethodUrl() {
		return "users/resetpassword";
	}

	@Override
	protected HttpMethod getMethod() {
		return HttpMethod.POST;
	}
	
	@Override
    public boolean isRequireAuth() {
    	return false;
    }
}