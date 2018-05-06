package com.google.android.gms.internal;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;
import com.google.android.gms.C0126R;

public final class C0193u extends Button {
    public C0193u(Context context) {
        this(context, null);
    }

    public C0193u(Context context, AttributeSet attributeSet) {
        super(context, attributeSet, 16842824);
    }

    private int m522a(int i, int i2, int i3) {
        switch (i) {
            case 0:
                return i2;
            case 1:
                return i3;
            default:
                throw new IllegalStateException("Unknown color scheme: " + i);
        }
    }

    private void m523b(Resources resources, int i, int i2) {
        int a;
        switch (i) {
            case 0:
            case 1:
                a = m522a(i2, C0126R.drawable.common_signin_btn_text_dark, C0126R.drawable.common_signin_btn_text_light);
                break;
            case 2:
                a = m522a(i2, C0126R.drawable.common_signin_btn_icon_dark, C0126R.drawable.common_signin_btn_icon_light);
                break;
            default:
                throw new IllegalStateException("Unknown button size: " + i);
        }
        if (a == -1) {
            throw new IllegalStateException("Could not find background resource!");
        }
        setBackgroundDrawable(resources.getDrawable(a));
    }

    private void m524c(Resources resources) {
        setTypeface(Typeface.DEFAULT_BOLD);
        setTextSize(14.0f);
        float f = resources.getDisplayMetrics().density;
        setMinHeight((int) ((f * 48.0f) + 0.5f));
        setMinWidth((int) ((f * 48.0f) + 0.5f));
    }

    private void m525c(Resources resources, int i, int i2) {
        setTextColor(resources.getColorStateList(m522a(i2, C0126R.color.common_signin_btn_text_dark, C0126R.color.common_signin_btn_text_light)));
        switch (i) {
            case 0:
                setText(resources.getString(C0126R.string.common_signin_button_text));
                return;
            case 1:
                setText(resources.getString(C0126R.string.common_signin_button_text_long));
                return;
            case 2:
                setText(null);
                return;
            default:
                throw new IllegalStateException("Unknown button size: " + i);
        }
    }

    public void m526a(Resources resources, int i, int i2) {
        boolean z = true;
        boolean z2 = i >= 0 && i < 3;
        C0192s.m516a(z2, "Unknown button size " + i);
        if (i2 < 0 || i2 >= 2) {
            z = false;
        }
        C0192s.m516a(z, "Unknown color scheme " + i2);
        m524c(resources);
        m523b(resources, i, i2);
        m525c(resources, i, i2);
    }
}
