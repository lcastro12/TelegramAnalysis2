package org.telegram.ui;

import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.text.TextUtils.TruncateAt;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import com.google.android.gms.location.LocationStatusCodes;
import com.google.android.gms.plus.PlusShare;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.Semaphore;
import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.AnimationCompat.AnimatorListenerAdapterProxy;
import org.telegram.messenger.AnimationCompat.AnimatorSetProxy;
import org.telegram.messenger.AnimationCompat.ObjectAnimatorProxy;
import org.telegram.messenger.AnimationCompat.ViewProxy;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.C0553R;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.SecretChatHelper;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.query.BotQuery;
import org.telegram.messenger.query.SharedMediaQuery;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.messenger.support.widget.RecyclerView.Adapter;
import org.telegram.messenger.support.widget.RecyclerView.OnScrollListener;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.BotInfo;
import org.telegram.tgnet.TLRPC.ChannelParticipant;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.ChatFull;
import org.telegram.tgnet.TLRPC.ChatParticipant;
import org.telegram.tgnet.TLRPC.EncryptedChat;
import org.telegram.tgnet.TLRPC.FileLocation;
import org.telegram.tgnet.TLRPC.InputFile;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.TL_channelFull;
import org.telegram.tgnet.TLRPC.TL_channelParticipant;
import org.telegram.tgnet.TLRPC.TL_channelParticipantEditor;
import org.telegram.tgnet.TLRPC.TL_channelParticipantsRecent;
import org.telegram.tgnet.TLRPC.TL_channelRoleEditor;
import org.telegram.tgnet.TLRPC.TL_channels_channelParticipants;
import org.telegram.tgnet.TLRPC.TL_channels_editAdmin;
import org.telegram.tgnet.TLRPC.TL_channels_getParticipants;
import org.telegram.tgnet.TLRPC.TL_chatChannelParticipant;
import org.telegram.tgnet.TLRPC.TL_chatFull;
import org.telegram.tgnet.TLRPC.TL_chatParticipant;
import org.telegram.tgnet.TLRPC.TL_chatParticipantsForbidden;
import org.telegram.tgnet.TLRPC.TL_chatPhotoEmpty;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionSetMessageTTL;
import org.telegram.tgnet.TLRPC.TL_encryptedChat;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_messageEncryptedAction;
import org.telegram.tgnet.TLRPC.TL_userEmpty;
import org.telegram.tgnet.TLRPC.Updates;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Cells.AboutLinkCell;
import org.telegram.ui.Cells.AboutLinkCell.AboutLinkCellDelegate;
import org.telegram.ui.Cells.DividerCell;
import org.telegram.ui.Cells.EmptyCell;
import org.telegram.ui.Cells.LoadingCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextDetailCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.AvatarUpdater;
import org.telegram.ui.Components.AvatarUpdater.AvatarUpdaterDelegate;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.IdenticonDrawable;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.RecyclerListView.OnItemClickListener;
import org.telegram.ui.Components.RecyclerListView.OnItemLongClickListener;
import org.telegram.ui.ContactsActivity.ContactsActivityDelegate;
import org.telegram.ui.DialogsActivity.MessagesActivityDelegate;
import org.telegram.ui.PhotoViewer.PhotoViewerProvider;
import org.telegram.ui.PhotoViewer.PlaceProviderObject;

public class ProfileActivity extends BaseFragment implements NotificationCenterDelegate, MessagesActivityDelegate, PhotoViewerProvider {
    private static final int add_contact = 1;
    private static final int block_contact = 2;
    private static final int delete_contact = 5;
    private static final int edit_channel = 12;
    private static final int edit_contact = 4;
    private static final int edit_name = 8;
    private static final int invite_to_group = 9;
    private static final int leave_group = 7;
    private static final int set_admins = 11;
    private static final int share = 10;
    private static final int share_contact = 3;
    private int addMemberRow;
    private ActionBarMenuItem animatingItem;
    private float animationProgress;
    private AvatarDrawable avatarDrawable;
    private BackupImageView avatarImage;
    private AvatarUpdater avatarUpdater;
    private int blockedUsersRow;
    private BotInfo botInfo;
    private int botInfoRow;
    private int botSectionRow;
    private int channelInfoRow;
    private int channelNameRow;
    private int chat_id;
    private int convertHelpRow;
    private int convertRow;
    private boolean creatingChat;
    private Chat currentChat;
    private EncryptedChat currentEncryptedChat;
    private long dialog_id;
    private int emptyRow;
    private int emptyRowChat;
    private int emptyRowChat2;
    private int extraHeight;
    private View extraHeightView;
    private ChatFull info;
    private int initialAnimationExtraHeight;
    private LinearLayoutManager layoutManager;
    private int leaveChannelRow;
    private ListAdapter listAdapter;
    private RecyclerListView listView;
    private int loadMoreMembersRow;
    private boolean loadingUsers;
    private int managementRow;
    private int membersEndRow;
    private int membersRow;
    private int membersSectionRow;
    private long mergeDialogId;
    private TextView[] nameTextView = new TextView[2];
    private int onlineCount = -1;
    private TextView[] onlineTextView = new TextView[2];
    private boolean openAnimationInProgress;
    private int overscrollRow;
    private ArrayList<ChannelParticipant> participants = new ArrayList();
    private HashMap<Integer, ChannelParticipant> participantsMap = new HashMap();
    private int phoneRow;
    private boolean playProfileAnimation;
    private int rowCount = 0;
    private int sectionRow;
    private int selectedUser;
    private int settingsKeyRow;
    private int settingsNotificationsRow;
    private int settingsTimerRow;
    private View shadowView;
    private int sharedMediaRow;
    private ArrayList<Integer> sortedUsers;
    private int startSecretChatRow;
    private int totalMediaCount = -1;
    private int totalMediaCountMerge = -1;
    private boolean userBlocked;
    private int user_id;
    private int usernameRow;
    private boolean usersEndReached;
    private ImageView writeButton;
    private AnimatorSetProxy writeButtonAnimation;

    class C11809 implements OnClickListener {
        C11809() {
        }

        public void onClick(View v) {
            if (ProfileActivity.this.user_id != 0) {
                User user = MessagesController.getInstance().getUser(Integer.valueOf(ProfileActivity.this.user_id));
                if (user.photo != null && user.photo.photo_big != null) {
                    PhotoViewer.getInstance().setParentActivity(ProfileActivity.this.getParentActivity());
                    PhotoViewer.getInstance().openPhoto(user.photo.photo_big, ProfileActivity.this);
                }
            } else if (ProfileActivity.this.chat_id != 0) {
                Chat chat = MessagesController.getInstance().getChat(Integer.valueOf(ProfileActivity.this.chat_id));
                if (chat.photo != null && chat.photo.photo_big != null) {
                    PhotoViewer.getInstance().setParentActivity(ProfileActivity.this.getParentActivity());
                    PhotoViewer.getInstance().openPhoto(chat.photo.photo_big, ProfileActivity.this);
                }
            }
        }
    }

    class C16532 implements AvatarUpdaterDelegate {
        C16532() {
        }

        public void didUploadedPhoto(InputFile file, PhotoSize small, PhotoSize big) {
            if (ProfileActivity.this.chat_id != 0) {
                MessagesController.getInstance().changeChatAvatar(ProfileActivity.this.chat_id, file);
            }
        }
    }

    class C16553 extends ActionBarMenuOnItemClick {

        class C11681 implements DialogInterface.OnClickListener {
            C11681() {
            }

            public void onClick(DialogInterface dialogInterface, int i) {
                if (ProfileActivity.this.userBlocked) {
                    MessagesController.getInstance().unblockUser(ProfileActivity.this.user_id);
                } else {
                    MessagesController.getInstance().blockUser(ProfileActivity.this.user_id);
                }
            }
        }

        C16553() {
        }

