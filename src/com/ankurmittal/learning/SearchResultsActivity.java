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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Toast;

import com.ankurmittal.learning.adapters.UserAdapter;
import com.ankurmittal.learning.util.ParseConstants;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class SearchResultsActivity extends ListActivity {

	public static final String TAG = SearchResultsActivity.class
			.getSimpleName();
	ArrayList<ParseUser> mUsers;
	ArrayList<String> mRaw;
	String[] mUsernames;
	SearchView searchView;
	ProgressBar mProgBar;
	ArrayList<Integer> isFrndReqSent;
	protected ParseRelation<ParseUser> friendsRel;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_results);
		mProgBar = (ProgressBar) findViewById(R.id.searchProgressBar);
		ParseUser currentUser = ParseUser.getCurrentUser();
		friendsRel = currentUser
				.getRelation(ParseConstants.KEY_FRIENDS_RELATION);

		handleIntent(getIntent());
	}

	@Override
	protected void onListItemClick(ListView l, View v, final int position,
			long id) {

		super.onListItemClick(l, v, position, id);
		if(isFrndReqSent.get(position) == 00) {String friendName = mUsernames[position];
		final ParseUser friend = mUsers.get(position);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.add_friend_dialog_title);
		builder.setMessage("Send friend request to " + friendName + " ?");
		builder.setPositiveButton(android.R.string.ok, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				ParseObject friendRequest = new ParseObject(
						ParseConstants.KEY_FRIENDS_REQUEST);
				friendRequest.put(ParseConstants.KEY_FRND_REQ_RECEIVER,
						friend.getObjectId());
				friendRequest.put(ParseConstants.KEY_REQUEST_SENDER,
						ParseUser.getCurrentUser());
				friendRequest.put(ParseConstants.KEY_SENDER_NAME, ParseUser
						.getCurrentUser().getUsername());
				friendRequest.saveInBackground(new SaveCallback() {

					@Override
					public void done(ParseException e) {

						if (e == null) {
							// friend request sent
							isFrndReqSent.set(position, 10);
							Toast.makeText(SearchResultsActivity.this,
									R.string.friend_request_sent_label,
									Toast.LENGTH_SHORT).show();
							showQueryResults();
						} else {
							// there was error
							Toast.makeText(SearchResultsActivity.this,
									R.string.friend_request_error_label,
									Toast.LENGTH_SHORT).show();
						}

					}
				});
			}
		});
		builder.setNegativeButton(android.R.string.cancel, null);
		AlertDialog dialog = builder.create();
		dialog.show();}
		
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
		searchView.setOnQueryTextListener(new OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String queryText) {

				handleQueryText(queryText);
				return false;
			}

			@Override
			public boolean onQueryTextChange(String queryText) {

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

	// /////////////HANDLE INTENT/////////////
	private void handleIntent(Intent intent) {

		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {

			String queryText = intent.getStringExtra(SearchManager.QUERY);

			handleQueryText(queryText);

			// showResults(query);
		} else {

		}
	}

	// //////////////HANDLE TEXT QUERY//////////////////////////
	private void handleQueryText(String queryText) {
		ParseQuery<ParseUser> users = ParseUser.getQuery();

		users.whereStartsWith("username", queryText);
		mProgBar.setVisibility(View.VISIBLE);

		users.findInBackground(new FindCallback<ParseUser>() {

			@Override
			public void done(List<ParseUser> users, ParseException e) {

				if (e == null) {
					// success, display users
					Log.d(TAG, "users :" + users.size());
					mUsers = new ArrayList<ParseUser>();
					mRaw = new ArrayList<String>();
					isFrndReqSent = new ArrayList<Integer>();
					for (int i = 0; i < users.size(); i++) {
						if (users
								.get(i)
								.getUsername()
								.equals(ParseUser.getCurrentUser()
										.getUsername())) {

						} else {
							mRaw.add(users.get(i).getUsername());
							mUsers.add(users.get(i));
						}
					}

					for (int i = 0; i < mUsers.size(); i++) {

						Log.d(TAG, isFrndReqSent.size() + " >> size");
						findUsersAndCheckFrndReqSent(mUsers, i);

					}
					for (int i = 0; i < mUsers.size(); i++) {

						isFriends(mUsers, i);
					}

					// findLastUser(users);
					mUsernames = mRaw.toArray(new String[mRaw.size()]);

				} else {
					// there was an error
					Log.e(TAG, e.getMessage(), e);
				}

			}
		});
	}

	// ////////////find users///////////////////////////////////
	private void findUsersAndCheckFrndReqSent(final List<ParseUser> users,
			final int i) {

		isFrndReqSent.add(11);
		ParseQuery<ParseObject> getFriendReqs = new ParseQuery<ParseObject>(
				ParseConstants.KEY_FRIENDS_REQUEST);
		getFriendReqs.whereEqualTo(ParseConstants.KEY_REQUEST_SENDER,
				ParseUser.getCurrentUser());
		getFriendReqs.whereEqualTo(ParseConstants.KEY_FRND_REQ_RECEIVER, users
				.get(i).getObjectId());

		Log.d(TAG, "" + i);

		getFriendReqs.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> frnds, ParseException e) {
				if (e == null) {
					if (frnds.size() > 0) {

						Log.d(TAG, "requestfound" + i);
						isFrndReqSent.set(i, 10);

					} else {
						Log.d(TAG, "req not found" + i);
						if (isFrndReqSent.get(i) != 00) {
							isFrndReqSent.set(i, 11);
						}
						else {
							if (isFrendReqListFull() ) {
								 //display list

								 Log.d(TAG, "frndreqSiz :" + isFrndReqSent.size()
								 + "  mUsers Size: " + mUsers.size());
								 Log.d(TAG, "true");
								 mProgBar.setVisibility(View.INVISIBLE);
								 showQueryResults();

							}
						}
					}

				} else {
					Log.d(TAG, "query error " + e.getMessage());
				}
				// /check friends here

			}

		});
	}

	// // ///////////////////find last user ////////////////////
	// private void findLastUser(final List<ParseUser> users) {
	// int i = users.size() - 1;
	// if (users.get(i).getUsername()
	// .equals(ParseUser.getCurrentUser().getUsername())) {
	// return;
	// } else {
	// mRaw.add(users.get(i).getUsername());
	// mUsers.add(users.get(i));
	// ParseQuery<ParseObject> getFriendReqs = new
	// ParseQuery<ParseObject>(
	// ParseConstants.KEY_FRIENDS_REQUEST);
	// getFriendReqs.whereEqualTo(ParseConstants.KEY_SENDER,
	// ParseUser.getCurrentUser());
	// getFriendReqs.whereEqualTo(
	// ParseConstants.KEY_FRND_REQ_RECEIVER, users.get(i)
	// .getObjectId());
	// getFriendReqs
	// .findInBackground(new FindCallback<ParseObject>() {
	//
	// @Override
	// public void done(List<ParseObject> frnds,
	// ParseException e) {
	// mProgBar.setVisibility(View.INVISIBLE);
	// if (e == null) {
	// if (frnds.size() > 0) {
	// Log.d(TAG, "requestfound");
	// isFrndReqSent.add(10);
	// } else {
	// Log.d(TAG, "req not found");
	// isFrndReqSent.add(00);
	// }
	// } else {
	// Log.d(TAG,
	// "query error " + e.getMessage());
	// }
	//
	//
	// }
	// });
	//
	// }
	// }

	private void isFriends(final List<ParseUser> users, final int i) {
		ParseUser user = users.get(i);

		ParseQuery<ParseUser> query = friendsRel.getQuery();
		query.whereEqualTo("objectId", user.getObjectId());

		query.findInBackground(new FindCallback<ParseUser>() {

			@Override
			public void done(List<ParseUser> frnds, ParseException e) {
				if (e == null) {
					if (frnds.size() > 0) {
						Log.d(TAG,
								"friend found " + i + " "
										+ isFrndReqSent.get(i));
						isFrndReqSent.set(i, 01);

					} else {
						Log.d(TAG,
								"not found " + i + " " + isFrndReqSent.get(i));
						if (isFrndReqSent.get(i) == 11) {
							isFrndReqSent.set(i, 00);
						}

					}
				} else {
					Log.d(TAG, "query error " + e.getMessage());
				}
				// mProgBar.setVisibility(View.INVISIBLE);
				// showQueryResults();
				if (isFrendReqListFull() ) {
					 //display list

					 Log.d(TAG, "frndreqSiz :" + isFrndReqSent.size()
					 + "  mUsers Size: " + mUsers.size());
					 Log.d(TAG, "true");
					 mProgBar.setVisibility(View.INVISIBLE);
					 showQueryResults();

				}

			}
		});
	}

	// /////////////DISPLAY USERS///////////////////
	private void showQueryResults() {
		Log.d(TAG, "frndreqSiz :" + isFrndReqSent.size() + "  mUsers Size: "
				+ mUsers.size());
		UserAdapter adapter = new UserAdapter(SearchResultsActivity.this,
				mUsers, isFrndReqSent);
		getListView().setAdapter(adapter);
	}

	private boolean isFrendReqListFull() {
		for (int x = 0; x < isFrndReqSent.size(); x++) {
			if (isFrndReqSent.get(x) == 11) {
				Log.d(TAG, "false" + x);
				return false;
			} 
		}
		return true;

	}
}
