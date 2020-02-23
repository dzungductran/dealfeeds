package com.notalenthack.dealfeeds.appl.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.notalenthack.dealfeeds.R;

/*
 * Progress dialog mostly from this: http://stackoverflow.com/questions/3225889/
 *   how-to-center-progress-indicator-in-progressdialog-easily-when-no-title-text-pa/3226233#3226233
 */
public class ProgressIndicator extends Dialog {
    static OnCancelListener mListener;

    public static ProgressIndicator show(Context context, CharSequence title, CharSequence message) {
        return show(context, title, message, false, null);
    }

    public static ProgressIndicator show(Context context, CharSequence title,
                                        CharSequence message, boolean cancelable) {
        return show(context, title, message, cancelable, null);
    }

    public static ProgressIndicator show(Context context, CharSequence title, CharSequence message,
                                        boolean cancelable, OnCancelListener cancelListener) {
        mListener = cancelListener;
        ProgressIndicator dialog = new ProgressIndicator(context);
        dialog.setCancelable(false);
        //dialog.setCanceledOnTouchOutside(true);
        dialog.setOnCancelListener(null);

        /* The next line will add the ProgressBar to the dialog. */
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

        TextView windowTitle = new TextView(context);
        windowTitle.setText(title);

        View v = new ProgressBar(context);
        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mListener.onCancel(null);
                return true;
            }
        });
        linearLayout.addView(v);
        linearLayout.addView(windowTitle);
        dialog.addContentView(linearLayout,
            new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        dialog.show();

        return dialog;
    }

    public ProgressIndicator(Context context) {
        super(context, R.style.NewDialog);
    }
}
