package com.lhn.watchandlearn.api.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Comment {
	private String mId;
	private String mVideoId;
	private String mUserId;
	private String mEmail;
	private String mText;
	private String mCreated;

	public String getId() {
		return mId;
	}

	@JsonProperty("_id")
	public void setId(String aId) {
		this.mId = aId;
	}

	public String getVideoId() {
		return mVideoId;
	}

	public void setVideoId(String aVideoId) {
		mVideoId = aVideoId;
	}

	public String getUserId() {
		return mUserId;
	}

	public void setUserId(String aUserId) {
		mUserId = aUserId;
	}

	public String getText() {
		return mText;
	}

	public void setText(String aText) {
		mText = aText;
	}

	public String getCreated() {
		return mCreated;
	}

	public void setCreated(String aCreated) {
		mCreated = aCreated;
	}

	public String getEmail() {
		return mEmail;
	}

	public void setEmail(String aEmail) {
		mEmail = aEmail;
	}	
}
