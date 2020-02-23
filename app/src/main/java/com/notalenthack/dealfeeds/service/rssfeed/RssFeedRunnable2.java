package com.notalenthack.dealfeeds.service.rssfeed;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.notalenthack.dealfeeds.common.Constant;
import com.notalenthack.dealfeeds.common.ContentHelper;
import com.notalenthack.dealfeeds.common.ItemsContract;
import com.notalenthack.dealfeeds.model.Product;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Use XmlPullParser instead of SAX
 * Use https://www.regextester.com/
 */
public class RssFeedRunnable2 implements Runnable {
    private static final boolean debug = true;
    private static final String TAG = "RssFeedRunnable2";

    private enum ParseState {
        UNKNOWN, ITEM, TITLE, IMAGELINK, PRICE, LINK, DATE, DESCRIPTION, CONTENT, VENDOR, ERROR
    }

    String uri;
    Context mContext;
    private ContentHelper mContentHelper = null;
    private IThreadCompleteListener mCompleteListener = null;

    public RssFeedRunnable2(Context context, String uri, IThreadCompleteListener completeListener) {
        this.uri = uri;
        mContext = context;
        mCompleteListener = completeListener;
        mContentHelper = ContentHelper.getInstance(context);
    }

    public void run() {

        if (debug) Log.d(TAG, "Processing feed: " + uri);
        ArrayList<Product> pList = new ArrayList<Product>();
        try {
            Product product = null;
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            URL url = new URL(uri);
            InputStream stream = url.openStream();
            xpp.setInput(stream, null);
            ParseState state = ParseState.UNKNOWN;
            int eventType = xpp.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if(eventType == XmlPullParser.START_DOCUMENT) {
                    if (Constant.debug) Log.d(Constant.LOGTAG, "Start document");
                } else if(eventType == XmlPullParser.END_DOCUMENT) {
                    if (Constant.debug) Log.d(Constant.LOGTAG, "End document");
                } else if(eventType == XmlPullParser.START_TAG) {
                    if (Constant.debug) Log.d(Constant.LOGTAG, "Start tag "+xpp.getName());
                    if (xpp.getName().contentEquals("item")) {
                        product = new Product();
                        state = ParseState.ITEM;
                    } else if (xpp.getName().contentEquals("description")) {
                        state = ParseState.DESCRIPTION;
                    } else if (xpp.getName().contentEquals("link")) {
                        state = ParseState.LINK;
                    } else if (xpp.getName().contentEquals("title")) {
                        state = ParseState.TITLE;
                    } else if (xpp.getName().contentEquals("image")) {
                        state = ParseState.IMAGELINK;
                    } else if (xpp.getName().contentEquals("pubDate")) {
                        state = ParseState.DATE;
                    } else if (xpp.getName().contentEquals("encoded")) {
                        state = ParseState.CONTENT;
                    } else if (xpp.getName().contentEquals("vendorName")) {
                        state = ParseState.VENDOR;
                    } else {
                        state = ParseState.UNKNOWN;
                    }
                } else if(eventType == XmlPullParser.END_TAG) {
                    if (Constant.debug) Log.d(Constant.LOGTAG, "End tag "+xpp.getName());
                    if (xpp.getName().contentEquals("item")) {
                        if (product != null) {
                            pList.add(product);
                            product = null;
                        }
                    }
                    state = ParseState.UNKNOWN;
                } else if(eventType == XmlPullParser.TEXT) {
                    if (Constant.debug) Log.d(Constant.LOGTAG, "Text "+xpp.getText());
                    switch(state) {
                        case DESCRIPTION:
                            if (product != null) {
                                processDesc(product, xpp.getText());
                            }
                            break;
                        case IMAGELINK:
                            if (product != null && product.getImageLink().isEmpty()) {
                                product.setImageLink(xpp.getText());
                            }
                            break;
                        case LINK:
                            if (product != null) {
                                product.setLink(xpp.getText());
                            }
                            break;
                        case DATE:
                            if (product != null) {
                                product.setDate(xpp.getText());
                            }
                            break;
                        case VENDOR:    // seems not to be there
                            if (product != null) {
                                product.setVendor(xpp.getText());
                            }
                            break;
                        case CONTENT:
                            if (product != null) {
                                processDesc(product, xpp.getText());
                            }
                            break;
                        case TITLE:
                            if (product != null) {
                                String title = xpp.getText().replaceAll("&quot;", " ");
                                product.setTitle(title);
                            }
                            break;
                        case PRICE:
                            if (product != null && product.getPrice() == 0) {
                                try {
                                    String str = xpp.getText().replace("$", "");
                                    product.setPrice(Float.parseFloat(str));
                                } catch (NumberFormatException ex) {
                                    product.setPrice(0);
                                }
                            }
                            break;
                        default:
                            break;
                    }
                }
                eventType = xpp.next();
            }

        } catch (Exception e) {
            Log.e(Constant.LOGTAG, "Exception: " + e.getMessage());
        }

