package com.lhn.watchandlearn.api.request.movies;

import org.springframework.http.HttpMethod;

import com.lhn.watchandlearn.api.request.base.ApiRequest;
import com.lhn.watchandlearn.api.response.base.Response;

public class AddWatchedRequest extends ApiRequest<Response> {    
    public AddWatchedRequest(){
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
        return "watched";
    }

    @Override
    protected HttpMethod getMethod(){
        return HttpMethod.POST;
    }
}
