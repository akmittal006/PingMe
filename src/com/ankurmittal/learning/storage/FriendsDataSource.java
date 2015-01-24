package com.ankurmittal.learning.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ankurmittal.learning.util.ParseConstants;
import com.parse.ParseObject;
import com.parse.ParseUser;

public class FriendsDataSource {
	private SQLiteDatabase mDatabase; // The actual DB!
	private FriendsHelper mFriendsHelper; // Helper class for creating and
											// opening the DB
	private Context mContext;

	public FriendsDataSource(Context context) {
		mContext = context;
		mFriendsHelper = new FriendsHelper(mContext);
	}

	/*
	 * Open the db. Will create if it doesn't exist
	 */
	public void open() throws SQLException {
		mDatabase = mFriendsHelper.getWritableDatabase();
	}

	/*
	 * We always need to close our db connections
	 */
	public void close() {
		mDatabase.close();
	}

	// INSERT
	public void insert(ParseUser friend) {

		Cursor cursor = isFriendNew(friend);
		if (cursor.getCount() == 0) {
			// we got a new frnd since cursor cud not find it
			// sendNotification(friend);
			mDatabase.beginTransaction();
			try {
				ContentValues values = new ContentValues();
				values.put(FriendsHelper.COLUMN_NAME, friend.getUsername());
				values.put(FriendsHelper.COLUMN_OBJECT_ID, friend.getObjectId());
				values.put(FriendsHelper.COLUMN_EMAIL, friend.getEmail());
				mDatabase.insert(FriendsHelper.TABLE_FRIENDS, null, values);
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

	private Cursor isFriendNew(ParseUser friend) {
		String whereClause = FriendsHelper.COLUMN_OBJECT_ID + " = ?";

		Cursor cursor = mDatabase.query(FriendsHelper.TABLE_FRIENDS, // table
				new String[] { FriendsHelper.COLUMN_OBJECT_ID }, // column names
				whereClause, // where clause
				new String[] { friend.getObjectId() }, // where params
				null, // groupby
				null, // having
				null // orderby
				);
		return cursor;
	}

	public void deleteAll() {
		mDatabase.delete(FriendsHelper.TABLE_FRIENDS, // table
				null, // where clause
				null // where params
				);
	}

	public Cursor selectAll() {

		Cursor cursor = mDatabase.query(FriendsHelper.TABLE_FRIENDS, // table
				new String[] { FriendsHelper.COLUMN_NAME,
						FriendsHelper.COLUMN_OBJECT_ID,
						FriendsHelper.COLUMN_EMAIL }, // column names
				null, // where clause
				null, // where params
				null, // groupby
				null, // having
				null // orderby
				);
		
		return cursor;
	}

	public ArrayList<ParseUser> getAllFriends() {
		
		Cursor cursor = selectAll();
		ArrayList<ParseUser> mFriends = new ArrayList<ParseUser>();

		if (cursor.getCount() == 0) {
			return null;
		} else {
			cursor.moveToFirst();
			int row = 0;
			while (!cursor.isAfterLast()) {
				
				ParseUser frnd = new ParseUser();
				int i = cursor.getColumnIndex(FriendsHelper.COLUMN_OBJECT_ID);
				
				frnd.put("UserId", cursor.getString(i));
				Log.d("Database check", "to add :" + cursor.getString(i));
				Log.d("Database check", "to add :" + frnd.getObjectId());
				// do stuff
				mFriends.add( frnd);
				i = cursor.getColumnIndex(FriendsHelper.COLUMN_NAME);
				mFriends.get(row).setUsername(cursor.getString(i));
				i = cursor.getColumnIndex(FriendsHelper.COLUMN_EMAIL);
				mFriends.get(row).setEmail(cursor.getString(i));

				cursor.moveToNext();
				row++;
				
			}
			return mFriends;
		}

	}
}
