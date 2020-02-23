package com.notalenthack.dealfeeds.appl;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.notalenthack.dealfeeds.R;

public class ImgAdapter extends ArrayAdapter<String> {
	private final Context context;
	private final String[] values;
	private final int[] resources;

	public ImgAdapter(Context context, String[] values, int[] resources) {
		super(context, R.layout.rowlayout, values);
		this.context = context;
		this.values = values;
		this.resources = resources;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.rowlayout, parent, false);
		TextView textView = (TextView) rowView.findViewById(R.id.text);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.image);
		textView.setText(values[position]);
		imageView.setImageResource(resources[position]);

		return rowView;
	}
}
