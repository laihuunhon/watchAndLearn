package com.lhn.watchandlearn.api.request.comments;

import org.springframework.http.HttpMethod;

import com.lhn.watchandlearn.api.request.base.PagedApiRequest;
import com.lhn.watchandlearn.api.response.comments.CommentsResponse;

public class GetCommentListRequest extends PagedApiRequest<CommentsResponse> {    
    public GetCommentListRequest(){
        super(CommentsResponse.class);
    }
    
    public void setVideoId(String aVideoId) {
    	setParam("video_id", aVideoId);
    }

    @Override
    protected String getMethodUrl(){
        return "comments";
    }

    @Override
    protected HttpMethod getMethod(){
        return HttpMethod.GET;
    }
}
