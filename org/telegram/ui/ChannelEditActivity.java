package org.telegram.ui;

import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import java.util.concurrent.Semaphore;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.C0553R;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.ChatFull;
import org.telegram.tgnet.TLRPC.FileLocation;
import org.telegram.tgnet.TLRPC.InputFile;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.TL_boolTrue;
import org.telegram.tgnet.TLRPC.TL_channels_checkUsername;
import org.telegram.tgnet.TLRPC.TL_chatPhoto;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.AvatarUpdater;
import org.telegram.ui.Components.AvatarUpdater.AvatarUpdaterDelegate;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.FrameLayoutFixed;
import org.telegram.ui.Components.LayoutHelper;

public class ChannelEditActivity extends BaseFragment implements AvatarUpdaterDelegate, NotificationCenterDelegate {
    private static final int done_button = 1;
    private boolean allowComments = true;
    private FileLocation avatar;
    private AvatarDrawable avatarDrawable = new AvatarDrawable();
    private BackupImageView avatarImage;
    private AvatarUpdater avatarUpdater = new AvatarUpdater();
    private int chatId;
    private int checkReqId = 0;
    private Runnable checkRunnable = null;
    private TextView checkTextView;
    private boolean createAfterUpload;
    private Chat currentChat;
    private EditText descriptionTextView;
    private View doneButton;
    private boolean donePressed;
    private ChatFull info;
    private String lastCheckName = null;
    private boolean lastNameAvailable = false;
    private EditText nameTextView;
    private boolean privateAlertShown;
    private ProgressDialog progressDialog = null;
    private InputFile uploadedAvatar;
    private EditText userNameTextView;
    private boolean wasPrivate;

    class C08433 implements OnClickListener {

