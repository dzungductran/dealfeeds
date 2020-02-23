package com.notalenthack.dealfeeds.service.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.notalenthack.dealfeeds.common.ItemsContract;

public class ItemsSQLiteOpenHelper extends SQLiteOpenHelper {

    public ItemsSQLiteOpenHelper(Context context) {
        super(context, ItemsContract.DATABASE_NAME, null, ItemsContract.DATABASE_VERSION);
    }

    // Method is called during creation of the database
    @Override
    public void onCreate(SQLiteDatabase database) {
        ItemsTable.onCreate(database);
    }

    // Method is called during an upgrade of the database,
    // e.g. if you increase the database version
    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion,
                          int newVersion) {
        ItemsTable.onUpgrade(database, oldVersion, newVersion);
    }
}

