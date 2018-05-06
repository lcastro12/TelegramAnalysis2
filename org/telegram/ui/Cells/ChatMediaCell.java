package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Layout.Alignment;
import android.text.Spannable;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.view.View.MeasureSpec;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationStatusCodes;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import java.io.File;
import java.util.Iterator;
import java.util.Locale;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.C0553R;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.DocumentAttribute;
import org.telegram.tgnet.TLRPC.FileLocation;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.TL_documentAttributeImageSize;
import org.telegram.tgnet.TLRPC.TL_fileLocationUnavailable;
import org.telegram.ui.Components.GifDrawable;
import org.telegram.ui.Components.RadialProgress;
import org.telegram.ui.Components.ResourceLoader;
import org.telegram.ui.Components.StaticLayoutEx;
import org.telegram.ui.Components.URLSpanBotCommand;
import org.telegram.ui.PhotoViewer;

public class ChatMediaCell extends ChatBaseCell {
    private static Paint deleteProgressPaint;
    private static Paint docBackPaint;
    private static TextPaint infoPaint;
    private static MessageObject lastDownloadedGifMessage = null;
    private static TextPaint locationAddressPaint;
    private static TextPaint locationTitlePaint;
    private static TextPaint namePaint;
    private int additionHeight;
    private boolean allowedToSetPhoto = true;
    private int buttonPressed = 0;
    private int buttonState = 0;
    private int buttonX;
    private int buttonY;
    private boolean cancelLoading = false;
    private int captionHeight;
    private int captionX;
    private int captionY;
    private String currentInfoString;
    private String currentNameString;
    private String currentPhotoFilter;
    private PhotoSize currentPhotoObject;
    private PhotoSize currentPhotoObjectThumb;
    private String currentUrl;
    private RectF deleteProgressRect = new RectF();
    private GifDrawable gifDrawable = null;
    private boolean imagePressed = false;
    private StaticLayout infoLayout;
    private int infoOffset = 0;
    private int infoWidth;
    private ChatMediaCellDelegate mediaDelegate = null;
    private StaticLayout nameLayout;
    private int nameOffsetX = 0;
    private int nameWidth = 0;
    private boolean otherPressed = false;
    private int photoHeight;
    private ImageReceiver photoImage;
    private boolean photoNotSet = false;
    private int photoWidth;
    private RadialProgress radialProgress;

    public interface ChatMediaCellDelegate {
        void didPressedOther(ChatMediaCell chatMediaCell);
    }

    public ChatMediaCell(Context context) {
        super(context);
        if (infoPaint == null) {
            infoPaint = new TextPaint(1);
            infoPaint.setTextSize((float) AndroidUtilities.dp(12.0f));
            namePaint = new TextPaint(1);
            namePaint.setColor(-14606047);
            namePaint.setTextSize((float) AndroidUtilities.dp(16.0f));
            docBackPaint = new Paint();
            deleteProgressPaint = new Paint(1);
            deleteProgressPaint.setColor(-1776928);
            locationTitlePaint = new TextPaint(1);
            locationTitlePaint.setTextSize((float) AndroidUtilities.dp(14.0f));
            locationTitlePaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            locationAddressPaint = new TextPaint(1);
            locationAddressPaint.setTextSize((float) AndroidUtilities.dp(14.0f));
        }
        this.photoImage = new ImageReceiver(this);
        this.radialProgress = new RadialProgress(this);
    }

    public void clearGifImage() {
        if (this.currentMessageObject != null && this.currentMessageObject.type == 8) {
            this.gifDrawable = null;
            this.buttonState = 2;
            this.radialProgress.setBackground(getDrawableForCurrentState(), false, false);
            invalidate();
        }
    }

