package com.ankurmittal.learning;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ankurmittal.learning.storage.ChatContent;
import com.ankurmittal.learning.storage.ChatItem;
import com.ankurmittal.learning.storage.TextMessage;
import com.ankurmittal.learning.storage.TextMessageDataSource;
import com.ankurmittal.learning.util.ParseConstants;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (ParseUser.getCurrentUser() != null) {
			// retrieveMessages();
		}else {
			Intent intent2 = new Intent(getActivity(), LoginActivity.class);
			intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent2);
		}

		mMessageDataSource = new TextMessageDataSource(getActivity());

		// TODO: replace with a real list adapter.
		setListAdapter(new ArrayAdapter<ChatItem>(getActivity(),
				android.R.layout.simple_list_item_activated_1,
				android.R.id.text1, ChatContent.ITEMS));

	}

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
	public void onPause() {
		super.onPause();
		// close database connection
		// mMessageDataSource.close();
	}

	@Override
	public void onResume() {
		super.onResume();
		// open database connection
		mMessageDataSource.open();
		if (ParseUser.getCurrentUser() != null) {
			retrieveMessages();
			sortMessagesFromDatabase();
		}

		setListAdapter(new ArrayAdapter<ChatItem>(getActivity(),
				android.R.layout.simple_list_item_activated_1,
				android.R.id.text1, ChatContent.ITEMS));
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
	public void onListItemClick(ListView listView, View view, int position,
			long id) {
		super.onListItemClick(listView, view, position, id);

		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an item has been selected.
		mCallbacks.onItemSelected(ChatContent.ITEMS.get(position).id);
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

	private void retrieveMessages() {
		 
		ParseQuery<ParseObject> messagesQuery = new ParseQuery<ParseObject>(
				ParseConstants.TEXT_MESSAGE);
		messagesQuery.whereEqualTo(ParseConstants.KEY_MESSAGE_RECEIVER_ID,
				ParseUser.getCurrentUser().get(ParseConstants.KEY_USER_ID));
		messagesQuery.include(ParseConstants.KEY_MESSAGE_SENDER);
		messagesQuery.include(ParseConstants.KEY_CREATED_AT);
		messagesQuery.addAscendingOrder(ParseConstants.KEY_CREATED_AT);
		// messagesQuery.include(ParseConstants.KEY_MESSAGE_RECEIVER_ID);
		// messagesQuery.include(ParseConstants.KEY_MESSAGE_RECEIVER_NAME);
		messagesQuery.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> pTextMessages, ParseException e) {
				Date prevDate = null;
				Date currDate = null;

				if (e == null) {
					Log.d("Retrieved messages", "NO. :-" + pTextMessages.size());
					// hurray we received our messages
					for (ParseObject pTextMessage : pTextMessages) {
						// create a text message and save it to database
						TextMessage textmessage = createTextMessage(pTextMessage);
						if(ParseUser.getCurrentUser() != null) {
							mMessageDataSource.insert(textmessage);
						}
					}
					if(ParseUser.getCurrentUser() != null) {
						sortMessagesFromDatabase();
						updateView();
					}
					
				} else {
					Log.d("REtrieval errror", "" + e.getMessage());
				}
			}
		});
	}
	private String getDateTime(java.util.Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        //Date date = new Date();
        return dateFormat.format(date);
}
	
	private TextMessage createTextMessage(ParseObject pTextMessage) {
		TextMessage textMessage = new TextMessage();
		textMessage.setMessage(pTextMessage
				.getString(ParseConstants.KEY_MESSAGE));
		Log.d("list frag ",
				" " + pTextMessage.getString(ParseConstants.KEY_MESSAGE));
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
		textMessage.setSent(pTextMessage.getBoolean("isSent"));

		return textMessage;
	}

	private void sortMessagesFromDatabase() {
		if (mMessageDataSource.selectAll().getCount() != 0) {
			ArrayList<TextMessage> allMessages = mMessageDataSource
					.getAllMessages();

			for (TextMessage message : allMessages) {
				String id;
				String content;
				if (message.getSenderId().equals(ParseUser.getCurrentUser().getObjectId()) ) {
					id = message.getReceiverId(); // equivalent to item id
					content = message.getReceiverName();
				}else {
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
					ChatItem chatItem = new ChatItem(id,
							content);
					Log.d("chat list", "new chat item created :" + content);
					chatItem.addMessage(message);
					ChatContent.addItem(chatItem);
				}
			}

		}
	}

	private void updateView() {
		setListAdapter(new ArrayAdapter<ChatItem>(getActivity(),
				android.R.layout.simple_list_item_activated_1,
				android.R.id.text1, ChatContent.ITEMS));
	}

	@Override
	public void onDestroy() {
		mMessageDataSource.close();
		super.onDestroy();

	}

}
