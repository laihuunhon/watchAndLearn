package com.lhn.watchandlearn.api.request.comments;

import org.springframework.http.HttpMethod;

import com.lhn.watchandlearn.api.request.base.ApiRequest;
import com.lhn.watchandlearn.api.response.base.Response;

public class AddCommentRequest extends ApiRequest<Response> {    
    public AddCommentRequest(){
        super(Response.class);
    }

    public void setMovieId(String aMovieId){
        setParam("movie_id", aMovieId);
    }

    public void setVideoId(String aVideoId){
        setParam("video_id", aVideoId);
    }
    
    public void setText(String aText) {
    	setParam("text", aText);
    }

    @Override
    protected String getMethodUrl(){
        return "comments";
    }

    @Override
    protected HttpMethod getMethod(){
        return HttpMethod.POST;
    }
}
