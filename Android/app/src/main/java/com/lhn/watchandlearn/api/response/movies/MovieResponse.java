package com.lhn.watchandlearn.api.response.movies;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lhn.watchandlearn.api.pojo.Movie;
import com.lhn.watchandlearn.api.response.base.Response;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MovieResponse extends Response {
	private Movie movie;

	public Movie getMovie() {
		return movie;
	}

	@JsonProperty("movie")
	public void setMovie(Movie movie) {
		this.movie = movie;
	}	
}
