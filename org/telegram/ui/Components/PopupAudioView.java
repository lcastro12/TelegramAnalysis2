package org.telegram.ui.Components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.MotionEvent;
import android.view.View.MeasureSpec;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import java.lang.reflect.Array;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.C0553R;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MediaController.FileDownloadProgressListener;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.ui.Cells.BaseCell;
import org.telegram.ui.Components.SeekBar.SeekBarDelegate;

public class PopupAudioView extends BaseCell implements SeekBarDelegate, FileDownloadProgressListener {
    private static Drawable backgroundMediaDrawableIn;
    private static Drawable[][] statesDrawable = ((Drawable[][]) Array.newInstance(Drawable.class, new int[]{8, 2}));
    private static TextPaint timePaint;
    private int TAG;
    private int buttonPressed = 0;
    private int buttonState = 0;
    private int buttonX;
    private int buttonY;
    protected MessageObject currentMessageObject;
    private String lastTimeString = null;
    private ProgressView progressView;
    private SeekBar seekBar;
    private int seekBarX;
    private int seekBarY;
    private StaticLayout timeLayout;
    int timeWidth = 0;
    private int timeX;
    private boolean wasLayout = false;

    public PopupAudioView(Context context) {
        super(context);
        if (backgroundMediaDrawableIn == null) {
            backgroundMediaDrawableIn = getResources().getDrawable(C0553R.drawable.msg_in_photo);
            statesDrawable[0][0] = getResources().getDrawable(C0553R.drawable.play_w2);
            statesDrawable[0][1] = getResources().getDrawable(C0553R.drawable.play_w2_pressed);
            statesDrawable[1][0] = getResources().getDrawable(C0553R.drawable.pause_w2);
            statesDrawable[1][1] = getResources().getDrawable(C0553R.drawable.pause_w2_pressed);
            statesDrawable[2][0] = getResources().getDrawable(C0553R.drawable.download_g);
            statesDrawable[2][1] = getResources().getDrawable(C0553R.drawable.download_g_pressed);
            statesDrawable[3][0] = getResources().getDrawable(C0553R.drawable.pause_g);
            statesDrawable[3][1] = getResources().getDrawable(C0553R.drawable.pause_g_pressed);
            statesDrawable[4][0] = getResources().getDrawable(C0553R.drawable.play_w);
            statesDrawable[4][1] = getResources().getDrawable(C0553R.drawable.play_w_pressed);
            statesDrawable[5][0] = getResources().getDrawable(C0553R.drawable.pause_w);
            statesDrawable[5][1] = getResources().getDrawable(C0553R.drawable.pause_w_pressed);
            statesDrawable[6][0] = getResources().getDrawable(C0553R.drawable.download_b);
            statesDrawable[6][1] = getResources().getDrawable(C0553R.drawable.download_b_pressed);
            statesDrawable[7][0] = getResources().getDrawable(C0553R.drawable.pause_b);
            statesDrawable[7][1] = getResources().getDrawable(C0553R.drawable.pause_b_pressed);
            timePaint = new TextPaint(1);
            timePaint.setTextSize((float) AndroidUtilities.dp(16.0f));
        }
        this.TAG = MediaController.getInstance().generateObserverTag();
        this.seekBar = new SeekBar(getContext());
        this.seekBar.delegate = this;
        this.progressView = new ProgressView();
    }

    public void setMessageObject(MessageObject messageObject) {
        if (this.currentMessageObject != messageObject) {
            this.seekBar.type = 1;
            this.progressView.setProgressColors(-2497813, -7944712);
            this.currentMessageObject = messageObject;
            this.wasLayout = false;
            requestLayout();
        }
        updateButtonState();
    }

