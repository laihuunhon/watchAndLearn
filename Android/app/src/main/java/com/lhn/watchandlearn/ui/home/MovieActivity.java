package com.lhn.watchandlearn.ui.home;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd;
import com.lhn.watchandlearn.R;
import com.lhn.watchandlearn.WatchAndLearnApp;
import com.lhn.watchandlearn.WatchAndLearnConfig;
import com.lhn.watchandlearn.api.ServerException;
import com.lhn.watchandlearn.api.pojo.Movie;
import com.lhn.watchandlearn.api.pojo.Video;
import com.lhn.watchandlearn.api.request.movies.AddWatchedRequest;
import com.lhn.watchandlearn.api.request.movies.GetMovieRequest;
import com.lhn.watchandlearn.api.response.base.Response;
import com.lhn.watchandlearn.api.response.config.ApiConfig;
import com.lhn.watchandlearn.api.response.movies.MovieResponse;
import com.lhn.watchandlearn.ui.common.ApiOptions;
import com.lhn.watchandlearn.ui.common.BaseActivity;
import com.lhn.watchandlearn.ui.common.SimpleResponseListener;
import com.lhn.watchandlearn.ui.utils.EditTextUtils;
import com.lhn.watchandlearn.ui.widget.MovieFragmentPagerAdapter;
import com.lhn.watchandlearn.ui.widget.VideoControllerView;
import com.lhn.watchandlearn.utils.Caption;
import com.lhn.watchandlearn.utils.FormatSRT;
import com.lhn.watchandlearn.utils.TimedTextObject;
import com.startapp.android.publish.Ad;
import com.startapp.android.publish.AdDisplayListener;
import com.startapp.android.publish.StartAppAd;