    public void setMediaDelegate(ChatMediaCellDelegate delegate) {
        this.mediaDelegate = delegate;
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.photoImage.onDetachedFromWindow();
        if (this.gifDrawable != null) {
            MediaController.getInstance().clearGifDrawable(this);
            this.gifDrawable = null;
        }
        MediaController.getInstance().removeLoadingFileObserver(this);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.photoImage.onAttachedToWindow()) {
            updateButtonState(false);
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        boolean result = false;
        int side = AndroidUtilities.dp(48.0f);
        if ((this.currentMessageObject.caption instanceof Spannable) && this.delegate.canPerformActions()) {
            if (event.getAction() == 0 || ((this.linkPreviewPressed || this.pressedLink != null) && event.getAction() == 1)) {
                if (this.nameLayout == null || x < ((float) this.captionX) || x > ((float) (this.captionX + this.backgroundWidth)) || y < ((float) this.captionY) || y > ((float) (this.captionY + this.captionHeight))) {
                    resetPressedLink();
                } else if (event.getAction() == 0) {
                    resetPressedLink();
                    try {
                        int x2 = (int) (x - ((float) this.captionX));
                        int line = this.nameLayout.getLineForVertical((int) (y - ((float) this.captionY)));
                        int off = this.nameLayout.getOffsetForHorizontal(line, (float) x2);
                        float left = this.nameLayout.getLineLeft(line);
                        if (left > ((float) x2) || this.nameLayout.getLineWidth(line) + left < ((float) x2)) {
                            resetPressedLink();
                        } else {
                            Spannable buffer = this.currentMessageObject.caption;
                            ClickableSpan[] link = (ClickableSpan[]) buffer.getSpans(off, off, ClickableSpan.class);
                            boolean ignore = false;
                            if (link.length == 0 || !(link.length == 0 || !(link[0] instanceof URLSpanBotCommand) || URLSpanBotCommand.enabled)) {
                                ignore = true;
                            }
                            if (ignore) {
                                resetPressedLink();
                            } else {
                                resetPressedLink();
                                this.pressedLink = link[0];
                                this.linkPreviewPressed = true;
                                result = true;
                                try {
                                    int start = buffer.getSpanStart(this.pressedLink);
                                    this.urlPath.setCurrentLayout(this.nameLayout, start);
                                    this.nameLayout.getSelectionPath(start, buffer.getSpanEnd(this.pressedLink), this.urlPath);
                                } catch (Throwable e) {
                                    FileLog.m611e("tmessages", e);
                                }
                            }
                        }
                    } catch (Throwable e2) {
                        resetPressedLink();
                        FileLog.m611e("tmessages", e2);
                    }
                } else if (this.linkPreviewPressed) {
                    try {
                        this.delegate.didPressUrl(this.currentMessageObject, this.pressedLink, false);
                    } catch (Throwable e22) {
                        FileLog.m611e("tmessages", e22);
                    }
                    resetPressedLink();
                    result = true;
                }
            } else if (event.getAction() == 3) {
                resetPressedLink();
            }
            if (result && event.getAction() == 0) {
                startCheckLongPress();
            }
            if (!(event.getAction() == 0 || event.getAction() == 2)) {
                cancelCheckLongPress();
            }
            if (result) {
                return true;
            }
        }
        if (event.getAction() != 0) {
            if (event.getAction() != 2) {
                cancelCheckLongPress();
            }
            if (this.buttonPressed == 1) {
                if (event.getAction() == 1) {
                    this.buttonPressed = 0;
                    playSoundEffect(0);
                    didPressedButton(false);
                    invalidate();
                } else if (event.getAction() == 3) {
                    this.buttonPressed = 0;
                    invalidate();
                } else if (event.getAction() == 2 && (x < ((float) this.buttonX) || x > ((float) (this.buttonX + side)) || y < ((float) this.buttonY) || y > ((float) (this.buttonY + side)))) {
                    this.buttonPressed = 0;
                    invalidate();
                }
            } else if (this.imagePressed) {
                if (event.getAction() == 1) {
                    this.imagePressed = false;
                    if (this.buttonState == -1 || this.buttonState == 2 || this.buttonState == 3) {
                        playSoundEffect(0);
                        didClickedImage();
                    }
                    invalidate();
                } else if (event.getAction() == 3) {
                    this.imagePressed = false;
                    invalidate();
                } else if (event.getAction() == 2) {
                    if (this.currentMessageObject.type == 9) {
                        if (x < ((float) this.photoImage.getImageX()) || x > ((float) ((this.photoImage.getImageX() + this.backgroundWidth) - AndroidUtilities.dp(50.0f))) || y < ((float) this.photoImage.getImageY()) || y > ((float) (this.photoImage.getImageY() + this.photoImage.getImageHeight()))) {
                            this.imagePressed = false;
                            invalidate();
                        }
                    } else if (!this.photoImage.isInsideImage(x, y)) {
                        this.imagePressed = false;
                        invalidate();
                    }
                }
            } else if (this.otherPressed) {
                if (event.getAction() == 1) {
                    this.otherPressed = false;
                    playSoundEffect(0);
                    if (this.mediaDelegate != null) {
                        this.mediaDelegate.didPressedOther(this);
                    }
                } else if (event.getAction() == 3) {
                    this.otherPressed = false;
                } else if (event.getAction() == 2 && this.currentMessageObject.type == 9 && (x < ((float) ((this.photoImage.getImageX() + this.backgroundWidth) - AndroidUtilities.dp(50.0f))) || x > ((float) (this.photoImage.getImageX() + this.backgroundWidth)) || y < ((float) this.photoImage.getImageY()) || y > ((float) (this.photoImage.getImageY() + this.photoImage.getImageHeight())))) {
                    this.otherPressed = false;
                }
            }
        } else if (this.delegate == null || this.delegate.canPerformActions()) {
            if (this.buttonState != -1 && x >= ((float) this.buttonX) && x <= ((float) (this.buttonX + side)) && y >= ((float) this.buttonY) && y <= ((float) (this.buttonY + side))) {
                this.buttonPressed = 1;
                invalidate();
                result = true;
            } else if (this.currentMessageObject.type == 9) {
                if (x >= ((float) this.photoImage.getImageX()) && x <= ((float) ((this.photoImage.getImageX() + this.backgroundWidth) - AndroidUtilities.dp(50.0f))) && y >= ((float) this.photoImage.getImageY()) && y <= ((float) (this.photoImage.getImageY() + this.photoImage.getImageHeight()))) {
                    this.imagePressed = true;
                    result = true;
                } else if (x >= ((float) ((this.photoImage.getImageX() + this.backgroundWidth) - AndroidUtilities.dp(50.0f))) && x <= ((float) (this.photoImage.getImageX() + this.backgroundWidth)) && y >= ((float) this.photoImage.getImageY()) && y <= ((float) (this.photoImage.getImageY() + this.photoImage.getImageHeight()))) {
                    this.otherPressed = true;
                    result = true;
                }
            } else if (this.currentMessageObject.type != 13 && x >= ((float) this.photoImage.getImageX()) && x <= ((float) (this.photoImage.getImageX() + this.backgroundWidth)) && y >= ((float) this.photoImage.getImageY()) && y <= ((float) (this.photoImage.getImageY() + this.photoImage.getImageHeight()))) {
                this.imagePressed = true;
                result = true;
            }
            if (this.imagePressed && this.currentMessageObject.isSecretPhoto()) {
                this.imagePressed = false;
            } else if (result) {
                startCheckLongPress();
            }
        }
        if (!result) {
            result = super.onTouchEvent(event);
        }
        return result;
    }

    private void didClickedImage() {
        if (this.currentMessageObject.type == 1) {
            if (this.buttonState == -1) {
                if (this.delegate != null) {
                    this.delegate.didClickedImage(this);
                }
            } else if (this.buttonState == 0) {
                didPressedButton(false);
            }
        } else if (this.currentMessageObject.type == 8) {
            if (this.buttonState == -1) {
                this.buttonState = 2;
                if (this.gifDrawable != null) {
                    this.gifDrawable.pause();
                }
                this.radialProgress.setBackground(getDrawableForCurrentState(), false, false);
                invalidate();
            } else if (this.buttonState == 2 || this.buttonState == 0) {
                didPressedButton(false);
            }
        } else if (this.currentMessageObject.type == 3) {
            if (this.buttonState == 0 || this.buttonState == 3) {
                didPressedButton(false);
            }
        } else if (this.currentMessageObject.type == 4) {
            if (this.delegate != null) {
                this.delegate.didClickedImage(this);
            }
        } else if (this.currentMessageObject.type == 9 && this.buttonState == -1 && this.delegate != null) {
            this.delegate.didClickedImage(this);
        }
    }

    private Drawable getDrawableForCurrentState() {
        int i = 1;
        if (this.buttonState < 0 || this.buttonState >= 4) {
            if (this.buttonState == -1 && this.currentMessageObject.type == 9 && this.gifDrawable == null) {
                return this.currentMessageObject.isOutOwner() ? ResourceLoader.placeholderDocOutDrawable : ResourceLoader.placeholderDocInDrawable;
            } else {
                return null;
            }
        } else if (this.currentMessageObject.type == 9 && this.gifDrawable == null) {
            Drawable[] drawableArr;
            if (this.buttonState != 1 || this.currentMessageObject.isSending()) {
                drawableArr = ResourceLoader.buttonStatesDrawablesDoc[this.buttonState];
                if (!this.currentMessageObject.isOutOwner()) {
                    i = 0;
                }
                return drawableArr[i];
            }
            drawableArr = ResourceLoader.buttonStatesDrawablesDoc[2];
            if (!this.currentMessageObject.isOutOwner()) {
                i = 0;
            }
            return drawableArr[i];
        } else if (this.buttonState != 1 || this.currentMessageObject.isSending()) {
            return ResourceLoader.buttonStatesDrawables[this.buttonState];
        } else {
            return ResourceLoader.buttonStatesDrawables[4];
        }
    }

