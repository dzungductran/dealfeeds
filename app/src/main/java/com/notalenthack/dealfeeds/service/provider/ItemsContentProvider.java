package com.notalenthack.dealfeeds.service.provider;

import java.util.Arrays;
import java.util.HashSet;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.notalenthack.dealfeeds.common.ItemsContract;

public class ItemsContentProvider extends ContentProvider {

    // database
    private ItemsSQLiteOpenHelper database;

    // Used for the UriMacher
    private static final int CRAPS = 10;
    private static final int CRAP_ID = 20;

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(ItemsContract.AUTHORITY, ItemsContract.CRAP_TABLE, CRAPS);
        sURIMatcher.addURI(ItemsContract.AUTHORITY, ItemsContract.CRAP_TABLE + "/#", CRAP_ID);
    }

    @Override
    public boolean onCreate() {
        database = new ItemsSQLiteOpenHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        // Uisng SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // Check if the caller has requested a column which does not exists
        checkColumns(projection);

        // Set the table
        queryBuilder.setTables(ItemsContract.CRAP_TABLE);

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case CRAPS:
                break;
            case CRAP_ID:
                // Adding the ID to the original query
                queryBuilder.appendWhere(ItemsContract.COLUMN_ID + "="
                        + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = database.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);
        // Make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        long id = 0;
        switch (uriType) {
            case CRAPS:
                id = sqlDB.insert(ItemsContract.CRAP_TABLE, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        Uri returnURI = ContentUris.withAppendedId(uri, id);

        getContext().getContentResolver().notifyChange(returnURI, null);
        return returnURI;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsDeleted = 0;
        switch (uriType) {
            case CRAPS:
                rowsDeleted = sqlDB.delete(ItemsContract.CRAP_TABLE, selection,
                        selectionArgs);
                break;
            case CRAP_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(ItemsContract.CRAP_TABLE,
                            ItemsContract.COLUMN_ID + "=" + id,
                            null);
                } else {
                    rowsDeleted = sqlDB.delete(ItemsContract.CRAP_TABLE,
                            ItemsContract.COLUMN_ID + "=" + id
                                    + " and " + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsUpdated = 0;
        switch (uriType) {
            case CRAPS:
                rowsUpdated = sqlDB.update(ItemsContract.CRAP_TABLE,
                        values,
                        selection,
                        selectionArgs);
                break;
            case CRAP_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDB.update(ItemsContract.CRAP_TABLE,
                            values,
                            ItemsContract.COLUMN_ID + "=" + id,
                            null);
                } else {
                    rowsUpdated = sqlDB.update(ItemsContract.CRAP_TABLE,
                            values,
                            ItemsContract.COLUMN_ID + "=" + id
                                    + " and "
                                    + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    private void checkColumns(String[] projection) {
        String[] available = { ItemsContract.COLUMN_CATEGORY, ItemsContract.COLUMN_DESCRIPTION,
                ItemsContract.COLUMN_ID, ItemsContract.COLUMN_IMAGE_LINK,
                ItemsContract.COLUMN_LINK, ItemsContract.COLUMN_NAME,
                ItemsContract.COLUMN_PRICE, ItemsContract.COLUMN_TITLE };
        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
            // Check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }

}