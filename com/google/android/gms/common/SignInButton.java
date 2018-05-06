package com.google.android.gms.common;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import com.google.android.gms.dynamic.C0149e.C0148a;
import com.google.android.gms.internal.C0192s;
import com.google.android.gms.internal.C0193u;
import com.google.android.gms.internal.C1361t;

public final class SignInButton extends FrameLayout implements OnClickListener {
    public static final int COLOR_DARK = 0;
    public static final int COLOR_LIGHT = 1;
    public static final int SIZE_ICON_ONLY = 2;
    public static final int SIZE_STANDARD = 0;
    public static final int SIZE_WIDE = 1;
    private int f23O;
    private int f24P;
    private View f25Q;
    private OnClickListener f26R;

    public SignInButton(Context context) {
        this(context, null);
    }

    public SignInButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SignInButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.f26R = null;
        setStyle(0, 0);
    }

    private static Button m31c(Context context, int i, int i2) {
        Button c0193u = new C0193u(context);
        c0193u.m526a(context.getResources(), i, i2);
        return c0193u;
    }

    private void m32d(Context context) {
        if (this.f25Q != null) {
            removeView(this.f25Q);
        }
        try {
            this.f25Q = C1361t.m1021d(context, this.f23O, this.f24P);
        } catch (C0148a e) {
            Log.w("SignInButton", "Sign in button not found, using placeholder instead");
            this.f25Q = m31c(context, this.f23O, this.f24P);
        }
        addView(this.f25Q);
        this.f25Q.setEnabled(isEnabled());
        this.f25Q.setOnClickListener(this);
    }

    public void onClick(View view) {
        if (this.f26R != null && view == this.f25Q) {
            this.f26R.onClick(this);
        }
    }

    public void setColorScheme(int colorScheme) {
        setStyle(this.f23O, colorScheme);
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.f25Q.setEnabled(enabled);
    }

    public void setOnClickListener(OnClickListener listener) {
        this.f26R = listener;
        if (this.f25Q != null) {
            this.f25Q.setOnClickListener(this);
        }
    }

    public void setSize(int buttonSize) {
        setStyle(buttonSize, this.f24P);
    }

    public void setStyle(int buttonSize, int colorScheme) {
        boolean z = true;
        boolean z2 = buttonSize >= 0 && buttonSize < 3;
        C0192s.m516a(z2, "Unknown button size " + buttonSize);
        if (colorScheme < 0 || colorScheme >= 2) {
            z = false;
        }
        C0192s.m516a(z, "Unknown color scheme " + colorScheme);
        this.f23O = buttonSize;
        this.f24P = colorScheme;
        m32d(getContext());
    }
}