    private void didPressedButton(boolean animated) {
        if (this.buttonState == 0) {
            this.cancelLoading = false;
            this.radialProgress.setProgress(0.0f, false);
            if (this.currentMessageObject.type == 1) {
                this.photoImage.setImage(this.currentPhotoObject.location, this.currentPhotoFilter, this.currentPhotoObjectThumb != null ? this.currentPhotoObjectThumb.location : null, this.currentPhotoFilter, this.currentPhotoObject.size, null, false);
            } else if (this.currentMessageObject.type == 8 || this.currentMessageObject.type == 9) {
                FileLoader.getInstance().loadFile(this.currentMessageObject.messageOwner.media.document, true, false);
                lastDownloadedGifMessage = this.currentMessageObject;
            } else if (this.currentMessageObject.type == 3) {
                FileLoader.getInstance().loadFile(this.currentMessageObject.messageOwner.media.video, true);
            }
            this.buttonState = 1;
            this.radialProgress.setBackground(getDrawableForCurrentState(), true, animated);
            invalidate();
        } else if (this.buttonState == 1) {
            if (!this.currentMessageObject.isOut() || !this.currentMessageObject.isSending()) {
                this.cancelLoading = true;
                if (this.currentMessageObject.type == 1) {
                    this.photoImage.cancelLoadImage();
                } else if (this.currentMessageObject.type == 8 || this.currentMessageObject.type == 9) {
                    FileLoader.getInstance().cancelLoadFile(this.currentMessageObject.messageOwner.media.document);
                    if (lastDownloadedGifMessage != null && lastDownloadedGifMessage.getId() == this.currentMessageObject.getId()) {
                        lastDownloadedGifMessage = null;
                    }
                } else if (this.currentMessageObject.type == 3) {
                    FileLoader.getInstance().cancelLoadFile(this.currentMessageObject.messageOwner.media.video);
                }
                this.buttonState = 0;
                this.radialProgress.setBackground(getDrawableForCurrentState(), false, animated);
                invalidate();
            } else if (this.delegate != null) {
                this.delegate.didPressedCancelSendButton(this);
            }
        } else if (this.buttonState == 2) {
            if (this.gifDrawable == null) {
                this.gifDrawable = MediaController.getInstance().getGifDrawable(this, true);
            }
            if (this.gifDrawable != null) {
                this.gifDrawable.start();
                this.gifDrawable.invalidateSelf();
                this.buttonState = -1;
                this.radialProgress.setBackground(getDrawableForCurrentState(), false, animated);
            }
        } else if (this.buttonState == 3 && this.delegate != null) {
            this.delegate.didClickedImage(this);
        }
    }

    private boolean isPhotoDataChanged(MessageObject object) {
        if (object.type == 4) {
            if (this.currentUrl == null) {
                return true;
            }
            double lat = object.messageOwner.media.geo.lat;
            double lon = object.messageOwner.media.geo._long;
            if (!String.format(Locale.US, "https://maps.googleapis.com/maps/api/staticmap?center=%f,%f&zoom=15&size=100x100&maptype=roadmap&scale=%d&markers=color:red|size:big|%f,%f&sensor=false", new Object[]{Double.valueOf(lat), Double.valueOf(lon), Integer.valueOf(Math.min(2, (int) Math.ceil((double) AndroidUtilities.density))), Double.valueOf(lat), Double.valueOf(lon)}).equals(this.currentUrl)) {
                return true;
            }
        } else if (this.currentPhotoObject == null || (this.currentPhotoObject.location instanceof TL_fileLocationUnavailable)) {
            return true;
        } else {
            if (this.currentMessageObject != null && this.photoNotSet && FileLoader.getPathToMessage(this.currentMessageObject.messageOwner).exists()) {
                return true;
            }
        }
        return false;
    }

