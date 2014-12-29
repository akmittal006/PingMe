package com.ankurmittal.learning;

import java.util.ArrayList;
import java.util.List;

import com.ankurmittal.learning.adapters.ParseConstants;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

public class ShowFrndReqs extends ListActivity {
	
	String[] frndReqSenders;
	ArrayList<ParseUser> mSenders;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_frnd_reqs);
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
		ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(ParseConstants.KEY_FRIENDS_REQUEST);
		query.whereEqualTo(ParseConstants.KEY_FRND_REQ_RECEIVER, ParseUser.getCurrentUser().getObjectId());
		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> requests, ParseException e) {
				
				if (e == null) {
					// We found requests!
					frndReqSenders = new String[requests.size()];
					
					int i =0;
					for(ParseObject request : requests) {
						mSenders.add(request.getParseUser(ParseConstants.KEY_SENDER));
						frndReqSenders[i] = mSenders.get(i).getUsername();
					}
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(ShowFrndReqs.this, android.R.layout.simple_list_item_1, frndReqSenders);
					setListAdapter(adapter);
				}else {
					//getListView().setEmptyView(android.R.id.empty);
				}
			}
		});
	}
}
