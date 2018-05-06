package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RadioButton;

public class LastSeenRadioCell extends FrameLayout {
    private static Paint paint;
    private boolean needDivider;
    private RadioButton radioButton;
    private TextView textView;

    public LastSeenRadioCell(Context context) {
        int i;
        int i2 = 3;
        super(context);
        if (paint == null) {
            paint = new Paint();
            paint.setColor(-2500135);
            paint.setStrokeWidth(1.0f);
        }
        this.textView = new TextView(context);
        this.textView.setTextColor(-14606047);
        this.textView.setTextSize(1, 16.0f);
        this.textView.setLines(1);
        this.textView.setMaxLines(1);
        this.textView.setSingleLine(true);
        this.textView.setEllipsize(TruncateAt.END);
        this.textView.setGravity((LocaleController.isRTL ? 5 : 3) | 16);
        View view = this.textView;
        if (LocaleController.isRTL) {
            i = 5;
        } else {
            i = 3;
        }
        addView(view, LayoutHelper.createFrame(-1, GroundOverlayOptions.NO_DIMENSION, i | 48, 17.0f, 0.0f, 17.0f, 0.0f));
        this.radioButton = new RadioButton(context);
        this.radioButton.setSize(AndroidUtilities.dp(20.0f));
        this.radioButton.setColor(-5000269, -13129232);
        View view2 = this.radioButton;
        if (!LocaleController.isRTL) {
            i2 = 5;
        }
        addView(view2, LayoutHelper.createFrame(22, 22.0f, i2 | 48, (float) (LocaleController.isRTL ? 18 : 0), 13.0f, (float) (LocaleController.isRTL ? 0 : 18), 0.0f));
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), (this.needDivider ? 1 : 0) + AndroidUtilities.dp(48.0f));
        int availableWidth = ((getMeasuredWidth() - getPaddingLeft()) - getPaddingRight()) - AndroidUtilities.dp(34.0f);
        this.radioButton.measure(MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(22.0f), Integer.MIN_VALUE), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(22.0f), 1073741824));
        this.textView.measure(MeasureSpec.makeMeasureSpec(availableWidth, 1073741824), MeasureSpec.makeMeasureSpec(getMeasuredHeight(), 1073741824));
    }

    public void setTextColor(int color) {
        this.textView.setTextColor(color);
    }

    public void setText(String text, boolean checked, boolean divider) {
        boolean z = false;
        this.textView.setText(text);
        this.radioButton.setChecked(checked, false);
        this.needDivider = divider;
        if (!divider) {
            z = true;
        }
        setWillNotDraw(z);
    }

    public void setChecked(boolean checked, boolean animated) {
        this.radioButton.setChecked(checked, animated);
    }

    protected void onDraw(Canvas canvas) {
        if (this.needDivider) {
            canvas.drawLine((float) getPaddingLeft(), (float) (getHeight() - 1), (float) (getWidth() - getPaddingRight()), (float) (getHeight() - 1), paint);
        }
    }
}
