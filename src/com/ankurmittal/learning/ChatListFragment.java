package com.ankurmittal.learning;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.json.JSONObject;

import android.app.Activity;
import android.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.DataSetObserver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.ankurmittal.learning.adapters.ChatItemsAdapter;
import com.ankurmittal.learning.storage.ChatContent;
import com.ankurmittal.learning.storage.ChatItem;
import com.ankurmittal.learning.storage.ChatItemDataSource;
import com.ankurmittal.learning.storage.TextMessage;
import com.ankurmittal.learning.storage.TextMessageDataSource;
import com.ankurmittal.learning.util.Constants;
import com.ankurmittal.learning.util.ParseConstants;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

/**
 * A list fragment representing a list of Chats. This fragment also supports
 * tablet devices by allowing list items to be given an 'activated' state upon
 * selection. This helps indicate which item is currently being viewed in a
 * {@link ChatDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class ChatListFragment extends ListFragment {

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * activated item position. Only used on tablets.
	 */
	private static final String STATE_ACTIVATED_POSITION = "activated_position";

	/**
	 * The fragment's current callback object, which is notified of list item
	 * clicks.
	 */
	private Callbacks mCallbacks = sDummyCallbacks;

	/**
	 * The current activated item position. Only used on tablets.
	 */
	private int mActivatedPosition = ListView.INVALID_POSITION;

	/**
	 * A callback interface that all activities containing this fragment must
	 * implement. This mechanism allows activities to be notified of item
	 * selections.
	 */
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

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ChatListFragment() {
	}

	private TextMessageDataSource mMessageDataSource;
	private ChatItemDataSource mChatItemDataSource;
	private ArrayList<TextMessage> latestMessages;
	private ListView chatListView;
	private ChatItemsAdapter adapter;
	private ArrayList<ChatItem> mChatItems;

	private View rootView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (ParseUser.getCurrentUser() != null) {
			// throw new RuntimeException("Test Exception!");
			retrieveMessages();
			mChatItemDataSource = new ChatItemDataSource(getActivity());
			mChatItemDataSource.open();
		} else {
			Intent intent2 = new Intent(getActivity(), LoginActivity.class);
			intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent2);
		}
		final HashMap<String, Object> params = new HashMap<String, Object>();
		ParseCloud.callFunctionInBackground("hello", params,
				new FunctionCallback<String>() {

					@Override
					public void done(String arg0, ParseException e) {
						// TODO Auto-generated method stub
						if (e == null) {
							Log.i("Cloud code", "Yay it worked!");
						} else {
							Log.i("Cloud Code", "Some error" + e.getMessage());
						}
					}
				});

		mMessageDataSource = new TextMessageDataSource(getActivity());
		mChatItems = new ArrayList<ChatItem>();
		latestMessages = new ArrayList<TextMessage>();
		if (mChatItemDataSource != null) {
			mChatItems = mChatItemDataSource.getAllChatItems();
		}

		if (mChatItems != null) {
			for (ChatItem chatItem : mChatItems) {

				ChatContent.addItem(chatItem);

			}

		} else {
			mChatItems = new ArrayList<ChatItem>();
		}

		if (!getActivity().isFinishing()) {
			adapter = new ChatItemsAdapter(getActivity(), mChatItems);
			// Log.e("DEBUG", "Calling Adapter");
			setListAdapter(adapter);
		}

		// TODO: replace with a real list adapter.
		// chatListView.setAdapter(adapter);

	}

	// @Override
	// public View onCreateView(LayoutInflater inflater, ViewGroup container,
	// Bundle savedInstanceState) {
	// rootView = inflater.inflate(R.layout.fragment_chat_list, container,
	// false);
	// chatListView = (ListView)rootView.findViewById(R.id.chatList);
	// return rootView;
	// }

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Restore the previously serialized activated item position.
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			setActivatedPosition(savedInstanceState
					.getInt(STATE_ACTIVATED_POSITION));
		}
	}

	private BroadcastReceiver notificationMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			try {
				Log.e("chat list ", "intent received + " + intent.getAction());
				// Extract data included in the Intent

				String jsonData = intent.getStringExtra(Constants.JSON_MESSAGE);
				if (jsonData.equals("refresh")) {
					// callback from notification receiver after sending push to
					// refresh
					if (mChatItemDataSource != null) {
						mChatItems.clear();
						mChatItems = mChatItemDataSource.getAllChatItems();
						adapter.refill(mChatItems);
						// Log.d("cat List", "" + mChatItems.size());
					}
				}

				JSONObject jsonMessage = new JSONObject(jsonData);
				Log.e("chat list",
						"push received" + jsonMessage.getString("type"));
				if (jsonMessage.getString("type").equals("message")) {
					// Log.e("chat list","message push received");
				} else if (jsonMessage.getString("type").equals("update")) {
					// Log.e("chat list","update  push received");
				}
				Log.e("chat List Activity", "hurray updating activity");
				if (mChatItemDataSource != null) {
					mChatItems.clear();

				}

				updateView();
			} catch (Exception e) {
				Log.i("chat List error", "error while receiving notification");
			}
			// do other stuff here
		}
	};

	@Override
	public void onPause() {
		getActivity().unregisterReceiver(notificationMessageReceiver);
		super.onPause();
		// close database connection
		// mMessageDataSource.close();
		if (mMessageDataSource != null) {
			mMessageDataSource.close();
		}

	}

	@Override
	public void onResume() {
		super.onResume();

		getActivity().registerReceiver(notificationMessageReceiver,
				new IntentFilter(Constants.PUSH_TO_CHAT));

		// open database connection
		if (mMessageDataSource != null && mChatItemDataSource != null) {
			mMessageDataSource.open();
			mChatItemDataSource.open();
		}
		if (ParseUser.getCurrentUser() != null) {

			if (!(getActivity().isFinishing())) {
				retrieveMessages();
				updateDeliveredMessages();
				// sortMessagesFromDatabase();
			}

		}

		setListAdapter(adapter);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		((ChatListActivity) activity).onSectionAttached(1);

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
	public void onListItemClick(ListView listView, View view, int position,
			long id) {
		super.onListItemClick(listView, view, position, id);

		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an item has been selected.
		mCallbacks.onItemSelected(mChatItems.get(position).id);
		// Log.e("DEBUG","" + mChatItems.get(position).getContent());
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
	}

	/**
	 * Turns on activate-on-click mode. When this mode is on, list items will be
	 * given the 'activated' state when touched.
	 */
	public void setActivateOnItemClick(boolean activateOnItemClick) {
		// When setting CHOICE_MODE_SINGLE, ListView will automatically
		// give items the 'activated' state when touched.
		getListView().setChoiceMode(
				activateOnItemClick ? ListView.CHOICE_MODE_SINGLE
						: ListView.CHOICE_MODE_NONE);
	}

	private void setActivatedPosition(int position) {
		if (position == ListView.INVALID_POSITION) {
			getListView().setItemChecked(mActivatedPosition, false);
		} else {
			getListView().setItemChecked(position, true);
		}

		mActivatedPosition = position;
	}

	// /either we receive message by push or by forcefully updating this
	// fragment

	// //////forcefully receiving messages
	private void retrieveMessages() {
		Log.i("chat lis", "retrieving");
		ParseQuery<ParseObject> messagesQuery = new ParseQuery<ParseObject>(
				ParseConstants.TEXT_MESSAGE);
		messagesQuery.whereEqualTo(ParseConstants.KEY_MESSAGE_RECEIVER_ID,
				ParseUser.getCurrentUser().get(ParseConstants.KEY_USER_ID));
		messagesQuery.whereEqualTo("isSent", Constants.MESSAGE_STATUS_PENDING);
		messagesQuery.include(ParseConstants.KEY_MESSAGE_SENDER);
		messagesQuery.include(ParseConstants.KEY_CREATED_AT);
		messagesQuery.addAscendingOrder(ParseConstants.KEY_CREATED_AT);
		messagesQuery.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> pTextMessages, ParseException e) {
				Date prevDate = null;
				Date currDate = null;
				Log.i("chat lis", "retrieved");

				if (e == null) {
					Log.d("Retrieved messages", "NO. :-" + pTextMessages.size());
					// hurray we received our messages
					int i = 0;
					if (ParseUser.getCurrentUser() != null) {
						try {
							for (ParseObject pTextMessage : pTextMessages) {
								// create a text message and save it to database
								TextMessage textmessage = createTextMessage(pTextMessage);
								latestMessages.add(textmessage);
								mMessageDataSource.insert(textmessage);

								pTextMessage.pinInBackground(
										ParseConstants.GROUP_MESSAGE_DELIVERED,
										new SaveCallback() {

											@Override
											public void done(ParseException e) {
												if (e == null) {
													Log.i("cht list",
															"to update Delivered msg pinned");

												} else {
													Log.i("cht list",
															"not  pinned");
												}

											}
										});
							}
						} finally {
							// sortMessagesFromDatabase();
							updateChatItemsFromMessages(latestMessages);
							updateView();
						}
					}

					if (getActivity() != null) {
						updateDeliveredMessages();
					}

				} else {
					Log.d("REtrieval errror", "" + e.getMessage());
				}
			}

		});
	}

	private void updateChatItemsFromMessages(ArrayList<TextMessage> messages) {
		for (TextMessage message : messages) {
			updateChatItem(message);
		}
		latestMessages.clear();
	}

	private void updateChatItem(TextMessage textmessage) {
		// TODO Auto-generated method stub
		String id;
		String content;
		if (textmessage.getSenderId().equals(
				ParseUser.getCurrentUser().getObjectId())) {
			id = textmessage.getReceiverId(); // equivalent to item id
			content = textmessage.getReceiverName();

		} else {
			id = textmessage.getSenderId(); // equivalent to item id
			content = textmessage.getSenderName();
		}

		ChatItem chatItem = new ChatItem(id, content);
		if(!ChatContent.ITEM_MAP.containsKey(chatItem.id)) {
			ChatContent.ITEM_MAP.put(id, chatItem);
		}
		chatItem.setLastMessage(textmessage);
		
		mChatItemDataSource.insert(chatItem);

	}

	private String getDateTime(java.util.Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		// Date date = new Date();
		return dateFormat.format(date);
	}

	private TextMessage createTextMessage(ParseObject pTextMessage) {
		TextMessage textMessage = new TextMessage();
		textMessage.setMessage(pTextMessage
				.getString(ParseConstants.KEY_MESSAGE));
		Log.d("list frag ",
				" "
						+ pTextMessage.getString(ParseConstants.KEY_MESSAGE)
						+ ": "
						+ pTextMessage
								.getString(ParseConstants.KEY_MESSAGE_RECEIVER_NAME
										+ ", ")
						+ pTextMessage.getString("isSent"));
		textMessage.setMessageId(pTextMessage.getObjectId());
		textMessage.setReceiverId(pTextMessage
				.getString(ParseConstants.KEY_MESSAGE_RECEIVER_ID));
		textMessage.setReceiverName(pTextMessage
				.getString(ParseConstants.KEY_MESSAGE_RECEIVER_NAME));
		textMessage.setSenderId(pTextMessage.getParseUser(
				ParseConstants.KEY_MESSAGE_SENDER).getObjectId());
		textMessage.setSenderName(pTextMessage.getParseUser(
				ParseConstants.KEY_MESSAGE_SENDER).getUsername());
		textMessage.setCreatedAt(getDateTime(pTextMessage.getCreatedAt()));
		textMessage.setMessageStatus(pTextMessage.getString("isSent"));

		return textMessage;
	}

	// @Deprecated
	private void sortMessagesFromDatabase() {

		if (mMessageDataSource.selectAll().getCount() != 0) {
			// getting all stored messages
			ArrayList<TextMessage> allMessages = mMessageDataSource
					.getAllMessages();

			for (TextMessage message : allMessages) {
				String id;
				String content;
				if (message.getSenderId().equals(
						ParseUser.getCurrentUser().getObjectId())) {
					id = message.getReceiverId(); // equivalent to item id
					content = message.getReceiverName();

				} else {
					id = message.getSenderId(); // equivalent to item id
					content = message.getSenderName();
				}

				if (ChatContent.ITEM_MAP.containsKey(id)) {
					if (mMessageDataSource.isMessageNew(message).getCount() == 0) {
						// messages from that sender exist
						ChatItem chatItem = ChatContent.ITEM_MAP.get(id);
						chatItem.addMessage(message);
					}
				} else {
					// new chat item is created
					ChatItem chatItem = new ChatItem(id, content);
					chatItem.getEmail();
					Log.d("chat list", "new chat item created :" + content);
					chatItem.addMessage(message);
					ChatContent.addItem(chatItem);
				}
			}

		}
	}

	public void updateView() {
		if (getActivity() != null && !(getActivity().isFinishing())) {

			if (mChatItemDataSource != null && mChatItems != null) {
				Log.i("chat list frag", "............updating view...........");
				mChatItems.clear();
				mChatItems = mChatItemDataSource.getAllChatItems();
			}
			if (mChatItems != null && mChatItems.size() != 0) {
				// TODO Auto-generated method stub
				setListAdapter(new ChatItemsAdapter(getActivity(), mChatItems));

			}

		}

	}

	@Override
	public void onDestroy() {
		mMessageDataSource.close();
		super.onDestroy();

	}

	private void updateDeliveredMessages() {
		Log.i("chat list frag", "update delivered message");

		ConnectivityManager cm = (ConnectivityManager) getActivity()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();

		if ((ni != null) && (ni.isConnected())) {
			if (!ParseAnonymousUtils.isLinked(ParseUser.getCurrentUser())) {
				ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(
						"TextMessage");
				query.fromPin(ParseConstants.GROUP_MESSAGE_DELIVERED);
				query.findInBackground(new FindCallback<ParseObject>() {
					public void done(List<ParseObject> messages,
							ParseException e) {
						if (e == null && messages.size() > 0) {
							Log.e("pinned to update deliveredmsgs", ""
									+ messages.size());
							final HashMap<String, String> params = new HashMap<String, String>();
							int i = 0;
							for (final ParseObject message : messages) {
								params.put(message.getObjectId(),
										Constants.MESSAGE_STATUS_DELIVERED);
								i++;

							}
							if (i == messages.size()) {
								Log.e("calling cloud function update msgs ",
										"now+ psrsms: " + params.size());
								ParseCloud.callFunctionInBackground(
										"updateMessages", params,
										new FunctionCallback<String>() {

											@Override
											public void done(String arg0,
													ParseException e) {
												// TODO Auto-generated method
												// stub
												if (e == null) {
													Log.e("Success in updating delivered msgs",
															"Yay it worked! "
																	+ arg0);
													ParseObject
															.unpinAllInBackground(ParseConstants.GROUP_MESSAGE_DELIVERED);
												} else {
													Log.e("ERROR in updating delivered msgs",
															"Some error"
																	+ e.getMessage());
												}
											}
										});
							}

						} else {
							// Log.i("To update pinned messages",
							// " Error finding pinned messages "
							// + e.getMessage());
						}
					}
				});
			} else {
				// If we have a network connection but no logged in user, direct
				// the person to log in or sign up.

			}
		} else {
			// If there is no connection, let the user know the sync didn't
			// happen
			Toast.makeText(
					getActivity().getApplicationContext(),
					"Your device appears to be offline. Some Messages may not have been synced .",
					Toast.LENGTH_SHORT).show();
		}
	}

}
