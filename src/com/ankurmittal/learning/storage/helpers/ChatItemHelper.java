package com.ankurmittal.learning.storage.helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ChatItemHelper extends SQLiteOpenHelper {
	
	private static ChatItemHelper sInstance;
	/*
     * Table and column information
     */
    public static final String TABLE_CHAT_ITEMS = " CHAT_ITEMS";
    public static final String COLUMN_ID = "_ID";
    public static final String COLUMN_SENDER_NAME = "SENDER_NAME";
    public static final String COLUMN_SENDER_ID = "SENDER_ID";
    public static final String COLUMN_SENDER_EMAIL = "SENDER_EMAIL";
    public static final String COLUMN_SENDER_IMG = "SENDER_IMG";
    public static final String COLUMN_SENDER_CONTACT = "SENDER_CONTACT";
    public static final String COLUMN_LAST_MESSAGE = "LAST_MESSAGE";
    public static final String COLUMN_CREATED_AT = "CREATED_AT";
    public static final String COLUMN_IS_SENT = "IS_SENT";

    /*
     * Database information
     */
    private static final String DB_NAME = "chat_items.db";
    private static final int DB_VERSION = 1; // Must increment to trigger an upgrade
    private static final String DB_CREATE =
            "CREATE TABLE " + TABLE_CHAT_ITEMS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_SENDER_NAME + " TEXT,"+
                    COLUMN_SENDER_ID + " TEXT,"+
                    COLUMN_SENDER_EMAIL + " TEXT,"+
                    COLUMN_SENDER_IMG + " TEXT,"+
                    COLUMN_SENDER_CONTACT + " TEXT,"+
                    COLUMN_CREATED_AT + " DATETIME,"+
                    COLUMN_IS_SENT + " TEXT," +
                    COLUMN_LAST_MESSAGE + " TEXT)";
//    private static final String DB_ALTER =
//            "ALTER TABLE " + TABLE_CHAT_ITEMS + " ADD COLUMN " + COLUMN_IS_SENT + " TEXT" ;
   
    
	public static synchronized ChatItemHelper getInstance(Context context) {

		// Use the application context, which will ensure that you
		// don't accidentally leak an Activity's context.
		// See this article for more information: http://bit.ly/6LRzfx
		if (sInstance == null) {
			sInstance = new ChatItemHelper(context.getApplicationContext());
		}
		return sInstance;
	}
  
    private ChatItemHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    	Log.d("sql", "executing");
        db.execSQL(DB_CREATE);
    }

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		//db.execSQL(DB_ALTER);
	}

}

