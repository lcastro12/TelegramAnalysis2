package org.telegram.messenger;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Base64;
import java.io.File;
import net.hockeyapp.android.utils.HttpURLConnectionBuilder;
import org.telegram.tgnet.SerializedData;
import org.telegram.tgnet.TLRPC.User;

public class UserConfig {
    public static boolean appLocked = false;
    public static int autoLockIn = 3600;
    public static boolean blockedUsersLoaded = false;
    public static String contactsHash = "";
    public static int contactsVersion = 1;
    private static User currentUser;
    public static String importHash = "";
    public static boolean isWaitingForPasscodeEnter = false;
    public static int lastBroadcastId = -1;
    public static int lastContactsSyncTime;
    public static int lastLocalId = -210000;
    public static int lastPauseTime = 0;
    public static int lastSendMessageId = -210000;
    public static int lastUpdateVersion;
    public static long migrateOffsetAccess = -1;
    public static int migrateOffsetChannelId = -1;
    public static int migrateOffsetChatId = -1;
    public static int migrateOffsetDate = -1;
    public static int migrateOffsetId = -1;
    public static int migrateOffsetUserId = -1;
    public static String passcodeHash = "";
    public static byte[] passcodeSalt = new byte[0];
    public static int passcodeType = 0;
    public static String pushString = "";
    public static boolean registeredForPush = false;
    public static boolean saveIncomingPhotos = false;
    private static final Object sync = new Object();
    public static boolean useFingerprint = true;

    public static int getNewMessageId() {
        int id;
        synchronized (sync) {
            id = lastSendMessageId;
            lastSendMessageId--;
        }
        return id;
    }

    public static void saveConfig(boolean withFile) {
        saveConfig(withFile, null);
    }

