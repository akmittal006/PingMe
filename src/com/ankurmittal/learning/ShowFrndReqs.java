package com.ankurmittal.learning;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ankurmittal.learning.adapters.ParseConstants;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class ShowFrndReqs extends ListActivity {
	
	String[] frndReqSenders;
	ArrayList<ParseUser> mSenders;
	ProgressBar mProgressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_frnd_reqs);
		mProgressBar = (ProgressBar)findViewById(R.id.progressBar1);
		mProgressBar.setVisibility(View.VISIBLE);
		loadFriendRequests();
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
				mProgressBar.setVisibility(View.INVISIBLE);
				if (e == null) {
					// We found requests!
					frndReqSenders = new String[requests.size()];
					mSenders = new ArrayList<ParseUser>();
					int i =0;
					for(ParseObject request : requests) {
						Log.d("ERROR:", ""+requests.size());
						//Toast.makeText(ShowFrndReqs.this, ""+requests.size(), Toast.LENGTH_SHORT).show();
						mSenders.add(request.getParseUser(ParseConstants.KEY_SENDER));
						
						frndReqSenders[i] = request.getString(ParseConstants.KEY_SENDER_NAME);
					}
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(ShowFrndReqs.this, android.R.layout.simple_list_item_1, frndReqSenders);
					setListAdapter(adapter);
				}else {
					//getListView().setEmptyView(android.R.id.empty);
					Toast.makeText(ShowFrndReqs.this, "No requests!", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		//ParseUser preFriend = mfrnd(position);
		super.onListItemClick(l, v, position, id);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		View view = this.getLayoutInflater().inflate(R.layout.custom_dialog, null);
		TextView usernameTextView = (TextView)view.findViewById(R.id.usernameTextView);
		usernameTextView.setText(frndReqSenders[position]);
		builder.setView(view);
		
		builder.setTitle("Add friend?");
//		builder.setPositiveButton(R.string.accept_label, null);
//		builder.setNegativeButton(android.R.string.cancel, null);
		AlertDialog dialog = builder.create();
		dialog.show();
	}
}
