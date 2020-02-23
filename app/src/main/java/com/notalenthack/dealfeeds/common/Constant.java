package com.notalenthack.dealfeeds.common;

/**
 * Constants used across app and service
 */
public class Constant {
    public static boolean debug = true;
    public static String LOGTAG = "Tightwad: ";

    public static final String ARG_FEEDS = "feeds";
    public static final String ARG_FEED_ID = "feed_id";
    public static final String ARG_CATEGORY = "category";
    public static final String ARG_SEARCH = "search";
    public static final String ARG_ITEMS = "items";
    public static final String ARG_ITEM = "item";
    public static final String ARG_LINK = "link";

    public static final String UNKNOWN = "Unknown";

    public static final String TAB_SELECTED = "tabSelected";

    public static final String COMPARE_EBAY   = "compareEB";
    public static final String PULL_DATA_ON_STARTUP = "pullOnStartUp";
    public static final String PULL_DATA_ONCE_DAILY = "pullOnceDaily";
    public static final String PULL_DATA_TWICE_DAILY = "pullTwiceDaily";
    public static final String TIME_ONCE_DAILY = "timeOnceDaily";
    public static final String TIME_TWICE_DAILY = "timeTwiceDaily";
    public static final String KEEP_DATA_FOR_DAYS = "keepDays";

    // TAGs for dialogs
    public static final String TAG_TIME_PICKER_DIALOG = "timePickerDialog";
    public static final String TAG_NUMBER_PICKER_DIALOG = "numberPickerDialog";

    public static final int DEFAULT_KEEP_DATA_DAYS = 1;

    public static final int MILLISECS_IN_MINUTE = 60000;
    public static final int MILLISECS_IN_HOUR   = 60 * MILLISECS_IN_MINUTE;
}
