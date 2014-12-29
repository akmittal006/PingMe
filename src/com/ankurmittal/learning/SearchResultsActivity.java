package com.ankurmittal.learning;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Toast;

import com.ankurmittal.learning.adapters.ParseConstants;
import com.ankurmittal.learning.adapters.UserAdapter;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class SearchResultsActivity extends ListActivity {

	public static final String TAG = SearchResultsActivity.class
			.getSimpleName();
	ArrayList<ParseUser> mUsers;
	String[] mUsernames;
	SearchView searchView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		// Get the SearchView and set the search able configuration

		// Do not iconify the widget;expand it by default

		handleIntent(getIntent());
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		
		super.onListItemClick(l, v, position, id);
		String friendName = mUsernames[position];
		final ParseUser friend = mUsers.get(position);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.add_friend_dialog_title);
		builder.setMessage("Send friend request to "+ friendName + " ?");
		builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				ParseObject friendRequest = new ParseObject(ParseConstants.KEY_FRIENDS_REQUEST);
				friendRequest.put(ParseConstants.KEY_FRND_REQ_RECEIVER, friend.getObjectId());
				friendRequest.put(ParseConstants.KEY_SENDER_NAME, ParseUser.getCurrentUser().getUsername());
				friendRequest.saveInBackground(new SaveCallback() {
					
					@Override
					public void done(ParseException e) {
						if (e == null) {
							//friend request send
							Toast.makeText(SearchResultsActivity.this, R.string.friend_request_sent_label, Toast.LENGTH_SHORT).show();
						} else {
							// there was error
							Toast.makeText(SearchResultsActivity.this, R.string.friend_request_error_label, Toast.LENGTH_SHORT).show();
						}
						
					}
				});
			}
		});
		builder.setNegativeButton(android.R.string.cancel, null);
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.search_results, menu);
		MenuItem searchItem = menu.findItem(R.id.search);
		SearchView searchView = (SearchView) MenuItemCompat
				.getActionView(searchItem);
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		searchView.setSearchableInfo(searchManager
				.getSearchableInfo(getComponentName()));
		searchView.setIconified(false);
		String query = getIntent().getStringExtra(SearchManager.QUERY);
		searchView.setQuery(query, false);
		searchView.setOnQueryTextListener(new OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String queryText) {
				setProgressBarIndeterminateVisibility(true);
				handleQueryText(queryText);
				return false;
			}

			@Override
			public boolean onQueryTextChange(String queryText) {
				setProgressBarIndeterminateVisibility(true);
				handleQueryText(queryText);
				return false;
			}
		});
		return true;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		handleIntent(intent);
	}
	
	
///////////////HANDLE INTENT/////////////
	private void handleIntent(Intent intent) {

		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {

			String queryText = intent.getStringExtra(SearchManager.QUERY);
			handleQueryText(queryText);

			// showResults(query);
		} 
		else {
			
		}
	}

	
////////////////HANDLE TEXT QUERY//////////////////////////
	public void handleQueryText(String queryText) {
		ParseQuery<ParseUser> users = ParseUser.getQuery();

		users.whereStartsWith("username", queryText);

		users.findInBackground(new FindCallback<ParseUser>() {

			@Override
			public void done(List<ParseUser> users, ParseException e) {
				setProgressBarIndeterminateVisibility(false);
				if (e == null) {
					// success, display users
					Log.d(TAG, "users :" + users.size());
					mUsers = new ArrayList<ParseUser>(users);
					mUsernames = new String[mUsers.size()];
					for (int i = 0; i < users.size(); i++) {
						mUsernames[i] = (mUsers.get(i).getUsername());
					}
					showQueryResults();
				} else {
					// there was an error
					Log.e(TAG, e.getMessage(), e);
				}
			}

///////////////DISPLAY USERS///////////////////
			private void showQueryResults()  {
				UserAdapter adapter = new UserAdapter(SearchResultsActivity.this, mUsers);
				getListView().setAdapter(adapter);
			}
		});
	}
}
