package net.hockeyapp.android.views;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import net.hockeyapp.android.utils.ViewHelper;

public class ExpiryInfoView extends RelativeLayout {
    public ExpiryInfoView(Context context) {
        this(context, "");
    }

    public ExpiryInfoView(Context context, String text) {
        super(context);
        loadLayoutParams(context);
        loadShadowView(context);
        loadTextView(context, text);
    }

    private void loadLayoutParams(Context context) {
        LayoutParams params = new LayoutParams(-1, -1);
        setBackgroundColor(-1);
        setLayoutParams(params);
    }

    private void loadShadowView(Context context) {
        LayoutParams params = new LayoutParams(-1, (int) TypedValue.applyDimension(1, 3.0f, getResources().getDisplayMetrics()));
        params.addRule(10, -1);
        ImageView shadowView = new ImageView(context);
        shadowView.setLayoutParams(params);
        shadowView.setBackgroundDrawable(ViewHelper.getGradient());
        addView(shadowView);
    }

    private void loadTextView(Context context, String text) {
        int margin = (int) TypedValue.applyDimension(1, 20.0f, getResources().getDisplayMetrics());
        LayoutParams params = new LayoutParams(-1, -2);
        params.addRule(13, -1);
        params.setMargins(margin, margin, margin, margin);
        TextView textView = new TextView(context);
        textView.setGravity(17);
        textView.setLayoutParams(params);
        textView.setText(text);
        textView.setTextColor(ViewCompat.MEASURED_STATE_MASK);
        addView(textView);
    }
}
