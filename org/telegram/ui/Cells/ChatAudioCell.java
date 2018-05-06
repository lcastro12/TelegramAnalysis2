package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.MotionEvent;
import android.view.View.MeasureSpec;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import java.io.File;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.ui.Components.RadialProgress;
import org.telegram.ui.Components.ResourceLoader;
import org.telegram.ui.Components.SeekBar;
import org.telegram.ui.Components.SeekBar.SeekBarDelegate;

public class ChatAudioCell extends ChatBaseCell implements SeekBarDelegate {
    private static Paint circlePaint;
    private static TextPaint timePaint;
    private boolean buttonPressed = false;
    private int buttonState = 0;
    private int buttonX;
    private int buttonY;
    private String lastTimeString = null;
    private RadialProgress radialProgress;
    private SeekBar seekBar;
    private int seekBarX;
    private int seekBarY;
    private StaticLayout timeLayout;
    private int timeWidth;
    private int timeX;

    public ChatAudioCell(Context context) {
        super(context);
        this.seekBar = new SeekBar(context);
        this.seekBar.delegate = this;
        this.radialProgress = new RadialProgress(this);
        this.drawForwardedName = true;
        if (timePaint == null) {
            timePaint = new TextPaint(1);
            timePaint.setTextSize((float) AndroidUtilities.dp(12.0f));
            circlePaint = new Paint(1);
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        MediaController.getInstance().removeLoadingFileObserver(this);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateButtonState(false);
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
                this.buttonPressed = true;
                invalidate();
                result = true;
            }
        } else if (this.buttonPressed) {
            if (event.getAction() == 1) {
                this.buttonPressed = false;
                playSoundEffect(0);
                didPressedButton();
                invalidate();
            } else if (event.getAction() == 3) {
                this.buttonPressed = false;
                invalidate();
            } else if (event.getAction() == 2 && (x < ((float) this.buttonX) || x > ((float) (this.buttonX + side)) || y < ((float) this.buttonY) || y > ((float) (this.buttonY + side)))) {
                this.buttonPressed = false;
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
            }
            if (result) {
                this.buttonState = 1;
                this.radialProgress.setBackground(getDrawableForCurrentState(), false, false);
                invalidate();
            }
        } else if (this.buttonState == 1) {
            if (MediaController.getInstance().pauseAudio(this.currentMessageObject)) {
                this.buttonState = 0;
                this.radialProgress.setBackground(getDrawableForCurrentState(), false, false);
                invalidate();
            }
        } else if (this.buttonState == 2) {
            this.radialProgress.setProgress(0.0f, false);
            FileLoader.getInstance().loadFile(this.currentMessageObject.messageOwner.media.audio, true);
            this.buttonState = 3;
            this.radialProgress.setBackground(getDrawableForCurrentState(), true, false);
            invalidate();
        } else if (this.buttonState == 3) {
            FileLoader.getInstance().cancelLoadFile(this.currentMessageObject.messageOwner.media.audio);
            this.buttonState = 2;
            this.radialProgress.setBackground(getDrawableForCurrentState(), false, false);
            invalidate();
        } else if (this.buttonState == 4 && this.currentMessageObject.isOut() && this.currentMessageObject.isSending() && this.delegate != null) {
            this.delegate.didPressedCancelSendButton(this);
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
                this.lastTimeString = timeString;
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
            this.radialProgress.setBackground(getDrawableForCurrentState(), false, false);
        }
    }

    public void updateButtonState(boolean animated) {
        float f = 0.0f;
        if (this.currentMessageObject != null) {
            Float progress;
            if (this.currentMessageObject.isOut() && this.currentMessageObject.isSending()) {
                MediaController.getInstance().addLoadingFileObserver(this.currentMessageObject.messageOwner.attachPath, this);
                this.buttonState = 4;
                this.radialProgress.setBackground(getDrawableForCurrentState(), true, animated);
                progress = ImageLoader.getInstance().getFileProgress(this.currentMessageObject.messageOwner.attachPath);
                if (progress == null && SendMessagesHelper.getInstance().isSendingMessage(this.currentMessageObject.getId())) {
                    progress = Float.valueOf(1.0f);
                }
                RadialProgress radialProgress = this.radialProgress;
                if (progress != null) {
                    f = progress.floatValue();
                }
                radialProgress.setProgress(f, false);
            } else {
                File cacheFile = null;
                if (this.currentMessageObject.messageOwner.attachPath != null && this.currentMessageObject.messageOwner.attachPath.length() > 0) {
                    cacheFile = new File(this.currentMessageObject.messageOwner.attachPath);
                    if (!cacheFile.exists()) {
                        cacheFile = null;
                    }
                }
                if (cacheFile == null) {
                    cacheFile = FileLoader.getPathToMessage(this.currentMessageObject.messageOwner);
                }
                if (BuildVars.DEBUG_VERSION) {
                    FileLog.m608d("tmessages", "looking for audio in " + cacheFile);
                }
                if (cacheFile.exists()) {
                    MediaController.getInstance().removeLoadingFileObserver(this);
                    boolean playing = MediaController.getInstance().isPlayingAudio(this.currentMessageObject);
                    if (!playing || (playing && MediaController.getInstance().isAudioPaused())) {
                        this.buttonState = 0;
                    } else {
                        this.buttonState = 1;
                    }
                    this.radialProgress.setProgress(0.0f, animated);
                    this.radialProgress.setBackground(getDrawableForCurrentState(), false, animated);
                } else {
                    String fileName = this.currentMessageObject.getFileName();
                    MediaController.getInstance().addLoadingFileObserver(fileName, this);
                    if (FileLoader.getInstance().isLoadingFile(fileName)) {
                        this.buttonState = 3;
                        progress = ImageLoader.getInstance().getFileProgress(fileName);
                        if (progress != null) {
                            this.radialProgress.setProgress(progress.floatValue(), animated);
                        } else {
                            this.radialProgress.setProgress(0.0f, animated);
                        }
                        this.radialProgress.setBackground(getDrawableForCurrentState(), true, animated);
                    } else {
                        this.buttonState = 2;
                        this.radialProgress.setProgress(0.0f, animated);
                        this.radialProgress.setBackground(getDrawableForCurrentState(), false, animated);
                    }
                }
            }
            updateProgress();
        }
    }

    public void onFailedDownload(String fileName) {
        updateButtonState(true);
    }

    public void onSuccessDownload(String fileName) {
        updateButtonState(true);
    }

    public void onProgressDownload(String fileName, float progress) {
        this.radialProgress.setProgress(progress, true);
        if (this.buttonState != 3) {
            updateButtonState(false);
        }
    }

    public void onProgressUpload(String fileName, float progress, boolean isEncrypted) {
        this.radialProgress.setProgress(progress, true);
    }

    public void onSeekBarDrag(float progress) {
        if (this.currentMessageObject != null) {
            this.currentMessageObject.audioProgress = progress;
            MediaController.getInstance().seekToProgress(this.currentMessageObject, progress);
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), AndroidUtilities.dp(66.0f) + this.namesOffset);
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (this.currentMessageObject.isOutOwner()) {
            this.seekBarX = (this.layoutWidth - this.backgroundWidth) + AndroidUtilities.dp(55.0f);
            this.buttonX = (this.layoutWidth - this.backgroundWidth) + AndroidUtilities.dp(13.0f);
            this.timeX = (this.layoutWidth - this.backgroundWidth) + AndroidUtilities.dp(66.0f);
        } else if (!this.isChat || this.currentMessageObject.messageOwner.from_id <= 0) {
            this.seekBarX = AndroidUtilities.dp(64.0f);
            this.buttonX = AndroidUtilities.dp(22.0f);
            this.timeX = AndroidUtilities.dp(75.0f);
        } else {
            this.seekBarX = AndroidUtilities.dp(116.0f);
            this.buttonX = AndroidUtilities.dp(74.0f);
            this.timeX = AndroidUtilities.dp(127.0f);
        }
        this.seekBar.width = this.backgroundWidth - AndroidUtilities.dp(70.0f);
        this.seekBar.height = AndroidUtilities.dp(BitmapDescriptorFactory.HUE_ORANGE);
        this.seekBarY = AndroidUtilities.dp(11.0f) + this.namesOffset;
        this.buttonY = AndroidUtilities.dp(13.0f) + this.namesOffset;
        this.radialProgress.setProgressRect(this.buttonX, this.buttonY, this.buttonX + AndroidUtilities.dp(40.0f), this.buttonY + AndroidUtilities.dp(40.0f));
        updateProgress();
    }

    public void setMessageObject(MessageObject messageObject) {
        boolean dataChanged;
        float f = 102.0f;
        if (this.currentMessageObject == messageObject && isUserDataChanged()) {
            dataChanged = true;
        } else {
            dataChanged = false;
        }
        if (this.currentMessageObject != messageObject || dataChanged) {
            int minTabletSide;
            if (AndroidUtilities.isTablet()) {
                minTabletSide = AndroidUtilities.getMinTabletSide();
                if (!this.isChat || messageObject.messageOwner.from_id <= 0) {
                    f = 50.0f;
                }
                this.backgroundWidth = Math.min(minTabletSide - AndroidUtilities.dp(f), AndroidUtilities.dp(BitmapDescriptorFactory.HUE_MAGENTA));
            } else {
                minTabletSide = AndroidUtilities.displaySize.x;
                if (!this.isChat || messageObject.messageOwner.from_id <= 0) {
                    f = 50.0f;
                }
                this.backgroundWidth = Math.min(minTabletSide - AndroidUtilities.dp(f), AndroidUtilities.dp(BitmapDescriptorFactory.HUE_MAGENTA));
            }
            if (messageObject.isOutOwner()) {
                this.seekBar.type = 0;
                this.radialProgress.setProgressColor(-7880840);
            } else {
                this.seekBar.type = 1;
                this.radialProgress.setProgressColor(-6113849);
            }
            super.setMessageObject(messageObject);
        }
        updateButtonState(dataChanged);
    }

    private Drawable getDrawableForCurrentState() {
        return ResourceLoader.audioStatesDrawable[this.currentMessageObject.isOutOwner() ? this.buttonState : this.buttonState + 5][0];
    }

    protected void onDraw(Canvas canvas) {
        if (this.currentMessageObject != null) {
            super.onDraw(canvas);
            canvas.save();
            canvas.translate((float) this.seekBarX, (float) this.seekBarY);
            this.seekBar.draw(canvas);
            canvas.restore();
            if (this.currentMessageObject.isOutOwner()) {
                timePaint.setColor(-9391780);
                circlePaint.setColor(-7880840);
            } else {
                timePaint.setColor(-6182221);
                circlePaint.setColor(-12479003);
            }
            this.radialProgress.draw(canvas);
            canvas.save();
            canvas.translate((float) this.timeX, (float) (AndroidUtilities.dp(42.0f) + this.namesOffset));
            this.timeLayout.draw(canvas);
            canvas.restore();
            if (this.currentMessageObject.messageOwner.to_id.channel_id == 0 && this.currentMessageObject.isContentUnread()) {
                canvas.drawCircle((float) ((this.timeX + this.timeWidth) + AndroidUtilities.dp(8.0f)), (float) (AndroidUtilities.dp(49.5f) + this.namesOffset), (float) AndroidUtilities.dp(3.0f), circlePaint);
            }
        }
    }
}
