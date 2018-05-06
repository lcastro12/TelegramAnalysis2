package org.telegram.messenger;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Base64;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.location.LocationStatusCodes;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.concurrent.atomic.AtomicInteger;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.SerializedData;
import org.telegram.ui.Components.ForegroundDetector;

public class ApplicationLoader extends Application {
    public static final String EXTRA_MESSAGE = "message";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String PROPERTY_APP_VERSION = "appVersion";
    public static final String PROPERTY_REG_ID = "registration_id";
    public static volatile Context applicationContext;
    public static volatile Handler applicationHandler;
    private static volatile boolean applicationInited = false;
    private static Drawable cachedWallpaper;
    private static boolean isCustomTheme;
    public static volatile boolean isScreenOn = false;
    public static volatile boolean mainInterfacePaused = true;
    private static int selectedColor;
    private static final Object sync = new Object();
    private GoogleCloudMessaging gcm;
    private AtomicInteger msgId = new AtomicInteger();
    private String regid;

    static class C03031 implements Runnable {
        C03031() {
        }

        public void run() {
            synchronized (ApplicationLoader.sync) {
                int selectedColor = 0;
                try {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0);
                    int selectedBackground = preferences.getInt("selectedBackground", 1000001);
                    selectedColor = preferences.getInt("selectedColor", 0);
                    if (selectedColor == 0) {
                        if (selectedBackground == 1000001) {
                            ApplicationLoader.cachedWallpaper = ApplicationLoader.applicationContext.getResources().getDrawable(C0553R.drawable.background_hd);
                            ApplicationLoader.isCustomTheme = false;
                        } else {
                            File toFile = new File(ApplicationLoader.getFilesDirFixed(), "wallpaper.jpg");
                            if (toFile.exists()) {
                                ApplicationLoader.cachedWallpaper = Drawable.createFromPath(toFile.getAbsolutePath());
                                ApplicationLoader.isCustomTheme = true;
                            } else {
                                ApplicationLoader.cachedWallpaper = ApplicationLoader.applicationContext.getResources().getDrawable(C0553R.drawable.background_hd);
                                ApplicationLoader.isCustomTheme = false;
                            }
                        }
                    }
                } catch (Throwable th) {
                }
                if (ApplicationLoader.cachedWallpaper == null) {
                    if (selectedColor == 0) {
                        selectedColor = -2693905;
                    }
                    ApplicationLoader.cachedWallpaper = new ColorDrawable(selectedColor);
                }
            }
        }
    }

    class C03042 implements Runnable {
        C03042() {
        }

        public void run() {
            if (ApplicationLoader.this.checkPlayServices()) {
                ApplicationLoader.this.gcm = GoogleCloudMessaging.getInstance(ApplicationLoader.this);
                ApplicationLoader.this.regid = ApplicationLoader.this.getRegistrationId();
                if (ApplicationLoader.this.regid.length() == 0) {
                    ApplicationLoader.this.registerInBackground();
                    return;
                } else {
                    ApplicationLoader.this.sendRegistrationIdToBackend(false);
                    return;
                }
            }
            FileLog.m608d("tmessages", "No valid Google Play Services APK found.");
        }
    }

    class C03053 extends AsyncTask<String, String, Boolean> {
        C03053() {
        }

        protected Boolean doInBackground(String... objects) {
            if (ApplicationLoader.this.gcm == null) {
                ApplicationLoader.this.gcm = GoogleCloudMessaging.getInstance(ApplicationLoader.applicationContext);
            }
            int count = 0;
            while (count < LocationStatusCodes.GEOFENCE_NOT_AVAILABLE) {
                count++;
                try {
                    ApplicationLoader.this.regid = ApplicationLoader.this.gcm.register(BuildVars.GCM_SENDER_ID);
                    ApplicationLoader.this.sendRegistrationIdToBackend(true);
                    ApplicationLoader.this.storeRegistrationId(ApplicationLoader.applicationContext, ApplicationLoader.this.regid);
                    return Boolean.valueOf(true);
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                    try {
                        if (count % 20 == 0) {
                            Thread.sleep(1800000);
                        } else {
                            Thread.sleep(5000);
                        }
                    } catch (Throwable e2) {
                        FileLog.m611e("tmessages", e2);
                    }
                }
            }
            return Boolean.valueOf(false);
        }
    }

    public static boolean isCustomTheme() {
        return isCustomTheme;
    }

    public static int getSelectedColor() {
        return selectedColor;
    }

    public static void reloadWallpaper() {
        cachedWallpaper = null;
        loadWallpaper();
    }

    public static void loadWallpaper() {
        if (cachedWallpaper == null) {
            Utilities.searchQueue.postRunnable(new C03031());
        }
    }

    public static Drawable getCachedWallpaper() {
        Drawable drawable;
        synchronized (sync) {
            drawable = cachedWallpaper;
        }
        return drawable;
    }

    private static void convertConfig() {
        SharedPreferences preferences = applicationContext.getSharedPreferences("dataconfig", 0);
        if (preferences.contains("currentDatacenterId")) {
            boolean z;
            SerializedData buffer = new SerializedData(32768);
            buffer.writeInt32(2);
            if (preferences.getInt("datacenterSetId", 0) != 0) {
                z = true;
            } else {
                z = false;
            }
            buffer.writeBool(z);
            buffer.writeBool(true);
            buffer.writeInt32(preferences.getInt("currentDatacenterId", 0));
            buffer.writeInt32(preferences.getInt("timeDifference", 0));
            buffer.writeInt32(preferences.getInt("lastDcUpdateTime", 0));
            buffer.writeInt64(preferences.getLong("pushSessionId", 0));
            buffer.writeBool(false);
            buffer.writeInt32(0);
            try {
                String datacentersString = preferences.getString("datacenters", null);
                if (datacentersString != null) {
                    byte[] datacentersBytes = Base64.decode(datacentersString, 0);
                    if (datacentersBytes != null) {
                        SerializedData data = new SerializedData(datacentersBytes);
                        buffer.writeInt32(data.readInt32(false));
                        buffer.writeBytes(datacentersBytes, 4, datacentersBytes.length - 4);
                        data.cleanup();
                    }
                }
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
            try {
                RandomAccessFile fileOutputStream = new RandomAccessFile(new File(getFilesDirFixed(), "tgnet.dat"), "rws");
                byte[] bytes = buffer.toByteArray();
                fileOutputStream.writeInt(Integer.reverseBytes(bytes.length));
                fileOutputStream.write(bytes);
                fileOutputStream.close();
            } catch (Throwable e2) {
                FileLog.m611e("tmessages", e2);
            }
            buffer.cleanup();
            preferences.edit().clear().commit();
        }
    }

    public static File getFilesDirFixed() {
        for (int a = 0; a < 10; a++) {
            File path = applicationContext.getFilesDir();
            if (path != null) {
                return path;
            }
        }
        try {
            path = new File(applicationContext.getApplicationInfo().dataDir, "files");
            path.mkdirs();
            return path;
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
            return new File("/data/data/org.telegram.messenger/files");
        }
    }

    public static void postInitApplication() {
        if (!applicationInited) {
            String langCode;
            String deviceModel;
            String appVersion;
            String systemVersion;
            applicationInited = true;
            convertConfig();
            try {
                LocaleController.getInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                IntentFilter filter = new IntentFilter("android.intent.action.SCREEN_ON");
                filter.addAction("android.intent.action.SCREEN_OFF");
                applicationContext.registerReceiver(new ScreenReceiver(), filter);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            try {
                isScreenOn = ((PowerManager) applicationContext.getSystemService("power")).isScreenOn();
                FileLog.m609e("tmessages", "screen state = " + isScreenOn);
            } catch (Throwable e3) {
                FileLog.m611e("tmessages", e3);
            }
            UserConfig.loadConfig();
            String configPath = getFilesDirFixed().toString();
            try {
                langCode = LocaleController.getLocaleString(LocaleController.getInstance().getSystemDefaultLocale());
                deviceModel = Build.MANUFACTURER + Build.MODEL;
                PackageInfo pInfo = applicationContext.getPackageManager().getPackageInfo(applicationContext.getPackageName(), 0);
                appVersion = pInfo.versionName + " (" + pInfo.versionCode + ")";
                systemVersion = "SDK " + VERSION.SDK_INT;
            } catch (Exception e4) {
                langCode = "en";
                deviceModel = "Android unknown";
                appVersion = "App version unknown";
                systemVersion = "SDK " + VERSION.SDK_INT;
            }
            if (langCode.trim().length() == 0) {
                langCode = "en";
            }
            if (deviceModel.trim().length() == 0) {
                deviceModel = "Android unknown";
            }
            if (appVersion.trim().length() == 0) {
                appVersion = "App version unknown";
            }
            if (systemVersion.trim().length() == 0) {
                systemVersion = "SDK Unknown";
            }
            MessagesController.getInstance();
            ConnectionsManager.getInstance().init(BuildVars.BUILD_VERSION, 43, BuildVars.APP_ID, deviceModel, systemVersion, appVersion, langCode, configPath, FileLog.getNetworkLogPath(), UserConfig.getClientUserId());
            if (UserConfig.getCurrentUser() != null) {
                MessagesController.getInstance().putUser(UserConfig.getCurrentUser(), true);
                ConnectionsManager.getInstance().applyCountryPortNumber(UserConfig.getCurrentUser().phone);
                MessagesController.getInstance().getBlockedUsers(true);
                SendMessagesHelper.getInstance().checkUnsentMessages();
            }
            ((ApplicationLoader) applicationContext).initPlayServices();
            FileLog.m609e("tmessages", "app initied");
            ContactsController.getInstance().checkAppAccount();
            MediaController.getInstance();
        }
    }

    public void onCreate() {
        super.onCreate();
        if (VERSION.SDK_INT < 11) {
            System.setProperty("java.net.preferIPv4Stack", "true");
            System.setProperty("java.net.preferIPv6Addresses", "false");
        }
        applicationContext = getApplicationContext();
        NativeLoader.initNativeLibs(applicationContext);
        boolean z = VERSION.SDK_INT == 14 || VERSION.SDK_INT == 15;
        ConnectionsManager.native_setJava(z);
        if (VERSION.SDK_INT >= 14) {
            ForegroundDetector foregroundDetector = new ForegroundDetector(this);
        }
        applicationHandler = new Handler(applicationContext.getMainLooper());
        startPushService();
    }

    public static void startPushService() {
        if (applicationContext.getSharedPreferences("Notifications", 0).getBoolean("pushService", true)) {
            applicationContext.startService(new Intent(applicationContext, NotificationsService.class));
            if (VERSION.SDK_INT >= 19) {
                ((AlarmManager) applicationContext.getSystemService("alarm")).cancel(PendingIntent.getService(applicationContext, 0, new Intent(applicationContext, NotificationsService.class), 0));
                return;
            }
            return;
        }
        stopPushService();
    }

    public static void stopPushService() {
        applicationContext.stopService(new Intent(applicationContext, NotificationsService.class));
        ((AlarmManager) applicationContext.getSystemService("alarm")).cancel(PendingIntent.getService(applicationContext, 0, new Intent(applicationContext, NotificationsService.class), 0));
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        try {
            LocaleController.getInstance().onDeviceConfigurationChange(newConfig);
            AndroidUtilities.checkDisplaySize();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initPlayServices() {
        AndroidUtilities.runOnUIThread(new C03042(), 1000);
    }

    private boolean checkPlayServices() {
        return GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == 0;
    }

    private String getRegistrationId() {
        SharedPreferences prefs = getGCMPreferences(applicationContext);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.length() == 0) {
            FileLog.m608d("tmessages", "Registration not found.");
            return "";
        } else if (prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE) == BuildVars.BUILD_VERSION) {
            return registrationId;
        } else {
            FileLog.m608d("tmessages", "App version changed.");
            return "";
        }
    }

    private SharedPreferences getGCMPreferences(Context context) {
        return getSharedPreferences(ApplicationLoader.class.getSimpleName(), 0);
    }

    private void registerInBackground() {
        AsyncTask<String, String, Boolean> task = new C03053();
        if (VERSION.SDK_INT >= 11) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new String[]{null, null, null});
            return;
        }
        task.execute(new String[]{null, null, null});
    }

    private void sendRegistrationIdToBackend(final boolean isNew) {
        Utilities.stageQueue.postRunnable(new Runnable() {

            class C03061 implements Runnable {
                C03061() {
                }

                public void run() {
                    MessagesController.getInstance().registerForPush(ApplicationLoader.this.regid);
                }
            }

            public void run() {
                UserConfig.pushString = ApplicationLoader.this.regid;
                UserConfig.registeredForPush = !isNew;
                UserConfig.saveConfig(false);
                if (UserConfig.getClientUserId() != 0) {
                    AndroidUtilities.runOnUIThread(new C03061());
                }
            }
        });
    }

    private void storeRegistrationId(Context context, String regId) {
        SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = BuildVars.BUILD_VERSION;
        FileLog.m609e("tmessages", "Saving regId on app version " + appVersion);
        Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }
}
