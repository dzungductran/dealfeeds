package com.notalenthack.dealfeeds.service;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.*;
import android.os.*;
import android.preference.PreferenceManager;
import android.util.Log;

import com.notalenthack.dealfeeds.appl.BrowserActivity;
import com.notalenthack.dealfeeds.appl.ItemListActivity;
import com.notalenthack.dealfeeds.common.Constant;
import com.notalenthack.dealfeeds.common.ItemsContract;
import com.notalenthack.dealfeeds.service.rssfeed.RssFeedHandler;
import com.notalenthack.dealfeeds.service.notify.*;

import java.text.SimpleDateFormat;
import java.util.*;

/* This class starts the service to content provider */
public class TightwadService extends Service {

    // Broadcast receiver for receiving intents
    private BroadcastReceiver mReceiver = null;

    // This instance
    private static TightwadService sInstance;

    // List of feeds
    //List<String> mFeeds = new ArrayList<String>();
    Set<String> mFeeds = new HashSet<String>();

    private RssFeedHandler mFeedHandler = null;
    private SearchHandler mSearchHandler = null;
    private OnNewItemListener mNewItemListener = null;
    private SettingData mSettings;
    private IStatusListener mListener = null;

    private ITightwadAPI.Stub apiEndpoint = new ITightwadAPI.Stub() {

        /**
         * Refresh the feeds
         */
        @Override
        public void refresh() {
            for (String feed : mFeeds) {
                if (Constant.debug) Log.d(Constant.LOGTAG, "Refreshing feed " + feed);
                if (mFeedHandler != null) {
                    mFeedHandler.request(feed);
                } else {
                    Log.e(Constant.LOGTAG, "Bad feed handler");
                }
            }
        }

        /**
         * Refresh the feeds
         * @param feed - the feed to pull from
         * @return nothing
         */
        @Override
        public void refreshFeed( String feed ) {
            // First add the feed just in case it is not in the list
            addFeed(feed);
            if (Constant.debug) Log.d(Constant.LOGTAG, "Refreshing feed " + feed);
            if (mFeedHandler != null) {
                mFeedHandler.request(feed);
            } else {
                Log.e(Constant.LOGTAG, "Bad feed handler");
            }
        }

        /**
         * Add feed into the pulling cycle
         * @param feed - the feed to pull from
         * @return nothing
         */
        @Override
        public void addFeed( String feed ) {
            if (mFeeds.contains(feed) == false) {
                mFeeds.add(feed);
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(sInstance);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putStringSet(Constant.ARG_FEEDS, mFeeds); // value to store
                editor.apply();
            }
        }

        /**
         * Remove the feed from pulling
         * @param feed - the feed to remove
         * @return nothing
         */
        @Override
        public void removeFeed( String feed ) {
            if (mFeeds.contains(feed) == true) {
                mFeeds.remove(feed);
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(sInstance);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putStringSet(Constant.ARG_FEEDS, mFeeds); // value to store
                editor.apply();
            }
        }

        /**
         * Store settings in preferences
         * @param settings
         */
        public void setSettings(SettingData settings) {
            saveSettings(settings);
        }

        /**
         * Get the settings and return
         * @return
         */
        public SettingData getSettings() {
            return mSettings;
        }

        /**
         * Add a listener to receive status
         */
        public void addListener(IStatusListener listener) {
            mListener = listener;
        }

        /**
         * Remove the listener
         */
        public void removeListener() {
            mListener = null;
        }
    };

    /**
     * @param intent
     * @return
     */
    @Override
    public IBinder onBind(Intent intent) {
        if (Constant.debug) {
            Log.d(Constant.LOGTAG, "onBind " + intent);
            Log.d(Constant.LOGTAG, "intent.getAction " + intent.getAction());
            Log.d(Constant.LOGTAG, "intent.getComponent " + intent.getComponent());
            Log.d(Constant.LOGTAG, "name " + TightwadService.class.getName() );
        }

        if (TightwadService.class.getName().equals(intent.getAction())) {
            return apiEndpoint;
        } else {
            return null;
        }
    }

    /**
     * Store settings in preferences
     * @param settings
     */
    private void saveSettings(SettingData settings) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(sInstance);
        SharedPreferences.Editor editor = preferences.edit();

        String oldSearchStrings[] = mSettings.getCurrentSearchTerms();
        String searchStrings[] = settings.getCurrentSearchTerms();

