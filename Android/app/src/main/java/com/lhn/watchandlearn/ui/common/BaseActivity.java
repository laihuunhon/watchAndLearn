package com.lhn.watchandlearn.ui.common;

import java.lang.ref.SoftReference;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.lhn.watchandlearn.R;
import com.lhn.watchandlearn.SplashActivity;
import com.lhn.watchandlearn.WatchAndLearnApp;
import com.lhn.watchandlearn.WatchAndLearnConfig;
import com.lhn.watchandlearn.api.ServerException;
import com.lhn.watchandlearn.api.WALSpiceService;
import com.lhn.watchandlearn.api.pojo.Movie;
import com.lhn.watchandlearn.api.request.base.ApiRequest;
import com.lhn.watchandlearn.api.request.users.CheckUserEndDateRequest;
import com.lhn.watchandlearn.api.request.users.GetUserRequest;
import com.lhn.watchandlearn.api.response.auth.LoginResponse;
import com.lhn.watchandlearn.api.response.config.ApiConfig;
import com.lhn.watchandlearn.api.response.users.CheckUserEndDateResponse;
import com.lhn.watchandlearn.ui.home.ExtendDateActivity;
import com.lhn.watchandlearn.ui.home.HomeActivity;
import com.lhn.watchandlearn.ui.home.LoginActivity;
import com.lhn.watchandlearn.ui.home.MovieActivity;
import com.lhn.watchandlearn.ui.utils.ImageUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.DiscCacheUtil;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;

import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;

public class BaseActivity extends AppCompatActivity {

    private SpiceManager mManager;
    private ProgressDialog mProgressDialog;
    private Handler mHandler = new Handler();
    
    private final String TAG = BaseActivity.class.getName();

    private Hashtable<String, SoftReference<Bitmap>> mBitmapCache = new Hashtable<String, SoftReference<Bitmap>>();

    @Override
    protected void onCreate(final Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        
        getSupportActionBar().hide();
//        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mManager = new SpiceManager(WALSpiceService.class);
        WatchAndLearnApp.setCurrentLocale(getResources().getConfiguration().locale.toString());
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Iterator<Map.Entry<String, SoftReference<Bitmap>>> iterator = mBitmapCache.entrySet().iterator();

        while(iterator.hasNext()){
            Map.Entry<String, SoftReference<Bitmap>> entry = iterator.next();
            if(entry != null && entry.getValue().get() != null){
                entry.getValue().get().recycle();
            }
        }
        mBitmapCache.clear();
    }

    @Override
    protected void onStart(){
        super.onStart();
        mManager.start(this);
    }

    @Override
    protected void onStop(){
        dismissLoadingDialog();
        mManager.shouldStop();
        clearAllToast();

        super.onStop();
    }

    public void clearRequestsCache(){
        mManager.removeAllDataFromCache();
    }
    
    public void showErrorToast(String aErrorMessage){
        View view = getLayoutInflater().inflate(R.layout.view_error, null);
        TextView textView = (TextView) view.findViewById(R.id.tvError);
        textView.setText(aErrorMessage);
        clearAllToast();
        Configuration croutonConfiguration = new Configuration.Builder().setDuration(Configuration.DURATION_SHORT)
                .build();
        Crouton.make(this, view).setConfiguration(croutonConfiguration).show();
    }

    public void showInfoToast(String aInfoMessage){
        View view = getLayoutInflater().inflate(R.layout.view_info, null);
        TextView textView = (TextView) view.findViewById(R.id.tvInfo);
        textView.setText(aInfoMessage);
        clearAllToast();
        Configuration croutonConfiguration = new Configuration.Builder().setDuration(Configuration.DURATION_SHORT)
                .build();
        Crouton.make(this, view).setConfiguration(croutonConfiguration).show();
    }

    public void showInfoToast(String aInfoMessage, int aViewGroupResId){
        View view = getLayoutInflater().inflate(R.layout.view_info, null);
        TextView textView = (TextView) view.findViewById(R.id.tvInfo);
        textView.setText(aInfoMessage);
        Crouton.make(this, view, aViewGroupResId).show();
    }

    public void clearAllToast(){
        Crouton.cancelAllCroutons();
    }

    public void showLoadingDialog(){
        showLoadingDialog("", "");
    }

    public void showLoadingDialog(String aMessage){
        showLoadingDialog("", aMessage);
    }

    public void showLoadingDialog(String aTitle, String aMessage){
        if (mProgressDialog == null || !mProgressDialog.isShowing()) {
            mProgressDialog = ProgressDialog.show(this, aTitle, aMessage);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }
    }

