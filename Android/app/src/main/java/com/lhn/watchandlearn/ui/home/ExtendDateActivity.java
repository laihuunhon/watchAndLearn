package com.lhn.watchandlearn.ui.home;

import org.joda.time.DateTime;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import com.lhn.watchandlearn.R;
import com.lhn.watchandlearn.WatchAndLearnApp;
import com.lhn.watchandlearn.api.ServerException;
import com.lhn.watchandlearn.api.request.users.AddTransactionRequest;
import com.lhn.watchandlearn.api.response.users.AddTransactionResponse;
import com.lhn.watchandlearn.ui.common.BaseActivity;
import com.lhn.watchandlearn.ui.common.SimpleResponseListener;
import com.lhn.watchandlearn.ui.utils.EditTextUtils;
import com.lhn.watchandlearn.ui.widget.FormEditText;

public class ExtendDateActivity extends BaseActivity implements View.OnClickListener {
	private FormEditText mTvCardNumber, mTvCardSerial;
	private Spinner spCardType;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_extenddate);

		bindViews();
	}

	private void bindViews() {
		TextView title = (TextView) findViewById(R.id.viewTitle);
		title.setText(getString(R.string.extenddate));

		mTvCardNumber = (FormEditText) findViewById(R.id.tvCardNumber);
		mTvCardNumber.setValidator(FormEditText.NotEmptyValidator.getInstance());
		mTvCardNumber.getEditText().setOnFocusChangeListener(
				new View.OnFocusChangeListener() {
					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						if (hasFocus) {
							mTvCardNumber.checkmarkOff();
							mTvCardNumber.errorOff();
						} else {
							if (!mTvCardNumber.validate()) {
								showErrorToast(getString(R.string.invalid_field));
							}
						}
					}
				});
		
		mTvCardSerial = (FormEditText) findViewById(R.id.tvCardSerial);
		mTvCardSerial.setValidator(FormEditText.NotEmptyValidator.getInstance());
		mTvCardSerial.getEditText().setOnFocusChangeListener(
				new View.OnFocusChangeListener() {
					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						if (hasFocus) {
							mTvCardSerial.checkmarkOff();
							mTvCardSerial.errorOff();
						} else {
							if (!mTvCardSerial.validate()) {
								showErrorToast(getString(R.string.invalid_field));
							}
						}
					}
				});

		spCardType = (Spinner) findViewById(R.id.spCardType);
		findViewById(R.id.btnCancel).setOnClickListener(this);
		findViewById(R.id.btnSubmit).setOnClickListener(this);
	}

	private boolean validate() {
		EditTextUtils.hideKeyboard(this, mTvCardNumber.getEditText());
		EditTextUtils.hideKeyboard(this, mTvCardSerial.getEditText());
		
		if (TextUtils.isEmpty(mTvCardNumber.getValue())) {
			showErrorToast(getString(R.string.invalid_field));
			mTvCardNumber.errorOn();
			return false;
		}
		
		if (TextUtils.isEmpty(mTvCardSerial.getValue())) {
			showErrorToast(getString(R.string.invalid_field));
			mTvCardSerial.errorOn();
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
				doExtendDate();
			}
			break;
		}
	}
	
	private void doExtendDate() {
		showLoadingDialog(getString(R.string.loading_waiting));
		
		AddTransactionRequest request = new AddTransactionRequest();
		if ("MobiPhone".equals(spCardType.getSelectedItem().toString())) {
			request.setCardId("VMS");
		} else if ("VinaPhone".equals(spCardType.getSelectedItem().toString())) {
			request.setCardId("VNP");
		} else {
			request.setCardId("VIETTEL");
		}
		
		request.setPinField(mTvCardNumber.getValue());
		request.setSeriField(mTvCardSerial.getValue());
		
		send(request, new SimpleResponseListener<AddTransactionResponse>(this) {
			@Override
			public void onBeforeResult() {
				super.onBeforeResult();
				dismissLoadingDialog();
			}
			
			@Override
			public void onSuccess(AddTransactionResponse aResult) {		
				DateTime dt = new DateTime(aResult.getEndDate());
				showInfoToast(getString(R.string.extenddate_success) + dt.toString("dd/MM/yyyy hh:mm"));
				WatchAndLearnApp.getCurrentUser().setEndDate(aResult.getEndDate());
				WatchAndLearnApp.getCurrentUser().setHasAds(false);
			}

			@Override
			public void onServerError(ServerException aException) {
				showErrorToast(aException.getMessage());
			}
		});
	}
}
