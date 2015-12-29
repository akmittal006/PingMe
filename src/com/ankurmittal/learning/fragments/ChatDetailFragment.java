package com.ankurmittal.learning.fragments;

import io.socket.client.Socket;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;
import android.widget.Toast;

import com.ankurmittal.learning.ChatDetailActivity;
import com.ankurmittal.learning.ChatListActivity;
import com.ankurmittal.learning.R;
import com.ankurmittal.learning.adapters.TextMessageAdapter;
import com.ankurmittal.learning.application.PingMeApplication;
import com.ankurmittal.learning.emojicon.EmojiconEditText;
import com.ankurmittal.learning.emojicon.EmojiconGridView.OnEmojiconClickedListener;
import com.ankurmittal.learning.emojicon.EmojiconsPopup;
import com.ankurmittal.learning.emojicon.EmojiconsPopup.OnEmojiconBackspaceClickedListener;
import com.ankurmittal.learning.emojicon.EmojiconsPopup.OnSoftKeyboardOpenCloseListener;
import com.ankurmittal.learning.emojicon.emoji.Emojicon;
import com.ankurmittal.learning.storage.ChatContent;
import com.ankurmittal.learning.storage.ChatItem;
import com.ankurmittal.learning.storage.ChatItemDataSource;
import com.ankurmittal.learning.storage.TextMessage;
import com.ankurmittal.learning.storage.TextMessageDataSource;
import com.ankurmittal.learning.util.Constants;
import com.ankurmittal.learning.util.ParseConstants;
import com.ankurmittal.learning.util.PushNotificationReceiver;
import com.ankurmittal.learning.util.Utils;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SendCallback;
import com.pubnub.api.Pubnub;

/**
 * A fragment representing a single Chat detail screen. This fragment is either
 * contained in a {@link ChatListActivity} in two-pane mode (on tablets) or a
 * {@link ChatDetailActivity} on handsets.
 */
public class ChatDetailFragment extends Fragment {
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";
	private TestInterface mCallbacks;

	/**
	 * The dummy content this fragment is presenting.
	 */
	private ChatItem mItem;
	private Button mSendButton;
	private EmojiconEditText mTextMessage;
	private TextView dateView;
	private TextMessageDataSource mMessageDataSource;
	private ListView chatView;
	private View rootView;
	Date prevDate = null;
	Date currDate = null;
	int mLastFirstVisibleItem;
	boolean mIsScrollingUp;
	ArrayList<Long> selectedIds;
	private ArrayList<String> toDeleteMessages;
	private ArrayList<TextMessage> notReadMessages;
	protected View emptyView;
	Pubnub pubnub;
	private Socket mSocket;

	String myChannelName;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ChatDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final PingMeApplication app = (PingMeApplication) getActivity().getApplicationContext();
		mSocket = app.mSocket;
		//((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(false);
		//Log.e("DEBUG", "" + ((AppCompatActivity)getActivity()).getSupportActionBar().isShowing());
		try {
			ChatItemDataSource mChatItemDataSource = new ChatItemDataSource(
					getActivity());
			mChatItemDataSource.open();
			mItem = mChatItemDataSource.getChatItemFromId(getArguments()
					.getString(ARG_ITEM_ID));
			if (ChatContent.ITEM_MAP.containsKey((getArguments()
					.getString(ARG_ITEM_ID)))) {
				ChatContent.ITEM_MAP.put(mItem.id, mItem);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (ChatContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID)) != null) {
			mItem = ChatContent.ITEM_MAP.get(getArguments().getString(
					ARG_ITEM_ID));
		}
		mMessageDataSource = new TextMessageDataSource(getActivity());
		notReadMessages = new ArrayList<TextMessage>();

		PushNotificationReceiver mReceiver = new PushNotificationReceiver();
		mCallbacks = (TestInterface) mReceiver;

