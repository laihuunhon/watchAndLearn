package com.lhn.watchandlearn.api.request.base;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpMethod;

import android.net.Uri;
import android.util.Log;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.octo.android.robospice.request.SpiceRequest;

import com.lhn.watchandlearn.WatchAndLearnApp;
import com.lhn.watchandlearn.api.ServerException;
import com.lhn.watchandlearn.api.response.base.Response;
import com.lhn.watchandlearn.api.response.config.ApiConfig;

/**
 * Copyright (c) 2013 , Saritasa LLC (www.saritasa.com) . All rights reserved.
 * 
 * @author ak
 */
public abstract class ApiRequest<RESULT> extends SpiceRequest<RESULT> {

	private static final String LOG_TAG = "wal.api.request";
	//
	protected HashMap<String, Object> mPostParams = new HashMap<String, Object>();
	protected HashMap<String, String> mUrlParams = new HashMap<String, String>();
	protected HashMap<String, String> mHeaders = new HashMap<String, String>();

	private ApiConfig mApiConfig;

	public ApiRequest(final Class<RESULT> clazz) {
		super(clazz);
		//
		setHeader("Connection", "Close");
	}

	public void setAuthorization(final String aToken) {
		setHeader("Authorization", aToken);
	}

	public void setApiConfig(ApiConfig aApiConfig) {
		mApiConfig = aApiConfig;
	}

	protected void setParam(String aName, Object aValue) {
		if (HttpMethod.POST.equals(getMethod()) || HttpMethod.PUT.equals(getMethod())) {
			mPostParams.put(aName, aValue);
		} else {
			mUrlParams.put(aName, String.valueOf(aValue));
		}
	}

	public void setHeader(String aName, String aValue) {
		mHeaders.put(aName, aValue);
	}

	@Override
	public RESULT loadDataFromNetwork() throws Exception {
		setParam("locale", WatchAndLearnApp.getCurrentLocale());
		setParam("device", "A");
		setParam("versionCode", WatchAndLearnApp.getAppVersion());
		
		URL url = new URL(buildUrl());
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoInput(true);
		conn.setRequestMethod(getMethod().name());
		conn.setReadTimeout(20000);
		// setup headers
		for (Map.Entry<String, String> entry : mHeaders.entrySet()) {
			conn.setRequestProperty(entry.getKey(), entry.getValue());
		}
		// send params
		if (!mPostParams.isEmpty()) {
			conn.setDoOutput(true);
			MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));
			buildPostParams(entity);

			conn.setRequestProperty("Accept-Charset", "UTF-8");
			conn.setRequestProperty("Content-Length", String.valueOf(entity.getContentLength()));
			conn.setRequestProperty(entity.getContentType().getName(), entity.getContentType().getValue());
			entity.writeTo(conn.getOutputStream());
		}

		Log.v(LOG_TAG, MessageFormat.format("Sending request: url={0}", url));

		int responseCode = conn.getResponseCode();
		String response;
		boolean isServerError = responseCode / 400 > 0 || responseCode / 500 > 0;
		InputStream inputStream = null;
		BufferedInputStream bufferedInputStream = null;
		try {
			if (isServerError) {
				inputStream = conn.getErrorStream();
				bufferedInputStream = new BufferedInputStream(inputStream);
				response = IOUtils.toString(new BufferedInputStream(inputStream));
				Log.v(LOG_TAG, MessageFormat.format("Request failure: {0} url={1}", response, url));
				// proceedServerError(response);
			} else {
				inputStream = conn.getInputStream();
				bufferedInputStream = new BufferedInputStream(inputStream);
				response = IOUtils.toString(new BufferedInputStream(inputStream));
			}
		} finally {
			IOUtils.closeQuietly(inputStream);
			IOUtils.closeQuietly(bufferedInputStream);
		}

		Log.v(LOG_TAG, MessageFormat.format("Successfully received response: url={0}", url));
		return parse(response);
	}

	protected Object getParam(String aName) {
		if (HttpMethod.POST.equals(getMethod()) || HttpMethod.PUT.equals(getMethod())) {
			return mPostParams.get(aName);
		} else {
			return mUrlParams.get(aName);
		}
	}

	protected abstract String getMethodUrl();

	protected abstract HttpMethod getMethod();

	protected String buildUrl() {
		Uri.Builder builder = Uri.parse(mApiConfig.getApiUrl()).buildUpon();
		builder.appendEncodedPath(getMethodUrl());

		for (Map.Entry<String, String> e : mUrlParams.entrySet()) {
			builder.appendQueryParameter(e.getKey(), e.getValue());
		}
		return builder.toString();
	}

	private void buildPostParams(final MultipartEntity aEntity) throws Exception {
		if (mPostParams != null) {
			for (Map.Entry<String, Object> e : mPostParams.entrySet()) {
				Object value = e.getValue();

				if (value instanceof FileSystemResource) {
					FileSystemResource fileSystemResource = (FileSystemResource) value;
					aEntity.addPart(e.getKey(), new FileBody(fileSystemResource.getFile()));
				} else {
					aEntity.addPart(e.getKey(), new StringBody(String.valueOf(value), Charset.forName("UTF-8")));
				}
			}
		}
	}

	protected void proceedServerError(final String aResponseBody) throws JSONException, ServerException {
		JSONObject json = new JSONObject(aResponseBody);
		String error;
		if (json.has("success")) {
			boolean success = json.getInt("success") != 0;
			if (success) {
				return;
			}
			error = json.optString("message");
		} else {
			error = aResponseBody;
		}
		throw new ServerException(error != null ? error : "");
	}

	@SuppressWarnings("unchecked")
	protected RESULT parse(final String aResponseBody) throws JSONException {
		JsonFactory factory = new JsonFactory();
		ObjectMapper mapper = new ObjectMapper(factory);
		RESULT result = null;
		try {
			result = mapper.readValue(aResponseBody, getResultType());
		} catch (IOException e) {
			throw new JSONException(e.getMessage());
		}

		if (result instanceof Response) {
			try {
				Response response = (Response) result;
				if (response.getSuccess() != 1) {
					JSONObject json = new JSONObject(aResponseBody);

					Map<String, String> errors = new HashMap<String, String>();
					JSONObject data = json.optJSONObject("data");
					if (data != null) {
						Iterator<String> iterator = data.keys();
						while (iterator.hasNext()) {
							String key = (String) iterator.next();
							JSONObject value = data.optJSONObject(key);

							errors.put(key, value.optString("error"));
						}

						response.setErrors(errors);
					}
				}
			} catch (Exception e) {
				Log.e(LOG_TAG, "Failed to parse the response", e);
			}
		}

		return result;
	}

	public String getKey() {
		return buildUrl();
	}

	public boolean isRequireAuth() {
		return true;
	}
}
