package org.telegram.messenger;

import android.util.SparseArray;
import java.util.ArrayList;
import java.util.Iterator;

public class NotificationCenter {
    public static final int FileDidFailUpload;
    public static final int FileDidFailedLoad;
    public static final int FileDidLoaded;
    public static final int FileDidUpload;
    public static final int FileLoadProgressChanged;
    public static final int FileNewChunkAvailable;
    public static final int FilePreparingFailed;
    public static final int FilePreparingStarted;
    public static final int FileUploadProgressChanged;
    private static volatile NotificationCenter Instance = null;
    public static final int albumsDidLoaded;
    public static final int appDidLogout;
    public static final int audioDidReset;
    public static final int audioDidSent;
    public static final int audioDidStarted;
    public static final int audioPlayStateChanged;
    public static final int audioProgressDidChanged;
    public static final int audioRouteChanged;
    public static final int blockedUsersDidLoaded;
    public static final int botInfoDidLoaded;
    public static final int botKeyboardDidLoaded;
    public static final int chatDidCreated;
    public static final int chatDidFailCreate;
    public static final int chatInfoCantLoad;
    public static final int chatInfoDidLoaded;
    public static final int chatSearchResultsAvailable;
    public static final int closeChats;
    public static final int closeOtherAppActivities;
    public static final int contactsDidLoaded;
    public static final int dialogsNeedReload;
    public static final int didCreatedNewDeleteTask;
    public static final int didLoadedReplyMessages;
    public static final int didReceiveSmsCode;
    public static final int didReceivedNewMessages;
    public static final int didReceivedWebpages;
    public static final int didReceivedWebpagesInUpdates;
    public static final int didReplacedPhotoInMemCache;
    public static final int didSetPasscode;
    public static final int didSetTwoStepPassword;
    public static final int didUpdatedConnectionState;
    public static final int didUpdatedMessagesViews;
    public static final int emojiDidLoaded;
    public static final int encryptedChatCreated;
    public static final int encryptedChatUpdated;
    public static final int httpFileDidFailedLoad;
    public static final int httpFileDidLoaded;
    public static final int mainUserInfoChanged;
    public static final int mediaCountDidLoaded;
    public static final int mediaDidLoaded;
    public static final int messageReceivedByAck;
    public static final int messageReceivedByServer;
    public static final int messageSendError;
    public static final int messageThumbGenerated;
    public static final int messagesDeleted;
    public static final int messagesDidLoaded;
    public static final int messagesRead;
    public static final int messagesReadContent;
    public static final int messagesReadEncrypted;
    public static final int musicDidLoaded;
    public static final int needReloadRecentDialogsSearch;
    public static final int needShowAlert;
    public static final int newSessionReceived;
    public static final int notificationsSettingsUpdated;
    public static final int openedChatChanged;
    public static final int privacyRulesUpdated;
    public static final int pushMessagesUpdated;
    public static final int recentImagesDidLoaded;
    public static final int recordProgressChanged;
    public static final int recordStartError;
    public static final int recordStarted;
    public static final int recordStopped;
    public static final int removeAllMessagesFromDialog;
    public static final int replaceMessagesObjects;
    public static final int screenStateChanged;
    public static final int screenshotTook;
    public static final int stickersDidLoaded;
    public static final int stopEncodingService;
    private static int totalEvents;
    public static final int updateInterfaces;
    public static final int updateMessageMedia;
    public static final int userPhotosLoaded;
    public static final int wallpapersDidLoaded;
    private SparseArray<ArrayList<Object>> addAfterBroadcast = new SparseArray();
    private boolean animationInProgress;
    private int broadcasting = 0;
    private ArrayList<DelayedPost> delayedPosts = new ArrayList(10);
    private SparseArray<ArrayList<Object>> observers = new SparseArray();
    private SparseArray<ArrayList<Object>> removeAfterBroadcast = new SparseArray();

    private class DelayedPost {
        private Object[] args;
        private int id;

        private DelayedPost(int id, Object[] args) {
            this.id = id;
            this.args = args;
        }
    }

    public interface NotificationCenterDelegate {
        void didReceivedNotification(int i, Object... objArr);
    }

