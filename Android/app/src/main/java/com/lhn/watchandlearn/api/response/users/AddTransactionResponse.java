package com.lhn.watchandlearn.api.response.users;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lhn.watchandlearn.api.response.base.Response;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AddTransactionResponse extends Response {
	private String endDate;

	public String getEndDate() {
		return endDate;
	}

	@JsonProperty("end_date")
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
}