    public void setMessageObject(MessageObject messageObject) {
        boolean dataChanged = this.currentMessageObject == messageObject && (isUserDataChanged() || this.photoNotSet);
        if (this.currentMessageObject != messageObject || isPhotoDataChanged(messageObject) || dataChanged) {
            int maxWidth;
            boolean z = ((messageObject.type != 3 && messageObject.type != 1) || messageObject.messageOwner.fwd_from_id == null || messageObject.messageOwner.fwd_from_id.channel_id == 0) ? false : true;
            this.drawForwardedName = z;
            this.media = messageObject.type != 9;
            this.cancelLoading = false;
            this.additionHeight = 0;
            resetPressedLink();
            this.buttonState = -1;
            this.gifDrawable = null;
            this.currentPhotoObject = null;
            this.currentPhotoObjectThumb = null;
            this.currentUrl = null;
            this.photoNotSet = false;
            this.drawBackground = true;
            this.photoImage.setForcePreview(messageObject.isSecretPhoto());
            String str;
            if (messageObject.type == 9) {
                String name = messageObject.getDocumentName();
                if (name == null || name.length() == 0) {
                    name = LocaleController.getString("AttachDocument", C0553R.string.AttachDocument);
                }
                if (AndroidUtilities.isTablet()) {
                    maxWidth = AndroidUtilities.getMinTabletSide() - AndroidUtilities.dp(232.0f);
                } else {
                    maxWidth = Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y) - AndroidUtilities.dp(232.0f);
                }
                if (this.currentNameString == null || !this.currentNameString.equals(name)) {
                    this.currentNameString = name;
                    this.nameLayout = StaticLayoutEx.createStaticLayout(this.currentNameString, namePaint, maxWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false, TruncateAt.END, maxWidth, 1);
                    if (this.nameLayout == null || this.nameLayout.getLineCount() <= 0) {
                        this.nameWidth = maxWidth;
                    } else {
                        this.nameWidth = Math.min(maxWidth, (int) Math.ceil((double) this.nameLayout.getLineWidth(0)));
                        this.nameOffsetX = (int) Math.ceil((double) (-this.nameLayout.getLineLeft(0)));
                    }
                }
                str = AndroidUtilities.formatFileSize((long) messageObject.messageOwner.media.document.size) + " " + messageObject.getExtension();
                if (this.currentInfoString == null || !this.currentInfoString.equals(str)) {
                    this.currentInfoString = str;
                    this.infoOffset = 0;
                    this.infoWidth = Math.min(maxWidth, (int) Math.ceil((double) infoPaint.measureText(this.currentInfoString)));
                    this.infoLayout = new StaticLayout(TextUtils.ellipsize(this.currentInfoString, infoPaint, (float) this.infoWidth, TruncateAt.END), infoPaint, this.infoWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                }
            } else if (messageObject.type == 8) {
                this.gifDrawable = MediaController.getInstance().getGifDrawable(this, false);
                str = AndroidUtilities.formatFileSize((long) messageObject.messageOwner.media.document.size);
                if (this.currentInfoString == null || !this.currentInfoString.equals(str)) {
                    this.currentInfoString = str;
                    this.infoOffset = 0;
                    this.infoWidth = (int) Math.ceil((double) infoPaint.measureText(this.currentInfoString));
                    this.infoLayout = new StaticLayout(this.currentInfoString, infoPaint, this.infoWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                }
                this.nameLayout = null;
                this.currentNameString = null;
            } else if (messageObject.type == 3) {
                int duration = messageObject.messageOwner.media.video.duration;
                int seconds = duration - ((duration / 60) * 60);
                str = String.format("%d:%02d, %s", new Object[]{Integer.valueOf(duration / 60), Integer.valueOf(seconds), AndroidUtilities.formatFileSize((long) messageObject.messageOwner.media.video.size)});
                if (this.currentInfoString == null || !this.currentInfoString.equals(str)) {
                    this.currentInfoString = str;
                    this.infoOffset = ResourceLoader.videoIconDrawable.getIntrinsicWidth() + AndroidUtilities.dp(4.0f);
                    this.infoWidth = (int) Math.ceil((double) infoPaint.measureText(this.currentInfoString));
                    this.infoLayout = new StaticLayout(this.currentInfoString, infoPaint, this.infoWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                }
                this.nameLayout = null;
                this.currentNameString = null;
            } else {
                this.currentInfoString = null;
                this.currentNameString = null;
                this.infoLayout = null;
                this.nameLayout = null;
                updateSecretTimeText(messageObject);
            }
            if (messageObject.type == 9) {
                this.photoWidth = AndroidUtilities.dp(86.0f);
                this.photoHeight = AndroidUtilities.dp(86.0f);
                this.backgroundWidth = (this.photoWidth + Math.max(this.nameWidth, this.infoWidth)) + AndroidUtilities.dp(68.0f);
                this.currentPhotoObject = FileLoader.getClosestPhotoSizeWithSize(messageObject.photoThumbs, AndroidUtilities.getPhotoSize());
                this.photoImage.setNeedsQualityThumb(true);
                this.photoImage.setShouldGenerateQualityThumb(true);
                this.photoImage.setParentMessageObject(messageObject);
                if (this.currentPhotoObject != null) {
                    this.currentPhotoFilter = String.format(Locale.US, "%d_%d_b", new Object[]{Integer.valueOf(this.photoWidth), Integer.valueOf(this.photoHeight)});
                    this.photoImage.setImage(null, null, null, null, this.currentPhotoObject.location, this.currentPhotoFilter, 0, null, true);
                } else {
                    this.photoImage.setImageBitmap((BitmapDrawable) null);
                }
            } else if (messageObject.type == 4) {
                Drawable drawable;
                double lat = messageObject.messageOwner.media.geo.lat;
                double lon = messageObject.messageOwner.media.geo._long;
                if (messageObject.messageOwner.media.title == null || messageObject.messageOwner.media.title.length() <= 0) {
                    this.photoWidth = AndroidUtilities.dp(200.0f);
                    this.photoHeight = AndroidUtilities.dp(100.0f);
                    this.backgroundWidth = this.photoWidth + AndroidUtilities.dp(12.0f);
                    this.currentUrl = String.format(Locale.US, "https://maps.googleapis.com/maps/api/staticmap?center=%f,%f&zoom=15&size=200x100&maptype=roadmap&scale=%d&markers=color:red|size:big|%f,%f&sensor=false", new Object[]{Double.valueOf(lat), Double.valueOf(lon), Integer.valueOf(Math.min(2, (int) Math.ceil((double) AndroidUtilities.density))), Double.valueOf(lat), Double.valueOf(lon)});
                } else {
                    int a;
                    int minTabletSide = AndroidUtilities.isTablet() ? AndroidUtilities.getMinTabletSide() : Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y);
                    int i = (!this.isChat || messageObject.isOutOwner()) ? 40 : LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
                    maxWidth = minTabletSide - AndroidUtilities.dp((float) ((i + 86) + 24));
                    this.nameLayout = StaticLayoutEx.createStaticLayout(messageObject.messageOwner.media.title, locationTitlePaint, maxWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false, TruncateAt.END, maxWidth - AndroidUtilities.dp(4.0f), 3);
                    int lineCount = this.nameLayout.getLineCount();
                    if (messageObject.messageOwner.media.address == null || messageObject.messageOwner.media.address.length() <= 0) {
                        this.infoLayout = null;
                    } else {
                        this.infoLayout = StaticLayoutEx.createStaticLayout(messageObject.messageOwner.media.address, locationAddressPaint, maxWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false, TruncateAt.END, maxWidth - AndroidUtilities.dp(4.0f), Math.min(3, 4 - lineCount));
                    }
                    this.media = false;
                    measureTime(messageObject);
                    this.photoWidth = AndroidUtilities.dp(86.0f);
                    this.photoHeight = AndroidUtilities.dp(86.0f);
                    maxWidth = this.timeWidth + AndroidUtilities.dp(messageObject.isOutOwner() ? 29.0f : 9.0f);
                    for (a = 0; a < lineCount; a++) {
                        maxWidth = (int) Math.max((float) maxWidth, this.nameLayout.getLineWidth(a) + ((float) AndroidUtilities.dp(16.0f)));
                    }
                    if (this.infoLayout != null) {
                        for (a = 0; a < this.infoLayout.getLineCount(); a++) {
                            maxWidth = (int) Math.max((float) maxWidth, this.infoLayout.getLineWidth(a) + ((float) AndroidUtilities.dp(16.0f)));
                        }
                    }
                    this.backgroundWidth = (this.photoWidth + AndroidUtilities.dp(21.0f)) + maxWidth;
                    this.currentUrl = String.format(Locale.US, "https://maps.googleapis.com/maps/api/staticmap?center=%f,%f&zoom=15&size=72x72&maptype=roadmap&scale=%d&markers=color:red|size:big|%f,%f&sensor=false", new Object[]{Double.valueOf(lat), Double.valueOf(lon), Integer.valueOf(Math.min(2, (int) Math.ceil((double) AndroidUtilities.density))), Double.valueOf(lat), Double.valueOf(lon)});
                }
                this.photoImage.setNeedsQualityThumb(false);
                this.photoImage.setShouldGenerateQualityThumb(false);
                this.photoImage.setParentMessageObject(null);
                r7 = this.photoImage;
                String str2 = this.currentUrl;
                if (messageObject.isOutOwner()) {
                    drawable = ResourceLoader.geoOutDrawable;
                } else {
                    drawable = ResourceLoader.geoInDrawable;
                }
                r7.setImage(str2, null, drawable, null, 0);
            } else if (messageObject.type == 13) {
                float maxWidth2;
                this.drawBackground = false;
                Iterator i$ = messageObject.messageOwner.media.document.attributes.iterator();
                while (i$.hasNext()) {
                    DocumentAttribute attribute = (DocumentAttribute) i$.next();
                    if (attribute instanceof TL_documentAttributeImageSize) {
                        this.photoWidth = attribute.f135w;
                        this.photoHeight = attribute.f134h;
                        break;
                    }
                }
                float maxHeight = ((float) AndroidUtilities.displaySize.y) * 0.4f;
                if (AndroidUtilities.isTablet()) {
                    maxWidth2 = ((float) AndroidUtilities.getMinTabletSide()) * 0.5f;
                } else {
                    maxWidth2 = ((float) AndroidUtilities.displaySize.x) * 0.5f;
                }
                if (this.photoWidth == 0) {
                    this.photoHeight = (int) maxHeight;
                    this.photoWidth = this.photoHeight + AndroidUtilities.dp(100.0f);
                }
                if (((float) this.photoHeight) > maxHeight) {
                    this.photoWidth = (int) (((float) this.photoWidth) * (maxHeight / ((float) this.photoHeight)));
                    this.photoHeight = (int) maxHeight;
                }
                if (((float) this.photoWidth) > maxWidth2) {
                    this.photoHeight = (int) (((float) this.photoHeight) * (maxWidth2 / ((float) this.photoWidth)));
                    this.photoWidth = (int) maxWidth2;
                }
                this.backgroundWidth = this.photoWidth + AndroidUtilities.dp(12.0f);
                this.currentPhotoObjectThumb = FileLoader.getClosestPhotoSizeWithSize(messageObject.photoThumbs, 80);
                this.photoImage.setNeedsQualityThumb(false);
                this.photoImage.setShouldGenerateQualityThumb(false);
                this.photoImage.setParentMessageObject(null);
                if (messageObject.messageOwner.attachPath == null || messageObject.messageOwner.attachPath.length() <= 0) {
                    if (messageObject.messageOwner.media.document.id != 0) {
                        this.photoImage.setImage(messageObject.messageOwner.media.document, null, String.format(Locale.US, "%d_%d", new Object[]{Integer.valueOf(this.photoWidth), Integer.valueOf(this.photoHeight)}), null, this.currentPhotoObjectThumb != null ? this.currentPhotoObjectThumb.location : null, "b1", messageObject.messageOwner.media.document.size, "webp", true);
                    }
                } else if (new File(messageObject.messageOwner.attachPath).exists()) {
                    FileLocation fileLocation;
                    r7 = this.photoImage;
                    r9 = messageObject.messageOwner.attachPath;
                    String format = String.format(Locale.US, "%d_%d", new Object[]{Integer.valueOf(this.photoWidth), Integer.valueOf(this.photoHeight)});
                    if (this.currentPhotoObjectThumb != null) {
                        fileLocation = this.currentPhotoObjectThumb.location;
                    } else {
                        fileLocation = null;
                    }
                    r7.setImage(null, r9, format, null, fileLocation, "b1", messageObject.messageOwner.media.document.size, "webp", true);
                }
            } else {
                if (AndroidUtilities.isTablet()) {
                    this.photoWidth = (int) (((float) AndroidUtilities.getMinTabletSide()) * 0.7f);
                } else {
                    this.photoWidth = (int) (((float) Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y)) * 0.7f);
                }
                this.photoHeight = this.photoWidth + AndroidUtilities.dp(100.0f);
                if (this.photoWidth > AndroidUtilities.getPhotoSize()) {
                    this.photoWidth = AndroidUtilities.getPhotoSize();
                }
                if (this.photoHeight > AndroidUtilities.getPhotoSize()) {
                    this.photoHeight = AndroidUtilities.getPhotoSize();
                }
                if (messageObject.type == 1) {
                    this.photoImage.setNeedsQualityThumb(false);
                    this.photoImage.setShouldGenerateQualityThumb(false);
                    this.photoImage.setParentMessageObject(null);
                    this.currentPhotoObjectThumb = FileLoader.getClosestPhotoSizeWithSize(messageObject.photoThumbs, 80);
                } else if (messageObject.type == 3) {
                    this.photoImage.setNeedsQualityThumb(true);
                    this.photoImage.setShouldGenerateQualityThumb(true);
                    this.photoImage.setParentMessageObject(messageObject);
                } else if (messageObject.type == 8) {
                    this.photoImage.setNeedsQualityThumb(true);
                    this.photoImage.setShouldGenerateQualityThumb(true);
                    this.photoImage.setParentMessageObject(messageObject);
                }
                if (messageObject.caption != null) {
                    this.media = false;
                }
                this.currentPhotoObject = FileLoader.getClosestPhotoSizeWithSize(messageObject.photoThumbs, AndroidUtilities.getPhotoSize());
                int w = AndroidUtilities.dp(100.0f);
                int h = AndroidUtilities.dp(100.0f);
                if (this.currentPhotoObject != null) {
                    if (this.currentPhotoObject == this.currentPhotoObjectThumb) {
                        this.currentPhotoObjectThumb = null;
                    }
                    float scale = ((float) this.currentPhotoObject.f139w) / ((float) this.photoWidth);
                    w = (int) (((float) this.currentPhotoObject.f139w) / scale);
                    h = (int) (((float) this.currentPhotoObject.f138h) / scale);
                    if (w == 0) {
                        if (messageObject.type == 3) {
                            w = (this.infoWidth + this.infoOffset) + AndroidUtilities.dp(16.0f);
                        } else {
                            w = AndroidUtilities.dp(100.0f);
                        }
                    }
                    if (h == 0) {
                        h = AndroidUtilities.dp(100.0f);
                    }
                    if (h > this.photoHeight) {
                        float scale2 = (float) h;
                        h = this.photoHeight;
                        w = (int) (((float) w) / (scale2 / ((float) h)));
                    } else if (h < AndroidUtilities.dp(BitmapDescriptorFactory.HUE_GREEN)) {
                        h = AndroidUtilities.dp(BitmapDescriptorFactory.HUE_GREEN);
                        float hScale = ((float) this.currentPhotoObject.f138h) / ((float) h);
                        if (((float) this.currentPhotoObject.f139w) / hScale < ((float) this.photoWidth)) {
                            w = (int) (((float) this.currentPhotoObject.f139w) / hScale);
                        }
                    }
                }
                measureTime(messageObject);
                int timeWidthTotal = this.timeWidth + AndroidUtilities.dp((float) ((messageObject.isOutOwner() ? 20 : 0) + 14));
                if (w < timeWidthTotal) {
                    w = timeWidthTotal;
                }
                if (messageObject.isSecretPhoto()) {
                    if (AndroidUtilities.isTablet()) {
                        h = (int) (((float) AndroidUtilities.getMinTabletSide()) * 0.5f);
                        w = h;
                    } else {
                        h = (int) (((float) Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y)) * 0.5f);
                        w = h;
                    }
                }
                this.photoWidth = w;
                this.photoHeight = h;
                this.backgroundWidth = AndroidUtilities.dp(12.0f) + w;
                if (!this.media) {
                    this.backgroundWidth += AndroidUtilities.dp(9.0f);
                }
                if (messageObject.caption != null) {
                    try {
                        this.nameLayout = new StaticLayout(messageObject.caption, MessageObject.textPaint, this.photoWidth - AndroidUtilities.dp(10.0f), Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                        if (this.nameLayout != null && this.nameLayout.getLineCount() > 0) {
                            this.captionHeight = this.nameLayout.getHeight();
                            this.additionHeight += this.captionHeight + AndroidUtilities.dp(9.0f);
                            if (((float) (this.photoWidth - AndroidUtilities.dp(8.0f))) - (this.nameLayout.getLineWidth(this.nameLayout.getLineCount() - 1) + this.nameLayout.getLineLeft(this.nameLayout.getLineCount() - 1)) < ((float) timeWidthTotal)) {
                                this.additionHeight += AndroidUtilities.dp(14.0f);
                            }
                        }
                    } catch (Throwable e) {
                        FileLog.m611e("tmessages", e);
                    }
                }
                this.currentPhotoFilter = String.format(Locale.US, "%d_%d", new Object[]{Integer.valueOf((int) (((float) w) / AndroidUtilities.density)), Integer.valueOf((int) (((float) h) / AndroidUtilities.density))});
                if (messageObject.photoThumbs.size() > 1 || messageObject.type == 3 || messageObject.type == 8) {
                    if (messageObject.isSecretPhoto()) {
                        this.currentPhotoFilter += "_b2";
                    } else {
                        this.currentPhotoFilter += "_b";
                    }
                }
                if (this.currentPhotoObject != null) {
                    boolean noSize = false;
                    if (messageObject.type == 3 || messageObject.type == 8) {
                        noSize = true;
                    }
                    if (!noSize && this.currentPhotoObject.size == 0) {
                        this.currentPhotoObject.size = -1;
                    }
                    String fileName = FileLoader.getAttachFileName(this.currentPhotoObject);
                    if (messageObject.type == 1) {
                        boolean photoExist = true;
                        if (FileLoader.getPathToMessage(messageObject.messageOwner).exists()) {
                            MediaController.getInstance().removeLoadingFileObserver(this);
                        } else {
                            photoExist = false;
                        }
                        if (!photoExist && !MediaController.getInstance().canDownloadMedia(1) && !FileLoader.getInstance().isLoadingFile(fileName)) {
                            this.photoNotSet = true;
                            if (this.currentPhotoObjectThumb != null) {
                                this.photoImage.setImage(null, null, this.currentPhotoObjectThumb.location, this.currentPhotoFilter, 0, null, false);
                            } else {
                                this.photoImage.setImageBitmap((Drawable) null);
                            }
                        } else if (this.allowedToSetPhoto || ImageLoader.getInstance().getImageFromMemory(this.currentPhotoObject.location, null, this.currentPhotoFilter) != null) {
                            FileLocation fileLocation2;
                            int i2;
                            this.allowedToSetPhoto = true;
                            r7 = this.photoImage;
                            TLObject tLObject = this.currentPhotoObject.location;
                            r9 = this.currentPhotoFilter;
                            if (this.currentPhotoObjectThumb != null) {
                                fileLocation2 = this.currentPhotoObjectThumb.location;
                            } else {
                                fileLocation2 = null;
                            }
                            String str3 = this.currentPhotoFilter;
                            if (noSize) {
                                i2 = 0;
                            } else {
                                i2 = this.currentPhotoObject.size;
                            }
                            r7.setImage(tLObject, r9, fileLocation2, str3, i2, null, false);
                        } else if (this.currentPhotoObjectThumb != null) {
                            this.photoImage.setImage(null, null, this.currentPhotoObjectThumb.location, this.currentPhotoFilter, 0, null, false);
                        } else {
                            this.photoImage.setImageBitmap((Drawable) null);
                        }
                    } else {
                        this.photoImage.setImage(null, null, this.currentPhotoObject.location, this.currentPhotoFilter, 0, null, false);
                    }
                } else {
                    this.photoImage.setImageBitmap((Bitmap) null);
                }
            }
            super.setMessageObject(messageObject);
            if (this.drawForwardedName) {
                this.namesOffset += AndroidUtilities.dp(5.0f);
            }
            invalidate();
        }
        updateButtonState(dataChanged);
    }

