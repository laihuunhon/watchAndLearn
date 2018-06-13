package com.lhn.watchandlearn.api.response.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import android.text.TextUtils;

/**
 * Copyright (c) 2013 , Saritasa LLC (www.saritasa.com) . All rights reserved.
 *
 * @author ak
 */
@SuppressWarnings("unchecked")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorCodeMessages implements Serializable{

    private static final long serialVersionUID = -8206018541689861335L;
    
    private HashMap<String, String> codes = new HashMap<String, String>();

    public String asString(){
        JSONObject json = new JSONObject(codes);
        return json.toString();
    }

    public static ErrorCodeMessages fromString(String aString){
        ErrorCodeMessages messages = new ErrorCodeMessages();
        try{
            JSONObject json = new JSONObject(aString);
            Iterator<String> iterator = json.keys();
            while(iterator.hasNext()){
                String code = iterator.next();
                messages.add(code, json.getString(code));
            }
        } catch(JSONException e){
            e.printStackTrace();
        }
        return messages;
    }

    public void add(String aCode, String aErrorMessage){
        codes.put(aCode, aErrorMessage);
    }

    public String getError(String aCode){
        String msg = codes.get(aCode);
        return msg != null ? msg : "";
    }

    public List<String> getErrors(Map<String, String> aCodes){
        List<String> result = new ArrayList<String>();
        if(aCodes != null){
            for(Map.Entry<String, String> entry : aCodes.entrySet()){
                String errorCode = entry.getKey();
                String message = getError(errorCode);
                if(TextUtils.isEmpty(message)){ // no message found in json -> use server error message
                    message = entry.getValue();
                }
                result.add(message);

            }
        }
        return result;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("ErrorCodeMessages{").append("codes=");
        Iterator<Map.Entry<String,String>> iterator = codes.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<String, String> entry = iterator.next();
            sb.append(entry.getKey()).append(":").append(entry.getValue());
            if(iterator.hasNext()){
                sb.append(",");
            }
        }
        sb.append('}');
        return sb.toString();
    }
}
