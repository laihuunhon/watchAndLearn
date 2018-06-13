package com.lhn.watchandlearn.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lhn.watchandlearn.R;
import com.lhn.watchandlearn.ui.widget.VideoControllerView;
import com.lhn.watchandlearn.utils.Caption;
import com.lhn.watchandlearn.utils.FormatSRT;
import com.lhn.watchandlearn.utils.TimedTextObject;

public class VideoPlayerActivity extends Activity implements
		SurfaceHolder.Callback, MediaPlayer.OnPreparedListener,
		VideoControllerView.MediaPlayerControl {

	private static final String TAG = "PlayerActivity";
	private SubtitleProcessingTask subsFetchTask;
	FrameLayout videoSurfaceContainer;
	SurfaceView videoSurface;
	MediaPlayer player;
	VideoControllerView controller;
	Boolean isFullscreen = false;
	TextView textView;
	private TextView subtitleText;
	private Boolean isPause = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_video_player);

		videoSurface = (SurfaceView) findViewById(R.id.videoSurface);
		SurfaceHolder videoHolder = videoSurface.getHolder();
		videoHolder.addCallback(this);

		videoSurfaceContainer = (FrameLayout) findViewById(R.id.videoSurfaceContainer);

		controller = new VideoControllerView(this);
		subtitleText = (TextView) findViewById(R.id.offLine_subtitleText);

