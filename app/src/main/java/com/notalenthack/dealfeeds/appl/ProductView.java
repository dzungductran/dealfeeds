package com.notalenthack.dealfeeds.appl;

import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.widget.*;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.notalenthack.dealfeeds.R;
import com.notalenthack.dealfeeds.common.Constant;
import com.notalenthack.dealfeeds.common.ItemsContract;


public class ProductView extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    // views
    private ListView mProdListView;

    // Adapter used to display data
    private ProductCursorAdapter mAdapter;

    private int mFeedId = 0;

    // Current loader
    private CursorLoader mLoader = null;
    private View mRootView = null;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    public ProductView() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**
         * ListAdapter will get info from dataArray and put it to the list
         */
        // Create an empty adapter we will use to display the loaded data.
        // We pass null for the cursor, then update it in onLoadFinished()
        // Just pass it the integer 0. You don't want to pass it FLAG_REGISTER_CONTENT_OBSERVER,
        // since you are using a CursorLoader with your CursorAdapter
        // (since the CursorLoader registers the ContentObserver for you)
        mAdapter = new ProductCursorAdapter(getActivity(), null, 0);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Uri uri                = ItemsContract.CONTENT_URI_CRAPS;
        String[] projection    = null;
        String   selection     = "(" + ItemsContract.COLUMN_FEED_ID + " =?)";
        String[] selectionArgs = new String[]{String.valueOf(mFeedId)};
        String   sortOrder     = null;

        if (Constant.debug) Log.d(Constant.LOGTAG, "Load cursor mCategory " + mFeedId);

        mLoader = new CursorLoader(getActivity(), uri, projection, selection, selectionArgs, sortOrder);
        // call has completed until a new load is scheduled.
        return mLoader;
    }

    // Called when a previously created loader has finished loading
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cur) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mAdapter.swapCursor(cur);
        if (Constant.debug) Log.d(Constant.LOGTAG, "onLoadFinished swap cursor " + cur.toString());
    }

    // Called when a previously created loader is reset, making the data unavailable
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);
        if (Constant.debug) Log.d(Constant.LOGTAG, "onLoaderReset set cursor to null");
    }

    /*
    The container parameter passed to onCreateView() is the parent ViewGroup (from the activity's layout) in which
    your fragment layout will be inserted. The savedInstanceState parameter is a Bundle that provides data about
    the previous instance of the fragment,
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (Constant.debug) Log.d(Constant.LOGTAG, "onCreateView");

        // inflate the view here
        mRootView = inflater.inflate(R.layout.fragment_listview, container, false);

        Bundle args = getArguments();
        mFeedId = args.getInt(Constant.ARG_FEED_ID);
        if (Constant.debug) Log.d(Constant.LOGTAG, "mFeedId " + mFeedId);

        mProdListView = (ListView) mRootView.findViewById(R.id.listView);
        mProdListView.setAdapter(mAdapter);

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        // Start out with all items
        getLoaderManager().initLoader(mFeedId, null, this);

        return mRootView;
    }
}
