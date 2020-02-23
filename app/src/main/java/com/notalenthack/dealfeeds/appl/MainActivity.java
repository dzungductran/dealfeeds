package com.notalenthack.dealfeeds.appl;

import android.app.*;
import android.content.*;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.RemoteException;
import android.support.v4.app.*;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.*;
import android.widget.*;
import android.os.Bundle;

import com.notalenthack.dealfeeds.R;
import com.notalenthack.dealfeeds.common.Constant;
import com.notalenthack.dealfeeds.common.ContentHelper;
import com.notalenthack.dealfeeds.service.ITightwadAPI;
import com.notalenthack.dealfeeds.service.SettingData;
import com.notalenthack.dealfeeds.service.TightwadClient;

import java.util.Calendar;
import java.util.regex.Pattern;

public class MainActivity extends FragmentActivity {
    private Context mContext = null;
    private SearchView searchView;
    private MenuItem refreshMenuItem = null;

    // Settings
    private static SettingData mSettings;

    private DrawerLayout mDrawerLayout;
    private ScrollView mDrawerView;
    private ActionBarDrawerToggle mDrawerToggle;

    // Broadcast receiver for receiving intents
    private BroadcastReceiver mReceiver;

    // Controls in settings drawer
    private boolean mNeedTurnOnRefreshMenu = false;

    // validation
    private Drawable errorIcon;
    private Pattern pattern;
    private static final String TIME24HOURS_PATTERN = "([01]?[0-9]|2[0-3]):[0-5][0-9]";

    private static String[] mFeeds = {
            "https://rss.dealcatcher.com/rss.xml",
            "https://www.tigerdirect.com/email/retro/rss.xml",
            "https://feeds.feedburner.com/SlickdealsnetFP",
            "http://www.freestufffinder.com/feed/",
            "http://s31.dlnws.com/dealnews/rss/todays-edition.xml",
            "https://www.techbargains.com/rss.xml",
            "http://bargainbabe.com/feed/"
    };
    private static String[] mNameResources = {
            "Deal Catcher",
            "TigerDirect",
            "Slick Deals",
            "Free Stuff Finder",
            "Deal News",
            "Tech Bargains",
            "Bargain Babe"
    };

    // Start service if it has not started
    private TightwadClient mService = null;
    private ITightwadAPI mServiceApi = null;
    private TightwadClient.OnAvailableAPI mApiAvailable = new TightwadClient.OnAvailableAPI() {
        @Override
        public void onAvailableAPI(ITightwadAPI api) {
            mServiceApi = api;

            try {
                // Add all of our sources here (duplicate adds are ignores)
                for (String feedURI : mFeeds) {
                    mServiceApi.addFeed(feedURI);
                }

                mSettings = mServiceApi.getSettings();
                updateSettingDrawer();

                if (mSettings.isPullDataOnStartup()) {
                    if (Constant.debug) Log.d(Constant.LOGTAG, "Refresh on startup");
                    ContentHelper.getInstance(mContext).delete();
                    mServiceApi.refresh();
                    turnRefreshOn();
                }
            } catch (RemoteException ex) {
                Log.e(Constant.LOGTAG, ex.getMessage());
            }
        }
    };

    ViewPager mViewPager;
    TabPagerAdapter mTabPagerAdapter;

    public static SettingData getSettings() {
        return mSettings;
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;

        mService = new TightwadClient(this, mApiAvailable);

        setContentView(R.layout.main2);

        setupFilters();

        setupSettingDrawer();

        // Create the adapter that will return a fragment for each of the tabs
        mTabPagerAdapter = new TabPagerAdapter(getSupportFragmentManager(), this);

        // Attaching the adapter and setting up swipes listener
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mTabPagerAdapter);

        PagerTabStrip pagerTabStrip = (PagerTabStrip) findViewById(R.id.pager_header);
        pagerTabStrip.setDrawFullUnderline(true);
        pagerTabStrip.setTabIndicatorColor(getResources().getColor(R.color.holo_light_background));

        errorIcon = getResources().getDrawable(R.drawable.ic_error);
        errorIcon.setBounds(new Rect(0, 0, errorIcon.getIntrinsicWidth(), errorIcon.getIntrinsicHeight()));
        pattern = Pattern.compile(TIME24HOURS_PATTERN);

