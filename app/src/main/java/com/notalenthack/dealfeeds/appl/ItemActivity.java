package com.notalenthack.dealfeeds.appl;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.notalenthack.dealfeeds.R;
import com.notalenthack.dealfeeds.appl.dialog.ProgressIndicator;
import com.notalenthack.dealfeeds.common.Constant;
import com.notalenthack.dealfeeds.common.StringHelper;
import com.notalenthack.dealfeeds.model.Product;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Created by dtran on 9/4/13.
 */
public class ItemActivity extends Activity {

    private Product mProduct;
    private LinearLayout mItemsView = null;
    private Activity mThisActivity;
    private boolean bSearchEbay = true;

    private ProgressIndicator mIndicator;
    private Set<AsyncTask> mAsyncTasks = new HashSet<AsyncTask>();

    // We seen these items
    private Map<String, Product> mItemsSeen = new Hashtable<String, Product>();

    private ImageDownloader mImageDownloader;

    private enum Vendor
    {
        EBAY, UNKNOWN;
    }

    // Add item to the list
    private void addNoItemMessage() {
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        TextView descView = (TextView)inflater.inflate(R.layout.list_item, mItemsView, false);
        descView.setText(getResources().getString(R.string.no_item_str));
        mItemsView.addView(descView);
    }

    // Add item to the list
    private void addItemLayout(Product product, Vendor vendor) {
        // Fill data
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        RelativeLayout itemLayout = (RelativeLayout)inflater.inflate(R.layout.product_item_vendor, mItemsView, false);

        // click listener
        ItemClickListener listener = new ItemClickListener(this, product);

        // Fill data
        ImageView imageViewIcon = (ImageView)itemLayout.findViewById(R.id.product_vendor);
        if (vendor == Vendor.EBAY) {
            imageViewIcon.setImageResource(R.drawable.ebay);
        } else  {
            Log.e(Constant.LOGTAG, "Unknown vendor to compare to");
        }
        imageViewIcon.setOnClickListener(listener);


        ImageView imageView = (ImageView)itemLayout.findViewById(R.id.product_image);
        imageView.setOnClickListener(listener);

        Bitmap bitmap = mImageDownloader.getBitmap(product.getImageLink());
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            if (Constant.debug) Log.d(Constant.LOGTAG, "LOAD_BITMAP from http: " + product.getImageLink());
            mImageDownloader.download(product.getImageLink(), imageView);
        }

        TextView titleView = (TextView)itemLayout.findViewById(R.id.product_title);
        titleView.setOnClickListener(listener);
        titleView.setText(product.getTitle() + " $" + product.getPriceStr());

        View sepView = inflater.inflate(R.layout.separator, mItemsView, false);

