package com.ankurmittal.learning.fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.support.v4.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.ankurmittal.learning.ChatListActivity;
import com.ankurmittal.learning.LoginActivity;
import com.ankurmittal.learning.R;
import com.ankurmittal.learning.R.layout;
import com.ankurmittal.learning.R.string;
import com.ankurmittal.learning.adapters.FriendsAdapter;
import com.ankurmittal.learning.storage.ChatContent;
import com.ankurmittal.learning.storage.ChatItem;
import com.ankurmittal.learning.storage.ChatItemDataSource;
import com.ankurmittal.learning.storage.FriendsDataSource;
import com.ankurmittal.learning.util.ParseConstants;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
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
	protected FriendsAdapter adapter;
	private Callbacks mCallbacks = sDummyCallbacks;
	private ChatItemDataSource mChatItemDataSource;
	FriendsDataSource mFriendsDataSource;

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

	BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (ParseUser.getCurrentUser() != null) {
				adapter.refill(mFriendsDataSource.getAllFriends());
			}

		}

	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_friends, container,
				false);

		mChatItemDataSource = ChatItemDataSource.getInstance(getActivity());

		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		getActivity().registerReceiver(broadcastReceiver,
				new IntentFilter("Custom target refresh"));

		if (ParseUser.getCurrentUser() == null) {
			// throw new RuntimeException("Test Exception!");
			Intent intent2 = new Intent(getActivity(), LoginActivity.class);
			intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent2);

		} else {

			// open frnds databse connection
			Log.d("frinds frag", "on resume");
			mFriends = new ArrayList<ParseUser>();
			mCurrentUser = ParseUser.getCurrentUser();
			mFriendsRelation = mCurrentUser
					.getRelation(ParseConstants.KEY_FRIENDS_RELATION);
			mFriendsDataSource = FriendsDataSource.getInstance(getActivity());
			loadFromDatabase();
			loadFriends();
			getActivity().setProgressBarIndeterminateVisibility(true);
		}

	}

	private void loadFromDatabase() {
		// mFriendsDataSource.open();
		// check if database has data
		if (mFriendsDataSource.selectAll().getCount() > 0) {
			if (getListAdapter() == null) {
				// mFriends = new
				// ArrayList<ParseUser>(mFriendsDataSource.getAllFriends());
				mFriends.clear();
				mFriends.addAll(mFriendsDataSource.getAllFriends());
				adapter = new FriendsAdapter(getActivity(), mFriends);
				getListView().setAdapter(adapter);
			} else {
				((FriendsAdapter) getListAdapter()).refill(mFriendsDataSource
						.getAllFriends());
			}
		}

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		((ChatListActivity) activity).onSectionAttached(2);

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
	public void onPause() {
		super.onPause();
		getActivity().unregisterReceiver(broadcastReceiver);
		// close frnds database connection
		// mChatItemDataSource.close();
		// mFriendsDataSource.close();

	}

	@Override
	public void onListItemClick(ListView l, View v, int i, long id) {
		super.onListItemClick(l, v, i, id);
		if (ChatContent.ITEM_MAP.containsKey(mFriends.get(i).getString(
				ParseConstants.KEY_USER_ID))) {

			// Notify the active callbacks interface (the activity, if the
			// fragment is attached to one) that an item has been selected.
			mCallbacks.onItemSelected(mFriends.get(i).getString(
					ParseConstants.KEY_USER_ID));
		} else {
			// add item

			// mChatItemDataSource.open();
			addChatItem(mFriends.get(i).getString(ParseConstants.KEY_USER_ID),
					mFriends.get(i).getUsername());
			mCallbacks.onItemSelected(mFriends.get(i).getString(
					ParseConstants.KEY_USER_ID));

		}
	}

	private void loadFriends() {
		ParseQuery<ParseUser> query = mFriendsRelation.getQuery();
		query.addAscendingOrder(ParseConstants.KEY_USERNAME);
		// query.include(ParseConstants.KEY_PROFILE_IMAGE);
		query.findInBackground(new FindCallback<ParseUser>() {
			@Override
			public void done(List<ParseUser> friends, ParseException e) {
				if (getActivity() != null) {
					getActivity().setProgressBarIndeterminateVisibility(false);
				}
				if (e == null) {
					mFriends.clear();
					try {
						for (ParseUser friend : friends) {
							// not in database
							mFriends.add(friend);
							// add frnds to database
							mFriendsDataSource.insert(friend);
						}
					} finally {
						// mFriendsDataSource.close();
						if (getActivity() != null) {
							if (getListAdapter() == null) {
								Log.d("Call to frnds adapter1", friends.size()
										+ "");
								if (adapter != null) {
									adapter.refill(mFriendsDataSource
											.getAllFriends());
								} else {
									adapter = new FriendsAdapter(getActivity(),
											mFriendsDataSource.getAllFriends());
									setListAdapter(adapter);
								}

								updateFriendPics();
							}
						}
					}
					// load from database
				} else {
					Log.e(TAG, e.getMessage());
					if (getActivity() != null) {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								getActivity());
						builder.setMessage(e.getMessage())
								.setTitle(R.string.error_title)
								.setPositiveButton(android.R.string.ok, null);
						AlertDialog dialog = builder.create();
						dialog.show();
						// mFriendsDataSource.close();
					}

				}

			}
		});
		Log.i("Friends frag", "updating profile pics");
		// Fucking awsm
	}

	// Function which adds new chat item to database
	private void addChatItem(String id, String content) {
		ChatItem chatItem = new ChatItem(id, content);
		mChatItemDataSource.insert(chatItem);
		ChatContent.ITEM_MAP.put(chatItem.getId(), chatItem);
	}

	private void updateFriendPics() {
		ParseCloud.callFunctionInBackground("updateFriendsPics",
				new HashMap<String, String>(),
				new FunctionCallback<ArrayList<HashMap<String, String>>>() {

					@Override
					public void done(
							ArrayList<HashMap<String, String>> results,
							ParseException e) {
						// TODO Auto-generated method stub
						if (e == null) {
							if (results.size() > 0) {
								Log.e("frnds frag",
										"updating pics successfull  "
												+ results.get(0).get("img_url"));
								if (!getActivity().isFinishing()
										&& !getActivity().isDestroyed()) {
									mFriendsDataSource.updateImageUrlFromId(
											getActivity(), results);
									adapter.refill(mFriendsDataSource
											.getAllFriends());

									// update chat Items
									ChatItemDataSource chatDataSource = ChatItemDataSource
											.getInstance(getActivity());
									chatDataSource.updateImageUrlFromId(
											getActivity(), results);
								}

							}

						} else {
							Log.e("frnds frag",
									"ERROR updating pics successfull  "
											+ e.getMessage());
						}
					}
				});
	}
}
