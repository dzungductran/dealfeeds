package com.notalenthack.dealfeeds.service.rssfeed;

/**
 * Listener to notify parent thread is done
 */
public interface IThreadCompleteListener {
    void notifyOfThreadComplete(String feed);
}
