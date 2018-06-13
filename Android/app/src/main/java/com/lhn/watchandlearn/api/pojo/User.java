package com.lhn.watchandlearn.api.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
	private String userId;
	private String email;
	private String role;
	private String endDate;
	private String phone;
	private String deviceUUID;
	private String facebookId;
	private Boolean hasAds;

	public String getUserId() {
		return userId;
	}

	@JsonProperty("_id")
	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getEmail() {
		return email;
	}

	@JsonProperty("email")
	public void setEmail(String email) {
		this.email = email;
	}

	public String getRole() {
		return role;
	}

	@JsonProperty("role")
	public void setRole(String role) {
		this.role = role;
	}
	
	public String getPhone() {
		return phone;
	}

	@JsonProperty("phone")
	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEndDate() {
		return endDate;
	}

	@JsonProperty("end_date")
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getDeviceUUID() {
		return deviceUUID;
	}

	@JsonProperty("device_uuid")
	public void setDeviceUUID(String deviceUUID) {
		this.deviceUUID = deviceUUID;
	}

	public String getFacebookId() {
		return facebookId;
	}

	@JsonProperty("facebook_id")
	public void setFacebookId(String facebookId) {
		this.facebookId = facebookId;
	}

	public Boolean getHasAds() {
		return hasAds;
	}

	@JsonProperty("has_ads")
	public void setHasAds(Boolean hasAds) {
		this.hasAds = hasAds;
	}
}
