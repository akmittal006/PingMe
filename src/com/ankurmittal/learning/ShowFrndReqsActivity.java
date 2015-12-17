package com.ankurmittal.learning;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ankurmittal.learning.adapters.FrndReqAdapter;
import com.ankurmittal.learning.util.ParseConstants;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class ShowFrndReqsActivity extends ListActivity {
	ArrayList<ParseUser> mSenders;
	ArrayList<ParseObject> mReqs;
	ProgressBar mProgressBar;
	ParseUser currentUser;
	FrndReqAdapter adapter;
	ParseQuery<ParseObject> query;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_frnd_reqs_2);
		currentUser = ParseUser.getCurrentUser();
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar1);
		mProgressBar.setVisibility(View.VISIBLE);
		loadFriendRequests();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		super.onListItemClick(l, v, position, id);
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.show_frnd_reqs, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void loadFriendRequests() {
		query = new ParseQuery<ParseObject>(
				ParseConstants.KEY_FRIENDS_REQUEST);
		query.whereEqualTo(ParseConstants.KEY_FRND_REQ_RECEIVER, ParseUser
				.getCurrentUser().getObjectId());
		query.include(ParseConstants.KEY_REQUEST_SENDER);
		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> requests, ParseException e) {
				mProgressBar.setVisibility(View.INVISIBLE);
				if (e == null) {
					// We found requests!

					mReqs = new ArrayList<ParseObject>(requests);
					displayReqs();

				} else {
					// getListView().setEmptyView(android.R.id.empty);
					Toast.makeText(ShowFrndReqsActivity.this, "No requests!",
							Toast.LENGTH_SHORT).show();
				}
			}

			private void displayReqs() {

				mSenders = new ArrayList<ParseUser>();
				for (ParseObject req : mReqs) {
					mSenders.add(req.getParseUser(ParseConstants.KEY_REQUEST_SENDER));
				}
				adapter = new FrndReqAdapter(ShowFrndReqsActivity.this,
						mSenders,mReqs,mProgressBar);
				getListView().setAdapter(adapter);
			}
		});

	}
	

}
