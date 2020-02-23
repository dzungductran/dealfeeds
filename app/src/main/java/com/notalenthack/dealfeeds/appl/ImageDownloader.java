
/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.notalenthack.dealfeeds.appl;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.notalenthack.dealfeeds.R;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;
import android.widget.ImageView;

/**
 * This helper class download images from the Internet and binds those with the provided ImageView.
 *
 * <p>It requires the INTERNET permission, which should be added to your application's manifest
 * file.</p>
 *
 * A local cache of downloaded images is maintained internally to improve performance.
 */
public class ImageDownloader extends LruCache<String, Bitmap> implements ImageLoader.ImageCache {
    private static final String LOG_TAG = "ImageDownloader";

    private Context mContext;
    private static ImageDownloader sInstance = null;

    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;

    private static Bitmap mBmpStub;  // stub bitmap

    public static int getDefaultLruCacheSize(Context context) {
        final int cacheSize = 1024 * 1024 * (((ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE))
                .getMemoryClass()) / 8;

        return cacheSize;
    }

    private ImageDownloader(Context context) {
        super(getDefaultLruCacheSize(context));

        mContext = context;
        mRequestQueue = Volley.newRequestQueue(context);
        mImageLoader = new ImageLoader(mRequestQueue, this);

        mBmpStub = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.untitled);
    }

    public static synchronized ImageDownloader getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ImageDownloader(context);
        }

        return sInstance;
    }

    @Override
    protected int sizeOf(String key, Bitmap value) {
        return value.getByteCount();
    }

    @Override
    public Bitmap getBitmap(String url) {
        if (url == null || url.isEmpty()) {
            return mBmpStub;
        } else {
            return get(url);
        }
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        if (bitmap.getByteCount() > 16)  {
            put(url, bitmap);
        } else {
            put(url, mBmpStub);
        }
    }

    /**
     * Download the specified image from the Internet and binds it to the provided ImageView. The
     * binding is immediate if the image is found in the cache and will be done asynchronously
     * otherwise. A null bitmap will be associated to the ImageView if an error occurs.
     *
     * @param url The URL of the image to download.
     * @param imageView The ImageView to bind the downloaded image to.
     */
    public ImageLoader.ImageContainer download(String url, ImageView imageView) {
        return mImageLoader.get(url,
                ImageLoader.getImageListener(imageView, R.drawable.untitled, R.drawable.unknown_item));
    }
}