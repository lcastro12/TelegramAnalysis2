package org.telegram.ui.Components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.C0553R;

public class VideoSeekBarView extends View {
    private static Paint innerPaint1 = new Paint();
    private static Drawable thumbDrawable1;
    private static int thumbHeight;
    private static int thumbWidth;
    public SeekBarDelegate delegate;
    private boolean pressed = false;
    private float progress = 0.0f;
    private int thumbDX = 0;

    public interface SeekBarDelegate {
        void onSeekBarDrag(float f);
    }

    private void init(Context context) {
        if (thumbDrawable1 == null) {
            thumbDrawable1 = context.getResources().getDrawable(C0553R.drawable.videolapse);
            innerPaint1.setColor(-1717986919);
            thumbWidth = thumbDrawable1.getIntrinsicWidth();
            thumbHeight = thumbDrawable1.getIntrinsicHeight();
        }
    }

    public VideoSeekBarView(Context context) {
        super(context);
        init(context);
    }

    public VideoSeekBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VideoSeekBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event == null) {
            return false;
        }
        float x = event.getX();
        float y = event.getY();
        float thumbX = (float) ((int) (((float) (getMeasuredWidth() - thumbWidth)) * this.progress));
        if (event.getAction() == 0) {
            int additionWidth = (getMeasuredHeight() - thumbWidth) / 2;
            if (thumbX - ((float) additionWidth) > x || x > (((float) thumbWidth) + thumbX) + ((float) additionWidth) || y < 0.0f || y > ((float) getMeasuredHeight())) {
                return false;
            }
            this.pressed = true;
            this.thumbDX = (int) (x - thumbX);
            getParent().requestDisallowInterceptTouchEvent(true);
            invalidate();
            return true;
        } else if (event.getAction() == 1 || event.getAction() == 3) {
            if (!this.pressed) {
                return false;
            }
            if (event.getAction() == 1 && this.delegate != null) {
                this.delegate.onSeekBarDrag(thumbX / ((float) (getMeasuredWidth() - thumbWidth)));
            }
            this.pressed = false;
            invalidate();
            return true;
        } else if (event.getAction() != 2 || !this.pressed) {
            return false;
        } else {
            thumbX = (float) ((int) (x - ((float) this.thumbDX)));
            if (thumbX < 0.0f) {
                thumbX = 0.0f;
            } else if (thumbX > ((float) (getMeasuredWidth() - thumbWidth))) {
                thumbX = (float) (getMeasuredWidth() - thumbWidth);
            }
            this.progress = thumbX / ((float) (getMeasuredWidth() - thumbWidth));
            invalidate();
            return true;
        }
    }

    public void setProgress(float progress) {
        if (progress < 0.0f) {
            progress = 0.0f;
        } else if (progress > 1.0f) {
            progress = 1.0f;
        }
        this.progress = progress;
        invalidate();
    }

    public float getProgress() {
        return this.progress;
    }

    protected void onDraw(Canvas canvas) {
        int y = (getMeasuredHeight() - thumbHeight) / 2;
        int thumbX = (int) (((float) (getMeasuredWidth() - thumbWidth)) * this.progress);
        canvas.drawRect((float) (thumbWidth / 2), (float) ((getMeasuredHeight() / 2) - AndroidUtilities.dp(1.0f)), (float) (getMeasuredWidth() - (thumbWidth / 2)), (float) ((getMeasuredHeight() / 2) + AndroidUtilities.dp(1.0f)), innerPaint1);
        thumbDrawable1.setBounds(thumbX, y, thumbWidth + thumbX, thumbHeight + y);
        thumbDrawable1.draw(canvas);
    }
}
