package org.telegram.ui.Cells;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build.VERSION;
import android.support.v4.view.ViewCompat;
import android.text.Layout.Alignment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils.TruncateAt;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.view.View.MeasureSpec;
import android.view.ViewStructure;
import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.C0553R;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessageObject.TextLayoutBlock;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.TL_messageMediaWebPage;
import org.telegram.tgnet.TLRPC.TL_webPage;
import org.telegram.tgnet.TLRPC.WebPage;
import org.telegram.ui.Components.RadialProgress;
import org.telegram.ui.Components.ResourceLoader;
import org.telegram.ui.Components.StaticLayoutEx;
import org.telegram.ui.Components.URLSpanBotCommand;
import org.telegram.ui.Components.URLSpanNoUnderline;

public class ChatMessageCell extends ChatBaseCell {
    private static TextPaint durationPaint;
    private static Drawable igvideoDrawable;
    private StaticLayout authorLayout;
    private int authorX;
    private boolean buttonPressed;
    private int buttonState;
    private int buttonX;
    private int buttonY;
    private boolean cancelLoading;
    private String currentPhotoFilter;
    private String currentPhotoFilterThumb;
    private PhotoSize currentPhotoObject;
    private PhotoSize currentPhotoObjectThumb;
    private StaticLayout descriptionLayout;
    private int descriptionX;
    private int descriptionY;
    private boolean drawImageButton;
    private boolean drawLinkImageView;
    private StaticLayout durationLayout;
    private int durationWidth;
    private int firstVisibleBlockNum;
    private boolean hasLinkPreview;
    private boolean isInstagram;
    private boolean isSmallImage;
    private int lastVisibleBlockNum;
    private int linkBlockNum;
    private ImageReceiver linkImageView;
    private int linkPreviewHeight;
    private boolean photoNotSet;
    private RadialProgress radialProgress;
    private StaticLayout siteNameLayout;
    private int textX;
    private int textY;
    private StaticLayout titleLayout;
    private int titleX;
    private int totalHeight;
    private int totalVisibleBlocksCount;

    public ChatMessageCell(Context context) {
        super(context);
        this.totalHeight = 0;
        this.lastVisibleBlockNum = 0;
        this.firstVisibleBlockNum = 0;
        this.totalVisibleBlocksCount = 0;
        this.drawForwardedName = true;
        this.linkImageView = new ImageReceiver(this);
        this.radialProgress = new RadialProgress(this);
    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean result = false;
        if (this.currentMessageObject == null || this.currentMessageObject.textLayoutBlocks == null || this.currentMessageObject.textLayoutBlocks.isEmpty() || !(this.currentMessageObject.messageText instanceof Spannable) || !this.delegate.canPerformActions()) {
            resetPressedLink();
        } else if (event.getAction() == 0 || ((this.linkPreviewPressed || this.pressedLink != null || this.buttonPressed) && event.getAction() == 1)) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            int line;
            int off;
            float left;
            Spannable buffer;
            ClickableSpan[] link;
            boolean ignore;
            int start;
            if (x >= this.textX && y >= this.textY && x <= this.textX + this.currentMessageObject.textWidth && y <= this.textY + this.currentMessageObject.textHeight) {
                y -= this.textY;
                int blockNum = Math.max(0, y / this.currentMessageObject.blockHeight);
                if (blockNum < this.currentMessageObject.textLayoutBlocks.size()) {
                    try {
                        TextLayoutBlock block = (TextLayoutBlock) this.currentMessageObject.textLayoutBlocks.get(blockNum);
                        x -= this.textX - ((int) Math.ceil((double) block.textXOffset));
                        line = block.textLayout.getLineForVertical((int) (((float) y) - block.textYOffset));
                        off = block.textLayout.getOffsetForHorizontal(line, (float) x) + block.charactersOffset;
                        left = block.textLayout.getLineLeft(line);
                        if (left > ((float) x) || block.textLayout.getLineWidth(line) + left < ((float) x)) {
                            resetPressedLink();
                        } else {
                            buffer = this.currentMessageObject.messageText;
                            link = (ClickableSpan[]) buffer.getSpans(off, off, ClickableSpan.class);
                            ignore = false;
                            if (link.length == 0 || !(link.length == 0 || !(link[0] instanceof URLSpanBotCommand) || URLSpanBotCommand.enabled)) {
                                ignore = true;
                            }
                            if (ignore) {
                                resetPressedLink();
                            } else if (event.getAction() == 0) {
                                resetPressedLink();
                                this.pressedLink = link[0];
                                this.linkBlockNum = blockNum;
                                try {
                                    start = buffer.getSpanStart(this.pressedLink) - block.charactersOffset;
                                    this.urlPath.setCurrentLayout(block.textLayout, start);
                                    block.textLayout.getSelectionPath(start, buffer.getSpanEnd(this.pressedLink) - block.charactersOffset, this.urlPath);
                                } catch (Throwable e) {
                                    FileLog.m611e("tmessages", e);
                                }
                                result = true;
                            } else if (link[0] == this.pressedLink) {
                                try {
                                    this.delegate.didPressUrl(this.currentMessageObject, this.pressedLink, false);
                                } catch (Throwable e2) {
                                    FileLog.m611e("tmessages", e2);
                                }
                                resetPressedLink();
                                result = true;
                            }
                        }
                    } catch (Throwable e22) {
                        resetPressedLink();
                        FileLog.m611e("tmessages", e22);
                    }
                } else {
                    resetPressedLink();
                }
            } else if (!this.hasLinkPreview || x < this.textX || x > this.textX + this.backgroundWidth || y < this.textY + this.currentMessageObject.textHeight || y > ((this.textY + this.currentMessageObject.textHeight) + this.linkPreviewHeight) + AndroidUtilities.dp(8.0f)) {
                resetPressedLink();
            } else if (event.getAction() == 0) {
                resetPressedLink();
                if (this.drawLinkImageView && this.linkImageView.isInsideImage((float) x, (float) y)) {
                    if (!this.drawImageButton || this.buttonState == -1 || x < this.buttonX || x > this.buttonX + AndroidUtilities.dp(48.0f) || y < this.buttonY || y > this.buttonY + AndroidUtilities.dp(48.0f)) {
                        this.linkPreviewPressed = true;
                        result = true;
                    } else {
                        this.buttonPressed = true;
                        result = true;
                    }
                } else if (this.descriptionLayout != null && y >= this.descriptionY) {
                    try {
                        x -= (this.textX + AndroidUtilities.dp(10.0f)) + this.descriptionX;
                        line = this.descriptionLayout.getLineForVertical(y - this.descriptionY);
                        off = this.descriptionLayout.getOffsetForHorizontal(line, (float) x);
                        left = this.descriptionLayout.getLineLeft(line);
                        if (left > ((float) x) || this.descriptionLayout.getLineWidth(line) + left < ((float) x)) {
                            resetPressedLink();
                        } else {
                            buffer = (Spannable) this.currentMessageObject.linkDescription;
                            link = (ClickableSpan[]) buffer.getSpans(off, off, ClickableSpan.class);
                            ignore = false;
                            if (link.length == 0 || !(link.length == 0 || !(link[0] instanceof URLSpanBotCommand) || URLSpanBotCommand.enabled)) {
                                ignore = true;
                            }
                            if (ignore) {
                                resetPressedLink();
                            } else {
                                resetPressedLink();
                                this.pressedLink = link[0];
                                this.linkPreviewPressed = true;
                                this.linkBlockNum = -10;
                                result = true;
                                try {
                                    start = buffer.getSpanStart(this.pressedLink);
                                    this.urlPath.setCurrentLayout(this.descriptionLayout, start);
                                    this.descriptionLayout.getSelectionPath(start, buffer.getSpanEnd(this.pressedLink), this.urlPath);
                                } catch (Throwable e222) {
                                    FileLog.m611e("tmessages", e222);
                                }
                            }
                        }
                    } catch (Throwable e2222) {
                        resetPressedLink();
                        FileLog.m611e("tmessages", e2222);
                    }
                }
            } else if (this.linkPreviewPressed) {
                try {
                    if (this.pressedLink != null) {
                        this.pressedLink.onClick(this);
                    } else if (!this.drawImageButton || this.delegate == null) {
                        WebPage webPage = this.currentMessageObject.messageOwner.media.webpage;
                        if (VERSION.SDK_INT < 16 || webPage.embed_url == null || webPage.embed_url.length() == 0) {
                            Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(webPage.url));
                            intent.putExtra("com.android.browser.application_id", getContext().getPackageName());
                            getContext().startActivity(intent);
                        } else {
                            this.delegate.needOpenWebView(webPage.embed_url, webPage.site_name, webPage.url, webPage.embed_width, webPage.embed_height);
                        }
                    } else if (this.buttonState == -1) {
                        playSoundEffect(0);
                        this.delegate.didClickedImage(this);
                    }
                } catch (Throwable e22222) {
                    FileLog.m611e("tmessages", e22222);
                }
                resetPressedLink();
                result = true;
            } else if (this.buttonPressed) {
                if (event.getAction() == 1) {
                    this.buttonPressed = false;
                    playSoundEffect(0);
                    didPressedButton(false);
                    invalidate();
                } else if (event.getAction() == 3) {
                    this.buttonPressed = false;
                    invalidate();
                } else if (event.getAction() == 2 && (x < this.buttonX || x > this.buttonX + AndroidUtilities.dp(48.0f) || y < this.buttonY || y > this.buttonY + AndroidUtilities.dp(48.0f))) {
                    this.buttonPressed = false;
                    invalidate();
                }
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
        if (result || super.onTouchEvent(event)) {
            return true;
        }
        return false;
    }

