package com.lhn.watchandlearn.ui.home;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.lhn.watchandlearn.R;
import com.lhn.watchandlearn.WatchAndLearnApp;
import com.lhn.watchandlearn.WatchAndLearnConfig;
import com.lhn.watchandlearn.api.ServerException;
import com.lhn.watchandlearn.api.pojo.Movie;
import com.lhn.watchandlearn.api.request.auth.LogoutRequest;
import com.lhn.watchandlearn.api.request.movies.GetMovieListRequest;
import com.lhn.watchandlearn.api.request.movies.SearchMovieListRequest;
import com.lhn.watchandlearn.api.response.base.Response;
import com.lhn.watchandlearn.api.response.movies.MoviesResponse;
import com.lhn.watchandlearn.ui.common.AppPreferences;
import com.lhn.watchandlearn.ui.common.BaseActivity;
import com.lhn.watchandlearn.ui.common.SimpleResponseListener;
import com.lhn.watchandlearn.ui.utils.EditTextUtils;
import com.lhn.watchandlearn.ui.widget.HomeFragmentPagerAdapter;

public class HomeActivity extends BaseActivity {
	private final String TAG = HomeActivity.class.getName();

	private ListView searchListView;

	private ArrayList<Movie> mMovies = new ArrayList<Movie>();
	private MovieSearchAdapter mAdapter;
	private Boolean isSearchingTimeout = false;
	private String currentSearchString = "";
	private SearchView searchView;
	
	private boolean mIsFetchDone = false;
	private boolean mIsFetching = false;
	private int mCurrentPage = -1;
	
	private MenuItem searchMenuItem;
	private AdView mAdView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		if (!WatchAndLearnConfig.isDebugMode() &&  WatchAndLearnApp.getCurrentUser().getHasAds()) {
			mAdView = (AdView) findViewById(R.id.adView);
			mAdView.setVisibility(View.VISIBLE);
			AdRequest adRequest = new AdRequest.Builder().build();
			mAdView.loadAd(adRequest);
		}

		getSupportActionBar().show();

		searchListView = (ListView) findViewById(R.id.listView);

		mAdapter = new MovieSearchAdapter();
		searchListView.setAdapter(mAdapter);

		searchListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {				
				Movie selectedMovie = mMovies.get(position);
				searchView.setQuery("", false);
				searchView.setIconified(true);
				resetView();
				checkUserEndDate(selectedMovie.getId());
			}
		});

		ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
		viewPager.setAdapter(new HomeFragmentPagerAdapter(getSupportFragmentManager(), HomeActivity.this));

		TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
		tabLayout.setupWithViewPager(viewPager);
	}

	private void resetView() {
		searchListView.setVisibility(View.GONE);
		mMovies.clear();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		
		final Handler handler = new Handler();
		final Runnable requestSearch = new Runnable() {

			@Override
			public void run() {
				searchMovie();
			}
		};
		
		searchMenuItem = menu.findItem(R.id.action_search);

		searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String s) {
				return false;
			}

			@Override
			public boolean onQueryTextChange(String s) {
				mIsFetchDone = false;
				mIsFetching = false;
				mCurrentPage = -1;
				mMovies.clear();
				if (isSearchingTimeout) {
					handler.removeCallbacks(requestSearch);
				}
				if (s.isEmpty()) {					
					searchListView.setVisibility(View.GONE);
				} else {					
					currentSearchString = s;
					isSearchingTimeout = true;
					handler.postDelayed(requestSearch, 500);
				}
				return false;
			}
		});

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_logout:
			doLogout();
			return true;
		case R.id.action_profile:
			startActivity(new Intent(this, ProfileActivity.class));

			return true;
		case R.id.action_support:
			startActivity(new Intent(this, SupportActivity.class));

			return true;
		case R.id.action_extend_date:
			startActivity(new Intent(this, ExtendDateActivity.class));

			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onBackPressed() {
		new AlertDialog.Builder(this).setTitle(getString(R.string.exitApp)).setMessage(getString(R.string.exitAppTitle)).setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finishAffinity();
			}

		}).setNegativeButton(getString(R.string.no), null).show();
	}

	private void doLogout() {
		showLoadingDialog(getString(R.string.loading_waiting));
		LogoutRequest request = new LogoutRequest();
		send(request, new SimpleResponseListener<Response>(this) {
			@Override
			public void onSuccess(Response aResult) {
				dismissLoadingDialog();
				AppPreferences.clear(HomeActivity.this);
				startActivity(new Intent(HomeActivity.this, LoginActivity.class));
				finish();
			}

			@Override
			public void onServerError(ServerException e) {
				dismissLoadingDialog();
				showErrorToast(e.getMessage());
			}
		});
	}

	public class MovieSearchAdapter extends BaseAdapter {

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
				aView = getLayoutInflater().inflate(R.layout.adapter_search_movie, null);
				holder = new ViewHolder();
				holder.titleText = (TextView) aView.findViewById(R.id.tvText);

				aView.setTag(holder);
			} else {
				holder = (ViewHolder) aView.getTag();
			}
			
			Movie movie = getItem(i);
			String title = movie.getTitle();
			Matcher match = Pattern.compile("(?i)" + currentSearchString).matcher(title);
			if (match.find()) {
				title = title.replaceAll(match.group(), "<b>" + match.group() + "</b>");
			}		
			
			holder.titleText.setText(Html.fromHtml(title));	
			
			if (i == getCount() - 1 && !mIsFetchDone && !mIsFetching) {
				searchMovie();
			}

			return aView;
		}
	}

	private static class ViewHolder {
		TextView titleText;
	}

	private void searchMovie() {
		if (!mIsFetchDone && !mIsFetching) {
			mIsFetching = true;
			mCurrentPage++;

			showLoadingDialog(getString(R.string.loading_waiting));
			SearchMovieListRequest request = new SearchMovieListRequest();
			request.setLimit(20);
			request.setOffset(mCurrentPage * 20);
			request.setSearchText(currentSearchString);

			send(request, new SimpleResponseListener<MoviesResponse>(this) {

				@Override
				public void onBeforeResult() {
					super.onBeforeResult();
					mIsFetching = false;
					dismissLoadingDialog();
				}

				@Override
				public void onSuccess(final MoviesResponse aResult) {
					Log.i(TAG, aResult.toString());
					mMovies.addAll(aResult.getItems());
					if (mMovies.size() >= aResult.getTotal()) {
						mIsFetchDone = true;
					}
					if (mMovies.size() > 0) {
						searchListView.setVisibility(View.VISIBLE);
					} else {
						searchListView.setVisibility(View.GONE);
					}

					mAdapter.notifyDataSetChanged();
				}
			});
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mAdView != null) {
			mAdView.pause();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mAdView != null) {
			mAdView.resume();
		}
	}
}
