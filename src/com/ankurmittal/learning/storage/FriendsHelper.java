package com.ankurmittal.learning.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FriendsHelper extends SQLiteOpenHelper {
	
	/*
     * Table and column information
     */
    public static final String TABLE_FRIENDS = "FRIENDS";
    public static final String COLUMN_ID = "_ID";
    public static final String COLUMN_NAME = "NAME";
    public static final String COLUMN_OBJECT_ID = "OBJECT_ID";
    public static final String COLUMN_EMAIL = "EMAIL";
    public static final String COLUMN_PROFILE_IMAGE_ADDRESS = "PROFILE_IMAGE_ADDRESS";

    /*
     * Database information
     */
    private static final String DB_NAME = "ping_friends.db";
    private static final int DB_VERSION = 1; // Must increment to trigger an upgrade
    private static final String DB_CREATE =
            "CREATE TABLE " + TABLE_FRIENDS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_NAME + " TEXT,"+
                    COLUMN_OBJECT_ID + " TEXT,"+
                    COLUMN_PROFILE_IMAGE_ADDRESS + " TEXT,"+
                    COLUMN_EMAIL + " TEXT)";
    public FriendsHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DB_CREATE);
    }

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

}

