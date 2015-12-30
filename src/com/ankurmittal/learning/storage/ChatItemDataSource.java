package com.ankurmittal.learning.storage;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ankurmittal.learning.storage.helpers.ChatItemHelper;
import com.ankurmittal.learning.storage.helpers.TextMessageHelper;

public class ChatItemDataSource {
	private SQLiteDatabase mDatabase; // The actual DB!
	private ChatItemHelper mChatItemHelper; // Helper class for creating
	private FriendsDataSource frndsDataSource;
	private static ChatItemDataSource mInstance;
	// and
	// opening the DB
	private Context mContext;

	public static synchronized ChatItemDataSource getInstance(Context context) {

		// Use the application context, which will ensure that you
		// don't accidentally leak an Activity's context.
		// See this article for more information: http://bit.ly/6LRzfx
		if (mInstance == null) {
			mInstance = new ChatItemDataSource(context.getApplicationContext());
		}
		return mInstance;
	}

	private ChatItemDataSource(Context context) {
		mContext = context;
		mChatItemHelper = ChatItemHelper.getInstance(mContext);
		open();
	}

	/*
	 * Open the db. Will create if it doesn't exist
	 */
	public void open() throws SQLException {
		mDatabase = mChatItemHelper.getWritableDatabase();
	}

	/*
	 * We always need to close our db connections
	 */
	public void close() {
		if (mDatabase != null) {
			mDatabase.close();
		}
		Log.e("Chat ItemDatabse check", "database closed");
	}

	// INSERT
	public void insert(ChatItem chatItem) {
		//
		// if (!mDatabase.isOpen()) {
		// open();
		// }

		Cursor cursor = isChatItemNew(chatItem);
		if (cursor.getCount() == 0) {
			// we got a new chatItem since cursor cud not find it
			Log.d("DATA SOURCE", "INSERTING NEW ChatItem" + chatItem);
			String imgUrl;
			String lastMessage;
			frndsDataSource = FriendsDataSource.getInstance(mContext);
			if (frndsDataSource.getImageUrlFromId(mContext, chatItem.getId()) != null) {
				imgUrl = frndsDataSource.getImageUrlFromId(mContext,
						chatItem.getId());
			} else {
				imgUrl = "null";
			}

			// sendNotification(friend);

			mDatabase.beginTransaction();
			try {
				ContentValues values = new ContentValues();
				values.put(ChatItemHelper.COLUMN_SENDER_ID, chatItem.getId());
				values.put(ChatItemHelper.COLUMN_SENDER_NAME,
						chatItem.getContent());
				// values.put(ChatItemHelper.COLUMN_SENDER_EMAIL,
				// null);
				values.put(ChatItemHelper.COLUMN_SENDER_IMG, imgUrl);
				// values.put(ChatItemHelper.COLUMN_SENDER_CONTACT,
				// chatItem.getMessageId());
				if (chatItem.getLastMessage() != null) {
					values.put(ChatItemHelper.COLUMN_LAST_MESSAGE, chatItem
							.getLastMessage().getMessage());

					Log.d("inseting", chatItem.getContent());

					values.put(ChatItemHelper.COLUMN_CREATED_AT, chatItem
							.getLastMessage().getCreatedAtString());
				}

				// values.put(ChatItemHelper.COLUMN_IS_SENT,
				// chatItem.isSent() + "");

				// friend.setViewed(false);
				mDatabase.insert(ChatItemHelper.TABLE_CHAT_ITEMS, null, values);
				mDatabase.setTransactionSuccessful();
				Log.d("INSERTED", "Chat Item ADDED");
			} finally {
				mDatabase.endTransaction();
			}
		} else {

			Log.d(" NOT INSERTED", "ROW NOT ADDED");
		}

	}

	public Cursor isChatItemNew(ChatItem chatItem) {

		String whereClause = ChatItemHelper.COLUMN_SENDER_ID + " = ?";
		//
		// if (mDatabase.isOpen()) {
		// open();
		// }
		Cursor cursor = mDatabase.query(ChatItemHelper.TABLE_CHAT_ITEMS, // table
				new String[] { ChatItemHelper.COLUMN_SENDER_ID }, // column
																	// names
				whereClause, // where clause
				new String[] { chatItem.getId() }, // where params
				null, // groupby
				null, // having
				null // orderby
				);
		return cursor;

	}

