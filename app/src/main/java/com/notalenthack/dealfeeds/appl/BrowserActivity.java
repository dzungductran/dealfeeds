package com.notalenthack.dealfeeds.appl;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import android.widget.ShareActionProvider;
import com.notalenthack.dealfeeds.R;
import com.notalenthack.dealfeeds.common.Constant;

public class BrowserActivity extends Activity {

    private String mLink;
    private WebView mWebView;
    private ShareActionProvider mShareActionProvider;

    /** Called when the activity is first created. */
    @Override
    public void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        final Intent launchingIntent = getIntent();

        // Retrieve the Query Type and Position from the intent or bundle
        if (savedInstanceState != null) {
            mLink = savedInstanceState.getString(Constant.ARG_LINK);
        } else if (launchingIntent != null) {
            mLink = launchingIntent.getStringExtra(Constant.ARG_LINK);
        }

        setContentView(R.layout.browser);

        if (mLink != null) {
            mWebView = (WebView) findViewById(R.id.webControl);
            mWebView.getSettings().setJavaScriptEnabled(true);

            mWebView.getSettings().setBuiltInZoomControls(true);
            mWebView.getSettings().setUseWideViewPort(true);
            mWebView.getSettings().setLoadWithOverviewMode(true);
            // remove a weird white line on the right size
            mWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
//            mWebView.setInitialScale(50);
            String ua = "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.4) Gecko/20100101 Firefox/4.0";
            mWebView.getSettings().setUserAgentString(ua);

            mWebView.setWebViewClient(new WebClient());
            mWebView.loadUrl(mLink);

            getActionBar().setHomeButtonEnabled(true);
            getActionBar().setIcon(R.drawable.ic_action_navigation_previous_item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        if (mLink != null) {
            savedInstanceState.putString(Constant.ARG_LINK, mLink);
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mLink = savedInstanceState.getString(Constant.ARG_LINK);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.options, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);
        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) item.getActionProvider();

        // Calling super after populating the menu is necessary here to ensure that the
        // action bar helpers have a chance to handle this event.
        return super.onCreateOptionsMenu(menu);
    }

    // Call to update the share intent. You may only need to set the share intent once during the creation of your
    // menus, or you may want to set it and then update it as the UI changes. For example, when you view photos
    // full screen in the Gallery app, the sharing intent changes as you flip between photos.
    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private class WebClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            getActionBar().setTitle(view.getTitle());
            mLink = url;
            return false;
        }

        @Override
        public void onPageStarted (WebView view, String url, Bitmap favicon) {
            getActionBar().setTitle("Loading...");
            mLink = url;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            getActionBar().setTitle(view.getTitle());
            mLink = url;
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, view.getTitle());
            i.putExtra(Intent.EXTRA_TEXT, mLink);
            //startActivity(Intent.createChooser(i, "Share URL"));
            setShareIntent(i);
        }
    }

}