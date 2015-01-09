package com.ankurmittal.learning;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.ankurmittal.learning.adapters.FriendsAdapter;
import com.ankurmittal.learning.adapters.ParseConstants;
import com.ankurmittal.learning.storage.ChatContent;
import com.ankurmittal.learning.storage.ChatItem;
import com.ankurmittal.learning.storage.TextMessageSource;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

public class FriendsFragment extends ListFragment {
	
	public static final String TAG = FriendsFragment.class.getSimpleName();

	protected ParseRelation<ParseUser> mFriendsRelation;
	protected ParseUser mCurrentUser;	
	protected ArrayList<ParseUser> mFriends;
	protected List<ParseObject> mAccepts;
	private Callbacks mCallbacks = sDummyCallbacks;
	
	public interface Callbacks {
		/**
		 * Callback for when an item has been selected.
		 */
		public void onItemSelected(String id);
	}
	/**
	 * A dummy implementation of the {@link Callbacks} interface that does
	 * nothing. Used only when this fragment is not attached to an activity.
	 */
	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onItemSelected(String id) {
		}
	};
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_friends,
				container, false);
		
		//TextView emptyTextView = (TextView)rootView.findViewById(android.R.id.empty);
		//mGridView.setEmptyView(emptyTextView);

		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		loadAccepts();
		mCurrentUser = ParseUser.getCurrentUser();
		mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);
		
		getActivity().setProgressBarIndeterminateVisibility(true);
		
		
	}
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}

		mCallbacks = (Callbacks) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface to the dummy implementation.
		mCallbacks = sDummyCallbacks;
	}
	@Override
	public void onListItemClick(ListView l, View v, int i, long id) {
		super.onListItemClick(l, v, i, id);
		TextMessageSource  msgSource= new TextMessageSource(mCurrentUser);
		if (ChatContent.ITEM_MAP.containsKey(mFriends.get(i).getObjectId()) ) {
			// Notify the active callbacks interface (the activity, if the
			// fragment is attached to one) that an item has been selected.
			mCallbacks.onItemSelected(mFriends.get(i).getObjectId());
		}else {
			ChatContent.addItem(new ChatItem(mFriends.get(i).getObjectId(), mFriends.get(i).getUsername(), msgSource.getMessages()));
			mCallbacks.onItemSelected(mFriends.get(i).getObjectId());
		}
		
	}

	private void loadAccepts() {
		ParseQuery<ParseObject> acceptsQuery = new ParseQuery<ParseObject>(
				ParseConstants.KEY_FRIENDS_REQUEST_ACCEPTS);
		acceptsQuery.whereEqualTo(ParseConstants.KEY_RECEIVER, ParseUser
				.getCurrentUser().getObjectId());
		acceptsQuery.include(ParseConstants.KEY_SENDER);
		acceptsQuery.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> accepts, ParseException e) {
				if (e == null) {
					// We found requests!
					mFriends = new ArrayList<ParseUser>();

					mAccepts = new ArrayList<ParseObject>(accepts);
					if(mAccepts.size() > 0) {
						ParseRelation<ParseUser> friendRel = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);
						for(ParseObject accept : mAccepts) {
							//save at backend
							friendRel.add(accept.getParseUser(ParseConstants.KEY_SENDER));
							
							accept.deleteInBackground(new DeleteCallback() {
								
								@Override
								public void done(ParseException e) {
									if(e == null) {
										Log.d("DELETED", "DELETED");
									}
									else {
										Log.d("Not DELETED", " NOT DELETED");
									}
									
								}
							});
						}
						mCurrentUser.saveInBackground();
						mAccepts.clear();
					}
					
					loadFriends();

				} else {
					mAccepts = new ArrayList<ParseObject>();
					loadFriends();
					// getListView().setEmptyView(android.R.id.empty);
//					Toast.makeText(ShowFrndReqsActivity.this, "No requests!",
//							Toast.LENGTH_SHORT).show();
				}
			}

			private void loadFriends() {
				ParseQuery<ParseUser> query = mFriendsRelation.getQuery();
				query.addAscendingOrder(ParseConstants.KEY_USERNAME);
				query.findInBackground(new FindCallback<ParseUser>() {
					@Override
					public void done(List<ParseUser> friends, ParseException e) {
						getActivity().setProgressBarIndeterminateVisibility(false);
						
						if (e == null) {
							
							for(ParseUser friend : friends) {
								mFriends.add(friend);
								
							}
							
							if (getListAdapter() == null) {
								FriendsAdapter adapter = new FriendsAdapter(getActivity(), mFriends);
								getListView().setAdapter(adapter);
							}
							else {
								((FriendsAdapter)getListAdapter()).refill(mFriends);
							}
						}
						else {
							Log.e(TAG, e.getMessage());
							AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
							builder.setMessage(e.getMessage())
								.setTitle(R.string.error_title)
								.setPositiveButton(android.R.string.ok, null);
							AlertDialog dialog = builder.create();
							dialog.show();
						}
					}
				});
			}


		});
	}
}
