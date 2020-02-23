package com.notalenthack.dealfeeds.service.rssfeed;

import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.util.Log;

import com.notalenthack.dealfeeds.common.Constant;
import com.notalenthack.dealfeeds.service.TightwadClient;

import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class provides the base functionality for a message handling
 */
public class RssFeedHandler implements Runnable, IThreadCompleteListener {

    private static boolean SINGLE_THREAD = true;
    private static int STOP_SYSTEM  = 1;
    private static int PROCESS_FEED = 2;

    // Queue for requests. It is a blocking queue, so the thread will block on a take from the queue if
    // the queue is empty
    private LinkedBlockingQueue<Message> mMsgQueue = new LinkedBlockingQueue<Message>();
    private boolean mIsRunning = false;

    private Context mContext;
    private Map<String, Thread> mRequests = new Hashtable<String, Thread>();

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

    private void handle(String feed) {
        if (SINGLE_THREAD) {
            RssFeedRunnable2 rssFeedRunnable = new RssFeedRunnable2(mContext, feed, null);
            rssFeedRunnable.run();
            if (mMsgQueue.isEmpty()) {
                // send a message to the application
                Intent intent = new Intent();
                intent.setAction(TightwadClient.ACTION_REFRESH_DONE);
                mContext.sendBroadcast(intent);
            }
        } else {
            Thread rssThread = new Thread(new RssFeedRunnable2(mContext, feed, this));
            mRequests.put(feed, rssThread);
            rssThread.start();
        }
    }

    /**
     * Notify that the thread is done
     * @param feed
     */
    @Override
    public void notifyOfThreadComplete(String feed) {
        mRequests.remove(feed);
        if (mRequests.size() == 0) {  // no more threads
            // send a message to the application
            Intent intent = new Intent();
            intent.setAction(TightwadClient.ACTION_REFRESH_DONE);
            mContext.sendBroadcast(intent);
        }
    }

    public RssFeedHandler(Context context) {
        mContext = context;
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
                if (msg.what == STOP_SYSTEM) {
                    break;
                }
                // handle the request
                if (Constant.debug) Log.d(Constant.LOGTAG, "Process feed " + msg.obj.toString());
                handle((String)msg.obj);
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
     * Request the feed to be processed
     * @param feed
     */
    public void request(String feed) {
        // If feed is not in current process list
        Thread rssThread = mRequests.get(feed);
        if (rssThread == null) {
            Message msg = obtainMessage(PROCESS_FEED, feed);
            sendMessage(msg);
        } else if (rssThread.getState() == Thread.State.TERMINATED) {
            mRequests.remove(feed);
            Message msg = obtainMessage(PROCESS_FEED, feed);
            sendMessage(msg);
        }
    }
}