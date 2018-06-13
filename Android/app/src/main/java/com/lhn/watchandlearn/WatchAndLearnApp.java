package com.lhn.watchandlearn;

import java.io.File;

import android.app.Application;
import android.content.pm.PackageInfo;

import com.lhn.watchandlearn.api.pojo.User;
import com.nostra13.universalimageloader.cache.disc.impl.LimitedAgeDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.StorageUtils;

public class WatchAndLearnApp extends Application {
	private static User currentUser;
	private static String currentLocale;
	private static int appVersion;

	@Override
	public void onCreate() {
		super.onCreate();
		initImageLoader();
		// initGCM();
	}

//	public void setCurrentUser(User user) {
//		currentUser = user;
//	}

	private void initImageLoader() {
		File cacheDir = StorageUtils.getCacheDirectory(this);

		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext()).threadPoolSize(5).threadPriority(Thread.MAX_PRIORITY).denyCacheImageMultipleSizesInMemory().memoryCache(new LruMemoryCache(16 * 1024 * 1024)) // 16 Mb
				.tasksProcessingOrder(QueueProcessingType.FIFO).discCache(new LimitedAgeDiscCache(cacheDir, 1000 * 60 * 60 * 24 * 2)).imageDownloader(new BaseImageDownloader(this, 5 * 1000, 30 * 1000)) // connectTimeout (5s), readTimeout(30s)
				.defaultDisplayImageOptions(new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisc(true).resetViewBeforeLoading(true).imageScaleType(ImageScaleType.NONE).build()).writeDebugLogs().build();
		ImageLoader.getInstance().init(config);
	}

	public static User getCurrentUser() {
		return currentUser;
	}

	public static void setCurrentUser(User currentUser) {
		WatchAndLearnApp.currentUser = currentUser;
	}
	
	public static String getCurrentLocale() {
		return currentLocale;
	}
	
	public static void setCurrentLocale(String currentLocale) {
		WatchAndLearnApp.currentLocale = currentLocale;
	}
	
	public static int getAppVersion() {
		return appVersion;
	}

	public static void setAppVersion(int appVersion) {
		WatchAndLearnApp.appVersion = appVersion;
	}

	// private File getAppCacheDir() {
	// File cacheDir = getExternalCacheDir();
	// if (cacheDir == null) {
	// cacheDir = getExternalFilesDir(null);
	// }
	// if (cacheDir == null) {
	// cacheDir = getCacheDir();
	// }
	// //todo check if cache dir is not available
	// return cacheDir;
	// }
	//
	// private void initGCM(){
	// String regid =
	// NotificationPreferences.getRegistrationId(getApplicationContext());
	// if (regid.length() == 0) {
	// registerBackground();
	// }
	// }
	//
	// private void registerBackground(){
	// Intent intent = new Intent(this, GcmIntentService.class);
	// startService(intent);
	// }

}
