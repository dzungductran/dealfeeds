package com.notalenthack.dealfeeds.service;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.util.Log;
import com.notalenthack.dealfeeds.common.Constant;
import com.notalenthack.dealfeeds.common.ItemsContract;
import com.notalenthack.dealfeeds.service.notify.IRequest;

/**
 * Watch for new item and send a message to get the item process
 */
public class OnNewItemListener extends ContentObserver {
    private Context mContext;
    private IRequest mRequestHandler;

    public OnNewItemListener(Context context, IRequest iRequest) {
        super(null);
        if (Constant.debug) Log.d(Constant.LOGTAG, "Creating listener for new item");
        mContext = context;
        mRequestHandler = iRequest;
        ContentResolver cr = mContext.getContentResolver();
        cr.registerContentObserver(ItemsContract.CONTENT_URI_CRAPS, true, this);
    }

    // Implement the onChange(boolean) method to delegate the change notification to
    // the onChange(boolean, Uri) method to ensure correct operation on older versions
    // of the framework that did not have the onChange(boolean, Uri) method.
    @Override
    public void onChange(boolean selfChange) {
        onChange(selfChange, null);
    }

    @Override
    public void onChange(boolean selfChange, Uri uri ) {
        if(Constant.debug) Log.d(Constant.LOGTAG, "onChange selfChange: " + selfChange + " uri: " + uri);
        long id = -1;
        try {
            id = ContentUris.parseId(uri);
        } catch (UnsupportedOperationException e) {
        } catch (NumberFormatException ne) {
        }
        if(Constant.debug) Log.d(Constant.LOGTAG, "onChange id: " + id);
        if (id != -1 && mRequestHandler != null) {
            mRequestHandler.request(id);
        }
    }

    /**
     * This method must be called during service stopping
     */
    public void onClose() {
        ContentResolver cr = mContext.getContentResolver();
        cr.unregisterContentObserver(this);
    }
}
