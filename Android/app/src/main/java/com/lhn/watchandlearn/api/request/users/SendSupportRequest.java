package com.lhn.watchandlearn.api.request.users;

import org.springframework.http.HttpMethod;

import com.lhn.watchandlearn.api.request.base.ApiRequest;
import com.lhn.watchandlearn.api.response.base.Response;

public class SendSupportRequest extends ApiRequest<Response> {    
    public SendSupportRequest(){
        super(Response.class);
    }
    
    public void setSupportType(String aSupportType){
        setParam("support_type", aSupportType);
    }

    public void setSupportDetail(String aSupportDetail){
        setParam("support_detail", aSupportDetail);
    }
    
    @Override
    protected String getMethodUrl(){
        return "support";
    }

    @Override
    protected HttpMethod getMethod(){
        return HttpMethod.POST;
    }
}