    public void dismissLoadingDialog(){
        if(mProgressDialog != null){
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    public <T> void send(ApiRequest<T> aRequest, ResponseListener<T> aListener){
        ApiConfig apiConfig = ApiOptions.loadApi(WatchAndLearnConfig.state, this);
        aRequest.setApiConfig(apiConfig);
        if (aRequest.isRequireAuth()) {
        	aRequest.setAuthorization(AppPreferences.getToken(this));
        }
        
        aListener.setApiRequest(aRequest);
        mManager.execute(aRequest, aListener.getRequestListener());
    }

    public <T> void getCache(ApiRequest<T> aRequest, ResponseListener<T> aListener){
        ApiConfig apiConfig = ApiOptions.loadApi(WatchAndLearnConfig.state, this);
        aRequest.setApiConfig(apiConfig);
                
        aListener.setApiRequest(aRequest);
        mManager.execute(aRequest, aRequest.getKey(), DurationInMillis.ONE_MINUTE, aListener.getRequestListener());
    }

    public void pushFragment(Class<? extends Fragment> cls, Bundle args, int viewId){
        pushFragment(cls, args, viewId, cls.getName(), false);
    }

    public void pushFragment(Class<? extends Fragment> cls, Bundle args, int viewId, boolean addToBackStack){
        pushFragment(cls, args, viewId, cls.getName(), addToBackStack);
    }

    public void pushFragment(Class<? extends Fragment> cls, Bundle args, int viewId, String tag, boolean addToBackStack){
        FragmentManager manager = getFragmentManager();
        FragmentTransaction tr = manager.beginTransaction();

        if(manager.findFragmentByTag(cls.getName()) == null){
            tr.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
            Fragment fragment = Fragment.instantiate(this, cls.getName(), args);

            tr.replace(viewId, fragment, tag);
            if(addToBackStack){
                tr.addToBackStack(null);
            }

            tr.commit();
        }
    }
    
    public void initUserInfo() {
		GetUserRequest request = new GetUserRequest();
		send(request, new SimpleResponseListener<LoginResponse>(this) {
			@Override
			public void onSuccess(LoginResponse aResult) {
				Log.i(TAG, aResult.getUser().toString());
				onGetUserInfoSuccess(aResult);
			}
			
			@Override
			public void onServerError(ServerException e) {
				startActivity(new Intent(BaseActivity.this,
						LoginActivity.class));
				finish();
			}
        });
	}
    
    protected void onGetUserInfoSuccess(LoginResponse aResult) {
    	WatchAndLearnApp.setCurrentUser(aResult.getUser());
    }
    
    public void loadImage(ImageView aImageView, String aFileName){
        if(TextUtils.isEmpty(aFileName) || !ImageUtils.isImageUrl(aFileName)){
            return;
        }
        boolean cachedImageFile = DiscCacheUtil.findInCache(aFileName,
                                                            ImageLoader.getInstance().getDiscCache()) != null;
        if(cachedImageFile){
            aImageView.setImageDrawable(null);
            ImageLoader.getInstance().displayImage(aFileName, aImageView,
                                                   new DisplayImageOptions.Builder().
                                                           cacheOnDisc(true).
                                                           cacheInMemory(true).
                                                           build()
            );
        } else{
            ImageLoader.getInstance().displayImage(aFileName, aImageView,
                                                   new DisplayImageOptions.Builder().
                                                           cacheOnDisc(true).
                                                           cacheInMemory(true).
                                                           resetViewBeforeLoading(true).
                                                           build()
            );
        }
    }
    
    public void checkUserEndDate(final String movieId) {
    	showLoadingDialog(getString(R.string.loading_waiting));
		CheckUserEndDateRequest request = new CheckUserEndDateRequest();

		send(request, new SimpleResponseListener<CheckUserEndDateResponse>(BaseActivity.this) {
			@Override
			public void onBeforeResult() {
				super.onBeforeResult();
				dismissLoadingDialog();
			}
			
			@Override
			public void onSuccess(final CheckUserEndDateResponse aResult) {
				Log.i(TAG, aResult.toString());

				if (aResult.getIsExpired()) {
                    WatchAndLearnApp.getCurrentUser().setHasAds(true);
//					showErrorToast(getString(R.string.txt_account_expired));
				} else {
                    WatchAndLearnApp.getCurrentUser().setHasAds(false);
				}
                Intent intent = new Intent(BaseActivity.this, MovieActivity.class);
                intent.putExtra("movieId", movieId);
                startActivity(intent);
			}
		});
	}
    
    public void checkUserEndDateOnMoviePage() {
		CheckUserEndDateRequest request = new CheckUserEndDateRequest();

		send(request, new SimpleResponseListener<CheckUserEndDateResponse>(BaseActivity.this) {
			@Override
			public void onSuccess(final CheckUserEndDateResponse aResult) {
				Log.i(TAG, aResult.toString());

				if (aResult.getIsExpired()) {
                    WatchAndLearnApp.getCurrentUser().setHasAds(true);
				} else {
                    WatchAndLearnApp.getCurrentUser().setHasAds(false);
                }
			}
		});
    }
    
    public void onInvalidToken() {
    	AppPreferences.clear(this);
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setCancelable(false);
		builder.setMessage(getString(R.string.invalidToken));
		builder.setTitle(R.string.error);
		builder.setPositiveButton(R.string.ok, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
				
				startActivity(intent);				
			}
		});
		builder.create().show();
    }
    
