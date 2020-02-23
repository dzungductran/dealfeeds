package com.notalenthack.dealfeeds.service.rssfeed;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.notalenthack.dealfeeds.common.ContentHelper;
import com.notalenthack.dealfeeds.common.ItemsContract;
import com.notalenthack.dealfeeds.model.Product;

public class RssFeedRunnable implements Runnable {
    private static final boolean debug = true;
    private static final String TAG = "RssFeedRunnable";

	String uri;
	Context mContext;
    private ContentHelper mContentHelper = null;
    private IThreadCompleteListener mCompleteListener = null;

    public RssFeedRunnable(Context context, String uri, IThreadCompleteListener completeListener) {
		this.uri = uri;
		mContext = context;
        mCompleteListener = completeListener;
        mContentHelper = ContentHelper.getInstance(context);
    }

    public void run() {

         if (debug) Log.d(TAG, "Processing feed: " + uri);
         ArrayList<Product> pList = new ArrayList<Product>();
         try {
             URL url = new URL(uri);
             RssFeedParser feedHandler = new RssFeedParser(pList);
             SAXParserFactory factory = SAXParserFactory.newInstance();
             SAXParser parser = factory.newSAXParser();
             XMLReader xmlreader = parser.getXMLReader();
             xmlreader.setContentHandler(feedHandler);
             InputSource is = new InputSource(url.openStream());
             xmlreader.parse(is);
         } catch (Exception e) {
             e.printStackTrace();
         }

         Iterator<Product> resIter = pList.iterator();
         while (resIter.hasNext()) {
             Product item = resIter.next();

             ContentValues values = new ContentValues();
             values.put(ItemsContract.COLUMN_CATEGORY, item.getCategory());
             values.put(ItemsContract.COLUMN_LINK, item.getLink());
             values.put(ItemsContract.COLUMN_STATUS, item.getStatus());
             values.put(ItemsContract.COLUMN_DATE, System.currentTimeMillis());
             values.put(ItemsContract.COLUMN_TITLE, item.getTitle());
             values.put(ItemsContract.COLUMN_DESCRIPTION, item.getDescription());   // set to haven't seen
             values.put(ItemsContract.COLUMN_IMAGE_LINK, item.getImageLink());
             values.put(ItemsContract.COLUMN_PRICE, item.getPriceStr());


             // Check to see if we have the item already
             long id = mContentHelper.exist(item.getLink());
             if (id == -1) {
                 mContentHelper.insert(values);
             }
         }

         if (debug) Log.d(TAG, "Done Processing feed: " + uri);

         if (mCompleteListener != null) {
             mCompleteListener.notifyOfThreadComplete(this.uri);
         }
    }
}