    static {
        totalEvents = 1;
        int i = totalEvents;
        totalEvents = i + 1;
        didReceivedNewMessages = i;
        i = totalEvents;
        totalEvents = i + 1;
        updateInterfaces = i;
        i = totalEvents;
        totalEvents = i + 1;
        dialogsNeedReload = i;
        i = totalEvents;
        totalEvents = i + 1;
        closeChats = i;
        i = totalEvents;
        totalEvents = i + 1;
        messagesDeleted = i;
        i = totalEvents;
        totalEvents = i + 1;
        messagesRead = i;
        i = totalEvents;
        totalEvents = i + 1;
        messagesDidLoaded = i;
        i = totalEvents;
        totalEvents = i + 1;
        messageReceivedByAck = i;
        i = totalEvents;
        totalEvents = i + 1;
        messageReceivedByServer = i;
        i = totalEvents;
        totalEvents = i + 1;
        messageSendError = i;
        i = totalEvents;
        totalEvents = i + 1;
        contactsDidLoaded = i;
        i = totalEvents;
        totalEvents = i + 1;
        chatDidCreated = i;
        i = totalEvents;
        totalEvents = i + 1;
        chatDidFailCreate = i;
        i = totalEvents;
        totalEvents = i + 1;
        chatInfoDidLoaded = i;
        i = totalEvents;
        totalEvents = i + 1;
        chatInfoCantLoad = i;
        i = totalEvents;
        totalEvents = i + 1;
        mediaDidLoaded = i;
        i = totalEvents;
        totalEvents = i + 1;
        mediaCountDidLoaded = i;
        i = totalEvents;
        totalEvents = i + 1;
        encryptedChatUpdated = i;
        i = totalEvents;
        totalEvents = i + 1;
        messagesReadEncrypted = i;
        i = totalEvents;
        totalEvents = i + 1;
        encryptedChatCreated = i;
        i = totalEvents;
        totalEvents = i + 1;
        userPhotosLoaded = i;
        i = totalEvents;
        totalEvents = i + 1;
        removeAllMessagesFromDialog = i;
        i = totalEvents;
        totalEvents = i + 1;
        notificationsSettingsUpdated = i;
        i = totalEvents;
        totalEvents = i + 1;
        pushMessagesUpdated = i;
        i = totalEvents;
        totalEvents = i + 1;
        blockedUsersDidLoaded = i;
        i = totalEvents;
        totalEvents = i + 1;
        openedChatChanged = i;
        i = totalEvents;
        totalEvents = i + 1;
        stopEncodingService = i;
        i = totalEvents;
        totalEvents = i + 1;
        didCreatedNewDeleteTask = i;
        i = totalEvents;
        totalEvents = i + 1;
        mainUserInfoChanged = i;
        i = totalEvents;
        totalEvents = i + 1;
        privacyRulesUpdated = i;
        i = totalEvents;
        totalEvents = i + 1;
        updateMessageMedia = i;
        i = totalEvents;
        totalEvents = i + 1;
        recentImagesDidLoaded = i;
        i = totalEvents;
        totalEvents = i + 1;
        replaceMessagesObjects = i;
        i = totalEvents;
        totalEvents = i + 1;
        didSetPasscode = i;
        i = totalEvents;
        totalEvents = i + 1;
        didSetTwoStepPassword = i;
        i = totalEvents;
        totalEvents = i + 1;
        screenStateChanged = i;
        i = totalEvents;
        totalEvents = i + 1;
        didLoadedReplyMessages = i;
        i = totalEvents;
        totalEvents = i + 1;
        newSessionReceived = i;
        i = totalEvents;
        totalEvents = i + 1;
        didReceivedWebpages = i;
        i = totalEvents;
        totalEvents = i + 1;
        didReceivedWebpagesInUpdates = i;
        i = totalEvents;
        totalEvents = i + 1;
        stickersDidLoaded = i;
        i = totalEvents;
        totalEvents = i + 1;
        didReplacedPhotoInMemCache = i;
        i = totalEvents;
        totalEvents = i + 1;
        messagesReadContent = i;
        i = totalEvents;
        totalEvents = i + 1;
        botInfoDidLoaded = i;
        i = totalEvents;
        totalEvents = i + 1;
        botKeyboardDidLoaded = i;
        i = totalEvents;
        totalEvents = i + 1;
        chatSearchResultsAvailable = i;
        i = totalEvents;
        totalEvents = i + 1;
        musicDidLoaded = i;
        i = totalEvents;
        totalEvents = i + 1;
        needShowAlert = i;
        i = totalEvents;
        totalEvents = i + 1;
        didUpdatedMessagesViews = i;
        i = totalEvents;
        totalEvents = i + 1;
        needReloadRecentDialogsSearch = i;
        i = totalEvents;
        totalEvents = i + 1;
        httpFileDidLoaded = i;
        i = totalEvents;
        totalEvents = i + 1;
        httpFileDidFailedLoad = i;
        i = totalEvents;
        totalEvents = i + 1;
        messageThumbGenerated = i;
        i = totalEvents;
        totalEvents = i + 1;
        wallpapersDidLoaded = i;
        i = totalEvents;
        totalEvents = i + 1;
        closeOtherAppActivities = i;
        i = totalEvents;
        totalEvents = i + 1;
        didUpdatedConnectionState = i;
        i = totalEvents;
        totalEvents = i + 1;
        didReceiveSmsCode = i;
        i = totalEvents;
        totalEvents = i + 1;
        emojiDidLoaded = i;
        i = totalEvents;
        totalEvents = i + 1;
        appDidLogout = i;
        i = totalEvents;
        totalEvents = i + 1;
        FileDidUpload = i;
        i = totalEvents;
        totalEvents = i + 1;
        FileDidFailUpload = i;
        i = totalEvents;
        totalEvents = i + 1;
        FileUploadProgressChanged = i;
        i = totalEvents;
        totalEvents = i + 1;
        FileLoadProgressChanged = i;
        i = totalEvents;
        totalEvents = i + 1;
        FileDidLoaded = i;
        i = totalEvents;
        totalEvents = i + 1;
        FileDidFailedLoad = i;
        i = totalEvents;
        totalEvents = i + 1;
        FilePreparingStarted = i;
        i = totalEvents;
        totalEvents = i + 1;
        FileNewChunkAvailable = i;
        i = totalEvents;
        totalEvents = i + 1;
        FilePreparingFailed = i;
        i = totalEvents;
        totalEvents = i + 1;
        audioProgressDidChanged = i;
        i = totalEvents;
        totalEvents = i + 1;
        audioDidReset = i;
        i = totalEvents;
        totalEvents = i + 1;
        audioPlayStateChanged = i;
        i = totalEvents;
        totalEvents = i + 1;
        recordProgressChanged = i;
        i = totalEvents;
        totalEvents = i + 1;
        recordStarted = i;
        i = totalEvents;
        totalEvents = i + 1;
        recordStartError = i;
        i = totalEvents;
        totalEvents = i + 1;
        recordStopped = i;
        i = totalEvents;
        totalEvents = i + 1;
        screenshotTook = i;
        i = totalEvents;
        totalEvents = i + 1;
        albumsDidLoaded = i;
        i = totalEvents;
        totalEvents = i + 1;
        audioDidSent = i;
        i = totalEvents;
        totalEvents = i + 1;
        audioDidStarted = i;
        i = totalEvents;
        totalEvents = i + 1;
        audioRouteChanged = i;
    }

