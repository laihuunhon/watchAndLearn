package com.lhn.watchandlearn.api.pojo;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Movie {
	private String mId;
	private String mThumbnail;
	private String mTitle;
	private String mDescription;
	private	long mWatched;
	private	long mLiked;
	private	long mCommented;
	private ArrayList<Video> mItems; 

	public String getId() {
		return mId;
	}

	@JsonProperty("_id")
	public void setId(String aId) {
		mId = aId;
	}

	public String getThumbnail() {
		return mThumbnail;
	}

	@JsonProperty("thumbnail")
	public void setThumbnail(String aThumbnail) {
		mThumbnail = aThumbnail;
	}

	public String getTitle() {
		return mTitle;
	}

	@JsonProperty("title")
	public void setTitle(String aTitle) {
		mTitle = aTitle;
	}

	public String getDescription() {
		return mDescription;
	}

	@JsonProperty("description")
	public void setDescription(String aDescription) {
		mDescription = aDescription;
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

	public ArrayList<Video> getItems() {
		return mItems;
	}

	@JsonProperty("videos")
	public void setItems(ArrayList<Video> aItems) {
		mItems = aItems;
	}
}
