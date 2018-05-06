package org.telegram.ui.Cells;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewCompat;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.C0553R;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MediaController.FileDownloadProgressListener;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.FileLocation;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.TL_peerChannel;
import org.telegram.tgnet.TLRPC.TL_peerUser;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.LinkPath;
import org.telegram.ui.Components.ResourceLoader;
import org.telegram.ui.Components.StaticLayoutEx;

public class ChatBaseCell extends BaseCell implements FileDownloadProgressListener {
    private static TextPaint forwardNamePaint;
    private static TextPaint namePaint;
    protected static Paint replyLinePaint;
    protected static TextPaint replyNamePaint;
    protected static TextPaint replyTextPaint;
    private static TextPaint timeMediaPaint;
    private static TextPaint timePaintIn;
    private static TextPaint timePaintOut;
    protected static Paint urlPaint;
    private int TAG;
    protected boolean allowAssistant = false;
    private AvatarDrawable avatarDrawable;
    private ImageReceiver avatarImage;
    private boolean avatarPressed = false;
    protected int backgroundWidth = 100;
    private Chat currentChat;
    private Chat currentForwardChannel;
    private String currentForwardNameString;
    private User currentForwardUser;
    protected MessageObject currentMessageObject;
    private String currentNameString;
    private FileLocation currentPhoto;
    private FileLocation currentReplyPhoto;
    private TextPaint currentTimePaint;
    private String currentTimeString;
    private User currentUser;
    private String currentViewsString;
    protected ChatBaseCellDelegate delegate;
    protected boolean drawBackground = true;
    protected boolean drawForwardedName = false;
    protected boolean drawName = false;
    private boolean drawShareButton;
    protected boolean drawTime = true;
    protected boolean forwardName = false;
    private float forwardNameOffsetX = 0.0f;
    private boolean forwardNamePressed = false;
    private int forwardNameX;
    private int forwardNameY;
    private StaticLayout forwardedNameLayout;
    protected int forwardedNameWidth;
    protected boolean isAvatarVisible = false;
    public boolean isChat = false;
    protected boolean isCheckPressed = true;
    protected boolean isHighlighted = false;
    protected boolean isPressed = false;
    private int lastDeleteDate;
    private int lastSendState;
    private int lastViewsCount;
    protected int layoutHeight;
    protected int layoutWidth;
    protected boolean linkPreviewPressed;
    protected boolean media = false;
    private StaticLayout nameLayout;
    private float nameOffsetX = 0.0f;
    protected int nameWidth;
    protected int namesOffset;
    private boolean needReplyImage = false;
    protected ClickableSpan pressedLink;
    private ImageReceiver replyImageReceiver;
    private StaticLayout replyNameLayout;
    private float replyNameOffset;
    protected int replyNameWidth;
    private boolean replyPressed = false;
    private int replyStartX;
    private int replyStartY;
    private StaticLayout replyTextLayout;
    private float replyTextOffset;
    protected int replyTextWidth;
    private boolean sharePressed;
    private int shareStartX;
    private int shareStartY;
    private StaticLayout timeLayout;
    private int timeTextWidth;
    protected int timeWidth;
    private int timeX;
    protected LinkPath urlPath = new LinkPath();
    private StaticLayout viewsLayout;
    private int viewsTextWidth;
    private boolean wasLayout = false;

    public interface ChatBaseCellDelegate {
        boolean canPerformActions();

        void didClickedImage(ChatBaseCell chatBaseCell);

        void didLongPressed(ChatBaseCell chatBaseCell);

        void didPressReplyMessage(ChatBaseCell chatBaseCell, int i);

        void didPressShare(ChatBaseCell chatBaseCell);

        void didPressUrl(MessageObject messageObject, ClickableSpan clickableSpan, boolean z);

        void didPressedCancelSendButton(ChatBaseCell chatBaseCell);

        void didPressedChannelAvatar(ChatBaseCell chatBaseCell, Chat chat);

        void didPressedUserAvatar(ChatBaseCell chatBaseCell, User user);

        void needOpenWebView(String str, String str2, String str3, int i, int i2);
    }

    public ChatBaseCell(Context context) {
        super(context);
        if (timePaintIn == null) {
            timePaintIn = new TextPaint(1);
            timePaintIn.setTextSize((float) AndroidUtilities.dp(12.0f));
            timePaintIn.setColor(-6182221);
            timePaintOut = new TextPaint(1);
            timePaintOut.setTextSize((float) AndroidUtilities.dp(12.0f));
            timePaintOut.setColor(-9391780);
            timeMediaPaint = new TextPaint(1);
            timeMediaPaint.setTextSize((float) AndroidUtilities.dp(12.0f));
            timeMediaPaint.setColor(-1);
            namePaint = new TextPaint(1);
            namePaint.setTextSize((float) AndroidUtilities.dp(15.0f));
            forwardNamePaint = new TextPaint(1);
            forwardNamePaint.setTextSize((float) AndroidUtilities.dp(14.0f));
            replyNamePaint = new TextPaint(1);
            replyNamePaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            replyNamePaint.setTextSize((float) AndroidUtilities.dp(14.0f));
            replyTextPaint = new TextPaint(1);
            replyTextPaint.setTextSize((float) AndroidUtilities.dp(14.0f));
            replyTextPaint.linkColor = -13537377;
            replyLinePaint = new Paint();
            urlPaint = new Paint();
            urlPaint.setColor(858877855);
        }
        this.avatarImage = new ImageReceiver(this);
        this.avatarImage.setRoundRadius(AndroidUtilities.dp(21.0f));
        this.avatarDrawable = new AvatarDrawable();
        this.replyImageReceiver = new ImageReceiver(this);
        this.TAG = MediaController.getInstance().generateObserverTag();
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.avatarImage.onDetachedFromWindow();
        this.replyImageReceiver.onDetachedFromWindow();
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.avatarImage.onAttachedToWindow();
        this.replyImageReceiver.onAttachedToWindow();
    }

    public void setPressed(boolean pressed) {
        super.setPressed(pressed);
        invalidate();
    }

    protected void resetPressedLink() {
        if (this.pressedLink != null) {
            this.pressedLink = null;
        }
        this.linkPreviewPressed = false;
        invalidate();
    }

    public void setDelegate(ChatBaseCellDelegate delegate) {
        this.delegate = delegate;
    }

    public void setHighlighted(boolean value) {
        if (this.isHighlighted != value) {
            this.isHighlighted = value;
            invalidate();
        }
    }

    public void setCheckPressed(boolean value, boolean pressed) {
        this.isCheckPressed = value;
        this.isPressed = pressed;
        invalidate();
    }

    public void setAllowAssistant(boolean value) {
        this.allowAssistant = value;
    }