    public static void saveConfig(boolean withFile, File oldFile) {
        synchronized (sync) {
            try {
                Editor editor = ApplicationLoader.applicationContext.getSharedPreferences("userconfing", 0).edit();
                editor.putBoolean("registeredForPush", registeredForPush);
                editor.putString("pushString", pushString);
                editor.putInt("lastSendMessageId", lastSendMessageId);
                editor.putInt("lastLocalId", lastLocalId);
                editor.putString("contactsHash", contactsHash);
                editor.putString("importHash", importHash);
                editor.putBoolean("saveIncomingPhotos", saveIncomingPhotos);
                editor.putInt("contactsVersion", contactsVersion);
                editor.putInt("lastBroadcastId", lastBroadcastId);
                editor.putBoolean("blockedUsersLoaded", blockedUsersLoaded);
                editor.putString("passcodeHash1", passcodeHash);
                editor.putString("passcodeSalt", passcodeSalt.length > 0 ? Base64.encodeToString(passcodeSalt, 0) : "");
                editor.putBoolean("appLocked", appLocked);
                editor.putInt("passcodeType", passcodeType);
                editor.putInt("autoLockIn", autoLockIn);
                editor.putInt("lastPauseTime", lastPauseTime);
                editor.putInt("lastUpdateVersion", lastUpdateVersion);
                editor.putInt("lastContactsSyncTime", lastContactsSyncTime);
                editor.putBoolean("useFingerprint", useFingerprint);
                editor.putInt("migrateOffsetId", migrateOffsetId);
                if (migrateOffsetId != -1) {
                    editor.putInt("migrateOffsetDate", migrateOffsetDate);
                    editor.putInt("migrateOffsetUserId", migrateOffsetUserId);
                    editor.putInt("migrateOffsetChatId", migrateOffsetChatId);
                    editor.putInt("migrateOffsetChannelId", migrateOffsetChannelId);
                    editor.putLong("migrateOffsetAccess", migrateOffsetAccess);
                }
                if (currentUser == null) {
                    editor.remove("user");
                } else if (withFile) {
                    SerializedData data = new SerializedData();
                    currentUser.serializeToStream(data);
                    editor.putString("user", Base64.encodeToString(data.toByteArray(), 0));
                    data.cleanup();
                }
                editor.commit();
                if (oldFile != null) {
                    oldFile.delete();
                }
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
        }
    }

    public static boolean isClientActivated() {
        boolean z;
        synchronized (sync) {
            z = currentUser != null;
        }
        return z;
    }

    public static int getClientUserId() {
        int i;
        synchronized (sync) {
            i = currentUser != null ? currentUser.id : 0;
        }
        return i;
    }

    public static User getCurrentUser() {
        User user;
        synchronized (sync) {
            user = currentUser;
        }
        return user;
    }

    public static void setCurrentUser(User user) {
        synchronized (sync) {
            currentUser = user;
        }
    }

    public static void loadConfig() {
        synchronized (sync) {
            final File configFile = new File(ApplicationLoader.getFilesDirFixed(), "user.dat");
            SerializedData data;
            SharedPreferences preferences;
            if (configFile.exists()) {
                try {
                    data = new SerializedData(configFile);
                    int ver = data.readInt32(false);
                    if (ver == 1) {
                        currentUser = User.TLdeserialize(data, data.readInt32(false), false);
                        MessagesStorage.lastDateValue = data.readInt32(false);
                        MessagesStorage.lastPtsValue = data.readInt32(false);
                        MessagesStorage.lastSeqValue = data.readInt32(false);
                        registeredForPush = data.readBool(false);
                        pushString = data.readString(false);
                        lastSendMessageId = data.readInt32(false);
                        lastLocalId = data.readInt32(false);
                        contactsHash = data.readString(false);
                        importHash = data.readString(false);
                        saveIncomingPhotos = data.readBool(false);
                        contactsVersion = 0;
                        MessagesStorage.lastQtsValue = data.readInt32(false);
                        MessagesStorage.lastSecretVersion = data.readInt32(false);
                        if (data.readInt32(false) == 1) {
                            MessagesStorage.secretPBytes = data.readByteArray(false);
                        }
                        MessagesStorage.secretG = data.readInt32(false);
                        Utilities.stageQueue.postRunnable(new Runnable() {
                            public void run() {
                                UserConfig.saveConfig(true, configFile);
                            }
                        });
                    } else if (ver == 2) {
                        currentUser = User.TLdeserialize(data, data.readInt32(false), false);
                        preferences = ApplicationLoader.applicationContext.getSharedPreferences("userconfing", 0);
                        registeredForPush = preferences.getBoolean("registeredForPush", false);
                        pushString = preferences.getString("pushString", "");
                        lastSendMessageId = preferences.getInt("lastSendMessageId", -210000);
                        lastLocalId = preferences.getInt("lastLocalId", -210000);
                        contactsHash = preferences.getString("contactsHash", "");
                        importHash = preferences.getString("importHash", "");
                        saveIncomingPhotos = preferences.getBoolean("saveIncomingPhotos", false);
                        contactsVersion = preferences.getInt("contactsVersion", 0);
                    }
                    if (lastLocalId > -210000) {
                        lastLocalId = -210000;
                    }
                    if (lastSendMessageId > -210000) {
                        lastSendMessageId = -210000;
                    }
                    data.cleanup();
                    Utilities.stageQueue.postRunnable(new Runnable() {
                        public void run() {
                            UserConfig.saveConfig(true, configFile);
                        }
                    });
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            } else {
                preferences = ApplicationLoader.applicationContext.getSharedPreferences("userconfing", 0);
                registeredForPush = preferences.getBoolean("registeredForPush", false);
                pushString = preferences.getString("pushString", "");
                lastSendMessageId = preferences.getInt("lastSendMessageId", -210000);
                lastLocalId = preferences.getInt("lastLocalId", -210000);
                contactsHash = preferences.getString("contactsHash", "");
                importHash = preferences.getString("importHash", "");
                saveIncomingPhotos = preferences.getBoolean("saveIncomingPhotos", false);
                contactsVersion = preferences.getInt("contactsVersion", 0);
                lastBroadcastId = preferences.getInt("lastBroadcastId", -1);
                blockedUsersLoaded = preferences.getBoolean("blockedUsersLoaded", false);
                passcodeHash = preferences.getString("passcodeHash1", "");
                appLocked = preferences.getBoolean("appLocked", false);
                passcodeType = preferences.getInt("passcodeType", 0);
                autoLockIn = preferences.getInt("autoLockIn", 3600);
                lastPauseTime = preferences.getInt("lastPauseTime", 0);
                useFingerprint = preferences.getBoolean("useFingerprint", true);
                lastUpdateVersion = preferences.getInt("lastUpdateVersion", 511);
                lastContactsSyncTime = preferences.getInt("lastContactsSyncTime", ((int) (System.currentTimeMillis() / 1000)) - 82800);
                migrateOffsetId = preferences.getInt("migrateOffsetId", 0);
                if (migrateOffsetId != -1) {
                    migrateOffsetDate = preferences.getInt("migrateOffsetDate", 0);
                    migrateOffsetUserId = preferences.getInt("migrateOffsetUserId", 0);
                    migrateOffsetChatId = preferences.getInt("migrateOffsetChatId", 0);
                    migrateOffsetChannelId = preferences.getInt("migrateOffsetChannelId", 0);
                    migrateOffsetAccess = preferences.getLong("migrateOffsetAccess", 0);
                }
                String user = preferences.getString("user", null);
                if (user != null) {
                    byte[] userBytes = Base64.decode(user, 0);
                    if (userBytes != null) {
                        data = new SerializedData(userBytes);
                        currentUser = User.TLdeserialize(data, data.readInt32(false), false);
                        data.cleanup();
                    }
                }
                String passcodeSaltString = preferences.getString("passcodeSalt", "");
                if (passcodeSaltString.length() > 0) {
                    passcodeSalt = Base64.decode(passcodeSaltString, 0);
                } else {
                    passcodeSalt = new byte[0];
                }
            }
        }
    }

    public static boolean checkPasscode(String passcode) {
        boolean result = false;
        byte[] passcodeBytes;
        byte[] bytes;
        if (passcodeSalt.length == 0) {
            result = Utilities.MD5(passcode).equals(passcodeHash);
            if (result) {
                try {
                    passcodeSalt = new byte[16];
                    Utilities.random.nextBytes(passcodeSalt);
                    passcodeBytes = passcode.getBytes(HttpURLConnectionBuilder.DEFAULT_CHARSET);
                    bytes = new byte[(passcodeBytes.length + 32)];
                    System.arraycopy(passcodeSalt, 0, bytes, 0, 16);
                    System.arraycopy(passcodeBytes, 0, bytes, 16, passcodeBytes.length);
                    System.arraycopy(passcodeSalt, 0, bytes, passcodeBytes.length + 16, 16);
                    passcodeHash = Utilities.bytesToHex(Utilities.computeSHA256(bytes, 0, bytes.length));
                    saveConfig(false);
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
        } else {
            try {
                passcodeBytes = passcode.getBytes(HttpURLConnectionBuilder.DEFAULT_CHARSET);
                bytes = new byte[(passcodeBytes.length + 32)];
                System.arraycopy(passcodeSalt, 0, bytes, 0, 16);
                System.arraycopy(passcodeBytes, 0, bytes, 16, passcodeBytes.length);
                System.arraycopy(passcodeSalt, 0, bytes, passcodeBytes.length + 16, 16);
                result = passcodeHash.equals(Utilities.bytesToHex(Utilities.computeSHA256(bytes, 0, bytes.length)));
            } catch (Throwable e2) {
                FileLog.m611e("tmessages", e2);
            }
        }
        return result;
    }

    public static void clearConfig() {
        currentUser = null;
        registeredForPush = false;
        contactsHash = "";
        importHash = "";
        lastSendMessageId = -210000;
        contactsVersion = 1;
        lastBroadcastId = -1;
        saveIncomingPhotos = false;
        blockedUsersLoaded = false;
        migrateOffsetId = -1;
        migrateOffsetDate = -1;
        migrateOffsetUserId = -1;
        migrateOffsetChatId = -1;
        migrateOffsetChannelId = -1;
        migrateOffsetAccess = -1;
        appLocked = false;
        passcodeType = 0;
        passcodeHash = "";
        passcodeSalt = new byte[0];
        autoLockIn = 3600;
        lastPauseTime = 0;
        useFingerprint = true;
        isWaitingForPasscodeEnter = false;
        lastUpdateVersion = BuildVars.BUILD_VERSION;
        lastContactsSyncTime = ((int) (System.currentTimeMillis() / 1000)) - 82800;
        saveConfig(true);
    }
}
