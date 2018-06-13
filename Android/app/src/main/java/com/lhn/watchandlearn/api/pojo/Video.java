package com.lhn.watchandlearn.api.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Video {
	private String mId;
	private String mUrl;
	private String mIndex;
	private	long mWatched;
	private	long mLiked;
	private	long mCommented;
	private Boolean mIsLiked;

	public String getId() {
		return mId;
	}

	@JsonProperty("_id")
	public void setId(String aId) {
		this.mId = aId;
	}
	
	public String getUrl() {
		return mUrl;
	}

	@JsonProperty("url")
	public void setUrl(String aUrl) {
		mUrl = aUrl;
	}

	public String getIndex() {
		return mIndex;
	}

	@JsonProperty("index")
	public void setIndex(String aIndex) {
		mIndex = aIndex;
	}	
	
	public long getWatched() {
		return mWatched;
	}

	@JsonProperty("total_watched")
	public void setWatched(long aWatched) {
		mWatched = aWatched;
	}

	public long getLiked() {
		return mLiked;
	}

	@JsonProperty("total_liked")
	public void setLiked(long aLiked) {
		mLiked = aLiked;
	}

	public long getCommented() {
		return mCommented;
	}

	@JsonProperty("total_comments")
	public void setCommented(long aCommented) {
		mCommented = aCommented;
	}
	
	public Boolean getIsLiked() {
		return mIsLiked;
	}

	@JsonProperty("isLiked")
	public void setIsLiked(Boolean aIsLiked) {
		mIsLiked = aIsLiked;
	}
}
