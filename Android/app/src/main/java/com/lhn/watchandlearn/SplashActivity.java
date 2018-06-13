package com.lhn.watchandlearn;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lhn.watchandlearn.api.response.auth.LoginResponse;
import com.lhn.watchandlearn.api.response.config.ApiConfig;
import com.lhn.watchandlearn.api.response.config.ServersApiConfig;
import com.lhn.watchandlearn.ui.common.ApiOptions;
import com.lhn.watchandlearn.ui.common.AppPreferences;
import com.lhn.watchandlearn.ui.common.BaseActivity;
import com.lhn.watchandlearn.ui.home.ExtendDateActivity;
import com.lhn.watchandlearn.ui.home.HomeActivity;
import com.lhn.watchandlearn.ui.home.LoginActivity;
import com.lhn.watchandlearn.ui.utils.AlertUtil;
import com.lhn.watchandlearn.ui.utils.AlertUtil.IDialogCallback;
import com.startapp.android.publish.StartAppSDK;

public class SplashActivity extends BaseActivity {

	private static final String PREF_FIRST_RUN = "first.run";
	private SharedPreferences mPref;

	private Handler mHandler = new Handler();

	private final Runnable mInitializer = new Runnable() {

		@SuppressWarnings("rawtypes")
		@Override
		public void run() {
			try {
				RestTemplate restTemplate = new RestTemplate(true);
				List<HttpMessageConverter<?>> converters = restTemplate.getMessageConverters();
				for (HttpMessageConverter<?> converter : converters) {
			        if (converter instanceof MappingJackson2HttpMessageConverter) {
			            MappingJackson2HttpMessageConverter jsonConverter = (MappingJackson2HttpMessageConverter) converter;
			            jsonConverter.setObjectMapper(new ObjectMapper());
			            List<MediaType> supportMediaTypes = new ArrayList<MediaType>();
			            supportMediaTypes.add(new MediaType("application", "json", MappingJackson2HttpMessageConverter.DEFAULT_CHARSET));
			            supportMediaTypes.add(new MediaType("text", "plain", MappingJackson2HttpMessageConverter.DEFAULT_CHARSET));
			            jsonConverter.setSupportedMediaTypes(supportMediaTypes);
			        }
			    }
				ServersApiConfig servers = restTemplate.getForObject(WatchAndLearnConfig.CONFIG_URL, ServersApiConfig.class);
				ApiOptions.saveServers(servers, SplashActivity.this);

				ApiConfig apiConfig = servers.getDev();
				switch (WatchAndLearnConfig.state) {
				case Prod:
					apiConfig = servers.getProd();
					break;
				case Staging:
					apiConfig = servers.getStaging();
					break;
				default:
					apiConfig = servers.getDev();
				}

				int configVersion = Integer.valueOf(apiConfig.getVersion());
				PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
				int versionCode = pInfo.versionCode;
				WatchAndLearnApp.setAppVersion(versionCode);
				if (versionCode < configVersion) {
					SplashActivity.this.runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
							builder.setCancelable(false);
							builder.setMessage(getString(R.string.newUpdate));
							builder.setTitle(R.string.notice);
							builder.setNegativeButton(R.string.cancel, new OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									finish();									
								}
							});
							builder.setPositiveButton(R.string.ok, new OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									finish();
									Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.android.vending");
								    ComponentName comp = new ComponentName("com.android.vending", "com.google.android.finsky.activities.LaunchUrlHandlerActivity"); // package name and activity
								    launchIntent.setComponent(comp);
								    launchIntent.setData(Uri.parse("market://details?id=com.lhn.watchandlearn"));

								    startActivity(launchIntent);
								}
							});
							
							builder.create().show();
							
						}
					});					
				} else {
					mHandler.post(mProcessNext);
				}				
			} catch (Exception e) {
				e.printStackTrace();
				Log.e("poppin.api.init", "Api initialization failed", e);
				mHandler.post(mProcessError);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		StartAppSDK.init(this, "209856120", true);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		mPref = getPreferences(MODE_PRIVATE);

		initialize(this);
	}

	private Runnable mProcessError = new Runnable() {

		@Override
		public void run() {
			AlertUtil.showAlert(SplashActivity.this, R.string.error, R.string.fail_to_initialize_the_app, new IDialogCallback() {

				@Override
				public void onClick(DialogInterface dialog, int id) {
					finish();
				}
			});
		}
	};

	private Runnable mProcessNext = new Runnable() {

		@Override
		public void run() {
			if (mPref.getBoolean(PREF_FIRST_RUN, true)) {
				mPref.edit().putBoolean(PREF_FIRST_RUN, false).commit();
			}
			mNextScreenRun.run();
		}
	};

	private Runnable mNextScreenRun = new Runnable() {

		@Override
		public void run() {
			if (TextUtils.isEmpty(AppPreferences.getToken(getApplicationContext()))) {
				startActivity(new Intent(SplashActivity.this, LoginActivity.class));
				finish();
			} else {
				initUserInfo();
			}
		}
	};

	@Override
	protected void onGetUserInfoSuccess(LoginResponse aResult) {
		super.onGetUserInfoSuccess(aResult);

		startActivity(new Intent(SplashActivity.this, HomeActivity.class));
		finish();
	};

	public void initialize(final Context aContext) {
		ExecutorService service = Executors.newSingleThreadExecutor();
		service.submit(mInitializer, Boolean.TRUE);
	}
}