	public ChatItem getChatItemFromId(String id) {
		String whereClause = ChatItemHelper.COLUMN_SENDER_ID + " = ?";
		//
		// if (mDatabase.isOpen()) {
		// open();
		// }
		Cursor cursor = mDatabase.query(ChatItemHelper.TABLE_CHAT_ITEMS, // table
				new String[] { ChatItemHelper.COLUMN_SENDER_ID,
						ChatItemHelper.COLUMN_ID,
						ChatItemHelper.COLUMN_SENDER_NAME,
						ChatItemHelper.COLUMN_LAST_MESSAGE,
						ChatItemHelper.COLUMN_SENDER_CONTACT,
						ChatItemHelper.COLUMN_SENDER_IMG,
						ChatItemHelper.COLUMN_IS_SENT,
						ChatItemHelper.COLUMN_CREATED_AT }, // column
															// names
				whereClause, // where clause
				new String[] { id }, // where params
				null, // groupby
				null, // having
				null // orderby
				);

		cursor.moveToFirst();

		ChatItem chatItem = new ChatItem();

		// int i = cursor.getColumnIndex(ChatItemHelper.COLUMN_SENDER_IMG);
		// chatItem.setImgUrl(cursor.getString(i));
		// Log.e("MChatItem data source", "url-" + cursor.getString(i));
		// do stuff

		int i = cursor.getColumnIndex(ChatItemHelper.COLUMN_SENDER_ID);
		chatItem.setId(cursor.getString(i));

		TextMessageDataSource mMessageDataSource = TextMessageDataSource
				.getInstance(mContext);
		if (chatItem.mMessages == null) {
			chatItem.mMessages = new ArrayList<TextMessage>();
		}
		chatItem.mMessages.clear();
		chatItem.mMessages = mMessageDataSource.getMessagesFrom(cursor
				.getString(i));

		i = cursor.getColumnIndex(ChatItemHelper.COLUMN_SENDER_NAME);
		chatItem.setContent(cursor.getString(i));

		// i =
		// cursor.getColumnIndex(ChatItemHelper.COLUMN_SENDER_EMAIL);
		// mChatItems.get(row).setReceiverId(cursor.getString(i));

		// i = cursor
		// .getColumnIndex(ChatItemHelper.COLUMN_SENDER_CONTACT);
		// mChatItems.get(row).setReceiverName(cursor.getString(i));

		// i = cursor.getColumnIndex(ChatItemHelper.COLUMN_LAST_MESSAGE);
		// if (cursor.getString(i) != null) {
		// Log.i("quick check", cursor.getString(i));
		// chatItem.setLastMessage(cursor.getString(i));
		// }
		//
		// i = cursor.getColumnIndex(ChatItemHelper.COLUMN_CREATED_AT);
		// if (cursor.getString(i) != null) {
		// chatItem.setLastMessageCreatedAt(cursor.getString(i));
		// }
		// update chat items map also
		if (ChatContent.ITEM_MAP.containsKey(chatItem.id)) {
			ChatContent.ITEM_MAP.put(chatItem.id, chatItem);
		}

		return chatItem;
	}

	public void deleteAll() {
		mDatabase.delete(ChatItemHelper.TABLE_CHAT_ITEMS, // table
				null, // where clause
				null // where params
				);
	}

	public void deleteChatItem(String id) {
		String whereClause = ChatItemHelper.COLUMN_SENDER_ID + " = ?";
		mDatabase.delete(ChatItemHelper.TABLE_CHAT_ITEMS, // table
				whereClause, // where clause
				new String[] { id } // where params
				);

	}

	public Cursor selectAll() {

		// if (!mDatabase.isOpen()) {
		// Log.i("chat item data source", "opening before select all method");
		// open();
		// }
		Cursor cursor = mDatabase.query(ChatItemHelper.TABLE_CHAT_ITEMS, // table
				new String[] { ChatItemHelper.COLUMN_SENDER_ID,
						ChatItemHelper.COLUMN_ID,
						ChatItemHelper.COLUMN_SENDER_NAME,
						ChatItemHelper.COLUMN_LAST_MESSAGE,
						ChatItemHelper.COLUMN_SENDER_CONTACT,
						ChatItemHelper.COLUMN_SENDER_IMG,
						ChatItemHelper.COLUMN_IS_SENT,
						ChatItemHelper.COLUMN_CREATED_AT }, // column names
				null, // where clause
				null, // where params
				null, // groupby
				null, // having
				null // orderby
				);

		return cursor;
	}

