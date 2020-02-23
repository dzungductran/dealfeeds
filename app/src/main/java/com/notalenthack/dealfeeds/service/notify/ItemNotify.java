package com.notalenthack.dealfeeds.service.notify;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import com.notalenthack.dealfeeds.R;
import com.notalenthack.dealfeeds.common.Constant;
import com.notalenthack.dealfeeds.common.StringHelper;
import com.notalenthack.dealfeeds.model.Product;
import com.notalenthack.dealfeeds.service.TightwadClient;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Android Status Bar notification:
 * See: http://developer.android.com/design/patterns/notifications.html
 *      http://somethingididnotknow.wordpress.com/2013/06/12/
 *         stack-notifications-on-android-plus-get-users-to-see-your-notifications-on-jellybean-phonephablet-ui/
 *      http://developer.android.com/reference/android/app/Notification.Builder.html
 *      http://androidresearch.wordpress.com/2012/01/15/showing-status-bar-notifications-in-android/
 */
public class ItemNotify {
    private Context mContext;
    private static AtomicInteger notificationCounter = new AtomicInteger();
    private static ArrayList<Product> mItems = new ArrayList<Product>();
    private static ItemNotify sInstance = null;

    private ItemNotify(Context context) {
        mContext = context;
    }

    public static synchronized ItemNotify getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ItemNotify(context);
        }

        return sInstance;
    }

    public void clearNotification() {
        notificationCounter.set(0);
        mItems.clear();
    }

    public void showNotification(Product product, String searchStr) {
        Notification notification = null;

        // notificationCounter is a private static AtomicInteger
        int notificationNumber = notificationCounter.incrementAndGet();
        mItems.add(product);

        Notification.Builder builder = new Notification.Builder(mContext)
                .setSmallIcon(R.drawable.ic_launcher, 0)
                .setContentTitle(mContext.getResources().getString(R.string.item_found) + " " + searchStr)
                .setContentText(StringHelper.buildSearchString(product.getTitle()))
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setTicker(mContext.getResources().getString(R.string.status_ticker))
                .setContentIntent(getPendingIntent(product, searchStr))
                .setDeleteIntent(getDismissIntent())
                .setNumber(notificationNumber);   // update the counter */


            // for some reason Notification.PRIORITY_DEFAULT doesn't show the counter
            builder.setPriority(Notification.PRIORITY_HIGH);

        if (mItems.size() > 1) {
            Notification.InboxStyle inboxStyle = new Notification.InboxStyle();
            inboxStyle.setBigContentTitle(mItems.size() + " "
                    + mContext.getResources().getString(R.string.item_found) + " " + searchStr);
            for (Product item : mItems) {
                inboxStyle.addLine(StringHelper.buildSearchString(item.getTitle()));
            }
            builder.setContentIntent(getPendingIntentForInbox(mItems, searchStr));
            builder.setStyle(inboxStyle);
        }

        NotificationManager nm = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        // cancel previous notification to clean up garbage in the status bar
        nm.cancel(notificationNumber - 1);
        // add new notification
        nm.notify(notificationNumber, builder.build());
    }

    /**
     * Get the pending itent for launching the notification
     * See issue below: http://stackoverflow.com/questions/3127957/
     *                 why-the-pendingintent-doesnt-send-back-my-custom-extras-setup-for-the-intent
     * @param items
     * @param searchStr
     * @return
     */
    private PendingIntent getPendingIntentForInbox(ArrayList<Product> items, String searchStr) {

        Intent launchingIntent = new Intent();
        launchingIntent.putExtra(Constant.ARG_SEARCH, searchStr);
        Log.d(Constant.LOGTAG, "Product ArrayList size " + items.size());
        Product products[] = items.toArray(new Product[items.size()]);
        Log.d(Constant.LOGTAG, "Product Array size " + products.length);
        launchingIntent.putExtra(Constant.ARG_ITEMS, products);
        // For some unspecified reason, extras will be delivered only if you've set some action
        // so just set it to some action
        launchingIntent.setAction(TightwadClient.ACTION_ITEM_ROUTE); // route to our service

        return PendingIntent.getBroadcast(mContext, 0, launchingIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Get the pending itent for launching the notification
     * See issue below: http://stackoverflow.com/questions/3127957/
     *                 why-the-pendingintent-doesnt-send-back-my-custom-extras-setup-for-the-intent
     *   http://stackoverflow.com/questions/15694364/why-does-it-say-the-requestcode-in-pendingintent-getbroadcast-is-not-used
     * @param product
     * @param searchStr
     * @return
     */
    private PendingIntent getPendingIntent(Product product, String searchStr) {

        Intent launchingIntent = new Intent();
        launchingIntent.putExtra(Constant.ARG_LINK, product.getLink());
        Log.d(Constant.LOGTAG, "itemURL: " + product.getLink());
        // For some unspecified reason, extras will be delivered only if you've set some action
        // so just set it to some action
        launchingIntent.setAction(TightwadClient.ACTION_ITEM_ROUTE); // route to our service

        return PendingIntent.getBroadcast(mContext, 0, launchingIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getDismissIntent() {
        Intent intent = new Intent();
        intent.setAction(TightwadClient.ACTION_ITEM_DISMISS);
        return PendingIntent.getBroadcast(mContext, 0, intent, 0);
    }
}
