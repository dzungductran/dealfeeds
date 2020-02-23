package com.notalenthack.dealfeeds.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.notalenthack.dealfeeds.common.Constant;

/**
 * Starts service on boot
 */
public class OnBootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // On boot up
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            try {
                Log.d(Constant.LOGTAG, "Starting up service " + TightwadClient.SERVICE_NAME);
                context.startService(new Intent(TightwadClient.SERVICE_NAME));
            } catch (Exception e) {
                Log.e("Tightwad Service", "Failed to start Service");
                e.printStackTrace();
            }
        }
    }
}