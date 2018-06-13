package com.lhn.watchandlearn.api.request.users;

import org.springframework.http.HttpMethod;

import com.lhn.watchandlearn.api.request.base.ApiRequest;
import com.lhn.watchandlearn.api.response.users.AddTransactionResponse;

public class AddTransactionRequest extends ApiRequest<AddTransactionResponse> {    
    public AddTransactionRequest(){
        super(AddTransactionResponse.class);
    }
    
    public void setCardId(String aCardId){
        setParam("card_id", aCardId);
    }

    public void setPinField(String aPinField){
        setParam("pin_field", aPinField);
    }
    
    public void setSeriField(String aSeriField) {
    	setParam("seri_field", aSeriField);
    }
    
    @Override
    protected String getMethodUrl(){
        return "transactions";
    }

    @Override
    protected HttpMethod getMethod(){
        return HttpMethod.POST;
    }
}
