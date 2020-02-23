package com.ebay.api;

import android.util.Log;

import com.notalenthack.dealfeeds.common.Constant;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/*
 * http://developer.ebay.com/DevZone/finding/Concepts/FindingAPIGuide.html
 * http://go.developer.ebay.com/developers/ebay/products/finding-api
 */
public class RequestsHelper 
{
    /**
     * All strings are handled as UTF-8
     */
    private static final String UTF8_CHARSET = "UTF-8";

    public final static String EBAY_FINDING_SERVICE_URI = "https://svcs.ebay.com/services/search/FindingService/v1?OPERATION-NAME="
            + "{operation}&SERVICE-VERSION={version}&SECURITY-APPNAME="
            + "{applicationId}&GLOBAL-ID={globalId}&keywords={keywords}"
            + "&paginationInput.entriesPerPage={maxresults}";
    public static final String SERVICE_VERSION = "1.0.0";
    // https://developer.ebay.com/DevZone/finding/CallRef/findItemsByKeywords.html
    public static final String FIND_OPERATION_NAME = "findItemsByKeywords";
    // https://developer.ebay.com/DevZone/finding/CallRef/getSearchKeywordsRecommendation.html
    public static final String GET_KEYWORDS_OPERATION_NAME = "getSearchKeywordsRecommendation";
    public final static int REQUEST_DELAY = 3000;
    public final static int MAX_RESULTS = 10;
    private int maxResults;

    private String globalId = "";
    private String operation = "";
    private String appId = "";

    public static RequestsHelper getInstance(String endpoint, String accessKeyId, String op)
    {
        if (null == endpoint || endpoint.length() == 0)
        { throw new IllegalArgumentException("endpoint is null or empty"); }
        if (null == accessKeyId || accessKeyId.length() == 0)
        { throw new IllegalArgumentException("awsAccessKeyId is null or empty"); }

    	RequestsHelper instance = new RequestsHelper();
        instance.globalId = endpoint;
        instance.appId = accessKeyId;
        instance.operation = op;
        return instance;
    }

    /**
     * Percent-encode values according the RFC 3986. The built-in Java
     * URLEncoder does not encode according to the RFC, so we make the
     * extra replacements.
     *
     * @param s decoded string
     * @return  encoded string per RFC 3986
     */
    private String percentEncodeRfc3986(String s) {
        String out;
        try {
            out = URLEncoder.encode(s, UTF8_CHARSET)
                    .replace("+", "%20")
                    .replace("*", "%2A")
                    .replace("%7E", "~");
        } catch (UnsupportedEncodingException e) {
            out = s;
        }
        return out;
    }

    /**
     * The construct is private since we'd rather use getInstance()
     */
    private RequestsHelper() {        
    	this.maxResults = MAX_RESULTS;
    }
    
    public String createAddress(String tag) {

        //substitute token
        String address = RequestsHelper.EBAY_FINDING_SERVICE_URI;
        address = address.replace("{version}", RequestsHelper.SERVICE_VERSION);
        address = address.replace("{operation}", this.operation);
        address = address.replace("{globalId}", this.globalId);
        address = address.replace("{applicationId}", this.appId);
        address = address.replace("{keywords}", percentEncodeRfc3986(tag));
        address = address.replace("{maxresults}", "" + this.maxResults);

        if (Constant.debug) Log.d(Constant.LOGTAG, "Address " + address);
        return address;

    }
}
