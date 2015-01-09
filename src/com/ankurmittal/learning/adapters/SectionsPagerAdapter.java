package com.ankurmittal.learning.adapters;

import java.util.Locale;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v13.app.FragmentPagerAdapter;

import com.ankurmittal.learning.FriendsFragment;
import com.ankurmittal.learning.R;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {
	
	Context mContext;

	public SectionsPagerAdapter(Context context,FragmentManager fm) {
		super(fm);
		mContext = context;
	}

	@Override
	public Fragment getItem(int position) {
		// getItem is called to instantiate the fragment for the given page.
		// Return a PlaceholderFragment (defined as a static inner class
		// below).
		switch (position) {
		
		case 0 :
			return new FriendsFragment();
		}
		return null;
	}

	@Override
	public int getCount() {
		return 1;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		Locale l = Locale.getDefault();
		switch (position) {
		
		case 0:
			return mContext.getString(R.string.title_friends_fragment);
		
		}
		return null;
	}
}