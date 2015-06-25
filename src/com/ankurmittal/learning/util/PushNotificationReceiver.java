package com.ankurmittal.learning.util;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;

import com.ankurmittal.learning.storage.TextMessage;
import com.ankurmittal.learning.storage.TextMessageDataSource;
import com.parse.ParsePushBroadcastReceiver;

public class PushNotificationReceiver extends ParsePushBroadcastReceiver{



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
		// TODO Auto-generated method stub
		Log.i("push received3", "yayy");
		super.onPushReceive(context, intent);
		Log.i("push received2", "yayy");
		String jsonData = intent.getExtras().getString("com.parse.Data");
		
		try {
			JSONObject jsonMessage = new JSONObject(jsonData);
			TextMessageDataSource mMessageDataSource = new TextMessageDataSource(context);
			mMessageDataSource.open();
			mMessageDataSource.insert(createTextMessageFromJsonData(jsonMessage));
			mMessageDataSource.close();
			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("json error", "error retrieving json");
		}
		
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		super.onReceive(context, intent);
	}
private TextMessage createTextMessageFromJsonData(JSONObject message) {
	TextMessage textMessage = new TextMessage();
	try {
	textMessage.setCreatedAt(message.getString(ParseConstants.KEY_CREATED_AT));
	textMessage.setMessage(message.getString(ParseConstants.KEY_MESSAGE));
	textMessage.setMessageId(message.getString(ParseConstants.KEY_MESSAGE_ID));
	textMessage.setReceiverId(message.getString(ParseConstants.KEY_MESSAGE_RECEIVER_ID));
	textMessage.setReceiverName(message.getString(ParseConstants.KEY_MESSAGE_RECEIVER_NAME));
	textMessage.setSenderId(message.getString(ParseConstants.KEY_SENDER_ID));
	textMessage.setSenderName(message.getString(ParseConstants.KEY_SENDER_NAME));
	textMessage.setSent(true);
	return textMessage;}
	catch (Exception e) {
		e.printStackTrace();
		Log.e("json error", "error creating message");
	}
	return null;
}
}
