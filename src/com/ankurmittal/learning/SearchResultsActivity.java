package com.ankurmittal.learning;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Toast;

import com.ankurmittal.learning.adapters.UserAdapter;
import com.ankurmittal.learning.util.ParseConstants;
import com.ankurmittal.learning.util.TypefaceSpan;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseRelation;
import com.parse.ParseUser;

public class SearchResultsActivity extends ListActivity {

	public static final String TAG = SearchResultsActivity.class
			.getSimpleName();
	ArrayList<HashMap<String, Object>> mUsers;
	ArrayList<String> mRaw;
	String[] mUsernames;
	SearchView searchView;
	ProgressBar mProgBar;
	ArrayList<Integer> isFrndReqSent;
	boolean result;
	protected ParseRelation<ParseUser> friendsRel;
	UserAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_results);
		mProgBar = (ProgressBar) findViewById(R.id.searchProgressBar);
		ParseUser currentUser = ParseUser.getCurrentUser();
		friendsRel = currentUser
				.getRelation(ParseConstants.KEY_FRIENDS_RELATION);

		// handleIntent(getIntent());
		//getActionBar().setDisplayShowHomeEnabled(false);
		SpannableString s = new SpannableString("Search People");
		s.setSpan(new TypefaceSpan(this, "LOBSTERTWO-BOLD.OTF"), 0, s.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		// Update the action bar title with the TypefaceSpan instance
//		ActionBar actionBar = getActionBar();
//		actionBar.setTitle(s);
		mUsers = new ArrayList<HashMap<String, Object>>();
		adapter  = new UserAdapter(this, mUsers);
		
	}

	@Override
	protected void onListItemClick(ListView l, View v, final int position,
			long id) {
		if((Boolean)mUsers.get(position).get(ParseConstants.KEY_IS_FRIENDS)) {
			Toast.makeText(this, "Already Friends", Toast.LENGTH_LONG).show();;
		} else {
			final ParseUser user = (ParseUser)mUsers.get(position).get(ParseConstants.KEY_USER);
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Send Friend Request?");
			builder.setMessage("Send friend request to "+user.getUsername() + " ?");
			builder.setNegativeButton("Cancel", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.dismiss();
				}
			});
			builder.setPositiveButton("Send", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					mProgBar.setVisibility(View.VISIBLE);
					dialog.dismiss();
					HashMap<String, String> params = new HashMap<String, String>();
					
					params.put("receiverId",user.getObjectId());
					ParseCloud.callFunctionInBackground("sendFriendRequest", params, new FunctionCallback<String>() {

						@Override
						public void done(String message, ParseException e) {
							// TODO Auto-generated method stub
							mProgBar.setVisibility(View.INVISIBLE);
							if(e == null) {
								Toast.makeText(SearchResultsActivity.this, message, Toast.LENGTH_LONG).show();
							} else {
								Toast.makeText(SearchResultsActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
							}
						}
					});
				}
			});
			AlertDialog dialog = builder.create();
			dialog.show();
			
		}
		super.onListItemClick(l, v, position, id);
		

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.search_results, menu);
		mProgBar = (ProgressBar) findViewById(R.id.searchProgressBar);
		MenuItem searchItem = menu.findItem(R.id.search);
		SearchView searchView = (SearchView) MenuItemCompat
				.getActionView(searchItem);
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		searchView.setSearchableInfo(searchManager
				.getSearchableInfo(getComponentName()));
		searchView.setIconified(false);
		String query = getIntent().getStringExtra(SearchManager.QUERY);
		searchView.setQuery(query, false);
		searchQueryText(query);
		
		return true;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		handleIntent(intent);
	}

	// /////////////HANDLE INTENT/////////////
	private void handleIntent(Intent intent) {

		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {

			String queryText = intent.getStringExtra(SearchManager.QUERY);
			searchQueryText(queryText);
		} else {

		}
	}


	private void searchQueryText(String query) {
		mProgBar.setVisibility(View.VISIBLE);
		
		HashMap<String, String> params = new HashMap<String, String>();
		params.put(ParseConstants.KEY_QUERY_TEXT, query);
		ParseCloud.callFunctionInBackground("showQueryResults", params,
				new FunctionCallback<HashMap<String, Object>>() {

					@Override
					public void done(HashMap<String, Object> results,
							ParseException e) {
						mProgBar.setVisibility(View.INVISIBLE);
						// TODO Auto-generated method stub
						if (e == null) {
							Toast.makeText(
									SearchResultsActivity.this,
									"user received " + results.get("isFriends"),
									Toast.LENGTH_SHORT).show();
							mUsers.clear();
							mUsers.add(results);
							adapter = new UserAdapter(SearchResultsActivity.this, mUsers);
							setListAdapter(adapter);
						} else {
							Toast.makeText(
									SearchResultsActivity.this,
									e.getMessage(),
									Toast.LENGTH_SHORT).show();
						}

					}

				});
	}
}
