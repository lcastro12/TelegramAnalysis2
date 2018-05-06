package org.telegram.ui;

import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Html;
import android.text.Spannable;
import android.text.TextUtils.TruncateAt;
import android.text.method.LinkMovementMethod;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.TextView;
import com.google.android.gms.location.LocationStatusCodes;
import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.AnimationCompat.AnimatorListenerAdapterProxy;
import org.telegram.messenger.AnimationCompat.AnimatorSetProxy;
import org.telegram.messenger.AnimationCompat.ObjectAnimatorProxy;
import org.telegram.messenger.AnimationCompat.ViewProxy;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.C0553R;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.SerializedData;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.FileLocation;
import org.telegram.tgnet.TLRPC.InputFile;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_help_getSupport;
import org.telegram.tgnet.TLRPC.TL_help_support;
import org.telegram.tgnet.TLRPC.TL_inputGeoPointEmpty;
import org.telegram.tgnet.TLRPC.TL_inputPhotoCropAuto;
import org.telegram.tgnet.TLRPC.TL_photos_photo;
import org.telegram.tgnet.TLRPC.TL_photos_uploadProfilePhoto;
import org.telegram.tgnet.TLRPC.TL_userProfilePhoto;
import org.telegram.tgnet.TLRPC.TL_userProfilePhotoEmpty;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Adapters.BaseFragmentAdapter;
import org.telegram.ui.Cells.EmptyCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextDetailSettingsCell;
import org.telegram.ui.Cells.TextInfoCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.AvatarUpdater;
import org.telegram.ui.Components.AvatarUpdater.AvatarUpdaterDelegate;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.NumberPicker;
import org.telegram.ui.PhotoViewer.PhotoViewerProvider;
import org.telegram.ui.PhotoViewer.PlaceProviderObject;

public class SettingsActivity extends BaseFragment implements NotificationCenterDelegate, PhotoViewerProvider {
    private static final int edit_name = 1;
    private static final int logout = 2;
    private int askQuestionRow;
    private BackupImageView avatarImage;
    private AvatarUpdater avatarUpdater = new AvatarUpdater();
    private int backgroundRow;
    private int cacheRow;
    private int clearLogsRow;
    private int contactsReimportRow;
    private int contactsSectionRow;
    private int contactsSortRow;
    private int emptyRow;
    private int enableAnimationsRow;
    private int extraHeight;
    private View extraHeightView;
    private int languageRow;
    private ListAdapter listAdapter;
    private ListView listView;
    private int mediaDownloadSection;
    private int mediaDownloadSection2;
    private int messagesSectionRow;
    private int messagesSectionRow2;
    private int mobileDownloadRow;
    private TextView nameTextView;
    private int notificationRow;
    private int numberRow;
    private int numberSectionRow;
    private TextView onlineTextView;
    private int overscrollRow;
    private int privacyRow;
    private int roamingDownloadRow;
    private int rowCount;
    private int saveToGalleryRow;
    private int sendByEnterRow;
    private int sendLogsRow;
    private int settingsSectionRow;
    private int settingsSectionRow2;
    private View shadowView;
    private int stickersRow;
    private int supportSectionRow;
    private int supportSectionRow2;
    private int switchBackendButtonRow;
    private int telegramFaqRow;
    private int textSizeRow;
    private int usernameRow;
    private int versionRow;
    private int wifiDownloadRow;
    private ImageView writeButton;
    private AnimatorSetProxy writeButtonAnimation;

    class C12134 implements OnItemClickListener {

        class C12092 implements OnClickListener {
            C12092() {
            }

            public void onClick(DialogInterface dialogInterface, int i) {
                SettingsActivity.this.performAskAQuestion();
            }
        }

        class C12103 implements OnClickListener {
            C12103() {
            }

            public void onClick(DialogInterface dialogInterface, int i) {
                ConnectionsManager.getInstance().switchBackend();
            }
        }

        class C12114 implements OnClickListener {
            C12114() {
            }

            public void onClick(DialogInterface dialog, int which) {
                Editor editor = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0).edit();
                editor.putInt("sortContactsBy", which);
                editor.commit();
                if (SettingsActivity.this.listView != null) {
                    SettingsActivity.this.listView.invalidateViews();
                }
            }
        }

