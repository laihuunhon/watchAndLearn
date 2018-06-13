package com.lhn.watchandlearn.api.response.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lhn.watchandlearn.api.pojo.User;
import com.lhn.watchandlearn.api.response.base.Response;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginResponse extends Response {
	private String token;
	private User user;

	public User getUser() {
		return user;
	}

	@JsonProperty("user")
	public void setUser(User user) {
		this.user = user;
	}

	public String getToken() {
		return token;
	}

	@JsonProperty("token")
	public void setToken(final String token) {
		this.token = token;
	}

	@Override
	public String toString() {
		return "LoginResponse [token=" + token + ", user=" + user + "]";
	}
}
