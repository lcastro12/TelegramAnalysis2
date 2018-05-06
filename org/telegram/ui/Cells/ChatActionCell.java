package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.style.URLSpan;
import android.view.MotionEvent;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.TL_messageActionUserUpdatedPhoto;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.ResourceLoader;
import org.telegram.ui.PhotoViewer;

public class ChatActionCell extends BaseCell {
    private static TextPaint textPaint;
    private AvatarDrawable avatarDrawable;
    private MessageObject currentMessageObject;
    private ChatActionCellDelegate delegate;
    private boolean imagePressed = false;
    private ImageReceiver imageReceiver;
    private URLSpan pressedLink;
    private int previousWidth = 0;
    private int textHeight = 0;
    private StaticLayout textLayout;
    private int textWidth = 0;
    private int textX = 0;
    private int textXLeft = 0;
    private int textY = 0;

    public interface ChatActionCellDelegate {
        void didClickedImage(ChatActionCell chatActionCell);

        void didLongPressed(ChatActionCell chatActionCell);

        void needOpenUserProfile(int i);
    }

    public ChatActionCell(Context context) {
        super(context);
        if (textPaint == null) {
            textPaint = new TextPaint(1);
            textPaint.setColor(-1);
            textPaint.linkColor = -1;
        }
        this.imageReceiver = new ImageReceiver(this);
        this.imageReceiver.setRoundRadius(AndroidUtilities.dp(32.0f));
        this.avatarDrawable = new AvatarDrawable();
        textPaint.setTextSize((float) AndroidUtilities.dp((float) MessagesController.getInstance().fontSize));
    }

    public void setDelegate(ChatActionCellDelegate delegate) {
        this.delegate = delegate;
    }

    public void setMessageObject(MessageObject messageObject) {
        if (this.currentMessageObject != messageObject) {
            this.currentMessageObject = messageObject;
            this.previousWidth = 0;
            if (this.currentMessageObject.type == 11) {
                boolean z;
                int id = 0;
                if (messageObject.messageOwner.to_id != null) {
                    if (messageObject.messageOwner.to_id.chat_id != 0) {
                        id = messageObject.messageOwner.to_id.chat_id;
                    } else if (messageObject.messageOwner.to_id.channel_id != 0) {
                        id = messageObject.messageOwner.to_id.channel_id;
                    } else {
                        id = messageObject.messageOwner.to_id.user_id;
                        if (id == UserConfig.getClientUserId()) {
                            id = messageObject.messageOwner.from_id;
                        }
                    }
                }
                this.avatarDrawable.setInfo(id, null, null, false);
                if (this.currentMessageObject.messageOwner.action instanceof TL_messageActionUserUpdatedPhoto) {
                    this.imageReceiver.setImage(this.currentMessageObject.messageOwner.action.newUserPhoto.photo_small, "50_50", this.avatarDrawable, null, false);
                } else {
                    PhotoSize photo = FileLoader.getClosestPhotoSizeWithSize(this.currentMessageObject.photoThumbs, AndroidUtilities.dp(64.0f));
                    if (photo != null) {
                        this.imageReceiver.setImage(photo.location, "50_50", this.avatarDrawable, null, false);
                    } else {
                        this.imageReceiver.setImageBitmap(this.avatarDrawable);
                    }
                }
                ImageReceiver imageReceiver = this.imageReceiver;
                if (PhotoViewer.getInstance().isShowingImage(this.currentMessageObject)) {
                    z = false;
                } else {
                    z = true;
                }
                imageReceiver.setVisible(z, false);
            } else {
                this.imageReceiver.setImageBitmap((Bitmap) null);
            }
            requestLayout();
        }
    }

    public MessageObject getMessageObject() {
        return this.currentMessageObject;
    }

    public ImageReceiver getPhotoImage() {
        return this.imageReceiver;
    }

