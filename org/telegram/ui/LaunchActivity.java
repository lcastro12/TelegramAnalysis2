package org.telegram.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Iterator;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.C0553R;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.query.StickersQuery;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.ChatInvite;
import org.telegram.tgnet.TLRPC.InputStickerSet;
import org.telegram.tgnet.TLRPC.TL_contacts_resolveUsername;
import org.telegram.tgnet.TLRPC.TL_contacts_resolvedPeer;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_inputStickerSetShortName;
import org.telegram.tgnet.TLRPC.TL_messages_checkChatInvite;
import org.telegram.tgnet.TLRPC.TL_messages_importChatInvite;
import org.telegram.tgnet.TLRPC.Updates;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.ActionBar.ActionBarLayout;
import org.telegram.ui.ActionBar.ActionBarLayout.ActionBarLayoutDelegate;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.DrawerLayoutContainer;
import org.telegram.ui.Adapters.DrawerLayoutAdapter;
import org.telegram.ui.Components.PasscodeView;
import org.telegram.ui.Components.PasscodeView.PasscodeViewDelegate;
import org.telegram.ui.DialogsActivity.MessagesActivityDelegate;

public class LaunchActivity extends Activity implements ActionBarLayoutDelegate, NotificationCenterDelegate, MessagesActivityDelegate {
    private static ArrayList<BaseFragment> layerFragmentsStack = new ArrayList();
    private static ArrayList<BaseFragment> mainFragmentsStack = new ArrayList();
    private static ArrayList<BaseFragment> rightFragmentsStack = new ArrayList();
    private ActionBarLayout actionBarLayout;
    private ImageView backgroundTablet;
    private ArrayList<User> contactsToSend;
    private int currentConnectionState;
    private String documentsMimeType;
    private ArrayList<String> documentsOriginalPathsArray;
    private ArrayList<String> documentsPathsArray;
    private ArrayList<Uri> documentsUrisArray;
    private DrawerLayoutAdapter drawerLayoutAdapter;
    protected DrawerLayoutContainer drawerLayoutContainer;
    private boolean finished;
    private ActionBarLayout layersActionBarLayout;
    private Runnable lockRunnable;
    private OnGlobalLayoutListener onGlobalLayoutListener;
    private Intent passcodeSaveIntent;
    private boolean passcodeSaveIntentIsNew;
    private boolean passcodeSaveIntentIsRestore;
    private PasscodeView passcodeView;
    private ArrayList<Uri> photoPathsArray;
    private ActionBarLayout rightActionBarLayout;
    private String sendingText;
    private FrameLayout shadowTablet;
    private FrameLayout shadowTabletSide;
    private boolean tabletFullSize;
    private String videoPath;
    private AlertDialog visibleDialog;

