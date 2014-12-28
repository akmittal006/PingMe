package com.ankurmittal.learning;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

import com.ankurmittal.learning.adapters.UserAdapter;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class SearchResultsActivity extends ListActivity {

	public static final String TAG = SearchResultsActivity.class
			.getSimpleName();
	ArrayList<ParseUser> mUsers;
	String[] mUsernmaes;
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
					mUsernmaes = new String[mUsers.size()];
					for (int i = 0; i < users.size(); i++) {
						mUsernmaes[i] = (mUsers.get(i).getUsername());
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
