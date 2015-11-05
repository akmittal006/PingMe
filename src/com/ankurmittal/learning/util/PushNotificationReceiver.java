package com.ankurmittal.learning.util;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import com.ankurmittal.learning.storage.ChatItem;
import com.ankurmittal.learning.storage.ChatItemDataSource;
import com.ankurmittal.learning.storage.MessageStatus;
import com.ankurmittal.learning.storage.TextMessage;
import com.ankurmittal.learning.storage.TextMessageDataSource;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseUser;

public class PushNotificationReceiver extends ParsePushBroadcastReceiver {

	@Override
	protected Bitmap getLargeIcon(Context context, Intent intent) {
		// TODO Auto-generated method stub
		return super.getLargeIcon(context, intent);
	}

	@Override
	protected Notification getNotification(Context context, Intent intent) {
		// TODO Auto-generated method stub
		return super.getNotification(context, intent);
	}

	@Override
	protected int getSmallIconId(Context context, Intent intent) {
		// TODO Auto-generated method stub
		return super.getSmallIconId(context, intent);
	}

	@Override
	protected void onPushDismiss(Context context, Intent intent) {
		// TODO Auto-generated method stub
		super.onPushDismiss(context, intent);
	}

	@Override
	protected void onPushOpen(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		super.onPushOpen(arg0, arg1);
	}

	@Override
	protected void onPushReceive(Context context, Intent intent) {
		// Push received it can be one of following cases
		// 1. New Message
		//2. Message Status Update
		super.onPushReceive(context, intent);
		String jsonData = intent.getExtras().getString("com.parse.Data");
		Toast.makeText(context, "Push Received", Toast.LENGTH_LONG).show();

		try {
			JSONObject jsonMessage = new JSONObject(jsonData);
			TextMessageDataSource mMessageDataSource = new TextMessageDataSource(
					context);

				mMessageDataSource.open();
			
			//CASE -- 1. NEW MESSAGE
			if(jsonMessage.getString("type").equals("message")) {
				
				TextMessage receivedMessage = createTextMessageFromJsonData(jsonMessage);
				//Push type is message
				
				//1. saving new message in database
				mMessageDataSource
						.insert(receivedMessage);
				mMessageDataSource.close();
				
				
				//2. sending push back to update message status to delivered
				final HashMap<String, String> params = new HashMap<String, String>();
					params.put(receivedMessage.getMessageId(), Constants.MESSAGE_STATUS_DELIVERED);

					Log.i("calling cloud", "now");
					ParseCloud.callFunctionInBackground("updateMessages",params,  new FunctionCallback<String>() {

						@Override
						public void done(String arg0, ParseException e) {
							// TODO Auto-generated method stub
							if(e == null) {
								Log.i("Notification receiver Cloud Code", "Yay it worked! "+ arg0);
								ParseObject.unpinAllInBackground(ParseConstants.GROUP_MESSAGE_DELIVERED);
							} else {
								Log.i("Notification receiver Cloud Code", "Some error" + e.getMessage());
							}
						}
					});
				
				
				updateChatItem(context, createTextMessageFromJsonData(jsonMessage));
				
				updateMyActivity(context, jsonData);
			} 
			//CASE -- 2. MESSAGE STATUS UPDATE
			else {
				//Push type is update message status
				int updated = mMessageDataSource.updateMessageStatus(jsonMessage.getString("ObjectId"),jsonMessage.getString("messageStatus"));
				Log.e("update push received4", " " + jsonMessage.getString("messageStatus")   + "  " + updated);
				updateMyActivity(context, jsonData);
			}
			
			

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("json error", "error retrieving json");
		}

	}

	static void updateMyActivity(Context context, String message) {

		Intent intent = new Intent(Constants.PUSH_TO_CHAT);
		// put whatever data you want to send, if any
		intent.putExtra(Constants.JSON_MESSAGE, message);
		// flag to check type of activity
		intent.putExtra(Constants.PUSH_INTENT_TYPE, Constants.PUSH_INTENT_FLAG);
		// send broadcast
		context.sendBroadcast(intent);

	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		super.onReceive(context, intent);
	}

	private TextMessage createTextMessageFromJsonData(JSONObject message) {
		TextMessage textMessage = new TextMessage();
		try {
			textMessage.setCreatedAt(message
					.getString(ParseConstants.KEY_CREATED_AT));
			textMessage.setMessage(message
					.getString(ParseConstants.KEY_MESSAGE));
			textMessage.setMessageId(message
					.getString(ParseConstants.KEY_MESSAGE_ID));
			textMessage.setReceiverId(message
					.getString(ParseConstants.KEY_MESSAGE_RECEIVER_ID));
			textMessage.setReceiverName(message
					.getString(ParseConstants.KEY_MESSAGE_RECEIVER_NAME));
			textMessage.setSenderId(message
					.getString(ParseConstants.KEY_SENDER_ID));
			textMessage.setSenderName(message
					.getString(ParseConstants.KEY_SENDER_NAME));
			textMessage.setMessageStatus(Constants.MESSAGE_STATUS_DELIVERED);
			return textMessage;
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("json error", "error creating message");
		}
		return null;
	}

	private void updateChatItem(Context context, TextMessage textmessage) {
		// TODO Auto-generated method stub
		String id;
		String content;
		if (textmessage.getSenderId().equals(
				ParseUser.getCurrentUser().getObjectId())) {
			id = textmessage.getReceiverId(); // equivalent to item id
			content = textmessage.getReceiverName();

		} else {
			id = textmessage.getSenderId(); // equivalent to item id
			content = textmessage.getSenderName();
		}

		ChatItem chatItem = new ChatItem(id, content);
		chatItem.setLastMessage(textmessage);
		
		ChatItemDataSource mChatItemDataSource = new ChatItemDataSource(
				context);
		mChatItemDataSource.open();
		mChatItemDataSource.insert(chatItem);
		mChatItemDataSource.close();

	}

}
