package com.ankurmittal.learning.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONObject;

import android.util.Log;

import com.ankurmittal.learning.storage.TextMessage;
import com.parse.ParseObject;
import com.parse.ParseUser;

public class Utils {

	public static TextMessage createTextMessage(ParseObject pTextMessage) {
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
		if (pTextMessage.getObjectId() != null) {
			textMessage.setMessageId(pTextMessage.getObjectId());
		}

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

	public static String getDateTime(java.util.Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		// Date date = new Date();
		return dateFormat.format(date);
	}

	public static JSONObject createJSONObject(ParseObject message) {
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
					Utils.getDateTime(message.getCreatedAt()));
			jsonMessage.put("type", "message");

			Log.d("Json message", jsonMessage.toString());
			return jsonMessage;
		} catch (Exception e) {
			Log.e("JSON ERROR", "error creating message");
		}
		return null;
	}

	public static TextMessage createNeutralMessage(Date currDate,
			TextMessage pTextMessage) {
		TextMessage textMessage = new TextMessage();
		textMessage.setMessage(getDateString(currDate));
		Log.d("list frag ", " " + pTextMessage.getMessage());
		textMessage.setMessageId(currDate.toString());
		textMessage.setReceiverId(pTextMessage.getReceiverId());
		textMessage.setReceiverName("pingMe9872719390");
		textMessage.setSenderId(pTextMessage.getSenderId());
		textMessage.setSenderName(pTextMessage.getSenderName());
		textMessage.setCreatedAt(currDate);
		textMessage.setType(Constants.TYPE_NEUTRAL);
		return textMessage;
	}

	public static String getDateString(Date date) {
		String strDate = null;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		strDate = dateFormat.format(date);
		return strDate;
	}

}
