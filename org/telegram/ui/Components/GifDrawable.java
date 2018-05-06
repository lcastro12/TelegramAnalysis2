package org.telegram.ui.Components;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.MediaController.MediaPlayerControl;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Locale;

public class GifDrawable extends Drawable implements Animatable, MediaPlayerControl {
    private static final Handler UI_HANDLER = new Handler(Looper.getMainLooper());
    private boolean mApplyTransformation;
    protected final int[] mColors;
    private final Rect mDstRect;
    private volatile int mGifInfoPtr;
    private final long mInputSourceLength;
    private final Runnable mInvalidateTask;
    private volatile boolean mIsRunning;
    private final int[] mMetaData;
    protected final Paint mPaint;
    private final Runnable mResetTask;
    private final Runnable mSaveRemainderTask;
    private final Runnable mStartTask;
    private float mSx;
    private float mSy;
    public WeakReference<View> parentView;

    class C09171 implements Runnable {
        C09171() {
        }

        public void run() {
            GifDrawable.reset(GifDrawable.this.mGifInfoPtr);
        }
    }

    class C09182 implements Runnable {
        C09182() {
        }

        public void run() {
            GifDrawable.restoreRemainder(GifDrawable.this.mGifInfoPtr);
            if (!(GifDrawable.this.parentView == null || GifDrawable.this.parentView.get() == null)) {
                ((View) GifDrawable.this.parentView.get()).invalidate();
            }
            GifDrawable.this.mMetaData[4] = 0;
        }
    }

    class C09193 implements Runnable {
        C09193() {
        }

        public void run() {
            GifDrawable.saveRemainder(GifDrawable.this.mGifInfoPtr);
        }
    }

    class C09204 implements Runnable {
        C09204() {
        }

        public void run() {
            if (GifDrawable.this.parentView != null && GifDrawable.this.parentView.get() != null) {
                ((View) GifDrawable.this.parentView.get()).invalidate();
            }
        }
    }

    private static native void free(int i);

    private static native long getAllocationByteCount(int i);

    private static native String getComment(int i);

    private static native int getCurrentPosition(int i);

    private static native int getDuration(int i);

    private static native int getLoopCount(int i);

    private static native int openFile(int[] iArr, String str);

    private static native void renderFrame(int[] iArr, int i, int[] iArr2);

    private static native void reset(int i);

    private static native int restoreRemainder(int i);

    private static native int saveRemainder(int i);

    private static native int seekToFrame(int i, int i2, int[] iArr);

    private static native int seekToTime(int i, int i2, int[] iArr);

    private static native void setSpeedFactor(int i, float f);

    private static void runOnUiThread(Runnable task) {
        if (Looper.myLooper() == UI_HANDLER.getLooper()) {
            task.run();
        } else {
            UI_HANDLER.post(task);
        }
    }

    public GifDrawable(String filePath) throws Exception {
        this.mIsRunning = true;
        this.mMetaData = new int[5];
        this.mSx = 1.0f;
        this.mSy = 1.0f;
        this.mDstRect = new Rect();
        this.parentView = null;
        this.mPaint = new Paint(6);
        this.mResetTask = new C09171();
        this.mStartTask = new C09182();
        this.mSaveRemainderTask = new C09193();
        this.mInvalidateTask = new C09204();
        this.mInputSourceLength = new File(filePath).length();
        this.mGifInfoPtr = openFile(this.mMetaData, filePath);
        this.mColors = new int[(this.mMetaData[0] * this.mMetaData[1])];
    }

    public GifDrawable(File file) throws Exception {
        this.mIsRunning = true;
        this.mMetaData = new int[5];
        this.mSx = 1.0f;
        this.mSy = 1.0f;
        this.mDstRect = new Rect();
        this.parentView = null;
        this.mPaint = new Paint(6);
        this.mResetTask = new C09171();
        this.mStartTask = new C09182();
        this.mSaveRemainderTask = new C09193();
        this.mInvalidateTask = new C09204();
        this.mInputSourceLength = file.length();
        this.mGifInfoPtr = openFile(this.mMetaData, file.getPath());
        this.mColors = new int[(this.mMetaData[0] * this.mMetaData[1])];
    }

    public void recycle() {
        this.mIsRunning = false;
        int tmpPtr = this.mGifInfoPtr;
        this.mGifInfoPtr = 0;
        free(tmpPtr);
    }

    protected void finalize() throws Throwable {
        try {
            recycle();
        } finally {
            super.finalize();
        }
    }

    public int getIntrinsicHeight() {
        return this.mMetaData[1];
    }

    public int getIntrinsicWidth() {
        return this.mMetaData[0];
    }

    public void setAlpha(int alpha) {
        this.mPaint.setAlpha(alpha);
    }

    public void setColorFilter(ColorFilter cf) {
        this.mPaint.setColorFilter(cf);
    }

    public int getOpacity() {
        return -2;
    }

    public void start() {
        if (!this.mIsRunning) {
            this.mIsRunning = true;
            runOnUiThread(this.mStartTask);
        }
    }

    public void reset() {
        runOnUiThread(this.mResetTask);
    }

    public void stop() {
        this.mIsRunning = false;
        runOnUiThread(this.mSaveRemainderTask);
    }

    public boolean isRunning() {
        return this.mIsRunning;
    }

    public String getComment() {
        return getComment(this.mGifInfoPtr);
    }

    public int getLoopCount() {
        return getLoopCount(this.mGifInfoPtr);
    }

    public String toString() {
        return String.format(Locale.US, "Size: %dx%d, %d frames, error: %d", new Object[]{Integer.valueOf(this.mMetaData[0]), Integer.valueOf(this.mMetaData[1]), Integer.valueOf(this.mMetaData[2]), Integer.valueOf(this.mMetaData[3])});
    }

