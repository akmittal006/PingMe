package com.ankurmittal.learning.storage;

import java.util.ArrayList;

import com.parse.ParseUser;


/**
 * A dummy item representing a piece of content.
 */
public class ChatItem2 {
	public String id;
	public String content;
	public ArrayList<TextMessage> mMessages;
	public String mStatus;
	public String mImage; // email

	public String getImage() {
		return mImage;
	}

	public void setImage(String mImage) {
		this.mImage = mImage;
	}

	public ChatItem2(String id, String content, ArrayList<TextMessage> messages) {
		this.id = id;
		this.content = content;
		this.mMessages = messages;
	}
	
	public ChatItem2(String id, String content) {
		this.id = id;
		this.content = content;
		mMessages = new ArrayList<TextMessage>();
	}
	
	public void setStatus(String status) {
		mStatus = status;
	}
	
	public String getStatus() {
		return mStatus;
	}
	
	public void addMessage(TextMessage message) {
		mMessages.add(message);
	}
	public TextMessage getMessage(int i) {
		if(mMessages.size() > 0) {
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
		return mMessages;
	} 
	public String getId() {
		return id;
	}
	public void clearMessages() {
		mMessages.clear();
	}
}