    public void setVisiblePart(int position, int height) {
        if (this.currentMessageObject != null && this.currentMessageObject.textLayoutBlocks != null) {
            int newFirst = -1;
            int newLast = -1;
            int newCount = 0;
            for (int a = Math.max(0, (position - this.textY) / this.currentMessageObject.blockHeight); a < this.currentMessageObject.textLayoutBlocks.size(); a++) {
                float y = ((float) this.textY) + ((TextLayoutBlock) this.currentMessageObject.textLayoutBlocks.get(a)).textYOffset;
                if (intersect(y, ((float) this.currentMessageObject.blockHeight) + y, (float) position, (float) (position + height))) {
                    if (newFirst == -1) {
                        newFirst = a;
                    }
                    newLast = a;
                    newCount++;
                } else if (y > ((float) position)) {
                    break;
                }
            }
            if (this.lastVisibleBlockNum != newLast || this.firstVisibleBlockNum != newFirst || this.totalVisibleBlocksCount != newCount) {
                this.lastVisibleBlockNum = newLast;
                this.firstVisibleBlockNum = newFirst;
                this.totalVisibleBlocksCount = newCount;
                invalidate();
            }
        }
    }

    private boolean intersect(float left1, float right1, float left2, float right2) {
        if (left1 <= left2) {
            if (right1 >= left2) {
                return true;
            }
            return false;
        } else if (left1 > right2) {
            return false;
        } else {
            return true;
        }
    }

    public static StaticLayout generateStaticLayout(CharSequence text, TextPaint paint, int maxWidth, int smallWidth, int linesCount, int maxLines) {
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder(text);
        int addedChars = 0;
        StaticLayout layout = new StaticLayout(text, paint, smallWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        int a = 0;
        while (a < linesCount) {
            if (layout.getLineLeft(a) != 0.0f) {
                maxWidth = smallWidth;
            }
            int pos = layout.getLineEnd(a);
            if (pos != text.length()) {
                pos--;
                if (stringBuilder.charAt(pos + addedChars) == ' ') {
                    stringBuilder.replace(pos + addedChars, (pos + addedChars) + 1, "\n");
                } else if (stringBuilder.charAt(pos + addedChars) != '\n') {
                    stringBuilder.insert(pos + addedChars, "\n");
                    addedChars++;
                }
                if (a == layout.getLineCount() - 1 || a == maxLines - 1) {
                    break;
                }
                a++;
            } else {
                break;
            }
        }
        return StaticLayoutEx.createStaticLayout(stringBuilder, paint, maxWidth, Alignment.ALIGN_NORMAL, 1.0f, (float) AndroidUtilities.dp(1.0f), false, TruncateAt.END, maxWidth, maxLines);
    }

    protected boolean isUserDataChanged() {
        if (this.hasLinkPreview || this.currentMessageObject.messageOwner.media == null || !(this.currentMessageObject.messageOwner.media.webpage instanceof TL_webPage)) {
            return super.isUserDataChanged();
        }
        return true;
    }

    public ImageReceiver getPhotoImage() {
        return this.linkImageView;
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.linkImageView.onDetachedFromWindow();
        MediaController.getInstance().removeLoadingFileObserver(this);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.linkImageView.onAttachedToWindow()) {
            updateButtonState(false);
        }
    }

    protected void onLongPress() {
        if ((this.pressedLink instanceof URLSpanNoUnderline) && this.pressedLink.getURL().startsWith("/")) {
            this.delegate.didPressUrl(this.currentMessageObject, this.pressedLink, true);
        } else {
            super.onLongPress();
        }
    }

