package org.telegram.ui.Cells;

import android.content.Context;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.C0553R;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.FileLocation;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.CheckBox;
import org.telegram.ui.Components.CheckBoxSquare;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.SimpleTextView;

public class UserCell extends FrameLayout {
    private AvatarDrawable avatarDrawable = new AvatarDrawable();
    private BackupImageView avatarImageView;
    private CheckBox checkBox;
    private CheckBoxSquare checkBoxBig;
    private int currentDrawable;
    private CharSequence currentName;
    private TLObject currentObject = null;
    private CharSequence currrntStatus;
    private ImageView imageView;
    private FileLocation lastAvatar = null;
    private String lastName = null;
    private int lastStatus = 0;
    private SimpleTextView nameTextView;
    private int statusColor = -5723992;
    private int statusOnlineColor = -12876608;
    private SimpleTextView statusTextView;

    public UserCell(Context context, int padding, int checkbox) {
        int i;
        int i2;
        int i3 = 5;
        int i4 = 3;
        super(context);
        this.avatarImageView = new BackupImageView(context);
        this.avatarImageView.setRoundRadius(AndroidUtilities.dp(24.0f));
        addView(this.avatarImageView, LayoutHelper.createFrame(48, 48.0f, (LocaleController.isRTL ? 5 : 3) | 48, LocaleController.isRTL ? 0.0f : (float) (padding + 7), 8.0f, LocaleController.isRTL ? (float) (padding + 7) : 0.0f, 0.0f));
        this.nameTextView = new SimpleTextView(context);
        this.nameTextView.setTextColor(-14606047);
        this.nameTextView.setTextSize(17);
        SimpleTextView simpleTextView = this.nameTextView;
        if (LocaleController.isRTL) {
            i = 5;
        } else {
            i = 3;
        }
        simpleTextView.setGravity(i | 48);
        View view = this.nameTextView;
        if (LocaleController.isRTL) {
            i2 = 5;
        } else {
            i2 = 3;
        }
        addView(view, LayoutHelper.createFrame(-1, 20.0f, i2 | 48, LocaleController.isRTL ? 28.0f : (float) (padding + 68), 11.5f, LocaleController.isRTL ? (float) (padding + 68) : 28.0f, 0.0f));
        this.statusTextView = new SimpleTextView(context);
        this.statusTextView.setTextSize(14);
        simpleTextView = this.statusTextView;
        if (LocaleController.isRTL) {
            i = 5;
        } else {
            i = 3;
        }
        simpleTextView.setGravity(i | 48);
        view = this.statusTextView;
        if (LocaleController.isRTL) {
            i2 = 5;
        } else {
            i2 = 3;
        }
        addView(view, LayoutHelper.createFrame(-1, 20.0f, i2 | 48, LocaleController.isRTL ? 28.0f : (float) (padding + 68), 34.5f, LocaleController.isRTL ? (float) (padding + 68) : 28.0f, 0.0f));
        this.imageView = new ImageView(context);
        this.imageView.setScaleType(ScaleType.CENTER);
        this.imageView.setVisibility(8);
        View view2 = this.imageView;
        if (LocaleController.isRTL) {
            i = 5;
        } else {
            i = 3;
        }
        addView(view2, LayoutHelper.createFrame(-2, -2.0f, i | 16, LocaleController.isRTL ? 0.0f : 16.0f, 0.0f, LocaleController.isRTL ? 16.0f : 0.0f, 0.0f));
        if (checkbox == 2) {
            this.checkBoxBig = new CheckBoxSquare(context);
            View view3 = this.checkBoxBig;
            if (!LocaleController.isRTL) {
                i4 = 5;
            }
            addView(view3, LayoutHelper.createFrame(18, 18.0f, i4 | 16, LocaleController.isRTL ? 19.0f : 0.0f, 0.0f, LocaleController.isRTL ? 0.0f : 19.0f, 0.0f));
        } else if (checkbox == 1) {
            this.checkBox = new CheckBox(context, C0553R.drawable.round_check2);
            this.checkBox.setVisibility(4);
            View view4 = this.checkBox;
            if (!LocaleController.isRTL) {
                i3 = 3;
            }
            addView(view4, LayoutHelper.createFrame(22, 22.0f, i3 | 48, LocaleController.isRTL ? 0.0f : (float) (padding + 37), 38.0f, LocaleController.isRTL ? (float) (padding + 37) : 0.0f, 0.0f));
        }
    }

    public void setData(TLObject user, CharSequence name, CharSequence status, int resId) {
        if (user == null) {
            this.currrntStatus = null;
            this.currentName = null;
            this.currentObject = null;
            this.nameTextView.setText("");
            this.statusTextView.setText("");
            this.avatarImageView.setImageDrawable(null);
            return;
        }
        this.currrntStatus = status;
        this.currentName = name;
        this.currentObject = user;
        this.currentDrawable = resId;
        update(0);
    }

