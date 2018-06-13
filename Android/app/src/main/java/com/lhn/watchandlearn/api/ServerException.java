package com.lhn.watchandlearn.api;

public class ServerException extends Exception {
	private String errorCode;
	
	public ServerException(String detailMessage) {
		super(detailMessage);
	}

	public ServerException(String errorCode, String detailMessage) {
		super(detailMessage);
		setErrorCode(errorCode);
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
}
