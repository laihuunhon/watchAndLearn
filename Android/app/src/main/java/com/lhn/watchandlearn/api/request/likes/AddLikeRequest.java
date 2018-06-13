package com.lhn.watchandlearn.api.request.likes;

import org.springframework.http.HttpMethod;

import com.lhn.watchandlearn.api.request.base.ApiRequest;
import com.lhn.watchandlearn.api.response.base.Response;

public class AddLikeRequest extends ApiRequest<Response> {    
    public AddLikeRequest(){
        super(Response.class);
    }

    public void setMovieId(String aMovieId){
        setParam("movie_id", aMovieId);
    }

    public void setVideoId(String aVideoId){
        setParam("video_id", aVideoId);
    }
    
    @Override
    protected String getMethodUrl(){
        return "likes";
    }

    @Override
    protected HttpMethod getMethod(){
        return HttpMethod.POST;
    }
}
