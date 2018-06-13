package com.lhn.watchandlearn.api.response.comments;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lhn.watchandlearn.api.pojo.Comment;
import com.lhn.watchandlearn.api.response.base.Response;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CommentsResponse extends Response {
	private ArrayList<Comment> mItems;
	private int mTotal;

	public ArrayList<Comment> getItems(){
        return mItems;
    }

    @JsonProperty("commentList")
    public void setItems(final ArrayList<Comment> aItems){
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
		return "CommentsResponse [mItems=" + mItems + ", mTotal=" + mTotal + "]";
	}
}
