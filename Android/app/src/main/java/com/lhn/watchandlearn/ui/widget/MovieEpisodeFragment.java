package com.lhn.watchandlearn.ui.widget;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lhn.watchandlearn.R;
import com.lhn.watchandlearn.api.pojo.Movie;
import com.lhn.watchandlearn.api.pojo.Video;
import com.lhn.watchandlearn.ui.home.MovieActivity;
import com.lhn.watchandlearn.ui.widget.MovieFragment.MovieAdapter;

// In this case, the fragment displays simple text based on the page
public class MovieEpisodeFragment extends Fragment {
	private final String TAG = MovieEpisodeFragment.class.getName();
	
	private static ArrayList<Video> mVideos = new ArrayList<Video>();
	private static Movie currentMovie;
	private static int currentVideoIndex;
	private static Video currentVideo;
	
	private EposideAdapter mAdapter;

	public static Fragment newInstance(Movie movie, int videoIndex) {
		currentMovie = movie;
		currentVideoIndex = videoIndex;
		mVideos.clear();
		for (int i=0; i<movie.getItems().size(); i++) {
			mVideos.add(movie.getItems().get(i));
		}
		currentVideo = mVideos.get(currentVideoIndex-1);
		mVideos.remove(currentVideoIndex-1);
		
		MovieEpisodeFragment fragment = new MovieEpisodeFragment();
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_movie_episode, container, false);
		
		ListView listView = (ListView) view.findViewById(R.id.listView);
		mAdapter = new EposideAdapter();
		listView.setAdapter(mAdapter);
		
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Video episode = mVideos.get(position);
				
				Intent intent = new Intent(getActivity(), MovieActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.putExtra("movieId", currentMovie.getId());
				intent.putExtra("videoIndex", Integer.valueOf(episode.getIndex()));
				startActivity(intent);
			}
		});

		return view;
	}
	
	public class EposideAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return mVideos.size();
		}

		@Override
		public Video getItem(final int i) {
			return mVideos.get(i);
		}

		@Override
		public long getItemId(final int i) {
			return i;
		}

		@Override
		public View getView(final int i, View aView, final ViewGroup aViewGroup) {
			ViewHolder holder;
			if (aView == null) {
				aView = getActivity().getLayoutInflater().inflate(
						R.layout.adapter_episode, null);
				holder = new ViewHolder();
				holder.titleText = (TextView) aView.findViewById(R.id.tvTitle);
				holder.episodeText = (TextView) aView
						.findViewById(R.id.tvEpisode2);
				holder.watchedCount = (TextView) aView
						.findViewById(R.id.tvWatchedCount);
				holder.likedCount = (TextView) aView
						.findViewById(R.id.tvLikedCount);
				holder.commentedCount = (TextView) aView
						.findViewById(R.id.tvCommentedCount);

				aView.setTag(holder);
			} else {
				holder = (ViewHolder) aView.getTag();
			}
			
			Video video = getItem(i);
			holder.titleText.setText(currentMovie.getTitle());
			holder.episodeText.setText("Episode " + video.getIndex());
			holder.watchedCount.setText(String.valueOf(video.getWatched()));
			holder.likedCount.setText(String.valueOf(video.getLiked()));
			holder.commentedCount.setText(String.valueOf(video.getCommented()));

			return aView;		
		}
	}

	private static class ViewHolder {
		TextView titleText;
		TextView episodeText;
		TextView watchedCount;
		TextView likedCount;
		TextView commentedCount;
	}
}