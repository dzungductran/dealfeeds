package com.notalenthack.dealfeeds.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.notalenthack.dealfeeds.common.Constant;

public class TightwadClient {
    public static final String SERVICE_NAME = "com.notalenthack.dealfeeds.service.TightwadService";

    public static final String ACTION_CLEAN_UP = "com.notalenthack.dealfeeds.cleanup";
    public static final String ACTION_ITEM_DISMISS = "com.notalenthack.dealfeeds.item_dismiss";
    public static final String ACTION_ITEM_ROUTE = "com.notalenthack.dealfeeds.item_route";
    public static final String ACTION_ITEM_DETAIL = "com.notalenthack.dealfeeds.item_detail";
    public static final String ACTION_ITEM_LIST = "com.notalenthack.dealfeeds.item_list";
    public static final String ACTION_REFRESH_DONE = "com.notalenthack.dealfeeds.refresh_done";
    public static final String ACTION_PULL_DATA = "com.notalenthack.dealfeeds.pull_data";

    private ITightwadAPI mApi;

    private Context mContext;

    private OnAvailableAPI mListener = null;

    // Define our custom Listener interface to receive API when service is binded
    public interface OnAvailableAPI {
        public abstract void onAvailableAPI(ITightwadAPI api);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (Constant.debug) Log.i(Constant.LOGTAG, "Service connection established");

            // that's how we get the client side of the IPC connection
            mApi = ITightwadAPI.Stub.asInterface(service);
            if (mApi == null) {
                Log.e(Constant.LOGTAG, "Oops can't seem to bind");
            }
            else {
                if (mListener != null) {
                    mListener.onAvailableAPI(mApi);
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (Constant.debug) Log.i(Constant.LOGTAG, "Service connection closed");
            mApi = null;
        }
    };

    /* this could be null */
    public ITightwadAPI getApi() {
        return mApi;
    }

    /* Create instance of a collect annotate service */
    public TightwadClient(Context ctx, OnAvailableAPI listener) {
        mListener = listener;
        mContext = ctx;

        // start the service explicitly.
        // otherwise it will only run while the IPC connection is up.
        Intent intentser = new Intent(SERVICE_NAME, null, ctx, TightwadService.class);
        mContext.startService(intentser);
        mContext.bindService(intentser, serviceConnection, Context.BIND_AUTO_CREATE);
        if (Constant.debug) Log.d(Constant.LOGTAG, "Binding...to service " + SERVICE_NAME);
    }

    public void unBind() {
        if (Constant.debug) Log.d(Constant.LOGTAG, "Unbind the service " + SERVICE_NAME);
        mContext.unbindService(serviceConnection);
    }
}
