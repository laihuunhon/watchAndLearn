package com.lhn.watchandlearn.api.response.movies;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lhn.watchandlearn.api.pojo.Movie;
import com.lhn.watchandlearn.api.response.base.Response;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MoviesResponse extends Response {
	private ArrayList<Movie> mItems;
	private int mTotal;

	public ArrayList<Movie> getItems(){
        return mItems;
    }

    @JsonProperty("movieList")
    public void setItems(final ArrayList<Movie> aItems){
        mItems = aItems;
    }

    public int getTotal(){
        return mTotal;
    }

    @JsonProperty("total")
    public void setTotal(final int aTotal){
        mTotal = aTotal;
    }

	@Override
	public String toString() {
		return "MoviesResponse [mItems=" + mItems + ", mTotal=" + mTotal + "]";
	}
}