        C12134() {
        }

        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Builder builder;
            if (i == SettingsActivity.this.textSizeRow) {
                if (SettingsActivity.this.getParentActivity() != null) {
                    builder = new Builder(SettingsActivity.this.getParentActivity());
                    builder.setTitle(LocaleController.getString("TextSize", C0553R.string.TextSize));
                    final NumberPicker numberPicker = new NumberPicker(SettingsActivity.this.getParentActivity());
                    numberPicker.setMinValue(12);
                    numberPicker.setMaxValue(30);
                    numberPicker.setValue(MessagesController.getInstance().fontSize);
                    builder.setView(numberPicker);
                    builder.setNegativeButton(LocaleController.getString("Done", C0553R.string.Done), new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Editor editor = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0).edit();
                            editor.putInt("fons_size", numberPicker.getValue());
                            MessagesController.getInstance().fontSize = numberPicker.getValue();
                            editor.commit();
                            if (SettingsActivity.this.listView != null) {
                                SettingsActivity.this.listView.invalidateViews();
                            }
                        }
                    });
                    SettingsActivity.this.showDialog(builder.create());
                }
            } else if (i == SettingsActivity.this.enableAnimationsRow) {
                preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0);
                boolean animations = preferences.getBoolean("view_animations", true);
                editor = preferences.edit();
                editor.putBoolean("view_animations", !animations);
                editor.commit();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(!animations);
                }
            } else if (i == SettingsActivity.this.notificationRow) {
                SettingsActivity.this.presentFragment(new NotificationsSettingsActivity());
            } else if (i == SettingsActivity.this.backgroundRow) {
                SettingsActivity.this.presentFragment(new WallpapersActivity());
            } else if (i == SettingsActivity.this.askQuestionRow) {
                if (SettingsActivity.this.getParentActivity() != null) {
                    TextView message = new TextView(SettingsActivity.this.getParentActivity());
                    message.setText(Html.fromHtml(LocaleController.getString("AskAQuestionInfo", C0553R.string.AskAQuestionInfo)));
                    message.setTextSize(18.0f);
                    message.setPadding(AndroidUtilities.dp(8.0f), AndroidUtilities.dp(5.0f), AndroidUtilities.dp(8.0f), AndroidUtilities.dp(6.0f));
                    message.setMovementMethod(new LinkMovementMethodMy());
                    builder = new Builder(SettingsActivity.this.getParentActivity());
                    builder.setView(message);
                    builder.setPositiveButton(LocaleController.getString("AskButton", C0553R.string.AskButton), new C12092());
                    builder.setNegativeButton(LocaleController.getString("Cancel", C0553R.string.Cancel), null);
                    SettingsActivity.this.showDialog(builder.create());
                }
            } else if (i == SettingsActivity.this.sendLogsRow) {
                SettingsActivity.this.sendLogs();
            } else if (i == SettingsActivity.this.clearLogsRow) {
                FileLog.cleanupLogs();
            } else if (i == SettingsActivity.this.sendByEnterRow) {
                preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0);
                boolean send = preferences.getBoolean("send_by_enter", false);
                editor = preferences.edit();
                editor.putBoolean("send_by_enter", !send);
                editor.commit();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(!send);
                }
            } else if (i == SettingsActivity.this.saveToGalleryRow) {
                MediaController.getInstance().toggleSaveToGallery();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(MediaController.getInstance().canSaveToGallery());
                }
            } else if (i == SettingsActivity.this.privacyRow) {
                SettingsActivity.this.presentFragment(new PrivacySettingsActivity());
            } else if (i == SettingsActivity.this.languageRow) {
                SettingsActivity.this.presentFragment(new LanguageSelectActivity());
            } else if (i == SettingsActivity.this.switchBackendButtonRow) {
                if (SettingsActivity.this.getParentActivity() != null) {
                    builder = new Builder(SettingsActivity.this.getParentActivity());
                    builder.setMessage(LocaleController.getString("AreYouSure", C0553R.string.AreYouSure));
                    builder.setTitle(LocaleController.getString("AppName", C0553R.string.AppName));
                    builder.setPositiveButton(LocaleController.getString("OK", C0553R.string.OK), new C12103());
                    builder.setNegativeButton(LocaleController.getString("Cancel", C0553R.string.Cancel), null);
                    SettingsActivity.this.showDialog(builder.create());
                }
            } else if (i == SettingsActivity.this.telegramFaqRow) {
                try {
                    SettingsActivity.this.getParentActivity().startActivityForResult(new Intent("android.intent.action.VIEW", Uri.parse(LocaleController.getString("TelegramFaqUrl", C0553R.string.TelegramFaqUrl))), 500);
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            } else if (i == SettingsActivity.this.contactsReimportRow) {
            } else {
                if (i == SettingsActivity.this.contactsSortRow) {
                    if (SettingsActivity.this.getParentActivity() != null) {
                        builder = new Builder(SettingsActivity.this.getParentActivity());
                        builder.setTitle(LocaleController.getString("SortBy", C0553R.string.SortBy));
                        builder.setItems(new CharSequence[]{LocaleController.getString("Default", C0553R.string.Default), LocaleController.getString("SortFirstName", C0553R.string.SortFirstName), LocaleController.getString("SortLastName", C0553R.string.SortLastName)}, new C12114());
                        builder.setNegativeButton(LocaleController.getString("Cancel", C0553R.string.Cancel), null);
                        SettingsActivity.this.showDialog(builder.create());
                    }
                } else if (i == SettingsActivity.this.wifiDownloadRow || i == SettingsActivity.this.mobileDownloadRow || i == SettingsActivity.this.roamingDownloadRow) {
                    if (SettingsActivity.this.getParentActivity() != null) {
                        boolean z;
                        builder = new Builder(SettingsActivity.this.getParentActivity());
                        int mask = 0;
                        if (i == SettingsActivity.this.mobileDownloadRow) {
                            builder.setTitle(LocaleController.getString("WhenUsingMobileData", C0553R.string.WhenUsingMobileData));
                            mask = MediaController.getInstance().mobileDataDownloadMask;
                        } else if (i == SettingsActivity.this.wifiDownloadRow) {
                            builder.setTitle(LocaleController.getString("WhenConnectedOnWiFi", C0553R.string.WhenConnectedOnWiFi));
                            mask = MediaController.getInstance().wifiDownloadMask;
                        } else if (i == SettingsActivity.this.roamingDownloadRow) {
                            builder.setTitle(LocaleController.getString("WhenRoaming", C0553R.string.WhenRoaming));
                            mask = MediaController.getInstance().roamingDownloadMask;
                        }
                        CharSequence[] charSequenceArr = new CharSequence[]{LocaleController.getString("AttachPhoto", C0553R.string.AttachPhoto), LocaleController.getString("AttachAudio", C0553R.string.AttachAudio), LocaleController.getString("AttachVideo", C0553R.string.AttachVideo), LocaleController.getString("AttachDocument", C0553R.string.AttachDocument)};
                        boolean[] zArr = new boolean[4];
                        if ((mask & 1) != 0) {
                            z = true;
                        } else {
                            z = false;
                        }
                        zArr[0] = z;
                        if ((mask & 2) != 0) {
                            z = true;
                        } else {
                            z = false;
                        }
                        zArr[1] = z;
                        if ((mask & 4) != 0) {
                            z = true;
                        } else {
                            z = false;
                        }
                        zArr[2] = z;
                        if ((mask & 8) != 0) {
                            z = true;
                        } else {
                            z = false;
                        }
                        zArr[3] = z;
                        final int i2 = i;
                        builder.setMultiChoiceItems(charSequenceArr, zArr, new OnMultiChoiceClickListener() {
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                int mask = 0;
                                Editor editor = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0).edit();
                                if (i2 == SettingsActivity.this.mobileDownloadRow) {
                                    mask = MediaController.getInstance().mobileDataDownloadMask;
                                } else if (i2 == SettingsActivity.this.wifiDownloadRow) {
                                    mask = MediaController.getInstance().wifiDownloadMask;
                                } else if (i2 == SettingsActivity.this.roamingDownloadRow) {
                                    mask = MediaController.getInstance().roamingDownloadMask;
                                }
                                int maskDiff = 0;
                                if (which == 0) {
                                    maskDiff = 1;
                                } else if (which == 1) {
                                    maskDiff = 2;
                                } else if (which == 2) {
                                    maskDiff = 4;
                                } else if (which == 3) {
                                    maskDiff = 8;
                                }
                                if (isChecked) {
                                    mask |= maskDiff;
                                } else {
                                    mask &= maskDiff ^ -1;
                                }
                                if (i2 == SettingsActivity.this.mobileDownloadRow) {
                                    editor.putInt("mobileDataDownloadMask", mask);
                                    MediaController.getInstance().mobileDataDownloadMask = mask;
                                } else if (i2 == SettingsActivity.this.wifiDownloadRow) {
                                    editor.putInt("wifiDownloadMask", mask);
                                    MediaController.getInstance().wifiDownloadMask = mask;
                                } else if (i2 == SettingsActivity.this.roamingDownloadRow) {
                                    editor.putInt("roamingDownloadMask", mask);
                                    MediaController.getInstance().roamingDownloadMask = mask;
                                }
                                editor.commit();
                                if (SettingsActivity.this.listView != null) {
                                    SettingsActivity.this.listView.invalidateViews();
                                }
                            }
                        });
                        builder.setNegativeButton(LocaleController.getString("OK", C0553R.string.OK), null);
                        SettingsActivity.this.showDialog(builder.create());
                    }
                } else if (i == SettingsActivity.this.usernameRow) {
                    SettingsActivity.this.presentFragment(new ChangeUsernameActivity());
                } else if (i == SettingsActivity.this.numberRow) {
                    SettingsActivity.this.presentFragment(new ChangePhoneHelpActivity());
                } else if (i == SettingsActivity.this.stickersRow) {
                    SettingsActivity.this.presentFragment(new StickersActivity());
                } else if (i == SettingsActivity.this.cacheRow) {
                    SettingsActivity.this.presentFragment(new CacheControlActivity());
                }
            }
        }
    }

    class C12145 implements View.OnClickListener {
        C12145() {
        }

        public void onClick(View v) {
            User user = MessagesController.getInstance().getUser(Integer.valueOf(UserConfig.getClientUserId()));
            if (user.photo != null && user.photo.photo_big != null) {
                PhotoViewer.getInstance().setParentActivity(SettingsActivity.this.getParentActivity());
                PhotoViewer.getInstance().openPhoto(user.photo.photo_big, SettingsActivity.this);
            }
        }
    }

    class C12156 extends ViewOutlineProvider {
        C12156() {
        }

        @SuppressLint({"NewApi"})
        public void getOutline(View view, Outline outline) {
            outline.setOval(0, 0, AndroidUtilities.dp(56.0f), AndroidUtilities.dp(56.0f));
        }
    }

    class C12177 implements View.OnClickListener {

        class C12161 implements OnClickListener {
            C12161() {
            }

            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    SettingsActivity.this.avatarUpdater.openCamera();
                } else if (i == 1) {
                    SettingsActivity.this.avatarUpdater.openGallery();
                } else if (i == 2) {
                    MessagesController.getInstance().deleteUserPhoto(null);
                }
            }
        }

        C12177() {
        }

        public void onClick(View v) {
            if (SettingsActivity.this.getParentActivity() != null) {
                Builder builder = new Builder(SettingsActivity.this.getParentActivity());
                User user = MessagesController.getInstance().getUser(Integer.valueOf(UserConfig.getClientUserId()));
                if (user == null) {
                    user = UserConfig.getCurrentUser();
                }
                if (user != null) {
                    CharSequence[] items;
                    boolean fullMenu = false;
                    if (user.photo == null || user.photo.photo_big == null || (user.photo instanceof TL_userProfilePhotoEmpty)) {
                        items = new CharSequence[]{LocaleController.getString("FromCamera", C0553R.string.FromCamera), LocaleController.getString("FromGalley", C0553R.string.FromGalley)};
                    } else {
                        items = new CharSequence[]{LocaleController.getString("FromCamera", C0553R.string.FromCamera), LocaleController.getString("FromGalley", C0553R.string.FromGalley), LocaleController.getString("DeletePhoto", C0553R.string.DeletePhoto)};
                        fullMenu = true;
                    }
                    boolean full = fullMenu;
                    builder.setItems(items, new C12161());
                    SettingsActivity.this.showDialog(builder.create());
                }
            }
        }
    }

    class C12188 implements OnScrollListener {
        C12188() {
        }

        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            int i = 0;
            if (totalItemCount != 0) {
                int height = 0;
                View child = view.getChildAt(0);
                if (child != null) {
                    if (firstVisibleItem == 0) {
                        int dp = AndroidUtilities.dp(88.0f);
                        if (child.getTop() < 0) {
                            i = child.getTop();
                        }
                        height = dp + i;
                    }
                    if (SettingsActivity.this.extraHeight != height) {
                        SettingsActivity.this.extraHeight = height;
                        SettingsActivity.this.needLayout();
                    }
                }
            }
        }
    }

    private static class LinkMovementMethodMy extends LinkMovementMethod {
        private LinkMovementMethodMy() {
        }

        public boolean onTouchEvent(@NonNull TextView widget, @NonNull Spannable buffer, @NonNull MotionEvent event) {
            try {
                return super.onTouchEvent(widget, buffer, event);
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
                return false;
            }
        }
    }

    class C16711 implements AvatarUpdaterDelegate {

        class C16701 implements RequestDelegate {

            class C12051 implements Runnable {
                C12051() {
                }

                public void run() {
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateInterfaces, Integer.valueOf(MessagesController.UPDATE_MASK_ALL));
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.mainUserInfoChanged, new Object[0]);
                    UserConfig.saveConfig(true);
                }
            }

            C16701() {
            }

            public void run(TLObject response, TL_error error) {
                if (error == null) {
                    User user = MessagesController.getInstance().getUser(Integer.valueOf(UserConfig.getClientUserId()));
                    if (user == null) {
                        user = UserConfig.getCurrentUser();
                        if (user != null) {
                            MessagesController.getInstance().putUser(user, false);
                        } else {
                            return;
                        }
                    }
                    UserConfig.setCurrentUser(user);
                    TL_photos_photo photo = (TL_photos_photo) response;
                    ArrayList<PhotoSize> sizes = photo.photo.sizes;
                    PhotoSize smallSize = FileLoader.getClosestPhotoSizeWithSize(sizes, 100);
                    PhotoSize bigSize = FileLoader.getClosestPhotoSizeWithSize(sizes, LocationStatusCodes.GEOFENCE_NOT_AVAILABLE);
                    user.photo = new TL_userProfilePhoto();
                    user.photo.photo_id = photo.photo.id;
                    if (smallSize != null) {
                        user.photo.photo_small = smallSize.location;
                    }
                    if (bigSize != null) {
                        user.photo.photo_big = bigSize.location;
                    } else if (smallSize != null) {
                        user.photo.photo_small = smallSize.location;
                    }
                    MessagesStorage.getInstance().clearUserPhotos(user.id);
                    ArrayList<User> users = new ArrayList();
                    users.add(user);
                    MessagesStorage.getInstance().putUsersAndChats(users, null, false, true);
                    AndroidUtilities.runOnUIThread(new C12051());
                }
            }
        }

        C16711() {
        }

        public void didUploadedPhoto(InputFile file, PhotoSize small, PhotoSize big) {
            TL_photos_uploadProfilePhoto req = new TL_photos_uploadProfilePhoto();
            req.caption = "";
            req.crop = new TL_inputPhotoCropAuto();
            req.file = file;
            req.geo_point = new TL_inputGeoPointEmpty();
            ConnectionsManager.getInstance().sendRequest(req, new C16701());
        }
    }

    class C16722 extends ActionBarMenuOnItemClick {

        class C12061 implements OnClickListener {
            C12061() {
            }

            public void onClick(DialogInterface dialogInterface, int i) {
                MessagesController.getInstance().performLogout(true);
            }
        }

        C16722() {
        }

        public void onItemClick(int id) {
            if (id == -1) {
                SettingsActivity.this.finishFragment();
            } else if (id == 1) {
                SettingsActivity.this.presentFragment(new ChangeNameActivity());
            } else if (id == 2 && SettingsActivity.this.getParentActivity() != null) {
                Builder builder = new Builder(SettingsActivity.this.getParentActivity());
                builder.setMessage(LocaleController.getString("AreYouSureLogout", C0553R.string.AreYouSureLogout));
                builder.setTitle(LocaleController.getString("AppName", C0553R.string.AppName));
                builder.setPositiveButton(LocaleController.getString("OK", C0553R.string.OK), new C12061());
                builder.setNegativeButton(LocaleController.getString("Cancel", C0553R.string.Cancel), null);
                SettingsActivity.this.showDialog(builder.create());
            }
        }
    }

    private class ListAdapter extends BaseFragmentAdapter {
        private Context mContext;

        public ListAdapter(Context context) {
            this.mContext = context;
        }

        public boolean areAllItemsEnabled() {
            return false;
        }

        public boolean isEnabled(int i) {
            return i == SettingsActivity.this.textSizeRow || i == SettingsActivity.this.enableAnimationsRow || i == SettingsActivity.this.notificationRow || i == SettingsActivity.this.backgroundRow || i == SettingsActivity.this.numberRow || i == SettingsActivity.this.askQuestionRow || i == SettingsActivity.this.sendLogsRow || i == SettingsActivity.this.sendByEnterRow || i == SettingsActivity.this.privacyRow || i == SettingsActivity.this.wifiDownloadRow || i == SettingsActivity.this.mobileDownloadRow || i == SettingsActivity.this.clearLogsRow || i == SettingsActivity.this.roamingDownloadRow || i == SettingsActivity.this.languageRow || i == SettingsActivity.this.usernameRow || i == SettingsActivity.this.switchBackendButtonRow || i == SettingsActivity.this.telegramFaqRow || i == SettingsActivity.this.contactsSortRow || i == SettingsActivity.this.contactsReimportRow || i == SettingsActivity.this.saveToGalleryRow || i == SettingsActivity.this.stickersRow || i == SettingsActivity.this.cacheRow;
        }

        public int getCount() {
            return SettingsActivity.this.rowCount;
        }

        public Object getItem(int i) {
            return null;
        }

        public long getItemId(int i) {
            return (long) i;
        }

        public boolean hasStableIds() {
            return false;
        }

        public View getView(int i, View view, ViewGroup viewGroup) {
            View emptyCell;
            int type = getItemViewType(i);
            if (type == 0) {
                if (view == null) {
                    emptyCell = new EmptyCell(this.mContext);
                }
                if (i == SettingsActivity.this.overscrollRow) {
                    ((EmptyCell) view).setHeight(AndroidUtilities.dp(88.0f));
                    return view;
                }
                ((EmptyCell) view).setHeight(AndroidUtilities.dp(16.0f));
                return view;
            } else if (type == 1) {
                if (view == null) {
                    return new ShadowSectionCell(this.mContext);
                }
                return view;
            } else if (type == 2) {
                if (view == null) {
                    emptyCell = new TextSettingsCell(this.mContext);
                }
                TextSettingsCell textCell = (TextSettingsCell) view;
                if (i == SettingsActivity.this.textSizeRow) {
                    int size = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0).getInt("fons_size", AndroidUtilities.isTablet() ? 18 : 16);
                    textCell.setTextAndValue(LocaleController.getString("TextSize", C0553R.string.TextSize), String.format("%d", new Object[]{Integer.valueOf(size)}), true);
                    return view;
                } else if (i == SettingsActivity.this.languageRow) {
                    textCell.setTextAndValue(LocaleController.getString("Language", C0553R.string.Language), LocaleController.getCurrentLanguageName(), true);
                    return view;
                } else if (i == SettingsActivity.this.contactsSortRow) {
                    int sort = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0).getInt("sortContactsBy", 0);
                    if (sort == 0) {
                        value = LocaleController.getString("Default", C0553R.string.Default);
                    } else if (sort == 1) {
                        value = LocaleController.getString("FirstName", C0553R.string.SortFirstName);
                    } else {
                        value = LocaleController.getString("LastName", C0553R.string.SortLastName);
                    }
                    textCell.setTextAndValue(LocaleController.getString("SortBy", C0553R.string.SortBy), value, true);
                    return view;
                } else if (i == SettingsActivity.this.notificationRow) {
                    textCell.setText(LocaleController.getString("NotificationsAndSounds", C0553R.string.NotificationsAndSounds), true);
                    return view;
                } else if (i == SettingsActivity.this.backgroundRow) {
                    textCell.setText(LocaleController.getString("ChatBackground", C0553R.string.ChatBackground), true);
                    return view;
                } else if (i == SettingsActivity.this.sendLogsRow) {
                    textCell.setText("Send Logs", true);
                    return view;
                } else if (i == SettingsActivity.this.clearLogsRow) {
                    textCell.setText("Clear Logs", true);
                    return view;
                } else if (i == SettingsActivity.this.askQuestionRow) {
                    textCell.setText(LocaleController.getString("AskAQuestion", C0553R.string.AskAQuestion), true);
                    return view;
                } else if (i == SettingsActivity.this.privacyRow) {
                    textCell.setText(LocaleController.getString("PrivacySettings", C0553R.string.PrivacySettings), true);
                    return view;
                } else if (i == SettingsActivity.this.switchBackendButtonRow) {
                    textCell.setText("Switch Backend", true);
                    return view;
                } else if (i == SettingsActivity.this.telegramFaqRow) {
                    textCell.setText(LocaleController.getString("TelegramFAQ", C0553R.string.TelegramFaq), true);
                    return view;
                } else if (i == SettingsActivity.this.contactsReimportRow) {
                    textCell.setText(LocaleController.getString("ImportContacts", C0553R.string.ImportContacts), true);
                    return view;
                } else if (i == SettingsActivity.this.stickersRow) {
                    textCell.setText(LocaleController.getString("Stickers", C0553R.string.Stickers), true);
                    return view;
                } else if (i != SettingsActivity.this.cacheRow) {
                    return view;
                } else {
                    textCell.setText(LocaleController.getString("CacheSettings", C0553R.string.CacheSettings), true);
                    return view;
                }
            } else if (type == 3) {
                if (view == null) {
                    emptyCell = new TextCheckCell(this.mContext);
                }
                TextCheckCell textCell2 = (TextCheckCell) view;
                preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0);
                if (i == SettingsActivity.this.enableAnimationsRow) {
                    textCell2.setTextAndCheck(LocaleController.getString("EnableAnimations", C0553R.string.EnableAnimations), preferences.getBoolean("view_animations", true), false);
                    return view;
                } else if (i == SettingsActivity.this.sendByEnterRow) {
                    textCell2.setTextAndCheck(LocaleController.getString("SendByEnter", C0553R.string.SendByEnter), preferences.getBoolean("send_by_enter", false), false);
                    return view;
                } else if (i != SettingsActivity.this.saveToGalleryRow) {
                    return view;
                } else {
                    textCell2.setTextAndCheck(LocaleController.getString("SaveToGallerySettings", C0553R.string.SaveToGallerySettings), MediaController.getInstance().canSaveToGallery(), false);
                    return view;
                }
            } else if (type == 4) {
                if (view == null) {
                    emptyCell = new HeaderCell(this.mContext);
                }
                if (i == SettingsActivity.this.settingsSectionRow2) {
                    ((HeaderCell) view).setText(LocaleController.getString("SETTINGS", C0553R.string.SETTINGS));
                    return view;
                } else if (i == SettingsActivity.this.supportSectionRow2) {
                    ((HeaderCell) view).setText(LocaleController.getString("Support", C0553R.string.Support));
                    return view;
                } else if (i == SettingsActivity.this.messagesSectionRow2) {
                    ((HeaderCell) view).setText(LocaleController.getString("MessagesSettings", C0553R.string.MessagesSettings));
                    return view;
                } else if (i == SettingsActivity.this.mediaDownloadSection2) {
                    ((HeaderCell) view).setText(LocaleController.getString("AutomaticMediaDownload", C0553R.string.AutomaticMediaDownload));
                    return view;
                } else if (i != SettingsActivity.this.numberSectionRow) {
                    return view;
                } else {
                    ((HeaderCell) view).setText(LocaleController.getString("Info", C0553R.string.Info));
                    return view;
                }
            } else if (type == 5) {
                if (view != null) {
                    return view;
                }
                emptyCell = new TextInfoCell(this.mContext);
                try {
                    PackageInfo pInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
                    ((TextInfoCell) emptyCell).setText(String.format(Locale.US, "Telegram for Android v%s (%d)", new Object[]{pInfo.versionName, Integer.valueOf(pInfo.versionCode)}));
                    return emptyCell;
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                    return emptyCell;
                }
            } else if (type != 6) {
                return view;
            } else {
                if (view == null) {
                    emptyCell = new TextDetailSettingsCell(this.mContext);
                }
                TextDetailSettingsCell textCell3 = (TextDetailSettingsCell) view;
                if (i == SettingsActivity.this.mobileDownloadRow || i == SettingsActivity.this.wifiDownloadRow || i == SettingsActivity.this.roamingDownloadRow) {
                    int mask;
                    preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0);
                    if (i == SettingsActivity.this.mobileDownloadRow) {
                        value = LocaleController.getString("WhenUsingMobileData", C0553R.string.WhenUsingMobileData);
                        mask = MediaController.getInstance().mobileDataDownloadMask;
                    } else if (i == SettingsActivity.this.wifiDownloadRow) {
                        value = LocaleController.getString("WhenConnectedOnWiFi", C0553R.string.WhenConnectedOnWiFi);
                        mask = MediaController.getInstance().wifiDownloadMask;
                    } else {
                        value = LocaleController.getString("WhenRoaming", C0553R.string.WhenRoaming);
                        mask = MediaController.getInstance().roamingDownloadMask;
                    }
                    String text = "";
                    if ((mask & 1) != 0) {
                        text = text + LocaleController.getString("AttachPhoto", C0553R.string.AttachPhoto);
                    }
                    if ((mask & 2) != 0) {
                        if (text.length() != 0) {
                            text = text + ", ";
                        }
                        text = text + LocaleController.getString("AttachAudio", C0553R.string.AttachAudio);
                    }
                    if ((mask & 4) != 0) {
                        if (text.length() != 0) {
                            text = text + ", ";
                        }
                        text = text + LocaleController.getString("AttachVideo", C0553R.string.AttachVideo);
                    }
                    if ((mask & 8) != 0) {
                        if (text.length() != 0) {
                            text = text + ", ";
                        }
                        text = text + LocaleController.getString("AttachDocument", C0553R.string.AttachDocument);
                    }
                    if (text.length() == 0) {
                        text = LocaleController.getString("NoMediaAutoDownload", C0553R.string.NoMediaAutoDownload);
                    }
                    textCell3.setTextAndValue(value, text, true);
                    return view;
                } else if (i == SettingsActivity.this.numberRow) {
                    user = UserConfig.getCurrentUser();
                    if (user == null || user.phone == null || user.phone.length() == 0) {
                        value = LocaleController.getString("NumberUnknown", C0553R.string.NumberUnknown);
                    } else {
                        value = PhoneFormat.getInstance().format("+" + user.phone);
                    }
                    textCell3.setTextAndValue(value, LocaleController.getString("Phone", C0553R.string.Phone), true);
                    return view;
                } else if (i != SettingsActivity.this.usernameRow) {
                    return view;
                } else {
                    user = UserConfig.getCurrentUser();
                    if (user == null || user.username == null || user.username.length() == 0) {
                        value = LocaleController.getString("UsernameEmpty", C0553R.string.UsernameEmpty);
                    } else {
                        value = "@" + user.username;
                    }
                    textCell3.setTextAndValue(value, LocaleController.getString("Username", C0553R.string.Username), false);
                    return view;
                }
            }
        }

        public int getItemViewType(int i) {
            if (i == SettingsActivity.this.emptyRow || i == SettingsActivity.this.overscrollRow) {
                return 0;
            }
            if (i == SettingsActivity.this.settingsSectionRow || i == SettingsActivity.this.supportSectionRow || i == SettingsActivity.this.messagesSectionRow || i == SettingsActivity.this.mediaDownloadSection || i == SettingsActivity.this.contactsSectionRow) {
                return 1;
            }
            if (i == SettingsActivity.this.enableAnimationsRow || i == SettingsActivity.this.sendByEnterRow || i == SettingsActivity.this.saveToGalleryRow) {
                return 3;
            }
            if (i == SettingsActivity.this.notificationRow || i == SettingsActivity.this.backgroundRow || i == SettingsActivity.this.askQuestionRow || i == SettingsActivity.this.sendLogsRow || i == SettingsActivity.this.privacyRow || i == SettingsActivity.this.clearLogsRow || i == SettingsActivity.this.switchBackendButtonRow || i == SettingsActivity.this.telegramFaqRow || i == SettingsActivity.this.contactsReimportRow || i == SettingsActivity.this.textSizeRow || i == SettingsActivity.this.languageRow || i == SettingsActivity.this.contactsSortRow || i == SettingsActivity.this.stickersRow || i == SettingsActivity.this.cacheRow) {
                return 2;
            }
            if (i == SettingsActivity.this.versionRow) {
                return 5;
            }
            if (i == SettingsActivity.this.wifiDownloadRow || i == SettingsActivity.this.mobileDownloadRow || i == SettingsActivity.this.roamingDownloadRow || i == SettingsActivity.this.numberRow || i == SettingsActivity.this.usernameRow) {
                return 6;
            }
            if (i == SettingsActivity.this.settingsSectionRow2 || i == SettingsActivity.this.messagesSectionRow2 || i == SettingsActivity.this.supportSectionRow2 || i == SettingsActivity.this.numberSectionRow || i == SettingsActivity.this.mediaDownloadSection2) {
                return 4;
            }
            return 2;
        }

        public int getViewTypeCount() {
            return 7;
        }

        public boolean isEmpty() {
            return false;
        }
    }

    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        this.avatarUpdater.parentFragment = this;
        this.avatarUpdater.delegate = new C16711();
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.updateInterfaces);
        this.rowCount = 0;
        int i = this.rowCount;
        this.rowCount = i + 1;
        this.overscrollRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.emptyRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.numberSectionRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.numberRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.usernameRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.settingsSectionRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.settingsSectionRow2 = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.notificationRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.privacyRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.backgroundRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.languageRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.enableAnimationsRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.mediaDownloadSection = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.mediaDownloadSection2 = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.mobileDownloadRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.wifiDownloadRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.roamingDownloadRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.saveToGalleryRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.messagesSectionRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.messagesSectionRow2 = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.textSizeRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.stickersRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.cacheRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.sendByEnterRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.supportSectionRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.supportSectionRow2 = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.askQuestionRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.telegramFaqRow = i;
        if (BuildVars.DEBUG_VERSION) {
            i = this.rowCount;
            this.rowCount = i + 1;
            this.sendLogsRow = i;
            i = this.rowCount;
            this.rowCount = i + 1;
            this.clearLogsRow = i;
            i = this.rowCount;
            this.rowCount = i + 1;
            this.switchBackendButtonRow = i;
        }
        i = this.rowCount;
        this.rowCount = i + 1;
        this.versionRow = i;
        MessagesController.getInstance().loadFullUser(UserConfig.getCurrentUser(), this.classGuid);
        return true;
    }

    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        if (this.avatarImage != null) {
            this.avatarImage.setImageDrawable(null);
        }
        MessagesController.getInstance().cancelLoadFullUser(UserConfig.getClientUserId());
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.updateInterfaces);
        this.avatarUpdater.clear();
    }

    public View createView(Context context) {
        this.actionBar.setBackgroundColor(AvatarDrawable.getProfileBackColorForId(5));
        this.actionBar.setItemsBackground(AvatarDrawable.getButtonColorForId(5));
        this.actionBar.setBackButtonImage(C0553R.drawable.ic_ab_back);
        this.actionBar.setAddToContainer(false);
        this.extraHeight = 88;
        if (AndroidUtilities.isTablet()) {
            this.actionBar.setOccupyStatusBar(false);
        }
        this.actionBar.setActionBarMenuOnItemClick(new C16722());
        ActionBarMenuItem item = this.actionBar.createMenu().addItem(0, (int) C0553R.drawable.ic_ab_other);
        item.addSubItem(1, LocaleController.getString("EditName", C0553R.string.EditName), 0);
        item.addSubItem(2, LocaleController.getString("LogOut", C0553R.string.LogOut), 0);
        this.listAdapter = new ListAdapter(context);
        this.fragmentView = new FrameLayout(context) {
            protected boolean drawChild(@NonNull Canvas canvas, @NonNull View child, long drawingTime) {
                if (child != SettingsActivity.this.listView) {
                    return super.drawChild(canvas, child, drawingTime);
                }
                boolean result = super.drawChild(canvas, child, drawingTime);
                if (SettingsActivity.this.parentLayout == null) {
                    return result;
                }
                int actionBarHeight = 0;
                int childCount = getChildCount();
                for (int a = 0; a < childCount; a++) {
                    View view = getChildAt(a);
                    if (view != child && (view instanceof ActionBar) && view.getVisibility() == 0) {
                        if (((ActionBar) view).getCastShadows()) {
                            actionBarHeight = view.getMeasuredHeight();
                        }
                        SettingsActivity.this.parentLayout.drawHeaderShadow(canvas, actionBarHeight);
                        return result;
                    }
                }
                SettingsActivity.this.parentLayout.drawHeaderShadow(canvas, actionBarHeight);
                return result;
            }
        };
        FrameLayout frameLayout = this.fragmentView;
        this.listView = new ListView(context);
        this.listView.setDivider(null);
        this.listView.setDividerHeight(0);
        this.listView.setVerticalScrollBarEnabled(false);
        AndroidUtilities.setListViewEdgeEffectColor(this.listView, AvatarDrawable.getProfileBackColorForId(5));
        frameLayout.addView(this.listView, LayoutHelper.createFrame(-1, -1, 51));
        this.listView.setAdapter(this.listAdapter);
        this.listView.setOnItemClickListener(new C12134());
        frameLayout.addView(this.actionBar);
        this.extraHeightView = new View(context);
        ViewProxy.setPivotY(this.extraHeightView, 0.0f);
        this.extraHeightView.setBackgroundColor(AvatarDrawable.getProfileBackColorForId(5));
        frameLayout.addView(this.extraHeightView, LayoutHelper.createFrame(-1, 88.0f));
        this.shadowView = new View(context);
        this.shadowView.setBackgroundResource(C0553R.drawable.header_shadow);
        frameLayout.addView(this.shadowView, LayoutHelper.createFrame(-1, 3.0f));
        this.avatarImage = new BackupImageView(context);
        this.avatarImage.setRoundRadius(AndroidUtilities.dp(21.0f));
        ViewProxy.setPivotX(this.avatarImage, 0.0f);
        ViewProxy.setPivotY(this.avatarImage, 0.0f);
        frameLayout.addView(this.avatarImage, LayoutHelper.createFrame(42, 42.0f, 51, 64.0f, 0.0f, 0.0f, 0.0f));
        this.avatarImage.setOnClickListener(new C12145());
        this.nameTextView = new TextView(context);
        this.nameTextView.setTextColor(-1);
        this.nameTextView.setTextSize(1, 18.0f);
        this.nameTextView.setLines(1);
        this.nameTextView.setMaxLines(1);
        this.nameTextView.setSingleLine(true);
        this.nameTextView.setEllipsize(TruncateAt.END);
        this.nameTextView.setGravity(3);
        this.nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        ViewProxy.setPivotX(this.nameTextView, 0.0f);
        ViewProxy.setPivotY(this.nameTextView, 0.0f);
        frameLayout.addView(this.nameTextView, LayoutHelper.createFrame(-2, -2.0f, 51, 118.0f, 0.0f, 48.0f, 0.0f));
        this.onlineTextView = new TextView(context);
        this.onlineTextView.setTextColor(AvatarDrawable.getProfileTextColorForId(5));
        this.onlineTextView.setTextSize(1, 14.0f);
        this.onlineTextView.setLines(1);
        this.onlineTextView.setMaxLines(1);
        this.onlineTextView.setSingleLine(true);
        this.onlineTextView.setEllipsize(TruncateAt.END);
        this.onlineTextView.setGravity(3);
        frameLayout.addView(this.onlineTextView, LayoutHelper.createFrame(-2, -2.0f, 51, 118.0f, 0.0f, 48.0f, 0.0f));
        this.writeButton = new ImageView(context);
        this.writeButton.setBackgroundResource(C0553R.drawable.floating_user_states);
        this.writeButton.setImageResource(C0553R.drawable.floating_camera);
        this.writeButton.setScaleType(ScaleType.CENTER);
        if (VERSION.SDK_INT >= 21) {
            StateListAnimator animator = new StateListAnimator();
            animator.addState(new int[]{16842919}, ObjectAnimator.ofFloat(this.writeButton, "translationZ", new float[]{(float) AndroidUtilities.dp(2.0f), (float) AndroidUtilities.dp(4.0f)}).setDuration(200));
            animator.addState(new int[0], ObjectAnimator.ofFloat(this.writeButton, "translationZ", new float[]{(float) AndroidUtilities.dp(4.0f), (float) AndroidUtilities.dp(2.0f)}).setDuration(200));
            this.writeButton.setStateListAnimator(animator);
            this.writeButton.setOutlineProvider(new C12156());
        }
        frameLayout.addView(this.writeButton, LayoutHelper.createFrame(-2, -2.0f, 53, 0.0f, 0.0f, 16.0f, 0.0f));
        this.writeButton.setOnClickListener(new C12177());
        needLayout();
        this.listView.setOnScrollListener(new C12188());
        return this.fragmentView;
    }

    protected void onDialogDismiss(Dialog dialog) {
        MediaController.getInstance().checkAutodownloadSettings();
    }

    public void updatePhotoAtIndex(int index) {
    }

    public PlaceProviderObject getPlaceForPhoto(MessageObject messageObject, FileLocation fileLocation, int index) {
        if (fileLocation == null) {
            return null;
        }
        User user = MessagesController.getInstance().getUser(Integer.valueOf(UserConfig.getClientUserId()));
        if (user == null || user.photo == null || user.photo.photo_big == null) {
            return null;
        }
        FileLocation photoBig = user.photo.photo_big;
        if (photoBig.local_id != fileLocation.local_id || photoBig.volume_id != fileLocation.volume_id || photoBig.dc_id != fileLocation.dc_id) {
            return null;
        }
        int[] coords = new int[2];
        this.avatarImage.getLocationInWindow(coords);
        PlaceProviderObject object = new PlaceProviderObject();
        object.viewX = coords[0];
        object.viewY = coords[1] - AndroidUtilities.statusBarHeight;
        object.parentView = this.avatarImage;
        object.imageReceiver = this.avatarImage.getImageReceiver();
        object.user_id = UserConfig.getClientUserId();
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

    public void performAskAQuestion() {
        final SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0);
        int uid = preferences.getInt("support_id", 0);
        User supportUser = null;
        if (uid != 0) {
            supportUser = MessagesController.getInstance().getUser(Integer.valueOf(uid));
            if (supportUser == null) {
                String userString = preferences.getString("support_user", null);
                if (userString != null) {
                    try {
                        byte[] datacentersBytes = Base64.decode(userString, 0);
                        if (datacentersBytes != null) {
                            SerializedData data = new SerializedData(datacentersBytes);
                            supportUser = User.TLdeserialize(data, data.readInt32(false), false);
                            if (supportUser != null && supportUser.id == 333000) {
                                supportUser = null;
                            }
                            data.cleanup();
                        }
                    } catch (Throwable e) {
                        FileLog.m611e("tmessages", e);
                        supportUser = null;
                    }
                }
            }
        }
        if (supportUser == null) {
            final ProgressDialog progressDialog = new ProgressDialog(getParentActivity());
            progressDialog.setMessage(LocaleController.getString("Loading", C0553R.string.Loading));
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setCancelable(false);
            progressDialog.show();
            ConnectionsManager.getInstance().sendRequest(new TL_help_getSupport(), new RequestDelegate() {

                class C12202 implements Runnable {
                    C12202() {
                    }

                    public void run() {
                        try {
                            progressDialog.dismiss();
                        } catch (Throwable e) {
                            FileLog.m611e("tmessages", e);
                        }
                    }
                }

                public void run(TLObject response, TL_error error) {
                    if (error == null) {
                        final TL_help_support res = (TL_help_support) response;
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            public void run() {
                                Editor editor = preferences.edit();
                                editor.putInt("support_id", res.user.id);
                                SerializedData data = new SerializedData();
                                res.user.serializeToStream(data);
                                editor.putString("support_user", Base64.encodeToString(data.toByteArray(), 0));
                                editor.commit();
                                data.cleanup();
                                try {
                                    progressDialog.dismiss();
                                } catch (Throwable e) {
                                    FileLog.m611e("tmessages", e);
                                }
                                ArrayList<User> users = new ArrayList();
                                users.add(res.user);
                                MessagesStorage.getInstance().putUsersAndChats(users, null, true, true);
                                MessagesController.getInstance().putUser(res.user, false);
                                Bundle args = new Bundle();
                                args.putInt("user_id", res.user.id);
                                SettingsActivity.this.presentFragment(new ChatActivity(args));
                            }
                        });
                        return;
                    }
                    AndroidUtilities.runOnUIThread(new C12202());
                }
            });
            return;
        }
        MessagesController.getInstance().putUser(supportUser, true);
        Bundle args = new Bundle();
        args.putInt("user_id", supportUser.id);
        presentFragment(new ChatActivity(args));
    }

    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        this.avatarUpdater.onActivityResult(requestCode, resultCode, data);
    }

    public void saveSelfArgs(Bundle args) {
        if (this.avatarUpdater != null && this.avatarUpdater.currentPicturePath != null) {
            args.putString("path", this.avatarUpdater.currentPicturePath);
        }
    }

    public void restoreSelfArgs(Bundle args) {
        if (this.avatarUpdater != null) {
            this.avatarUpdater.currentPicturePath = args.getString("path");
        }
    }

    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.updateInterfaces) {
            int mask = ((Integer) args[0]).intValue();
            if ((mask & 2) != 0 || (mask & 1) != 0) {
                updateUserData();
            }
        }
    }

    public void onResume() {
        super.onResume();
        if (this.listAdapter != null) {
            this.listAdapter.notifyDataSetChanged();
        }
        updateUserData();
        fixLayout();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        fixLayout();
    }

    private void needLayout() {
        LayoutParams layoutParams;
        int newTop = (this.actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight();
        if (this.listView != null) {
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
            if (VERSION.SDK_INT < 11) {
                layoutParams = (LayoutParams) this.writeButton.getLayoutParams();
                layoutParams.topMargin = (((this.actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight()) + this.extraHeight) - AndroidUtilities.dp(29.5f);
                this.writeButton.setLayoutParams(layoutParams);
            } else {
                ViewProxy.setTranslationY(this.writeButton, (float) ((((this.actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight()) + this.extraHeight) - AndroidUtilities.dp(29.5f)));
            }
            final boolean setVisible = diff > 0.2f;
            if (setVisible != (this.writeButton.getTag() == null)) {
                if (setVisible) {
                    this.writeButton.setTag(null);
                    this.writeButton.setVisibility(0);
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
                        if (SettingsActivity.this.writeButtonAnimation != null && SettingsActivity.this.writeButtonAnimation.equals(animation)) {
                            SettingsActivity.this.writeButton.clearAnimation();
                            SettingsActivity.this.writeButton.setVisibility(setVisible ? 0 : 8);
                            SettingsActivity.this.writeButtonAnimation = null;
                        }
                    }
                });
                this.writeButtonAnimation.start();
            }
            ViewProxy.setScaleX(this.avatarImage, (42.0f + (18.0f * diff)) / 42.0f);
            ViewProxy.setScaleY(this.avatarImage, (42.0f + (18.0f * diff)) / 42.0f);
            float avatarY = ((((float) (this.actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0)) + ((((float) ActionBar.getCurrentActionBarHeight()) / 2.0f) * (1.0f + diff))) - (21.0f * AndroidUtilities.density)) + ((27.0f * AndroidUtilities.density) * diff);
            ViewProxy.setTranslationX(this.avatarImage, ((float) (-AndroidUtilities.dp(47.0f))) * diff);
            ViewProxy.setTranslationY(this.avatarImage, (float) Math.ceil((double) avatarY));
            ViewProxy.setTranslationX(this.nameTextView, (-21.0f * AndroidUtilities.density) * diff);
            ViewProxy.setTranslationY(this.nameTextView, (((float) Math.floor((double) avatarY)) - ((float) Math.ceil((double) AndroidUtilities.density))) + ((float) Math.floor((double) ((7.0f * AndroidUtilities.density) * diff))));
            ViewProxy.setTranslationX(this.onlineTextView, (-21.0f * AndroidUtilities.density) * diff);
            ViewProxy.setTranslationY(this.onlineTextView, (((float) Math.floor((double) avatarY)) + ((float) AndroidUtilities.dp(22.0f))) + (((float) Math.floor((double) (11.0f * AndroidUtilities.density))) * diff));
            ViewProxy.setScaleX(this.nameTextView, 1.0f + (0.12f * diff));
            ViewProxy.setScaleY(this.nameTextView, 1.0f + (0.12f * diff));
        }
    }

    private void fixLayout() {
        if (this.fragmentView != null) {
            this.fragmentView.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
                public boolean onPreDraw() {
                    if (SettingsActivity.this.fragmentView != null) {
                        SettingsActivity.this.needLayout();
                        SettingsActivity.this.fragmentView.getViewTreeObserver().removeOnPreDrawListener(this);
                    }
                    return true;
                }
            });
        }
    }

    private void updateUserData() {
        boolean z = true;
        User user = MessagesController.getInstance().getUser(Integer.valueOf(UserConfig.getClientUserId()));
        TLObject photo = null;
        FileLocation photoBig = null;
        if (user.photo != null) {
            photo = user.photo.photo_small;
            photoBig = user.photo.photo_big;
        }
        Drawable avatarDrawable = new AvatarDrawable(user, true);
        avatarDrawable.setColor(-10708787);
        if (this.avatarImage != null) {
            this.avatarImage.setImage(photo, "50_50", avatarDrawable);
            this.avatarImage.getImageReceiver().setVisible(!PhotoViewer.getInstance().isShowingImage(photoBig), false);
            this.nameTextView.setText(UserObject.getUserName(user));
            this.onlineTextView.setText(LocaleController.getString("Online", C0553R.string.Online));
            ImageReceiver imageReceiver = this.avatarImage.getImageReceiver();
            if (PhotoViewer.getInstance().isShowingImage(photoBig)) {
                z = false;
            }
            imageReceiver.setVisible(z, false);
        }
    }

    private void sendLogs() {
        try {
            ArrayList<Uri> uris = new ArrayList();
            for (File file : new File(ApplicationLoader.applicationContext.getExternalFilesDir(null).getAbsolutePath() + "/logs").listFiles()) {
                uris.add(Uri.fromFile(file));
            }
            if (!uris.isEmpty()) {
                Intent i = new Intent("android.intent.action.SEND_MULTIPLE");
                i.setType("message/rfc822");
                i.putExtra("android.intent.extra.EMAIL", new String[]{BuildVars.SEND_LOGS_EMAIL});
                i.putExtra("android.intent.extra.SUBJECT", "last logs");
                i.putParcelableArrayListExtra("android.intent.extra.STREAM", uris);
                getParentActivity().startActivityForResult(Intent.createChooser(i, "Select email application."), 500);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