public class MovieActivity extends BaseActivity implements
		SurfaceHolder.Callback, MediaPlayer.OnPreparedListener,
		VideoControllerView.MediaPlayerControl {
	private final String TAG = MovieActivity.class.getName();

	private SubtitleProcessingTask subsFetchTask;
	private FrameLayout videoSurfaceContainer;
	private SurfaceView videoSurface;
	private MediaPlayer player;
	private VideoControllerView controller;
	private Boolean isFullscreen = false;
	private TextView subtitleText;
	private Boolean isPause = false;
	private String currentMovieId;
	private int currentVideoIndex;
	private Movie currentMovie;
	private Video currentVideo;
	private CharSequence fontsizes[] = new CharSequence[] { "15", "20", "25",
			"30" };
	private CharSequence subtitleType[];
	public TimedTextObject currentSrt;
	public TimedTextObject englishSrt;
	public TimedTextObject vietnameseSrt;
	private ViewPager viewPager;
	private MovieFragmentPagerAdapter movieFragmentPagerAdapter;
	private int currentSelectSubtitle = 1;
	private int currentSelectFontSize = 0;
	private Boolean isBackPress = false;

	Timer checkExpDateTimer;

//	InterstitialAd mInterstitialAd = null;

	private StartAppAd startAppAd = new StartAppAd(this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_movie);

		if (!WatchAndLearnConfig.isDebugMode() && WatchAndLearnApp.getCurrentUser().getHasAds()) {
			AdView mAdView = (AdView) findViewById(R.id.adView);
			mAdView.setVisibility(View.VISIBLE);
			AdRequest adRequest = new AdRequest.Builder().build();
			mAdView.loadAd(adRequest);
		}

		currentMovieId = getIntent().getStringExtra("movieId");
		currentVideoIndex = getIntent().getIntExtra("videoIndex", 1);

		videoSurface = (SurfaceView) findViewById(R.id.videoSurface);
		SurfaceHolder videoHolder = videoSurface.getHolder();
		videoHolder.addCallback(this);

		videoSurfaceContainer = (FrameLayout) findViewById(R.id.videoSurfaceContainer);
		videoSurfaceContainer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				controller.show();
			}
		});

		controller = new VideoControllerView(this);
		subtitleText = (TextView) findViewById(R.id.offLine_subtitleText);

		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		android.widget.LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) videoSurfaceContainer
				.getLayoutParams();
		params.height = displaymetrics.heightPixels / 2;

		getMovieDetail();

		try {
			player = new MediaPlayer();
			player.setAudioStreamType(AudioManager.STREAM_MUSIC);
			player.reset();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} finally {
			if (null == player) {
				Toast.makeText(getApplicationContext(), "Error",
						Toast.LENGTH_SHORT).show();
				return;
			}
		}

		player.setOnPreparedListener(new OnPreparedListener() {

			@Override
			public void onPrepared(MediaPlayer mp) {
				dismissLoadingDialog();
				subsFetchTask = new SubtitleProcessingTask();
				subsFetchTask.execute();

				controller.setMediaPlayer(MovieActivity.this);
				controller
						.setAnchorView((LinearLayout) findViewById(R.id.videoControllerContainer));

				player.start();
				isPause = false;
				addWatched();
			}
		});
		player.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
			}
		});
	}

	private void getMovieDetail() {
		GetMovieRequest request = new GetMovieRequest();
		request.setId(currentMovieId);

		showLoadingDialog(getString(R.string.loading_waiting));
		send(request, new SimpleResponseListener<MovieResponse>(this) {
			@Override
			public void onSuccess(final MovieResponse aResult) {
				currentMovie = aResult.getMovie();
				currentVideo = currentMovie.getItems().get(
						currentVideoIndex - 1);
				if (currentVideo.getUrl().isEmpty()) {
					dismissLoadingDialog();
					AlertDialog.Builder builder = new AlertDialog.Builder(
							MovieActivity.this);
					builder.setCancelable(false);
					builder.setMessage(getString(R.string.txt_video_error2));
					builder.setTitle(R.string.error);
					builder.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
													int which) {
									finish();
								}
							});
					builder.create().show();
				} else {
					initTabLayout();

					bindVideoData();

					checkExpDateTimer = new Timer();
					checkExpDateTimer.scheduleAtFixedRate(new TimerTask() {
						@Override
						public void run() {
							System.out.println("+++++++++++++++++++++++++ CHECK END DATE");
							checkUserEndDateOnMoviePage();

							MovieActivity.this.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									if (player != null && player.isPlaying()) {
										player.pause();
										isPause = true;
									}
									startAppAd.showAd(new AdDisplayListener() {
										@Override
										public void adHidden(Ad ad) {
											player.start();
											isPause = false;
											if (subtitleDisplayHandler != null) {
												subtitleDisplayHandler.post(subtitleProcessesor);
											}
										}

										@Override
										public void adDisplayed(Ad ad) {

										}

										@Override
										public void adClicked(Ad ad) {

										}

										@Override
										public void adNotDisplayed(Ad ad) {

										}
									});
									startAppAd.loadAd(); // load the next ad
								}
							});
						}
					}, 30000, 900000);
				}
			}

			@Override
			public void onServerError(ServerException aException) {
				dismissLoadingDialog();
				if (aException.getErrorCode().equals("EM00002")) {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							MovieActivity.this);
					builder.setCancelable(false);
					builder.setMessage(getString(R.string.txt_video_error));
					builder.setTitle(R.string.error);
					builder.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									finish();
								}
							});
					builder.create().show();
				} else {
					showErrorToast(aException.getMessage());
				}
			}
		});
	}

	private void initTabLayout() {
		viewPager = (ViewPager) findViewById(R.id.viewpager);
		movieFragmentPagerAdapter = new MovieFragmentPagerAdapter(
				getSupportFragmentManager(), this, currentMovie,
				currentVideoIndex);

		viewPager.setAdapter(movieFragmentPagerAdapter);

		// Give the TabLayout the ViewPager
		TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
		tabLayout.setupWithViewPager(viewPager);

		viewPager.addOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				EditTextUtils.hideKeyboard(MovieActivity.this);
			}

			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {
				EditTextUtils.hideKeyboard(MovieActivity.this);
			}

			@Override
			public void onPageScrollStateChanged(int state) {
				EditTextUtils.hideKeyboard(MovieActivity.this);
			}
		});
	}

	private void bindVideoData() {
		try {
			player.setDataSource(getApplicationContext(),
					Uri.parse(currentVideo.getUrl()));
			player.prepareAsync();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null == player) {
				Toast.makeText(getApplicationContext(), "Error",
						Toast.LENGTH_SHORT).show();
				return;
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		startAppAd.onPause();

		System.out.println("+++ PAUSE +++");
		if (isBackPress) {
			System.out.println("+++ PAUSE + BACK +++");
			cleanUp();
		} else {
			if (player != null && player.isPlaying()) {
				System.out.println(String.valueOf(isBackPress));

				player.pause();
				isPause = true;
			} else {
				return;
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		startAppAd.onResume();

		System.out.println("+++ RESUME +++");
	}
	
	// Implement SurfaceHolder.Callback
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			player.setDisplay(holder);

			if (!isPause) {
				player.prepareAsync();
			} else {
				player.start();
				isPause = false;
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {

	}

	// End SurfaceHolder.Callback

	@Override
	public void finish() {
		super.finish();
	}
	
	@Override
	public void onInvalidToken() {
		isBackPress = true;
		if (player != null && player.isPlaying()) {
			player.pause();
		}
		
		super.onInvalidToken();
	}
	
	@Override
	public void onExpiredDate() {
		isBackPress = true;
		if (player != null && player.isPlaying()) {
			player.pause();
		}
		
		super.onExpiredDate();
	}

	private void cleanUp() {
		try {
			checkExpDateTimer.cancel();
			if (subtitleDisplayHandler != null) {
				subtitleDisplayHandler.removeCallbacks(subtitleProcessesor);
			}
			if (player != null) {
				player.release();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Runnable subtitleProcessesor = new Runnable() {

		@Override
		public void run() {
			try {
				if (player != null && player.isPlaying()) {
					int currentPos = player.getCurrentPosition();
					if (currentSelectSubtitle == 3) {
						Collection<Caption> enSubtitles = englishSrt.captions
								.values();
						Collection<Caption> vnSubtitles = vietnameseSrt.captions
								.values();
						Caption enCaption = null;
						Caption vnCaption = null;
						for (Caption caption : enSubtitles) {
							if (currentPos >= caption.start.mseconds
									&& currentPos <= caption.end.mseconds) {
								enCaption = caption;
								break;
							} else if (currentPos > caption.end.mseconds) {
								enCaption = null;
							}
						}
						for (Caption caption : vnSubtitles) {
							if (currentPos >= caption.start.mseconds
									&& currentPos <= caption.end.mseconds) {
								vnCaption = caption;
								break;
							} else if (currentPos > caption.end.mseconds) {
								vnCaption = null;
							}
						}
						if (enCaption != null) {
							onTimedTextString(Html.fromHtml(enCaption.content
									+ vnCaption.content));
						} else {
							onTimedTextString("");
						}
					} else {
						Collection<Caption> subtitles = currentSrt.captions
								.values();

						for (Caption caption : subtitles) {
							if (currentPos >= caption.start.mseconds
									&& currentPos <= caption.end.mseconds) {
								onTimedText(caption);
								break;
							} else if (currentPos > caption.end.mseconds) {
								onTimedText(null);
							}
						}
					}

				}
				subtitleDisplayHandler.postDelayed(this, 100);
			} catch (IllegalStateException e) {
				e.printStackTrace();
			}
		}
	};
	private Handler subtitleDisplayHandler = new Handler();
	private boolean mDragging;

	public class SubtitleProcessingTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			getSubtitleFromServer("en");
			getSubtitleFromServer("vn");
			List<String> listSubtitle = new ArrayList<String>();
			listSubtitle.add("None");
			if (null != vietnameseSrt) {
				listSubtitle.add("Vietnamese");
			}
			if (null != englishSrt) {
				listSubtitle.add("English");
			}
			if (null != englishSrt && null != vietnameseSrt) {
				listSubtitle.add("Both");
			}
			subtitleType = listSubtitle.toArray(new CharSequence[listSubtitle
					.size()]);

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (null != vietnameseSrt) {
				currentSrt = vietnameseSrt;
				subtitleText.setText("");
				subtitleDisplayHandler.post(subtitleProcessesor);
			} else if (null != englishSrt) {
				currentSrt = englishSrt;
				subtitleText.setText("");
				subtitleDisplayHandler.post(subtitleProcessesor);
			}
			super.onPostExecute(result);
		}
	}

	private void getSubtitleFromServer(String type) {
		int count;
		try {
			// URL url = new URL(WatchAndLearnConfig.ROOT_WS_URL
			// + "/resources/" + currentMovieId + "/"
			// + currentVideoIndex + "/" + type + ".vtt");

			ApiConfig apiConfig = ApiOptions.loadApi(WatchAndLearnConfig.state,
					this);
			URL url = new URL(apiConfig.getApiUrl().replace("/api/v1",
					"/resources")
					+ currentMovieId
					+ "/"
					+ currentVideoIndex
					+ "/"
					+ type
					+ ".vtt");
			InputStream is = url.openStream();
			File f = getExternalFile(type);
			FileOutputStream fos = new FileOutputStream(f);
			byte data[] = new byte[1024];
			while ((count = is.read(data)) != -1) {
				fos.write(data, 0, count);
			}
			is.close();
			fos.close();

			InputStream stream = new FileInputStream(getApplicationContext()
					.getExternalFilesDir(null).getPath() + "/" + type + ".srt");
			FormatSRT formatSRT = new FormatSRT();
			if ("en".equals(type)) {
				englishSrt = formatSRT.parseFile(type + ".srt", stream);
			} else {
				vietnameseSrt = formatSRT.parseFile(type + ".srt", stream);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "error in downloading subs");
		}
	}

	public void onTimedText(Caption text) {
		if (text == null) {
			subtitleText.setText("");
			// subtitleText.setVisibility(View.INVISIBLE);
			return;
		}
		text.content = text.content.replaceAll("<br />$", "");
		subtitleText.setText(Html.fromHtml(text.content));
		// subtitleText.setVisibility(View.VISIBLE);
	}

	public void onTimedTextString(CharSequence text) {
		subtitleText.setText(text);
	}

	public File getExternalFile(String type) {
		File srt = null;
		try {
			srt = new File(getApplicationContext().getExternalFilesDir(null)
					.getPath() + "/" + type + ".srt");
			srt.createNewFile();
			return srt;
		} catch (Exception e) {
			Log.e(TAG, "exception in file creation");
		}
		return null;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		// TODO Auto-generated method stub

	}

	private int convertDpToPx(int dp) {
		Resources r = this.getApplicationContext().getResources();
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				r.getDisplayMetrics());
	}

	public Movie getMovie() {
		return currentMovie;
	}

	// Implement VideoMediaController.MediaPlayerControl
	@Override
	public boolean canPause() {
		return true;
	}

	@Override
	public boolean canSeekBackward() {
		return true;
	}

	@Override
	public boolean canSeekForward() {
		return true;
	}

	@Override
	public int getBufferPercentage() {
		return 0;
	}

	@Override
	public int getCurrentPosition() {
		try {
			return player.getCurrentPosition();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public int getDuration() {
		try {
			return player.getDuration();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public boolean isPlaying() {
		try {
			return player.isPlaying();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public void pause() {
		try {
			player.pause();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void seekTo(int i) {
		try {
			player.seekTo(i);
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void start() {
		try {
			player.start();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isFullScreen() {
		return isFullscreen;
	}

	@Override
	public void toggleFullScreen() {
		if (isFullscreen) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			DisplayMetrics displaymetrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
			android.widget.LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) videoSurfaceContainer
					.getLayoutParams();
			params.height = displaymetrics.heightPixels / 2;
			android.widget.FrameLayout.LayoutParams videoParams = (android.widget.FrameLayout.LayoutParams) videoSurface
					.getLayoutParams();
			int px = convertDpToPx(50);
			videoParams.bottomMargin = px;
			videoParams.topMargin = px;

			android.widget.FrameLayout.LayoutParams subtitleParams = (android.widget.FrameLayout.LayoutParams) subtitleText
					.getLayoutParams();
			subtitleParams.bottomMargin = px;

			isFullscreen = false;
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			DisplayMetrics displaymetrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
			android.widget.LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) videoSurfaceContainer
					.getLayoutParams();
			params.height = displaymetrics.heightPixels;

			android.widget.FrameLayout.LayoutParams videoParams = (android.widget.FrameLayout.LayoutParams) videoSurface
					.getLayoutParams();
			videoParams.bottomMargin = 0;
			videoParams.topMargin = 0;

			android.widget.FrameLayout.LayoutParams subtitleParams = (android.widget.FrameLayout.LayoutParams) subtitleText
					.getLayoutParams();
			int px = convertDpToPx(20);
			subtitleParams.bottomMargin = px;

			isFullscreen = true;
		}
	}

	@Override
	public void close() {
		isBackPress = true;
		finish();
	}

	@Override
	public void openFontSizeDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setSingleChoiceItems(fontsizes, currentSelectFontSize,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						currentSelectFontSize = which;
						subtitleText.setTextSize(TypedValue.COMPLEX_UNIT_SP,
								Float.valueOf(fontsizes[which].toString()));
					}
				}).setPositiveButton(getString(R.string.ok), null);
		builder.create().show();
	}

	@Override
	public void openSubtitleDialog() {
		if (subtitleType != null && subtitleType.length > 0) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setSingleChoiceItems(subtitleType, currentSelectSubtitle,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if ("None".equals(subtitleType[which].toString())) {
								currentSelectSubtitle = 0;
								subtitleText.setVisibility(View.INVISIBLE);
							} else {
								subtitleText.setVisibility(View.VISIBLE);

								if ("English".equals(subtitleType[which]
										.toString())) {
									currentSelectSubtitle = 2;
									currentSrt = englishSrt;
								} else if ("Vietnamese"
										.equals(subtitleType[which].toString())) {
									currentSelectSubtitle = 1;
									currentSrt = vietnameseSrt;
								} else {
									currentSelectSubtitle = 3;
								}
							}
						}
					}).setPositiveButton(getString(R.string.ok), null);
			builder.create().show();
		}
	}

	// End VideoMediaController.MediaPlayerControl

	public Fragment findFragmentByPosition(int position) {
		return getSupportFragmentManager().findFragmentByTag(
				"android:switcher:" + viewPager.getId() + ":"
						+ movieFragmentPagerAdapter.getItemId(position));
	}

	private void addWatched() {
		AddWatchedRequest request = new AddWatchedRequest();
		request.setMovieId(currentMovie.getId());
		request.setVideoId(currentVideo.getId());

		send(request, new SimpleResponseListener<Response>(this) {
			@Override
			public void onSuccess(final Response aResult) {
				Log.i(TAG, aResult.toString());
			}
		});
	}

	@Override
	public void onBackPressed() {
		isBackPress = true;
		super.onBackPressed();
	}

	@Override
	protected void onDestroy() {
		cleanUp();
		super.onDestroy();
	}
}