    public void onExpiredDate() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setCancelable(false);
		builder.setMessage(getString(R.string.txt_account_expired));
		builder.setTitle(R.string.error);
		builder.setPositiveButton(R.string.ok, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
				Intent intent = new Intent(getApplicationContext(), ExtendDateActivity.class);				
				startActivity(intent);				
			}
		});
		builder.create().show();
    }

    public void onUpdateVersion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
    
    @Override
    protected void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
    	WatchAndLearnApp.setCurrentLocale(getResources().getConfiguration().locale.toString());
    }

//    protected void loadPurchase(){
//        GetPurchaseRequest request = Requests.create(this, GetPurchaseRequest.class);
//        send(request, new SimpleResponseListener<PurchaseResponse>(this){
//
//            @Override
//            public void onSuccess(PurchaseResponse aResult){
//                showPurchaseInfo(aResult);
//            }
//
//            @Override
//            public void onNetworkError(Throwable e){
//                showErrorToast("Network Error!!!");
//            }
//
//            @Override
//            public void onServerError(ServerException e){
//                showErrorToast("Failed to get purchase info.");
//            }
//        });
//    }
//
//    protected void postPurchase(){
//    }
//
//    protected IDialogCallback mPurchaseCallback = new IDialogCallback(){
//
//        @Override
//        public void onClick(DialogFragment dialog, int id){
//            postPurchase();
//        }
//    };
//
//    protected void showPurchaseInfo(PurchaseResponse aResult){
//        if(PurchaseResponse.Data.STATUS_TRIAL.equals(aResult.getData().getStatus())){
//            if(aResult.getData().getTrial().getPeriod() > 14){
//                if(!LoginPreferences.isShowedTrial(this)){
//                    String endDate = aResult.getData().getTrial().getEndDate();
//                    String message1 = String.format(getResources().getString(R.string.free_trial_message_1),
//                                                    DateUtils.format(endDate));
//                    String message2 = String.format(getResources().getString(R.string.upgrade_message),
//                                                    aResult.getData().getSeasonYear());
//                    FreeTrialDialog.showDialog(getFragmentManager(), message1, message2, mPurchaseCallback);
//                    LoginPreferences.setShowedTrial(this, true);
//                }
//            } else{
//                String message1 = String.format(getResources().getString(R.string.free_trial_message_2), aResult
//                        .getData().getTrial().getPeriod());
//                String message2 = String.format(getResources().getString(R.string.upgrade_message),
//                                                aResult.getData().getSeasonYear());
//                FreeTrialDialog.showDialog(getFragmentManager(), message1, message2, mPurchaseCallback);
//            }
//            LoginPreferences.setPurchaseResponse(this, aResult);
//        } else if(PurchaseResponse.Data.STATUS_NEED_BUY.equals(aResult.getData().getStatus())){
//            String msg1 = String.format(getResources().getString(R.string.restricted_message), aResult.getData()
//                    .getSeasonYear());
//            String msg2 = String.format(getResources().getString(R.string.upgrade_message),
//                                        aResult.getData().getSeasonYear());
//            RestrictionDialog.show(getFragmentManager(), msg1, msg2, new IDialogCallback(){
//
//                @Override
//                public void onClick(DialogFragment dialog, int id){
//                    startActivityForResult(new Intent(BaseActivity.this, StoreActivity.class), SlideMenuFragment.RESULT_STORES);
//                }
//            });
//            LoginPreferences.setPurchaseResponse(this, aResult);
//        } else{
//            LoginPreferences.removePurchaseResponse(this);
//            postPurchase();
//        }
//    }
}
