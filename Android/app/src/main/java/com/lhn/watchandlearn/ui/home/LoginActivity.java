package com.lhn.watchandlearn.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.lhn.watchandlearn.R;
import com.lhn.watchandlearn.WatchAndLearnApp;
import com.lhn.watchandlearn.api.ServerException;
import com.lhn.watchandlearn.api.request.auth.LoginFBRequest;
import com.lhn.watchandlearn.api.request.auth.LoginRequest;
import com.lhn.watchandlearn.api.response.auth.LoginResponse;
import com.lhn.watchandlearn.ui.common.AppPreferences;
import com.lhn.watchandlearn.ui.common.BaseActivity;
import com.lhn.watchandlearn.ui.common.SimpleResponseListener;
import com.lhn.watchandlearn.ui.utils.EditTextUtils;
import com.lhn.watchandlearn.ui.widget.FormEditText;

import java.util.Arrays;

public class LoginActivity extends BaseActivity implements View.OnClickListener {
	private FormEditText mEmailText;
	private FormEditText mPasswordText;
	private Button btnDummyLoginFB;
	private CallbackManager mCallbackManager;

	private final String TAG = LoginActivity.class.getName();

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		FacebookSdk.sdkInitialize(this.getApplicationContext());

		mCallbackManager = CallbackManager.Factory.create();
		LoginManager.getInstance().registerCallback(mCallbackManager,
			new FacebookCallback<LoginResult>() {
				@Override
				public void onSuccess(LoginResult loginResult) {
					Log.d(TAG, "SUCCESS");

					showLoadingDialog(getString(R.string.loading_waiting));
					LoginFBRequest request = new LoginFBRequest();
					request.setAccessToken(loginResult.getAccessToken().getToken());
					request.setDeviceUUID(Secure.getString(getContentResolver(), Secure.ANDROID_ID));

					send(request, new SimpleResponseListener<LoginResponse>(LoginActivity.this) {
						@Override
						public void onBeforeResult() {
							super.onBeforeResult();
							dismissLoadingDialog();
						}

						@Override
						public void onSuccess(final LoginResponse aResult) {
							AppPreferences.setToken(LoginActivity.this, aResult.getToken());
							WatchAndLearnApp.setCurrentUser(aResult.getUser());

							startActivity(new Intent(LoginActivity.this, HomeActivity.class));
							finish();
						}

						@Override
						public void onServerError(ServerException aException) {
							showErrorToast(aException.getMessage());
						}
					});
				}

				@Override
				public void onCancel() {
					Log.d(TAG, "CANCEL");
				}

				@Override
				public void onError(FacebookException exception) {
					Log.d(TAG, "ERROR");
					exception.printStackTrace();
				}
			});

