package com.lhn.watchandlearn.ui.widget;

import com.lhn.watchandlearn.R;
import com.lhn.watchandlearn.api.pojo.Movie;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class MovieFragmentPagerAdapter extends FragmentPagerAdapter {
	private String tabTitles[] = new String[] { "INFORMATION", "COMMENT" };
	int PAGE_COUNT = tabTitles.length;
	private Context context;
	private Movie currentMovie;
	private int currentVideoIndex;

	public MovieFragmentPagerAdapter(FragmentManager fm, Context context, Movie movie, int index) {
		super(fm);

		currentMovie = movie;
		currentVideoIndex = index;

		if (movie.getItems().size() > 1) {
			tabTitles = new String[] { context.getString(R.string.tab_info), context.getString(R.string.tab_episode), context.getString(R.string.tab_comment) };
		} else {
			tabTitles = new String[] { context.getString(R.string.tab_info), context.getString(R.string.tab_comment) };
		}
		PAGE_COUNT = tabTitles.length;

		this.context = context;
	}

	@Override
	public int getCount() {
		return PAGE_COUNT;
	}

	@Override
	public Fragment getItem(int position) {
		if (position == 0) {
			return MovieInfoFragment.newInstance(currentMovie, currentVideoIndex);
		} else {
			if (PAGE_COUNT == 2) {
				return MovieCommentFragment.newInstance(currentMovie, currentVideoIndex);
			} else {
				if (position == 1) {
					return MovieEpisodeFragment.newInstance(currentMovie, currentVideoIndex);
				} else {
					return MovieCommentFragment.newInstance(currentMovie, currentVideoIndex);
				}
			}
		}
	}

	@Override
	public CharSequence getPageTitle(int position) {
		// Generate title based on item position
		return tabTitles[position];
	}
}