package com.ankurmittal.learning;

import java.util.ArrayList;
import java.util.List;

import com.ankurmittal.learning.adapters.ParseConstants;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * An activity representing a list of Chats. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link ChatDetailActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link ChatListFragment} and the item details (if present) is a
 * {@link ChatDetailFragment}.
 * <p>
 * This activity also implements the required {@link ChatListFragment.Callbacks}
 * interface to listen for item selections.
 */
public class ChatListActivity extends Activity implements
		ChatListFragment.Callbacks  {

	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
	 * device.
	 */
	private boolean mTwoPane;
	private ArrayList<ParseObject> friendRequests;
	private ParseUser currentUser ;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat_list);
		friendRequests = new ArrayList<ParseObject>();
		if (findViewById(R.id.chat_detail_container) != null) {
			// The detail container view will be present only in the
			// large-screen layouts (res/values-large and
			// res/values-sw600dp). If this view is present, then the
			// activity should be in two-pane mode.
			mTwoPane = true;

			// In two-pane mode, list items should be given the
			// 'activated' state when touched.
			((ChatListFragment) getFragmentManager().findFragmentById(
					R.id.chat_list)).setActivateOnItemClick(true);
		}
		
		

		// TODO: If exposing deep links into your app, handle intents here.
	}

	/**
	 * Callback method from {@link ChatListFragment.Callbacks} indicating that
	 * the item with the given ID was selected.
	 */
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
	@Override
	protected void onResume() {

		super.onResume();
		currentUser = ParseUser.getCurrentUser();
		if (currentUser != null) {
			// do stuff with the user
			loadFriendRequests();
		} else {
			// show the signup or login screen
			Intent intent = new Intent(ChatListActivity.this, LoginActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_menu, menu);

		int count = getFriendRequestCount();
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

					Intent intent = new Intent(ChatListActivity.this,
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
					ChatListActivity.this.invalidateOptionsMenu();
				} else {
					Toast.makeText(ChatListActivity.this, "Network not available!",
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
			Intent intent = new Intent(ChatListActivity.this, LoginActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			ParseUser.logOut();
			
			return true;

		} else if (id == R.id.friend_req) {
			Toast.makeText(ChatListActivity.this, R.string.no_frnd_reqs_label,
					Toast.LENGTH_SHORT).show();
		} else if(id == R.id.friends) {
			Intent intent = new Intent(ChatListActivity.this, MainActivity.class);
			startActivity(intent);
		}

		return super.onOptionsItemSelected(item);
	}

}