        public void onItemClick(int id) {
            if (ProfileActivity.this.getParentActivity() != null) {
                if (id == -1) {
                    ProfileActivity.this.finishFragment();
                } else if (id == 2) {
                    user = MessagesController.getInstance().getUser(Integer.valueOf(ProfileActivity.this.user_id));
                    if (user == null) {
                        return;
                    }
                    if (!user.bot) {
                        builder = new Builder(ProfileActivity.this.getParentActivity());
                        if (ProfileActivity.this.userBlocked) {
                            builder.setMessage(LocaleController.getString("AreYouSureUnblockContact", C0553R.string.AreYouSureUnblockContact));
                        } else {
                            builder.setMessage(LocaleController.getString("AreYouSureBlockContact", C0553R.string.AreYouSureBlockContact));
                        }
                        builder.setTitle(LocaleController.getString("AppName", C0553R.string.AppName));
                        builder.setPositiveButton(LocaleController.getString("OK", C0553R.string.OK), new C11681());
                        builder.setNegativeButton(LocaleController.getString("Cancel", C0553R.string.Cancel), null);
                        ProfileActivity.this.showDialog(builder.create());
                    } else if (ProfileActivity.this.userBlocked) {
                        MessagesController.getInstance().unblockUser(ProfileActivity.this.user_id);
                        SendMessagesHelper.getInstance().sendMessage("/start", (long) ProfileActivity.this.user_id, null, null, false, false);
                        ProfileActivity.this.finishFragment();
                    } else {
                        MessagesController.getInstance().blockUser(ProfileActivity.this.user_id);
                    }
                } else if (id == 1) {
                    user = MessagesController.getInstance().getUser(Integer.valueOf(ProfileActivity.this.user_id));
                    args = new Bundle();
                    args.putInt("user_id", user.id);
                    args.putBoolean("addContact", true);
                    ProfileActivity.this.presentFragment(new ContactAddActivity(args));
                } else if (id == 3) {
                    args = new Bundle();
                    args.putBoolean("onlySelect", true);
                    args.putInt("dialogsType", 1);
                    args.putString("selectAlertString", LocaleController.getString("SendContactTo", C0553R.string.SendContactTo));
                    args.putString("selectAlertStringGroup", LocaleController.getString("SendContactToGroup", C0553R.string.SendContactToGroup));
                    fragment = new DialogsActivity(args);
                    fragment.setDelegate(ProfileActivity.this);
                    ProfileActivity.this.presentFragment(fragment);
                } else if (id == 4) {
                    args = new Bundle();
                    args.putInt("user_id", ProfileActivity.this.user_id);
                    ProfileActivity.this.presentFragment(new ContactAddActivity(args));
                } else if (id == 5) {
                    user = MessagesController.getInstance().getUser(Integer.valueOf(ProfileActivity.this.user_id));
                    if (user != null && ProfileActivity.this.getParentActivity() != null) {
                        builder = new Builder(ProfileActivity.this.getParentActivity());
                        builder.setMessage(LocaleController.getString("AreYouSureDeleteContact", C0553R.string.AreYouSureDeleteContact));
                        builder.setTitle(LocaleController.getString("AppName", C0553R.string.AppName));
                        builder.setPositiveButton(LocaleController.getString("OK", C0553R.string.OK), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ArrayList<User> arrayList = new ArrayList();
                                arrayList.add(user);
                                ContactsController.getInstance().deleteContact(arrayList);
                            }
                        });
                        builder.setNegativeButton(LocaleController.getString("Cancel", C0553R.string.Cancel), null);
                        ProfileActivity.this.showDialog(builder.create());
                    }
                } else if (id == 7) {
                    ProfileActivity.this.leaveChatPressed();
                } else if (id == 8) {
                    args = new Bundle();
                    args.putInt("chat_id", ProfileActivity.this.chat_id);
                    ProfileActivity.this.presentFragment(new ChangeChatNameActivity(args));
                } else if (id == 12) {
                    args = new Bundle();
                    args.putInt("chat_id", ProfileActivity.this.chat_id);
                    ChannelEditActivity fragment = new ChannelEditActivity(args);
                    fragment.setInfo(ProfileActivity.this.info);
                    ProfileActivity.this.presentFragment(fragment);
                } else if (id == 9) {
                    user = MessagesController.getInstance().getUser(Integer.valueOf(ProfileActivity.this.user_id));
                    if (user != null) {
                        args = new Bundle();
                        args.putBoolean("onlySelect", true);
                        args.putInt("dialogsType", 2);
                        args.putString("addToGroupAlertString", LocaleController.formatString("AddToTheGroupTitle", C0553R.string.AddToTheGroupTitle, UserObject.getUserName(user), "%1$s"));
                        fragment = new DialogsActivity(args);
                        fragment.setDelegate(new MessagesActivityDelegate() {
                            public void didSelectDialog(DialogsActivity fragment, long did, boolean param) {
                                NotificationCenter.getInstance().removeObserver(ProfileActivity.this, NotificationCenter.closeChats);
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats, new Object[0]);
                                MessagesController.getInstance().addUserToChat(-((int) did), user, null, 0, null, ProfileActivity.this);
                                Bundle args = new Bundle();
                                args.putBoolean("scrollToTopOnResume", true);
                                args.putInt("chat_id", -((int) did));
                                ProfileActivity.this.presentFragment(new ChatActivity(args), true);
                                ProfileActivity.this.removeSelfFromStack();
                            }
                        });
                        ProfileActivity.this.presentFragment(fragment);
                    }
                } else if (id == 10) {
                    try {
                        if (MessagesController.getInstance().getUser(Integer.valueOf(ProfileActivity.this.user_id)) != null) {
                            Intent intent = new Intent("android.intent.action.SEND");
                            intent.setType("text/plain");
                            if (ProfileActivity.this.botInfo == null || ProfileActivity.this.botInfo.share_text == null || ProfileActivity.this.botInfo.share_text.length() <= 0) {
                                intent.putExtra("android.intent.extra.TEXT", String.format("https://telegram.me/%s", new Object[]{user.username}));
                            } else {
                                intent.putExtra("android.intent.extra.TEXT", String.format("%s https://telegram.me/%s", new Object[]{ProfileActivity.this.botInfo.share_text, user.username}));
                            }
                            ProfileActivity.this.startActivityForResult(Intent.createChooser(intent, LocaleController.getString("BotShare", C0553R.string.BotShare)), 500);
                        }
                    } catch (Throwable e) {
                        FileLog.m611e("tmessages", e);
                    }
                } else if (id == 11) {
                    args = new Bundle();
                    args.putInt("chat_id", ProfileActivity.this.chat_id);
                    SetAdminsActivity fragment2 = new SetAdminsActivity(args);
                    fragment2.setChatInfo(ProfileActivity.this.info);
                    ProfileActivity.this.presentFragment(fragment2);
                }
            }
        }
    }

    class C16567 implements OnItemClickListener {

        class C11711 implements DialogInterface.OnClickListener {
            C11711() {
            }

            public void onClick(DialogInterface dialogInterface, int i) {
                ProfileActivity.this.creatingChat = true;
                SecretChatHelper.getInstance().startSecretChat(ProfileActivity.this.getParentActivity(), MessagesController.getInstance().getUser(Integer.valueOf(ProfileActivity.this.user_id)));
            }
        }

        class C11744 implements DialogInterface.OnClickListener {
            C11744() {
            }

            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    if (VERSION.SDK_INT < 11) {
                        ((ClipboardManager) ApplicationLoader.applicationContext.getSystemService("clipboard")).setText(ProfileActivity.this.info.about);
                    } else {
                        ((android.content.ClipboardManager) ApplicationLoader.applicationContext.getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText(PlusShare.KEY_CALL_TO_ACTION_LABEL, ProfileActivity.this.info.about));
                    }
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
        }

        class C11755 implements DialogInterface.OnClickListener {
            C11755() {
            }

            public void onClick(DialogInterface dialogInterface, int i) {
                MessagesController.getInstance().convertToMegaGroup(ProfileActivity.this.getParentActivity(), ProfileActivity.this.chat_id);
            }
        }

        C16567() {
        }

        public void onItemClick(View view, int position) {
            if (ProfileActivity.this.getParentActivity() != null) {
                Bundle args;
                if (position == ProfileActivity.this.sharedMediaRow) {
                    args = new Bundle();
                    if (ProfileActivity.this.user_id != 0) {
                        args.putLong("dialog_id", ProfileActivity.this.dialog_id != 0 ? ProfileActivity.this.dialog_id : (long) ProfileActivity.this.user_id);
                    } else {
                        args.putLong("dialog_id", (long) (-ProfileActivity.this.chat_id));
                    }
                    MediaActivity fragment = new MediaActivity(args);
                    fragment.setChatInfo(ProfileActivity.this.info);
                    ProfileActivity.this.presentFragment(fragment);
                } else if (position == ProfileActivity.this.settingsKeyRow) {
                    args = new Bundle();
                    args.putInt("chat_id", (int) (ProfileActivity.this.dialog_id >> 32));
                    ProfileActivity.this.presentFragment(new IdenticonActivity(args));
                } else if (position == ProfileActivity.this.settingsTimerRow) {
                    ProfileActivity.this.showDialog(AndroidUtilities.buildTTLAlert(ProfileActivity.this.getParentActivity(), ProfileActivity.this.currentEncryptedChat).create());
                } else if (position == ProfileActivity.this.settingsNotificationsRow) {
                    args = new Bundle();
                    if (ProfileActivity.this.user_id != 0) {
                        args.putLong("dialog_id", ProfileActivity.this.dialog_id == 0 ? (long) ProfileActivity.this.user_id : ProfileActivity.this.dialog_id);
                    } else if (ProfileActivity.this.chat_id != 0) {
                        args.putLong("dialog_id", (long) (-ProfileActivity.this.chat_id));
                    }
                    ProfileActivity.this.presentFragment(new ProfileNotificationsActivity(args));
                } else if (position == ProfileActivity.this.startSecretChatRow) {
                    builder = new Builder(ProfileActivity.this.getParentActivity());
                    builder.setMessage(LocaleController.getString("AreYouSureSecretChat", C0553R.string.AreYouSureSecretChat));
                    builder.setTitle(LocaleController.getString("AppName", C0553R.string.AppName));
                    builder.setPositiveButton(LocaleController.getString("OK", C0553R.string.OK), new C11711());
                    builder.setNegativeButton(LocaleController.getString("Cancel", C0553R.string.Cancel), null);
                    ProfileActivity.this.showDialog(builder.create());
                } else if (position == ProfileActivity.this.usernameRow) {
                    user = MessagesController.getInstance().getUser(Integer.valueOf(ProfileActivity.this.user_id));
                    if (user != null && user.username != null) {
                        builder = new Builder(ProfileActivity.this.getParentActivity());
                        builder.setItems(new CharSequence[]{LocaleController.getString("Copy", C0553R.string.Copy)}, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0) {
                                    try {
                                        if (VERSION.SDK_INT < 11) {
                                            ((ClipboardManager) ApplicationLoader.applicationContext.getSystemService("clipboard")).setText("@" + user.username);
                                        } else {
                                            ((android.content.ClipboardManager) ApplicationLoader.applicationContext.getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText(PlusShare.KEY_CALL_TO_ACTION_LABEL, "@" + user.username));
                                        }
                                    } catch (Throwable e) {
                                        FileLog.m611e("tmessages", e);
                                    }
                                }
                            }
                        });
                        ProfileActivity.this.showDialog(builder.create());
                    }
                } else if (position == ProfileActivity.this.phoneRow) {
                    user = MessagesController.getInstance().getUser(Integer.valueOf(ProfileActivity.this.user_id));
                    if (user != null && user.phone != null && user.phone.length() != 0 && ProfileActivity.this.getParentActivity() != null) {
                        builder = new Builder(ProfileActivity.this.getParentActivity());
                        builder.setItems(new CharSequence[]{LocaleController.getString("Call", C0553R.string.Call), LocaleController.getString("Copy", C0553R.string.Copy)}, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0) {
                                    try {
                                        Intent intent = new Intent("android.intent.action.DIAL", Uri.parse("tel:+" + user.phone));
                                        intent.addFlags(268435456);
                                        ProfileActivity.this.getParentActivity().startActivityForResult(intent, 500);
                                    } catch (Throwable e) {
                                        FileLog.m611e("tmessages", e);
                                    }
                                } else if (i == 1) {
                                    try {
                                        if (VERSION.SDK_INT < 11) {
                                            ((ClipboardManager) ApplicationLoader.applicationContext.getSystemService("clipboard")).setText("+" + user.phone);
                                        } else {
                                            ((android.content.ClipboardManager) ApplicationLoader.applicationContext.getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText(PlusShare.KEY_CALL_TO_ACTION_LABEL, "+" + user.phone));
                                        }
                                    } catch (Throwable e2) {
                                        FileLog.m611e("tmessages", e2);
                                    }
                                }
                            }
                        });
                        ProfileActivity.this.showDialog(builder.create());
                    }
                } else if (position > ProfileActivity.this.emptyRowChat2 && position < ProfileActivity.this.membersEndRow) {
                    int user_id;
                    if (ProfileActivity.this.participants != null) {
                        user_id = ((ChannelParticipant) ProfileActivity.this.participants.get((position - ProfileActivity.this.emptyRowChat2) - 1)).user_id;
                    } else {
                        user_id = ((ChatParticipant) ProfileActivity.this.info.participants.participants.get(((Integer) ProfileActivity.this.sortedUsers.get((position - ProfileActivity.this.emptyRowChat2) - 1)).intValue())).user_id;
                    }
                    if (user_id != UserConfig.getClientUserId()) {
                        args = new Bundle();
                        args.putInt("user_id", user_id);
                        ProfileActivity.this.presentFragment(new ProfileActivity(args));
                    }
                } else if (position == ProfileActivity.this.addMemberRow) {
                    ProfileActivity.this.openAddMember();
                } else if (position == ProfileActivity.this.channelNameRow) {
                    try {
                        Intent intent = new Intent("android.intent.action.SEND");
                        intent.setType("text/plain");
                        if (ProfileActivity.this.info.about == null || ProfileActivity.this.info.about.length() <= 0) {
                            intent.putExtra("android.intent.extra.TEXT", ProfileActivity.this.currentChat.title + "\nhttps://telegram.me/" + ProfileActivity.this.currentChat.username);
                        } else {
                            intent.putExtra("android.intent.extra.TEXT", ProfileActivity.this.currentChat.title + "\n" + ProfileActivity.this.info.about + "\nhttps://telegram.me/" + ProfileActivity.this.currentChat.username);
                        }
                        ProfileActivity.this.getParentActivity().startActivityForResult(Intent.createChooser(intent, LocaleController.getString("BotShare", C0553R.string.BotShare)), 500);
                    } catch (Throwable e) {
                        FileLog.m611e("tmessages", e);
                    }
                } else if (position == ProfileActivity.this.leaveChannelRow) {
                    ProfileActivity.this.leaveChatPressed();
                } else if (position == ProfileActivity.this.membersRow || position == ProfileActivity.this.blockedUsersRow || position == ProfileActivity.this.managementRow) {
                    args = new Bundle();
                    args.putInt("chat_id", ProfileActivity.this.chat_id);
                    if (position == ProfileActivity.this.blockedUsersRow) {
                        args.putInt("type", 0);
                    } else if (position == ProfileActivity.this.managementRow) {
                        args.putInt("type", 1);
                    } else if (position == ProfileActivity.this.membersRow) {
                        args.putInt("type", 2);
                    }
                    ProfileActivity.this.presentFragment(new ChannelUsersActivity(args));
                } else if (position == ProfileActivity.this.channelInfoRow) {
                    builder = new Builder(ProfileActivity.this.getParentActivity());
                    builder.setItems(new CharSequence[]{LocaleController.getString("Copy", C0553R.string.Copy)}, new C11744());
                    ProfileActivity.this.showDialog(builder.create());
                } else if (position == ProfileActivity.this.convertRow) {
                    builder = new Builder(ProfileActivity.this.getParentActivity());
                    builder.setMessage(LocaleController.getString("ConvertGroupAlert", C0553R.string.ConvertGroupAlert));
                    builder.setTitle(LocaleController.getString("AppName", C0553R.string.AppName));
                    builder.setPositiveButton(LocaleController.getString("OK", C0553R.string.OK), new C11755());
                    builder.setNegativeButton(LocaleController.getString("Cancel", C0553R.string.Cancel), null);
                    ProfileActivity.this.showDialog(builder.create());
                }
            }
        }
    }

    class C16588 implements OnItemLongClickListener {

        class C11792 implements DialogInterface.OnClickListener {
            C11792() {
            }

            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    ProfileActivity.this.kickUser(ProfileActivity.this.selectedUser);
                }
            }
        }

        C16588() {
        }

        public boolean onItemClick(View view, int position) {
            if (position <= ProfileActivity.this.emptyRowChat2 || position >= ProfileActivity.this.membersEndRow) {
                return false;
            }
            if (ProfileActivity.this.getParentActivity() == null) {
                return false;
            }
            boolean allowKick = false;
            boolean allowSetAdmin = false;
            ChannelParticipant channelParticipant = null;
            if (ChatObject.isChannel(ProfileActivity.this.currentChat)) {
                channelParticipant = (ChannelParticipant) ProfileActivity.this.participants.get((position - ProfileActivity.this.emptyRowChat2) - 1);
                if (channelParticipant.user_id != UserConfig.getClientUserId()) {
                    if (ProfileActivity.this.currentChat.creator) {
                        allowKick = true;
                    } else if ((channelParticipant instanceof TL_channelParticipant) && (ProfileActivity.this.currentChat.editor || channelParticipant.inviter_id == UserConfig.getClientUserId())) {
                        allowKick = true;
                    }
                }
                allowSetAdmin = (channelParticipant instanceof TL_channelParticipant) && !MessagesController.getInstance().getUser(Integer.valueOf(channelParticipant.user_id)).bot;
                ProfileActivity.this.selectedUser = channelParticipant.user_id;
            } else {
                ChatParticipant user = (ChatParticipant) ProfileActivity.this.info.participants.participants.get(((Integer) ProfileActivity.this.sortedUsers.get((position - ProfileActivity.this.emptyRowChat2) - 1)).intValue());
                if (user.user_id != UserConfig.getClientUserId()) {
                    if (ProfileActivity.this.currentChat.creator) {
                        allowKick = true;
                    } else if ((user instanceof TL_chatParticipant) && ((ProfileActivity.this.currentChat.admin && ProfileActivity.this.currentChat.admins_enabled) || user.inviter_id == UserConfig.getClientUserId())) {
                        allowKick = true;
                    }
                }
                ProfileActivity.this.selectedUser = user.user_id;
            }
            if (!allowKick) {
                return false;
            }
            Builder builder = new Builder(ProfileActivity.this.getParentActivity());
            if (ProfileActivity.this.currentChat.megagroup && ProfileActivity.this.currentChat.creator && allowSetAdmin) {
                final ChannelParticipant channelParticipantFinal = channelParticipant;
                builder.setItems(new CharSequence[]{LocaleController.getString("SetAsAdmin", C0553R.string.SetAsAdmin), LocaleController.getString("KickFromGroup", C0553R.string.KickFromGroup)}, new DialogInterface.OnClickListener() {

                    class C16571 implements RequestDelegate {

                        class C11761 implements Runnable {
                            C11761() {
                            }

                            public void run() {
                                MessagesController.getInstance().loadFullChat(ProfileActivity.this.chat_id, 0, true);
                            }
                        }

                        C16571() {
                        }

                        public void run(TLObject response, final TL_error error) {
                            if (error == null) {
                                MessagesController.getInstance().processUpdates((Updates) response, false);
                                AndroidUtilities.runOnUIThread(new C11761(), 1000);
                                return;
                            }
                            AndroidUtilities.runOnUIThread(new Runnable() {
                                public void run() {
                                    AlertsCreator.showAddUserAlert(error.text, ProfileActivity.this, false);
                                }
                            });
                        }
                    }

                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            int index = ProfileActivity.this.participants.indexOf(channelParticipantFinal);
                            if (index != -1) {
                                TL_channelParticipantEditor editor = new TL_channelParticipantEditor();
                                editor.inviter_id = UserConfig.getClientUserId();
                                editor.user_id = channelParticipantFinal.user_id;
                                editor.date = channelParticipantFinal.date;
                                ProfileActivity.this.participants.set(index, editor);
                            }
                            TL_channels_editAdmin req = new TL_channels_editAdmin();
                            req.channel = MessagesController.getInputChannel(ProfileActivity.this.chat_id);
                            req.user_id = MessagesController.getInputUser(ProfileActivity.this.selectedUser);
                            req.role = new TL_channelRoleEditor();
                            ConnectionsManager.getInstance().sendRequest(req, new C16571());
                        } else if (i == 1) {
                            ProfileActivity.this.kickUser(ProfileActivity.this.selectedUser);
                        }
                    }
                });
            } else {
                String string;
                CharSequence[] items = new CharSequence[1];
                if (ProfileActivity.this.chat_id > 0) {
                    string = LocaleController.getString("KickFromGroup", C0553R.string.KickFromGroup);
                } else {
                    string = LocaleController.getString("KickFromBroadcast", C0553R.string.KickFromBroadcast);
                }
                items[0] = string;
                builder.setItems(items, new C11792());
            }
            ProfileActivity.this.showDialog(builder.create());
            return true;
        }
    }

    private class ListAdapter extends Adapter {
        private Context mContext;

        class C16624 implements AboutLinkCellDelegate {
            C16624() {
            }

            public void didPressUrl(String url) {
                if (url.startsWith("@")) {
                    MessagesController.openByUserName(url.substring(1), ProfileActivity.this, 0);
                } else if (url.startsWith("#")) {
                    DialogsActivity fragment = new DialogsActivity(null);
                    fragment.setSearchString(url);
                    ProfileActivity.this.presentFragment(fragment);
                } else if (url.startsWith("/") && ProfileActivity.this.parentLayout.fragmentsStack.size() > 1) {
                    BaseFragment previousFragment = (BaseFragment) ProfileActivity.this.parentLayout.fragmentsStack.get(ProfileActivity.this.parentLayout.fragmentsStack.size() - 2);
                    if (previousFragment instanceof ChatActivity) {
                        ProfileActivity.this.finishFragment();
                        ((ChatActivity) previousFragment).chatActivityEnterView.setCommand(null, url, false, false);
                    }
                }
            }
        }

        private class Holder extends ViewHolder {
            public Holder(View itemView) {
                super(itemView);
            }
        }

        public ListAdapter(Context context) {
            this.mContext = context;
        }

        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = null;
            switch (viewType) {
                case 0:
                    view = new EmptyCell(this.mContext);
                    break;
                case 1:
                    view = new DividerCell(this.mContext);
                    view.setPadding(AndroidUtilities.dp(72.0f), 0, 0, 0);
                    break;
                case 2:
                    view = new TextDetailCell(this.mContext) {
                        public boolean onTouchEvent(MotionEvent event) {
                            if (VERSION.SDK_INT >= 21 && getBackground() != null && (event.getAction() == 0 || event.getAction() == 2)) {
                                getBackground().setHotspot(event.getX(), event.getY());
                            }
                            return super.onTouchEvent(event);
                        }
                    };
                    break;
                case 3:
                    view = new TextCell(this.mContext) {
                        public boolean onTouchEvent(MotionEvent event) {
                            if (VERSION.SDK_INT >= 21 && getBackground() != null && (event.getAction() == 0 || event.getAction() == 2)) {
                                getBackground().setHotspot(event.getX(), event.getY());
                            }
                            return super.onTouchEvent(event);
                        }
                    };
                    break;
                case 4:
                    view = new UserCell(this.mContext, 61, 0) {
                        public boolean onTouchEvent(MotionEvent event) {
                            if (VERSION.SDK_INT >= 21 && getBackground() != null && (event.getAction() == 0 || event.getAction() == 2)) {
                                getBackground().setHotspot(event.getX(), event.getY());
                            }
                            return super.onTouchEvent(event);
                        }
                    };
                    break;
                case 5:
                    view = new ShadowSectionCell(this.mContext);
                    break;
                case 6:
                    view = new TextInfoPrivacyCell(this.mContext);
                    TextInfoPrivacyCell cell = (TextInfoPrivacyCell) view;
                    cell.setBackgroundResource(C0553R.drawable.greydivider);
                    cell.setText(AndroidUtilities.replaceTags(LocaleController.formatString("ConvertGroupInfo", C0553R.string.ConvertGroupInfo, LocaleController.formatPluralString("Members", MessagesController.getInstance().maxMegagroupCount))));
                    break;
                case 7:
                    view = new LoadingCell(this.mContext);
                    break;
                case 8:
                    view = new AboutLinkCell(this.mContext);
                    ((AboutLinkCell) view).setDelegate(new C16624());
                    break;
            }
            return new Holder(view);
        }

        public void onBindViewHolder(ViewHolder holder, int i) {
            boolean checkBackground = true;
            String text;
            switch (holder.getItemViewType()) {
                case 0:
                    if (i != ProfileActivity.this.overscrollRow) {
                        if (i != ProfileActivity.this.emptyRowChat && i != ProfileActivity.this.emptyRowChat2) {
                            ((EmptyCell) holder.itemView).setHeight(AndroidUtilities.dp(36.0f));
                            break;
                        } else {
                            ((EmptyCell) holder.itemView).setHeight(AndroidUtilities.dp(8.0f));
                            break;
                        }
                    }
                    ((EmptyCell) holder.itemView).setHeight(AndroidUtilities.dp(88.0f));
                    break;
                    break;
                case 2:
                    TextDetailCell textDetailCell = holder.itemView;
                    User user;
                    if (i != ProfileActivity.this.phoneRow) {
                        if (i != ProfileActivity.this.usernameRow) {
                            if (i == ProfileActivity.this.channelNameRow) {
                                if (ProfileActivity.this.currentChat == null || ProfileActivity.this.currentChat.username == null || ProfileActivity.this.currentChat.username.length() == 0) {
                                    text = "-";
                                } else {
                                    text = "@" + ProfileActivity.this.currentChat.username;
                                }
                                textDetailCell.setTextAndValue(text, "telegram.me/" + ProfileActivity.this.currentChat.username);
                                break;
                            }
                        }
                        user = MessagesController.getInstance().getUser(Integer.valueOf(ProfileActivity.this.user_id));
                        if (user == null || user.username == null || user.username.length() == 0) {
                            text = "-";
                        } else {
                            text = "@" + user.username;
                        }
                        textDetailCell.setTextAndValue(text, LocaleController.getString("Username", C0553R.string.Username));
                        break;
                    }
                    user = MessagesController.getInstance().getUser(Integer.valueOf(ProfileActivity.this.user_id));
                    if (user.phone == null || user.phone.length() == 0) {
                        text = LocaleController.getString("NumberUnknown", C0553R.string.NumberUnknown);
                    } else {
                        text = PhoneFormat.getInstance().format("+" + user.phone);
                    }
                    textDetailCell.setTextAndValueAndIcon(text, LocaleController.getString("PhoneMobile", C0553R.string.PhoneMobile), C0553R.drawable.phone_grey);
                    break;
                    break;
                case 3:
                    TextCell textCell = holder.itemView;
                    textCell.setTextColor(-14606047);
                    String value;
                    if (i != ProfileActivity.this.sharedMediaRow) {
                        if (i != ProfileActivity.this.settingsTimerRow) {
                            if (i != ProfileActivity.this.settingsNotificationsRow) {
                                if (i != ProfileActivity.this.startSecretChatRow) {
                                    if (i != ProfileActivity.this.settingsKeyRow) {
                                        if (i != ProfileActivity.this.leaveChannelRow) {
                                            if (i != ProfileActivity.this.convertRow) {
                                                if (i != ProfileActivity.this.membersRow) {
                                                    if (i != ProfileActivity.this.managementRow) {
                                                        if (i != ProfileActivity.this.blockedUsersRow) {
                                                            if (i == ProfileActivity.this.addMemberRow) {
                                                                if (ProfileActivity.this.chat_id <= 0) {
                                                                    textCell.setText(LocaleController.getString("AddRecipient", C0553R.string.AddRecipient));
                                                                    break;
                                                                } else {
                                                                    textCell.setText(LocaleController.getString("AddMember", C0553R.string.AddMember));
                                                                    break;
                                                                }
                                                            }
                                                        } else if (ProfileActivity.this.info == null) {
                                                            textCell.setText(LocaleController.getString("ChannelBlockedUsers", C0553R.string.ChannelBlockedUsers));
                                                            break;
                                                        } else {
                                                            textCell.setTextAndValue(LocaleController.getString("ChannelBlockedUsers", C0553R.string.ChannelBlockedUsers), String.format("%d", new Object[]{Integer.valueOf(ProfileActivity.this.info.kicked_count)}));
                                                            break;
                                                        }
                                                    } else if (ProfileActivity.this.info == null) {
                                                        textCell.setText(LocaleController.getString("ChannelAdministrators", C0553R.string.ChannelAdministrators));
                                                        break;
                                                    } else {
                                                        textCell.setTextAndValue(LocaleController.getString("ChannelAdministrators", C0553R.string.ChannelAdministrators), String.format("%d", new Object[]{Integer.valueOf(ProfileActivity.this.info.admins_count)}));
                                                        break;
                                                    }
                                                } else if (ProfileActivity.this.info == null) {
                                                    textCell.setText(LocaleController.getString("ChannelMembers", C0553R.string.ChannelMembers));
                                                    break;
                                                } else {
                                                    textCell.setTextAndValue(LocaleController.getString("ChannelMembers", C0553R.string.ChannelMembers), String.format("%d", new Object[]{Integer.valueOf(ProfileActivity.this.info.participants_count)}));
                                                    break;
                                                }
                                            }
                                            textCell.setText(LocaleController.getString("ConvertGroup", C0553R.string.ConvertGroup));
                                            textCell.setTextColor(-13129447);
                                            break;
                                        }
                                        textCell.setTextColor(-1229511);
                                        textCell.setText(LocaleController.getString("LeaveChannel", C0553R.string.LeaveChannel));
                                        break;
                                    }
                                    IdenticonDrawable identiconDrawable = new IdenticonDrawable();
                                    identiconDrawable.setEncryptedChat(MessagesController.getInstance().getEncryptedChat(Integer.valueOf((int) (ProfileActivity.this.dialog_id >> 32))));
                                    textCell.setTextAndValueDrawable(LocaleController.getString("EncryptionKey", C0553R.string.EncryptionKey), identiconDrawable);
                                    break;
                                }
                                textCell.setText(LocaleController.getString("StartEncryptedChat", C0553R.string.StartEncryptedChat));
                                textCell.setTextColor(-13129447);
                                break;
                            }
                            textCell.setTextAndIcon(LocaleController.getString("NotificationsAndSounds", C0553R.string.NotificationsAndSounds), C0553R.drawable.profile_list);
                            break;
                        }
                        EncryptedChat encryptedChat = MessagesController.getInstance().getEncryptedChat(Integer.valueOf((int) (ProfileActivity.this.dialog_id >> 32)));
                        if (encryptedChat.ttl == 0) {
                            value = LocaleController.getString("ShortMessageLifetimeForever", C0553R.string.ShortMessageLifetimeForever);
                        } else {
                            value = AndroidUtilities.formatTTLString(encryptedChat.ttl);
                        }
                        textCell.setTextAndValue(LocaleController.getString("MessageLifetime", C0553R.string.MessageLifetime), value);
                        break;
                    }
                    if (ProfileActivity.this.totalMediaCount == -1) {
                        value = LocaleController.getString("Loading", C0553R.string.Loading);
                    } else {
                        String str = "%d";
                        Object[] objArr = new Object[1];
                        objArr[0] = Integer.valueOf((ProfileActivity.this.totalMediaCountMerge != -1 ? ProfileActivity.this.totalMediaCountMerge : 0) + ProfileActivity.this.totalMediaCount);
                        value = String.format(str, objArr);
                    }
                    textCell.setTextAndValue(LocaleController.getString("SharedMedia", C0553R.string.SharedMedia), value);
                    break;
                    break;
                case 4:
                    if (ProfileActivity.this.participants == null) {
                        ((UserCell) holder.itemView).setData(MessagesController.getInstance().getUser(Integer.valueOf(((ChatParticipant) ProfileActivity.this.info.participants.participants.get(((Integer) ProfileActivity.this.sortedUsers.get((i - ProfileActivity.this.emptyRowChat2) - 1)).intValue())).user_id)), null, null, i == ProfileActivity.this.emptyRowChat2 + 1 ? C0553R.drawable.menu_newgroup : 0);
                        break;
                    } else {
                        ((UserCell) holder.itemView).setData(MessagesController.getInstance().getUser(Integer.valueOf(((ChannelParticipant) ProfileActivity.this.participants.get((i - ProfileActivity.this.emptyRowChat2) - 1)).user_id)), null, null, i == ProfileActivity.this.emptyRowChat2 + 1 ? C0553R.drawable.menu_newgroup : 0);
                        break;
                    }
                case 8:
                    AboutLinkCell aboutLinkCell = holder.itemView;
                    if (i != ProfileActivity.this.botInfoRow) {
                        if (i == ProfileActivity.this.channelInfoRow) {
                            text = ProfileActivity.this.info.about;
                            while (text.contains("\n\n\n")) {
                                text = text.replace("\n\n\n", "\n\n");
                            }
                            aboutLinkCell.setTextAndIcon(text, C0553R.drawable.bot_info);
                            break;
                        }
                    }
                    aboutLinkCell.setTextAndIcon(ProfileActivity.this.botInfo.share_text, C0553R.drawable.bot_info);
                    break;
                    break;
                default:
                    checkBackground = false;
                    break;
            }
            if (checkBackground) {
                boolean enabled = false;
                if (ProfileActivity.this.user_id != 0) {
                    enabled = i == ProfileActivity.this.phoneRow || i == ProfileActivity.this.settingsTimerRow || i == ProfileActivity.this.settingsKeyRow || i == ProfileActivity.this.settingsNotificationsRow || i == ProfileActivity.this.sharedMediaRow || i == ProfileActivity.this.startSecretChatRow || i == ProfileActivity.this.usernameRow;
                } else if (ProfileActivity.this.chat_id != 0) {
                    enabled = i == ProfileActivity.this.convertRow || i == ProfileActivity.this.settingsNotificationsRow || i == ProfileActivity.this.sharedMediaRow || ((i > ProfileActivity.this.emptyRowChat2 && i < ProfileActivity.this.membersEndRow) || i == ProfileActivity.this.addMemberRow || i == ProfileActivity.this.channelNameRow || i == ProfileActivity.this.leaveChannelRow || i == ProfileActivity.this.membersRow || i == ProfileActivity.this.managementRow || i == ProfileActivity.this.blockedUsersRow || i == ProfileActivity.this.channelInfoRow);
                }
                if (enabled) {
                    if (holder.itemView.getBackground() == null) {
                        holder.itemView.setBackgroundResource(C0553R.drawable.list_selector);
                    }
                } else if (holder.itemView.getBackground() != null) {
                    holder.itemView.setBackgroundDrawable(null);
                }
            }
        }

        public int getItemCount() {
            return ProfileActivity.this.rowCount;
        }

        public int getItemViewType(int i) {
            if (i == ProfileActivity.this.emptyRow || i == ProfileActivity.this.overscrollRow || i == ProfileActivity.this.emptyRowChat || i == ProfileActivity.this.emptyRowChat2) {
                return 0;
            }
            if (i == ProfileActivity.this.sectionRow || i == ProfileActivity.this.botSectionRow) {
                return 1;
            }
            if (i == ProfileActivity.this.phoneRow || i == ProfileActivity.this.usernameRow || i == ProfileActivity.this.channelNameRow) {
                return 2;
            }
            if (i == ProfileActivity.this.leaveChannelRow || i == ProfileActivity.this.sharedMediaRow || i == ProfileActivity.this.settingsTimerRow || i == ProfileActivity.this.settingsNotificationsRow || i == ProfileActivity.this.startSecretChatRow || i == ProfileActivity.this.settingsKeyRow || i == ProfileActivity.this.membersRow || i == ProfileActivity.this.managementRow || i == ProfileActivity.this.blockedUsersRow || i == ProfileActivity.this.convertRow || i == ProfileActivity.this.addMemberRow) {
                return 3;
            }
            if (i > ProfileActivity.this.emptyRowChat2 && i < ProfileActivity.this.membersEndRow) {
                return 4;
            }
            if (i == ProfileActivity.this.membersSectionRow) {
                return 5;
            }
            if (i == ProfileActivity.this.convertHelpRow) {
                return 6;
            }
            if (i == ProfileActivity.this.loadMoreMembersRow) {
                return 7;
            }
            if (i == ProfileActivity.this.botInfoRow || i == ProfileActivity.this.channelInfoRow) {
                return 8;
            }
            return 0;
        }
    }

    public ProfileActivity(Bundle args) {
        super(args);
    }

    public boolean onFragmentCreate() {
        this.user_id = this.arguments.getInt("user_id", 0);
        this.chat_id = getArguments().getInt("chat_id", 0);
        if (this.user_id != 0) {
            this.dialog_id = this.arguments.getLong("dialog_id", 0);
            if (this.dialog_id != 0) {
                this.currentEncryptedChat = MessagesController.getInstance().getEncryptedChat(Integer.valueOf((int) (this.dialog_id >> 32)));
            }
            User user = MessagesController.getInstance().getUser(Integer.valueOf(this.user_id));
            if (user == null) {
                return false;
            }
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.updateInterfaces);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.contactsDidLoaded);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.encryptedChatCreated);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.encryptedChatUpdated);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.blockedUsersDidLoaded);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.botInfoDidLoaded);
            if (this.currentEncryptedChat != null) {
                NotificationCenter.getInstance().addObserver(this, NotificationCenter.didReceivedNewMessages);
            }
            this.userBlocked = MessagesController.getInstance().blockedUsers.contains(Integer.valueOf(this.user_id));
            if (user.bot) {
                BotQuery.loadBotInfo(user.id, true, this.classGuid);
            }
            MessagesController.getInstance().loadFullUser(MessagesController.getInstance().getUser(Integer.valueOf(this.user_id)), this.classGuid);
            this.participants = null;
            this.participantsMap = null;
        } else if (this.chat_id == 0) {
            return false;
        } else {
            this.currentChat = MessagesController.getInstance().getChat(Integer.valueOf(this.chat_id));
            if (this.currentChat == null) {
                final Semaphore semaphore = new Semaphore(0);
                MessagesStorage.getInstance().getStorageQueue().postRunnable(new Runnable() {
                    public void run() {
                        ProfileActivity.this.currentChat = MessagesStorage.getInstance().getChat(ProfileActivity.this.chat_id);
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
            }
            if (this.currentChat.megagroup) {
                getChannelParticipants(true);
            } else {
                this.participants = null;
                this.participantsMap = null;
            }
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.chatInfoDidLoaded);
            this.sortedUsers = new ArrayList();
            updateOnlineCount();
            this.avatarUpdater = new AvatarUpdater();
            this.avatarUpdater.delegate = new C16532();
            this.avatarUpdater.parentFragment = this;
        }
        if (this.dialog_id != 0) {
            SharedMediaQuery.getMediaCount(this.dialog_id, 0, this.classGuid, true);
        } else if (this.user_id != 0) {
            SharedMediaQuery.getMediaCount((long) this.user_id, 0, this.classGuid, true);
        } else if (this.chat_id > 0) {
            SharedMediaQuery.getMediaCount((long) (-this.chat_id), 0, this.classGuid, true);
            if (this.mergeDialogId != 0) {
                SharedMediaQuery.getMediaCount(this.mergeDialogId, 0, this.classGuid, true);
            }
        }
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.mediaCountDidLoaded);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.updateInterfaces);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.closeChats);
        updateRowsIds();
        return true;
    }

    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.mediaCountDidLoaded);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.updateInterfaces);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.closeChats);
        if (this.user_id != 0) {
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.contactsDidLoaded);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.encryptedChatCreated);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.encryptedChatUpdated);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.blockedUsersDidLoaded);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.botInfoDidLoaded);
            MessagesController.getInstance().cancelLoadFullUser(this.user_id);
            if (this.currentEncryptedChat != null) {
                NotificationCenter.getInstance().removeObserver(this, NotificationCenter.didReceivedNewMessages);
            }
        } else if (this.chat_id != 0) {
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.chatInfoDidLoaded);
            this.avatarUpdater.clear();
        }
    }

    public View createView(Context context) {
        ActionBar actionBar = this.actionBar;
        int i = (this.user_id != 0 || (ChatObject.isChannel(this.chat_id) && !this.currentChat.megagroup)) ? 5 : this.chat_id;
        actionBar.setBackgroundColor(AvatarDrawable.getProfileBackColorForId(i));
        actionBar = this.actionBar;
        i = (this.user_id != 0 || (ChatObject.isChannel(this.chat_id) && !this.currentChat.megagroup)) ? 5 : this.chat_id;
        actionBar.setItemsBackground(AvatarDrawable.getButtonColorForId(i));
        this.actionBar.setBackButtonDrawable(new BackDrawable(false));
        this.actionBar.setCastShadows(false);
        this.actionBar.setAddToContainer(false);
        this.hasOwnBackground = true;
        this.extraHeight = 88;
        if (AndroidUtilities.isTablet()) {
            this.actionBar.setOccupyStatusBar(false);
        }
        this.actionBar.setActionBarMenuOnItemClick(new C16553());
        createActionBarMenu();
        this.listAdapter = new ListAdapter(context);
        this.avatarDrawable = new AvatarDrawable();
        this.avatarDrawable.setProfile(true);
        this.fragmentView = new FrameLayout(context) {
            public boolean hasOverlappingRendering() {
                return false;
            }
        };
        FrameLayout frameLayout = this.fragmentView;
        this.listView = new RecyclerListView(context) {
            public boolean hasOverlappingRendering() {
                return false;
            }
        };
        this.listView.setBackgroundColor(-1);
        this.listView.setVerticalScrollBarEnabled(false);
        this.listView.setItemAnimator(null);
        this.listView.setLayoutAnimation(null);
        this.layoutManager = new LinearLayoutManager(context) {
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        };
        this.layoutManager.setOrientation(1);
        this.listView.setLayoutManager(this.layoutManager);
        RecyclerListView recyclerListView = this.listView;
        i = (this.user_id != 0 || (ChatObject.isChannel(this.chat_id) && !this.currentChat.megagroup)) ? 5 : this.chat_id;
        recyclerListView.setGlowColor(AvatarDrawable.getProfileBackColorForId(i));
        frameLayout.addView(this.listView, LayoutHelper.createFrame(-1, -1, 51));
        this.listView.setAdapter(this.listAdapter);
        this.listView.setOnItemClickListener(new C16567());
        if (this.chat_id != 0) {
            this.listView.setOnItemLongClickListener(new C16588());
        }
        frameLayout.addView(this.actionBar);
        this.extraHeightView = new View(context);
        ViewProxy.setPivotY(this.extraHeightView, 0.0f);
        View view = this.extraHeightView;
        i = (this.user_id != 0 || (ChatObject.isChannel(this.chat_id) && !this.currentChat.megagroup)) ? 5 : this.chat_id;
        view.setBackgroundColor(AvatarDrawable.getProfileBackColorForId(i));
        frameLayout.addView(this.extraHeightView, LayoutHelper.createFrame(-1, 88.0f));
        this.shadowView = new View(context);
        try {
            this.shadowView.setBackgroundResource(C0553R.drawable.header_shadow);
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
        }
        frameLayout.addView(this.shadowView, LayoutHelper.createFrame(-1, 3.0f));
        this.avatarImage = new BackupImageView(context);
        this.avatarImage.setRoundRadius(AndroidUtilities.dp(21.0f));
        ViewProxy.setPivotX(this.avatarImage, 0.0f);
        ViewProxy.setPivotY(this.avatarImage, 0.0f);
        frameLayout.addView(this.avatarImage, LayoutHelper.createFrame(42, 42.0f, 51, 64.0f, 0.0f, 0.0f, 0.0f));
        this.avatarImage.setOnClickListener(new C11809());
        int a = 0;
        while (a < 2) {
            if (this.playProfileAnimation || a != 0) {
                float f;
                this.nameTextView[a] = new TextView(context);
                this.nameTextView[a].setTextColor(-1);
                this.nameTextView[a].setTextSize(1, 18.0f);
                this.nameTextView[a].setLines(1);
                this.nameTextView[a].setMaxLines(1);
                this.nameTextView[a].setSingleLine(true);
                this.nameTextView[a].setEllipsize(TruncateAt.END);
                this.nameTextView[a].setGravity(3);
                this.nameTextView[a].setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
                this.nameTextView[a].setCompoundDrawablePadding(AndroidUtilities.dp(4.0f));
                ViewProxy.setPivotX(this.nameTextView[a], 0.0f);
                ViewProxy.setPivotY(this.nameTextView[a], 0.0f);
                frameLayout.addView(this.nameTextView[a], LayoutHelper.createFrame(-2, -2.0f, 51, 118.0f, 0.0f, a == 0 ? 48.0f : 0.0f, 0.0f));
                this.onlineTextView[a] = new TextView(context);
                TextView textView = this.onlineTextView[a];
                i = (this.user_id != 0 || (ChatObject.isChannel(this.chat_id) && !this.currentChat.megagroup)) ? 5 : this.chat_id;
                textView.setTextColor(AvatarDrawable.getProfileTextColorForId(i));
                this.onlineTextView[a].setTextSize(1, 14.0f);
                this.onlineTextView[a].setLines(1);
                this.onlineTextView[a].setMaxLines(1);
                this.onlineTextView[a].setSingleLine(true);
                this.onlineTextView[a].setEllipsize(TruncateAt.END);
                this.onlineTextView[a].setGravity(3);
                View view2 = this.onlineTextView[a];
                if (a == 0) {
                    f = 48.0f;
                } else {
                    f = 8.0f;
                }
                frameLayout.addView(view2, LayoutHelper.createFrame(-2, -2.0f, 51, 118.0f, 0.0f, f, 0.0f));
            }
            a++;
        }
        if (this.user_id != 0 || (this.chat_id >= 0 && !ChatObject.isLeftFromChat(this.currentChat))) {
            this.writeButton = new ImageView(context);
            try {
                this.writeButton.setBackgroundResource(C0553R.drawable.floating_user_states);
            } catch (Throwable e2) {
                FileLog.m611e("tmessages", e2);
            }
            this.writeButton.setScaleType(ScaleType.CENTER);
            if (this.user_id != 0) {
                this.writeButton.setImageResource(C0553R.drawable.floating_message);
                this.writeButton.setPadding(0, AndroidUtilities.dp(3.0f), 0, 0);
            } else if (this.chat_id != 0) {
                boolean isChannel = ChatObject.isChannel(this.currentChat);
                if ((!isChannel || this.currentChat.creator || (this.currentChat.megagroup && this.currentChat.editor)) && (isChannel || this.currentChat.admin || this.currentChat.creator || !this.currentChat.admins_enabled)) {
                    this.writeButton.setImageResource(C0553R.drawable.floating_camera);
                } else {
                    this.writeButton.setImageResource(C0553R.drawable.floating_message);
                    this.writeButton.setPadding(0, AndroidUtilities.dp(3.0f), 0, 0);
                }
            }
            frameLayout.addView(this.writeButton, LayoutHelper.createFrame(-2, -2.0f, 53, 0.0f, 0.0f, 16.0f, 0.0f));
            if (VERSION.SDK_INT >= 21) {
                StateListAnimator animator = new StateListAnimator();
                animator.addState(new int[]{16842919}, ObjectAnimator.ofFloat(this.writeButton, "translationZ", new float[]{(float) AndroidUtilities.dp(2.0f), (float) AndroidUtilities.dp(4.0f)}).setDuration(200));
                animator.addState(new int[0], ObjectAnimator.ofFloat(this.writeButton, "translationZ", new float[]{(float) AndroidUtilities.dp(4.0f), (float) AndroidUtilities.dp(2.0f)}).setDuration(200));
                this.writeButton.setStateListAnimator(animator);
                this.writeButton.setOutlineProvider(new ViewOutlineProvider() {
                    @SuppressLint({"NewApi"})
                    public void getOutline(View view, Outline outline) {
                        outline.setOval(0, 0, AndroidUtilities.dp(56.0f), AndroidUtilities.dp(56.0f));
                    }
                });
            }
            this.writeButton.setOnClickListener(new OnClickListener() {

                class C11651 implements DialogInterface.OnClickListener {
                    C11651() {
                    }

                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            ProfileActivity.this.avatarUpdater.openCamera();
                        } else if (i == 1) {
                            ProfileActivity.this.avatarUpdater.openGallery();
                        } else if (i == 2) {
                            MessagesController.getInstance().changeChatAvatar(ProfileActivity.this.chat_id, null);
                        }
                    }
                }

                public void onClick(View v) {
                    if (ProfileActivity.this.getParentActivity() != null) {
                        Bundle args;
                        if (ProfileActivity.this.user_id != 0) {
                            if (ProfileActivity.this.playProfileAnimation && (ProfileActivity.this.parentLayout.fragmentsStack.get(ProfileActivity.this.parentLayout.fragmentsStack.size() - 2) instanceof ChatActivity)) {
                                ProfileActivity.this.finishFragment();
                                return;
                            }
                            User user = MessagesController.getInstance().getUser(Integer.valueOf(ProfileActivity.this.user_id));
                            if (user != null && !(user instanceof TL_userEmpty)) {
                                NotificationCenter.getInstance().removeObserver(ProfileActivity.this, NotificationCenter.closeChats);
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats, new Object[0]);
                                args = new Bundle();
                                args.putInt("user_id", ProfileActivity.this.user_id);
                                ProfileActivity.this.presentFragment(new ChatActivity(args), true);
                            }
                        } else if (ProfileActivity.this.chat_id != 0) {
                            boolean isChannel = ChatObject.isChannel(ProfileActivity.this.currentChat);
                            if ((!isChannel || ProfileActivity.this.currentChat.creator || (ProfileActivity.this.currentChat.megagroup && ProfileActivity.this.currentChat.editor)) && (isChannel || ProfileActivity.this.currentChat.admin || ProfileActivity.this.currentChat.creator || !ProfileActivity.this.currentChat.admins_enabled)) {
                                Builder builder = new Builder(ProfileActivity.this.getParentActivity());
                                Chat chat = MessagesController.getInstance().getChat(Integer.valueOf(ProfileActivity.this.chat_id));
                                CharSequence[] items = (chat.photo == null || chat.photo.photo_big == null || (chat.photo instanceof TL_chatPhotoEmpty)) ? new CharSequence[]{LocaleController.getString("FromCamera", C0553R.string.FromCamera), LocaleController.getString("FromGalley", C0553R.string.FromGalley)} : new CharSequence[]{LocaleController.getString("FromCamera", C0553R.string.FromCamera), LocaleController.getString("FromGalley", C0553R.string.FromGalley), LocaleController.getString("DeletePhoto", C0553R.string.DeletePhoto)};
                                builder.setItems(items, new C11651());
                                ProfileActivity.this.showDialog(builder.create());
                            } else if (ProfileActivity.this.playProfileAnimation && (ProfileActivity.this.parentLayout.fragmentsStack.get(ProfileActivity.this.parentLayout.fragmentsStack.size() - 2) instanceof ChatActivity)) {
                                ProfileActivity.this.finishFragment();
                            } else {
                                NotificationCenter.getInstance().removeObserver(ProfileActivity.this, NotificationCenter.closeChats);
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats, new Object[0]);
                                args = new Bundle();
                                args.putInt("chat_id", ProfileActivity.this.currentChat.id);
                                ProfileActivity.this.presentFragment(new ChatActivity(args), true);
                            }
                        }
                    }
                }
            });
        }
        needLayout();
        this.listView.setOnScrollListener(new OnScrollListener() {
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                ProfileActivity.this.checkListViewScroll();
                if (ProfileActivity.this.participants != null && ProfileActivity.this.loadMoreMembersRow != -1 && ProfileActivity.this.layoutManager.findLastVisibleItemPosition() > ProfileActivity.this.loadMoreMembersRow - 8) {
                    ProfileActivity.this.getChannelParticipants(false);
                }
            }
        });
        return this.fragmentView;
    }

    private void leaveChatPressed() {
        Builder builder = new Builder(getParentActivity());
        if (!ChatObject.isChannel(this.chat_id) || this.currentChat.megagroup) {
            builder.setMessage(LocaleController.getString("AreYouSureDeleteAndExit", C0553R.string.AreYouSureDeleteAndExit));
        } else {
            builder.setMessage(ChatObject.isChannel(this.chat_id) ? LocaleController.getString("ChannelLeaveAlert", C0553R.string.ChannelLeaveAlert) : LocaleController.getString("AreYouSureDeleteAndExit", C0553R.string.AreYouSureDeleteAndExit));
        }
        builder.setTitle(LocaleController.getString("AppName", C0553R.string.AppName));
        builder.setPositiveButton(LocaleController.getString("OK", C0553R.string.OK), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                ProfileActivity.this.kickUser(0);
            }
        });
        builder.setNegativeButton(LocaleController.getString("Cancel", C0553R.string.Cancel), null);
        showDialog(builder.create());
    }

    public void saveSelfArgs(Bundle args) {
        if (this.chat_id != 0 && this.avatarUpdater != null && this.avatarUpdater.currentPicturePath != null) {
            args.putString("path", this.avatarUpdater.currentPicturePath);
        }
    }

    public void restoreSelfArgs(Bundle args) {
        if (this.chat_id != 0) {
            MessagesController.getInstance().loadChatInfo(this.chat_id, null, false);
            if (this.avatarUpdater != null) {
                this.avatarUpdater.currentPicturePath = args.getString("path");
            }
        }
    }

    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        if (this.chat_id != 0) {
            this.avatarUpdater.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void getChannelParticipants(boolean reload) {
        int i = 0;
        if (!this.loadingUsers && this.participants != null) {
            int delay;
            this.loadingUsers = true;
            if (VERSION.SDK_INT < 11 || this.participants.isEmpty() || !reload) {
                delay = 0;
            } else {
                delay = 300;
            }
            final TL_channels_getParticipants req = new TL_channels_getParticipants();
            req.channel = MessagesController.getInputChannel(this.chat_id);
            req.filter = new TL_channelParticipantsRecent();
            if (!reload) {
                i = this.participants.size();
            }
            req.offset = i;
            req.limit = 33;
            ConnectionsManager.getInstance().bindRequestToGuid(ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                public void run(final TLObject response, final TL_error error) {
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        public void run() {
                            if (error == null) {
                                TL_channels_channelParticipants res = response;
                                MessagesController.getInstance().putUsers(res.users, false);
                                if (res.participants.size() == 33) {
                                    res.participants.remove(32);
                                } else {
                                    ProfileActivity.this.usersEndReached = true;
                                }
                                if (req.offset == 0) {
                                    ProfileActivity.this.participants.clear();
                                    ProfileActivity.this.participantsMap.clear();
                                    MessagesStorage.getInstance().putUsersAndChats(res.users, null, true, true);
                                    MessagesStorage.getInstance().updateChannelUsers(ProfileActivity.this.chat_id, res.participants);
                                }
                                for (int a = 0; a < res.participants.size(); a++) {
                                    ChannelParticipant participant = (ChannelParticipant) res.participants.get(a);
                                    if (!ProfileActivity.this.participantsMap.containsKey(Integer.valueOf(participant.user_id))) {
                                        ProfileActivity.this.participants.add(participant);
                                        ProfileActivity.this.participantsMap.put(Integer.valueOf(participant.user_id), participant);
                                    }
                                }
                            }
                            ProfileActivity.this.loadingUsers = false;
                            ProfileActivity.this.updateRowsIds();
                            if (ProfileActivity.this.listAdapter != null) {
                                ProfileActivity.this.listAdapter.notifyDataSetChanged();
                            }
                        }
                    }, (long) delay);
                }
            }), this.classGuid);
        }
    }

    private void openAddMember() {
        boolean z = true;
        Bundle args = new Bundle();
        args.putBoolean("onlyUsers", true);
        args.putBoolean("destroyAfterSelect", true);
        args.putBoolean("returnAsResult", true);
        String str = "needForwardCount";
        if (ChatObject.isChannel(this.currentChat)) {
            z = false;
        }
        args.putBoolean(str, z);
        if (this.chat_id > 0) {
            if (this.currentChat.creator) {
                args.putInt("chat_id", this.currentChat.id);
            }
            args.putString("selectAlertString", LocaleController.getString("AddToTheGroup", C0553R.string.AddToTheGroup));
        }
        ContactsActivity fragment = new ContactsActivity(args);
        fragment.setDelegate(new ContactsActivityDelegate() {
            public void didSelectContact(User user, String param) {
                MessagesController.getInstance().addUserToChat(ProfileActivity.this.chat_id, user, ProfileActivity.this.info, param != null ? Utilities.parseInt(param).intValue() : 0, null, ProfileActivity.this);
            }
        });
        HashMap<Integer, User> users;
        int a;
        if (this.info instanceof TL_chatFull) {
            users = new HashMap();
            for (a = 0; a < this.info.participants.participants.size(); a++) {
                users.put(Integer.valueOf(((ChatParticipant) this.info.participants.participants.get(a)).user_id), null);
            }
            fragment.setIgnoreUsers(users);
        } else if (this.participants != null) {
            users = new HashMap();
            for (a = 0; a < this.participants.size(); a++) {
                users.put(Integer.valueOf(((ChannelParticipant) this.participants.get(a)).user_id), null);
            }
            fragment.setIgnoreUsers(users);
        }
        presentFragment(fragment);
    }

    private void checkListViewScroll() {
        int i = 0;
        if (this.listView.getChildCount() != 0 && !this.openAnimationInProgress) {
            int height = 0;
            View child = this.listView.getChildAt(0);
            if (child != null) {
                if (this.layoutManager.findFirstVisibleItemPosition() == 0) {
                    int dp = AndroidUtilities.dp(88.0f);
                    if (child.getTop() < 0) {
                        i = child.getTop();
                    }
                    height = dp + i;
                }
                if (this.extraHeight != height) {
                    this.extraHeight = height;
                    needLayout();
                }
            }
        }
    }

    private void needLayout() {
        LayoutParams layoutParams;
        int newTop = (this.actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight();
        if (!(this.listView == null || this.openAnimationInProgress)) {
            layoutParams = (LayoutParams) this.listView.getLayoutParams();
            if (layoutParams.topMargin != newTop) {
                layoutParams.topMargin = newTop;
                this.listView.setLayoutParams(layoutParams);
                ViewProxy.setTranslationY(this.extraHeightView, (float) newTop);
            }
        }
        if (this.avatarImage != null) {
            float diff = ((float) this.extraHeight) / ((float) AndroidUtilities.dp(88.0f));
            ViewProxy.setScaleY(this.extraHeightView, diff);
            ViewProxy.setTranslationY(this.shadowView, (float) (this.extraHeight + newTop));
            this.listView.setTopGlowOffset(this.extraHeight);
            if (this.writeButton != null) {
                if (VERSION.SDK_INT < 11) {
                    layoutParams = (LayoutParams) this.writeButton.getLayoutParams();
                    layoutParams.topMargin = (((this.actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight()) + this.extraHeight) - AndroidUtilities.dp(29.5f);
                    this.writeButton.setLayoutParams(layoutParams);
                } else {
                    ViewProxy.setTranslationY(this.writeButton, (float) ((((this.actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight()) + this.extraHeight) - AndroidUtilities.dp(29.5f)));
                }
                if (!this.openAnimationInProgress) {
                    final boolean setVisible = diff > 0.2f;
                    if (setVisible != (this.writeButton.getTag() == null)) {
                        if (setVisible) {
                            this.writeButton.setTag(null);
                            if (VERSION.SDK_INT < 11) {
                                this.writeButton.setVisibility(0);
                            }
                        } else {
                            this.writeButton.setTag(Integer.valueOf(0));
                        }
                        if (this.writeButtonAnimation != null) {
                            AnimatorSetProxy old = this.writeButtonAnimation;
                            this.writeButtonAnimation = null;
                            old.cancel();
                        }
                        this.writeButtonAnimation = new AnimatorSetProxy();
                        AnimatorSetProxy animatorSetProxy;
                        Object[] objArr;
                        if (setVisible) {
                            this.writeButtonAnimation.setInterpolator(new DecelerateInterpolator());
                            animatorSetProxy = this.writeButtonAnimation;
                            objArr = new Object[3];
                            objArr[0] = ObjectAnimatorProxy.ofFloat(this.writeButton, "scaleX", 1.0f);
                            objArr[1] = ObjectAnimatorProxy.ofFloat(this.writeButton, "scaleY", 1.0f);
                            objArr[2] = ObjectAnimatorProxy.ofFloat(this.writeButton, "alpha", 1.0f);
                            animatorSetProxy.playTogether(objArr);
                        } else {
                            this.writeButtonAnimation.setInterpolator(new AccelerateInterpolator());
                            animatorSetProxy = this.writeButtonAnimation;
                            objArr = new Object[3];
                            objArr[0] = ObjectAnimatorProxy.ofFloat(this.writeButton, "scaleX", 0.2f);
                            objArr[1] = ObjectAnimatorProxy.ofFloat(this.writeButton, "scaleY", 0.2f);
                            objArr[2] = ObjectAnimatorProxy.ofFloat(this.writeButton, "alpha", 0.0f);
                            animatorSetProxy.playTogether(objArr);
                        }
                        this.writeButtonAnimation.setDuration(150);
                        this.writeButtonAnimation.addListener(new AnimatorListenerAdapterProxy() {
                            public void onAnimationEnd(Object animation) {
                                if (ProfileActivity.this.writeButtonAnimation != null && ProfileActivity.this.writeButtonAnimation.equals(animation)) {
                                    ProfileActivity.this.writeButton.clearAnimation();
                                    if (VERSION.SDK_INT < 11) {
                                        ProfileActivity.this.writeButton.setVisibility(setVisible ? 0 : 8);
                                    }
                                    ProfileActivity.this.writeButtonAnimation = null;
                                }
                            }
                        });
                        this.writeButtonAnimation.start();
                    }
                }
            }
            float avatarY = ((((float) (this.actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0)) + ((((float) ActionBar.getCurrentActionBarHeight()) / 2.0f) * (1.0f + diff))) - (21.0f * AndroidUtilities.density)) + ((27.0f * AndroidUtilities.density) * diff);
            if (VERSION.SDK_INT < 11) {
                layoutParams = (LayoutParams) this.avatarImage.getLayoutParams();
                int ceil = (int) Math.ceil((double) ((((float) AndroidUtilities.dp(42.0f)) * (42.0f + (18.0f * diff))) / 42.0f));
                layoutParams.width = ceil;
                layoutParams.height = ceil;
                layoutParams.leftMargin = (int) Math.ceil((double) (((float) AndroidUtilities.dp(64.0f)) - (((float) AndroidUtilities.dp(47.0f)) * diff)));
                layoutParams.topMargin = (int) Math.ceil((double) avatarY);
                this.avatarImage.setLayoutParams(layoutParams);
                this.avatarImage.setRoundRadius(layoutParams.height / 2);
            } else {
                ViewProxy.setScaleX(this.avatarImage, (42.0f + (18.0f * diff)) / 42.0f);
                ViewProxy.setScaleY(this.avatarImage, (42.0f + (18.0f * diff)) / 42.0f);
                ViewProxy.setTranslationX(this.avatarImage, ((float) (-AndroidUtilities.dp(47.0f))) * diff);
                ViewProxy.setTranslationY(this.avatarImage, (float) Math.ceil((double) avatarY));
            }
            for (int a = 0; a < 2; a++) {
                if (this.nameTextView[a] != null) {
                    ViewProxy.setTranslationX(this.nameTextView[a], (-21.0f * AndroidUtilities.density) * diff);
                    ViewProxy.setTranslationY(this.nameTextView[a], (((float) Math.floor((double) avatarY)) - ((float) Math.ceil((double) AndroidUtilities.density))) + ((float) Math.floor((double) ((7.0f * AndroidUtilities.density) * diff))));
                    ViewProxy.setTranslationX(this.onlineTextView[a], (-21.0f * AndroidUtilities.density) * diff);
                    ViewProxy.setTranslationY(this.onlineTextView[a], (((float) Math.floor((double) avatarY)) + ((float) AndroidUtilities.dp(22.0f))) + (((float) Math.floor((double) (11.0f * AndroidUtilities.density))) * diff));
                    ViewProxy.setScaleX(this.nameTextView[a], 1.0f + (0.12f * diff));
                    ViewProxy.setScaleY(this.nameTextView[a], 1.0f + (0.12f * diff));
                    if (a == 1 && !this.openAnimationInProgress) {
                        int width;
                        if (AndroidUtilities.isTablet()) {
                            width = AndroidUtilities.dp(490.0f);
                        } else {
                            width = AndroidUtilities.displaySize.x;
                        }
                        width = (int) (((float) (width - AndroidUtilities.dp(126.0f + (40.0f * (1.0f - diff))))) - ViewProxy.getTranslationX(this.nameTextView[a]));
                        float width2 = this.nameTextView[a].getPaint().measureText(this.nameTextView[a].getText().toString()) * ViewProxy.getScaleX(this.nameTextView[a]);
                        Drawable[] drawables = this.nameTextView[a].getCompoundDrawables();
                        for (int b = 0; b < drawables.length; b++) {
                            if (drawables[b] != null) {
                                width2 += (float) (drawables[b].getIntrinsicWidth() + AndroidUtilities.dp(4.0f));
                            }
                        }
                        layoutParams = (LayoutParams) this.nameTextView[a].getLayoutParams();
                        if (((float) width) < width2) {
                            layoutParams.width = (int) Math.ceil((double) (((float) width) / ViewProxy.getScaleX(this.nameTextView[a])));
                        } else {
                            layoutParams.width = -2;
                        }
                        this.nameTextView[a].setLayoutParams(layoutParams);
                        layoutParams = (LayoutParams) this.onlineTextView[a].getLayoutParams();
                        layoutParams.rightMargin = (int) Math.ceil((double) ((ViewProxy.getTranslationX(this.onlineTextView[a]) + ((float) AndroidUtilities.dp(8.0f))) + (((float) AndroidUtilities.dp(40.0f)) * (1.0f - diff))));
                        this.onlineTextView[a].setLayoutParams(layoutParams);
                    }
                }
            }
        }
    }

    private void fixLayout() {
        if (this.fragmentView != null) {
            this.fragmentView.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
                public boolean onPreDraw() {
                    if (ProfileActivity.this.fragmentView != null) {
                        ProfileActivity.this.checkListViewScroll();
                        ProfileActivity.this.needLayout();
                        ProfileActivity.this.fragmentView.getViewTreeObserver().removeOnPreDrawListener(this);
                    }
                    return true;
                }
            });
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        fixLayout();
    }

    public void didReceivedNotification(int id, Object... args) {
        Holder holder;
        Chat newChat;
        int count;
        int a;
        if (id == NotificationCenter.updateInterfaces) {
            int mask = ((Integer) args[0]).intValue();
            if (this.user_id != 0) {
                if (!((mask & 2) == 0 && (mask & 1) == 0 && (mask & 4) == 0)) {
                    updateProfileData();
                }
                if ((mask & 1024) != 0 && this.listView != null) {
                    holder = (Holder) this.listView.findViewHolderForPosition(this.phoneRow);
                    if (holder != null) {
                        this.listAdapter.onBindViewHolder(holder, this.phoneRow);
                    }
                }
            } else if (this.chat_id != 0) {
                if ((mask & 16384) != 0) {
                    newChat = MessagesController.getInstance().getChat(Integer.valueOf(this.chat_id));
                    if (newChat != null) {
                        this.currentChat = newChat;
                        createActionBarMenu();
                        updateRowsIds();
                        if (this.listAdapter != null) {
                            this.listAdapter.notifyDataSetChanged();
                        }
                    }
                }
                if (!((mask & 8192) == 0 && (mask & 8) == 0 && (mask & 16) == 0 && (mask & 32) == 0 && (mask & 4) == 0)) {
                    updateOnlineCount();
                    updateProfileData();
                }
                if ((mask & 8192) != 0) {
                    updateRowsIds();
                    if (this.listAdapter != null) {
                        this.listAdapter.notifyDataSetChanged();
                    }
                }
                if (((mask & 2) != 0 || (mask & 1) != 0 || (mask & 4) != 0) && this.listView != null) {
                    count = this.listView.getChildCount();
                    for (a = 0; a < count; a++) {
                        View child = this.listView.getChildAt(a);
                        if (child instanceof UserCell) {
                            ((UserCell) child).update(mask);
                        }
                    }
                }
            }
        } else if (id == NotificationCenter.contactsDidLoaded) {
            createActionBarMenu();
        } else if (id == NotificationCenter.mediaCountDidLoaded) {
            long uid = ((Long) args[0]).longValue();
            long did = this.dialog_id;
            if (did == 0) {
                if (this.user_id != 0) {
                    did = (long) this.user_id;
                } else if (this.chat_id != 0) {
                    did = (long) (-this.chat_id);
                }
            }
            if (uid == did || uid == this.mergeDialogId) {
                if (uid == did) {
                    this.totalMediaCount = ((Integer) args[1]).intValue();
                } else {
                    this.totalMediaCountMerge = ((Integer) args[1]).intValue();
                }
                if (this.listView != null) {
                    count = this.listView.getChildCount();
                    for (a = 0; a < count; a++) {
                        holder = (Holder) this.listView.getChildViewHolder(this.listView.getChildAt(a));
                        if (holder.getAdapterPosition() == this.sharedMediaRow) {
                            this.listAdapter.onBindViewHolder(holder, this.sharedMediaRow);
                            return;
                        }
                    }
                }
            }
        } else if (id == NotificationCenter.encryptedChatCreated) {
            if (this.creatingChat) {
                final Object[] objArr = args;
                AndroidUtilities.runOnUIThread(new Runnable() {
                    public void run() {
                        NotificationCenter.getInstance().removeObserver(ProfileActivity.this, NotificationCenter.closeChats);
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats, new Object[0]);
                        EncryptedChat encryptedChat = objArr[0];
                        Bundle args2 = new Bundle();
                        args2.putInt("enc_id", encryptedChat.id);
                        ProfileActivity.this.presentFragment(new ChatActivity(args2), true);
                    }
                });
            }
        } else if (id == NotificationCenter.encryptedChatUpdated) {
            EncryptedChat chat = args[0];
            if (this.currentEncryptedChat != null && chat.id == this.currentEncryptedChat.id) {
                this.currentEncryptedChat = chat;
                updateRowsIds();
                if (this.listAdapter != null) {
                    this.listAdapter.notifyDataSetChanged();
                    checkListViewScroll();
                }
            }
        } else if (id == NotificationCenter.blockedUsersDidLoaded) {
            boolean oldValue = this.userBlocked;
            this.userBlocked = MessagesController.getInstance().blockedUsers.contains(Integer.valueOf(this.user_id));
            if (oldValue != this.userBlocked) {
                createActionBarMenu();
            }
        } else if (id == NotificationCenter.chatInfoDidLoaded) {
            ChatFull chatFull = args[0];
            if (chatFull.id == this.chat_id) {
                boolean byChannelUsers = ((Boolean) args[2]).booleanValue();
                if ((this.info instanceof TL_channelFull) && chatFull.participants == null && this.info != null) {
                    chatFull.participants = this.info.participants;
                }
                this.info = chatFull;
                if (this.mergeDialogId == 0 && this.info.migrated_from_chat_id != 0) {
                    this.mergeDialogId = (long) (-this.info.migrated_from_chat_id);
                    SharedMediaQuery.getMediaCount(this.mergeDialogId, 0, this.classGuid, true);
                }
                fetchUsersFromChannelInfo();
                updateOnlineCount();
                updateRowsIds();
                if (this.listAdapter != null) {
                    this.listAdapter.notifyDataSetChanged();
                    checkListViewScroll();
                }
                newChat = MessagesController.getInstance().getChat(Integer.valueOf(this.chat_id));
                if (newChat != null) {
                    this.currentChat = newChat;
                    createActionBarMenu();
                }
                if (this.currentChat.megagroup && !byChannelUsers) {
                    getChannelParticipants(true);
                }
            }
        } else if (id == NotificationCenter.closeChats) {
            removeSelfFromStack();
        } else if (id == NotificationCenter.botInfoDidLoaded) {
            BotInfo info = args[0];
            if (info.user_id == this.user_id) {
                this.botInfo = info;
                updateRowsIds();
                if (this.listAdapter != null) {
                    this.listAdapter.notifyDataSetChanged();
                    checkListViewScroll();
                }
            }
        } else if (id == NotificationCenter.didReceivedNewMessages && ((Long) args[0]).longValue() == this.dialog_id) {
            ArrayList<MessageObject> arr = args[1];
            for (a = 0; a < arr.size(); a++) {
                MessageObject obj = (MessageObject) arr.get(a);
                if (this.currentEncryptedChat != null && obj.messageOwner.action != null && (obj.messageOwner.action instanceof TL_messageEncryptedAction) && (obj.messageOwner.action.encryptedAction instanceof TL_decryptedMessageActionSetMessageTTL)) {
                    TL_decryptedMessageActionSetMessageTTL action = obj.messageOwner.action.encryptedAction;
                    if (this.listAdapter != null) {
                        this.listAdapter.notifyDataSetChanged();
                        checkListViewScroll();
                    }
                }
            }
        }
    }

    public void onResume() {
        super.onResume();
        if (this.listAdapter != null) {
            this.listAdapter.notifyDataSetChanged();
        }
        updateProfileData();
        fixLayout();
    }

    public void setPlayProfileAnimation(boolean value) {
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0);
        if (!AndroidUtilities.isTablet() && VERSION.SDK_INT > 10 && preferences.getBoolean("view_animations", true)) {
            this.playProfileAnimation = value;
        }
    }

    protected void onTransitionAnimationStart(boolean isOpen, boolean backward) {
        if (!backward && this.playProfileAnimation) {
            this.openAnimationInProgress = true;
        }
        NotificationCenter.getInstance().setAnimationInProgress(true);
    }

    protected void onTransitionAnimationEnd(boolean isOpen, boolean backward) {
        if (!backward && this.playProfileAnimation) {
            this.openAnimationInProgress = false;
        }
        NotificationCenter.getInstance().setAnimationInProgress(false);
    }

    public float getAnimationProgress() {
        return this.animationProgress;
    }

    public void setAnimationProgress(float progress) {
        int i = 5;
        this.animationProgress = progress;
        ViewProxy.setAlpha(this.listView, progress);
        ViewProxy.setTranslationX(this.listView, ((float) AndroidUtilities.dp(48.0f)) * (1.0f - progress));
        int i2 = (this.user_id != 0 || (ChatObject.isChannel(this.chat_id) && !this.currentChat.megagroup)) ? 5 : this.chat_id;
        int color = AvatarDrawable.getProfileBackColorForId(i2);
        int rD = (int) (((float) (Color.red(color) - 84)) * progress);
        int gD = (int) (((float) (Color.green(color) - 117)) * progress);
        int bD = (int) (((float) (Color.blue(color) - 158)) * progress);
        this.actionBar.setBackgroundColor(Color.rgb(rD + 84, gD + 117, bD + 158));
        this.extraHeightView.setBackgroundColor(Color.rgb(rD + 84, gD + 117, bD + 158));
        if (this.user_id == 0 && (!ChatObject.isChannel(this.chat_id) || this.currentChat.megagroup)) {
            i = this.chat_id;
        }
        color = AvatarDrawable.getProfileTextColorForId(i);
        rD = (int) (((float) (Color.red(color) - 215)) * progress);
        gD = (int) (((float) (Color.green(color) - 232)) * progress);
        bD = (int) (((float) (Color.blue(color) - 247)) * progress);
        for (int a = 0; a < 2; a++) {
            if (this.onlineTextView[a] != null) {
                this.onlineTextView[a].setTextColor(Color.rgb(rD + 215, gD + 232, bD + 247));
            }
        }
        this.extraHeight = (int) (((float) this.initialAnimationExtraHeight) * progress);
        color = AvatarDrawable.getProfileColorForId(this.user_id != 0 ? this.user_id : this.chat_id);
        int color2 = AvatarDrawable.getColorForId(this.user_id != 0 ? this.user_id : this.chat_id);
        if (color != color2) {
            this.avatarDrawable.setColor(Color.rgb(Color.red(color2) + ((int) (((float) (Color.red(color) - Color.red(color2))) * progress)), Color.green(color2) + ((int) (((float) (Color.green(color) - Color.green(color2))) * progress)), Color.blue(color2) + ((int) (((float) (Color.blue(color) - Color.blue(color2))) * progress))));
            this.avatarImage.invalidate();
        }
        needLayout();
    }

    protected AnimatorSetProxy onCustomTransitionAnimation(boolean isOpen, Runnable callback) {
        if (!this.playProfileAnimation) {
            return null;
        }
        final AnimatorSetProxy animatorSet = new AnimatorSetProxy();
        animatorSet.setDuration(150);
        if (VERSION.SDK_INT > 15) {
            this.listView.setLayerType(2, null);
        }
        ActionBarMenu menu = this.actionBar.createMenu();
        if (menu.getItem(10) == null && this.animatingItem == null) {
            this.animatingItem = menu.addItem(10, (int) C0553R.drawable.ic_ab_other);
        }
        ArrayList animators;
        int a;
        Object obj;
        String str;
        float[] fArr;
        if (isOpen) {
            LayoutParams layoutParams = (LayoutParams) this.onlineTextView[1].getLayoutParams();
            layoutParams.rightMargin = (int) ((-21.0f * AndroidUtilities.density) + ((float) AndroidUtilities.dp(8.0f)));
            this.onlineTextView[1].setLayoutParams(layoutParams);
            int width = (int) Math.ceil((double) (((float) (AndroidUtilities.displaySize.x - AndroidUtilities.dp(126.0f))) + (21.0f * AndroidUtilities.density)));
            float width2 = this.nameTextView[1].getPaint().measureText(this.nameTextView[1].getText().toString()) * 1.12f;
            Drawable[] drawables = this.nameTextView[1].getCompoundDrawables();
            for (int b = 0; b < drawables.length; b++) {
                if (drawables[b] != null) {
                    width2 += (float) (drawables[b].getIntrinsicWidth() + AndroidUtilities.dp(4.0f));
                }
            }
            layoutParams = (LayoutParams) this.nameTextView[1].getLayoutParams();
            if (((float) width) < width2) {
                layoutParams.width = (int) Math.ceil((double) (((float) width) / 1.12f));
            } else {
                layoutParams.width = -2;
            }
            this.nameTextView[1].setLayoutParams(layoutParams);
            this.initialAnimationExtraHeight = AndroidUtilities.dp(88.0f);
            this.fragmentView.setBackgroundColor(0);
            setAnimationProgress(0.0f);
            animators = new ArrayList();
            animators.add(ObjectAnimatorProxy.ofFloat(this, "animationProgress", 0.0f, 1.0f));
            if (this.writeButton != null) {
                ViewProxy.setScaleX(this.writeButton, 0.2f);
                ViewProxy.setScaleY(this.writeButton, 0.2f);
                ViewProxy.setAlpha(this.writeButton, 0.0f);
                animators.add(ObjectAnimatorProxy.ofFloat(this.writeButton, "scaleX", 1.0f));
                animators.add(ObjectAnimatorProxy.ofFloat(this.writeButton, "scaleY", 1.0f));
                animators.add(ObjectAnimatorProxy.ofFloat(this.writeButton, "alpha", 1.0f));
            }
            a = 0;
            while (a < 2) {
                ViewProxy.setAlpha(this.onlineTextView[a], a == 0 ? 1.0f : 0.0f);
                ViewProxy.setAlpha(this.nameTextView[a], a == 0 ? 1.0f : 0.0f);
                obj = this.onlineTextView[a];
                str = "alpha";
                fArr = new float[1];
                fArr[0] = a == 0 ? 0.0f : 1.0f;
                animators.add(ObjectAnimatorProxy.ofFloat(obj, str, fArr));
                obj = this.nameTextView[a];
                str = "alpha";
                fArr = new float[1];
                fArr[0] = a == 0 ? 0.0f : 1.0f;
                animators.add(ObjectAnimatorProxy.ofFloat(obj, str, fArr));
                a++;
            }
            if (this.animatingItem != null) {
                ViewProxy.setAlpha(this.animatingItem, 1.0f);
                animators.add(ObjectAnimatorProxy.ofFloat(this.animatingItem, "alpha", 0.0f));
            }
            animatorSet.playTogether(animators);
        } else {
            this.initialAnimationExtraHeight = this.extraHeight;
            animators = new ArrayList();
            animators.add(ObjectAnimatorProxy.ofFloat(this, "animationProgress", 1.0f, 0.0f));
            if (this.writeButton != null) {
                animators.add(ObjectAnimatorProxy.ofFloat(this.writeButton, "scaleX", 0.2f));
                animators.add(ObjectAnimatorProxy.ofFloat(this.writeButton, "scaleY", 0.2f));
                animators.add(ObjectAnimatorProxy.ofFloat(this.writeButton, "alpha", 0.0f));
            }
            a = 0;
            while (a < 2) {
                obj = this.onlineTextView[a];
                str = "alpha";
                fArr = new float[1];
                fArr[0] = a == 0 ? 1.0f : 0.0f;
                animators.add(ObjectAnimatorProxy.ofFloat(obj, str, fArr));
                obj = this.nameTextView[a];
                str = "alpha";
                fArr = new float[1];
                fArr[0] = a == 0 ? 1.0f : 0.0f;
                animators.add(ObjectAnimatorProxy.ofFloat(obj, str, fArr));
                a++;
            }
            if (this.animatingItem != null) {
                ViewProxy.setAlpha(this.animatingItem, 0.0f);
                animators.add(ObjectAnimatorProxy.ofFloat(this.animatingItem, "alpha", 1.0f));
            }
            animatorSet.playTogether(animators);
        }
        final Runnable runnable = callback;
        animatorSet.addListener(new AnimatorListenerAdapterProxy() {
            public void onAnimationEnd(Object animation) {
                if (VERSION.SDK_INT > 15) {
                    ProfileActivity.this.listView.setLayerType(0, null);
                }
                if (ProfileActivity.this.animatingItem != null) {
                    ProfileActivity.this.actionBar.createMenu().clearItems();
                    ProfileActivity.this.animatingItem = null;
                }
                runnable.run();
            }

            public void onAnimationCancel(Object animation) {
                if (VERSION.SDK_INT > 15) {
                    ProfileActivity.this.listView.setLayerType(0, null);
                }
            }
        });
        animatorSet.setInterpolator(new DecelerateInterpolator());
        AndroidUtilities.runOnUIThread(new Runnable() {
            public void run() {
                animatorSet.start();
            }
        }, 50);
        return animatorSet;
    }

    public void updatePhotoAtIndex(int index) {
    }

    public PlaceProviderObject getPlaceForPhoto(MessageObject messageObject, FileLocation fileLocation, int index) {
        if (fileLocation == null) {
            return null;
        }
        FileLocation photoBig = null;
        if (this.user_id != 0) {
            User user = MessagesController.getInstance().getUser(Integer.valueOf(this.user_id));
            if (!(user == null || user.photo == null || user.photo.photo_big == null)) {
                photoBig = user.photo.photo_big;
            }
        } else if (this.chat_id != 0) {
            Chat chat = MessagesController.getInstance().getChat(Integer.valueOf(this.chat_id));
            if (!(chat == null || chat.photo == null || chat.photo.photo_big == null)) {
                photoBig = chat.photo.photo_big;
            }
        }
        if (photoBig == null || photoBig.local_id != fileLocation.local_id || photoBig.volume_id != fileLocation.volume_id || photoBig.dc_id != fileLocation.dc_id) {
            return null;
        }
        int[] coords = new int[2];
        this.avatarImage.getLocationInWindow(coords);
        PlaceProviderObject object = new PlaceProviderObject();
        object.viewX = coords[0];
        object.viewY = coords[1] - AndroidUtilities.statusBarHeight;
        object.parentView = this.avatarImage;
        object.imageReceiver = this.avatarImage.getImageReceiver();
        object.user_id = this.user_id;
        object.thumb = object.imageReceiver.getBitmap();
        object.size = -1;
        object.radius = this.avatarImage.getImageReceiver().getRoundRadius();
        object.scale = ViewProxy.getScaleX(this.avatarImage);
        return object;
    }

    public Bitmap getThumbForPhoto(MessageObject messageObject, FileLocation fileLocation, int index) {
        return null;
    }

    public void willSwitchFromPhoto(MessageObject messageObject, FileLocation fileLocation, int index) {
    }

    public void willHidePhotoViewer() {
        this.avatarImage.getImageReceiver().setVisible(true, true);
    }

    public boolean isPhotoChecked(int index) {
        return false;
    }

    public void setPhotoChecked(int index) {
    }

    public boolean cancelButtonPressed() {
        return true;
    }

    public void sendButtonPressed(int index) {
    }

    public int getSelectedCount() {
        return 0;
    }

    private void updateOnlineCount() {
        this.onlineCount = 0;
        if (this.info instanceof TL_chatFull) {
            int currentTime = ConnectionsManager.getInstance().getCurrentTime();
            this.sortedUsers.clear();
            int i = 0;
            Iterator i$ = this.info.participants.participants.iterator();
            while (i$.hasNext()) {
                User user = MessagesController.getInstance().getUser(Integer.valueOf(((ChatParticipant) i$.next()).user_id));
                if (!(user == null || user.status == null || ((user.status.expires <= currentTime && user.id != UserConfig.getClientUserId()) || user.status.expires <= 10000))) {
                    this.onlineCount++;
                }
                this.sortedUsers.add(Integer.valueOf(i));
                i++;
            }
            try {
                Collections.sort(this.sortedUsers, new Comparator<Integer>() {
                    public int compare(Integer lhs, Integer rhs) {
                        User user1 = MessagesController.getInstance().getUser(Integer.valueOf(((ChatParticipant) ProfileActivity.this.info.participants.participants.get(rhs.intValue())).user_id));
                        User user2 = MessagesController.getInstance().getUser(Integer.valueOf(((ChatParticipant) ProfileActivity.this.info.participants.participants.get(lhs.intValue())).user_id));
                        int status1 = 0;
                        int status2 = 0;
                        if (!(user1 == null || user1.status == null)) {
                            status1 = user1.id == UserConfig.getClientUserId() ? ConnectionsManager.getInstance().getCurrentTime() + 50000 : user1.status.expires;
                        }
                        if (!(user2 == null || user2.status == null)) {
                            status2 = user2.id == UserConfig.getClientUserId() ? ConnectionsManager.getInstance().getCurrentTime() + 50000 : user2.status.expires;
                        }
                        if (status1 <= 0 || status2 <= 0) {
                            if (status1 >= 0 || status2 >= 0) {
                                if ((status1 < 0 && status2 > 0) || (status1 == 0 && status2 != 0)) {
                                    return -1;
                                }
                                if ((status2 >= 0 || status1 <= 0) && (status2 != 0 || status1 == 0)) {
                                    return 0;
                                }
                                return 1;
                            } else if (status1 > status2) {
                                return 1;
                            } else {
                                if (status1 < status2) {
                                    return -1;
                                }
                                return 0;
                            }
                        } else if (status1 > status2) {
                            return 1;
                        } else {
                            if (status1 < status2) {
                                return -1;
                            }
                            return 0;
                        }
                    }
                });
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
            if (this.listAdapter != null) {
                this.listAdapter.notifyItemRangeChanged(this.emptyRowChat2 + 1, this.sortedUsers.size());
            }
        }
    }

    public void setChatInfo(ChatFull chatInfo) {
        this.info = chatInfo;
        if (!(this.info == null || this.info.migrated_from_chat_id == 0)) {
            this.mergeDialogId = (long) (-this.info.migrated_from_chat_id);
        }
        fetchUsersFromChannelInfo();
    }

    private void fetchUsersFromChannelInfo() {
        if (this.info != null && (this.info instanceof TL_channelFull) && this.info.participants != null && this.participants != null && this.participants.isEmpty()) {
            for (int a = 0; a < this.info.participants.participants.size(); a++) {
                ChatParticipant chatParticipant = (ChatParticipant) this.info.participants.participants.get(a);
                if (chatParticipant instanceof TL_chatChannelParticipant) {
                    ChannelParticipant channelParticipant = ((TL_chatChannelParticipant) chatParticipant).channelParticipant;
                    this.participants.add(channelParticipant);
                    this.participantsMap.put(Integer.valueOf(channelParticipant.user_id), channelParticipant);
                }
            }
        }
    }

    private void kickUser(int uid) {
        if (uid != 0) {
            MessagesController.getInstance().deleteUserFromChat(this.chat_id, MessagesController.getInstance().getUser(Integer.valueOf(uid)), this.info);
            if (this.currentChat.megagroup && this.participants != null) {
                int a;
                boolean changed = false;
                for (a = 0; a < this.participants.size(); a++) {
                    if (((ChannelParticipant) this.participants.get(a)).user_id == uid) {
                        if (this.info != null) {
                            ChatFull chatFull = this.info;
                            chatFull.participants_count--;
                        }
                        this.participants.remove(a);
                        changed = true;
                        if (this.info != null && this.info.participants != null) {
                            for (a = 0; a < this.info.participants.participants.size(); a++) {
                                if (((ChatParticipant) this.info.participants.participants.get(a)).user_id == uid) {
                                    this.info.participants.participants.remove(a);
                                    changed = true;
                                    break;
                                }
                            }
                        }
                        if (changed) {
                            updateRowsIds();
                            this.listAdapter.notifyDataSetChanged();
                            return;
                        }
                        return;
                    }
                }
                for (a = 0; a < this.info.participants.participants.size(); a++) {
                    if (((ChatParticipant) this.info.participants.participants.get(a)).user_id == uid) {
                        this.info.participants.participants.remove(a);
                        changed = true;
                        break;
                    }
                }
                if (changed) {
                    updateRowsIds();
                    this.listAdapter.notifyDataSetChanged();
                    return;
                }
                return;
            }
            return;
        }
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.closeChats);
        if (AndroidUtilities.isTablet()) {
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats, Long.valueOf(-((long) this.chat_id)));
        } else {
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats, new Object[0]);
        }
        MessagesController.getInstance().deleteUserFromChat(this.chat_id, MessagesController.getInstance().getUser(Integer.valueOf(UserConfig.getClientUserId())), this.info);
        this.playProfileAnimation = false;
        finishFragment();
    }

    public boolean isChat() {
        return this.chat_id != 0;
    }

    private void updateRowsIds() {
        this.rowCount = 0;
        int i = this.rowCount;
        this.rowCount = i + 1;
        this.overscrollRow = i;
        if (this.user_id != 0) {
            this.phoneRow = -1;
            this.usernameRow = -1;
            this.settingsTimerRow = -1;
            this.settingsKeyRow = -1;
            this.startSecretChatRow = -1;
            this.blockedUsersRow = -1;
            User user = MessagesController.getInstance().getUser(Integer.valueOf(this.user_id));
            i = this.rowCount;
            this.rowCount = i + 1;
            this.emptyRow = i;
            if (user == null || !user.bot) {
                i = this.rowCount;
                this.rowCount = i + 1;
                this.phoneRow = i;
            }
            if (!(user == null || user.username == null || user.username.length() <= 0)) {
                i = this.rowCount;
                this.rowCount = i + 1;
                this.usernameRow = i;
            }
            if (!(this.botInfo == null || this.botInfo.share_text == null || this.botInfo.share_text.length() <= 0)) {
                i = this.rowCount;
                this.rowCount = i + 1;
                this.botSectionRow = i;
                i = this.rowCount;
                this.rowCount = i + 1;
                this.botInfoRow = i;
            }
            i = this.rowCount;
            this.rowCount = i + 1;
            this.sectionRow = i;
            i = this.rowCount;
            this.rowCount = i + 1;
            this.settingsNotificationsRow = i;
            i = this.rowCount;
            this.rowCount = i + 1;
            this.sharedMediaRow = i;
            if (this.currentEncryptedChat instanceof TL_encryptedChat) {
                i = this.rowCount;
                this.rowCount = i + 1;
                this.settingsTimerRow = i;
                i = this.rowCount;
                this.rowCount = i + 1;
                this.settingsKeyRow = i;
            }
            if (user != null && !user.bot && this.currentEncryptedChat == null) {
                i = this.rowCount;
                this.rowCount = i + 1;
                this.startSecretChatRow = i;
            }
        } else if (this.chat_id != 0) {
            this.membersEndRow = -1;
            this.membersSectionRow = -1;
            this.emptyRowChat2 = -1;
            this.addMemberRow = -1;
            this.channelInfoRow = -1;
            this.channelNameRow = -1;
            this.convertRow = -1;
            this.convertHelpRow = -1;
            this.emptyRowChat = -1;
            this.membersSectionRow = -1;
            this.membersRow = -1;
            this.managementRow = -1;
            this.leaveChannelRow = -1;
            this.loadMoreMembersRow = -1;
            this.blockedUsersRow = -1;
            if (this.chat_id > 0) {
                i = this.rowCount;
                this.rowCount = i + 1;
                this.emptyRow = i;
                if (ChatObject.isChannel(this.currentChat) && (!(this.info == null || this.info.about == null || this.info.about.length() <= 0) || (this.currentChat.username != null && this.currentChat.username.length() > 0))) {
                    if (!(this.info == null || this.info.about == null || this.info.about.length() <= 0)) {
                        i = this.rowCount;
                        this.rowCount = i + 1;
                        this.channelInfoRow = i;
                    }
                    if (this.currentChat.username != null && this.currentChat.username.length() > 0) {
                        i = this.rowCount;
                        this.rowCount = i + 1;
                        this.channelNameRow = i;
                    }
                    i = this.rowCount;
                    this.rowCount = i + 1;
                    this.sectionRow = i;
                }
                i = this.rowCount;
                this.rowCount = i + 1;
                this.settingsNotificationsRow = i;
                i = this.rowCount;
                this.rowCount = i + 1;
                this.sharedMediaRow = i;
                if (ChatObject.isChannel(this.currentChat)) {
                    if (!(this.currentChat.megagroup || this.info == null || (!this.currentChat.creator && !this.info.can_view_participants))) {
                        i = this.rowCount;
                        this.rowCount = i + 1;
                        this.membersRow = i;
                    }
                    if (!ChatObject.isNotInChat(this.currentChat) && (this.currentChat.creator || this.currentChat.editor || this.currentChat.moderator)) {
                        i = this.rowCount;
                        this.rowCount = i + 1;
                        this.managementRow = i;
                    }
                    if (!ChatObject.isNotInChat(this.currentChat) && this.currentChat.megagroup && (this.currentChat.editor || this.currentChat.creator)) {
                        i = this.rowCount;
                        this.rowCount = i + 1;
                        this.blockedUsersRow = i;
                    }
                    if (!(this.currentChat.creator || this.currentChat.left || this.currentChat.kicked || this.currentChat.megagroup)) {
                        i = this.rowCount;
                        this.rowCount = i + 1;
                        this.leaveChannelRow = i;
                    }
                    if (this.currentChat.megagroup && ((this.currentChat.editor || this.currentChat.creator) && (this.info == null || this.info.participants_count < MessagesController.getInstance().maxMegagroupCount))) {
                        i = this.rowCount;
                        this.rowCount = i + 1;
                        this.addMemberRow = i;
                    }
                    if (this.participants != null && !this.participants.isEmpty()) {
                        i = this.rowCount;
                        this.rowCount = i + 1;
                        this.emptyRowChat = i;
                        i = this.rowCount;
                        this.rowCount = i + 1;
                        this.membersSectionRow = i;
                        i = this.rowCount;
                        this.rowCount = i + 1;
                        this.emptyRowChat2 = i;
                        this.rowCount += this.participants.size();
                        this.membersEndRow = this.rowCount;
                        if (!this.usersEndReached) {
                            i = this.rowCount;
                            this.rowCount = i + 1;
                            this.loadMoreMembersRow = i;
                            return;
                        }
                        return;
                    }
                    return;
                }
                if (this.info != null) {
                    if (!(this.info.participants instanceof TL_chatParticipantsForbidden) && this.info.participants.participants.size() < MessagesController.getInstance().maxGroupCount && (this.currentChat.admin || this.currentChat.creator || !this.currentChat.admins_enabled)) {
                        i = this.rowCount;
                        this.rowCount = i + 1;
                        this.addMemberRow = i;
                    }
                    if (this.currentChat.creator && this.info.participants.participants.size() >= MessagesController.getInstance().minGroupConvertSize) {
                        i = this.rowCount;
                        this.rowCount = i + 1;
                        this.convertRow = i;
                    }
                }
                i = this.rowCount;
                this.rowCount = i + 1;
                this.emptyRowChat = i;
                if (this.convertRow != -1) {
                    i = this.rowCount;
                    this.rowCount = i + 1;
                    this.convertHelpRow = i;
                } else {
                    i = this.rowCount;
                    this.rowCount = i + 1;
                    this.membersSectionRow = i;
                }
                if (this.info != null && !(this.info.participants instanceof TL_chatParticipantsForbidden)) {
                    i = this.rowCount;
                    this.rowCount = i + 1;
                    this.emptyRowChat2 = i;
                    this.rowCount += this.info.participants.participants.size();
                    this.membersEndRow = this.rowCount;
                }
            } else if (!ChatObject.isChannel(this.currentChat) && this.info != null && !(this.info.participants instanceof TL_chatParticipantsForbidden)) {
                i = this.rowCount;
                this.rowCount = i + 1;
                this.addMemberRow = i;
                i = this.rowCount;
                this.rowCount = i + 1;
                this.emptyRowChat2 = i;
                this.rowCount += this.info.participants.participants.size();
                this.membersEndRow = this.rowCount;
            }
        }
    }

    private void updateProfileData() {
        if (this.avatarImage != null && this.nameTextView != null) {
            TLObject photo;
            FileLocation photoBig;
            String newString;
            int a;
            if (this.user_id != 0) {
                String newString2;
                User user = MessagesController.getInstance().getUser(Integer.valueOf(this.user_id));
                photo = null;
                photoBig = null;
                if (user.photo != null) {
                    photo = user.photo.photo_small;
                    photoBig = user.photo.photo_big;
                }
                this.avatarDrawable.setInfo(user);
                this.avatarImage.setImage(photo, "50_50", this.avatarDrawable);
                newString = UserObject.getUserName(user);
                if (user.id == 333000 || user.id == 777000) {
                    newString2 = LocaleController.getString("ServiceNotifications", C0553R.string.ServiceNotifications);
                } else if (user.bot) {
                    newString2 = LocaleController.getString("Bot", C0553R.string.Bot);
                } else {
                    newString2 = LocaleController.formatUserStatus(user);
                }
                a = 0;
                while (a < 2) {
                    if (this.nameTextView[a] != null) {
                        if (a == 0) {
                            if (user.id / LocationStatusCodes.GEOFENCE_NOT_AVAILABLE == 777 || user.id / LocationStatusCodes.GEOFENCE_NOT_AVAILABLE == 333 || ContactsController.getInstance().contactsDict.get(user.id) != null || (ContactsController.getInstance().contactsDict.size() == 0 && ContactsController.getInstance().isLoadingContacts())) {
                                this.nameTextView[a].setText(UserObject.getUserName(user));
                            } else if (user.phone == null || user.phone.length() == 0) {
                                this.nameTextView[a].setText(UserObject.getUserName(user));
                            } else {
                                this.nameTextView[a].setText(PhoneFormat.getInstance().format("+" + user.phone));
                            }
                        } else if (!this.nameTextView[a].getText().equals(newString)) {
                            this.nameTextView[a].setText(newString);
                        }
                        if (!this.onlineTextView[a].getText().equals(newString2)) {
                            this.onlineTextView[a].setText(newString2);
                        }
                        int leftIcon = this.currentEncryptedChat != null ? C0553R.drawable.ic_lock_header : 0;
                        if (a == 0) {
                            this.nameTextView[a].setCompoundDrawablesWithIntrinsicBounds(leftIcon, 0, MessagesController.getInstance().isDialogMuted((this.dialog_id > 0 ? 1 : (this.dialog_id == 0 ? 0 : -1)) != 0 ? this.dialog_id : (long) this.user_id) ? C0553R.drawable.mute_fixed : 0, 0);
                        } else if (user.verified) {
                            if (this.nameTextView[a].getCompoundDrawables()[2] == null || (this.nameTextView[a].getCompoundDrawables()[0] == null && leftIcon != 0)) {
                                this.nameTextView[a].setCompoundDrawablesWithIntrinsicBounds(leftIcon, 0, C0553R.drawable.check_profile_fixed, 0);
                            }
                        } else if (this.nameTextView[a].getCompoundDrawables()[2] != null || (this.nameTextView[a].getCompoundDrawables()[0] == null && leftIcon != 0)) {
                            this.nameTextView[a].setCompoundDrawablesWithIntrinsicBounds(leftIcon, 0, 0, 0);
                        }
                    }
                    a++;
                }
                this.avatarImage.getImageReceiver().setVisible(!PhotoViewer.getInstance().isShowingImage(photoBig), false);
            } else if (this.chat_id != 0) {
                int[] result;
                Chat chat = MessagesController.getInstance().getChat(Integer.valueOf(this.chat_id));
                if (chat != null) {
                    this.currentChat = chat;
                } else {
                    chat = this.currentChat;
                }
                if (!ChatObject.isChannel(chat)) {
                    int count = chat.participants_count;
                    if (this.info != null) {
                        count = this.info.participants.participants.size();
                    }
                    if (count == 0 || this.onlineCount <= 1) {
                        newString = LocaleController.formatPluralString("Members", count);
                    } else {
                        newString = String.format("%s, %s", new Object[]{LocaleController.formatPluralString("Members", count), LocaleController.formatPluralString("Online", this.onlineCount)});
                    }
                } else if (this.info != null && (this.currentChat.megagroup || (this.info.participants_count != 0 && !this.currentChat.admin && !this.info.can_view_participants))) {
                    result = new int[1];
                    newString = LocaleController.formatPluralString("Members", result[0]).replace(String.format("%d", new Object[]{Integer.valueOf(result[0])}), LocaleController.formatShortNumber(this.info.participants_count, result));
                } else if ((chat.flags & 64) != 0) {
                    newString = LocaleController.getString("ChannelPublic", C0553R.string.ChannelPublic).toLowerCase();
                } else {
                    newString = LocaleController.getString("ChannelPrivate", C0553R.string.ChannelPrivate).toLowerCase();
                }
                a = 0;
                while (a < 2) {
                    if (this.nameTextView[a] != null) {
                        if (!(chat.title == null || this.nameTextView[a].getText().equals(chat.title))) {
                            this.nameTextView[a].setText(chat.title);
                        }
                        if (a == 0) {
                            this.nameTextView[a].setCompoundDrawablesWithIntrinsicBounds(0, 0, MessagesController.getInstance().isDialogMuted((long) (-this.chat_id)) ? C0553R.drawable.mute_fixed : 0, 0);
                        } else if (chat.verified) {
                            if (this.nameTextView[a].getCompoundDrawables()[2] == null) {
                                this.nameTextView[a].setCompoundDrawablesWithIntrinsicBounds(0, 0, C0553R.drawable.check_profile_fixed, 0);
                            }
                        } else if (this.nameTextView[a].getCompoundDrawables()[2] != null) {
                            this.nameTextView[a].setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                        }
                        if (a == 0 && ChatObject.isChannel(this.currentChat) && this.info != null && this.info.participants_count != 0 && (this.currentChat.megagroup || this.currentChat.broadcast)) {
                            result = new int[1];
                            this.onlineTextView[a].setText(LocaleController.formatPluralString("Members", result[0]).replace(String.format("%d", new Object[]{Integer.valueOf(result[0])}), LocaleController.formatShortNumber(this.info.participants_count, result)));
                        } else if (!this.onlineTextView[a].getText().equals(newString)) {
                            this.onlineTextView[a].setText(newString);
                        }
                    }
                    a++;
                }
                photo = null;
                photoBig = null;
                if (chat.photo != null) {
                    photo = chat.photo.photo_small;
                    photoBig = chat.photo.photo_big;
                }
                this.avatarDrawable.setInfo(chat);
                this.avatarImage.setImage(photo, "50_50", this.avatarDrawable);
                this.avatarImage.getImageReceiver().setVisible(!PhotoViewer.getInstance().isShowingImage(photoBig), false);
            }
        }
    }

    private void createActionBarMenu() {
        ActionBarMenu menu = this.actionBar.createMenu();
        menu.clearItems();
        this.animatingItem = null;
        ActionBarMenuItem item;
        if (this.user_id != 0) {
            if (ContactsController.getInstance().contactsDict.get(this.user_id) == null) {
                User user = MessagesController.getInstance().getUser(Integer.valueOf(this.user_id));
                if (user != null) {
                    item = menu.addItem(10, (int) C0553R.drawable.ic_ab_other);
                    if (user.bot) {
                        if (!user.bot_nochats) {
                            item.addSubItem(9, LocaleController.getString("BotInvite", C0553R.string.BotInvite), 0);
                        }
                        item.addSubItem(10, LocaleController.getString("BotShare", C0553R.string.BotShare), 0);
                    }
                    if (user.phone != null && user.phone.length() != 0) {
                        item.addSubItem(1, LocaleController.getString("AddContact", C0553R.string.AddContact), 0);
                        item.addSubItem(3, LocaleController.getString("ShareContact", C0553R.string.ShareContact), 0);
                        item.addSubItem(2, !this.userBlocked ? LocaleController.getString("BlockContact", C0553R.string.BlockContact) : LocaleController.getString("Unblock", C0553R.string.Unblock), 0);
                        return;
                    } else if (user.bot) {
                        item.addSubItem(2, !this.userBlocked ? LocaleController.getString("BotStop", C0553R.string.BotStop) : LocaleController.getString("BotRestart", C0553R.string.BotRestart), 0);
                        return;
                    } else {
                        item.addSubItem(2, !this.userBlocked ? LocaleController.getString("BlockContact", C0553R.string.BlockContact) : LocaleController.getString("Unblock", C0553R.string.Unblock), 0);
                        return;
                    }
                }
                return;
            }
            item = menu.addItem(10, (int) C0553R.drawable.ic_ab_other);
            item.addSubItem(3, LocaleController.getString("ShareContact", C0553R.string.ShareContact), 0);
            item.addSubItem(2, !this.userBlocked ? LocaleController.getString("BlockContact", C0553R.string.BlockContact) : LocaleController.getString("Unblock", C0553R.string.Unblock), 0);
            item.addSubItem(4, LocaleController.getString("EditContact", C0553R.string.EditContact), 0);
            item.addSubItem(5, LocaleController.getString("DeleteContact", C0553R.string.DeleteContact), 0);
        } else if (this.chat_id == 0) {
        } else {
            if (this.chat_id > 0) {
                Chat chat = MessagesController.getInstance().getChat(Integer.valueOf(this.chat_id));
                if (this.writeButton != null) {
                    boolean isChannel = ChatObject.isChannel(this.currentChat);
                    if ((!isChannel || this.currentChat.creator || (this.currentChat.megagroup && this.currentChat.editor)) && (isChannel || this.currentChat.admin || this.currentChat.creator || !this.currentChat.admins_enabled)) {
                        this.writeButton.setImageResource(C0553R.drawable.floating_camera);
                        this.writeButton.setPadding(0, 0, 0, 0);
                    } else {
                        this.writeButton.setImageResource(C0553R.drawable.floating_message);
                        this.writeButton.setPadding(0, AndroidUtilities.dp(3.0f), 0, 0);
                    }
                }
                if (ChatObject.isChannel(chat)) {
                    item = null;
                    if (chat.creator) {
                        item = menu.addItem(10, (int) C0553R.drawable.ic_ab_other);
                        item.addSubItem(12, LocaleController.getString("ChannelEdit", C0553R.string.ChannelEdit), 0);
                    } else if (chat.megagroup && chat.editor) {
                        item = menu.addItem(10, (int) C0553R.drawable.ic_ab_other);
                        item.addSubItem(8, LocaleController.getString("EditName", C0553R.string.EditName), 0);
                    }
                    if (!chat.creator && !chat.left && !chat.kicked && chat.megagroup) {
                        if (item == null) {
                            item = menu.addItem(10, (int) C0553R.drawable.ic_ab_other);
                        }
                        item.addSubItem(7, LocaleController.getString("LeaveMegaMenu", C0553R.string.LeaveMegaMenu), 0);
                        return;
                    }
                    return;
                }
                item = menu.addItem(10, (int) C0553R.drawable.ic_ab_other);
                if (chat.creator && this.chat_id > 0) {
                    item.addSubItem(11, LocaleController.getString("SetAdmins", C0553R.string.SetAdmins), 0);
                }
                if (!chat.admins_enabled || chat.creator || chat.admin) {
                    item.addSubItem(8, LocaleController.getString("EditName", C0553R.string.EditName), 0);
                }
                item.addSubItem(7, LocaleController.getString("DeleteAndExit", C0553R.string.DeleteAndExit), 0);
                return;
            }
            menu.addItem(10, (int) C0553R.drawable.ic_ab_other).addSubItem(8, LocaleController.getString("EditName", C0553R.string.EditName), 0);
        }
    }

    protected void onDialogDismiss(Dialog dialog) {
        if (this.listView != null) {
            this.listView.invalidateViews();
        }
    }

    public void didSelectDialog(DialogsActivity messageFragment, long dialog_id, boolean param) {
        if (dialog_id != 0) {
            Bundle args = new Bundle();
            args.putBoolean("scrollToTopOnResume", true);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.closeChats);
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats, new Object[0]);
            int lower_part = (int) dialog_id;
            if (lower_part == 0) {
                args.putInt("enc_id", (int) (dialog_id >> 32));
            } else if (lower_part > 0) {
                args.putInt("user_id", lower_part);
            } else if (lower_part < 0) {
                args.putInt("chat_id", -lower_part);
            }
            presentFragment(new ChatActivity(args), true);
            removeSelfFromStack();
            SendMessagesHelper.getInstance().sendMessage(MessagesController.getInstance().getUser(Integer.valueOf(this.user_id)), dialog_id, null, true);
        }
    }
}
