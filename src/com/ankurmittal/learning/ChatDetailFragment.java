package com.ankurmittal.learning;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONObject;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ankurmittal.learning.adapters.TextMessageAdapter;
import com.ankurmittal.learning.storage.ChatContent;
import com.ankurmittal.learning.storage.ChatItem;
import com.ankurmittal.learning.storage.TextMessage;
import com.ankurmittal.learning.storage.TextMessageDataSource;
import com.ankurmittal.learning.util.Constants;
import com.ankurmittal.learning.util.ParseConstants;
import com.parse.FindCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SendCallback;

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

	/**
	 * The dummy content this fragment is presenting.
	 */
	private ChatItem mItem;
	private Button mSendButton;
	private EditText mTextMessage;
	TextView dateView;
	private TextMessageDataSource mMessageDataSource;
	ListView chatView;
	private View rootView;
	Date prevDate = null;
	Date currDate = null;
	int mLastFirstVisibleItem;
	boolean mIsScrollingUp;
	ArrayList<Long> selectedIds;
	private ArrayList<String> toDeleteMessages;
	protected View emptyView;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ChatDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActivity().getActionBar().setDisplayShowHomeEnabled(false);

		if (getArguments().containsKey(ARG_ITEM_ID)) {
			// Load the dummy content specified by the fragment
			// arguments. In a real-world scenario, use a Loader
			// to load content from a content provider.
			mItem = ChatContent.ITEM_MAP.get(getArguments().getString(
					ARG_ITEM_ID));

		}
		mMessageDataSource = new TextMessageDataSource(getActivity());
	}
	
	private BroadcastReceiver notificationMessageReceiver = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	
	    	try {
	    		ParseAnalytics.trackAppOpenedInBackground(getActivity().getIntent());
	    		   // Extract data included in the Intent
		        String jsonData = intent.getStringExtra(Constants.JSON_MESSAGE);
		        JSONObject jsonMessage = new JSONObject(jsonData);
		        if(jsonMessage.getString("type").equals("message")) {
		        	Log.d("chat list","message push received");
		        } else if (jsonMessage.getString("type").equals("message")) {
		        	Log.e("chat list","update  push received");
		        }
		        Log.d("chat detail Activity", "hurray updating activity");
		        loadChatItemMessagesFromDatabase();
	    	} catch (Exception e) {
	    		Log.i("chat detail error", "error while receiving notification");
	    	}
	        //do other stuff here
	    }
	};

	@Override
	public void onPause() {
		getActivity().unregisterReceiver(notificationMessageReceiver);
		mMessageDataSource.close();
		super.onPause();
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	@Override
	public void onResume() {
		super.onResume();
		// open database connection
		getActivity().registerReceiver(notificationMessageReceiver, new IntentFilter(Constants.PUSH_TO_CHAT));
		mMessageDataSource.open();
		loadChatItemMessagesFromDatabase();
		syncMsgsToParse();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_chat_detail, container,
				false);

		// Show the dummy content as text in a TextView.
		if (mItem != null) {
			// ((EditText) rootView.findViewById(R.id.chatEditText))
			// .setText(mItem.content);
			chatView = (ListView) rootView.findViewById(R.id.chatListView);
			emptyView = (TextView) rootView.findViewById(android.R.id.empty);
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
					if(selectedIds.size() >0) {
						toDeleteMessages = new ArrayList<String>();
						if(item.getItemId() == R.id.delete) {
							for(long i : selectedIds) {
								int j = (int) i;
								toDeleteMessages.add(mItem.getMessage(j).getMessageId());
								Log.i("to delete", mItem.getMessage(j).getMessageId() + " " + toDeleteMessages.size());
							}
							deleteMessages(toDeleteMessages);
							
					} else if(item.getItemId() == R.id.copy) {
						String copyString = "";
						for(long i : selectedIds) {
							int j = (int) i;
							copyString += " " + mItem.getMessage(j).getMessage();
						}
						Log.i("copy success", "" + copyString);
						ClipboardManager clipboard = (ClipboardManager)
						        getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
						ClipData clip = ClipData.newPlainText("copyString", copyString);
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
		                    mode.setSubtitle("" + checkedCount + " messages selected");
		                    break;
		            }
		            if(checked) {
		            	selectedIds.add(id);
		            }else {
		            	selectedIds.remove(id);
		            }
		            

					Log.i("" + id, "something checked " + selectedIds.size());
				}
			});

			chatView.setAdapter(new TextMessageAdapter(getActivity(), mItem
					.getItemMessages()));
			getActivity().setTitle(mItem.toString());
			dateView = (TextView) rootView.findViewById(R.id.dateTextView);
			mTextMessage = (EditText) rootView.findViewById(R.id.chatEditText);
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
//						Log.i("chehck ",
//								pTextMessage
//										.getString(ParseConstants.KEY_MESSAGE_RECEIVER_NAME));
						pTextMessage.put("isSent", Constants.MESSAGE_STATUS_PENDING);
						pTextMessage.pinInBackground(Constants.GROUP_NOT_SENT, new SaveCallback() {
							@Override
							public void done(ParseException e) {

								if (e == null) {
									Log.i("written msg pinned", "pinned");
									message = new TextMessage();
									message.setMessage(newTextMessage);
									message.setMessageId(pTextMessage
											.getObjectId());
									message.setSenderId(ParseUser
											.getCurrentUser().getObjectId());
									message.setSenderName(ParseUser
											.getCurrentUser().getUsername());
									message.setReceiverId(mItem.id);
									message.setReceiverName(mItem.content);
									Log.d("check", mItem.content);
									message.setCreatedAt(new Date());
									message.setMessageStatus(Constants.MESSAGE_STATUS_PENDING);

									// add to chat item
									String id = mItem.id;
									if (ChatContent.ITEM_MAP.containsKey(id)) {
										ChatItem chatItem = ChatContent.ITEM_MAP
												.get(id);
										maintainDate(message, chatItem);
										chatItem.addMessage(message);
									} else {
										Log.d("ChatDetail",
												"this shud not b called");
										ChatItem chatItem = new ChatItem(id,
												ParseUser.getCurrentUser()
														.getUsername());
										maintainDate(message, chatItem);
										chatItem.addMessage(message);
									}
									chatView.setAdapter(new TextMessageAdapter(
											getActivity(), mItem
													.getItemMessages()));
									chatView.setSelection(chatView.getAdapter()
											.getCount() - 1);

								} else {

								}
								Log.i("sys check", "calling sync msgs to parse method");
								syncMsgsToParse();
							}
						});
						

					}
				}
			});
		}
		chatView.setOnScrollListener(new OnScrollListener() {
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				final AbsListView lw = chatView;

				if (view.getId() == lw.getId()
						&& mItem.getItemMessages().size() > 0) {
					if (mItem.getMessage(firstVisibleItem) != null) {
						dateView.setText(getDateString(mItem.getMessage(
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

		return rootView;
	}

	private void loadChatItemMessagesFromDatabase() {
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

				Log.d("chat frag", "" + id);
				if (ChatContent.ITEM_MAP.containsKey(id)) {
					// messages from that sender exist
					ChatItem chatItem = ChatContent.ITEM_MAP.get(id);
					Log.d("message sender name", message.getSenderName());
					maintainDate(message, chatItem);
					chatItem.addMessage(message);
					Log.d("IMPPP chat frag",
							"msg added " + message.getCreatedAtString());
				} else {
					// new chat item is created
					ChatItem chatItem = new ChatItem(id,
							message.getSenderName());
					Log.d("chat frag",
							"new chat item created :" + message.getSenderName());
					maintainDate(message, chatItem);
					chatItem.addMessage(message);
					ChatContent.addItem(chatItem);
					Log.d("IMPPP chat frag", "msg added");
				}
			}
			if (ChatContent.ITEM_MAP.containsKey(id)) {
				// messages from that sender exist
				ChatItem chatItem = ChatContent.ITEM_MAP.get(id);
				addNotSentMessages(chatItem);
				Log.i("added", "not sent messages");
			}
		} else {
			TextView emptyView = (TextView) rootView
					.findViewById(android.R.id.empty);
			emptyView.setVisibility(View.VISIBLE);
		}
		
		Log.i("error check", mItem
				.getItemMessages().size() + " " +  getActivity().toString());

		chatView.setAdapter(new TextMessageAdapter(getActivity(), mItem
				.getItemMessages()));
		chatView.setSelection(chatView.getAdapter().getCount() - 1);
	}

	private void maintainDate(TextMessage pTextMessage, ChatItem chatItem) {

		prevDate = currDate;
		currDate = getDate(pTextMessage.getCreatedAt());
		if (prevDate == null) {
			TextMessage dateMessage = createNeutralMessage(currDate,
					pTextMessage);
			chatItem.addMessage(dateMessage);
		} else {
			if (currDate.after(prevDate)) {
				TextMessage dateMessage = createNeutralMessage(currDate,
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

	private String getDateString(Date date) {
		String strDate = null;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		strDate = dateFormat.format(date);
		return strDate;
	}

	private TextMessage createNeutralMessage(Date currDate,
			TextMessage pTextMessage) {
		TextMessage textMessage = new TextMessage();
		textMessage.setMessage(getDateString(currDate));
		Log.d("list frag ", " " + pTextMessage.getMessage());
		textMessage.setMessageId(currDate.toString());
		textMessage.setReceiverId(pTextMessage.getReceiverId());
		textMessage.setReceiverName("pingMe");
		textMessage.setSenderId(pTextMessage.getSenderId());
		textMessage.setSenderName(pTextMessage.getSenderName());
		textMessage.setCreatedAt(currDate);
		textMessage.setType(Constants.TYPE_NEUTRAL);
		return textMessage;
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
				// query.whereEqualTo("isSent", false);
				query.findInBackground(new FindCallback<ParseObject>() {
					public void done(List<ParseObject> messages,
							ParseException e) {
						if (e == null) {
							Log.i("pinned msgs", "" + messages.size());
							for (final ParseObject message : messages) {
								// Set is draft flag to false before
								// syncing to Parse

								message.saveInBackground(new SaveCallback() {

									@Override
									public void done(ParseException e) {
										if (e == null) {
											// message sent
											message.put("isSent", Constants.MESSAGE_STATUS_SENT);
											Log.i("saving",
													""
															+ message
																	.getBoolean("isSent"));
											Log.i("sys check ", "inserting...") ;
											mMessageDataSource
													.insert(createTextMessage(message));
											prevDate = null;
											currDate = null;
											Log.i("sys check ", "loading...") ;
											if(getActivity() != null) {
												loadChatItemMessagesFromDatabase();
											}
											
											// unpin the message
											message.unpinInBackground(Constants.GROUP_NOT_SENT);
											sendNotification(message);
										} else {
											// Reset the is draft flag locally
											// to true
											message.put("isSent", Constants.MESSAGE_STATUS_PENDING);
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

	private TextMessage createTextMessage(ParseObject pTextMessage) {
		TextMessage textMessage = new TextMessage();
		textMessage.setMessage(pTextMessage
				.getString(ParseConstants.KEY_MESSAGE));
		Log.d("detail frag ",
				" "
						+ pTextMessage.getString(ParseConstants.KEY_MESSAGE)
						+ ": "
						+ pTextMessage
								.getString(ParseConstants.KEY_MESSAGE_RECEIVER_NAME)
						+ ", " + pTextMessage.getString("isSent"));
		textMessage.setMessageId(pTextMessage.getObjectId());
		textMessage.setReceiverId(pTextMessage
				.getString(ParseConstants.KEY_MESSAGE_RECEIVER_ID));
		textMessage.setReceiverName(pTextMessage
				.getString(ParseConstants.KEY_MESSAGE_RECEIVER_NAME));
		textMessage.setSenderId(pTextMessage.getParseUser(
				ParseConstants.KEY_MESSAGE_SENDER).getObjectId());
		textMessage.setSenderName(pTextMessage.getParseUser(
				ParseConstants.KEY_MESSAGE_SENDER).getUsername());
		if (pTextMessage.getCreatedAt() != null) {
			textMessage.setCreatedAt(getDateTime(pTextMessage.getCreatedAt()));
		} else {
			textMessage.setCreatedAt(new Date());
		}
		if (textMessage.getSenderId() == ParseUser.getCurrentUser()
				.getObjectId()) {
			textMessage.setType(Constants.TYPE_SENT);
		} else {
			textMessage.setType(Constants.TYPE_RECEIVED);
		}

		textMessage.setMessageStatus(pTextMessage.getString("isSent"));

		return textMessage;
	}

	private String getDateTime(java.util.Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		// Date date = new Date();
		return dateFormat.format(date);
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
						item.addMessage(createTextMessage(message));
					}
					if(!getActivity().isFinishing()) {
						chatView.setAdapter(new TextMessageAdapter(getActivity(),
								mItem.getItemMessages()));
						chatView.setSelection(chatView.getAdapter().getCount() - 1);
					}
					
				}
			}
		});
	}

	protected void sendNotification(ParseObject message) {
		ParseQuery<ParseInstallation> query = ParseInstallation.getQuery();
		query.whereEqualTo(ParseConstants.KEY_USER_ID,
				message.getString(ParseConstants.KEY_MESSAGE_RECEIVER_ID));

		ParsePush push = new ParsePush();
		
		push.setQuery(query);
		push.setMessage("" + message.getString(ParseConstants.KEY_SENDER_NAME)
				+ ": " + message.getString(ParseConstants.KEY_MESSAGE));
		push.setData(createJSONObject(message));
		push.sendInBackground(new SendCallback() {

			@Override
			public void done(ParseException arg0) {
				// msg sent!
				Log.i("Message Push sent", "hurray");
			}
		});
	}

	protected JSONObject createJSONObject(ParseObject message) {
		JSONObject jsonMessage = new JSONObject();
		try {
			jsonMessage.put(
					"alert",
					message.getString(ParseConstants.KEY_MESSAGE_RECEIVER_NAME)
							+ ": "
							+ message.getString(ParseConstants.KEY_MESSAGE));
			jsonMessage.put(ParseConstants.KEY_MESSAGE,
					message.getString(ParseConstants.KEY_MESSAGE));
			jsonMessage.put(ParseConstants.KEY_MESSAGE_ID,
					message.getObjectId());
			jsonMessage.put(ParseConstants.KEY_SENDER_NAME, message
					.getParseUser(ParseConstants.KEY_MESSAGE_SENDER)
					.getUsername());
			jsonMessage.put(ParseConstants.KEY_SENDER_ID,
					message.getParseUser(ParseConstants.KEY_MESSAGE_SENDER)
							.getObjectId());
			jsonMessage.put(ParseConstants.KEY_MESSAGE_RECEIVER_ID,
					message.getString(ParseConstants.KEY_MESSAGE_RECEIVER_ID));
			jsonMessage
					.put(ParseConstants.KEY_MESSAGE_RECEIVER_NAME,
							message.getString(ParseConstants.KEY_MESSAGE_RECEIVER_NAME));
			jsonMessage.put("isSent", message.getString("isSent"));
			jsonMessage.put(ParseConstants.KEY_CREATED_AT,
					getDateTime(message.getCreatedAt()));
			jsonMessage.put("type", "message");

			Log.d("Json message", jsonMessage.toString());
			return jsonMessage;
		} catch (Exception e) {
			Log.e("JSON ERROR", "error creating message");
		}
		return null;
	}
	private void deleteMessages(ArrayList<String> ids) {
		for(String id : ids) {
			mMessageDataSource.deleteMessage(id);
		}
		loadChatItemMessagesFromDatabase();
	}
}
