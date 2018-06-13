package com.lhn.watchandlearn.ui.home;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.lhn.watchandlearn.R;
import com.lhn.watchandlearn.api.ServerException;
import com.lhn.watchandlearn.api.request.auth.ResetPasswordRequest;
import com.lhn.watchandlearn.api.response.base.Response;
import com.lhn.watchandlearn.ui.common.BaseActivity;
import com.lhn.watchandlearn.ui.common.SimpleResponseListener;
import com.lhn.watchandlearn.ui.utils.EditTextUtils;
import com.lhn.watchandlearn.ui.utils.Validator;
import com.lhn.watchandlearn.ui.widget.FormEditText;

public class ResetPasswordActivity extends BaseActivity implements View.OnClickListener {
	private FormEditText mTvEmail;
	private FormEditText mTvPhone;
	private final String TAG = ResetPasswordActivity.class.getName();

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_resetpassword);

		bindViews();
	}

	private void bindViews() {
		TextView title = (TextView) findViewById(R.id.viewTitle);
		title.setText(getString(R.string.password_recovery));

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

	private void submit() {
		EditTextUtils.hideKeyboard(this, mTvEmail.getEditText());

		if (TextUtils.isEmpty(mTvEmail.getValue()) || !Validator.isValidEmail(mTvEmail.getValue())) {
			showErrorToast(getString(R.string.invalid_email));
			mTvEmail.errorOn();
			return;
		}
		if (TextUtils.isEmpty(mTvPhone.getValue())) {
			showErrorToast(getString(R.string.invalid_phone));
			mTvPhone.errorOn();
			return;
		}

		showLoadingDialog(getString(R.string.loading_waiting));
		ResetPasswordRequest request = new ResetPasswordRequest();
		request.setEmail(mTvEmail.getValue());
		request.setPhone(mTvPhone.getValue());
		send(request, new SimpleResponseListener<Response>(this) {
			@Override
			public void onBeforeResult() {
				super.onBeforeResult();
				dismissLoadingDialog();
			}

			@Override
			public void onSuccess(final Response aResult) {
				AlertDialog.Builder builder = new AlertDialog.Builder(ResetPasswordActivity.this);
				builder.setTitle(R.string.password_recovery);
				builder.setMessage(R.string.send_reset_password_success);
				builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(final DialogInterface aDialogInterface, final int i) {
						finish();
					}
				});
				builder.setCancelable(false);
				builder.create().show();
			}

			@Override
			public void onServerError(ServerException aException) {
				showErrorToast(aException.getMessage());
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnCancel:
			finish();
			break;

		case R.id.btnSubmit:
			submit();
			break;
		}
	}
}