        if (!Arrays.equals(searchStrings, oldSearchStrings)) {
            if (searchStrings != null && searchStrings.length > 0) {
            // Need to convert String[] to Set<String> for storage in Prefs
                Set<String> searchSet = new HashSet<String>();

                for (int searchStringIndex=0; searchStringIndex < searchStrings.length; searchStringIndex++) {
                    if (Constant.debug) Log.d(Constant.LOGTAG, "Adding Term ("+searchStrings[searchStringIndex]+") to set");
                    searchSet.add(searchStrings[searchStringIndex]);
                }
                editor.putStringSet(Constant.ARG_SEARCH, searchSet); // value to store
                mSearchHandler.onNewSearchTerm(searchSet);
            } else {
                editor.putStringSet(Constant.ARG_SEARCH, null);
                mSearchHandler.onNewSearchTerm(null);
            }
            mSettings.setCurrentSearchTerms(settings.getCurrentSearchTerms());
        }

        if (settings.isCompareWithEbay() != mSettings.isCompareWithEbay()) {
            editor.putBoolean(Constant.COMPARE_EBAY, settings.isCompareWithEbay());
            mSettings.setCompareWithEbay(settings.isCompareWithEbay());
        }

        if (settings.isPullDataOnStartup() != mSettings.isPullDataOnStartup()) {
            editor.putBoolean(Constant.PULL_DATA_ON_STARTUP, settings.isPullDataOnStartup());
            mSettings.setPullDataOnStartup(settings.isPullDataOnStartup());
        }

