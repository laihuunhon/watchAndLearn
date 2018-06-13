package com.lhn.watchandlearn.api.request.movies;

import org.springframework.http.HttpMethod;

import com.lhn.watchandlearn.api.request.base.PagedApiRequest;
import com.lhn.watchandlearn.api.response.auth.LoginResponse;
import com.lhn.watchandlearn.api.response.movies.MoviesResponse;

public class GetMovieListRequest extends PagedApiRequest<MoviesResponse> {    
    public GetMovieListRequest(){
        super(MoviesResponse.class);
    }
    
    public void setMovieType(String aMovieType) {
    	setParam("movie_type", aMovieType);
    }

    @Override
    protected String getMethodUrl(){
        return "movies";
    }

    @Override
    protected HttpMethod getMethod(){
        return HttpMethod.GET;
    }
}
