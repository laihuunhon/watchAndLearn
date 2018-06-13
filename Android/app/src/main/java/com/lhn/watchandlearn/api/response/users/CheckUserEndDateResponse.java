package com.lhn.watchandlearn.api.response.users;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lhn.watchandlearn.api.response.base.Response;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CheckUserEndDateResponse extends Response {
	private Boolean isExpired;

	public Boolean getIsExpired() {
		return isExpired;
	}

	@JsonProperty("isExpired")
	public void setIsExpired(Boolean isExpired) {
		this.isExpired = isExpired;
	}
}
