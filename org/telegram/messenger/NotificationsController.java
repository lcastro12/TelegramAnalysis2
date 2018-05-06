package org.telegram.messenger;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.provider.Settings.System;
import android.support.v4.app.NotificationCompat.Action;
import android.support.v4.app.NotificationCompat.BigTextStyle;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.NotificationCompat.CarExtender;
import android.support.v4.app.NotificationCompat.CarExtender.UnreadConversation;
import android.support.v4.app.NotificationCompat.InboxStyle;
import android.support.v4.app.NotificationCompat.Style;
import android.support.v4.app.NotificationCompat.WearableExtender;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.util.SparseArray;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.location.LocationStatusCodes;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import org.aspectj.lang.JoinPoint;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.EncryptedChat;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.tgnet.TLRPC.TL_account_updateNotifySettings;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_inputNotifyPeer;
import org.telegram.tgnet.TLRPC.TL_inputPeerNotifySettings;
import org.telegram.tgnet.TLRPC.TL_messageActionChannelCreate;
import org.telegram.tgnet.TLRPC.TL_messageActionChannelMigrateFrom;
import org.telegram.tgnet.TLRPC.TL_messageActionChatAddUser;
import org.telegram.tgnet.TLRPC.TL_messageActionChatCreate;
import org.telegram.tgnet.TLRPC.TL_messageActionChatDeletePhoto;
import org.telegram.tgnet.TLRPC.TL_messageActionChatDeleteUser;
import org.telegram.tgnet.TLRPC.TL_messageActionChatEditPhoto;
import org.telegram.tgnet.TLRPC.TL_messageActionChatEditTitle;
import org.telegram.tgnet.TLRPC.TL_messageActionChatJoinedByLink;
import org.telegram.tgnet.TLRPC.TL_messageActionChatMigrateTo;
import org.telegram.tgnet.TLRPC.TL_messageActionEmpty;
import org.telegram.tgnet.TLRPC.TL_messageActionLoginUnknownLocation;
import org.telegram.tgnet.TLRPC.TL_messageActionUserJoined;
import org.telegram.tgnet.TLRPC.TL_messageActionUserUpdatedPhoto;
import org.telegram.tgnet.TLRPC.TL_messageMediaAudio;
import org.telegram.tgnet.TLRPC.TL_messageMediaContact;
import org.telegram.tgnet.TLRPC.TL_messageMediaDocument;
import org.telegram.tgnet.TLRPC.TL_messageMediaGeo;
import org.telegram.tgnet.TLRPC.TL_messageMediaPhoto;
import org.telegram.tgnet.TLRPC.TL_messageMediaVenue;
import org.telegram.tgnet.TLRPC.TL_messageMediaVideo;
import org.telegram.tgnet.TLRPC.TL_messageService;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.PopupNotificationActivity;

public class NotificationsController {
    public static final String EXTRA_VOICE_REPLY = "extra_voice_reply";
    private static volatile NotificationsController Instance = null;
    private AlarmManager alarmManager;
    protected AudioManager audioManager;
    private int autoNotificationId;
    private HashMap<Long, Integer> autoNotificationsIds;
    private ArrayList<MessageObject> delayedPushMessages;
    private boolean inChatSoundEnabled;
    private int lastBadgeCount;
    private int lastOnlineFromOtherDevice;
    private long lastSoundOutPlay;
    private long lastSoundPlay;
    private String launcherClassName;
    private Runnable notificationDelayRunnable;
    private WakeLock notificationDelayWakelock;
    private NotificationManagerCompat notificationManager;
    private DispatchQueue notificationsQueue;
    private boolean notifyCheck;
    private long openned_dialog_id;
    private int personal_count;
    public ArrayList<MessageObject> popupMessages;
    private HashMap<Long, Integer> pushDialogs;
    private HashMap<Long, Integer> pushDialogsOverrideMention;
    private ArrayList<MessageObject> pushMessages;
    private HashMap<Long, MessageObject> pushMessagesDict;
    private HashMap<Long, Point> smartNotificationsDialogs;
    private int soundIn;
    private boolean soundInLoaded;
    private int soundOut;
    private boolean soundOutLoaded;
    private SoundPool soundPool;
    private int total_unread_count;
    private int wearNotificationId;
    private HashMap<Long, Integer> wearNotificationsIds;

    class C05391 implements Runnable {
        C05391() {
        }

        public void run() {
            FileLog.m609e("tmessages", "delay reached");
            if (!NotificationsController.this.delayedPushMessages.isEmpty()) {
                NotificationsController.this.showOrUpdateNotification(true);
                NotificationsController.this.delayedPushMessages.clear();
            }
            try {
                if (NotificationsController.this.notificationDelayWakelock.isHeld()) {
                    NotificationsController.this.notificationDelayWakelock.release();
                }
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
        }
    }

    class C05402 implements Runnable {
        C05402() {
        }

        public void run() {
            NotificationsController.this.openned_dialog_id = 0;
            NotificationsController.this.total_unread_count = 0;
            NotificationsController.this.personal_count = 0;
            NotificationsController.this.pushMessages.clear();
            NotificationsController.this.pushMessagesDict.clear();
            NotificationsController.this.pushDialogs.clear();
            NotificationsController.this.wearNotificationsIds.clear();
            NotificationsController.this.autoNotificationsIds.clear();
            NotificationsController.this.delayedPushMessages.clear();
            NotificationsController.this.notifyCheck = false;
            NotificationsController.this.lastBadgeCount = 0;
            try {
                if (NotificationsController.this.notificationDelayWakelock.isHeld()) {
                    NotificationsController.this.notificationDelayWakelock.release();
                }
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
            NotificationsController.this.setBadge(0);
            Editor editor = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", 0).edit();
            editor.clear();
            editor.commit();
        }
    }

    static /* synthetic */ int access$412(NotificationsController x0, int x1) {
        int i = x0.total_unread_count + x1;
        x0.total_unread_count = i;
        return i;
    }

    static /* synthetic */ int access$420(NotificationsController x0, int x1) {
        int i = x0.total_unread_count - x1;
        x0.total_unread_count = i;
        return i;
    }

    public static NotificationsController getInstance() {
        NotificationsController localInstance = Instance;
        if (localInstance == null) {
            synchronized (MessagesController.class) {
                try {
                    localInstance = Instance;
                    if (localInstance == null) {
                        NotificationsController localInstance2 = new NotificationsController();
                        try {
                            Instance = localInstance2;
                            localInstance = localInstance2;
                        } catch (Throwable th) {
                            Throwable th2 = th;
                            localInstance = localInstance2;
                            throw th2;
                        }
                    }
                } catch (Throwable th3) {
                    th2 = th3;
                    throw th2;
                }
            }
        }
        return localInstance;
    }

    public NotificationsController() {
        this.notificationsQueue = new DispatchQueue("notificationsQueue");
        this.pushMessages = new ArrayList();
        this.delayedPushMessages = new ArrayList();
        this.pushMessagesDict = new HashMap();
        this.smartNotificationsDialogs = new HashMap();
        this.notificationManager = null;
        this.pushDialogs = new HashMap();
        this.wearNotificationsIds = new HashMap();
        this.autoNotificationsIds = new HashMap();
        this.pushDialogsOverrideMention = new HashMap();
        this.wearNotificationId = 10000;
        this.autoNotificationId = 20000;
        this.popupMessages = new ArrayList();
        this.openned_dialog_id = 0;
        this.total_unread_count = 0;
        this.personal_count = 0;
        this.notifyCheck = false;
        this.lastOnlineFromOtherDevice = 0;
        this.inChatSoundEnabled = true;
        this.notificationManager = NotificationManagerCompat.from(ApplicationLoader.applicationContext);
        this.inChatSoundEnabled = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", 0).getBoolean("EnableInChatSound", true);
        try {
            this.audioManager = (AudioManager) ApplicationLoader.applicationContext.getSystemService("audio");
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
        }
        try {
            this.alarmManager = (AlarmManager) ApplicationLoader.applicationContext.getSystemService("alarm");
        } catch (Throwable e2) {
            FileLog.m611e("tmessages", e2);
        }
        try {
            this.notificationDelayWakelock = ((PowerManager) ApplicationLoader.applicationContext.getSystemService("power")).newWakeLock(1, JoinPoint.SYNCHRONIZATION_LOCK);
            this.notificationDelayWakelock.setReferenceCounted(false);
        } catch (Throwable e22) {
            FileLog.m611e("tmessages", e22);
        }
        this.notificationDelayRunnable = new C05391();
    }

    public void cleanup() {
        this.popupMessages.clear();
        this.notificationsQueue.postRunnable(new C05402());
    }

    public void setInChatSoundEnabled(boolean value) {
        this.inChatSoundEnabled = value;
    }

    public void setOpennedDialogId(final long dialog_id) {
        this.notificationsQueue.postRunnable(new Runnable() {
            public void run() {
                NotificationsController.this.openned_dialog_id = dialog_id;
            }
        });
    }

    public void setLastOnlineFromOtherDevice(final int time) {
        this.notificationsQueue.postRunnable(new Runnable() {
            public void run() {
                FileLog.m609e("tmessages", "set last online from other device = " + time);
                NotificationsController.this.lastOnlineFromOtherDevice = time;
            }
        });
    }

    public void removeNotificationsForDialog(long did) {
        getInstance().processReadMessages(null, did, 0, ConnectionsManager.DEFAULT_DATACENTER_ID, false);
        HashMap<Long, Integer> dialogsToUpdate = new HashMap();
        dialogsToUpdate.put(Long.valueOf(did), Integer.valueOf(0));
        getInstance().processDialogsUpdateRead(dialogsToUpdate);
    }

