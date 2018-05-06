package org.telegram.ui.Components;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.view.InputDeviceCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import java.util.ArrayList;
import org.telegram.messenger.FileLog;

public class FrameLayoutFixed extends FrameLayout {
    private final ArrayList<View> mMatchParentChildren = new ArrayList(1);

    public FrameLayoutFixed(Context context) {
        super(context);
    }

    public FrameLayoutFixed(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FrameLayoutFixed(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public final int getMeasuredStateFixed(View view) {
        return (view.getMeasuredWidth() & ViewCompat.MEASURED_STATE_MASK) | ((view.getMeasuredHeight() >> 16) & InputDeviceCompat.SOURCE_ANY);
    }

    public static int resolveSizeAndStateFixed(int size, int measureSpec, int childMeasuredState) {
        int result = size;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case Integer.MIN_VALUE:
                if (specSize >= size) {
                    result = size;
                    break;
                }
                result = specSize | ViewCompat.MEASURED_STATE_TOO_SMALL;
                break;
            case 0:
                result = size;
                break;
            case 1073741824:
                result = specSize;
                break;
        }
        return (ViewCompat.MEASURED_STATE_MASK & childMeasuredState) | result;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        try {
            int i;
            View child;
            int count = getChildCount();
            boolean measureMatchParentChildren = (MeasureSpec.getMode(widthMeasureSpec) == 1073741824 && MeasureSpec.getMode(heightMeasureSpec) == 1073741824) ? false : true;
            this.mMatchParentChildren.clear();
            int maxHeight = 0;
            int maxWidth = 0;
            int childState = 0;
            for (i = 0; i < count; i++) {
                child = getChildAt(i);
                if (child.getVisibility() != 8) {
                    measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                    LayoutParams lp = (LayoutParams) child.getLayoutParams();
                    maxWidth = Math.max(maxWidth, (child.getMeasuredWidth() + lp.leftMargin) + lp.rightMargin);
                    maxHeight = Math.max(maxHeight, (child.getMeasuredHeight() + lp.topMargin) + lp.bottomMargin);
                    childState |= getMeasuredStateFixed(child);
                    if (measureMatchParentChildren && (lp.width == -1 || lp.height == -1)) {
                        this.mMatchParentChildren.add(child);
                    }
                }
            }
            maxWidth += getPaddingLeft() + getPaddingRight();
            maxHeight = Math.max(maxHeight + (getPaddingTop() + getPaddingBottom()), getSuggestedMinimumHeight());
            maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());
            Drawable drawable = getForeground();
            if (drawable != null) {
                maxHeight = Math.max(maxHeight, drawable.getMinimumHeight());
                maxWidth = Math.max(maxWidth, drawable.getMinimumWidth());
            }
            setMeasuredDimension(resolveSizeAndStateFixed(maxWidth, widthMeasureSpec, childState), resolveSizeAndStateFixed(maxHeight, heightMeasureSpec, childState << 16));
            count = this.mMatchParentChildren.size();
            if (count > 1) {
                for (i = 0; i < count; i++) {
                    int childWidthMeasureSpec;
                    int childHeightMeasureSpec;
                    child = (View) this.mMatchParentChildren.get(i);
                    MarginLayoutParams lp2 = (MarginLayoutParams) child.getLayoutParams();
                    if (lp2.width == -1) {
                        childWidthMeasureSpec = MeasureSpec.makeMeasureSpec((((getMeasuredWidth() - getPaddingLeft()) - getPaddingRight()) - lp2.leftMargin) - lp2.rightMargin, 1073741824);
                    } else {
                        childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, ((getPaddingLeft() + getPaddingRight()) + lp2.leftMargin) + lp2.rightMargin, lp2.width);
                    }
                    if (lp2.height == -1) {
                        childHeightMeasureSpec = MeasureSpec.makeMeasureSpec((((getMeasuredHeight() - getPaddingTop()) - getPaddingBottom()) - lp2.topMargin) - lp2.bottomMargin, 1073741824);
                    } else {
                        childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, ((getPaddingTop() + getPaddingBottom()) + lp2.topMargin) + lp2.bottomMargin, lp2.height);
                    }
                    child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
                }
            }
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
            try {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            } catch (Throwable e2) {
                FileLog.m611e("tmessages", e2);
            }
        }
    }
}
