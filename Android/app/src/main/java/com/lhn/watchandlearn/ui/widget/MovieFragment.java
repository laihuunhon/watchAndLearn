package com.lhn.watchandlearn.ui.widget;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.lhn.watchandlearn.R;
import com.lhn.watchandlearn.api.pojo.Movie;
import com.lhn.watchandlearn.api.request.movies.GetMovieListRequest;
import com.lhn.watchandlearn.api.response.movies.MoviesResponse;
import com.lhn.watchandlearn.ui.common.BaseActivity;
import com.lhn.watchandlearn.ui.common.SimpleResponseListener;

// In this case, the fragment displays simple text based on the page
public class MovieFragment extends Fragment {
	private final String TAG = MovieFragment.class.getName();

	public static final String ARG_PAGE = "ARG_PAGE";
	private int mPage;
	
	private SwipeRefreshLayout swipeLayout;

	private ArrayList<Movie> mMovies = new ArrayList<Movie>();
	private boolean mIsFetchDone = false;
	private boolean mIsFetching = false;
	private int mCurrentPage = -1;
	private MovieAdapter mAdapter;

	public static Fragment newInstance(int page) {
		Bundle args = new Bundle();
		args.putInt(ARG_PAGE, page);
		MovieFragment fragment = new MovieFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPage = getArguments().getInt(ARG_PAGE);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_page, container, false);
		ListView listView = (ListView) view.findViewById(R.id.listView);
		mAdapter = new MovieAdapter();
		listView.setAdapter(mAdapter);

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Movie selectedMovie = mMovies.get(position);
				
				getBaseActivity().checkUserEndDate(selectedMovie.getId());
			}
		});
		
		swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
		swipeLayout.setOnRefreshListener(new OnRefreshListener() {
			
			@Override
			public void onRefresh() {
				mIsFetchDone = false;
				mIsFetching = false;
				mCurrentPage = -1;
				fetchData(true);
			}
		});		

		fetchData(false);

		return view;
	}

	public class MovieAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return mMovies.size();
		}

		@Override
		public Movie getItem(final int i) {
			return mMovies.get(i);
		}

		@Override
		public long getItemId(final int i) {
			return i;
		}

		@Override
		public View getView(final int i, View aView, final ViewGroup aViewGroup) {
			ViewHolder holder;
			if (aView == null) {
				aView = getActivity().getLayoutInflater().inflate(R.layout.adapter_movie, null);
				holder = new ViewHolder();
				holder.thumbnailImage = (ImageView) aView.findViewById(R.id.ivImage);
				holder.titleText = (TextView) aView.findViewById(R.id.tvTitle);
				holder.descriptionText = (TextView) aView.findViewById(R.id.tvDescription);
				holder.watchedCount = (TextView) aView.findViewById(R.id.tvWatchedCount);
				holder.likedCount = (TextView) aView.findViewById(R.id.tvLikedCount);
				holder.commentedCount = (TextView) aView.findViewById(R.id.tvCommentedCount);

				aView.setTag(holder);
			} else {
				holder = (ViewHolder) aView.getTag();
			}

			Movie movie = getItem(i);
			getBaseActivity().loadImage(holder.thumbnailImage, movie.getThumbnail());
			holder.titleText.setText(movie.getTitle());
			holder.descriptionText.setText(movie.getDescription());
			holder.watchedCount.setText(String.valueOf(movie.getWatched()));
			holder.likedCount.setText(String.valueOf(movie.getLiked()));
			holder.commentedCount.setText(String.valueOf(movie.getCommented()));

			if (i == getCount() - 1 && !mIsFetchDone && !mIsFetching) {
				fetchData(false);
			}

			return aView;
		}
	}

	private static class ViewHolder {
		ImageView thumbnailImage;
		TextView titleText;
		TextView descriptionText;
		TextView watchedCount;
		TextView likedCount;
		TextView commentedCount;
	}

	private void fetchData(final boolean isClearMovieList) {
		if (!mIsFetchDone && !mIsFetching) {
			mIsFetching = true;
			mCurrentPage++;

			getBaseActivity().showLoadingDialog(getString(R.string.loading_waiting));
			GetMovieListRequest request = new GetMovieListRequest();
			request.setLimit(20);
			request.setOffset(mCurrentPage * 20);
			if (mPage == 1) {
				request.setMovieType("new");
			} else if (mPage == 2) {
				request.setMovieType("single");
			} else if (mPage == 3) {
				request.setMovieType("series");
			} else if (mPage == 4) {
				request.setMovieType("ted");
			}

			getBaseActivity().send(request, new SimpleResponseListener<MoviesResponse>(getBaseActivity()) {

				@Override
				public void onBeforeResult() {
					super.onBeforeResult();
					mIsFetching = false;
					swipeLayout.setRefreshing(false);
					getBaseActivity().dismissLoadingDialog();
				}

				@Override
				public void onSuccess(final MoviesResponse aResult) {
					Log.i(TAG, aResult.toString());
					if (isClearMovieList) {
						mMovies.clear();
					}
					mMovies.addAll(aResult.getItems());
					if (mMovies.size() >= aResult.getTotal()) {
						mIsFetchDone = true;
					}

					mAdapter.notifyDataSetChanged();
				}
			});
		}
	}

	private BaseActivity getBaseActivity() {
		return ((BaseActivity) getActivity());
	}
}