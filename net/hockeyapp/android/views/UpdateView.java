package net.hockeyapp.android.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import net.hockeyapp.android.utils.ViewHelper;

public class UpdateView extends RelativeLayout {
    public static final int HEADER_VIEW_ID = 4097;
    public static final int NAME_LABEL_ID = 4098;
    public static final int UPDATE_BUTTON_ID = 4100;
    public static final int VERSION_LABEL_ID = 4099;
    public static final int WEB_VIEW_ID = 4101;
    protected RelativeLayout headerView;
    protected boolean layoutHorizontally;
    protected boolean limitHeight;

    public UpdateView(Context context) {
        this(context, true);
    }

    public UpdateView(Context context, AttributeSet attrs) {
        this(context, true, false);
    }

    public UpdateView(Context context, boolean allowHorizontalLayout) {
        this(context, true, false);
    }

    public UpdateView(Context context, boolean allowHorizontalLayout, boolean limitHeight) {
        super(context);
        this.headerView = null;
        this.layoutHorizontally = false;
        this.limitHeight = false;
        if (allowHorizontalLayout) {
            setLayoutHorizontally(context);
        } else {
            this.layoutHorizontally = false;
        }
        this.limitHeight = limitHeight;
        loadLayoutParams(context);
        loadHeaderView(context);
        loadWebView(context);
        loadShadow(this.headerView, context);
    }

    private void setLayoutHorizontally(Context context) {
        if (getResources().getConfiguration().orientation == 2) {
            this.layoutHorizontally = true;
        } else {
            this.layoutHorizontally = false;
        }
    }

    private void loadLayoutParams(Context context) {
        LayoutParams params = new LayoutParams(-1, -2);
        setBackgroundColor(-1);
        setLayoutParams(params);
    }

    private void loadHeaderView(Context context) {
        LayoutParams params;
        this.headerView = new RelativeLayout(context);
        this.headerView.setId(4097);
        if (this.layoutHorizontally) {
            params = new LayoutParams((int) TypedValue.applyDimension(1, 250.0f, getResources().getDisplayMetrics()), -1);
            params.addRule(9, -1);
            this.headerView.setPadding(0, 0, 0, 0);
        } else {
            params = new LayoutParams(-1, -2);
            this.headerView.setPadding(0, 0, 0, (int) TypedValue.applyDimension(1, 20.0f, getResources().getDisplayMetrics()));
        }
        this.headerView.setLayoutParams(params);
        this.headerView.setBackgroundColor(Color.rgb(230, 236, 239));
        loadTitleLabel(this.headerView, context);
        loadVersionLabel(this.headerView, context);
        loadUpdateButton(this.headerView, context);
        addView(this.headerView);
    }

    private void loadTitleLabel(RelativeLayout headerView, Context context) {
        TextView textView = new TextView(context);
        textView.setId(4098);
        LayoutParams params = new LayoutParams(-2, -2);
        int margin = (int) TypedValue.applyDimension(1, 20.0f, getResources().getDisplayMetrics());
        params.setMargins(margin, margin, margin, 0);
        textView.setBackgroundColor(Color.rgb(230, 236, 239));
        textView.setLayoutParams(params);
        textView.setEllipsize(TruncateAt.END);
        textView.setShadowLayer(1.0f, 0.0f, 1.0f, -1);
        textView.setSingleLine(true);
        textView.setTextColor(ViewCompat.MEASURED_STATE_MASK);
        textView.setTextSize(2, 20.0f);
        textView.setTypeface(null, 1);
        headerView.addView(textView);
    }

    private void loadVersionLabel(RelativeLayout headerView, Context context) {
        TextView textView = new TextView(context);
        textView.setId(4099);
        LayoutParams params = new LayoutParams(-2, -2);
        int marginSide = (int) TypedValue.applyDimension(1, 20.0f, getResources().getDisplayMetrics());
        params.setMargins(marginSide, (int) TypedValue.applyDimension(1, 10.0f, getResources().getDisplayMetrics()), marginSide, 0);
        params.addRule(3, 4098);
        textView.setBackgroundColor(Color.rgb(230, 236, 239));
        textView.setLayoutParams(params);
        textView.setEllipsize(TruncateAt.END);
        textView.setShadowLayer(1.0f, 0.0f, 1.0f, -1);
        textView.setLines(2);
        textView.setLineSpacing(0.0f, 1.1f);
        textView.setTextColor(ViewCompat.MEASURED_STATE_MASK);
        textView.setTextSize(2, 16.0f);
        textView.setTypeface(null, 1);
        headerView.addView(textView);
    }

    private void loadUpdateButton(RelativeLayout headerView, Context context) {
        Button button = new Button(context);
        button.setId(UPDATE_BUTTON_ID);
        int margin = (int) TypedValue.applyDimension(1, 20.0f, getResources().getDisplayMetrics());
        LayoutParams params = new LayoutParams((int) TypedValue.applyDimension(1, BitmapDescriptorFactory.HUE_GREEN, getResources().getDisplayMetrics()), -2);
        params.setMargins(margin, margin, margin, margin);
        params.addRule(9, -1);
        params.addRule(3, 4099);
        button.setLayoutParams(params);
        button.setBackgroundDrawable(getButtonSelector());
        button.setText("Update");
        button.setTextColor(-1);
        button.setTextSize(2, 16.0f);
        headerView.addView(button);
    }

    private Drawable getButtonSelector() {
        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[]{-16842919}, new ColorDrawable(ViewCompat.MEASURED_STATE_MASK));
        drawable.addState(new int[]{-16842919, 16842908}, new ColorDrawable(-12303292));
        drawable.addState(new int[]{16842919}, new ColorDrawable(-7829368));
        return drawable;
    }

    private void loadShadow(RelativeLayout headerView, Context context) {
        LayoutParams params;
        int height = (int) TypedValue.applyDimension(1, 3.0f, getResources().getDisplayMetrics());
        ImageView topShadowView = new ImageView(context);
        if (this.layoutHorizontally) {
            params = new LayoutParams(1, -1);
            params.addRule(11, -1);
            topShadowView.setBackgroundDrawable(new ColorDrawable(ViewCompat.MEASURED_STATE_MASK));
        } else {
            params = new LayoutParams(-1, height);
            params.addRule(10, -1);
            topShadowView.setBackgroundDrawable(ViewHelper.getGradient());
        }
        topShadowView.setLayoutParams(params);
        headerView.addView(topShadowView);
        ImageView bottomShadowView = new ImageView(context);
        params = new LayoutParams(-1, height);
        if (this.layoutHorizontally) {
            params.addRule(10, -1);
        } else {
            params.addRule(3, 4097);
        }
        bottomShadowView.setLayoutParams(params);
        bottomShadowView.setBackgroundDrawable(ViewHelper.getGradient());
        addView(bottomShadowView);
    }

    private void loadWebView(Context context) {
        WebView webView = new WebView(context);
        webView.setId(WEB_VIEW_ID);
        int height = (int) TypedValue.applyDimension(1, 400.0f, context.getResources().getDisplayMetrics());
        if (!this.limitHeight) {
            height = -1;
        }
        LayoutParams params = new LayoutParams(-1, height);
        if (this.layoutHorizontally) {
            params.addRule(1, 4097);
        } else {
            params.addRule(3, 4097);
        }
        params.setMargins(0, 0, 0, 0);
        webView.setLayoutParams(params);
        webView.setBackgroundColor(-1);
        addView(webView);
    }
}
