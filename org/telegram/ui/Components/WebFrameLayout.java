package org.telegram.ui.Components;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build.VERSION;
import android.support.v4.view.ViewCompat;
import android.text.ClipboardManager;
import android.text.TextUtils.TruncateAt;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebChromeClient.CustomViewCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.plus.PlusShare;
import java.util.HashMap;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.C0553R;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.BottomSheet.BottomSheetDelegate;

public class WebFrameLayout extends FrameLayout {
    private View customView;
    private CustomViewCallback customViewCallback;
    private BottomSheet dialog;
    private FrameLayout fullscreenVideoContainer;
    private int height;
    private String openUrl;
    private ProgressBar progressBar;
    private WebView webView;
    private int width;

    class C09751 implements OnClickListener {
        C09751() {
        }

        public void onClick(View v) {
            Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(WebFrameLayout.this.openUrl));
            intent.putExtra("com.android.browser.application_id", WebFrameLayout.this.getContext().getPackageName());
            WebFrameLayout.this.getContext().startActivity(intent);
            if (WebFrameLayout.this.dialog != null) {
                WebFrameLayout.this.dialog.dismiss();
            }
        }
    }

    class C09762 implements OnClickListener {
        C09762() {
        }

        public void onClick(View v) {
            try {
                if (VERSION.SDK_INT < 11) {
                    ((ClipboardManager) ApplicationLoader.applicationContext.getSystemService("clipboard")).setText(WebFrameLayout.this.openUrl);
                } else {
                    ((android.content.ClipboardManager) ApplicationLoader.applicationContext.getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText(PlusShare.KEY_CALL_TO_ACTION_LABEL, WebFrameLayout.this.openUrl));
                }
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
            Toast.makeText(WebFrameLayout.this.getContext(), LocaleController.getString("LinkCopied", C0553R.string.LinkCopied), 0).show();
            if (WebFrameLayout.this.dialog != null) {
                WebFrameLayout.this.dialog.dismiss();
            }
        }
    }

    class C09773 extends WebChromeClient {
        C09773() {
        }

        public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {
            onShowCustomView(view, callback);
        }

        public void onShowCustomView(View view, CustomViewCallback callback) {
            if (WebFrameLayout.this.customView != null) {
                callback.onCustomViewHidden();
                return;
            }
            WebFrameLayout.this.customView = view;
            if (WebFrameLayout.this.dialog != null) {
                WebFrameLayout.this.dialog.getSheetContainer().setVisibility(4);
                WebFrameLayout.this.fullscreenVideoContainer.setVisibility(0);
                WebFrameLayout.this.fullscreenVideoContainer.addView(view, LayoutHelper.createFrame(-1, GroundOverlayOptions.NO_DIMENSION));
            }
            WebFrameLayout.this.customViewCallback = callback;
        }

        public void onHideCustomView() {
            super.onHideCustomView();
            if (WebFrameLayout.this.customView != null) {
                if (WebFrameLayout.this.dialog != null) {
                    WebFrameLayout.this.dialog.getSheetContainer().setVisibility(0);
                    WebFrameLayout.this.fullscreenVideoContainer.setVisibility(4);
                    WebFrameLayout.this.fullscreenVideoContainer.removeView(WebFrameLayout.this.customView);
                }
                if (!(WebFrameLayout.this.customViewCallback == null || WebFrameLayout.this.customViewCallback.getClass().getName().contains(".chromium."))) {
                    WebFrameLayout.this.customViewCallback.onCustomViewHidden();
                }
                WebFrameLayout.this.customView = null;
            }
        }
    }

    class C09784 extends WebViewClient {
        C09784() {
        }

        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
        }

        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            WebFrameLayout.this.progressBar.setVisibility(4);
        }
    }

    class C09795 implements OnTouchListener {
        C09795() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            return true;
        }
    }

    @SuppressLint({"SetJavaScriptEnabled"})
    public WebFrameLayout(Context context, BottomSheet parentDialog, String title, String originalUrl, String url, int w, int h) {
        super(context);
        this.openUrl = originalUrl;
        this.width = w;
        this.height = h;
        if (this.width == 0 || this.height == 0) {
            this.width = AndroidUtilities.displaySize.x;
            this.height = AndroidUtilities.displaySize.y / 2;
        }
        this.dialog = parentDialog;
        this.fullscreenVideoContainer = new FrameLayout(context);
        this.fullscreenVideoContainer.setBackgroundColor(ViewCompat.MEASURED_STATE_MASK);
        if (VERSION.SDK_INT >= 21) {
            this.fullscreenVideoContainer.setFitsSystemWindows(true);
        }
        this.dialog.getContainer().addView(this.fullscreenVideoContainer, LayoutHelper.createFrame(-1, GroundOverlayOptions.NO_DIMENSION));
        this.fullscreenVideoContainer.setVisibility(4);
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(0);
        addView(linearLayout, LayoutHelper.createFrame(-1, 32, 51));
        TextView textView = new TextView(context);
        textView.setTextColor(-10066330);
        textView.setText(title);
        textView.setSingleLine(true);
        textView.setEllipsize(TruncateAt.END);
        textView.setGravity(16);
        textView.setTextSize(1, 18.0f);
        linearLayout.addView(textView, LayoutHelper.createLinear(0, -1, 1.0f, 16, 0, 0, 0));
        textView = new TextView(context);
        textView.setTextColor(-6710887);
        textView.setText(LocaleController.getString("OpenInBrowser", C0553R.string.OpenInBrowser));
        textView.setGravity(16);
        textView.setTextSize(1, 12.0f);
        linearLayout.addView(textView, LayoutHelper.createLinear(-2, -1, 16.0f, 0.0f, 0.0f, 0.0f));
        textView.setOnClickListener(new C09751());
        textView = new TextView(context);
        textView.setTextColor(-6710887);
        textView.setText(LocaleController.getString("CopyUrl", C0553R.string.CopyUrl));
        textView.setGravity(16);
        textView.setTextSize(1, 12.0f);
        linearLayout.addView(textView, LayoutHelper.createLinear(-2, -1, 16.0f, 0.0f, 16.0f, 0.0f));
        textView.setOnClickListener(new C09762());
        View lineView = new View(context);
        lineView.setBackgroundResource(C0553R.drawable.header_shadow);
        addView(lineView, LayoutHelper.createFrame(-1, 3.0f, 51, 0.0f, 40.0f, 0.0f, 0.0f));
        this.webView = new WebView(context);
        this.webView.getSettings().setJavaScriptEnabled(true);
        this.webView.getSettings().setDomStorageEnabled(true);
        if (VERSION.SDK_INT >= 17) {
            this.webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        }
        String userAgent = this.webView.getSettings().getUserAgentString();
        if (userAgent != null) {
            this.webView.getSettings().setUserAgentString(userAgent.replace("Android", ""));
        }
        if (VERSION.SDK_INT >= 21) {
            this.webView.getSettings().setMixedContentMode(0);
            CookieManager.getInstance().setAcceptThirdPartyCookies(this.webView, true);
        }
        this.webView.setWebChromeClient(new C09773());
        this.webView.setWebViewClient(new C09784());
        addView(this.webView, LayoutHelper.createFrame(-1, GroundOverlayOptions.NO_DIMENSION, 51, 8.0f, 49.0f, 8.0f, 0.0f));
        this.progressBar = new ProgressBar(context);
        addView(this.progressBar, LayoutHelper.createFrame(-2, -2.0f, 17, 8.0f, 24.0f, 8.0f, 0.0f));
        setOnTouchListener(new C09795());
        final String str = url;
        parentDialog.setDelegate(new BottomSheetDelegate() {
            public void onOpenAnimationEnd() {
                HashMap<String, String> args = new HashMap();
                args.put("Referer", "http://youtube.com");
                try {
                    WebFrameLayout.this.webView.loadUrl(str, args);
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
        });
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        try {
            removeView(this.webView);
            this.webView.stopLoading();
            this.webView.loadUrl("about:blank");
            this.webView.destroy();
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(49.0f) + ((int) Math.min(((float) this.height) / ((float) (this.width / MeasureSpec.getSize(widthMeasureSpec))), (float) (AndroidUtilities.displaySize.y / 2))), 1073741824));
    }
}