    public void setChecked(boolean checked, boolean animated) {
        if (this.checkBox != null) {
            if (this.checkBox.getVisibility() != 0) {
                this.checkBox.setVisibility(0);
            }
            this.checkBox.setChecked(checked, animated);
        } else if (this.checkBoxBig != null) {
            if (this.checkBoxBig.getVisibility() != 0) {
                this.checkBoxBig.setVisibility(0);
            }
            this.checkBoxBig.setChecked(checked, animated);
        }
    }

    public void setCheckDisabled(boolean disabled) {
        if (this.checkBoxBig != null) {
            this.checkBoxBig.setDisabled(disabled);
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), 1073741824), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(64.0f), 1073741824));
    }

    public void setStatusColors(int color, int onlineColor) {
        this.statusColor = color;
        this.statusOnlineColor = onlineColor;
    }

    public void update(int mask) {
        int i = 8;
        if (this.currentObject != null) {
            TLObject photo = null;
            String newName = null;
            User currentUser = null;
            Chat currentChat = null;
            if (this.currentObject instanceof User) {
                currentUser = this.currentObject;
                if (currentUser.photo != null) {
                    photo = currentUser.photo.photo_small;
                }
            } else {
                currentChat = this.currentObject;
                if (currentChat.photo != null) {
                    photo = currentChat.photo.photo_small;
                }
            }
            if (mask != 0) {
                boolean continueUpdate = false;
                if ((mask & 2) != 0 && ((this.lastAvatar != null && photo == null) || !(this.lastAvatar != null || photo == null || this.lastAvatar == null || photo == null || (this.lastAvatar.volume_id == photo.volume_id && this.lastAvatar.local_id == photo.local_id)))) {
                    continueUpdate = true;
                }
                if (!(currentUser == null || continueUpdate || (mask & 4) == 0)) {
                    int newStatus = 0;
                    if (currentUser.status != null) {
                        newStatus = currentUser.status.expires;
                    }
                    if (newStatus != this.lastStatus) {
                        continueUpdate = true;
                    }
                }
                if (!(continueUpdate || this.currentName != null || this.lastName == null || (mask & 1) == 0)) {
                    if (currentUser != null) {
                        newName = UserObject.getUserName(currentUser);
                    } else {
                        newName = currentChat.title;
                    }
                    if (!newName.equals(this.lastName)) {
                        continueUpdate = true;
                    }
                }
                if (!continueUpdate) {
                    return;
                }
            }
            if (currentUser != null) {
                this.avatarDrawable.setInfo(currentUser);
                if (currentUser.status != null) {
                    this.lastStatus = currentUser.status.expires;
                } else {
                    this.lastStatus = 0;
                }
            } else {
                this.avatarDrawable.setInfo(currentChat);
            }
            if (this.currentName != null) {
                this.lastName = null;
                this.nameTextView.setText(this.currentName);
            } else {
                if (currentUser != null) {
                    if (newName == null) {
                        newName = UserObject.getUserName(currentUser);
                    }
                    this.lastName = newName;
                } else {
                    if (newName == null) {
                        newName = currentChat.title;
                    }
                    this.lastName = newName;
                }
                this.nameTextView.setText(this.lastName);
            }
            if (this.currrntStatus != null) {
                this.statusTextView.setTextColor(this.statusColor);
                this.statusTextView.setText(this.currrntStatus);
            } else if (currentUser != null) {
                if (currentUser.bot) {
                    this.statusTextView.setTextColor(this.statusColor);
                    if (currentUser.bot_chat_history) {
                        this.statusTextView.setText(LocaleController.getString("BotStatusRead", C0553R.string.BotStatusRead));
                    } else {
                        this.statusTextView.setText(LocaleController.getString("BotStatusCantRead", C0553R.string.BotStatusCantRead));
                    }
                } else if (currentUser.id == UserConfig.getClientUserId() || ((currentUser.status != null && currentUser.status.expires > ConnectionsManager.getInstance().getCurrentTime()) || MessagesController.getInstance().onlinePrivacy.containsKey(Integer.valueOf(currentUser.id)))) {
                    this.statusTextView.setTextColor(this.statusOnlineColor);
                    this.statusTextView.setText(LocaleController.getString("Online", C0553R.string.Online));
                } else {
                    this.statusTextView.setTextColor(this.statusColor);
                    this.statusTextView.setText(LocaleController.formatUserStatus(currentUser));
                }
            }
            if ((this.imageView.getVisibility() == 0 && this.currentDrawable == 0) || (this.imageView.getVisibility() == 8 && this.currentDrawable != 0)) {
                ImageView imageView = this.imageView;
                if (this.currentDrawable != 0) {
                    i = 0;
                }
                imageView.setVisibility(i);
                this.imageView.setImageResource(this.currentDrawable);
            }
            this.avatarImageView.setImage(photo, "50_50", this.avatarDrawable);
        }
    }

    public boolean hasOverlappingRendering() {
        return false;
    }
}
