package com.google.android.gms.plus;

import android.app.PendingIntent;
import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import com.google.android.gms.internal.C0192s;
import com.google.android.gms.internal.br;
import com.google.android.gms.internal.br.C1341a;
import com.google.android.gms.internal.bu;

public final class PlusOneButtonWithPopup extends ViewGroup {
    private int f44O;
    private String f45g;
    private View ic;
    private int id;
    private String ie;
    private OnClickListener ij;

    public PlusOneButtonWithPopup(Context context) {
        this(context, null);
    }

    public PlusOneButtonWithPopup(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.f44O = PlusOneButton.getSize(context, attrs);
        this.id = PlusOneButton.getAnnotation(context, attrs);
        this.ic = new PlusOneDummyView(context, this.f44O);
        addView(this.ic);
    }

    private void bv() {
        if (this.ic != null) {
            removeView(this.ic);
        }
        this.ic = bu.m410a(getContext(), this.f44O, this.id, this.ie, this.f45g);
        if (this.ij != null) {
            setOnClickListener(this.ij);
        }
        addView(this.ic);
    }

    private br bw() throws RemoteException {
        br aa = C1341a.aa((IBinder) this.ic.getTag());
        if (aa != null) {
            return aa;
        }
        if (Log.isLoggable("PlusOneButtonWithPopup", 5)) {
            Log.w("PlusOneButtonWithPopup", "Failed to get PlusOneDelegate");
        }
        throw new RemoteException();
    }

    private int m602c(int i, int i2) {
        int mode = MeasureSpec.getMode(i);
        switch (mode) {
            case Integer.MIN_VALUE:
            case 1073741824:
                return MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(i) - i2, mode);
            default:
                return i;
        }
    }

    public void cancelClick() {
        if (this.ic != null) {
            try {
                bw().cancelClick();
            } catch (RemoteException e) {
            }
        }
    }

    public PendingIntent getResolution() {
        if (this.ic != null) {
            try {
                return bw().getResolution();
            } catch (RemoteException e) {
            }
        }
        return null;
    }

    public void initialize(String url, String accountName) {
        C0192s.m518b((Object) url, (Object) "Url must not be null");
        this.ie = url;
        this.f45g = accountName;
        bv();
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        this.ic.layout(getPaddingLeft(), getPaddingTop(), (right - left) - getPaddingRight(), (bottom - top) - getPaddingBottom());
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int paddingLeft = getPaddingLeft() + getPaddingRight();
        int paddingTop = getPaddingTop() + getPaddingBottom();
        this.ic.measure(m602c(widthMeasureSpec, paddingLeft), m602c(heightMeasureSpec, paddingTop));
        setMeasuredDimension(paddingLeft + this.ic.getMeasuredWidth(), paddingTop + this.ic.getMeasuredHeight());
    }

    public void reinitialize() {
        if (this.ic != null) {
            try {
                bw().reinitialize();
            } catch (RemoteException e) {
            }
        }
    }

    public void setAnnotation(int annotation) {
        this.id = annotation;
        bv();
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.ij = onClickListener;
        this.ic.setOnClickListener(onClickListener);
    }

    public void setSize(int size) {
        this.f44O = size;
        bv();
    }
}