		/*
		 * pubnub = new Pubnub("pub-c-72023601-94db-4a47-93e0-a1a111212e14",
		 * "sub-c-7728d700-3dd7-11e5-b53d-0619f8945a4f"); myChannelName =
		 * ParseUser.getCurrentUser().getObjectId(); try {
		 * pubnub.subscribe(myChannelName , new Callback() {
		 * 
		 * @Override public void connectCallback(String channel, Object message)
		 * { pubnub.publish(myChannelName, "Hello from the PubNub Java SDK", new
		 * Callback() {}); }
		 * 
		 * @Override public void disconnectCallback(String channel, Object
		 * message) { System.out.println("SUBSCRIBE : DISCONNECT on channel:" +
		 * channel + " : " + message.getClass() + " : " + message.toString()); }
		 * 
		 * public void reconnectCallback(String channel, Object message) {
		 * System.out.println("SUBSCRIBE : RECONNECT on channel:" + channel +
		 * " : " + message.getClass() + " : " + message.toString()); }
		 * 
		 * @Override public void successCallback(String channel, Object message)
		 * { System.out.println("SUBSCRIBE : " + channel + " : " +
		 * message.getClass() + " : " + message.toString()); }
		 * 
		 * @Override public void errorCallback(String channel, PubnubError
		 * error) { System.out.println("SUBSCRIBE : ERROR on channel " + channel
		 * + " : " + error.toString()); } } ); } catch (PubnubException e) {
		 * System.out.println(e.toString()); }
		 */
		