    public final MessageObject getMessageObject() {
        return this.currentMessageObject;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), AndroidUtilities.dp(56.0f));
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (this.currentMessageObject == null) {
            super.onLayout(changed, left, top, right, bottom);
            return;
        }
        this.seekBarX = AndroidUtilities.dp(54.0f);
        this.buttonX = AndroidUtilities.dp(10.0f);
        this.timeX = (getMeasuredWidth() - this.timeWidth) - AndroidUtilities.dp(16.0f);
        this.seekBar.width = (getMeasuredWidth() - AndroidUtilities.dp(70.0f)) - this.timeWidth;
        this.seekBar.height = AndroidUtilities.dp(BitmapDescriptorFactory.HUE_ORANGE);
        this.progressView.width = (getMeasuredWidth() - AndroidUtilities.dp(94.0f)) - this.timeWidth;
        this.progressView.height = AndroidUtilities.dp(BitmapDescriptorFactory.HUE_ORANGE);
        this.seekBarY = AndroidUtilities.dp(13.0f);
        this.buttonY = AndroidUtilities.dp(10.0f);
        updateProgress();
        if (changed || !this.wasLayout) {
            this.wasLayout = true;
        }
    }

    protected void onDraw(Canvas canvas) {
        if (this.currentMessageObject != null) {
            if (this.wasLayout) {
                setDrawableBounds(backgroundMediaDrawableIn, 0, 0, getMeasuredWidth(), getMeasuredHeight());
                backgroundMediaDrawableIn.draw(canvas);
                if (this.currentMessageObject != null) {
                    canvas.save();
                    if (this.buttonState == 0 || this.buttonState == 1) {
                        canvas.translate((float) this.seekBarX, (float) this.seekBarY);
                        this.seekBar.draw(canvas);
                    } else {
                        canvas.translate((float) (this.seekBarX + AndroidUtilities.dp(12.0f)), (float) this.seekBarY);
                        this.progressView.draw(canvas);
                    }
                    canvas.restore();
                    int state = this.buttonState + 4;
                    timePaint.setColor(-6182221);
                    Drawable buttonDrawable = statesDrawable[state][this.buttonPressed];
                    int side = AndroidUtilities.dp(36.0f);
                    setDrawableBounds(buttonDrawable, this.buttonX + ((side - buttonDrawable.getIntrinsicWidth()) / 2), this.buttonY + ((side - buttonDrawable.getIntrinsicHeight()) / 2));
                    buttonDrawable.draw(canvas);
                    canvas.save();
                    canvas.translate((float) this.timeX, (float) AndroidUtilities.dp(18.0f));
                    this.timeLayout.draw(canvas);
                    canvas.restore();
                    return;
                }
                return;
            }
            requestLayout();
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        MediaController.getInstance().removeLoadingFileObserver(this);
    }

    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        boolean result = this.seekBar.onTouch(event.getAction(), event.getX() - ((float) this.seekBarX), event.getY() - ((float) this.seekBarY));
        if (result) {
            if (event.getAction() == 0) {
                getParent().requestDisallowInterceptTouchEvent(true);
            }
            invalidate();
            return result;
        }
        int side = AndroidUtilities.dp(36.0f);
        if (event.getAction() == 0) {
            if (x >= ((float) this.buttonX) && x <= ((float) (this.buttonX + side)) && y >= ((float) this.buttonY) && y <= ((float) (this.buttonY + side))) {
                this.buttonPressed = 1;
                invalidate();
                result = true;
            }
        } else if (this.buttonPressed == 1) {
            if (event.getAction() == 1) {
                this.buttonPressed = 0;
                playSoundEffect(0);
                didPressedButton();
                invalidate();
            } else if (event.getAction() == 3) {
                this.buttonPressed = 0;
                invalidate();
            } else if (event.getAction() == 2 && (x < ((float) this.buttonX) || x > ((float) (this.buttonX + side)) || y < ((float) this.buttonY) || y > ((float) (this.buttonY + side)))) {
                this.buttonPressed = 0;
                invalidate();
            }
        }
        if (result) {
            return result;
        }
        return super.onTouchEvent(event);
    }

    private void didPressedButton() {
        if (this.buttonState == 0) {
            boolean result = MediaController.getInstance().playAudio(this.currentMessageObject);
            if (!this.currentMessageObject.isOut() && this.currentMessageObject.isContentUnread() && this.currentMessageObject.messageOwner.to_id.channel_id == 0) {
                MessagesController.getInstance().markMessageContentAsRead(this.currentMessageObject.messageOwner);
                this.currentMessageObject.setContentIsRead();
            }
            if (result) {
                this.buttonState = 1;
                invalidate();
            }
        } else if (this.buttonState == 1) {
            if (MediaController.getInstance().pauseAudio(this.currentMessageObject)) {
                this.buttonState = 0;
                invalidate();
            }
        } else if (this.buttonState == 2) {
            FileLoader.getInstance().loadFile(this.currentMessageObject.messageOwner.media.audio, true);
            this.buttonState = 3;
            invalidate();
        } else if (this.buttonState == 3) {
            FileLoader.getInstance().cancelLoadFile(this.currentMessageObject.messageOwner.media.audio);
            this.buttonState = 2;
            invalidate();
        }
    }

    public void updateProgress() {
        if (this.currentMessageObject != null) {
            int duration;
            if (!this.seekBar.isDragging()) {
                this.seekBar.setProgress(this.currentMessageObject.audioProgress);
            }
            if (MediaController.getInstance().isPlayingAudio(this.currentMessageObject)) {
                duration = this.currentMessageObject.audioProgressSec;
            } else {
                duration = this.currentMessageObject.messageOwner.media.audio.duration;
            }
            String timeString = String.format("%02d:%02d", new Object[]{Integer.valueOf(duration / 60), Integer.valueOf(duration % 60)});
            if (this.lastTimeString == null || !(this.lastTimeString == null || this.lastTimeString.equals(timeString))) {
                this.timeWidth = (int) Math.ceil((double) timePaint.measureText(timeString));
                this.timeLayout = new StaticLayout(timeString, timePaint, this.timeWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            }
            invalidate();
        }
    }

    public void downloadAudioIfNeed() {
        if (this.buttonState == 2) {
            FileLoader.getInstance().loadFile(this.currentMessageObject.messageOwner.media.audio, true);
            this.buttonState = 3;
            invalidate();
        }
    }

    public void updateButtonState() {
        String fileName = this.currentMessageObject.getFileName();
        if (FileLoader.getPathToMessage(this.currentMessageObject.messageOwner).exists()) {
            MediaController.getInstance().removeLoadingFileObserver(this);
            boolean playing = MediaController.getInstance().isPlayingAudio(this.currentMessageObject);
            if (!playing || (playing && MediaController.getInstance().isAudioPaused())) {
                this.buttonState = 0;
            } else {
                this.buttonState = 1;
            }
            this.progressView.setProgress(0.0f);
        } else {
            MediaController.getInstance().addLoadingFileObserver(fileName, this);
            if (FileLoader.getInstance().isLoadingFile(fileName)) {
                this.buttonState = 3;
                Float progress = ImageLoader.getInstance().getFileProgress(fileName);
                if (progress != null) {
                    this.progressView.setProgress(progress.floatValue());
                } else {
                    this.progressView.setProgress(0.0f);
                }
            } else {
                this.buttonState = 2;
                this.progressView.setProgress(0.0f);
            }
        }
        updateProgress();
    }

    public void onFailedDownload(String fileName) {
        updateButtonState();
    }

    public void onSuccessDownload(String fileName) {
        updateButtonState();
    }

    public void onProgressDownload(String fileName, float progress) {
        this.progressView.setProgress(progress);
        if (this.buttonState != 3) {
            updateButtonState();
        }
        invalidate();
    }

    public void onProgressUpload(String fileName, float progress, boolean isEncrypted) {
    }

    public int getObserverTag() {
        return this.TAG;
    }

    public void onSeekBarDrag(float progress) {
        if (this.currentMessageObject != null) {
            this.currentMessageObject.audioProgress = progress;
            MediaController.getInstance().seekToProgress(this.currentMessageObject, progress);
        }
    }
}