		bindViews();
	}

	private void bindViews() {
		mEmailText = (FormEditText) findViewById(R.id.edtEmail);
		mEmailText.setValidator(FormEditText.EmailValidator.getInstance());

		mEmailText.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					mEmailText.checkmarkOff();
					mEmailText.errorOff();
				} else {
					if (!mEmailText.validate()) {
						showErrorToast(getString(R.string.invalid_email));
					}
				}
			}
		});
		mPasswordText = (FormEditText) findViewById(R.id.edtPassword);
		mPasswordText.setValidator(FormEditText.NotEmptyValidator.getInstance());
		mPasswordText.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					mPasswordText.errorOff();
				} else {
					if (!mPasswordText.validate()) {
						showErrorToast(getString(R.string.invalid_password));
					}
				}
			}
		});
		mPasswordText.getEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {

			@Override
			public boolean onEditorAction(final TextView aTextView, final int i, final KeyEvent aKeyEvent) {
				if (i == EditorInfo.IME_ACTION_DONE) {
					doLogin();
					EditTextUtils.hideKeyboard(LoginActivity.this);
					return true;
				}
				return false;
			}
		});
		
		btnDummyLoginFB = (Button) findViewById(R.id.btnDummyLoginFB);
		btnDummyLoginFB.setOnClickListener(this);

		findViewById(R.id.btnLogin).setOnClickListener(this);
		findViewById(R.id.btnRegister).setOnClickListener(this);
		findViewById(R.id.tvForgotPassword).setOnClickListener(this);
	}

	private void doLogin() {
		EditTextUtils.hideKeyboard(this, mPasswordText.getEditText());
		EditTextUtils.hideKeyboard(this, mEmailText.getEditText());

		if (!mEmailText.validate()) {
			showErrorToast(getString(R.string.invalid_email));
			return;
		}

		if (!mPasswordText.validate()) {
			showErrorToast(getString(R.string.invalid_password));
			return;
		}

		showLoadingDialog(getString(R.string.loading_waiting));
		LoginRequest request = new LoginRequest();
		request.setEmail(mEmailText.getValue());
		request.setPassword(mPasswordText.getValue());
		send(request, new SimpleResponseListener<LoginResponse>(this) {
			@Override
			public void onBeforeResult() {
				super.onBeforeResult();
				dismissLoadingDialog();
			}

			@Override
			public void onSuccess(final LoginResponse aResult) {
				AppPreferences.setToken(LoginActivity.this, aResult.getToken());
				WatchAndLearnApp.setCurrentUser(aResult.getUser());

				startActivity(new Intent(LoginActivity.this, HomeActivity.class));
				finish();
			}

			@Override
			public void onServerError(ServerException aException) {
				showErrorToast(aException.getMessage());
			}
		});
	}
	
	private void doLoginFB() {
		Log.d(TAG, "doLoginFB");
		LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email"));
//		Session session = Session.getActiveSession();
//	    if (session.isOpened()) {
//	    	showLoadingDialog(getString(R.string.loading_waiting));
//			LoginFBRequest request = new LoginFBRequest();
//			request.setAccessToken(session.getAccessToken());
//			request.setDeviceUUID(Secure.getString(getContentResolver(), Secure.ANDROID_ID));
//
//			send(request, new SimpleResponseListener<LoginResponse>(this) {
//				@Override
//				public void onBeforeResult() {
//					super.onBeforeResult();
//					dismissLoadingDialog();
//				}
//
//				@Override
//				public void onSuccess(final LoginResponse aResult) {
//					AppPreferences.setToken(LoginActivity.this, aResult.getToken());
//					WatchAndLearnApp.setCurrentUser(aResult.getUser());
//
//					startActivity(new Intent(LoginActivity.this, HomeActivity.class));
//					finish();
//				}
//
//				@Override
//				public void onServerError(ServerException aException) {
//					showErrorToast(aException.getMessage());
//				}
//			});
//	    } else {
//	    	loginFbBtn.performClick();
//	    }
	}

	@Override
	public void onClick(final View aView) {
		switch (aView.getId()) {
		case R.id.btnLogin:
			doLogin();
			break;
		case R.id.btnRegister:
			startActivity(new Intent(this, RegisterActivity.class));
			break;
		case R.id.tvForgotPassword:
			startActivity(new Intent(this, ResetPasswordActivity.class));
			break;
		case R.id.btnDummyLoginFB:
			doLoginFB();
			break;
		}
	}

	// private void saveGcmRegistration(String aRegId){
	// SaveGcmRegistrationIdRequest request = Requests.create(this,
	// SaveGcmRegistrationIdRequest.class);
	// request.setGcmRegistrationId(aRegId);
	// send(request, new SimpleResponseListener<Response>(this){
	//
	// @Override
	// public void onBeforeResult(){
	// dismissLoadingDialog();
	// }
	//
	// @Override
	// public void onSuccess(Response aResult){
	// startActivity(new Intent(LoginActivity.this, HomeActivity.class));
	// finish();
	// }
	//
	// @Override
	// public void onNetworkError(Throwable e){
	// Log.e(TAG, "Failed to save GCM reg Id for user to push notification", e);
	// }
	//
	// @Override
	// public void onServerError(ServerException e){
	// Log.e(TAG, "Failed to save GCM reg Id for user to push notification");
	// }
	// });
	// }
	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (mCallbackManager.onActivityResult(requestCode, resultCode, data)) {
			return;
		}
	}

	@Override
	public void onSaveInstanceState(Bundle savedState) {
		super.onSaveInstanceState(savedState);
	}

}