	public ArrayList<ChatItem> getAllChatItems() {

		Cursor cursor = selectAll();
		// ChatContent.ITEM_MAP.clear();
		ArrayList<ChatItem> mChatItems = new ArrayList<ChatItem>();

		if (cursor.getCount() == 0) {
			return null;
		} else {
			cursor.moveToFirst();
			int row = 0;
			while (!cursor.isAfterLast()) {

				TextMessageDataSource mMessageDataSource = TextMessageDataSource
						.getInstance(mContext);

				ChatItem chatItem = new ChatItem();

				int i = cursor.getColumnIndex(ChatItemHelper.COLUMN_SENDER_IMG);
				chatItem.setImgUrl(cursor.getString(i));
				Log.e("MChatItem data source", "url-" + cursor.getString(i));
				// do stuff
				mChatItems.add(chatItem);

				i = cursor.getColumnIndex(ChatItemHelper.COLUMN_SENDER_ID);
				mChatItems.get(row).setId(cursor.getString(i));

				if (chatItem.mMessages == null) {
					chatItem.mMessages = new ArrayList<TextMessage>();
				}
				chatItem.mMessages.clear();
				chatItem.mMessages = mMessageDataSource.getMessagesFrom(cursor
						.getString(i));

				i = cursor.getColumnIndex(ChatItemHelper.COLUMN_SENDER_NAME);
				mChatItems.get(row).setContent(cursor.getString(i));

				// i =
				// cursor.getColumnIndex(ChatItemHelper.COLUMN_SENDER_EMAIL);
				// mChatItems.get(row).setReceiverId(cursor.getString(i));

				// i = cursor
				// .getColumnIndex(ChatItemHelper.COLUMN_SENDER_CONTACT);
				// mChatItems.get(row).setReceiverName(cursor.getString(i));

				i = cursor.getColumnIndex(ChatItemHelper.COLUMN_LAST_MESSAGE);
				if (cursor.getString(i) != null) {
					Log.i("quick check", cursor.getString(i));
					mChatItems.get(row).setLastMessage(cursor.getString(i));
				}

				i = cursor.getColumnIndex(ChatItemHelper.COLUMN_CREATED_AT);
				if (cursor.getString(i) != null) {
					mChatItems.get(row).setLastMessageCreatedAt(
							cursor.getString(i));
				}
				// update chat items map also
				if (ChatContent.ITEM_MAP.containsKey(mChatItems.get(row).id)) {
					ChatContent.ITEM_MAP.put(mChatItems.get(row).id,
							mChatItems.get(row));
				}

				Log.d("ChatItem -- check", "retrieving :"
						+ mChatItems.get(row).getContent());

				cursor.moveToNext();
				row++;

			}
			// for (int i=0;i< mTextMessages.size(); i++) {
			// //Log.d("Database check", "added" +
			// mTextMessages.get(i).getSenderName());
			// }

			return mChatItems;
		}

	}

	public void upadteLastMessage(String id, TextMessage lastMessage) {
		// if (!mDatabase.isOpen()) {
		// open();
		// }
		String whereClause = ChatItemHelper.COLUMN_SENDER_ID + " = ?";

		ContentValues values = new ContentValues();
		values.put(ChatItemHelper.COLUMN_LAST_MESSAGE, lastMessage.getMessage());
		mDatabase.update(TextMessageHelper.TABLE_MESSAGES, // table
				values, // values
				whereClause, // where clause
				new String[] { id } // where params
				);
		// mDatabase.close();
	}

	public int updateImageUrlFromId(Context context,
			final ArrayList<HashMap<String, String>> friends) {

		int ans = 0;
		// mChatItemHelper = ChatItemHelper.getInstance(context);
		// mDatabase = mChatItemHelper.getWritableDatabase();
		// if (!mDatabase.isOpen()) {
		// open();
		// }

		Log.e("ChatItem data source", "updating picss ");

		for (int i = 0; i < friends.size(); i++) {

			final HashMap<String, String> frnd = friends.get(i);

			String whereClause = ChatItemHelper.COLUMN_SENDER_ID + " = ?";
			ContentValues values = new ContentValues();
			values.put(ChatItemHelper.COLUMN_SENDER_IMG, frnd.get("img_url"));
			int res = mDatabase.update(ChatItemHelper.TABLE_CHAT_ITEMS, // table
					values, // column names
					whereClause, // where clause
					new String[] { frnd.get("id") });
			if (res > 0) {
				Log.e("frnds data ", "deleting and updating file");
				// String middlePath = frnd.get("img_url").substring(93, 116);
				// CustomTarget target = new CustomTarget(mContext);
				// target.setTargetHash(middlePath);
				// // TODO Auto-generated method stub
				// Picasso.with(context).load(frnd.get("img_url"))
				// .memoryPolicy(MemoryPolicy.NO_CACHE).into(target);

			}
			Log.e("cht item data source", "updated rows- " + res);
			// ans = ans + res;
		}

		return ans;

	}

}