    public static NotificationCenter getInstance() {
        NotificationCenter localInstance = Instance;
        if (localInstance == null) {
            synchronized (NotificationCenter.class) {
                try {
                    localInstance = Instance;
                    if (localInstance == null) {
                        NotificationCenter localInstance2 = new NotificationCenter();
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

    public void setAnimationInProgress(boolean value) {
        this.animationInProgress = value;
        if (!this.animationInProgress && !this.delayedPosts.isEmpty()) {
            Iterator i$ = this.delayedPosts.iterator();
            while (i$.hasNext()) {
                DelayedPost delayedPost = (DelayedPost) i$.next();
                postNotificationNameInternal(delayedPost.id, true, delayedPost.args);
            }
            this.delayedPosts.clear();
        }
    }

    public void postNotificationName(int id, Object... args) {
        boolean allowDuringAnimation = false;
        if (id == chatInfoDidLoaded || id == dialogsNeedReload || id == closeChats || id == messagesDidLoaded || id == mediaCountDidLoaded || id == mediaDidLoaded || id == botInfoDidLoaded || id == botKeyboardDidLoaded) {
            allowDuringAnimation = true;
        }
        postNotificationNameInternal(id, allowDuringAnimation, args);
    }

    public void postNotificationNameInternal(int id, boolean allowDuringAnimation, Object... args) {
        if (BuildVars.DEBUG_VERSION && Thread.currentThread() != ApplicationLoader.applicationHandler.getLooper().getThread()) {
            throw new RuntimeException("postNotificationName allowed only from MAIN thread");
        } else if (allowDuringAnimation || !this.animationInProgress) {
            int a;
            this.broadcasting++;
            ArrayList<Object> objects = (ArrayList) this.observers.get(id);
            if (!(objects == null || objects.isEmpty())) {
                for (a = 0; a < objects.size(); a++) {
                    ((NotificationCenterDelegate) objects.get(a)).didReceivedNotification(id, args);
                }
            }
            this.broadcasting--;
            if (this.broadcasting == 0) {
                int key;
                ArrayList<Object> arrayList;
                int b;
                if (this.removeAfterBroadcast.size() != 0) {
                    for (a = 0; a < this.removeAfterBroadcast.size(); a++) {
                        key = this.removeAfterBroadcast.keyAt(a);
                        arrayList = (ArrayList) this.removeAfterBroadcast.get(key);
                        for (b = 0; b < arrayList.size(); b++) {
                            removeObserver(arrayList.get(b), key);
                        }
                    }
                    this.removeAfterBroadcast.clear();
                }
                if (this.addAfterBroadcast.size() != 0) {
                    for (a = 0; a < this.addAfterBroadcast.size(); a++) {
                        key = this.addAfterBroadcast.keyAt(a);
                        arrayList = (ArrayList) this.addAfterBroadcast.get(key);
                        for (b = 0; b < arrayList.size(); b++) {
                            addObserver(arrayList.get(b), key);
                        }
                    }
                    this.addAfterBroadcast.clear();
                }
            }
        } else {
            this.delayedPosts.add(new DelayedPost(id, args));
            if (BuildVars.DEBUG_VERSION) {
                FileLog.m609e("tmessages", "delay post notification " + id + " with args count = " + args.length);
            }
        }
    }

    public void addObserver(Object observer, int id) {
        if (BuildVars.DEBUG_VERSION && Thread.currentThread() != ApplicationLoader.applicationHandler.getLooper().getThread()) {
            throw new RuntimeException("addObserver allowed only from MAIN thread");
        } else if (this.broadcasting != 0) {
            ArrayList<Object> arrayList = (ArrayList) this.addAfterBroadcast.get(id);
            if (arrayList == null) {
                arrayList = new ArrayList();
                this.addAfterBroadcast.put(id, arrayList);
            }
            arrayList.add(observer);
        } else {
            ArrayList<Object> objects = (ArrayList) this.observers.get(id);
            if (objects == null) {
                SparseArray sparseArray = this.observers;
                objects = new ArrayList();
                sparseArray.put(id, objects);
            }
            if (!objects.contains(observer)) {
                objects.add(observer);
            }
        }
    }

    public void removeObserver(Object observer, int id) {
        if (BuildVars.DEBUG_VERSION && Thread.currentThread() != ApplicationLoader.applicationHandler.getLooper().getThread()) {
            throw new RuntimeException("removeObserver allowed only from MAIN thread");
        } else if (this.broadcasting != 0) {
            ArrayList<Object> arrayList = (ArrayList) this.removeAfterBroadcast.get(id);
            if (arrayList == null) {
                arrayList = new ArrayList();
                this.removeAfterBroadcast.put(id, arrayList);
            }
            arrayList.add(observer);
        } else {
            ArrayList<Object> objects = (ArrayList) this.observers.get(id);
            if (objects != null) {
                objects.remove(observer);
            }
        }
    }
}