    class C10431 implements OnTouchListener {
        C10431() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            if (LaunchActivity.this.actionBarLayout.fragmentsStack.isEmpty() || event.getAction() != 1) {
                return false;
            }
            float x = event.getX();
            float y = event.getY();
            int[] location = new int[2];
            LaunchActivity.this.layersActionBarLayout.getLocationOnScreen(location);
            int viewX = location[0];
            int viewY = location[1];
            if (LaunchActivity.this.layersActionBarLayout.checkTransitionAnimation() || (x > ((float) viewX) && x < ((float) (LaunchActivity.this.layersActionBarLayout.getWidth() + viewX)) && y > ((float) viewY) && y < ((float) (LaunchActivity.this.layersActionBarLayout.getHeight() + viewY)))) {
                return false;
            }
            if (!LaunchActivity.this.layersActionBarLayout.fragmentsStack.isEmpty()) {
                int a = 0;
                while (LaunchActivity.this.layersActionBarLayout.fragmentsStack.size() - 1 > 0) {
                    LaunchActivity.this.layersActionBarLayout.removeFragmentFromStack((BaseFragment) LaunchActivity.this.layersActionBarLayout.fragmentsStack.get(0));
                    a = (a - 1) + 1;
                }
                LaunchActivity.this.layersActionBarLayout.closeLastFragment(true);
            }
            return true;
        }
    }

    class C10442 implements OnClickListener {
        C10442() {
        }

        public void onClick(View v) {
        }
    }

    class C10464 implements OnItemClickListener {
        C10464() {
        }

        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            if (position == 2) {
                if (MessagesController.isFeatureEnabled("chat_create", (BaseFragment) LaunchActivity.this.actionBarLayout.fragmentsStack.get(LaunchActivity.this.actionBarLayout.fragmentsStack.size() - 1))) {
                    LaunchActivity.this.presentFragment(new GroupCreateActivity());
                    LaunchActivity.this.drawerLayoutContainer.closeDrawer(false);
                }
            } else if (position == 3) {
                args = new Bundle();
                args.putBoolean("onlyUsers", true);
                args.putBoolean("destroyAfterSelect", true);
                args.putBoolean("createSecretChat", true);
                LaunchActivity.this.presentFragment(new ContactsActivity(args));
                LaunchActivity.this.drawerLayoutContainer.closeDrawer(false);
            } else if (position == 4) {
                if (MessagesController.isFeatureEnabled("broadcast_create", (BaseFragment) LaunchActivity.this.actionBarLayout.fragmentsStack.get(LaunchActivity.this.actionBarLayout.fragmentsStack.size() - 1))) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0);
                    if (preferences.getBoolean("channel_intro", false)) {
                        args = new Bundle();
                        args.putInt("step", 0);
                        LaunchActivity.this.presentFragment(new ChannelCreateActivity(args));
                    } else {
                        LaunchActivity.this.presentFragment(new ChannelIntroActivity());
                        preferences.edit().putBoolean("channel_intro", true).commit();
                    }
                    LaunchActivity.this.drawerLayoutContainer.closeDrawer(false);
                }
            } else if (position == 6) {
                LaunchActivity.this.presentFragment(new ContactsActivity(null));
                LaunchActivity.this.drawerLayoutContainer.closeDrawer(false);
            } else if (position == 7) {
                try {
                    Intent intent = new Intent("android.intent.action.SEND");
                    intent.setType("text/plain");
                    intent.putExtra("android.intent.extra.TEXT", ContactsController.getInstance().getInviteText());
                    LaunchActivity.this.startActivityForResult(Intent.createChooser(intent, LocaleController.getString("InviteFriends", C0553R.string.InviteFriends)), 500);
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
                LaunchActivity.this.drawerLayoutContainer.closeDrawer(false);
            } else if (position == 8) {
                LaunchActivity.this.presentFragment(new SettingsActivity());
                LaunchActivity.this.drawerLayoutContainer.closeDrawer(false);
            } else if (position == 9) {
                try {
                    LaunchActivity.this.startActivityForResult(new Intent("android.intent.action.VIEW", Uri.parse(LocaleController.getString("TelegramFaqUrl", C0553R.string.TelegramFaqUrl))), 500);
                } catch (Throwable e2) {
                    FileLog.m611e("tmessages", e2);
                }
                LaunchActivity.this.drawerLayoutContainer.closeDrawer(false);
            }
        }
    }

    class C16036 implements PasscodeViewDelegate {
        C16036() {
        }

        public void didAcceptedPassword() {
            UserConfig.isWaitingForPasscodeEnter = false;
            if (LaunchActivity.this.passcodeSaveIntent != null) {
                LaunchActivity.this.handleIntent(LaunchActivity.this.passcodeSaveIntent, LaunchActivity.this.passcodeSaveIntentIsNew, LaunchActivity.this.passcodeSaveIntentIsRestore, true);
                LaunchActivity.this.passcodeSaveIntent = null;
            }
            LaunchActivity.this.drawerLayoutContainer.setAllowOpenDrawer(true, false);
            LaunchActivity.this.actionBarLayout.showLastFragment();
            if (AndroidUtilities.isTablet()) {
                LaunchActivity.this.layersActionBarLayout.showLastFragment();
                LaunchActivity.this.rightActionBarLayout.showLastFragment();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void onCreate(android.os.Bundle r32) {
        /*
        r31 = this;
        org.telegram.messenger.ApplicationLoader.postInitApplication();
        org.telegram.messenger.NativeCrashManager.handleDumpFiles(r31);
        r26 = org.telegram.messenger.UserConfig.isClientActivated();
        if (r26 != 0) goto L_0x0072;
    L_0x000c:
        r12 = r31.getIntent();
        if (r12 == 0) goto L_0x0037;
    L_0x0012:
        r26 = r12.getAction();
        if (r26 == 0) goto L_0x0037;
    L_0x0018:
        r26 = "android.intent.action.SEND";
        r27 = r12.getAction();
        r26 = r26.equals(r27);
        if (r26 != 0) goto L_0x0030;
    L_0x0024:
        r26 = r12.getAction();
        r27 = "android.intent.action.SEND_MULTIPLE";
        r26 = r26.equals(r27);
        if (r26 == 0) goto L_0x0037;
    L_0x0030:
        super.onCreate(r32);
        r31.finish();
    L_0x0036:
        return;
    L_0x0037:
        if (r12 == 0) goto L_0x0072;
    L_0x0039:
        r26 = "fromIntro";
        r27 = 0;
        r0 = r26;
        r1 = r27;
        r26 = r12.getBooleanExtra(r0, r1);
        if (r26 != 0) goto L_0x0072;
    L_0x0047:
        r26 = org.telegram.messenger.ApplicationLoader.applicationContext;
        r27 = "logininfo";
        r28 = 0;
        r18 = r26.getSharedPreferences(r27, r28);
        r24 = r18.getAll();
        r26 = r24.isEmpty();
        if (r26 == 0) goto L_0x0072;
    L_0x005b:
        r13 = new android.content.Intent;
        r26 = org.telegram.ui.IntroActivity.class;
        r0 = r31;
        r1 = r26;
        r13.<init>(r0, r1);
        r0 = r31;
        r0.startActivity(r13);
        super.onCreate(r32);
        r31.finish();
        goto L_0x0036;
    L_0x0072:
        r26 = 1;
        r0 = r31;
        r1 = r26;
        r0.requestWindowFeature(r1);
        r26 = 2131296259; // 0x7f090003 float:1.821043E38 double:1.0530002627E-314;
        r0 = r31;
        r1 = r26;
        r0.setTheme(r1);
        r26 = r31.getWindow();
        r27 = 2130837932; // 0x7f0201ac float:1.7280832E38 double:1.052773819E-314;
        r26.setBackgroundDrawableResource(r27);
        super.onCreate(r32);
        r26 = org.telegram.messenger.UserConfig.passcodeHash;
        r26 = r26.length();
        if (r26 == 0) goto L_0x00a8;
    L_0x009a:
        r26 = org.telegram.messenger.UserConfig.appLocked;
        if (r26 == 0) goto L_0x00a8;
    L_0x009e:
        r26 = org.telegram.tgnet.ConnectionsManager.getInstance();
        r26 = r26.getCurrentTime();
        org.telegram.messenger.UserConfig.lastPauseTime = r26;
    L_0x00a8:
        r26 = r31.getResources();
        r27 = "status_bar_height";
        r28 = "dimen";
        r29 = "android";
        r21 = r26.getIdentifier(r27, r28, r29);
        if (r21 <= 0) goto L_0x00c6;
    L_0x00b8:
        r26 = r31.getResources();
        r0 = r26;
        r1 = r21;
        r26 = r0.getDimensionPixelSize(r1);
        org.telegram.messenger.AndroidUtilities.statusBarHeight = r26;
    L_0x00c6:
        r26 = new org.telegram.ui.ActionBar.ActionBarLayout;
        r0 = r26;
        r1 = r31;
        r0.<init>(r1);
        r0 = r26;
        r1 = r31;
        r1.actionBarLayout = r0;
        r26 = new org.telegram.ui.ActionBar.DrawerLayoutContainer;
        r0 = r26;
        r1 = r31;
        r0.<init>(r1);
        r0 = r26;
        r1 = r31;
        r1.drawerLayoutContainer = r0;
        r0 = r31;
        r0 = r0.drawerLayoutContainer;
        r26 = r0;
        r27 = new android.view.ViewGroup$LayoutParams;
        r28 = -1;
        r29 = -1;
        r27.<init>(r28, r29);
        r0 = r31;
        r1 = r26;
        r2 = r27;
        r0.setContentView(r1, r2);
        r26 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r26 == 0) goto L_0x05d4;
    L_0x0102:
        r26 = r31.getWindow();
        r27 = 16;
        r26.setSoftInputMode(r27);
        r14 = new android.widget.RelativeLayout;
        r0 = r31;
        r14.<init>(r0);
        r0 = r31;
        r0 = r0.drawerLayoutContainer;
        r26 = r0;
        r0 = r26;
        r0.addView(r14);
        r16 = r14.getLayoutParams();
        r16 = (android.widget.FrameLayout.LayoutParams) r16;
        r26 = -1;
        r0 = r26;
        r1 = r16;
        r1.width = r0;
        r26 = -1;
        r0 = r26;
        r1 = r16;
        r1.height = r0;
        r0 = r16;
        r14.setLayoutParams(r0);
        r26 = new android.widget.ImageView;
        r0 = r26;
        r1 = r31;
        r0.<init>(r1);
        r0 = r26;
        r1 = r31;
        r1.backgroundTablet = r0;
        r0 = r31;
        r0 = r0.backgroundTablet;
        r26 = r0;
        r27 = android.widget.ImageView.ScaleType.CENTER_CROP;
        r26.setScaleType(r27);
        r0 = r31;
        r0 = r0.backgroundTablet;
        r26 = r0;
        r27 = 2130837587; // 0x7f020053 float:1.7280132E38 double:1.0527736486E-314;
        r26.setImageResource(r27);
        r0 = r31;
        r0 = r0.backgroundTablet;
        r26 = r0;
        r0 = r26;
        r14.addView(r0);
        r0 = r31;
        r0 = r0.backgroundTablet;
        r26 = r0;
        r20 = r26.getLayoutParams();
        r20 = (android.widget.RelativeLayout.LayoutParams) r20;
        r26 = -1;
        r0 = r26;
        r1 = r20;
        r1.width = r0;
        r26 = -1;
        r0 = r26;
        r1 = r20;
        r1.height = r0;
        r0 = r31;
        r0 = r0.backgroundTablet;
        r26 = r0;
        r0 = r26;
        r1 = r20;
        r0.setLayoutParams(r1);
        r0 = r31;
        r0 = r0.actionBarLayout;
        r26 = r0;
        r0 = r26;
        r14.addView(r0);
        r0 = r31;
        r0 = r0.actionBarLayout;
        r26 = r0;
        r20 = r26.getLayoutParams();
        r20 = (android.widget.RelativeLayout.LayoutParams) r20;
        r26 = -1;
        r0 = r26;
        r1 = r20;
        r1.width = r0;
        r26 = -1;
        r0 = r26;
        r1 = r20;
        r1.height = r0;
        r0 = r31;
        r0 = r0.actionBarLayout;
        r26 = r0;
        r0 = r26;
        r1 = r20;
        r0.setLayoutParams(r1);
        r26 = new org.telegram.ui.ActionBar.ActionBarLayout;
        r0 = r26;
        r1 = r31;
        r0.<init>(r1);
        r0 = r26;
        r1 = r31;
        r1.rightActionBarLayout = r0;
        r0 = r31;
        r0 = r0.rightActionBarLayout;
        r26 = r0;
        r0 = r26;
        r14.addView(r0);
        r0 = r31;
        r0 = r0.rightActionBarLayout;
        r26 = r0;
        r20 = r26.getLayoutParams();
        r20 = (android.widget.RelativeLayout.LayoutParams) r20;
        r26 = 1134559232; // 0x43a00000 float:320.0 double:5.605467397E-315;
        r26 = org.telegram.messenger.AndroidUtilities.dp(r26);
        r0 = r26;
        r1 = r20;
        r1.width = r0;
        r26 = -1;
        r0 = r26;
        r1 = r20;
        r1.height = r0;
        r0 = r31;
        r0 = r0.rightActionBarLayout;
        r26 = r0;
        r0 = r26;
        r1 = r20;
        r0.setLayoutParams(r1);
        r0 = r31;
        r0 = r0.rightActionBarLayout;
        r26 = r0;
        r27 = rightFragmentsStack;
        r26.init(r27);
        r0 = r31;
        r0 = r0.rightActionBarLayout;
        r26 = r0;
        r0 = r26;
        r1 = r31;
        r0.setDelegate(r1);
        r26 = new android.widget.FrameLayout;
        r0 = r26;
        r1 = r31;
        r0.<init>(r1);
        r0 = r26;
        r1 = r31;
        r1.shadowTabletSide = r0;
        r0 = r31;
        r0 = r0.shadowTabletSide;
        r26 = r0;
        r27 = 1076449908; // 0x40295274 float:2.6456575 double:5.31836919E-315;
        r26.setBackgroundColor(r27);
        r0 = r31;
        r0 = r0.shadowTabletSide;
        r26 = r0;
        r0 = r26;
        r14.addView(r0);
        r0 = r31;
        r0 = r0.shadowTabletSide;
        r26 = r0;
        r20 = r26.getLayoutParams();
        r20 = (android.widget.RelativeLayout.LayoutParams) r20;
        r26 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r26 = org.telegram.messenger.AndroidUtilities.dp(r26);
        r0 = r26;
        r1 = r20;
        r1.width = r0;
        r26 = -1;
        r0 = r26;
        r1 = r20;
        r1.height = r0;
        r0 = r31;
        r0 = r0.shadowTabletSide;
        r26 = r0;
        r0 = r26;
        r1 = r20;
        r0.setLayoutParams(r1);
        r26 = new android.widget.FrameLayout;
        r0 = r26;
        r1 = r31;
        r0.<init>(r1);
        r0 = r26;
        r1 = r31;
        r1.shadowTablet = r0;
        r0 = r31;
        r0 = r0.shadowTablet;
        r26 = r0;
        r27 = 8;
        r26.setVisibility(r27);
        r0 = r31;
        r0 = r0.shadowTablet;
        r26 = r0;
        r27 = 2130706432; // 0x7f000000 float:1.7014118E38 double:1.0527088494E-314;
        r26.setBackgroundColor(r27);
        r0 = r31;
        r0 = r0.shadowTablet;
        r26 = r0;
        r0 = r26;
        r14.addView(r0);
        r0 = r31;
        r0 = r0.shadowTablet;
        r26 = r0;
        r20 = r26.getLayoutParams();
        r20 = (android.widget.RelativeLayout.LayoutParams) r20;
        r26 = -1;
        r0 = r26;
        r1 = r20;
        r1.width = r0;
        r26 = -1;
        r0 = r26;
        r1 = r20;
        r1.height = r0;
        r0 = r31;
        r0 = r0.shadowTablet;
        r26 = r0;
        r0 = r26;
        r1 = r20;
        r0.setLayoutParams(r1);
        r0 = r31;
        r0 = r0.shadowTablet;
        r26 = r0;
        r27 = new org.telegram.ui.LaunchActivity$1;
        r0 = r27;
        r1 = r31;
        r0.<init>();
        r26.setOnTouchListener(r27);
        r0 = r31;
        r0 = r0.shadowTablet;
        r26 = r0;
        r27 = new org.telegram.ui.LaunchActivity$2;
        r0 = r27;
        r1 = r31;
        r0.<init>();
        r26.setOnClickListener(r27);
        r26 = new org.telegram.ui.ActionBar.ActionBarLayout;
        r0 = r26;
        r1 = r31;
        r0.<init>(r1);
        r0 = r26;
        r1 = r31;
        r1.layersActionBarLayout = r0;
        r0 = r31;
        r0 = r0.layersActionBarLayout;
        r26 = r0;
        r27 = 1;
        r26.setRemoveActionBarExtraHeight(r27);
        r0 = r31;
        r0 = r0.layersActionBarLayout;
        r26 = r0;
        r0 = r31;
        r0 = r0.shadowTablet;
        r27 = r0;
        r26.setBackgroundView(r27);
        r0 = r31;
        r0 = r0.layersActionBarLayout;
        r26 = r0;
        r27 = 1;
        r26.setUseAlphaAnimations(r27);
        r0 = r31;
        r0 = r0.layersActionBarLayout;
        r26 = r0;
        r27 = 2130837573; // 0x7f020045 float:1.7280104E38 double:1.0527736417E-314;
        r26.setBackgroundResource(r27);
        r0 = r31;
        r0 = r0.layersActionBarLayout;
        r26 = r0;
        r0 = r26;
        r14.addView(r0);
        r0 = r31;
        r0 = r0.layersActionBarLayout;
        r26 = r0;
        r20 = r26.getLayoutParams();
        r20 = (android.widget.RelativeLayout.LayoutParams) r20;
        r26 = 1141145600; // 0x44048000 float:530.0 double:5.63800838E-315;
        r26 = org.telegram.messenger.AndroidUtilities.dp(r26);
        r0 = r26;
        r1 = r20;
        r1.width = r0;
        r26 = 1141112832; // 0x44040000 float:528.0 double:5.637846483E-315;
        r26 = org.telegram.messenger.AndroidUtilities.dp(r26);
        r0 = r26;
        r1 = r20;
        r1.height = r0;
        r0 = r31;
        r0 = r0.layersActionBarLayout;
        r26 = r0;
        r0 = r26;
        r1 = r20;
        r0.setLayoutParams(r1);
        r0 = r31;
        r0 = r0.layersActionBarLayout;
        r26 = r0;
        r27 = layerFragmentsStack;
        r26.init(r27);
        r0 = r31;
        r0 = r0.layersActionBarLayout;
        r26 = r0;
        r0 = r26;
        r1 = r31;
        r0.setDelegate(r1);
        r0 = r31;
        r0 = r0.layersActionBarLayout;
        r26 = r0;
        r0 = r31;
        r0 = r0.drawerLayoutContainer;
        r27 = r0;
        r26.setDrawerLayoutContainer(r27);
        r0 = r31;
        r0 = r0.layersActionBarLayout;
        r26 = r0;
        r27 = 8;
        r26.setVisibility(r27);
    L_0x03a4:
        r17 = new org.telegram.ui.LaunchActivity$3;
        r0 = r17;
        r1 = r31;
        r2 = r31;
        r0.<init>(r2);
        r26 = -1;
        r0 = r17;
        r1 = r26;
        r0.setBackgroundColor(r1);
        r26 = new org.telegram.ui.Adapters.DrawerLayoutAdapter;
        r0 = r26;
        r1 = r31;
        r0.<init>(r1);
        r0 = r26;
        r1 = r31;
        r1.drawerLayoutAdapter = r0;
        r0 = r17;
        r1 = r26;
        r0.setAdapter(r1);
        r26 = 1;
        r0 = r17;
        r1 = r26;
        r0.setChoiceMode(r1);
        r26 = 0;
        r0 = r17;
        r1 = r26;
        r0.setDivider(r1);
        r26 = 0;
        r0 = r17;
        r1 = r26;
        r0.setDividerHeight(r1);
        r26 = 0;
        r0 = r17;
        r1 = r26;
        r0.setVerticalScrollBarEnabled(r1);
        r0 = r31;
        r0 = r0.drawerLayoutContainer;
        r26 = r0;
        r0 = r26;
        r1 = r17;
        r0.setDrawerLayout(r1);
        r15 = r17.getLayoutParams();
        r15 = (android.widget.FrameLayout.LayoutParams) r15;
        r22 = org.telegram.messenger.AndroidUtilities.getRealScreenSize();
        r26 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r26 == 0) goto L_0x05ee;
    L_0x040f:
        r26 = 1134559232; // 0x43a00000 float:320.0 double:5.605467397E-315;
        r26 = org.telegram.messenger.AndroidUtilities.dp(r26);
    L_0x0415:
        r0 = r26;
        r15.width = r0;
        r26 = -1;
        r0 = r26;
        r15.height = r0;
        r0 = r17;
        r0.setLayoutParams(r15);
        r26 = new org.telegram.ui.LaunchActivity$4;
        r0 = r26;
        r1 = r31;
        r0.<init>();
        r0 = r17;
        r1 = r26;
        r0.setOnItemClickListener(r1);
        r0 = r31;
        r0 = r0.drawerLayoutContainer;
        r26 = r0;
        r0 = r31;
        r0 = r0.actionBarLayout;
        r27 = r0;
        r26.setParentActionBarLayout(r27);
        r0 = r31;
        r0 = r0.actionBarLayout;
        r26 = r0;
        r0 = r31;
        r0 = r0.drawerLayoutContainer;
        r27 = r0;
        r26.setDrawerLayoutContainer(r27);
        r0 = r31;
        r0 = r0.actionBarLayout;
        r26 = r0;
        r27 = mainFragmentsStack;
        r26.init(r27);
        r0 = r31;
        r0 = r0.actionBarLayout;
        r26 = r0;
        r0 = r26;
        r1 = r31;
        r0.setDelegate(r1);
        org.telegram.messenger.ApplicationLoader.loadWallpaper();
        r26 = new org.telegram.ui.Components.PasscodeView;
        r0 = r26;
        r1 = r31;
        r0.<init>(r1);
        r0 = r26;
        r1 = r31;
        r1.passcodeView = r0;
        r0 = r31;
        r0 = r0.drawerLayoutContainer;
        r26 = r0;
        r0 = r31;
        r0 = r0.passcodeView;
        r27 = r0;
        r26.addView(r27);
        r0 = r31;
        r0 = r0.passcodeView;
        r26 = r0;
        r16 = r26.getLayoutParams();
        r16 = (android.widget.FrameLayout.LayoutParams) r16;
        r26 = -1;
        r0 = r26;
        r1 = r16;
        r1.width = r0;
        r26 = -1;
        r0 = r26;
        r1 = r16;
        r1.height = r0;
        r0 = r31;
        r0 = r0.passcodeView;
        r26 = r0;
        r0 = r26;
        r1 = r16;
        r0.setLayoutParams(r1);
        r26 = org.telegram.messenger.NotificationCenter.getInstance();
        r27 = org.telegram.messenger.NotificationCenter.closeOtherAppActivities;
        r28 = 1;
        r0 = r28;
        r0 = new java.lang.Object[r0];
        r28 = r0;
        r29 = 0;
        r28[r29] = r31;
        r26.postNotificationName(r27, r28);
        r26 = org.telegram.tgnet.ConnectionsManager.getInstance();
        r26 = r26.getConnectionState();
        r0 = r26;
        r1 = r31;
        r1.currentConnectionState = r0;
        r26 = org.telegram.messenger.NotificationCenter.getInstance();
        r27 = org.telegram.messenger.NotificationCenter.appDidLogout;
        r0 = r26;
        r1 = r31;
        r2 = r27;
        r0.addObserver(r1, r2);
        r26 = org.telegram.messenger.NotificationCenter.getInstance();
        r27 = org.telegram.messenger.NotificationCenter.mainUserInfoChanged;
        r0 = r26;
        r1 = r31;
        r2 = r27;
        r0.addObserver(r1, r2);
        r26 = org.telegram.messenger.NotificationCenter.getInstance();
        r27 = org.telegram.messenger.NotificationCenter.closeOtherAppActivities;
        r0 = r26;
        r1 = r31;
        r2 = r27;
        r0.addObserver(r1, r2);
        r26 = org.telegram.messenger.NotificationCenter.getInstance();
        r27 = org.telegram.messenger.NotificationCenter.didUpdatedConnectionState;
        r0 = r26;
        r1 = r31;
        r2 = r27;
        r0.addObserver(r1, r2);
        r26 = org.telegram.messenger.NotificationCenter.getInstance();
        r27 = org.telegram.messenger.NotificationCenter.needShowAlert;
        r0 = r26;
        r1 = r31;
        r2 = r27;
        r0.addObserver(r1, r2);
        r26 = android.os.Build.VERSION.SDK_INT;
        r27 = 14;
        r0 = r26;
        r1 = r27;
        if (r0 >= r1) goto L_0x053b;
    L_0x052c:
        r26 = org.telegram.messenger.NotificationCenter.getInstance();
        r27 = org.telegram.messenger.NotificationCenter.screenStateChanged;
        r0 = r26;
        r1 = r31;
        r2 = r27;
        r0.addObserver(r1, r2);
    L_0x053b:
        r0 = r31;
        r0 = r0.actionBarLayout;
        r26 = r0;
        r0 = r26;
        r0 = r0.fragmentsStack;
        r26 = r0;
        r26 = r26.isEmpty();
        if (r26 == 0) goto L_0x075b;
    L_0x054d:
        r26 = org.telegram.messenger.UserConfig.isClientActivated();
        if (r26 != 0) goto L_0x0608;
    L_0x0553:
        r0 = r31;
        r0 = r0.actionBarLayout;
        r26 = r0;
        r27 = new org.telegram.ui.LoginActivity;
        r27.<init>();
        r26.addFragmentToStack(r27);
        r0 = r31;
        r0 = r0.drawerLayoutContainer;
        r26 = r0;
        r27 = 0;
        r28 = 0;
        r26.setAllowOpenDrawer(r27, r28);
    L_0x056e:
        if (r32 == 0) goto L_0x0592;
    L_0x0570:
        r26 = "fragment";
        r0 = r32;
        r1 = r26;
        r10 = r0.getString(r1);	 Catch:{ Exception -> 0x06a5 }
        if (r10 == 0) goto L_0x0592;
    L_0x057c:
        r26 = "args";
        r0 = r32;
        r1 = r26;
        r6 = r0.getBundle(r1);	 Catch:{ Exception -> 0x06a5 }
        r26 = -1;
        r27 = r10.hashCode();	 Catch:{ Exception -> 0x06a5 }
        switch(r27) {
            case -1529105743: goto L_0x067b;
            case -1349522494: goto L_0x066d;
            case 3052376: goto L_0x0627;
            case 3108362: goto L_0x065f;
            case 98629247: goto L_0x0643;
            case 738950403: goto L_0x0651;
            case 1434631203: goto L_0x0635;
            default: goto L_0x058f;
        };
    L_0x058f:
        switch(r26) {
            case 0: goto L_0x0689;
            case 1: goto L_0x06af;
            case 2: goto L_0x06ca;
            case 3: goto L_0x06e6;
            case 4: goto L_0x0702;
            case 5: goto L_0x071e;
            case 6: goto L_0x0740;
            default: goto L_0x0592;
        };
    L_0x0592:
        r27 = r31.getIntent();
        r28 = 0;
        if (r32 == 0) goto L_0x0806;
    L_0x059a:
        r26 = 1;
    L_0x059c:
        r29 = 0;
        r0 = r31;
        r1 = r27;
        r2 = r28;
        r3 = r26;
        r4 = r29;
        r0.handleIntent(r1, r2, r3, r4);
        r31.needLayout();
        r26 = r31.getWindow();
        r26 = r26.getDecorView();
        r25 = r26.getRootView();
        r26 = r25.getViewTreeObserver();
        r27 = new org.telegram.ui.LaunchActivity$5;
        r0 = r27;
        r1 = r31;
        r2 = r25;
        r0.<init>(r2);
        r0 = r27;
        r1 = r31;
        r1.onGlobalLayoutListener = r0;
        r26.addOnGlobalLayoutListener(r27);
        goto L_0x0036;
    L_0x05d4:
        r0 = r31;
        r0 = r0.drawerLayoutContainer;
        r26 = r0;
        r0 = r31;
        r0 = r0.actionBarLayout;
        r27 = r0;
        r28 = new android.view.ViewGroup$LayoutParams;
        r29 = -1;
        r30 = -1;
        r28.<init>(r29, r30);
        r26.addView(r27, r28);
        goto L_0x03a4;
    L_0x05ee:
        r0 = r22;
        r0 = r0.x;
        r26 = r0;
        r0 = r22;
        r0 = r0.y;
        r27 = r0;
        r26 = java.lang.Math.min(r26, r27);
        r27 = 1113587712; // 0x42600000 float:56.0 double:5.50185432E-315;
        r27 = org.telegram.messenger.AndroidUtilities.dp(r27);
        r26 = r26 - r27;
        goto L_0x0415;
    L_0x0608:
        r0 = r31;
        r0 = r0.actionBarLayout;
        r26 = r0;
        r27 = new org.telegram.ui.DialogsActivity;
        r28 = 0;
        r27.<init>(r28);
        r26.addFragmentToStack(r27);
        r0 = r31;
        r0 = r0.drawerLayoutContainer;
        r26 = r0;
        r27 = 1;
        r28 = 0;
        r26.setAllowOpenDrawer(r27, r28);
        goto L_0x056e;
    L_0x0627:
        r27 = "chat";
        r0 = r27;
        r27 = r10.equals(r0);	 Catch:{ Exception -> 0x06a5 }
        if (r27 == 0) goto L_0x058f;
    L_0x0631:
        r26 = 0;
        goto L_0x058f;
    L_0x0635:
        r27 = "settings";
        r0 = r27;
        r27 = r10.equals(r0);	 Catch:{ Exception -> 0x06a5 }
        if (r27 == 0) goto L_0x058f;
    L_0x063f:
        r26 = 1;
        goto L_0x058f;
    L_0x0643:
        r27 = "group";
        r0 = r27;
        r27 = r10.equals(r0);	 Catch:{ Exception -> 0x06a5 }
        if (r27 == 0) goto L_0x058f;
    L_0x064d:
        r26 = 2;
        goto L_0x058f;
    L_0x0651:
        r27 = "channel";
        r0 = r27;
        r27 = r10.equals(r0);	 Catch:{ Exception -> 0x06a5 }
        if (r27 == 0) goto L_0x058f;
    L_0x065b:
        r26 = 3;
        goto L_0x058f;
    L_0x065f:
        r27 = "edit";
        r0 = r27;
        r27 = r10.equals(r0);	 Catch:{ Exception -> 0x06a5 }
        if (r27 == 0) goto L_0x058f;
    L_0x0669:
        r26 = 4;
        goto L_0x058f;
    L_0x066d:
        r27 = "chat_profile";
        r0 = r27;
        r27 = r10.equals(r0);	 Catch:{ Exception -> 0x06a5 }
        if (r27 == 0) goto L_0x058f;
    L_0x0677:
        r26 = 5;
        goto L_0x058f;
    L_0x067b:
        r27 = "wallpapers";
        r0 = r27;
        r27 = r10.equals(r0);	 Catch:{ Exception -> 0x06a5 }
        if (r27 == 0) goto L_0x058f;
    L_0x0685:
        r26 = 6;
        goto L_0x058f;
    L_0x0689:
        if (r6 == 0) goto L_0x0592;
    L_0x068b:
        r8 = new org.telegram.ui.ChatActivity;	 Catch:{ Exception -> 0x06a5 }
        r8.<init>(r6);	 Catch:{ Exception -> 0x06a5 }
        r0 = r31;
        r0 = r0.actionBarLayout;	 Catch:{ Exception -> 0x06a5 }
        r26 = r0;
        r0 = r26;
        r26 = r0.addFragmentToStack(r8);	 Catch:{ Exception -> 0x06a5 }
        if (r26 == 0) goto L_0x0592;
    L_0x069e:
        r0 = r32;
        r8.restoreSelfArgs(r0);	 Catch:{ Exception -> 0x06a5 }
        goto L_0x0592;
    L_0x06a5:
        r9 = move-exception;
        r26 = "tmessages";
        r0 = r26;
        org.telegram.messenger.FileLog.m611e(r0, r9);
        goto L_0x0592;
    L_0x06af:
        r23 = new org.telegram.ui.SettingsActivity;	 Catch:{ Exception -> 0x06a5 }
        r23.<init>();	 Catch:{ Exception -> 0x06a5 }
        r0 = r31;
        r0 = r0.actionBarLayout;	 Catch:{ Exception -> 0x06a5 }
        r26 = r0;
        r0 = r26;
        r1 = r23;
        r0.addFragmentToStack(r1);	 Catch:{ Exception -> 0x06a5 }
        r0 = r23;
        r1 = r32;
        r0.restoreSelfArgs(r1);	 Catch:{ Exception -> 0x06a5 }
        goto L_0x0592;
    L_0x06ca:
        if (r6 == 0) goto L_0x0592;
    L_0x06cc:
        r11 = new org.telegram.ui.GroupCreateFinalActivity;	 Catch:{ Exception -> 0x06a5 }
        r11.<init>(r6);	 Catch:{ Exception -> 0x06a5 }
        r0 = r31;
        r0 = r0.actionBarLayout;	 Catch:{ Exception -> 0x06a5 }
        r26 = r0;
        r0 = r26;
        r26 = r0.addFragmentToStack(r11);	 Catch:{ Exception -> 0x06a5 }
        if (r26 == 0) goto L_0x0592;
    L_0x06df:
        r0 = r32;
        r11.restoreSelfArgs(r0);	 Catch:{ Exception -> 0x06a5 }
        goto L_0x0592;
    L_0x06e6:
        if (r6 == 0) goto L_0x0592;
    L_0x06e8:
        r7 = new org.telegram.ui.ChannelCreateActivity;	 Catch:{ Exception -> 0x06a5 }
        r7.<init>(r6);	 Catch:{ Exception -> 0x06a5 }
        r0 = r31;
        r0 = r0.actionBarLayout;	 Catch:{ Exception -> 0x06a5 }
        r26 = r0;
        r0 = r26;
        r26 = r0.addFragmentToStack(r7);	 Catch:{ Exception -> 0x06a5 }
        if (r26 == 0) goto L_0x0592;
    L_0x06fb:
        r0 = r32;
        r7.restoreSelfArgs(r0);	 Catch:{ Exception -> 0x06a5 }
        goto L_0x0592;
    L_0x0702:
        if (r6 == 0) goto L_0x0592;
    L_0x0704:
        r7 = new org.telegram.ui.ChannelEditActivity;	 Catch:{ Exception -> 0x06a5 }
        r7.<init>(r6);	 Catch:{ Exception -> 0x06a5 }
        r0 = r31;
        r0 = r0.actionBarLayout;	 Catch:{ Exception -> 0x06a5 }
        r26 = r0;
        r0 = r26;
        r26 = r0.addFragmentToStack(r7);	 Catch:{ Exception -> 0x06a5 }
        if (r26 == 0) goto L_0x0592;
    L_0x0717:
        r0 = r32;
        r7.restoreSelfArgs(r0);	 Catch:{ Exception -> 0x06a5 }
        goto L_0x0592;
    L_0x071e:
        if (r6 == 0) goto L_0x0592;
    L_0x0720:
        r19 = new org.telegram.ui.ProfileActivity;	 Catch:{ Exception -> 0x06a5 }
        r0 = r19;
        r0.<init>(r6);	 Catch:{ Exception -> 0x06a5 }
        r0 = r31;
        r0 = r0.actionBarLayout;	 Catch:{ Exception -> 0x06a5 }
        r26 = r0;
        r0 = r26;
        r1 = r19;
        r26 = r0.addFragmentToStack(r1);	 Catch:{ Exception -> 0x06a5 }
        if (r26 == 0) goto L_0x0592;
    L_0x0737:
        r0 = r19;
        r1 = r32;
        r0.restoreSelfArgs(r1);	 Catch:{ Exception -> 0x06a5 }
        goto L_0x0592;
    L_0x0740:
        r23 = new org.telegram.ui.WallpapersActivity;	 Catch:{ Exception -> 0x06a5 }
        r23.<init>();	 Catch:{ Exception -> 0x06a5 }
        r0 = r31;
        r0 = r0.actionBarLayout;	 Catch:{ Exception -> 0x06a5 }
        r26 = r0;
        r0 = r26;
        r1 = r23;
        r0.addFragmentToStack(r1);	 Catch:{ Exception -> 0x06a5 }
        r0 = r23;
        r1 = r32;
        r0.restoreSelfArgs(r1);	 Catch:{ Exception -> 0x06a5 }
        goto L_0x0592;
    L_0x075b:
        r5 = 1;
        r26 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r26 == 0) goto L_0x07c0;
    L_0x0762:
        r0 = r31;
        r0 = r0.actionBarLayout;
        r26 = r0;
        r0 = r26;
        r0 = r0.fragmentsStack;
        r26 = r0;
        r26 = r26.size();
        r27 = 1;
        r0 = r26;
        r1 = r27;
        if (r0 > r1) goto L_0x0804;
    L_0x077a:
        r0 = r31;
        r0 = r0.layersActionBarLayout;
        r26 = r0;
        r0 = r26;
        r0 = r0.fragmentsStack;
        r26 = r0;
        r26 = r26.isEmpty();
        if (r26 == 0) goto L_0x0804;
    L_0x078c:
        r5 = 1;
    L_0x078d:
        r0 = r31;
        r0 = r0.layersActionBarLayout;
        r26 = r0;
        r0 = r26;
        r0 = r0.fragmentsStack;
        r26 = r0;
        r26 = r26.size();
        r27 = 1;
        r0 = r26;
        r1 = r27;
        if (r0 != r1) goto L_0x07c0;
    L_0x07a5:
        r0 = r31;
        r0 = r0.layersActionBarLayout;
        r26 = r0;
        r0 = r26;
        r0 = r0.fragmentsStack;
        r26 = r0;
        r27 = 0;
        r26 = r26.get(r27);
        r0 = r26;
        r0 = r0 instanceof org.telegram.ui.LoginActivity;
        r26 = r0;
        if (r26 == 0) goto L_0x07c0;
    L_0x07bf:
        r5 = 0;
    L_0x07c0:
        r0 = r31;
        r0 = r0.actionBarLayout;
        r26 = r0;
        r0 = r26;
        r0 = r0.fragmentsStack;
        r26 = r0;
        r26 = r26.size();
        r27 = 1;
        r0 = r26;
        r1 = r27;
        if (r0 != r1) goto L_0x07f3;
    L_0x07d8:
        r0 = r31;
        r0 = r0.actionBarLayout;
        r26 = r0;
        r0 = r26;
        r0 = r0.fragmentsStack;
        r26 = r0;
        r27 = 0;
        r26 = r26.get(r27);
        r0 = r26;
        r0 = r0 instanceof org.telegram.ui.LoginActivity;
        r26 = r0;
        if (r26 == 0) goto L_0x07f3;
    L_0x07f2:
        r5 = 0;
    L_0x07f3:
        r0 = r31;
        r0 = r0.drawerLayoutContainer;
        r26 = r0;
        r27 = 0;
        r0 = r26;
        r1 = r27;
        r0.setAllowOpenDrawer(r5, r1);
        goto L_0x0592;
    L_0x0804:
        r5 = 0;
        goto L_0x078d;
    L_0x0806:
        r26 = 0;
        goto L_0x059c;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.LaunchActivity.onCreate(android.os.Bundle):void");
    }

    private void showPasscodeActivity() {
        if (this.passcodeView != null) {
            UserConfig.appLocked = true;
            if (PhotoViewer.getInstance().isVisible()) {
                PhotoViewer.getInstance().closePhoto(false, true);
            }
            this.passcodeView.onShow();
            UserConfig.isWaitingForPasscodeEnter = true;
            this.drawerLayoutContainer.setAllowOpenDrawer(false, false);
            this.passcodeView.setDelegate(new C16036());
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean handleIntent(android.content.Intent r69, boolean r70, boolean r71, boolean r72) {
        /*
        r68 = this;
        r34 = r69.getFlags();
        if (r72 != 0) goto L_0x002d;
    L_0x0006:
        r4 = 1;
        r4 = org.telegram.messenger.AndroidUtilities.needShowPasscode(r4);
        if (r4 != 0) goto L_0x0011;
    L_0x000d:
        r4 = org.telegram.messenger.UserConfig.isWaitingForPasscodeEnter;
        if (r4 == 0) goto L_0x002d;
    L_0x0011:
        r68.showPasscodeActivity();
        r0 = r69;
        r1 = r68;
        r1.passcodeSaveIntent = r0;
        r0 = r70;
        r1 = r68;
        r1.passcodeSaveIntentIsNew = r0;
        r0 = r71;
        r1 = r68;
        r1.passcodeSaveIntentIsRestore = r0;
        r4 = 0;
        org.telegram.messenger.UserConfig.saveConfig(r4);
        r51 = 0;
    L_0x002c:
        return r51;
    L_0x002d:
        r51 = 0;
        r4 = 0;
        r54 = java.lang.Integer.valueOf(r4);
        r4 = 0;
        r52 = java.lang.Integer.valueOf(r4);
        r4 = 0;
        r53 = java.lang.Integer.valueOf(r4);
        r4 = 0;
        r43 = java.lang.Integer.valueOf(r4);
        if (r69 == 0) goto L_0x0136;
    L_0x0045:
        r4 = r69.getExtras();
        if (r4 == 0) goto L_0x0136;
    L_0x004b:
        r4 = r69.getExtras();
        r12 = "dialogId";
        r14 = 0;
        r30 = r4.getLong(r12, r14);
    L_0x0057:
        r57 = 0;
        r58 = 0;
        r4 = 0;
        r0 = r68;
        r0.photoPathsArray = r4;
        r4 = 0;
        r0 = r68;
        r0.videoPath = r4;
        r4 = 0;
        r0 = r68;
        r0.sendingText = r4;
        r4 = 0;
        r0 = r68;
        r0.documentsPathsArray = r4;
        r4 = 0;
        r0 = r68;
        r0.documentsOriginalPathsArray = r4;
        r4 = 0;
        r0 = r68;
        r0.documentsMimeType = r4;
        r4 = 0;
        r0 = r68;
        r0.documentsUrisArray = r4;
        r4 = 0;
        r0 = r68;
        r0.contactsToSend = r4;
        r4 = org.telegram.messenger.UserConfig.isClientActivated();
        if (r4 == 0) goto L_0x01f6;
    L_0x0089:
        r4 = 1048576; // 0x100000 float:1.469368E-39 double:5.180654E-318;
        r4 = r4 & r34;
        if (r4 != 0) goto L_0x01f6;
    L_0x008f:
        if (r69 == 0) goto L_0x01f6;
    L_0x0091:
        r4 = r69.getAction();
        if (r4 == 0) goto L_0x01f6;
    L_0x0097:
        if (r71 != 0) goto L_0x01f6;
    L_0x0099:
        r4 = "android.intent.action.SEND";
        r12 = r69.getAction();
        r4 = r4.equals(r12);
        if (r4 == 0) goto L_0x0453;
    L_0x00a5:
        r33 = 0;
        r62 = r69.getType();
        if (r62 == 0) goto L_0x02d4;
    L_0x00ad:
        r4 = "text/x-vcard";
        r0 = r62;
        r4 = r0.equals(r4);
        if (r4 == 0) goto L_0x02d4;
    L_0x00b7:
        r4 = r69.getExtras();	 Catch:{ Exception -> 0x01de }
        r12 = "android.intent.extra.STREAM";
        r63 = r4.get(r12);	 Catch:{ Exception -> 0x01de }
        r63 = (android.net.Uri) r63;	 Catch:{ Exception -> 0x01de }
        if (r63 == 0) goto L_0x02d0;
    L_0x00c5:
        r25 = r68.getContentResolver();	 Catch:{ Exception -> 0x01de }
        r0 = r25;
        r1 = r63;
        r59 = r0.openInputStream(r1);	 Catch:{ Exception -> 0x01de }
        r40 = 0;
        r42 = 0;
        r41 = 0;
        r50 = new java.util.ArrayList;	 Catch:{ Exception -> 0x01de }
        r50.<init>();	 Catch:{ Exception -> 0x01de }
        r22 = new java.io.BufferedReader;	 Catch:{ Exception -> 0x01de }
        r4 = new java.io.InputStreamReader;	 Catch:{ Exception -> 0x01de }
        r12 = "UTF-8";
        r0 = r59;
        r4.<init>(r0, r12);	 Catch:{ Exception -> 0x01de }
        r0 = r22;
        r0.<init>(r4);	 Catch:{ Exception -> 0x01de }
    L_0x00ec:
        r39 = r22.readLine();	 Catch:{ Exception -> 0x01de }
        if (r39 == 0) goto L_0x027a;
    L_0x00f2:
        r4 = ":";
        r0 = r39;
        r19 = r0.split(r4);	 Catch:{ Exception -> 0x01de }
        r0 = r19;
        r4 = r0.length;	 Catch:{ Exception -> 0x01de }
        r12 = 2;
        if (r4 != r12) goto L_0x00ec;
    L_0x0100:
        r4 = 0;
        r4 = r19[r4];	 Catch:{ Exception -> 0x01de }
        r12 = "FN";
        r4 = r4.startsWith(r12);	 Catch:{ Exception -> 0x01de }
        if (r4 == 0) goto L_0x01bc;
    L_0x010b:
        r4 = 0;
        r4 = r19[r4];	 Catch:{ Exception -> 0x01de }
        r12 = ";";
        r46 = r4.split(r12);	 Catch:{ Exception -> 0x01de }
        r21 = r46;
        r0 = r21;
        r0 = r0.length;	 Catch:{ Exception -> 0x01de }
        r38 = r0;
        r37 = 0;
    L_0x011d:
        r0 = r37;
        r1 = r38;
        if (r0 >= r1) goto L_0x0158;
    L_0x0123:
        r45 = r21[r37];	 Catch:{ Exception -> 0x01de }
        r4 = "=";
        r0 = r45;
        r20 = r0.split(r4);	 Catch:{ Exception -> 0x01de }
        r0 = r20;
        r4 = r0.length;	 Catch:{ Exception -> 0x01de }
        r12 = 2;
        if (r4 == r12) goto L_0x013a;
    L_0x0133:
        r37 = r37 + 1;
        goto L_0x011d;
    L_0x0136:
        r30 = 0;
        goto L_0x0057;
    L_0x013a:
        r4 = 0;
        r4 = r20[r4];	 Catch:{ Exception -> 0x01de }
        r12 = "CHARSET";
        r4 = r4.equals(r12);	 Catch:{ Exception -> 0x01de }
        if (r4 == 0) goto L_0x0149;
    L_0x0145:
        r4 = 1;
        r41 = r20[r4];	 Catch:{ Exception -> 0x01de }
        goto L_0x0133;
    L_0x0149:
        r4 = 0;
        r4 = r20[r4];	 Catch:{ Exception -> 0x01de }
        r12 = "ENCODING";
        r4 = r4.equals(r12);	 Catch:{ Exception -> 0x01de }
        if (r4 == 0) goto L_0x0133;
    L_0x0154:
        r4 = 1;
        r42 = r20[r4];	 Catch:{ Exception -> 0x01de }
        goto L_0x0133;
    L_0x0158:
        r4 = 1;
        r40 = r19[r4];	 Catch:{ Exception -> 0x01de }
        if (r42 == 0) goto L_0x00ec;
    L_0x015d:
        r4 = "QUOTED-PRINTABLE";
        r0 = r42;
        r4 = r0.equalsIgnoreCase(r4);	 Catch:{ Exception -> 0x01de }
        if (r4 == 0) goto L_0x00ec;
    L_0x0167:
        r4 = "=";
        r0 = r40;
        r4 = r0.endsWith(r4);	 Catch:{ Exception -> 0x01de }
        if (r4 == 0) goto L_0x0186;
    L_0x0171:
        if (r42 == 0) goto L_0x0186;
    L_0x0173:
        r4 = 0;
        r12 = r40.length();	 Catch:{ Exception -> 0x01de }
        r12 = r12 + -1;
        r0 = r40;
        r40 = r0.substring(r4, r12);	 Catch:{ Exception -> 0x01de }
        r39 = r22.readLine();	 Catch:{ Exception -> 0x01de }
        if (r39 != 0) goto L_0x01a6;
    L_0x0186:
        r4 = r40.getBytes();	 Catch:{ Exception -> 0x01de }
        r23 = org.telegram.messenger.AndroidUtilities.decodeQuotedPrintable(r4);	 Catch:{ Exception -> 0x01de }
        if (r23 == 0) goto L_0x00ec;
    L_0x0190:
        r0 = r23;
        r4 = r0.length;	 Catch:{ Exception -> 0x01de }
        if (r4 == 0) goto L_0x00ec;
    L_0x0195:
        r28 = new java.lang.String;	 Catch:{ Exception -> 0x01de }
        r0 = r28;
        r1 = r23;
        r2 = r41;
        r0.<init>(r1, r2);	 Catch:{ Exception -> 0x01de }
        if (r28 == 0) goto L_0x00ec;
    L_0x01a2:
        r40 = r28;
        goto L_0x00ec;
    L_0x01a6:
        r4 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x01de }
        r4.<init>();	 Catch:{ Exception -> 0x01de }
        r0 = r40;
        r4 = r4.append(r0);	 Catch:{ Exception -> 0x01de }
        r0 = r39;
        r4 = r4.append(r0);	 Catch:{ Exception -> 0x01de }
        r40 = r4.toString();	 Catch:{ Exception -> 0x01de }
        goto L_0x0167;
    L_0x01bc:
        r4 = 0;
        r4 = r19[r4];	 Catch:{ Exception -> 0x01de }
        r12 = "TEL";
        r4 = r4.startsWith(r12);	 Catch:{ Exception -> 0x01de }
        if (r4 == 0) goto L_0x00ec;
    L_0x01c7:
        r4 = 1;
        r4 = r19[r4];	 Catch:{ Exception -> 0x01de }
        r12 = 1;
        r49 = org.telegram.PhoneFormat.PhoneFormat.stripExceptNumbers(r4, r12);	 Catch:{ Exception -> 0x01de }
        r4 = r49.length();	 Catch:{ Exception -> 0x01de }
        if (r4 <= 0) goto L_0x00ec;
    L_0x01d5:
        r0 = r50;
        r1 = r49;
        r0.add(r1);	 Catch:{ Exception -> 0x01de }
        goto L_0x00ec;
    L_0x01de:
        r29 = move-exception;
        r4 = "tmessages";
        r0 = r29;
        org.telegram.messenger.FileLog.m611e(r4, r0);
        r33 = 1;
    L_0x01e8:
        if (r33 == 0) goto L_0x01f6;
    L_0x01ea:
        r4 = "Unsupported content";
        r12 = 0;
        r0 = r68;
        r4 = android.widget.Toast.makeText(r0, r4, r12);
        r4.show();
    L_0x01f6:
        r4 = r54.intValue();
        if (r4 == 0) goto L_0x0849;
    L_0x01fc:
        r19 = new android.os.Bundle;
        r19.<init>();
        r4 = "user_id";
        r12 = r54.intValue();
        r0 = r19;
        r0.putInt(r4, r12);
        r35 = new org.telegram.ui.ChatActivity;
        r0 = r35;
        r1 = r19;
        r0.<init>(r1);
        r0 = r68;
        r4 = r0.actionBarLayout;
        r12 = 0;
        r13 = 1;
        r14 = 1;
        r0 = r35;
        r4 = r4.presentFragment(r0, r12, r13, r14);
        if (r4 == 0) goto L_0x0226;
    L_0x0224:
        r51 = 1;
    L_0x0226:
        if (r51 != 0) goto L_0x0272;
    L_0x0228:
        if (r70 != 0) goto L_0x0272;
    L_0x022a:
        r4 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r4 == 0) goto L_0x0b57;
    L_0x0230:
        r4 = org.telegram.messenger.UserConfig.isClientActivated();
        if (r4 != 0) goto L_0x0b33;
    L_0x0236:
        r0 = r68;
        r4 = r0.layersActionBarLayout;
        r4 = r4.fragmentsStack;
        r4 = r4.isEmpty();
        if (r4 == 0) goto L_0x0257;
    L_0x0242:
        r0 = r68;
        r4 = r0.layersActionBarLayout;
        r12 = new org.telegram.ui.LoginActivity;
        r12.<init>();
        r4.addFragmentToStack(r12);
        r0 = r68;
        r4 = r0.drawerLayoutContainer;
        r12 = 0;
        r13 = 0;
        r4.setAllowOpenDrawer(r12, r13);
    L_0x0257:
        r0 = r68;
        r4 = r0.actionBarLayout;
        r4.showLastFragment();
        r4 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r4 == 0) goto L_0x0272;
    L_0x0264:
        r0 = r68;
        r4 = r0.layersActionBarLayout;
        r4.showLastFragment();
        r0 = r68;
        r4 = r0.rightActionBarLayout;
        r4.showLastFragment();
    L_0x0272:
        r4 = 0;
        r0 = r69;
        r0.setAction(r4);
        goto L_0x002c;
    L_0x027a:
        r22.close();	 Catch:{ Exception -> 0x02c7 }
        r59.close();	 Catch:{ Exception -> 0x02c7 }
    L_0x0280:
        if (r40 == 0) goto L_0x01e8;
    L_0x0282:
        r4 = r50.isEmpty();	 Catch:{ Exception -> 0x01de }
        if (r4 != 0) goto L_0x01e8;
    L_0x0288:
        r4 = new java.util.ArrayList;	 Catch:{ Exception -> 0x01de }
        r4.<init>();	 Catch:{ Exception -> 0x01de }
        r0 = r68;
        r0.contactsToSend = r4;	 Catch:{ Exception -> 0x01de }
        r37 = r50.iterator();	 Catch:{ Exception -> 0x01de }
    L_0x0295:
        r4 = r37.hasNext();	 Catch:{ Exception -> 0x01de }
        if (r4 == 0) goto L_0x01e8;
    L_0x029b:
        r49 = r37.next();	 Catch:{ Exception -> 0x01de }
        r49 = (java.lang.String) r49;	 Catch:{ Exception -> 0x01de }
        r66 = new org.telegram.tgnet.TLRPC$TL_userContact_old2;	 Catch:{ Exception -> 0x01de }
        r66.<init>();	 Catch:{ Exception -> 0x01de }
        r0 = r49;
        r1 = r66;
        r1.phone = r0;	 Catch:{ Exception -> 0x01de }
        r0 = r40;
        r1 = r66;
        r1.first_name = r0;	 Catch:{ Exception -> 0x01de }
        r4 = "";
        r0 = r66;
        r0.last_name = r4;	 Catch:{ Exception -> 0x01de }
        r4 = 0;
        r0 = r66;
        r0.id = r4;	 Catch:{ Exception -> 0x01de }
        r0 = r68;
        r4 = r0.contactsToSend;	 Catch:{ Exception -> 0x01de }
        r0 = r66;
        r4.add(r0);	 Catch:{ Exception -> 0x01de }
        goto L_0x0295;
    L_0x02c7:
        r29 = move-exception;
        r4 = "tmessages";
        r0 = r29;
        org.telegram.messenger.FileLog.m611e(r4, r0);	 Catch:{ Exception -> 0x01de }
        goto L_0x0280;
    L_0x02d0:
        r33 = 1;
        goto L_0x01e8;
    L_0x02d4:
        if (r62 == 0) goto L_0x0361;
    L_0x02d6:
        r4 = "text/plain";
        r0 = r62;
        r4 = r0.equals(r4);
        if (r4 != 0) goto L_0x02ea;
    L_0x02e0:
        r4 = "message/rfc822";
        r0 = r62;
        r4 = r0.equals(r4);
        if (r4 == 0) goto L_0x0361;
    L_0x02ea:
        r4 = "android.intent.extra.TEXT";
        r0 = r69;
        r4 = r0.getStringExtra(r4);
        if (r4 != 0) goto L_0x02fe;
    L_0x02f4:
        r4 = "android.intent.extra.TEXT";
        r0 = r69;
        r4 = r0.getCharSequenceExtra(r4);
        if (r4 == 0) goto L_0x0361;
    L_0x02fe:
        r4 = "android.intent.extra.TEXT";
        r0 = r69;
        r61 = r0.getStringExtra(r4);
        if (r61 != 0) goto L_0x0314;
    L_0x0308:
        r4 = "android.intent.extra.TEXT";
        r0 = r69;
        r4 = r0.getCharSequenceExtra(r4);
        r61 = r4.toString();
    L_0x0314:
        r4 = "android.intent.extra.SUBJECT";
        r0 = r69;
        r60 = r0.getStringExtra(r4);
        if (r61 == 0) goto L_0x03cc;
    L_0x031e:
        r4 = r61.length();
        if (r4 == 0) goto L_0x03cc;
    L_0x0324:
        r4 = "http://";
        r0 = r61;
        r4 = r0.startsWith(r4);
        if (r4 != 0) goto L_0x0338;
    L_0x032e:
        r4 = "https://";
        r0 = r61;
        r4 = r0.startsWith(r4);
        if (r4 == 0) goto L_0x035b;
    L_0x0338:
        if (r60 == 0) goto L_0x035b;
    L_0x033a:
        r4 = r60.length();
        if (r4 == 0) goto L_0x035b;
    L_0x0340:
        r4 = new java.lang.StringBuilder;
        r4.<init>();
        r0 = r60;
        r4 = r4.append(r0);
        r12 = "\n";
        r4 = r4.append(r12);
        r0 = r61;
        r4 = r4.append(r0);
        r61 = r4.toString();
    L_0x035b:
        r0 = r61;
        r1 = r68;
        r1.sendingText = r0;
    L_0x0361:
        r4 = "android.intent.extra.STREAM";
        r0 = r69;
        r47 = r0.getParcelableExtra(r4);
        if (r47 == 0) goto L_0x0449;
    L_0x036b:
        r0 = r47;
        r4 = r0 instanceof android.net.Uri;
        if (r4 != 0) goto L_0x0379;
    L_0x0371:
        r4 = r47.toString();
        r47 = android.net.Uri.parse(r4);
    L_0x0379:
        r63 = r47;
        r63 = (android.net.Uri) r63;
        if (r63 == 0) goto L_0x03cf;
    L_0x037f:
        if (r62 == 0) goto L_0x038b;
    L_0x0381:
        r4 = "image/";
        r0 = r62;
        r4 = r0.startsWith(r4);
        if (r4 != 0) goto L_0x039b;
    L_0x038b:
        r4 = r63.toString();
        r4 = r4.toLowerCase();
        r12 = ".jpg";
        r4 = r4.endsWith(r12);
        if (r4 == 0) goto L_0x03cf;
    L_0x039b:
        r0 = r68;
        r4 = r0.photoPathsArray;
        if (r4 != 0) goto L_0x03aa;
    L_0x03a1:
        r4 = new java.util.ArrayList;
        r4.<init>();
        r0 = r68;
        r0.photoPathsArray = r4;
    L_0x03aa:
        r0 = r68;
        r4 = r0.photoPathsArray;
        r0 = r63;
        r4.add(r0);
    L_0x03b3:
        r0 = r68;
        r4 = r0.sendingText;
        if (r4 == 0) goto L_0x01e8;
    L_0x03b9:
        r0 = r68;
        r4 = r0.sendingText;
        r12 = "WhatsApp";
        r4 = r4.contains(r12);
        if (r4 == 0) goto L_0x01e8;
    L_0x03c5:
        r4 = 0;
        r0 = r68;
        r0.sendingText = r4;
        goto L_0x01e8;
    L_0x03cc:
        r33 = 1;
        goto L_0x0361;
    L_0x03cf:
        r48 = org.telegram.messenger.AndroidUtilities.getPath(r63);
        if (r48 == 0) goto L_0x0429;
    L_0x03d5:
        r4 = "file:";
        r0 = r48;
        r4 = r0.startsWith(r4);
        if (r4 == 0) goto L_0x03e9;
    L_0x03df:
        r4 = "file://";
        r12 = "";
        r0 = r48;
        r48 = r0.replace(r4, r12);
    L_0x03e9:
        if (r62 == 0) goto L_0x03fc;
    L_0x03eb:
        r4 = "video/";
        r0 = r62;
        r4 = r0.startsWith(r4);
        if (r4 == 0) goto L_0x03fc;
    L_0x03f5:
        r0 = r48;
        r1 = r68;
        r1.videoPath = r0;
        goto L_0x03b3;
    L_0x03fc:
        r0 = r68;
        r4 = r0.documentsPathsArray;
        if (r4 != 0) goto L_0x0414;
    L_0x0402:
        r4 = new java.util.ArrayList;
        r4.<init>();
        r0 = r68;
        r0.documentsPathsArray = r4;
        r4 = new java.util.ArrayList;
        r4.<init>();
        r0 = r68;
        r0.documentsOriginalPathsArray = r4;
    L_0x0414:
        r0 = r68;
        r4 = r0.documentsPathsArray;
        r0 = r48;
        r4.add(r0);
        r0 = r68;
        r4 = r0.documentsOriginalPathsArray;
        r12 = r63.toString();
        r4.add(r12);
        goto L_0x03b3;
    L_0x0429:
        r0 = r68;
        r4 = r0.documentsUrisArray;
        if (r4 != 0) goto L_0x0438;
    L_0x042f:
        r4 = new java.util.ArrayList;
        r4.<init>();
        r0 = r68;
        r0.documentsUrisArray = r4;
    L_0x0438:
        r0 = r68;
        r4 = r0.documentsUrisArray;
        r0 = r63;
        r4.add(r0);
        r0 = r62;
        r1 = r68;
        r1.documentsMimeType = r0;
        goto L_0x03b3;
    L_0x0449:
        r0 = r68;
        r4 = r0.sendingText;
        if (r4 != 0) goto L_0x01e8;
    L_0x044f:
        r33 = 1;
        goto L_0x01e8;
    L_0x0453:
        r4 = r69.getAction();
        r12 = "android.intent.action.SEND_MULTIPLE";
        r4 = r4.equals(r12);
        if (r4 == 0) goto L_0x0546;
    L_0x045f:
        r33 = 0;
        r4 = "android.intent.extra.STREAM";
        r0 = r69;
        r64 = r0.getParcelableArrayListExtra(r4);	 Catch:{ Exception -> 0x04b8 }
        r62 = r69.getType();	 Catch:{ Exception -> 0x04b8 }
        if (r64 == 0) goto L_0x0542;
    L_0x046f:
        if (r62 == 0) goto L_0x04d2;
    L_0x0471:
        r4 = "image/";
        r0 = r62;
        r4 = r0.startsWith(r4);	 Catch:{ Exception -> 0x04b8 }
        if (r4 == 0) goto L_0x04d2;
    L_0x047b:
        r37 = r64.iterator();	 Catch:{ Exception -> 0x04b8 }
    L_0x047f:
        r4 = r37.hasNext();	 Catch:{ Exception -> 0x04b8 }
        if (r4 == 0) goto L_0x04c2;
    L_0x0485:
        r47 = r37.next();	 Catch:{ Exception -> 0x04b8 }
        r47 = (android.os.Parcelable) r47;	 Catch:{ Exception -> 0x04b8 }
        r0 = r47;
        r4 = r0 instanceof android.net.Uri;	 Catch:{ Exception -> 0x04b8 }
        if (r4 != 0) goto L_0x0499;
    L_0x0491:
        r4 = r47.toString();	 Catch:{ Exception -> 0x04b8 }
        r47 = android.net.Uri.parse(r4);	 Catch:{ Exception -> 0x04b8 }
    L_0x0499:
        r0 = r47;
        r0 = (android.net.Uri) r0;	 Catch:{ Exception -> 0x04b8 }
        r63 = r0;
        r0 = r68;
        r4 = r0.photoPathsArray;	 Catch:{ Exception -> 0x04b8 }
        if (r4 != 0) goto L_0x04ae;
    L_0x04a5:
        r4 = new java.util.ArrayList;	 Catch:{ Exception -> 0x04b8 }
        r4.<init>();	 Catch:{ Exception -> 0x04b8 }
        r0 = r68;
        r0.photoPathsArray = r4;	 Catch:{ Exception -> 0x04b8 }
    L_0x04ae:
        r0 = r68;
        r4 = r0.photoPathsArray;	 Catch:{ Exception -> 0x04b8 }
        r0 = r63;
        r4.add(r0);	 Catch:{ Exception -> 0x04b8 }
        goto L_0x047f;
    L_0x04b8:
        r29 = move-exception;
        r4 = "tmessages";
        r0 = r29;
        org.telegram.messenger.FileLog.m611e(r4, r0);
        r33 = 1;
    L_0x04c2:
        if (r33 == 0) goto L_0x01f6;
    L_0x04c4:
        r4 = "Unsupported content";
        r12 = 0;
        r0 = r68;
        r4 = android.widget.Toast.makeText(r0, r4, r12);
        r4.show();
        goto L_0x01f6;
    L_0x04d2:
        r37 = r64.iterator();	 Catch:{ Exception -> 0x04b8 }
    L_0x04d6:
        r4 = r37.hasNext();	 Catch:{ Exception -> 0x04b8 }
        if (r4 == 0) goto L_0x04c2;
    L_0x04dc:
        r47 = r37.next();	 Catch:{ Exception -> 0x04b8 }
        r47 = (android.os.Parcelable) r47;	 Catch:{ Exception -> 0x04b8 }
        r0 = r47;
        r4 = r0 instanceof android.net.Uri;	 Catch:{ Exception -> 0x04b8 }
        if (r4 != 0) goto L_0x04f0;
    L_0x04e8:
        r4 = r47.toString();	 Catch:{ Exception -> 0x04b8 }
        r47 = android.net.Uri.parse(r4);	 Catch:{ Exception -> 0x04b8 }
    L_0x04f0:
        r0 = r47;
        r0 = (android.net.Uri) r0;	 Catch:{ Exception -> 0x04b8 }
        r4 = r0;
        r48 = org.telegram.messenger.AndroidUtilities.getPath(r4);	 Catch:{ Exception -> 0x04b8 }
        r44 = r47.toString();	 Catch:{ Exception -> 0x04b8 }
        if (r44 != 0) goto L_0x0501;
    L_0x04ff:
        r44 = r48;
    L_0x0501:
        if (r48 == 0) goto L_0x04d6;
    L_0x0503:
        r4 = "file:";
        r0 = r48;
        r4 = r0.startsWith(r4);	 Catch:{ Exception -> 0x04b8 }
        if (r4 == 0) goto L_0x0517;
    L_0x050d:
        r4 = "file://";
        r12 = "";
        r0 = r48;
        r48 = r0.replace(r4, r12);	 Catch:{ Exception -> 0x04b8 }
    L_0x0517:
        r0 = r68;
        r4 = r0.documentsPathsArray;	 Catch:{ Exception -> 0x04b8 }
        if (r4 != 0) goto L_0x052f;
    L_0x051d:
        r4 = new java.util.ArrayList;	 Catch:{ Exception -> 0x04b8 }
        r4.<init>();	 Catch:{ Exception -> 0x04b8 }
        r0 = r68;
        r0.documentsPathsArray = r4;	 Catch:{ Exception -> 0x04b8 }
        r4 = new java.util.ArrayList;	 Catch:{ Exception -> 0x04b8 }
        r4.<init>();	 Catch:{ Exception -> 0x04b8 }
        r0 = r68;
        r0.documentsOriginalPathsArray = r4;	 Catch:{ Exception -> 0x04b8 }
    L_0x052f:
        r0 = r68;
        r4 = r0.documentsPathsArray;	 Catch:{ Exception -> 0x04b8 }
        r0 = r48;
        r4.add(r0);	 Catch:{ Exception -> 0x04b8 }
        r0 = r68;
        r4 = r0.documentsOriginalPathsArray;	 Catch:{ Exception -> 0x04b8 }
        r0 = r44;
        r4.add(r0);	 Catch:{ Exception -> 0x04b8 }
        goto L_0x04d6;
    L_0x0542:
        r33 = 1;
        goto L_0x04c2;
    L_0x0546:
        r4 = "android.intent.action.VIEW";
        r12 = r69.getAction();
        r4 = r4.equals(r12);
        if (r4 == 0) goto L_0x07bf;
    L_0x0552:
        r27 = r69.getData();
        if (r27 == 0) goto L_0x01f6;
    L_0x0558:
        r5 = 0;
        r6 = 0;
        r7 = 0;
        r8 = 0;
        r9 = 0;
        r10 = 0;
        r11 = 0;
        r56 = r27.getScheme();
        if (r56 == 0) goto L_0x05b3;
    L_0x0565:
        r4 = "http";
        r0 = r56;
        r4 = r0.equals(r4);
        if (r4 != 0) goto L_0x0579;
    L_0x056f:
        r4 = "https";
        r0 = r56;
        r4 = r0.equals(r4);
        if (r4 == 0) goto L_0x0649;
    L_0x0579:
        r4 = r27.getHost();
        r36 = r4.toLowerCase();
        r4 = "telegram.me";
        r0 = r36;
        r4 = r0.equals(r4);
        if (r4 == 0) goto L_0x05b3;
    L_0x058b:
        r48 = r27.getPath();
        if (r48 == 0) goto L_0x05b3;
    L_0x0591:
        r4 = r48.length();
        r12 = 1;
        if (r4 <= r12) goto L_0x05b3;
    L_0x0598:
        r4 = 1;
        r0 = r48;
        r48 = r0.substring(r4);
        r4 = "joinchat/";
        r0 = r48;
        r4 = r0.startsWith(r4);
        if (r4 == 0) goto L_0x05c3;
    L_0x05a9:
        r4 = "joinchat/";
        r12 = "";
        r0 = r48;
        r6 = r0.replace(r4, r12);
    L_0x05b3:
        if (r5 != 0) goto L_0x05bb;
    L_0x05b5:
        if (r6 != 0) goto L_0x05bb;
    L_0x05b7:
        if (r7 != 0) goto L_0x05bb;
    L_0x05b9:
        if (r10 == 0) goto L_0x0778;
    L_0x05bb:
        r12 = 0;
        r4 = r68;
        r4.runLinkRequest(r5, r6, r7, r8, r9, r10, r11, r12);
        goto L_0x01f6;
    L_0x05c3:
        r4 = "addstickers/";
        r0 = r48;
        r4 = r0.startsWith(r4);
        if (r4 == 0) goto L_0x05d8;
    L_0x05cd:
        r4 = "addstickers/";
        r12 = "";
        r0 = r48;
        r7 = r0.replace(r4, r12);
        goto L_0x05b3;
    L_0x05d8:
        r4 = "msg/";
        r0 = r48;
        r4 = r0.startsWith(r4);
        if (r4 == 0) goto L_0x062c;
    L_0x05e2:
        r4 = "url";
        r0 = r27;
        r10 = r0.getQueryParameter(r4);
        if (r10 != 0) goto L_0x05ee;
    L_0x05ec:
        r10 = "";
    L_0x05ee:
        r4 = "text";
        r0 = r27;
        r4 = r0.getQueryParameter(r4);
        if (r4 == 0) goto L_0x05b3;
    L_0x05f8:
        r4 = r10.length();
        if (r4 <= 0) goto L_0x0612;
    L_0x05fe:
        r11 = 1;
        r4 = new java.lang.StringBuilder;
        r4.<init>();
        r4 = r4.append(r10);
        r12 = "\n";
        r4 = r4.append(r12);
        r10 = r4.toString();
    L_0x0612:
        r4 = new java.lang.StringBuilder;
        r4.<init>();
        r4 = r4.append(r10);
        r12 = "text";
        r0 = r27;
        r12 = r0.getQueryParameter(r12);
        r4 = r4.append(r12);
        r10 = r4.toString();
        goto L_0x05b3;
    L_0x062c:
        r4 = r48.length();
        r12 = 5;
        if (r4 < r12) goto L_0x05b3;
    L_0x0633:
        r5 = r27.getLastPathSegment();
        r4 = "start";
        r0 = r27;
        r8 = r0.getQueryParameter(r4);
        r4 = "startgroup";
        r0 = r27;
        r9 = r0.getQueryParameter(r4);
        goto L_0x05b3;
    L_0x0649:
        r4 = "tg";
        r0 = r56;
        r4 = r0.equals(r4);
        if (r4 == 0) goto L_0x05b3;
    L_0x0653:
        r65 = r27.toString();
        r4 = "tg:resolve";
        r0 = r65;
        r4 = r0.startsWith(r4);
        if (r4 != 0) goto L_0x066b;
    L_0x0661:
        r4 = "tg://resolve";
        r0 = r65;
        r4 = r0.startsWith(r4);
        if (r4 == 0) goto L_0x069b;
    L_0x066b:
        r4 = "tg:resolve";
        r12 = "tg://telegram.org";
        r0 = r65;
        r4 = r0.replace(r4, r12);
        r12 = "tg://resolve";
        r13 = "tg://telegram.org";
        r65 = r4.replace(r12, r13);
        r27 = android.net.Uri.parse(r65);
        r4 = "domain";
        r0 = r27;
        r5 = r0.getQueryParameter(r4);
        r4 = "start";
        r0 = r27;
        r8 = r0.getQueryParameter(r4);
        r4 = "startgroup";
        r0 = r27;
        r9 = r0.getQueryParameter(r4);
        goto L_0x05b3;
    L_0x069b:
        r4 = "tg:join";
        r0 = r65;
        r4 = r0.startsWith(r4);
        if (r4 != 0) goto L_0x06af;
    L_0x06a5:
        r4 = "tg://join";
        r0 = r65;
        r4 = r0.startsWith(r4);
        if (r4 == 0) goto L_0x06cf;
    L_0x06af:
        r4 = "tg:join";
        r12 = "tg://telegram.org";
        r0 = r65;
        r4 = r0.replace(r4, r12);
        r12 = "tg://join";
        r13 = "tg://telegram.org";
        r65 = r4.replace(r12, r13);
        r27 = android.net.Uri.parse(r65);
        r4 = "invite";
        r0 = r27;
        r6 = r0.getQueryParameter(r4);
        goto L_0x05b3;
    L_0x06cf:
        r4 = "tg:addstickers";
        r0 = r65;
        r4 = r0.startsWith(r4);
        if (r4 != 0) goto L_0x06e3;
    L_0x06d9:
        r4 = "tg://addstickers";
        r0 = r65;
        r4 = r0.startsWith(r4);
        if (r4 == 0) goto L_0x0703;
    L_0x06e3:
        r4 = "tg:addstickers";
        r12 = "tg://telegram.org";
        r0 = r65;
        r4 = r0.replace(r4, r12);
        r12 = "tg://addstickers";
        r13 = "tg://telegram.org";
        r65 = r4.replace(r12, r13);
        r27 = android.net.Uri.parse(r65);
        r4 = "set";
        r0 = r27;
        r7 = r0.getQueryParameter(r4);
        goto L_0x05b3;
    L_0x0703:
        r4 = "tg:msg";
        r0 = r65;
        r4 = r0.startsWith(r4);
        if (r4 != 0) goto L_0x0717;
    L_0x070d:
        r4 = "tg://msg";
        r0 = r65;
        r4 = r0.startsWith(r4);
        if (r4 == 0) goto L_0x05b3;
    L_0x0717:
        r4 = "tg:msg";
        r12 = "tg://telegram.org";
        r0 = r65;
        r4 = r0.replace(r4, r12);
        r12 = "tg://msg";
        r13 = "tg://telegram.org";
        r65 = r4.replace(r12, r13);
        r27 = android.net.Uri.parse(r65);
        r4 = "url";
        r0 = r27;
        r10 = r0.getQueryParameter(r4);
        if (r10 != 0) goto L_0x0739;
    L_0x0737:
        r10 = "";
    L_0x0739:
        r4 = "text";
        r0 = r27;
        r4 = r0.getQueryParameter(r4);
        if (r4 == 0) goto L_0x05b3;
    L_0x0743:
        r4 = r10.length();
        if (r4 <= 0) goto L_0x075d;
    L_0x0749:
        r11 = 1;
        r4 = new java.lang.StringBuilder;
        r4.<init>();
        r4 = r4.append(r10);
        r12 = "\n";
        r4 = r4.append(r12);
        r10 = r4.toString();
    L_0x075d:
        r4 = new java.lang.StringBuilder;
        r4.<init>();
        r4 = r4.append(r10);
        r12 = "text";
        r0 = r27;
        r12 = r0.getQueryParameter(r12);
        r4 = r4.append(r12);
        r10 = r4.toString();
        goto L_0x05b3;
    L_0x0778:
        r12 = r68.getContentResolver();	 Catch:{ Exception -> 0x07b5 }
        r13 = r69.getData();	 Catch:{ Exception -> 0x07b5 }
        r14 = 0;
        r15 = 0;
        r16 = 0;
        r17 = 0;
        r26 = r12.query(r13, r14, r15, r16, r17);	 Catch:{ Exception -> 0x07b5 }
        if (r26 == 0) goto L_0x01f6;
    L_0x078c:
        r4 = r26.moveToFirst();	 Catch:{ Exception -> 0x07b5 }
        if (r4 == 0) goto L_0x07b0;
    L_0x0792:
        r4 = "DATA4";
        r0 = r26;
        r4 = r0.getColumnIndex(r4);	 Catch:{ Exception -> 0x07b5 }
        r0 = r26;
        r67 = r0.getInt(r4);	 Catch:{ Exception -> 0x07b5 }
        r4 = org.telegram.messenger.NotificationCenter.getInstance();	 Catch:{ Exception -> 0x07b5 }
        r12 = org.telegram.messenger.NotificationCenter.closeChats;	 Catch:{ Exception -> 0x07b5 }
        r13 = 0;
        r13 = new java.lang.Object[r13];	 Catch:{ Exception -> 0x07b5 }
        r4.postNotificationName(r12, r13);	 Catch:{ Exception -> 0x07b5 }
        r54 = java.lang.Integer.valueOf(r67);	 Catch:{ Exception -> 0x07b5 }
    L_0x07b0:
        r26.close();	 Catch:{ Exception -> 0x07b5 }
        goto L_0x01f6;
    L_0x07b5:
        r29 = move-exception;
        r4 = "tmessages";
        r0 = r29;
        org.telegram.messenger.FileLog.m611e(r4, r0);
        goto L_0x01f6;
    L_0x07bf:
        r4 = r69.getAction();
        r12 = "org.telegram.messenger.OPEN_ACCOUNT";
        r4 = r4.equals(r12);
        if (r4 == 0) goto L_0x07d2;
    L_0x07cb:
        r4 = 1;
        r43 = java.lang.Integer.valueOf(r4);
        goto L_0x01f6;
    L_0x07d2:
        r4 = r69.getAction();
        r12 = "com.tmessages.openchat";
        r4 = r4.startsWith(r12);
        if (r4 == 0) goto L_0x0839;
    L_0x07de:
        r4 = "chatId";
        r12 = 0;
        r0 = r69;
        r24 = r0.getIntExtra(r4, r12);
        r4 = "userId";
        r12 = 0;
        r0 = r69;
        r67 = r0.getIntExtra(r4, r12);
        r4 = "encId";
        r12 = 0;
        r0 = r69;
        r32 = r0.getIntExtra(r4, r12);
        if (r24 == 0) goto L_0x080d;
    L_0x07fb:
        r4 = org.telegram.messenger.NotificationCenter.getInstance();
        r12 = org.telegram.messenger.NotificationCenter.closeChats;
        r13 = 0;
        r13 = new java.lang.Object[r13];
        r4.postNotificationName(r12, r13);
        r52 = java.lang.Integer.valueOf(r24);
        goto L_0x01f6;
    L_0x080d:
        if (r67 == 0) goto L_0x0821;
    L_0x080f:
        r4 = org.telegram.messenger.NotificationCenter.getInstance();
        r12 = org.telegram.messenger.NotificationCenter.closeChats;
        r13 = 0;
        r13 = new java.lang.Object[r13];
        r4.postNotificationName(r12, r13);
        r54 = java.lang.Integer.valueOf(r67);
        goto L_0x01f6;
    L_0x0821:
        if (r32 == 0) goto L_0x0835;
    L_0x0823:
        r4 = org.telegram.messenger.NotificationCenter.getInstance();
        r12 = org.telegram.messenger.NotificationCenter.closeChats;
        r13 = 0;
        r13 = new java.lang.Object[r13];
        r4.postNotificationName(r12, r13);
        r53 = java.lang.Integer.valueOf(r32);
        goto L_0x01f6;
    L_0x0835:
        r57 = 1;
        goto L_0x01f6;
    L_0x0839:
        r4 = r69.getAction();
        r12 = "com.tmessages.openplayer";
        r4 = r4.equals(r12);
        if (r4 == 0) goto L_0x01f6;
    L_0x0845:
        r58 = 1;
        goto L_0x01f6;
    L_0x0849:
        r4 = r52.intValue();
        if (r4 == 0) goto L_0x087b;
    L_0x084f:
        r19 = new android.os.Bundle;
        r19.<init>();
        r4 = "chat_id";
        r12 = r52.intValue();
        r0 = r19;
        r0.putInt(r4, r12);
        r35 = new org.telegram.ui.ChatActivity;
        r0 = r35;
        r1 = r19;
        r0.<init>(r1);
        r0 = r68;
        r4 = r0.actionBarLayout;
        r12 = 0;
        r13 = 1;
        r14 = 1;
        r0 = r35;
        r4 = r4.presentFragment(r0, r12, r13, r14);
        if (r4 == 0) goto L_0x0226;
    L_0x0877:
        r51 = 1;
        goto L_0x0226;
    L_0x087b:
        r4 = r53.intValue();
        if (r4 == 0) goto L_0x08ad;
    L_0x0881:
        r19 = new android.os.Bundle;
        r19.<init>();
        r4 = "enc_id";
        r12 = r53.intValue();
        r0 = r19;
        r0.putInt(r4, r12);
        r35 = new org.telegram.ui.ChatActivity;
        r0 = r35;
        r1 = r19;
        r0.<init>(r1);
        r0 = r68;
        r4 = r0.actionBarLayout;
        r12 = 0;
        r13 = 1;
        r14 = 1;
        r0 = r35;
        r4 = r4.presentFragment(r0, r12, r13, r14);
        if (r4 == 0) goto L_0x0226;
    L_0x08a9:
        r51 = 1;
        goto L_0x0226;
    L_0x08ad:
        if (r57 == 0) goto L_0x0900;
    L_0x08af:
        r4 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r4 != 0) goto L_0x08c2;
    L_0x08b5:
        r0 = r68;
        r4 = r0.actionBarLayout;
        r4.removeAllFragments();
    L_0x08bc:
        r51 = 0;
        r70 = 0;
        goto L_0x0226;
    L_0x08c2:
        r0 = r68;
        r4 = r0.layersActionBarLayout;
        r4 = r4.fragmentsStack;
        r4 = r4.isEmpty();
        if (r4 != 0) goto L_0x08bc;
    L_0x08ce:
        r18 = 0;
    L_0x08d0:
        r0 = r68;
        r4 = r0.layersActionBarLayout;
        r4 = r4.fragmentsStack;
        r4 = r4.size();
        r4 = r4 + -1;
        if (r4 <= 0) goto L_0x08f7;
    L_0x08de:
        r0 = r68;
        r12 = r0.layersActionBarLayout;
        r0 = r68;
        r4 = r0.layersActionBarLayout;
        r4 = r4.fragmentsStack;
        r13 = 0;
        r4 = r4.get(r13);
        r4 = (org.telegram.ui.ActionBar.BaseFragment) r4;
        r12.removeFragmentFromStack(r4);
        r18 = r18 + -1;
        r18 = r18 + 1;
        goto L_0x08d0;
    L_0x08f7:
        r0 = r68;
        r4 = r0.layersActionBarLayout;
        r12 = 0;
        r4.closeLastFragment(r12);
        goto L_0x08bc;
    L_0x0900:
        if (r58 == 0) goto L_0x099c;
    L_0x0902:
        r4 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r4 == 0) goto L_0x0962;
    L_0x0908:
        r18 = 0;
    L_0x090a:
        r0 = r68;
        r4 = r0.layersActionBarLayout;
        r4 = r4.fragmentsStack;
        r4 = r4.size();
        r0 = r18;
        if (r0 >= r4) goto L_0x0935;
    L_0x0918:
        r0 = r68;
        r4 = r0.layersActionBarLayout;
        r4 = r4.fragmentsStack;
        r0 = r18;
        r35 = r4.get(r0);
        r35 = (org.telegram.ui.ActionBar.BaseFragment) r35;
        r0 = r35;
        r4 = r0 instanceof org.telegram.ui.AudioPlayerActivity;
        if (r4 == 0) goto L_0x095f;
    L_0x092c:
        r0 = r68;
        r4 = r0.layersActionBarLayout;
        r0 = r35;
        r4.removeFragmentFromStack(r0);
    L_0x0935:
        r0 = r68;
        r4 = r0.actionBarLayout;
        r4.showLastFragment();
        r0 = r68;
        r4 = r0.rightActionBarLayout;
        r4.showLastFragment();
        r0 = r68;
        r4 = r0.drawerLayoutContainer;
        r12 = 0;
        r13 = 0;
        r4.setAllowOpenDrawer(r12, r13);
    L_0x094c:
        r0 = r68;
        r4 = r0.actionBarLayout;
        r12 = new org.telegram.ui.AudioPlayerActivity;
        r12.<init>();
        r13 = 0;
        r14 = 1;
        r15 = 1;
        r4.presentFragment(r12, r13, r14, r15);
        r51 = 1;
        goto L_0x0226;
    L_0x095f:
        r18 = r18 + 1;
        goto L_0x090a;
    L_0x0962:
        r18 = 0;
    L_0x0964:
        r0 = r68;
        r4 = r0.actionBarLayout;
        r4 = r4.fragmentsStack;
        r4 = r4.size();
        r0 = r18;
        if (r0 >= r4) goto L_0x098f;
    L_0x0972:
        r0 = r68;
        r4 = r0.actionBarLayout;
        r4 = r4.fragmentsStack;
        r0 = r18;
        r35 = r4.get(r0);
        r35 = (org.telegram.ui.ActionBar.BaseFragment) r35;
        r0 = r35;
        r4 = r0 instanceof org.telegram.ui.AudioPlayerActivity;
        if (r4 == 0) goto L_0x0999;
    L_0x0986:
        r0 = r68;
        r4 = r0.actionBarLayout;
        r0 = r35;
        r4.removeFragmentFromStack(r0);
    L_0x098f:
        r0 = r68;
        r4 = r0.drawerLayoutContainer;
        r12 = 1;
        r13 = 0;
        r4.setAllowOpenDrawer(r12, r13);
        goto L_0x094c;
    L_0x0999:
        r18 = r18 + 1;
        goto L_0x0964;
    L_0x099c:
        r0 = r68;
        r4 = r0.videoPath;
        if (r4 != 0) goto L_0x09c0;
    L_0x09a2:
        r0 = r68;
        r4 = r0.photoPathsArray;
        if (r4 != 0) goto L_0x09c0;
    L_0x09a8:
        r0 = r68;
        r4 = r0.sendingText;
        if (r4 != 0) goto L_0x09c0;
    L_0x09ae:
        r0 = r68;
        r4 = r0.documentsPathsArray;
        if (r4 != 0) goto L_0x09c0;
    L_0x09b4:
        r0 = r68;
        r4 = r0.contactsToSend;
        if (r4 != 0) goto L_0x09c0;
    L_0x09ba:
        r0 = r68;
        r4 = r0.documentsUrisArray;
        if (r4 == 0) goto L_0x0af3;
    L_0x09c0:
        r4 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r4 != 0) goto L_0x09d2;
    L_0x09c6:
        r4 = org.telegram.messenger.NotificationCenter.getInstance();
        r12 = org.telegram.messenger.NotificationCenter.closeChats;
        r13 = 0;
        r13 = new java.lang.Object[r13];
        r4.postNotificationName(r12, r13);
    L_0x09d2:
        r12 = 0;
        r4 = (r30 > r12 ? 1 : (r30 == r12 ? 0 : -1));
        if (r4 != 0) goto L_0x0ae8;
    L_0x09d8:
        r19 = new android.os.Bundle;
        r19.<init>();
        r4 = "onlySelect";
        r12 = 1;
        r0 = r19;
        r0.putBoolean(r4, r12);
        r0 = r68;
        r4 = r0.contactsToSend;
        if (r4 == 0) goto L_0x0a8a;
    L_0x09eb:
        r4 = "selectAlertString";
        r12 = "SendContactTo";
        r13 = 2131166006; // 0x7f070336 float:1.7946245E38 double:1.052935909E-314;
        r12 = org.telegram.messenger.LocaleController.getString(r12, r13);
        r0 = r19;
        r0.putString(r4, r12);
        r4 = "selectAlertStringGroup";
        r12 = "SendContactToGroup";
        r13 = 2131166002; // 0x7f070332 float:1.7946237E38 double:1.052935907E-314;
        r12 = org.telegram.messenger.LocaleController.getString(r12, r13);
        r0 = r19;
        r0.putString(r4, r12);
    L_0x0a0b:
        r35 = new org.telegram.ui.DialogsActivity;
        r0 = r35;
        r1 = r19;
        r0.<init>(r1);
        r0 = r35;
        r1 = r68;
        r0.setDelegate(r1);
        r4 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r4 == 0) goto L_0x0aaf;
    L_0x0a21:
        r0 = r68;
        r4 = r0.layersActionBarLayout;
        r4 = r4.fragmentsStack;
        r4 = r4.size();
        if (r4 <= 0) goto L_0x0aac;
    L_0x0a2d:
        r0 = r68;
        r4 = r0.layersActionBarLayout;
        r4 = r4.fragmentsStack;
        r0 = r68;
        r12 = r0.layersActionBarLayout;
        r12 = r12.fragmentsStack;
        r12 = r12.size();
        r12 = r12 + -1;
        r4 = r4.get(r12);
        r4 = r4 instanceof org.telegram.ui.DialogsActivity;
        if (r4 == 0) goto L_0x0aac;
    L_0x0a47:
        r55 = 1;
    L_0x0a49:
        r0 = r68;
        r4 = r0.actionBarLayout;
        r12 = 1;
        r13 = 1;
        r0 = r35;
        r1 = r55;
        r4.presentFragment(r0, r1, r12, r13);
        r51 = 1;
        r4 = org.telegram.ui.PhotoViewer.getInstance();
        r4 = r4.isVisible();
        if (r4 == 0) goto L_0x0a6b;
    L_0x0a62:
        r4 = org.telegram.ui.PhotoViewer.getInstance();
        r12 = 0;
        r13 = 1;
        r4.closePhoto(r12, r13);
    L_0x0a6b:
        r0 = r68;
        r4 = r0.drawerLayoutContainer;
        r12 = 0;
        r13 = 0;
        r4.setAllowOpenDrawer(r12, r13);
        r4 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r4 == 0) goto L_0x0add;
    L_0x0a7a:
        r0 = r68;
        r4 = r0.actionBarLayout;
        r4.showLastFragment();
        r0 = r68;
        r4 = r0.rightActionBarLayout;
        r4.showLastFragment();
        goto L_0x0226;
    L_0x0a8a:
        r4 = "selectAlertString";
        r12 = "SendMessagesTo";
        r13 = 2131166006; // 0x7f070336 float:1.7946245E38 double:1.052935909E-314;
        r12 = org.telegram.messenger.LocaleController.getString(r12, r13);
        r0 = r19;
        r0.putString(r4, r12);
        r4 = "selectAlertStringGroup";
        r12 = "SendMessagesToGroup";
        r13 = 2131166007; // 0x7f070337 float:1.7946247E38 double:1.0529359096E-314;
        r12 = org.telegram.messenger.LocaleController.getString(r12, r13);
        r0 = r19;
        r0.putString(r4, r12);
        goto L_0x0a0b;
    L_0x0aac:
        r55 = 0;
        goto L_0x0a49;
    L_0x0aaf:
        r0 = r68;
        r4 = r0.actionBarLayout;
        r4 = r4.fragmentsStack;
        r4 = r4.size();
        r12 = 1;
        if (r4 <= r12) goto L_0x0ada;
    L_0x0abc:
        r0 = r68;
        r4 = r0.actionBarLayout;
        r4 = r4.fragmentsStack;
        r0 = r68;
        r12 = r0.actionBarLayout;
        r12 = r12.fragmentsStack;
        r12 = r12.size();
        r12 = r12 + -1;
        r4 = r4.get(r12);
        r4 = r4 instanceof org.telegram.ui.DialogsActivity;
        if (r4 == 0) goto L_0x0ada;
    L_0x0ad6:
        r55 = 1;
    L_0x0ad8:
        goto L_0x0a49;
    L_0x0ada:
        r55 = 0;
        goto L_0x0ad8;
    L_0x0add:
        r0 = r68;
        r4 = r0.drawerLayoutContainer;
        r12 = 1;
        r13 = 0;
        r4.setAllowOpenDrawer(r12, r13);
        goto L_0x0226;
    L_0x0ae8:
        r4 = 0;
        r12 = 0;
        r0 = r68;
        r1 = r30;
        r0.didSelectDialog(r4, r1, r12);
        goto L_0x0226;
    L_0x0af3:
        r4 = r43.intValue();
        if (r4 == 0) goto L_0x0226;
    L_0x0af9:
        r0 = r68;
        r4 = r0.actionBarLayout;
        r12 = new org.telegram.ui.SettingsActivity;
        r12.<init>();
        r13 = 0;
        r14 = 1;
        r15 = 1;
        r4.presentFragment(r12, r13, r14, r15);
        r4 = org.telegram.messenger.AndroidUtilities.isTablet();
        if (r4 == 0) goto L_0x0b29;
    L_0x0b0e:
        r0 = r68;
        r4 = r0.actionBarLayout;
        r4.showLastFragment();
        r0 = r68;
        r4 = r0.rightActionBarLayout;
        r4.showLastFragment();
        r0 = r68;
        r4 = r0.drawerLayoutContainer;
        r12 = 0;
        r13 = 0;
        r4.setAllowOpenDrawer(r12, r13);
    L_0x0b25:
        r51 = 1;
        goto L_0x0226;
    L_0x0b29:
        r0 = r68;
        r4 = r0.drawerLayoutContainer;
        r12 = 1;
        r13 = 0;
        r4.setAllowOpenDrawer(r12, r13);
        goto L_0x0b25;
    L_0x0b33:
        r0 = r68;
        r4 = r0.actionBarLayout;
        r4 = r4.fragmentsStack;
        r4 = r4.isEmpty();
        if (r4 == 0) goto L_0x0257;
    L_0x0b3f:
        r0 = r68;
        r4 = r0.actionBarLayout;
        r12 = new org.telegram.ui.DialogsActivity;
        r13 = 0;
        r12.<init>(r13);
        r4.addFragmentToStack(r12);
        r0 = r68;
        r4 = r0.drawerLayoutContainer;
        r12 = 1;
        r13 = 0;
        r4.setAllowOpenDrawer(r12, r13);
        goto L_0x0257;
    L_0x0b57:
        r0 = r68;
        r4 = r0.actionBarLayout;
        r4 = r4.fragmentsStack;
        r4 = r4.isEmpty();
        if (r4 == 0) goto L_0x0257;
    L_0x0b63:
        r4 = org.telegram.messenger.UserConfig.isClientActivated();
        if (r4 != 0) goto L_0x0b80;
    L_0x0b69:
        r0 = r68;
        r4 = r0.actionBarLayout;
        r12 = new org.telegram.ui.LoginActivity;
        r12.<init>();
        r4.addFragmentToStack(r12);
        r0 = r68;
        r4 = r0.drawerLayoutContainer;
        r12 = 0;
        r13 = 0;
        r4.setAllowOpenDrawer(r12, r13);
        goto L_0x0257;
    L_0x0b80:
        r0 = r68;
        r4 = r0.actionBarLayout;
        r12 = new org.telegram.ui.DialogsActivity;
        r13 = 0;
        r12.<init>(r13);
        r4.addFragmentToStack(r12);
        r0 = r68;
        r4 = r0.drawerLayoutContainer;
        r12 = 1;
        r13 = 0;
        r4.setAllowOpenDrawer(r12, r13);
        goto L_0x0257;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.LaunchActivity.handleIntent(android.content.Intent, boolean, boolean, boolean):boolean");
    }

    private void runLinkRequest(String username, String group, String sticker, String botUser, String botChat, String message, boolean hasUrl, int state) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(LocaleController.getString("Loading", C0553R.string.Loading));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        int requestId = 0;
        final String str;
        if (username != null) {
            TL_contacts_resolveUsername req = new TL_contacts_resolveUsername();
            req.username = username;
            str = botChat;
            final String str2 = botUser;
            requestId = ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                public void run(final TLObject response, final TL_error error) {
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        public void run() {
                            if (!LaunchActivity.this.isFinishing()) {
                                try {
                                    progressDialog.dismiss();
                                } catch (Throwable e) {
                                    FileLog.m611e("tmessages", e);
                                }
                                if (error != null || LaunchActivity.this.actionBarLayout == null) {
                                    try {
                                        Toast.makeText(LaunchActivity.this, LocaleController.getString("NoUsernameFound", C0553R.string.NoUsernameFound), 0).show();
                                        return;
                                    } catch (Throwable e2) {
                                        FileLog.m611e("tmessages", e2);
                                        return;
                                    }
                                }
                                TL_contacts_resolvedPeer res = response;
                                MessagesController.getInstance().putUsers(res.users, false);
                                MessagesController.getInstance().putChats(res.chats, false);
                                MessagesStorage.getInstance().putUsersAndChats(res.users, res.chats, false, true);
                                Bundle args;
                                if (str != null) {
                                    final User user = !res.users.isEmpty() ? (User) res.users.get(0) : null;
                                    if (user == null || (user.bot && user.bot_nochats)) {
                                        try {
                                            Toast.makeText(LaunchActivity.this, LocaleController.getString("BotCantJoinGroups", C0553R.string.BotCantJoinGroups), 0).show();
                                            return;
                                        } catch (Throwable e22) {
                                            FileLog.m611e("tmessages", e22);
                                            return;
                                        }
                                    }
                                    args = new Bundle();
                                    args.putBoolean("onlySelect", true);
                                    args.putInt("dialogsType", 2);
                                    args.putString("addToGroupAlertString", LocaleController.formatString("AddToTheGroupTitle", C0553R.string.AddToTheGroupTitle, UserObject.getUserName(user), "%1$s"));
                                    DialogsActivity fragment = new DialogsActivity(args);
                                    fragment.setDelegate(new MessagesActivityDelegate() {
                                        public void didSelectDialog(DialogsActivity fragment, long did, boolean param) {
                                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats, new Object[0]);
                                            MessagesController.getInstance().addUserToChat(-((int) did), user, null, 0, str, null);
                                            Bundle args = new Bundle();
                                            args.putBoolean("scrollToTopOnResume", true);
                                            args.putInt("chat_id", -((int) did));
                                            LaunchActivity.this.actionBarLayout.presentFragment(new ChatActivity(args), true, false, true);
                                        }
                                    });
                                    LaunchActivity.this.presentFragment(fragment);
                                    return;
                                }
                                args = new Bundle();
                                if (res.chats.isEmpty()) {
                                    args.putInt("user_id", ((User) res.users.get(0)).id);
                                } else {
                                    args.putInt("chat_id", ((Chat) res.chats.get(0)).id);
                                }
                                if (str2 != null && res.users.size() > 0 && ((User) res.users.get(0)).bot) {
                                    args.putString("botUser", str2);
                                }
                                ChatActivity fragment2 = new ChatActivity(args);
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats, new Object[0]);
                                LaunchActivity.this.actionBarLayout.presentFragment(fragment2, false, true, true);
                            }
                        }
                    });
                }
            });
        } else if (group != null) {
            if (state == 0) {
                TL_messages_checkChatInvite req2 = new TL_messages_checkChatInvite();
                req2.hash = group;
                final String str3 = username;
                final String str4 = group;
                final String str5 = sticker;
                final String str6 = botUser;
                final String str7 = botChat;
                final String str8 = message;
                final boolean z = hasUrl;
                requestId = ConnectionsManager.getInstance().sendRequest(req2, new RequestDelegate() {
                    public void run(final TLObject response, final TL_error error) {
                        AndroidUtilities.runOnUIThread(new Runnable() {

                            class C10491 implements DialogInterface.OnClickListener {
                                C10491() {
                                }

                                public void onClick(DialogInterface dialogInterface, int i) {
                                    LaunchActivity.this.runLinkRequest(str3, str4, str5, str6, str7, str8, z, 1);
                                }
                            }

                            public void run() {
                                if (!LaunchActivity.this.isFinishing()) {
                                    try {
                                        progressDialog.dismiss();
                                    } catch (Throwable e) {
                                        FileLog.m611e("tmessages", e);
                                    }
                                    Builder builder;
                                    if (error != null || LaunchActivity.this.actionBarLayout == null) {
                                        builder = new Builder(LaunchActivity.this);
                                        builder.setTitle(LocaleController.getString("AppName", C0553R.string.AppName));
                                        if (error.text.startsWith("FLOOD_WAIT")) {
                                            builder.setMessage(LocaleController.getString("FloodWait", C0553R.string.FloodWait));
                                        } else {
                                            builder.setMessage(LocaleController.getString("JoinToGroupErrorNotExist", C0553R.string.JoinToGroupErrorNotExist));
                                        }
                                        builder.setPositiveButton(LocaleController.getString("OK", C0553R.string.OK), null);
                                        LaunchActivity.this.showAlertDialog(builder);
                                        return;
                                    }
                                    ChatInvite invite = response;
                                    if (invite.chat == null || ChatObject.isLeftFromChat(invite.chat)) {
                                        builder = new Builder(LaunchActivity.this);
                                        builder.setTitle(LocaleController.getString("AppName", C0553R.string.AppName));
                                        String str;
                                        Object[] objArr;
                                        if ((invite.megagroup || !invite.channel) && (!ChatObject.isChannel(invite.chat) || invite.chat.megagroup)) {
                                            str = "JoinToGroup";
                                            objArr = new Object[1];
                                            objArr[0] = invite.chat != null ? invite.chat.title : invite.title;
                                            builder.setMessage(LocaleController.formatString(str, C0553R.string.JoinToGroup, objArr));
                                        } else {
                                            str = "ChannelJoinTo";
                                            objArr = new Object[1];
                                            objArr[0] = invite.chat != null ? invite.chat.title : invite.title;
                                            builder.setMessage(LocaleController.formatString(str, C0553R.string.ChannelJoinTo, objArr));
                                        }
                                        builder.setPositiveButton(LocaleController.getString("OK", C0553R.string.OK), new C10491());
                                        builder.setNegativeButton(LocaleController.getString("Cancel", C0553R.string.Cancel), null);
                                        LaunchActivity.this.showAlertDialog(builder);
                                        return;
                                    }
                                    MessagesController.getInstance().putChat(invite.chat, false);
                                    ArrayList<Chat> chats = new ArrayList();
                                    chats.add(invite.chat);
                                    MessagesStorage.getInstance().putUsersAndChats(null, chats, false, true);
                                    Bundle args = new Bundle();
                                    args.putInt("chat_id", invite.chat.id);
                                    ChatActivity fragment = new ChatActivity(args);
                                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats, new Object[0]);
                                    LaunchActivity.this.actionBarLayout.presentFragment(fragment, false, true, true);
                                }
                            }
                        });
                    }
                }, 2);
            } else if (state == 1) {
                TL_messages_importChatInvite req3 = new TL_messages_importChatInvite();
                req3.hash = group;
                ConnectionsManager.getInstance().sendRequest(req3, new RequestDelegate() {
                    public void run(final TLObject response, final TL_error error) {
                        if (error == null) {
                            MessagesController.getInstance().processUpdates((Updates) response, false);
                        }
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            public void run() {
                                if (!LaunchActivity.this.isFinishing()) {
                                    try {
                                        progressDialog.dismiss();
                                    } catch (Throwable e) {
                                        FileLog.m611e("tmessages", e);
                                    }
                                    if (error != null) {
                                        Builder builder = new Builder(LaunchActivity.this);
                                        builder.setTitle(LocaleController.getString("AppName", C0553R.string.AppName));
                                        if (error.text.startsWith("FLOOD_WAIT")) {
                                            builder.setMessage(LocaleController.getString("FloodWait", C0553R.string.FloodWait));
                                        } else if (error.text.equals("USERS_TOO_MUCH")) {
                                            builder.setMessage(LocaleController.getString("JoinToGroupErrorFull", C0553R.string.JoinToGroupErrorFull));
                                        } else {
                                            builder.setMessage(LocaleController.getString("JoinToGroupErrorNotExist", C0553R.string.JoinToGroupErrorNotExist));
                                        }
                                        builder.setPositiveButton(LocaleController.getString("OK", C0553R.string.OK), null);
                                        LaunchActivity.this.showAlertDialog(builder);
                                    } else if (LaunchActivity.this.actionBarLayout != null) {
                                        Updates updates = response;
                                        if (!updates.chats.isEmpty()) {
                                            Chat chat = (Chat) updates.chats.get(0);
                                            chat.left = false;
                                            chat.kicked = false;
                                            MessagesController.getInstance().putUsers(updates.users, false);
                                            MessagesController.getInstance().putChats(updates.chats, false);
                                            Bundle args = new Bundle();
                                            args.putInt("chat_id", chat.id);
                                            ChatActivity fragment = new ChatActivity(args);
                                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats, new Object[0]);
                                            LaunchActivity.this.actionBarLayout.presentFragment(fragment, false, true, true);
                                        }
                                    }
                                }
                            }
                        });
                    }
                }, 2);
            }
        } else if (sticker != null) {
            if (!mainFragmentsStack.isEmpty()) {
                InputStickerSet stickerset = new TL_inputStickerSetShortName();
                stickerset.short_name = sticker;
                StickersQuery.loadStickers((BaseFragment) mainFragmentsStack.get(0), stickerset);
                return;
            }
            return;
        } else if (message != null) {
            Bundle args = new Bundle();
            args.putBoolean("onlySelect", true);
            DialogsActivity fragment = new DialogsActivity(args);
            str = message;
            final boolean z2 = hasUrl;
            fragment.setDelegate(new MessagesActivityDelegate() {
                public void didSelectDialog(DialogsActivity fragment, long did, boolean param) {
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats, new Object[0]);
                    Editor editor = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0).edit();
                    editor.putString("dialog_" + did, str);
                    editor.commit();
                    Bundle args = new Bundle();
                    args.putBoolean("scrollToTopOnResume", true);
                    args.putBoolean("hasUrl", z2);
                    int lower_part = (int) did;
                    int high_id = (int) (did >> 32);
                    if (lower_part == 0) {
                        args.putInt("enc_id", high_id);
                    } else if (high_id == 1) {
                        args.putInt("chat_id", lower_part);
                    } else if (lower_part > 0) {
                        args.putInt("user_id", lower_part);
                    } else if (lower_part < 0) {
                        args.putInt("chat_id", -lower_part);
                    }
                    LaunchActivity.this.actionBarLayout.presentFragment(new ChatActivity(args), true, false, true);
                }
            });
            presentFragment(fragment, false, true);
        }
        if (requestId != 0) {
            final int i = requestId;
            progressDialog.setButton(-2, LocaleController.getString("Cancel", C0553R.string.Cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    ConnectionsManager.getInstance().cancelRequest(i, true);
                    try {
                        dialog.dismiss();
                    } catch (Throwable e) {
                        FileLog.m611e("tmessages", e);
                    }
                }
            });
            progressDialog.show();
        }
    }

    public AlertDialog showAlertDialog(Builder builder) {
        AlertDialog alertDialog = null;
        try {
            if (this.visibleDialog != null) {
                this.visibleDialog.dismiss();
                this.visibleDialog = null;
            }
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
        }
        try {
            this.visibleDialog = builder.show();
            this.visibleDialog.setCanceledOnTouchOutside(true);
            this.visibleDialog.setOnDismissListener(new OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    LaunchActivity.this.visibleDialog = null;
                }
            });
            return this.visibleDialog;
        } catch (Throwable e2) {
            FileLog.m611e("tmessages", e2);
            return alertDialog;
        }
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent, true, false, false);
    }

    public void didSelectDialog(DialogsActivity dialogsFragment, long dialog_id, boolean param) {
        if (dialog_id != 0) {
            int lower_part = (int) dialog_id;
            int high_id = (int) (dialog_id >> 32);
            Bundle args = new Bundle();
            args.putBoolean("scrollToTopOnResume", true);
            if (!AndroidUtilities.isTablet()) {
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats, new Object[0]);
            }
            if (lower_part == 0) {
                args.putInt("enc_id", high_id);
            } else if (high_id == 1) {
                args.putInt("chat_id", lower_part);
            } else if (lower_part > 0) {
                args.putInt("user_id", lower_part);
            } else if (lower_part < 0) {
                args.putInt("chat_id", -lower_part);
            }
            BaseFragment chatActivity = new ChatActivity(args);
            if (this.videoPath == null) {
                this.actionBarLayout.presentFragment(chatActivity, dialogsFragment != null, dialogsFragment == null, true);
                if (this.sendingText != null) {
                    SendMessagesHelper.prepareSendingText(this.sendingText, dialog_id, true);
                }
                if (this.photoPathsArray != null) {
                    SendMessagesHelper.prepareSendingPhotos(null, this.photoPathsArray, dialog_id, null, null, true);
                }
                if (!(this.documentsPathsArray == null && this.documentsUrisArray == null)) {
                    SendMessagesHelper.prepareSendingDocuments(this.documentsPathsArray, this.documentsOriginalPathsArray, this.documentsUrisArray, this.documentsMimeType, dialog_id, null, true);
                }
                if (!(this.contactsToSend == null || this.contactsToSend.isEmpty())) {
                    Iterator i$ = this.contactsToSend.iterator();
                    while (i$.hasNext()) {
                        SendMessagesHelper.getInstance().sendMessage((User) i$.next(), dialog_id, null, true);
                    }
                }
            } else if (VERSION.SDK_INT >= 16) {
                if (AndroidUtilities.isTablet()) {
                    this.actionBarLayout.presentFragment(chatActivity, false, true, true);
                } else {
                    this.actionBarLayout.addFragmentToStack(chatActivity, this.actionBarLayout.fragmentsStack.size() - 1);
                }
                if (!(chatActivity.openVideoEditor(this.videoPath, dialogsFragment != null, false) || dialogsFragment == null || AndroidUtilities.isTablet())) {
                    dialogsFragment.finishFragment(true);
                }
            } else {
                this.actionBarLayout.presentFragment(chatActivity, dialogsFragment != null, dialogsFragment == null, true);
                SendMessagesHelper.prepareSendingVideo(this.videoPath, 0, 0, 0, 0, null, dialog_id, null, true);
            }
            this.photoPathsArray = null;
            this.videoPath = null;
            this.sendingText = null;
            this.documentsPathsArray = null;
            this.documentsOriginalPathsArray = null;
            this.contactsToSend = null;
        }
    }

    private void onFinish() {
        if (!this.finished) {
            this.finished = true;
            if (this.lockRunnable != null) {
                AndroidUtilities.cancelRunOnUIThread(this.lockRunnable);
                this.lockRunnable = null;
            }
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.appDidLogout);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.mainUserInfoChanged);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.closeOtherAppActivities);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.didUpdatedConnectionState);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.needShowAlert);
            if (VERSION.SDK_INT < 14) {
                NotificationCenter.getInstance().removeObserver(this, NotificationCenter.screenStateChanged);
            }
        }
    }

    public void presentFragment(BaseFragment fragment) {
        this.actionBarLayout.presentFragment(fragment);
    }

    public boolean presentFragment(BaseFragment fragment, boolean removeLast, boolean forceWithoutAnimation) {
        return this.actionBarLayout.presentFragment(fragment, removeLast, forceWithoutAnimation, true);
    }

    public void needLayout() {
        int i = 8;
        int i2 = 0;
        if (AndroidUtilities.isTablet()) {
            int y;
            LayoutParams relativeLayoutParams = (LayoutParams) this.layersActionBarLayout.getLayoutParams();
            relativeLayoutParams.leftMargin = (AndroidUtilities.displaySize.x - relativeLayoutParams.width) / 2;
            if (VERSION.SDK_INT >= 21) {
                y = AndroidUtilities.statusBarHeight;
            } else {
                y = 0;
            }
            relativeLayoutParams.topMargin = (((AndroidUtilities.displaySize.y - relativeLayoutParams.height) - y) / 2) + y;
            this.layersActionBarLayout.setLayoutParams(relativeLayoutParams);
            BaseFragment chatFragment;
            if (!AndroidUtilities.isSmallTablet() || getResources().getConfiguration().orientation == 2) {
                int i3;
                this.tabletFullSize = false;
                int leftWidth = (AndroidUtilities.displaySize.x / 100) * 35;
                if (leftWidth < AndroidUtilities.dp(320.0f)) {
                    leftWidth = AndroidUtilities.dp(320.0f);
                }
                relativeLayoutParams = (LayoutParams) this.actionBarLayout.getLayoutParams();
                relativeLayoutParams.width = leftWidth;
                relativeLayoutParams.height = -1;
                this.actionBarLayout.setLayoutParams(relativeLayoutParams);
                relativeLayoutParams = (LayoutParams) this.shadowTabletSide.getLayoutParams();
                relativeLayoutParams.leftMargin = leftWidth;
                this.shadowTabletSide.setLayoutParams(relativeLayoutParams);
                relativeLayoutParams = (LayoutParams) this.rightActionBarLayout.getLayoutParams();
                relativeLayoutParams.width = AndroidUtilities.displaySize.x - leftWidth;
                relativeLayoutParams.height = -1;
                relativeLayoutParams.leftMargin = leftWidth;
                this.rightActionBarLayout.setLayoutParams(relativeLayoutParams);
                if (AndroidUtilities.isSmallTablet() && this.actionBarLayout.fragmentsStack.size() == 2) {
                    chatFragment = (BaseFragment) this.actionBarLayout.fragmentsStack.get(1);
                    chatFragment.onPause();
                    this.actionBarLayout.fragmentsStack.remove(1);
                    this.rightActionBarLayout.fragmentsStack.add(chatFragment);
                    if (this.passcodeView.getVisibility() != 0) {
                        this.actionBarLayout.showLastFragment();
                        this.rightActionBarLayout.showLastFragment();
                    }
                }
                ActionBarLayout actionBarLayout = this.rightActionBarLayout;
                if (this.rightActionBarLayout.fragmentsStack.isEmpty()) {
                    i3 = 8;
                } else {
                    i3 = 0;
                }
                actionBarLayout.setVisibility(i3);
                ImageView imageView = this.backgroundTablet;
                if (this.rightActionBarLayout.fragmentsStack.isEmpty()) {
                    i3 = 0;
                } else {
                    i3 = 8;
                }
                imageView.setVisibility(i3);
                FrameLayout frameLayout = this.shadowTabletSide;
                if (this.actionBarLayout.fragmentsStack.isEmpty()) {
                    i2 = 8;
                }
                frameLayout.setVisibility(i2);
                return;
            }
            this.tabletFullSize = true;
            relativeLayoutParams = (LayoutParams) this.actionBarLayout.getLayoutParams();
            relativeLayoutParams.width = -1;
            relativeLayoutParams.height = -1;
            this.actionBarLayout.setLayoutParams(relativeLayoutParams);
            this.shadowTabletSide.setVisibility(8);
            this.rightActionBarLayout.setVisibility(8);
            ImageView imageView2 = this.backgroundTablet;
            if (this.actionBarLayout.fragmentsStack.isEmpty()) {
                i = 0;
            }
            imageView2.setVisibility(i);
            if (this.rightActionBarLayout.fragmentsStack.size() == 1) {
                chatFragment = (BaseFragment) this.rightActionBarLayout.fragmentsStack.get(0);
                chatFragment.onPause();
                this.rightActionBarLayout.fragmentsStack.remove(0);
                this.actionBarLayout.fragmentsStack.add(chatFragment);
                if (this.passcodeView.getVisibility() != 0) {
                    this.actionBarLayout.showLastFragment();
                }
            }
        }
    }

    public void fixLayout() {
        if (AndroidUtilities.isTablet() && this.actionBarLayout != null) {
            this.actionBarLayout.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    LaunchActivity.this.needLayout();
                    if (LaunchActivity.this.actionBarLayout == null) {
                        return;
                    }
                    if (VERSION.SDK_INT < 16) {
                        LaunchActivity.this.actionBarLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        LaunchActivity.this.actionBarLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            });
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!(UserConfig.passcodeHash.length() == 0 || UserConfig.lastPauseTime == 0)) {
            UserConfig.lastPauseTime = 0;
            UserConfig.saveConfig(false);
        }
        super.onActivityResult(requestCode, resultCode, data);
        if (this.actionBarLayout.fragmentsStack.size() != 0) {
            ((BaseFragment) this.actionBarLayout.fragmentsStack.get(this.actionBarLayout.fragmentsStack.size() - 1)).onActivityResultFragment(requestCode, resultCode, data);
        }
        if (AndroidUtilities.isTablet()) {
            if (this.rightActionBarLayout.fragmentsStack.size() != 0) {
                ((BaseFragment) this.rightActionBarLayout.fragmentsStack.get(this.rightActionBarLayout.fragmentsStack.size() - 1)).onActivityResultFragment(requestCode, resultCode, data);
            }
            if (this.layersActionBarLayout.fragmentsStack.size() != 0) {
                ((BaseFragment) this.layersActionBarLayout.fragmentsStack.get(this.layersActionBarLayout.fragmentsStack.size() - 1)).onActivityResultFragment(requestCode, resultCode, data);
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != 3 && requestCode != 4 && requestCode != 5) {
            if (this.actionBarLayout.fragmentsStack.size() != 0) {
                ((BaseFragment) this.actionBarLayout.fragmentsStack.get(this.actionBarLayout.fragmentsStack.size() - 1)).onRequestPermissionsResultFragment(requestCode, permissions, grantResults);
            }
            if (AndroidUtilities.isTablet()) {
                if (this.rightActionBarLayout.fragmentsStack.size() != 0) {
                    ((BaseFragment) this.rightActionBarLayout.fragmentsStack.get(this.rightActionBarLayout.fragmentsStack.size() - 1)).onRequestPermissionsResultFragment(requestCode, permissions, grantResults);
                }
                if (this.layersActionBarLayout.fragmentsStack.size() != 0) {
                    ((BaseFragment) this.layersActionBarLayout.fragmentsStack.get(this.layersActionBarLayout.fragmentsStack.size() - 1)).onRequestPermissionsResultFragment(requestCode, permissions, grantResults);
                }
            }
        } else if (grantResults[0] != 0) {
            Builder builder = new Builder(this);
            builder.setTitle(LocaleController.getString("AppName", C0553R.string.AppName));
            if (requestCode == 3) {
                builder.setMessage(LocaleController.getString("PermissionNoAudio", C0553R.string.PermissionNoAudio));
            } else if (requestCode == 4) {
                builder.setMessage(LocaleController.getString("PermissionStorage", C0553R.string.PermissionStorage));
            } else if (requestCode == 5) {
                builder.setMessage(LocaleController.getString("PermissionContacts", C0553R.string.PermissionContacts));
            }
            builder.setNegativeButton(LocaleController.getString("PermissionOpenSettings", C0553R.string.PermissionOpenSettings), new DialogInterface.OnClickListener() {
                @TargetApi(9)
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
                        intent.setData(Uri.parse("package:" + ApplicationLoader.applicationContext.getPackageName()));
                        LaunchActivity.this.startActivity(intent);
                    } catch (Throwable e) {
                        FileLog.m611e("tmessages", e);
                    }
                }
            });
            builder.setPositiveButton(LocaleController.getString("OK", C0553R.string.OK), null);
            builder.show();
        } else if (requestCode == 4) {
            ImageLoader.getInstance().createMediaPaths();
        } else if (requestCode == 5) {
            ContactsController.getInstance().readContacts();
        }
    }

    protected void onPause() {
        super.onPause();
        ApplicationLoader.mainInterfacePaused = true;
        onPasscodePause();
        this.actionBarLayout.onPause();
        if (AndroidUtilities.isTablet()) {
            this.rightActionBarLayout.onPause();
            this.layersActionBarLayout.onPause();
        }
        if (this.passcodeView != null) {
            this.passcodeView.onPause();
        }
        ConnectionsManager.getInstance().setAppPaused(true, false);
        AndroidUtilities.unregisterUpdates();
    }

    protected void onDestroy() {
        PhotoViewer.getInstance().destroyPhotoViewer();
        SecretPhotoViewer.getInstance().destroyPhotoViewer();
        StickerPreviewViewer.getInstance().destroy();
        try {
            if (this.visibleDialog != null) {
                this.visibleDialog.dismiss();
                this.visibleDialog = null;
            }
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
        }
        try {
            if (this.onGlobalLayoutListener != null) {
                View view = getWindow().getDecorView().getRootView();
                if (VERSION.SDK_INT < 16) {
                    view.getViewTreeObserver().removeGlobalOnLayoutListener(this.onGlobalLayoutListener);
                } else {
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this.onGlobalLayoutListener);
                }
            }
        } catch (Throwable e2) {
            FileLog.m611e("tmessages", e2);
        }
        super.onDestroy();
        onFinish();
    }

    protected void onResume() {
        super.onResume();
        ApplicationLoader.mainInterfacePaused = false;
        onPasscodeResume();
        if (this.passcodeView.getVisibility() != 0) {
            this.actionBarLayout.onResume();
            if (AndroidUtilities.isTablet()) {
                this.rightActionBarLayout.onResume();
                this.layersActionBarLayout.onResume();
            }
        } else {
            this.passcodeView.onResume();
        }
        AndroidUtilities.checkForCrashes(this);
        AndroidUtilities.checkForUpdates(this);
        ConnectionsManager.getInstance().setAppPaused(false, false);
        updateCurrentConnectionState();
        if (PhotoViewer.getInstance().isVisible()) {
            PhotoViewer.getInstance().onResume();
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        AndroidUtilities.checkDisplaySize();
        super.onConfigurationChanged(newConfig);
        fixLayout();
    }

    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.appDidLogout) {
            if (this.drawerLayoutAdapter != null) {
                this.drawerLayoutAdapter.notifyDataSetChanged();
            }
            Iterator i$ = this.actionBarLayout.fragmentsStack.iterator();
            while (i$.hasNext()) {
                ((BaseFragment) i$.next()).onFragmentDestroy();
            }
            this.actionBarLayout.fragmentsStack.clear();
            if (AndroidUtilities.isTablet()) {
                i$ = this.layersActionBarLayout.fragmentsStack.iterator();
                while (i$.hasNext()) {
                    ((BaseFragment) i$.next()).onFragmentDestroy();
                }
                this.layersActionBarLayout.fragmentsStack.clear();
                i$ = this.rightActionBarLayout.fragmentsStack.iterator();
                while (i$.hasNext()) {
                    ((BaseFragment) i$.next()).onFragmentDestroy();
                }
                this.rightActionBarLayout.fragmentsStack.clear();
            }
            startActivity(new Intent(this, IntroActivity.class));
            onFinish();
            finish();
        } else if (id == NotificationCenter.closeOtherAppActivities) {
            if (args[0] != this) {
                onFinish();
                finish();
            }
        } else if (id == NotificationCenter.didUpdatedConnectionState) {
            int state = ConnectionsManager.getInstance().getConnectionState();
            if (this.currentConnectionState != state) {
                FileLog.m608d("tmessages", "switch to state " + state);
                this.currentConnectionState = state;
                updateCurrentConnectionState();
            }
        } else if (id == NotificationCenter.mainUserInfoChanged) {
            this.drawerLayoutAdapter.notifyDataSetChanged();
        } else if (id == NotificationCenter.screenStateChanged) {
            if (!ApplicationLoader.mainInterfacePaused) {
                if (ApplicationLoader.isScreenOn) {
                    onPasscodeResume();
                } else {
                    onPasscodePause();
                }
            }
        } else if (id == NotificationCenter.needShowAlert) {
            Integer reason = args[0];
            Builder builder = new Builder(this);
            builder.setTitle(LocaleController.getString("AppName", C0553R.string.AppName));
            builder.setPositiveButton(LocaleController.getString("OK", C0553R.string.OK), null);
            if (reason.intValue() != 2) {
                builder.setNegativeButton(LocaleController.getString("MoreInfo", C0553R.string.MoreInfo), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(LocaleController.getString("NobodyLikesSpamUrl", C0553R.string.NobodyLikesSpamUrl)));
                            intent.putExtra("com.android.browser.application_id", LaunchActivity.this.getPackageName());
                            LaunchActivity.this.startActivity(intent);
                        } catch (Throwable e) {
                            FileLog.m611e("tmessages", e);
                        }
                    }
                });
            }
            if (reason.intValue() == 0) {
                builder.setMessage(LocaleController.getString("NobodyLikesSpam1", C0553R.string.NobodyLikesSpam1));
            } else if (reason.intValue() == 1) {
                builder.setMessage(LocaleController.getString("NobodyLikesSpam2", C0553R.string.NobodyLikesSpam2));
            } else if (reason.intValue() == 2) {
                builder.setMessage((String) args[1]);
            }
            if (!mainFragmentsStack.isEmpty()) {
                ((BaseFragment) mainFragmentsStack.get(mainFragmentsStack.size() - 1)).showDialog(builder.create());
            }
        }
    }

    private void onPasscodePause() {
        if (this.lockRunnable != null) {
            AndroidUtilities.cancelRunOnUIThread(this.lockRunnable);
            this.lockRunnable = null;
        }
        if (UserConfig.passcodeHash.length() != 0) {
            UserConfig.lastPauseTime = ConnectionsManager.getInstance().getCurrentTime();
            this.lockRunnable = new Runnable() {
                public void run() {
                    if (LaunchActivity.this.lockRunnable == this) {
                        if (AndroidUtilities.needShowPasscode(true)) {
                            FileLog.m609e("tmessages", "lock app");
                            LaunchActivity.this.showPasscodeActivity();
                        } else {
                            FileLog.m609e("tmessages", "didn't pass lock check");
                        }
                        LaunchActivity.this.lockRunnable = null;
                    }
                }
            };
            if (UserConfig.appLocked) {
                AndroidUtilities.runOnUIThread(this.lockRunnable, 1000);
            } else if (UserConfig.autoLockIn != 0) {
                AndroidUtilities.runOnUIThread(this.lockRunnable, (((long) UserConfig.autoLockIn) * 1000) + 1000);
            }
        } else {
            UserConfig.lastPauseTime = 0;
        }
        UserConfig.saveConfig(false);
    }

    private void onPasscodeResume() {
        if (this.lockRunnable != null) {
            AndroidUtilities.cancelRunOnUIThread(this.lockRunnable);
            this.lockRunnable = null;
        }
        if (AndroidUtilities.needShowPasscode(true)) {
            showPasscodeActivity();
        }
        if (UserConfig.lastPauseTime != 0) {
            UserConfig.lastPauseTime = 0;
            UserConfig.saveConfig(false);
        }
    }

    private void updateCurrentConnectionState() {
        String text = null;
        if (this.currentConnectionState == 2) {
            text = LocaleController.getString("WaitingForNetwork", C0553R.string.WaitingForNetwork);
        } else if (this.currentConnectionState == 1) {
            text = LocaleController.getString("Connecting", C0553R.string.Connecting);
        } else if (this.currentConnectionState == 4) {
            text = LocaleController.getString("Updating", C0553R.string.Updating);
        }
        this.actionBarLayout.setTitleOverlayText(text);
    }

    protected void onSaveInstanceState(Bundle outState) {
        try {
            super.onSaveInstanceState(outState);
            BaseFragment lastFragment = null;
            if (AndroidUtilities.isTablet()) {
                if (!this.layersActionBarLayout.fragmentsStack.isEmpty()) {
                    lastFragment = (BaseFragment) this.layersActionBarLayout.fragmentsStack.get(this.layersActionBarLayout.fragmentsStack.size() - 1);
                } else if (!this.rightActionBarLayout.fragmentsStack.isEmpty()) {
                    lastFragment = (BaseFragment) this.rightActionBarLayout.fragmentsStack.get(this.rightActionBarLayout.fragmentsStack.size() - 1);
                } else if (!this.actionBarLayout.fragmentsStack.isEmpty()) {
                    lastFragment = (BaseFragment) this.actionBarLayout.fragmentsStack.get(this.actionBarLayout.fragmentsStack.size() - 1);
                }
            } else if (!this.actionBarLayout.fragmentsStack.isEmpty()) {
                lastFragment = (BaseFragment) this.actionBarLayout.fragmentsStack.get(this.actionBarLayout.fragmentsStack.size() - 1);
            }
            if (lastFragment != null) {
                Bundle args = lastFragment.getArguments();
                if ((lastFragment instanceof ChatActivity) && args != null) {
                    outState.putBundle("args", args);
                    outState.putString("fragment", "chat");
                } else if (lastFragment instanceof SettingsActivity) {
                    outState.putString("fragment", "settings");
                } else if ((lastFragment instanceof GroupCreateFinalActivity) && args != null) {
                    outState.putBundle("args", args);
                    outState.putString("fragment", "group");
                } else if (lastFragment instanceof WallpapersActivity) {
                    outState.putString("fragment", "wallpapers");
                } else if ((lastFragment instanceof ProfileActivity) && ((ProfileActivity) lastFragment).isChat() && args != null) {
                    outState.putBundle("args", args);
                    outState.putString("fragment", "chat_profile");
                } else if ((lastFragment instanceof ChannelCreateActivity) && args != null && args.getInt("step") == 0) {
                    outState.putBundle("args", args);
                    outState.putString("fragment", "channel");
                } else if ((lastFragment instanceof ChannelEditActivity) && args != null) {
                    outState.putBundle("args", args);
                    outState.putString("fragment", "edit");
                }
                lastFragment.saveSelfArgs(outState);
            }
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
        }
    }

    public void onBackPressed() {
        if (this.passcodeView.getVisibility() == 0) {
            finish();
        } else if (PhotoViewer.getInstance().isVisible()) {
            PhotoViewer.getInstance().closePhoto(true, false);
        } else if (this.drawerLayoutContainer.isDrawerOpened()) {
            this.drawerLayoutContainer.closeDrawer(false);
        } else if (!AndroidUtilities.isTablet()) {
            this.actionBarLayout.onBackPressed();
        } else if (this.layersActionBarLayout.getVisibility() == 0) {
            this.layersActionBarLayout.onBackPressed();
        } else {
            boolean cancel = false;
            if (this.rightActionBarLayout.getVisibility() == 0 && !this.rightActionBarLayout.fragmentsStack.isEmpty()) {
                cancel = !((BaseFragment) this.rightActionBarLayout.fragmentsStack.get(this.rightActionBarLayout.fragmentsStack.size() + -1)).onBackPressed();
            }
            if (!cancel) {
                this.actionBarLayout.onBackPressed();
            }
        }
    }

    public void onLowMemory() {
        super.onLowMemory();
        this.actionBarLayout.onLowMemory();
        if (AndroidUtilities.isTablet()) {
            this.rightActionBarLayout.onLowMemory();
            this.layersActionBarLayout.onLowMemory();
        }
    }

    public void onActionModeStarted(ActionMode mode) {
        super.onActionModeStarted(mode);
        if (VERSION.SDK_INT < 23 || mode.getType() != 1) {
            this.actionBarLayout.onActionModeStarted(mode);
            if (AndroidUtilities.isTablet()) {
                this.rightActionBarLayout.onActionModeStarted(mode);
                this.layersActionBarLayout.onActionModeStarted(mode);
            }
        }
    }

    public void onActionModeFinished(ActionMode mode) {
        super.onActionModeFinished(mode);
        if (VERSION.SDK_INT < 23 || mode.getType() != 1) {
            this.actionBarLayout.onActionModeFinished(mode);
            if (AndroidUtilities.isTablet()) {
                this.rightActionBarLayout.onActionModeFinished(mode);
                this.layersActionBarLayout.onActionModeFinished(mode);
            }
        }
    }

    public boolean onPreIme() {
        if (!PhotoViewer.getInstance().isVisible()) {
            return false;
        }
        PhotoViewer.getInstance().closePhoto(true, false);
        return true;
    }

    public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == 82) {
            if (AndroidUtilities.isTablet()) {
                if (this.layersActionBarLayout.getVisibility() == 0 && !this.layersActionBarLayout.fragmentsStack.isEmpty()) {
                    this.layersActionBarLayout.onKeyUp(keyCode, event);
                } else if (this.rightActionBarLayout.getVisibility() != 0 || this.rightActionBarLayout.fragmentsStack.isEmpty()) {
                    this.actionBarLayout.onKeyUp(keyCode, event);
                } else {
                    this.rightActionBarLayout.onKeyUp(keyCode, event);
                }
            } else if (this.actionBarLayout.fragmentsStack.size() != 1) {
                this.actionBarLayout.onKeyUp(keyCode, event);
            } else if (this.drawerLayoutContainer.isDrawerOpened()) {
                this.drawerLayoutContainer.closeDrawer(false);
            } else {
                if (getCurrentFocus() != null) {
                    AndroidUtilities.hideKeyboard(getCurrentFocus());
                }
                this.drawerLayoutContainer.openDrawer(false);
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    public boolean needPresentFragment(BaseFragment fragment, boolean removeLast, boolean forceWithoutAnimation, ActionBarLayout layout) {
        boolean z = true;
        boolean z2;
        if (AndroidUtilities.isTablet()) {
            DrawerLayoutContainer drawerLayoutContainer = this.drawerLayoutContainer;
            z2 = ((fragment instanceof LoginActivity) || (fragment instanceof CountrySelectActivity) || this.layersActionBarLayout.getVisibility() == 0) ? false : true;
            drawerLayoutContainer.setAllowOpenDrawer(z2, true);
            if ((fragment instanceof DialogsActivity) && ((DialogsActivity) fragment).isMainDialogList() && layout != this.actionBarLayout) {
                this.actionBarLayout.removeAllFragments();
                this.actionBarLayout.presentFragment(fragment, removeLast, forceWithoutAnimation, false);
                this.layersActionBarLayout.removeAllFragments();
                this.layersActionBarLayout.setVisibility(8);
                this.drawerLayoutContainer.setAllowOpenDrawer(true, false);
                if (this.tabletFullSize) {
                    return false;
                }
                this.shadowTabletSide.setVisibility(0);
                if (!this.rightActionBarLayout.fragmentsStack.isEmpty()) {
                    return false;
                }
                this.backgroundTablet.setVisibility(0);
                return false;
            } else if (fragment instanceof ChatActivity) {
                int a;
                ActionBarLayout actionBarLayout;
                if ((!this.tabletFullSize && layout == this.rightActionBarLayout) || (this.tabletFullSize && layout == this.actionBarLayout)) {
                    boolean result;
                    if (this.tabletFullSize && layout == this.actionBarLayout && this.actionBarLayout.fragmentsStack.size() == 1) {
                        result = false;
                    } else {
                        result = true;
                    }
                    if (!this.layersActionBarLayout.fragmentsStack.isEmpty()) {
                        a = 0;
                        while (this.layersActionBarLayout.fragmentsStack.size() - 1 > 0) {
                            this.layersActionBarLayout.removeFragmentFromStack((BaseFragment) this.layersActionBarLayout.fragmentsStack.get(0));
                            a = (a - 1) + 1;
                        }
                        actionBarLayout = this.layersActionBarLayout;
                        if (forceWithoutAnimation) {
                            z = false;
                        }
                        actionBarLayout.closeLastFragment(z);
                    }
                    if (!result) {
                        this.actionBarLayout.presentFragment(fragment, false, forceWithoutAnimation, false);
                    }
                    return result;
                } else if (!this.tabletFullSize && layout != this.rightActionBarLayout) {
                    this.rightActionBarLayout.setVisibility(0);
                    this.backgroundTablet.setVisibility(8);
                    this.rightActionBarLayout.removeAllFragments();
                    this.rightActionBarLayout.presentFragment(fragment, removeLast, true, false);
                    if (this.layersActionBarLayout.fragmentsStack.isEmpty()) {
                        return false;
                    }
                    a = 0;
                    while (this.layersActionBarLayout.fragmentsStack.size() - 1 > 0) {
                        this.layersActionBarLayout.removeFragmentFromStack((BaseFragment) this.layersActionBarLayout.fragmentsStack.get(0));
                        a = (a - 1) + 1;
                    }
                    actionBarLayout = this.layersActionBarLayout;
                    if (forceWithoutAnimation) {
                        z = false;
                    }
                    actionBarLayout.closeLastFragment(z);
                    return false;
                } else if (!this.tabletFullSize || layout == this.actionBarLayout) {
                    if (!this.layersActionBarLayout.fragmentsStack.isEmpty()) {
                        a = 0;
                        while (this.layersActionBarLayout.fragmentsStack.size() - 1 > 0) {
                            this.layersActionBarLayout.removeFragmentFromStack((BaseFragment) this.layersActionBarLayout.fragmentsStack.get(0));
                            a = (a - 1) + 1;
                        }
                        r6 = this.layersActionBarLayout;
                        if (forceWithoutAnimation) {
                            z2 = false;
                        } else {
                            z2 = true;
                        }
                        r6.closeLastFragment(z2);
                    }
                    actionBarLayout = this.actionBarLayout;
                    if (this.actionBarLayout.fragmentsStack.size() <= 1) {
                        z = false;
                    }
                    actionBarLayout.presentFragment(fragment, z, forceWithoutAnimation, false);
                    return false;
                } else {
                    r6 = this.actionBarLayout;
                    if (this.actionBarLayout.fragmentsStack.size() > 1) {
                        z2 = true;
                    } else {
                        z2 = false;
                    }
                    r6.presentFragment(fragment, z2, forceWithoutAnimation, false);
                    if (this.layersActionBarLayout.fragmentsStack.isEmpty()) {
                        return false;
                    }
                    a = 0;
                    while (this.layersActionBarLayout.fragmentsStack.size() - 1 > 0) {
                        this.layersActionBarLayout.removeFragmentFromStack((BaseFragment) this.layersActionBarLayout.fragmentsStack.get(0));
                        a = (a - 1) + 1;
                    }
                    actionBarLayout = this.layersActionBarLayout;
                    if (forceWithoutAnimation) {
                        z = false;
                    }
                    actionBarLayout.closeLastFragment(z);
                    return false;
                }
            } else if (layout == this.layersActionBarLayout) {
                return true;
            } else {
                this.layersActionBarLayout.setVisibility(0);
                this.drawerLayoutContainer.setAllowOpenDrawer(false, true);
                if (fragment instanceof LoginActivity) {
                    this.backgroundTablet.setVisibility(0);
                    this.shadowTabletSide.setVisibility(8);
                    this.shadowTablet.setBackgroundColor(0);
                } else {
                    this.shadowTablet.setBackgroundColor(2130706432);
                }
                this.layersActionBarLayout.presentFragment(fragment, removeLast, forceWithoutAnimation, false);
                return false;
            }
        }
        drawerLayoutContainer = this.drawerLayoutContainer;
        if ((fragment instanceof LoginActivity) || (fragment instanceof CountrySelectActivity)) {
            z2 = false;
        } else {
            z2 = true;
        }
        drawerLayoutContainer.setAllowOpenDrawer(z2, false);
        return true;
    }

    public boolean needAddFragmentToStack(BaseFragment fragment, ActionBarLayout layout) {
        if (AndroidUtilities.isTablet()) {
            DrawerLayoutContainer drawerLayoutContainer = this.drawerLayoutContainer;
            boolean z = ((fragment instanceof LoginActivity) || (fragment instanceof CountrySelectActivity) || this.layersActionBarLayout.getVisibility() == 0) ? false : true;
            drawerLayoutContainer.setAllowOpenDrawer(z, true);
            if (fragment instanceof DialogsActivity) {
                if (((DialogsActivity) fragment).isMainDialogList() && layout != this.actionBarLayout) {
                    this.actionBarLayout.removeAllFragments();
                    this.actionBarLayout.addFragmentToStack(fragment);
                    this.layersActionBarLayout.removeAllFragments();
                    this.layersActionBarLayout.setVisibility(8);
                    this.drawerLayoutContainer.setAllowOpenDrawer(true, false);
                    if (this.tabletFullSize) {
                        return false;
                    }
                    this.shadowTabletSide.setVisibility(0);
                    if (!this.rightActionBarLayout.fragmentsStack.isEmpty()) {
                        return false;
                    }
                    this.backgroundTablet.setVisibility(0);
                    return false;
                }
            } else if (fragment instanceof ChatActivity) {
                int a;
                if (!this.tabletFullSize && layout != this.rightActionBarLayout) {
                    this.rightActionBarLayout.setVisibility(0);
                    this.backgroundTablet.setVisibility(8);
                    this.rightActionBarLayout.removeAllFragments();
                    this.rightActionBarLayout.addFragmentToStack(fragment);
                    if (this.layersActionBarLayout.fragmentsStack.isEmpty()) {
                        return false;
                    }
                    a = 0;
                    while (this.layersActionBarLayout.fragmentsStack.size() - 1 > 0) {
                        this.layersActionBarLayout.removeFragmentFromStack((BaseFragment) this.layersActionBarLayout.fragmentsStack.get(0));
                        a = (a - 1) + 1;
                    }
                    this.layersActionBarLayout.closeLastFragment(true);
                    return false;
                } else if (this.tabletFullSize && layout != this.actionBarLayout) {
                    this.actionBarLayout.addFragmentToStack(fragment);
                    if (this.layersActionBarLayout.fragmentsStack.isEmpty()) {
                        return false;
                    }
                    a = 0;
                    while (this.layersActionBarLayout.fragmentsStack.size() - 1 > 0) {
                        this.layersActionBarLayout.removeFragmentFromStack((BaseFragment) this.layersActionBarLayout.fragmentsStack.get(0));
                        a = (a - 1) + 1;
                    }
                    this.layersActionBarLayout.closeLastFragment(true);
                    return false;
                }
            } else if (layout != this.layersActionBarLayout) {
                this.layersActionBarLayout.setVisibility(0);
                this.drawerLayoutContainer.setAllowOpenDrawer(false, true);
                if (fragment instanceof LoginActivity) {
                    this.backgroundTablet.setVisibility(0);
                    this.shadowTabletSide.setVisibility(8);
                    this.shadowTablet.setBackgroundColor(0);
                } else {
                    this.shadowTablet.setBackgroundColor(2130706432);
                }
                this.layersActionBarLayout.addFragmentToStack(fragment);
                return false;
            }
            return true;
        }
        drawerLayoutContainer = this.drawerLayoutContainer;
        if ((fragment instanceof LoginActivity) || (fragment instanceof CountrySelectActivity)) {
            z = false;
        } else {
            z = true;
        }
        drawerLayoutContainer.setAllowOpenDrawer(z, false);
        return true;
    }

    public boolean needCloseLastFragment(ActionBarLayout layout) {
        if (AndroidUtilities.isTablet()) {
            if (layout == this.actionBarLayout && layout.fragmentsStack.size() <= 1) {
                onFinish();
                finish();
                return false;
            } else if (layout == this.rightActionBarLayout) {
                if (!this.tabletFullSize) {
                    this.backgroundTablet.setVisibility(0);
                }
            } else if (layout == this.layersActionBarLayout && this.actionBarLayout.fragmentsStack.isEmpty() && this.layersActionBarLayout.fragmentsStack.size() == 1) {
                onFinish();
                finish();
                return false;
            }
        } else if (layout.fragmentsStack.size() <= 1) {
            onFinish();
            finish();
            return false;
        }
        return true;
    }

    public void onRebuildAllFragments(ActionBarLayout layout) {
        if (AndroidUtilities.isTablet() && layout == this.layersActionBarLayout) {
            this.rightActionBarLayout.rebuildAllFragmentViews(true);
            this.rightActionBarLayout.showLastFragment();
            this.actionBarLayout.rebuildAllFragmentViews(true);
            this.actionBarLayout.showLastFragment();
        }
        this.drawerLayoutAdapter.notifyDataSetChanged();
    }
}
