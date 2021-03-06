package org.telegram.tgnet;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Build.VERSION;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.aspectj.lang.JoinPoint;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.TLRPC.TL_config;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.Updates;

public class ConnectionsManager {
    public static final int ConnectionStateConnected = 3;
    public static final int ConnectionStateConnecting = 1;
    public static final int ConnectionStateUpdating = 4;
    public static final int ConnectionStateWaitingForNetwork = 2;
    public static final int ConnectionTypeDownload = 2;
    public static final int ConnectionTypeDownload2 = 65538;
    public static final int ConnectionTypeGeneric = 1;
    public static final int ConnectionTypePush = 8;
    public static final int ConnectionTypeUpload = 4;
    public static final int DEFAULT_DATACENTER_ID = Integer.MAX_VALUE;
    private static volatile ConnectionsManager Instance = null;
    public static final int RequestFlagCanCompress = 4;
    public static final int RequestFlagEnableUnauthorized = 1;
    public static final int RequestFlagFailOnServerErrors = 2;
    public static final int RequestFlagForceDownload = 32;
    public static final int RequestFlagInvokeAfter = 64;
    public static final int RequestFlagNeedQuickAck = 128;
    public static final int RequestFlagTryDifferentDc = 16;
    public static final int RequestFlagWithoutLogin = 8;
    private boolean appPaused = true;
    private int connectionState = native_getConnectionState();
    private boolean isUpdating = false;
    private int lastClassGuid = 1;
    private long lastPauseTime = System.currentTimeMillis();
    private AtomicInteger lastRequestToken = new AtomicInteger(1);
    private WakeLock wakeLock = null;

    class C06782 extends BroadcastReceiver {
        C06782() {
        }

        public void onReceive(Context context, Intent intent) {
            ConnectionsManager.this.checkConnection();
        }
    }

    static class C06793 implements Runnable {
        C06793() {
        }

        public void run() {
            if (ConnectionsManager.getInstance().wakeLock.isHeld()) {
                FileLog.m608d("tmessages", "release wakelock");
                ConnectionsManager.getInstance().wakeLock.release();
            }
        }
    }

    static class C06815 implements Runnable {
        C06815() {
        }

        public void run() {
            MessagesController.getInstance().updateTimerProc();
        }
    }

    static class C06826 implements Runnable {
        C06826() {
        }

        public void run() {
            MessagesController.getInstance().getDifference();
        }
    }

    static class C06848 implements Runnable {
        C06848() {
        }

        public void run() {
            if (UserConfig.getClientUserId() != 0) {
                UserConfig.clearConfig();
                MessagesController.getInstance().performLogout(false);
            }
        }
    }

    public static native void native_applyDatacenterAddress(int i, String str, int i2);

    public static native void native_bindRequestToGuid(int i, int i2);

    public static native void native_cancelRequest(int i, boolean z);

    public static native void native_cancelRequestsForGuid(int i);

    public static native void native_cleanUp();

    public static native int native_getConnectionState();

    public static native int native_getCurrentTime();

    public static native long native_getCurrentTimeMillis();

    public static native int native_getTimeDifference();

    public static native void native_init(int i, int i2, int i3, String str, String str2, String str3, String str4, String str5, String str6, int i4);

    public static native void native_pauseNetwork();

    public static native void native_resumeNetwork(boolean z);

    public static native void native_sendRequest(int i, RequestDelegateInternal requestDelegateInternal, QuickAckDelegate quickAckDelegate, int i2, int i3, int i4, boolean z, int i5);

    public static native void native_setJava(boolean z);

    public static native void native_setNetworkAvailable(boolean z);

    public static native void native_setUseIpv6(boolean z);

    public static native void native_setUserId(int i);

    public static native void native_switchBackend();

    public static native void native_updateDcSettings();