    public void removeDeletedMessagesFromNotifications(final SparseArray<ArrayList<Integer>> deletedMessages) {
        final ArrayList<MessageObject> popupArray = this.popupMessages.isEmpty() ? null : new ArrayList(this.popupMessages);
        this.notificationsQueue.postRunnable(new Runnable() {

            class C05431 implements Runnable {
                C05431() {
                }

                public void run() {
                    NotificationsController.this.popupMessages = popupArray;
                }
            }

            public void run() {
                int old_unread_count = NotificationsController.this.total_unread_count;
                SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", 0);
                for (int a = 0; a < deletedMessages.size(); a++) {
                    int key = deletedMessages.keyAt(a);
                    long dialog_id = (long) (-key);
                    ArrayList<Integer> mids = (ArrayList) deletedMessages.get(key);
                    Integer currentCount = (Integer) NotificationsController.this.pushDialogs.get(Long.valueOf(dialog_id));
                    if (currentCount == null) {
                        currentCount = Integer.valueOf(0);
                    }
                    Integer newCount = currentCount;
                    for (int b = 0; b < mids.size(); b++) {
                        long mid = ((long) ((Integer) mids.get(b)).intValue()) | (((long) key) << 32);
                        MessageObject messageObject = (MessageObject) NotificationsController.this.pushMessagesDict.get(Long.valueOf(mid));
                        if (messageObject != null) {
                            NotificationsController.this.pushMessagesDict.remove(Long.valueOf(mid));
                            NotificationsController.this.delayedPushMessages.remove(messageObject);
                            NotificationsController.this.pushMessages.remove(messageObject);
                            if (NotificationsController.this.isPersonalMessage(messageObject)) {
                                NotificationsController.this.personal_count = NotificationsController.this.personal_count - 1;
                            }
                            if (popupArray != null) {
                                popupArray.remove(messageObject);
                            }
                            newCount = Integer.valueOf(newCount.intValue() - 1);
                        }
                    }
                    if (newCount.intValue() <= 0) {
                        newCount = Integer.valueOf(0);
                        NotificationsController.this.smartNotificationsDialogs.remove(Long.valueOf(dialog_id));
                    }
                    if (!newCount.equals(currentCount)) {
                        NotificationsController.access$420(NotificationsController.this, currentCount.intValue());
                        NotificationsController.access$412(NotificationsController.this, newCount.intValue());
                        NotificationsController.this.pushDialogs.put(Long.valueOf(dialog_id), newCount);
                    }
                    if (newCount.intValue() == 0) {
                        NotificationsController.this.pushDialogs.remove(Long.valueOf(dialog_id));
                        NotificationsController.this.pushDialogsOverrideMention.remove(Long.valueOf(dialog_id));
                        if (!(popupArray == null || !NotificationsController.this.pushMessages.isEmpty() || popupArray.isEmpty())) {
                            popupArray.clear();
                        }
                    }
                }
                if (popupArray != null) {
                    AndroidUtilities.runOnUIThread(new C05431());
                }
                if (old_unread_count != NotificationsController.this.total_unread_count) {
                    if (NotificationsController.this.notifyCheck) {
                        NotificationsController.this.scheduleNotificationDelay(NotificationsController.this.lastOnlineFromOtherDevice > ConnectionsManager.getInstance().getCurrentTime());
                    } else {
                        NotificationsController.this.delayedPushMessages.clear();
                        NotificationsController.this.showOrUpdateNotification(NotificationsController.this.notifyCheck);
                    }
                }
                NotificationsController.this.notifyCheck = false;
                if (preferences.getBoolean("badgeNumber", true)) {
                    NotificationsController.this.setBadge(NotificationsController.this.total_unread_count);
                }
            }
        });
    }

    public void processReadMessages(SparseArray<Long> inbox, long dialog_id, int max_date, int max_id, boolean isPopup) {
        final ArrayList<MessageObject> popupArray = this.popupMessages.isEmpty() ? null : new ArrayList(this.popupMessages);
        final SparseArray<Long> sparseArray = inbox;
        final long j = dialog_id;
        final int i = max_id;
        final int i2 = max_date;
        final boolean z = isPopup;
        this.notificationsQueue.postRunnable(new Runnable() {

            class C05451 implements Runnable {
                C05451() {
                }

                public void run() {
                    NotificationsController.this.popupMessages = popupArray;
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.pushMessagesUpdated, new Object[0]);
                }
            }

            public void run() {
                int a;
                MessageObject messageObject;
                long mid;
                int oldCount = popupArray != null ? popupArray.size() : 0;
                if (sparseArray != null) {
                    for (int b = 0; b < sparseArray.size(); b++) {
                        int key = sparseArray.keyAt(b);
                        long messageId = ((Long) sparseArray.get(key)).longValue();
                        a = 0;
                        while (a < NotificationsController.this.pushMessages.size()) {
                            messageObject = (MessageObject) NotificationsController.this.pushMessages.get(a);
                            if (messageObject.getDialogId() == ((long) key) && messageObject.getId() <= ((int) messageId)) {
                                if (NotificationsController.this.isPersonalMessage(messageObject)) {
                                    NotificationsController.this.personal_count = NotificationsController.this.personal_count - 1;
                                }
                                if (popupArray != null) {
                                    popupArray.remove(messageObject);
                                }
                                mid = (long) messageObject.messageOwner.id;
                                if (messageObject.messageOwner.to_id.channel_id != 0) {
                                    mid |= ((long) messageObject.messageOwner.to_id.channel_id) << 32;
                                }
                                NotificationsController.this.pushMessagesDict.remove(Long.valueOf(mid));
                                NotificationsController.this.delayedPushMessages.remove(messageObject);
                                NotificationsController.this.pushMessages.remove(a);
                                a--;
                            }
                            a++;
                        }
                    }
                    if (!(popupArray == null || !NotificationsController.this.pushMessages.isEmpty() || popupArray.isEmpty())) {
                        popupArray.clear();
                    }
                }
                if (!(j == 0 || (i == 0 && i2 == 0))) {
                    a = 0;
                    while (a < NotificationsController.this.pushMessages.size()) {
                        messageObject = (MessageObject) NotificationsController.this.pushMessages.get(a);
                        if (messageObject.getDialogId() == j) {
                            boolean remove = false;
                            if (i2 != 0) {
                                if (messageObject.messageOwner.date <= i2) {
                                    remove = true;
                                }
                            } else if (z) {
                                if (messageObject.getId() == i || i < 0) {
                                    remove = true;
                                }
                            } else if (messageObject.getId() <= i || i < 0) {
                                remove = true;
                            }
                            if (remove) {
                                if (NotificationsController.this.isPersonalMessage(messageObject)) {
                                    NotificationsController.this.personal_count = NotificationsController.this.personal_count - 1;
                                }
                                NotificationsController.this.pushMessages.remove(a);
                                NotificationsController.this.delayedPushMessages.remove(messageObject);
                                if (popupArray != null) {
                                    popupArray.remove(messageObject);
                                }
                                mid = (long) messageObject.messageOwner.id;
                                if (messageObject.messageOwner.to_id.channel_id != 0) {
                                    mid |= ((long) messageObject.messageOwner.to_id.channel_id) << 32;
                                }
                                NotificationsController.this.pushMessagesDict.remove(Long.valueOf(mid));
                                a--;
                            }
                        }
                        a++;
                    }
                    if (!(popupArray == null || !NotificationsController.this.pushMessages.isEmpty() || popupArray.isEmpty())) {
                        popupArray.clear();
                    }
                }
                if (popupArray != null && oldCount != popupArray.size()) {
                    AndroidUtilities.runOnUIThread(new C05451());
                }
            }
        });
    }

