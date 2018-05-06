package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.view.MotionEvent;
import android.view.View.MeasureSpec;
import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.C0553R;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC.FileLocation;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.Components.AvatarDrawable;

public class ChatContactCell extends ChatBaseCell {
    private static Drawable addContactDrawableIn;
    private static Drawable addContactDrawableOut;
    private static TextPaint namePaint;
    private static TextPaint phonePaint;
    private AvatarDrawable avatarDrawable;
    private ImageReceiver avatarImage;
    private boolean avatarPressed = false;
    private boolean buttonPressed = false;
    private ChatContactCellDelegate contactDelegate = null;
    private User contactUser;
    private FileLocation currentPhoto;
    private boolean drawAddButton = false;
    private StaticLayout nameLayout;
    private int namesWidth = 0;
    private StaticLayout phoneLayout;

    public interface ChatContactCellDelegate {
        void didClickAddButton(ChatContactCell chatContactCell, User user);

        void didClickPhone(ChatContactCell chatContactCell);
    }

    public ChatContactCell(Context context) {
        super(context);
        if (namePaint == null) {
            namePaint = new TextPaint(1);
            namePaint.setTextSize((float) AndroidUtilities.dp(15.0f));
            phonePaint = new TextPaint(1);
            phonePaint.setTextSize((float) AndroidUtilities.dp(15.0f));
            phonePaint.setColor(-14606047);
            addContactDrawableIn = getResources().getDrawable(C0553R.drawable.addcontact_blue);
            addContactDrawableOut = getResources().getDrawable(C0553R.drawable.addcontact_green);
        }
        this.avatarImage = new ImageReceiver(this);
        this.avatarImage.setRoundRadius(AndroidUtilities.dp(21.0f));
        this.avatarDrawable = new AvatarDrawable();
    }

    public void setContactDelegate(ChatContactCellDelegate delegate) {
        this.contactDelegate = delegate;
    }

