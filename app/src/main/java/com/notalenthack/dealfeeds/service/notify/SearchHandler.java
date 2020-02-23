package com.notalenthack.dealfeeds.service.notify;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Message;
import android.util.Log;
import com.notalenthack.dealfeeds.common.Constant;
import com.notalenthack.dealfeeds.common.ContentHelper;
import com.notalenthack.dealfeeds.common.ItemsContract;
import com.notalenthack.dealfeeds.model.Product;

import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class provides the base functionality for a message handling
 */
public class SearchHandler implements Runnable, IRequest {

    private static int STOP_SYSTEM  = 1;
    private static int PROCESS_SEARCH = 2;
    private static int NEW_SEARCH_TERM = 3;

    // Queue for requests. It is a blocking queue, so the thread will block on a take from the queue if
    // the queue is empty
    private LinkedBlockingQueue<Message> mMsgQueue = new LinkedBlockingQueue<Message>();
    private boolean mIsRunning = false;
    private ContentHelper mContentHelper;

    private Context mContext;
    private Set<String> mSearchPattern = null;
    private String mSearchStr = null;

    private String convertToSearchPattern(Set<String> searchSet) {
        if (searchSet == null || searchSet.size() == 0)
            return null;

        StringBuilder sb = new StringBuilder("");
        String strings[] = searchSet.toArray(new String[searchSet.size()]);
        sb.append(strings[0]);
        for (int i=1; i<strings.length; i++) {
            sb.append("|");
            sb.append(strings[i]);
        }
        if (Constant.debug) Log.d(Constant.LOGTAG, "Search pattern: " + sb.toString());
        return sb.toString().toLowerCase(Locale.getDefault());
    }

    /**
     * Helper to create a message with message type and object.
     */
    private Message obtainMessage(int what, Object obj) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = obj;
        return msg;
    }

    /**
     * This routine put message in the queue to be process
     */
    private void sendMessage(Message msg) {
        if (Constant.debug) Log.d(Constant.LOGTAG, "sendMessage(Message msg) " + msg.toString());

        if (msg.what == STOP_SYSTEM) {
            mMsgQueue.clear();
        }
        mMsgQueue.offer(msg);
    }

    /**
     * Handle the pattern matching. If there is a match then do the notification
     * @param id
     */
    private void handle(Long id) {
        if (mSearchPattern == null) {
            return;
        }

        if (Constant.debug) Log.d(Constant.LOGTAG, "SEARCH_HANDLER: Handle id " + id);
        Product product = mContentHelper.getProduct(id);
        if (product.getStatus() != ItemsContract.STATUS_NOTIFIED && product.getStatus() != ItemsContract.STATUS_SEEN) {
            String title = product.getTitle().toLowerCase(Locale.getDefault());
            String desc = product.getDescription().toLowerCase(Locale.getDefault());
            boolean match = false;
            Iterator iterator = mSearchPattern.iterator();
            while (iterator.hasNext()) {
                String value = (String)iterator.next();
                if (!title.isEmpty() && title.contains(value)) {
                    match = true;
                    break;
                }

                if (!desc.isEmpty() && desc.contains(value)) {
                    match = true;
                    break;
                }
            }

            if (match) {
                ItemNotify.getInstance(mContext).showNotification(product, mSearchStr);
                ContentValues values = new ContentValues();
                values.put(ItemsContract.COLUMN_STATUS, ItemsContract.STATUS_NOTIFIED);
                mContentHelper.update(values, ContentHelper.SELECTION_ID, new String[]{String.valueOf(product.getId())});
            }
        }
    }

    private void reScanDatabase() {
        Cursor cursor = mContentHelper.getCursor();
        if (cursor.moveToFirst()) {
            long id;
            do {
                id = cursor.getLong(cursor.getColumnIndex(ItemsContract.COLUMN_ID));
                handle(id);
            } while (cursor.moveToNext());
            cursor.close();
            if (Constant.debug) Log.d(Constant.LOGTAG, "id " + id);
        }

        if(cursor != null){
            cursor.close();
        }
    }

    public SearchHandler(Context context) {
        mContext = context;
        mContentHelper = ContentHelper.getInstance(mContext);
    }

    /**
     * See if the message handler is running or not
     *
     * @return the status of the message handler if it is running or not
     */
    public boolean isRunning() {
        return mIsRunning;
    }

    /**
     * Stop the system
     */
    public void stop() {
        Message msg = obtainMessage(STOP_SYSTEM, null);
        sendMessage(msg);
    }

    /**
     * This function handle the running of the message handler by taking an request from the queue and
     * handle it
     */
    public void run() {
        if (Constant.debug) Log.d(Constant.LOGTAG, "runHandling()");

        mIsRunning = true;
        while (true) {
            try {
                // take a request from the queue
                Message msg = mMsgQueue.take();
                if (Constant.debug) Log.d(Constant.LOGTAG, "Got a message " + msg.toString());
                if (msg.what == STOP_SYSTEM) {
                    break;
                } else if (msg.what == PROCESS_SEARCH) {
                    // handle the request
                    handle((Long)msg.obj);
                } else if (msg.what == NEW_SEARCH_TERM) {
                    mSearchPattern = (Set<String>)msg.obj;
                    mSearchStr = convertToSearchPattern(mSearchPattern);
                    reScanDatabase();
                }
                msg.recycle();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.e(Constant.LOGTAG, "Interrupt - stopping the system");
                break;
            }
        }
        mIsRunning = false;
    }

    /**
     * Request to process the product against the search terms. If product is null then process the whole database
     * Product is null usually on the startup of the service
     * @param id
     */
    @Override
    public void request(long id) {
        Message msg = obtainMessage(PROCESS_SEARCH, Long.valueOf(id));
        sendMessage(msg);
    }

    // Search term should be in a set of strings
    public void onNewSearchTerm(Set<String> searchSet) {
        Message msg = obtainMessage(NEW_SEARCH_TERM, searchSet);
        sendMessage(msg);
    }
}