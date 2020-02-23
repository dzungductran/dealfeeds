package com.notalenthack.dealfeeds.service.provider;

/**
 * Class to create all the tables for the database
 */

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.notalenthack.dealfeeds.common.ItemsContract;

public class ItemsTable {

    private static final String DATABASE_CREATE =
            "CREATE TABLE IF NOT EXISTS " + ItemsContract.CRAP_TABLE + " (" +
                    ItemsContract.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    ItemsContract.COLUMN_FEED_ID + " INTEGER NOT NULL, " +
                    ItemsContract.COLUMN_CATEGORY + " INTEGER NOT NULL, " +
                    ItemsContract.COLUMN_DATE + " INTEGER NOT NULL, " +
                    ItemsContract.COLUMN_STATUS + " INTEGER NOT NULL, " +       // notified, seen, ..etc
                    ItemsContract.COLUMN_LINK + " TEXT, " +
                    ItemsContract.COLUMN_TITLE + " TEXT, " +
                    ItemsContract.COLUMN_DESCRIPTION + " TEXT, " +
                    ItemsContract.COLUMN_NAME + " TEXT, " +
                    ItemsContract.COLUMN_IMAGE_LINK + " TEXT, " +
                    ItemsContract.COLUMN_PRICE + " TEXT);";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion,
                                 int newVersion) {
        Log.w(ItemsTable.class.getName(), "Upgrading database from version "
                + oldVersion + " to " + newVersion
                + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + ItemsContract.CRAP_TABLE);
        onCreate(database);
    }
}
