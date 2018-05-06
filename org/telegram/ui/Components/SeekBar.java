package org.telegram.ui.Components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import org.telegram.messenger.AndroidUtilities;

public class SeekBar {
    private static Paint innerPaint1;
    private static Paint innerPaint2;
    private static Paint outerPaint1;
    private static Paint outerPaint2;
    private static int thumbHeight;
    private static int thumbWidth;
    public SeekBarDelegate delegate;
    public int height;
    private boolean pressed = false;
    public int thumbDX = 0;
    public int thumbX = 0;
    public int type;
    public int width;

    public interface SeekBarDelegate {
        void onSeekBarDrag(float f);
    }

    public SeekBar(Context context) {
        if (innerPaint1 == null) {
            innerPaint1 = new Paint(1);
            innerPaint1.setColor(-3939413);
            outerPaint1 = new Paint(1);
            outerPaint1.setColor(-7880840);
            innerPaint2 = new Paint(1);
            innerPaint2.setColor(-1774864);
            outerPaint2 = new Paint(1);
            outerPaint2.setColor(-12479003);
            thumbWidth = AndroidUtilities.dp(24.0f);
            thumbHeight = AndroidUtilities.dp(24.0f);
        }
    }

    public boolean onTouch(int action, float x, float y) {
        if (action == 0) {
            int additionWidth = (this.height - thumbWidth) / 2;
            if (((float) (this.thumbX - additionWidth)) <= x && x <= ((float) ((this.thumbX + thumbWidth) + additionWidth)) && y >= 0.0f && y <= ((float) this.height)) {
                this.pressed = true;
                this.thumbDX = (int) (x - ((float) this.thumbX));
                return true;
            }
        } else if (action == 1 || action == 3) {
            if (this.pressed) {
                if (action == 1 && this.delegate != null) {
                    this.delegate.onSeekBarDrag(((float) this.thumbX) / ((float) (this.width - thumbWidth)));
                }
                this.pressed = false;
                return true;
            }
        } else if (action == 2 && this.pressed) {
            this.thumbX = (int) (x - ((float) this.thumbDX));
            if (this.thumbX < 0) {
                this.thumbX = 0;
                return true;
            } else if (this.thumbX <= this.width - thumbWidth) {
                return true;
            } else {
                this.thumbX = this.width - thumbWidth;
                return true;
            }
        }
        return false;
    }

    public void setProgress(float progress) {
        this.thumbX = (int) Math.ceil((double) (((float) (this.width - thumbWidth)) * progress));
        if (this.thumbX < 0) {
            this.thumbX = 0;
        } else if (this.thumbX > this.width - thumbWidth) {
            this.thumbX = this.width - thumbWidth;
        }
    }

    public boolean isDragging() {
        return this.pressed;
    }

    public void draw(Canvas canvas) {
        Paint inner = null;
        Paint outer = null;
        if (this.type == 0) {
            inner = innerPaint1;
            outer = outerPaint1;
        } else if (this.type == 1) {
            inner = innerPaint2;
            outer = outerPaint2;
        }
        int y = (this.height - thumbHeight) / 2;
        canvas.drawRect((float) (thumbWidth / 2), (float) ((this.height / 2) - AndroidUtilities.dp(1.0f)), (float) (this.width - (thumbWidth / 2)), (float) ((this.height / 2) + AndroidUtilities.dp(1.0f)), inner);
        canvas.drawRect((float) (thumbWidth / 2), (float) ((this.height / 2) - AndroidUtilities.dp(1.0f)), (float) ((thumbWidth / 2) + this.thumbX), (float) ((this.height / 2) + AndroidUtilities.dp(1.0f)), outer);
        canvas.drawCircle((float) (this.thumbX + (thumbWidth / 2)), (float) ((thumbHeight / 2) + y), (float) AndroidUtilities.dp(this.pressed ? 8.0f : 6.0f), outer);
    }
}
