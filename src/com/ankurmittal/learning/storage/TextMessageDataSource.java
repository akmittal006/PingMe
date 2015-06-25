package com.ankurmittal.learning.storage;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class TextMessageDataSource {
	private SQLiteDatabase mDatabase; // The actual DB!
	private TextMessageHelper mTextMessageHelper; // Helper class for creating
													// and
	// opening the DB
	private Context mContext;

	public TextMessageDataSource(Context context) {
		mContext = context;
		mTextMessageHelper = new TextMessageHelper(mContext);
	}

	/*
	 * Open the db. Will create if it doesn't exist
	 */
	public void open() throws SQLException {
		mDatabase = mTextMessageHelper.getWritableDatabase();
		// Log.d("TEXT Databse check", "database opened");
	}

	/*
	 * We always need to close our db connections
	 */
	public void close() {
		mDatabase.close();
		// Log.d("TEXT Databse check", "database closed");
	}

	// INSERT
	public void insert(TextMessage textMessage) {

		Cursor cursor = isMessageNew(textMessage);
		if (cursor.getCount() == 0) {
			// we got a new frnd since cursor cud not find it
			Log.d("DATA SOURCE",
					"INSERTING NEW Friend..." + textMessage.getSenderName());

			// sendNotification(friend);

			mDatabase.beginTransaction();
			try {
				ContentValues values = new ContentValues();
				values.put(TextMessageHelper.COLUMN_SENDER_ID,
						textMessage.getSenderId());
				values.put(TextMessageHelper.COLUMN_SENDER_NAME,
						textMessage.getSenderName());
				values.put(TextMessageHelper.COLUMN_RECEIVER_ID,
						textMessage.getReceiverId());
				values.put(TextMessageHelper.COLUMN_RECEIVER_NAME,
						textMessage.getReceiverName());
				values.put(TextMessageHelper.COLUMN_MESSAGE_ID,
						textMessage.getMessageId());
				values.put(TextMessageHelper.COLUMN_MESSAGE,
						textMessage.getMessage());
				values.put(TextMessageHelper.COLUMN_IS_SENT,
						textMessage.isSent() + "");
				Log.d("inseting", textMessage.getCreatedAtString());
				values.put(TextMessageHelper.COLUMN_CREATED_AT,
						textMessage.getCreatedAtString());
				// friend.setViewed(false);
				mDatabase
						.insert(TextMessageHelper.TABLE_MESSAGES, null, values);
				mDatabase.setTransactionSuccessful();
				Log.d("INSERTED", "ROW ADDED");
			} finally {
				mDatabase.endTransaction();
			}
		} else {

			Log.d(" NOT INSERTED", "ROW NOT ADDED");
		}

		
	}

	// public void insertByMain(ParseUser friend) {
	//
	// Cursor cursor = isFriendNew(friend);
	// if (cursor.getCount() == 0) {
	// Log.d("DATA SOURCE", "INSERTING NEW ROW...");
	//
	// //sendNotification(friend);
	//
	// mDatabase.beginTransaction();
	// try {
	// ContentValues values = new ContentValues();
	// values.put(FriendsHelper.COLUMN_DATE, friend.getDate());
	// values.put(FriendsHelper.COLUMN_ORDER, friend.getOrder());
	// values.put(FriendsHelper.COLUMN_SUBJECT, friend.getSubject());
	// values.put(FriendsHelper.COLUMN_URL, friend.getUrl());
	// friend.setViewed(false);
	// mDatabase.insert(FriendsHelper.TABLE_POSTINGS, null, values);
	// mDatabase.setTransactionSuccessful();
	// Log.d("INSERTED" , "ROW ADDED");
	// } finally {
	// mDatabase.endTransaction();
	// }
	// }
	// else{
	//
	// Log.d(" NOT INSERTED" , "ROW NOT ADDED");
	// }
	//
	// }

	// private void sendNotification(Post post) {
	// Intent resultIntent = new Intent(mContext, MainActivity.class);
	// // Because clicking the notification opens a new ("special") activity,
	// // there's
	// // no need to create an artificial back stack.
	// PendingIntent resultPendingIntent = PendingIntent.getActivity(mContext,
	// 0,
	// resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
	// NotificationCompat.Builder mBuilder = new
	// NotificationCompat.Builder(mContext)
	// .setSmallIcon(R.drawable.pspcl_logo)
	// .setContentTitle("New Update")
	// .setContentText(post.getDate());
	//
	//
	// mBuilder.setContentIntent(resultPendingIntent);
	// // Sets an ID for the notification
	// int mNotificationId = 001;
	// // Gets an instance of the NotificationManager service
	// NotificationManager mNotifyMgr =
	// (NotificationManager)
	// mContext.getSystemService(Context.NOTIFICATION_SERVICE);
	// // Builds the notification and issues it.
	// mNotifyMgr.notify(mNotificationId, mBuilder.build());
	// }

	public Cursor isMessageNew(TextMessage textMessage) {
//		Log.i("error",
//				textMessage.getMessage() 
//						+ "");
		String whereClause = TextMessageHelper.COLUMN_MESSAGE_ID + " = ?";
 
		if(mDatabase.isOpen()) {
			Cursor cursor = mDatabase.query(TextMessageHelper.TABLE_MESSAGES, // table
					new String[] { TextMessageHelper.COLUMN_MESSAGE_ID }, // column
																			// names
					whereClause, // where clause
					new String[] { textMessage.getMessageId() }, // where params
					null, // groupby
					null, // having
					null // orderby
					);
			return cursor;
		} else {
			open();
			Cursor cursor = mDatabase.query(TextMessageHelper.TABLE_MESSAGES, // table
					new String[] { TextMessageHelper.COLUMN_MESSAGE_ID }, // column
																			// names
					whereClause, // where clause
					new String[] { textMessage.getMessageId() }, // where params
					null, // groupby
					null, // having
					null // orderby
					);
			return cursor;
		}
		
	}

	public void deleteAll() {
		mDatabase.delete(TextMessageHelper.TABLE_MESSAGES, // table
				null, // where clause
				null // where params
				);
	}
	
	public void deleteMessage(String id) {
		String whereClause = TextMessageHelper.COLUMN_MESSAGE_ID + " = ?";
		mDatabase.delete(TextMessageHelper.TABLE_MESSAGES, // table
				whereClause, // where clause
				new String[] { id } // where params
				);
		
	}

	public Cursor selectAll() {

		if (!mDatabase.isOpen()) {
			Log.i("text msg data source", "opening before select all method");
			open();
		}
		Cursor cursor = mDatabase.query(TextMessageHelper.TABLE_MESSAGES, // table
				new String[] { TextMessageHelper.COLUMN_SENDER_ID,
						TextMessageHelper.COLUMN_RECEIVER_ID,
						TextMessageHelper.COLUMN_SENDER_NAME,
						TextMessageHelper.COLUMN_RECEIVER_NAME,
						TextMessageHelper.COLUMN_MESSAGE_ID,
						TextMessageHelper.COLUMN_MESSAGE,
						TextMessageHelper.COLUMN_IS_SENT,
						TextMessageHelper.COLUMN_CREATED_AT }, // column names
				null, // where clause
				null, // where params
				null, // groupby
				null, // having
				null // orderby
				);
		
		return cursor;
	}

	public ArrayList<TextMessage> getAllMessages() {
		

		Cursor cursor = selectAll();
		ArrayList<TextMessage> mTextMessages = new ArrayList<TextMessage>();

		if (cursor.getCount() == 0) {
			return null;
		} else {
			cursor.moveToFirst();
			int row = 0;
			while (!cursor.isAfterLast()) {

				TextMessage textMessage = new TextMessage();
				int i = cursor
						.getColumnIndex(TextMessageHelper.COLUMN_MESSAGE_ID);
				textMessage.setMessageId(cursor.getString(i));
				// do stuff
				mTextMessages.add(textMessage);

				i = cursor.getColumnIndex(TextMessageHelper.COLUMN_SENDER_ID);
				mTextMessages.get(row).setSenderId(cursor.getString(i));

				i = cursor.getColumnIndex(TextMessageHelper.COLUMN_SENDER_NAME);
				mTextMessages.get(row).setSenderName(cursor.getString(i));

				i = cursor.getColumnIndex(TextMessageHelper.COLUMN_RECEIVER_ID);
				mTextMessages.get(row).setReceiverId(cursor.getString(i));

				i = cursor
						.getColumnIndex(TextMessageHelper.COLUMN_RECEIVER_NAME);
				mTextMessages.get(row).setReceiverName(cursor.getString(i));

				i = cursor.getColumnIndex(TextMessageHelper.COLUMN_MESSAGE);
				mTextMessages.get(row).setMessage(cursor.getString(i));

				i = cursor.getColumnIndex(TextMessageHelper.COLUMN_CREATED_AT);
				mTextMessages.get(row).setCreatedAt(cursor.getString(i));

				i = cursor.getColumnIndex(TextMessageHelper.COLUMN_IS_SENT);
				boolean sent = false;
				if (cursor.getString(i).equals("true")) {
					sent = true;
				}
				mTextMessages.get(row).setSent(sent);

				// Log.d("Database check", "retrieving :" +
				// mTextMessages.get(row).getSenderName());

				cursor.moveToNext();
				row++;

			}
			// for (int i=0;i< mTextMessages.size(); i++) {
			// //Log.d("Database check", "added" +
			// mTextMessages.get(i).getSenderName());
			// }
			
			return mTextMessages;
		}

	}

	public ArrayList<TextMessage> getMessagesFrom(String senderId) {
		
		String whereClause = TextMessageHelper.COLUMN_SENDER_ID + " = ? OR "
				+ TextMessageHelper.COLUMN_RECEIVER_ID + " = ?";

		Cursor cursor;
		//check if database is open 
		if(mDatabase.isOpen()) {
			cursor = mDatabase.query(TextMessageHelper.TABLE_MESSAGES, // table
					new String[] { TextMessageHelper.COLUMN_SENDER_ID,
							TextMessageHelper.COLUMN_RECEIVER_ID,
							TextMessageHelper.COLUMN_SENDER_NAME,
							TextMessageHelper.COLUMN_RECEIVER_NAME,
							TextMessageHelper.COLUMN_MESSAGE_ID,
							TextMessageHelper.COLUMN_MESSAGE,
							TextMessageHelper.COLUMN_IS_SENT,
							TextMessageHelper.COLUMN_CREATED_AT }, // column names
					whereClause, // where clause
					new String[] { senderId, senderId }, // where params
					null, // groupby
					null, // having
					null // orderby
					);
		} else {
			open();
			cursor = mDatabase.query(TextMessageHelper.TABLE_MESSAGES, // table
					new String[] { TextMessageHelper.COLUMN_SENDER_ID,
							TextMessageHelper.COLUMN_RECEIVER_ID,
							TextMessageHelper.COLUMN_SENDER_NAME,
							TextMessageHelper.COLUMN_RECEIVER_NAME,
							TextMessageHelper.COLUMN_MESSAGE_ID,
							TextMessageHelper.COLUMN_MESSAGE,
							TextMessageHelper.COLUMN_IS_SENT,
							TextMessageHelper.COLUMN_CREATED_AT }, // column names
					whereClause, // where clause
					new String[] { senderId, senderId }, // where params
					null, // groupby
					null, // having
					null // orderby
					);
		}
		ArrayList<TextMessage> mTextMessages = new ArrayList<TextMessage>();

		if (cursor.getCount() == 0) {
			return null;
		} else {
			cursor.moveToFirst();
			int row = 0;
			while (!cursor.isAfterLast()) {

				TextMessage textMessage = new TextMessage();
				int i = cursor
						.getColumnIndex(TextMessageHelper.COLUMN_MESSAGE_ID);
				textMessage.setMessageId(cursor.getString(i));
				// do stuff
				mTextMessages.add(textMessage);

				i = cursor.getColumnIndex(TextMessageHelper.COLUMN_SENDER_ID);
				mTextMessages.get(row).setSenderId(cursor.getString(i));

				i = cursor.getColumnIndex(TextMessageHelper.COLUMN_SENDER_NAME);
				mTextMessages.get(row).setSenderName(cursor.getString(i));

				i = cursor.getColumnIndex(TextMessageHelper.COLUMN_RECEIVER_ID);
				mTextMessages.get(row).setReceiverId(cursor.getString(i));

				i = cursor
						.getColumnIndex(TextMessageHelper.COLUMN_RECEIVER_NAME);
				mTextMessages.get(row).setReceiverName(cursor.getString(i));

				i = cursor.getColumnIndex(TextMessageHelper.COLUMN_MESSAGE);
				mTextMessages.get(row).setMessage(cursor.getString(i));

				i = cursor.getColumnIndex(TextMessageHelper.COLUMN_CREATED_AT);
				mTextMessages.get(row).setCreatedAt(cursor.getString(i));

				i = cursor.getColumnIndex(TextMessageHelper.COLUMN_IS_SENT);
				boolean sent = false;
				if (cursor.getString(i).equals("true")) {
					sent = true;
				}
				//Log.i("SENT", "" + sent);
				mTextMessages.get(row).setSent(sent);

				cursor.moveToNext();
				row++;
			}
			return mTextMessages;
		}
	}
}
