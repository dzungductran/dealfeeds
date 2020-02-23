package com.notalenthack.dealfeeds.appl;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.notalenthack.dealfeeds.R;
import com.notalenthack.dealfeeds.common.Constant;
import com.notalenthack.dealfeeds.model.Product;

/**
 * Simple adapter
 */
public class ItemListAdapter extends ArrayAdapter<Product> {
    private final Context context;
    private final Product[] items;
    private ImageDownloader mImageDownloader;

    public ItemListAdapter(Context context, Product[] items) {
        super(context, R.layout.product_item, items);
        this.context = context;
        this.items = items;
        mImageDownloader = ImageDownloader.getInstance(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.product_item, parent, false);
        ImageView imageView = (ImageView)view.findViewById(R.id.product_image);
        TextView title = (TextView) view.findViewById(R.id.product_title);

        Bitmap bitmap = mImageDownloader.getBitmap(items[position].getImageLink());
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            if (Constant.debug) Log.d(Constant.LOGTAG, "LOAD_BITMAP from http: " + items[position].getImageLink());
            mImageDownloader.download(items[position].getImageLink(), imageView);
        }
        title.setText(items[position].getTitle());
        return view;
    }
}