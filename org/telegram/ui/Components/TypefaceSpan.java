package org.telegram.ui.Components;

import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

public class TypefaceSpan extends MetricAffectingSpan {
    private Typeface mTypeface;

    public TypefaceSpan(Typeface typeface) {
        this.mTypeface = typeface;
    }

    public void updateMeasureState(TextPaint p) {
        p.setTypeface(this.mTypeface);
        p.setFlags(p.getFlags() | 128);
    }

    public void updateDrawState(TextPaint tp) {
        tp.setTypeface(this.mTypeface);
        tp.setFlags(tp.getFlags() | 128);
    }
}