        if (Constant.debug) Log.d(Constant.LOGTAG, "Storing search term set to prefs");
        editor.apply();
    }


    /**
     * Get the settings and return
     * @return
     */
    private SettingData restoreSettings() {
        SettingData settings = new SettingData();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(sInstance);

        // Get the feeds
        mFeeds = preferences.getStringSet(Constant.ARG_FEEDS, mFeeds);

        Set<String> searchSet = preferences.getStringSet(Constant.ARG_SEARCH, new HashSet<String>());
        // Need to convert Set<String> to String[] for marshalling via parcelable interface
        if (searchSet != null) {
            String[] searchStrings = new String[searchSet.size()];
            if (Constant.debug) Log.d(Constant.LOGTAG, "Fetching " + searchSet.size() + " terms from prefs");
            settings.setCurrentSearchTerms(searchSet.toArray(searchStrings));
        }

        settings.setPullDataOnStartup(preferences.getBoolean(Constant.PULL_DATA_ON_STARTUP, settings.isPullDataOnStartup()));
        settings.setCompareWithEbay(preferences.getBoolean(Constant.COMPARE_EBAY, settings.isCompareWithEbay()));

        return settings;
    }

    /*
     * Set repeating schedule for delete old cards from database depending on the number of days
     */
    private void setScheduleForCleanup(int days) {

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 01);
        calendar.set(Calendar.HOUR_OF_DAY, 00);
        calendar.set(Calendar.MINUTE, 00);          // midnight
        calendar.set(Calendar.SECOND, 00);

        if (Constant.debug) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Log.d(Constant.LOGTAG, "Starting AlarmManager for deleting old feeds " + formatter.format(calendar.getTimeInMillis()));
        }

        AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent();
        intent.setAction(TightwadClient.ACTION_CLEAN_UP);
        PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
        am.cancel(pi);

        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY * days, pi);
    }

    /* Start up the service */
    private void onStartService() {

        mSettings = restoreSettings();  // get settings on startup
    }

    // Delete old feeds  before today midnight
    @SuppressLint("Wakelock")
    private int cleanUp() {
        /* Acquire wakelock */
        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "dealfeeds:Tightwad Service");
        // Acquire the lock
        wl.acquire(300000);  // 5 mins

        // delete everything
        int rowdel = getContentResolver().delete(ItemsContract.CONTENT_URI_CRAPS, null, null);

        if (Constant.debug) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Log.d(Constant.LOGTAG, "Deleted old feeds " + rowdel + " date: " + sdf.format(new Date(System.currentTimeMillis())));
        }

        wl.release(); // release wake lock
        return rowdel;  // number of cards deleted
    }

    // Refresh feeds
    @SuppressLint("Wakelock")
    private void refreshFeeds() {
        /* Acquire wakelock */
        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "dealfeeds:Tightwad Service");
        // Acquire the lock
        wl.acquire(300000); // 5mins

        for (String feed : mFeeds) {
            if (Constant.debug) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                Log.d(Constant.LOGTAG, "Refreshing feed " + feed + " date: " +  sdf.format(new Date(System.currentTimeMillis())));
            }
            if (mFeedHandler != null) {
                mFeedHandler.request(feed);
            } else {
                Log.e(Constant.LOGTAG, "Bad feed handler");
            }
        }

        if (Constant.debug) Log.d(Constant.LOGTAG, "Refresh feeds done");
        wl.release(); // release wake lock
    }

    /**
     * This is routine executed when the service start up. This is when we create all instances of classes
     * that we need to run the service. We also register for Intents to listen to.
     */
    private void onCreateActions() {
        sInstance = this;

        mFeedHandler = new RssFeedHandler(getApplicationContext());

        final IntentFilter filter = new IntentFilter();
        filter.addAction(TightwadClient.ACTION_CLEAN_UP);
        filter.addAction(TightwadClient.ACTION_ITEM_DISMISS);
        filter.addAction(TightwadClient.ACTION_ITEM_ROUTE);
        filter.addAction(TightwadClient.ACTION_PULL_DATA);

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Constant.debug) Log.d(Constant.LOGTAG, "onReceive intent " + intent.toString());
                // Got intent to clean up
                if (intent.getAction().equals(TightwadClient.ACTION_CLEAN_UP)) {
                    int row = cleanUp();
                    if (Constant.debug) Log.d(Constant.LOGTAG, "feeds deleted " + row);
                    // TODO: should we refresh the feeds after delete?
                    refreshFeeds();
                }
                if (intent.getAction().equals(TightwadClient.ACTION_ITEM_DISMISS)) {
                    // clear notification
                    ItemNotify.getInstance(context).clearNotification();
                }
                if (intent.getAction().equals(TightwadClient.ACTION_PULL_DATA)) {
                    if (Constant.debug) Log.d(Constant.LOGTAG, "refesh feed " + intent.getAction());
                    refreshFeeds();
                }
                if (intent.getAction().equals(TightwadClient.ACTION_ITEM_ROUTE)) {
                    ItemNotify.getInstance(context).clearNotification();
                    // Create a new Intent
                    String link = intent.getStringExtra(Constant.ARG_LINK);
                    if (link != null && !link.isEmpty()) {
                        Intent launchIntent = new Intent(getBaseContext(), BrowserActivity.class);
                        launchIntent.putExtra(Constant.ARG_LINK, link);
                        launchIntent.setAction(TightwadClient.ACTION_ITEM_DETAIL);
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getApplication().startActivity(launchIntent);
                    } else {
                        Intent launchIntent = new Intent(getBaseContext(), ItemListActivity.class);
                        launchIntent.putExtras(intent.getExtras());
                        launchIntent.setAction(TightwadClient.ACTION_ITEM_LIST);
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getApplication().startActivity(launchIntent);
                    }

                }
            }
        };
        registerReceiver(mReceiver, filter);

        // Start service
        onStartService();

        mSearchHandler = new SearchHandler(getApplicationContext());
        mNewItemListener = new OnNewItemListener(getApplicationContext(), mSearchHandler);

        Thread searchHandler = new Thread(mSearchHandler);
        searchHandler.start();

        mFeedHandler = new RssFeedHandler(getApplicationContext());
        Thread feedHandler = new Thread(mFeedHandler);
        feedHandler.start();
    }

    /**
     * This routine is part of service lifecycle and get call when the service starts up. This is when we
     * create all our class object. This is done by calling
     *
     * @param intent the intent to create this service with
     * @param flags additional data
     * @param startId unique integer
     * @return flag indicates that this what kind of service is this.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Constant.debug) Log.d(Constant.LOGTAG, "onStartCommand( " + intent + " , " + flags + " , " + startId + " )");
        if (sInstance == null) {
            if (Constant.debug) Log.d(Constant.LOGTAG, "On Start service");
            onCreateActions();
        }

        return START_STICKY;
    }

    /* Clean up */
    @Override
    public void onDestroy() {
        if (mFeedHandler != null) {
            mFeedHandler.stop();
        }
        if (mSearchHandler != null) {
            mSearchHandler.stop();
        }
        if (mNewItemListener != null) {
            mNewItemListener.onClose();
        }
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }

        // Wait for handler to die
        long waitingStartTime = System.currentTimeMillis();
        while ((mFeedHandler != null && mFeedHandler.isRunning()) ||
                (mSearchHandler != null && mSearchHandler.isRunning())) {
            if (System.currentTimeMillis() - waitingStartTime < 3000L) {   // wait for 3 secs
                try {
                    Thread.currentThread().sleep(1000L);
                } catch (InterruptedException ex) {
                    break;
                }
            } else {
                break;
            }
        }

        super.onDestroy();
    }

}
