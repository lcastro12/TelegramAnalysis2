package org.telegram.ui.Components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import org.telegram.messenger.AndroidUtilities;

public class SimpleTextView extends View {
    private int gravity;
    private Layout layout;
    private int offsetX;
    private SpannableStringBuilder spannableStringBuilder;
    private CharSequence text;
    private TextPaint textPaint = new TextPaint(1);
    private boolean wasLayout = false;

    public SimpleTextView(Context context) {
        super(context);
    }

    public void setTextColor(int color) {
        this.textPaint.setColor(color);
    }

    public void setTextSize(int size) {
        this.textPaint.setTextSize((float) AndroidUtilities.dp((float) size));
    }

    public void setGravity(int value) {
        this.gravity = value;
    }

    public void setTypeface(Typeface typeface) {
        this.textPaint.setTypeface(typeface);
    }

    private void createLayout(int width) {
        if (this.text != null) {
            try {
                CharSequence string = TextUtils.ellipsize(this.text, this.textPaint, (float) width, TruncateAt.END);
                this.layout = new StaticLayout(string, 0, string.length(), this.textPaint, width, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                if (this.layout.getLineCount() <= 0) {
                    return;
                }
                if ((this.gravity & 7) == 3) {
                    this.offsetX = -((int) this.layout.getLineLeft(0));
                } else if (this.layout.getLineLeft(0) == 0.0f) {
                    this.offsetX = (int) (((float) width) - this.layout.getLineWidth(0));
                } else {
                    this.offsetX = 0;
                }
            } catch (Exception e) {
            }
        }
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed) {
            createLayout(right - left);
            invalidate();
            this.wasLayout = true;
        }
    }

    public void setText(CharSequence value) {
        this.text = value;
        if (this.wasLayout) {
            createLayout(getMeasuredWidth());
            invalidate();
            return;
        }
        requestLayout();
    }

    protected void onDraw(Canvas canvas) {
        if (this.layout != null) {
            if (this.offsetX != 0) {
                canvas.save();
                canvas.translate((float) this.offsetX, 0.0f);
            }
            this.layout.draw(canvas);
            if (this.offsetX != 0) {
                canvas.restore();
            }
        }
    }
}
