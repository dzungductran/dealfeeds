package com.notalenthack.dealfeeds.appl;

import android.graphics.Bitmap;

import com.android.volley.toolbox.ImageLoader;
import com.notalenthack.dealfeeds.R;
import com.notalenthack.dealfeeds.common.Constant;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.notalenthack.dealfeeds.model.Product;
import com.notalenthack.dealfeeds.service.SettingData;

public class ProductCursorAdapter extends CursorAdapter {

    private Context mContext;
    private ImageDownloader mImageDownloader;

    class ViewHolder {
        View container;
        ImageView imageView;
        TextView title;
        ImageLoader.ImageContainer imageContainer;
    }

	public ProductCursorAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
        mContext = context;
        mImageDownloader = ImageDownloader.getInstance(context);
	}

    @Override
	public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder holder = (ViewHolder)view.getTag();

        // Create a product item out of cursor
        Product product = new Product(cursor);

        // link to the item
        ItemClickListener listener = new ItemClickListener(product);
        holder.container.setOnClickListener(listener);

        TextView title = holder.title;
        title.setOnClickListener(listener);
        StringBuffer sb = new StringBuffer();
        sb.append(product.getTitle());
        if (!product.getDescription().isEmpty()) {
            sb.append(" ");
            sb.append(product.getDescription());
        }
		title.setText(sb.toString());
		
		ImageView imageView = holder.imageView;
        imageView.setOnClickListener(listener);

        if (holder.imageContainer != null) {
            if (!holder.imageContainer.getRequestUrl().equals(product.getImageLink())) {
                holder.imageContainer.cancelRequest();
                holder.imageContainer = null;
            }
        }

        Bitmap bitmap = mImageDownloader.getBitmap(product.getImageLink());
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            if (Constant.debug) Log.d(Constant.LOGTAG, "LOAD_BITMAP from http: " + product.getImageLink());
            holder.imageContainer = mImageDownloader.download(product.getImageLink(), imageView);
        }
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
        if (Constant.debug) Log.d(Constant.LOGTAG, "newView called");
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);		
		View view = inflater.inflate(R.layout.product_item, parent, false);

        // Create a holder for holding the controls
        ViewHolder holder = new ViewHolder();
        holder.container = view.findViewById(R.id.product_container);
        holder.imageView = (ImageView)view.findViewById(R.id.product_image);
        holder.title = (TextView) view.findViewById(R.id.product_title);
        holder.imageContainer = null;
        view.setTag(holder);

		return view;
	}

    class ItemClickListener implements View.OnClickListener {
        private Product product;

        public ItemClickListener(Product product) {
            this.product = product;
        }

        @Override
        public void onClick(View v) {
            Intent launchingIntent = null;

            SettingData settings = MainActivity.getSettings();
            if (settings != null && settings.isCompareWithEbay()) {
                launchingIntent = new Intent(mContext, ItemActivity.class);
                launchingIntent.putExtra(Constant.ARG_ITEM, product);
                launchingIntent.putExtra(Constant.COMPARE_EBAY, settings.isCompareWithEbay());
            } else {
                launchingIntent = new Intent(mContext, BrowserActivity.class);
                launchingIntent.putExtra(Constant.ARG_LINK, product.getLink());
            }

            if (Constant.debug) Log.d(Constant.LOGTAG, "Launch item detail: " + product.getTitle());

            mContext.startActivity(launchingIntent);
        }
    }
}