        class C08421 implements DialogInterface.OnClickListener {
            C08421() {
            }

            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    ChannelEditActivity.this.avatarUpdater.openCamera();
                } else if (i == 1) {
                    ChannelEditActivity.this.avatarUpdater.openGallery();
                } else if (i == 2) {
                    ChannelEditActivity.this.avatar = null;
                    ChannelEditActivity.this.uploadedAvatar = null;
                    ChannelEditActivity.this.avatarImage.setImage(ChannelEditActivity.this.avatar, "50_50", ChannelEditActivity.this.avatarDrawable);
                }
            }
        }

        C08433() {
        }

        public void onClick(View view) {
            if (ChannelEditActivity.this.getParentActivity() != null) {
                Builder builder = new Builder(ChannelEditActivity.this.getParentActivity());
                builder.setItems(ChannelEditActivity.this.avatar != null ? new CharSequence[]{LocaleController.getString("FromCamera", C0553R.string.FromCamera), LocaleController.getString("FromGalley", C0553R.string.FromGalley), LocaleController.getString("DeletePhoto", C0553R.string.DeletePhoto)} : new CharSequence[]{LocaleController.getString("FromCamera", C0553R.string.FromCamera), LocaleController.getString("FromGalley", C0553R.string.FromGalley)}, new C08421());
                ChannelEditActivity.this.showDialog(builder.create());
            }
        }
    }

    class C08444 implements TextWatcher {
        C08444() {
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        public void afterTextChanged(Editable s) {
            String obj;
            AvatarDrawable access$1500 = ChannelEditActivity.this.avatarDrawable;
            if (ChannelEditActivity.this.nameTextView.length() > 0) {
                obj = ChannelEditActivity.this.nameTextView.getText().toString();
            } else {
                obj = null;
            }
            access$1500.setInfo(5, obj, null, false);
            ChannelEditActivity.this.avatarImage.invalidate();
        }
    }

    class C08455 implements OnEditorActionListener {
        C08455() {
        }

        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
            if (i != 6 || ChannelEditActivity.this.doneButton == null) {
                return false;
            }
            ChannelEditActivity.this.doneButton.performClick();
            return true;
        }
    }

    class C08466 implements TextWatcher {
        C08466() {
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        public void afterTextChanged(Editable editable) {
        }
    }

    class C08477 implements OnFocusChangeListener {
        C08477() {
        }

        public void onFocusChange(View v, boolean hasFocus) {
            if (ChannelEditActivity.this.wasPrivate && hasFocus && !ChannelEditActivity.this.privateAlertShown) {
                Builder builder = new Builder(ChannelEditActivity.this.getParentActivity());
                builder.setTitle(LocaleController.getString("AppName", C0553R.string.AppName));
                builder.setMessage(LocaleController.getString("ChannelWasPrivateAlert", C0553R.string.ChannelWasPrivateAlert));
                builder.setPositiveButton(LocaleController.getString("Close", C0553R.string.Close), null);
                ChannelEditActivity.this.showDialog(builder.create());
            }
        }
    }

    class C08488 implements TextWatcher {
        C08488() {
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            ChannelEditActivity.this.checkUserName(ChannelEditActivity.this.userNameTextView.getText().toString(), false);
        }

        public void afterTextChanged(Editable editable) {
        }
    }

    class C08509 implements OnClickListener {

        class C08491 implements DialogInterface.OnClickListener {
            C08491() {
            }

            public void onClick(DialogInterface dialogInterface, int i) {
                NotificationCenter.getInstance().removeObserver(this, NotificationCenter.closeChats);
                if (AndroidUtilities.isTablet()) {
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats, Long.valueOf(-((long) ChannelEditActivity.this.chatId)));
                } else {
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats, new Object[0]);
                }
                MessagesController.getInstance().deleteUserFromChat(ChannelEditActivity.this.chatId, MessagesController.getInstance().getUser(Integer.valueOf(UserConfig.getClientUserId())), ChannelEditActivity.this.info);
                ChannelEditActivity.this.finishFragment();
            }
        }

        C08509() {
        }

        public void onClick(View v) {
            Builder builder = new Builder(ChannelEditActivity.this.getParentActivity());
            if (ChannelEditActivity.this.currentChat.megagroup) {
                builder.setMessage(LocaleController.getString("MegaDeleteAlert", C0553R.string.MegaDeleteAlert));
            } else {
                builder.setMessage(LocaleController.getString("ChannelDeleteAlert", C0553R.string.ChannelDeleteAlert));
            }
            builder.setTitle(LocaleController.getString("AppName", C0553R.string.AppName));
            builder.setPositiveButton(LocaleController.getString("OK", C0553R.string.OK), new C08491());
            builder.setNegativeButton(LocaleController.getString("Cancel", C0553R.string.Cancel), null);
            ChannelEditActivity.this.showDialog(builder.create());
        }
    }

    class C15312 extends ActionBarMenuOnItemClick {

        class C08411 implements DialogInterface.OnClickListener {
            C08411() {
            }

            public void onClick(DialogInterface dialog, int which) {
                ChannelEditActivity.this.createAfterUpload = false;
                ChannelEditActivity.this.progressDialog = null;
                ChannelEditActivity.this.donePressed = false;
                try {
                    dialog.dismiss();
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
        }

        C15312() {
        }

        public void onItemClick(int id) {
            boolean currentAllowComments = true;
            if (id == -1) {
                ChannelEditActivity.this.finishFragment();
            } else if (id == 1 && !ChannelEditActivity.this.donePressed) {
                ChannelEditActivity.this.donePressed = true;
                Vibrator v;
                if (ChannelEditActivity.this.nameTextView.length() == 0) {
                    v = (Vibrator) ChannelEditActivity.this.getParentActivity().getSystemService("vibrator");
                    if (v != null) {
                        v.vibrate(200);
                    }
                    AndroidUtilities.shakeView(ChannelEditActivity.this.nameTextView, 2.0f, 0);
                } else if (ChannelEditActivity.this.userNameTextView != null && (((ChannelEditActivity.this.currentChat.username == null && ChannelEditActivity.this.userNameTextView.length() != 0) || (ChannelEditActivity.this.currentChat.username != null && !ChannelEditActivity.this.currentChat.username.equalsIgnoreCase(ChannelEditActivity.this.userNameTextView.getText().toString()))) && ChannelEditActivity.this.userNameTextView.length() != 0 && !ChannelEditActivity.this.lastNameAvailable)) {
                    v = (Vibrator) ChannelEditActivity.this.getParentActivity().getSystemService("vibrator");
                    if (v != null) {
                        v.vibrate(200);
                    }
                    AndroidUtilities.shakeView(ChannelEditActivity.this.checkTextView, 2.0f, 0);
                } else if (ChannelEditActivity.this.avatarUpdater.uploadingAvatar != null) {
                    ChannelEditActivity.this.createAfterUpload = true;
                    ChannelEditActivity.this.progressDialog = new ProgressDialog(ChannelEditActivity.this.getParentActivity());
                    ChannelEditActivity.this.progressDialog.setMessage(LocaleController.getString("Loading", C0553R.string.Loading));
                    ChannelEditActivity.this.progressDialog.setCanceledOnTouchOutside(false);
                    ChannelEditActivity.this.progressDialog.setCancelable(false);
                    ChannelEditActivity.this.progressDialog.setButton(-2, LocaleController.getString("Cancel", C0553R.string.Cancel), new C08411());
                    ChannelEditActivity.this.progressDialog.show();
                } else {
                    if (ChannelEditActivity.this.currentChat.broadcast) {
                        currentAllowComments = false;
                    }
                    if (ChannelEditActivity.this.allowComments != currentAllowComments) {
                        MessagesController.getInstance().toogleChannelComments(ChannelEditActivity.this.chatId, ChannelEditActivity.this.allowComments);
                    }
                    if (!ChannelEditActivity.this.currentChat.title.equals(ChannelEditActivity.this.nameTextView.getText().toString())) {
                        MessagesController.getInstance().changeChatTitle(ChannelEditActivity.this.chatId, ChannelEditActivity.this.nameTextView.getText().toString());
                    }
                    if (!(ChannelEditActivity.this.info == null || ChannelEditActivity.this.info.about.equals(ChannelEditActivity.this.descriptionTextView.getText().toString()))) {
                        MessagesController.getInstance().updateChannelAbout(ChannelEditActivity.this.chatId, ChannelEditActivity.this.descriptionTextView.getText().toString(), ChannelEditActivity.this.info);
                    }
                    if (ChannelEditActivity.this.userNameTextView != null) {
                        if (!(ChannelEditActivity.this.currentChat.username != null ? ChannelEditActivity.this.currentChat.username : "").equals(ChannelEditActivity.this.userNameTextView.getText().toString())) {
                            MessagesController.getInstance().updateChannelUserName(ChannelEditActivity.this.chatId, ChannelEditActivity.this.userNameTextView.getText().toString());
                        }
                    }
                    if (ChannelEditActivity.this.uploadedAvatar != null) {
                        MessagesController.getInstance().changeChatAvatar(ChannelEditActivity.this.chatId, ChannelEditActivity.this.uploadedAvatar);
                    } else if (ChannelEditActivity.this.avatar == null && (ChannelEditActivity.this.currentChat.photo instanceof TL_chatPhoto)) {
                        MessagesController.getInstance().changeChatAvatar(ChannelEditActivity.this.chatId, null);
                    }
                    ChannelEditActivity.this.finishFragment();
                }
            }
        }
    }

    public ChannelEditActivity(Bundle args) {
        super(args);
        this.chatId = args.getInt("chat_id", 0);
    }

    public boolean onFragmentCreate() {
        boolean z;
        boolean z2 = true;
        this.currentChat = MessagesController.getInstance().getChat(Integer.valueOf(this.chatId));
        if (this.currentChat == null) {
            final Semaphore semaphore = new Semaphore(0);
            MessagesStorage.getInstance().getStorageQueue().postRunnable(new Runnable() {
                public void run() {
                    ChannelEditActivity.this.currentChat = MessagesStorage.getInstance().getChat(ChannelEditActivity.this.chatId);
                    semaphore.release();
                }
            });
            try {
                semaphore.acquire();
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
            if (this.currentChat == null) {
                return false;
            }
            MessagesController.getInstance().putChat(this.currentChat, true);
            if (this.info == null) {
                MessagesStorage.getInstance().loadChatInfo(this.chatId, semaphore, false, false);
                try {
                    semaphore.acquire();
                } catch (Throwable e2) {
                    FileLog.m611e("tmessages", e2);
                }
                if (this.info == null) {
                    return false;
                }
            }
        }
        if (this.currentChat.username == null || this.currentChat.username.length() == 0) {
            z = true;
        } else {
            z = false;
        }
        this.wasPrivate = z;
        this.avatarUpdater.parentFragment = this;
        this.avatarUpdater.delegate = this;
        if (this.currentChat.broadcast) {
            z2 = false;
        }
        this.allowComments = z2;
        return super.onFragmentCreate();
    }

    public void setInfo(ChatFull chatFull) {
        this.info = chatFull;
    }

    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        if (this.avatarUpdater != null) {
            this.avatarUpdater.clear();
        }
        AndroidUtilities.removeAdjustResize(getParentActivity(), this.classGuid);
    }

    public void onResume() {
        super.onResume();
        AndroidUtilities.requestAdjustResize(getParentActivity(), this.classGuid);
    }

    public View createView(Context context) {
        float f;
        float f2;
        View linearLayout;
        this.actionBar.setBackButtonImage(C0553R.drawable.ic_ab_back);
        this.actionBar.setAllowOverlayTitle(true);
        this.actionBar.setActionBarMenuOnItemClick(new C15312());
        this.doneButton = this.actionBar.createMenu().addItemWithWidth(1, C0553R.drawable.ic_done, AndroidUtilities.dp(56.0f));
        this.fragmentView = new ScrollView(context);
        this.fragmentView.setBackgroundColor(-986896);
        ScrollView scrollView = (ScrollView) this.fragmentView;
        scrollView.setFillViewport(true);
        LinearLayout linearLayout2 = new LinearLayout(context);
        scrollView.addView(linearLayout2, new LayoutParams(-1, -2));
        linearLayout2.setOrientation(1);
        this.actionBar.setTitle(LocaleController.getString("ChannelEdit", C0553R.string.ChannelEdit));
        LinearLayout linearLayout22 = new LinearLayout(context);
        linearLayout22.setOrientation(1);
        linearLayout22.setBackgroundColor(-1);
        linearLayout2.addView(linearLayout22, LayoutHelper.createLinear(-1, -2));
        FrameLayout frameLayout = new FrameLayoutFixed(context);
        linearLayout22.addView(frameLayout, LayoutHelper.createLinear(-1, -2));
        this.avatarImage = new BackupImageView(context);
        this.avatarImage.setRoundRadius(AndroidUtilities.dp(32.0f));
        this.avatarDrawable.setInfo(5, null, null, false);
        this.avatarDrawable.setDrawPhoto(true);
        View view = this.avatarImage;
        int i = (LocaleController.isRTL ? 5 : 3) | 48;
        if (LocaleController.isRTL) {
            f = 0.0f;
        } else {
            f = 16.0f;
        }
        if (LocaleController.isRTL) {
            f2 = 16.0f;
        } else {
            f2 = 0.0f;
        }
        frameLayout.addView(view, LayoutHelper.createFrame(64, 64.0f, i, f, 12.0f, f2, 12.0f));
        this.avatarImage.setOnClickListener(new C08433());
        this.nameTextView = new EditText(context);
        if (this.currentChat.megagroup) {
            this.nameTextView.setHint(LocaleController.getString("GroupName", C0553R.string.GroupName));
        } else {
            this.nameTextView.setHint(LocaleController.getString("EnterChannelName", C0553R.string.EnterChannelName));
        }
        this.nameTextView.setMaxLines(4);
        this.nameTextView.setGravity((LocaleController.isRTL ? 5 : 3) | 16);
        this.nameTextView.setTextSize(1, 16.0f);
        this.nameTextView.setHintTextColor(-6842473);
        this.nameTextView.setImeOptions(268435456);
        this.nameTextView.setInputType(16385);
        this.nameTextView.setPadding(0, 0, 0, AndroidUtilities.dp(8.0f));
        this.nameTextView.setFilters(new InputFilter[]{new LengthFilter(100)});
        AndroidUtilities.clearCursorDrawable(this.nameTextView);
        this.nameTextView.setTextColor(-14606047);
        view = this.nameTextView;
        f = LocaleController.isRTL ? 16.0f : 96.0f;
        if (LocaleController.isRTL) {
            f2 = 96.0f;
        } else {
            f2 = 16.0f;
        }
        frameLayout.addView(view, LayoutHelper.createFrame(-1, -2.0f, 16, f, 0.0f, f2, 0.0f));
        this.nameTextView.addTextChangedListener(new C08444());
        linearLayout2.addView(new ShadowSectionCell(context), LayoutHelper.createLinear(-1, -2));
        linearLayout22 = new LinearLayout(context);
        linearLayout22.setOrientation(1);
        linearLayout22.setBackgroundColor(-1);
        linearLayout2.addView(linearLayout22, LayoutHelper.createLinear(-1, -2));
        this.descriptionTextView = new EditText(context);
        this.descriptionTextView.setTextSize(1, 18.0f);
        this.descriptionTextView.setHintTextColor(-6842473);
        this.descriptionTextView.setTextColor(-14606047);
        this.descriptionTextView.setPadding(0, 0, 0, AndroidUtilities.dp(6.0f));
        this.descriptionTextView.setBackgroundDrawable(null);
        this.descriptionTextView.setGravity(LocaleController.isRTL ? 5 : 3);
        this.descriptionTextView.setInputType(180225);
        this.descriptionTextView.setImeOptions(6);
        this.descriptionTextView.setFilters(new InputFilter[]{new LengthFilter(120)});
        this.descriptionTextView.setHint(LocaleController.getString("DescriptionPlaceholder", C0553R.string.DescriptionPlaceholder));
        AndroidUtilities.clearCursorDrawable(this.descriptionTextView);
        linearLayout22.addView(this.descriptionTextView, LayoutHelper.createLinear(-1, -2, 17.0f, 12.0f, 17.0f, 6.0f));
        this.descriptionTextView.setOnEditorActionListener(new C08455());
        this.descriptionTextView.addTextChangedListener(new C08466());
        TextInfoPrivacyCell infoCell = new TextInfoPrivacyCell(context);
        if (this.currentChat.megagroup) {
            infoCell.setText(LocaleController.getString("DescriptionInfoMega", C0553R.string.DescriptionInfoMega));
        } else {
            infoCell.setText(LocaleController.getString("DescriptionInfo", C0553R.string.DescriptionInfo));
        }
        infoCell.setBackgroundResource(C0553R.drawable.greydivider);
        linearLayout2.addView(infoCell, LayoutHelper.createLinear(-1, -2));
        if (!this.currentChat.megagroup) {
            linearLayout22 = new LinearLayout(context);
            linearLayout22.setOrientation(1);
            linearLayout22.setBackgroundColor(-1);
            linearLayout22.setPadding(0, 0, 0, AndroidUtilities.dp(7.0f));
            linearLayout2.addView(linearLayout22, LayoutHelper.createLinear(-1, -2));
            linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(0);
            linearLayout22.addView(linearLayout, LayoutHelper.createLinear(-1, 36, 17.0f, 7.0f, 17.0f, 0.0f));
            EditText editText = new EditText(context);
            editText.setText("telegram.me/");
            editText.setTextSize(1, 18.0f);
            editText.setHintTextColor(-6842473);
            editText.setTextColor(-14606047);
            editText.setMaxLines(1);
            editText.setLines(1);
            editText.setEnabled(false);
            editText.setBackgroundDrawable(null);
            editText.setPadding(0, 0, 0, 0);
            editText.setSingleLine(true);
            editText.setInputType(163840);
            editText.setImeOptions(6);
            linearLayout.addView(editText, LayoutHelper.createLinear(-2, 36));
            this.userNameTextView = new EditText(context);
            this.userNameTextView.setTextSize(1, 18.0f);
            this.userNameTextView.setHintTextColor(-6842473);
            this.userNameTextView.setTextColor(-14606047);
            this.userNameTextView.setMaxLines(1);
            this.userNameTextView.setLines(1);
            this.userNameTextView.setBackgroundDrawable(null);
            this.userNameTextView.setPadding(0, 0, 0, 0);
            this.userNameTextView.setSingleLine(true);
            this.userNameTextView.setText(this.currentChat.username);
            this.userNameTextView.setInputType(163872);
            this.userNameTextView.setImeOptions(6);
            this.userNameTextView.setHint(LocaleController.getString("ChannelUsernamePlaceholder", C0553R.string.ChannelUsernamePlaceholder));
            this.userNameTextView.setOnFocusChangeListener(new C08477());
            AndroidUtilities.clearCursorDrawable(this.userNameTextView);
            linearLayout.addView(this.userNameTextView, LayoutHelper.createLinear(-1, 36));
            this.userNameTextView.addTextChangedListener(new C08488());
            this.checkTextView = new TextView(context);
            this.checkTextView.setTextSize(1, 15.0f);
            this.checkTextView.setGravity(LocaleController.isRTL ? 5 : 3);
            this.checkTextView.setVisibility(8);
            linearLayout22.addView(this.checkTextView, LayoutHelper.createLinear(-2, -2, LocaleController.isRTL ? 5 : 3, 17, 3, 17, 7));
            infoCell = new TextInfoPrivacyCell(context);
            infoCell.setBackgroundResource(C0553R.drawable.greydivider);
            infoCell.setText(LocaleController.getString("ChannelUsernameHelp", C0553R.string.ChannelUsernameHelp));
            linearLayout2.addView(infoCell, LayoutHelper.createLinear(-1, -2));
        }
        frameLayout = new FrameLayoutFixed(context);
        frameLayout.setBackgroundColor(-1);
        linearLayout2.addView(frameLayout, LayoutHelper.createLinear(-1, -2));
        linearLayout = new TextSettingsCell(context);
        linearLayout.setTextColor(-1229511);
        linearLayout.setBackgroundResource(C0553R.drawable.list_selector);
        if (this.currentChat.megagroup) {
            linearLayout.setText(LocaleController.getString("DeleteMega", C0553R.string.DeleteMega), false);
        } else {
            linearLayout.setText(LocaleController.getString("ChannelDelete", C0553R.string.ChannelDelete), false);
        }
        frameLayout.addView(linearLayout, LayoutHelper.createFrame(-1, -2.0f));
        linearLayout.setOnClickListener(new C08509());
        infoCell = new TextInfoPrivacyCell(context);
        infoCell.setBackgroundResource(C0553R.drawable.greydivider_bottom);
        if (this.currentChat.megagroup) {
            infoCell.setText(LocaleController.getString("MegaDeleteInfo", C0553R.string.MegaDeleteInfo));
        } else {
            infoCell.setText(LocaleController.getString("ChannelDeleteInfo", C0553R.string.ChannelDeleteInfo));
        }
        linearLayout2.addView(infoCell, LayoutHelper.createLinear(-1, -2));
        this.nameTextView.setText(this.currentChat.title);
        this.nameTextView.setSelection(this.nameTextView.length());
        if (this.info != null) {
            this.descriptionTextView.setText(this.info.about);
        }
        if (this.currentChat.photo != null) {
            this.avatar = this.currentChat.photo.photo_small;
            this.avatarImage.setImage(this.avatar, "50_50", this.avatarDrawable);
        } else {
            this.avatarImage.setImageDrawable(this.avatarDrawable);
        }
        return this.fragmentView;
    }

    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.chatInfoDidLoaded) {
            ChatFull chatFull = args[0];
            if (chatFull.id == this.chatId) {
                if (this.info == null) {
                    this.descriptionTextView.setText(chatFull.about);
                }
                this.info = chatFull;
            }
        }
    }

    public void didUploadedPhoto(final InputFile file, final PhotoSize small, PhotoSize big) {
        AndroidUtilities.runOnUIThread(new Runnable() {
            public void run() {
                ChannelEditActivity.this.uploadedAvatar = file;
                ChannelEditActivity.this.avatar = small.location;
                ChannelEditActivity.this.avatarImage.setImage(ChannelEditActivity.this.avatar, "50_50", ChannelEditActivity.this.avatarDrawable);
                if (ChannelEditActivity.this.createAfterUpload) {
                    try {
                        if (ChannelEditActivity.this.progressDialog != null && ChannelEditActivity.this.progressDialog.isShowing()) {
                            ChannelEditActivity.this.progressDialog.dismiss();
                            ChannelEditActivity.this.progressDialog = null;
                        }
                    } catch (Throwable e) {
                        FileLog.m611e("tmessages", e);
                    }
                    ChannelEditActivity.this.doneButton.performClick();
                }
            }
        });
    }

    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        this.avatarUpdater.onActivityResult(requestCode, resultCode, data);
    }

    public void saveSelfArgs(Bundle args) {
        if (!(this.avatarUpdater == null || this.avatarUpdater.currentPicturePath == null)) {
            args.putString("path", this.avatarUpdater.currentPicturePath);
        }
        if (this.nameTextView != null) {
            String text = this.nameTextView.getText().toString();
            if (text != null && text.length() != 0) {
                args.putString("nameTextView", text);
            }
        }
    }

    public void restoreSelfArgs(Bundle args) {
        if (this.avatarUpdater != null) {
            this.avatarUpdater.currentPicturePath = args.getString("path");
        }
    }

    private boolean checkUserName(final String name, boolean alert) {
        if (name == null || name.length() <= 0) {
            this.checkTextView.setVisibility(8);
        } else {
            this.checkTextView.setVisibility(0);
        }
        if (alert && name.length() == 0) {
            return true;
        }
        if (this.checkRunnable != null) {
            AndroidUtilities.cancelRunOnUIThread(this.checkRunnable);
            this.checkRunnable = null;
            this.lastCheckName = null;
            if (this.checkReqId != 0) {
                ConnectionsManager.getInstance().cancelRequest(this.checkReqId, true);
            }
        }
        this.lastNameAvailable = false;
        if (name != null) {
            if (name.startsWith("_") || name.endsWith("_")) {
                this.checkTextView.setText(LocaleController.getString("LinkInvalid", C0553R.string.LinkInvalid));
                this.checkTextView.setTextColor(-3198928);
                return false;
            }
            int a = 0;
            while (a < name.length()) {
                char ch = name.charAt(a);
                if (a == 0 && ch >= '0' && ch <= '9') {
                    if (alert) {
                        showErrorAlert(LocaleController.getString("LinkInvalidStartNumber", C0553R.string.LinkInvalidStartNumber));
                    } else {
                        this.checkTextView.setText(LocaleController.getString("LinkInvalidStartNumber", C0553R.string.LinkInvalidStartNumber));
                        this.checkTextView.setTextColor(-3198928);
                    }
                    return false;
                } else if ((ch < '0' || ch > '9') && ((ch < 'a' || ch > 'z') && ((ch < 'A' || ch > 'Z') && ch != '_'))) {
                    if (alert) {
                        showErrorAlert(LocaleController.getString("LinkInvalid", C0553R.string.LinkInvalid));
                    } else {
                        this.checkTextView.setText(LocaleController.getString("LinkInvalid", C0553R.string.LinkInvalid));
                        this.checkTextView.setTextColor(-3198928);
                    }
                    return false;
                } else {
                    a++;
                }
            }
        }
        if (name == null || name.length() < 5) {
            if (alert) {
                showErrorAlert(LocaleController.getString("LinkInvalidShort", C0553R.string.LinkInvalidShort));
            } else {
                this.checkTextView.setText(LocaleController.getString("LinkInvalidShort", C0553R.string.LinkInvalidShort));
                this.checkTextView.setTextColor(-3198928);
            }
            return false;
        } else if (name.length() > 32) {
            if (alert) {
                showErrorAlert(LocaleController.getString("LinkInvalidLong", C0553R.string.LinkInvalidLong));
            } else {
                this.checkTextView.setText(LocaleController.getString("LinkInvalidLong", C0553R.string.LinkInvalidLong));
                this.checkTextView.setTextColor(-3198928);
            }
            return false;
        } else if (alert) {
            return true;
        } else {
            this.checkTextView.setText(LocaleController.getString("LinkChecking", C0553R.string.LinkChecking));
            this.checkTextView.setTextColor(-9605774);
            this.lastCheckName = name;
            this.checkRunnable = new Runnable() {

                class C15301 implements RequestDelegate {
                    C15301() {
                    }

                    public void run(final TLObject response, final TL_error error) {
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            public void run() {
                                ChannelEditActivity.this.checkReqId = 0;
                                if (ChannelEditActivity.this.lastCheckName != null && ChannelEditActivity.this.lastCheckName.equals(name)) {
                                    if (error == null && (response instanceof TL_boolTrue)) {
                                        ChannelEditActivity.this.checkTextView.setText(LocaleController.formatString("LinkAvailable", C0553R.string.LinkAvailable, name));
                                        ChannelEditActivity.this.checkTextView.setTextColor(-14248148);
                                        ChannelEditActivity.this.lastNameAvailable = true;
                                        return;
                                    }
                                    if (error == null || !error.text.equals("CHANNELS_ADMIN_PUBLIC_TOO_MUCH")) {
                                        ChannelEditActivity.this.checkTextView.setText(LocaleController.getString("LinkInUse", C0553R.string.LinkInUse));
                                    } else {
                                        ChannelEditActivity.this.checkTextView.setText(LocaleController.getString("ChannelPublicLimitReached", C0553R.string.ChannelPublicLimitReached));
                                    }
                                    ChannelEditActivity.this.checkTextView.setTextColor(-3198928);
                                    ChannelEditActivity.this.lastNameAvailable = false;
                                }
                            }
                        });
                    }
                }

                public void run() {
                    TL_channels_checkUsername req = new TL_channels_checkUsername();
                    req.username = name;
                    req.channel = MessagesController.getInputChannel(ChannelEditActivity.this.chatId);
                    ChannelEditActivity.this.checkReqId = ConnectionsManager.getInstance().sendRequest(req, new C15301(), 2);
                }
            };
            AndroidUtilities.runOnUIThread(this.checkRunnable, 300);
            return true;
        }
    }

    private void showErrorAlert(String error) {
        if (getParentActivity() != null) {
            Builder builder = new Builder(getParentActivity());
            builder.setTitle(LocaleController.getString("AppName", C0553R.string.AppName));
            Object obj = -1;
            switch (error.hashCode()) {
                case -141887186:
                    if (error.equals("USERNAMES_UNAVAILABLE")) {
                        obj = 2;
                        break;
                    }
                    break;
                case 288843630:
                    if (error.equals("USERNAME_INVALID")) {
                        obj = null;
                        break;
                    }
                    break;
                case 533175271:
                    if (error.equals("USERNAME_OCCUPIED")) {
                        obj = 1;
                        break;
                    }
                    break;
            }
            switch (obj) {
                case null:
                    builder.setMessage(LocaleController.getString("LinkInvalid", C0553R.string.LinkInvalid));
                    break;
                case 1:
                    builder.setMessage(LocaleController.getString("LinkInUse", C0553R.string.LinkInUse));
                    break;
                case 2:
                    builder.setMessage(LocaleController.getString("FeatureUnavailable", C0553R.string.FeatureUnavailable));
                    break;
                default:
                    builder.setMessage(LocaleController.getString("ErrorOccurred", C0553R.string.ErrorOccurred));
                    break;
            }
            builder.setPositiveButton(LocaleController.getString("OK", C0553R.string.OK), null);
            showDialog(builder.create());
        }
    }
}