//		textView = (TextView) findViewById(R.id.textView1);

		try {
			player = new MediaPlayer();
			player.setAudioStreamType(AudioManager.STREAM_MUSIC);
			player.reset();
			player.setDataSource(
					getApplicationContext(),
					Uri.parse("http://media.studyphim.vn/VIPN6/Phimbo/Prison%20Break%201/1.mp4"));
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
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
				subsFetchTask = new SubtitleProcessingTask();
				subsFetchTask.execute();

				controller.setMediaPlayer(VideoPlayerActivity.this);
				controller
						.setAnchorView((FrameLayout) findViewById(R.id.videoSurfaceContainer));

				player.start();
				// mHandler.sendEmptyMessage(SHOW_PROGRESS);
			}
		});
		player.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				// finish();
			}
		});

		// try {
		// player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		// // player.setDataSource(this,
		// Uri.parse("http://media.studyphim.vn/VIPN6/Phimbo/Prison%20Break%201/1.mp4"));
		// player.setDataSource(
		// getApplicationContext(),
		// Uri.parse("android.resource://" + getPackageName() + "/"
		// + R.raw.jellies));
		// player.setOnPreparedListener(this);
		// } catch (IllegalArgumentException e) {
		// e.printStackTrace();
		// } catch (SecurityException e) {
		// e.printStackTrace();
		// } catch (IllegalStateException e) {
		// e.printStackTrace();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
	}

	@Override
	protected void onPause() {
		super.onPause();

		System.out.println("+++ PAUSE +++");
		if (player != null && player.isPlaying()) {
			player.pause();
			isPause = true;
		} else {
			return;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		System.out.println("+++ RESUME +++");
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		controller.show();
		return false;
	}

	// Implement SurfaceHolder.Callback
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		player.setDisplay(holder);

		if (!isPause) {
			player.prepareAsync();
		} else {
			player.start();
			isPause = false;
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {

	}

	// End SurfaceHolder.Callback

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
		return player.getCurrentPosition();
	}

	@Override
	public int getDuration() {
		return player.getDuration();
	}

	@Override
	public boolean isPlaying() {
		return player.isPlaying();
	}

	@Override
	public void pause() {
		player.pause();
	}

	@Override
	public void seekTo(int i) {
		player.seekTo(i);
	}

	@Override
	public void start() {
		player.start();
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

			isFullscreen = false;
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			DisplayMetrics displaymetrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
			android.widget.LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) videoSurfaceContainer
					.getLayoutParams();
			params.height = displaymetrics.heightPixels;

			isFullscreen = true;
		}

		// DisplayMetrics displaymetrics = new DisplayMetrics();
		// getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		// int height = displaymetrics.heightPixels;
		// int width = displaymetrics.widthPixels;
		//
		// android.widget.FrameLayout.LayoutParams params =
		// (android.widget.FrameLayout.LayoutParams)
		// videoSurface.getLayoutParams();
		// params.width = width;
		// params.height = height-80;// -80 for android controls
		// params.setMargins(0, 0, 0, 50);
	}

	// End VideoMediaController.MediaPlayerControl

	@Override
	public void finish() {
		super.finish();
	}

	private void cleanUp() {
		if (subtitleDisplayHandler != null) {
			subtitleDisplayHandler.removeCallbacks(subtitleProcessesor);
		}
		if (player != null) {
			player.stop();
		}
	}

	public TimedTextObject srt;
	private Runnable subtitleProcessesor = new Runnable() {

		@Override
		public void run() {
			if (player != null && player.isPlaying()) {
				int currentPos = player.getCurrentPosition();
				Collection<Caption> subtitles = srt.captions.values();
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
			subtitleDisplayHandler.postDelayed(this, 100);
		}
	};
	private Handler subtitleDisplayHandler = new Handler();
	private boolean mDragging;

	public class SubtitleProcessingTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			subtitleText.setText("Loading subtitles..");
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			int count;
			try {
				/*
				 * if you want to download file from Internet, use commented
				 * code.
				 */
				URL url = new URL(
						"http://113.161.65.212:5555/resources/55cb79a93963a5e01fbc2d50/1/en.vtt");
				InputStream is = url.openStream();
				File f = getExternalFile();
				FileOutputStream fos = new FileOutputStream(f);
				byte data[] = new byte[1024];
				while ((count = is.read(data)) != -1) {
					fos.write(data, 0, count);
				}
				is.close();
				fos.close();				
				InputStream stream = new FileInputStream(getApplicationContext().getExternalFilesDir(null).getPath() + "/sample.srt");
				
//				InputStream stream = getResources().openRawResource(R.raw.en1);
				FormatSRT formatSRT = new FormatSRT();
				srt = formatSRT.parseFile("sample.srt", stream);
			} catch (Exception e) {
				e.printStackTrace();
				Log.e(TAG, "error in downloading subs");
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (null != srt) {
				subtitleText.setText("");
				Toast.makeText(getApplicationContext(), "subtitles loaded!!",
						Toast.LENGTH_SHORT).show();
				subtitleDisplayHandler.post(subtitleProcessesor);
			}
			super.onPostExecute(result);
		}
	}

	public void onTimedText(Caption text) {
		if (text == null) {
			subtitleText.setVisibility(View.INVISIBLE);
			return;
		}
		subtitleText.setText(Html.fromHtml(text.content));
		subtitleText.setVisibility(View.VISIBLE);
	}

	public File getExternalFile() {
		File srt = null;
		try {
			srt = new File(getApplicationContext().getExternalFilesDir(null)
					.getPath() + "/sample.srt");
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

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void openFontSizeDialog() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void openSubtitleDialog() {
		// TODO Auto-generated method stub
		
	}

	// @Override
	// public void onProgressChanged(SeekBar seekBar, int progress,
	// boolean fromUser) {
	// if (player == null) {
	// return;
	// }
	//
	// if (!fromUser) {
	// // We're not interested in programmatically generated changes to
	// // the progress bar's position.
	// return;
	// }
	//
	// long duration = player.getDuration();
	// if (duration == -1)
	// return;
	// long newposition = (duration * progress) / 1000L;
	// player.seekTo((int) newposition);
	// }
	//
	// private int setProgress() {
	// if (player == null || mDragging) {
	// return 0;
	// }
	//
	// int position = player.getCurrentPosition();
	// if (position == -1)
	// return 0;
	// int duration = player.getDuration();
	// if (mSeeker != null && duration > 0) {
	// long pos = 1000L * position / duration;
	// mSeeker.setProgress((int) pos);
	// }
	//
	// return position;
	// }
	//
	// @Override
	// public void onStartTrackingTouch(SeekBar seekBar) {
	// mDragging = true;
	// }
	//
	// @Override
	// public void onStopTrackingTouch(SeekBar seekBar) {
	// mDragging = false;
	// setProgress();
	// }
	//
	// private class MessageHandler extends Handler {
	//
	// @Override
	// public void handleMessage(Message msg) {
	// if (player == null) {
	// return;
	// }
	//
	// int pos;
	// switch (msg.what) {
	// case FADE_OUT:
	// break;
	// case SHOW_PROGRESS:
	// try {
	// pos = setProgress();
	// } catch (IllegalStateException ise) {
	// ise.printStackTrace();
	// break;
	// }
	// if (!mDragging && player.isPlaying()) {
	// msg = obtainMessage(SHOW_PROGRESS);
	// sendMessageDelayed(msg, 1000 - (pos % 1000));
	// }
	// break;
	// }
	// }
	// }
}
