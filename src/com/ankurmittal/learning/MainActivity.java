package com.ankurmittal.learning;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.ankurmittal.learning.adapters.ParseConstants;
import com.ankurmittal.learning.adapters.SectionsPagerAdapter;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class MainActivity extends Activity implements FriendsFragment.Callbacks {

	ParseUser currentUser = null;
	ArrayList<ParseObject> friendRequests;
	String[] frndReqSenders;
	int count;
	private boolean mTwoPane = false;

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v13.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// getActionBar().hide();
		friendRequests = new ArrayList<ParseObject>();
		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		FriendsFragment fragment = new FriendsFragment();
		// fragment.setArguments(arguments);
		getFragmentManager().beginTransaction()
				.add(R.id.friends_container, fragment).commit();
	
	}

	@Override
	protected void onResume() {

		super.onResume();
		currentUser = ParseUser.getCurrentUser();
		if (currentUser != null) {
			// do stuff with the user
			loadFriendRequests();
		} else {
			// show the signup or login screen
			Intent intent = new Intent(MainActivity.this, LoginActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.options_menu, menu);

		count = getFriendRequestCount();
		if (count > 0) {
			MenuItem item = menu.findItem(R.id.friend_req);
			MenuItemCompat
					.setActionView(item, R.layout.action_bar_notification);
			View view = MenuItemCompat.getActionView(item);
			TextView notificationTextView = (TextView) view
					.findViewById(R.id.actionbar_notifcation_textview);
			notificationTextView.setText(count + "");
			ImageButton newItem = (ImageButton) view
					.findViewById(R.id.frnd_req_btn);
			newItem.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {

					Intent intent = new Intent(MainActivity.this,
							ShowFrndReqsActivity.class);
					startActivity(intent);
				}
			});

		}

		// Get the SearchView and set the searchable configuration

		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		final SearchView searchView = (SearchView) menu.findItem(R.id.search)
				.getActionView();
		searchView.setSearchableInfo(searchManager
				.getSearchableInfo(getComponentName()));

		// Do not iconify the widget;expand it by default
		searchView.setIconifiedByDefault(true);
		searchView.setIconified(true);
		return true;
	}

	private void loadFriendRequests() {
		ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(
				ParseConstants.KEY_FRIENDS_REQUEST);
		query.whereEqualTo(ParseConstants.KEY_FRND_REQ_RECEIVER, ParseUser
				.getCurrentUser().getObjectId());
		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> requests, ParseException e) {

				if (e == null) {
					// We found requests!
					// frndReqSenders = new String[requests.size()];
					friendRequests = new ArrayList<ParseObject>(requests);
					// getActionBar().show();
					MainActivity.this.invalidateOptionsMenu();
				} else {
					Toast.makeText(MainActivity.this, "Network not available!",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	private int getFriendRequestCount() {

		return friendRequests.size();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.sign_out) {
			Intent intent = new Intent(MainActivity.this, LoginActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			ParseUser.logOut();
			currentUser = ParseUser.getCurrentUser();
			return true;

		} else if (id == R.id.friend_req) {
			Toast.makeText(MainActivity.this, R.string.no_frnd_reqs_label,
					Toast.LENGTH_SHORT).show();
		}

		return super.onOptionsItemSelected(item);
	}


	@Override
	public void onItemSelected(String id) {
		if (mTwoPane) {
			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.
			Bundle arguments = new Bundle();
			arguments.putString(ChatDetailFragment.ARG_ITEM_ID, id);
			ChatDetailFragment fragment = new ChatDetailFragment();
			fragment.setArguments(arguments);
			getFragmentManager().beginTransaction()
					.replace(R.id.chat_detail_container, fragment).commit();

		} else {
			// In single-pane mode, simply start the detail activity
			// for the selected item ID.
			Intent detailIntent = new Intent(this, ChatDetailActivity.class);
			detailIntent.putExtra(ChatDetailFragment.ARG_ITEM_ID, id);
			startActivity(detailIntent);
		}

	}

}
