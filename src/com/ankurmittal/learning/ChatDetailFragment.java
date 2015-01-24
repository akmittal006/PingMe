package com.ankurmittal.learning;

import java.util.ArrayList;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.ankurmittal.learning.adapters.TextMessageAdapter;
import com.ankurmittal.learning.storage.ChatContent;
import com.ankurmittal.learning.storage.ChatItem;
import com.ankurmittal.learning.storage.TextMessage;
import com.ankurmittal.learning.storage.TextMessageDataSource;
import com.ankurmittal.learning.util.MD5Util;
import com.ankurmittal.learning.util.ParseConstants;
import com.parse.ParseConfig;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

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
	private TextMessageDataSource mMessageDataSource;
	ListView chatView;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ChatDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments().containsKey(ARG_ITEM_ID)) {
			// Load the dummy content specified by the fragment
			// arguments. In a real-world scenario, use a Loader
			// to load content from a content provider.
			mItem = ChatContent.ITEM_MAP.get(getArguments().getString(
					ARG_ITEM_ID));
			
			
		}
		mMessageDataSource = new TextMessageDataSource(getActivity());
	}

	@Override
	public void onPause() {
		super.onPause();
		//close database connection
		mMessageDataSource.close();
	}

	@Override
	public void onResume() {
		super.onResume();
		//open database connection
		mMessageDataSource.open();
		loadChatItemMessagesFromDatabase();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_chat_detail,
				container, false);

		// Show the dummy content as text in a TextView.
		if (mItem != null) {
//			((EditText) rootView.findViewById(R.id.chatEditText))
//					.setText(mItem.content);
			chatView = (ListView)rootView.findViewById(R.id.chatListView);
			chatView.setDivider(null);
			chatView.setDividerHeight(0);
			chatView.setAdapter(new TextMessageAdapter(getActivity(), mItem.getItemMessages()));
			getActivity().setTitle(mItem.toString());
			mTextMessage = (EditText)rootView.findViewById(R.id.chatEditText);
			mTextMessage.requestFocus(0);
			chatView.setSelection(chatView.getAdapter().getCount() - 1);
			mSendButton = (Button)rootView.findViewById(R.id.sendButton);
			//if send button is clicked , save new message 
			mSendButton.setOnClickListener(new OnClickListener() {
				String newTextMessage;
				ParseObject pTextMessage;
				TextMessage message;
				@Override
				public void onClick(View arg0) {
					
					newTextMessage = mTextMessage.getText().toString();
					mTextMessage.setText("");
					//saving new message at backend
					 pTextMessage = new ParseObject(ParseConstants.TEXT_MESSAGE);
					pTextMessage.put(ParseConstants.KEY_MESSAGE, newTextMessage);
					pTextMessage.put(ParseConstants.KEY_MESSAGE_SENDER, ParseUser.getCurrentUser());

					pTextMessage.put(ParseConstants.KEY_MESSAGE_RECEIVER_ID, getArguments().getString(
							ARG_ITEM_ID));
					pTextMessage.put(ParseConstants.KEY_MESSAGE_RECEIVER_NAME, mItem.content);
					pTextMessage.saveInBackground(new SaveCallback() {
						
						@Override
						public void done(ParseException e) {
							if(e == null) {
								//put message id
								pTextMessage.put(ParseConstants.KEY_MESSAGE_ID, pTextMessage.getObjectId());
								//saving new message at database
								message = new TextMessage();
								message.setMessage(newTextMessage);
								message.setMessageId(pTextMessage.getObjectId());
								message.setSenderId(ParseUser.getCurrentUser().getObjectId());
								message.setSenderName(ParseUser.getCurrentUser().getUsername());
								message.setReceiverId(mItem.id);
								message.setReceiverName(mItem.content);
								mMessageDataSource.insert(message);
								//add to chat item
								String id = mItem.id;
								if (ChatContent.ITEM_MAP.containsKey(id) ) {
									// Notify the active callbacks interface (the activity, if the
									// fragment is attached to one) that an item has b een selected.
									//mCallbacks.onItemSelected(id);
									ChatItem chatItem  = ChatContent.ITEM_MAP.get(id);
									chatItem.addMessage(message);
								}else {
									Log.d("ChatDetail", "this shud not b called");
									ChatItem chatItem = new ChatItem(id, ParseUser.getCurrentUser().getUsername());
									chatItem.addMessage(message);
								}
								chatView.setAdapter(new TextMessageAdapter(getActivity(), mItem.getItemMessages()));
								chatView.setSelection(chatView.getAdapter().getCount() - 1);
							}
							
						}
					});
				}
			});
		}

		return rootView;
	}
	private void loadChatItemMessagesFromDatabase() {
		 //ArrayList<TextMessage> chatItemMessages ;
		if(mMessageDataSource
				.getMessagesFrom(mItem.id) != null) {
			String id = mItem.id ; // equivalent to item id
			ArrayList<TextMessage> allMessages = mMessageDataSource
					.getMessagesFrom(mItem.id);
			if (ChatContent.ITEM_MAP.containsKey(mItem.id)) {
			//clear all prev msgs
			ChatItem chatItem = ChatContent.ITEM_MAP.get(mItem.id);
			chatItem.clearMessages();
		}
			for (TextMessage message : allMessages) {
				

				Log.d("chat frag", "" + id);
					if (ChatContent.ITEM_MAP.containsKey(id)) {
						// messages from that sender exist
						ChatItem chatItem = ChatContent.ITEM_MAP.get(id);
						Log.d("message sender name" , message.getSenderName());
						chatItem.addMessage(message);
						Log.d("IMPPP chat frag", "msg added");
					} else {
						// new chat item is created
						
						ChatItem chatItem = new ChatItem(id, message.getSenderName());
						Log.d("chat frag", "new chat item created :" + message.getSenderName());
						chatItem.addMessage(message);
						ChatContent.addItem(chatItem);
						Log.d("IMPPP chat frag", "msg added");
					}
			}
		}
		chatView.setAdapter(new TextMessageAdapter(getActivity(), mItem.getItemMessages()));
		chatView.setSelection(chatView.getAdapter().getCount() - 1);
	}
}
