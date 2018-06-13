package com.lhn.watchandlearn.ui.widget;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.lhn.watchandlearn.R;

public class HomeFragmentPagerAdapter extends FragmentPagerAdapter {
	final int PAGE_COUNT = 4;
	private Context context;

	public HomeFragmentPagerAdapter(FragmentManager fm, Context context) {
		super(fm);
		this.context = context;
	}

	@Override
	public int getCount() {
		return PAGE_COUNT;
	}

	@Override
	public Fragment getItem(int position) {
		return MovieFragment.newInstance(position + 1);
	}

	@Override
	public CharSequence getPageTitle(int position) {
		switch (position) {
		case 0:
			return context.getString(R.string.tab_movie_new);
		case 1:
			return context.getString(R.string.tab_movie_single);
		case 2:
			return context.getString(R.string.tab_movie_series);
		case 3:
			return context.getString(R.string.tab_movie_ted);
		}
		return null;
	}
}