    public void setMessageObject(MessageObject messageObject) {
        int linkPreviewMaxWidth;
        int height;
        Throwable e;
        int a;
        int lineLeft;
        boolean authorIsRTL;
        boolean hasRTL;
        boolean smallImage;
        ArrayList arrayList;
        int i;
        boolean z;
        float scale;
        String fileName;
        boolean photoExist;
        int seconds;
        String str;
        boolean dataChanged = this.currentMessageObject == messageObject && (isUserDataChanged() || this.photoNotSet);
        if (this.currentMessageObject != messageObject || dataChanged) {
            boolean z2;
            int maxWidth;
            if (this.currentMessageObject != messageObject) {
                this.firstVisibleBlockNum = 0;
                this.lastVisibleBlockNum = 0;
            }
            this.drawLinkImageView = false;
            this.hasLinkPreview = false;
            resetPressedLink();
            this.linkPreviewPressed = false;
            this.buttonPressed = false;
            this.linkPreviewHeight = 0;
            this.isInstagram = false;
            this.durationLayout = null;
            this.descriptionLayout = null;
            this.titleLayout = null;
            this.siteNameLayout = null;
            this.authorLayout = null;
            this.drawImageButton = false;
            this.currentPhotoObject = null;
            this.currentPhotoObjectThumb = null;
            this.currentPhotoFilter = null;
            if (AndroidUtilities.isTablet()) {
                if (!this.isChat || messageObject.isOutOwner() || messageObject.messageOwner.from_id <= 0) {
                    z2 = (messageObject.messageOwner.to_id.channel_id == 0 || messageObject.isOutOwner()) ? false : true;
                    this.drawName = z2;
                    maxWidth = AndroidUtilities.getMinTabletSide() - AndroidUtilities.dp(80.0f);
                } else {
                    maxWidth = AndroidUtilities.getMinTabletSide() - AndroidUtilities.dp(122.0f);
                    this.drawName = true;
                }
            } else if (!this.isChat || messageObject.isOutOwner() || messageObject.messageOwner.from_id <= 0) {
                maxWidth = Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y) - AndroidUtilities.dp(80.0f);
                z2 = (messageObject.messageOwner.to_id.channel_id == 0 || messageObject.isOutOwner()) ? false : true;
                this.drawName = z2;
            } else {
                maxWidth = Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y) - AndroidUtilities.dp(122.0f);
                this.drawName = true;
            }
            this.backgroundWidth = maxWidth;
            super.setMessageObject(messageObject);
            this.backgroundWidth = messageObject.textWidth;
            this.totalHeight = (messageObject.textHeight + AndroidUtilities.dp(19.5f)) + this.namesOffset;
            int maxChildWidth = Math.max(Math.max(Math.max(Math.max(this.backgroundWidth, this.nameWidth), this.forwardedNameWidth), this.replyNameWidth), this.replyTextWidth);
            int maxWebWidth = 0;
            int timeMore = this.timeWidth + AndroidUtilities.dp(6.0f);
            if (messageObject.isOutOwner()) {
                timeMore += AndroidUtilities.dp(20.5f);
            }
            if ((messageObject.messageOwner.media instanceof TL_messageMediaWebPage) && (messageObject.messageOwner.media.webpage instanceof TL_webPage)) {
                int width;
                int restLines;
                int restLinesCount;
                int maxPhotoWidth;
                if (AndroidUtilities.isTablet()) {
                    if (messageObject.messageOwner.from_id <= 0 || ((this.currentMessageObject.messageOwner.to_id.channel_id == 0 && this.currentMessageObject.messageOwner.to_id.chat_id == 0) || this.currentMessageObject.isOut())) {
                        linkPreviewMaxWidth = AndroidUtilities.getMinTabletSide() - AndroidUtilities.dp(80.0f);
                    } else {
                        linkPreviewMaxWidth = AndroidUtilities.getMinTabletSide() - AndroidUtilities.dp(122.0f);
                    }
                } else if (messageObject.messageOwner.from_id <= 0 || ((this.currentMessageObject.messageOwner.to_id.channel_id == 0 && this.currentMessageObject.messageOwner.to_id.chat_id == 0) || this.currentMessageObject.isOutOwner())) {
                    linkPreviewMaxWidth = Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y) - AndroidUtilities.dp(80.0f);
                } else {
                    linkPreviewMaxWidth = Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y) - AndroidUtilities.dp(122.0f);
                }
                TL_webPage webPage = (TL_webPage) messageObject.messageOwner.media.webpage;
                if (!(webPage.site_name == null || webPage.photo == null || !webPage.site_name.toLowerCase().equals("instagram"))) {
                    linkPreviewMaxWidth = Math.max(AndroidUtilities.displaySize.y / 3, this.currentMessageObject.textWidth);
                }
                int additinalWidth = AndroidUtilities.dp(10.0f);
                int restLinesCount2 = 3;
                int additionalHeight = 0;
                linkPreviewMaxWidth -= additinalWidth;
                this.hasLinkPreview = true;
                if (this.currentMessageObject.photoThumbs == null && webPage.photo != null) {
                    this.currentMessageObject.generateThumbs(true);
                }
                z2 = (webPage.description == null || webPage.type == null || ((!webPage.type.equals("app") && !webPage.type.equals("profile") && !webPage.type.equals("article")) || this.currentMessageObject.photoThumbs == null)) ? false : true;
                this.isSmallImage = z2;
                if (webPage.site_name != null) {
                    try {
                        this.siteNameLayout = new StaticLayout(webPage.site_name, replyNamePaint, Math.min((int) Math.ceil((double) replyNamePaint.measureText(webPage.site_name)), linkPreviewMaxWidth), Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                        height = this.siteNameLayout.getLineBottom(this.siteNameLayout.getLineCount() - 1);
                        this.linkPreviewHeight += height;
                        this.totalHeight += height;
                        additionalHeight = 0 + height;
                        width = this.siteNameLayout.getWidth();
                        maxChildWidth = Math.max(maxChildWidth, width + additinalWidth);
                        maxWebWidth = Math.max(maxWebWidth, width + additinalWidth);
                    } catch (Throwable e2) {
                        FileLog.m611e("tmessages", e2);
                    }
                }
                boolean titleIsRTL = false;
                if (webPage.title != null) {
                    try {
                        this.titleX = 0;
                        if (this.linkPreviewHeight != 0) {
                            this.linkPreviewHeight += AndroidUtilities.dp(2.0f);
                            this.totalHeight += AndroidUtilities.dp(2.0f);
                        }
                        restLines = 0;
                        if (!this.isSmallImage || webPage.description == null) {
                            this.titleLayout = StaticLayoutEx.createStaticLayout(webPage.title, replyNamePaint, linkPreviewMaxWidth, Alignment.ALIGN_NORMAL, 1.0f, (float) AndroidUtilities.dp(1.0f), false, TruncateAt.END, linkPreviewMaxWidth, 4);
                            restLinesCount = 3;
                        } else {
                            restLines = 3;
                            this.titleLayout = generateStaticLayout(webPage.title, replyNamePaint, linkPreviewMaxWidth, linkPreviewMaxWidth - AndroidUtilities.dp(50.0f), 3, 4);
                            restLinesCount = 3 - this.titleLayout.getLineCount();
                        }
                        try {
                            height = this.titleLayout.getLineBottom(this.titleLayout.getLineCount() - 1);
                            this.linkPreviewHeight += height;
                            this.totalHeight += height;
                            for (a = 0; a < this.titleLayout.getLineCount(); a++) {
                                lineLeft = (int) this.titleLayout.getLineLeft(a);
                                if (lineLeft != 0) {
                                    titleIsRTL = true;
                                    if (this.titleX == 0) {
                                        this.titleX = -lineLeft;
                                    } else {
                                        this.titleX = Math.max(this.titleX, -lineLeft);
                                    }
                                }
                                if (lineLeft != 0) {
                                    width = this.titleLayout.getWidth() - lineLeft;
                                } else {
                                    width = (int) Math.ceil((double) this.titleLayout.getLineWidth(a));
                                }
                                if (a < restLines || (lineLeft != 0 && this.isSmallImage)) {
                                    width += AndroidUtilities.dp(50.0f);
                                }
                                maxChildWidth = Math.max(maxChildWidth, width + additinalWidth);
                                maxWebWidth = Math.max(maxWebWidth, width + additinalWidth);
                            }
                            restLinesCount2 = restLinesCount;
                        } catch (Exception e3) {
                            e2 = e3;
                        }
                    } catch (Exception e4) {
                        e2 = e4;
                        restLinesCount = 3;
                        FileLog.m611e("tmessages", e2);
                        restLinesCount2 = restLinesCount;
                        authorIsRTL = false;
                        if (webPage.author == null) {
                            restLinesCount = restLinesCount2;
                        } else {
                            try {
                                if (this.linkPreviewHeight != 0) {
                                    this.linkPreviewHeight += AndroidUtilities.dp(2.0f);
                                    this.totalHeight += AndroidUtilities.dp(2.0f);
                                }
                                if (restLinesCount2 == 3) {
                                }
                                this.authorLayout = generateStaticLayout(webPage.author, replyNamePaint, linkPreviewMaxWidth, linkPreviewMaxWidth - AndroidUtilities.dp(50.0f), restLinesCount2, 1);
                                restLinesCount = restLinesCount2 - this.authorLayout.getLineCount();
                                try {
                                    height = this.authorLayout.getLineBottom(this.authorLayout.getLineCount() - 1);
                                    this.linkPreviewHeight += height;
                                    this.totalHeight += height;
                                    lineLeft = (int) this.authorLayout.getLineLeft(0);
                                    this.authorX = -lineLeft;
                                    if (lineLeft == 0) {
                                        width = (int) Math.ceil((double) this.authorLayout.getLineWidth(0));
                                    } else {
                                        width = this.authorLayout.getWidth() - lineLeft;
                                        authorIsRTL = true;
                                    }
                                    maxChildWidth = Math.max(maxChildWidth, width + additinalWidth);
                                    maxWebWidth = Math.max(maxWebWidth, width + additinalWidth);
                                } catch (Exception e5) {
                                    e2 = e5;
                                    FileLog.m611e("tmessages", e2);
                                    if (webPage.description != null) {
                                        try {
                                            this.descriptionX = 0;
                                            this.currentMessageObject.generateLinkDescription();
                                            if (this.linkPreviewHeight != 0) {
                                                this.linkPreviewHeight += AndroidUtilities.dp(2.0f);
                                                this.totalHeight += AndroidUtilities.dp(2.0f);
                                            }
                                            restLines = 0;
                                            if (restLinesCount == 3) {
                                            }
                                            restLines = restLinesCount;
                                            this.descriptionLayout = generateStaticLayout(messageObject.linkDescription, replyTextPaint, linkPreviewMaxWidth, linkPreviewMaxWidth - AndroidUtilities.dp(50.0f), restLinesCount, 6);
                                            height = this.descriptionLayout.getLineBottom(this.descriptionLayout.getLineCount() - 1);
                                            this.linkPreviewHeight += height;
                                            this.totalHeight += height;
                                            hasRTL = false;
                                            for (a = 0; a < this.descriptionLayout.getLineCount(); a++) {
                                                lineLeft = (int) Math.ceil((double) this.descriptionLayout.getLineLeft(a));
                                                if (lineLeft == 0) {
                                                    hasRTL = true;
                                                    if (this.descriptionX != 0) {
                                                        this.descriptionX = -lineLeft;
                                                    } else {
                                                        this.descriptionX = Math.max(this.descriptionX, -lineLeft);
                                                    }
                                                }
                                            }
                                            while (a < this.descriptionLayout.getLineCount()) {
                                                lineLeft = (int) Math.ceil((double) this.descriptionLayout.getLineLeft(a));
                                                this.descriptionX = 0;
                                                if (lineLeft == 0) {
                                                    width = this.descriptionLayout.getWidth() - lineLeft;
                                                } else if (hasRTL) {
                                                    width = (int) Math.ceil((double) this.descriptionLayout.getLineWidth(a));
                                                } else {
                                                    width = this.descriptionLayout.getWidth();
                                                }
                                                width += AndroidUtilities.dp(50.0f);
                                                if (maxWebWidth >= width + additinalWidth) {
                                                    if (titleIsRTL) {
                                                        this.titleX += (width + additinalWidth) - maxWebWidth;
                                                    }
                                                    if (authorIsRTL) {
                                                        this.authorX += (width + additinalWidth) - maxWebWidth;
                                                    }
                                                    maxWebWidth = width + additinalWidth;
                                                }
                                                maxChildWidth = Math.max(maxChildWidth, width + additinalWidth);
                                            }
                                        } catch (Throwable e22) {
                                            FileLog.m611e("tmessages", e22);
                                        }
                                    }
                                    if (webPage.photo == null) {
                                        if (webPage.type == null) {
                                        }
                                        smallImage = false;
                                        this.isSmallImage = false;
                                        if (webPage.type == null) {
                                        }
                                        this.drawImageButton = z2;
                                        if (smallImage) {
                                            maxPhotoWidth = linkPreviewMaxWidth;
                                        } else {
                                            maxPhotoWidth = AndroidUtilities.dp(48.0f);
                                        }
                                        arrayList = messageObject.photoThumbs;
                                        if (this.drawImageButton) {
                                            i = maxPhotoWidth;
                                        } else {
                                            i = AndroidUtilities.getPhotoSize();
                                        }
                                        if (this.drawImageButton) {
                                            z = true;
                                        } else {
                                            z = false;
                                        }
                                        this.currentPhotoObject = FileLoader.getClosestPhotoSizeWithSize(arrayList, i, z);
                                        this.currentPhotoObjectThumb = FileLoader.getClosestPhotoSizeWithSize(messageObject.photoThumbs, 80);
                                        if (this.currentPhotoObjectThumb == this.currentPhotoObject) {
                                            this.currentPhotoObjectThumb = null;
                                        }
                                        if (this.currentPhotoObject != null) {
                                            if (this.linkPreviewHeight != 0) {
                                                this.linkPreviewHeight += AndroidUtilities.dp(2.0f);
                                                this.totalHeight += AndroidUtilities.dp(2.0f);
                                            }
                                            maxChildWidth = Math.max(maxChildWidth, maxPhotoWidth + additinalWidth);
                                            this.currentPhotoObject.size = -1;
                                            if (this.currentPhotoObjectThumb != null) {
                                                this.currentPhotoObjectThumb.size = -1;
                                            }
                                            if (smallImage) {
                                                width = this.currentPhotoObject.f139w;
                                                scale = ((float) width) / ((float) maxPhotoWidth);
                                                width = (int) (((float) width) / scale);
                                                height = (int) (((float) this.currentPhotoObject.f138h) / scale);
                                                height = AndroidUtilities.displaySize.y / 3;
                                            } else {
                                                height = maxPhotoWidth;
                                                width = maxPhotoWidth;
                                            }
                                            if (this.isSmallImage) {
                                                this.totalHeight += AndroidUtilities.dp(12.0f) + height;
                                                this.linkPreviewHeight += height;
                                            } else {
                                                if (AndroidUtilities.dp(50.0f) + additionalHeight > this.linkPreviewHeight) {
                                                    this.totalHeight += ((AndroidUtilities.dp(50.0f) + additionalHeight) - this.linkPreviewHeight) + AndroidUtilities.dp(8.0f);
                                                    this.linkPreviewHeight = AndroidUtilities.dp(50.0f) + additionalHeight;
                                                }
                                                this.linkPreviewHeight -= AndroidUtilities.dp(8.0f);
                                            }
                                            this.linkImageView.setImageCoords(0, 0, width, height);
                                            fileName = FileLoader.getAttachFileName(this.currentPhotoObject);
                                            photoExist = true;
                                            if (!FileLoader.getPathToAttach(this.currentPhotoObject, true).exists()) {
                                                photoExist = false;
                                            }
                                            this.currentPhotoFilter = String.format(Locale.US, "%d_%d", new Object[]{Integer.valueOf(width), Integer.valueOf(height)});
                                            this.currentPhotoFilterThumb = String.format(Locale.US, "%d_%d_b", new Object[]{Integer.valueOf(width), Integer.valueOf(height)});
                                            if (!photoExist) {
                                            }
                                            this.photoNotSet = false;
                                            this.linkImageView.setImage(this.currentPhotoObject.location, this.currentPhotoFilter, this.currentPhotoObjectThumb == null ? this.currentPhotoObjectThumb.location : null, this.currentPhotoFilterThumb, 0, null, false);
                                            this.drawLinkImageView = true;
                                            this.isInstagram = true;
                                            if (igvideoDrawable == null) {
                                                igvideoDrawable = getResources().getDrawable(C0553R.drawable.igvideo);
                                            }
                                        }
                                        if (durationPaint == null) {
                                            durationPaint = new TextPaint(1);
                                            durationPaint.setTextSize((float) AndroidUtilities.dp(12.0f));
                                            durationPaint.setColor(-1);
                                        }
                                        seconds = webPage.duration - ((webPage.duration / 60) * 60);
                                        str = String.format("%d:%02d", new Object[]{Integer.valueOf(webPage.duration / 60), Integer.valueOf(seconds)});
                                        this.durationWidth = (int) Math.ceil((double) durationPaint.measureText(str));
                                        this.durationLayout = new StaticLayout(str, durationPaint, this.durationWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                                    } else {
                                        this.linkImageView.setImageBitmap((Drawable) null);
                                        this.linkPreviewHeight -= AndroidUtilities.dp(6.0f);
                                        this.totalHeight += AndroidUtilities.dp(4.0f);
                                    }
                                    if (!this.hasLinkPreview) {
                                    }
                                    this.totalHeight += AndroidUtilities.dp(14.0f);
                                    this.backgroundWidth = Math.max(maxChildWidth, messageObject.lastLineWidth) + AndroidUtilities.dp(29.0f);
                                    updateButtonState(dataChanged);
                                }
                            } catch (Exception e6) {
                                e22 = e6;
                                restLinesCount = restLinesCount2;
                                FileLog.m611e("tmessages", e22);
                                if (webPage.description != null) {
                                    this.descriptionX = 0;
                                    this.currentMessageObject.generateLinkDescription();
                                    if (this.linkPreviewHeight != 0) {
                                        this.linkPreviewHeight += AndroidUtilities.dp(2.0f);
                                        this.totalHeight += AndroidUtilities.dp(2.0f);
                                    }
                                    restLines = 0;
                                    if (restLinesCount == 3) {
                                    }
                                    restLines = restLinesCount;
                                    this.descriptionLayout = generateStaticLayout(messageObject.linkDescription, replyTextPaint, linkPreviewMaxWidth, linkPreviewMaxWidth - AndroidUtilities.dp(50.0f), restLinesCount, 6);
                                    height = this.descriptionLayout.getLineBottom(this.descriptionLayout.getLineCount() - 1);
                                    this.linkPreviewHeight += height;
                                    this.totalHeight += height;
                                    hasRTL = false;
                                    for (a = 0; a < this.descriptionLayout.getLineCount(); a++) {
                                        lineLeft = (int) Math.ceil((double) this.descriptionLayout.getLineLeft(a));
                                        if (lineLeft == 0) {
                                            hasRTL = true;
                                            if (this.descriptionX != 0) {
                                                this.descriptionX = Math.max(this.descriptionX, -lineLeft);
                                            } else {
                                                this.descriptionX = -lineLeft;
                                            }
                                        }
                                    }
                                    while (a < this.descriptionLayout.getLineCount()) {
                                        lineLeft = (int) Math.ceil((double) this.descriptionLayout.getLineLeft(a));
                                        this.descriptionX = 0;
                                        if (lineLeft == 0) {
                                            width = this.descriptionLayout.getWidth() - lineLeft;
                                        } else if (hasRTL) {
                                            width = (int) Math.ceil((double) this.descriptionLayout.getLineWidth(a));
                                        } else {
                                            width = this.descriptionLayout.getWidth();
                                        }
                                        width += AndroidUtilities.dp(50.0f);
                                        if (maxWebWidth >= width + additinalWidth) {
                                            if (titleIsRTL) {
                                                this.titleX += (width + additinalWidth) - maxWebWidth;
                                            }
                                            if (authorIsRTL) {
                                                this.authorX += (width + additinalWidth) - maxWebWidth;
                                            }
                                            maxWebWidth = width + additinalWidth;
                                        }
                                        maxChildWidth = Math.max(maxChildWidth, width + additinalWidth);
                                    }
                                }
                                if (webPage.photo == null) {
                                    this.linkImageView.setImageBitmap((Drawable) null);
                                    this.linkPreviewHeight -= AndroidUtilities.dp(6.0f);
                                    this.totalHeight += AndroidUtilities.dp(4.0f);
                                } else {
                                    if (webPage.type == null) {
                                    }
                                    smallImage = false;
                                    this.isSmallImage = false;
                                    if (webPage.type == null) {
                                    }
                                    this.drawImageButton = z2;
                                    if (smallImage) {
                                        maxPhotoWidth = linkPreviewMaxWidth;
                                    } else {
                                        maxPhotoWidth = AndroidUtilities.dp(48.0f);
                                    }
                                    arrayList = messageObject.photoThumbs;
                                    if (this.drawImageButton) {
                                        i = maxPhotoWidth;
                                    } else {
                                        i = AndroidUtilities.getPhotoSize();
                                    }
                                    if (this.drawImageButton) {
                                        z = false;
                                    } else {
                                        z = true;
                                    }
                                    this.currentPhotoObject = FileLoader.getClosestPhotoSizeWithSize(arrayList, i, z);
                                    this.currentPhotoObjectThumb = FileLoader.getClosestPhotoSizeWithSize(messageObject.photoThumbs, 80);
                                    if (this.currentPhotoObjectThumb == this.currentPhotoObject) {
                                        this.currentPhotoObjectThumb = null;
                                    }
                                    if (this.currentPhotoObject != null) {
                                        if (this.linkPreviewHeight != 0) {
                                            this.linkPreviewHeight += AndroidUtilities.dp(2.0f);
                                            this.totalHeight += AndroidUtilities.dp(2.0f);
                                        }
                                        maxChildWidth = Math.max(maxChildWidth, maxPhotoWidth + additinalWidth);
                                        this.currentPhotoObject.size = -1;
                                        if (this.currentPhotoObjectThumb != null) {
                                            this.currentPhotoObjectThumb.size = -1;
                                        }
                                        if (smallImage) {
                                            width = this.currentPhotoObject.f139w;
                                            scale = ((float) width) / ((float) maxPhotoWidth);
                                            width = (int) (((float) width) / scale);
                                            height = (int) (((float) this.currentPhotoObject.f138h) / scale);
                                            height = AndroidUtilities.displaySize.y / 3;
                                        } else {
                                            height = maxPhotoWidth;
                                            width = maxPhotoWidth;
                                        }
                                        if (this.isSmallImage) {
                                            this.totalHeight += AndroidUtilities.dp(12.0f) + height;
                                            this.linkPreviewHeight += height;
                                        } else {
                                            if (AndroidUtilities.dp(50.0f) + additionalHeight > this.linkPreviewHeight) {
                                                this.totalHeight += ((AndroidUtilities.dp(50.0f) + additionalHeight) - this.linkPreviewHeight) + AndroidUtilities.dp(8.0f);
                                                this.linkPreviewHeight = AndroidUtilities.dp(50.0f) + additionalHeight;
                                            }
                                            this.linkPreviewHeight -= AndroidUtilities.dp(8.0f);
                                        }
                                        this.linkImageView.setImageCoords(0, 0, width, height);
                                        fileName = FileLoader.getAttachFileName(this.currentPhotoObject);
                                        photoExist = true;
                                        if (FileLoader.getPathToAttach(this.currentPhotoObject, true).exists()) {
                                            photoExist = false;
                                        }
                                        this.currentPhotoFilter = String.format(Locale.US, "%d_%d", new Object[]{Integer.valueOf(width), Integer.valueOf(height)});
                                        this.currentPhotoFilterThumb = String.format(Locale.US, "%d_%d_b", new Object[]{Integer.valueOf(width), Integer.valueOf(height)});
                                        if (photoExist) {
                                        }
                                        this.photoNotSet = false;
                                        if (this.currentPhotoObjectThumb == null) {
                                        }
                                        this.linkImageView.setImage(this.currentPhotoObject.location, this.currentPhotoFilter, this.currentPhotoObjectThumb == null ? this.currentPhotoObjectThumb.location : null, this.currentPhotoFilterThumb, 0, null, false);
                                        this.drawLinkImageView = true;
                                        this.isInstagram = true;
                                        if (igvideoDrawable == null) {
                                            igvideoDrawable = getResources().getDrawable(C0553R.drawable.igvideo);
                                        }
                                    }
                                    if (durationPaint == null) {
                                        durationPaint = new TextPaint(1);
                                        durationPaint.setTextSize((float) AndroidUtilities.dp(12.0f));
                                        durationPaint.setColor(-1);
                                    }
                                    seconds = webPage.duration - ((webPage.duration / 60) * 60);
                                    str = String.format("%d:%02d", new Object[]{Integer.valueOf(webPage.duration / 60), Integer.valueOf(seconds)});
                                    this.durationWidth = (int) Math.ceil((double) durationPaint.measureText(str));
                                    this.durationLayout = new StaticLayout(str, durationPaint, this.durationWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                                }
                                if (this.hasLinkPreview) {
                                }
                                this.totalHeight += AndroidUtilities.dp(14.0f);
                                this.backgroundWidth = Math.max(maxChildWidth, messageObject.lastLineWidth) + AndroidUtilities.dp(29.0f);
                                updateButtonState(dataChanged);
                            }
                        }
                        if (webPage.description != null) {
                            this.descriptionX = 0;
                            this.currentMessageObject.generateLinkDescription();
                            if (this.linkPreviewHeight != 0) {
                                this.linkPreviewHeight += AndroidUtilities.dp(2.0f);
                                this.totalHeight += AndroidUtilities.dp(2.0f);
                            }
                            restLines = 0;
                            if (restLinesCount == 3) {
                            }
                            restLines = restLinesCount;
                            this.descriptionLayout = generateStaticLayout(messageObject.linkDescription, replyTextPaint, linkPreviewMaxWidth, linkPreviewMaxWidth - AndroidUtilities.dp(50.0f), restLinesCount, 6);
                            height = this.descriptionLayout.getLineBottom(this.descriptionLayout.getLineCount() - 1);
                            this.linkPreviewHeight += height;
                            this.totalHeight += height;
                            hasRTL = false;
                            for (a = 0; a < this.descriptionLayout.getLineCount(); a++) {
                                lineLeft = (int) Math.ceil((double) this.descriptionLayout.getLineLeft(a));
                                if (lineLeft == 0) {
                                    hasRTL = true;
                                    if (this.descriptionX != 0) {
                                        this.descriptionX = Math.max(this.descriptionX, -lineLeft);
                                    } else {
                                        this.descriptionX = -lineLeft;
                                    }
                                }
                            }
                            while (a < this.descriptionLayout.getLineCount()) {
                                lineLeft = (int) Math.ceil((double) this.descriptionLayout.getLineLeft(a));
                                this.descriptionX = 0;
                                if (lineLeft == 0) {
                                    width = this.descriptionLayout.getWidth() - lineLeft;
                                } else if (hasRTL) {
                                    width = (int) Math.ceil((double) this.descriptionLayout.getLineWidth(a));
                                } else {
                                    width = this.descriptionLayout.getWidth();
                                }
                                width += AndroidUtilities.dp(50.0f);
                                if (maxWebWidth >= width + additinalWidth) {
                                    if (titleIsRTL) {
                                        this.titleX += (width + additinalWidth) - maxWebWidth;
                                    }
                                    if (authorIsRTL) {
                                        this.authorX += (width + additinalWidth) - maxWebWidth;
                                    }
                                    maxWebWidth = width + additinalWidth;
                                }
                                maxChildWidth = Math.max(maxChildWidth, width + additinalWidth);
                            }
                        }
                        if (webPage.photo == null) {
                            this.linkImageView.setImageBitmap((Drawable) null);
                            this.linkPreviewHeight -= AndroidUtilities.dp(6.0f);
                            this.totalHeight += AndroidUtilities.dp(4.0f);
                        } else {
                            if (webPage.type == null) {
                            }
                            smallImage = false;
                            this.isSmallImage = false;
                            if (webPage.type == null) {
                            }
                            this.drawImageButton = z2;
                            if (smallImage) {
                                maxPhotoWidth = linkPreviewMaxWidth;
                            } else {
                                maxPhotoWidth = AndroidUtilities.dp(48.0f);
                            }
                            arrayList = messageObject.photoThumbs;
                            if (this.drawImageButton) {
                                i = maxPhotoWidth;
                            } else {
                                i = AndroidUtilities.getPhotoSize();
                            }
                            if (this.drawImageButton) {
                                z = false;
                            } else {
                                z = true;
                            }
                            this.currentPhotoObject = FileLoader.getClosestPhotoSizeWithSize(arrayList, i, z);
                            this.currentPhotoObjectThumb = FileLoader.getClosestPhotoSizeWithSize(messageObject.photoThumbs, 80);
                            if (this.currentPhotoObjectThumb == this.currentPhotoObject) {
                                this.currentPhotoObjectThumb = null;
                            }
                            if (this.currentPhotoObject != null) {
                                if (this.linkPreviewHeight != 0) {
                                    this.linkPreviewHeight += AndroidUtilities.dp(2.0f);
                                    this.totalHeight += AndroidUtilities.dp(2.0f);
                                }
                                maxChildWidth = Math.max(maxChildWidth, maxPhotoWidth + additinalWidth);
                                this.currentPhotoObject.size = -1;
                                if (this.currentPhotoObjectThumb != null) {
                                    this.currentPhotoObjectThumb.size = -1;
                                }
                                if (smallImage) {
                                    width = this.currentPhotoObject.f139w;
                                    scale = ((float) width) / ((float) maxPhotoWidth);
                                    width = (int) (((float) width) / scale);
                                    height = (int) (((float) this.currentPhotoObject.f138h) / scale);
                                    height = AndroidUtilities.displaySize.y / 3;
                                } else {
                                    height = maxPhotoWidth;
                                    width = maxPhotoWidth;
                                }
                                if (this.isSmallImage) {
                                    this.totalHeight += AndroidUtilities.dp(12.0f) + height;
                                    this.linkPreviewHeight += height;
                                } else {
                                    if (AndroidUtilities.dp(50.0f) + additionalHeight > this.linkPreviewHeight) {
                                        this.totalHeight += ((AndroidUtilities.dp(50.0f) + additionalHeight) - this.linkPreviewHeight) + AndroidUtilities.dp(8.0f);
                                        this.linkPreviewHeight = AndroidUtilities.dp(50.0f) + additionalHeight;
                                    }
                                    this.linkPreviewHeight -= AndroidUtilities.dp(8.0f);
                                }
                                this.linkImageView.setImageCoords(0, 0, width, height);
                                fileName = FileLoader.getAttachFileName(this.currentPhotoObject);
                                photoExist = true;
                                if (FileLoader.getPathToAttach(this.currentPhotoObject, true).exists()) {
                                    photoExist = false;
                                }
                                this.currentPhotoFilter = String.format(Locale.US, "%d_%d", new Object[]{Integer.valueOf(width), Integer.valueOf(height)});
                                this.currentPhotoFilterThumb = String.format(Locale.US, "%d_%d_b", new Object[]{Integer.valueOf(width), Integer.valueOf(height)});
                                if (photoExist) {
                                }
                                this.photoNotSet = false;
                                if (this.currentPhotoObjectThumb == null) {
                                }
                                this.linkImageView.setImage(this.currentPhotoObject.location, this.currentPhotoFilter, this.currentPhotoObjectThumb == null ? this.currentPhotoObjectThumb.location : null, this.currentPhotoFilterThumb, 0, null, false);
                                this.drawLinkImageView = true;
                                this.isInstagram = true;
                                if (igvideoDrawable == null) {
                                    igvideoDrawable = getResources().getDrawable(C0553R.drawable.igvideo);
                                }
                            }
                            if (durationPaint == null) {
                                durationPaint = new TextPaint(1);
                                durationPaint.setTextSize((float) AndroidUtilities.dp(12.0f));
                                durationPaint.setColor(-1);
                            }
                            seconds = webPage.duration - ((webPage.duration / 60) * 60);
                            str = String.format("%d:%02d", new Object[]{Integer.valueOf(webPage.duration / 60), Integer.valueOf(seconds)});
                            this.durationWidth = (int) Math.ceil((double) durationPaint.measureText(str));
                            this.durationLayout = new StaticLayout(str, durationPaint, this.durationWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                        }
                        if (this.hasLinkPreview) {
                        }
                        this.totalHeight += AndroidUtilities.dp(14.0f);
                        this.backgroundWidth = Math.max(maxChildWidth, messageObject.lastLineWidth) + AndroidUtilities.dp(29.0f);
                        updateButtonState(dataChanged);
                    }
                }
                authorIsRTL = false;
                if (webPage.author == null) {
                    if (this.linkPreviewHeight != 0) {
                        this.linkPreviewHeight += AndroidUtilities.dp(2.0f);
                        this.totalHeight += AndroidUtilities.dp(2.0f);
                    }
                    if (restLinesCount2 == 3 || (this.isSmallImage && webPage.description != null)) {
                        this.authorLayout = generateStaticLayout(webPage.author, replyNamePaint, linkPreviewMaxWidth, linkPreviewMaxWidth - AndroidUtilities.dp(50.0f), restLinesCount2, 1);
                        restLinesCount = restLinesCount2 - this.authorLayout.getLineCount();
                    } else {
                        this.authorLayout = new StaticLayout(webPage.author, replyNamePaint, linkPreviewMaxWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                        restLinesCount = restLinesCount2;
                    }
                    height = this.authorLayout.getLineBottom(this.authorLayout.getLineCount() - 1);
                    this.linkPreviewHeight += height;
                    this.totalHeight += height;
                    lineLeft = (int) this.authorLayout.getLineLeft(0);
                    this.authorX = -lineLeft;
                    if (lineLeft == 0) {
                        width = this.authorLayout.getWidth() - lineLeft;
                        authorIsRTL = true;
                    } else {
                        width = (int) Math.ceil((double) this.authorLayout.getLineWidth(0));
                    }
                    maxChildWidth = Math.max(maxChildWidth, width + additinalWidth);
                    maxWebWidth = Math.max(maxWebWidth, width + additinalWidth);
                } else {
                    restLinesCount = restLinesCount2;
                }
                if (webPage.description != null) {
                    this.descriptionX = 0;
                    this.currentMessageObject.generateLinkDescription();
                    if (this.linkPreviewHeight != 0) {
                        this.linkPreviewHeight += AndroidUtilities.dp(2.0f);
                        this.totalHeight += AndroidUtilities.dp(2.0f);
                    }
                    restLines = 0;
                    if (restLinesCount == 3 || this.isSmallImage) {
                        restLines = restLinesCount;
                        this.descriptionLayout = generateStaticLayout(messageObject.linkDescription, replyTextPaint, linkPreviewMaxWidth, linkPreviewMaxWidth - AndroidUtilities.dp(50.0f), restLinesCount, 6);
                    } else {
                        this.descriptionLayout = StaticLayoutEx.createStaticLayout(messageObject.linkDescription, replyTextPaint, linkPreviewMaxWidth, Alignment.ALIGN_NORMAL, 1.0f, (float) AndroidUtilities.dp(1.0f), false, TruncateAt.END, linkPreviewMaxWidth, 6);
                    }
                    height = this.descriptionLayout.getLineBottom(this.descriptionLayout.getLineCount() - 1);
                    this.linkPreviewHeight += height;
                    this.totalHeight += height;
                    hasRTL = false;
                    for (a = 0; a < this.descriptionLayout.getLineCount(); a++) {
                        lineLeft = (int) Math.ceil((double) this.descriptionLayout.getLineLeft(a));
                        if (lineLeft == 0) {
                            hasRTL = true;
                            if (this.descriptionX != 0) {
                                this.descriptionX = -lineLeft;
                            } else {
                                this.descriptionX = Math.max(this.descriptionX, -lineLeft);
                            }
                        }
                    }
                    for (a = 0; a < this.descriptionLayout.getLineCount(); a++) {
                        lineLeft = (int) Math.ceil((double) this.descriptionLayout.getLineLeft(a));
                        if (lineLeft == 0 && this.descriptionX != 0) {
                            this.descriptionX = 0;
                        }
                        if (lineLeft == 0) {
                            width = this.descriptionLayout.getWidth() - lineLeft;
                        } else if (hasRTL) {
                            width = this.descriptionLayout.getWidth();
                        } else {
                            width = (int) Math.ceil((double) this.descriptionLayout.getLineWidth(a));
                        }
                        if (a < restLines || (lineLeft != 0 && this.isSmallImage)) {
                            width += AndroidUtilities.dp(50.0f);
                        }
                        if (maxWebWidth >= width + additinalWidth) {
                            if (titleIsRTL) {
                                this.titleX += (width + additinalWidth) - maxWebWidth;
                            }
                            if (authorIsRTL) {
                                this.authorX += (width + additinalWidth) - maxWebWidth;
                            }
                            maxWebWidth = width + additinalWidth;
                        }
                        maxChildWidth = Math.max(maxChildWidth, width + additinalWidth);
                    }
                }
                if (webPage.photo == null) {
                    smallImage = webPage.type == null && (webPage.type.equals("app") || webPage.type.equals("profile") || webPage.type.equals("article"));
                    if (smallImage && (this.descriptionLayout == null || (this.descriptionLayout != null && this.descriptionLayout.getLineCount() == 1))) {
                        smallImage = false;
                        this.isSmallImage = false;
                    }
                    z2 = webPage.type == null && webPage.type.equals("photo");
                    this.drawImageButton = z2;
                    if (smallImage) {
                        maxPhotoWidth = AndroidUtilities.dp(48.0f);
                    } else {
                        maxPhotoWidth = linkPreviewMaxWidth;
                    }
                    arrayList = messageObject.photoThumbs;
                    if (this.drawImageButton) {
                        i = AndroidUtilities.getPhotoSize();
                    } else {
                        i = maxPhotoWidth;
                    }
                    if (this.drawImageButton) {
                        z = true;
                    } else {
                        z = false;
                    }
                    this.currentPhotoObject = FileLoader.getClosestPhotoSizeWithSize(arrayList, i, z);
                    this.currentPhotoObjectThumb = FileLoader.getClosestPhotoSizeWithSize(messageObject.photoThumbs, 80);
                    if (this.currentPhotoObjectThumb == this.currentPhotoObject) {
                        this.currentPhotoObjectThumb = null;
                    }
                    if (this.currentPhotoObject != null) {
                        if (this.linkPreviewHeight != 0) {
                            this.linkPreviewHeight += AndroidUtilities.dp(2.0f);
                            this.totalHeight += AndroidUtilities.dp(2.0f);
                        }
                        maxChildWidth = Math.max(maxChildWidth, maxPhotoWidth + additinalWidth);
                        this.currentPhotoObject.size = -1;
                        if (this.currentPhotoObjectThumb != null) {
                            this.currentPhotoObjectThumb.size = -1;
                        }
                        if (smallImage) {
                            height = maxPhotoWidth;
                            width = maxPhotoWidth;
                        } else {
                            width = this.currentPhotoObject.f139w;
                            scale = ((float) width) / ((float) maxPhotoWidth);
                            width = (int) (((float) width) / scale);
                            height = (int) (((float) this.currentPhotoObject.f138h) / scale);
                            if (!(webPage.site_name == null || webPage.site_name.toLowerCase().equals("instagram") || height <= AndroidUtilities.displaySize.y / 3)) {
                                height = AndroidUtilities.displaySize.y / 3;
                            }
                        }
                        if (this.isSmallImage) {
                            if (AndroidUtilities.dp(50.0f) + additionalHeight > this.linkPreviewHeight) {
                                this.totalHeight += ((AndroidUtilities.dp(50.0f) + additionalHeight) - this.linkPreviewHeight) + AndroidUtilities.dp(8.0f);
                                this.linkPreviewHeight = AndroidUtilities.dp(50.0f) + additionalHeight;
                            }
                            this.linkPreviewHeight -= AndroidUtilities.dp(8.0f);
                        } else {
                            this.totalHeight += AndroidUtilities.dp(12.0f) + height;
                            this.linkPreviewHeight += height;
                        }
                        this.linkImageView.setImageCoords(0, 0, width, height);
                        fileName = FileLoader.getAttachFileName(this.currentPhotoObject);
                        photoExist = true;
                        if (FileLoader.getPathToAttach(this.currentPhotoObject, true).exists()) {
                            photoExist = false;
                        }
                        this.currentPhotoFilter = String.format(Locale.US, "%d_%d", new Object[]{Integer.valueOf(width), Integer.valueOf(height)});
                        this.currentPhotoFilterThumb = String.format(Locale.US, "%d_%d_b", new Object[]{Integer.valueOf(width), Integer.valueOf(height)});
                        if (photoExist || MediaController.getInstance().canDownloadMedia(1) || FileLoader.getInstance().isLoadingFile(fileName)) {
                            this.photoNotSet = false;
                            if (this.currentPhotoObjectThumb == null) {
                            }
                            this.linkImageView.setImage(this.currentPhotoObject.location, this.currentPhotoFilter, this.currentPhotoObjectThumb == null ? this.currentPhotoObjectThumb.location : null, this.currentPhotoFilterThumb, 0, null, false);
                        } else {
                            this.photoNotSet = true;
                            if (this.currentPhotoObjectThumb != null) {
                                this.linkImageView.setImage(null, null, this.currentPhotoObjectThumb.location, String.format(Locale.US, "%d_%d_b", new Object[]{Integer.valueOf(width), Integer.valueOf(height)}), 0, null, false);
                            } else {
                                this.linkImageView.setImageBitmap((Drawable) null);
                            }
                        }
                        this.drawLinkImageView = true;
                        if (webPage.site_name != null && webPage.site_name.toLowerCase().equals("instagram") && webPage.type != null && webPage.type.equals("video")) {
                            this.isInstagram = true;
                            if (igvideoDrawable == null) {
                                igvideoDrawable = getResources().getDrawable(C0553R.drawable.igvideo);
                            }
                        }
                    }
                    if (!(webPage.type == null || !webPage.type.equals("video") || webPage.duration == 0)) {
                        if (durationPaint == null) {
                            durationPaint = new TextPaint(1);
                            durationPaint.setTextSize((float) AndroidUtilities.dp(12.0f));
                            durationPaint.setColor(-1);
                        }
                        seconds = webPage.duration - ((webPage.duration / 60) * 60);
                        str = String.format("%d:%02d", new Object[]{Integer.valueOf(webPage.duration / 60), Integer.valueOf(seconds)});
                        this.durationWidth = (int) Math.ceil((double) durationPaint.measureText(str));
                        this.durationLayout = new StaticLayout(str, durationPaint, this.durationWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                    }
                } else {
                    this.linkImageView.setImageBitmap((Drawable) null);
                    this.linkPreviewHeight -= AndroidUtilities.dp(6.0f);
                    this.totalHeight += AndroidUtilities.dp(4.0f);
                }
            } else {
                this.linkImageView.setImageBitmap((Drawable) null);
            }
            if (this.hasLinkPreview || maxWidth - messageObject.lastLineWidth < timeMore) {
                this.totalHeight += AndroidUtilities.dp(14.0f);
                this.backgroundWidth = Math.max(maxChildWidth, messageObject.lastLineWidth) + AndroidUtilities.dp(29.0f);
            } else {
                int diff = maxChildWidth - messageObject.lastLineWidth;
                if (diff < 0 || diff > timeMore) {
                    this.backgroundWidth = Math.max(maxChildWidth, messageObject.lastLineWidth + timeMore) + AndroidUtilities.dp(29.0f);
                } else {
                    this.backgroundWidth = ((maxChildWidth + timeMore) - diff) + AndroidUtilities.dp(29.0f);
                }
            }
        }
        updateButtonState(dataChanged);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), this.totalHeight);
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (this.currentMessageObject.isOutOwner()) {
            this.textX = (this.layoutWidth - this.backgroundWidth) + AndroidUtilities.dp(10.0f);
            this.textY = AndroidUtilities.dp(10.0f) + this.namesOffset;
            return;
        }
        int dp = AndroidUtilities.dp(19.0f);
        int dp2 = (!this.isChat || this.currentMessageObject.messageOwner.from_id <= 0) ? 0 : AndroidUtilities.dp(52.0f);
        this.textX = dp2 + dp;
        this.textY = AndroidUtilities.dp(10.0f) + this.namesOffset;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.currentMessageObject != null && this.currentMessageObject.textLayoutBlocks != null && !this.currentMessageObject.textLayoutBlocks.isEmpty()) {
            if (this.currentMessageObject.isOutOwner()) {
                this.textX = (this.layoutWidth - this.backgroundWidth) + AndroidUtilities.dp(10.0f);
                this.textY = AndroidUtilities.dp(10.0f) + this.namesOffset;
            } else {
                int dp = AndroidUtilities.dp(19.0f);
                int dp2 = (!this.isChat || this.currentMessageObject.messageOwner.from_id <= 0) ? 0 : AndroidUtilities.dp(52.0f);
                this.textX = dp2 + dp;
                this.textY = AndroidUtilities.dp(10.0f) + this.namesOffset;
            }
            if (this.firstVisibleBlockNum >= 0) {
                int a = this.firstVisibleBlockNum;
                while (a <= this.lastVisibleBlockNum && a < this.currentMessageObject.textLayoutBlocks.size()) {
                    TextLayoutBlock block = (TextLayoutBlock) this.currentMessageObject.textLayoutBlocks.get(a);
                    canvas.save();
                    canvas.translate((float) (this.textX - ((int) Math.ceil((double) block.textXOffset))), ((float) this.textY) + block.textYOffset);
                    if (this.pressedLink != null && a == this.linkBlockNum) {
                        canvas.drawPath(this.urlPath, urlPaint);
                    }
                    try {
                        block.textLayout.draw(canvas);
                    } catch (Throwable e) {
                        FileLog.m611e("tmessages", e);
                    }
                    canvas.restore();
                    a++;
                }
            }
            if (this.hasLinkPreview) {
                int startY = (this.textY + this.currentMessageObject.textHeight) + AndroidUtilities.dp(8.0f);
                int linkPreviewY = startY;
                int smallImageStartY = 0;
                replyLinePaint.setColor(this.currentMessageObject.isOutOwner() ? -7485062 : -9658414);
                canvas.drawRect((float) this.textX, (float) (linkPreviewY - AndroidUtilities.dp(3.0f)), (float) (this.textX + AndroidUtilities.dp(2.0f)), (float) ((this.linkPreviewHeight + linkPreviewY) + AndroidUtilities.dp(3.0f)), replyLinePaint);
                if (this.siteNameLayout != null) {
                    replyNamePaint.setColor(this.currentMessageObject.isOutOwner() ? -9391780 : -11824689);
                    canvas.save();
                    canvas.translate((float) (this.textX + AndroidUtilities.dp(10.0f)), (float) (linkPreviewY - AndroidUtilities.dp(3.0f)));
                    this.siteNameLayout.draw(canvas);
                    canvas.restore();
                    linkPreviewY += this.siteNameLayout.getLineBottom(this.siteNameLayout.getLineCount() - 1);
                }
                if (this.titleLayout != null) {
                    if (linkPreviewY != startY) {
                        linkPreviewY += AndroidUtilities.dp(2.0f);
                    }
                    replyNamePaint.setColor(ViewCompat.MEASURED_STATE_MASK);
                    smallImageStartY = linkPreviewY - AndroidUtilities.dp(1.0f);
                    canvas.save();
                    canvas.translate((float) ((this.textX + AndroidUtilities.dp(10.0f)) + this.titleX), (float) (linkPreviewY - AndroidUtilities.dp(3.0f)));
                    this.titleLayout.draw(canvas);
                    canvas.restore();
                    linkPreviewY += this.titleLayout.getLineBottom(this.titleLayout.getLineCount() - 1);
                }
                if (this.authorLayout != null) {
                    if (linkPreviewY != startY) {
                        linkPreviewY += AndroidUtilities.dp(2.0f);
                    }
                    if (smallImageStartY == 0) {
                        smallImageStartY = linkPreviewY - AndroidUtilities.dp(1.0f);
                    }
                    replyNamePaint.setColor(ViewCompat.MEASURED_STATE_MASK);
                    canvas.save();
                    canvas.translate((float) ((this.textX + AndroidUtilities.dp(10.0f)) + this.authorX), (float) (linkPreviewY - AndroidUtilities.dp(3.0f)));
                    this.authorLayout.draw(canvas);
                    canvas.restore();
                    linkPreviewY += this.authorLayout.getLineBottom(this.authorLayout.getLineCount() - 1);
                }
                if (this.descriptionLayout != null) {
                    if (linkPreviewY != startY) {
                        linkPreviewY += AndroidUtilities.dp(2.0f);
                    }
                    if (smallImageStartY == 0) {
                        smallImageStartY = linkPreviewY - AndroidUtilities.dp(1.0f);
                    }
                    replyTextPaint.setColor(ViewCompat.MEASURED_STATE_MASK);
                    this.descriptionY = linkPreviewY - AndroidUtilities.dp(3.0f);
                    canvas.save();
                    canvas.translate((float) ((this.textX + AndroidUtilities.dp(10.0f)) + this.descriptionX), (float) this.descriptionY);
                    if (this.pressedLink != null && this.linkBlockNum == -10) {
                        canvas.drawPath(this.urlPath, urlPaint);
                    }
                    this.descriptionLayout.draw(canvas);
                    canvas.restore();
                    linkPreviewY += this.descriptionLayout.getLineBottom(this.descriptionLayout.getLineCount() - 1);
                }
                if (this.drawLinkImageView) {
                    int x;
                    int y;
                    if (linkPreviewY != startY) {
                        linkPreviewY += AndroidUtilities.dp(2.0f);
                    }
                    if (this.isSmallImage) {
                        this.linkImageView.setImageCoords((this.textX + this.backgroundWidth) - AndroidUtilities.dp(77.0f), smallImageStartY, this.linkImageView.getImageWidth(), this.linkImageView.getImageHeight());
                    } else {
                        this.linkImageView.setImageCoords(this.textX + AndroidUtilities.dp(10.0f), linkPreviewY, this.linkImageView.getImageWidth(), this.linkImageView.getImageHeight());
                        if (this.drawImageButton) {
                            int size = AndroidUtilities.dp(48.0f);
                            this.buttonX = (int) (((float) this.linkImageView.getImageX()) + (((float) (this.linkImageView.getImageWidth() - size)) / 2.0f));
                            this.buttonY = (int) (((float) this.linkImageView.getImageY()) + (((float) (this.linkImageView.getImageHeight() - size)) / 2.0f));
                            this.radialProgress.setProgressRect(this.buttonX, this.buttonY, this.buttonX + AndroidUtilities.dp(48.0f), this.buttonY + AndroidUtilities.dp(48.0f));
                        }
                    }
                    this.linkImageView.draw(canvas);
                    if (this.drawImageButton) {
                        this.radialProgress.draw(canvas);
                    }
                    if (this.isInstagram && igvideoDrawable != null) {
                        x = ((this.linkImageView.getImageX() + this.linkImageView.getImageWidth()) - igvideoDrawable.getIntrinsicWidth()) - AndroidUtilities.dp(4.0f);
                        y = this.linkImageView.getImageY() + AndroidUtilities.dp(4.0f);
                        igvideoDrawable.setBounds(x, y, igvideoDrawable.getIntrinsicWidth() + x, igvideoDrawable.getIntrinsicHeight() + y);
                        igvideoDrawable.draw(canvas);
                    }
                    if (this.durationLayout != null) {
                        x = ((this.linkImageView.getImageX() + this.linkImageView.getImageWidth()) - AndroidUtilities.dp(8.0f)) - this.durationWidth;
                        y = (this.linkImageView.getImageY() + this.linkImageView.getImageHeight()) - AndroidUtilities.dp(19.0f);
                        ResourceLoader.mediaBackgroundDrawable.setBounds(x - AndroidUtilities.dp(4.0f), y - AndroidUtilities.dp(1.5f), (this.durationWidth + x) + AndroidUtilities.dp(4.0f), AndroidUtilities.dp(14.5f) + y);
                        ResourceLoader.mediaBackgroundDrawable.draw(canvas);
                        canvas.save();
                        canvas.translate((float) x, (float) y);
                        this.durationLayout.draw(canvas);
                        canvas.restore();
                    }
                }
            }
        }
    }

    private Drawable getDrawableForCurrentState() {
        if (this.buttonState < 0 || this.buttonState >= 4) {
            return null;
        }
        if (this.buttonState == 1) {
            return ResourceLoader.buttonStatesDrawables[4];
        }
        return ResourceLoader.buttonStatesDrawables[this.buttonState];
    }

    public void updateButtonState(boolean animated) {
        if (this.currentPhotoObject != null && this.drawImageButton) {
            String fileName = FileLoader.getAttachFileName(this.currentPhotoObject);
            File cacheFile = FileLoader.getPathToAttach(this.currentPhotoObject, true);
            if (fileName == null) {
                this.radialProgress.setBackground(null, false, false);
            } else if (cacheFile.exists()) {
                MediaController.getInstance().removeLoadingFileObserver(this);
                this.buttonState = -1;
                this.radialProgress.setBackground(getDrawableForCurrentState(), false, animated);
                invalidate();
            } else {
                MediaController.getInstance().addLoadingFileObserver(fileName, this);
                float setProgress = 0.0f;
                boolean progressVisible = false;
                if (FileLoader.getInstance().isLoadingFile(fileName)) {
                    progressVisible = true;
                    this.buttonState = 1;
                    Float progress = ImageLoader.getInstance().getFileProgress(fileName);
                    setProgress = progress != null ? progress.floatValue() : 0.0f;
                } else if (this.cancelLoading || !MediaController.getInstance().canDownloadMedia(1)) {
                    this.buttonState = 0;
                } else {
                    progressVisible = true;
                    this.buttonState = 1;
                }
                this.radialProgress.setProgress(setProgress, false);
                this.radialProgress.setBackground(getDrawableForCurrentState(), progressVisible, animated);
                invalidate();
            }
        }
    }

    private void didPressedButton(boolean animated) {
        if (this.buttonState == 0) {
            this.cancelLoading = false;
            this.radialProgress.setProgress(0.0f, false);
            this.linkImageView.setImage(this.currentPhotoObject.location, this.currentPhotoFilter, this.currentPhotoObjectThumb != null ? this.currentPhotoObjectThumb.location : null, this.currentPhotoFilterThumb, 0, null, false);
            this.buttonState = 1;
            this.radialProgress.setBackground(getDrawableForCurrentState(), true, animated);
            invalidate();
        } else if (this.buttonState != 1) {
        } else {
            if (!this.currentMessageObject.isOut() || !this.currentMessageObject.isSending()) {
                this.cancelLoading = true;
                this.linkImageView.cancelLoadImage();
                this.buttonState = 0;
                this.radialProgress.setBackground(getDrawableForCurrentState(), false, animated);
                invalidate();
            } else if (this.delegate != null) {
                this.delegate.didPressedCancelSendButton(this);
            }
        }
    }

    public void onFailedDownload(String fileName) {
        updateButtonState(false);
    }

    public void onSuccessDownload(String fileName) {
        this.radialProgress.setProgress(1.0f, true);
        if (this.photoNotSet) {
            setMessageObject(this.currentMessageObject);
        } else {
            updateButtonState(true);
        }
    }

    public void onProgressDownload(String fileName, float progress) {
        this.radialProgress.setProgress(progress, true);
        if (this.buttonState != 1) {
            updateButtonState(false);
        }
    }

    public void onProvideStructure(ViewStructure structure) {
        super.onProvideStructure(structure);
        if (this.allowAssistant && VERSION.SDK_INT >= 23) {
            structure.setText(this.currentMessageObject.messageText);
        }
    }
}
