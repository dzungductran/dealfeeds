package com.ebay.api;

import java.io.StringReader;
import java.util.ArrayList;
import com.notalenthack.dealfeeds.model.Product;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.os.AsyncTask;
import android.util.Log;

import com.notalenthack.dealfeeds.common.Constant;
import com.notalenthack.dealfeeds.model.ProductCategories;

/**
 * This class find item on Ebay
 */
public class FindItem extends AsyncTask<String, Void, ArrayList<Product>> {

    // Ebay app id goes here
    private static final String EBAY_APP_ID = "Ebay_app_identifier_goes_here";
    private static final String GLOBAL_ID = "EBAY-US";

    private FindItemCallback mCallback = null;
    private DoneCallback mDoneCallback = null;

    private enum ParseState {
        UNKNOWN, TITLE, DATE, DESCRIPTION, IMAGELINK, NAME, CATEGORY, PRICE, LINK
    }

    public interface FindItemCallback {
        public void foundItemCB(Product item);
    }

    public interface DoneCallback {
        public void onFinished(AsyncTask task);
    }

    public FindItem(FindItemCallback cb, DoneCallback doneCallback) {
        super();
        if (cb != null) {
            mCallback = cb;
        }
        if (doneCallback != null) {
            mDoneCallback = doneCallback;
        }
    }

    @Override
    protected ArrayList<Product> doInBackground(String... itemStr) {
        /*
         * Set up the signed requests helper 
         */
        RequestsHelper helper;
        try {
            ArrayList<Product> products = new ArrayList<Product>();
            helper = RequestsHelper.getInstance(GLOBAL_ID, EBAY_APP_ID, RequestsHelper.FIND_OPERATION_NAME);
            for (String strSearch : itemStr) {
                String address = helper.createAddress(strSearch);
                if (Constant.debug) Log.d(Constant.LOGTAG, "sending request to :: " + address);
                String response = URLReader.read(address);
                if (Constant.debug) Log.d(Constant.LOGTAG, "response :: " + response);
                //process xml dump returned from EBAY
                products.addAll(processResponse(response));
            }
            return products;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private ArrayList<Product> processResponse(String response) throws Exception {
        ArrayList<Product> products = new ArrayList<Product>();
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(response));
            int eventType = xpp.getEventType();
            Product product = null;
            ParseState state = ParseState.UNKNOWN;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if(eventType == XmlPullParser.START_DOCUMENT) {
                    if (Constant.debug) Log.d(Constant.LOGTAG, "Start document");
                } else if(eventType == XmlPullParser.END_DOCUMENT) {
                    if (Constant.debug) Log.d(Constant.LOGTAG, "End document");
                } else if(eventType == XmlPullParser.START_TAG) {
                    if (Constant.debug) Log.d(Constant.LOGTAG, "Start tag "+xpp.getName());
                    if (xpp.getName().contentEquals("item")) {
                        product = new Product();
                        state = ParseState.UNKNOWN;
                    } else if (xpp.getName().contentEquals("categoryName")) {
                        state = ParseState.CATEGORY;
                    } else if (xpp.getName().contentEquals("title")) {
                        state = ParseState.TITLE;
                    } else if (xpp.getName().contentEquals("galleryURL")) {
                        state = ParseState.IMAGELINK;
                    } else if (xpp.getName().contentEquals("viewItemURL")) {
                        state = ParseState.LINK;
                    } else if (xpp.getName().contentEquals("currentPrice")) {
                        state = ParseState.PRICE;
                    } else {
                        state = ParseState.UNKNOWN;
                    }
                } else if(eventType == XmlPullParser.END_TAG) {
                    if (Constant.debug) Log.d(Constant.LOGTAG, "End tag "+xpp.getName());
                    if (xpp.getName().contentEquals("item")) {
                        products.add(product);
                    }
                    state = ParseState.UNKNOWN;
                } else if(eventType == XmlPullParser.TEXT) {
                    if (Constant.debug) Log.d(Constant.LOGTAG, "Text "+xpp.getText());
                    if (product == null) {
                        product = new Product();
                    }
                    switch(state) {
                        case CATEGORY:
                            int cat = ProductCategories.getCatIdFromTitle(xpp.getText());
                            if (cat != ProductCategories.CAT_OTHER) {
                                product.setCategory(cat);
                            }
                            break;
                        case DATE:
                            product.setDate(xpp.getText());
                            break;
                        case DESCRIPTION:
                            product.setDescription(xpp.getText());
                            break;
                        case IMAGELINK:
                            product.setImageLink(xpp.getText());
                            break;
                        case LINK:
                            product.setLink(xpp.getText());
                            break;
                        case NAME:
                            product.setName(xpp.getText());
                            break;
                        case PRICE:
                            try {
                                product.setPrice(Float.parseFloat(xpp.getText()));
                            } catch (NumberFormatException ex) {
                                product.setPrice(0.0f);
                            }
                            break;
                        case TITLE:
                            product.setTitle(xpp.getText());
                            break;
                    }
                }
                eventType = xpp.next();
            }

            return products;
        } catch (Exception ex) {
            ex.printStackTrace();
            return products;
        }
    }

    @Override
    protected void onPostExecute(ArrayList<Product> products) {
        if (mCallback != null && products != null) {
            for (Product item : products) {
                if (Constant.debug) Log.d(Constant.LOGTAG, "title: " + item.getTitle());

                mCallback.foundItemCB(item);
            }
        }

        if (mDoneCallback != null) {
            mDoneCallback.onFinished(this);
        }
    }
}
