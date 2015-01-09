package com.ankurmittal.learning.storage;

import java.util.ArrayList;


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

	@Override
	public String toString() {
		return content;
	}
	public ArrayList<TextMessage> getItemMessages() {
		return mMessages;
	} 
}