        // Attaching the adapter and setting up swipes listener
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mTabPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When swiping between different tab, we need to select the corresponding tab.
                if (Constant.debug) Log.d(Constant.LOGTAG, "tab on page " + position);
            }
        });
    }

    // Update the drawer screen from the data from settings
    private void updateSettingDrawer() {
        CheckBox chkEbay = (CheckBox) mDrawerView.findViewById(R.id.compare_ebay);
        chkEbay.setChecked(mSettings.isCompareWithEbay());

        CheckBox chkPullOnStartup = (CheckBox) mDrawerView.findViewById(R.id.pull_app_start);
        chkPullOnStartup.setChecked(mSettings.isPullDataOnStartup());
    }

    // Store away settings for the application
    private void storeSettings() {
        CheckBox chkEbay = (CheckBox) mDrawerView.findViewById(R.id.compare_ebay);
        mSettings.setCompareWithEbay(chkEbay.isChecked());

        CheckBox chkPullOnStartup = (CheckBox) mDrawerView.findViewById(R.id.pull_app_start);
        mSettings.setPullDataOnStartup(chkPullOnStartup.isChecked());
        if (mServiceApi != null) {
            try {
                mServiceApi.setSettings(mSettings);
            } catch (RemoteException e) {
                Log.e(Constant.LOGTAG, "Remote exception when saving settings " + e.getMessage());
            }
        }
    }

    // Setup receiver to get message from service
    private void setupFilters() {
        final IntentFilter filter = new IntentFilter();
        filter.addAction(TightwadClient.ACTION_REFRESH_DONE);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Constant.debug) Log.d(Constant.LOGTAG, "onReceive intent " + intent.toString());
                // Got intent to clean up
                if (intent.getAction().equals(TightwadClient.ACTION_REFRESH_DONE)) {
                    if (refreshMenuItem != null) {
                        refreshMenuItem.setActionView(null);
                    }
                }
            }
        };
        registerReceiver(mReceiver, filter);
    }

    // setup the Drawer for the settings
    private void setupSettingDrawer() {

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerView = (ScrollView) findViewById(R.id.left_drawer);

        final Button btnRefreshNow = (Button) mDrawerView.findViewById(R.id.once_daily);
        btnRefreshNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (Constant.debug) Log.d(Constant.LOGTAG, "Refresh Now");
                    ContentHelper.getInstance(mContext).delete();
                    mServiceApi.refresh();
                    turnRefreshOn();
                } catch (RemoteException ex) {
                    Log.e(Constant.LOGTAG, ex.getMessage());
                }
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_navigation_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(getString(R.string.app_name));
                storeSettings();
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(getString(R.string.title_settings));
                updateSettingDrawer();
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    // Turn refresh on
    private void turnRefreshOn() {
        if (refreshMenuItem != null) {
            if (Constant.debug) Log.d(Constant.LOGTAG, "Refresh: Turn refresh menu on");
            refreshMenuItem.setActionView(R.layout.actionbar_indeterminate_progress);

            // Setup expiration if we never get a message from the service
            AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent();
            intent.setAction(TightwadClient.ACTION_REFRESH_DONE);
            PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            // Getting current time and add the seconds in it
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.SECOND, 60);
            am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi); // 60 seconds
        } else {
            // if state since we don't have menu yet
            mNeedTurnOnRefreshMenu = true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true); // Do not iconify the widget; expand it by default

        refreshMenuItem = menu.findItem(R.id.menu_refresh);
        if (mNeedTurnOnRefreshMenu) {
            turnRefreshOn();
        }

        // Calling super after populating the menu is necessary here to ensure that the
        // action bar helpers have a chance to handle this event.
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                try {
                    // mServiceApi.addFeed("http://www.techbargains.com/rss.xml");
                    int pos = mViewPager.getCurrentItem();
                    if (Constant.debug) Log.d(Constant.LOGTAG, "Refresh: tab pos " + pos);
                    ContentHelper.getInstance(this).delete(ContentHelper.SELECTION_FEED_ID, mFeeds[pos].hashCode());

                    mServiceApi.refreshFeed(mFeeds[pos]);
                } catch (RemoteException ex) {
                    Log.e(Constant.LOGTAG, ex.getMessage());
                }
                turnRefreshOn();
                break;

            case R.id.menu_search:
                searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
                //Toast.makeText(this, "Tapped search", Toast.LENGTH_SHORT).show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mService == null) {
            mService = new TightwadClient(this, mApiAvailable);
        }
    }

    @Override
    protected void onDestroy() {
        if (mService != null)
            mService.unBind();
        mService = null;
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    private void showError(String error) {
        Dialog dialog = new Dialog(this, R.style.DialogSlideAnim);
        dialog.setTitle(error);
        //dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.show();
    }

    /**
     * Returns a fragment corresponding to one of the tab section of the application. Uses FragmentPagerAdapter
     * which will keep every loaded fragment in memory which is fast but could be memory intensive. If so, might want to
     * switch to FragmentStatePagerAdapter.
     */
    public static class TabPagerAdapter extends FragmentPagerAdapter {

        public static FragmentManager mFragmentManager;

        private Activity mActivity;

        int iconResources[] = new int[]{
                R.drawable.ic_dealcatcher,
                R.drawable.ic_tigerdirect,
                R.drawable.ic_slickdeals,
                R.drawable.ic_freestufffinder,
                R.drawable.ic_dealnews,
                R.drawable.ic_techbargains,
                R.drawable.ic_bargainbabe};

        public TabPagerAdapter(FragmentManager fm, Activity act) {
            super(fm);
            mFragmentManager = fm;
            mActivity = act;
        }

        @Override
        public Fragment getItem(int position) {
            Bundle args;
            // The other sections of the app are dummy placeholders.
            Fragment view = new ProductView();
            args = new Bundle();
            args.putInt(Constant.ARG_FEED_ID, mFeeds[position].hashCode());
            if (Constant.debug) Log.d(Constant.LOGTAG, "bundle " + args.toString());
            view.setArguments(args);

            return view;
        }

        @Override
        public int getCount() { return mFeeds.length; }

        @Override
        public CharSequence getPageTitle(int position) {
            Drawable myDrawable = mActivity.getResources().getDrawable(iconResources[position]); //Drawable you want to display

            SpannableStringBuilder sb = new SpannableStringBuilder(" " + " "); // space added before text for convenience

            myDrawable.setBounds(0, 0, myDrawable.getIntrinsicWidth(), myDrawable.getIntrinsicHeight());
            ImageSpan span = new ImageSpan(myDrawable, ImageSpan.ALIGN_BOTTOM);
            sb.setSpan(span, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            return sb;
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Restore the previously serialized current tab position.
        if (savedInstanceState.containsKey(Constant.TAB_SELECTED) && mViewPager != null) {
            mViewPager.setCurrentItem(savedInstanceState.getInt(Constant.TAB_SELECTED));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Serialize the current tab position.
        if (mViewPager != null)
            outState.putInt(Constant.TAB_SELECTED, mViewPager.getCurrentItem());
    }
}