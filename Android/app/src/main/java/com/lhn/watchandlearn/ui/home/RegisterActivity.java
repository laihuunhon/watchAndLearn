package com.lhn.watchandlearn.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.lhn.watchandlearn.R;
import com.lhn.watchandlearn.WatchAndLearnApp;
import com.lhn.watchandlearn.api.ServerException;
import com.lhn.watchandlearn.api.request.users.AddUserRequest;
import com.lhn.watchandlearn.api.response.auth.LoginResponse;
import com.lhn.watchandlearn.ui.common.AppPreferences;
import com.lhn.watchandlearn.ui.common.BaseActivity;
import com.lhn.watchandlearn.ui.common.SimpleResponseListener;
import com.lhn.watchandlearn.ui.utils.EditTextUtils;
import com.lhn.watchandlearn.ui.utils.Validator;
import com.lhn.watchandlearn.ui.widget.FormEditText;

public class RegisterActivity extends BaseActivity implements View.OnClickListener {
	private FormEditText mTvEmail, mTvPassword, mTvRetypePassword, mTvPhone;

	private final String TAG = RegisterActivity.class.getName();

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		bindViews();
	}

	private void bindViews() {
		TextView title = (TextView) findViewById(R.id.viewTitle);
		title.setText(getString(R.string.register));

		mTvEmail = (FormEditText) findViewById(R.id.tvEmail);
		mTvEmail.setValidator(FormEditText.EmailValidator.getInstance());
		mTvEmail.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					mTvEmail.checkmarkOff();
					mTvEmail.errorOff();
				} else {
					if (!mTvEmail.validate()) {
						showErrorToast(getString(R.string.invalid_email));
					}
				}
			}
		});

		mTvPassword = (FormEditText) findViewById(R.id.tvPassword);
		mTvPassword.setValidator(FormEditText.NotEmptyValidator.getInstance());
		mTvPassword.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					mTvPassword.checkmarkOff();
					mTvPassword.errorOff();
				} else {
					if (!mTvPassword.validate()) {
						showErrorToast(getString(R.string.invalid_password));
					}
				}
			}
		});

		mTvRetypePassword = (FormEditText) findViewById(R.id.tvRetypePassword);
		mTvRetypePassword.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					mTvRetypePassword.checkmarkOff();
					mTvRetypePassword.errorOff();
				} else {
					if (TextUtils.isEmpty(mTvRetypePassword.getValue()) || !mTvRetypePassword.getValue().equals(mTvPassword.getValue())) {
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

		mTvPhone = (FormEditText) findViewById(R.id.tvPhone);
		mTvPhone.setValidator(FormEditText.NotEmptyValidator.getInstance());
		mTvPhone.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					mTvPhone.checkmarkOff();
					mTvPhone.errorOff();
				} else {
					if (!mTvPhone.validate()) {
						showErrorToast(getString(R.string.invalid_phone));
					}
				}
			}
		});

		findViewById(R.id.btnCancel).setOnClickListener(this);
		findViewById(R.id.btnSubmit).setOnClickListener(this);
	}

	private boolean validate() {
		EditTextUtils.hideKeyboard(this, mTvEmail.getEditText());
		EditTextUtils.hideKeyboard(this, mTvPassword.getEditText());
		EditTextUtils.hideKeyboard(this, mTvRetypePassword.getEditText());
		EditTextUtils.hideKeyboard(this, mTvPhone.getEditText());

		if (TextUtils.isEmpty(mTvEmail.getValue()) || !Validator.isValidEmail(mTvEmail.getValue())) {
			showErrorToast(getString(R.string.invalid_email));
			mTvEmail.errorOn();
			return false;
		}
		if (TextUtils.isEmpty(mTvPassword.getValue())) {
			showErrorToast(getString(R.string.invalid_password));
			mTvPassword.errorOn();
			return false;
		}
		if (!mTvPassword.getValue().equals(mTvRetypePassword.getValue())) {
			showErrorToast(getString(R.string.invalid_retype_password));
			mTvRetypePassword.errorOn();
			return false;
		}
		if (TextUtils.isEmpty(mTvPhone.getValue())) {
			showErrorToast(getString(R.string.invalid_phone));
			mTvPhone.errorOn();
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
				doRegister();
			}
			break;
		}
	}

	private void doRegister() {
		showLoadingDialog(getString(R.string.loading_waiting));
		AddUserRequest request = new AddUserRequest();
		request.setEmail(mTvEmail.getValue());
		request.setPassword(mTvPassword.getValue());
		request.setPhone(mTvPhone.getValue());
		request.setDeviceUUID(Secure.getString(getContentResolver(), Secure.ANDROID_ID));

		send(request, new SimpleResponseListener<LoginResponse>(this) {
			@Override
			public void onBeforeResult() {
				super.onBeforeResult();
				dismissLoadingDialog();
			}

			@Override
			public void onSuccess(LoginResponse aResult) {
				AppPreferences.setToken(RegisterActivity.this, aResult.getToken());
				WatchAndLearnApp.setCurrentUser(aResult.getUser());

				startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
				finish();
			}

			@Override
			public void onServerError(ServerException aException) {
				showErrorToast(aException.getMessage());
			}
		});
	}
}