    protected void onLongPress() {
        if (this.delegate != null) {
            this.delegate.didLongPressed(this);
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        boolean result = false;
        if (event.getAction() != 0) {
            if (event.getAction() != 2) {
                cancelCheckLongPress();
            }
            if (this.imagePressed) {
                if (event.getAction() == 1) {
                    this.imagePressed = false;
                    if (this.delegate != null) {
                        this.delegate.didClickedImage(this);
                        playSoundEffect(0);
                    }
                } else if (event.getAction() == 3) {
                    this.imagePressed = false;
                } else if (event.getAction() == 2 && !this.imageReceiver.isInsideImage(x, y)) {
                    this.imagePressed = false;
                }
            }
        } else if (this.delegate != null) {
            if (this.currentMessageObject.type == 11 && this.imageReceiver.isInsideImage(x, y)) {
                this.imagePressed = true;
                result = true;
            }
            if (result) {
                startCheckLongPress();
            }
        }
        if (!result && (event.getAction() == 0 || (this.pressedLink != null && event.getAction() == 1))) {
            if (x < ((float) this.textX) || y < ((float) this.textY) || x > ((float) (this.textX + this.textWidth)) || y > ((float) (this.textY + this.textHeight))) {
                this.pressedLink = null;
            } else {
                x -= (float) this.textXLeft;
                int line = this.textLayout.getLineForVertical((int) (y - ((float) this.textY)));
                int off = this.textLayout.getOffsetForHorizontal(line, x);
                float left = this.textLayout.getLineLeft(line);
                if (left > x || this.textLayout.getLineWidth(line) + left < x || !(this.currentMessageObject.messageText instanceof Spannable)) {
                    this.pressedLink = null;
                } else {
                    URLSpan[] link = (URLSpan[]) this.currentMessageObject.messageText.getSpans(off, off, URLSpan.class);
                    if (link.length == 0) {
                        this.pressedLink = null;
                    } else if (event.getAction() == 0) {
                        this.pressedLink = link[0];
                        result = true;
                    } else if (link[0] == this.pressedLink) {
                        if (this.delegate != null) {
                            this.delegate.needOpenUserProfile(Integer.parseInt(link[0].getURL()));
                        }
                        result = true;
                    }
                }
            }
        }
        if (result) {
            return result;
        }
        return super.onTouchEvent(event);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void onMeasure(int r14, int r15) {
        /*
        r13 = this;
        r0 = r13.currentMessageObject;
        if (r0 != 0) goto L_0x0015;
    L_0x0004:
        r0 = android.view.View.MeasureSpec.getSize(r14);
        r1 = r13.textHeight;
        r2 = 1096810496; // 0x41600000 float:14.0 double:5.41896386E-315;
        r2 = org.telegram.messenger.AndroidUtilities.dp(r2);
        r1 = r1 + r2;
        r13.setMeasuredDimension(r0, r1);
    L_0x0014:
        return;
    L_0x0015:
        r0 = 1106247680; // 0x41f00000 float:30.0 double:5.465589745E-315;
        r0 = org.telegram.messenger.AndroidUtilities.dp(r0);
        r1 = android.view.View.MeasureSpec.getSize(r14);
        r12 = java.lang.Math.max(r0, r1);
        r0 = r13.previousWidth;
        if (r12 == r0) goto L_0x00d5;
    L_0x0027:
        r13.previousWidth = r12;
        r0 = new android.text.StaticLayout;
        r1 = r13.currentMessageObject;
        r1 = r1.messageText;
        r2 = textPaint;
        r3 = 1106247680; // 0x41f00000 float:30.0 double:5.465589745E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r3 = r12 - r3;
        r4 = android.text.Layout.Alignment.ALIGN_CENTER;
        r5 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r6 = 0;
        r7 = 0;
        r0.<init>(r1, r2, r3, r4, r5, r6, r7);
        r13.textLayout = r0;
        r0 = 0;
        r13.textHeight = r0;
        r0 = 0;
        r13.textWidth = r0;
        r0 = r13.textLayout;	 Catch:{ Exception -> 0x0087 }
        r11 = r0.getLineCount();	 Catch:{ Exception -> 0x0087 }
        r8 = 0;
    L_0x0051:
        if (r8 >= r11) goto L_0x008d;
    L_0x0053:
        r0 = r13.textLayout;	 Catch:{ Exception -> 0x0080 }
        r10 = r0.getLineWidth(r8);	 Catch:{ Exception -> 0x0080 }
        r0 = r13.textHeight;	 Catch:{ Exception -> 0x0080 }
        r0 = (double) r0;	 Catch:{ Exception -> 0x0080 }
        r2 = r13.textLayout;	 Catch:{ Exception -> 0x0080 }
        r2 = r2.getLineBottom(r8);	 Catch:{ Exception -> 0x0080 }
        r2 = (double) r2;	 Catch:{ Exception -> 0x0080 }
        r2 = java.lang.Math.ceil(r2);	 Catch:{ Exception -> 0x0080 }
        r0 = java.lang.Math.max(r0, r2);	 Catch:{ Exception -> 0x0080 }
        r0 = (int) r0;	 Catch:{ Exception -> 0x0080 }
        r13.textHeight = r0;	 Catch:{ Exception -> 0x0080 }
        r0 = r13.textWidth;	 Catch:{ Exception -> 0x0087 }
        r0 = (double) r0;	 Catch:{ Exception -> 0x0087 }
        r2 = (double) r10;	 Catch:{ Exception -> 0x0087 }
        r2 = java.lang.Math.ceil(r2);	 Catch:{ Exception -> 0x0087 }
        r0 = java.lang.Math.max(r0, r2);	 Catch:{ Exception -> 0x0087 }
        r0 = (int) r0;	 Catch:{ Exception -> 0x0087 }
        r13.textWidth = r0;	 Catch:{ Exception -> 0x0087 }
        r8 = r8 + 1;
        goto L_0x0051;
    L_0x0080:
        r9 = move-exception;
        r0 = "tmessages";
        org.telegram.messenger.FileLog.m611e(r0, r9);	 Catch:{ Exception -> 0x0087 }
        goto L_0x0014;
    L_0x0087:
        r9 = move-exception;
        r0 = "tmessages";
        org.telegram.messenger.FileLog.m611e(r0, r9);
    L_0x008d:
        r0 = r13.textWidth;
        r0 = r12 - r0;
        r0 = r0 / 2;
        r13.textX = r0;
        r0 = 1088421888; // 0x40e00000 float:7.0 double:5.37751863E-315;
        r0 = org.telegram.messenger.AndroidUtilities.dp(r0);
        r13.textY = r0;
        r0 = r13.textLayout;
        r0 = r0.getWidth();
        r0 = r12 - r0;
        r0 = r0 / 2;
        r13.textXLeft = r0;
        r0 = r13.currentMessageObject;
        r0 = r0.type;
        r1 = 11;
        if (r0 != r1) goto L_0x00d5;
    L_0x00b1:
        r0 = r13.imageReceiver;
        r1 = 1115684864; // 0x42800000 float:64.0 double:5.51221563E-315;
        r1 = org.telegram.messenger.AndroidUtilities.dp(r1);
        r1 = r12 - r1;
        r1 = r1 / 2;
        r2 = r13.textHeight;
        r3 = 1097859072; // 0x41700000 float:15.0 double:5.424144515E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r2 = r2 + r3;
        r3 = 1115684864; // 0x42800000 float:64.0 double:5.51221563E-315;
        r3 = org.telegram.messenger.AndroidUtilities.dp(r3);
        r4 = 1115684864; // 0x42800000 float:64.0 double:5.51221563E-315;
        r4 = org.telegram.messenger.AndroidUtilities.dp(r4);
        r0.setImageCoords(r1, r2, r3, r4);
    L_0x00d5:
        r1 = r13.textHeight;
        r0 = r13.currentMessageObject;
        r0 = r0.type;
        r2 = 11;
        if (r0 != r2) goto L_0x00ee;
    L_0x00df:
        r0 = 70;
    L_0x00e1:
        r0 = r0 + 14;
        r0 = (float) r0;
        r0 = org.telegram.messenger.AndroidUtilities.dp(r0);
        r0 = r0 + r1;
        r13.setMeasuredDimension(r12, r0);
        goto L_0x0014;
    L_0x00ee:
        r0 = 0;
        goto L_0x00e1;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Cells.ChatActionCell.onMeasure(int, int):void");
    }

    protected void onDraw(Canvas canvas) {
        if (this.currentMessageObject != null) {
            Drawable backgroundDrawable;
            if (ApplicationLoader.isCustomTheme()) {
                backgroundDrawable = ResourceLoader.backgroundBlack;
            } else {
                backgroundDrawable = ResourceLoader.backgroundBlue;
            }
            backgroundDrawable.setBounds(this.textX - AndroidUtilities.dp(5.0f), AndroidUtilities.dp(5.0f), (this.textX + this.textWidth) + AndroidUtilities.dp(5.0f), AndroidUtilities.dp(9.0f) + this.textHeight);
            backgroundDrawable.draw(canvas);
            if (this.currentMessageObject.type == 11) {
                this.imageReceiver.draw(canvas);
            }
            if (this.textLayout != null) {
                canvas.save();
                canvas.translate((float) this.textXLeft, (float) this.textY);
                this.textLayout.draw(canvas);
                canvas.restore();
            }
        }
    }
}