        mItemsView.addView(itemLayout);
        mItemsView.addView(sepView);
    }

    public com.ebay.api.FindItem.FindItemCallback callbackEB = new com.ebay.api.FindItem.FindItemCallback() {
        @Override
        public void foundItemCB(Product item) {
            if (mItemsView != null) {
                if (Constant.debug) Log.d(Constant.LOGTAG, "Got ebay item " + item.toString());
                if (!mItemsSeen.containsKey(item.getLink())) {
                    mItemsSeen.put(item.getLink(), item);
                    addItemLayout(item, Vendor.EBAY);
                }
            }
        }
    };

    public com.ebay.api.FindItem.DoneCallback doneCallbackEB = new com.ebay.api.FindItem.DoneCallback() {
        @Override
        public void onFinished(AsyncTask task) {
            mAsyncTasks.remove(task);
            if (mAsyncTasks.size() == 0 && mIndicator != null) {
                doneWithFind();
            }
        }
    };

    private void doneWithFind() {
        try {
            if (mIndicator.isShowing()) {
                mIndicator.dismiss();
                mIndicator = null;
            }
        } catch (Exception ex) {
            Log.e(Constant.LOGTAG, "Bad error: " + ex.getMessage());
        }

        // see if we need to add a message that there are no item found
        if (mItemsSeen.size() == 0) {
            addNoItemMessage();
        }
    }

    @Override
    public void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mThisActivity = this;

        mImageDownloader = ImageDownloader.getInstance(this);

        final Intent launchingIntent = getIntent();

        // Retrieve the Query Type and Position from the intent or bundle
        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        } else if (launchingIntent != null) {
            mProduct = launchingIntent.getParcelableExtra(Constant.ARG_ITEM);
            bSearchEbay = launchingIntent.getBooleanExtra(Constant.COMPARE_EBAY, bSearchEbay);
        }

        setContentView(R.layout.detail);
        setTitle(getResources().getString(R.string.competive_screen_title));

        if (mProduct != null) {
            // link to the item
            ItemClickListener listener = new ItemClickListener(this, mProduct);

            getActionBar().setHomeButtonEnabled(true);
            getActionBar().setIcon(R.drawable.ic_action_navigation_previous_item);

            // Fill data
            TextView descView = (TextView)findViewById(R.id.product_vendor);
            descView.setOnClickListener(listener);
            descView.setText(mProduct.getVendor());

            ImageView imageView = (ImageView)findViewById(R.id.product_image);
            imageView.setOnClickListener(listener);
            mImageDownloader.download(mProduct.getImageLink(), imageView);

            TextView titleView = (TextView)findViewById(R.id.product_title);
            titleView.setOnClickListener(listener);
            StringBuffer sb = new StringBuffer();
            sb.append(mProduct.getTitle());
            if (!mProduct.getDescription().isEmpty()) {
                sb.append(" ");
                sb.append(mProduct.getDescription());
            }
            titleView.setText(sb.toString());

            ScrollView view = (ScrollView)findViewById(R.id.competitive_list);
            mItemsView = (LinearLayout)view.findViewById(R.id.competive_items);

            String searchParams[] = null;
            String strSearch = null;
            if (!mProduct.getTitle().isEmpty()) {
                strSearch = StringHelper.buildSearchString(mProduct.getTitle());
            }
            else {
                strSearch = StringHelper.buildSearchString(mProduct.getDescription());
            }
            String strSearchNoModelNumber = StringHelper.getStringNoModelNumber(strSearch);
            if (strSearchNoModelNumber != null) {
                String strM[] = StringHelper.buildStringList(strSearchNoModelNumber.toLowerCase(Locale.getDefault()));
                String strS[] = StringHelper.buildStringList(strSearch.toLowerCase(Locale.getDefault()));
                searchParams = new String[strS.length + strM.length];
                for(int i=0; i<strS.length; i++) {
                    searchParams[i] = strS[i];
                }
                for(int i=0; i<strM.length; i++) {
                    searchParams[i+strS.length] = strM[i];
                }
            } else {
                searchParams =StringHelper.buildStringList(strSearch.toLowerCase(Locale.getDefault()));
                if (searchParams == null) {
                    // We used getTitle at first, so now try description
                    if (!mProduct.getTitle().isEmpty() && !mProduct.getDescription().isEmpty()) {
                        searchParams = StringHelper.buildStringList(mProduct.getDescription().toLowerCase(Locale.getDefault()));
                    }
                }
            }

            mIndicator = ProgressIndicator.show(mThisActivity,
                    getResources().getString(R.string.cancel_str), "", true,
                    new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    for (AsyncTask task : mAsyncTasks) {
                        task.cancel(true);
                    }
                    mIndicator.dismiss();
                }
            });

            int pos = searchParams.length-1;
            int count = 0;
            while (pos >= 0 && count <5) {
                String str = searchParams[pos--];
                count++;
                if (bSearchEbay) {
                    com.ebay.api.FindItem ebayFindItem = new com.ebay.api.FindItem(callbackEB, doneCallbackEB);
                    mAsyncTasks.add(ebayFindItem);
                    ebayFindItem.execute(str);
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        if (mProduct != null) {
            savedInstanceState.putParcelable(Constant.ARG_ITEM, mProduct);
            savedInstanceState.putBoolean(Constant.COMPARE_EBAY, bSearchEbay);
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mProduct = savedInstanceState.getParcelable(Constant.ARG_ITEM);
        bSearchEbay = savedInstanceState.getBoolean(Constant.COMPARE_EBAY);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    class ItemClickListener implements View.OnClickListener {
        private Product product;
        private Context mContext;

        public ItemClickListener(Context context, Product product) {
            this.product = product;
            this.mContext = context;
        }

        @Override
        public void onClick(View v) {
            Intent launchingIntent = new Intent(mContext, BrowserActivity.class);
            launchingIntent.putExtra(Constant.ARG_LINK, product.getLink());

            if (Constant.debug) Log.d(Constant.LOGTAG, "Launch item detail: " + product.getTitle());

            mContext.startActivity(launchingIntent);
        }
    }

}