package org.telegram.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.os.Build.VERSION;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageReceiver;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Document;

public class StickerPreviewViewer {
    private static volatile StickerPreviewViewer Instance = null;
    private ColorDrawable backgroundDrawable = new ColorDrawable(1895825408);
    private ImageReceiver centerImage = new ImageReceiver();
    private FrameLayoutDrawer containerView;
    private Document currentSticker = null;
    private boolean isVisible = false;
    private int keyboardHeight = AndroidUtilities.dp(200.0f);
    private long lastUpdateTime;
    private Activity parentActivity;
    private float showProgress;
    private LayoutParams windowLayoutParams;
    private FrameLayout windowView;

    class C12211 implements OnTouchListener {
        C12211() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == 1 || event.getAction() == 6 || event.getAction() == 3) {
                StickerPreviewViewer.this.close();
            }
            return true;
        }
    }

    class C12222 implements Runnable {
        C12222() {
        }

        public void run() {
            StickerPreviewViewer.this.centerImage.setImageBitmap((Bitmap) null);
        }
    }

    private class FrameLayoutDrawer extends FrameLayout {
        public FrameLayoutDrawer(Context context) {
            super(context);
            setWillNotDraw(false);
        }

        protected void onDraw(Canvas canvas) {
            StickerPreviewViewer.getInstance().onDraw(canvas);
        }
    }

    public static StickerPreviewViewer getInstance() {
        StickerPreviewViewer localInstance = Instance;
        if (localInstance == null) {
            synchronized (PhotoViewer.class) {
                try {
                    localInstance = Instance;
                    if (localInstance == null) {
                        StickerPreviewViewer localInstance2 = new StickerPreviewViewer();
                        try {
                            Instance = localInstance2;
                            localInstance = localInstance2;
                        } catch (Throwable th) {
                            Throwable th2 = th;
                            localInstance = localInstance2;
                            throw th2;
                        }
                    }
                } catch (Throwable th3) {
                    th2 = th3;
                    throw th2;
                }
            }
        }
        return localInstance;
    }

    public void setParentActivity(Activity activity) {
        if (this.parentActivity != activity) {
            this.parentActivity = activity;
            this.windowView = new FrameLayout(activity);
            this.windowView.setFocusable(true);
            this.windowView.setFocusableInTouchMode(true);
            this.containerView = new FrameLayoutDrawer(activity);
            this.containerView.setFocusable(false);
            this.windowView.addView(this.containerView);
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.containerView.getLayoutParams();
            layoutParams.width = -1;
            layoutParams.height = -1;
            layoutParams.gravity = 51;
            this.containerView.setLayoutParams(layoutParams);
            this.containerView.setOnTouchListener(new C12211());
            this.windowLayoutParams = new LayoutParams();
            this.windowLayoutParams.height = -1;
            this.windowLayoutParams.format = -3;
            this.windowLayoutParams.width = -1;
            this.windowLayoutParams.gravity = 48;
            this.windowLayoutParams.type = 99;
            if (VERSION.SDK_INT >= 21) {
                this.windowLayoutParams.flags = -2147483640;
            } else {
                this.windowLayoutParams.flags = 8;
            }
            this.centerImage.setAspectFit(true);
            this.centerImage.setInvalidateAll(true);
            this.centerImage.setParentView(this.containerView);
        }
    }

    public void setKeyboardHeight(int height) {
        this.keyboardHeight = height;
    }

    public void open(Document sticker) {
        if (this.parentActivity != null && sticker != null) {
            this.centerImage.setImage((TLObject) sticker, null, sticker.thumb.location, null, "webp", true);
            this.currentSticker = sticker;
            this.containerView.invalidate();
            if (!this.isVisible) {
                AndroidUtilities.lockOrientation(this.parentActivity);
                try {
                    if (this.windowView.getParent() != null) {
                        ((WindowManager) this.parentActivity.getSystemService("window")).removeView(this.windowView);
                    }
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
                ((WindowManager) this.parentActivity.getSystemService("window")).addView(this.windowView, this.windowLayoutParams);
                this.isVisible = true;
                this.showProgress = 0.0f;
                this.lastUpdateTime = System.currentTimeMillis();
            }
        }
    }

    public boolean isVisible() {
        return this.isVisible;
    }

    public void close() {
        if (this.parentActivity != null) {
            this.showProgress = 1.0f;
            this.currentSticker = null;
            this.isVisible = false;
            AndroidUtilities.unlockOrientation(this.parentActivity);
            AndroidUtilities.runOnUIThread(new C12222());
            try {
                if (this.windowView.getParent() != null) {
                    ((WindowManager) this.parentActivity.getSystemService("window")).removeView(this.windowView);
                }
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
        }
    }

    public void destroy() {
        this.isVisible = false;
        this.currentSticker = null;
        if (this.parentActivity != null && this.windowView != null) {
            try {
                if (this.windowView.getParent() != null) {
                    ((WindowManager) this.parentActivity.getSystemService("window")).removeViewImmediate(this.windowView);
                }
                this.windowView = null;
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
            Instance = null;
        }
    }

    private void onDraw(Canvas canvas) {
        this.backgroundDrawable.setAlpha((int) (BitmapDescriptorFactory.HUE_CYAN * this.showProgress));
        this.backgroundDrawable.setBounds(0, 0, this.containerView.getWidth(), this.containerView.getHeight());
        this.backgroundDrawable.draw(canvas);
        canvas.save();
        int size = (int) (((float) Math.min(this.containerView.getWidth(), this.containerView.getHeight())) / 1.8f);
        canvas.translate((float) (this.containerView.getWidth() / 2), (float) Math.max((size / 2) + AndroidUtilities.statusBarHeight, (this.containerView.getHeight() - this.keyboardHeight) / 2));
        if (this.centerImage.getBitmap() != null) {
            size = (int) (((float) size) * ((this.showProgress * 0.8f) / 0.8f));
            this.centerImage.setAlpha(this.showProgress);
            this.centerImage.setImageCoords((-size) / 2, (-size) / 2, size, size);
            this.centerImage.draw(canvas);
        }
        canvas.restore();
        if (this.showProgress != 1.0f) {
            long newTime = System.currentTimeMillis();
            long dt = newTime - this.lastUpdateTime;
            this.lastUpdateTime = newTime;
            this.showProgress += ((float) dt) / 150.0f;
            this.containerView.invalidate();
            if (this.showProgress > 1.0f) {
                this.showProgress = 1.0f;
            }
        }
    }
}
