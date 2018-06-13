package com.lhn.watchandlearn.ui.home;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import com.lhn.watchandlearn.R;
import com.lhn.watchandlearn.api.ServerException;
import com.lhn.watchandlearn.api.request.users.SendSupportRequest;
import com.lhn.watchandlearn.api.response.base.Response;
import com.lhn.watchandlearn.ui.common.BaseActivity;
import com.lhn.watchandlearn.ui.common.SimpleResponseListener;
import com.lhn.watchandlearn.ui.utils.EditTextUtils;
import com.lhn.watchandlearn.ui.widget.FormEditText;

public class SupportActivity extends BaseActivity implements View.OnClickListener {
	private FormEditText mTvSupportDetail;
	private Spinner spSupportType;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_support);

		bindViews();
	}

	private void bindViews() {
		TextView title = (TextView) findViewById(R.id.viewTitle);
		title.setText(getString(R.string.support));

		mTvSupportDetail = (FormEditText) findViewById(R.id.tvSupportDetail);
		mTvSupportDetail.setValidator(FormEditText.NotEmptyValidator.getInstance());
		mTvSupportDetail.getEditText().setOnFocusChangeListener(
				new View.OnFocusChangeListener() {
					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						if (hasFocus) {
							mTvSupportDetail.checkmarkOff();
							mTvSupportDetail.errorOff();
						} else {
							if (!mTvSupportDetail.validate()) {
								showErrorToast(getString(R.string.invalid_field));
							}
						}
					}
				});

		spSupportType = (Spinner) findViewById(R.id.spSupportType);
		findViewById(R.id.btnCancel).setOnClickListener(this);
		findViewById(R.id.btnSubmit).setOnClickListener(this);
	}

	private boolean validate() {
		EditTextUtils.hideKeyboard(this, mTvSupportDetail.getEditText());

		if (TextUtils.isEmpty(mTvSupportDetail.getValue())) {
			showErrorToast(getString(R.string.invalid_field));
			mTvSupportDetail.errorOn();
			return false;
		}
		
		return true;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnCancel:
			finish();
			break;

		case R.id.btnSubmit:
			if (validate()) {
				doSendSupport();
			}
			break;
		}
	}
	
	private void doSendSupport() {
		showLoadingDialog(getString(R.string.loading_waiting));
		
		SendSupportRequest request = new SendSupportRequest();
		
		request.setSupportType(spSupportType.getSelectedItem().toString());
		request.setSupportDetail(mTvSupportDetail.getValue());
		
		send(request, new SimpleResponseListener<Response>(this) {
			@Override
			public void onBeforeResult() {
				super.onBeforeResult();
				dismissLoadingDialog();
			}
			
			@Override
			public void onSuccess(Response aResult) {		
				showInfoToast(getString(R.string.support_success));
				mTvSupportDetail.setValue("");
			}

			@Override
			public void onServerError(ServerException aException) {
				showErrorToast(aException.getMessage());
			}
		});
	}
}
