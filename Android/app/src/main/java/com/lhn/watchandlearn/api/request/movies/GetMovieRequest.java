package com.lhn.watchandlearn.api.request.movies;

import org.springframework.http.HttpMethod;

import com.lhn.watchandlearn.api.request.base.ApiRequest;
import com.lhn.watchandlearn.api.response.movies.MovieResponse;

public class GetMovieRequest extends ApiRequest<MovieResponse> {
	private String methodUrl;
	
    public GetMovieRequest(){
        super(MovieResponse.class);
    }
    
    public void setId(String aId) {
    	methodUrl = "/movies/" + aId;
    }

    @Override
    protected String getMethodUrl(){
        return methodUrl;
    }

    @Override
    protected HttpMethod getMethod(){
        return HttpMethod.GET;
    }
}
