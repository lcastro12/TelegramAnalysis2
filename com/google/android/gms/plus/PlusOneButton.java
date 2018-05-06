package com.google.android.gms.plus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import com.google.android.gms.internal.C0192s;
import com.google.android.gms.internal.C0194v;
import com.google.android.gms.internal.bu;

public final class PlusOneButton extends FrameLayout {
    public static final int ANNOTATION_BUBBLE = 1;
    public static final int ANNOTATION_INLINE = 2;
    public static final int ANNOTATION_NONE = 0;
    public static final int DEFAULT_ACTIVITY_REQUEST_CODE = -1;
    public static final int SIZE_MEDIUM = 1;
    public static final int SIZE_SMALL = 0;
    public static final int SIZE_STANDARD = 3;
    public static final int SIZE_TALL = 2;
    private int f42O;
    private View ic;
    private int id;
    private String ie;
    private int f43if;
    private OnPlusOneClickListener ig;

    public interface OnPlusOneClickListener {
        void onPlusOneClick(Intent intent);
    }

    protected class DefaultOnPlusOneClickListener implements OnClickListener, OnPlusOneClickListener {
        private final OnPlusOneClickListener ih;
        final /* synthetic */ PlusOneButton ii;

        public DefaultOnPlusOneClickListener(PlusOneButton plusOneButton, OnPlusOneClickListener proxy) {
            this.ii = plusOneButton;
            this.ih = proxy;
        }

        public void onClick(View view) {
            Intent intent = (Intent) this.ii.ic.getTag();
            if (this.ih != null) {
                this.ih.onPlusOneClick(intent);
            } else {
                onPlusOneClick(intent);
            }
        }

        public void onPlusOneClick(Intent intent) {
            Context context = this.ii.getContext();
            if ((context instanceof Activity) && intent != null) {
                ((Activity) context).startActivityForResult(intent, this.ii.f43if);
            }
        }
    }

    public PlusOneButton(Context context) {
        this(context, null);
    }

    public PlusOneButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.f42O = getSize(context, attrs);
        this.id = getAnnotation(context, attrs);
        this.f43if = -1;
        m601d(getContext());
        if (!isInEditMode()) {
        }
    }

    private void m601d(Context context) {
        if (this.ic != null) {
            removeView(this.ic);
        }
        this.ic = bu.m409a(context, this.f42O, this.id, this.ie, this.f43if);
        setOnPlusOneClickListener(this.ig);
        addView(this.ic);
    }

    protected static int getAnnotation(Context context, AttributeSet attrs) {
        String a = C0194v.m527a("http://schemas.android.com/apk/lib/com.google.android.gms.plus", "annotation", context, attrs, true, false, "PlusOneButton");
        if ("INLINE".equalsIgnoreCase(a)) {
            return 2;
        }
        return !"NONE".equalsIgnoreCase(a) ? 1 : 0;
    }

    protected static int getSize(Context context, AttributeSet attrs) {
        String a = C0194v.m527a("http://schemas.android.com/apk/lib/com.google.android.gms.plus", "size", context, attrs, true, false, "PlusOneButton");
        if ("SMALL".equalsIgnoreCase(a)) {
            return 0;
        }
        if ("MEDIUM".equalsIgnoreCase(a)) {
            return 1;
        }
        return "TALL".equalsIgnoreCase(a) ? 2 : 3;
    }

    public void initialize(String url, int activityRequestCode) {
        C0192s.m516a(getContext() instanceof Activity, "To use this method, the PlusOneButton must be placed in an Activity. Use initialize(PlusClient, String, OnPlusOneClickListener).");
        this.ie = url;
        this.f43if = activityRequestCode;
        m601d(getContext());
    }

    public void initialize(String url, OnPlusOneClickListener plusOneClickListener) {
        this.ie = url;
        this.f43if = 0;
        m601d(getContext());
        setOnPlusOneClickListener(plusOneClickListener);
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        this.ic.layout(0, 0, right - left, bottom - top);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        View view = this.ic;
        measureChild(view, widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(view.getMeasuredWidth(), view.getMeasuredHeight());
    }

    public void setAnnotation(int annotation) {
        this.id = annotation;
        m601d(getContext());
    }

    public void setOnPlusOneClickListener(OnPlusOneClickListener listener) {
        this.ig = listener;
        this.ic.setOnClickListener(new DefaultOnPlusOneClickListener(this, listener));
    }

    public void setSize(int size) {
        this.f42O = size;
        m601d(getContext());
    }
}
