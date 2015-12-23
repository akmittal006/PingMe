package com.ankurmittal.learning.storage;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ankurmittal.learning.util.CustomTarget;
import com.ankurmittal.learning.util.ParseConstants;
import com.parse.ParseUser;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

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
		if (!mDatabase.isOpen()) {
			open();
		}

		Cursor cursor = isFriendNew(friend);
		if (cursor.getCount() == 0) {
			// we got a new frnd since cursor cud not find it
			// sendNotification(friend);
			mDatabase.beginTransaction();
			try {
				ContentValues values = new ContentValues();
				values.put(FriendsHelper.COLUMN_USERNAME, friend.getUsername());
				values.put(FriendsHelper.COLUMN_OBJECT_ID, friend.getObjectId());
				values.put(FriendsHelper.COLUMN_EMAIL, friend.getEmail());
				values.put(FriendsHelper.COLUMN_NAME,friend.getString(ParseConstants.KEY_NAME));
				values.put(FriendsHelper.COLUMN_PHN_NUMBER,friend.getString(ParseConstants.KEY_PHN_NUM));
				if (friend.getParseFile(ParseConstants.KEY_PROFILE_IMAGE) != null) {
					values.put(
							FriendsHelper.COLUMN_PROFILE_IMAGE_ADDRESS,
							friend.getParseFile(
									ParseConstants.KEY_PROFILE_IMAGE).getUrl());
				} else {
					values.put(FriendsHelper.COLUMN_PROFILE_IMAGE_ADDRESS,
							"null");
				}

				mDatabase.insert(FriendsHelper.TABLE_FRIENDS, null, values);
				mDatabase.setTransactionSuccessful();
				// Log.d("INSERTED", "ROW ADDED");
			} finally {
				mDatabase.endTransaction();
			}
		} else {

			// Log.d(" NOT INSERTED", "ROW NOT ADDED");
		}

	}


	public Cursor isFriendNew(ParseUser friend) {
		if (!mDatabase.isOpen()) {
			open();
		}
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
		if (!mDatabase.isOpen()) {
			open();
		}
		mDatabase.delete(FriendsHelper.TABLE_FRIENDS, // table
				null, // where clause
				null // where params
				);

	}

	public Cursor selectAll() {
		if (!mDatabase.isOpen()) {
			open();
		}

		Cursor cursor = mDatabase.query(FriendsHelper.TABLE_FRIENDS, // table
				new String[] { FriendsHelper.COLUMN_USERNAME,
						FriendsHelper.COLUMN_OBJECT_ID,
						FriendsHelper.COLUMN_EMAIL,
						FriendsHelper.COLUMN_PROFILE_IMAGE_ADDRESS,
						FriendsHelper.COLUMN_NAME,
						FriendsHelper.COLUMN_PHN_NUMBER}, // column
																		// names
				null, // where clause
				null, // where params
				null, // groupby
				null, // having
				null // orderby
				);

		return cursor;
	}

	public String getImageUrlFromId(Context context, String id) {
		mFriendsHelper = new FriendsHelper(context);
		mDatabase = mFriendsHelper.getWritableDatabase();
		if (!mDatabase.isOpen()) {
			open();
		}
		String whereClause = FriendsHelper.COLUMN_OBJECT_ID + " = ?";

		Cursor cursor = mDatabase.query(FriendsHelper.TABLE_FRIENDS, // table
				new String[] { FriendsHelper.COLUMN_PROFILE_IMAGE_ADDRESS }, // column
																				// names
				whereClause, // where clause
				new String[] { id }, // where params
				null, // groupby
				null, // having
				null // orderby
				);
		if (cursor.getCount() > 0) {
			Log.i("frnds data source",
					"getting img url from id ... cursor count- "
							+ cursor.getCount());
			cursor.moveToFirst();
			return cursor
					.getString(cursor
							.getColumnIndex(FriendsHelper.COLUMN_PROFILE_IMAGE_ADDRESS));
		} else {
			Log.i("frnds data source",
					"getting img url from id ... cursor count- " + 0);
			return null;
		}

	}

	public int updateImageUrlFromId(Context context,
			final ArrayList<HashMap<String, String>> friends) {

		int ans = 0;
		mFriendsHelper = new FriendsHelper(context);
		mDatabase = mFriendsHelper.getWritableDatabase();
		if (!mDatabase.isOpen()) {
			open();
		}

		Log.e("frnds data source", "updating picss ");

		for (int i = 0; i < friends.size(); i++) {

			final HashMap<String, String> frnd = friends.get(i);

			Log.e("frnds data source", "thread running");

			String whereClause = FriendsHelper.COLUMN_OBJECT_ID + " = ?";
			ContentValues values = new ContentValues();
			values.put(FriendsHelper.COLUMN_PROFILE_IMAGE_ADDRESS,
					frnd.get("img_url"));
			int res = mDatabase.update(FriendsHelper.TABLE_FRIENDS, // table
					values, // column names
					whereClause, // where clause
					new String[] { frnd.get("id") });
			if (res > 0) {
				Log.e("frnds data ", "deleting and updating file");
				String middlePath = frnd.get("img_url").substring(93, 116);
				CustomTarget target = new CustomTarget(mContext);
				// Log.d("frnds adpater",user.getString("UserId"));
				target.setTargetHash(middlePath);
				// target.deleteFile();

				// TODO Auto-generated method stub
				Picasso.with(mContext).load(frnd.get("img_url"))
						.memoryPolicy(MemoryPolicy.NO_CACHE).into(target);

			}
			Log.e("frnds data source", "updated rows- " + res);
			// ans = ans + res;
		}

		return ans;

	}

	public ArrayList<ParseUser> getAllFriends() {
		if (!mDatabase.isOpen()) {
			open();
		}
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
				// Log.d("Database check", "to add :" + cursor.getString(i));
				// Log.d("Database check", "to add :" + frnd.getObjectId());
				// do stuff
				mFriends.add(frnd);
				i = cursor.getColumnIndex(FriendsHelper.COLUMN_USERNAME);
				mFriends.get(row).setUsername(cursor.getString(i));
				i = cursor.getColumnIndex(FriendsHelper.COLUMN_EMAIL);
				mFriends.get(row).setEmail(cursor.getString(i));
				i = cursor
						.getColumnIndex(FriendsHelper.COLUMN_PROFILE_IMAGE_ADDRESS);
				mFriends.get(row).put(ParseConstants.KEY_PROFILE_IMAGE,
						cursor.getString(i));
				i = cursor
						.getColumnIndex(FriendsHelper.COLUMN_PHN_NUMBER);
				mFriends.get(row).put(ParseConstants.KEY_PHN_NUM,
						cursor.getString(i));
				i = cursor
						.getColumnIndex(FriendsHelper.COLUMN_NAME);
				mFriends.get(row).put(ParseConstants.KEY_NAME,
						cursor.getString(i));
				Log.d("frnds data source",
						"profile url- " + cursor.getString(i));

				cursor.moveToNext();
				row++;

			}
			mDatabase.close();
			return mFriends;
		}

	}
}
