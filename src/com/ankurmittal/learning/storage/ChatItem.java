package com.ankurmittal.learning.storage;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

/**
 * A dummy item representing a piece of content.
 */
public class ChatItem {
	public String id;

	public String content;
	public TextMessage lastMessage;

	public ArrayList<TextMessage> mMessages;
	public ArrayList<TextMessage> notReadMessages;
	public String email;
	public String imgUrl;

	private ParseUser senderUser;

	public ChatItem() {
		lastMessage = new TextMessage();
		mMessages = new ArrayList<TextMessage>();
		notReadMessages = new ArrayList<TextMessage>();
	}

	public ChatItem(String id, String content, ArrayList<TextMessage> messages) {
		this.id = id;
		this.content = content;
		mMessages = new ArrayList<TextMessage>();
		this.mMessages = messages;
	}

	public ChatItem(String id, String content) {
		Log.e("DEBUG", "creating chat item using id and content " + id
				+ " - " + content );
		this.id = id;
		this.content = content;
		mMessages = new ArrayList<TextMessage>();
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getImgUrl() {
		return imgUrl;
	}

	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}

	public TextMessage getLastMessage() {
		return lastMessage;
	}

	public void setLastMessage(String lastMessage) {
		this.lastMessage.setMessage(lastMessage);
	}

	public void setLastMessage(TextMessage lastMessage) {
		this.lastMessage = lastMessage;
	}

	public void setLastMessageCreatedAt(String dateString) {
		this.lastMessage.setCreatedAt(dateString);
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setEmail() {
		ParseQuery<ParseUser> query = new ParseQuery<ParseUser>(ParseUser.class);
		query.whereEqualTo("objectId", id);
		query.findInBackground(new FindCallback<ParseUser>() {

			@Override
			public void done(List<ParseUser> users, ParseException e) {
				if (e == null) {
					Log.i("chatItem", "found email");
					senderUser = users.get(0);
					setEmail(senderUser.getEmail());
				}

			}
		});
	}

	public void addMessage(TextMessage message) {
		mMessages.add(message);
	}

	public TextMessage getMessage(int i) {
		if (mMessages.size() > 0) {
			return mMessages.get(i);
		} else {
			return null;
		}

	}

	@Override
	public String toString() {
		return content;
	}

	public ArrayList<TextMessage> getItemMessages() {
		if (mMessages == null) {
			mMessages = new ArrayList<TextMessage>();
		}
		return mMessages;
	}

	public String getId() {
		return id;
	}

	public void clearMessages() {
		mMessages.clear();
	}

	public ArrayList<TextMessage> getNotReadMessages() {

		if (notReadMessages == null) {
			notReadMessages = new ArrayList<TextMessage>();
		}
		notReadMessages.clear();

		ArrayList<TextMessage> allMessages = new ArrayList<TextMessage>();
		allMessages = getItemMessages();
		for (TextMessage message : allMessages) {
			if (message.getMessageStatus().equals("readPingMeMessage10123452")) {

			} else {
				if (message.getSenderId().equals(id)) {
					// received message

					if (!message.getReceiverName().equals("pingMe9872719390")) {

						notReadMessages.add(message);
					}

				}

			}
		}
		Log.e("chat ITEM", "total messages- " + allMessages.size()
				+ " Not read messages" + notReadMessages.size());
		return notReadMessages;
	}
}