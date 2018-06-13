package com.lhn.watchandlearn.ui.home;

import org.joda.time.DateTime;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.lhn.watchandlearn.R;
import com.lhn.watchandlearn.WatchAndLearnApp;
import com.lhn.watchandlearn.ui.common.BaseActivity;

public class ProfileActivity extends BaseActivity implements View.OnClickListener {
	private TextView tvEmail, tvPhone, tvExpireDate;

	private final String TAG = ProfileActivity.class.getName();

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);

		bindViews();
	}

	private void bindViews() {
		TextView title = (TextView) findViewById(R.id.viewTitle);
		title.setText(getString(R.string.profile));

		tvEmail = (TextView) findViewById(R.id.tvEmail);
		tvEmail.setText(WatchAndLearnApp.getCurrentUser().getEmail());

		tvPhone = (TextView) findViewById(R.id.tvPhone);
		tvPhone.setText(WatchAndLearnApp.getCurrentUser().getPhone());

		tvExpireDate = (TextView) findViewById(R.id.tvExpireDate);
		DateTime dt = new DateTime(WatchAndLearnApp.getCurrentUser().getEndDate());
		tvExpireDate.setText(dt.toString("dd/MM/yyyy hh:mm"));
		
		if (WatchAndLearnApp.getCurrentUser().getFacebookId() != null) {
			findViewById(R.id.btnChangePassword).setVisibility(View.GONE);
			tvPhone.setVisibility(View.GONE);
		}
		findViewById(R.id.btnCancel).setOnClickListener(this);
		findViewById(R.id.btnChangePassword).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnCancel:
			finish();
			break;
		case R.id.btnChangePassword:
			startActivity(new Intent(this, ChangePasswordActivity.class));
			break;
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		DateTime dt = new DateTime(WatchAndLearnApp.getCurrentUser().getEndDate());
		tvExpireDate.setText(dt.toString("dd/MM/yyyy hh:mm"));
	}
}