        Iterator<Product> resIter = pList.iterator();
        while (resIter.hasNext()) {
            Product item = resIter.next();
            if (debug) Log.d(TAG, "item: " + item);

            ContentValues values = new ContentValues();
            values.put(ItemsContract.COLUMN_FEED_ID, uri.hashCode());
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

    // Process the description
    private void processDesc(Product product, String description) {
        final Pattern imagePattern = Pattern.compile("<img\\s.*src=[\"'](.*?[^\\\\])[\"']");
        Matcher imageMatcher = imagePattern.matcher(description);

        if (imageMatcher.find()) {
            String imageURI = imageMatcher.group(1);
            product.setImageLink(imageURI);
        }

        final Pattern descPattern = Pattern.compile("<a\\s*href=.*\">(.*)</a>(\\s*.*)");
        Matcher descMatcher = descPattern.matcher(description);

        final Pattern descPattern2 = Pattern.compile("^\\<!\\[CDATA\\[\\n(.*)");

        if (descMatcher.find()) {
            String desc = descMatcher.group(1);
            if (desc.startsWith("<img")) {
                product.setDescription("");
            } else {
                product.setDescription(desc.replaceAll("</span>", ""));
            }
            if (Constant.debug) Log.d(Constant.LOGTAG, "Desc: " + product.getDescription());
        } else {
            Matcher descMatcher2 = descPattern2.matcher(description);
            if (descMatcher2.find()) {
                String desc = descMatcher2.group(1);
                if (desc.startsWith("<img")) {
                    product.setDescription("");
                } else {
                    product.setDescription(desc.replaceAll("</span>", ""));
                }
                if (Constant.debug) Log.d(Constant.LOGTAG, "Desc: " + product.getDescription());
            }
        }

        final Pattern pricePattern = Pattern.compile("for\\s*(\\$?[0-9]+\\.*[0-9]*)");
        final Pattern pricePattern2 = Pattern.compile("for\\s<.*>(\\$?[0-9]+\\.*[0-9]*)<.*>(.*)");

        Matcher priceMatcher = pricePattern.matcher(description);

        if (priceMatcher.find()) {
            String price = priceMatcher.group(1);
            price = price.replaceAll("[^\\d\\.]", "");
            product.setPrice(Float.parseFloat(price));
            if (Constant.debug) Log.d(Constant.LOGTAG, "Price: " + price);
        } else {
            Matcher priceMatcher2 = pricePattern2.matcher(description);
            if (priceMatcher2.find()) {
                String price = priceMatcher2.group(1);
                price = price.replaceAll("[^\\d\\.]", "");
                product.setPrice(Float.parseFloat(price));
                if (Constant.debug) Log.d(Constant.LOGTAG, "Price: " + price);
                if (product.getDescription().isEmpty()) {
                    product.setDescription(priceMatcher2.group(2));
                }
            }
        }
    }
}
