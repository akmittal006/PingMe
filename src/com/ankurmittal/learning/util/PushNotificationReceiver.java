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
import com.ankurmittal.learning.storage.TextMessage;
import com.ankurmittal.learning.storage.TextMessageDataSource;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseUser;

public class PushNotificationReceiver extends ParsePushBroadcastReceiver
		implements com.ankurmittal.learning.fragments.ChatDetailFragment.TestInterface {

	private Context mContext;
	private TextMessageDataSource mMessageDataSource;

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

		mContext = context;

		// Push received it can be one of following cases
		// 1. New Message
		// 2. Message Status Update
		super.onPushReceive(context, intent);
		String jsonData = intent.getExtras().getString("com.parse.Data");
		Toast.makeText(context, "Push Received", Toast.LENGTH_LONG).show();

		try {
			JSONObject jsonMessage = new JSONObject(jsonData);
			mMessageDataSource = new TextMessageDataSource(context);

			mMessageDataSource.open();

			// CASE -- 1. NEW MESSAGE
			if (jsonMessage.getString("type").equals("message")) {

				TextMessage receivedMessage = Utils.createTextMessageFromJsonData(jsonMessage);
				// Push type is message

				// 1. saving new message in database
				mMessageDataSource.insert(receivedMessage);
				mMessageDataSource.close();

				// 2. sending push back to update message status to delivered

				// 2. a) if app is opened then send read status instead of
				// delivered

				// //// broadcast intent to check if activity is active
				Intent intent2 = new Intent(Constants.PUSH_TO_CHECK);
				intent2.putExtra(Constants.JSON_MESSAGE_ID,
						receivedMessage.getMessageId());
				// flag to check type of activity
				intent2.putExtra(Constants.PUSH_INTENT_TYPE,
						Constants.PUSH_INTENT_FLAG);
				// send broadcast
				context.sendBroadcast(intent2);

				// 2. b) otherwise do same
				// if(ChatListActivity.class.is)
				final HashMap<String, String> params = new HashMap<String, String>();
				params.put(receivedMessage.getMessageId(),
						Constants.MESSAGE_STATUS_DELIVERED);

				Log.i("calling cloud", "now");

				ParseCloud.callFunctionInBackground("updateMessages", params,
						new FunctionCallback<String>() {
							@Override
							public void done(String arg0, ParseException e) {
								// TODO Auto-generated method stub
								if (e == null) {
									// Log.i("Notification receiver Cloud Code",
									// "Yay it worked! "+ arg0);
									ParseObject
											.unpinAllInBackground(ParseConstants.GROUP_MESSAGE_DELIVERED);
								} else {
									e.printStackTrace();
									Log.e("Notification receiver Cloud Code",
											"Some error" + e.getMessage());
								}
							}
						});

				updateChatItem(context,
						Utils.createTextMessageFromJsonData(jsonMessage));

				updateMyActivity(context, jsonData);
			}
			// CASE -- 2. MESSAGE STATUS UPDATE
			else {
				// Push type is update message status
				int updated = mMessageDataSource.updateMessageStatus(
						jsonMessage.getString("ObjectId"),
						jsonMessage.getString("messageStatus"));
				if (updated > 0) {
					updateMyActivity(context, jsonData);
				}

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

		ChatItemDataSource mChatItemDataSource = new ChatItemDataSource(context);
		mChatItemDataSource.open();
		mChatItemDataSource.insert(chatItem);
		mChatItemDataSource.close();

	}

	@Override
	public void callbackCall(final String id, Context context) {
		// Send read status instead of delivered status

		mContext = context;

		final HashMap<String, String> params = new HashMap<String, String>();
		params.put(id, Constants.MESSAGE_STATUS_READ);

		Log.e("CALLBACK", "CALLBACK +" + id);

		ParseCloud.callFunctionInBackground("updateMessages", params,
				new FunctionCallback<String>() {
					@Override
					public void done(String arg0, ParseException e) {
						// TODO Auto-generated method stub
						if (e == null) {
							// Log.i("Notification receiver Cloud Code",
							// "Yay it worked! " + arg0);
							// update database

							mMessageDataSource = new TextMessageDataSource(
									mContext);
							mMessageDataSource.open();
							// Log.e("DEBUG", "" + mContext.toString());

							// if (!mMessageDataSource.isOpen()) {
							//
							// }
							mMessageDataSource.updateMessageStatus(id,
									Constants.MESSAGE_STATUS_READ);

						} else {
							e.printStackTrace();
							Log.e("Notification receiver Cloud Code",
									"Some error" + e.getMessage());
						}
					}
				});

		Intent intent3 = new Intent(Constants.PUSH_TO_CHAT);
		// flag to check type of activity
		intent3.putExtra(Constants.PUSH_INTENT_TYPE, "refresh");
		// send broadcast
		mContext.sendBroadcast(intent3);
	}

}
