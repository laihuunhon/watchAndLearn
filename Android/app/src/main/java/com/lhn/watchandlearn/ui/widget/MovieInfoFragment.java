package com.lhn.watchandlearn.ui.widget;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lhn.watchandlearn.R;
import com.lhn.watchandlearn.api.pojo.Movie;
import com.lhn.watchandlearn.api.pojo.Video;
import com.lhn.watchandlearn.api.request.likes.AddLikeRequest;
import com.lhn.watchandlearn.api.response.base.Response;
import com.lhn.watchandlearn.ui.common.BaseActivity;
import com.lhn.watchandlearn.ui.common.SimpleResponseListener;

public class MovieInfoFragment extends Fragment {
	private final String TAG = MovieInfoFragment.class.getName();

	private TextView tvTitle;
	private TextView tvLiked;
	private TextView tvEpisode;
	private TextView tvDescription;
	private static Movie currentMovie;
	private static int currentVideoIndex;
	private static ArrayList<Video> mVideos = new ArrayList<Video>();
	private static Video currentVideo;

	public static Fragment newInstance(Movie movie, int videoIndex) {
		currentMovie = movie;
		currentVideoIndex = videoIndex;
		mVideos = movie.getItems();
		currentVideo = mVideos.get(currentVideoIndex - 1);
		
		MovieInfoFragment fragment = new MovieInfoFragment();
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_movie_info, container,
				false);

		tvTitle = (TextView) view.findViewById(R.id.tvTitle);
		tvLiked = (TextView) view.findViewById(R.id.tvLiked);
		tvDescription = (TextView) view.findViewById(R.id.tvDescription);
		tvEpisode = (TextView) view.findViewById(R.id.tvEpisode);
		
		tvTitle.setText(currentMovie.getTitle());
		tvLiked.setText(currentVideo.getLiked() + " Liked");
		tvDescription.setText(currentMovie.getDescription());
		if (currentMovie.getItems().size() > 1) {
			tvEpisode.setVisibility(View.VISIBLE);
			tvEpisode.setText("Episode " + String.valueOf(currentVideoIndex));
		} else {
			tvEpisode.setVisibility(View.GONE);
		}
		
		if (currentVideo.getIsLiked() != null && currentVideo.getIsLiked()) {
			tvLiked.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_green_36dp, 0, 0, 0);
			tvLiked.setClickable(false);
		} else {
			tvLiked.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					addLike();
				}
			});
		}	

		return view;
	}
	
	private void addLike() {
		getBaseActivity().showLoadingDialog(getString(R.string.loading_waiting));
		AddLikeRequest request = new AddLikeRequest();
		request.setMovieId(currentMovie.getId());
		request.setVideoId(currentVideo.getId());

		getBaseActivity().send(request, new SimpleResponseListener<Response>(getBaseActivity()) {
			@Override
			public void onBeforeResult() {
				super.onBeforeResult();
				getBaseActivity().dismissLoadingDialog();
			}

			@Override
			public void onSuccess(final Response aResult) {
				Log.i(TAG, aResult.toString());
				
				tvLiked.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_green_36dp, 0, 0, 0);
				tvLiked.setClickable(false);
				tvLiked.setText(String.valueOf(currentVideo.getLiked() + 1) + " Liked");
			}
		});
	}
	
	private BaseActivity getBaseActivity() {
		return ((BaseActivity) getActivity());
	}
}