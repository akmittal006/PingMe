package com.ankurmittal.learning.storage;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.util.Log;

import com.ankurmittal.learning.util.Constants;

public class TextMessage  {
	
	public String mMessage;
	public String mReceiverName;
	public String mSenderName;
	public String mSenderId;
	public String mReceiverId;
	public String mMessageId;
	public String mType;
	public java.util.Date createdAt;
	public String mMessageStatus = Constants.MESSAGE_STATUS_PENDING;
	public TextMessage() {
		
	}
	public void setType(String type) {
		mType = type;
	}
	public String getType() {
		return mType;
	}
	private String getDateTime(java.util.Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        //Date date = new Date();
        return dateFormat.format(date);
}
	public String getMessageId() {
		return mMessageId;
	}
	
	public String getMessageStatus() {
		return mMessageStatus;
	}
	public void setMessageStatus(String messageStatus) {
		this.mMessageStatus = messageStatus;
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
	public void setCreatedAt(String date) {
		//Log.d("textMessage created at raw" , date);
		SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		try {
			createdAt = dateFormat.parse(date);
		} catch (ParseException e) {
			Log.e("date error in text message", "invalid date string");
			e.printStackTrace();
		}
		//Log.d("textMessage created at" , createdAt.toString());
	}
	public void setCreatedAt(java.util.Date date) {
		createdAt = date;
	}
	public Date getCreatedAt() {
		Date date = new Date(createdAt.getTime());
		return date;
	}
	public String getCreatedAtString() {
		return getDateTime(createdAt);
	}

}