    public int getNumberOfFrames() {
        return this.mMetaData[2];
    }

    public int getError() {
        return this.mMetaData[3];
    }

    public void setSpeed(float factor) {
        if (factor <= 0.0f) {
            throw new IllegalArgumentException("Speed factor is not positive");
        }
        setSpeedFactor(this.mGifInfoPtr, factor);
    }

    public void pause() {
        stop();
    }

    public int getDuration() {
        return getDuration(this.mGifInfoPtr);
    }

    public int getCurrentPosition() {
        return getCurrentPosition(this.mGifInfoPtr);
    }

    public void seekTo(final int position) {
        if (position < 0) {
            throw new IllegalArgumentException("Position is not positive");
        }
        runOnUiThread(new Runnable() {
            public void run() {
                GifDrawable.seekToTime(GifDrawable.this.mGifInfoPtr, position, GifDrawable.this.mColors);
                if (GifDrawable.this.parentView != null && GifDrawable.this.parentView.get() != null) {
                    ((View) GifDrawable.this.parentView.get()).invalidate();
                }
            }
        });
    }

    public void seekToFrame(final int frameIndex) {
        if (frameIndex < 0) {
            throw new IllegalArgumentException("frameIndex is not positive");
        }
        runOnUiThread(new Runnable() {
            public void run() {
                GifDrawable.seekToFrame(GifDrawable.this.mGifInfoPtr, frameIndex, GifDrawable.this.mColors);
                if (GifDrawable.this.parentView != null && GifDrawable.this.parentView.get() != null) {
                    ((View) GifDrawable.this.parentView.get()).invalidate();
                }
            }
        });
    }

    public boolean isPlaying() {
        return this.mIsRunning;
    }

    public int getBufferPercentage() {
        return 100;
    }

    public boolean canPause() {
        return true;
    }

    public boolean canSeekBackward() {
        return false;
    }

    public boolean canSeekForward() {
        return getNumberOfFrames() > 1;
    }

    public int getAudioSessionId() {
        return 0;
    }

    public int getFrameByteCount() {
        return (this.mMetaData[0] * this.mMetaData[1]) * 4;
    }

    public long getAllocationByteCount() {
        return getAllocationByteCount(this.mGifInfoPtr) + (((long) this.mColors.length) * 4);
    }

    public long getInputSourceByteCount() {
        return this.mInputSourceLength;
    }

    public void getPixels(int[] pixels) {
        if (pixels.length < this.mColors.length) {
            throw new ArrayIndexOutOfBoundsException("Pixels array is too small. Required length: " + this.mColors.length);
        }
        System.arraycopy(this.mColors, 0, pixels, 0, this.mColors.length);
    }

    public int getPixel(int x, int y) {
        if (x < 0) {
            throw new IllegalArgumentException("x must be >= 0");
        } else if (y < 0) {
            throw new IllegalArgumentException("y must be >= 0");
        } else if (x >= this.mMetaData[0]) {
            throw new IllegalArgumentException("x must be < GIF width");
        } else if (y < this.mMetaData[1]) {
            return this.mColors[(this.mMetaData[1] * y) + x];
        } else {
            throw new IllegalArgumentException("y must be < GIF height");
        }
    }

    public Bitmap getBitmap() {
        seekToFrame(this.mGifInfoPtr, 0, this.mColors);
        return Bitmap.createBitmap(this.mColors, 0, this.mMetaData[0], this.mMetaData[0], this.mMetaData[1], Config.ARGB_8888);
    }

    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        this.mApplyTransformation = true;
    }

    public void draw(Canvas canvas) {
        if (this.mApplyTransformation) {
            this.mDstRect.set(getBounds());
            this.mSx = ((float) this.mDstRect.width()) / ((float) this.mMetaData[0]);
            this.mSy = ((float) this.mDstRect.height()) / ((float) this.mMetaData[1]);
            this.mApplyTransformation = false;
        }
        if (this.mPaint.getShader() == null) {
            if (this.mIsRunning) {
                renderFrame(this.mColors, this.mGifInfoPtr, this.mMetaData);
            } else {
                this.mMetaData[4] = -1;
            }
            canvas.translate((float) this.mDstRect.left, (float) this.mDstRect.top);
            canvas.scale(this.mSx, this.mSy);
            if (this.mMetaData[0] > 0 && this.mMetaData[1] > 0) {
                canvas.drawBitmap(this.mColors, 0, this.mMetaData[0], 0.0f, 0.0f, this.mMetaData[0], this.mMetaData[1], true, this.mPaint);
            }
            if (this.mMetaData[4] >= 0 && this.mMetaData[2] > 1) {
                UI_HANDLER.postDelayed(this.mInvalidateTask, (long) this.mMetaData[4]);
                return;
            }
            return;
        }
        canvas.drawRect(this.mDstRect, this.mPaint);
    }

    public final Paint getPaint() {
        return this.mPaint;
    }

    public int getAlpha() {
        return this.mPaint.getAlpha();
    }

    public void setFilterBitmap(boolean filter) {
        this.mPaint.setFilterBitmap(filter);
        if (this.parentView != null && this.parentView.get() != null) {
            ((View) this.parentView.get()).invalidate();
        }
    }

    public void setDither(boolean dither) {
        this.mPaint.setDither(dither);
        if (this.parentView != null && this.parentView.get() != null) {
            ((View) this.parentView.get()).invalidate();
        }
    }

    public int getMinimumHeight() {
        return this.mMetaData[1];
    }

    public int getMinimumWidth() {
        return this.mMetaData[0];
    }
}
