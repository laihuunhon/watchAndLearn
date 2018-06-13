package com.lhn.watchandlearn.api.request.movies;

import org.springframework.http.HttpMethod;

import com.lhn.watchandlearn.api.request.base.PagedApiRequest;
import com.lhn.watchandlearn.api.response.movies.MoviesResponse;

public class SearchMovieListRequest extends PagedApiRequest<MoviesResponse> {    
    public SearchMovieListRequest(){
        super(MoviesResponse.class);
    }
    
    public void setSearchText(String aSearchText) {
    	setParam("search_text", aSearchText);
    }

    @Override
    protected String getMethodUrl(){
        return "movies/search";
    }

    @Override
    protected HttpMethod getMethod(){
        return HttpMethod.GET;
    }
}
