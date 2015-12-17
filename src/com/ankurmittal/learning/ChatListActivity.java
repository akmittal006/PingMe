package com.ankurmittal.learning;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.ankurmittal.learning.application.PingMeApplication;
import com.ankurmittal.learning.storage.ChatContent;
import com.ankurmittal.learning.storage.ChatItemDataSource;
import com.ankurmittal.learning.storage.FriendsDataSource;
import com.ankurmittal.learning.storage.TextMessageDataSource;
import com.ankurmittal.learning.util.Constants;
import com.ankurmittal.learning.util.ParseConstants;
import com.ankurmittal.learning.util.TypefaceSpan;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;


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
		getActionBar().setDisplayShowHomeEnabled(false);
		
		ChatListFragment fragment = new ChatListFragment();
//		fragment.setArguments(arguments);
		getFragmentManager().beginTransaction()
				.add(R.id.chat_list_container, fragment).commit();
		
		
		
		if (findViewById(R.id.chat_detail_container) != null) {
			// The detail container view will be present only in the
			// large-screen layouts (res/values-large and
			// res/values-sw600dp). If this view is present, then the
			// activity should be in two-pane mode.
			mTwoPane = true;

			// In two-pane mode, list items should be given the
			// 'activated' state when touched.
			fragment.setActivateOnItemClick(true);
		}
		
	}

//	private void makeMeRequest() {
//		Request request = Request.newMeRequest(ParseFacebookUtils.getSession(),
//	            new Request.GraphUserCallback() {
//	                @Override
//	                public void onCompleted(GraphUser user, Response response) {
//	                    // handle response
//	                	setTitle(user.getFirstName());
//	                	Log.d("hooooorrayyyy", user.getFirstName());
//	                	ParseUser.getCurrentUser().put(ParseConstants.KEY_USER_ID, ParseUser.getCurrentUser().getObjectId());
//	    				ParseUser.getCurrentUser().setUsername(user.getFirstName());
//	    				ParseUser.getCurrentUser().setEmail(user.getProperty("email").toString());
//	    				ParseUser.getCurrentUser().put(ParseConstants.KEY_LOWER_USERNAME, user.getFirstName().toLowerCase());
//	    				ParseUser.getCurrentUser().saveInBackground();
//	    				
//	                }
//	            });
//	    request.executeAsync();
//	}

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
			PingMeApplication.updateParseInstallation(currentUser);
			loadFriendRequests();
			ParseUser.getCurrentUser().put("UserId", ParseUser.getCurrentUser().getObjectId());
			ParseUser.getCurrentUser().saveInBackground();
			
			SpannableString s = new SpannableString("Ping Me");
		    s.setSpan(new TypefaceSpan(this, "LOBSTERTWO-BOLD.OTF"), 0, s.length(),
		            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		 
		    // Update the action bar title with the TypefaceSpan instance
		    ActionBar actionBar = getActionBar();
		    actionBar.setTitle(s);
//			
//			ParseFacebookUtils.initialize(R.string.facebook_app_id + "");
//			Session session = ParseFacebookUtils.getSession();
//		    if (session != null && session.isOpened()) {
//		        makeMeRequest();
//		        
//		    }
//			// Logs 'install' and 'app activate' App Events.
//			  AppEventsLogger.activateApp(this);
		} else {
			// show the signup or login screen
			Intent intent2 = new Intent(ChatListActivity.this, LoginActivity.class);
			intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent2);
		}

	}
	@Override
	protected void onPause() {
	  super.onPause();

	  // Logs 'app deactivate' App Event.
//	  AppEventsLogger.deactivateApp(this);
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
			//prompt if they wanna logout
			//TODO:do smthing
			//if yes delete frnds database
			ChatItemDataSource mChatItemDataSource = new ChatItemDataSource(this);
			mChatItemDataSource.open();
			mChatItemDataSource.deleteAll();
			mChatItemDataSource.close();
			FriendsDataSource mFriendsDataSource = new FriendsDataSource(this);
			mFriendsDataSource.open();
			Log.d("DATABASE CHECK",""+ mFriendsDataSource.selectAll().getCount());
			mFriendsDataSource.deleteAll();
			Log.d("DATABASE CHECK",""+ mFriendsDataSource.selectAll().getCount());
			mFriendsDataSource.close();
			TextMessageDataSource mMessageSource = new TextMessageDataSource(this);
			mMessageSource.open();
			Log.d("DATABASE CHECK",""+ mMessageSource.selectAll().getCount());
			mMessageSource.deleteAll();
			Log.d("DATABASE CHECK",""+ mMessageSource.selectAll().getCount());
			mMessageSource.close();
			ChatContent.deleteAllItems();
			ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(
					ParseConstants.TEXT_MESSAGE);
			query.fromPin(Constants.GROUP_NOT_SENT);
			query.fromPin("Delete Messages");
			
			query.findInBackground(new FindCallback<ParseObject>() {
				
				@Override
				public void done(List<ParseObject> arg0, ParseException arg1) {
					// TODO Auto-generated method stub
					Log.e("logging out ", "total pinned msgs" + arg0.size()); 
					final int i = 0;
					for (ParseObject message: arg0) {
						
						message.unpinInBackground(Constants.GROUP_NOT_SENT, new DeleteCallback() {
							
							@Override 
							public void done(ParseException e) {
								// TODO Auto-generated method stub
								if(e == null) {
									Log.i("unpinned", "msg unpinned " + i);
								}
								else  {
									Log.e("unpinned error", "msg unpinned " + i + " " + e.getMessage());
								}
								
							}
						});
						message.unpinInBackground("Delete Messages");
						
					}
					
				}
			});
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
		else if(id == R.id.profile) {
			Intent intent = new Intent(ChatListActivity.this, ProfileActivity.class);
			startActivity(intent);
		}

		return super.onOptionsItemSelected(item);
	}
	
	


}