package com.ankurmittal.learning.fragments;

import io.socket.client.Socket;
import io.socket.emitter.Emitter.Listener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.SQLException;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.ankurmittal.learning.ChatListActivity;
import com.ankurmittal.learning.LoginActivity;
import com.ankurmittal.learning.R;
import com.ankurmittal.learning.adapters.ChatItemsAdapter;
import com.ankurmittal.learning.application.PingMeApplication;
import com.ankurmittal.learning.storage.ChatContent;
import com.ankurmittal.learning.storage.ChatItem;
import com.ankurmittal.learning.storage.ChatItemDataSource;
import com.ankurmittal.learning.storage.TextMessage;
import com.ankurmittal.learning.storage.TextMessageDataSource;
import com.ankurmittal.learning.util.Constants;
import com.ankurmittal.learning.util.ParseConstants;
import com.ankurmittal.learning.util.Utils;
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

	public ImageView expandedImageView;
	Rect finalBounds = new Rect();
	Point globalOffset = new Point();
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

	public static final String SHARED_PREF_KEY = "com.ankurmittal.learning.PREF_KEY";

	public interface Callbacks {
		/**
		 * Callback for when an item has been selected.
		 */
		public void onItemSelected(String id, View sharedView);
	}

	/**
	 * A dummy implementation of the {@link Callbacks} interface that does
	 * nothing. Used only when this fragment is not attached to an activity.
	 */
	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onItemSelected(String id, View sharedview) {
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
	private ChatItemsAdapter adapter;
	private ArrayList<ChatItem> mChatItems;

	private View rootView;
	private Socket mSocket;
	private Listener messageSentEventListener = new Listener() {

		@Override
		public void call(Object... arg0) {
			final String senderId;
			JSONObject data = (JSONObject) arg0[0];
			TextMessage messageReceived = new TextMessage();
			messageReceived = Utils.createTextMessageFromSocketData(data);

		}

	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (ParseUser.getCurrentUser() != null) {
			// throw new RuntimeException("Test Exception!");
			retrieveMessages();
			mChatItemDataSource = new ChatItemDataSource(getActivity());
			mChatItemDataSource.open();

			mSocket = PingMeApplication.mSocket;
			mSocket.on(Constants.EVENT_TYPING, new Listener() {

				@Override
				public void call(final Object... arg0) {
					// TODO Auto-generated method stub
					final String senderId;
					JSONObject data = (JSONObject) arg0[0];
					try {
						senderId = data.getString("sender");

						if (ChatContent.ITEM_MAP.containsKey(senderId)
								&& getActivity() != null) {
							((AppCompatActivity) getActivity())
									.runOnUiThread(new Runnable() {

										@Override
										public void run() {
											// TODO Auto-generated method stub
											if (mChatItems
													.indexOf(ChatContent.ITEM_MAP
															.get(senderId)) >= 0) {
												adapter.changeSubtitleToTyping(mChatItems
														.indexOf(ChatContent.ITEM_MAP
																.get(senderId)));
											}

										}
									});
						} else {

						}
					} catch (JSONException e) {
						Log.e("VERY BIG DEBUG ***", "" + e.getMessage()
								+ " - - -" + data.toString());
						return;
					}
					// add the message to view
				}

			});

			mSocket.on(Constants.EVENT_MESSAGE_SENT, messageSentEventListener);

		} else {
			Intent intent2 = new Intent(getActivity(), LoginActivity.class);
			intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent2);
		}
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

		// ParseCloud.callFunctionInBackground("hello2", new HashMap<String,
		// String>(), new FunctionCallback<String>() {
		//
		// @Override
		// public void done(String arg0, ParseException arg1) {
		// // TODO Auto-generated method stub
		//
		// }
		// });

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_chat_list2, container,
				false);

		View containerView = (View) rootView.findViewById(R.id.listContainer);
		expandedImageView = (ImageView) containerView
				.findViewById(R.id.expanded_image);
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
		try {
			if (ParseUser.getCurrentUser() == null) {
				Intent intent2 = new Intent(getActivity(), LoginActivity.class);
				intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
				intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent2);
			}
		} finally {
			getActivity().registerReceiver(notificationMessageReceiver,
					new IntentFilter(Constants.PUSH_TO_CHAT));

			// open database connection
			if (mMessageDataSource != null && mChatItemDataSource != null) {
				mMessageDataSource.open();
				mChatItemDataSource.open();
			} else {
				mMessageDataSource = new TextMessageDataSource(getActivity());
				mChatItemDataSource = new ChatItemDataSource(getActivity());
			}
			if (ParseUser.getCurrentUser() != null) {

				if (!(getActivity().isFinishing())) {
					retrieveMessages();
					updateDeliveredMessages();
					// sortMessagesFromDatabase();
				}

			}

			updateChatListView();
		}

	}

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
		try {
			mMessageDataSource.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
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

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position,
			long id) {
		super.onListItemClick(listView, view, position, id);

		ImageView profilePicView = (ImageView) view
				.findViewById(R.id.userImageView);

		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an item has been selected.
		Log.e("DEBUG BEFORE ERROR", mChatItems.get(position).id);
		mCallbacks.onItemSelected(mChatItems.get(position).id, profilePicView);
		// Log.e("DEBUG","" + mChatItems.get(position).getContent());
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

	@Override
	public void onDestroy() {
		mMessageDataSource.close();
		super.onDestroy();

	}

	public void updateView() {
		if (getActivity() != null && !(getActivity().isFinishing())) {

			if (mChatItemDataSource != null && mChatItems != null) {
				Log.i("chat list frag", "............updating view...........");
				mChatItems.clear();
				mChatItems = mChatItemDataSource.getAllChatItems();
			}
			if (mChatItems != null && mChatItems.size() != 0) {
				updateChatListView();
			}

		}

	}

	private void updateChatListView() {
		try {
			mMessageDataSource.open();
			mChatItemDataSource.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			if (mChatItemDataSource == null) {
				mChatItemDataSource = new ChatItemDataSource(getActivity());
				mChatItemDataSource.open();
			}
			mChatItems = new ArrayList<ChatItem>();
			mChatItems = mChatItemDataSource.getAllChatItems();
			if (mChatItems != null) {
				for (ChatItem chatItem : mChatItems) {
					chatItem.setLastMessage(mMessageDataSource
							.getLastMessageFrom(chatItem.id));
				}
			}

		} finally {
			if (mChatItems != null) {
				Collections.sort(mChatItems, new Comparator<ChatItem>() {
					@Override
					public int compare(ChatItem chatItem1, ChatItem chatItem2) {
						if (chatItem1.lastMessage != null
								&& chatItem2.lastMessage != null) {
							return -chatItem1.lastMessage.getCreatedAt()
									.compareTo(
											chatItem2.lastMessage
													.getCreatedAt());
						}
						return 0;
					}

				});

				if (mChatItems.size() == 0) {
					Log.e("DEBUG", "set empty view");
				} else {
					if (adapter == null) {
						adapter = new ChatItemsAdapter(getActivity(),
								mChatItems);
						setListAdapter(adapter);
					} else {
						adapter.refill(mChatItems);
					}
				}

			} else {
				// Show empty view
				mChatItems = new ArrayList<ChatItem>();
				adapter = new ChatItemsAdapter(getActivity(), mChatItems);
				setListAdapter(adapter);
			}

		}

		try {
			mMessageDataSource.close();
		} catch (Exception e) {

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

						updateChatListView();
					}
				}

				JSONObject jsonMessage = new JSONObject(jsonData);
				Log.e("chat list",
						"push received" + jsonMessage.getString("type"));
				if (jsonMessage.getString("type").equals("message")) {
					// Log.e("chat list","message push received");
					updateChatItem(Utils
							.createTextMessageFromJsonData(jsonMessage));
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
				Log.i("chat lis", "retrieved");
				if (e == null) {
					Log.d("Retrieved messages", "NO. :-" + pTextMessages.size());
					// hurray we received our messages
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
		if (!ChatContent.ITEM_MAP.containsKey(chatItem.id)) {
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

							if (e == null) {
								updateChatListView();
							} else {
								Log.e("To update pinned messages",
										" Error finding pinned messages "
												+ e.getMessage());
							}

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
