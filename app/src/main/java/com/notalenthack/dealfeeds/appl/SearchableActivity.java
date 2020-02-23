package com.notalenthack.dealfeeds.appl;


import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.notalenthack.dealfeeds.R;
import com.notalenthack.dealfeeds.common.Constant;
import com.notalenthack.dealfeeds.common.ItemsContract;


public class SearchableActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {
	static private final String TAG = "SearchActivity";
    // views
    private ListView mProdListView;

    // Adapter used to display data
    private ProductCursorAdapter mAdapter;

    private String mSearchString = null;

    // Current loader
    private CursorLoader mLoader = null;

    private Activity mThisActivity;

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            mSearchString = intent.getStringExtra(SearchManager.QUERY);
            Bundle bundle = new Bundle();
            bundle.putString(Constant.ARG_SEARCH, mSearchString);

            getSupportLoaderManager().initLoader(0, bundle, this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (Constant.debug)  Log.d(Constant.LOGTAG, "onNewIntent: " + intent.toString());
        handleIntent(intent);
    }

	/** Called when the activity is first created. */
	@Override 
	public void onCreate (Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        mThisActivity = this;
        final Intent launchingIntent = getIntent();

        // Retrieve the search string from the intent or bundle
        if (savedInstanceState != null) {
            mSearchString = savedInstanceState.getString(Constant.ARG_SEARCH);
        } else if (launchingIntent != null) {
            mSearchString = launchingIntent.getStringExtra(SearchManager.QUERY);
        }
        if (Constant.debug) Log.d(Constant.LOGTAG, "Search string " + mSearchString);

        // inflate the view here
        setContentView(R.layout.fragment_listview);

        // Create an empty adapter we will use to display the loaded data.
        // We pass null for the cursor, then update it in onLoadFinished()
        // Just pass it the integer 0. You don't want to pass it FLAG_REGISTER_CONTENT_OBSERVER,
        // since you are using a CursorLoader with your CursorAdapter
        // (since the CursorLoader registers the ContentObserver for you)
        mAdapter = new ProductCursorAdapter(this, null, 0);

        mProdListView = (ListView)findViewById(R.id.listView);
        mProdListView.setAdapter(mAdapter);

        // Set the title
        setTitle(getResources().getString(R.string.search_result) + " " + mSearchString);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setIcon(R.drawable.ic_action_navigation_previous_item);

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        // Start out with all items
        Bundle bundle = new Bundle();
        bundle.putString(Constant.ARG_SEARCH, mSearchString);

        getSupportLoaderManager().initLoader(0, bundle, this);

    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        if (mSearchString != null) {
            savedInstanceState.putString(Constant.ARG_SEARCH, mSearchString);
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mSearchString = savedInstanceState.getString(Constant.ARG_SEARCH);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int n, Bundle bundle) {
        String strSearch = bundle.getString(Constant.ARG_SEARCH);

        Uri uri                = ItemsContract.CONTENT_URI_CRAPS;
        String[] projection    = null;
        String   sortOrder     = null;

        // split the string into multiple words
        String[] words = strSearch.split("\\s");

        StringBuffer sbT = new StringBuffer(" LIKE ?");
        StringBuffer sbD = new StringBuffer(" LIKE ?");
        for (int i=1; i < words.length; i++) {
            sbT.append(" AND ");
            sbT.append(ItemsContract.COLUMN_TITLE);
            sbT.append(" LIKE ?");
            sbD.append(" AND ");
            sbD.append(ItemsContract.COLUMN_DESCRIPTION);
            sbD.append(" LIKE ?");
        }
        String selection = "(" + ItemsContract.COLUMN_TITLE + sbT.toString() + ")"
                + " OR "
                + "(" + ItemsContract.COLUMN_DESCRIPTION + sbD.toString() + ")";

        String[] selectionArgs = new String[words.length * 2];
        int i=0;
        for (String s : words) {
            selectionArgs[i] = "%"+s+"%";
            selectionArgs[i+words.length] = "%"+s+"%";
            i++;
        }

        if (Constant.debug) Log.d(Constant.LOGTAG, "Load cursor for search terms " + selection);

        mLoader = new CursorLoader(mThisActivity, uri, projection, selection, selectionArgs, sortOrder);
        // call has completed until a new load is scheduled.
        return mLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> objectLoader, Cursor cur) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mAdapter.swapCursor(cur);
        if (Constant.debug) Log.d(Constant.LOGTAG, "onLoadFinished swap cursor " + cur.toString());
    }

    @Override
    public void onLoaderReset(Loader<Cursor> objectLoader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);
        if (Constant.debug) Log.d(Constant.LOGTAG, "onLoaderReset set cursor to null");
    }

    @Override protected void onDestroy ()
	 {
         mAdapter.swapCursor(null);
	     super.onDestroy ();
	 }
	 
	 protected void onListItemClick(View v, int pos, long id) {  
	     Log.i(TAG, "onListItemClick id=" + id);  
	 }
}
