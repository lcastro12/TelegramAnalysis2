package org.telegram.ui.Components;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.C0553R;
import org.telegram.messenger.LocaleController;

public class EmptyTextProgressView extends FrameLayout {
    private boolean inLayout;
    private ProgressBar progressBar;
    private boolean showAtCenter;
    private TextView textView;

    class C09161 implements OnTouchListener {
        C09161() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            return true;
        }
    }

    public EmptyTextProgressView(Context context) {
        super(context);
        this.progressBar = new ProgressBar(context);
        this.progressBar.setVisibility(4);
        addView(this.progressBar, LayoutHelper.createFrame(-2, -2.0f));
        this.textView = new TextView(context);
        this.textView.setTextSize(1, 20.0f);
        this.textView.setTextColor(-8355712);
        this.textView.setGravity(17);
        this.textView.setVisibility(4);
        this.textView.setPadding(AndroidUtilities.dp(20.0f), 0, AndroidUtilities.dp(20.0f), 0);
        this.textView.setText(LocaleController.getString("NoResult", C0553R.string.NoResult));
        addView(this.textView, LayoutHelper.createFrame(-2, -2.0f));
        setOnTouchListener(new C09161());
    }

    public void showProgress() {
        this.textView.setVisibility(4);
        this.progressBar.setVisibility(0);
    }

    public void showTextView() {
        this.textView.setVisibility(0);
        this.progressBar.setVisibility(4);
    }

    public void setText(String text) {
        this.textView.setText(text);
    }

    public void setTextSize(int size) {
        this.textView.setTextSize(1, (float) size);
    }

    public void setShowAtCenter(boolean value) {
        this.showAtCenter = value;
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        this.inLayout = true;
        int width = r - l;
        int height = b - t;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != 8) {
                int y;
                int x = (width - child.getMeasuredWidth()) / 2;
                if (this.showAtCenter) {
                    y = ((height / 2) - child.getMeasuredHeight()) / 2;
                } else {
                    y = (height - child.getMeasuredHeight()) / 2;
                }
                child.layout(x, y, child.getMeasuredWidth() + x, child.getMeasuredHeight() + y);
            }
        }
        this.inLayout = false;
    }

    public void requestLayout() {
        if (!this.inLayout) {
            super.requestLayout();
        }
    }
}
