package com.ankurmittal.learning.storage;

import java.util.ArrayList;

import com.parse.ParseUser;


/**
 * A dummy item representing a piece of content.
 */
public class ChatItem {
	public String id;
	public String content;
	public ArrayList<TextMessage> mMessages;

	public ChatItem(String id, String content, ArrayList<TextMessage> messages) {
		this.id = id;
		this.content = content;
		this.mMessages = messages;
	}
	
	public ChatItem(String id, String content) {
		this.id = id;
		this.content = content;
		mMessages = new ArrayList<TextMessage>();
	}
	
	public void addMessage(TextMessage message) {
		mMessages.add(message);
	}
	public TextMessage getMessage(int i) {
		return mMessages.get(i);
	}

	@Override
	public String toString() {
		return content;
	}
	
	
	public ArrayList<TextMessage> getItemMessages() {
		return mMessages;
	} 
	public String getId() {
		return id;
	}
	public void clearMessages() {
		mMessages.clear();
	}
}