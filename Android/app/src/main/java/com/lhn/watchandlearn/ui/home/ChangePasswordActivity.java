package com.lhn.watchandlearn.ui.home;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.lhn.watchandlearn.R;
import com.lhn.watchandlearn.api.ServerException;
import com.lhn.watchandlearn.api.request.users.ChangePasswordRequest;
import com.lhn.watchandlearn.api.response.base.Response;
import com.lhn.watchandlearn.ui.common.BaseActivity;
import com.lhn.watchandlearn.ui.common.SimpleResponseListener;
import com.lhn.watchandlearn.ui.utils.EditTextUtils;
import com.lhn.watchandlearn.ui.widget.FormEditText;

public class ChangePasswordActivity extends BaseActivity implements View.OnClickListener {
	private FormEditText mTvCurrentPassword, mTvNewPassword, mTvRetypePassword;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_changepassword);

		bindViews();
	}

	private void bindViews() {
		TextView title = (TextView) findViewById(R.id.viewTitle);
		title.setText(getString(R.string.changepassword));

		mTvCurrentPassword = (FormEditText) findViewById(R.id.tvCurrentPassword);
		mTvCurrentPassword.setValidator(FormEditText.NotEmptyValidator.getInstance());
		mTvCurrentPassword.getEditText().setOnFocusChangeListener(
				new View.OnFocusChangeListener() {
					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						if (hasFocus) {
							mTvCurrentPassword.checkmarkOff();
							mTvCurrentPassword.errorOff();
						} else {
							if (!mTvCurrentPassword.validate()) {
								showErrorToast(getString(R.string.invalid_field));
							}
						}
					}
				});
		
		mTvNewPassword = (FormEditText) findViewById(R.id.tvNewPassword);
		mTvNewPassword.setValidator(FormEditText.NotEmptyValidator.getInstance());
		mTvNewPassword.getEditText().setOnFocusChangeListener(
				new View.OnFocusChangeListener() {
					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						if (hasFocus) {
							mTvNewPassword.checkmarkOff();
							mTvNewPassword.errorOff();
						} else {
							if (!mTvNewPassword.validate()) {
								showErrorToast(getString(R.string.invalid_field));
							}
						}
					}
				});

		mTvRetypePassword = (FormEditText) findViewById(R.id.tvRetypePassword);
		mTvRetypePassword.getEditText().setOnFocusChangeListener(
				new View.OnFocusChangeListener() {
					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						if (hasFocus) {
							mTvRetypePassword.checkmarkOff();
							mTvRetypePassword.errorOff();
						} else {
							if (TextUtils.isEmpty(mTvRetypePassword.getValue())
									|| !mTvRetypePassword.getValue().equals(
											mTvNewPassword.getValue())) {
								mTvRetypePassword.checkmarkOff();
								mTvRetypePassword.errorOn();

								showErrorToast(getString(R.string.invalid_retype_password));
							} else {
								mTvRetypePassword.checkmarkOn();
								mTvRetypePassword.errorOff();
							}
						}
					}
				});

		findViewById(R.id.btnCancel).setOnClickListener(this);
		findViewById(R.id.btnSubmit).setOnClickListener(this);
	}

	private boolean validate() {
		EditTextUtils.hideKeyboard(this, mTvCurrentPassword.getEditText());
		EditTextUtils.hideKeyboard(this, mTvNewPassword.getEditText());
		EditTextUtils.hideKeyboard(this, mTvRetypePassword.getEditText());

		if (TextUtils.isEmpty(mTvCurrentPassword.getValue())) {
			showErrorToast(getString(R.string.invalid_field));
			mTvCurrentPassword.errorOn();
			return false;
		}
		if (TextUtils.isEmpty(mTvNewPassword.getValue())) {
			showErrorToast(getString(R.string.invalid_field));
			mTvNewPassword.errorOn();
			return false;
		}
		if (!mTvNewPassword.getValue().equals(mTvRetypePassword.getValue())) {
			showErrorToast(getString(R.string.invalid_retype_password));
			mTvRetypePassword.errorOn();
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
				doChangePassword();
			}
			break;
		}
	}

	private void doChangePassword() {
		showLoadingDialog(getString(R.string.loading_waiting));
		ChangePasswordRequest request = new ChangePasswordRequest();
		request.setCurrentPassword(mTvCurrentPassword.getValue());
		request.setNewPassword(mTvNewPassword.getValue());
		
		send(request, new SimpleResponseListener<Response>(this) {
			@Override
			public void onBeforeResult() {
				super.onBeforeResult();
				dismissLoadingDialog();
			}
			
			@Override
			public void onSuccess(Response aResult) {				
				showInfoToast(getString(R.string.changepassword_success));
				mTvCurrentPassword.setValue("");
				mTvNewPassword.setValue("");
				mTvRetypePassword.setValue("");
			}

			@Override
			public void onServerError(ServerException aException) {
				showErrorToast(aException.getMessage());
			}
		});
	}
}