    public static ConnectionsManager getInstance() {
        ConnectionsManager localInstance = Instance;
        if (localInstance == null) {
            synchronized (ConnectionsManager.class) {
                try {
                    localInstance = Instance;
                    if (localInstance == null) {
                        ConnectionsManager localInstance2 = new ConnectionsManager();
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

    public ConnectionsManager() {
        try {
            this.wakeLock = ((PowerManager) ApplicationLoader.applicationContext.getSystemService("power")).newWakeLock(1, JoinPoint.SYNCHRONIZATION_LOCK);
            this.wakeLock.setReferenceCounted(false);
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
        }
    }

    public long getCurrentTimeMillis() {
        return native_getCurrentTimeMillis();
    }

    public int getCurrentTime() {
        return native_getCurrentTime();
    }

    public int getTimeDifference() {
        return native_getTimeDifference();
    }

    public int sendRequest(TLObject object, RequestDelegate completionBlock) {
        return sendRequest(object, completionBlock, null, 0);
    }

    public int sendRequest(TLObject object, RequestDelegate completionBlock, int flags) {
        return sendRequest(object, completionBlock, null, flags, DEFAULT_DATACENTER_ID, 1, true);
    }

    public int sendRequest(TLObject object, RequestDelegate completionBlock, int flags, int connetionType) {
        return sendRequest(object, completionBlock, null, flags, DEFAULT_DATACENTER_ID, connetionType, true);
    }

    public int sendRequest(TLObject object, RequestDelegate completionBlock, QuickAckDelegate quickAckBlock, int flags) {
        return sendRequest(object, completionBlock, quickAckBlock, flags, DEFAULT_DATACENTER_ID, 1, true);
    }

    public int sendRequest(TLObject object, RequestDelegate onComplete, QuickAckDelegate onQuickAck, int flags, int datacenterId, int connetionType, boolean immediate) {
        final int requestToken = this.lastRequestToken.getAndIncrement();
        final TLObject tLObject = object;
        final RequestDelegate requestDelegate = onComplete;
        final QuickAckDelegate quickAckDelegate = onQuickAck;
        final int i = flags;
        final int i2 = datacenterId;
        final int i3 = connetionType;
        final boolean z = immediate;
        Utilities.stageQueue.postRunnable(new Runnable() {

            class C14921 implements RequestDelegateInternal {
                C14921() {
                }

                public void run(int response, int errorCode, String errorText) {
                    Throwable e;
                    TLObject resp = null;
                    TL_error error = null;
                    if (response != 0) {
                        try {
                            NativeByteBuffer buff = NativeByteBuffer.wrap(response);
                            resp = tLObject.deserializeResponse(buff, buff.readInt32(true), true);
                        } catch (Exception e2) {
                            e = e2;
                            FileLog.m611e("tmessages", e);
                            return;
                        }
                    } else if (errorText != null) {
                        TL_error error2 = new TL_error();
                        try {
                            error2.code = errorCode;
                            error2.text = errorText;
                            FileLog.m609e("tmessages", tLObject + " got error " + error2.code + " " + error2.text);
                            error = error2;
                        } catch (Exception e3) {
                            e = e3;
                            error = error2;
                            FileLog.m611e("tmessages", e);
                            return;
                        }
                    }
                    FileLog.m608d("tmessages", "java received " + resp + " error = " + error);
                    final TLObject finalResponse = resp;
                    final TL_error finalError = error;
                    Utilities.stageQueue.postRunnable(new Runnable() {
                        public void run() {
                            requestDelegate.run(finalResponse, finalError);
                            if (finalResponse != null) {
                                finalResponse.freeResources();
                            }
                        }
                    });
                }
            }

            public void run() {
                FileLog.m608d("tmessages", "send request " + tLObject + " with token = " + requestToken);
                NativeByteBuffer buffer = new NativeByteBuffer(tLObject.getObjectSize());
                tLObject.serializeToStream(buffer);
                tLObject.freeResources();
                ConnectionsManager.native_sendRequest(buffer.address, new C14921(), quickAckDelegate, i, i2, i3, z, requestToken);
            }
        });
        return requestToken;
    }

    public void cancelRequest(int token, boolean notifyServer) {
        native_cancelRequest(token, notifyServer);
    }

    public void cleanUp() {
        native_cleanUp();
    }

    public void cancelRequestsForGuid(int guid) {
        native_cancelRequestsForGuid(guid);
    }

    public void bindRequestToGuid(int requestToken, int guid) {
        native_bindRequestToGuid(requestToken, guid);
    }

    public void applyDatacenterAddress(int datacenterId, String ipAddress, int port) {
        native_applyDatacenterAddress(datacenterId, ipAddress, port);
    }

    public int getConnectionState() {
        if (this.connectionState == 3 && this.isUpdating) {
            return 4;
        }
        return this.connectionState;
    }

    public void setUserId(int id) {
        native_setUserId(id);
    }

    private void checkConnection() {
        native_setUseIpv6(useIpv6Address());
        native_setNetworkAvailable(isNetworkOnline());
    }

    public void init(int version, int layer, int apiId, String deviceModel, String systemVersion, String appVersion, String langCode, String configPath, String logPath, int userId) {
        native_init(version, layer, apiId, deviceModel, systemVersion, appVersion, langCode, configPath, logPath, userId);
        checkConnection();
        ApplicationLoader.applicationContext.registerReceiver(new C06782(), new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
    }

    public void switchBackend() {
        native_switchBackend();
    }

    public void resumeNetworkMaybe() {
        native_resumeNetwork(true);
    }

    public void updateDcSettings() {
        native_updateDcSettings();
    }

    public long getPauseTime() {
        return this.lastPauseTime;
    }

    public void setAppPaused(boolean value, boolean byScreenState) {
        if (!byScreenState) {
            this.appPaused = value;
            FileLog.m608d("tmessages", "app paused = " + value);
        }
        if (value) {
            if (this.lastPauseTime == 0) {
                this.lastPauseTime = System.currentTimeMillis();
            }
            native_pauseNetwork();
        } else if (!this.appPaused) {
            FileLog.m609e("tmessages", "reset app pause time");
            if (this.lastPauseTime != 0 && System.currentTimeMillis() - this.lastPauseTime > 5000) {
                ContactsController.getInstance().checkContacts();
            }
            this.lastPauseTime = 0;
            native_resumeNetwork(false);
        }
    }

    public static void onUnparsedMessageReceived(int address) {
        try {
            NativeByteBuffer buff = NativeByteBuffer.wrap(address);
            final TLObject message = TLClassStore.Instance().TLdeserialize(buff, buff.readInt32(true), true);
            if (message instanceof Updates) {
                FileLog.m608d("tmessages", "java received " + message);
                AndroidUtilities.runOnUIThread(new C06793());
                Utilities.stageQueue.postRunnable(new Runnable() {
                    public void run() {
                        MessagesController.getInstance().processUpdates((Updates) message, false);
                    }
                });
            }
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
        }
    }

    public static void onUpdate() {
        Utilities.stageQueue.postRunnable(new C06815());
    }

    public static void onSessionCreated() {
        Utilities.stageQueue.postRunnable(new C06826());
    }

    public static void onConnectionStateChanged(final int state) {
        AndroidUtilities.runOnUIThread(new Runnable() {
            public void run() {
                ConnectionsManager.getInstance().connectionState = state;
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.didUpdatedConnectionState, new Object[0]);
            }
        });
    }

    public static void onLogout() {
        AndroidUtilities.runOnUIThread(new C06848());
    }

    public static void onUpdateConfig(int address) {
        try {
            NativeByteBuffer buff = NativeByteBuffer.wrap(address);
            final TL_config message = TL_config.TLdeserialize(buff, buff.readInt32(true), true);
            if (message != null) {
                Utilities.stageQueue.postRunnable(new Runnable() {
                    public void run() {
                        MessagesController.getInstance().updateConfig(message);
                    }
                });
            }
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
        }
    }

    public static void onInternalPushReceived() {
        AndroidUtilities.runOnUIThread(new Runnable() {
            public void run() {
                try {
                    ConnectionsManager.getInstance().wakeLock.acquire(20000);
                    FileLog.m608d("tmessages", "acquire wakelock");
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
        });
    }

    public int generateClassGuid() {
        int i = this.lastClassGuid;
        this.lastClassGuid = i + 1;
        return i;
    }

    public static boolean isRoaming() {
        try {
            NetworkInfo netInfo = ((ConnectivityManager) ApplicationLoader.applicationContext.getSystemService("connectivity")).getActiveNetworkInfo();
            if (netInfo != null) {
                return netInfo.isRoaming();
            }
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
        }
        return false;
    }

    public static boolean isConnectedToWiFi() {
        try {
            NetworkInfo netInfo = ((ConnectivityManager) ApplicationLoader.applicationContext.getSystemService("connectivity")).getNetworkInfo(1);
            if (netInfo != null && netInfo.getState() == State.CONNECTED) {
                return true;
            }
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
        }
        return false;
    }

    public void applyCountryPortNumber(String number) {
    }

    public void setIsUpdating(final boolean value) {
        AndroidUtilities.runOnUIThread(new Runnable() {
            public void run() {
                if (ConnectionsManager.this.isUpdating != value) {
                    ConnectionsManager.this.isUpdating = value;
                    if (ConnectionsManager.this.connectionState == 3) {
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.didUpdatedConnectionState, new Object[0]);
                    }
                }
            }
        });
    }

    @SuppressLint({"NewApi"})
    protected static boolean useIpv6Address() {
        if (VERSION.SDK_INT < 19) {
            return false;
        }
        Enumeration<NetworkInterface> networkInterfaces;
        NetworkInterface networkInterface;
        List<InterfaceAddress> interfaceAddresses;
        int a;
        InetAddress inetAddress;
        if (BuildVars.DEBUG_VERSION) {
            try {
                networkInterfaces = NetworkInterface.getNetworkInterfaces();
                while (networkInterfaces.hasMoreElements()) {
                    networkInterface = (NetworkInterface) networkInterfaces.nextElement();
                    if (!(!networkInterface.isUp() || networkInterface.isLoopback() || networkInterface.getInterfaceAddresses().isEmpty())) {
                        FileLog.m609e("tmessages", "valid interface: " + networkInterface);
                        interfaceAddresses = networkInterface.getInterfaceAddresses();
                        for (a = 0; a < interfaceAddresses.size(); a++) {
                            inetAddress = ((InterfaceAddress) interfaceAddresses.get(a)).getAddress();
                            if (BuildVars.DEBUG_VERSION) {
                                FileLog.m609e("tmessages", "address: " + inetAddress.getHostAddress());
                            }
                            if (!(inetAddress.isLinkLocalAddress() || inetAddress.isLoopbackAddress() || inetAddress.isMulticastAddress() || !BuildVars.DEBUG_VERSION)) {
                                FileLog.m609e("tmessages", "address is good");
                            }
                        }
                    }
                }
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
        }
        try {
            networkInterfaces = NetworkInterface.getNetworkInterfaces();
            boolean hasIpv4 = false;
            boolean hasIpv6 = false;
            while (networkInterfaces.hasMoreElements()) {
                networkInterface = (NetworkInterface) networkInterfaces.nextElement();
                if (networkInterface.isUp() && !networkInterface.isLoopback()) {
                    interfaceAddresses = networkInterface.getInterfaceAddresses();
                    for (a = 0; a < interfaceAddresses.size(); a++) {
                        inetAddress = ((InterfaceAddress) interfaceAddresses.get(a)).getAddress();
                        if (!(inetAddress.isLinkLocalAddress() || inetAddress.isLoopbackAddress() || inetAddress.isMulticastAddress())) {
                            if (inetAddress instanceof Inet6Address) {
                                hasIpv6 = true;
                            } else if ((inetAddress instanceof Inet4Address) && !inetAddress.getHostAddress().startsWith("192.0.0.")) {
                                hasIpv4 = true;
                            }
                        }
                    }
                }
            }
            if (hasIpv4 || !hasIpv6) {
                return false;
            }
            return true;
        } catch (Throwable e2) {
            FileLog.m611e("tmessages", e2);
            return false;
        }
    }

    public static boolean isNetworkOnline() {
        try {
            ConnectivityManager cm = (ConnectivityManager) ApplicationLoader.applicationContext.getSystemService("connectivity");
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && (netInfo.isConnectedOrConnecting() || netInfo.isAvailable())) {
                return true;
            }
            netInfo = cm.getNetworkInfo(0);
            if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                return true;
            }
            netInfo = cm.getNetworkInfo(1);
            if (netInfo == null || !netInfo.isConnectedOrConnecting()) {
                return false;
            }
            return true;
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
            return true;
        }
    }
}
