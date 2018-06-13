package com.lhn.watchandlearn.ui.common;

import android.app.AlertDialog;

import com.lhn.watchandlearn.R;
import com.lhn.watchandlearn.api.ServerException;
import com.lhn.watchandlearn.api.response.base.Response;

public abstract class SimpleResponseListener<RESULT> extends ResponseListener<RESULT> {

	private BaseActivity mActivity;

	public SimpleResponseListener(BaseActivity aActivity) {
		mActivity = aActivity;
	}

	@Override
	public void onSuccess(final RESULT aResult) {
	}

	protected void processRequestSuccess(final RESULT aResult) {
		onBeforeResult();
		if (aResult instanceof Response) {
			Response response = (Response) aResult;
			if (response.getSuccess() == 1) {
				onSuccess(aResult);
			} else {
				if ("ER00003".equals(response.getCode()) || "ER00009".equals(response.getCode())) {
					mActivity.onInvalidToken();
				} else if ("EU00014".equals(response.getCode())) {
					mActivity.onExpiredDate();
				} else if ("ER00014".equals(response.getCode())) {
					mActivity.onUpdateVersion();
				} else {
					onServerError(new ServerException(response.getCode(), response.getMessage()));
				}				
			}
		} else {
			onSuccess(aResult);
		}
	}

	@Override
	public void onNetworkError(final Throwable e) {
		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		builder.setMessage("Network error!");
		builder.setTitle(R.string.error);
		builder.setPositiveButton(R.string.ok, null);
		builder.create().show();
	}

	@Override
	public void onLicenseError(Throwable e) {
		if (mActivity instanceof BaseActivity) {
			BaseActivity activity = (BaseActivity) mActivity;
			activity.showErrorToast("User is restricted!");
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
			builder.setMessage("User is restricted!");
			builder.setTitle(R.string.error);
			builder.setPositiveButton(R.string.ok, null);
			builder.create().show();
		}
	}

	@Override
	public void onServerError(final ServerException e) {
		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		builder.setMessage(e.toString());
		builder.setTitle(R.string.error);
		builder.setPositiveButton(R.string.ok, null);
		builder.create().show();
	}

	@Override
	public void onBeforeResult() {
		super.onBeforeResult();
	}
}