		Log.e("LAST ESCAPE DEBUG", ChatContent.ITEM_MAP.size() + "");

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_chat_detail, container,
				false);
		
		Log.e("DEBUG", "On create view called");
		// Show the dummy content as text in a TextView.
		if (mItem != null) {
			Log.e("DEBUG", "Chat item ");
			final ImageView emojiButton = (ImageView) rootView
					.findViewById(R.id.emojiBtn);
			
			chatView = (ListView) rootView.findViewById(R.id.chatListView);
			emptyView = (TextView) rootView.findViewById(android.R.id.empty);
	
			// Give the topmost view of your activity layout hierarchy. This
			// will be used to measure soft keyboard height
			final EmojiconsPopup popup = new EmojiconsPopup(rootView,
					getActivity());
	
			// Will automatically set size according to the soft keyboard size
			popup.setSizeForSoftKeyboard();
	
			// If the emoji popup is dismissed, change emojiButton to smiley
			// icon
			popup.setOnDismissListener(new OnDismissListener() {
	
				@Override
				public void onDismiss() {
					changeEmojiKeyboardIcon(emojiButton, R.drawable.smiley);
				}
	
			});
	
			// If the text keyboard closes, also dismiss the emoji popup
			popup.setOnSoftKeyboardOpenCloseListener(new OnSoftKeyboardOpenCloseListener() {
	
				@Override
				public void onKeyboardOpen(int keyBoardHeight) {
					chatView.setSelection(chatView.getAdapter().getCount() - 1);
				}
	
				@Override
				public void onKeyboardClose() {
					if (popup.isShowing())
						popup.dismiss();
				}
			});
	
			// On emoji clicked, add it to edittext
			popup.setOnEmojiconClickedListener(new OnEmojiconClickedListener() {
	
				@Override
				public void onEmojiconClicked(Emojicon emojicon) {
					if (mTextMessage == null || emojicon == null) {
						return;
					}
	
					int start = mTextMessage.getSelectionStart();
					int end = mTextMessage.getSelectionEnd();
					if (start < 0) {
						mTextMessage.append(emojicon.getEmoji());
					} else {
						mTextMessage.getText().replace(Math.min(start, end),
								Math.max(start, end), emojicon.getEmoji(), 0,
								emojicon.getEmoji().length());
					}
				}
			});
	
			// On backspace clicked, emulate the KEYCODE_DEL key event
			popup.setOnEmojiconBackspaceClickedListener(new OnEmojiconBackspaceClickedListener() {
	
				@Override
				public void onEmojiconBackspaceClicked(View v) {
					KeyEvent event = new KeyEvent(0, 0, 0,
							KeyEvent.KEYCODE_DEL, 0, 0, 0, 0,
							KeyEvent.KEYCODE_ENDCALL);
					mTextMessage.dispatchKeyEvent(event);
				}
			});
	
			// To toggle between text keyboard and emoji keyboard
			// keyboard(Popup)
			emojiButton.setOnClickListener(new OnClickListener() {
	
				@Override
				public void onClick(View v) {
					Log.e("DEBUG", "on clicked");
					// If popup is not showing => emoji keyboard is not visible,
					// we need to show it
					if (!popup.isShowing()) {
	
						// If keyboard is visible, simply show the emoji popup
						if (popup.isKeyBoardOpen()) {
							popup.showAtBottom();
							changeEmojiKeyboardIcon(emojiButton,
									R.drawable.ic_action_keyboard);
						}
	
						// else, open the text keyboard first and immediately
						// after that show the emoji popup
						else {
							mTextMessage.setFocusableInTouchMode(true);
							mTextMessage.requestFocus();
							popup.showAtBottomPending();
							final InputMethodManager inputMethodManager = (InputMethodManager) getActivity()
									.getSystemService(
											Context.INPUT_METHOD_SERVICE);
							inputMethodManager.showSoftInput(mTextMessage,
									InputMethodManager.SHOW_IMPLICIT);
							changeEmojiKeyboardIcon(emojiButton,
									R.drawable.ic_action_keyboard);
						}
					}
	
					// If popup is showing, simply dismiss it to show the
					// undelying text keyboard
					else {
						popup.dismiss();
					}
				}
			});

			chatView.setDivider(null);
			chatView.setDividerHeight(0);
			chatView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
			chatView.setMultiChoiceModeListener(new ListView.MultiChoiceModeListener() {
	
				@Override
				public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
					Log.i("Prep called", "called");
	
					return true;
				}
	
				@Override
				public void onDestroyActionMode(ActionMode mode) {
	
				}
	
				@Override
				public boolean onCreateActionMode(ActionMode mode, Menu menu) {
					selectedIds = new ArrayList<Long>();
					Log.i("create called", "called");
					MenuInflater inflater = getActivity().getMenuInflater();
					inflater.inflate(R.menu.list_select_menu, menu);
					mode.setTitle("Select Messages");
					return true;
				}
	
				@Override
				public boolean onActionItemClicked(ActionMode mode,
						MenuItem item) {
					Log.i("called", "called");
					if (selectedIds.size() > 0) {
						toDeleteMessages = new ArrayList<String>();
						if (item.getItemId() == R.id.delete) {
							for (long i : selectedIds) {
								int j = (int) i;
								toDeleteMessages.add(mItem.getMessage(j)
										.getMessageId());
								Log.i("to delete", mItem.getMessage(j)
										.getMessageId()
										+ " "
										+ toDeleteMessages.size());
							}
							deleteMessages(toDeleteMessages);
	
						} else if (item.getItemId() == R.id.copy) {
							String copyString = "";
							for (long i : selectedIds) {
								int j = (int) i;
								copyString += " "
										+ mItem.getMessage(j).getMessage();
							}
							Log.i("copy success", "" + copyString);
							ClipboardManager clipboard = (ClipboardManager) getActivity()
									.getSystemService(Context.CLIPBOARD_SERVICE);
							ClipData clip = ClipData.newPlainText("copyString",
									copyString);
							clipboard.setPrimaryClip(clip);
						}
	
					}
					mode.finish();
	
					return true;
				}
	
				@Override
				public void onItemCheckedStateChanged(ActionMode mode,
						int position, long id, boolean checked) {
					final int checkedCount = chatView.getCheckedItemCount();
					switch (checkedCount) {
					case 0:
						mode.setSubtitle(null);
						break;
					case 1:
						mode.setSubtitle("1 message selected");
						break;
					default:
						mode.setSubtitle("" + checkedCount
								+ " messages selected");
						break;
					}
					if (checked) {
						selectedIds.add(id);
					} else {
						selectedIds.remove(id);
					}
	
					Log.i("" + id, "something checked " + selectedIds.size());
				}
			});
	
			chatView.setAdapter(new TextMessageAdapter(getActivity(), mItem
					.getItemMessages()));
			//((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(mItem.toString());
			dateView = (TextView) rootView.findViewById(R.id.dateTextView);
			mTextMessage = (EmojiconEditText) rootView
					.findViewById(R.id.chatEditText);
			mTextMessage.requestFocus(0);
			chatView.setSelection(chatView.getAdapter().getCount() - 1);
			mSendButton = (Button) rootView.findViewById(R.id.sendButton);
			// if send button is clicked , save new message
	
			mSendButton.setOnClickListener(new OnClickListener() {
				String newTextMessage;
				ParseObject pTextMessage;
				TextMessage message;
	
				@Override
				public void onClick(View arg0) {
					emptyView.setVisibility(View.GONE);
					newTextMessage = mTextMessage.getText().toString();
					if (newTextMessage.equals("")) {
						mTextMessage.setError(getString(R.string.empty_text));
					} else {
						mTextMessage.setText("");
						// saving new message at backend
						pTextMessage = new ParseObject(
								ParseConstants.TEXT_MESSAGE);
						pTextMessage.put(ParseConstants.KEY_MESSAGE,
								newTextMessage);
						pTextMessage.put(ParseConstants.KEY_MESSAGE_SENDER,
								ParseUser.getCurrentUser());
						pTextMessage.put(
								ParseConstants.KEY_MESSAGE_RECEIVER_ID,
								getArguments().getString(ARG_ITEM_ID));
						pTextMessage.put(
								ParseConstants.KEY_MESSAGE_RECEIVER_NAME,
								mItem.content);
						pTextMessage.put("isSent",
								Constants.MESSAGE_STATUS_PENDING);
						pTextMessage.pinInBackground(Constants.GROUP_NOT_SENT,
								new SaveCallback() {
									@Override
									public void done(ParseException e) {
	
										if (e == null) {
											Log.i("written msg pinned",
													"pinned");
											message = new TextMessage();
											message = Utils
													.createTextMessage(pTextMessage);
											if (mMessageDataSource.isOpen()) {
												mMessageDataSource
														.insert(message);
											} else {
												mMessageDataSource.open();
												mMessageDataSource
														.insert(message);
											}
	
											// add to chat item
											String id = mItem.id;
											if (ChatContent.ITEM_MAP
													.containsKey(id)) {
												Log.e("DEBUG",
														"AFTER CLICKING TRUE"
																+ ChatContent.ITEM_MAP
																		.size());
												ChatItem chatItem = ChatContent.ITEM_MAP
														.get(id);
												maintainDate(message, chatItem);
												Log.e("DEBUG",
														""
																+ chatItem.mMessages
																		.size());
												chatItem.addMessage(message);
												mItem = chatItem;
											} else {
												Log.d("ChatDetail",
														"this shud not b called");
												Log.e("DEBUG",
														"AFTER CLICKING False"
																+ ChatContent.ITEM_MAP
																		.size());
												ChatItem chatItem = new ChatItem(
														id,
														ParseUser
																.getCurrentUser()
																.getUsername());
												maintainDate(message, chatItem);
												chatItem.addMessage(message);
												mItem = chatItem;
											}
											Log.e("DEBUG", ""
													+ mItem.getItemMessages()
															.size());
											chatView.setAdapter(new TextMessageAdapter(
													getActivity(), mItem
															.getItemMessages()));
											chatView.setSelection(chatView
													.getAdapter().getCount() - 1);
	
										} else {
	
										}
										Log.i("sys check",
												"calling sync msgs to parse method");
										syncMsgsToParse();
									}
								});
	
					}
				}
			});
		} else {
			Log.e("CHAT DEtail", "Chat item null");
			// if(chatView)
		}
		if (chatView != null) {
			setScrollListener();
		}
	
		return rootView;
	}

	private BroadcastReceiver notificationMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			try {
				ParseAnalytics.trackAppOpenedInBackground(getActivity()
						.getIntent());
				// Extract data included in the Intent
				String jsonData = intent.getStringExtra(Constants.JSON_MESSAGE);

				if (jsonData.equals("refresh")) {
					// callback from notification receiver after sending push to
					// refresh
					loadChatItemMessagesFromDatabase();
				}

				JSONObject jsonMessage = new JSONObject(jsonData);
				if (jsonMessage.getString("type").equals("message")) {
					// Log.d("chat list", "message push received");
				} else if (jsonMessage.getString("type").equals("message")) {
					// Log.e("chat list", "update  push received");
				}
				// Log.d("chat detail Activity", "hurray updating activity");
				loadChatItemMessagesFromDatabase();
			} catch (Exception e) {
				Log.i("chat detail error", "error while receiving notification");
			}
			// do other stuff here
		}
	};

	private BroadcastReceiver notificationCheckReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			try {
				ParseAnalytics.trackAppOpenedInBackground(getActivity()
						.getIntent());
				String id = intent.getStringExtra(Constants.JSON_MESSAGE_ID);
				// Send callback to notification receiver
				mCallbacks = (TestInterface) new PushNotificationReceiver();
				mCallbacks.callbackCall(id, getActivity());

			} catch (Exception e) {
				Log.e("chat detail error",
						"error while receiving notification check "
								+ e.getMessage());
			}
			// do other stuff here
		}
	};

	@Override
	public void onPause() {
		getActivity().unregisterReceiver(notificationMessageReceiver);
		getActivity().unregisterReceiver(notificationCheckReceiver);
		mMessageDataSource.close();
		super.onPause();
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	@Override
	public void onResume() {

		if (getArguments().containsKey(ARG_ITEM_ID)) {
			// Load the dummy content specified by the fragment
			// arguments. In a real-world scenario, use a Loader
			// to load content from a content provider.
			// Log.e("DEBUG FOR ARGUMENTS TRUE","" +
			// ChatContent.ITEM_MAP.get(getArguments().getString(
			// ARG_ITEM_ID)).id);
			try {
				ChatItemDataSource mChatItemDataSource = new ChatItemDataSource(
						getActivity());
				mChatItemDataSource.open();
				mItem = mChatItemDataSource.getChatItemFromId(getArguments()
						.getString(ARG_ITEM_ID));
				Log.e("DEUBUG IN ON RESUME", "initail size- "
						+ ChatContent.ITEM_MAP.size());
				if (ChatContent.ITEM_MAP.containsKey((getArguments()
						.getString(ARG_ITEM_ID)))) {

					ChatContent.ITEM_MAP.put(mItem.id, mItem);

					Log.e("DEUBUG IN ON RESUME", "finall size- "
							+ ChatContent.ITEM_MAP.size());
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			if (ChatContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID)) != null) {
				mItem = ChatContent.ITEM_MAP.get(getArguments().getString(
						ARG_ITEM_ID));
			}

		} else {
			Log.e("DEBUG FOR ARGUMENTS FALSE", "" + ChatContent.ITEM_MAP.size());
		}
		super.onResume();
		// open database connection
		getActivity().registerReceiver(notificationMessageReceiver,
				new IntentFilter(Constants.PUSH_TO_CHAT));

		getActivity().registerReceiver(notificationCheckReceiver,
				new IntentFilter(Constants.PUSH_TO_CHECK));
		mMessageDataSource.open();
		loadChatItemMessagesFromDatabase();
		updateReadMessages();
		syncMsgsToParse();
	}

	private void setScrollListener() {
		chatView.setOnScrollListener(new OnScrollListener() {
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				final AbsListView lw = chatView;
				if (view.getId() == lw.getId()
						&& mItem.getItemMessages().size() > 0) {
					if (mItem.getMessage(firstVisibleItem) != null) {
						dateView.setText(Utils.getDateString(mItem.getMessage(
								firstVisibleItem).getCreatedAt()));
					}
				}
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == SCROLL_STATE_IDLE) {
					dateView.setVisibility(View.INVISIBLE);
				} else if (scrollState == SCROLL_STATE_TOUCH_SCROLL) {
					dateView.setVisibility(View.VISIBLE);
				}

			}

		});
	}

	private void changeEmojiKeyboardIcon(ImageView emojiButton,
			int drawableResourceId) {
		// TODO Auto-generated method stub
		emojiButton.setImageResource(drawableResourceId);
	}

	private void loadChatItemMessagesFromDatabase() {
		Log.e("DEBUG", "" + mItem.id);
		// ArrayList<TextMessage> chatItemMessages ;
		if (mMessageDataSource.getMessagesFrom(mItem.id) != null) {
			String id = mItem.id; // equivalent to item id
			ArrayList<TextMessage> allMessages = mMessageDataSource
					.getMessagesFrom(mItem.id);

			if (ChatContent.ITEM_MAP.containsKey(mItem.id)) {
				// clear all prev msgs
				ChatItem chatItem = ChatContent.ITEM_MAP.get(mItem.id);
				chatItem.clearMessages();
			}
			for (TextMessage message : allMessages) {

				if (ChatContent.ITEM_MAP.containsKey(id)) {
					// messages from that sender exist

					ChatItem chatItem = ChatContent.ITEM_MAP.get(id);
					Log.d("message sender name", message.getSenderName());
					maintainDate(message, chatItem);
					chatItem.addMessage(message);
					// Log.d("IMPPP chat frag",
					// "msg added " + message.getCreatedAtString());
				} else {
					// new chat item is created
					ChatItem chatItem = new ChatItem(id,
							message.getSenderName());
					Log.d("chat frag",
							"new chat item created :" + message.getSenderName());
					maintainDate(message, chatItem);
					chatItem.addMessage(message);
					ChatContent.addItem(chatItem);
					// Log.d("IMPPP chat frag", "msg added");
				}
			}
			if (ChatContent.ITEM_MAP.containsKey(id)) {
				// messages from that sender exist
				// ChatItem chatItem = ChatContent.ITEM_MAP.get(id);
				// // addNotSentMessages(chatItem);
				// // Log.i("added", "not sent messages");
			}
		} else {
			TextView emptyView = (TextView) rootView
					.findViewById(android.R.id.empty);
			emptyView.setVisibility(View.VISIBLE);
		}

		Log.i("error check", mItem.getItemMessages().size() + " "
				+ getActivity().toString());

		chatView.setAdapter(new TextMessageAdapter(getActivity(), mItem
				.getItemMessages()));
		chatView.setSelection(chatView.getAdapter().getCount() - 1);
	}

	private void maintainDate(TextMessage pTextMessage, ChatItem chatItem) {

		prevDate = currDate;
		currDate = getDate(pTextMessage.getCreatedAt());
		if (prevDate == null) {
			TextMessage dateMessage = Utils.createNeutralMessage(currDate,
					pTextMessage);
			chatItem.addMessage(dateMessage);
		} else {
			if (currDate.after(prevDate)) {
				TextMessage dateMessage = Utils.createNeutralMessage(currDate,
						pTextMessage);
				chatItem.addMessage(dateMessage);
			}
		}
	}

	private Date getDate(Date date) {
		Date resDate = null;
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd",
					Locale.getDefault());
			// Date date = new Date();
			String strDate = dateFormat.format(date);
			resDate = dateFormat.parse(strDate);

		} catch (java.text.ParseException e) {
			Log.e("chat list", "invalid format in getDate()");
			e.printStackTrace();
		}
		return resDate;
	}

	private void syncMsgsToParse() {
		ConnectivityManager cm = (ConnectivityManager) getActivity()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if ((ni != null) && (ni.isConnected())) {
			if (!ParseAnonymousUtils.isLinked(ParseUser.getCurrentUser())) {
				// If we have a network connection and a current
				// logged in user, sync the todos
				ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(
						ParseConstants.TEXT_MESSAGE);
				query.fromPin(Constants.GROUP_NOT_SENT);
				query.whereEqualTo("isSent", Constants.MESSAGE_STATUS_PENDING);
				query.findInBackground(new FindCallback<ParseObject>() {
					public void done(List<ParseObject> messages,
							ParseException e) {
						if (e == null) {
							Log.i("pending msgs", "" + messages.size());
							for (final ParseObject message : messages) {
								// Set is draft flag to false before
								// syncing to Parse

								message.saveInBackground(new SaveCallback() {

									@Override
									public void done(ParseException e) {
										if (e == null) {
											// message sent
											message.put(
													"isSent",
													Constants.MESSAGE_STATUS_SENT);
											mMessageDataSource.updatePendingMessage(
													message.getString(ParseConstants.KEY_MESSAGE),
													message.getObjectId());
											prevDate = null;
											currDate = null;
											Log.i("sys check ", "loading...");
											if (getActivity() != null) {
												loadChatItemMessagesFromDatabase();
											}

											// unpin the message
											message.unpinInBackground(Constants.GROUP_NOT_SENT);
											sendNotification(message);
										} else {
											// Reset the is draft flag locally
											// to true
											message.put(
													"isSent",
													Constants.MESSAGE_STATUS_PENDING);
										}
									}

								});

							}
						} else {
							Log.i("TodoListActivity",
									"syncTodosToParse: Error finding pinned todos: "
											+ e.getMessage());
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

	private void addNotSentMessages(final ChatItem item) {
		ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(
				ParseConstants.TEXT_MESSAGE);
		query.fromPin(Constants.GROUP_NOT_SENT);
		query.whereEqualTo("isSent", Constants.MESSAGE_STATUS_PENDING);
		query.whereEqualTo(ParseConstants.KEY_MESSAGE_RECEIVER_ID, item.id);
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> messages, ParseException e) {
				if (e == null) {
					Log.i("not sent ", "" + messages.size());
					for (ParseObject message : messages) {
						item.addMessage(Utils.createTextMessage(message));
					}
					if (!getActivity().isFinishing()) {
						chatView.setAdapter(new TextMessageAdapter(
								getActivity(), mItem.getItemMessages()));
						chatView.setSelection(chatView.getAdapter().getCount() - 1);
					}

				}
			}
		});
	}

	protected void sendNotification(final ParseObject message) {
		ParseQuery<ParseInstallation> query = ParseInstallation.getQuery();
		query.whereEqualTo(ParseConstants.KEY_USER_ID,
				message.getString(ParseConstants.KEY_MESSAGE_RECEIVER_ID));

		ParsePush push = new ParsePush();

		push.setQuery(query);
		push.setMessage("" + message.getString(ParseConstants.KEY_SENDER_NAME)
				+ ": " + message.getString(ParseConstants.KEY_MESSAGE));
		
		push.setData(Utils.createJSONObject(message));
		push.sendInBackground(new SendCallback() {

			@Override
			public void done(ParseException arg0) {
				// msg sent!
				Log.e("Message Push sent",
						"hurray + " + message.getString("isSent"));
			}
		});
	}

	private void deleteMessages(ArrayList<String> ids) {
		for (String id : ids) {
			mMessageDataSource.deleteMessage(id);
		}
		loadChatItemMessagesFromDatabase();
	}

	private void updateReadMessages() {
		Log.i("chat detail", "update read message");

		ConnectivityManager cm = (ConnectivityManager) getActivity()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if ((ni != null) && (ni.isConnected())) {
			if (!ParseAnonymousUtils.isLinked(ParseUser.getCurrentUser())) {
				ArrayList<TextMessage> messages = mItem.getNotReadMessages();
				if (messages.size() > 0) {
					Log.i("pinned to update read msgs", "" + messages.size());

					// create cloud function parameter which maps not read
					// messages to thier id
					final HashMap<String, String> params = new HashMap<String, String>();
					int i = 0;
					for (final TextMessage message : messages) {
						params.put(message.getMessageId(),
								Constants.MESSAGE_STATUS_READ);
						i++;

					}

					// call cloud function
					if (i == messages.size()) {
						Log.i("update read msgs calling cloud with params ",
								"now + params : " + params.size());
						ParseCloud.callFunctionInBackground("updateMessages",
								params, new FunctionCallback<String>() {

									@Override
									public void done(String arg0,
											ParseException e) {
										// TODO Auto-generated method
										// stub
										if (e == null) {
											Log.i("Read Msgs update Cloud code2",
													"Yay it worked! " + arg0);
											// set Read Message status
											int h = 0;
											for (TextMessage message : mItem
													.getNotReadMessages()) {
												int updated = mMessageDataSource.updateMessageStatus(
														message.getMessageId(),
														Constants.MESSAGE_STATUS_READ);
												Log.e("chat detail",
														" updated to chat read MEssage "
																+ updated);
												h++;
											}
											if (h == mItem.getNotReadMessages()
													.size()) {
												mItem.getNotReadMessages()
														.clear();
											}
										} else {
											Log.i("Read Msgs update Cloud Code2",
													"Some error"
															+ e.getMessage());
										}
									}
								});

						/*
						 * pubnub.publish(mItem.id, "messages seen", new
						 * Callback() {
						 * 
						 * @Override public void connectCallback(String channel,
						 * Object message) { pubnub.publish(myChannelName,
						 * "Hello from the other user", new Callback() { }); }
						 * 
						 * @Override public void disconnectCallback( String
						 * channel, Object message) { Log.e("pubnub",
						 * "SUBSCRIBE 2 : DISCONNECT on channel:" + channel +
						 * " : " + message.getClass() + " : " +
						 * message.toString()); }
						 * 
						 * public void reconnectCallback( String channel, Object
						 * message) { Log.e("pubnub",
						 * "SUBSCRIBE 2 : RECONNECT on channel:" + channel +
						 * " : " + message.getClass() + " : " +
						 * message.toString()); }
						 * 
						 * @Override public void successCallback(String channel,
						 * Object message) { Log.e("pubnub",
						 * "SUBSCRIBE SUCCESS : " + channel + " : " +
						 * message.getClass() + " : " + message.toString()); }
						 * 
						 * @Override public void errorCallback(String channel,
						 * PubnubError error) { Log.e("pubnub",
						 * "SUBSCRIBE : ERROR on channel " + channel + " : " +
						 * error.toString()); }
						 * 
						 * });
						 */
					}

				} else {
					// Log.i("To update pinned messages",
					// " Error finding pinned messages "
					// + e.getMessage());
				}
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

	public static interface TestInterface {
		void callbackCall(String id, Context context);
	}

}
