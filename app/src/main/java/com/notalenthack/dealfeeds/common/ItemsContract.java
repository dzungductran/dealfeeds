package com.notalenthack.dealfeeds.common;

import android.net.Uri;

public class ItemsContract {

    public static final String DATABASE_NAME = "tightwad.db";
    public static final int DATABASE_VERSION = 4;

    // table
    public static final String CRAP_TABLE = "crap";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_FEED_ID = "feed_id";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_LINK = "uri";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_IMAGE_LINK = "image_uri";
    public static final String COLUMN_PRICE = "price";
    public static final String COLUMN_STATUS = "status";


    public static final String AUTHORITY = "com.notalenthack.dealfeeds.service.provider.crapcontentprovider";

    public static final Uri CONTENT_URI_CRAPS = Uri.parse("content://" + AUTHORITY + "/" + CRAP_TABLE);

    public static final int STATUS_UNKNOWN = 0;
    public static final int STATUS_NOTIFIED = 1;
    public static final int STATUS_SEEN = 2;
}