    public void processNewMessages(final ArrayList<MessageObject> messageObjects, final boolean isLast) {
        if (!messageObjects.isEmpty()) {
            final ArrayList<MessageObject> popupArray = new ArrayList(this.popupMessages);
            this.notificationsQueue.postRunnable(new Runnable() {
                public void run() {
                    boolean added = false;
                    int oldCount = popupArray.size();
                    HashMap<Long, Boolean> settingsCache = new HashMap();
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", 0);
                    int popup = 0;
                    for (int a = 0; a < messageObjects.size(); a++) {
                        MessageObject messageObject = (MessageObject) messageObjects.get(a);
                        long mid = (long) messageObject.messageOwner.id;
                        if (messageObject.messageOwner.to_id.channel_id != 0) {
                            mid |= ((long) messageObject.messageOwner.to_id.channel_id) << 32;
                        }
                        if (!NotificationsController.this.pushMessagesDict.containsKey(Long.valueOf(mid))) {
                            long dialog_id = messageObject.getDialogId();
                            long original_dialog_id = dialog_id;
                            if (dialog_id == NotificationsController.this.openned_dialog_id && ApplicationLoader.isScreenOn) {
                                NotificationsController.this.playInChatSound();
                            } else {
                                if (messageObject.messageOwner.mentioned) {
                                    dialog_id = (long) messageObject.messageOwner.from_id;
                                }
                                if (NotificationsController.this.isPersonalMessage(messageObject)) {
                                    NotificationsController.this.personal_count = NotificationsController.this.personal_count + 1;
                                }
                                added = true;
                                Boolean value = (Boolean) settingsCache.get(Long.valueOf(dialog_id));
                                boolean isChat = ((int) dialog_id) < 0;
                                if (((int) dialog_id) == 0) {
                                    popup = 0;
                                } else {
                                    popup = preferences.getInt(isChat ? "popupGroup" : "popupAll", 0);
                                }
                                if (value == null) {
                                    int notifyOverride = NotificationsController.this.getNotifyOverride(preferences, dialog_id);
                                    boolean z = notifyOverride != 2 && ((preferences.getBoolean("EnableAll", true) && (!isChat || preferences.getBoolean("EnableGroup", true))) || notifyOverride != 0);
                                    value = Boolean.valueOf(z);
                                    settingsCache.put(Long.valueOf(dialog_id), value);
                                }
                                if (value.booleanValue()) {
                                    if (popup != 0) {
                                        popupArray.add(0, messageObject);
                                    }
                                    NotificationsController.this.delayedPushMessages.add(messageObject);
                                    NotificationsController.this.pushMessages.add(0, messageObject);
                                    NotificationsController.this.pushMessagesDict.put(Long.valueOf(mid), messageObject);
                                    if (original_dialog_id != dialog_id) {
                                        NotificationsController.this.pushDialogsOverrideMention.put(Long.valueOf(original_dialog_id), Integer.valueOf(1));
                                    }
                                }
                            }
                        }
                    }
                    if (added) {
                        NotificationsController.this.notifyCheck = isLast;
                    }
                    if (!popupArray.isEmpty() && oldCount != popupArray.size() && !AndroidUtilities.needShowPasscode(false)) {
                        final int i = popup;
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            public void run() {
                                NotificationsController.this.popupMessages = popupArray;
                                if (ApplicationLoader.mainInterfacePaused || !(ApplicationLoader.isScreenOn || UserConfig.isWaitingForPasscodeEnter)) {
                                    MessageObject messageObject = (MessageObject) messageObjects.get(0);
                                    if (i == 3 || ((i == 1 && ApplicationLoader.isScreenOn) || (i == 2 && !ApplicationLoader.isScreenOn))) {
                                        Intent popupIntent = new Intent(ApplicationLoader.applicationContext, PopupNotificationActivity.class);
                                        popupIntent.setFlags(268763140);
                                        ApplicationLoader.applicationContext.startActivity(popupIntent);
                                    }
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    public void processDialogsUpdateRead(final HashMap<Long, Integer> dialogsToUpdate) {
        final ArrayList<MessageObject> popupArray = this.popupMessages.isEmpty() ? null : new ArrayList(this.popupMessages);
        this.notificationsQueue.postRunnable(new Runnable() {

            class C05491 implements Runnable {
                C05491() {
                }

                public void run() {
                    NotificationsController.this.popupMessages = popupArray;
                }
            }

            public void run() {
                int old_unread_count = NotificationsController.this.total_unread_count;
                SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", 0);
                for (Entry<Long, Integer> entry : dialogsToUpdate.entrySet()) {
                    long dialog_id = ((Long) entry.getKey()).longValue();
                    int notifyOverride = NotificationsController.this.getNotifyOverride(preferences, dialog_id);
                    if (NotificationsController.this.notifyCheck) {
                        Integer override = (Integer) NotificationsController.this.pushDialogsOverrideMention.get(Long.valueOf(dialog_id));
                        if (override != null && override.intValue() == 1) {
                            NotificationsController.this.pushDialogsOverrideMention.put(Long.valueOf(dialog_id), Integer.valueOf(0));
                            notifyOverride = 1;
                        }
                    }
                    boolean canAddValue = notifyOverride != 2 && ((preferences.getBoolean("EnableAll", true) && (((int) dialog_id) >= 0 || preferences.getBoolean("EnableGroup", true))) || notifyOverride != 0);
                    Integer currentCount = (Integer) NotificationsController.this.pushDialogs.get(Long.valueOf(dialog_id));
                    Integer newCount = (Integer) entry.getValue();
                    if (newCount.intValue() == 0) {
                        NotificationsController.this.smartNotificationsDialogs.remove(Long.valueOf(dialog_id));
                    }
                    if (newCount.intValue() < 0) {
                        if (currentCount != null) {
                            newCount = Integer.valueOf(currentCount.intValue() + newCount.intValue());
                        }
                    }
                    if ((canAddValue || newCount.intValue() == 0) && currentCount != null) {
                        NotificationsController.access$420(NotificationsController.this, currentCount.intValue());
                    }
                    if (newCount.intValue() == 0) {
                        NotificationsController.this.pushDialogs.remove(Long.valueOf(dialog_id));
                        NotificationsController.this.pushDialogsOverrideMention.remove(Long.valueOf(dialog_id));
                        int a = 0;
                        while (a < NotificationsController.this.pushMessages.size()) {
                            MessageObject messageObject = (MessageObject) NotificationsController.this.pushMessages.get(a);
                            if (messageObject.getDialogId() == dialog_id) {
                                if (NotificationsController.this.isPersonalMessage(messageObject)) {
                                    NotificationsController.this.personal_count = NotificationsController.this.personal_count - 1;
                                }
                                NotificationsController.this.pushMessages.remove(a);
                                a--;
                                NotificationsController.this.delayedPushMessages.remove(messageObject);
                                long mid = (long) messageObject.messageOwner.id;
                                if (messageObject.messageOwner.to_id.channel_id != 0) {
                                    mid |= ((long) messageObject.messageOwner.to_id.channel_id) << 32;
                                }
                                NotificationsController.this.pushMessagesDict.remove(Long.valueOf(mid));
                                if (popupArray != null) {
                                    popupArray.remove(messageObject);
                                }
                            }
                            a++;
                        }
                        if (!(popupArray == null || !NotificationsController.this.pushMessages.isEmpty() || popupArray.isEmpty())) {
                            popupArray.clear();
                        }
                    } else if (canAddValue) {
                        NotificationsController.access$412(NotificationsController.this, newCount.intValue());
                        NotificationsController.this.pushDialogs.put(Long.valueOf(dialog_id), newCount);
                    }
                }
                if (popupArray != null) {
                    AndroidUtilities.runOnUIThread(new C05491());
                }
                if (old_unread_count != NotificationsController.this.total_unread_count) {
                    if (NotificationsController.this.notifyCheck) {
                        NotificationsController.this.scheduleNotificationDelay(NotificationsController.this.lastOnlineFromOtherDevice > ConnectionsManager.getInstance().getCurrentTime());
                    } else {
                        NotificationsController.this.delayedPushMessages.clear();
                        NotificationsController.this.showOrUpdateNotification(NotificationsController.this.notifyCheck);
                    }
                }
                NotificationsController.this.notifyCheck = false;
                if (preferences.getBoolean("badgeNumber", true)) {
                    NotificationsController.this.setBadge(NotificationsController.this.total_unread_count);
                }
            }
        });
    }

    public void processLoadedUnreadMessages(final HashMap<Long, Integer> dialogs, final ArrayList<Message> messages, ArrayList<User> users, ArrayList<Chat> chats, ArrayList<EncryptedChat> encryptedChats) {
        MessagesController.getInstance().putUsers(users, true);
        MessagesController.getInstance().putChats(chats, true);
        MessagesController.getInstance().putEncryptedChats(encryptedChats, true);
        this.notificationsQueue.postRunnable(new Runnable() {

            class C05511 implements Runnable {
                C05511() {
                }

                public void run() {
                    NotificationsController.this.popupMessages.clear();
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.pushMessagesUpdated, new Object[0]);
                }
            }

            public void run() {
                Iterator i$;
                long dialog_id;
                Boolean value;
                NotificationsController.this.pushDialogs.clear();
                NotificationsController.this.pushMessages.clear();
                NotificationsController.this.pushMessagesDict.clear();
                NotificationsController.this.total_unread_count = 0;
                NotificationsController.this.personal_count = 0;
                SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", 0);
                HashMap<Long, Boolean> settingsCache = new HashMap();
                if (messages != null) {
                    i$ = messages.iterator();
                    while (i$.hasNext()) {
                        Message message = (Message) i$.next();
                        long mid = (long) message.id;
                        if (message.to_id.channel_id != 0) {
                            mid |= ((long) message.to_id.channel_id) << 32;
                        }
                        if (!NotificationsController.this.pushMessagesDict.containsKey(Long.valueOf(mid))) {
                            MessageObject messageObject = new MessageObject(message, null, false);
                            if (NotificationsController.this.isPersonalMessage(messageObject)) {
                                NotificationsController.this.personal_count = NotificationsController.this.personal_count + 1;
                            }
                            dialog_id = messageObject.getDialogId();
                            long original_dialog_id = dialog_id;
                            if (messageObject.messageOwner.mentioned) {
                                dialog_id = (long) messageObject.messageOwner.from_id;
                            }
                            value = (Boolean) settingsCache.get(Long.valueOf(dialog_id));
                            if (value == null) {
                                int notifyOverride = NotificationsController.this.getNotifyOverride(preferences, dialog_id);
                                boolean z = notifyOverride != 2 && ((preferences.getBoolean("EnableAll", true) && (((int) dialog_id) >= 0 || preferences.getBoolean("EnableGroup", true))) || notifyOverride != 0);
                                value = Boolean.valueOf(z);
                                settingsCache.put(Long.valueOf(dialog_id), value);
                            }
                            if (value.booleanValue() && !(dialog_id == NotificationsController.this.openned_dialog_id && ApplicationLoader.isScreenOn)) {
                                NotificationsController.this.pushMessagesDict.put(Long.valueOf(mid), messageObject);
                                NotificationsController.this.pushMessages.add(0, messageObject);
                                if (original_dialog_id != dialog_id) {
                                    NotificationsController.this.pushDialogsOverrideMention.put(Long.valueOf(original_dialog_id), Integer.valueOf(1));
                                }
                            }
                        }
                    }
                }
                for (Entry<Long, Integer> entry : dialogs.entrySet()) {
                    dialog_id = ((Long) entry.getKey()).longValue();
                    value = (Boolean) settingsCache.get(Long.valueOf(dialog_id));
                    if (value == null) {
                        notifyOverride = NotificationsController.this.getNotifyOverride(preferences, dialog_id);
                        Integer override = (Integer) NotificationsController.this.pushDialogsOverrideMention.get(Long.valueOf(dialog_id));
                        if (override != null && override.intValue() == 1) {
                            NotificationsController.this.pushDialogsOverrideMention.put(Long.valueOf(dialog_id), Integer.valueOf(0));
                            notifyOverride = 1;
                        }
                        z = notifyOverride != 2 && ((preferences.getBoolean("EnableAll", true) && (((int) dialog_id) >= 0 || preferences.getBoolean("EnableGroup", true))) || notifyOverride != 0);
                        value = Boolean.valueOf(z);
                        settingsCache.put(Long.valueOf(dialog_id), value);
                    }
                    if (value.booleanValue()) {
                        int count = ((Integer) entry.getValue()).intValue();
                        NotificationsController.this.pushDialogs.put(Long.valueOf(dialog_id), Integer.valueOf(count));
                        NotificationsController.access$412(NotificationsController.this, count);
                    }
                }
                if (NotificationsController.this.total_unread_count == 0) {
                    AndroidUtilities.runOnUIThread(new C05511());
                }
                NotificationsController.this.showOrUpdateNotification(SystemClock.uptimeMillis() / 1000 < 60);
                if (preferences.getBoolean("badgeNumber", true)) {
                    NotificationsController.this.setBadge(NotificationsController.this.total_unread_count);
                }
            }
        });
    }

    public void setBadgeEnabled(boolean enabled) {
        setBadge(enabled ? this.total_unread_count : 0);
    }

    private void setBadge(final int count) {
        this.notificationsQueue.postRunnable(new Runnable() {

            class C05361 implements Runnable {
                C05361() {
                }

                public void run() {
                    try {
                        Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
                        intent.putExtra("badge_count", count);
                        intent.putExtra("badge_count_package_name", ApplicationLoader.applicationContext.getPackageName());
                        intent.putExtra("badge_count_class_name", NotificationsController.this.launcherClassName);
                        ApplicationLoader.applicationContext.sendBroadcast(intent);
                    } catch (Throwable e) {
                        FileLog.m611e("tmessages", e);
                    }
                }
            }

            public void run() {
                if (NotificationsController.this.lastBadgeCount != count) {
                    NotificationsController.this.lastBadgeCount = count;
                    try {
                        ContentValues cv = new ContentValues();
                        cv.put("tag", "org.telegram.messenger/org.telegram.ui.LaunchActivity");
                        cv.put("count", Integer.valueOf(count));
                        ApplicationLoader.applicationContext.getContentResolver().insert(Uri.parse("content://com.teslacoilsw.notifier/unread_count"), cv);
                    } catch (Throwable th) {
                    }
                    try {
                        if (NotificationsController.this.launcherClassName == null) {
                            NotificationsController.this.launcherClassName = NotificationsController.getLauncherClassName(ApplicationLoader.applicationContext);
                        }
                        if (NotificationsController.this.launcherClassName != null) {
                            AndroidUtilities.runOnUIThread(new C05361());
                        }
                    } catch (Throwable e) {
                        FileLog.m611e("tmessages", e);
                    }
                }
            }
        });
    }

    private String getStringForMessage(MessageObject messageObject, boolean shortMessage) {
        User user;
        Chat chat;
        long dialog_id = messageObject.messageOwner.dialog_id;
        int chat_id = messageObject.messageOwner.to_id.chat_id != 0 ? messageObject.messageOwner.to_id.chat_id : messageObject.messageOwner.to_id.channel_id;
        int from_id = messageObject.messageOwner.to_id.user_id;
        if (from_id == 0) {
            from_id = messageObject.messageOwner.from_id;
        } else if (from_id == UserConfig.getClientUserId()) {
            from_id = messageObject.messageOwner.from_id;
        }
        if (dialog_id == 0) {
            if (chat_id != 0) {
                dialog_id = (long) (-chat_id);
            } else if (from_id != 0) {
                dialog_id = (long) from_id;
            }
        }
        String name = null;
        if (from_id > 0) {
            user = MessagesController.getInstance().getUser(Integer.valueOf(from_id));
            if (user != null) {
                name = UserObject.getUserName(user);
            }
        } else {
            chat = MessagesController.getInstance().getChat(Integer.valueOf(-from_id));
            if (chat != null) {
                name = chat.title;
            }
        }
        if (name == null) {
            return null;
        }
        chat = null;
        if (chat_id != 0) {
            chat = MessagesController.getInstance().getChat(Integer.valueOf(chat_id));
            if (chat == null) {
                return null;
            }
        }
        if (((int) dialog_id) == 0 || AndroidUtilities.needShowPasscode(false) || UserConfig.isWaitingForPasscodeEnter) {
            return LocaleController.getString("YouHaveNewMessage", C0553R.string.YouHaveNewMessage);
        }
        if (chat_id != 0 || from_id == 0) {
            if (chat_id == 0) {
                return null;
            }
            if (!ApplicationLoader.applicationContext.getSharedPreferences("Notifications", 0).getBoolean("EnablePreviewGroup", true)) {
                return LocaleController.formatString("NotificationMessageGroupNoText", C0553R.string.NotificationMessageGroupNoText, name, chat.title);
            } else if (messageObject.messageOwner instanceof TL_messageService) {
                if (messageObject.messageOwner.action instanceof TL_messageActionChatAddUser) {
                    int singleUserId = messageObject.messageOwner.action.user_id;
                    if (singleUserId == 0 && messageObject.messageOwner.action.users.size() == 1) {
                        singleUserId = ((Integer) messageObject.messageOwner.action.users.get(0)).intValue();
                    }
                    if (singleUserId == 0) {
                        StringBuilder names = new StringBuilder("");
                        for (int a = 0; a < messageObject.messageOwner.action.users.size(); a++) {
                            user = MessagesController.getInstance().getUser((Integer) messageObject.messageOwner.action.users.get(a));
                            if (user != null) {
                                String name2 = UserObject.getUserName(user);
                                if (names.length() != 0) {
                                    names.append(", ");
                                }
                                names.append(name2);
                            }
                        }
                        return LocaleController.formatString("NotificationGroupAddMember", C0553R.string.NotificationGroupAddMember, name, chat.title, names.toString());
                    } else if (messageObject.messageOwner.to_id.channel_id != 0 && !messageObject.isMegagroup()) {
                        user = MessagesController.getInstance().getUser(Integer.valueOf(singleUserId));
                        if (user != null) {
                            name = UserObject.getUserName(user);
                        } else {
                            name = "";
                        }
                        return LocaleController.formatString("ChannelAddedByNotification", C0553R.string.ChannelAddedByNotification, name, chat.title);
                    } else if (singleUserId == UserConfig.getClientUserId()) {
                        return LocaleController.formatString("NotificationInvitedToGroup", C0553R.string.NotificationInvitedToGroup, name, chat.title);
                    } else {
                        User u2 = MessagesController.getInstance().getUser(Integer.valueOf(singleUserId));
                        if (u2 == null) {
                            return null;
                        }
                        if (from_id == u2.id) {
                            return LocaleController.formatString("NotificationGroupAddSelf", C0553R.string.NotificationGroupAddSelf, name, chat.title);
                        }
                        return LocaleController.formatString("NotificationGroupAddMember", C0553R.string.NotificationGroupAddMember, name, chat.title, UserObject.getUserName(u2));
                    }
                } else if (messageObject.messageOwner.action instanceof TL_messageActionChatJoinedByLink) {
                    return LocaleController.formatString("NotificationInvitedToGroupByLink", C0553R.string.NotificationInvitedToGroupByLink, name, chat.title);
                } else if (messageObject.messageOwner.action instanceof TL_messageActionChatEditTitle) {
                    return LocaleController.formatString("NotificationEditedGroupName", C0553R.string.NotificationEditedGroupName, name, messageObject.messageOwner.action.title);
                } else if ((messageObject.messageOwner.action instanceof TL_messageActionChatEditPhoto) || (messageObject.messageOwner.action instanceof TL_messageActionChatDeletePhoto)) {
                    if (messageObject.messageOwner.to_id.channel_id == 0 || messageObject.isMegagroup()) {
                        return LocaleController.formatString("NotificationEditedGroupPhoto", C0553R.string.NotificationEditedGroupPhoto, name, chat.title);
                    }
                    return LocaleController.formatString("ChannelPhotoEditNotification", C0553R.string.ChannelPhotoEditNotification, chat.title);
                } else if (messageObject.messageOwner.action instanceof TL_messageActionChatDeleteUser) {
                    if (messageObject.messageOwner.action.user_id == UserConfig.getClientUserId()) {
                        return LocaleController.formatString("NotificationGroupKickYou", C0553R.string.NotificationGroupKickYou, name, chat.title);
                    } else if (messageObject.messageOwner.action.user_id == from_id) {
                        return LocaleController.formatString("NotificationGroupLeftMember", C0553R.string.NotificationGroupLeftMember, name, chat.title);
                    } else {
                        if (MessagesController.getInstance().getUser(Integer.valueOf(messageObject.messageOwner.action.user_id)) == null) {
                            return null;
                        }
                        return LocaleController.formatString("NotificationGroupKickMember", C0553R.string.NotificationGroupKickMember, name, chat.title, UserObject.getUserName(MessagesController.getInstance().getUser(Integer.valueOf(messageObject.messageOwner.action.user_id))));
                    }
                } else if (messageObject.messageOwner.action instanceof TL_messageActionChatCreate) {
                    return messageObject.messageText.toString();
                } else {
                    if (messageObject.messageOwner.action instanceof TL_messageActionChannelCreate) {
                        return messageObject.messageText.toString();
                    }
                    if (messageObject.messageOwner.action instanceof TL_messageActionChatMigrateTo) {
                        return LocaleController.formatString("ActionMigrateFromGroupNotify", C0553R.string.ActionMigrateFromGroupNotify, chat.title);
                    } else if (!(messageObject.messageOwner.action instanceof TL_messageActionChannelMigrateFrom)) {
                        return null;
                    } else {
                        return LocaleController.formatString("ActionMigrateFromGroupNotify", C0553R.string.ActionMigrateFromGroupNotify, messageObject.messageOwner.action.title);
                    }
                }
            } else if (!ChatObject.isChannel(chat) || chat.megagroup) {
                if (messageObject.isMediaEmpty()) {
                    if (shortMessage || messageObject.messageOwner.message == null || messageObject.messageOwner.message.length() == 0) {
                        return LocaleController.formatString("NotificationMessageGroupNoText", C0553R.string.NotificationMessageGroupNoText, name, chat.title);
                    }
                    return LocaleController.formatString("NotificationMessageGroupText", C0553R.string.NotificationMessageGroupText, name, chat.title, messageObject.messageOwner.message);
                } else if (messageObject.messageOwner.media instanceof TL_messageMediaPhoto) {
                    return LocaleController.formatString("NotificationMessageGroupPhoto", C0553R.string.NotificationMessageGroupPhoto, name, chat.title);
                } else if (messageObject.messageOwner.media instanceof TL_messageMediaVideo) {
                    return LocaleController.formatString("NotificationMessageGroupVideo", C0553R.string.NotificationMessageGroupVideo, name, chat.title);
                } else if (messageObject.messageOwner.media instanceof TL_messageMediaContact) {
                    return LocaleController.formatString("NotificationMessageGroupContact", C0553R.string.NotificationMessageGroupContact, name, chat.title);
                } else if ((messageObject.messageOwner.media instanceof TL_messageMediaGeo) || (messageObject.messageOwner.media instanceof TL_messageMediaVenue)) {
                    return LocaleController.formatString("NotificationMessageGroupMap", C0553R.string.NotificationMessageGroupMap, name, chat.title);
                } else if (messageObject.messageOwner.media instanceof TL_messageMediaDocument) {
                    if (messageObject.isSticker()) {
                        return LocaleController.formatString("NotificationMessageGroupSticker", C0553R.string.NotificationMessageGroupSticker, name, chat.title);
                    }
                    return LocaleController.formatString("NotificationMessageGroupDocument", C0553R.string.NotificationMessageGroupDocument, name, chat.title);
                } else if (!(messageObject.messageOwner.media instanceof TL_messageMediaAudio)) {
                    return null;
                } else {
                    return LocaleController.formatString("NotificationMessageGroupAudio", C0553R.string.NotificationMessageGroupAudio, name, chat.title);
                }
            } else if (from_id < 0) {
                if (messageObject.isMediaEmpty()) {
                    if (shortMessage || messageObject.messageOwner.message == null || messageObject.messageOwner.message.length() == 0) {
                        return LocaleController.formatString("ChannelMessageNoText", C0553R.string.ChannelMessageNoText, name, chat.title);
                    }
                    return LocaleController.formatString("NotificationMessageGroupText", C0553R.string.NotificationMessageGroupText, name, chat.title, messageObject.messageOwner.message);
                } else if (messageObject.messageOwner.media instanceof TL_messageMediaPhoto) {
                    return LocaleController.formatString("ChannelMessagePhoto", C0553R.string.ChannelMessagePhoto, name, chat.title);
                } else if (messageObject.messageOwner.media instanceof TL_messageMediaVideo) {
                    return LocaleController.formatString("ChannelMessageVideo", C0553R.string.ChannelMessageVideo, name, chat.title);
                } else if (messageObject.messageOwner.media instanceof TL_messageMediaContact) {
                    return LocaleController.formatString("ChannelMessageContact", C0553R.string.ChannelMessageContact, name, chat.title);
                } else if ((messageObject.messageOwner.media instanceof TL_messageMediaGeo) || (messageObject.messageOwner.media instanceof TL_messageMediaVenue)) {
                    return LocaleController.formatString("ChannelMessageMap", C0553R.string.ChannelMessageMap, name, chat.title);
                } else if (messageObject.messageOwner.media instanceof TL_messageMediaDocument) {
                    if (messageObject.isSticker()) {
                        return LocaleController.formatString("ChannelMessageSticker", C0553R.string.ChannelMessageSticker, name, chat.title);
                    }
                    return LocaleController.formatString("ChannelMessageDocument", C0553R.string.ChannelMessageDocument, name, chat.title);
                } else if (!(messageObject.messageOwner.media instanceof TL_messageMediaAudio)) {
                    return null;
                } else {
                    return LocaleController.formatString("ChannelMessageAudio", C0553R.string.ChannelMessageAudio, name, chat.title);
                }
            } else if (messageObject.isMediaEmpty()) {
                if (shortMessage || messageObject.messageOwner.message == null || messageObject.messageOwner.message.length() == 0) {
                    return LocaleController.formatString("ChannelMessageGroupNoText", C0553R.string.ChannelMessageGroupNoText, name, chat.title);
                }
                return LocaleController.formatString("NotificationMessageGroupText", C0553R.string.NotificationMessageGroupText, name, chat.title, messageObject.messageOwner.message);
            } else if (messageObject.messageOwner.media instanceof TL_messageMediaPhoto) {
                return LocaleController.formatString("ChannelMessageGroupPhoto", C0553R.string.ChannelMessageGroupPhoto, name, chat.title);
            } else if (messageObject.messageOwner.media instanceof TL_messageMediaVideo) {
                return LocaleController.formatString("ChannelMessageGroupVideo", C0553R.string.ChannelMessageGroupVideo, name, chat.title);
            } else if (messageObject.messageOwner.media instanceof TL_messageMediaContact) {
                return LocaleController.formatString("ChannelMessageGroupContact", C0553R.string.ChannelMessageGroupContact, name, chat.title);
            } else if ((messageObject.messageOwner.media instanceof TL_messageMediaGeo) || (messageObject.messageOwner.media instanceof TL_messageMediaVenue)) {
                return LocaleController.formatString("ChannelMessageGroupMap", C0553R.string.ChannelMessageGroupMap, name, chat.title);
            } else if (messageObject.messageOwner.media instanceof TL_messageMediaDocument) {
                if (messageObject.isSticker()) {
                    return LocaleController.formatString("ChannelMessageGroupSticker", C0553R.string.ChannelMessageGroupSticker, name, chat.title);
                }
                return LocaleController.formatString("ChannelMessageGroupDocument", C0553R.string.ChannelMessageGroupDocument, name, chat.title);
            } else if (!(messageObject.messageOwner.media instanceof TL_messageMediaAudio)) {
                return null;
            } else {
                return LocaleController.formatString("ChannelMessageGroupAudio", C0553R.string.ChannelMessageGroupAudio, name, chat.title);
            }
        } else if (!ApplicationLoader.applicationContext.getSharedPreferences("Notifications", 0).getBoolean("EnablePreviewAll", true)) {
            return LocaleController.formatString("NotificationMessageNoText", C0553R.string.NotificationMessageNoText, name);
        } else if (messageObject.messageOwner instanceof TL_messageService) {
            if (messageObject.messageOwner.action instanceof TL_messageActionUserJoined) {
                return LocaleController.formatString("NotificationContactJoined", C0553R.string.NotificationContactJoined, name);
            } else if (messageObject.messageOwner.action instanceof TL_messageActionUserUpdatedPhoto) {
                return LocaleController.formatString("NotificationContactNewPhoto", C0553R.string.NotificationContactNewPhoto, name);
            } else if (!(messageObject.messageOwner.action instanceof TL_messageActionLoginUnknownLocation)) {
                return null;
            } else {
                String date = LocaleController.formatString("formatDateAtTime", C0553R.string.formatDateAtTime, LocaleController.getInstance().formatterYear.format(((long) messageObject.messageOwner.date) * 1000), LocaleController.getInstance().formatterDay.format(((long) messageObject.messageOwner.date) * 1000));
                return LocaleController.formatString("NotificationUnrecognizedDevice", C0553R.string.NotificationUnrecognizedDevice, UserConfig.getCurrentUser().first_name, date, messageObject.messageOwner.action.title, messageObject.messageOwner.action.address);
            }
        } else if (messageObject.isMediaEmpty()) {
            if (shortMessage) {
                return LocaleController.formatString("NotificationMessageNoText", C0553R.string.NotificationMessageNoText, name);
            } else if (messageObject.messageOwner.message == null || messageObject.messageOwner.message.length() == 0) {
                return LocaleController.formatString("NotificationMessageNoText", C0553R.string.NotificationMessageNoText, name);
            } else {
                return LocaleController.formatString("NotificationMessageText", C0553R.string.NotificationMessageText, name, messageObject.messageOwner.message);
            }
        } else if (messageObject.messageOwner.media instanceof TL_messageMediaPhoto) {
            return LocaleController.formatString("NotificationMessagePhoto", C0553R.string.NotificationMessagePhoto, name);
        } else if (messageObject.messageOwner.media instanceof TL_messageMediaVideo) {
            return LocaleController.formatString("NotificationMessageVideo", C0553R.string.NotificationMessageVideo, name);
        } else if (messageObject.messageOwner.media instanceof TL_messageMediaContact) {
            return LocaleController.formatString("NotificationMessageContact", C0553R.string.NotificationMessageContact, name);
        } else if ((messageObject.messageOwner.media instanceof TL_messageMediaGeo) || (messageObject.messageOwner.media instanceof TL_messageMediaVenue)) {
            return LocaleController.formatString("NotificationMessageMap", C0553R.string.NotificationMessageMap, name);
        } else if (messageObject.messageOwner.media instanceof TL_messageMediaDocument) {
            if (messageObject.isSticker()) {
                return LocaleController.formatString("NotificationMessageSticker", C0553R.string.NotificationMessageSticker, name);
            }
            return LocaleController.formatString("NotificationMessageDocument", C0553R.string.NotificationMessageDocument, name);
        } else if (!(messageObject.messageOwner.media instanceof TL_messageMediaAudio)) {
            return null;
        } else {
            return LocaleController.formatString("NotificationMessageAudio", C0553R.string.NotificationMessageAudio, name);
        }
    }

    private void scheduleNotificationRepeat() {
        try {
            PendingIntent pintent = PendingIntent.getService(ApplicationLoader.applicationContext, 0, new Intent(ApplicationLoader.applicationContext, NotificationRepeat.class), 0);
            int minutes = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", 0).getInt("repeat_messages", 60);
            if (minutes <= 0 || this.personal_count <= 0) {
                this.alarmManager.cancel(pintent);
            } else {
                this.alarmManager.set(2, SystemClock.elapsedRealtime() + ((long) ((minutes * 60) * LocationStatusCodes.GEOFENCE_NOT_AVAILABLE)), pintent);
            }
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
        }
    }

    private static String getLauncherClassName(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.addCategory("android.intent.category.LAUNCHER");
            for (ResolveInfo resolveInfo : pm.queryIntentActivities(intent, 0)) {
                if (resolveInfo.activityInfo.applicationInfo.packageName.equalsIgnoreCase(context.getPackageName())) {
                    return resolveInfo.activityInfo.name;
                }
            }
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
        }
        return null;
    }

    private boolean isPersonalMessage(MessageObject messageObject) {
        return messageObject.messageOwner.to_id != null && messageObject.messageOwner.to_id.chat_id == 0 && messageObject.messageOwner.to_id.channel_id == 0 && (messageObject.messageOwner.action == null || (messageObject.messageOwner.action instanceof TL_messageActionEmpty));
    }

    private int getNotifyOverride(SharedPreferences preferences, long dialog_id) {
        int notifyOverride = preferences.getInt("notify2_" + dialog_id, 0);
        if (notifyOverride != 3 || preferences.getInt("notifyuntil_" + dialog_id, 0) < ConnectionsManager.getInstance().getCurrentTime()) {
            return notifyOverride;
        }
        return 2;
    }

    private void dismissNotification() {
        try {
            this.notificationManager.cancel(1);
            this.pushMessages.clear();
            this.pushMessagesDict.clear();
            for (Entry<Long, Integer> entry : this.autoNotificationsIds.entrySet()) {
                this.notificationManager.cancel(((Integer) entry.getValue()).intValue());
            }
            this.autoNotificationsIds.clear();
            for (Entry<Long, Integer> entry2 : this.wearNotificationsIds.entrySet()) {
                this.notificationManager.cancel(((Integer) entry2.getValue()).intValue());
            }
            this.wearNotificationsIds.clear();
            AndroidUtilities.runOnUIThread(new Runnable() {
                public void run() {
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.pushMessagesUpdated, new Object[0]);
                }
            });
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
        }
    }

    private void playInChatSound() {
        if (this.inChatSoundEnabled) {
            try {
                if (this.audioManager.getRingerMode() == 0) {
                    return;
                }
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
            try {
                if (getNotifyOverride(ApplicationLoader.applicationContext.getSharedPreferences("Notifications", 0), this.openned_dialog_id) != 2) {
                    this.notificationsQueue.postRunnable(new Runnable() {

                        class C05371 implements OnLoadCompleteListener {
                            C05371() {
                            }

                            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                                if (status == 0) {
                                    soundPool.play(sampleId, 1.0f, 1.0f, 1, 0, 1.0f);
                                }
                            }
                        }

                        public void run() {
                            if (Math.abs(System.currentTimeMillis() - NotificationsController.this.lastSoundPlay) > 500) {
                                try {
                                    if (NotificationsController.this.soundPool == null) {
                                        NotificationsController.this.soundPool = new SoundPool(2, 1, 0);
                                        NotificationsController.this.soundPool.setOnLoadCompleteListener(new C05371());
                                    }
                                    if (NotificationsController.this.soundIn == 0 && !NotificationsController.this.soundInLoaded) {
                                        NotificationsController.this.soundInLoaded = true;
                                        NotificationsController.this.soundIn = NotificationsController.this.soundPool.load(ApplicationLoader.applicationContext, C0553R.raw.sound_in, 1);
                                    }
                                    if (NotificationsController.this.soundIn != 0) {
                                        NotificationsController.this.soundPool.play(NotificationsController.this.soundIn, 1.0f, 1.0f, 1, 0, 1.0f);
                                    }
                                } catch (Throwable e) {
                                    FileLog.m611e("tmessages", e);
                                }
                            }
                        }
                    });
                }
            } catch (Throwable e2) {
                FileLog.m611e("tmessages", e2);
            }
        }
    }

    private void scheduleNotificationDelay(boolean onlineReason) {
        try {
            FileLog.m609e("tmessages", "delay notification start, onlineReason = " + onlineReason);
            this.notificationDelayWakelock.acquire(10000);
            AndroidUtilities.cancelRunOnUIThread(this.notificationDelayRunnable);
            AndroidUtilities.runOnUIThread(this.notificationDelayRunnable, (long) (onlineReason ? GamesClient.STATUS_ACHIEVEMENT_UNLOCK_FAILURE : LocationStatusCodes.GEOFENCE_NOT_AVAILABLE));
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
            showOrUpdateNotification(this.notifyCheck);
        }
    }

    protected void repeatNotificationMaybe() {
        this.notificationsQueue.postRunnable(new Runnable() {
            public void run() {
                int hour = Calendar.getInstance().get(11);
                if (hour < 11 || hour > 22) {
                    NotificationsController.this.scheduleNotificationRepeat();
                    return;
                }
                NotificationsController.this.notificationManager.cancel(1);
                NotificationsController.this.showOrUpdateNotification(true);
            }
        });
    }

    private void showOrUpdateNotification(boolean notifyAboutLast) {
        if (!UserConfig.isClientActivated() || this.pushMessages.isEmpty()) {
            dismissNotification();
            return;
        }
        try {
            int count;
            String name;
            String detailText;
            ConnectionsManager.getInstance().resumeNetworkMaybe();
            MessageObject lastMessageObject = (MessageObject) this.pushMessages.get(0);
            long dialog_id = lastMessageObject.getDialogId();
            long override_dialog_id = dialog_id;
            if (lastMessageObject.messageOwner.mentioned) {
                override_dialog_id = (long) lastMessageObject.messageOwner.from_id;
            }
            int mid = lastMessageObject.getId();
            int chat_id = lastMessageObject.messageOwner.to_id.chat_id != 0 ? lastMessageObject.messageOwner.to_id.chat_id : lastMessageObject.messageOwner.to_id.channel_id;
            int user_id = lastMessageObject.messageOwner.to_id.user_id;
            if (user_id == 0) {
                user_id = lastMessageObject.messageOwner.from_id;
            } else if (user_id == UserConfig.getClientUserId()) {
                user_id = lastMessageObject.messageOwner.from_id;
            }
            User user = MessagesController.getInstance().getUser(Integer.valueOf(user_id));
            Chat chat = null;
            if (chat_id != 0) {
                chat = MessagesController.getInstance().getChat(Integer.valueOf(chat_id));
            }
            TLObject photoPath = null;
            boolean notifyDisabled = false;
            int needVibrate = 0;
            String choosenSoundPath = null;
            int ledColor = -16711936;
            boolean inAppPreview = false;
            int priority = 0;
            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", 0);
            int notifyOverride = getNotifyOverride(preferences, override_dialog_id);
            if (!notifyAboutLast || notifyOverride == 2 || (!(preferences.getBoolean("EnableAll", true) && (chat_id == 0 || preferences.getBoolean("EnableGroup", true))) && notifyOverride == 0)) {
                notifyDisabled = true;
            }
            if (!(notifyDisabled || dialog_id != override_dialog_id || chat == null)) {
                int notifyMaxCount = preferences.getInt("smart_max_count_" + dialog_id, 2);
                int notifyDelay = preferences.getInt("smart_delay_" + dialog_id, 180);
                if (notifyMaxCount != 0) {
                    Point dialogInfo = (Point) this.smartNotificationsDialogs.get(Long.valueOf(dialog_id));
                    if (dialogInfo == null) {
                        this.smartNotificationsDialogs.put(Long.valueOf(dialog_id), new Point(1, (int) (System.currentTimeMillis() / 1000)));
                    } else if (((long) (dialogInfo.y + notifyDelay)) < System.currentTimeMillis() / 1000) {
                        dialogInfo.set(1, (int) (System.currentTimeMillis() / 1000));
                    } else {
                        count = dialogInfo.x;
                        if (count < notifyMaxCount) {
                            dialogInfo.set(count + 1, (int) (System.currentTimeMillis() / 1000));
                        } else {
                            notifyDisabled = true;
                        }
                    }
                }
            }
            String defaultPath = System.DEFAULT_NOTIFICATION_URI.getPath();
            if (!notifyDisabled) {
                boolean inAppSounds = preferences.getBoolean("EnableInAppSounds", true);
                boolean inAppVibrate = preferences.getBoolean("EnableInAppVibrate", true);
                inAppPreview = preferences.getBoolean("EnableInAppPreview", true);
                boolean inAppPriority = preferences.getBoolean("EnableInAppPriority", false);
                int vibrateOverride = preferences.getInt("vibrate_" + dialog_id, 0);
                int priorityOverride = preferences.getInt("priority_" + dialog_id, 3);
                boolean vibrateOnlyIfSilent = false;
                choosenSoundPath = preferences.getString("sound_path_" + dialog_id, null);
                if (chat_id != 0) {
                    if (choosenSoundPath != null && choosenSoundPath.equals(defaultPath)) {
                        choosenSoundPath = null;
                    } else if (choosenSoundPath == null) {
                        choosenSoundPath = preferences.getString("GroupSoundPath", defaultPath);
                    }
                    needVibrate = preferences.getInt("vibrate_group", 0);
                    priority = preferences.getInt("priority_group", 1);
                    ledColor = preferences.getInt("GroupLed", -16711936);
                } else if (user_id != 0) {
                    if (choosenSoundPath != null && choosenSoundPath.equals(defaultPath)) {
                        choosenSoundPath = null;
                    } else if (choosenSoundPath == null) {
                        choosenSoundPath = preferences.getString("GlobalSoundPath", defaultPath);
                    }
                    needVibrate = preferences.getInt("vibrate_messages", 0);
                    priority = preferences.getInt("priority_group", 1);
                    ledColor = preferences.getInt("MessagesLed", -16711936);
                }
                if (preferences.contains("color_" + dialog_id)) {
                    ledColor = preferences.getInt("color_" + dialog_id, 0);
                }
                if (priorityOverride != 3) {
                    priority = priorityOverride;
                }
                if (needVibrate == 4) {
                    vibrateOnlyIfSilent = true;
                    needVibrate = 0;
                }
                if ((needVibrate == 2 && (vibrateOverride == 1 || vibrateOverride == 3 || vibrateOverride == 5)) || ((needVibrate != 2 && vibrateOverride == 2) || vibrateOverride != 0)) {
                    needVibrate = vibrateOverride;
                }
                if (!ApplicationLoader.mainInterfacePaused) {
                    if (!inAppSounds) {
                        choosenSoundPath = null;
                    }
                    if (!inAppVibrate) {
                        needVibrate = 2;
                    }
                    if (!inAppPriority) {
                        priority = 0;
                    } else if (priority == 2) {
                        priority = 1;
                    }
                }
                if (vibrateOnlyIfSilent && needVibrate != 2) {
                    try {
                        int mode = this.audioManager.getRingerMode();
                        if (!(mode == 0 || mode == 1)) {
                            needVibrate = 2;
                        }
                    } catch (Throwable e) {
                        FileLog.m611e("tmessages", e);
                    }
                }
            }
            Intent intent = new Intent(ApplicationLoader.applicationContext, LaunchActivity.class);
            intent.setAction("com.tmessages.openchat" + Math.random() + ConnectionsManager.DEFAULT_DATACENTER_ID);
            intent.setFlags(32768);
            if (((int) dialog_id) != 0) {
                if (this.pushDialogs.size() == 1) {
                    if (chat_id != 0) {
                        intent.putExtra("chatId", chat_id);
                    } else if (user_id != 0) {
                        intent.putExtra("userId", user_id);
                    }
                }
                if (AndroidUtilities.needShowPasscode(false) || UserConfig.isWaitingForPasscodeEnter) {
                    photoPath = null;
                } else if (this.pushDialogs.size() == 1) {
                    if (chat != null) {
                        if (!(chat.photo == null || chat.photo.photo_small == null || chat.photo.photo_small.volume_id == 0 || chat.photo.photo_small.local_id == 0)) {
                            photoPath = chat.photo.photo_small;
                        }
                    } else if (!(user == null || user.photo == null || user.photo.photo_small == null || user.photo.photo_small.volume_id == 0 || user.photo.photo_small.local_id == 0)) {
                        photoPath = user.photo.photo_small;
                    }
                }
            } else if (this.pushDialogs.size() == 1) {
                intent.putExtra("encId", (int) (dialog_id >> 32));
            }
            PendingIntent contentIntent = PendingIntent.getActivity(ApplicationLoader.applicationContext, 0, intent, 1073741824);
            boolean replace = true;
            if (((int) dialog_id) == 0 || this.pushDialogs.size() > 1 || AndroidUtilities.needShowPasscode(false) || UserConfig.isWaitingForPasscodeEnter) {
                name = LocaleController.getString("AppName", C0553R.string.AppName);
                replace = false;
            } else if (chat != null) {
                name = chat.title;
            } else {
                name = UserObject.getUserName(user);
            }
            if (this.pushDialogs.size() == 1) {
                detailText = LocaleController.formatPluralString("NewMessages", this.total_unread_count);
            } else {
                detailText = LocaleController.formatString("NotificationMessagesPeopleDisplayOrder", C0553R.string.NotificationMessagesPeopleDisplayOrder, LocaleController.formatPluralString("NewMessages", this.total_unread_count), LocaleController.formatPluralString("FromChats", this.pushDialogs.size()));
            }
            Builder mBuilder = new Builder(ApplicationLoader.applicationContext).setContentTitle(name).setSmallIcon(C0553R.drawable.notification).setAutoCancel(true).setNumber(this.total_unread_count).setContentIntent(contentIntent).setGroup("messages").setGroupSummary(true).setColor(-13851168);
            if (!notifyAboutLast) {
                mBuilder.setPriority(-1);
            } else if (priority == 0) {
                mBuilder.setPriority(0);
            } else if (priority == 1) {
                mBuilder.setPriority(1);
            } else if (priority == 2) {
                mBuilder.setPriority(2);
            }
            mBuilder.setCategory("msg");
            if (chat == null && user != null && user.phone != null && user.phone.length() > 0) {
                mBuilder.addPerson("tel:+" + user.phone);
            }
            String lastMessage = null;
            String message;
            if (this.pushMessages.size() == 1) {
                lastMessage = getStringForMessage((MessageObject) this.pushMessages.get(0), false);
                message = lastMessage;
                if (message != null) {
                    if (replace) {
                        if (chat != null) {
                            message = message.replace(" @ " + name, "");
                        } else {
                            message = message.replace(name + ": ", "").replace(name + " ", "");
                        }
                    }
                    mBuilder.setContentText(message);
                    mBuilder.setStyle(new BigTextStyle().bigText(message));
                } else {
                    return;
                }
            }
            mBuilder.setContentText(detailText);
            Style inboxStyle = new InboxStyle();
            inboxStyle.setBigContentTitle(name);
            count = Math.min(10, this.pushMessages.size());
            for (int i = 0; i < count; i++) {
                message = getStringForMessage((MessageObject) this.pushMessages.get(i), false);
                if (message != null) {
                    if (i == 0) {
                        lastMessage = message;
                    }
                    if (this.pushDialogs.size() == 1 && replace) {
                        message = chat != null ? message.replace(" @ " + name, "") : message.replace(name + ": ", "").replace(name + " ", "");
                    }
                    inboxStyle.addLine(message);
                }
            }
            inboxStyle.setSummaryText(detailText);
            mBuilder.setStyle(inboxStyle);
            if (photoPath != null) {
                BitmapDrawable img = ImageLoader.getInstance().getImageFromMemory(photoPath, null, "50_50");
                if (img != null) {
                    mBuilder.setLargeIcon(img.getBitmap());
                }
            }
            long[] jArr;
            if (notifyDisabled) {
                jArr = new long[2];
                mBuilder.setVibrate(new long[]{0, 0});
            } else {
                if (ApplicationLoader.mainInterfacePaused || inAppPreview) {
                    if (lastMessage.length() > 100) {
                        lastMessage = lastMessage.substring(0, 100).replace("\n", " ").trim() + "...";
                    }
                    mBuilder.setTicker(lastMessage);
                }
                if (!(choosenSoundPath == null || choosenSoundPath.equals("NoSound"))) {
                    if (choosenSoundPath.equals(defaultPath)) {
                        mBuilder.setSound(System.DEFAULT_NOTIFICATION_URI, 5);
                    } else {
                        mBuilder.setSound(Uri.parse(choosenSoundPath), 5);
                    }
                }
                if (ledColor != 0) {
                    mBuilder.setLights(ledColor, LocationStatusCodes.GEOFENCE_NOT_AVAILABLE, LocationStatusCodes.GEOFENCE_NOT_AVAILABLE);
                }
                if (needVibrate == 2) {
                    jArr = new long[2];
                    mBuilder.setVibrate(new long[]{0, 0});
                } else if (needVibrate == 1) {
                    jArr = new long[4];
                    mBuilder.setVibrate(new long[]{0, 100, 0, 100});
                } else if (needVibrate == 0 || needVibrate == 4) {
                    mBuilder.setDefaults(2);
                } else if (needVibrate == 3) {
                    jArr = new long[2];
                    mBuilder.setVibrate(new long[]{0, 1000});
                }
            }
            showExtraNotifications(mBuilder, notifyAboutLast);
            this.notificationManager.notify(1, mBuilder.build());
            scheduleNotificationRepeat();
        } catch (Throwable e2) {
            FileLog.m611e("tmessages", e2);
        }
    }

    @SuppressLint({"InlinedApi"})
    private void showExtraNotifications(Builder notificationBuilder, boolean notifyAboutLast) {
        if (VERSION.SDK_INT >= 18) {
            int a;
            long dialog_id;
            ArrayList<Long> sortedDialogs = new ArrayList();
            HashMap<Long, ArrayList<MessageObject>> messagesByDialogs = new HashMap();
            for (a = 0; a < this.pushMessages.size(); a++) {
                MessageObject messageObject = (MessageObject) this.pushMessages.get(a);
                dialog_id = messageObject.getDialogId();
                if (((int) dialog_id) != 0) {
                    ArrayList<MessageObject> arrayList = (ArrayList) messagesByDialogs.get(Long.valueOf(dialog_id));
                    if (arrayList == null) {
                        arrayList = new ArrayList();
                        messagesByDialogs.put(Long.valueOf(dialog_id), arrayList);
                        sortedDialogs.add(0, Long.valueOf(dialog_id));
                    }
                    arrayList.add(messageObject);
                }
            }
            HashMap<Long, Integer> oldIdsWear = new HashMap();
            oldIdsWear.putAll(this.wearNotificationsIds);
            this.wearNotificationsIds.clear();
            HashMap<Long, Integer> oldIdsAuto = new HashMap();
            oldIdsAuto.putAll(this.autoNotificationsIds);
            this.autoNotificationsIds.clear();
            for (int b = 0; b < sortedDialogs.size(); b++) {
                dialog_id = ((Long) sortedDialogs.get(b)).longValue();
                ArrayList<MessageObject> messageObjects = (ArrayList) messagesByDialogs.get(Long.valueOf(dialog_id));
                int max_id = ((MessageObject) messageObjects.get(0)).getId();
                int max_date = ((MessageObject) messageObjects.get(0)).messageOwner.date;
                Chat chat = null;
                User user = null;
                TLObject photoPath;
                String name;
                Integer notificationIdWear;
                int i;
                Integer notificationIdAuto;
                UnreadConversation.Builder unreadConvBuilder;
                Intent msgHeardIntent;
                Action wearReplyAction;
                Intent msgReplyIntent;
                Intent intent;
                PendingIntent replyPendingIntent;
                RemoteInput remoteInputWear;
                String replyToString;
                String text;
                String message;
                Intent intent2;
                PendingIntent contentIntent;
                WearableExtender wearableExtender;
                Builder builder;
                BitmapDrawable img;
                if (dialog_id > 0) {
                    user = MessagesController.getInstance().getUser(Integer.valueOf((int) dialog_id));
                    if (user == null) {
                    }
                    photoPath = null;
                    if (!AndroidUtilities.needShowPasscode(false) || UserConfig.isWaitingForPasscodeEnter) {
                        name = LocaleController.getString("AppName", C0553R.string.AppName);
                    } else {
                        if (chat != null) {
                            name = chat.title;
                        } else {
                            name = UserObject.getUserName(user);
                        }
                        if (chat != null) {
                            if (!(chat.photo == null || chat.photo.photo_small == null || chat.photo.photo_small.volume_id == 0 || chat.photo.photo_small.local_id == 0)) {
                                photoPath = chat.photo.photo_small;
                            }
                        } else if (!(user.photo == null || user.photo.photo_small == null || user.photo.photo_small.volume_id == 0 || user.photo.photo_small.local_id == 0)) {
                            photoPath = user.photo.photo_small;
                        }
                    }
                    notificationIdWear = (Integer) oldIdsWear.get(Long.valueOf(dialog_id));
                    if (notificationIdWear != null) {
                        i = this.wearNotificationId;
                        this.wearNotificationId = i + 1;
                        notificationIdWear = Integer.valueOf(i);
                    } else {
                        oldIdsWear.remove(Long.valueOf(dialog_id));
                    }
                    notificationIdAuto = (Integer) oldIdsAuto.get(Long.valueOf(dialog_id));
                    if (notificationIdAuto != null) {
                        i = this.autoNotificationId;
                        this.autoNotificationId = i + 1;
                        notificationIdAuto = Integer.valueOf(i);
                    } else {
                        oldIdsAuto.remove(Long.valueOf(dialog_id));
                    }
                    unreadConvBuilder = new UnreadConversation.Builder(name).setLatestTimestamp(((long) max_date) * 1000);
                    msgHeardIntent = new Intent();
                    msgHeardIntent.addFlags(32);
                    msgHeardIntent.setAction("org.telegram.messenger.ACTION_MESSAGE_HEARD");
                    msgHeardIntent.putExtra("dialog_id", dialog_id);
                    msgHeardIntent.putExtra("max_id", max_id);
                    unreadConvBuilder.setReadPendingIntent(PendingIntent.getBroadcast(ApplicationLoader.applicationContext, notificationIdAuto.intValue(), msgHeardIntent, 134217728));
                    wearReplyAction = null;
                    if (!(ChatObject.isChannel(chat) || AndroidUtilities.needShowPasscode(false) || UserConfig.isWaitingForPasscodeEnter)) {
                        msgReplyIntent = new Intent();
                        msgReplyIntent.addFlags(32);
                        msgReplyIntent.setAction("org.telegram.messenger.ACTION_MESSAGE_REPLY");
                        msgReplyIntent.putExtra("dialog_id", dialog_id);
                        msgReplyIntent.putExtra("max_id", max_id);
                        unreadConvBuilder.setReplyAction(PendingIntent.getBroadcast(ApplicationLoader.applicationContext, notificationIdAuto.intValue(), msgReplyIntent, 134217728), new RemoteInput.Builder(EXTRA_VOICE_REPLY).setLabel(LocaleController.getString("Reply", C0553R.string.Reply)).build());
                        intent = new Intent(ApplicationLoader.applicationContext, WearReplyReceiver.class);
                        intent.putExtra("dialog_id", dialog_id);
                        intent.putExtra("max_id", max_id);
                        replyPendingIntent = PendingIntent.getBroadcast(ApplicationLoader.applicationContext, notificationIdWear.intValue(), intent, 134217728);
                        remoteInputWear = new RemoteInput.Builder(EXTRA_VOICE_REPLY).setLabel(LocaleController.getString("Reply", C0553R.string.Reply)).build();
                        if (chat == null) {
                            replyToString = LocaleController.formatString("ReplyToGroup", C0553R.string.ReplyToGroup, name);
                        } else {
                            replyToString = LocaleController.formatString("ReplyToUser", C0553R.string.ReplyToUser, name);
                        }
                        wearReplyAction = new Action.Builder(C0553R.drawable.ic_reply_icon, replyToString, replyPendingIntent).addRemoteInput(remoteInputWear).build();
                    }
                    text = "";
                    for (a = messageObjects.size() - 1; a >= 0; a--) {
                        message = getStringForMessage((MessageObject) messageObjects.get(a), false);
                        if (message != null) {
                            if (chat == null) {
                                message = message.replace(" @ " + name, "");
                            } else {
                                message = message.replace(name + ": ", "").replace(name + " ", "");
                            }
                            if (text.length() > 0) {
                                text = text + "\n\n";
                            }
                            text = text + message;
                            unreadConvBuilder.addMessage(message);
                        }
                    }
                    intent2 = new Intent(ApplicationLoader.applicationContext, LaunchActivity.class);
                    intent2.setAction("com.tmessages.openchat" + Math.random() + ConnectionsManager.DEFAULT_DATACENTER_ID);
                    intent2.setFlags(32768);
                    if (chat != null) {
                        intent2.putExtra("chatId", chat.id);
                    } else if (user != null) {
                        intent2.putExtra("userId", user.id);
                    }
                    contentIntent = PendingIntent.getActivity(ApplicationLoader.applicationContext, 0, intent2, 1073741824);
                    wearableExtender = new WearableExtender();
                    if (wearReplyAction != null) {
                        wearableExtender.addAction(wearReplyAction);
                    }
                    builder = new Builder(ApplicationLoader.applicationContext).setContentTitle(name).setSmallIcon(C0553R.drawable.notification).setGroup("messages").setContentText(text).setColor(-13851168).setGroupSummary(false).setContentIntent(contentIntent).extend(wearableExtender).extend(new CarExtender().setUnreadConversation(unreadConvBuilder.build())).setCategory("msg");
                    if (photoPath != null) {
                        img = ImageLoader.getInstance().getImageFromMemory(photoPath, null, "50_50");
                        if (img != null) {
                            builder.setLargeIcon(img.getBitmap());
                        }
                    }
                    if (chat == null && user != null && user.phone != null && user.phone.length() > 0) {
                        builder.addPerson("tel:+" + user.phone);
                    }
                    this.notificationManager.notify(notificationIdWear.intValue(), builder.build());
                    this.wearNotificationsIds.put(Long.valueOf(dialog_id), notificationIdWear);
                } else {
                    chat = MessagesController.getInstance().getChat(Integer.valueOf(-((int) dialog_id)));
                    if (chat == null) {
                    }
                    photoPath = null;
                    if (AndroidUtilities.needShowPasscode(false)) {
                    }
                    name = LocaleController.getString("AppName", C0553R.string.AppName);
                    notificationIdWear = (Integer) oldIdsWear.get(Long.valueOf(dialog_id));
                    if (notificationIdWear != null) {
                        oldIdsWear.remove(Long.valueOf(dialog_id));
                    } else {
                        i = this.wearNotificationId;
                        this.wearNotificationId = i + 1;
                        notificationIdWear = Integer.valueOf(i);
                    }
                    notificationIdAuto = (Integer) oldIdsAuto.get(Long.valueOf(dialog_id));
                    if (notificationIdAuto != null) {
                        oldIdsAuto.remove(Long.valueOf(dialog_id));
                    } else {
                        i = this.autoNotificationId;
                        this.autoNotificationId = i + 1;
                        notificationIdAuto = Integer.valueOf(i);
                    }
                    unreadConvBuilder = new UnreadConversation.Builder(name).setLatestTimestamp(((long) max_date) * 1000);
                    msgHeardIntent = new Intent();
                    msgHeardIntent.addFlags(32);
                    msgHeardIntent.setAction("org.telegram.messenger.ACTION_MESSAGE_HEARD");
                    msgHeardIntent.putExtra("dialog_id", dialog_id);
                    msgHeardIntent.putExtra("max_id", max_id);
                    unreadConvBuilder.setReadPendingIntent(PendingIntent.getBroadcast(ApplicationLoader.applicationContext, notificationIdAuto.intValue(), msgHeardIntent, 134217728));
                    wearReplyAction = null;
                    msgReplyIntent = new Intent();
                    msgReplyIntent.addFlags(32);
                    msgReplyIntent.setAction("org.telegram.messenger.ACTION_MESSAGE_REPLY");
                    msgReplyIntent.putExtra("dialog_id", dialog_id);
                    msgReplyIntent.putExtra("max_id", max_id);
                    unreadConvBuilder.setReplyAction(PendingIntent.getBroadcast(ApplicationLoader.applicationContext, notificationIdAuto.intValue(), msgReplyIntent, 134217728), new RemoteInput.Builder(EXTRA_VOICE_REPLY).setLabel(LocaleController.getString("Reply", C0553R.string.Reply)).build());
                    intent = new Intent(ApplicationLoader.applicationContext, WearReplyReceiver.class);
                    intent.putExtra("dialog_id", dialog_id);
                    intent.putExtra("max_id", max_id);
                    replyPendingIntent = PendingIntent.getBroadcast(ApplicationLoader.applicationContext, notificationIdWear.intValue(), intent, 134217728);
                    remoteInputWear = new RemoteInput.Builder(EXTRA_VOICE_REPLY).setLabel(LocaleController.getString("Reply", C0553R.string.Reply)).build();
                    if (chat == null) {
                        replyToString = LocaleController.formatString("ReplyToUser", C0553R.string.ReplyToUser, name);
                    } else {
                        replyToString = LocaleController.formatString("ReplyToGroup", C0553R.string.ReplyToGroup, name);
                    }
                    wearReplyAction = new Action.Builder(C0553R.drawable.ic_reply_icon, replyToString, replyPendingIntent).addRemoteInput(remoteInputWear).build();
                    text = "";
                    for (a = messageObjects.size() - 1; a >= 0; a--) {
                        message = getStringForMessage((MessageObject) messageObjects.get(a), false);
                        if (message != null) {
                            if (chat == null) {
                                message = message.replace(name + ": ", "").replace(name + " ", "");
                            } else {
                                message = message.replace(" @ " + name, "");
                            }
                            if (text.length() > 0) {
                                text = text + "\n\n";
                            }
                            text = text + message;
                            unreadConvBuilder.addMessage(message);
                        }
                    }
                    intent2 = new Intent(ApplicationLoader.applicationContext, LaunchActivity.class);
                    intent2.setAction("com.tmessages.openchat" + Math.random() + ConnectionsManager.DEFAULT_DATACENTER_ID);
                    intent2.setFlags(32768);
                    if (chat != null) {
                        intent2.putExtra("chatId", chat.id);
                    } else if (user != null) {
                        intent2.putExtra("userId", user.id);
                    }
                    contentIntent = PendingIntent.getActivity(ApplicationLoader.applicationContext, 0, intent2, 1073741824);
                    wearableExtender = new WearableExtender();
                    if (wearReplyAction != null) {
                        wearableExtender.addAction(wearReplyAction);
                    }
                    builder = new Builder(ApplicationLoader.applicationContext).setContentTitle(name).setSmallIcon(C0553R.drawable.notification).setGroup("messages").setContentText(text).setColor(-13851168).setGroupSummary(false).setContentIntent(contentIntent).extend(wearableExtender).extend(new CarExtender().setUnreadConversation(unreadConvBuilder.build())).setCategory("msg");
                    if (photoPath != null) {
                        img = ImageLoader.getInstance().getImageFromMemory(photoPath, null, "50_50");
                        if (img != null) {
                            builder.setLargeIcon(img.getBitmap());
                        }
                    }
                    builder.addPerson("tel:+" + user.phone);
                    this.notificationManager.notify(notificationIdWear.intValue(), builder.build());
                    this.wearNotificationsIds.put(Long.valueOf(dialog_id), notificationIdWear);
                }
            }
            for (Entry<Long, Integer> entry : oldIdsWear.entrySet()) {
                this.notificationManager.cancel(((Integer) entry.getValue()).intValue());
            }
        }
    }

    public void playOutChatSound() {
        if (this.inChatSoundEnabled) {
            try {
                if (this.audioManager.getRingerMode() == 0) {
                    return;
                }
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
            this.notificationsQueue.postRunnable(new Runnable() {

                class C05381 implements OnLoadCompleteListener {
                    C05381() {
                    }

                    public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                        if (status == 0) {
                            soundPool.play(sampleId, 1.0f, 1.0f, 1, 0, 1.0f);
                        }
                    }
                }

                public void run() {
                    try {
                        if (Math.abs(System.currentTimeMillis() - NotificationsController.this.lastSoundOutPlay) > 100) {
                            NotificationsController.this.lastSoundOutPlay = System.currentTimeMillis();
                            if (NotificationsController.this.soundPool == null) {
                                NotificationsController.this.soundPool = new SoundPool(2, 1, 0);
                                NotificationsController.this.soundPool.setOnLoadCompleteListener(new C05381());
                            }
                            if (NotificationsController.this.soundOut == 0 && !NotificationsController.this.soundOutLoaded) {
                                NotificationsController.this.soundOutLoaded = true;
                                NotificationsController.this.soundOut = NotificationsController.this.soundPool.load(ApplicationLoader.applicationContext, C0553R.raw.sound_out, 1);
                            }
                            if (NotificationsController.this.soundOut != 0) {
                                NotificationsController.this.soundPool.play(NotificationsController.this.soundOut, 1.0f, 1.0f, 1, 0, 1.0f);
                            }
                        }
                    } catch (Throwable e) {
                        FileLog.m611e("tmessages", e);
                    }
                }
            });
        }
    }

    public static void updateServerNotificationsSettings(long dialog_id) {
        int i = 0;
        NotificationCenter.getInstance().postNotificationName(NotificationCenter.notificationsSettingsUpdated, new Object[0]);
        if (((int) dialog_id) != 0) {
            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", 0);
            TL_account_updateNotifySettings req = new TL_account_updateNotifySettings();
            req.settings = new TL_inputPeerNotifySettings();
            req.settings.sound = "default";
            req.settings.events_mask = 0;
            int mute_type = preferences.getInt("notify2_" + dialog_id, 0);
            if (mute_type == 3) {
                req.settings.mute_until = preferences.getInt("notifyuntil_" + dialog_id, 0);
            } else {
                TL_inputPeerNotifySettings tL_inputPeerNotifySettings = req.settings;
                if (mute_type == 2) {
                    i = ConnectionsManager.DEFAULT_DATACENTER_ID;
                }
                tL_inputPeerNotifySettings.mute_until = i;
            }
            req.settings.show_previews = preferences.getBoolean("preview_" + dialog_id, true);
            req.peer = new TL_inputNotifyPeer();
            ((TL_inputNotifyPeer) req.peer).peer = MessagesController.getInputPeer((int) dialog_id);
            if (((TL_inputNotifyPeer) req.peer).peer != null) {
                ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                    public void run(TLObject response, TL_error error) {
                    }
                });
            }
        }
    }
}
