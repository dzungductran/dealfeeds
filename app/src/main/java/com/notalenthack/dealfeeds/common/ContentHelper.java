package com.notalenthack.dealfeeds.common;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import com.notalenthack.dealfeeds.model.Product;

/**
 * Provides helper functions to access content provider. This is a
 * singleton class with one instance access to content provider, which should provide thread safe
 * access
 */
public class ContentHelper {

    public static final String[] PROJECTION = {ItemsContract.COLUMN_ID};
    public static final String SELECTION_LINK = "(" + ItemsContract.COLUMN_LINK + "=?)";
    public static final String SELECTION_ID = "(" + ItemsContract.COLUMN_ID + "=?)";
    public static final String SELECTION_CATEGORY = "(" + ItemsContract.COLUMN_CATEGORY + "=?)";
    public static final String SELECTION_FEED_ID = "(" + ItemsContract.COLUMN_FEED_ID + "=?)";
//    public static final String SELECTION_IMAGE_URI = "(" + ItemsContract.COLUMN_IMAGE_LINK + " = ?)";


    private static ContentHelper sInstance = null;  // instance
    private Context mContext = null;             // context

    private ContentHelper(Context context) {
        mContext = context;
    }

    // Get the instance for this class
    public static synchronized ContentHelper getInstance(Context context) {
        if (sInstance == null) {
            if (context == null) {
                Log.e(Constant.LOGTAG, "No Context");
                return null;    // Oops need context
            }
            sInstance = new ContentHelper(context);
        }

        return sInstance;
    }

    // Check to see if a certain item exist already in the database.
    public long exist(String link) {
        long returnID = -1;
        String[] selectionArgs = new String[]{link};

        // query for the link in the database
        Cursor cursor = mContext.getContentResolver().query(ItemsContract.CONTENT_URI_CRAPS,
                PROJECTION, SELECTION_LINK, selectionArgs, null);
        if (cursor.moveToNext()) {
            returnID = cursor.getLong(cursor.getColumnIndex(ItemsContract.COLUMN_ID));
        }
        cursor.close();
        return returnID;
    }

    public int delete() {
        return  mContext.getContentResolver().delete(ItemsContract.CONTENT_URI_CRAPS,
                null, null);
    }

    // Delete a item in the database
    public int delete(long id) {
        String[] selectionArgs = {String.valueOf(id)};
        int numDeleted = mContext.getContentResolver().delete(ItemsContract.CONTENT_URI_CRAPS,
                SELECTION_ID, selectionArgs);
        return numDeleted;
    }

    // delete base on a selection
    public int delete(String selection, int selID) {
        String[] selectionArgs = {String.valueOf(selID)};
        int numDeleted = mContext.getContentResolver().delete(ItemsContract.CONTENT_URI_CRAPS,
                selection, selectionArgs);
        return numDeleted;
    }

    // Insert a item in the database.
    public long insert(ContentValues values) {
        Uri uri = mContext.getContentResolver().insert(ItemsContract.CONTENT_URI_CRAPS, values);
        return (Long.valueOf(uri.getLastPathSegment()));
    }

    public int update(ContentValues values, String selection, String[] selectionArgs) {
        int num = mContext.getContentResolver().update(ItemsContract.CONTENT_URI_CRAPS, values,
                selection, selectionArgs);
        return num;
    }

    // Get the cursor
    public Cursor getCursor(String selection, int selID) {
        // Create the cursor
        Cursor cursor = mContext.getContentResolver().query(ItemsContract.CONTENT_URI_CRAPS,
                null, selection, new String[]{String.valueOf(selID)}, null);
        return cursor;
    }

    public Cursor getCursor() {
        // Create the cursor
        Cursor cursor = mContext.getContentResolver().query(ItemsContract.CONTENT_URI_CRAPS, null, null, null, null);
        return cursor;
    }

    public Product getProduct(long id) {
        Cursor cursor = mContext.getContentResolver().query(ItemsContract.CONTENT_URI_CRAPS,
                null, SELECTION_ID, new String[]{String.valueOf(id)}, null);
        if (cursor.moveToFirst()) {
            Product product = new Product(cursor);
            cursor.close();
            return product;
        } else {
            return null;
        }
    }
}
