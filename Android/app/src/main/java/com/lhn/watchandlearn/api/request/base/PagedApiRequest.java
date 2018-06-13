package com.lhn.watchandlearn.api.request.base;

public abstract class PagedApiRequest<T> extends ApiRequest<T>{
	
	public static final int DEFAULT_PAGE_SIZE = 20;

    public PagedApiRequest(final Class<T> clazz){
        super(clazz);
    }

    public void setLimit(int aLimit){
        setParam("limit", aLimit);
    }

    public void setOffset(int aOffset){
        setParam("offset", aOffset);
    }
    
    public int getLimit(){
        final Object limit = getParam("limit");
        return limit != null ? Integer.valueOf(String.valueOf(limit)) : DEFAULT_PAGE_SIZE;
    }
    
    public int getOffset(){
        final Object offset = getParam("offset");
        return offset != null ? Integer.valueOf(String.valueOf(offset)) : 0;
    }
}
