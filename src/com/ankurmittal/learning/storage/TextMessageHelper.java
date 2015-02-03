package com.ankurmittal.learning.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TextMessageHelper extends SQLiteOpenHelper {
	
	/*
     * Table and column information
     */
    public static final String TABLE_MESSAGES = "TEXT_MESSGAES";
    public static final String COLUMN_ID = "_ID";
    public static final String COLUMN_SENDER_NAME = "SENDER_NAME";
    public static final String COLUMN_SENDER_ID = "SENDER_ID";
    public static final String COLUMN_RECEIVER_NAME = "RECEIVER_NAME";
    public static final String COLUMN_RECEIVER_ID = "RECEIVER_ID";
    public static final String COLUMN_MESSAGE_ID = "MESSAGE_ID";
    public static final String COLUMN_MESSAGE = "MESSAGE";
    public static final String COLUMN_CREATED_AT = "CREATED_AT";
    public static final String COLUMN_IS_SENT = "IS_SENT";

    /*
     * Database information
     */
    private static final String DB_NAME = "ping_text_messages.db";
    private static final int DB_VERSION = 3; // Must increment to trigger an upgrade
    private static final String DB_CREATE =
            "CREATE TABLE " + TABLE_MESSAGES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_SENDER_NAME + " TEXT,"+
                    COLUMN_SENDER_ID + " TEXT,"+
                    COLUMN_RECEIVER_NAME + " TEXT,"+
                    COLUMN_RECEIVER_ID + " TEXT,"+
                    COLUMN_MESSAGE_ID + " TEXT,"+
                    COLUMN_CREATED_AT + " DATETIME,"+
                    COLUMN_IS_SENT + " TEXT," +
                    COLUMN_MESSAGE + " TEXT)";
    private static final String DB_ALTER =
            "ALTER TABLE " + TABLE_MESSAGES + " ADD COLUMN " + COLUMN_IS_SENT + " TEXT" ;
   
    public TextMessageHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DB_CREATE);
    }

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		//db.execSQL(DB_ALTER);
	}

}