    protected int getMaxNameWidth() {
        return this.backgroundWidth - AndroidUtilities.dp(14.0f);
    }

    public ImageReceiver getPhotoImage() {
        return this.photoImage;
    }

    public void updateButtonState(boolean animated) {
        String fileName = null;
        File cacheFile = null;
        if (this.currentMessageObject.type == 1) {
            if (this.currentPhotoObject != null) {
                fileName = FileLoader.getAttachFileName(this.currentPhotoObject);
                cacheFile = FileLoader.getPathToMessage(this.currentMessageObject.messageOwner);
            } else {
                return;
            }
        } else if (this.currentMessageObject.type == 8 || this.currentMessageObject.type == 3 || this.currentMessageObject.type == 9) {
            if (!(this.currentMessageObject.messageOwner.attachPath == null || this.currentMessageObject.messageOwner.attachPath.length() == 0)) {
                File f = new File(this.currentMessageObject.messageOwner.attachPath);
                if (f.exists()) {
                    fileName = this.currentMessageObject.messageOwner.attachPath;
                    cacheFile = f;
                }
            }
            if (fileName == null) {
                fileName = this.currentMessageObject.getFileName();
                cacheFile = FileLoader.getPathToMessage(this.currentMessageObject.messageOwner);
            }
        }
        if (fileName == null) {
            this.radialProgress.setBackground(null, false, false);
        } else if (!this.currentMessageObject.isOut() || !this.currentMessageObject.isSending()) {
            if (!(this.currentMessageObject.messageOwner.attachPath == null || this.currentMessageObject.messageOwner.attachPath.length() == 0)) {
                MediaController.getInstance().removeLoadingFileObserver(this);
            }
            if (cacheFile.exists() && cacheFile.length() == 0) {
                cacheFile.delete();
            }
            if (cacheFile.exists()) {
                MediaController.getInstance().removeLoadingFileObserver(this);
                if (this.currentMessageObject.type == 8 && (this.gifDrawable == null || !this.gifDrawable.isRunning())) {
                    this.buttonState = 2;
                } else if (this.currentMessageObject.type == 3) {
                    this.buttonState = 3;
                } else {
                    this.buttonState = -1;
                }
                this.radialProgress.setBackground(getDrawableForCurrentState(), false, animated);
                invalidate();
                return;
            }
            MediaController.getInstance().addLoadingFileObserver(fileName, this);
            float setProgress = 0.0f;
            boolean progressVisible = false;
            if (FileLoader.getInstance().isLoadingFile(fileName)) {
                progressVisible = true;
                this.buttonState = 1;
                progress = ImageLoader.getInstance().getFileProgress(fileName);
                setProgress = progress != null ? progress.floatValue() : 0.0f;
            } else if (!this.cancelLoading && this.currentMessageObject.type == 1 && MediaController.getInstance().canDownloadMedia(1)) {
                progressVisible = true;
                this.buttonState = 1;
            } else {
                this.buttonState = 0;
            }
            this.radialProgress.setProgress(setProgress, false);
            this.radialProgress.setBackground(getDrawableForCurrentState(), progressVisible, animated);
            invalidate();
        } else if (this.currentMessageObject.messageOwner.attachPath != null && this.currentMessageObject.messageOwner.attachPath.length() > 0) {
            MediaController.getInstance().addLoadingFileObserver(this.currentMessageObject.messageOwner.attachPath, this);
            this.buttonState = 1;
            this.radialProgress.setBackground(getDrawableForCurrentState(), true, animated);
            progress = ImageLoader.getInstance().getFileProgress(this.currentMessageObject.messageOwner.attachPath);
            if (progress == null && SendMessagesHelper.getInstance().isSendingMessage(this.currentMessageObject.getId())) {
                progress = Float.valueOf(1.0f);
            }
            this.radialProgress.setProgress(progress != null ? progress.floatValue() : 0.0f, false);
            invalidate();
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), ((this.photoHeight + AndroidUtilities.dp(14.0f)) + this.namesOffset) + this.additionHeight);
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int x;
        super.onLayout(changed, left, top, right, bottom);
        if (this.currentMessageObject.isOutOwner()) {
            if (this.media) {
                x = (this.layoutWidth - this.backgroundWidth) - AndroidUtilities.dp(3.0f);
            } else {
                x = (this.layoutWidth - this.backgroundWidth) + AndroidUtilities.dp(6.0f);
            }
        } else if (!this.isChat || this.currentMessageObject.messageOwner.from_id <= 0) {
            x = AndroidUtilities.dp(15.0f);
        } else {
            x = AndroidUtilities.dp(67.0f);
        }
        this.photoImage.setImageCoords(x, AndroidUtilities.dp(7.0f) + this.namesOffset, this.photoWidth, this.photoHeight);
        int size = AndroidUtilities.dp(48.0f);
        this.buttonX = (int) (((float) x) + (((float) (this.photoWidth - size)) / 2.0f));
        this.buttonY = ((int) (((float) AndroidUtilities.dp(7.0f)) + (((float) (this.photoHeight - size)) / 2.0f))) + this.namesOffset;
        this.radialProgress.setProgressRect(this.buttonX, this.buttonY, this.buttonX + AndroidUtilities.dp(48.0f), this.buttonY + AndroidUtilities.dp(48.0f));
        this.deleteProgressRect.set((float) (this.buttonX + AndroidUtilities.dp(3.0f)), (float) (this.buttonY + AndroidUtilities.dp(3.0f)), (float) (this.buttonX + AndroidUtilities.dp(45.0f)), (float) (this.buttonY + AndroidUtilities.dp(45.0f)));
    }

    private void updateSecretTimeText(MessageObject messageObject) {
        if (messageObject != null && !messageObject.isOut()) {
            String str = messageObject.getSecretTimeString();
            if (str == null) {
                this.infoLayout = null;
            } else if (this.currentInfoString == null || !this.currentInfoString.equals(str)) {
                this.currentInfoString = str;
                this.infoOffset = 0;
                this.infoWidth = (int) Math.ceil((double) infoPaint.measureText(this.currentInfoString));
                this.infoLayout = new StaticLayout(TextUtils.ellipsize(this.currentInfoString, infoPaint, (float) this.infoWidth, TruncateAt.END), infoPaint, this.infoWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                invalidate();
            }
        }
    }

    public void setAllowedToSetPhoto(boolean value) {
        if (this.allowedToSetPhoto != value && this.currentMessageObject != null && this.currentMessageObject.type == 1) {
            this.allowedToSetPhoto = value;
            if (value) {
                MessageObject temp = this.currentMessageObject;
                this.currentMessageObject = null;
                setMessageObject(temp);
            }
        }
    }

    protected void onAfterBackgroundDraw(Canvas canvas) {
        int i;
        boolean imageDrawn = false;
        if (this.gifDrawable != null) {
            this.drawTime = !this.gifDrawable.isPlaying();
            canvas.save();
            this.gifDrawable.setBounds(this.photoImage.getImageX(), this.photoImage.getImageY(), this.photoImage.getImageX() + this.photoWidth, this.photoImage.getImageY() + this.photoHeight);
            this.gifDrawable.draw(canvas);
            canvas.restore();
        } else {
            ImageReceiver imageReceiver = this.photoImage;
            boolean z = (isPressed() && this.isCheckPressed) || ((!this.isCheckPressed && this.isPressed) || this.isHighlighted);
            imageReceiver.setPressed(z);
            this.photoImage.setVisible(!PhotoViewer.getInstance().isShowingImage(this.currentMessageObject), false);
            imageDrawn = this.photoImage.draw(canvas);
            this.drawTime = this.photoImage.getVisible();
        }
        this.radialProgress.setHideCurrentDrawable(false);
        if (this.currentMessageObject.type == 9) {
            Drawable menuDrawable;
            if (this.currentMessageObject.isOutOwner()) {
                infoPaint.setColor(-9391780);
                docBackPaint.setColor(-2427453);
                menuDrawable = ResourceLoader.docMenuOutDrawable;
            } else {
                infoPaint.setColor(-6181445);
                docBackPaint.setColor(-1314571);
                menuDrawable = ResourceLoader.docMenuInDrawable;
            }
            setDrawableBounds(menuDrawable, (this.photoImage.getImageX() + this.backgroundWidth) - AndroidUtilities.dp(44.0f), AndroidUtilities.dp(10.0f) + this.namesOffset);
            menuDrawable.draw(canvas);
            if (this.buttonState >= 0 && this.buttonState < 4) {
                if (imageDrawn) {
                    if (this.buttonState != 1 || this.currentMessageObject.isSending()) {
                        this.radialProgress.swapBackground(ResourceLoader.buttonStatesDrawables[this.buttonState]);
                    } else {
                        this.radialProgress.swapBackground(ResourceLoader.buttonStatesDrawables[4]);
                    }
                } else if (this.buttonState != 1 || this.currentMessageObject.isSending()) {
                    this.radialProgress.swapBackground(ResourceLoader.buttonStatesDrawablesDoc[this.buttonState][this.currentMessageObject.isOutOwner() ? 1 : 0]);
                } else {
                    RadialProgress radialProgress = this.radialProgress;
                    Drawable[] drawableArr = ResourceLoader.buttonStatesDrawablesDoc[2];
                    if (this.currentMessageObject.isOutOwner()) {
                        i = 1;
                    } else {
                        i = 0;
                    }
                    radialProgress.swapBackground(drawableArr[i]);
                }
            }
            if (imageDrawn) {
                if (this.buttonState == -1) {
                    this.radialProgress.setHideCurrentDrawable(true);
                }
                this.radialProgress.setProgressColor(-1);
            } else {
                canvas.drawRect((float) this.photoImage.getImageX(), (float) this.photoImage.getImageY(), (float) (this.photoImage.getImageX() + this.photoImage.getImageWidth()), (float) (this.photoImage.getImageY() + this.photoImage.getImageHeight()), docBackPaint);
                if (this.currentMessageObject.isOutOwner()) {
                    this.radialProgress.setProgressColor(-8274574);
                } else {
                    this.radialProgress.setProgressColor(-5390900);
                }
            }
        } else {
            this.radialProgress.setProgressColor(-1);
        }
        if (this.buttonState == -1 && this.currentMessageObject.isSecretPhoto()) {
            int drawable = 5;
            if (this.currentMessageObject.messageOwner.destroyTime != 0) {
                if (this.currentMessageObject.isOutOwner()) {
                    drawable = 7;
                } else {
                    drawable = 6;
                }
            }
            setDrawableBounds(ResourceLoader.buttonStatesDrawables[drawable], this.buttonX, this.buttonY);
            ResourceLoader.buttonStatesDrawables[drawable].setAlpha((int) (255.0f * (1.0f - this.radialProgress.getAlpha())));
            ResourceLoader.buttonStatesDrawables[drawable].draw(canvas);
            if (!(this.currentMessageObject.isOutOwner() || this.currentMessageObject.messageOwner.destroyTime == 0)) {
                float progress = ((float) Math.max(0, (((long) this.currentMessageObject.messageOwner.destroyTime) * 1000) - (System.currentTimeMillis() + ((long) (ConnectionsManager.getInstance().getTimeDifference() * LocationStatusCodes.GEOFENCE_NOT_AVAILABLE))))) / (((float) this.currentMessageObject.messageOwner.ttl) * 1000.0f);
                canvas.drawArc(this.deleteProgressRect, -90.0f, -360.0f * progress, true, deleteProgressPaint);
                if (progress != 0.0f) {
                    int offset = AndroidUtilities.dp(2.0f);
                    invalidate(((int) this.deleteProgressRect.left) - offset, ((int) this.deleteProgressRect.top) - offset, ((int) this.deleteProgressRect.right) + (offset * 2), ((int) this.deleteProgressRect.bottom) + (offset * 2));
                }
                updateSecretTimeText(this.currentMessageObject);
            }
        }
        this.radialProgress.draw(canvas);
        if (this.currentMessageObject.type == 1 || this.currentMessageObject.type == 3) {
            if (this.nameLayout != null) {
                canvas.save();
                i = this.photoImage.getImageX() + AndroidUtilities.dp(5.0f);
                this.captionX = i;
                float f = (float) i;
                int imageY = (this.photoImage.getImageY() + this.photoHeight) + AndroidUtilities.dp(6.0f);
                this.captionY = imageY;
                canvas.translate(f, (float) imageY);
                if (this.pressedLink != null) {
                    canvas.drawPath(this.urlPath, urlPaint);
                }
                try {
                    this.nameLayout.draw(canvas);
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
                canvas.restore();
            }
            if (this.infoLayout == null) {
                return;
            }
            if (this.buttonState == 1 || this.buttonState == 0 || this.buttonState == 3 || this.currentMessageObject.isSecretPhoto()) {
                infoPaint.setColor(-1);
                setDrawableBounds(ResourceLoader.mediaBackgroundDrawable, AndroidUtilities.dp(4.0f) + this.photoImage.getImageX(), AndroidUtilities.dp(4.0f) + this.photoImage.getImageY(), this.infoOffset + (this.infoWidth + AndroidUtilities.dp(8.0f)), AndroidUtilities.dp(16.5f));
                ResourceLoader.mediaBackgroundDrawable.draw(canvas);
                if (this.currentMessageObject.type == 3) {
                    setDrawableBounds(ResourceLoader.videoIconDrawable, this.photoImage.getImageX() + AndroidUtilities.dp(8.0f), this.photoImage.getImageY() + AndroidUtilities.dp(7.5f));
                    ResourceLoader.videoIconDrawable.draw(canvas);
                }
                canvas.save();
                canvas.translate((float) ((this.photoImage.getImageX() + AndroidUtilities.dp(8.0f)) + this.infoOffset), (float) (this.photoImage.getImageY() + AndroidUtilities.dp(5.5f)));
                this.infoLayout.draw(canvas);
                canvas.restore();
            }
        } else if (this.currentMessageObject.type == 4) {
            if (this.nameLayout != null) {
                locationAddressPaint.setColor(this.currentMessageObject.isOutOwner() ? -9391780 : -6710887);
                canvas.save();
                canvas.translate((float) (((this.nameOffsetX + this.photoImage.getImageX()) + this.photoImage.getImageWidth()) + AndroidUtilities.dp(10.0f)), (float) (this.photoImage.getImageY() + AndroidUtilities.dp(3.0f)));
                this.nameLayout.draw(canvas);
                canvas.restore();
                if (this.infoLayout != null) {
                    canvas.save();
                    canvas.translate((float) ((this.photoImage.getImageX() + this.photoImage.getImageWidth()) + AndroidUtilities.dp(10.0f)), (float) (this.photoImage.getImageY() + AndroidUtilities.dp((float) ((this.nameLayout.getLineCount() * 16) + 5))));
                    this.infoLayout.draw(canvas);
                    canvas.restore();
                }
            }
        } else if (this.nameLayout != null) {
            canvas.save();
            canvas.translate((float) (((this.nameOffsetX + this.photoImage.getImageX()) + this.photoImage.getImageWidth()) + AndroidUtilities.dp(10.0f)), (float) (this.photoImage.getImageY() + AndroidUtilities.dp(8.0f)));
            this.nameLayout.draw(canvas);
            canvas.restore();
            if (this.infoLayout != null) {
                canvas.save();
                canvas.translate((float) ((this.photoImage.getImageX() + this.photoImage.getImageWidth()) + AndroidUtilities.dp(10.0f)), (float) (this.photoImage.getImageY() + AndroidUtilities.dp(BitmapDescriptorFactory.HUE_ORANGE)));
                this.infoLayout.draw(canvas);
                canvas.restore();
            }
        }
    }

    public void onFailedDownload(String fileName) {
        updateButtonState(false);
    }

    public void onSuccessDownload(String fileName) {
        this.radialProgress.setProgress(1.0f, true);
        if (this.currentMessageObject.type == 8 && lastDownloadedGifMessage != null && lastDownloadedGifMessage.getId() == this.currentMessageObject.getId()) {
            this.buttonState = 2;
            didPressedButton(true);
        } else if (!this.photoNotSet) {
            updateButtonState(true);
        }
        if (this.photoNotSet) {
            setMessageObject(this.currentMessageObject);
        }
    }

    public void onProgressDownload(String fileName, float progress) {
        this.radialProgress.setProgress(progress, true);
        if (this.buttonState != 1) {
            updateButtonState(false);
        }
    }

    public void onProgressUpload(String fileName, float progress, boolean isEncrypted) {
        this.radialProgress.setProgress(progress, true);
    }
}