    protected boolean isUserDataChanged() {
        boolean z = false;
        if (this.currentMessageObject == null || (this.currentUser == null && this.currentChat == null)) {
            return false;
        }
        if (this.lastSendState != this.currentMessageObject.messageOwner.send_state || this.lastDeleteDate != this.currentMessageObject.messageOwner.destroyTime || this.lastViewsCount != this.currentMessageObject.messageOwner.views) {
            return true;
        }
        User newUser = null;
        Chat newChat = null;
        if (this.currentMessageObject.messageOwner.from_id > 0) {
            newUser = MessagesController.getInstance().getUser(Integer.valueOf(this.currentMessageObject.messageOwner.from_id));
        } else if (this.currentMessageObject.messageOwner.from_id < 0) {
            newChat = MessagesController.getInstance().getChat(Integer.valueOf(-this.currentMessageObject.messageOwner.from_id));
        }
        FileLocation newPhoto = null;
        if (this.isAvatarVisible) {
            if (newUser != null && newUser.photo != null) {
                newPhoto = newUser.photo.photo_small;
            } else if (!(newChat == null || newChat.photo == null)) {
                newPhoto = newChat.photo.photo_small;
            }
        }
        if (this.replyTextLayout == null && this.currentMessageObject.replyMessageObject != null) {
            return true;
        }
        if (this.currentPhoto == null && newPhoto != null) {
            return true;
        }
        if (this.currentPhoto != null && newPhoto == null) {
            return true;
        }
        if (this.currentPhoto != null && newPhoto != null && (this.currentPhoto.local_id != newPhoto.local_id || this.currentPhoto.volume_id != newPhoto.volume_id)) {
            return true;
        }
        FileLocation newReplyPhoto = null;
        if (this.currentMessageObject.replyMessageObject != null) {
            PhotoSize photoSize = FileLoader.getClosestPhotoSizeWithSize(this.currentMessageObject.replyMessageObject.photoThumbs, 80);
            if (!(photoSize == null || this.currentMessageObject.replyMessageObject.type == 13)) {
                newReplyPhoto = photoSize.location;
            }
        }
        if (this.currentReplyPhoto == null && newReplyPhoto != null) {
            return true;
        }
        String newNameString = null;
        if (this.drawName && this.isChat && !this.currentMessageObject.isOutOwner()) {
            if (newUser != null) {
                newNameString = UserObject.getUserName(newUser);
            } else if (newChat != null) {
                newNameString = newChat.title;
            }
        }
        if (this.currentNameString == null && newNameString != null) {
            return true;
        }
        if (this.currentNameString != null && newNameString == null) {
            return true;
        }
        if (this.currentNameString != null && newNameString != null && !this.currentNameString.equals(newNameString)) {
            return true;
        }
        newNameString = this.currentMessageObject.getForwardedName();
        if ((this.currentForwardNameString == null && newNameString != null) || ((this.currentForwardNameString != null && newNameString == null) || !(this.currentForwardNameString == null || newNameString == null || this.currentForwardNameString.equals(newNameString)))) {
            z = true;
        }
        return z;
    }

    protected void measureTime(MessageObject messageObject) {
        this.currentTimeString = LocaleController.getInstance().formatterDay.format(((long) messageObject.messageOwner.date) * 1000);
        int ceil = (int) Math.ceil((double) timeMediaPaint.measureText(this.currentTimeString));
        this.timeWidth = ceil;
        this.timeTextWidth = ceil;
        if ((messageObject.messageOwner.flags & 1024) != 0) {
            this.currentViewsString = String.format("%s", new Object[]{LocaleController.formatShortNumber(Math.max(1, messageObject.messageOwner.views), null)});
            this.timeWidth += (((int) Math.ceil((double) timeMediaPaint.measureText(this.currentViewsString))) + ResourceLoader.viewsCountDrawable.getIntrinsicWidth()) + AndroidUtilities.dp(10.0f);
        }
    }