    protected boolean isUserDataChanged() {
        if (this.currentMessageObject == null) {
            return false;
        }
        boolean newDrawAdd;
        int uid = this.currentMessageObject.messageOwner.media.user_id;
        if (this.contactUser == null || uid == UserConfig.getClientUserId() || ContactsController.getInstance().contactsDict.get(uid) != null) {
            newDrawAdd = false;
        } else {
            newDrawAdd = true;
        }
        if (newDrawAdd != this.drawAddButton) {
            return true;
        }
        this.contactUser = MessagesController.getInstance().getUser(Integer.valueOf(this.currentMessageObject.messageOwner.media.user_id));
        FileLocation newPhoto = null;
        if (!(this.contactUser == null || this.contactUser.photo == null)) {
            newPhoto = this.contactUser.photo.photo_small;
        }
        if ((this.currentPhoto != null || newPhoto == null) && ((this.currentPhoto == null || newPhoto != null) && ((this.currentPhoto == null || newPhoto == null || (this.currentPhoto.local_id == newPhoto.local_id && this.currentPhoto.volume_id == newPhoto.volume_id)) && !super.isUserDataChanged()))) {
            return false;
        }
        return true;
    }

    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        boolean result = false;
        if (event.getAction() == 0) {
            if (x >= ((float) this.avatarImage.getImageX()) && x <= ((float) ((this.avatarImage.getImageX() + this.namesWidth) + AndroidUtilities.dp(42.0f))) && y >= ((float) this.avatarImage.getImageY()) && y <= ((float) (this.avatarImage.getImageY() + this.avatarImage.getImageHeight()))) {
                this.avatarPressed = true;
                result = true;
            } else if (x >= ((float) ((this.avatarImage.getImageX() + this.namesWidth) + AndroidUtilities.dp(52.0f))) && y >= ((float) (AndroidUtilities.dp(13.0f) + this.namesOffset)) && x <= ((float) ((this.avatarImage.getImageX() + this.namesWidth) + AndroidUtilities.dp(92.0f))) && y <= ((float) (AndroidUtilities.dp(52.0f) + this.namesOffset))) {
                this.buttonPressed = true;
                result = true;
            }
            if (result) {
                startCheckLongPress();
            }
        } else {
            if (event.getAction() != 2) {
                cancelCheckLongPress();
            }
            if (this.avatarPressed) {
                if (event.getAction() == 1) {
                    this.avatarPressed = false;
                    playSoundEffect(0);
                    if (this.contactUser != null) {
                        if (this.delegate != null) {
                            this.delegate.didPressedUserAvatar(this, this.contactUser);
                        }
                    } else if (this.contactDelegate != null) {
                        this.contactDelegate.didClickPhone(this);
                    }
                } else if (event.getAction() == 3) {
                    this.avatarPressed = false;
                } else if (event.getAction() == 2 && (x < ((float) this.avatarImage.getImageX()) || x > ((float) ((this.avatarImage.getImageX() + this.namesWidth) + AndroidUtilities.dp(42.0f))) || y < ((float) this.avatarImage.getImageY()) || y > ((float) (this.avatarImage.getImageY() + this.avatarImage.getImageHeight())))) {
                    this.avatarPressed = false;
                }
            } else if (this.buttonPressed) {
                if (event.getAction() == 1) {
                    this.buttonPressed = false;
                    playSoundEffect(0);
                    if (!(this.contactUser == null || this.contactDelegate == null)) {
                        this.contactDelegate.didClickAddButton(this, this.contactUser);
                    }
                } else if (event.getAction() == 3) {
                    this.buttonPressed = false;
                } else if (event.getAction() == 2 && (x < ((float) ((this.avatarImage.getImageX() + this.namesWidth) + AndroidUtilities.dp(52.0f))) || y < ((float) (AndroidUtilities.dp(13.0f) + this.namesOffset)) || x > ((float) ((this.avatarImage.getImageX() + this.namesWidth) + AndroidUtilities.dp(92.0f))) || y > ((float) (AndroidUtilities.dp(52.0f) + this.namesOffset)))) {
                    this.buttonPressed = false;
                }
            }
        }
        if (result) {
            return result;
        }
        return super.onTouchEvent(event);
    }

    public void setMessageObject(MessageObject messageObject) {
        if (this.currentMessageObject != messageObject || isUserDataChanged()) {
            int maxWidth;
            int uid = messageObject.messageOwner.media.user_id;
            this.contactUser = MessagesController.getInstance().getUser(Integer.valueOf(uid));
            boolean z = (this.contactUser == null || uid == UserConfig.getClientUserId() || ContactsController.getInstance().contactsDict.get(uid) != null) ? false : true;
            this.drawAddButton = z;
            if (AndroidUtilities.isTablet()) {
                maxWidth = (int) (((float) AndroidUtilities.getMinTabletSide()) * 0.7f);
            } else {
                maxWidth = (int) (((float) Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y)) * 0.7f);
            }
            maxWidth -= AndroidUtilities.dp((float) ((this.drawAddButton ? 42 : 0) + 58));
            if (this.contactUser != null) {
                if (this.contactUser.photo != null) {
                    this.currentPhoto = this.contactUser.photo.photo_small;
                } else {
                    this.currentPhoto = null;
                }
                this.avatarDrawable.setInfo(this.contactUser);
            } else {
                this.currentPhoto = null;
                this.avatarDrawable.setInfo(uid, null, null, false);
            }
            this.avatarImage.setImage(this.currentPhoto, "50_50", this.avatarDrawable, null, false);
            String currentNameString = ContactsController.formatName(messageObject.messageOwner.media.first_name, messageObject.messageOwner.media.last_name);
            int nameWidth = Math.min((int) Math.ceil((double) namePaint.measureText(currentNameString)), maxWidth);
            if (maxWidth < 0) {
                maxWidth = AndroidUtilities.dp(100.0f);
            }
            this.nameLayout = new StaticLayout(TextUtils.ellipsize(currentNameString.replace("\n", " "), namePaint, (float) nameWidth, TruncateAt.END), namePaint, nameWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            if (this.nameLayout.getLineCount() > 0) {
                nameWidth = (int) Math.ceil((double) this.nameLayout.getLineWidth(0));
            } else {
                nameWidth = 0;
            }
            String phone = messageObject.messageOwner.media.phone_number;
            if (phone == null || phone.length() == 0) {
                phone = LocaleController.getString("NumberUnknown", C0553R.string.NumberUnknown);
            } else {
                if (!phone.startsWith("+")) {
                    phone = "+" + phone;
                }
                phone = PhoneFormat.getInstance().format(phone);
            }
            int phoneWidth = Math.min((int) Math.ceil((double) phonePaint.measureText(phone)), maxWidth);
            this.phoneLayout = new StaticLayout(TextUtils.ellipsize(phone.replace("\n", " "), phonePaint, (float) phoneWidth, TruncateAt.END), phonePaint, phoneWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            if (this.phoneLayout.getLineCount() > 0) {
                phoneWidth = (int) Math.ceil((double) this.phoneLayout.getLineWidth(0));
            } else {
                phoneWidth = 0;
            }
            this.namesWidth = Math.max(nameWidth, phoneWidth);
            this.backgroundWidth = AndroidUtilities.dp((float) ((this.drawAddButton ? 42 : 0) + 77)) + this.namesWidth;
            super.setMessageObject(messageObject);
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), AndroidUtilities.dp(71.0f) + this.namesOffset);
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (this.currentMessageObject != null) {
            int x;
            if (this.currentMessageObject.isOutOwner()) {
                x = (this.layoutWidth - this.backgroundWidth) + AndroidUtilities.dp(8.0f);
            } else if (!this.isChat || this.currentMessageObject.messageOwner.from_id <= 0) {
                x = AndroidUtilities.dp(16.0f);
            } else {
                x = AndroidUtilities.dp(69.0f);
            }
            this.avatarImage.setImageCoords(x, AndroidUtilities.dp(9.0f) + this.namesOffset, AndroidUtilities.dp(42.0f), AndroidUtilities.dp(42.0f));
        }
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.currentMessageObject != null) {
            this.avatarImage.draw(canvas);
            if (this.nameLayout != null) {
                canvas.save();
                canvas.translate((float) ((this.avatarImage.getImageX() + this.avatarImage.getImageWidth()) + AndroidUtilities.dp(9.0f)), (float) (AndroidUtilities.dp(10.0f) + this.namesOffset));
                namePaint.setColor(AvatarDrawable.getColorForId(this.currentMessageObject.messageOwner.media.user_id));
                this.nameLayout.draw(canvas);
                canvas.restore();
            }
            if (this.phoneLayout != null) {
                canvas.save();
                canvas.translate((float) ((this.avatarImage.getImageX() + this.avatarImage.getImageWidth()) + AndroidUtilities.dp(9.0f)), (float) (AndroidUtilities.dp(31.0f) + this.namesOffset));
                this.phoneLayout.draw(canvas);
                canvas.restore();
            }
            if (this.drawAddButton) {
                Drawable addContactDrawable;
                if (this.currentMessageObject.isOutOwner()) {
                    addContactDrawable = addContactDrawableOut;
                } else {
                    addContactDrawable = addContactDrawableIn;
                }
                setDrawableBounds(addContactDrawable, (this.avatarImage.getImageX() + this.namesWidth) + AndroidUtilities.dp(78.0f), AndroidUtilities.dp(13.0f) + this.namesOffset);
                addContactDrawable.draw(canvas);
            }
        }
    }
}
