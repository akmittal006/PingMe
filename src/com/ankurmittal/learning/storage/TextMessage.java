package com.ankurmittal.learning.storage;

import com.parse.ParseObject;

public class TextMessage  {
	
	public String mMessage;
	public String mReceiverName;
	public String mSenderName;
	public String mSenderId;
	public String mReceiverId;
	public String mMessageId;
	
	public String getMessageId() {
		return mMessageId;
	}
	public void setMessageId(String mMessageId) {
		this.mMessageId = mMessageId;
	}
	public String getMessage() {
		return mMessage;
	}
	public void setMessage(String mMessage) {
		this.mMessage = mMessage;
	}
	public String getReceiverName() {
		return mReceiverName;
	}
	public void setReceiverName(String mReceiver) {
		this.mReceiverName = mReceiver;
	}
	public String getSenderId() {
		return mSenderId;
	}
	public void setSenderId(String mSenderId) {
		this.mSenderId = mSenderId;
	}
	public String getReceiverId() {
		return mReceiverId;
	}
	public void setReceiverId(String mReceiverId) {
		this.mReceiverId = mReceiverId;
	}
	public String getSenderName() {
		return mSenderName;
	}
	public void setSenderName(String mSender) {
		this.mSenderName = mSender;
	}
	
	

}
