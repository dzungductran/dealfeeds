package com.notalenthack.dealfeeds.appl;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;
import com.notalenthack.dealfeeds.R;
import com.notalenthack.dealfeeds.common.Constant;
import com.notalenthack.dealfeeds.model.Product;
import com.notalenthack.dealfeeds.service.TightwadClient;

/**
 * Created by dtran on 9/8/13.
 */
public class ItemListActivity extends Activity {
    // views
    private ListView mProdListView;

    // Adapter used to display data
    private ItemListAdapter mAdapter;

    private Product[] mItems = null;
    private String mSearchString = null;

    private Activity mThisActivity;

    private void handleIntent(Intent intent) {

        if (intent.getAction().equals(TightwadClient.ACTION_ITEM_LIST)) {
            mSearchString = intent.getStringExtra(Constant.ARG_SEARCH);
            mItems = (Product[])intent.getParcelableArrayExtra(Constant.ARG_ITEMS);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mThisActivity = this;
        final Intent intent = getIntent();

        // Retrieve the Query Type and Position from the intent or bundle
        if (savedInstanceState != null) {
            Parcelable parcelables[] = savedInstanceState.getParcelableArray(Constant.ARG_ITEMS);
            mItems = new Product[parcelables.length];
            for (int i=0; i<parcelables.length; i++) {
                mItems[i] = (Product)parcelables[i];
            }
            mSearchString = savedInstanceState.getString(Constant.ARG_SEARCH);
        } else if (intent != null) {
            Parcelable parcelables[] = intent.getParcelableArrayExtra(Constant.ARG_ITEMS);
            mItems = new Product[parcelables.length];
            for (int i=0; i<parcelables.length; i++) {
                mItems[i] = (Product)parcelables[i];
            }
            mSearchString = intent.getStringExtra(Constant.ARG_SEARCH);
        }
        if (Constant.debug) Log.d(Constant.LOGTAG, "item ids" + mItems.toString());

        // inflate the view here
        setContentView(R.layout.fragment_listview);

        // Create an adapter for the list of Product
        mAdapter = new ItemListAdapter(this, mItems);

        mProdListView = (ListView)findViewById(R.id.listView);
        mProdListView.setAdapter(mAdapter);

        // Set the title
        setTitle(mItems.length + " " + getResources().getString(R.string.item_found) + " " + mSearchString);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setIcon(R.drawable.ic_action_navigation_previous_item);
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        if (mSearchString != null) {
            savedInstanceState.putString(Constant.ARG_SEARCH, mSearchString);
        }
        if (mItems != null) {
            savedInstanceState.putParcelableArray(Constant.ARG_ITEMS, mItems);
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mSearchString = savedInstanceState.getString(Constant.ARG_SEARCH);
        Parcelable parcelables[] = savedInstanceState.getParcelableArray(Constant.ARG_ITEMS);
        mItems = new Product[parcelables.length];
        for (int i=0; i<parcelables.length; i++) {
            mItems[i] = (Product)parcelables[i];
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
