package org.telegram.ui.Components;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import org.telegram.messenger.AndroidUtilities;

public class RecordStatusDrawable extends Drawable {
    private boolean isChat = false;
    private long lastUpdateTime = 0;
    private Paint paint = new Paint(1);
    private float progress;
    private RectF rect = new RectF();
    private boolean started = false;

    public RecordStatusDrawable() {
        this.paint.setColor(-2627337);
        this.paint.setStyle(Style.STROKE);
        this.paint.setStrokeWidth((float) AndroidUtilities.dp(2.0f));
        this.paint.setStrokeCap(Cap.ROUND);
    }

    public void setIsChat(boolean value) {
        this.isChat = value;
    }

    private void update() {
        long newTime = System.currentTimeMillis();
        long dt = newTime - this.lastUpdateTime;
        this.lastUpdateTime = newTime;
        if (dt > 50) {
            dt = 50;
        }
        this.progress += ((float) dt) / BitmapDescriptorFactory.HUE_MAGENTA;
        while (this.progress > 1.0f) {
            this.progress -= 1.0f;
        }
        invalidateSelf();
    }

    public void start() {
        this.lastUpdateTime = System.currentTimeMillis();
        this.started = true;
        invalidateSelf();
    }

    public void stop() {
        this.started = false;
    }

    public void draw(Canvas canvas) {
        canvas.save();
        canvas.translate(0.0f, (float) (AndroidUtilities.dp(this.isChat ? 1.0f : 2.0f) + (getIntrinsicHeight() / 2)));
        for (int a = 0; a < 4; a++) {
            if (a == 0) {
                this.paint.setAlpha((int) (this.progress * 255.0f));
            } else if (a == 3) {
                this.paint.setAlpha((int) ((1.0f - this.progress) * 255.0f));
            } else {
                this.paint.setAlpha(255);
            }
            float side = ((float) (AndroidUtilities.dp(4.0f) * a)) + (((float) AndroidUtilities.dp(4.0f)) * this.progress);
            this.rect.set(-side, -side, side, side);
            canvas.drawArc(this.rect, -15.0f, BitmapDescriptorFactory.HUE_ORANGE, false, this.paint);
        }
        canvas.restore();
        if (this.started) {
            update();
        }
    }

    public void setAlpha(int alpha) {
    }

    public void setColorFilter(ColorFilter cf) {
    }

    public int getOpacity() {
        return 0;
    }

    public int getIntrinsicWidth() {
        return AndroidUtilities.dp(18.0f);
    }

    public int getIntrinsicHeight() {
        return AndroidUtilities.dp(14.0f);
    }
}