    public void setMessageObject(MessageObject messageObject) {
        this.currentMessageObject = messageObject;
        this.lastSendState = messageObject.messageOwner.send_state;
        this.lastDeleteDate = messageObject.messageOwner.destroyTime;
        this.lastViewsCount = messageObject.messageOwner.views;
        this.isPressed = false;
        this.isCheckPressed = true;
        this.isAvatarVisible = false;
        this.wasLayout = false;
        this.drawShareButton = false;
        this.replyNameLayout = null;
        this.replyTextLayout = null;
        this.replyNameWidth = 0;
        this.replyTextWidth = 0;
        this.currentReplyPhoto = null;
        this.currentUser = null;
        this.currentChat = null;
        if ((messageObject.messageOwner.flags & 1024) != 0) {
            if (this.currentMessageObject.isContentUnread() && !this.currentMessageObject.isOut()) {
                MessagesController.getInstance().addToViewsQueue(this.currentMessageObject.messageOwner, false);
                this.currentMessageObject.setContentIsRead();
            } else if (!this.currentMessageObject.viewsReloaded) {
                MessagesController.getInstance().addToViewsQueue(this.currentMessageObject.messageOwner, true);
                this.currentMessageObject.viewsReloaded = true;
            }
        }
        if (messageObject.messageOwner.from_id > 0) {
            this.currentUser = MessagesController.getInstance().getUser(Integer.valueOf(messageObject.messageOwner.from_id));
        } else if (messageObject.messageOwner.from_id < 0) {
            this.currentChat = MessagesController.getInstance().getChat(Integer.valueOf(-messageObject.messageOwner.from_id));
            if (messageObject.messageOwner.to_id.channel_id != 0 && (messageObject.messageOwner.reply_to_msg_id == 0 || messageObject.type != 13)) {
                this.drawShareButton = true;
            }
        }
        if (this.isChat && !messageObject.isOutOwner() && messageObject.messageOwner.from_id > 0) {
            this.isAvatarVisible = true;
            if (this.currentUser != null) {
                if (this.currentUser.photo != null) {
                    this.currentPhoto = this.currentUser.photo.photo_small;
                } else {
                    this.currentPhoto = null;
                }
                this.avatarDrawable.setInfo(this.currentUser);
            } else if (this.currentChat != null) {
                if (this.currentChat.photo != null) {
                    this.currentPhoto = this.currentChat.photo.photo_small;
                } else {
                    this.currentPhoto = null;
                }
                this.avatarDrawable.setInfo(this.currentChat);
            } else {
                this.currentPhoto = null;
                this.avatarDrawable.setInfo(messageObject.messageOwner.from_id, null, null, false);
            }
            this.avatarImage.setImage(this.currentPhoto, "50_50", this.avatarDrawable, null, false);
        }
        if (this.media) {
            this.currentTimePaint = timeMediaPaint;
        } else if (this.currentMessageObject.isOutOwner()) {
            this.currentTimePaint = timePaintOut;
        } else {
            this.currentTimePaint = timePaintIn;
        }
        this.currentTimeString = LocaleController.getInstance().formatterDay.format(((long) messageObject.messageOwner.date) * 1000);
        int ceil = (int) Math.ceil((double) this.currentTimePaint.measureText(this.currentTimeString));
        this.timeWidth = ceil;
        this.timeTextWidth = ceil;
        if ((messageObject.messageOwner.flags & 1024) != 0) {
            this.currentViewsString = String.format("%s", new Object[]{LocaleController.formatShortNumber(Math.max(1, messageObject.messageOwner.views), null)});
            this.viewsTextWidth = (int) Math.ceil((double) this.currentTimePaint.measureText(this.currentViewsString));
            this.timeWidth += (this.viewsTextWidth + ResourceLoader.viewsCountDrawable.getIntrinsicWidth()) + AndroidUtilities.dp(10.0f);
        }
        this.namesOffset = 0;
        if (this.drawName && this.isChat && !this.currentMessageObject.isOutOwner()) {
            if (this.currentUser != null) {
                this.currentNameString = UserObject.getUserName(this.currentUser);
            } else if (this.currentChat != null) {
                this.currentNameString = this.currentChat.title;
            } else {
                this.currentNameString = "DELETED";
            }
            this.nameWidth = getMaxNameWidth();
            if (this.nameWidth < 0) {
                this.nameWidth = AndroidUtilities.dp(100.0f);
            }
            try {
                this.nameLayout = new StaticLayout(TextUtils.ellipsize(this.currentNameString.replace("\n", " "), namePaint, (float) (this.nameWidth - AndroidUtilities.dp(12.0f)), TruncateAt.END), namePaint, this.nameWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                if (this.nameLayout == null || this.nameLayout.getLineCount() <= 0) {
                    this.nameWidth = 0;
                } else {
                    this.nameWidth = (int) Math.ceil((double) this.nameLayout.getLineWidth(0));
                    this.namesOffset += AndroidUtilities.dp(19.0f);
                    this.nameOffsetX = this.nameLayout.getLineLeft(0);
                }
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
        } else {
            this.currentNameString = null;
            this.nameLayout = null;
            this.nameWidth = 0;
        }
        if (this.drawForwardedName && messageObject.isForwarded()) {
            if (messageObject.messageOwner.fwd_from_id instanceof TL_peerChannel) {
                this.currentForwardChannel = MessagesController.getInstance().getChat(Integer.valueOf(messageObject.messageOwner.fwd_from_id.channel_id));
                this.currentForwardUser = null;
            } else if (messageObject.messageOwner.fwd_from_id instanceof TL_peerUser) {
                this.currentForwardChannel = null;
                this.currentForwardUser = MessagesController.getInstance().getUser(Integer.valueOf(messageObject.messageOwner.fwd_from_id.user_id));
            }
            if (this.currentForwardUser == null && this.currentForwardChannel == null) {
                this.currentForwardNameString = null;
                this.forwardedNameLayout = null;
                this.forwardedNameWidth = 0;
            } else {
                if (this.currentForwardUser != null) {
                    this.currentForwardNameString = UserObject.getUserName(this.currentForwardUser);
                } else {
                    this.currentForwardNameString = this.currentForwardChannel.title;
                }
                this.forwardedNameWidth = getMaxNameWidth();
                CharSequence str = TextUtils.ellipsize(this.currentForwardNameString.replace("\n", " "), forwardNamePaint, (float) (this.forwardedNameWidth - AndroidUtilities.dp(40.0f)), TruncateAt.END);
                this.forwardedNameLayout = StaticLayoutEx.createStaticLayout(AndroidUtilities.replaceTags(String.format("%s\n%s <b>%s</b>", new Object[]{LocaleController.getString("ForwardedMessage", C0553R.string.ForwardedMessage), LocaleController.getString("From", C0553R.string.From), str})), forwardNamePaint, this.forwardedNameWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false, TruncateAt.END, this.forwardedNameWidth, 2);
                if (this.forwardedNameLayout.getLineCount() > 1) {
                    this.forwardedNameWidth = Math.max((int) Math.ceil((double) this.forwardedNameLayout.getLineWidth(0)), (int) Math.ceil((double) this.forwardedNameLayout.getLineWidth(1)));
                    this.namesOffset += AndroidUtilities.dp(36.0f);
                    this.forwardNameOffsetX = Math.min(this.forwardedNameLayout.getLineLeft(0), this.forwardedNameLayout.getLineLeft(1));
                } else {
                    this.forwardedNameWidth = 0;
                }
            }
        } else {
            this.currentForwardNameString = null;
            this.forwardedNameLayout = null;
            this.forwardedNameWidth = 0;
        }
        if (messageObject.isReply()) {
            int maxWidth;
            int maxWidth2;
            CharSequence stringFinalName;
            this.namesOffset += AndroidUtilities.dp(42.0f);
            if (messageObject.contentType == 2 || messageObject.contentType == 3) {
                this.namesOffset += AndroidUtilities.dp(4.0f);
            } else if (messageObject.contentType == 1) {
                if (messageObject.type == 13) {
                    this.namesOffset -= AndroidUtilities.dp(42.0f);
                } else {
                    this.namesOffset += AndroidUtilities.dp(5.0f);
                }
            }
            if (messageObject.type == 13) {
                int width;
                if (!AndroidUtilities.isTablet()) {
                    width = AndroidUtilities.displaySize.x;
                } else if (AndroidUtilities.isSmallTablet() && getResources().getConfiguration().orientation == 1) {
                    width = AndroidUtilities.displaySize.x;
                } else {
                    int leftWidth = (AndroidUtilities.displaySize.x / 100) * 35;
                    if (leftWidth < AndroidUtilities.dp(320.0f)) {
                        leftWidth = AndroidUtilities.dp(320.0f);
                    }
                    width = AndroidUtilities.displaySize.x - leftWidth;
                }
                if (messageObject.isOutOwner()) {
                    maxWidth = (width - this.backgroundWidth) - AndroidUtilities.dp(BitmapDescriptorFactory.HUE_YELLOW);
                } else {
                    int i = width - this.backgroundWidth;
                    ceil = (!this.isChat || messageObject.messageOwner.from_id <= 0) ? 0 : 61;
                    maxWidth = i - AndroidUtilities.dp((float) (ceil + 56));
                }
            } else {
                maxWidth = getMaxNameWidth() - AndroidUtilities.dp(22.0f);
            }
            if (this.media || messageObject.contentType == 0) {
                maxWidth2 = maxWidth;
            } else {
                maxWidth2 = maxWidth - AndroidUtilities.dp(8.0f);
            }
            CharSequence stringFinalText = null;
            if (messageObject.replyMessageObject != null) {
                PhotoSize photoSize = FileLoader.getClosestPhotoSizeWithSize(messageObject.replyMessageObject.photoThumbs, 80);
                if (photoSize == null || messageObject.replyMessageObject.type == 13 || (messageObject.type == 13 && !AndroidUtilities.isTablet())) {
                    this.replyImageReceiver.setImageBitmap((Drawable) null);
                    this.needReplyImage = false;
                    maxWidth = maxWidth2;
                } else {
                    this.currentReplyPhoto = photoSize.location;
                    this.replyImageReceiver.setImage(photoSize.location, "50_50", null, null, true);
                    this.needReplyImage = true;
                    maxWidth = maxWidth2 - AndroidUtilities.dp(44.0f);
                }
                String name = null;
                if (messageObject.replyMessageObject.messageOwner.from_id > 0) {
                    User user = MessagesController.getInstance().getUser(Integer.valueOf(messageObject.replyMessageObject.messageOwner.from_id));
                    if (user != null) {
                        name = UserObject.getUserName(user);
                    }
                } else {
                    Chat chat = MessagesController.getInstance().getChat(Integer.valueOf(-messageObject.replyMessageObject.messageOwner.from_id));
                    if (chat != null) {
                        name = chat.title;
                    }
                }
                if (name != null) {
                    stringFinalName = TextUtils.ellipsize(name.replace("\n", " "), replyNamePaint, (float) (maxWidth - AndroidUtilities.dp(8.0f)), TruncateAt.END);
                } else {
                    stringFinalName = null;
                }
                if (messageObject.replyMessageObject.messageText != null && messageObject.replyMessageObject.messageText.length() > 0) {
                    String mess = messageObject.replyMessageObject.messageText.toString();
                    if (mess.length() > 150) {
                        mess = mess.substring(0, 150);
                    }
                    stringFinalText = TextUtils.ellipsize(Emoji.replaceEmoji(mess.replace("\n", " "), replyTextPaint.getFontMetricsInt(), AndroidUtilities.dp(14.0f), false), replyTextPaint, (float) (maxWidth - AndroidUtilities.dp(8.0f)), TruncateAt.END);
                }
            } else {
                stringFinalName = null;
                maxWidth = maxWidth2;
            }
            if (stringFinalName == null) {
                stringFinalName = LocaleController.getString("Loading", C0553R.string.Loading);
            }
            try {
                this.replyNameLayout = new StaticLayout(stringFinalName, replyNamePaint, maxWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                if (this.replyNameLayout.getLineCount() > 0) {
                    this.replyNameWidth = AndroidUtilities.dp((float) ((this.needReplyImage ? 44 : 0) + 12)) + ((int) Math.ceil((double) this.replyNameLayout.getLineWidth(0)));
                    this.replyNameOffset = this.replyNameLayout.getLineLeft(0);
                }
            } catch (Throwable e2) {
                FileLog.m611e("tmessages", e2);
            }
            if (stringFinalText != null) {
                try {
                    this.replyTextLayout = new StaticLayout(stringFinalText, replyTextPaint, maxWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                    if (this.replyTextLayout.getLineCount() > 0) {
                        this.replyTextWidth = AndroidUtilities.dp((float) ((this.needReplyImage ? 44 : 0) + 12)) + ((int) Math.ceil((double) this.replyTextLayout.getLineWidth(0)));
                        this.replyTextOffset = this.replyTextLayout.getLineLeft(0);
                    }
                } catch (Throwable e22) {
                    FileLog.m611e("tmessages", e22);
                }
            }
        }
        requestLayout();
    }

    public final MessageObject getMessageObject() {
        return this.currentMessageObject;
    }

    protected int getMaxNameWidth() {
        return this.backgroundWidth - AndroidUtilities.dp(8.0f);
    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean result = false;
        float x = event.getX();
        float y = event.getY();
        if (event.getAction() != 0) {
            if (event.getAction() != 2) {
                cancelCheckLongPress();
            }
            if (this.avatarPressed) {
                if (event.getAction() == 1) {
                    this.avatarPressed = false;
                    playSoundEffect(0);
                    if (this.delegate != null) {
                        if (this.currentUser != null) {
                            this.delegate.didPressedUserAvatar(this, this.currentUser);
                        } else if (this.currentChat != null) {
                            this.delegate.didPressedChannelAvatar(this, this.currentChat);
                        }
                    }
                } else if (event.getAction() == 3) {
                    this.avatarPressed = false;
                } else if (event.getAction() == 2 && this.isAvatarVisible && !this.avatarImage.isInsideImage(x, y)) {
                    this.avatarPressed = false;
                }
            } else if (this.forwardNamePressed) {
                if (event.getAction() == 1) {
                    this.forwardNamePressed = false;
                    playSoundEffect(0);
                    if (this.delegate != null) {
                        if (this.currentForwardUser != null) {
                            this.delegate.didPressedUserAvatar(this, this.currentForwardUser);
                        } else {
                            this.delegate.didPressedChannelAvatar(this, this.currentForwardChannel);
                        }
                    }
                } else if (event.getAction() == 3) {
                    this.forwardNamePressed = false;
                } else if (event.getAction() == 2 && (x < ((float) this.forwardNameX) || x > ((float) (this.forwardNameX + this.forwardedNameWidth)) || y < ((float) this.forwardNameY) || y > ((float) (this.forwardNameY + AndroidUtilities.dp(32.0f))))) {
                    this.forwardNamePressed = false;
                }
            } else if (this.replyPressed) {
                if (event.getAction() == 1) {
                    this.replyPressed = false;
                    playSoundEffect(0);
                    if (this.delegate != null) {
                        this.delegate.didPressReplyMessage(this, this.currentMessageObject.messageOwner.reply_to_msg_id);
                    }
                } else if (event.getAction() == 3) {
                    this.replyPressed = false;
                } else if (event.getAction() == 2 && (x < ((float) this.replyStartX) || x > ((float) (this.replyStartX + Math.max(this.replyNameWidth, this.replyTextWidth))) || y < ((float) this.replyStartY) || y > ((float) (this.replyStartY + AndroidUtilities.dp(35.0f))))) {
                    this.replyPressed = false;
                }
            } else if (this.sharePressed) {
                if (event.getAction() == 1) {
                    this.sharePressed = false;
                    playSoundEffect(0);
                    if (this.delegate != null) {
                        this.delegate.didPressShare(this);
                    }
                } else if (event.getAction() == 3) {
                    this.sharePressed = false;
                } else if (event.getAction() == 2 && (x < ((float) this.shareStartX) || x > ((float) (this.shareStartX + AndroidUtilities.dp(40.0f))) || y < ((float) this.shareStartY) || y > ((float) (this.shareStartY + AndroidUtilities.dp(32.0f))))) {
                    this.sharePressed = false;
                }
                invalidate();
            }
        } else if (this.delegate == null || this.delegate.canPerformActions()) {
            if (this.isAvatarVisible && this.avatarImage.isInsideImage(x, y)) {
                this.avatarPressed = true;
                result = true;
            } else if (this.drawForwardedName && this.forwardedNameLayout != null && x >= ((float) this.forwardNameX) && x <= ((float) (this.forwardNameX + this.forwardedNameWidth)) && y >= ((float) this.forwardNameY) && y <= ((float) (this.forwardNameY + AndroidUtilities.dp(32.0f)))) {
                this.forwardNamePressed = true;
                result = true;
            } else if (this.currentMessageObject.isReply() && x >= ((float) this.replyStartX) && x <= ((float) (this.replyStartX + Math.max(this.replyNameWidth, this.replyTextWidth))) && y >= ((float) this.replyStartY) && y <= ((float) (this.replyStartY + AndroidUtilities.dp(35.0f)))) {
                this.replyPressed = true;
                result = true;
            } else if (this.drawShareButton && x >= ((float) this.shareStartX) && x <= ((float) (this.shareStartX + AndroidUtilities.dp(40.0f))) && y >= ((float) this.shareStartY) && y <= ((float) (this.shareStartY + AndroidUtilities.dp(32.0f)))) {
                this.sharePressed = true;
                result = true;
                invalidate();
            }
            if (result) {
                startCheckLongPress();
            }
        }
        return result;
    }

    @SuppressLint({"DrawAllocation"})
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (this.currentMessageObject == null) {
            super.onLayout(changed, left, top, right, bottom);
        } else if (changed || !this.wasLayout) {
            this.layoutWidth = getMeasuredWidth();
            this.layoutHeight = getMeasuredHeight();
            this.timeLayout = new StaticLayout(this.currentTimeString, this.currentTimePaint, this.timeTextWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            int dp;
            int dp2;
            if (this.media) {
                if (this.currentMessageObject.isOutOwner()) {
                    this.timeX = (this.layoutWidth - this.timeWidth) - AndroidUtilities.dp(42.0f);
                } else {
                    dp = (this.backgroundWidth - AndroidUtilities.dp(4.0f)) - this.timeWidth;
                    dp2 = (!this.isChat || this.currentMessageObject.messageOwner.from_id <= 0) ? 0 : AndroidUtilities.dp(52.0f);
                    this.timeX = dp2 + dp;
                }
            } else if (this.currentMessageObject.isOutOwner()) {
                this.timeX = (this.layoutWidth - this.timeWidth) - AndroidUtilities.dp(38.5f);
            } else {
                dp = (this.backgroundWidth - AndroidUtilities.dp(9.0f)) - this.timeWidth;
                if (!this.isChat || this.currentMessageObject.messageOwner.from_id <= 0) {
                    dp2 = 0;
                } else {
                    dp2 = AndroidUtilities.dp(52.0f);
                }
                this.timeX = dp2 + dp;
            }
            if ((this.currentMessageObject.messageOwner.flags & 1024) != 0) {
                this.viewsLayout = new StaticLayout(this.currentViewsString, this.currentTimePaint, this.viewsTextWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            } else {
                this.viewsLayout = null;
            }
            if (this.isAvatarVisible) {
                this.avatarImage.setImageCoords(AndroidUtilities.dp(6.0f), this.layoutHeight - AndroidUtilities.dp(45.0f), AndroidUtilities.dp(42.0f), AndroidUtilities.dp(42.0f));
            }
            this.wasLayout = true;
        }
    }

    protected void onAfterBackgroundDraw(Canvas canvas) {
    }

    public ImageReceiver getPhotoImage() {
        return null;
    }

    protected void onLongPress() {
        if (this.delegate != null) {
            this.delegate.didLongPressed(this);
        }
    }

    protected void onDraw(Canvas canvas) {
        if (this.currentMessageObject != null) {
            if (this.wasLayout) {
                Drawable currentBackgroundDrawable;
                int i;
                int dp;
                if (this.isAvatarVisible) {
                    this.avatarImage.draw(canvas);
                }
                if (this.currentMessageObject.isOutOwner()) {
                    if (!(isPressed() && this.isCheckPressed) && ((this.isCheckPressed || !this.isPressed) && !this.isHighlighted)) {
                        if (this.media) {
                            currentBackgroundDrawable = ResourceLoader.backgroundMediaDrawableOut;
                        } else {
                            currentBackgroundDrawable = ResourceLoader.backgroundDrawableOut;
                        }
                    } else if (this.media) {
                        currentBackgroundDrawable = ResourceLoader.backgroundMediaDrawableOutSelected;
                    } else {
                        currentBackgroundDrawable = ResourceLoader.backgroundDrawableOutSelected;
                    }
                    i = this.layoutWidth - this.backgroundWidth;
                    if (this.media) {
                        dp = AndroidUtilities.dp(9.0f);
                    } else {
                        dp = 0;
                    }
                    setDrawableBounds(currentBackgroundDrawable, i - dp, AndroidUtilities.dp(1.0f), this.backgroundWidth, this.layoutHeight - AndroidUtilities.dp(2.0f));
                } else {
                    if (!(isPressed() && this.isCheckPressed) && ((this.isCheckPressed || !this.isPressed) && !this.isHighlighted)) {
                        if (this.media) {
                            currentBackgroundDrawable = ResourceLoader.backgroundMediaDrawableIn;
                        } else {
                            currentBackgroundDrawable = ResourceLoader.backgroundDrawableIn;
                        }
                    } else if (this.media) {
                        currentBackgroundDrawable = ResourceLoader.backgroundMediaDrawableInSelected;
                    } else {
                        currentBackgroundDrawable = ResourceLoader.backgroundDrawableInSelected;
                    }
                    if (!this.isChat || this.currentMessageObject.messageOwner.from_id <= 0) {
                        setDrawableBounds(currentBackgroundDrawable, !this.media ? 0 : AndroidUtilities.dp(9.0f), AndroidUtilities.dp(1.0f), this.backgroundWidth, this.layoutHeight - AndroidUtilities.dp(2.0f));
                    } else {
                        if (this.media) {
                            dp = 9;
                        } else {
                            dp = 0;
                        }
                        setDrawableBounds(currentBackgroundDrawable, AndroidUtilities.dp((float) (dp + 52)), AndroidUtilities.dp(1.0f), this.backgroundWidth, this.layoutHeight - AndroidUtilities.dp(2.0f));
                    }
                }
                if (this.drawBackground && currentBackgroundDrawable != null) {
                    currentBackgroundDrawable.draw(canvas);
                }
                onAfterBackgroundDraw(canvas);
                if (this.drawShareButton) {
                    Drawable drawable = ResourceLoader.shareDrawable[ApplicationLoader.isCustomTheme() ? 1 : 0][this.sharePressed ? 1 : 0];
                    i = currentBackgroundDrawable.getBounds().right + AndroidUtilities.dp(8.0f);
                    this.shareStartX = i;
                    int dp2 = this.layoutHeight - AndroidUtilities.dp(41.0f);
                    this.shareStartY = dp2;
                    drawable.setBounds(i, dp2, currentBackgroundDrawable.getBounds().right + AndroidUtilities.dp(40.0f), this.layoutHeight - AndroidUtilities.dp(9.0f));
                    ResourceLoader.shareDrawable[ApplicationLoader.isCustomTheme() ? 1 : 0][this.sharePressed ? 1 : 0].draw(canvas);
                }
                if (this.drawName && this.nameLayout != null) {
                    canvas.save();
                    if (this.media) {
                        canvas.translate(((float) (currentBackgroundDrawable.getBounds().left + AndroidUtilities.dp(10.0f))) - this.nameOffsetX, (float) AndroidUtilities.dp(10.0f));
                    } else {
                        canvas.translate(((float) (currentBackgroundDrawable.getBounds().left + AndroidUtilities.dp(19.0f))) - this.nameOffsetX, (float) AndroidUtilities.dp(10.0f));
                    }
                    if (this.currentUser != null) {
                        namePaint.setColor(AvatarDrawable.getNameColorForId(this.currentUser.id));
                    } else if (this.currentChat != null) {
                        namePaint.setColor(AvatarDrawable.getNameColorForId(this.currentChat.id));
                    } else {
                        namePaint.setColor(AvatarDrawable.getNameColorForId(0));
                    }
                    this.nameLayout.draw(canvas);
                    canvas.restore();
                }
                if (this.drawForwardedName && this.forwardedNameLayout != null) {
                    this.forwardNameY = AndroidUtilities.dp((float) ((this.drawName ? 19 : 0) + 10));
                    if (this.currentMessageObject.isOutOwner()) {
                        forwardNamePaint.setColor(-11890116);
                        this.forwardNameX = currentBackgroundDrawable.getBounds().left + AndroidUtilities.dp(10.0f);
                    } else {
                        forwardNamePaint.setColor(-16748600);
                        if (this.media) {
                            this.forwardNameX = currentBackgroundDrawable.getBounds().left + AndroidUtilities.dp(10.0f);
                        } else {
                            this.forwardNameX = currentBackgroundDrawable.getBounds().left + AndroidUtilities.dp(19.0f);
                        }
                    }
                    canvas.save();
                    canvas.translate(((float) this.forwardNameX) - this.forwardNameOffsetX, (float) this.forwardNameY);
                    this.forwardedNameLayout.draw(canvas);
                    canvas.restore();
                }
                if (this.currentMessageObject.isReply()) {
                    if (this.currentMessageObject.type == 13) {
                        int backWidth;
                        Drawable back;
                        replyLinePaint.setColor(-1);
                        replyNamePaint.setColor(-1);
                        replyTextPaint.setColor(-1);
                        if (this.currentMessageObject.isOutOwner()) {
                            backWidth = currentBackgroundDrawable.getBounds().left - AndroidUtilities.dp(32.0f);
                            this.replyStartX = (currentBackgroundDrawable.getBounds().left - AndroidUtilities.dp(9.0f)) - backWidth;
                        } else {
                            backWidth = (getWidth() - currentBackgroundDrawable.getBounds().right) - AndroidUtilities.dp(32.0f);
                            this.replyStartX = currentBackgroundDrawable.getBounds().right + AndroidUtilities.dp(23.0f);
                        }
                        if (ApplicationLoader.isCustomTheme()) {
                            back = ResourceLoader.backgroundBlack;
                        } else {
                            back = ResourceLoader.backgroundBlue;
                        }
                        this.replyStartY = this.layoutHeight - AndroidUtilities.dp(58.0f);
                        back.setBounds(this.replyStartX - AndroidUtilities.dp(7.0f), this.replyStartY - AndroidUtilities.dp(6.0f), (this.replyStartX - AndroidUtilities.dp(7.0f)) + backWidth, this.replyStartY + AndroidUtilities.dp(41.0f));
                        back.draw(canvas);
                    } else {
                        if (this.currentMessageObject.isOutOwner()) {
                            replyLinePaint.setColor(-7485062);
                            replyNamePaint.setColor(-10378423);
                            if (this.currentMessageObject.replyMessageObject == null || this.currentMessageObject.replyMessageObject.type != 0) {
                                replyTextPaint.setColor(-9391780);
                            } else {
                                replyTextPaint.setColor(ViewCompat.MEASURED_STATE_MASK);
                            }
                            this.replyStartX = currentBackgroundDrawable.getBounds().left + AndroidUtilities.dp(11.0f);
                        } else {
                            replyLinePaint.setColor(-9658414);
                            replyNamePaint.setColor(-13141330);
                            if (this.currentMessageObject.replyMessageObject == null || this.currentMessageObject.replyMessageObject.type != 0) {
                                replyTextPaint.setColor(-6710887);
                            } else {
                                replyTextPaint.setColor(ViewCompat.MEASURED_STATE_MASK);
                            }
                            if (this.currentMessageObject.contentType == 1 && this.media) {
                                this.replyStartX = currentBackgroundDrawable.getBounds().left + AndroidUtilities.dp(11.0f);
                            } else {
                                this.replyStartX = currentBackgroundDrawable.getBounds().left + AndroidUtilities.dp(20.0f);
                            }
                        }
                        if (!this.drawForwardedName || this.forwardedNameLayout == null) {
                            dp = 0;
                        } else {
                            dp = 36;
                        }
                        i = dp + 12;
                        if (!this.drawName || this.nameLayout == null) {
                            dp = 0;
                        } else {
                            dp = 20;
                        }
                        this.replyStartY = AndroidUtilities.dp((float) (dp + i));
                    }
                    canvas.drawRect((float) this.replyStartX, (float) this.replyStartY, (float) (this.replyStartX + AndroidUtilities.dp(2.0f)), (float) (this.replyStartY + AndroidUtilities.dp(35.0f)), replyLinePaint);
                    if (this.needReplyImage) {
                        this.replyImageReceiver.setImageCoords(this.replyStartX + AndroidUtilities.dp(10.0f), this.replyStartY, AndroidUtilities.dp(35.0f), AndroidUtilities.dp(35.0f));
                        this.replyImageReceiver.draw(canvas);
                    }
                    if (this.replyNameLayout != null) {
                        canvas.save();
                        canvas.translate(((float) AndroidUtilities.dp((float) ((this.needReplyImage ? 44 : 0) + 10))) + (((float) this.replyStartX) - this.replyNameOffset), (float) this.replyStartY);
                        this.replyNameLayout.draw(canvas);
                        canvas.restore();
                    }
                    if (this.replyTextLayout != null) {
                        canvas.save();
                        canvas.translate(((float) AndroidUtilities.dp((float) ((this.needReplyImage ? 44 : 0) + 10))) + (((float) this.replyStartX) - this.replyTextOffset), (float) (this.replyStartY + AndroidUtilities.dp(19.0f)));
                        this.replyTextLayout.draw(canvas);
                        canvas.restore();
                    }
                }
                if (this.drawTime || !this.media) {
                    int additionalX;
                    if (this.media) {
                        setDrawableBounds(ResourceLoader.mediaBackgroundDrawable, this.timeX - AndroidUtilities.dp(3.0f), this.layoutHeight - AndroidUtilities.dp(27.5f), this.timeWidth + AndroidUtilities.dp((float) ((this.currentMessageObject.isOutOwner() ? 20 : 0) + 6)), AndroidUtilities.dp(16.5f));
                        ResourceLoader.mediaBackgroundDrawable.draw(canvas);
                        additionalX = 0;
                        if ((this.currentMessageObject.messageOwner.flags & 1024) != 0) {
                            additionalX = (int) (((float) this.timeWidth) - this.timeLayout.getLineWidth(0));
                            if (this.currentMessageObject.isSending()) {
                                if (!this.currentMessageObject.isOutOwner()) {
                                    setDrawableBounds(ResourceLoader.clockMediaDrawable, this.timeX + AndroidUtilities.dp(11.0f), (this.layoutHeight - AndroidUtilities.dp(13.0f)) - ResourceLoader.clockMediaDrawable.getIntrinsicHeight());
                                    ResourceLoader.clockMediaDrawable.draw(canvas);
                                }
                            } else if (!this.currentMessageObject.isSendError()) {
                                setDrawableBounds(ResourceLoader.viewsMediaCountDrawable, this.timeX, (this.layoutHeight - AndroidUtilities.dp(10.0f)) - this.timeLayout.getHeight());
                                ResourceLoader.viewsMediaCountDrawable.draw(canvas);
                                if (this.viewsLayout != null) {
                                    canvas.save();
                                    canvas.translate((float) ((this.timeX + ResourceLoader.viewsMediaCountDrawable.getIntrinsicWidth()) + AndroidUtilities.dp(3.0f)), (float) ((this.layoutHeight - AndroidUtilities.dp(12.0f)) - this.timeLayout.getHeight()));
                                    this.viewsLayout.draw(canvas);
                                    canvas.restore();
                                }
                            } else if (!this.currentMessageObject.isOutOwner()) {
                                setDrawableBounds(ResourceLoader.errorDrawable, this.timeX + AndroidUtilities.dp(11.0f), (this.layoutHeight - AndroidUtilities.dp(12.5f)) - ResourceLoader.errorDrawable.getIntrinsicHeight());
                                ResourceLoader.errorDrawable.draw(canvas);
                            }
                        }
                        canvas.save();
                        canvas.translate((float) (this.timeX + additionalX), (float) ((this.layoutHeight - AndroidUtilities.dp(12.0f)) - this.timeLayout.getHeight()));
                        this.timeLayout.draw(canvas);
                        canvas.restore();
                    } else {
                        additionalX = 0;
                        if ((this.currentMessageObject.messageOwner.flags & 1024) != 0) {
                            additionalX = (int) (((float) this.timeWidth) - this.timeLayout.getLineWidth(0));
                            if (this.currentMessageObject.isSending()) {
                                if (!this.currentMessageObject.isOutOwner()) {
                                    setDrawableBounds(ResourceLoader.clockChannelDrawable, this.timeX + AndroidUtilities.dp(11.0f), (this.layoutHeight - AndroidUtilities.dp(8.5f)) - ResourceLoader.clockChannelDrawable.getIntrinsicHeight());
                                    ResourceLoader.clockChannelDrawable.draw(canvas);
                                }
                            } else if (!this.currentMessageObject.isSendError()) {
                                if (this.currentMessageObject.isOutOwner()) {
                                    setDrawableBounds(ResourceLoader.viewsOutCountDrawable, this.timeX, (this.layoutHeight - AndroidUtilities.dp(4.5f)) - this.timeLayout.getHeight());
                                    ResourceLoader.viewsOutCountDrawable.draw(canvas);
                                } else {
                                    setDrawableBounds(ResourceLoader.viewsCountDrawable, this.timeX, (this.layoutHeight - AndroidUtilities.dp(4.5f)) - this.timeLayout.getHeight());
                                    ResourceLoader.viewsCountDrawable.draw(canvas);
                                }
                                if (this.viewsLayout != null) {
                                    canvas.save();
                                    canvas.translate((float) ((this.timeX + ResourceLoader.viewsOutCountDrawable.getIntrinsicWidth()) + AndroidUtilities.dp(3.0f)), (float) ((this.layoutHeight - AndroidUtilities.dp(6.5f)) - this.timeLayout.getHeight()));
                                    this.viewsLayout.draw(canvas);
                                    canvas.restore();
                                }
                            } else if (!this.currentMessageObject.isOutOwner()) {
                                setDrawableBounds(ResourceLoader.errorDrawable, this.timeX + AndroidUtilities.dp(11.0f), (this.layoutHeight - AndroidUtilities.dp(6.5f)) - ResourceLoader.errorDrawable.getIntrinsicHeight());
                                ResourceLoader.errorDrawable.draw(canvas);
                            }
                        }
                        canvas.save();
                        canvas.translate((float) (this.timeX + additionalX), (float) ((this.layoutHeight - AndroidUtilities.dp(6.5f)) - this.timeLayout.getHeight()));
                        this.timeLayout.draw(canvas);
                        canvas.restore();
                    }
                    if (this.currentMessageObject.isOutOwner()) {
                        boolean drawCheck1 = false;
                        boolean drawCheck2 = false;
                        boolean drawClock = false;
                        boolean drawError = false;
                        boolean isBroadcast = ((int) (this.currentMessageObject.getDialogId() >> 32)) == 1;
                        if (this.currentMessageObject.isSending()) {
                            drawCheck1 = false;
                            drawCheck2 = false;
                            drawClock = true;
                            drawError = false;
                        } else if (this.currentMessageObject.isSendError()) {
                            drawCheck1 = false;
                            drawCheck2 = false;
                            drawClock = false;
                            drawError = true;
                        } else if (this.currentMessageObject.isSent()) {
                            if (this.currentMessageObject.isUnread()) {
                                drawCheck1 = false;
                                drawCheck2 = true;
                            } else {
                                drawCheck1 = true;
                                drawCheck2 = true;
                            }
                            drawClock = false;
                            drawError = false;
                        }
                        if (drawClock) {
                            if (this.media) {
                                setDrawableBounds(ResourceLoader.clockMediaDrawable, (this.layoutWidth - AndroidUtilities.dp(22.0f)) - ResourceLoader.clockMediaDrawable.getIntrinsicWidth(), (this.layoutHeight - AndroidUtilities.dp(13.0f)) - ResourceLoader.clockMediaDrawable.getIntrinsicHeight());
                                ResourceLoader.clockMediaDrawable.draw(canvas);
                            } else {
                                setDrawableBounds(ResourceLoader.clockDrawable, (this.layoutWidth - AndroidUtilities.dp(18.5f)) - ResourceLoader.clockDrawable.getIntrinsicWidth(), (this.layoutHeight - AndroidUtilities.dp(8.5f)) - ResourceLoader.clockDrawable.getIntrinsicHeight());
                                ResourceLoader.clockDrawable.draw(canvas);
                            }
                        }
                        if (!isBroadcast) {
                            if (drawCheck2) {
                                if (this.media) {
                                    if (drawCheck1) {
                                        setDrawableBounds(ResourceLoader.checkMediaDrawable, (this.layoutWidth - AndroidUtilities.dp(26.0f)) - ResourceLoader.checkMediaDrawable.getIntrinsicWidth(), (this.layoutHeight - AndroidUtilities.dp(13.0f)) - ResourceLoader.checkMediaDrawable.getIntrinsicHeight());
                                    } else {
                                        setDrawableBounds(ResourceLoader.checkMediaDrawable, (this.layoutWidth - AndroidUtilities.dp(22.0f)) - ResourceLoader.checkMediaDrawable.getIntrinsicWidth(), (this.layoutHeight - AndroidUtilities.dp(13.0f)) - ResourceLoader.checkMediaDrawable.getIntrinsicHeight());
                                    }
                                    ResourceLoader.checkMediaDrawable.draw(canvas);
                                } else {
                                    if (drawCheck1) {
                                        setDrawableBounds(ResourceLoader.checkDrawable, (this.layoutWidth - AndroidUtilities.dp(22.5f)) - ResourceLoader.checkDrawable.getIntrinsicWidth(), (this.layoutHeight - AndroidUtilities.dp(8.0f)) - ResourceLoader.checkDrawable.getIntrinsicHeight());
                                    } else {
                                        setDrawableBounds(ResourceLoader.checkDrawable, (this.layoutWidth - AndroidUtilities.dp(18.5f)) - ResourceLoader.checkDrawable.getIntrinsicWidth(), (this.layoutHeight - AndroidUtilities.dp(8.0f)) - ResourceLoader.checkDrawable.getIntrinsicHeight());
                                    }
                                    ResourceLoader.checkDrawable.draw(canvas);
                                }
                            }
                            if (drawCheck1) {
                                if (this.media) {
                                    setDrawableBounds(ResourceLoader.halfCheckMediaDrawable, (this.layoutWidth - AndroidUtilities.dp(20.5f)) - ResourceLoader.halfCheckMediaDrawable.getIntrinsicWidth(), (this.layoutHeight - AndroidUtilities.dp(13.0f)) - ResourceLoader.halfCheckMediaDrawable.getIntrinsicHeight());
                                    ResourceLoader.halfCheckMediaDrawable.draw(canvas);
                                } else {
                                    setDrawableBounds(ResourceLoader.halfCheckDrawable, (this.layoutWidth - AndroidUtilities.dp(18.0f)) - ResourceLoader.halfCheckDrawable.getIntrinsicWidth(), (this.layoutHeight - AndroidUtilities.dp(8.0f)) - ResourceLoader.halfCheckDrawable.getIntrinsicHeight());
                                    ResourceLoader.halfCheckDrawable.draw(canvas);
                                }
                            }
                        } else if (drawCheck1 || drawCheck2) {
                            if (this.media) {
                                setDrawableBounds(ResourceLoader.broadcastMediaDrawable, (this.layoutWidth - AndroidUtilities.dp(24.0f)) - ResourceLoader.broadcastMediaDrawable.getIntrinsicWidth(), (this.layoutHeight - AndroidUtilities.dp(13.0f)) - ResourceLoader.broadcastMediaDrawable.getIntrinsicHeight());
                                ResourceLoader.broadcastMediaDrawable.draw(canvas);
                            } else {
                                setDrawableBounds(ResourceLoader.broadcastDrawable, (this.layoutWidth - AndroidUtilities.dp(20.5f)) - ResourceLoader.broadcastDrawable.getIntrinsicWidth(), (this.layoutHeight - AndroidUtilities.dp(8.0f)) - ResourceLoader.broadcastDrawable.getIntrinsicHeight());
                                ResourceLoader.broadcastDrawable.draw(canvas);
                            }
                        }
                        if (!drawError) {
                            return;
                        }
                        if (this.media) {
                            setDrawableBounds(ResourceLoader.errorDrawable, (this.layoutWidth - AndroidUtilities.dp(20.5f)) - ResourceLoader.errorDrawable.getIntrinsicWidth(), (this.layoutHeight - AndroidUtilities.dp(12.5f)) - ResourceLoader.errorDrawable.getIntrinsicHeight());
                            ResourceLoader.errorDrawable.draw(canvas);
                            return;
                        }
                        setDrawableBounds(ResourceLoader.errorDrawable, (this.layoutWidth - AndroidUtilities.dp(18.0f)) - ResourceLoader.errorDrawable.getIntrinsicWidth(), (this.layoutHeight - AndroidUtilities.dp(6.5f)) - ResourceLoader.errorDrawable.getIntrinsicHeight());
                        ResourceLoader.errorDrawable.draw(canvas);
                        return;
                    }
                    return;
                }
                return;
            }
            requestLayout();
        }
    }

    public void onFailedDownload(String fileName) {
    }

    public void onSuccessDownload(String fileName) {
    }

    public void onProgressDownload(String fileName, float progress) {
    }

    public void onProgressUpload(String fileName, float progress, boolean isEncrypted) {
    }

    public int getObserverTag() {
        return this.TAG;
    }
}
