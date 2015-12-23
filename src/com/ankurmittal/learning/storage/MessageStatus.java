package com.ankurmittal.learning.storage;

import com.parse.ParseObject;

public class MessageStatus extends ParseObject{
	
	String messageId;
	String messageStatus;
	
	public MessageStatus(String id, String status) {
		setMessageId(id);
		setMessageStatus(status);
	}
	
	public String getMessageId() {
		return getString("messageId");
	}
	public void setMessageId(String messageId) {
		put("messageId", messageId);
	}
	public String getMessageStatus() {
		return  getString("messageStatus");
	}
	public void setMessageStatus(String messageStatus) {
		put("messageStatus", messageStatus);
	}
	
	

}
