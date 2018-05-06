package org.telegram.messenger;

import android.text.TextUtils;
import android.util.SparseArray;
import android.util.SparseIntArray;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;
import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.SQLite.SQLiteCursor;
import org.telegram.SQLite.SQLiteDatabase;
import org.telegram.SQLite.SQLitePreparedStatement;
import org.telegram.messenger.ContactsController.Contact;
import org.telegram.messenger.MediaController.SearchImage;
import org.telegram.messenger.query.BotQuery;
import org.telegram.messenger.query.SharedMediaQuery;
import org.telegram.tgnet.AbstractSerializedData;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.NativeByteBuffer;
import org.telegram.tgnet.TLClassStore;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.ChannelParticipant;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.ChatFull;
import org.telegram.tgnet.TLRPC.ChatParticipant;
import org.telegram.tgnet.TLRPC.ChatParticipants;
import org.telegram.tgnet.TLRPC.Dialog;
import org.telegram.tgnet.TLRPC.EncryptedChat;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.tgnet.TLRPC.Photo;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.TL_channelFull;
import org.telegram.tgnet.TLRPC.TL_chatFull;
import org.telegram.tgnet.TLRPC.TL_chatInviteEmpty;
import org.telegram.tgnet.TLRPC.TL_chatParticipant;
import org.telegram.tgnet.TLRPC.TL_chatParticipantAdmin;
import org.telegram.tgnet.TLRPC.TL_contact;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionScreenshotMessages;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionSetMessageTTL;
import org.telegram.tgnet.TLRPC.TL_dialog;
import org.telegram.tgnet.TLRPC.TL_dialogChannel;
import org.telegram.tgnet.TLRPC.TL_messageEncryptedAction;
import org.telegram.tgnet.TLRPC.TL_messageGroup;
import org.telegram.tgnet.TLRPC.TL_messageMediaAudio;
import org.telegram.tgnet.TLRPC.TL_messageMediaDocument;
import org.telegram.tgnet.TLRPC.TL_messageMediaPhoto;
import org.telegram.tgnet.TLRPC.TL_messageMediaUnsupported;
import org.telegram.tgnet.TLRPC.TL_messageMediaUnsupported_old;
import org.telegram.tgnet.TLRPC.TL_messageMediaVideo;
import org.telegram.tgnet.TLRPC.TL_messageMediaWebPage;
import org.telegram.tgnet.TLRPC.TL_message_secret;
import org.telegram.tgnet.TLRPC.TL_messages_dialogs;
import org.telegram.tgnet.TLRPC.TL_peerChannel;
import org.telegram.tgnet.TLRPC.TL_peerNotifySettings;
import org.telegram.tgnet.TLRPC.TL_peerNotifySettingsEmpty;
import org.telegram.tgnet.TLRPC.TL_peerUser;
import org.telegram.tgnet.TLRPC.TL_photoEmpty;
import org.telegram.tgnet.TLRPC.TL_updates_channelDifferenceTooLong;
import org.telegram.tgnet.TLRPC.TL_userStatusLastMonth;
import org.telegram.tgnet.TLRPC.TL_userStatusLastWeek;
import org.telegram.tgnet.TLRPC.TL_userStatusRecently;
import org.telegram.tgnet.TLRPC.TL_webPagePending;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.WallPaper;
import org.telegram.tgnet.TLRPC.WebPage;
import org.telegram.tgnet.TLRPC.messages_Dialogs;
import org.telegram.tgnet.TLRPC.messages_Messages;
import org.telegram.tgnet.TLRPC.photos_Photos;

public class MessagesStorage {
    private static volatile MessagesStorage Instance = null;
    public static int lastDateValue = 0;
    public static int lastPtsValue = 0;
    public static int lastQtsValue = 0;
    public static int lastSecretVersion = 0;
    public static int lastSeqValue = 0;
    public static int secretG = 0;
    public static byte[] secretPBytes = null;
    private File cacheFile;
    private SQLiteDatabase database;
    private int lastSavedDate = 0;
    private int lastSavedPts = 0;
    private int lastSavedQts = 0;
    private int lastSavedSeq = 0;
    private DispatchQueue storageQueue = new DispatchQueue("storageQueue");

    class C05257 implements Runnable {
        C05257() {
        }

        public void run() {
            try {
                long did;
                final HashMap<Long, Integer> pushDialogs = new HashMap();
                SQLiteCursor cursor = MessagesStorage.this.database.queryFinalized("SELECT d.did, d.unread_count, s.flags FROM dialogs as d LEFT JOIN dialog_settings as s ON d.did = s.did WHERE d.unread_count != 0", new Object[0]);
                StringBuilder ids = new StringBuilder();
                while (cursor.next()) {
                    if (cursor.isNull(2) || cursor.intValue(2) != 1) {
                        did = cursor.longValue(0);
                        pushDialogs.put(Long.valueOf(did), Integer.valueOf(cursor.intValue(1)));
                        if (ids.length() != 0) {
                            ids.append(",");
                        }
                        ids.append(did);
                    }
                }
                cursor.dispose();
                final ArrayList<Message> messages = new ArrayList();
                final ArrayList<User> users = new ArrayList();
                final ArrayList<Chat> chats = new ArrayList();
                final ArrayList<EncryptedChat> encryptedChats = new ArrayList();
                if (ids.length() > 0) {
                    ArrayList<Integer> usersToLoad = new ArrayList();
                    ArrayList<Integer> chatsToLoad = new ArrayList();
                    ArrayList<Integer> encryptedChatIds = new ArrayList();
                    cursor = MessagesStorage.this.database.queryFinalized("SELECT read_state, data, send_state, mid, date, uid FROM messages WHERE uid IN (" + ids.toString() + ") AND out = 0 AND read_state IN(0,2) ORDER BY date DESC LIMIT 50", new Object[0]);
                    while (cursor.next()) {
                        AbstractSerializedData nativeByteBuffer = new NativeByteBuffer(cursor.byteArrayLength(1));
                        if (!(nativeByteBuffer == null || cursor.byteBufferValue(1, (NativeByteBuffer) nativeByteBuffer) == 0)) {
                            Message message = Message.TLdeserialize(nativeByteBuffer, nativeByteBuffer.readInt32(false), false);
                            MessageObject.setUnreadFlags(message, cursor.intValue(0));
                            message.id = cursor.intValue(3);
                            message.date = cursor.intValue(4);
                            message.dialog_id = cursor.longValue(5);
                            messages.add(message);
                            int lower_id = (int) message.dialog_id;
                            int high_id = (int) (message.dialog_id >> 32);
                            if (lower_id == 0) {
                                if (!encryptedChatIds.contains(Integer.valueOf(high_id))) {
                                    encryptedChatIds.add(Integer.valueOf(high_id));
                                }
                            } else if (lower_id >= 0) {
                                if (!usersToLoad.contains(Integer.valueOf(lower_id))) {
                                    usersToLoad.add(Integer.valueOf(lower_id));
                                }
                            } else if (!chatsToLoad.contains(Integer.valueOf(-lower_id))) {
                                chatsToLoad.add(Integer.valueOf(-lower_id));
                            }
                            MessagesStorage.addUsersAndChatsFromMessage(message, usersToLoad, chatsToLoad);
                            message.send_state = cursor.intValue(2);
                            if (!(message.to_id.channel_id != 0 || MessageObject.isUnread(message) || lower_id == 0) || message.id > 0) {
                                message.send_state = 0;
                            }
                            if (lower_id == 0 && !cursor.isNull(5)) {
                                message.random_id = cursor.longValue(5);
                            }
                        }
                        nativeByteBuffer.reuse();
                    }
                    cursor.dispose();
                    if (!encryptedChatIds.isEmpty()) {
                        MessagesStorage.this.getEncryptedChatsInternal(TextUtils.join(",", encryptedChatIds), encryptedChats, usersToLoad);
                    }
                    if (!usersToLoad.isEmpty()) {
                        MessagesStorage.this.getUsersInternal(TextUtils.join(",", usersToLoad), users);
                    }
                    if (!chatsToLoad.isEmpty()) {
                        MessagesStorage.this.getChatsInternal(TextUtils.join(",", chatsToLoad), chats);
                        int a = 0;
                        while (a < chats.size()) {
                            Chat chat = (Chat) chats.get(a);
                            if (chat != null && (chat.left || chat.migrated_to != null)) {
                                MessagesStorage.this.database.executeFast("UPDATE dialogs SET unread_count = 0, unread_count_i = 0 WHERE did = " + ((long) (-chat.id))).stepThis().dispose();
                                MessagesStorage.this.database.executeFast(String.format(Locale.US, "UPDATE messages SET read_state = 3 WHERE uid = %d AND mid > 0 AND read_state IN(0,2) AND out = 0", new Object[]{Long.valueOf(did)})).stepThis().dispose();
                                chats.remove(a);
                                a--;
                                pushDialogs.remove(Long.valueOf((long) (-chat.id)));
                                int b = 0;
                                while (b < messages.size()) {
                                    if (((Message) messages.get(b)).dialog_id == ((long) (-chat.id))) {
                                        messages.remove(b);
                                        b--;
                                    }
                                    b++;
                                }
                            }
                            a++;
                        }
                    }
                }
                Collections.reverse(messages);
                AndroidUtilities.runOnUIThread(new Runnable() {
                    public void run() {
                        NotificationsController.getInstance().processLoadedUnreadMessages(pushDialogs, messages, users, chats, encryptedChats);
                    }
                });
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
        }
    }

    private class Hole {
        public int end;
        public int start;
        public int type;

        public Hole(int s, int e) {
            this.start = s;
            this.end = e;
        }

        public Hole(int t, int s, int e) {
            this.type = t;
            this.start = s;
            this.end = e;
        }
    }

    public static MessagesStorage getInstance() {
        MessagesStorage localInstance = Instance;
        if (localInstance == null) {
            synchronized (MessagesStorage.class) {
                try {
                    localInstance = Instance;
                    if (localInstance == null) {
                        MessagesStorage localInstance2 = new MessagesStorage();
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

    public MessagesStorage() {
        this.storageQueue.setPriority(10);
        openDatabase();
    }

    public SQLiteDatabase getDatabase() {
        return this.database;
    }

    public DispatchQueue getStorageQueue() {
        return this.storageQueue;
    }

    public void openDatabase() {
        this.cacheFile = new File(ApplicationLoader.getFilesDirFixed(), "cache4.db");
        boolean createTable = false;
        if (!this.cacheFile.exists()) {
            createTable = true;
        }
        try {
            this.database = new SQLiteDatabase(this.cacheFile.getPath());
            this.database.executeFast("PRAGMA secure_delete = ON").stepThis().dispose();
            this.database.executeFast("PRAGMA temp_store = 1").stepThis().dispose();
            if (createTable) {
                this.database.executeFast("CREATE TABLE channel_group(uid INTEGER, start INTEGER, end INTEGER, count INTEGER, PRIMARY KEY(uid, start));").stepThis().dispose();
                this.database.executeFast("CREATE TABLE messages_holes(uid INTEGER, start INTEGER, end INTEGER, PRIMARY KEY(uid, start));").stepThis().dispose();
                this.database.executeFast("CREATE INDEX IF NOT EXISTS uid_end_messages_holes ON messages_holes(uid, end);").stepThis().dispose();
                this.database.executeFast("CREATE TABLE messages_imp_holes(uid INTEGER, start INTEGER, end INTEGER, PRIMARY KEY(uid, start));").stepThis().dispose();
                this.database.executeFast("CREATE INDEX IF NOT EXISTS uid_end_messages_imp_holes ON messages_imp_holes(uid, end);").stepThis().dispose();
                this.database.executeFast("CREATE TABLE media_holes_v2(uid INTEGER, type INTEGER, start INTEGER, end INTEGER, PRIMARY KEY(uid, type, start));").stepThis().dispose();
                this.database.executeFast("CREATE INDEX IF NOT EXISTS uid_end_media_holes_v2 ON media_holes_v2(uid, type, end);").stepThis().dispose();
                this.database.executeFast("CREATE TABLE messages(mid INTEGER PRIMARY KEY, uid INTEGER, read_state INTEGER, send_state INTEGER, date INTEGER, data BLOB, out INTEGER, ttl INTEGER, media INTEGER, replydata BLOB, imp INTEGER)").stepThis().dispose();
                this.database.executeFast("CREATE INDEX IF NOT EXISTS uid_mid_idx_messages ON messages(uid, mid);").stepThis().dispose();
                this.database.executeFast("CREATE INDEX IF NOT EXISTS uid_date_mid_idx_messages ON messages(uid, date, mid);").stepThis().dispose();
                this.database.executeFast("CREATE INDEX IF NOT EXISTS uid_mid_idx_imp_messages ON messages(uid, mid, imp) WHERE imp = 1;").stepThis().dispose();
                this.database.executeFast("CREATE INDEX IF NOT EXISTS uid_date_mid_imp_idx_messages ON messages(uid, date, mid, imp) WHERE imp = 1;").stepThis().dispose();
                this.database.executeFast("CREATE INDEX IF NOT EXISTS mid_out_idx_messages ON messages(mid, out);").stepThis().dispose();
                this.database.executeFast("CREATE INDEX IF NOT EXISTS task_idx_messages ON messages(uid, out, read_state, ttl, date, send_state);").stepThis().dispose();
                this.database.executeFast("CREATE INDEX IF NOT EXISTS send_state_idx_messages ON messages(mid, send_state, date) WHERE mid < 0 AND send_state = 1;").stepThis().dispose();
                this.database.executeFast("CREATE TABLE download_queue(uid INTEGER, type INTEGER, date INTEGER, data BLOB, PRIMARY KEY (uid, type));").stepThis().dispose();
                this.database.executeFast("CREATE INDEX IF NOT EXISTS type_date_idx_download_queue ON download_queue(type, date);").stepThis().dispose();
                this.database.executeFast("CREATE TABLE user_phones_v6(uid INTEGER, phone TEXT, sphone TEXT, deleted INTEGER, PRIMARY KEY (uid, phone))").stepThis().dispose();
                this.database.executeFast("CREATE INDEX IF NOT EXISTS sphone_deleted_idx_user_phones ON user_phones_v6(sphone, deleted);").stepThis().dispose();
                this.database.executeFast("CREATE TABLE dialogs(did INTEGER PRIMARY KEY, date INTEGER, unread_count INTEGER, last_mid INTEGER, inbox_max INTEGER, outbox_max INTEGER, last_mid_i INTEGER, unread_count_i INTEGER, pts INTEGER, date_i INTEGER)").stepThis().dispose();
                this.database.executeFast("CREATE INDEX IF NOT EXISTS date_idx_dialogs ON dialogs(date);").stepThis().dispose();
                this.database.executeFast("CREATE INDEX IF NOT EXISTS last_mid_idx_dialogs ON dialogs(last_mid);").stepThis().dispose();
                this.database.executeFast("CREATE INDEX IF NOT EXISTS unread_count_idx_dialogs ON dialogs(unread_count);").stepThis().dispose();
                this.database.executeFast("CREATE INDEX IF NOT EXISTS last_mid_i_idx_dialogs ON dialogs(last_mid_i);").stepThis().dispose();
                this.database.executeFast("CREATE INDEX IF NOT EXISTS unread_count_i_idx_dialogs ON dialogs(unread_count_i);").stepThis().dispose();
                this.database.executeFast("CREATE TABLE randoms(random_id INTEGER, mid INTEGER, PRIMARY KEY (random_id, mid))").stepThis().dispose();
                this.database.executeFast("CREATE INDEX IF NOT EXISTS mid_idx_randoms ON randoms(mid);").stepThis().dispose();
                this.database.executeFast("CREATE TABLE enc_tasks_v2(mid INTEGER PRIMARY KEY, date INTEGER)").stepThis().dispose();
                this.database.executeFast("CREATE INDEX IF NOT EXISTS date_idx_enc_tasks_v2 ON enc_tasks_v2(date);").stepThis().dispose();
                this.database.executeFast("CREATE TABLE messages_seq(mid INTEGER PRIMARY KEY, seq_in INTEGER, seq_out INTEGER);").stepThis().dispose();
                this.database.executeFast("CREATE INDEX IF NOT EXISTS seq_idx_messages_seq ON messages_seq(seq_in, seq_out);").stepThis().dispose();
                this.database.executeFast("CREATE TABLE params(id INTEGER PRIMARY KEY, seq INTEGER, pts INTEGER, date INTEGER, qts INTEGER, lsv INTEGER, sg INTEGER, pbytes BLOB)").stepThis().dispose();
                this.database.executeFast("INSERT INTO params VALUES(1, 0, 0, 0, 0, 0, 0, NULL)").stepThis().dispose();
                this.database.executeFast("CREATE TABLE media_v2(mid INTEGER PRIMARY KEY, uid INTEGER, date INTEGER, type INTEGER, data BLOB)").stepThis().dispose();
                this.database.executeFast("CREATE INDEX IF NOT EXISTS uid_mid_type_date_idx_media ON media_v2(uid, mid, type, date);").stepThis().dispose();
                this.database.executeFast("CREATE TABLE bot_keyboard(uid INTEGER PRIMARY KEY, mid INTEGER, info BLOB)").stepThis().dispose();
                this.database.executeFast("CREATE INDEX IF NOT EXISTS bot_keyboard_idx_mid ON bot_keyboard(mid);").stepThis().dispose();
                this.database.executeFast("CREATE TABLE users(uid INTEGER PRIMARY KEY, name TEXT, status INTEGER, data BLOB)").stepThis().dispose();
                this.database.executeFast("CREATE TABLE chats(uid INTEGER PRIMARY KEY, name TEXT, data BLOB)").stepThis().dispose();
                this.database.executeFast("CREATE TABLE enc_chats(uid INTEGER PRIMARY KEY, user INTEGER, name TEXT, data BLOB, g BLOB, authkey BLOB, ttl INTEGER, layer INTEGER, seq_in INTEGER, seq_out INTEGER, use_count INTEGER, exchange_id INTEGER, key_date INTEGER, fprint INTEGER, fauthkey BLOB, khash BLOB)").stepThis().dispose();
                this.database.executeFast("CREATE TABLE chat_settings_v2(uid INTEGER PRIMARY KEY, info BLOB)").stepThis().dispose();
                this.database.executeFast("CREATE TABLE channel_users_v2(did INTEGER, uid INTEGER, date INTEGER, data BLOB, PRIMARY KEY(did, uid))").stepThis().dispose();
                this.database.executeFast("CREATE TABLE contacts(uid INTEGER PRIMARY KEY, mutual INTEGER)").stepThis().dispose();
                this.database.executeFast("CREATE TABLE pending_read(uid INTEGER PRIMARY KEY, max_id INTEGER)").stepThis().dispose();
                this.database.executeFast("CREATE TABLE wallpapers(uid INTEGER PRIMARY KEY, data BLOB)").stepThis().dispose();
                this.database.executeFast("CREATE TABLE user_photos(uid INTEGER, id INTEGER, data BLOB, PRIMARY KEY (uid, id))").stepThis().dispose();
                this.database.executeFast("CREATE TABLE blocked_users(uid INTEGER PRIMARY KEY)").stepThis().dispose();
                this.database.executeFast("CREATE TABLE dialog_settings(did INTEGER PRIMARY KEY, flags INTEGER);").stepThis().dispose();
                this.database.executeFast("CREATE TABLE web_recent_v3(id TEXT, type INTEGER, image_url TEXT, thumb_url TEXT, local_url TEXT, width INTEGER, height INTEGER, size INTEGER, date INTEGER, PRIMARY KEY (id, type));").stepThis().dispose();
                this.database.executeFast("CREATE TABLE stickers_v2(id INTEGER PRIMARY KEY, data BLOB, date INTEGER, hash TEXT);").stepThis().dispose();
                this.database.executeFast("CREATE TABLE hashtag_recent_v2(id TEXT PRIMARY KEY, date INTEGER);").stepThis().dispose();
                this.database.executeFast("CREATE TABLE webpage_pending(id INTEGER, mid INTEGER, PRIMARY KEY (id, mid));").stepThis().dispose();
                this.database.executeFast("CREATE TABLE user_contacts_v6(uid INTEGER PRIMARY KEY, fname TEXT, sname TEXT)").stepThis().dispose();
                this.database.executeFast("CREATE TABLE sent_files_v2(uid TEXT, type INTEGER, data BLOB, PRIMARY KEY (uid, type))").stepThis().dispose();
                this.database.executeFast("CREATE TABLE search_recent(did INTEGER PRIMARY KEY, date INTEGER);").stepThis().dispose();
                this.database.executeFast("CREATE TABLE media_counts_v2(uid INTEGER, type INTEGER, count INTEGER, PRIMARY KEY(uid, type))").stepThis().dispose();
                this.database.executeFast("CREATE TABLE keyvalue(id TEXT PRIMARY KEY, value TEXT)").stepThis().dispose();
                this.database.executeFast("CREATE TABLE bot_info(uid INTEGER PRIMARY KEY, info BLOB)").stepThis().dispose();
                this.database.executeFast("PRAGMA user_version = 27").stepThis().dispose();
            } else {
                try {
                    SQLiteCursor cursor = this.database.queryFinalized("SELECT seq, pts, date, qts, lsv, sg, pbytes FROM params WHERE id = 1", new Object[0]);
                    if (cursor.next()) {
                        lastSeqValue = cursor.intValue(0);
                        lastPtsValue = cursor.intValue(1);
                        lastDateValue = cursor.intValue(2);
                        lastQtsValue = cursor.intValue(3);
                        lastSecretVersion = cursor.intValue(4);
                        secretG = cursor.intValue(5);
                        if (cursor.isNull(6)) {
                            secretPBytes = null;
                        } else {
                            secretPBytes = cursor.byteArrayValue(6);
                            if (secretPBytes != null && secretPBytes.length == 1) {
                                secretPBytes = null;
                            }
                        }
                    }
                    cursor.dispose();
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                    try {
                        this.database.executeFast("CREATE TABLE IF NOT EXISTS params(id INTEGER PRIMARY KEY, seq INTEGER, pts INTEGER, date INTEGER, qts INTEGER, lsv INTEGER, sg INTEGER, pbytes BLOB)").stepThis().dispose();
                        this.database.executeFast("INSERT INTO params VALUES(1, 0, 0, 0, 0, 0, 0, NULL)").stepThis().dispose();
                    } catch (Throwable e2) {
                        FileLog.m611e("tmessages", e2);
                    }
                }
                int version = this.database.executeInt("PRAGMA user_version", new Object[0]).intValue();
                if (version < 27) {
                    updateDbToLastVersion(version);
                }
            }
        } catch (Throwable e3) {
            FileLog.m611e("tmessages", e3);
        }
        loadUnreadMessages();
    }

    public void updateDbToLastVersion(final int currentVersion) {
        this.storageQueue.postRunnable(new Runnable() {

            class C05061 implements Runnable {
                C05061() {
                }

                public void run() {
                    Iterator i$;
                    ArrayList<Integer> ids = new ArrayList();
                    for (Entry<String, ?> entry : ApplicationLoader.applicationContext.getSharedPreferences("Notifications", 0).getAll().entrySet()) {
                        String key = (String) entry.getKey();
                        if (key.startsWith("notify2_") && ((Integer) entry.getValue()).intValue() == 2) {
                            try {
                                ids.add(Integer.valueOf(Integer.parseInt(key.replace("notify2_", ""))));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    try {
                        MessagesStorage.this.database.beginTransaction();
                        SQLitePreparedStatement state = MessagesStorage.this.database.executeFast("REPLACE INTO dialog_settings VALUES(?, ?)");
                        i$ = ids.iterator();
                        while (i$.hasNext()) {
                            Integer id = (Integer) i$.next();
                            state.requery();
                            state.bindLong(1, (long) id.intValue());
                            state.bindInteger(2, 1);
                            state.step();
                        }
                        state.dispose();
                        MessagesStorage.this.database.commitTransaction();
                    } catch (Throwable e2) {
                        FileLog.m611e("tmessages", e2);
                    }
                }
            }

            public void run() {
                try {
                    SQLiteCursor cursor;
                    SQLitePreparedStatement state;
                    NativeByteBuffer data;
                    int version = currentVersion;
                    if (version < 4) {
                        MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS user_photos(uid INTEGER, id INTEGER, data BLOB, PRIMARY KEY (uid, id))").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("DROP INDEX IF EXISTS read_state_out_idx_messages;").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("DROP INDEX IF EXISTS ttl_idx_messages;").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("DROP INDEX IF EXISTS date_idx_messages;").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS mid_out_idx_messages ON messages(mid, out);").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS task_idx_messages ON messages(uid, out, read_state, ttl, date, send_state);").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS uid_date_mid_idx_messages ON messages(uid, date, mid);").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS user_contacts_v6(uid INTEGER PRIMARY KEY, fname TEXT, sname TEXT)").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS user_phones_v6(uid INTEGER, phone TEXT, sphone TEXT, deleted INTEGER, PRIMARY KEY (uid, phone))").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS sphone_deleted_idx_user_phones ON user_phones_v6(sphone, deleted);").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS mid_idx_randoms ON randoms(mid);").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS sent_files_v2(uid TEXT, type INTEGER, data BLOB, PRIMARY KEY (uid, type))").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS blocked_users(uid INTEGER PRIMARY KEY)").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS download_queue(uid INTEGER, type INTEGER, date INTEGER, data BLOB, PRIMARY KEY (uid, type));").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS type_date_idx_download_queue ON download_queue(type, date);").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS dialog_settings(did INTEGER PRIMARY KEY, flags INTEGER);").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS send_state_idx_messages ON messages(mid, send_state, date) WHERE mid < 0 AND send_state = 1;").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS unread_count_idx_dialogs ON dialogs(unread_count);").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("UPDATE messages SET send_state = 2 WHERE mid < 0 AND send_state = 1").stepThis().dispose();
                        MessagesStorage.this.storageQueue.postRunnable(new C05061());
                        MessagesStorage.this.database.executeFast("PRAGMA user_version = 4").stepThis().dispose();
                        version = 4;
                    }
                    if (version == 4) {
                        MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS enc_tasks_v2(mid INTEGER PRIMARY KEY, date INTEGER)").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS date_idx_enc_tasks_v2 ON enc_tasks_v2(date);").stepThis().dispose();
                        MessagesStorage.this.database.beginTransaction();
                        cursor = MessagesStorage.this.database.queryFinalized("SELECT date, data FROM enc_tasks WHERE 1", new Object[0]);
                        state = MessagesStorage.this.database.executeFast("REPLACE INTO enc_tasks_v2 VALUES(?, ?)");
                        if (cursor.next()) {
                            int date = cursor.intValue(0);
                            data = new NativeByteBuffer(cursor.byteArrayLength(1));
                            int length = cursor.byteBufferValue(1, data);
                            if (length != 0) {
                                for (int a = 0; a < length / 4; a++) {
                                    state.requery();
                                    state.bindInteger(1, data.readInt32(false));
                                    state.bindInteger(2, date);
                                    state.step();
                                }
                            }
                            data.reuse();
                        }
                        state.dispose();
                        cursor.dispose();
                        MessagesStorage.this.database.commitTransaction();
                        MessagesStorage.this.database.executeFast("DROP INDEX IF EXISTS date_idx_enc_tasks;").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("DROP TABLE IF EXISTS enc_tasks;").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("ALTER TABLE messages ADD COLUMN media INTEGER default 0").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("PRAGMA user_version = 6").stepThis().dispose();
                        version = 6;
                    }
                    if (version == 6) {
                        MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS messages_seq(mid INTEGER PRIMARY KEY, seq_in INTEGER, seq_out INTEGER);").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS seq_idx_messages_seq ON messages_seq(seq_in, seq_out);").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("ALTER TABLE enc_chats ADD COLUMN layer INTEGER default 0").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("ALTER TABLE enc_chats ADD COLUMN seq_in INTEGER default 0").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("ALTER TABLE enc_chats ADD COLUMN seq_out INTEGER default 0").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("PRAGMA user_version = 7").stepThis().dispose();
                        version = 7;
                    }
                    if (version == 7 || version == 8 || version == 9) {
                        MessagesStorage.this.database.executeFast("ALTER TABLE enc_chats ADD COLUMN use_count INTEGER default 0").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("ALTER TABLE enc_chats ADD COLUMN exchange_id INTEGER default 0").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("ALTER TABLE enc_chats ADD COLUMN key_date INTEGER default 0").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("ALTER TABLE enc_chats ADD COLUMN fprint INTEGER default 0").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("ALTER TABLE enc_chats ADD COLUMN fauthkey BLOB default NULL").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("ALTER TABLE enc_chats ADD COLUMN khash BLOB default NULL").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("PRAGMA user_version = 10").stepThis().dispose();
                        version = 10;
                    }
                    if (version == 10) {
                        MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS web_recent_v3(id TEXT, type INTEGER, image_url TEXT, thumb_url TEXT, local_url TEXT, width INTEGER, height INTEGER, size INTEGER, date INTEGER, PRIMARY KEY (id, type));").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("PRAGMA user_version = 11").stepThis().dispose();
                        version = 11;
                    }
                    if (version == 11) {
                        version = 12;
                    }
                    if (version == 12) {
                        MessagesStorage.this.database.executeFast("DROP INDEX IF EXISTS uid_mid_idx_media;").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("DROP INDEX IF EXISTS mid_idx_media;").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("DROP INDEX IF EXISTS uid_date_mid_idx_media;").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("DROP TABLE IF EXISTS media;").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("DROP TABLE IF EXISTS media_counts;").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS media_v2(mid INTEGER PRIMARY KEY, uid INTEGER, date INTEGER, type INTEGER, data BLOB)").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS media_counts_v2(uid INTEGER, type INTEGER, count INTEGER, PRIMARY KEY(uid, type))").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS uid_mid_type_date_idx_media ON media_v2(uid, mid, type, date);").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS keyvalue(id TEXT PRIMARY KEY, value TEXT)").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("PRAGMA user_version = 13").stepThis().dispose();
                        version = 13;
                    }
                    if (version == 13) {
                        MessagesStorage.this.database.executeFast("ALTER TABLE messages ADD COLUMN replydata BLOB default NULL").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("PRAGMA user_version = 14").stepThis().dispose();
                        version = 14;
                    }
                    if (version == 14) {
                        MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS hashtag_recent_v2(id TEXT PRIMARY KEY, date INTEGER);").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("PRAGMA user_version = 15").stepThis().dispose();
                        version = 15;
                    }
                    if (version == 15) {
                        MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS webpage_pending(id INTEGER, mid INTEGER, PRIMARY KEY (id, mid));").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("PRAGMA user_version = 16").stepThis().dispose();
                        version = 16;
                    }
                    if (version == 16) {
                        MessagesStorage.this.database.executeFast("ALTER TABLE dialogs ADD COLUMN inbox_max INTEGER default 0").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("ALTER TABLE dialogs ADD COLUMN outbox_max INTEGER default 0").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("PRAGMA user_version = 17").stepThis().dispose();
                        version = 17;
                    }
                    if (version == 17) {
                        MessagesStorage.this.database.executeFast("CREATE TABLE bot_info(uid INTEGER PRIMARY KEY, info BLOB)").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("PRAGMA user_version = 18").stepThis().dispose();
                        version = 18;
                    }
                    if (version == 18) {
                        MessagesStorage.this.database.executeFast("DROP TABLE IF EXISTS stickers;").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS stickers_v2(id INTEGER PRIMARY KEY, data BLOB, date INTEGER, hash TEXT);").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("PRAGMA user_version = 19").stepThis().dispose();
                        version = 19;
                    }
                    if (version == 19) {
                        MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS bot_keyboard(uid INTEGER PRIMARY KEY, mid INTEGER, info BLOB)").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS bot_keyboard_idx_mid ON bot_keyboard(mid);").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("PRAGMA user_version = 20").stepThis().dispose();
                        version = 20;
                    }
                    if (version == 20) {
                        MessagesStorage.this.database.executeFast("CREATE TABLE search_recent(did INTEGER PRIMARY KEY, date INTEGER);").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("PRAGMA user_version = 21").stepThis().dispose();
                        version = 21;
                    }
                    if (version == 21) {
                        MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS chat_settings_v2(uid INTEGER PRIMARY KEY, info BLOB)").stepThis().dispose();
                        cursor = MessagesStorage.this.database.queryFinalized("SELECT uid, participants FROM chat_settings WHERE uid < 0", new Object[0]);
                        state = MessagesStorage.this.database.executeFast("REPLACE INTO chat_settings_v2 VALUES(?, ?)");
                        while (cursor.next()) {
                            int chat_id = cursor.intValue(0);
                            data = new NativeByteBuffer(cursor.byteArrayLength(1));
                            if (!(data == null || cursor.byteBufferValue(1, data) == 0)) {
                                ChatParticipants participants = ChatParticipants.TLdeserialize(data, data.readInt32(false), false);
                                if (participants != null) {
                                    TL_chatFull chatFull = new TL_chatFull();
                                    chatFull.id = chat_id;
                                    chatFull.chat_photo = new TL_photoEmpty();
                                    chatFull.notify_settings = new TL_peerNotifySettingsEmpty();
                                    chatFull.exported_invite = new TL_chatInviteEmpty();
                                    chatFull.participants = participants;
                                    NativeByteBuffer data2 = new NativeByteBuffer(chatFull.getObjectSize());
                                    chatFull.serializeToStream(data2);
                                    state.requery();
                                    state.bindInteger(1, chat_id);
                                    state.bindByteBuffer(2, data2);
                                    state.step();
                                    data2.reuse();
                                }
                            }
                            data.reuse();
                        }
                        state.dispose();
                        cursor.dispose();
                        MessagesStorage.this.database.executeFast("DROP TABLE IF EXISTS chat_settings;").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("ALTER TABLE dialogs ADD COLUMN last_mid_i INTEGER default 0").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("ALTER TABLE dialogs ADD COLUMN unread_count_i INTEGER default 0").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("ALTER TABLE dialogs ADD COLUMN pts INTEGER default 0").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("ALTER TABLE dialogs ADD COLUMN date_i INTEGER default 0").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS last_mid_i_idx_dialogs ON dialogs(last_mid_i);").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS unread_count_i_idx_dialogs ON dialogs(unread_count_i);").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("ALTER TABLE messages ADD COLUMN imp INTEGER default 0").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS uid_mid_idx_imp_messages ON messages(uid, mid, imp) WHERE imp = 1;").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS uid_date_mid_imp_idx_messages ON messages(uid, date, mid, imp) WHERE imp = 1;").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS channel_group(uid INTEGER, start INTEGER, end INTEGER, count INTEGER, PRIMARY KEY(uid, start));").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS messages_holes(uid INTEGER, start INTEGER, end INTEGER, PRIMARY KEY(uid, start));").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS uid_end_messages_holes ON messages_holes(uid, end);").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS messages_imp_holes(uid INTEGER, start INTEGER, end INTEGER, PRIMARY KEY(uid, start));").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS uid_end_messages_imp_holes ON messages_imp_holes(uid, end);").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("PRAGMA user_version = 22").stepThis().dispose();
                        version = 22;
                    }
                    if (version == 22) {
                        MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS media_holes_v2(uid INTEGER, type INTEGER, start INTEGER, end INTEGER, PRIMARY KEY(uid, type, start));").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("CREATE INDEX IF NOT EXISTS uid_end_media_holes_v2 ON media_holes_v2(uid, type, end);").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("PRAGMA user_version = 23").stepThis().dispose();
                        version = 23;
                    }
                    if (version == 23) {
                        MessagesStorage.this.database.executeFast("DELETE FROM sent_files_v2 WHERE 1").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("PRAGMA user_version = 24").stepThis().dispose();
                        version = 24;
                    }
                    if (version == 24) {
                        MessagesStorage.this.database.executeFast("DELETE FROM media_holes_v2 WHERE uid != 0 AND type >= 0 AND start IN (0, 1)").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("PRAGMA user_version = 25").stepThis().dispose();
                        version = 25;
                    }
                    if (version == 25 || version == 26) {
                        MessagesStorage.this.database.executeFast("CREATE TABLE IF NOT EXISTS channel_users_v2(did INTEGER, uid INTEGER, date INTEGER, data BLOB, PRIMARY KEY(did, uid))").stepThis().dispose();
                        MessagesStorage.this.database.executeFast("PRAGMA user_version = 27").stepThis().dispose();
                    }
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
        });
    }

    public void cleanUp(final boolean isLogin) {
        this.storageQueue.cleanupQueue();
        this.storageQueue.postRunnable(new Runnable() {

            class C05111 implements Runnable {
                C05111() {
                }

                public void run() {
                    MessagesController.getInstance().getDifference();
                }
            }

            public void run() {
                MessagesStorage.lastDateValue = 0;
                MessagesStorage.lastSeqValue = 0;
                MessagesStorage.lastPtsValue = 0;
                MessagesStorage.lastQtsValue = 0;
                MessagesStorage.lastSecretVersion = 0;
                MessagesStorage.this.lastSavedSeq = 0;
                MessagesStorage.this.lastSavedPts = 0;
                MessagesStorage.this.lastSavedDate = 0;
                MessagesStorage.this.lastSavedQts = 0;
                MessagesStorage.secretPBytes = null;
                MessagesStorage.secretG = 0;
                if (MessagesStorage.this.database != null) {
                    MessagesStorage.this.database.close();
                    MessagesStorage.this.database = null;
                }
                if (MessagesStorage.this.cacheFile != null) {
                    MessagesStorage.this.cacheFile.delete();
                    MessagesStorage.this.cacheFile = null;
                }
                MessagesStorage.this.openDatabase();
                if (isLogin) {
                    Utilities.stageQueue.postRunnable(new C05111());
                }
            }
        });
    }

    public void saveSecretParams(final int lsv, final int sg, final byte[] pbytes) {
        this.storageQueue.postRunnable(new Runnable() {
            public void run() {
                int i = 1;
                try {
                    SQLitePreparedStatement state = MessagesStorage.this.database.executeFast("UPDATE params SET lsv = ?, sg = ?, pbytes = ? WHERE id = 1");
                    state.bindInteger(1, lsv);
                    state.bindInteger(2, sg);
                    if (pbytes != null) {
                        i = pbytes.length;
                    }
                    NativeByteBuffer data = new NativeByteBuffer(i);
                    if (pbytes != null) {
                        data.writeBytes(pbytes);
                    }
                    state.bindByteBuffer(3, data);
                    state.step();
                    state.dispose();
                    data.reuse();
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
        });
    }

    public void saveChannelPts(final int channelId, final int pts) {
        this.storageQueue.postRunnable(new Runnable() {
            public void run() {
                try {
                    SQLitePreparedStatement state = MessagesStorage.this.database.executeFast("UPDATE dialogs SET pts = ? WHERE did = ?");
                    state.bindInteger(1, pts);
                    state.bindInteger(2, -channelId);
                    state.step();
                    state.dispose();
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
        });
    }

    public void saveDiffParams(int seq, int pts, int date, int qts) {
        final int i = seq;
        final int i2 = pts;
        final int i3 = date;
        final int i4 = qts;
        this.storageQueue.postRunnable(new Runnable() {
            public void run() {
                try {
                    if (MessagesStorage.this.lastSavedSeq != i || MessagesStorage.this.lastSavedPts != i2 || MessagesStorage.this.lastSavedDate != i3 || MessagesStorage.lastQtsValue != i4) {
                        SQLitePreparedStatement state = MessagesStorage.this.database.executeFast("UPDATE params SET seq = ?, pts = ?, date = ?, qts = ? WHERE id = 1");
                        state.bindInteger(1, i);
                        state.bindInteger(2, i2);
                        state.bindInteger(3, i3);
                        state.bindInteger(4, i4);
                        state.step();
                        state.dispose();
                        MessagesStorage.this.lastSavedSeq = i;
                        MessagesStorage.this.lastSavedPts = i2;
                        MessagesStorage.this.lastSavedDate = i3;
                        MessagesStorage.this.lastSavedQts = i4;
                    }
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
        });
    }

    public void setDialogFlags(long did, long flags) {
        final long j = did;
        final long j2 = flags;
        this.storageQueue.postRunnable(new Runnable() {
            public void run() {
                try {
                    MessagesStorage.this.database.executeFast(String.format(Locale.US, "REPLACE INTO dialog_settings VALUES(%d, %d)", new Object[]{Long.valueOf(j), Long.valueOf(j2)})).stepThis().dispose();
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
        });
    }

    public void loadUnreadMessages() {
        this.storageQueue.postRunnable(new C05257());
    }

    public void putWallpapers(final ArrayList<WallPaper> wallPapers) {
        this.storageQueue.postRunnable(new Runnable() {
            public void run() {
                int num = 0;
                try {
                    MessagesStorage.this.database.executeFast("DELETE FROM wallpapers WHERE 1").stepThis().dispose();
                    MessagesStorage.this.database.beginTransaction();
                    SQLitePreparedStatement state = MessagesStorage.this.database.executeFast("REPLACE INTO wallpapers VALUES(?, ?)");
                    Iterator i$ = wallPapers.iterator();
                    while (i$.hasNext()) {
                        WallPaper wallPaper = (WallPaper) i$.next();
                        state.requery();
                        NativeByteBuffer data = new NativeByteBuffer(wallPaper.getObjectSize());
                        wallPaper.serializeToStream(data);
                        state.bindInteger(1, num);
                        state.bindByteBuffer(2, data);
                        state.step();
                        num++;
                        data.reuse();
                    }
                    state.dispose();
                    MessagesStorage.this.database.commitTransaction();
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
        });
    }

    public void loadWebRecent(final int type) {
        this.storageQueue.postRunnable(new Runnable() {

            class C05271 implements Comparator<SearchImage> {
                C05271() {
                }

                public int compare(SearchImage lhs, SearchImage rhs) {
                    if (lhs.date < rhs.date) {
                        return 1;
                    }
                    if (lhs.date > rhs.date) {
                        return -1;
                    }
                    return 0;
                }
            }

            public void run() {
                try {
                    SQLiteCursor cursor = MessagesStorage.this.database.queryFinalized("SELECT id, image_url, thumb_url, local_url, width, height, size, date FROM web_recent_v3 WHERE type = " + type, new Object[0]);
                    final ArrayList<SearchImage> arrayList = new ArrayList();
                    while (cursor.next()) {
                        SearchImage searchImage = new SearchImage();
                        searchImage.id = cursor.stringValue(0);
                        searchImage.imageUrl = cursor.stringValue(1);
                        searchImage.thumbUrl = cursor.stringValue(2);
                        searchImage.localUrl = cursor.stringValue(3);
                        searchImage.width = cursor.intValue(4);
                        searchImage.height = cursor.intValue(5);
                        searchImage.size = cursor.intValue(6);
                        searchImage.date = cursor.intValue(7);
                        searchImage.type = type;
                        arrayList.add(searchImage);
                    }
                    cursor.dispose();
                    Collections.sort(arrayList, new C05271());
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        public void run() {
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.recentImagesDidLoaded, Integer.valueOf(type), arrayList);
                        }
                    });
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
        });
    }

    public void addRecentLocalFile(final String imageUrl, final String localUrl) {
        if (imageUrl != null && localUrl != null && imageUrl.length() != 0 && localUrl.length() != 0) {
            this.storageQueue.postRunnable(new Runnable() {
                public void run() {
                    try {
                        MessagesStorage.this.database.executeFast("UPDATE web_recent_v3 SET local_url = '" + localUrl + "' WHERE image_url = '" + imageUrl + "'").stepThis().dispose();
                    } catch (Throwable e) {
                        FileLog.m611e("tmessages", e);
                    }
                }
            });
        }
    }

    public void clearWebRecent(final int type) {
        this.storageQueue.postRunnable(new Runnable() {
            public void run() {
                try {
                    MessagesStorage.this.database.executeFast("DELETE FROM web_recent_v3 WHERE type = " + type).stepThis().dispose();
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
        });
    }

    public void putWebRecent(final ArrayList<SearchImage> arrayList) {
        this.storageQueue.postRunnable(new Runnable() {
            public void run() {
                try {
                    MessagesStorage.this.database.beginTransaction();
                    SQLitePreparedStatement state = MessagesStorage.this.database.executeFast("REPLACE INTO web_recent_v3 VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)");
                    int a = 0;
                    while (a < arrayList.size() && a != 100) {
                        SearchImage searchImage = (SearchImage) arrayList.get(a);
                        if (searchImage.localUrl == null) {
                            searchImage.localUrl = "";
                        }
                        state.requery();
                        state.bindString(1, searchImage.id);
                        state.bindInteger(2, searchImage.type);
                        state.bindString(3, searchImage.imageUrl);
                        state.bindString(4, searchImage.thumbUrl);
                        state.bindString(5, searchImage.localUrl);
                        state.bindInteger(6, searchImage.width);
                        state.bindInteger(7, searchImage.height);
                        state.bindInteger(8, searchImage.size);
                        state.bindInteger(9, searchImage.date);
                        state.step();
                        a++;
                    }
                    state.dispose();
                    MessagesStorage.this.database.commitTransaction();
                    if (arrayList.size() >= 100) {
                        MessagesStorage.this.database.beginTransaction();
                        for (a = 100; a < arrayList.size(); a++) {
                            MessagesStorage.this.database.executeFast("DELETE FROM web_recent_v3 WHERE id = '" + ((SearchImage) arrayList.get(a)).id + "'").stepThis().dispose();
                        }
                        MessagesStorage.this.database.commitTransaction();
                    }
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
        });
    }

    public void getWallpapers() {
        this.storageQueue.postRunnable(new Runnable() {
            public void run() {
                try {
                    SQLiteCursor cursor = MessagesStorage.this.database.queryFinalized("SELECT data FROM wallpapers WHERE 1", new Object[0]);
                    final ArrayList<WallPaper> wallPapers = new ArrayList();
                    while (cursor.next()) {
                        NativeByteBuffer data = new NativeByteBuffer(cursor.byteArrayLength(0));
                        if (!(data == null || cursor.byteBufferValue(0, data) == 0)) {
                            wallPapers.add(WallPaper.TLdeserialize(data, data.readInt32(false), false));
                        }
                        data.reuse();
                    }
                    cursor.dispose();
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        public void run() {
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.wallpapersDidLoaded, wallPapers);
                        }
                    });
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
        });
    }

    public void getBlockedUsers() {
        this.storageQueue.postRunnable(new Runnable() {
            public void run() {
                try {
                    ArrayList<Integer> ids = new ArrayList();
                    ArrayList<User> users = new ArrayList();
                    SQLiteCursor cursor = MessagesStorage.this.database.queryFinalized("SELECT * FROM blocked_users WHERE 1", new Object[0]);
                    StringBuilder usersToLoad = new StringBuilder();
                    while (cursor.next()) {
                        int user_id = cursor.intValue(0);
                        ids.add(Integer.valueOf(user_id));
                        if (usersToLoad.length() != 0) {
                            usersToLoad.append(",");
                        }
                        usersToLoad.append(user_id);
                    }
                    cursor.dispose();
                    if (usersToLoad.length() != 0) {
                        MessagesStorage.this.getUsersInternal(usersToLoad.toString(), users);
                    }
                    MessagesController.getInstance().processLoadedBlockedUsers(ids, users, true);
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
        });
    }

    public void deleteBlockedUser(final int id) {
        this.storageQueue.postRunnable(new Runnable() {
            public void run() {
                try {
                    MessagesStorage.this.database.executeFast("DELETE FROM blocked_users WHERE uid = " + id).stepThis().dispose();
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
        });
    }

    public void putBlockedUsers(final ArrayList<Integer> ids, final boolean replace) {
        if (ids != null && !ids.isEmpty()) {
            this.storageQueue.postRunnable(new Runnable() {
                public void run() {
                    try {
                        if (replace) {
                            MessagesStorage.this.database.executeFast("DELETE FROM blocked_users WHERE 1").stepThis().dispose();
                        }
                        MessagesStorage.this.database.beginTransaction();
                        SQLitePreparedStatement state = MessagesStorage.this.database.executeFast("REPLACE INTO blocked_users VALUES(?)");
                        Iterator i$ = ids.iterator();
                        while (i$.hasNext()) {
                            Integer id = (Integer) i$.next();
                            state.requery();
                            state.bindInteger(1, id.intValue());
                            state.step();
                        }
                        state.dispose();
                        MessagesStorage.this.database.commitTransaction();
                    } catch (Throwable e) {
                        FileLog.m611e("tmessages", e);
                    }
                }
            });
        }
    }

    public void deleteDialog(final long did, final int messagesOnly) {
        this.storageQueue.postRunnable(new Runnable() {

            class C05081 implements Runnable {
                C05081() {
                }

                public void run() {
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.needReloadRecentDialogsSearch, new Object[0]);
                }
            }

            public void run() {
                try {
                    SQLiteCursor cursor;
                    NativeByteBuffer data;
                    Message message;
                    if (((int) did) == 0 || messagesOnly == 2) {
                        cursor = MessagesStorage.this.database.queryFinalized("SELECT data FROM messages WHERE uid = " + did, new Object[0]);
                        ArrayList<File> filesToDelete = new ArrayList();
                        while (cursor.next()) {
                            data = new NativeByteBuffer(cursor.byteArrayLength(0));
                            if (!(data == null || cursor.byteBufferValue(0, data) == 0)) {
                                message = Message.TLdeserialize(data, data.readInt32(false), false);
                                if (!(message == null || message.media == null)) {
                                    File file;
                                    if (message.media instanceof TL_messageMediaAudio) {
                                        file = FileLoader.getPathToAttach(message.media.audio);
                                        if (file != null && file.toString().length() > 0) {
                                            filesToDelete.add(file);
                                        }
                                    } else {
                                        try {
                                            if (message.media instanceof TL_messageMediaPhoto) {
                                                Iterator i$ = message.media.photo.sizes.iterator();
                                                while (i$.hasNext()) {
                                                    file = FileLoader.getPathToAttach((PhotoSize) i$.next());
                                                    if (file != null && file.toString().length() > 0) {
                                                        filesToDelete.add(file);
                                                    }
                                                }
                                            } else if (message.media instanceof TL_messageMediaVideo) {
                                                file = FileLoader.getPathToAttach(message.media.video);
                                                if (file != null && file.toString().length() > 0) {
                                                    filesToDelete.add(file);
                                                }
                                                file = FileLoader.getPathToAttach(message.media.video.thumb);
                                                if (file != null && file.toString().length() > 0) {
                                                    filesToDelete.add(file);
                                                }
                                            } else if (message.media instanceof TL_messageMediaDocument) {
                                                file = FileLoader.getPathToAttach(message.media.document);
                                                if (file != null && file.toString().length() > 0) {
                                                    filesToDelete.add(file);
                                                }
                                                file = FileLoader.getPathToAttach(message.media.document.thumb);
                                                if (file != null && file.toString().length() > 0) {
                                                    filesToDelete.add(file);
                                                }
                                            }
                                        } catch (Throwable e) {
                                            FileLog.m611e("tmessages", e);
                                        }
                                    }
                                }
                            }
                            data.reuse();
                        }
                        cursor.dispose();
                        FileLoader.getInstance().deleteFiles(filesToDelete, messagesOnly);
                    }
                    if (messagesOnly == 0) {
                        MessagesStorage.this.database.executeFast("DELETE FROM dialogs WHERE did = " + did).stepThis().dispose();
                        MessagesStorage.this.database.executeFast("DELETE FROM chat_settings_v2 WHERE uid = " + did).stepThis().dispose();
                        MessagesStorage.this.database.executeFast("DELETE FROM channel_users_v2 WHERE did = " + did).stepThis().dispose();
                        MessagesStorage.this.database.executeFast("DELETE FROM search_recent WHERE did = " + did).stepThis().dispose();
                        int lower_id = (int) did;
                        int high_id = (int) (did >> 32);
                        if (lower_id == 0) {
                            MessagesStorage.this.database.executeFast("DELETE FROM enc_chats WHERE uid = " + high_id).stepThis().dispose();
                        } else if (high_id == 1) {
                            MessagesStorage.this.database.executeFast("DELETE FROM chats WHERE uid = " + lower_id).stepThis().dispose();
                        } else if (lower_id < 0) {
                        }
                    } else if (messagesOnly == 2) {
                        cursor = MessagesStorage.this.database.queryFinalized("SELECT last_mid_i, last_mid FROM dialogs WHERE did = " + did, new Object[0]);
                        ArrayList<Message> arrayList = new ArrayList();
                        if (cursor.next()) {
                            long last_mid_i = cursor.longValue(0);
                            long last_mid = cursor.longValue(1);
                            SQLiteCursor cursor2 = MessagesStorage.this.database.queryFinalized("SELECT data FROM messages WHERE uid = " + did + " AND mid IN (" + last_mid_i + "," + last_mid + ")", new Object[0]);
                            while (cursor2.next()) {
                                try {
                                    data = new NativeByteBuffer(cursor2.byteArrayLength(0));
                                    if (!(data == null || cursor2.byteBufferValue(0, data) == 0)) {
                                        message = Message.TLdeserialize(data, data.readInt32(false), false);
                                        if (message != null) {
                                            arrayList.add(message);
                                        }
                                    }
                                    data.reuse();
                                } catch (Throwable e2) {
                                    FileLog.m611e("tmessages", e2);
                                }
                            }
                            cursor2.dispose();
                            MessagesStorage.this.database.executeFast("DELETE FROM messages WHERE uid = " + did + " AND mid != " + last_mid_i + " AND mid != " + last_mid).stepThis().dispose();
                            MessagesStorage.this.database.executeFast("DELETE FROM channel_group WHERE uid = " + did).stepThis().dispose();
                            MessagesStorage.this.database.executeFast("DELETE FROM messages_holes WHERE uid = " + did).stepThis().dispose();
                            MessagesStorage.this.database.executeFast("DELETE FROM messages_imp_holes WHERE uid = " + did).stepThis().dispose();
                            MessagesStorage.this.database.executeFast("DELETE FROM bot_keyboard WHERE uid = " + did).stepThis().dispose();
                            MessagesStorage.this.database.executeFast("DELETE FROM media_counts_v2 WHERE uid = " + did).stepThis().dispose();
                            MessagesStorage.this.database.executeFast("DELETE FROM media_v2 WHERE uid = " + did).stepThis().dispose();
                            MessagesStorage.this.database.executeFast("DELETE FROM media_holes_v2 WHERE uid = " + did).stepThis().dispose();
                            BotQuery.clearBotKeyboard(did, null);
                            SQLitePreparedStatement state5 = MessagesStorage.this.database.executeFast("REPLACE INTO messages_holes VALUES(?, ?, ?)");
                            SQLitePreparedStatement state6 = MessagesStorage.this.database.executeFast("REPLACE INTO media_holes_v2 VALUES(?, ?, ?, ?)");
                            SQLitePreparedStatement state7 = MessagesStorage.this.database.executeFast("REPLACE INTO messages_imp_holes VALUES(?, ?, ?)");
                            SQLitePreparedStatement state8 = MessagesStorage.this.database.executeFast("REPLACE INTO channel_group VALUES(?, ?, ?, ?)");
                            MessagesStorage.createFirstHoles(did, state5, state6, state7, state8, arrayList);
                            state5.dispose();
                            state6.dispose();
                            state7.dispose();
                            state8.dispose();
                        }
                        cursor.dispose();
                        return;
                    }
                    MessagesStorage.this.database.executeFast("UPDATE dialogs SET unread_count = 0, unread_count_i = 0 WHERE did = " + did).stepThis().dispose();
                    MessagesStorage.this.database.executeFast("DELETE FROM messages WHERE uid = " + did).stepThis().dispose();
                    MessagesStorage.this.database.executeFast("DELETE FROM channel_group WHERE uid = " + did).stepThis().dispose();
                    MessagesStorage.this.database.executeFast("DELETE FROM bot_keyboard WHERE uid = " + did).stepThis().dispose();
                    MessagesStorage.this.database.executeFast("DELETE FROM media_counts_v2 WHERE uid = " + did).stepThis().dispose();
                    MessagesStorage.this.database.executeFast("DELETE FROM media_v2 WHERE uid = " + did).stepThis().dispose();
                    MessagesStorage.this.database.executeFast("DELETE FROM messages_holes WHERE uid = " + did).stepThis().dispose();
                    MessagesStorage.this.database.executeFast("DELETE FROM messages_imp_holes WHERE uid = " + did).stepThis().dispose();
                    MessagesStorage.this.database.executeFast("DELETE FROM media_holes_v2 WHERE uid = " + did).stepThis().dispose();
                    BotQuery.clearBotKeyboard(did, null);
                    AndroidUtilities.runOnUIThread(new C05081());
                } catch (Throwable e22) {
                    FileLog.m611e("tmessages", e22);
                }
            }
        });
    }

    public void getUserPhotos(int uid, int offset, int count, long max_id, int classGuid) {
        final long j = max_id;
        final int i = uid;
        final int i2 = count;
        final int i3 = offset;
        final int i4 = classGuid;
        this.storageQueue.postRunnable(new Runnable() {
            public void run() {
                try {
                    SQLiteCursor cursor;
                    if (j != 0) {
                        cursor = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT data FROM user_photos WHERE uid = %d AND id < %d ORDER BY id DESC LIMIT %d", new Object[]{Integer.valueOf(i), Long.valueOf(j), Integer.valueOf(i2)}), new Object[0]);
                    } else {
                        cursor = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT data FROM user_photos WHERE uid = %d ORDER BY id DESC LIMIT %d,%d", new Object[]{Integer.valueOf(i), Integer.valueOf(i3), Integer.valueOf(i2)}), new Object[0]);
                    }
                    final photos_Photos res = new photos_Photos();
                    while (cursor.next()) {
                        NativeByteBuffer data = new NativeByteBuffer(cursor.byteArrayLength(0));
                        if (!(data == null || cursor.byteBufferValue(0, data) == 0)) {
                            res.photos.add(Photo.TLdeserialize(data, data.readInt32(false), false));
                        }
                        data.reuse();
                    }
                    cursor.dispose();
                    Utilities.stageQueue.postRunnable(new Runnable() {
                        public void run() {
                            MessagesController.getInstance().processLoadedUserPhotos(res, i, i3, i2, j, true, i4);
                        }
                    });
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
        });
    }

    public void clearUserPhotos(final int uid) {
        this.storageQueue.postRunnable(new Runnable() {
            public void run() {
                try {
                    MessagesStorage.this.database.executeFast("DELETE FROM user_photos WHERE uid = " + uid).stepThis().dispose();
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
        });
    }

    public void clearUserPhoto(final int uid, final long pid) {
        this.storageQueue.postRunnable(new Runnable() {
            public void run() {
                try {
                    MessagesStorage.this.database.executeFast("DELETE FROM user_photos WHERE uid = " + uid + " AND id = " + pid).stepThis().dispose();
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
        });
    }

    public void putUserPhotos(final int uid, final photos_Photos photos) {
        if (photos != null && !photos.photos.isEmpty()) {
            this.storageQueue.postRunnable(new Runnable() {
                public void run() {
                    try {
                        SQLitePreparedStatement state = MessagesStorage.this.database.executeFast("REPLACE INTO user_photos VALUES(?, ?, ?)");
                        Iterator i$ = photos.photos.iterator();
                        while (i$.hasNext()) {
                            Photo photo = (Photo) i$.next();
                            if (!(photo instanceof TL_photoEmpty)) {
                                state.requery();
                                NativeByteBuffer data = new NativeByteBuffer(photo.getObjectSize());
                                photo.serializeToStream(data);
                                state.bindInteger(1, uid);
                                state.bindLong(2, photo.id);
                                state.bindByteBuffer(3, data);
                                state.step();
                                data.reuse();
                            }
                        }
                        state.dispose();
                    } catch (Throwable e) {
                        FileLog.m611e("tmessages", e);
                    }
                }
            });
        }
    }

    public void getNewTask(final ArrayList<Integer> oldTask) {
        this.storageQueue.postRunnable(new Runnable() {
            public void run() {
                try {
                    if (oldTask != null) {
                        String ids = TextUtils.join(",", oldTask);
                        MessagesStorage.this.database.executeFast(String.format(Locale.US, "DELETE FROM enc_tasks_v2 WHERE mid IN(%s)", new Object[]{ids})).stepThis().dispose();
                    }
                    int date = 0;
                    ArrayList<Integer> arr = null;
                    SQLiteCursor cursor = MessagesStorage.this.database.queryFinalized("SELECT mid, date FROM enc_tasks_v2 WHERE date = (SELECT min(date) FROM enc_tasks_v2)", new Object[0]);
                    while (cursor.next()) {
                        Integer mid = Integer.valueOf(cursor.intValue(0));
                        date = cursor.intValue(1);
                        if (arr == null) {
                            arr = new ArrayList();
                        }
                        arr.add(mid);
                    }
                    cursor.dispose();
                    MessagesController.getInstance().processLoadedDeleteTask(date, arr);
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
        });
    }

    public void createTaskForSecretChat(int chat_id, int time, int readTime, int isOut, ArrayList<Long> random_ids) {
        final ArrayList<Long> arrayList = random_ids;
        final int i = chat_id;
        final int i2 = isOut;
        final int i3 = time;
        final int i4 = readTime;
        this.storageQueue.postRunnable(new Runnable() {
            public void run() {
                int minDate = ConnectionsManager.DEFAULT_DATACENTER_ID;
                try {
                    SQLiteCursor cursor;
                    SparseArray<ArrayList<Integer>> messages = new SparseArray();
                    StringBuilder mids = new StringBuilder();
                    if (arrayList == null) {
                        cursor = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT mid, ttl FROM messages WHERE uid = %d AND out = %d AND read_state != 0 AND ttl > 0 AND date <= %d AND send_state = 0 AND media != 1", new Object[]{Long.valueOf(((long) i) << 32), Integer.valueOf(i2), Integer.valueOf(i3)}), new Object[0]);
                    } else {
                        String ids = TextUtils.join(",", arrayList);
                        cursor = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT m.mid, m.ttl FROM messages as m INNER JOIN randoms as r ON m.mid = r.mid WHERE r.random_id IN (%s)", new Object[]{ids}), new Object[0]);
                    }
                    while (cursor.next()) {
                        int ttl = cursor.intValue(1);
                        if (ttl > 0) {
                            int mid = cursor.intValue(0);
                            int date = Math.min(i4, i3) + ttl;
                            minDate = Math.min(minDate, date);
                            ArrayList<Integer> arr = (ArrayList) messages.get(date);
                            if (arr == null) {
                                arr = new ArrayList();
                                messages.put(date, arr);
                            }
                            if (mids.length() != 0) {
                                mids.append(",");
                            }
                            mids.append(mid);
                            arr.add(Integer.valueOf(mid));
                        }
                    }
                    cursor.dispose();
                    if (messages.size() != 0) {
                        MessagesStorage.this.database.beginTransaction();
                        SQLitePreparedStatement state = MessagesStorage.this.database.executeFast("REPLACE INTO enc_tasks_v2 VALUES(?, ?)");
                        for (int a = 0; a < messages.size(); a++) {
                            int key = messages.keyAt(a);
                            Iterator i$ = ((ArrayList) messages.get(key)).iterator();
                            while (i$.hasNext()) {
                                Integer mid2 = (Integer) i$.next();
                                state.requery();
                                state.bindInteger(1, mid2.intValue());
                                state.bindInteger(2, key);
                                state.step();
                            }
                        }
                        state.dispose();
                        MessagesStorage.this.database.commitTransaction();
                        MessagesStorage.this.database.executeFast(String.format(Locale.US, "UPDATE messages SET ttl = 0 WHERE mid IN(%s)", new Object[]{mids.toString()})).stepThis().dispose();
                        MessagesController.getInstance().didAddedNewTask(minDate, messages);
                    }
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
        });
    }

    private void updateDialogsWithReadMessagesInternal(ArrayList<Integer> messages, SparseArray<Long> inbox) {
        try {
            SQLitePreparedStatement state;
            HashMap<Long, Integer> dialogsToUpdate = new HashMap();
            SQLiteCursor cursor;
            if (messages != null && !messages.isEmpty()) {
                String ids = TextUtils.join(",", messages);
                cursor = this.database.queryFinalized(String.format(Locale.US, "SELECT uid, read_state, out FROM messages WHERE mid IN(%s)", new Object[]{ids}), new Object[0]);
                while (cursor.next()) {
                    if (cursor.intValue(2) == 0 && cursor.intValue(1) == 0) {
                        long uid = cursor.longValue(0);
                        Integer currentCount = (Integer) dialogsToUpdate.get(Long.valueOf(uid));
                        if (currentCount == null) {
                            dialogsToUpdate.put(Long.valueOf(uid), Integer.valueOf(1));
                        } else {
                            dialogsToUpdate.put(Long.valueOf(uid), Integer.valueOf(currentCount.intValue() + 1));
                        }
                    }
                }
                cursor.dispose();
            } else if (!(inbox == null || inbox.size() == 0)) {
                for (int b = 0; b < inbox.size(); b++) {
                    int key = inbox.keyAt(b);
                    long messageId = ((Long) inbox.get(key)).longValue();
                    cursor = this.database.queryFinalized(String.format(Locale.US, "SELECT COUNT(mid) FROM messages WHERE uid = %d AND mid > %d AND read_state IN(0,2) AND out = 0", new Object[]{Integer.valueOf(key), Long.valueOf(messageId)}), new Object[0]);
                    if (cursor.next()) {
                        int count = cursor.intValue(0);
                        dialogsToUpdate.put(Long.valueOf((long) key), Integer.valueOf(count));
                    }
                    cursor.dispose();
                    state = this.database.executeFast("UPDATE dialogs SET inbox_max = max((SELECT inbox_max FROM dialogs WHERE did = ?), ?) WHERE did = ?");
                    state.requery();
                    state.bindLong(1, (long) key);
                    state.bindInteger(2, (int) messageId);
                    state.bindLong(3, (long) key);
                    state.step();
                    state.dispose();
                }
            }
            if (!dialogsToUpdate.isEmpty()) {
                this.database.beginTransaction();
                state = this.database.executeFast("UPDATE dialogs SET unread_count = ? WHERE did = ?");
                for (Entry<Long, Integer> entry : dialogsToUpdate.entrySet()) {
                    state.requery();
                    state.bindInteger(1, ((Integer) entry.getValue()).intValue());
                    state.bindLong(2, ((Long) entry.getKey()).longValue());
                    state.step();
                }
                state.dispose();
                this.database.commitTransaction();
            }
            if (!dialogsToUpdate.isEmpty()) {
                MessagesController.getInstance().processDialogsUpdateRead(dialogsToUpdate);
            }
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
        }
    }

    public void updateDialogsWithReadMessages(final SparseArray<Long> inbox, boolean useQueue) {
        if (inbox.size() != 0) {
            if (useQueue) {
                this.storageQueue.postRunnable(new Runnable() {
                    public void run() {
                        MessagesStorage.this.updateDialogsWithReadMessagesInternal(null, inbox);
                    }
                });
            } else {
                updateDialogsWithReadMessagesInternal(null, inbox);
            }
        }
    }

    public void updateChatParticipants(final ChatParticipants participants) {
        this.storageQueue.postRunnable(new Runnable() {
            public void run() {
                try {
                    NativeByteBuffer data;
                    SQLiteCursor cursor = MessagesStorage.this.database.queryFinalized("SELECT info FROM chat_settings_v2 WHERE uid = " + participants.chat_id, new Object[0]);
                    ChatFull info = null;
                    ArrayList<User> loadedUsers = new ArrayList();
                    if (cursor.next()) {
                        data = new NativeByteBuffer(cursor.byteArrayLength(0));
                        if (!(data == null || cursor.byteBufferValue(0, data) == 0)) {
                            info = ChatFull.TLdeserialize(data, data.readInt32(false), false);
                        }
                        data.reuse();
                    }
                    cursor.dispose();
                    if (info instanceof TL_chatFull) {
                        info.participants = participants;
                        final ChatFull finalInfo = info;
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            public void run() {
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.chatInfoDidLoaded, finalInfo, Integer.valueOf(0), Boolean.valueOf(false));
                            }
                        });
                        SQLitePreparedStatement state = MessagesStorage.this.database.executeFast("REPLACE INTO chat_settings_v2 VALUES(?, ?)");
                        data = new NativeByteBuffer(info.getObjectSize());
                        info.serializeToStream(data);
                        state.bindInteger(1, info.id);
                        state.bindByteBuffer(2, data);
                        state.step();
                        state.dispose();
                        data.reuse();
                    }
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
        });
    }

    public void updateChannelUsers(final int channel_id, final ArrayList<ChannelParticipant> participants) {
        this.storageQueue.postRunnable(new Runnable() {
            public void run() {
                try {
                    long did = (long) (-channel_id);
                    MessagesStorage.this.database.executeFast("DELETE FROM channel_users_v2 WHERE did = " + did).stepThis().dispose();
                    MessagesStorage.this.database.beginTransaction();
                    SQLitePreparedStatement state = MessagesStorage.this.database.executeFast("REPLACE INTO channel_users_v2 VALUES(?, ?, ?, ?)");
                    int date = (int) (System.currentTimeMillis() / 1000);
                    for (int a = 0; a < participants.size(); a++) {
                        ChannelParticipant participant = (ChannelParticipant) participants.get(a);
                        state.requery();
                        state.bindLong(1, did);
                        state.bindInteger(2, participant.user_id);
                        state.bindInteger(3, date);
                        NativeByteBuffer data = new NativeByteBuffer(participant.getObjectSize());
                        participant.serializeToStream(data);
                        state.bindByteBuffer(4, data);
                        data.reuse();
                        state.step();
                        date--;
                    }
                    state.dispose();
                    MessagesStorage.this.database.commitTransaction();
                    MessagesStorage.this.loadChatInfo(channel_id, null, false, true);
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
        });
    }

    public void updateChatInfo(final ChatFull info, final boolean ifExist) {
        this.storageQueue.postRunnable(new Runnable() {
            public void run() {
                try {
                    SQLiteCursor cursor;
                    if (ifExist) {
                        boolean dontExist = true;
                        cursor = MessagesStorage.this.database.queryFinalized("SELECT uid FROM chat_settings_v2 WHERE uid = " + info.id, new Object[0]);
                        if (cursor.next()) {
                            dontExist = false;
                        }
                        cursor.dispose();
                        if (dontExist) {
                            return;
                        }
                    }
                    SQLitePreparedStatement state = MessagesStorage.this.database.executeFast("REPLACE INTO chat_settings_v2 VALUES(?, ?)");
                    NativeByteBuffer data = new NativeByteBuffer(info.getObjectSize());
                    info.serializeToStream(data);
                    state.bindInteger(1, info.id);
                    state.bindByteBuffer(2, data);
                    state.step();
                    state.dispose();
                    data.reuse();
                    if (info instanceof TL_channelFull) {
                        cursor = MessagesStorage.this.database.queryFinalized("SELECT date, last_mid_i, pts, date_i, last_mid FROM dialogs WHERE did = " + (-info.id), new Object[0]);
                        if (cursor.next()) {
                            int dialog_date = cursor.intValue(0);
                            long last_mid_i = cursor.longValue(1);
                            int pts = cursor.intValue(2);
                            int dialog_date_i = cursor.intValue(3);
                            long last_mid = cursor.longValue(4);
                            state = MessagesStorage.this.database.executeFast("REPLACE INTO dialogs VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                            state.bindLong(1, (long) (-info.id));
                            state.bindInteger(2, dialog_date);
                            state.bindInteger(3, info.unread_important_count);
                            state.bindLong(4, last_mid);
                            state.bindInteger(5, info.read_inbox_max_id);
                            state.bindInteger(6, 0);
                            state.bindLong(7, last_mid_i);
                            state.bindInteger(8, info.unread_count);
                            state.bindInteger(9, pts);
                            state.bindInteger(10, dialog_date_i);
                            state.step();
                            state.dispose();
                        }
                        cursor.dispose();
                    }
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
        });
    }

    public void updateChatInfo(int chat_id, int user_id, int what, int invited_id, int version) {
        final int i = chat_id;
        final int i2 = what;
        final int i3 = user_id;
        final int i4 = invited_id;
        final int i5 = version;
        this.storageQueue.postRunnable(new Runnable() {
            public void run() {
                try {
                    NativeByteBuffer data;
                    SQLiteCursor cursor = MessagesStorage.this.database.queryFinalized("SELECT info FROM chat_settings_v2 WHERE uid = " + i, new Object[0]);
                    ChatFull info = null;
                    ArrayList<User> loadedUsers = new ArrayList();
                    if (cursor.next()) {
                        data = new NativeByteBuffer(cursor.byteArrayLength(0));
                        if (!(data == null || cursor.byteBufferValue(0, data) == 0)) {
                            info = ChatFull.TLdeserialize(data, data.readInt32(false), false);
                        }
                        data.reuse();
                    }
                    cursor.dispose();
                    if (info instanceof TL_chatFull) {
                        int a;
                        if (i2 == 1) {
                            for (a = 0; a < info.participants.participants.size(); a++) {
                                if (((ChatParticipant) info.participants.participants.get(a)).user_id == i3) {
                                    info.participants.participants.remove(a);
                                    break;
                                }
                            }
                        } else if (i2 == 0) {
                            Iterator i$ = info.participants.participants.iterator();
                            while (i$.hasNext()) {
                                if (((ChatParticipant) i$.next()).user_id == i3) {
                                    return;
                                }
                            }
                            TL_chatParticipant participant = new TL_chatParticipant();
                            participant.user_id = i3;
                            participant.inviter_id = i4;
                            participant.date = ConnectionsManager.getInstance().getCurrentTime();
                            info.participants.participants.add(participant);
                        } else if (i2 == 2) {
                            a = 0;
                            while (a < info.participants.participants.size()) {
                                ChatParticipant participant2 = (ChatParticipant) info.participants.participants.get(a);
                                if (participant2.user_id == i3) {
                                    ChatParticipant newParticipant;
                                    if (i4 == 1) {
                                        newParticipant = new TL_chatParticipantAdmin();
                                        newParticipant.user_id = participant2.user_id;
                                        newParticipant.date = participant2.date;
                                        newParticipant.inviter_id = participant2.inviter_id;
                                    } else {
                                        newParticipant = new TL_chatParticipant();
                                        newParticipant.user_id = participant2.user_id;
                                        newParticipant.date = participant2.date;
                                        newParticipant.inviter_id = participant2.inviter_id;
                                    }
                                    info.participants.participants.set(a, newParticipant);
                                } else {
                                    a++;
                                }
                            }
                        }
                        info.participants.version = i5;
                        final ChatFull finalInfo = info;
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            public void run() {
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.chatInfoDidLoaded, finalInfo, Integer.valueOf(0), Boolean.valueOf(false));
                            }
                        });
                        SQLitePreparedStatement state = MessagesStorage.this.database.executeFast("REPLACE INTO chat_settings_v2 VALUES(?, ?)");
                        data = new NativeByteBuffer(info.getObjectSize());
                        info.serializeToStream(data);
                        state.bindInteger(1, i);
                        state.bindByteBuffer(2, data);
                        state.step();
                        state.dispose();
                        data.reuse();
                    }
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
        });
    }

    public boolean isMigratedChat(final int chat_id) {
        final Semaphore semaphore = new Semaphore(0);
        final boolean[] result = new boolean[1];
        this.storageQueue.postRunnable(new Runnable() {
            public void run() {
                /* JADX: method processing error */
/*
Error: java.util.NoSuchElementException
	at java.util.HashMap$HashIterator.nextNode(HashMap.java:1431)
	at java.util.HashMap$KeyIterator.next(HashMap.java:1453)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.applyRemove(BlockFinallyExtract.java:535)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.extractFinally(BlockFinallyExtract.java:175)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.processExceptionHandler(BlockFinallyExtract.java:79)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:51)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
                /*
                r9 = this;
                r5 = 0;
                r6 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x0073, all -> 0x0083 }
                r6 = r6.database;	 Catch:{ Exception -> 0x0073, all -> 0x0083 }
                r7 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0073, all -> 0x0083 }
                r7.<init>();	 Catch:{ Exception -> 0x0073, all -> 0x0083 }
                r8 = "SELECT info FROM chat_settings_v2 WHERE uid = ";	 Catch:{ Exception -> 0x0073, all -> 0x0083 }
                r7 = r7.append(r8);	 Catch:{ Exception -> 0x0073, all -> 0x0083 }
                r8 = r7;	 Catch:{ Exception -> 0x0073, all -> 0x0083 }
                r7 = r7.append(r8);	 Catch:{ Exception -> 0x0073, all -> 0x0083 }
                r7 = r7.toString();	 Catch:{ Exception -> 0x0073, all -> 0x0083 }
                r8 = 0;	 Catch:{ Exception -> 0x0073, all -> 0x0083 }
                r8 = new java.lang.Object[r8];	 Catch:{ Exception -> 0x0073, all -> 0x0083 }
                r0 = r6.queryFinalized(r7, r8);	 Catch:{ Exception -> 0x0073, all -> 0x0083 }
                r3 = 0;	 Catch:{ Exception -> 0x0073, all -> 0x0083 }
                r4 = new java.util.ArrayList;	 Catch:{ Exception -> 0x0073, all -> 0x0083 }
                r4.<init>();	 Catch:{ Exception -> 0x0073, all -> 0x0083 }
                r6 = r0.next();	 Catch:{ Exception -> 0x0073, all -> 0x0083 }
                if (r6 == 0) goto L_0x004f;	 Catch:{ Exception -> 0x0073, all -> 0x0083 }
            L_0x002f:
                r1 = new org.telegram.tgnet.NativeByteBuffer;	 Catch:{ Exception -> 0x0073, all -> 0x0083 }
                r6 = 0;	 Catch:{ Exception -> 0x0073, all -> 0x0083 }
                r6 = r0.byteArrayLength(r6);	 Catch:{ Exception -> 0x0073, all -> 0x0083 }
                r1.<init>(r6);	 Catch:{ Exception -> 0x0073, all -> 0x0083 }
                if (r1 == 0) goto L_0x004c;	 Catch:{ Exception -> 0x0073, all -> 0x0083 }
            L_0x003b:
                r6 = 0;	 Catch:{ Exception -> 0x0073, all -> 0x0083 }
                r6 = r0.byteBufferValue(r6, r1);	 Catch:{ Exception -> 0x0073, all -> 0x0083 }
                if (r6 == 0) goto L_0x004c;	 Catch:{ Exception -> 0x0073, all -> 0x0083 }
            L_0x0042:
                r6 = 0;	 Catch:{ Exception -> 0x0073, all -> 0x0083 }
                r6 = r1.readInt32(r6);	 Catch:{ Exception -> 0x0073, all -> 0x0083 }
                r7 = 0;	 Catch:{ Exception -> 0x0073, all -> 0x0083 }
                r3 = org.telegram.tgnet.TLRPC.ChatFull.TLdeserialize(r1, r6, r7);	 Catch:{ Exception -> 0x0073, all -> 0x0083 }
            L_0x004c:
                r1.reuse();	 Catch:{ Exception -> 0x0073, all -> 0x0083 }
            L_0x004f:
                r0.dispose();	 Catch:{ Exception -> 0x0073, all -> 0x0083 }
                r6 = r1;	 Catch:{ Exception -> 0x0073, all -> 0x0083 }
                r7 = 0;	 Catch:{ Exception -> 0x0073, all -> 0x0083 }
                r8 = r3 instanceof org.telegram.tgnet.TLRPC.TL_channelFull;	 Catch:{ Exception -> 0x0073, all -> 0x0083 }
                if (r8 == 0) goto L_0x005e;	 Catch:{ Exception -> 0x0073, all -> 0x0083 }
            L_0x0059:
                r8 = r3.migrated_from_chat_id;	 Catch:{ Exception -> 0x0073, all -> 0x0083 }
                if (r8 == 0) goto L_0x005e;	 Catch:{ Exception -> 0x0073, all -> 0x0083 }
            L_0x005d:
                r5 = 1;	 Catch:{ Exception -> 0x0073, all -> 0x0083 }
            L_0x005e:
                r6[r7] = r5;	 Catch:{ Exception -> 0x0073, all -> 0x0083 }
                r5 = r2;	 Catch:{ Exception -> 0x0073, all -> 0x0083 }
                if (r5 == 0) goto L_0x0069;	 Catch:{ Exception -> 0x0073, all -> 0x0083 }
            L_0x0064:
                r5 = r2;	 Catch:{ Exception -> 0x0073, all -> 0x0083 }
                r5.release();	 Catch:{ Exception -> 0x0073, all -> 0x0083 }
            L_0x0069:
                r5 = r2;
                if (r5 == 0) goto L_0x0072;
            L_0x006d:
                r5 = r2;
                r5.release();
            L_0x0072:
                return;
            L_0x0073:
                r2 = move-exception;
                r5 = "tmessages";	 Catch:{ Exception -> 0x0073, all -> 0x0083 }
                org.telegram.messenger.FileLog.m611e(r5, r2);	 Catch:{ Exception -> 0x0073, all -> 0x0083 }
                r5 = r2;
                if (r5 == 0) goto L_0x0072;
            L_0x007d:
                r5 = r2;
                r5.release();
                goto L_0x0072;
            L_0x0083:
                r5 = move-exception;
                r6 = r2;
                if (r6 == 0) goto L_0x008d;
            L_0x0088:
                r6 = r2;
                r6.release();
            L_0x008d:
                throw r5;
                */
                throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MessagesStorage.29.run():void");
            }
        });
        try {
            semaphore.acquire();
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
        }
        return result[0];
    }

    public void loadChatInfo(int chat_id, Semaphore semaphore, boolean force, boolean byChannelUsers) {
        final int i = chat_id;
        final Semaphore semaphore2 = semaphore;
        final boolean z = force;
        final boolean z2 = byChannelUsers;
        this.storageQueue.postRunnable(new Runnable() {
            public void run() {
                /* JADX: method processing error */
/*
Error: java.util.NoSuchElementException
	at java.util.HashMap$HashIterator.nextNode(HashMap.java:1431)
	at java.util.HashMap$KeyIterator.next(HashMap.java:1453)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.applyRemove(BlockFinallyExtract.java:535)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.extractFinally(BlockFinallyExtract.java:175)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.processExceptionHandler(BlockFinallyExtract.java:79)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:51)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
                /*
                r19 = this;
                r0 = r19;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r1 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r1 = r1.database;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r2 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r2.<init>();	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r5 = "SELECT info FROM chat_settings_v2 WHERE uid = ";	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r2 = r2.append(r5);	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r0 = r19;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r5 = r2;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r2 = r2.append(r5);	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r2 = r2.toString();	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r5 = 0;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r5 = new java.lang.Object[r5];	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r12 = r1.queryFinalized(r2, r5);	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r3 = 0;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r4 = new java.util.ArrayList;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r4.<init>();	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r1 = r12.next();	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                if (r1 == 0) goto L_0x0052;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
            L_0x0032:
                r13 = new org.telegram.tgnet.NativeByteBuffer;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r1 = 0;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r1 = r12.byteArrayLength(r1);	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r13.<init>(r1);	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                if (r13 == 0) goto L_0x004f;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
            L_0x003e:
                r1 = 0;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r1 = r12.byteBufferValue(r1, r13);	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                if (r1 == 0) goto L_0x004f;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
            L_0x0045:
                r1 = 0;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r1 = r13.readInt32(r1);	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r2 = 0;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r3 = org.telegram.tgnet.TLRPC.ChatFull.TLdeserialize(r13, r1, r2);	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
            L_0x004f:
                r13.reuse();	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
            L_0x0052:
                r12.dispose();	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r1 = r3 instanceof org.telegram.tgnet.TLRPC.TL_chatFull;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                if (r1 == 0) goto L_0x00ca;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
            L_0x0059:
                r18 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r18.<init>();	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r8 = 0;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
            L_0x005f:
                r1 = r3.participants;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r1 = r1.participants;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r1 = r1.size();	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                if (r8 >= r1) goto L_0x008a;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
            L_0x0069:
                r1 = r3.participants;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r1 = r1.participants;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r10 = r1.get(r8);	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r10 = (org.telegram.tgnet.TLRPC.ChatParticipant) r10;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r1 = r18.length();	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                if (r1 == 0) goto L_0x0080;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
            L_0x0079:
                r1 = ",";	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r0 = r18;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r0.append(r1);	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
            L_0x0080:
                r1 = r10.user_id;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r0 = r18;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r0.append(r1);	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r8 = r8 + 1;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                goto L_0x005f;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
            L_0x008a:
                r1 = r18.length();	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                if (r1 == 0) goto L_0x009b;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
            L_0x0090:
                r0 = r19;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r1 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r2 = r18.toString();	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r1.getUsersInternal(r2, r4);	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
            L_0x009b:
                r0 = r19;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r1 = r3;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                if (r1 == 0) goto L_0x00a8;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
            L_0x00a1:
                r0 = r19;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r1 = r3;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r1.release();	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
            L_0x00a8:
                r1 = org.telegram.messenger.MessagesController.getInstance();	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r0 = r19;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r2 = r2;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r5 = 1;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r0 = r19;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r6 = r4;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r0 = r19;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r7 = r5;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r1.processChatInfo(r2, r3, r4, r5, r6, r7);	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r0 = r19;
                r1 = r3;
                if (r1 == 0) goto L_0x00c9;
            L_0x00c2:
                r0 = r19;
                r1 = r3;
                r1.release();
            L_0x00c9:
                return;
            L_0x00ca:
                r1 = r3 instanceof org.telegram.tgnet.TLRPC.TL_channelFull;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                if (r1 == 0) goto L_0x009b;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
            L_0x00ce:
                r0 = r19;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r1 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r1 = r1.database;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r2 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r2.<init>();	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r5 = "SELECT us.data, us.status, cu.data, cu.date FROM channel_users_v2 as cu LEFT JOIN users as us ON us.uid = cu.uid WHERE cu.did = ";	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r2 = r2.append(r5);	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r0 = r19;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r5 = r2;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r5 = -r5;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r2 = r2.append(r5);	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r5 = " ORDER BY cu.date DESC";	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r2 = r2.append(r5);	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r2 = r2.toString();	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r5 = 0;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r5 = new java.lang.Object[r5];	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r12 = r1.queryFinalized(r2, r5);	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r1 = new org.telegram.tgnet.TLRPC$TL_chatParticipants;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r1.<init>();	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r3.participants = r1;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
            L_0x0102:
                r1 = r12.next();	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                if (r1 == 0) goto L_0x01ac;
            L_0x0108:
                r13 = new org.telegram.tgnet.NativeByteBuffer;	 Catch:{ Exception -> 0x018f }
                r1 = 0;	 Catch:{ Exception -> 0x018f }
                r1 = r12.byteArrayLength(r1);	 Catch:{ Exception -> 0x018f }
                r13.<init>(r1);	 Catch:{ Exception -> 0x018f }
                r14 = new org.telegram.tgnet.NativeByteBuffer;	 Catch:{ Exception -> 0x018f }
                r1 = 2;	 Catch:{ Exception -> 0x018f }
                r1 = r12.byteArrayLength(r1);	 Catch:{ Exception -> 0x018f }
                r14.<init>(r1);	 Catch:{ Exception -> 0x018f }
                if (r13 == 0) goto L_0x0187;	 Catch:{ Exception -> 0x018f }
            L_0x011e:
                r1 = 0;	 Catch:{ Exception -> 0x018f }
                r1 = r12.byteBufferValue(r1, r13);	 Catch:{ Exception -> 0x018f }
                if (r1 == 0) goto L_0x0187;	 Catch:{ Exception -> 0x018f }
            L_0x0125:
                if (r14 == 0) goto L_0x0187;	 Catch:{ Exception -> 0x018f }
            L_0x0127:
                r1 = 2;	 Catch:{ Exception -> 0x018f }
                r1 = r12.byteBufferValue(r1, r14);	 Catch:{ Exception -> 0x018f }
                if (r1 == 0) goto L_0x0187;	 Catch:{ Exception -> 0x018f }
            L_0x012e:
                r1 = 0;	 Catch:{ Exception -> 0x018f }
                r1 = r13.readInt32(r1);	 Catch:{ Exception -> 0x018f }
                r2 = 0;	 Catch:{ Exception -> 0x018f }
                r17 = org.telegram.tgnet.TLRPC.User.TLdeserialize(r13, r1, r2);	 Catch:{ Exception -> 0x018f }
                r1 = 0;	 Catch:{ Exception -> 0x018f }
                r1 = r14.readInt32(r1);	 Catch:{ Exception -> 0x018f }
                r2 = 0;	 Catch:{ Exception -> 0x018f }
                r16 = org.telegram.tgnet.TLRPC.ChannelParticipant.TLdeserialize(r14, r1, r2);	 Catch:{ Exception -> 0x018f }
                if (r17 == 0) goto L_0x0187;	 Catch:{ Exception -> 0x018f }
            L_0x0144:
                if (r16 == 0) goto L_0x0187;	 Catch:{ Exception -> 0x018f }
            L_0x0146:
                r0 = r17;	 Catch:{ Exception -> 0x018f }
                r1 = r0.status;	 Catch:{ Exception -> 0x018f }
                if (r1 == 0) goto L_0x0157;	 Catch:{ Exception -> 0x018f }
            L_0x014c:
                r0 = r17;	 Catch:{ Exception -> 0x018f }
                r1 = r0.status;	 Catch:{ Exception -> 0x018f }
                r2 = 1;	 Catch:{ Exception -> 0x018f }
                r2 = r12.intValue(r2);	 Catch:{ Exception -> 0x018f }
                r1.expires = r2;	 Catch:{ Exception -> 0x018f }
            L_0x0157:
                r0 = r17;	 Catch:{ Exception -> 0x018f }
                r4.add(r0);	 Catch:{ Exception -> 0x018f }
                r1 = 3;	 Catch:{ Exception -> 0x018f }
                r1 = r12.intValue(r1);	 Catch:{ Exception -> 0x018f }
                r0 = r16;	 Catch:{ Exception -> 0x018f }
                r0.date = r1;	 Catch:{ Exception -> 0x018f }
                r11 = new org.telegram.tgnet.TLRPC$TL_chatChannelParticipant;	 Catch:{ Exception -> 0x018f }
                r11.<init>();	 Catch:{ Exception -> 0x018f }
                r0 = r16;	 Catch:{ Exception -> 0x018f }
                r1 = r0.user_id;	 Catch:{ Exception -> 0x018f }
                r11.user_id = r1;	 Catch:{ Exception -> 0x018f }
                r0 = r16;	 Catch:{ Exception -> 0x018f }
                r1 = r0.date;	 Catch:{ Exception -> 0x018f }
                r11.date = r1;	 Catch:{ Exception -> 0x018f }
                r0 = r16;	 Catch:{ Exception -> 0x018f }
                r1 = r0.inviter_id;	 Catch:{ Exception -> 0x018f }
                r11.inviter_id = r1;	 Catch:{ Exception -> 0x018f }
                r0 = r16;	 Catch:{ Exception -> 0x018f }
                r11.channelParticipant = r0;	 Catch:{ Exception -> 0x018f }
                r1 = r3.participants;	 Catch:{ Exception -> 0x018f }
                r1 = r1.participants;	 Catch:{ Exception -> 0x018f }
                r1.add(r11);	 Catch:{ Exception -> 0x018f }
            L_0x0187:
                r13.reuse();	 Catch:{ Exception -> 0x018f }
                r14.reuse();	 Catch:{ Exception -> 0x018f }
                goto L_0x0102;
            L_0x018f:
                r15 = move-exception;
                r1 = "tmessages";	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                org.telegram.messenger.FileLog.m611e(r1, r15);	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                goto L_0x0102;
            L_0x0197:
                r15 = move-exception;
                r1 = "tmessages";	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                org.telegram.messenger.FileLog.m611e(r1, r15);	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r0 = r19;
                r1 = r3;
                if (r1 == 0) goto L_0x00c9;
            L_0x01a3:
                r0 = r19;
                r1 = r3;
                r1.release();
                goto L_0x00c9;
            L_0x01ac:
                r12.dispose();	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r18 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r18.<init>();	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r8 = 0;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
            L_0x01b5:
                r1 = r3.bot_info;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r1 = r1.size();	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                if (r8 >= r1) goto L_0x01dc;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
            L_0x01bd:
                r1 = r3.bot_info;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r9 = r1.get(r8);	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r9 = (org.telegram.tgnet.TLRPC.BotInfo) r9;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r1 = r18.length();	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                if (r1 == 0) goto L_0x01d2;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
            L_0x01cb:
                r1 = ",";	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r0 = r18;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r0.append(r1);	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
            L_0x01d2:
                r1 = r9.user_id;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r0 = r18;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r0.append(r1);	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r8 = r8 + 1;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                goto L_0x01b5;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
            L_0x01dc:
                r1 = r18.length();	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                if (r1 == 0) goto L_0x009b;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
            L_0x01e2:
                r0 = r19;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r1 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r2 = r18.toString();	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                r1.getUsersInternal(r2, r4);	 Catch:{ Exception -> 0x0197, all -> 0x01ef }
                goto L_0x009b;
            L_0x01ef:
                r1 = move-exception;
                r0 = r19;
                r2 = r3;
                if (r2 == 0) goto L_0x01fd;
            L_0x01f6:
                r0 = r19;
                r2 = r3;
                r2.release();
            L_0x01fd:
                throw r1;
                */
                throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MessagesStorage.30.run():void");
            }
        });
    }

    public void processPendingRead(long dialog_id, long max_id, int max_date, boolean delete) {
        final boolean z = delete;
        final long j = dialog_id;
        final long j2 = max_id;
        final int i = max_date;
        this.storageQueue.postRunnable(new Runnable() {
            public void run() {
                try {
                    if (!z) {
                        SQLitePreparedStatement state;
                        MessagesStorage.this.database.beginTransaction();
                        if (((int) j) != 0) {
                            state = MessagesStorage.this.database.executeFast("UPDATE messages SET read_state = read_state | 1 WHERE uid = ? AND mid <= ? AND read_state IN(0,2) AND out = 0");
                            state.requery();
                            state.bindLong(1, j);
                            state.bindLong(2, j2);
                            state.step();
                            state.dispose();
                        } else {
                            state = MessagesStorage.this.database.executeFast("UPDATE messages SET read_state = read_state | 1 WHERE uid = ? AND date <= ? AND read_state IN(0,2) AND out = 0");
                            state.requery();
                            state.bindLong(1, j);
                            state.bindInteger(2, i);
                            state.step();
                            state.dispose();
                        }
                        int currentMaxId = 0;
                        SQLiteCursor cursor = MessagesStorage.this.database.queryFinalized("SELECT inbox_max FROM dialogs WHERE did = " + j, new Object[0]);
                        if (cursor.next()) {
                            currentMaxId = cursor.intValue(0);
                        }
                        cursor.dispose();
                        currentMaxId = Math.max(currentMaxId, (int) j2);
                        state = MessagesStorage.this.database.executeFast("UPDATE dialogs SET unread_count = 0, unread_count_i = 0, inbox_max = ? WHERE did = ?");
                        state.requery();
                        state.bindInteger(1, currentMaxId);
                        state.bindLong(2, j);
                        state.step();
                        state.dispose();
                        MessagesStorage.this.database.commitTransaction();
                    }
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
        });
    }

    public void putContacts(ArrayList<TL_contact> contacts, final boolean deleteAll) {
        if (!contacts.isEmpty()) {
            final ArrayList<TL_contact> contactsCopy = new ArrayList(contacts);
            this.storageQueue.postRunnable(new Runnable() {
                public void run() {
                    try {
                        if (deleteAll) {
                            MessagesStorage.this.database.executeFast("DELETE FROM contacts WHERE 1").stepThis().dispose();
                        }
                        MessagesStorage.this.database.beginTransaction();
                        SQLitePreparedStatement state = MessagesStorage.this.database.executeFast("REPLACE INTO contacts VALUES(?, ?)");
                        for (int a = 0; a < contactsCopy.size(); a++) {
                            TL_contact contact = (TL_contact) contactsCopy.get(a);
                            state.requery();
                            state.bindInteger(1, contact.user_id);
                            state.bindInteger(2, contact.mutual ? 1 : 0);
                            state.step();
                        }
                        state.dispose();
                        MessagesStorage.this.database.commitTransaction();
                    } catch (Throwable e) {
                        FileLog.m611e("tmessages", e);
                    }
                }
            });
        }
    }

    public void deleteContacts(final ArrayList<Integer> uids) {
        if (uids != null && !uids.isEmpty()) {
            this.storageQueue.postRunnable(new Runnable() {
                public void run() {
                    try {
                        MessagesStorage.this.database.executeFast("DELETE FROM contacts WHERE uid IN(" + TextUtils.join(",", uids) + ")").stepThis().dispose();
                    } catch (Throwable e) {
                        FileLog.m611e("tmessages", e);
                    }
                }
            });
        }
    }

    public void applyPhoneBookUpdates(final String adds, final String deletes) {
        if (adds.length() != 0 || deletes.length() != 0) {
            this.storageQueue.postRunnable(new Runnable() {
                public void run() {
                    try {
                        if (adds.length() != 0) {
                            MessagesStorage.this.database.executeFast(String.format(Locale.US, "UPDATE user_phones_v6 SET deleted = 0 WHERE sphone IN(%s)", new Object[]{adds})).stepThis().dispose();
                        }
                        if (deletes.length() != 0) {
                            MessagesStorage.this.database.executeFast(String.format(Locale.US, "UPDATE user_phones_v6 SET deleted = 1 WHERE sphone IN(%s)", new Object[]{deletes})).stepThis().dispose();
                        }
                    } catch (Throwable e) {
                        FileLog.m611e("tmessages", e);
                    }
                }
            });
        }
    }

    public void putCachedPhoneBook(final HashMap<Integer, Contact> contactHashMap) {
        this.storageQueue.postRunnable(new Runnable() {
            public void run() {
                try {
                    MessagesStorage.this.database.beginTransaction();
                    SQLitePreparedStatement state = MessagesStorage.this.database.executeFast("REPLACE INTO user_contacts_v6 VALUES(?, ?, ?)");
                    SQLitePreparedStatement state2 = MessagesStorage.this.database.executeFast("REPLACE INTO user_phones_v6 VALUES(?, ?, ?, ?)");
                    for (Entry<Integer, Contact> entry : contactHashMap.entrySet()) {
                        Contact contact = (Contact) entry.getValue();
                        if (!(contact.phones.isEmpty() || contact.shortPhones.isEmpty())) {
                            state.requery();
                            state.bindInteger(1, contact.id);
                            state.bindString(2, contact.first_name);
                            state.bindString(3, contact.last_name);
                            state.step();
                            for (int a = 0; a < contact.phones.size(); a++) {
                                state2.requery();
                                state2.bindInteger(1, contact.id);
                                state2.bindString(2, (String) contact.phones.get(a));
                                state2.bindString(3, (String) contact.shortPhones.get(a));
                                state2.bindInteger(4, ((Integer) contact.phoneDeleted.get(a)).intValue());
                                state2.step();
                            }
                        }
                    }
                    state.dispose();
                    state2.dispose();
                    MessagesStorage.this.database.commitTransaction();
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
        });
    }

    public void getCachedPhoneBook() {
        this.storageQueue.postRunnable(new Runnable() {
            public void run() {
                HashMap<Integer, Contact> contactHashMap = new HashMap();
                try {
                    SQLiteCursor cursor = MessagesStorage.this.database.queryFinalized("SELECT us.uid, us.fname, us.sname, up.phone, up.sphone, up.deleted FROM user_contacts_v6 as us LEFT JOIN user_phones_v6 as up ON us.uid = up.uid WHERE 1", new Object[0]);
                    while (cursor.next()) {
                        int uid = cursor.intValue(0);
                        Contact contact = (Contact) contactHashMap.get(Integer.valueOf(uid));
                        if (contact == null) {
                            contact = new Contact();
                            contact.first_name = cursor.stringValue(1);
                            contact.last_name = cursor.stringValue(2);
                            contact.id = uid;
                            contactHashMap.put(Integer.valueOf(uid), contact);
                        }
                        String phone = cursor.stringValue(3);
                        if (phone != null) {
                            contact.phones.add(phone);
                            String sphone = cursor.stringValue(4);
                            if (sphone != null) {
                                if (sphone.length() == 8 && phone.length() != 8) {
                                    sphone = PhoneFormat.stripExceptNumbers(phone);
                                }
                                contact.shortPhones.add(sphone);
                                contact.phoneDeleted.add(Integer.valueOf(cursor.intValue(5)));
                                contact.phoneTypes.add("");
                            }
                        }
                    }
                    cursor.dispose();
                } catch (Throwable e) {
                    contactHashMap.clear();
                    FileLog.m611e("tmessages", e);
                }
                ContactsController.getInstance().performSyncPhoneBook(contactHashMap, true, true, false);
            }
        });
    }

    public void getContacts() {
        this.storageQueue.postRunnable(new Runnable() {
            public void run() {
                ArrayList<TL_contact> contacts = new ArrayList();
                ArrayList<User> users = new ArrayList();
                try {
                    SQLiteCursor cursor = MessagesStorage.this.database.queryFinalized("SELECT * FROM contacts WHERE 1", new Object[0]);
                    StringBuilder uids = new StringBuilder();
                    while (cursor.next()) {
                        boolean z;
                        int user_id = cursor.intValue(0);
                        TL_contact contact = new TL_contact();
                        contact.user_id = user_id;
                        if (cursor.intValue(1) == 1) {
                            z = true;
                        } else {
                            z = false;
                        }
                        contact.mutual = z;
                        if (uids.length() != 0) {
                            uids.append(",");
                        }
                        contacts.add(contact);
                        uids.append(contact.user_id);
                    }
                    cursor.dispose();
                    if (uids.length() != 0) {
                        MessagesStorage.this.getUsersInternal(uids.toString(), users);
                    }
                } catch (Throwable e) {
                    contacts.clear();
                    users.clear();
                    FileLog.m611e("tmessages", e);
                }
                ContactsController.getInstance().processLoadedContacts(contacts, users, 1);
            }
        });
    }

    public void getUnsentMessages(final int count) {
        this.storageQueue.postRunnable(new Runnable() {
            public void run() {
                try {
                    HashMap<Integer, Message> messageHashMap = new HashMap();
                    ArrayList<Message> messages = new ArrayList();
                    ArrayList<User> users = new ArrayList();
                    ArrayList<Chat> chats = new ArrayList();
                    ArrayList<EncryptedChat> encryptedChats = new ArrayList();
                    ArrayList<Integer> usersToLoad = new ArrayList();
                    ArrayList<Integer> chatsToLoad = new ArrayList();
                    ArrayList<Integer> broadcastIds = new ArrayList();
                    ArrayList<Integer> encryptedChatIds = new ArrayList();
                    SQLiteCursor cursor = MessagesStorage.this.database.queryFinalized("SELECT m.read_state, m.data, m.send_state, m.mid, m.date, r.random_id, m.uid, s.seq_in, s.seq_out, m.ttl FROM messages as m LEFT JOIN randoms as r ON r.mid = m.mid LEFT JOIN messages_seq as s ON m.mid = s.mid WHERE m.mid < 0 AND m.send_state = 1 ORDER BY m.mid DESC LIMIT " + count, new Object[0]);
                    while (cursor.next()) {
                        NativeByteBuffer data = new NativeByteBuffer(cursor.byteArrayLength(1));
                        if (!(data == null || cursor.byteBufferValue(1, data) == 0)) {
                            Message message = Message.TLdeserialize(data, data.readInt32(false), false);
                            if (!messageHashMap.containsKey(Integer.valueOf(message.id))) {
                                MessageObject.setUnreadFlags(message, cursor.intValue(0));
                                message.id = cursor.intValue(3);
                                message.date = cursor.intValue(4);
                                if (!cursor.isNull(5)) {
                                    message.random_id = cursor.longValue(5);
                                }
                                message.dialog_id = cursor.longValue(6);
                                message.seq_in = cursor.intValue(7);
                                message.seq_out = cursor.intValue(8);
                                message.ttl = cursor.intValue(9);
                                messages.add(message);
                                messageHashMap.put(Integer.valueOf(message.id), message);
                                int lower_id = (int) message.dialog_id;
                                int high_id = (int) (message.dialog_id >> 32);
                                if (lower_id != 0) {
                                    if (high_id == 1) {
                                        if (!broadcastIds.contains(Integer.valueOf(lower_id))) {
                                            broadcastIds.add(Integer.valueOf(lower_id));
                                        }
                                    } else if (lower_id < 0) {
                                        if (!chatsToLoad.contains(Integer.valueOf(-lower_id))) {
                                            chatsToLoad.add(Integer.valueOf(-lower_id));
                                        }
                                    } else if (!usersToLoad.contains(Integer.valueOf(lower_id))) {
                                        usersToLoad.add(Integer.valueOf(lower_id));
                                    }
                                } else if (!encryptedChatIds.contains(Integer.valueOf(high_id))) {
                                    encryptedChatIds.add(Integer.valueOf(high_id));
                                }
                                MessagesStorage.addUsersAndChatsFromMessage(message, usersToLoad, chatsToLoad);
                                message.send_state = cursor.intValue(2);
                                if (!(message.to_id.channel_id != 0 || MessageObject.isUnread(message) || lower_id == 0) || message.id > 0) {
                                    message.send_state = 0;
                                }
                                if (lower_id == 0 && !cursor.isNull(5)) {
                                    message.random_id = cursor.longValue(5);
                                }
                            }
                        }
                        data.reuse();
                    }
                    cursor.dispose();
                    if (!encryptedChatIds.isEmpty()) {
                        MessagesStorage.this.getEncryptedChatsInternal(TextUtils.join(",", encryptedChatIds), encryptedChats, usersToLoad);
                    }
                    if (!usersToLoad.isEmpty()) {
                        MessagesStorage.this.getUsersInternal(TextUtils.join(",", usersToLoad), users);
                    }
                    if (!(chatsToLoad.isEmpty() && broadcastIds.isEmpty())) {
                        int a;
                        Integer cid;
                        StringBuilder stringToLoad = new StringBuilder();
                        for (a = 0; a < chatsToLoad.size(); a++) {
                            cid = (Integer) chatsToLoad.get(a);
                            if (stringToLoad.length() != 0) {
                                stringToLoad.append(",");
                            }
                            stringToLoad.append(cid);
                        }
                        for (a = 0; a < broadcastIds.size(); a++) {
                            cid = (Integer) broadcastIds.get(a);
                            if (stringToLoad.length() != 0) {
                                stringToLoad.append(",");
                            }
                            stringToLoad.append(-cid.intValue());
                        }
                        MessagesStorage.this.getChatsInternal(stringToLoad.toString(), chats);
                    }
                    SendMessagesHelper.getInstance().processUnsentMessages(messages, users, chats, encryptedChats);
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
        });
    }

    public void getMessages(long dialog_id, int count, int max_id, int minDate, int classGuid, int load_type, int important, int loadIndex) {
        final int i = count;
        final int i2 = max_id;
        final int i3 = important;
        final long j = dialog_id;
        final int i4 = load_type;
        final int i5 = minDate;
        final int i6 = classGuid;
        final int i7 = loadIndex;
        this.storageQueue.postRunnable(new Runnable() {

            class C05151 implements Comparator<Message> {
                C05151() {
                }

                public int compare(Message lhs, Message rhs) {
                    if (lhs.id <= 0 || rhs.id <= 0) {
                        if (lhs.id >= 0 || rhs.id >= 0) {
                            if (lhs.date > rhs.date) {
                                return -1;
                            }
                            if (lhs.date < rhs.date) {
                                return 1;
                            }
                        } else if (lhs.id < rhs.id) {
                            return -1;
                        } else {
                            if (lhs.id > rhs.id) {
                                return 1;
                            }
                        }
                    } else if (lhs.id > rhs.id) {
                        return -1;
                    } else {
                        if (lhs.id < rhs.id) {
                            return 1;
                        }
                    }
                    return 0;
                }
            }

            /* JADX WARNING: inconsistent code. */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void run() {
                /*
                r59 = this;
                r5 = new org.telegram.tgnet.TLRPC$TL_messages_messages;
                r5.<init>();
                r14 = 0;
                r0 = r59;
                r8 = r3;
                r52 = 0;
                r12 = 0;
                r13 = 0;
                r20 = 0;
                r15 = 0;
                r0 = r59;
                r4 = r4;
                r0 = (long) r4;
                r48 = r0;
                r0 = r59;
                r0 = r4;
                r43 = r0;
                r22 = 0;
                r0 = r59;
                r4 = r5;
                if (r4 == 0) goto L_0x002e;
            L_0x0026:
                r0 = r59;
                r6 = r6;
                r4 = (int) r6;
                r0 = -r4;
                r22 = r0;
            L_0x002e:
                r6 = 0;
                r4 = (r48 > r6 ? 1 : (r48 == r6 ? 0 : -1));
                if (r4 == 0) goto L_0x003e;
            L_0x0034:
                if (r22 == 0) goto L_0x003e;
            L_0x0036:
                r0 = r22;
                r6 = (long) r0;
                r4 = 32;
                r6 = r6 << r4;
                r48 = r48 | r6;
            L_0x003e:
                r18 = 0;
                r57 = new java.util.ArrayList;	 Catch:{ Exception -> 0x05ce }
                r57.<init>();	 Catch:{ Exception -> 0x05ce }
                r23 = new java.util.ArrayList;	 Catch:{ Exception -> 0x05ce }
                r23.<init>();	 Catch:{ Exception -> 0x05ce }
                r55 = new java.util.ArrayList;	 Catch:{ Exception -> 0x05ce }
                r55.<init>();	 Catch:{ Exception -> 0x05ce }
                r54 = new java.util.HashMap;	 Catch:{ Exception -> 0x05ce }
                r54.<init>();	 Catch:{ Exception -> 0x05ce }
                r0 = r59;
                r6 = r6;	 Catch:{ Exception -> 0x05ce }
                r0 = (int) r6;	 Catch:{ Exception -> 0x05ce }
                r40 = r0;
                if (r40 == 0) goto L_0x0b64;
            L_0x005d:
                r0 = r59;
                r4 = r5;	 Catch:{ Exception -> 0x05ce }
                r6 = 2;
                if (r4 != r6) goto L_0x0611;
            L_0x0064:
                r39 = " AND imp = 1 ";
            L_0x0066:
                r0 = r59;
                r4 = r5;	 Catch:{ Exception -> 0x05ce }
                r6 = 2;
                if (r4 != r6) goto L_0x0615;
            L_0x006d:
                r31 = "messages_imp_holes";
            L_0x006f:
                r0 = r59;
                r4 = r8;	 Catch:{ Exception -> 0x05ce }
                r6 = 1;
                if (r4 == r6) goto L_0x01a6;
            L_0x0076:
                r0 = r59;
                r4 = r8;	 Catch:{ Exception -> 0x05ce }
                r6 = 3;
                if (r4 == r6) goto L_0x01a6;
            L_0x007d:
                r0 = r59;
                r4 = r9;	 Catch:{ Exception -> 0x05ce }
                if (r4 != 0) goto L_0x01a6;
            L_0x0083:
                r0 = r59;
                r4 = r8;	 Catch:{ Exception -> 0x05ce }
                r6 = 2;
                if (r4 != r6) goto L_0x0191;
            L_0x008a:
                r0 = r59;
                r4 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x05ce }
                r4 = r4.database;	 Catch:{ Exception -> 0x05ce }
                r6 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x05ce }
                r6.<init>();	 Catch:{ Exception -> 0x05ce }
                r7 = "SELECT inbox_max, unread_count, date FROM dialogs WHERE did = ";
                r6 = r6.append(r7);	 Catch:{ Exception -> 0x05ce }
                r0 = r59;
                r10 = r6;	 Catch:{ Exception -> 0x05ce }
                r6 = r6.append(r10);	 Catch:{ Exception -> 0x05ce }
                r6 = r6.toString();	 Catch:{ Exception -> 0x05ce }
                r7 = 0;
                r7 = new java.lang.Object[r7];	 Catch:{ Exception -> 0x05ce }
                r25 = r4.queryFinalized(r6, r7);	 Catch:{ Exception -> 0x05ce }
                r4 = r25.next();	 Catch:{ Exception -> 0x05ce }
                if (r4 == 0) goto L_0x00e2;
            L_0x00b6:
                r4 = 0;
                r0 = r25;
                r12 = r0.intValue(r4);	 Catch:{ Exception -> 0x05ce }
                r43 = r12;
                r0 = (long) r12;	 Catch:{ Exception -> 0x05ce }
                r48 = r0;
                r4 = 1;
                r0 = r25;
                r14 = r0.intValue(r4);	 Catch:{ Exception -> 0x05ce }
                r4 = 2;
                r0 = r25;
                r15 = r0.intValue(r4);	 Catch:{ Exception -> 0x05ce }
                r20 = 1;
                r6 = 0;
                r4 = (r48 > r6 ? 1 : (r48 == r6 ? 0 : -1));
                if (r4 == 0) goto L_0x00e2;
            L_0x00d8:
                if (r22 == 0) goto L_0x00e2;
            L_0x00da:
                r0 = r22;
                r6 = (long) r0;	 Catch:{ Exception -> 0x05ce }
                r4 = 32;
                r6 = r6 << r4;
                r48 = r48 | r6;
            L_0x00e2:
                r25.dispose();	 Catch:{ Exception -> 0x05ce }
                if (r20 != 0) goto L_0x0191;
            L_0x00e7:
                r0 = r59;
                r4 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x05ce }
                r4 = r4.database;	 Catch:{ Exception -> 0x05ce }
                r6 = java.util.Locale.US;	 Catch:{ Exception -> 0x05ce }
                r7 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x05ce }
                r7.<init>();	 Catch:{ Exception -> 0x05ce }
                r9 = "SELECT min(mid), max(date) FROM messages WHERE uid = %d AND out = 0 AND read_state IN(0,2) AND mid > 0";
                r7 = r7.append(r9);	 Catch:{ Exception -> 0x05ce }
                r0 = r39;
                r7 = r7.append(r0);	 Catch:{ Exception -> 0x05ce }
                r7 = r7.toString();	 Catch:{ Exception -> 0x05ce }
                r9 = 1;
                r9 = new java.lang.Object[r9];	 Catch:{ Exception -> 0x05ce }
                r10 = 0;
                r0 = r59;
                r0 = r6;	 Catch:{ Exception -> 0x05ce }
                r16 = r0;
                r11 = java.lang.Long.valueOf(r16);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r6 = java.lang.String.format(r6, r7, r9);	 Catch:{ Exception -> 0x05ce }
                r7 = 0;
                r7 = new java.lang.Object[r7];	 Catch:{ Exception -> 0x05ce }
                r25 = r4.queryFinalized(r6, r7);	 Catch:{ Exception -> 0x05ce }
                r4 = r25.next();	 Catch:{ Exception -> 0x05ce }
                if (r4 == 0) goto L_0x0135;
            L_0x0127:
                r4 = 0;
                r0 = r25;
                r12 = r0.intValue(r4);	 Catch:{ Exception -> 0x05ce }
                r4 = 1;
                r0 = r25;
                r15 = r0.intValue(r4);	 Catch:{ Exception -> 0x05ce }
            L_0x0135:
                r25.dispose();	 Catch:{ Exception -> 0x05ce }
                if (r12 == 0) goto L_0x0191;
            L_0x013a:
                r0 = r59;
                r4 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x05ce }
                r4 = r4.database;	 Catch:{ Exception -> 0x05ce }
                r6 = java.util.Locale.US;	 Catch:{ Exception -> 0x05ce }
                r7 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x05ce }
                r7.<init>();	 Catch:{ Exception -> 0x05ce }
                r9 = "SELECT COUNT(*) FROM messages WHERE uid = %d AND mid >= %d ";
                r7 = r7.append(r9);	 Catch:{ Exception -> 0x05ce }
                r0 = r39;
                r7 = r7.append(r0);	 Catch:{ Exception -> 0x05ce }
                r9 = "AND out = 0 AND read_state IN(0,2)";
                r7 = r7.append(r9);	 Catch:{ Exception -> 0x05ce }
                r7 = r7.toString();	 Catch:{ Exception -> 0x05ce }
                r9 = 2;
                r9 = new java.lang.Object[r9];	 Catch:{ Exception -> 0x05ce }
                r10 = 0;
                r0 = r59;
                r0 = r6;	 Catch:{ Exception -> 0x05ce }
                r16 = r0;
                r11 = java.lang.Long.valueOf(r16);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r10 = 1;
                r11 = java.lang.Integer.valueOf(r12);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r6 = java.lang.String.format(r6, r7, r9);	 Catch:{ Exception -> 0x05ce }
                r7 = 0;
                r7 = new java.lang.Object[r7];	 Catch:{ Exception -> 0x05ce }
                r25 = r4.queryFinalized(r6, r7);	 Catch:{ Exception -> 0x05ce }
                r4 = r25.next();	 Catch:{ Exception -> 0x05ce }
                if (r4 == 0) goto L_0x018e;
            L_0x0187:
                r4 = 0;
                r0 = r25;
                r14 = r0.intValue(r4);	 Catch:{ Exception -> 0x05ce }
            L_0x018e:
                r25.dispose();	 Catch:{ Exception -> 0x05ce }
            L_0x0191:
                if (r8 > r14) goto L_0x0196;
            L_0x0193:
                r4 = 4;
                if (r14 >= r4) goto L_0x0619;
            L_0x0196:
                r4 = r14 + 10;
                r8 = java.lang.Math.max(r8, r4);	 Catch:{ Exception -> 0x05ce }
                r4 = 4;
                if (r14 >= r4) goto L_0x01a6;
            L_0x019f:
                r14 = 0;
                r12 = 0;
                r48 = 0;
                r13 = 0;
                r20 = 0;
            L_0x01a6:
                r0 = r59;
                r4 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x05ce }
                r4 = r4.database;	 Catch:{ Exception -> 0x05ce }
                r6 = java.util.Locale.US;	 Catch:{ Exception -> 0x05ce }
                r7 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x05ce }
                r7.<init>();	 Catch:{ Exception -> 0x05ce }
                r9 = "SELECT start FROM ";
                r7 = r7.append(r9);	 Catch:{ Exception -> 0x05ce }
                r0 = r31;
                r7 = r7.append(r0);	 Catch:{ Exception -> 0x05ce }
                r9 = " WHERE uid = %d AND start IN (0, 1)";
                r7 = r7.append(r9);	 Catch:{ Exception -> 0x05ce }
                r7 = r7.toString();	 Catch:{ Exception -> 0x05ce }
                r9 = 1;
                r9 = new java.lang.Object[r9];	 Catch:{ Exception -> 0x05ce }
                r10 = 0;
                r0 = r59;
                r0 = r6;	 Catch:{ Exception -> 0x05ce }
                r16 = r0;
                r11 = java.lang.Long.valueOf(r16);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r6 = java.lang.String.format(r6, r7, r9);	 Catch:{ Exception -> 0x05ce }
                r7 = 0;
                r7 = new java.lang.Object[r7];	 Catch:{ Exception -> 0x05ce }
                r25 = r4.queryFinalized(r6, r7);	 Catch:{ Exception -> 0x05ce }
                r4 = r25.next();	 Catch:{ Exception -> 0x05ce }
                if (r4 == 0) goto L_0x0623;
            L_0x01ec:
                r4 = 0;
                r0 = r25;
                r4 = r0.intValue(r4);	 Catch:{ Exception -> 0x05ce }
                r6 = 1;
                if (r4 != r6) goto L_0x061f;
            L_0x01f6:
                r18 = 1;
            L_0x01f8:
                r25.dispose();	 Catch:{ Exception -> 0x05ce }
            L_0x01fb:
                r0 = r59;
                r4 = r8;	 Catch:{ Exception -> 0x05ce }
                r6 = 3;
                if (r4 == r6) goto L_0x020b;
            L_0x0202:
                if (r20 == 0) goto L_0x0759;
            L_0x0204:
                r0 = r59;
                r4 = r8;	 Catch:{ Exception -> 0x05ce }
                r6 = 2;
                if (r4 != r6) goto L_0x0759;
            L_0x020b:
                r0 = r59;
                r4 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x05ce }
                r4 = r4.database;	 Catch:{ Exception -> 0x05ce }
                r6 = java.util.Locale.US;	 Catch:{ Exception -> 0x05ce }
                r7 = "SELECT max(mid) FROM messages WHERE uid = %d AND mid > 0";
                r9 = 1;
                r9 = new java.lang.Object[r9];	 Catch:{ Exception -> 0x05ce }
                r10 = 0;
                r0 = r59;
                r0 = r6;	 Catch:{ Exception -> 0x05ce }
                r16 = r0;
                r11 = java.lang.Long.valueOf(r16);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r6 = java.lang.String.format(r6, r7, r9);	 Catch:{ Exception -> 0x05ce }
                r7 = 0;
                r7 = new java.lang.Object[r7];	 Catch:{ Exception -> 0x05ce }
                r25 = r4.queryFinalized(r6, r7);	 Catch:{ Exception -> 0x05ce }
                r4 = r25.next();	 Catch:{ Exception -> 0x05ce }
                if (r4 == 0) goto L_0x023f;
            L_0x0238:
                r4 = 0;
                r0 = r25;
                r13 = r0.intValue(r4);	 Catch:{ Exception -> 0x05ce }
            L_0x023f:
                r25.dispose();	 Catch:{ Exception -> 0x05ce }
                r24 = 1;
                r0 = r59;
                r4 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x05ce }
                r4 = r4.database;	 Catch:{ Exception -> 0x05ce }
                r6 = java.util.Locale.US;	 Catch:{ Exception -> 0x05ce }
                r7 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x05ce }
                r7.<init>();	 Catch:{ Exception -> 0x05ce }
                r9 = "SELECT start FROM ";
                r7 = r7.append(r9);	 Catch:{ Exception -> 0x05ce }
                r0 = r31;
                r7 = r7.append(r0);	 Catch:{ Exception -> 0x05ce }
                r9 = " WHERE uid = %d AND start < %d AND end > %d";
                r7 = r7.append(r9);	 Catch:{ Exception -> 0x05ce }
                r7 = r7.toString();	 Catch:{ Exception -> 0x05ce }
                r9 = 3;
                r9 = new java.lang.Object[r9];	 Catch:{ Exception -> 0x05ce }
                r10 = 0;
                r0 = r59;
                r0 = r6;	 Catch:{ Exception -> 0x05ce }
                r16 = r0;
                r11 = java.lang.Long.valueOf(r16);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r10 = 1;
                r11 = java.lang.Integer.valueOf(r43);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r10 = 2;
                r11 = java.lang.Integer.valueOf(r43);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r6 = java.lang.String.format(r6, r7, r9);	 Catch:{ Exception -> 0x05ce }
                r7 = 0;
                r7 = new java.lang.Object[r7];	 Catch:{ Exception -> 0x05ce }
                r25 = r4.queryFinalized(r6, r7);	 Catch:{ Exception -> 0x05ce }
                r4 = r25.next();	 Catch:{ Exception -> 0x05ce }
                if (r4 == 0) goto L_0x029a;
            L_0x0298:
                r24 = 0;
            L_0x029a:
                r25.dispose();	 Catch:{ Exception -> 0x05ce }
                if (r24 == 0) goto L_0x0755;
            L_0x029f:
                r34 = 0;
                r36 = 1;
                r0 = r59;
                r4 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x05ce }
                r4 = r4.database;	 Catch:{ Exception -> 0x05ce }
                r6 = java.util.Locale.US;	 Catch:{ Exception -> 0x05ce }
                r7 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x05ce }
                r7.<init>();	 Catch:{ Exception -> 0x05ce }
                r9 = "SELECT start FROM ";
                r7 = r7.append(r9);	 Catch:{ Exception -> 0x05ce }
                r0 = r31;
                r7 = r7.append(r0);	 Catch:{ Exception -> 0x05ce }
                r9 = " WHERE uid = %d AND start >= %d ORDER BY start ASC LIMIT 1";
                r7 = r7.append(r9);	 Catch:{ Exception -> 0x05ce }
                r7 = r7.toString();	 Catch:{ Exception -> 0x05ce }
                r9 = 2;
                r9 = new java.lang.Object[r9];	 Catch:{ Exception -> 0x05ce }
                r10 = 0;
                r0 = r59;
                r0 = r6;	 Catch:{ Exception -> 0x05ce }
                r16 = r0;
                r11 = java.lang.Long.valueOf(r16);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r10 = 1;
                r11 = java.lang.Integer.valueOf(r43);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r6 = java.lang.String.format(r6, r7, r9);	 Catch:{ Exception -> 0x05ce }
                r7 = 0;
                r7 = new java.lang.Object[r7];	 Catch:{ Exception -> 0x05ce }
                r25 = r4.queryFinalized(r6, r7);	 Catch:{ Exception -> 0x05ce }
                r4 = r25.next();	 Catch:{ Exception -> 0x05ce }
                if (r4 == 0) goto L_0x0304;
            L_0x02f0:
                r4 = 0;
                r0 = r25;
                r4 = r0.intValue(r4);	 Catch:{ Exception -> 0x05ce }
                r0 = (long) r4;	 Catch:{ Exception -> 0x05ce }
                r34 = r0;
                if (r22 == 0) goto L_0x0304;
            L_0x02fc:
                r0 = r22;
                r6 = (long) r0;	 Catch:{ Exception -> 0x05ce }
                r4 = 32;
                r6 = r6 << r4;
                r34 = r34 | r6;
            L_0x0304:
                r25.dispose();	 Catch:{ Exception -> 0x05ce }
                r0 = r59;
                r4 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x05ce }
                r4 = r4.database;	 Catch:{ Exception -> 0x05ce }
                r6 = java.util.Locale.US;	 Catch:{ Exception -> 0x05ce }
                r7 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x05ce }
                r7.<init>();	 Catch:{ Exception -> 0x05ce }
                r9 = "SELECT end FROM ";
                r7 = r7.append(r9);	 Catch:{ Exception -> 0x05ce }
                r0 = r31;
                r7 = r7.append(r0);	 Catch:{ Exception -> 0x05ce }
                r9 = " WHERE uid = %d AND end <= %d ORDER BY end DESC LIMIT 1";
                r7 = r7.append(r9);	 Catch:{ Exception -> 0x05ce }
                r7 = r7.toString();	 Catch:{ Exception -> 0x05ce }
                r9 = 2;
                r9 = new java.lang.Object[r9];	 Catch:{ Exception -> 0x05ce }
                r10 = 0;
                r0 = r59;
                r0 = r6;	 Catch:{ Exception -> 0x05ce }
                r16 = r0;
                r11 = java.lang.Long.valueOf(r16);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r10 = 1;
                r11 = java.lang.Integer.valueOf(r43);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r6 = java.lang.String.format(r6, r7, r9);	 Catch:{ Exception -> 0x05ce }
                r7 = 0;
                r7 = new java.lang.Object[r7];	 Catch:{ Exception -> 0x05ce }
                r25 = r4.queryFinalized(r6, r7);	 Catch:{ Exception -> 0x05ce }
                r4 = r25.next();	 Catch:{ Exception -> 0x05ce }
                if (r4 == 0) goto L_0x0368;
            L_0x0354:
                r4 = 0;
                r0 = r25;
                r4 = r0.intValue(r4);	 Catch:{ Exception -> 0x05ce }
                r0 = (long) r4;	 Catch:{ Exception -> 0x05ce }
                r36 = r0;
                if (r22 == 0) goto L_0x0368;
            L_0x0360:
                r0 = r22;
                r6 = (long) r0;	 Catch:{ Exception -> 0x05ce }
                r4 = 32;
                r6 = r6 << r4;
                r36 = r36 | r6;
            L_0x0368:
                r25.dispose();	 Catch:{ Exception -> 0x05ce }
                r6 = 0;
                r4 = (r34 > r6 ? 1 : (r34 == r6 ? 0 : -1));
                if (r4 != 0) goto L_0x0377;
            L_0x0371:
                r6 = 1;
                r4 = (r36 > r6 ? 1 : (r36 == r6 ? 0 : -1));
                if (r4 == 0) goto L_0x06d4;
            L_0x0377:
                r6 = 0;
                r4 = (r34 > r6 ? 1 : (r34 == r6 ? 0 : -1));
                if (r4 != 0) goto L_0x038a;
            L_0x037d:
                r34 = 1000000000; // 0x3b9aca00 float:0.0047237873 double:4.94065646E-315;
                if (r22 == 0) goto L_0x038a;
            L_0x0382:
                r0 = r22;
                r6 = (long) r0;	 Catch:{ Exception -> 0x05ce }
                r4 = 32;
                r6 = r6 << r4;
                r34 = r34 | r6;
            L_0x038a:
                r0 = r59;
                r4 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x05ce }
                r4 = r4.database;	 Catch:{ Exception -> 0x05ce }
                r6 = java.util.Locale.US;	 Catch:{ Exception -> 0x05ce }
                r7 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x05ce }
                r7.<init>();	 Catch:{ Exception -> 0x05ce }
                r9 = "SELECT * FROM (SELECT m.read_state, m.data, m.send_state, m.mid, m.date, r.random_id, m.replydata, m.media FROM messages as m LEFT JOIN randoms as r ON r.mid = m.mid WHERE m.uid = %d AND m.mid <= %d AND m.mid >= %d ";
                r7 = r7.append(r9);	 Catch:{ Exception -> 0x05ce }
                r0 = r39;
                r7 = r7.append(r0);	 Catch:{ Exception -> 0x05ce }
                r9 = "ORDER BY m.date DESC, m.mid DESC LIMIT %d) UNION ";
                r7 = r7.append(r9);	 Catch:{ Exception -> 0x05ce }
                r9 = "SELECT * FROM (SELECT m.read_state, m.data, m.send_state, m.mid, m.date, r.random_id, m.replydata, m.media FROM messages as m LEFT JOIN randoms as r ON r.mid = m.mid WHERE m.uid = %d AND m.mid > %d AND m.mid <= %d ";
                r7 = r7.append(r9);	 Catch:{ Exception -> 0x05ce }
                r0 = r39;
                r7 = r7.append(r0);	 Catch:{ Exception -> 0x05ce }
                r9 = "ORDER BY m.date ASC, m.mid ASC LIMIT %d)";
                r7 = r7.append(r9);	 Catch:{ Exception -> 0x05ce }
                r7 = r7.toString();	 Catch:{ Exception -> 0x05ce }
                r9 = 8;
                r9 = new java.lang.Object[r9];	 Catch:{ Exception -> 0x05ce }
                r10 = 0;
                r0 = r59;
                r0 = r6;	 Catch:{ Exception -> 0x05ce }
                r16 = r0;
                r11 = java.lang.Long.valueOf(r16);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r10 = 1;
                r11 = java.lang.Long.valueOf(r48);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r10 = 2;
                r11 = java.lang.Long.valueOf(r36);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r10 = 3;
                r11 = r8 / 2;
                r11 = java.lang.Integer.valueOf(r11);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r10 = 4;
                r0 = r59;
                r0 = r6;	 Catch:{ Exception -> 0x05ce }
                r16 = r0;
                r11 = java.lang.Long.valueOf(r16);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r10 = 5;
                r11 = java.lang.Long.valueOf(r48);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r10 = 6;
                r11 = java.lang.Long.valueOf(r34);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r10 = 7;
                r11 = r8 / 2;
                r11 = java.lang.Integer.valueOf(r11);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r6 = java.lang.String.format(r6, r7, r9);	 Catch:{ Exception -> 0x05ce }
                r7 = 0;
                r7 = new java.lang.Object[r7];	 Catch:{ Exception -> 0x05ce }
                r25 = r4.queryFinalized(r6, r7);	 Catch:{ Exception -> 0x05ce }
            L_0x0418:
                if (r25 == 0) goto L_0x0d47;
            L_0x041a:
                r4 = r25.next();	 Catch:{ Exception -> 0x05ce }
                if (r4 == 0) goto L_0x0d44;
            L_0x0420:
                r27 = new org.telegram.tgnet.NativeByteBuffer;	 Catch:{ Exception -> 0x05ce }
                r4 = 1;
                r0 = r25;
                r4 = r0.byteArrayLength(r4);	 Catch:{ Exception -> 0x05ce }
                r0 = r27;
                r0.<init>(r4);	 Catch:{ Exception -> 0x05ce }
                if (r27 == 0) goto L_0x05c9;
            L_0x0430:
                r4 = 1;
                r0 = r25;
                r1 = r27;
                r4 = r0.byteBufferValue(r4, r1);	 Catch:{ Exception -> 0x05ce }
                if (r4 == 0) goto L_0x05c9;
            L_0x043b:
                r4 = 0;
                r0 = r27;
                r4 = r0.readInt32(r4);	 Catch:{ Exception -> 0x05ce }
                r6 = 0;
                r0 = r27;
                r44 = org.telegram.tgnet.TLRPC.Message.TLdeserialize(r0, r4, r6);	 Catch:{ Exception -> 0x05ce }
                r4 = 0;
                r0 = r25;
                r4 = r0.intValue(r4);	 Catch:{ Exception -> 0x05ce }
                r0 = r44;
                org.telegram.messenger.MessageObject.setUnreadFlags(r0, r4);	 Catch:{ Exception -> 0x05ce }
                r4 = 3;
                r0 = r25;
                r4 = r0.intValue(r4);	 Catch:{ Exception -> 0x05ce }
                r0 = r44;
                r0.id = r4;	 Catch:{ Exception -> 0x05ce }
                r4 = 4;
                r0 = r25;
                r4 = r0.intValue(r4);	 Catch:{ Exception -> 0x05ce }
                r0 = r44;
                r0.date = r4;	 Catch:{ Exception -> 0x05ce }
                r0 = r59;
                r6 = r6;	 Catch:{ Exception -> 0x05ce }
                r0 = r44;
                r0.dialog_id = r6;	 Catch:{ Exception -> 0x05ce }
                r0 = r44;
                r4 = r0.flags;	 Catch:{ Exception -> 0x05ce }
                r4 = r4 & 1024;
                if (r4 == 0) goto L_0x0486;
            L_0x047b:
                r4 = 7;
                r0 = r25;
                r4 = r0.intValue(r4);	 Catch:{ Exception -> 0x05ce }
                r0 = r44;
                r0.views = r4;	 Catch:{ Exception -> 0x05ce }
            L_0x0486:
                r4 = r5.messages;	 Catch:{ Exception -> 0x05ce }
                r0 = r44;
                r4.add(r0);	 Catch:{ Exception -> 0x05ce }
                r0 = r44;
                r1 = r57;
                r2 = r23;
                org.telegram.messenger.MessagesStorage.addUsersAndChatsFromMessage(r0, r1, r2);	 Catch:{ Exception -> 0x05ce }
                r0 = r44;
                r4 = r0.reply_to_msg_id;	 Catch:{ Exception -> 0x05ce }
                if (r4 == 0) goto L_0x0549;
            L_0x049c:
                r53 = 0;
                r4 = 6;
                r0 = r25;
                r4 = r0.isNull(r4);	 Catch:{ Exception -> 0x05ce }
                if (r4 != 0) goto L_0x04ea;
            L_0x04a7:
                r28 = new org.telegram.tgnet.NativeByteBuffer;	 Catch:{ Exception -> 0x05ce }
                r4 = 6;
                r0 = r25;
                r4 = r0.byteArrayLength(r4);	 Catch:{ Exception -> 0x05ce }
                r0 = r28;
                r0.<init>(r4);	 Catch:{ Exception -> 0x05ce }
                if (r28 == 0) goto L_0x04e7;
            L_0x04b7:
                r4 = 6;
                r0 = r25;
                r1 = r28;
                r4 = r0.byteBufferValue(r4, r1);	 Catch:{ Exception -> 0x05ce }
                if (r4 == 0) goto L_0x04e7;
            L_0x04c2:
                r4 = 0;
                r0 = r28;
                r4 = r0.readInt32(r4);	 Catch:{ Exception -> 0x05ce }
                r6 = 0;
                r0 = r28;
                r4 = org.telegram.tgnet.TLRPC.Message.TLdeserialize(r0, r4, r6);	 Catch:{ Exception -> 0x05ce }
                r0 = r44;
                r0.replyMessage = r4;	 Catch:{ Exception -> 0x05ce }
                r0 = r44;
                r4 = r0.replyMessage;	 Catch:{ Exception -> 0x05ce }
                if (r4 == 0) goto L_0x04e7;
            L_0x04da:
                r0 = r44;
                r4 = r0.replyMessage;	 Catch:{ Exception -> 0x05ce }
                r0 = r57;
                r1 = r23;
                org.telegram.messenger.MessagesStorage.addUsersAndChatsFromMessage(r4, r0, r1);	 Catch:{ Exception -> 0x05ce }
                r53 = 1;
            L_0x04e7:
                r28.reuse();	 Catch:{ Exception -> 0x05ce }
            L_0x04ea:
                if (r53 != 0) goto L_0x0549;
            L_0x04ec:
                r0 = r44;
                r4 = r0.reply_to_msg_id;	 Catch:{ Exception -> 0x05ce }
                r0 = (long) r4;	 Catch:{ Exception -> 0x05ce }
                r46 = r0;
                r0 = r44;
                r4 = r0.to_id;	 Catch:{ Exception -> 0x05ce }
                r4 = r4.channel_id;	 Catch:{ Exception -> 0x05ce }
                if (r4 == 0) goto L_0x0507;
            L_0x04fb:
                r0 = r44;
                r4 = r0.to_id;	 Catch:{ Exception -> 0x05ce }
                r4 = r4.channel_id;	 Catch:{ Exception -> 0x05ce }
                r6 = (long) r4;	 Catch:{ Exception -> 0x05ce }
                r4 = 32;
                r6 = r6 << r4;
                r46 = r46 | r6;
            L_0x0507:
                r4 = java.lang.Long.valueOf(r46);	 Catch:{ Exception -> 0x05ce }
                r0 = r55;
                r4 = r0.contains(r4);	 Catch:{ Exception -> 0x05ce }
                if (r4 != 0) goto L_0x051c;
            L_0x0513:
                r4 = java.lang.Long.valueOf(r46);	 Catch:{ Exception -> 0x05ce }
                r0 = r55;
                r0.add(r4);	 Catch:{ Exception -> 0x05ce }
            L_0x051c:
                r0 = r44;
                r4 = r0.reply_to_msg_id;	 Catch:{ Exception -> 0x05ce }
                r4 = java.lang.Integer.valueOf(r4);	 Catch:{ Exception -> 0x05ce }
                r0 = r54;
                r45 = r0.get(r4);	 Catch:{ Exception -> 0x05ce }
                r45 = (java.util.ArrayList) r45;	 Catch:{ Exception -> 0x05ce }
                if (r45 != 0) goto L_0x0542;
            L_0x052e:
                r45 = new java.util.ArrayList;	 Catch:{ Exception -> 0x05ce }
                r45.<init>();	 Catch:{ Exception -> 0x05ce }
                r0 = r44;
                r4 = r0.reply_to_msg_id;	 Catch:{ Exception -> 0x05ce }
                r4 = java.lang.Integer.valueOf(r4);	 Catch:{ Exception -> 0x05ce }
                r0 = r54;
                r1 = r45;
                r0.put(r4, r1);	 Catch:{ Exception -> 0x05ce }
            L_0x0542:
                r0 = r45;
                r1 = r44;
                r0.add(r1);	 Catch:{ Exception -> 0x05ce }
            L_0x0549:
                r4 = 2;
                r0 = r25;
                r4 = r0.intValue(r4);	 Catch:{ Exception -> 0x05ce }
                r0 = r44;
                r0.send_state = r4;	 Catch:{ Exception -> 0x05ce }
                r0 = r44;
                r4 = r0.id;	 Catch:{ Exception -> 0x05ce }
                if (r4 <= 0) goto L_0x0565;
            L_0x055a:
                r0 = r44;
                r4 = r0.send_state;	 Catch:{ Exception -> 0x05ce }
                if (r4 == 0) goto L_0x0565;
            L_0x0560:
                r4 = 0;
                r0 = r44;
                r0.send_state = r4;	 Catch:{ Exception -> 0x05ce }
            L_0x0565:
                if (r40 != 0) goto L_0x057b;
            L_0x0567:
                r4 = 5;
                r0 = r25;
                r4 = r0.isNull(r4);	 Catch:{ Exception -> 0x05ce }
                if (r4 != 0) goto L_0x057b;
            L_0x0570:
                r4 = 5;
                r0 = r25;
                r6 = r0.longValue(r4);	 Catch:{ Exception -> 0x05ce }
                r0 = r44;
                r0.random_id = r6;	 Catch:{ Exception -> 0x05ce }
            L_0x057b:
                r0 = r59;
                r6 = r6;	 Catch:{ Exception -> 0x05ce }
                r4 = (int) r6;	 Catch:{ Exception -> 0x05ce }
                if (r4 != 0) goto L_0x05c9;
            L_0x0582:
                r0 = r44;
                r4 = r0.media;	 Catch:{ Exception -> 0x05ce }
                if (r4 == 0) goto L_0x05c9;
            L_0x0588:
                r0 = r44;
                r4 = r0.media;	 Catch:{ Exception -> 0x05ce }
                r4 = r4.photo;	 Catch:{ Exception -> 0x05ce }
                if (r4 == 0) goto L_0x05c9;
            L_0x0590:
                r0 = r59;
                r4 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x0d3a }
                r4 = r4.database;	 Catch:{ Exception -> 0x0d3a }
                r6 = java.util.Locale.US;	 Catch:{ Exception -> 0x0d3a }
                r7 = "SELECT date FROM enc_tasks_v2 WHERE mid = %d";
                r9 = 1;
                r9 = new java.lang.Object[r9];	 Catch:{ Exception -> 0x0d3a }
                r10 = 0;
                r0 = r44;
                r11 = r0.id;	 Catch:{ Exception -> 0x0d3a }
                r11 = java.lang.Integer.valueOf(r11);	 Catch:{ Exception -> 0x0d3a }
                r9[r10] = r11;	 Catch:{ Exception -> 0x0d3a }
                r6 = java.lang.String.format(r6, r7, r9);	 Catch:{ Exception -> 0x0d3a }
                r7 = 0;
                r7 = new java.lang.Object[r7];	 Catch:{ Exception -> 0x0d3a }
                r26 = r4.queryFinalized(r6, r7);	 Catch:{ Exception -> 0x0d3a }
                r4 = r26.next();	 Catch:{ Exception -> 0x0d3a }
                if (r4 == 0) goto L_0x05c6;
            L_0x05bb:
                r4 = 0;
                r0 = r26;
                r4 = r0.intValue(r4);	 Catch:{ Exception -> 0x0d3a }
                r0 = r44;
                r0.destroyTime = r4;	 Catch:{ Exception -> 0x0d3a }
            L_0x05c6:
                r26.dispose();	 Catch:{ Exception -> 0x0d3a }
            L_0x05c9:
                r27.reuse();	 Catch:{ Exception -> 0x05ce }
                goto L_0x041a;
            L_0x05ce:
                r29 = move-exception;
                r4 = r5.messages;	 Catch:{ all -> 0x06aa }
                r4.clear();	 Catch:{ all -> 0x06aa }
                r4 = r5.chats;	 Catch:{ all -> 0x06aa }
                r4.clear();	 Catch:{ all -> 0x06aa }
                r4 = r5.users;	 Catch:{ all -> 0x06aa }
                r4.clear();	 Catch:{ all -> 0x06aa }
                r4 = r5.collapsed;	 Catch:{ all -> 0x06aa }
                r4.clear();	 Catch:{ all -> 0x06aa }
                r4 = "tmessages";
                r0 = r29;
                org.telegram.messenger.FileLog.m611e(r4, r0);	 Catch:{ all -> 0x06aa }
                r4 = org.telegram.messenger.MessagesController.getInstance();
                r0 = r59;
                r6 = r6;
                r0 = r59;
                r9 = r4;
                r10 = 1;
                r0 = r59;
                r11 = r10;
                r0 = r59;
                r0 = r8;
                r16 = r0;
                r0 = r59;
                r0 = r5;
                r17 = r0;
                r0 = r59;
                r0 = r11;
                r19 = r0;
                r4.processLoadedMessages(r5, r6, r8, r9, r10, r11, r12, r13, r14, r15, r16, r17, r18, r19, r20);
            L_0x0610:
                return;
            L_0x0611:
                r39 = "";
                goto L_0x0066;
            L_0x0615:
                r31 = "messages_holes";
                goto L_0x006f;
            L_0x0619:
                r52 = r14 - r8;
                r8 = r8 + 10;
                goto L_0x01a6;
            L_0x061f:
                r18 = 0;
                goto L_0x01f8;
            L_0x0623:
                r25.dispose();	 Catch:{ Exception -> 0x05ce }
                r0 = r59;
                r4 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x05ce }
                r4 = r4.database;	 Catch:{ Exception -> 0x05ce }
                r6 = java.util.Locale.US;	 Catch:{ Exception -> 0x05ce }
                r7 = "SELECT min(mid) FROM messages WHERE uid = %d AND mid > 0";
                r9 = 1;
                r9 = new java.lang.Object[r9];	 Catch:{ Exception -> 0x05ce }
                r10 = 0;
                r0 = r59;
                r0 = r6;	 Catch:{ Exception -> 0x05ce }
                r16 = r0;
                r11 = java.lang.Long.valueOf(r16);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r6 = java.lang.String.format(r6, r7, r9);	 Catch:{ Exception -> 0x05ce }
                r7 = 0;
                r7 = new java.lang.Object[r7];	 Catch:{ Exception -> 0x05ce }
                r25 = r4.queryFinalized(r6, r7);	 Catch:{ Exception -> 0x05ce }
                r4 = r25.next();	 Catch:{ Exception -> 0x05ce }
                if (r4 == 0) goto L_0x06a5;
            L_0x0653:
                r4 = 0;
                r0 = r25;
                r50 = r0.intValue(r4);	 Catch:{ Exception -> 0x05ce }
                if (r50 == 0) goto L_0x06a5;
            L_0x065c:
                r0 = r59;
                r4 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x05ce }
                r4 = r4.database;	 Catch:{ Exception -> 0x05ce }
                r6 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x05ce }
                r6.<init>();	 Catch:{ Exception -> 0x05ce }
                r7 = "REPLACE INTO ";
                r6 = r6.append(r7);	 Catch:{ Exception -> 0x05ce }
                r0 = r31;
                r6 = r6.append(r0);	 Catch:{ Exception -> 0x05ce }
                r7 = " VALUES(?, ?, ?)";
                r6 = r6.append(r7);	 Catch:{ Exception -> 0x05ce }
                r6 = r6.toString();	 Catch:{ Exception -> 0x05ce }
                r56 = r4.executeFast(r6);	 Catch:{ Exception -> 0x05ce }
                r56.requery();	 Catch:{ Exception -> 0x05ce }
                r4 = 1;
                r0 = r59;
                r6 = r6;	 Catch:{ Exception -> 0x05ce }
                r0 = r56;
                r0.bindLong(r4, r6);	 Catch:{ Exception -> 0x05ce }
                r4 = 2;
                r6 = 0;
                r0 = r56;
                r0.bindInteger(r4, r6);	 Catch:{ Exception -> 0x05ce }
                r4 = 3;
                r0 = r56;
                r1 = r50;
                r0.bindInteger(r4, r1);	 Catch:{ Exception -> 0x05ce }
                r56.step();	 Catch:{ Exception -> 0x05ce }
                r56.dispose();	 Catch:{ Exception -> 0x05ce }
            L_0x06a5:
                r25.dispose();	 Catch:{ Exception -> 0x05ce }
                goto L_0x01fb;
            L_0x06aa:
                r4 = move-exception;
                r58 = r4;
                r4 = org.telegram.messenger.MessagesController.getInstance();
                r0 = r59;
                r6 = r6;
                r0 = r59;
                r9 = r4;
                r10 = 1;
                r0 = r59;
                r11 = r10;
                r0 = r59;
                r0 = r8;
                r16 = r0;
                r0 = r59;
                r0 = r5;
                r17 = r0;
                r0 = r59;
                r0 = r11;
                r19 = r0;
                r4.processLoadedMessages(r5, r6, r8, r9, r10, r11, r12, r13, r14, r15, r16, r17, r18, r19, r20);
                throw r58;
            L_0x06d4:
                r0 = r59;
                r4 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x05ce }
                r4 = r4.database;	 Catch:{ Exception -> 0x05ce }
                r6 = java.util.Locale.US;	 Catch:{ Exception -> 0x05ce }
                r7 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x05ce }
                r7.<init>();	 Catch:{ Exception -> 0x05ce }
                r9 = "SELECT * FROM (SELECT m.read_state, m.data, m.send_state, m.mid, m.date, r.random_id, m.replydata, m.media FROM messages as m LEFT JOIN randoms as r ON r.mid = m.mid WHERE m.uid = %d AND m.mid <= %d ";
                r7 = r7.append(r9);	 Catch:{ Exception -> 0x05ce }
                r0 = r39;
                r7 = r7.append(r0);	 Catch:{ Exception -> 0x05ce }
                r9 = "ORDER BY m.date DESC, m.mid DESC LIMIT %d) UNION ";
                r7 = r7.append(r9);	 Catch:{ Exception -> 0x05ce }
                r9 = "SELECT * FROM (SELECT m.read_state, m.data, m.send_state, m.mid, m.date, r.random_id, m.replydata, m.media FROM messages as m LEFT JOIN randoms as r ON r.mid = m.mid WHERE m.uid = %d AND m.mid > %d ";
                r7 = r7.append(r9);	 Catch:{ Exception -> 0x05ce }
                r0 = r39;
                r7 = r7.append(r0);	 Catch:{ Exception -> 0x05ce }
                r9 = "ORDER BY m.date ASC, m.mid ASC LIMIT %d)";
                r7 = r7.append(r9);	 Catch:{ Exception -> 0x05ce }
                r7 = r7.toString();	 Catch:{ Exception -> 0x05ce }
                r9 = 6;
                r9 = new java.lang.Object[r9];	 Catch:{ Exception -> 0x05ce }
                r10 = 0;
                r0 = r59;
                r0 = r6;	 Catch:{ Exception -> 0x05ce }
                r16 = r0;
                r11 = java.lang.Long.valueOf(r16);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r10 = 1;
                r11 = java.lang.Long.valueOf(r48);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r10 = 2;
                r11 = r8 / 2;
                r11 = java.lang.Integer.valueOf(r11);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r10 = 3;
                r0 = r59;
                r0 = r6;	 Catch:{ Exception -> 0x05ce }
                r16 = r0;
                r11 = java.lang.Long.valueOf(r16);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r10 = 4;
                r11 = java.lang.Long.valueOf(r48);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r10 = 5;
                r11 = r8 / 2;
                r11 = java.lang.Integer.valueOf(r11);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r6 = java.lang.String.format(r6, r7, r9);	 Catch:{ Exception -> 0x05ce }
                r7 = 0;
                r7 = new java.lang.Object[r7];	 Catch:{ Exception -> 0x05ce }
                r25 = r4.queryFinalized(r6, r7);	 Catch:{ Exception -> 0x05ce }
                goto L_0x0418;
            L_0x0755:
                r25 = 0;
                goto L_0x0418;
            L_0x0759:
                r0 = r59;
                r4 = r8;	 Catch:{ Exception -> 0x05ce }
                r6 = 1;
                if (r4 != r6) goto L_0x088d;
            L_0x0760:
                r32 = 0;
                r0 = r59;
                r4 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x05ce }
                r4 = r4.database;	 Catch:{ Exception -> 0x05ce }
                r6 = java.util.Locale.US;	 Catch:{ Exception -> 0x05ce }
                r7 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x05ce }
                r7.<init>();	 Catch:{ Exception -> 0x05ce }
                r9 = "SELECT start, end FROM ";
                r7 = r7.append(r9);	 Catch:{ Exception -> 0x05ce }
                r0 = r31;
                r7 = r7.append(r0);	 Catch:{ Exception -> 0x05ce }
                r9 = " WHERE uid = %d AND start >= %d AND start != 1 AND end != 1 ORDER BY start ASC LIMIT 1";
                r7 = r7.append(r9);	 Catch:{ Exception -> 0x05ce }
                r7 = r7.toString();	 Catch:{ Exception -> 0x05ce }
                r9 = 2;
                r9 = new java.lang.Object[r9];	 Catch:{ Exception -> 0x05ce }
                r10 = 0;
                r0 = r59;
                r0 = r6;	 Catch:{ Exception -> 0x05ce }
                r16 = r0;
                r11 = java.lang.Long.valueOf(r16);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r10 = 1;
                r0 = r59;
                r11 = r4;	 Catch:{ Exception -> 0x05ce }
                r11 = java.lang.Integer.valueOf(r11);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r6 = java.lang.String.format(r6, r7, r9);	 Catch:{ Exception -> 0x05ce }
                r7 = 0;
                r7 = new java.lang.Object[r7];	 Catch:{ Exception -> 0x05ce }
                r25 = r4.queryFinalized(r6, r7);	 Catch:{ Exception -> 0x05ce }
                r4 = r25.next();	 Catch:{ Exception -> 0x05ce }
                if (r4 == 0) goto L_0x07c7;
            L_0x07b3:
                r4 = 0;
                r0 = r25;
                r4 = r0.intValue(r4);	 Catch:{ Exception -> 0x05ce }
                r0 = (long) r4;	 Catch:{ Exception -> 0x05ce }
                r32 = r0;
                if (r22 == 0) goto L_0x07c7;
            L_0x07bf:
                r0 = r22;
                r6 = (long) r0;	 Catch:{ Exception -> 0x05ce }
                r4 = 32;
                r6 = r6 << r4;
                r32 = r32 | r6;
            L_0x07c7:
                r25.dispose();	 Catch:{ Exception -> 0x05ce }
                r6 = 0;
                r4 = (r32 > r6 ? 1 : (r32 == r6 ? 0 : -1));
                if (r4 == 0) goto L_0x0832;
            L_0x07d0:
                r0 = r59;
                r4 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x05ce }
                r4 = r4.database;	 Catch:{ Exception -> 0x05ce }
                r6 = java.util.Locale.US;	 Catch:{ Exception -> 0x05ce }
                r7 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x05ce }
                r7.<init>();	 Catch:{ Exception -> 0x05ce }
                r9 = "SELECT m.read_state, m.data, m.send_state, m.mid, m.date, r.random_id, m.replydata, m.media FROM messages as m LEFT JOIN randoms as r ON r.mid = m.mid WHERE m.uid = %d AND m.date >= %d AND m.mid > %d AND m.mid <= %d ";
                r7 = r7.append(r9);	 Catch:{ Exception -> 0x05ce }
                r0 = r39;
                r7 = r7.append(r0);	 Catch:{ Exception -> 0x05ce }
                r9 = "ORDER BY m.date ASC, m.mid ASC LIMIT %d";
                r7 = r7.append(r9);	 Catch:{ Exception -> 0x05ce }
                r7 = r7.toString();	 Catch:{ Exception -> 0x05ce }
                r9 = 5;
                r9 = new java.lang.Object[r9];	 Catch:{ Exception -> 0x05ce }
                r10 = 0;
                r0 = r59;
                r0 = r6;	 Catch:{ Exception -> 0x05ce }
                r16 = r0;
                r11 = java.lang.Long.valueOf(r16);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r10 = 1;
                r0 = r59;
                r11 = r9;	 Catch:{ Exception -> 0x05ce }
                r11 = java.lang.Integer.valueOf(r11);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r10 = 2;
                r11 = java.lang.Long.valueOf(r48);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r10 = 3;
                r11 = java.lang.Long.valueOf(r32);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r10 = 4;
                r11 = java.lang.Integer.valueOf(r8);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r6 = java.lang.String.format(r6, r7, r9);	 Catch:{ Exception -> 0x05ce }
                r7 = 0;
                r7 = new java.lang.Object[r7];	 Catch:{ Exception -> 0x05ce }
                r25 = r4.queryFinalized(r6, r7);	 Catch:{ Exception -> 0x05ce }
                goto L_0x0418;
            L_0x0832:
                r0 = r59;
                r4 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x05ce }
                r4 = r4.database;	 Catch:{ Exception -> 0x05ce }
                r6 = java.util.Locale.US;	 Catch:{ Exception -> 0x05ce }
                r7 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x05ce }
                r7.<init>();	 Catch:{ Exception -> 0x05ce }
                r9 = "SELECT m.read_state, m.data, m.send_state, m.mid, m.date, r.random_id, m.replydata, m.media FROM messages as m LEFT JOIN randoms as r ON r.mid = m.mid WHERE m.uid = %d AND m.date >= %d AND m.mid > %d ";
                r7 = r7.append(r9);	 Catch:{ Exception -> 0x05ce }
                r0 = r39;
                r7 = r7.append(r0);	 Catch:{ Exception -> 0x05ce }
                r9 = "ORDER BY m.date ASC, m.mid ASC LIMIT %d";
                r7 = r7.append(r9);	 Catch:{ Exception -> 0x05ce }
                r7 = r7.toString();	 Catch:{ Exception -> 0x05ce }
                r9 = 4;
                r9 = new java.lang.Object[r9];	 Catch:{ Exception -> 0x05ce }
                r10 = 0;
                r0 = r59;
                r0 = r6;	 Catch:{ Exception -> 0x05ce }
                r16 = r0;
                r11 = java.lang.Long.valueOf(r16);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r10 = 1;
                r0 = r59;
                r11 = r9;	 Catch:{ Exception -> 0x05ce }
                r11 = java.lang.Integer.valueOf(r11);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r10 = 2;
                r11 = java.lang.Long.valueOf(r48);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r10 = 3;
                r11 = java.lang.Integer.valueOf(r8);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r6 = java.lang.String.format(r6, r7, r9);	 Catch:{ Exception -> 0x05ce }
                r7 = 0;
                r7 = new java.lang.Object[r7];	 Catch:{ Exception -> 0x05ce }
                r25 = r4.queryFinalized(r6, r7);	 Catch:{ Exception -> 0x05ce }
                goto L_0x0418;
            L_0x088d:
                r0 = r59;
                r4 = r9;	 Catch:{ Exception -> 0x05ce }
                if (r4 == 0) goto L_0x0a21;
            L_0x0893:
                r6 = 0;
                r4 = (r48 > r6 ? 1 : (r48 == r6 ? 0 : -1));
                if (r4 == 0) goto L_0x09c6;
            L_0x0899:
                r32 = 0;
                r0 = r59;
                r4 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x05ce }
                r4 = r4.database;	 Catch:{ Exception -> 0x05ce }
                r6 = java.util.Locale.US;	 Catch:{ Exception -> 0x05ce }
                r7 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x05ce }
                r7.<init>();	 Catch:{ Exception -> 0x05ce }
                r9 = "SELECT end FROM ";
                r7 = r7.append(r9);	 Catch:{ Exception -> 0x05ce }
                r0 = r31;
                r7 = r7.append(r0);	 Catch:{ Exception -> 0x05ce }
                r9 = " WHERE uid = %d AND end <= %d ORDER BY end DESC LIMIT 1";
                r7 = r7.append(r9);	 Catch:{ Exception -> 0x05ce }
                r7 = r7.toString();	 Catch:{ Exception -> 0x05ce }
                r9 = 2;
                r9 = new java.lang.Object[r9];	 Catch:{ Exception -> 0x05ce }
                r10 = 0;
                r0 = r59;
                r0 = r6;	 Catch:{ Exception -> 0x05ce }
                r16 = r0;
                r11 = java.lang.Long.valueOf(r16);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r10 = 1;
                r0 = r59;
                r11 = r4;	 Catch:{ Exception -> 0x05ce }
                r11 = java.lang.Integer.valueOf(r11);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r6 = java.lang.String.format(r6, r7, r9);	 Catch:{ Exception -> 0x05ce }
                r7 = 0;
                r7 = new java.lang.Object[r7];	 Catch:{ Exception -> 0x05ce }
                r25 = r4.queryFinalized(r6, r7);	 Catch:{ Exception -> 0x05ce }
                r4 = r25.next();	 Catch:{ Exception -> 0x05ce }
                if (r4 == 0) goto L_0x0900;
            L_0x08ec:
                r4 = 0;
                r0 = r25;
                r4 = r0.intValue(r4);	 Catch:{ Exception -> 0x05ce }
                r0 = (long) r4;	 Catch:{ Exception -> 0x05ce }
                r32 = r0;
                if (r22 == 0) goto L_0x0900;
            L_0x08f8:
                r0 = r22;
                r6 = (long) r0;	 Catch:{ Exception -> 0x05ce }
                r4 = 32;
                r6 = r6 << r4;
                r32 = r32 | r6;
            L_0x0900:
                r25.dispose();	 Catch:{ Exception -> 0x05ce }
                r6 = 0;
                r4 = (r32 > r6 ? 1 : (r32 == r6 ? 0 : -1));
                if (r4 == 0) goto L_0x096b;
            L_0x0909:
                r0 = r59;
                r4 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x05ce }
                r4 = r4.database;	 Catch:{ Exception -> 0x05ce }
                r6 = java.util.Locale.US;	 Catch:{ Exception -> 0x05ce }
                r7 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x05ce }
                r7.<init>();	 Catch:{ Exception -> 0x05ce }
                r9 = "SELECT m.read_state, m.data, m.send_state, m.mid, m.date, r.random_id, m.replydata, m.media FROM messages as m LEFT JOIN randoms as r ON r.mid = m.mid WHERE m.uid = %d AND m.date <= %d AND m.mid < %d AND (m.mid >= %d OR m.mid < 0) ";
                r7 = r7.append(r9);	 Catch:{ Exception -> 0x05ce }
                r0 = r39;
                r7 = r7.append(r0);	 Catch:{ Exception -> 0x05ce }
                r9 = "ORDER BY m.date DESC, m.mid DESC LIMIT %d";
                r7 = r7.append(r9);	 Catch:{ Exception -> 0x05ce }
                r7 = r7.toString();	 Catch:{ Exception -> 0x05ce }
                r9 = 5;
                r9 = new java.lang.Object[r9];	 Catch:{ Exception -> 0x05ce }
                r10 = 0;
                r0 = r59;
                r0 = r6;	 Catch:{ Exception -> 0x05ce }
                r16 = r0;
                r11 = java.lang.Long.valueOf(r16);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r10 = 1;
                r0 = r59;
                r11 = r9;	 Catch:{ Exception -> 0x05ce }
                r11 = java.lang.Integer.valueOf(r11);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r10 = 2;
                r11 = java.lang.Long.valueOf(r48);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r10 = 3;
                r11 = java.lang.Long.valueOf(r32);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r10 = 4;
                r11 = java.lang.Integer.valueOf(r8);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r6 = java.lang.String.format(r6, r7, r9);	 Catch:{ Exception -> 0x05ce }
                r7 = 0;
                r7 = new java.lang.Object[r7];	 Catch:{ Exception -> 0x05ce }
                r25 = r4.queryFinalized(r6, r7);	 Catch:{ Exception -> 0x05ce }
                goto L_0x0418;
            L_0x096b:
                r0 = r59;
                r4 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x05ce }
                r4 = r4.database;	 Catch:{ Exception -> 0x05ce }
                r6 = java.util.Locale.US;	 Catch:{ Exception -> 0x05ce }
                r7 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x05ce }
                r7.<init>();	 Catch:{ Exception -> 0x05ce }
                r9 = "SELECT m.read_state, m.data, m.send_state, m.mid, m.date, r.random_id, m.replydata, m.media FROM messages as m LEFT JOIN randoms as r ON r.mid = m.mid WHERE m.uid = %d AND m.date <= %d AND m.mid < %d ";
                r7 = r7.append(r9);	 Catch:{ Exception -> 0x05ce }
                r0 = r39;
                r7 = r7.append(r0);	 Catch:{ Exception -> 0x05ce }
                r9 = "ORDER BY m.date DESC, m.mid DESC LIMIT %d";
                r7 = r7.append(r9);	 Catch:{ Exception -> 0x05ce }
                r7 = r7.toString();	 Catch:{ Exception -> 0x05ce }
                r9 = 4;
                r9 = new java.lang.Object[r9];	 Catch:{ Exception -> 0x05ce }
                r10 = 0;
                r0 = r59;
                r0 = r6;	 Catch:{ Exception -> 0x05ce }
                r16 = r0;
                r11 = java.lang.Long.valueOf(r16);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r10 = 1;
                r0 = r59;
                r11 = r9;	 Catch:{ Exception -> 0x05ce }
                r11 = java.lang.Integer.valueOf(r11);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r10 = 2;
                r11 = java.lang.Long.valueOf(r48);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r10 = 3;
                r11 = java.lang.Integer.valueOf(r8);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r6 = java.lang.String.format(r6, r7, r9);	 Catch:{ Exception -> 0x05ce }
                r7 = 0;
                r7 = new java.lang.Object[r7];	 Catch:{ Exception -> 0x05ce }
                r25 = r4.queryFinalized(r6, r7);	 Catch:{ Exception -> 0x05ce }
                goto L_0x0418;
            L_0x09c6:
                r0 = r59;
                r4 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x05ce }
                r4 = r4.database;	 Catch:{ Exception -> 0x05ce }
                r6 = java.util.Locale.US;	 Catch:{ Exception -> 0x05ce }
                r7 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x05ce }
                r7.<init>();	 Catch:{ Exception -> 0x05ce }
                r9 = "SELECT m.read_state, m.data, m.send_state, m.mid, m.date, r.random_id, m.replydata, m.media FROM messages as m LEFT JOIN randoms as r ON r.mid = m.mid WHERE m.uid = %d AND m.date <= %d ";
                r7 = r7.append(r9);	 Catch:{ Exception -> 0x05ce }
                r0 = r39;
                r7 = r7.append(r0);	 Catch:{ Exception -> 0x05ce }
                r9 = "ORDER BY m.date DESC, m.mid DESC LIMIT %d,%d";
                r7 = r7.append(r9);	 Catch:{ Exception -> 0x05ce }
                r7 = r7.toString();	 Catch:{ Exception -> 0x05ce }
                r9 = 4;
                r9 = new java.lang.Object[r9];	 Catch:{ Exception -> 0x05ce }
                r10 = 0;
                r0 = r59;
                r0 = r6;	 Catch:{ Exception -> 0x05ce }
                r16 = r0;
                r11 = java.lang.Long.valueOf(r16);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r10 = 1;
                r0 = r59;
                r11 = r9;	 Catch:{ Exception -> 0x05ce }
                r11 = java.lang.Integer.valueOf(r11);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r10 = 2;
                r11 = java.lang.Integer.valueOf(r52);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r10 = 3;
                r11 = java.lang.Integer.valueOf(r8);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r6 = java.lang.String.format(r6, r7, r9);	 Catch:{ Exception -> 0x05ce }
                r7 = 0;
                r7 = new java.lang.Object[r7];	 Catch:{ Exception -> 0x05ce }
                r25 = r4.queryFinalized(r6, r7);	 Catch:{ Exception -> 0x05ce }
                goto L_0x0418;
            L_0x0a21:
                r0 = r59;
                r4 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x05ce }
                r4 = r4.database;	 Catch:{ Exception -> 0x05ce }
                r6 = java.util.Locale.US;	 Catch:{ Exception -> 0x05ce }
                r7 = "SELECT max(mid) FROM messages WHERE uid = %d AND mid > 0";
                r9 = 1;
                r9 = new java.lang.Object[r9];	 Catch:{ Exception -> 0x05ce }
                r10 = 0;
                r0 = r59;
                r0 = r6;	 Catch:{ Exception -> 0x05ce }
                r16 = r0;
                r11 = java.lang.Long.valueOf(r16);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r6 = java.lang.String.format(r6, r7, r9);	 Catch:{ Exception -> 0x05ce }
                r7 = 0;
                r7 = new java.lang.Object[r7];	 Catch:{ Exception -> 0x05ce }
                r25 = r4.queryFinalized(r6, r7);	 Catch:{ Exception -> 0x05ce }
                r4 = r25.next();	 Catch:{ Exception -> 0x05ce }
                if (r4 == 0) goto L_0x0a55;
            L_0x0a4e:
                r4 = 0;
                r0 = r25;
                r13 = r0.intValue(r4);	 Catch:{ Exception -> 0x05ce }
            L_0x0a55:
                r25.dispose();	 Catch:{ Exception -> 0x05ce }
                r32 = 0;
                r0 = r59;
                r4 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x05ce }
                r4 = r4.database;	 Catch:{ Exception -> 0x05ce }
                r6 = java.util.Locale.US;	 Catch:{ Exception -> 0x05ce }
                r7 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x05ce }
                r7.<init>();	 Catch:{ Exception -> 0x05ce }
                r9 = "SELECT max(end) FROM ";
                r7 = r7.append(r9);	 Catch:{ Exception -> 0x05ce }
                r0 = r31;
                r7 = r7.append(r0);	 Catch:{ Exception -> 0x05ce }
                r9 = " WHERE uid = %d";
                r7 = r7.append(r9);	 Catch:{ Exception -> 0x05ce }
                r7 = r7.toString();	 Catch:{ Exception -> 0x05ce }
                r9 = 1;
                r9 = new java.lang.Object[r9];	 Catch:{ Exception -> 0x05ce }
                r10 = 0;
                r0 = r59;
                r0 = r6;	 Catch:{ Exception -> 0x05ce }
                r16 = r0;
                r11 = java.lang.Long.valueOf(r16);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r6 = java.lang.String.format(r6, r7, r9);	 Catch:{ Exception -> 0x05ce }
                r7 = 0;
                r7 = new java.lang.Object[r7];	 Catch:{ Exception -> 0x05ce }
                r25 = r4.queryFinalized(r6, r7);	 Catch:{ Exception -> 0x05ce }
                r4 = r25.next();	 Catch:{ Exception -> 0x05ce }
                if (r4 == 0) goto L_0x0ab4;
            L_0x0aa0:
                r4 = 0;
                r0 = r25;
                r4 = r0.intValue(r4);	 Catch:{ Exception -> 0x05ce }
                r0 = (long) r4;	 Catch:{ Exception -> 0x05ce }
                r32 = r0;
                if (r22 == 0) goto L_0x0ab4;
            L_0x0aac:
                r0 = r22;
                r6 = (long) r0;	 Catch:{ Exception -> 0x05ce }
                r4 = 32;
                r6 = r6 << r4;
                r32 = r32 | r6;
            L_0x0ab4:
                r25.dispose();	 Catch:{ Exception -> 0x05ce }
                r6 = 0;
                r4 = (r32 > r6 ? 1 : (r32 == r6 ? 0 : -1));
                if (r4 == 0) goto L_0x0b14;
            L_0x0abd:
                r0 = r59;
                r4 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x05ce }
                r4 = r4.database;	 Catch:{ Exception -> 0x05ce }
                r6 = java.util.Locale.US;	 Catch:{ Exception -> 0x05ce }
                r7 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x05ce }
                r7.<init>();	 Catch:{ Exception -> 0x05ce }
                r9 = "SELECT m.read_state, m.data, m.send_state, m.mid, m.date, r.random_id, m.replydata, m.media FROM messages as m LEFT JOIN randoms as r ON r.mid = m.mid WHERE m.uid = %d AND (m.mid >= %d OR m.mid < 0) ";
                r7 = r7.append(r9);	 Catch:{ Exception -> 0x05ce }
                r0 = r39;
                r7 = r7.append(r0);	 Catch:{ Exception -> 0x05ce }
                r9 = "ORDER BY m.date DESC, m.mid DESC LIMIT %d,%d";
                r7 = r7.append(r9);	 Catch:{ Exception -> 0x05ce }
                r7 = r7.toString();	 Catch:{ Exception -> 0x05ce }
                r9 = 4;
                r9 = new java.lang.Object[r9];	 Catch:{ Exception -> 0x05ce }
                r10 = 0;
                r0 = r59;
                r0 = r6;	 Catch:{ Exception -> 0x05ce }
                r16 = r0;
                r11 = java.lang.Long.valueOf(r16);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r10 = 1;
                r11 = java.lang.Long.valueOf(r32);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r10 = 2;
                r11 = java.lang.Integer.valueOf(r52);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r10 = 3;
                r11 = java.lang.Integer.valueOf(r8);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r6 = java.lang.String.format(r6, r7, r9);	 Catch:{ Exception -> 0x05ce }
                r7 = 0;
                r7 = new java.lang.Object[r7];	 Catch:{ Exception -> 0x05ce }
                r25 = r4.queryFinalized(r6, r7);	 Catch:{ Exception -> 0x05ce }
                goto L_0x0418;
            L_0x0b14:
                r0 = r59;
                r4 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x05ce }
                r4 = r4.database;	 Catch:{ Exception -> 0x05ce }
                r6 = java.util.Locale.US;	 Catch:{ Exception -> 0x05ce }
                r7 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x05ce }
                r7.<init>();	 Catch:{ Exception -> 0x05ce }
                r9 = "SELECT m.read_state, m.data, m.send_state, m.mid, m.date, r.random_id, m.replydata, m.media FROM messages as m LEFT JOIN randoms as r ON r.mid = m.mid WHERE m.uid = %d ";
                r7 = r7.append(r9);	 Catch:{ Exception -> 0x05ce }
                r0 = r39;
                r7 = r7.append(r0);	 Catch:{ Exception -> 0x05ce }
                r9 = "ORDER BY m.date DESC, m.mid DESC LIMIT %d,%d";
                r7 = r7.append(r9);	 Catch:{ Exception -> 0x05ce }
                r7 = r7.toString();	 Catch:{ Exception -> 0x05ce }
                r9 = 3;
                r9 = new java.lang.Object[r9];	 Catch:{ Exception -> 0x05ce }
                r10 = 0;
                r0 = r59;
                r0 = r6;	 Catch:{ Exception -> 0x05ce }
                r16 = r0;
                r11 = java.lang.Long.valueOf(r16);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r10 = 1;
                r11 = java.lang.Integer.valueOf(r52);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r10 = 2;
                r11 = java.lang.Integer.valueOf(r8);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r6 = java.lang.String.format(r6, r7, r9);	 Catch:{ Exception -> 0x05ce }
                r7 = 0;
                r7 = new java.lang.Object[r7];	 Catch:{ Exception -> 0x05ce }
                r25 = r4.queryFinalized(r6, r7);	 Catch:{ Exception -> 0x05ce }
                goto L_0x0418;
            L_0x0b64:
                r18 = 1;
                r0 = r59;
                r4 = r8;	 Catch:{ Exception -> 0x05ce }
                r6 = 1;
                if (r4 != r6) goto L_0x0ba8;
            L_0x0b6d:
                r0 = r59;
                r4 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x05ce }
                r4 = r4.database;	 Catch:{ Exception -> 0x05ce }
                r6 = java.util.Locale.US;	 Catch:{ Exception -> 0x05ce }
                r7 = "SELECT m.read_state, m.data, m.send_state, m.mid, m.date, r.random_id, m.replydata, m.media FROM messages as m LEFT JOIN randoms as r ON r.mid = m.mid WHERE m.uid = %d AND m.mid < %d ORDER BY m.mid DESC LIMIT %d";
                r9 = 3;
                r9 = new java.lang.Object[r9];	 Catch:{ Exception -> 0x05ce }
                r10 = 0;
                r0 = r59;
                r0 = r6;	 Catch:{ Exception -> 0x05ce }
                r16 = r0;
                r11 = java.lang.Long.valueOf(r16);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r10 = 1;
                r0 = r59;
                r11 = r4;	 Catch:{ Exception -> 0x05ce }
                r11 = java.lang.Integer.valueOf(r11);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r10 = 2;
                r11 = java.lang.Integer.valueOf(r8);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r6 = java.lang.String.format(r6, r7, r9);	 Catch:{ Exception -> 0x05ce }
                r7 = 0;
                r7 = new java.lang.Object[r7];	 Catch:{ Exception -> 0x05ce }
                r25 = r4.queryFinalized(r6, r7);	 Catch:{ Exception -> 0x05ce }
                goto L_0x0418;
            L_0x0ba8:
                r0 = r59;
                r4 = r9;	 Catch:{ Exception -> 0x05ce }
                if (r4 == 0) goto L_0x0c31;
            L_0x0bae:
                r0 = r59;
                r4 = r4;	 Catch:{ Exception -> 0x05ce }
                if (r4 == 0) goto L_0x0bef;
            L_0x0bb4:
                r0 = r59;
                r4 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x05ce }
                r4 = r4.database;	 Catch:{ Exception -> 0x05ce }
                r6 = java.util.Locale.US;	 Catch:{ Exception -> 0x05ce }
                r7 = "SELECT m.read_state, m.data, m.send_state, m.mid, m.date, r.random_id, m.replydata, m.media FROM messages as m LEFT JOIN randoms as r ON r.mid = m.mid WHERE m.uid = %d AND m.mid > %d ORDER BY m.mid ASC LIMIT %d";
                r9 = 3;
                r9 = new java.lang.Object[r9];	 Catch:{ Exception -> 0x05ce }
                r10 = 0;
                r0 = r59;
                r0 = r6;	 Catch:{ Exception -> 0x05ce }
                r16 = r0;
                r11 = java.lang.Long.valueOf(r16);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r10 = 1;
                r0 = r59;
                r11 = r4;	 Catch:{ Exception -> 0x05ce }
                r11 = java.lang.Integer.valueOf(r11);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r10 = 2;
                r11 = java.lang.Integer.valueOf(r8);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r6 = java.lang.String.format(r6, r7, r9);	 Catch:{ Exception -> 0x05ce }
                r7 = 0;
                r7 = new java.lang.Object[r7];	 Catch:{ Exception -> 0x05ce }
                r25 = r4.queryFinalized(r6, r7);	 Catch:{ Exception -> 0x05ce }
                goto L_0x0418;
            L_0x0bef:
                r0 = r59;
                r4 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x05ce }
                r4 = r4.database;	 Catch:{ Exception -> 0x05ce }
                r6 = java.util.Locale.US;	 Catch:{ Exception -> 0x05ce }
                r7 = "SELECT m.read_state, m.data, m.send_state, m.mid, m.date, r.random_id, m.replydata, m.media FROM messages as m LEFT JOIN randoms as r ON r.mid = m.mid WHERE m.uid = %d AND m.date <= %d ORDER BY m.mid ASC LIMIT %d,%d";
                r9 = 4;
                r9 = new java.lang.Object[r9];	 Catch:{ Exception -> 0x05ce }
                r10 = 0;
                r0 = r59;
                r0 = r6;	 Catch:{ Exception -> 0x05ce }
                r16 = r0;
                r11 = java.lang.Long.valueOf(r16);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r10 = 1;
                r0 = r59;
                r11 = r9;	 Catch:{ Exception -> 0x05ce }
                r11 = java.lang.Integer.valueOf(r11);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r10 = 2;
                r11 = java.lang.Integer.valueOf(r52);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r10 = 3;
                r11 = java.lang.Integer.valueOf(r8);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r6 = java.lang.String.format(r6, r7, r9);	 Catch:{ Exception -> 0x05ce }
                r7 = 0;
                r7 = new java.lang.Object[r7];	 Catch:{ Exception -> 0x05ce }
                r25 = r4.queryFinalized(r6, r7);	 Catch:{ Exception -> 0x05ce }
                goto L_0x0418;
            L_0x0c31:
                r0 = r59;
                r4 = r8;	 Catch:{ Exception -> 0x05ce }
                r6 = 2;
                if (r4 != r6) goto L_0x0ced;
            L_0x0c38:
                r0 = r59;
                r4 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x05ce }
                r4 = r4.database;	 Catch:{ Exception -> 0x05ce }
                r6 = java.util.Locale.US;	 Catch:{ Exception -> 0x05ce }
                r7 = "SELECT min(mid) FROM messages WHERE uid = %d AND mid < 0";
                r9 = 1;
                r9 = new java.lang.Object[r9];	 Catch:{ Exception -> 0x05ce }
                r10 = 0;
                r0 = r59;
                r0 = r6;	 Catch:{ Exception -> 0x05ce }
                r16 = r0;
                r11 = java.lang.Long.valueOf(r16);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r6 = java.lang.String.format(r6, r7, r9);	 Catch:{ Exception -> 0x05ce }
                r7 = 0;
                r7 = new java.lang.Object[r7];	 Catch:{ Exception -> 0x05ce }
                r25 = r4.queryFinalized(r6, r7);	 Catch:{ Exception -> 0x05ce }
                r4 = r25.next();	 Catch:{ Exception -> 0x05ce }
                if (r4 == 0) goto L_0x0c6c;
            L_0x0c65:
                r4 = 0;
                r0 = r25;
                r13 = r0.intValue(r4);	 Catch:{ Exception -> 0x05ce }
            L_0x0c6c:
                r25.dispose();	 Catch:{ Exception -> 0x05ce }
                r0 = r59;
                r4 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x05ce }
                r4 = r4.database;	 Catch:{ Exception -> 0x05ce }
                r6 = java.util.Locale.US;	 Catch:{ Exception -> 0x05ce }
                r7 = "SELECT max(mid), max(date) FROM messages WHERE uid = %d AND out = 0 AND read_state IN(0,2) AND mid < 0";
                r9 = 1;
                r9 = new java.lang.Object[r9];	 Catch:{ Exception -> 0x05ce }
                r10 = 0;
                r0 = r59;
                r0 = r6;	 Catch:{ Exception -> 0x05ce }
                r16 = r0;
                r11 = java.lang.Long.valueOf(r16);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r6 = java.lang.String.format(r6, r7, r9);	 Catch:{ Exception -> 0x05ce }
                r7 = 0;
                r7 = new java.lang.Object[r7];	 Catch:{ Exception -> 0x05ce }
                r25 = r4.queryFinalized(r6, r7);	 Catch:{ Exception -> 0x05ce }
                r4 = r25.next();	 Catch:{ Exception -> 0x05ce }
                if (r4 == 0) goto L_0x0caa;
            L_0x0c9c:
                r4 = 0;
                r0 = r25;
                r12 = r0.intValue(r4);	 Catch:{ Exception -> 0x05ce }
                r4 = 1;
                r0 = r25;
                r15 = r0.intValue(r4);	 Catch:{ Exception -> 0x05ce }
            L_0x0caa:
                r25.dispose();	 Catch:{ Exception -> 0x05ce }
                if (r12 == 0) goto L_0x0ced;
            L_0x0caf:
                r0 = r59;
                r4 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x05ce }
                r4 = r4.database;	 Catch:{ Exception -> 0x05ce }
                r6 = java.util.Locale.US;	 Catch:{ Exception -> 0x05ce }
                r7 = "SELECT COUNT(*) FROM messages WHERE uid = %d AND mid <= %d AND out = 0 AND read_state IN(0,2)";
                r9 = 2;
                r9 = new java.lang.Object[r9];	 Catch:{ Exception -> 0x05ce }
                r10 = 0;
                r0 = r59;
                r0 = r6;	 Catch:{ Exception -> 0x05ce }
                r16 = r0;
                r11 = java.lang.Long.valueOf(r16);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r10 = 1;
                r11 = java.lang.Integer.valueOf(r12);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r6 = java.lang.String.format(r6, r7, r9);	 Catch:{ Exception -> 0x05ce }
                r7 = 0;
                r7 = new java.lang.Object[r7];	 Catch:{ Exception -> 0x05ce }
                r25 = r4.queryFinalized(r6, r7);	 Catch:{ Exception -> 0x05ce }
                r4 = r25.next();	 Catch:{ Exception -> 0x05ce }
                if (r4 == 0) goto L_0x0cea;
            L_0x0ce3:
                r4 = 0;
                r0 = r25;
                r14 = r0.intValue(r4);	 Catch:{ Exception -> 0x05ce }
            L_0x0cea:
                r25.dispose();	 Catch:{ Exception -> 0x05ce }
            L_0x0ced:
                if (r8 > r14) goto L_0x0cf2;
            L_0x0cef:
                r4 = 4;
                if (r14 >= r4) goto L_0x0d35;
            L_0x0cf2:
                r4 = r14 + 10;
                r8 = java.lang.Math.max(r8, r4);	 Catch:{ Exception -> 0x05ce }
                r4 = 4;
                if (r14 >= r4) goto L_0x0cfe;
            L_0x0cfb:
                r14 = 0;
                r12 = 0;
                r13 = 0;
            L_0x0cfe:
                r0 = r59;
                r4 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x05ce }
                r4 = r4.database;	 Catch:{ Exception -> 0x05ce }
                r6 = java.util.Locale.US;	 Catch:{ Exception -> 0x05ce }
                r7 = "SELECT m.read_state, m.data, m.send_state, m.mid, m.date, r.random_id, m.replydata, m.media FROM messages as m LEFT JOIN randoms as r ON r.mid = m.mid WHERE m.uid = %d ORDER BY m.mid ASC LIMIT %d,%d";
                r9 = 3;
                r9 = new java.lang.Object[r9];	 Catch:{ Exception -> 0x05ce }
                r10 = 0;
                r0 = r59;
                r0 = r6;	 Catch:{ Exception -> 0x05ce }
                r16 = r0;
                r11 = java.lang.Long.valueOf(r16);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r10 = 1;
                r11 = java.lang.Integer.valueOf(r52);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r10 = 2;
                r11 = java.lang.Integer.valueOf(r8);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r6 = java.lang.String.format(r6, r7, r9);	 Catch:{ Exception -> 0x05ce }
                r7 = 0;
                r7 = new java.lang.Object[r7];	 Catch:{ Exception -> 0x05ce }
                r25 = r4.queryFinalized(r6, r7);	 Catch:{ Exception -> 0x05ce }
                goto L_0x0418;
            L_0x0d35:
                r52 = r14 - r8;
                r8 = r8 + 10;
                goto L_0x0cfe;
            L_0x0d3a:
                r29 = move-exception;
                r4 = "tmessages";
                r0 = r29;
                org.telegram.messenger.FileLog.m611e(r4, r0);	 Catch:{ Exception -> 0x05ce }
                goto L_0x05c9;
            L_0x0d44:
                r25.dispose();	 Catch:{ Exception -> 0x05ce }
            L_0x0d47:
                r4 = r5.messages;	 Catch:{ Exception -> 0x05ce }
                r6 = new org.telegram.messenger.MessagesStorage$39$1;	 Catch:{ Exception -> 0x05ce }
                r0 = r59;
                r6.<init>();	 Catch:{ Exception -> 0x05ce }
                java.util.Collections.sort(r4, r6);	 Catch:{ Exception -> 0x05ce }
                r0 = r59;
                r4 = r8;	 Catch:{ Exception -> 0x05ce }
                r6 = 3;
                if (r4 == r6) goto L_0x0d63;
            L_0x0d5a:
                r0 = r59;
                r4 = r8;	 Catch:{ Exception -> 0x05ce }
                r6 = 2;
                if (r4 != r6) goto L_0x0da6;
            L_0x0d61:
                if (r20 == 0) goto L_0x0da6;
            L_0x0d63:
                r4 = r5.messages;	 Catch:{ Exception -> 0x05ce }
                r4 = r4.isEmpty();	 Catch:{ Exception -> 0x05ce }
                if (r4 != 0) goto L_0x0da6;
            L_0x0d6b:
                r4 = r5.messages;	 Catch:{ Exception -> 0x05ce }
                r6 = r5.messages;	 Catch:{ Exception -> 0x05ce }
                r6 = r6.size();	 Catch:{ Exception -> 0x05ce }
                r6 = r6 + -1;
                r4 = r4.get(r6);	 Catch:{ Exception -> 0x05ce }
                r4 = (org.telegram.tgnet.TLRPC.Message) r4;	 Catch:{ Exception -> 0x05ce }
                r0 = r4.id;	 Catch:{ Exception -> 0x05ce }
                r51 = r0;
                r4 = r5.messages;	 Catch:{ Exception -> 0x05ce }
                r6 = 0;
                r4 = r4.get(r6);	 Catch:{ Exception -> 0x05ce }
                r4 = (org.telegram.tgnet.TLRPC.Message) r4;	 Catch:{ Exception -> 0x05ce }
                r0 = r4.id;	 Catch:{ Exception -> 0x05ce }
                r42 = r0;
                r0 = r51;
                r1 = r43;
                if (r0 > r1) goto L_0x0d98;
            L_0x0d92:
                r0 = r42;
                r1 = r43;
                if (r0 >= r1) goto L_0x0da6;
            L_0x0d98:
                r55.clear();	 Catch:{ Exception -> 0x05ce }
                r57.clear();	 Catch:{ Exception -> 0x05ce }
                r23.clear();	 Catch:{ Exception -> 0x05ce }
                r4 = r5.messages;	 Catch:{ Exception -> 0x05ce }
                r4.clear();	 Catch:{ Exception -> 0x05ce }
            L_0x0da6:
                r0 = r59;
                r4 = r8;	 Catch:{ Exception -> 0x05ce }
                r6 = 3;
                if (r4 != r6) goto L_0x0dbb;
            L_0x0dad:
                r4 = r5.messages;	 Catch:{ Exception -> 0x05ce }
                r4 = r4.size();	 Catch:{ Exception -> 0x05ce }
                r6 = 1;
                if (r4 != r6) goto L_0x0dbb;
            L_0x0db6:
                r4 = r5.messages;	 Catch:{ Exception -> 0x05ce }
                r4.clear();	 Catch:{ Exception -> 0x05ce }
            L_0x0dbb:
                r0 = r59;
                r4 = r5;	 Catch:{ Exception -> 0x05ce }
                r6 = 2;
                if (r4 != r6) goto L_0x0eba;
            L_0x0dc2:
                r4 = r5.messages;	 Catch:{ Exception -> 0x05ce }
                r4 = r4.isEmpty();	 Catch:{ Exception -> 0x05ce }
                if (r4 != 0) goto L_0x0eba;
            L_0x0dca:
                r0 = r59;
                r4 = r4;	 Catch:{ Exception -> 0x05ce }
                if (r4 == 0) goto L_0x0e72;
            L_0x0dd0:
                r0 = r59;
                r4 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x05ce }
                r6 = r4.database;	 Catch:{ Exception -> 0x05ce }
                r7 = java.util.Locale.US;	 Catch:{ Exception -> 0x05ce }
                r9 = "SELECT start, end, count FROM channel_group WHERE uid = %d AND ((start >= %d AND end <= %d) OR (start = %d))";
                r4 = 4;
                r10 = new java.lang.Object[r4];	 Catch:{ Exception -> 0x05ce }
                r4 = 0;
                r0 = r59;
                r0 = r6;	 Catch:{ Exception -> 0x05ce }
                r16 = r0;
                r11 = java.lang.Long.valueOf(r16);	 Catch:{ Exception -> 0x05ce }
                r10[r4] = r11;	 Catch:{ Exception -> 0x05ce }
                r11 = 1;
                r4 = r5.messages;	 Catch:{ Exception -> 0x05ce }
                r0 = r5.messages;	 Catch:{ Exception -> 0x05ce }
                r16 = r0;
                r16 = r16.size();	 Catch:{ Exception -> 0x05ce }
                r16 = r16 + -1;
                r0 = r16;
                r4 = r4.get(r0);	 Catch:{ Exception -> 0x05ce }
                r4 = (org.telegram.tgnet.TLRPC.Message) r4;	 Catch:{ Exception -> 0x05ce }
                r4 = r4.id;	 Catch:{ Exception -> 0x05ce }
                r4 = java.lang.Integer.valueOf(r4);	 Catch:{ Exception -> 0x05ce }
                r10[r11] = r4;	 Catch:{ Exception -> 0x05ce }
                r11 = 2;
                r4 = r5.messages;	 Catch:{ Exception -> 0x05ce }
                r16 = 0;
                r0 = r16;
                r4 = r4.get(r0);	 Catch:{ Exception -> 0x05ce }
                r4 = (org.telegram.tgnet.TLRPC.Message) r4;	 Catch:{ Exception -> 0x05ce }
                r4 = r4.id;	 Catch:{ Exception -> 0x05ce }
                r4 = java.lang.Integer.valueOf(r4);	 Catch:{ Exception -> 0x05ce }
                r10[r11] = r4;	 Catch:{ Exception -> 0x05ce }
                r11 = 3;
                r4 = r5.messages;	 Catch:{ Exception -> 0x05ce }
                r16 = 0;
                r0 = r16;
                r4 = r4.get(r0);	 Catch:{ Exception -> 0x05ce }
                r4 = (org.telegram.tgnet.TLRPC.Message) r4;	 Catch:{ Exception -> 0x05ce }
                r4 = r4.id;	 Catch:{ Exception -> 0x05ce }
                r4 = java.lang.Integer.valueOf(r4);	 Catch:{ Exception -> 0x05ce }
                r10[r11] = r4;	 Catch:{ Exception -> 0x05ce }
                r4 = java.lang.String.format(r7, r9, r10);	 Catch:{ Exception -> 0x05ce }
                r7 = 0;
                r7 = new java.lang.Object[r7];	 Catch:{ Exception -> 0x05ce }
                r25 = r6.queryFinalized(r4, r7);	 Catch:{ Exception -> 0x05ce }
            L_0x0e3e:
                r4 = r25.next();	 Catch:{ Exception -> 0x05ce }
                if (r4 == 0) goto L_0x0eb7;
            L_0x0e44:
                r30 = new org.telegram.tgnet.TLRPC$TL_messageGroup;	 Catch:{ Exception -> 0x05ce }
                r30.<init>();	 Catch:{ Exception -> 0x05ce }
                r4 = 0;
                r0 = r25;
                r4 = r0.intValue(r4);	 Catch:{ Exception -> 0x05ce }
                r0 = r30;
                r0.min_id = r4;	 Catch:{ Exception -> 0x05ce }
                r4 = 1;
                r0 = r25;
                r4 = r0.intValue(r4);	 Catch:{ Exception -> 0x05ce }
                r0 = r30;
                r0.max_id = r4;	 Catch:{ Exception -> 0x05ce }
                r4 = 2;
                r0 = r25;
                r4 = r0.intValue(r4);	 Catch:{ Exception -> 0x05ce }
                r0 = r30;
                r0.count = r4;	 Catch:{ Exception -> 0x05ce }
                r4 = r5.collapsed;	 Catch:{ Exception -> 0x05ce }
                r0 = r30;
                r4.add(r0);	 Catch:{ Exception -> 0x05ce }
                goto L_0x0e3e;
            L_0x0e72:
                r0 = r59;
                r4 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x05ce }
                r6 = r4.database;	 Catch:{ Exception -> 0x05ce }
                r7 = java.util.Locale.US;	 Catch:{ Exception -> 0x05ce }
                r9 = "SELECT start, end, count FROM channel_group WHERE uid = %d AND start >= %d";
                r4 = 2;
                r10 = new java.lang.Object[r4];	 Catch:{ Exception -> 0x05ce }
                r4 = 0;
                r0 = r59;
                r0 = r6;	 Catch:{ Exception -> 0x05ce }
                r16 = r0;
                r11 = java.lang.Long.valueOf(r16);	 Catch:{ Exception -> 0x05ce }
                r10[r4] = r11;	 Catch:{ Exception -> 0x05ce }
                r11 = 1;
                r4 = r5.messages;	 Catch:{ Exception -> 0x05ce }
                r0 = r5.messages;	 Catch:{ Exception -> 0x05ce }
                r16 = r0;
                r16 = r16.size();	 Catch:{ Exception -> 0x05ce }
                r16 = r16 + -1;
                r0 = r16;
                r4 = r4.get(r0);	 Catch:{ Exception -> 0x05ce }
                r4 = (org.telegram.tgnet.TLRPC.Message) r4;	 Catch:{ Exception -> 0x05ce }
                r4 = r4.id;	 Catch:{ Exception -> 0x05ce }
                r4 = java.lang.Integer.valueOf(r4);	 Catch:{ Exception -> 0x05ce }
                r10[r11] = r4;	 Catch:{ Exception -> 0x05ce }
                r4 = java.lang.String.format(r7, r9, r10);	 Catch:{ Exception -> 0x05ce }
                r7 = 0;
                r7 = new java.lang.Object[r7];	 Catch:{ Exception -> 0x05ce }
                r25 = r6.queryFinalized(r4, r7);	 Catch:{ Exception -> 0x05ce }
                goto L_0x0e3e;
            L_0x0eb7:
                r25.dispose();	 Catch:{ Exception -> 0x05ce }
            L_0x0eba:
                r4 = r55.isEmpty();	 Catch:{ Exception -> 0x05ce }
                if (r4 != 0) goto L_0x0f6c;
            L_0x0ec0:
                r0 = r59;
                r4 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x05ce }
                r4 = r4.database;	 Catch:{ Exception -> 0x05ce }
                r6 = java.util.Locale.US;	 Catch:{ Exception -> 0x05ce }
                r7 = "SELECT data, mid, date FROM messages WHERE mid IN(%s)";
                r9 = 1;
                r9 = new java.lang.Object[r9];	 Catch:{ Exception -> 0x05ce }
                r10 = 0;
                r11 = ",";
                r0 = r55;
                r11 = android.text.TextUtils.join(r11, r0);	 Catch:{ Exception -> 0x05ce }
                r9[r10] = r11;	 Catch:{ Exception -> 0x05ce }
                r6 = java.lang.String.format(r6, r7, r9);	 Catch:{ Exception -> 0x05ce }
                r7 = 0;
                r7 = new java.lang.Object[r7];	 Catch:{ Exception -> 0x05ce }
                r25 = r4.queryFinalized(r6, r7);	 Catch:{ Exception -> 0x05ce }
            L_0x0ee5:
                r4 = r25.next();	 Catch:{ Exception -> 0x05ce }
                if (r4 == 0) goto L_0x0f69;
            L_0x0eeb:
                r27 = new org.telegram.tgnet.NativeByteBuffer;	 Catch:{ Exception -> 0x05ce }
                r4 = 0;
                r0 = r25;
                r4 = r0.byteArrayLength(r4);	 Catch:{ Exception -> 0x05ce }
                r0 = r27;
                r0.<init>(r4);	 Catch:{ Exception -> 0x05ce }
                if (r27 == 0) goto L_0x0f64;
            L_0x0efb:
                r4 = 0;
                r0 = r25;
                r1 = r27;
                r4 = r0.byteBufferValue(r4, r1);	 Catch:{ Exception -> 0x05ce }
                if (r4 == 0) goto L_0x0f64;
            L_0x0f06:
                r4 = 0;
                r0 = r27;
                r4 = r0.readInt32(r4);	 Catch:{ Exception -> 0x05ce }
                r6 = 0;
                r0 = r27;
                r44 = org.telegram.tgnet.TLRPC.Message.TLdeserialize(r0, r4, r6);	 Catch:{ Exception -> 0x05ce }
                r4 = 1;
                r0 = r25;
                r4 = r0.intValue(r4);	 Catch:{ Exception -> 0x05ce }
                r0 = r44;
                r0.id = r4;	 Catch:{ Exception -> 0x05ce }
                r4 = 2;
                r0 = r25;
                r4 = r0.intValue(r4);	 Catch:{ Exception -> 0x05ce }
                r0 = r44;
                r0.date = r4;	 Catch:{ Exception -> 0x05ce }
                r0 = r59;
                r6 = r6;	 Catch:{ Exception -> 0x05ce }
                r0 = r44;
                r0.dialog_id = r6;	 Catch:{ Exception -> 0x05ce }
                r0 = r44;
                r1 = r57;
                r2 = r23;
                org.telegram.messenger.MessagesStorage.addUsersAndChatsFromMessage(r0, r1, r2);	 Catch:{ Exception -> 0x05ce }
                r0 = r44;
                r4 = r0.id;	 Catch:{ Exception -> 0x05ce }
                r4 = java.lang.Integer.valueOf(r4);	 Catch:{ Exception -> 0x05ce }
                r0 = r54;
                r21 = r0.get(r4);	 Catch:{ Exception -> 0x05ce }
                r21 = (java.util.ArrayList) r21;	 Catch:{ Exception -> 0x05ce }
                if (r21 == 0) goto L_0x0f64;
            L_0x0f4d:
                r38 = r21.iterator();	 Catch:{ Exception -> 0x05ce }
            L_0x0f51:
                r4 = r38.hasNext();	 Catch:{ Exception -> 0x05ce }
                if (r4 == 0) goto L_0x0f64;
            L_0x0f57:
                r41 = r38.next();	 Catch:{ Exception -> 0x05ce }
                r41 = (org.telegram.tgnet.TLRPC.Message) r41;	 Catch:{ Exception -> 0x05ce }
                r0 = r44;
                r1 = r41;
                r1.replyMessage = r0;	 Catch:{ Exception -> 0x05ce }
                goto L_0x0f51;
            L_0x0f64:
                r27.reuse();	 Catch:{ Exception -> 0x05ce }
                goto L_0x0ee5;
            L_0x0f69:
                r25.dispose();	 Catch:{ Exception -> 0x05ce }
            L_0x0f6c:
                r4 = r57.isEmpty();	 Catch:{ Exception -> 0x05ce }
                if (r4 != 0) goto L_0x0f83;
            L_0x0f72:
                r0 = r59;
                r4 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x05ce }
                r6 = ",";
                r0 = r57;
                r6 = android.text.TextUtils.join(r6, r0);	 Catch:{ Exception -> 0x05ce }
                r7 = r5.users;	 Catch:{ Exception -> 0x05ce }
                r4.getUsersInternal(r6, r7);	 Catch:{ Exception -> 0x05ce }
            L_0x0f83:
                r4 = r23.isEmpty();	 Catch:{ Exception -> 0x05ce }
                if (r4 != 0) goto L_0x0f9a;
            L_0x0f89:
                r0 = r59;
                r4 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x05ce }
                r6 = ",";
                r0 = r23;
                r6 = android.text.TextUtils.join(r6, r0);	 Catch:{ Exception -> 0x05ce }
                r7 = r5.chats;	 Catch:{ Exception -> 0x05ce }
                r4.getChatsInternal(r6, r7);	 Catch:{ Exception -> 0x05ce }
            L_0x0f9a:
                r4 = org.telegram.messenger.MessagesController.getInstance();
                r0 = r59;
                r6 = r6;
                r0 = r59;
                r9 = r4;
                r10 = 1;
                r0 = r59;
                r11 = r10;
                r0 = r59;
                r0 = r8;
                r16 = r0;
                r0 = r59;
                r0 = r5;
                r17 = r0;
                r0 = r59;
                r0 = r11;
                r19 = r0;
                r4.processLoadedMessages(r5, r6, r8, r9, r10, r11, r12, r13, r14, r15, r16, r17, r18, r19, r20);
                goto L_0x0610;
                */
                throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MessagesStorage.39.run():void");
            }
        });
    }

    public void startTransaction(boolean useQueue) {
        if (useQueue) {
            this.storageQueue.postRunnable(new Runnable() {
                public void run() {
                    try {
                        MessagesStorage.this.database.beginTransaction();
                    } catch (Throwable e) {
                        FileLog.m611e("tmessages", e);
                    }
                }
            });
            return;
        }
        try {
            this.database.beginTransaction();
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
        }
    }

    public void commitTransaction(boolean useQueue) {
        if (useQueue) {
            this.storageQueue.postRunnable(new Runnable() {
                public void run() {
                    try {
                        MessagesStorage.this.database.commitTransaction();
                    } catch (Throwable e) {
                        FileLog.m611e("tmessages", e);
                    }
                }
            });
            return;
        }
        try {
            this.database.commitTransaction();
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
        }
    }

    public TLObject getSentFile(String path, int type) {
        if (path == null) {
            return null;
        }
        TLObject tLObject;
        final Semaphore semaphore = new Semaphore(0);
        final ArrayList<TLObject> result = new ArrayList();
        final String str = path;
        final int i = type;
        this.storageQueue.postRunnable(new Runnable() {
            /* JADX WARNING: inconsistent code. */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void run() {
                /*
                r11 = this;
                r5 = r2;	 Catch:{ Exception -> 0x0066 }
                r4 = org.telegram.messenger.Utilities.MD5(r5);	 Catch:{ Exception -> 0x0066 }
                if (r4 == 0) goto L_0x0060;
            L_0x0008:
                r5 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x0066 }
                r5 = r5.database;	 Catch:{ Exception -> 0x0066 }
                r6 = java.util.Locale.US;	 Catch:{ Exception -> 0x0066 }
                r7 = "SELECT data FROM sent_files_v2 WHERE uid = '%s' AND type = %d";
                r8 = 2;
                r8 = new java.lang.Object[r8];	 Catch:{ Exception -> 0x0066 }
                r9 = 0;
                r8[r9] = r4;	 Catch:{ Exception -> 0x0066 }
                r9 = 1;
                r10 = r3;	 Catch:{ Exception -> 0x0066 }
                r10 = java.lang.Integer.valueOf(r10);	 Catch:{ Exception -> 0x0066 }
                r8[r9] = r10;	 Catch:{ Exception -> 0x0066 }
                r6 = java.lang.String.format(r6, r7, r8);	 Catch:{ Exception -> 0x0066 }
                r7 = 0;
                r7 = new java.lang.Object[r7];	 Catch:{ Exception -> 0x0066 }
                r0 = r5.queryFinalized(r6, r7);	 Catch:{ Exception -> 0x0066 }
                r5 = r0.next();	 Catch:{ Exception -> 0x0066 }
                if (r5 == 0) goto L_0x005d;
            L_0x0032:
                r1 = new org.telegram.tgnet.NativeByteBuffer;	 Catch:{ Exception -> 0x0066 }
                r5 = 0;
                r5 = r0.byteArrayLength(r5);	 Catch:{ Exception -> 0x0066 }
                r1.<init>(r5);	 Catch:{ Exception -> 0x0066 }
                if (r1 == 0) goto L_0x005a;
            L_0x003e:
                r5 = 0;
                r5 = r0.byteBufferValue(r5, r1);	 Catch:{ Exception -> 0x0066 }
                if (r5 == 0) goto L_0x005a;
            L_0x0045:
                r5 = org.telegram.tgnet.TLClassStore.Instance();	 Catch:{ Exception -> 0x0066 }
                r6 = 0;
                r6 = r1.readInt32(r6);	 Catch:{ Exception -> 0x0066 }
                r7 = 0;
                r3 = r5.TLdeserialize(r1, r6, r7);	 Catch:{ Exception -> 0x0066 }
                if (r3 == 0) goto L_0x005a;
            L_0x0055:
                r5 = r4;	 Catch:{ Exception -> 0x0066 }
                r5.add(r3);	 Catch:{ Exception -> 0x0066 }
            L_0x005a:
                r1.reuse();	 Catch:{ Exception -> 0x0066 }
            L_0x005d:
                r0.dispose();	 Catch:{ Exception -> 0x0066 }
            L_0x0060:
                r5 = r5;
                r5.release();
            L_0x0065:
                return;
            L_0x0066:
                r2 = move-exception;
                r5 = "tmessages";
                org.telegram.messenger.FileLog.m611e(r5, r2);	 Catch:{ all -> 0x0072 }
                r5 = r5;
                r5.release();
                goto L_0x0065;
            L_0x0072:
                r5 = move-exception;
                r6 = r5;
                r6.release();
                throw r5;
                */
                throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MessagesStorage.42.run():void");
            }
        });
        try {
            semaphore.acquire();
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
        }
        if (result.isEmpty()) {
            tLObject = null;
        } else {
            tLObject = (TLObject) result.get(0);
        }
        return tLObject;
    }

    public void putSentFile(final String path, final TLObject file, final int type) {
        if (path != null && file != null) {
            this.storageQueue.postRunnable(new Runnable() {
                public void run() {
                    /* JADX: method processing error */
/*
Error: java.util.NoSuchElementException
	at java.util.HashMap$HashIterator.nextNode(HashMap.java:1431)
	at java.util.HashMap$KeyIterator.next(HashMap.java:1453)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.applyRemove(BlockFinallyExtract.java:535)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.extractFinally(BlockFinallyExtract.java:175)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.processExceptionHandler(BlockFinallyExtract.java:79)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:51)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
                    /*
                    r6 = this;
                    r3 = 0;
                    r4 = r3;	 Catch:{ Exception -> 0x0042, all -> 0x004e }
                    r2 = org.telegram.messenger.Utilities.MD5(r4);	 Catch:{ Exception -> 0x0042, all -> 0x004e }
                    if (r2 == 0) goto L_0x003c;	 Catch:{ Exception -> 0x0042, all -> 0x004e }
                L_0x0009:
                    r4 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x0042, all -> 0x004e }
                    r4 = r4.database;	 Catch:{ Exception -> 0x0042, all -> 0x004e }
                    r5 = "REPLACE INTO sent_files_v2 VALUES(?, ?, ?)";	 Catch:{ Exception -> 0x0042, all -> 0x004e }
                    r3 = r4.executeFast(r5);	 Catch:{ Exception -> 0x0042, all -> 0x004e }
                    r3.requery();	 Catch:{ Exception -> 0x0042, all -> 0x004e }
                    r0 = new org.telegram.tgnet.NativeByteBuffer;	 Catch:{ Exception -> 0x0042, all -> 0x004e }
                    r4 = r4;	 Catch:{ Exception -> 0x0042, all -> 0x004e }
                    r4 = r4.getObjectSize();	 Catch:{ Exception -> 0x0042, all -> 0x004e }
                    r0.<init>(r4);	 Catch:{ Exception -> 0x0042, all -> 0x004e }
                    r4 = r4;	 Catch:{ Exception -> 0x0042, all -> 0x004e }
                    r4.serializeToStream(r0);	 Catch:{ Exception -> 0x0042, all -> 0x004e }
                    r4 = 1;	 Catch:{ Exception -> 0x0042, all -> 0x004e }
                    r3.bindString(r4, r2);	 Catch:{ Exception -> 0x0042, all -> 0x004e }
                    r4 = 2;	 Catch:{ Exception -> 0x0042, all -> 0x004e }
                    r5 = r5;	 Catch:{ Exception -> 0x0042, all -> 0x004e }
                    r3.bindInteger(r4, r5);	 Catch:{ Exception -> 0x0042, all -> 0x004e }
                    r4 = 3;	 Catch:{ Exception -> 0x0042, all -> 0x004e }
                    r3.bindByteBuffer(r4, r0);	 Catch:{ Exception -> 0x0042, all -> 0x004e }
                    r3.step();	 Catch:{ Exception -> 0x0042, all -> 0x004e }
                    r0.reuse();	 Catch:{ Exception -> 0x0042, all -> 0x004e }
                L_0x003c:
                    if (r3 == 0) goto L_0x0041;
                L_0x003e:
                    r3.dispose();
                L_0x0041:
                    return;
                L_0x0042:
                    r1 = move-exception;
                    r4 = "tmessages";	 Catch:{ Exception -> 0x0042, all -> 0x004e }
                    org.telegram.messenger.FileLog.m611e(r4, r1);	 Catch:{ Exception -> 0x0042, all -> 0x004e }
                    if (r3 == 0) goto L_0x0041;
                L_0x004a:
                    r3.dispose();
                    goto L_0x0041;
                L_0x004e:
                    r4 = move-exception;
                    if (r3 == 0) goto L_0x0054;
                L_0x0051:
                    r3.dispose();
                L_0x0054:
                    throw r4;
                    */
                    throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MessagesStorage.43.run():void");
                }
            });
        }
    }

    public void updateEncryptedChatSeq(final EncryptedChat chat) {
        if (chat != null) {
            this.storageQueue.postRunnable(new Runnable() {
                public void run() {
                    /* JADX: method processing error */
/*
Error: java.util.NoSuchElementException
	at java.util.HashMap$HashIterator.nextNode(HashMap.java:1431)
	at java.util.HashMap$KeyIterator.next(HashMap.java:1453)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.applyRemove(BlockFinallyExtract.java:535)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.extractFinally(BlockFinallyExtract.java:175)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.processExceptionHandler(BlockFinallyExtract.java:79)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:51)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
                    /*
                    r5 = this;
                    r1 = 0;
                    r2 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x003d, all -> 0x0049 }
                    r2 = r2.database;	 Catch:{ Exception -> 0x003d, all -> 0x0049 }
                    r3 = "UPDATE enc_chats SET seq_in = ?, seq_out = ?, use_count = ? WHERE uid = ?";	 Catch:{ Exception -> 0x003d, all -> 0x0049 }
                    r1 = r2.executeFast(r3);	 Catch:{ Exception -> 0x003d, all -> 0x0049 }
                    r2 = 1;	 Catch:{ Exception -> 0x003d, all -> 0x0049 }
                    r3 = r3;	 Catch:{ Exception -> 0x003d, all -> 0x0049 }
                    r3 = r3.seq_in;	 Catch:{ Exception -> 0x003d, all -> 0x0049 }
                    r1.bindInteger(r2, r3);	 Catch:{ Exception -> 0x003d, all -> 0x0049 }
                    r2 = 2;	 Catch:{ Exception -> 0x003d, all -> 0x0049 }
                    r3 = r3;	 Catch:{ Exception -> 0x003d, all -> 0x0049 }
                    r3 = r3.seq_out;	 Catch:{ Exception -> 0x003d, all -> 0x0049 }
                    r1.bindInteger(r2, r3);	 Catch:{ Exception -> 0x003d, all -> 0x0049 }
                    r2 = 3;	 Catch:{ Exception -> 0x003d, all -> 0x0049 }
                    r3 = r3;	 Catch:{ Exception -> 0x003d, all -> 0x0049 }
                    r3 = r3.key_use_count_in;	 Catch:{ Exception -> 0x003d, all -> 0x0049 }
                    r3 = r3 << 16;	 Catch:{ Exception -> 0x003d, all -> 0x0049 }
                    r4 = r3;	 Catch:{ Exception -> 0x003d, all -> 0x0049 }
                    r4 = r4.key_use_count_out;	 Catch:{ Exception -> 0x003d, all -> 0x0049 }
                    r3 = r3 | r4;	 Catch:{ Exception -> 0x003d, all -> 0x0049 }
                    r1.bindInteger(r2, r3);	 Catch:{ Exception -> 0x003d, all -> 0x0049 }
                    r2 = 4;	 Catch:{ Exception -> 0x003d, all -> 0x0049 }
                    r3 = r3;	 Catch:{ Exception -> 0x003d, all -> 0x0049 }
                    r3 = r3.id;	 Catch:{ Exception -> 0x003d, all -> 0x0049 }
                    r1.bindInteger(r2, r3);	 Catch:{ Exception -> 0x003d, all -> 0x0049 }
                    r1.step();	 Catch:{ Exception -> 0x003d, all -> 0x0049 }
                    if (r1 == 0) goto L_0x003c;
                L_0x0039:
                    r1.dispose();
                L_0x003c:
                    return;
                L_0x003d:
                    r0 = move-exception;
                    r2 = "tmessages";	 Catch:{ Exception -> 0x003d, all -> 0x0049 }
                    org.telegram.messenger.FileLog.m611e(r2, r0);	 Catch:{ Exception -> 0x003d, all -> 0x0049 }
                    if (r1 == 0) goto L_0x003c;
                L_0x0045:
                    r1.dispose();
                    goto L_0x003c;
                L_0x0049:
                    r2 = move-exception;
                    if (r1 == 0) goto L_0x004f;
                L_0x004c:
                    r1.dispose();
                L_0x004f:
                    throw r2;
                    */
                    throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MessagesStorage.44.run():void");
                }
            });
        }
    }

    public void updateEncryptedChatTTL(final EncryptedChat chat) {
        if (chat != null) {
            this.storageQueue.postRunnable(new Runnable() {
                public void run() {
                    /* JADX: method processing error */
/*
Error: java.util.NoSuchElementException
	at java.util.HashMap$HashIterator.nextNode(HashMap.java:1431)
	at java.util.HashMap$KeyIterator.next(HashMap.java:1453)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.applyRemove(BlockFinallyExtract.java:535)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.extractFinally(BlockFinallyExtract.java:175)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.processExceptionHandler(BlockFinallyExtract.java:79)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:51)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
                    /*
                    r4 = this;
                    r1 = 0;
                    r2 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x0026, all -> 0x0032 }
                    r2 = r2.database;	 Catch:{ Exception -> 0x0026, all -> 0x0032 }
                    r3 = "UPDATE enc_chats SET ttl = ? WHERE uid = ?";	 Catch:{ Exception -> 0x0026, all -> 0x0032 }
                    r1 = r2.executeFast(r3);	 Catch:{ Exception -> 0x0026, all -> 0x0032 }
                    r2 = 1;	 Catch:{ Exception -> 0x0026, all -> 0x0032 }
                    r3 = r3;	 Catch:{ Exception -> 0x0026, all -> 0x0032 }
                    r3 = r3.ttl;	 Catch:{ Exception -> 0x0026, all -> 0x0032 }
                    r1.bindInteger(r2, r3);	 Catch:{ Exception -> 0x0026, all -> 0x0032 }
                    r2 = 2;	 Catch:{ Exception -> 0x0026, all -> 0x0032 }
                    r3 = r3;	 Catch:{ Exception -> 0x0026, all -> 0x0032 }
                    r3 = r3.id;	 Catch:{ Exception -> 0x0026, all -> 0x0032 }
                    r1.bindInteger(r2, r3);	 Catch:{ Exception -> 0x0026, all -> 0x0032 }
                    r1.step();	 Catch:{ Exception -> 0x0026, all -> 0x0032 }
                    if (r1 == 0) goto L_0x0025;
                L_0x0022:
                    r1.dispose();
                L_0x0025:
                    return;
                L_0x0026:
                    r0 = move-exception;
                    r2 = "tmessages";	 Catch:{ Exception -> 0x0026, all -> 0x0032 }
                    org.telegram.messenger.FileLog.m611e(r2, r0);	 Catch:{ Exception -> 0x0026, all -> 0x0032 }
                    if (r1 == 0) goto L_0x0025;
                L_0x002e:
                    r1.dispose();
                    goto L_0x0025;
                L_0x0032:
                    r2 = move-exception;
                    if (r1 == 0) goto L_0x0038;
                L_0x0035:
                    r1.dispose();
                L_0x0038:
                    throw r2;
                    */
                    throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MessagesStorage.45.run():void");
                }
            });
        }
    }

    public void updateEncryptedChatLayer(final EncryptedChat chat) {
        if (chat != null) {
            this.storageQueue.postRunnable(new Runnable() {
                public void run() {
                    /* JADX: method processing error */
/*
Error: java.util.NoSuchElementException
	at java.util.HashMap$HashIterator.nextNode(HashMap.java:1431)
	at java.util.HashMap$KeyIterator.next(HashMap.java:1453)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.applyRemove(BlockFinallyExtract.java:535)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.extractFinally(BlockFinallyExtract.java:175)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.processExceptionHandler(BlockFinallyExtract.java:79)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:51)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
                    /*
                    r4 = this;
                    r1 = 0;
                    r2 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x0026, all -> 0x0032 }
                    r2 = r2.database;	 Catch:{ Exception -> 0x0026, all -> 0x0032 }
                    r3 = "UPDATE enc_chats SET layer = ? WHERE uid = ?";	 Catch:{ Exception -> 0x0026, all -> 0x0032 }
                    r1 = r2.executeFast(r3);	 Catch:{ Exception -> 0x0026, all -> 0x0032 }
                    r2 = 1;	 Catch:{ Exception -> 0x0026, all -> 0x0032 }
                    r3 = r3;	 Catch:{ Exception -> 0x0026, all -> 0x0032 }
                    r3 = r3.layer;	 Catch:{ Exception -> 0x0026, all -> 0x0032 }
                    r1.bindInteger(r2, r3);	 Catch:{ Exception -> 0x0026, all -> 0x0032 }
                    r2 = 2;	 Catch:{ Exception -> 0x0026, all -> 0x0032 }
                    r3 = r3;	 Catch:{ Exception -> 0x0026, all -> 0x0032 }
                    r3 = r3.id;	 Catch:{ Exception -> 0x0026, all -> 0x0032 }
                    r1.bindInteger(r2, r3);	 Catch:{ Exception -> 0x0026, all -> 0x0032 }
                    r1.step();	 Catch:{ Exception -> 0x0026, all -> 0x0032 }
                    if (r1 == 0) goto L_0x0025;
                L_0x0022:
                    r1.dispose();
                L_0x0025:
                    return;
                L_0x0026:
                    r0 = move-exception;
                    r2 = "tmessages";	 Catch:{ Exception -> 0x0026, all -> 0x0032 }
                    org.telegram.messenger.FileLog.m611e(r2, r0);	 Catch:{ Exception -> 0x0026, all -> 0x0032 }
                    if (r1 == 0) goto L_0x0025;
                L_0x002e:
                    r1.dispose();
                    goto L_0x0025;
                L_0x0032:
                    r2 = move-exception;
                    if (r1 == 0) goto L_0x0038;
                L_0x0035:
                    r1.dispose();
                L_0x0038:
                    throw r2;
                    */
                    throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MessagesStorage.46.run():void");
                }
            });
        }
    }

    public void updateEncryptedChat(final EncryptedChat chat) {
        if (chat != null) {
            this.storageQueue.postRunnable(new Runnable() {
                public void run() {
                    /* JADX: method processing error */
/*
Error: java.util.NoSuchElementException
	at java.util.HashMap$HashIterator.nextNode(HashMap.java:1431)
	at java.util.HashMap$KeyIterator.next(HashMap.java:1453)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.applyRemove(BlockFinallyExtract.java:535)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.extractFinally(BlockFinallyExtract.java:175)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.processExceptionHandler(BlockFinallyExtract.java:79)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:51)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
                    /*
                    r13 = this;
                    r10 = 16;
                    r8 = 1;
                    r7 = 0;
                    r9 = r3;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r9 = r9.key_hash;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    if (r9 == 0) goto L_0x0011;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                L_0x000a:
                    r9 = r3;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r9 = r9.key_hash;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r9 = r9.length;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    if (r9 == r10) goto L_0x0035;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                L_0x0011:
                    r9 = r3;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r9 = r9.auth_key;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    if (r9 == 0) goto L_0x0035;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                L_0x0017:
                    r9 = r3;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r9 = r9.auth_key;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r6 = org.telegram.messenger.Utilities.computeSHA1(r9);	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r9 = r3;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r10 = 16;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r10 = new byte[r10];	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r9.key_hash = r10;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r9 = 0;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r10 = r3;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r10 = r10.key_hash;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r11 = 0;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r12 = r3;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r12 = r12.key_hash;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r12 = r12.length;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    java.lang.System.arraycopy(r6, r9, r10, r11, r12);	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                L_0x0035:
                    r9 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r9 = r9.database;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r10 = "UPDATE enc_chats SET data = ?, g = ?, authkey = ?, ttl = ?, layer = ?, seq_in = ?, seq_out = ?, use_count = ?, exchange_id = ?, key_date = ?, fprint = ?, fauthkey = ?, khash = ? WHERE uid = ?";	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r7 = r9.executeFast(r10);	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r0 = new org.telegram.tgnet.NativeByteBuffer;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r9 = r3;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r9 = r9.getObjectSize();	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r0.<init>(r9);	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r1 = new org.telegram.tgnet.NativeByteBuffer;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r9 = r3;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r9 = r9.a_or_b;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    if (r9 == 0) goto L_0x0147;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                L_0x0054:
                    r9 = r3;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r9 = r9.a_or_b;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r9 = r9.length;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                L_0x0059:
                    r1.<init>(r9);	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r2 = new org.telegram.tgnet.NativeByteBuffer;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r9 = r3;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r9 = r9.auth_key;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    if (r9 == 0) goto L_0x014a;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                L_0x0064:
                    r9 = r3;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r9 = r9.auth_key;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r9 = r9.length;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                L_0x0069:
                    r2.<init>(r9);	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r3 = new org.telegram.tgnet.NativeByteBuffer;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r9 = r3;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r9 = r9.future_auth_key;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    if (r9 == 0) goto L_0x014d;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                L_0x0074:
                    r9 = r3;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r9 = r9.future_auth_key;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r9 = r9.length;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                L_0x0079:
                    r3.<init>(r9);	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r4 = new org.telegram.tgnet.NativeByteBuffer;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r9 = r3;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r9 = r9.key_hash;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    if (r9 == 0) goto L_0x0089;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                L_0x0084:
                    r8 = r3;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r8 = r8.key_hash;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r8 = r8.length;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                L_0x0089:
                    r4.<init>(r8);	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r8 = r3;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r8.serializeToStream(r0);	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r8 = 1;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r7.bindByteBuffer(r8, r0);	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r8 = r3;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r8 = r8.a_or_b;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    if (r8 == 0) goto L_0x00a2;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                L_0x009b:
                    r8 = r3;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r8 = r8.a_or_b;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r1.writeBytes(r8);	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                L_0x00a2:
                    r8 = r3;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r8 = r8.auth_key;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    if (r8 == 0) goto L_0x00af;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                L_0x00a8:
                    r8 = r3;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r8 = r8.auth_key;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r2.writeBytes(r8);	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                L_0x00af:
                    r8 = r3;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r8 = r8.future_auth_key;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    if (r8 == 0) goto L_0x00bc;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                L_0x00b5:
                    r8 = r3;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r8 = r8.future_auth_key;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r3.writeBytes(r8);	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                L_0x00bc:
                    r8 = r3;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r8 = r8.key_hash;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    if (r8 == 0) goto L_0x00c9;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                L_0x00c2:
                    r8 = r3;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r8 = r8.key_hash;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r4.writeBytes(r8);	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                L_0x00c9:
                    r8 = 2;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r7.bindByteBuffer(r8, r1);	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r8 = 3;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r7.bindByteBuffer(r8, r2);	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r8 = 4;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r9 = r3;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r9 = r9.ttl;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r7.bindInteger(r8, r9);	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r8 = 5;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r9 = r3;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r9 = r9.layer;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r7.bindInteger(r8, r9);	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r8 = 6;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r9 = r3;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r9 = r9.seq_in;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r7.bindInteger(r8, r9);	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r8 = 7;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r9 = r3;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r9 = r9.seq_out;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r7.bindInteger(r8, r9);	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r8 = 8;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r9 = r3;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r9 = r9.key_use_count_in;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r9 = r9 << 16;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r10 = r3;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r10 = r10.key_use_count_out;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r9 = r9 | r10;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r7.bindInteger(r8, r9);	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r8 = 9;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r9 = r3;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r10 = r9.exchange_id;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r7.bindLong(r8, r10);	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r8 = 10;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r9 = r3;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r9 = r9.key_create_date;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r7.bindInteger(r8, r9);	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r8 = 11;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r9 = r3;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r10 = r9.future_key_fingerprint;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r7.bindLong(r8, r10);	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r8 = 12;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r7.bindByteBuffer(r8, r3);	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r8 = 13;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r7.bindByteBuffer(r8, r4);	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r8 = 14;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r9 = r3;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r9 = r9.id;	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r7.bindInteger(r8, r9);	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r7.step();	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r0.reuse();	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r1.reuse();	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r2.reuse();	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r3.reuse();	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    r4.reuse();	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    if (r7 == 0) goto L_0x0146;
                L_0x0143:
                    r7.dispose();
                L_0x0146:
                    return;
                L_0x0147:
                    r9 = r8;
                    goto L_0x0059;
                L_0x014a:
                    r9 = r8;
                    goto L_0x0069;
                L_0x014d:
                    r9 = r8;
                    goto L_0x0079;
                L_0x0150:
                    r5 = move-exception;
                    r8 = "tmessages";	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    org.telegram.messenger.FileLog.m611e(r8, r5);	 Catch:{ Exception -> 0x0150, all -> 0x015c }
                    if (r7 == 0) goto L_0x0146;
                L_0x0158:
                    r7.dispose();
                    goto L_0x0146;
                L_0x015c:
                    r8 = move-exception;
                    if (r7 == 0) goto L_0x0162;
                L_0x015f:
                    r7.dispose();
                L_0x0162:
                    throw r8;
                    */
                    throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MessagesStorage.47.run():void");
                }
            });
        }
    }

    public boolean isDialogHasMessages(long did) {
        final Semaphore semaphore = new Semaphore(0);
        final boolean[] result = new boolean[1];
        final long j = did;
        this.storageQueue.postRunnable(new Runnable() {
            public void run() {
                try {
                    SQLiteCursor cursor = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT mid FROM messages WHERE uid = %d LIMIT 1", new Object[]{Long.valueOf(j)}), new Object[0]);
                    result[0] = cursor.next();
                    cursor.dispose();
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                } finally {
                    semaphore.release();
                }
            }
        });
        try {
            semaphore.acquire();
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
        }
        return result[0];
    }

    public void getEncryptedChat(final int chat_id, final Semaphore semaphore, final ArrayList<TLObject> result) {
        if (semaphore != null && result != null) {
            this.storageQueue.postRunnable(new Runnable() {
                /* JADX WARNING: inconsistent code. */
                /* Code decompiled incorrectly, please refer to instructions dump. */
                public void run() {
                    /*
                    r7 = this;
                    r3 = new java.util.ArrayList;	 Catch:{ Exception -> 0x0060 }
                    r3.<init>();	 Catch:{ Exception -> 0x0060 }
                    r1 = new java.util.ArrayList;	 Catch:{ Exception -> 0x0060 }
                    r1.<init>();	 Catch:{ Exception -> 0x0060 }
                    r4 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x0060 }
                    r5 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0060 }
                    r5.<init>();	 Catch:{ Exception -> 0x0060 }
                    r6 = "";
                    r5 = r5.append(r6);	 Catch:{ Exception -> 0x0060 }
                    r6 = r3;	 Catch:{ Exception -> 0x0060 }
                    r5 = r5.append(r6);	 Catch:{ Exception -> 0x0060 }
                    r5 = r5.toString();	 Catch:{ Exception -> 0x0060 }
                    r4.getEncryptedChatsInternal(r5, r1, r3);	 Catch:{ Exception -> 0x0060 }
                    r4 = r1.isEmpty();	 Catch:{ Exception -> 0x0060 }
                    if (r4 != 0) goto L_0x005a;
                L_0x002a:
                    r4 = r3.isEmpty();	 Catch:{ Exception -> 0x0060 }
                    if (r4 != 0) goto L_0x005a;
                L_0x0030:
                    r2 = new java.util.ArrayList;	 Catch:{ Exception -> 0x0060 }
                    r2.<init>();	 Catch:{ Exception -> 0x0060 }
                    r4 = org.telegram.messenger.MessagesStorage.this;	 Catch:{ Exception -> 0x0060 }
                    r5 = ",";
                    r5 = android.text.TextUtils.join(r5, r3);	 Catch:{ Exception -> 0x0060 }
                    r4.getUsersInternal(r5, r2);	 Catch:{ Exception -> 0x0060 }
                    r4 = r2.isEmpty();	 Catch:{ Exception -> 0x0060 }
                    if (r4 != 0) goto L_0x005a;
                L_0x0046:
                    r4 = r5;	 Catch:{ Exception -> 0x0060 }
                    r5 = 0;
                    r5 = r1.get(r5);	 Catch:{ Exception -> 0x0060 }
                    r4.add(r5);	 Catch:{ Exception -> 0x0060 }
                    r4 = r5;	 Catch:{ Exception -> 0x0060 }
                    r5 = 0;
                    r5 = r2.get(r5);	 Catch:{ Exception -> 0x0060 }
                    r4.add(r5);	 Catch:{ Exception -> 0x0060 }
                L_0x005a:
                    r4 = r4;
                    r4.release();
                L_0x005f:
                    return;
                L_0x0060:
                    r0 = move-exception;
                    r4 = "tmessages";
                    org.telegram.messenger.FileLog.m611e(r4, r0);	 Catch:{ all -> 0x006c }
                    r4 = r4;
                    r4.release();
                    goto L_0x005f;
                L_0x006c:
                    r4 = move-exception;
                    r5 = r4;
                    r5.release();
                    throw r4;
                    */
                    throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.MessagesStorage.49.run():void");
                }
            });
        }
    }

    public void putEncryptedChat(final EncryptedChat chat, final User user, final Dialog dialog) {
        if (chat != null) {
            this.storageQueue.postRunnable(new Runnable() {
                public void run() {
                    int i = 1;
                    try {
                        int length;
                        if ((chat.key_hash == null || chat.key_hash.length != 16) && chat.auth_key != null) {
                            byte[] sha1 = Utilities.computeSHA1(chat.auth_key);
                            chat.key_hash = new byte[16];
                            System.arraycopy(sha1, 0, chat.key_hash, 0, chat.key_hash.length);
                        }
                        SQLitePreparedStatement state = MessagesStorage.this.database.executeFast("REPLACE INTO enc_chats VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                        NativeByteBuffer data = new NativeByteBuffer(chat.getObjectSize());
                        if (chat.a_or_b != null) {
                            length = chat.a_or_b.length;
                        } else {
                            length = 1;
                        }
                        NativeByteBuffer data2 = new NativeByteBuffer(length);
                        if (chat.auth_key != null) {
                            length = chat.auth_key.length;
                        } else {
                            length = 1;
                        }
                        NativeByteBuffer data3 = new NativeByteBuffer(length);
                        if (chat.future_auth_key != null) {
                            length = chat.future_auth_key.length;
                        } else {
                            length = 1;
                        }
                        NativeByteBuffer data4 = new NativeByteBuffer(length);
                        if (chat.key_hash != null) {
                            i = chat.key_hash.length;
                        }
                        NativeByteBuffer data5 = new NativeByteBuffer(i);
                        chat.serializeToStream(data);
                        state.bindInteger(1, chat.id);
                        state.bindInteger(2, user.id);
                        state.bindString(3, MessagesStorage.this.formatUserSearchName(user));
                        state.bindByteBuffer(4, data);
                        if (chat.a_or_b != null) {
                            data2.writeBytes(chat.a_or_b);
                        }
                        if (chat.auth_key != null) {
                            data3.writeBytes(chat.auth_key);
                        }
                        if (chat.future_auth_key != null) {
                            data4.writeBytes(chat.future_auth_key);
                        }
                        if (chat.key_hash != null) {
                            data5.writeBytes(chat.key_hash);
                        }
                        state.bindByteBuffer(5, data2);
                        state.bindByteBuffer(6, data3);
                        state.bindInteger(7, chat.ttl);
                        state.bindInteger(8, chat.layer);
                        state.bindInteger(9, chat.seq_in);
                        state.bindInteger(10, chat.seq_out);
                        state.bindInteger(11, (chat.key_use_count_in << 16) | chat.key_use_count_out);
                        state.bindLong(12, chat.exchange_id);
                        state.bindInteger(13, chat.key_create_date);
                        state.bindLong(14, chat.future_key_fingerprint);
                        state.bindByteBuffer(15, data4);
                        state.bindByteBuffer(16, data5);
                        state.step();
                        state.dispose();
                        data.reuse();
                        data2.reuse();
                        data3.reuse();
                        data4.reuse();
                        data5.reuse();
                        if (dialog != null) {
                            state = MessagesStorage.this.database.executeFast("REPLACE INTO dialogs VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                            state.bindLong(1, dialog.id);
                            state.bindInteger(2, dialog.last_message_date);
                            state.bindInteger(3, dialog.unread_count);
                            state.bindInteger(4, dialog.top_message);
                            state.bindInteger(5, dialog.read_inbox_max_id);
                            state.bindInteger(6, 0);
                            state.bindInteger(7, dialog.top_not_important_message);
                            state.bindInteger(8, dialog.unread_not_important_count);
                            state.bindInteger(9, dialog.pts);
                            state.bindInteger(10, 0);
                            state.step();
                            state.dispose();
                        }
                    } catch (Throwable e) {
                        FileLog.m611e("tmessages", e);
                    }
                }
            });
        }
    }

    private String formatUserSearchName(User user) {
        StringBuilder str = new StringBuilder("");
        if (user.first_name != null && user.first_name.length() > 0) {
            str.append(user.first_name);
        }
        if (user.last_name != null && user.last_name.length() > 0) {
            if (str.length() > 0) {
                str.append(" ");
            }
            str.append(user.last_name);
        }
        str.append(";;;");
        if (user.username != null && user.username.length() > 0) {
            str.append(user.username);
        }
        return str.toString().toLowerCase();
    }

    private void putUsersInternal(ArrayList<User> users) throws Exception {
        if (users != null && !users.isEmpty()) {
            SQLitePreparedStatement state = this.database.executeFast("REPLACE INTO users VALUES(?, ?, ?, ?)");
            Iterator i$ = users.iterator();
            while (i$.hasNext()) {
                User user = (User) i$.next();
                state.requery();
                NativeByteBuffer data = new NativeByteBuffer(user.getObjectSize());
                user.serializeToStream(data);
                state.bindInteger(1, user.id);
                state.bindString(2, formatUserSearchName(user));
                if (user.status != null) {
                    if (user.status instanceof TL_userStatusRecently) {
                        user.status.expires = -100;
                    } else if (user.status instanceof TL_userStatusLastWeek) {
                        user.status.expires = -101;
                    } else if (user.status instanceof TL_userStatusLastMonth) {
                        user.status.expires = -102;
                    }
                    state.bindInteger(3, user.status.expires);
                } else {
                    state.bindInteger(3, 0);
                }
                state.bindByteBuffer(4, data);
                state.step();
                data.reuse();
            }
            state.dispose();
        }
    }

    private void putChatsInternal(ArrayList<Chat> chats) throws Exception {
        if (chats != null && !chats.isEmpty()) {
            SQLitePreparedStatement state = this.database.executeFast("REPLACE INTO chats VALUES(?, ?, ?)");
            Iterator i$ = chats.iterator();
            while (i$.hasNext()) {
                Chat chat = (Chat) i$.next();
                state.requery();
                NativeByteBuffer data = new NativeByteBuffer(chat.getObjectSize());
                chat.serializeToStream(data);
                state.bindInteger(1, chat.id);
                if (chat.title != null) {
                    state.bindString(2, chat.title.toLowerCase());
                } else {
                    state.bindString(2, "");
                }
                state.bindByteBuffer(3, data);
                state.step();
                data.reuse();
            }
            state.dispose();
        }
    }

    public void getUsersInternal(String usersToLoad, ArrayList<User> result) throws Exception {
        if (usersToLoad != null && usersToLoad.length() != 0 && result != null) {
            SQLiteCursor cursor = this.database.queryFinalized(String.format(Locale.US, "SELECT data, status FROM users WHERE uid IN(%s)", new Object[]{usersToLoad}), new Object[0]);
            while (cursor.next()) {
                try {
                    NativeByteBuffer data = new NativeByteBuffer(cursor.byteArrayLength(0));
                    if (!(data == null || cursor.byteBufferValue(0, data) == 0)) {
                        User user = User.TLdeserialize(data, data.readInt32(false), false);
                        if (user != null) {
                            if (user.status != null) {
                                user.status.expires = cursor.intValue(1);
                            }
                            result.add(user);
                        }
                    }
                    data.reuse();
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
            cursor.dispose();
        }
    }

    public void getChatsInternal(String chatsToLoad, ArrayList<Chat> result) throws Exception {
        if (chatsToLoad != null && chatsToLoad.length() != 0 && result != null) {
            SQLiteCursor cursor = this.database.queryFinalized(String.format(Locale.US, "SELECT data FROM chats WHERE uid IN(%s)", new Object[]{chatsToLoad}), new Object[0]);
            while (cursor.next()) {
                try {
                    NativeByteBuffer data = new NativeByteBuffer(cursor.byteArrayLength(0));
                    if (!(data == null || cursor.byteBufferValue(0, data) == 0)) {
                        Chat chat = Chat.TLdeserialize(data, data.readInt32(false), false);
                        if (chat != null) {
                            result.add(chat);
                        }
                    }
                    data.reuse();
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
            cursor.dispose();
        }
    }

    public void getEncryptedChatsInternal(String chatsToLoad, ArrayList<EncryptedChat> result, ArrayList<Integer> usersToLoad) throws Exception {
        if (chatsToLoad != null && chatsToLoad.length() != 0 && result != null) {
            SQLiteCursor cursor = this.database.queryFinalized(String.format(Locale.US, "SELECT data, user, g, authkey, ttl, layer, seq_in, seq_out, use_count, exchange_id, key_date, fprint, fauthkey, khash FROM enc_chats WHERE uid IN(%s)", new Object[]{chatsToLoad}), new Object[0]);
            while (cursor.next()) {
                try {
                    NativeByteBuffer data = new NativeByteBuffer(cursor.byteArrayLength(0));
                    if (!(data == null || cursor.byteBufferValue(0, data) == 0)) {
                        EncryptedChat chat = EncryptedChat.TLdeserialize(data, data.readInt32(false), false);
                        if (chat != null) {
                            chat.user_id = cursor.intValue(1);
                            if (!(usersToLoad == null || usersToLoad.contains(Integer.valueOf(chat.user_id)))) {
                                usersToLoad.add(Integer.valueOf(chat.user_id));
                            }
                            chat.a_or_b = cursor.byteArrayValue(2);
                            chat.auth_key = cursor.byteArrayValue(3);
                            chat.ttl = cursor.intValue(4);
                            chat.layer = cursor.intValue(5);
                            chat.seq_in = cursor.intValue(6);
                            chat.seq_out = cursor.intValue(7);
                            int use_count = cursor.intValue(8);
                            chat.key_use_count_in = (short) (use_count >> 16);
                            chat.key_use_count_out = (short) use_count;
                            chat.exchange_id = cursor.longValue(9);
                            chat.key_create_date = cursor.intValue(10);
                            chat.future_key_fingerprint = cursor.longValue(11);
                            chat.future_auth_key = cursor.byteArrayValue(12);
                            chat.key_hash = cursor.byteArrayValue(13);
                            result.add(chat);
                        }
                    }
                    data.reuse();
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
            cursor.dispose();
        }
    }

    private void putUsersAndChatsInternal(ArrayList<User> users, ArrayList<Chat> chats, boolean withTransaction) {
        if (withTransaction) {
            try {
                this.database.beginTransaction();
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
                return;
            }
        }
        putUsersInternal(users);
        putChatsInternal(chats);
        if (withTransaction) {
            this.database.commitTransaction();
        }
    }

    public void putUsersAndChats(final ArrayList<User> users, final ArrayList<Chat> chats, final boolean withTransaction, boolean useQueue) {
        if (users != null && users.isEmpty() && chats != null && chats.isEmpty()) {
            return;
        }
        if (useQueue) {
            this.storageQueue.postRunnable(new Runnable() {
                public void run() {
                    MessagesStorage.this.putUsersAndChatsInternal(users, chats, withTransaction);
                }
            });
        } else {
            putUsersAndChatsInternal(users, chats, withTransaction);
        }
    }

    public void removeFromDownloadQueue(long id, int type, boolean move) {
        final boolean z = move;
        final int i = type;
        final long j = id;
        this.storageQueue.postRunnable(new Runnable() {
            public void run() {
                try {
                    if (z) {
                        int minDate = -1;
                        SQLiteCursor cursor = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT min(date) FROM download_queue WHERE type = %d", new Object[]{Integer.valueOf(i)}), new Object[0]);
                        if (cursor.next()) {
                            minDate = cursor.intValue(0);
                        }
                        cursor.dispose();
                        if (minDate != -1) {
                            MessagesStorage.this.database.executeFast(String.format(Locale.US, "UPDATE download_queue SET date = %d WHERE uid = %d AND type = %d", new Object[]{Integer.valueOf(minDate - 1), Long.valueOf(j), Integer.valueOf(i)})).stepThis().dispose();
                            return;
                        }
                        return;
                    }
                    MessagesStorage.this.database.executeFast(String.format(Locale.US, "DELETE FROM download_queue WHERE uid = %d AND type = %d", new Object[]{Long.valueOf(j), Integer.valueOf(i)})).stepThis().dispose();
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
        });
    }

    public void clearDownloadQueue(final int type) {
        this.storageQueue.postRunnable(new Runnable() {
            public void run() {
                try {
                    if (type == 0) {
                        MessagesStorage.this.database.executeFast("DELETE FROM download_queue WHERE 1").stepThis().dispose();
                        return;
                    }
                    MessagesStorage.this.database.executeFast(String.format(Locale.US, "DELETE FROM download_queue WHERE type = %d", new Object[]{Integer.valueOf(type)})).stepThis().dispose();
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
        });
    }

    public void getDownloadQueue(final int type) {
        this.storageQueue.postRunnable(new Runnable() {
            public void run() {
                try {
                    final ArrayList<DownloadObject> objects = new ArrayList();
                    SQLiteCursor cursor = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT uid, type, data FROM download_queue WHERE type = %d ORDER BY date DESC LIMIT 3", new Object[]{Integer.valueOf(type)}), new Object[0]);
                    while (cursor.next()) {
                        DownloadObject downloadObject = new DownloadObject();
                        downloadObject.type = cursor.intValue(1);
                        downloadObject.id = cursor.longValue(0);
                        NativeByteBuffer data = new NativeByteBuffer(cursor.byteArrayLength(2));
                        if (!(data == null || cursor.byteBufferValue(2, data) == 0)) {
                            downloadObject.object = TLClassStore.Instance().TLdeserialize(data, data.readInt32(false), false);
                        }
                        data.reuse();
                        objects.add(downloadObject);
                    }
                    cursor.dispose();
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        public void run() {
                            MediaController.getInstance().processDownloadObjects(type, objects);
                        }
                    });
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
        });
    }

    private int getMessageMediaType(Message message) {
        if ((message instanceof TL_message_secret) && (((message.media instanceof TL_messageMediaPhoto) && message.ttl > 0 && message.ttl <= 60) || (message.media instanceof TL_messageMediaAudio) || (message.media instanceof TL_messageMediaVideo))) {
            return 1;
        }
        if ((message.media instanceof TL_messageMediaPhoto) || (message.media instanceof TL_messageMediaVideo)) {
            return 0;
        }
        return -1;
    }

    public void putWebPages(final HashMap<Long, WebPage> webPages) {
        if (webPages != null && !webPages.isEmpty()) {
            this.storageQueue.postRunnable(new Runnable() {
                public void run() {
                    try {
                        String ids = TextUtils.join(",", webPages.keySet());
                        SQLiteCursor cursor = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT mid FROM webpage_pending WHERE id IN (%s)", new Object[]{ids}), new Object[0]);
                        ArrayList<Long> mids = new ArrayList();
                        while (cursor.next()) {
                            mids.add(Long.valueOf(cursor.longValue(0)));
                        }
                        cursor.dispose();
                        if (!mids.isEmpty()) {
                            NativeByteBuffer data;
                            Message message;
                            final ArrayList<Message> messages = new ArrayList();
                            cursor = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT mid, data FROM messages WHERE mid IN (%s)", new Object[]{TextUtils.join(",", mids)}), new Object[0]);
                            while (cursor.next()) {
                                int mid = cursor.intValue(0);
                                data = new NativeByteBuffer(cursor.byteArrayLength(1));
                                if (!(data == null || cursor.byteBufferValue(1, data) == 0)) {
                                    message = Message.TLdeserialize(data, data.readInt32(false), false);
                                    if (message.media instanceof TL_messageMediaWebPage) {
                                        message.id = mid;
                                        message.media.webpage = (WebPage) webPages.get(Long.valueOf(message.media.webpage.id));
                                        messages.add(message);
                                    }
                                }
                                data.reuse();
                            }
                            cursor.dispose();
                            MessagesStorage.this.database.executeFast(String.format(Locale.US, "DELETE FROM webpage_pending WHERE id IN (%s)", new Object[]{ids})).stepThis().dispose();
                            if (!messages.isEmpty()) {
                                MessagesStorage.this.database.beginTransaction();
                                SQLitePreparedStatement state = MessagesStorage.this.database.executeFast("UPDATE messages SET data = ? WHERE mid = ?");
                                SQLitePreparedStatement state2 = MessagesStorage.this.database.executeFast("UPDATE media_v2 SET data = ? WHERE mid = ?");
                                Iterator i$ = messages.iterator();
                                while (i$.hasNext()) {
                                    message = (Message) i$.next();
                                    data = new NativeByteBuffer(message.getObjectSize());
                                    message.serializeToStream(data);
                                    long messageId = (long) message.id;
                                    if (message.to_id.channel_id != 0) {
                                        messageId |= ((long) message.to_id.channel_id) << 32;
                                    }
                                    state.requery();
                                    state.bindByteBuffer(1, data);
                                    state.bindLong(2, messageId);
                                    state.step();
                                    state2.requery();
                                    state2.bindByteBuffer(1, data);
                                    state2.bindLong(2, messageId);
                                    state2.step();
                                    data.reuse();
                                }
                                state.dispose();
                                state2.dispose();
                                MessagesStorage.this.database.commitTransaction();
                                AndroidUtilities.runOnUIThread(new Runnable() {
                                    public void run() {
                                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.didReceivedWebpages, messages);
                                    }
                                });
                            }
                        }
                    } catch (Throwable e) {
                        FileLog.m611e("tmessages", e);
                    }
                }
            });
        }
    }

    public void overwriteChannel(final int channel_id, final TL_updates_channelDifferenceTooLong difference, final int newDialogType) {
        this.storageQueue.postRunnable(new Runnable() {
            public void run() {
                boolean checkInvite = false;
                try {
                    final long did = (long) (-channel_id);
                    if (newDialogType != 0) {
                        SQLiteCursor cursor = MessagesStorage.this.database.queryFinalized("SELECT pts FROM dialogs WHERE did = " + did, new Object[0]);
                        if (!cursor.next()) {
                            checkInvite = true;
                        }
                        cursor.dispose();
                    }
                    MessagesStorage.this.database.executeFast("DELETE FROM messages WHERE uid = " + did).stepThis().dispose();
                    MessagesStorage.this.database.executeFast("DELETE FROM channel_group WHERE uid = " + did).stepThis().dispose();
                    MessagesStorage.this.database.executeFast("DELETE FROM bot_keyboard WHERE uid = " + did).stepThis().dispose();
                    MessagesStorage.this.database.executeFast("DELETE FROM media_counts_v2 WHERE uid = " + did).stepThis().dispose();
                    MessagesStorage.this.database.executeFast("DELETE FROM media_v2 WHERE uid = " + did).stepThis().dispose();
                    MessagesStorage.this.database.executeFast("DELETE FROM messages_holes WHERE uid = " + did).stepThis().dispose();
                    MessagesStorage.this.database.executeFast("DELETE FROM messages_imp_holes WHERE uid = " + did).stepThis().dispose();
                    MessagesStorage.this.database.executeFast("DELETE FROM media_holes_v2 WHERE uid = " + did).stepThis().dispose();
                    BotQuery.clearBotKeyboard(did, null);
                    TL_messages_dialogs dialogs = new TL_messages_dialogs();
                    dialogs.chats.addAll(difference.chats);
                    dialogs.users.addAll(difference.users);
                    dialogs.messages.addAll(difference.messages);
                    TL_dialogChannel dialog = new TL_dialogChannel();
                    dialog.id = did;
                    dialog.peer = new TL_peerChannel();
                    dialog.peer.channel_id = channel_id;
                    dialog.top_not_important_message = difference.top_message;
                    dialog.top_message = difference.top_important_message;
                    dialog.read_inbox_max_id = difference.read_inbox_max_id;
                    dialog.unread_not_important_count = difference.unread_count;
                    dialog.unread_count = difference.unread_important_count;
                    dialog.notify_settings = null;
                    dialog.pts = difference.pts;
                    dialogs.dialogs.add(dialog);
                    MessagesStorage.this.putDialogsInternal(dialogs);
                    MessagesStorage.getInstance().updateDialogsWithDeletedMessages(new ArrayList(), false, channel_id);
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        public void run() {
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.removeAllMessagesFromDialog, Long.valueOf(did), Boolean.valueOf(true));
                        }
                    });
                    if (!checkInvite) {
                        return;
                    }
                    if (newDialogType == 1) {
                        MessagesController.getInstance().checkChannelInviter(channel_id);
                    } else {
                        MessagesController.getInstance().generateJoinMessage(channel_id, false);
                    }
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
        });
    }

    public void putChannelViews(final SparseArray<SparseIntArray> channelViews, final boolean isChannel) {
        this.storageQueue.postRunnable(new Runnable() {
            public void run() {
                try {
                    MessagesStorage.this.database.beginTransaction();
                    SQLitePreparedStatement state = MessagesStorage.this.database.executeFast("UPDATE messages SET media = max((SELECT media FROM messages WHERE mid = ?), ?) WHERE mid = ?");
                    for (int a = 0; a < channelViews.size(); a++) {
                        int peer = channelViews.keyAt(a);
                        SparseIntArray messages = (SparseIntArray) channelViews.get(peer);
                        for (int b = 0; b < messages.size(); b++) {
                            int views = messages.get(messages.keyAt(b));
                            long messageId = (long) messages.keyAt(b);
                            if (isChannel) {
                                messageId |= ((long) (-peer)) << 32;
                            }
                            state.requery();
                            state.bindLong(1, messageId);
                            state.bindInteger(2, views);
                            state.bindLong(3, messageId);
                            state.step();
                        }
                    }
                    state.dispose();
                    MessagesStorage.this.database.commitTransaction();
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
        });
    }

    private void putMessagesInternal(ArrayList<Message> messages, boolean withTransaction, boolean doNotUpdateDialogDate, int downloadMask) {
        int a;
        Integer type;
        Integer count;
        if (withTransaction) {
            try {
                this.database.beginTransaction();
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
                return;
            }
        }
        HashMap<Long, Message> messagesMap = new HashMap();
        HashMap<Long, Message> messagesMapNotImportant = new HashMap();
        HashMap<Long, Integer> messagesCounts = new HashMap();
        HashMap<Long, Integer> messagesCountsNotImportant = new HashMap();
        HashMap<Integer, HashMap<Long, Integer>> mediaCounts = null;
        HashMap<Long, Message> botKeyboards = new HashMap();
        HashMap<Long, Long> messagesMediaIdsMap = null;
        StringBuilder messageMediaIds = null;
        HashMap<Long, Integer> mediaTypes = null;
        StringBuilder messageIds = new StringBuilder();
        HashMap<Long, Integer> dialogsReadMax = new HashMap();
        HashMap<Long, Long> messagesIdsMap = new HashMap();
        HashMap<Long, Long> messagesIdsMapNotImportant = new HashMap();
        SQLitePreparedStatement state = this.database.executeFast("REPLACE INTO messages VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, NULL, ?)");
        SQLitePreparedStatement state2 = null;
        SQLitePreparedStatement state3 = this.database.executeFast("REPLACE INTO randoms VALUES(?, ?)");
        SQLitePreparedStatement state4 = this.database.executeFast("REPLACE INTO download_queue VALUES(?, ?, ?, ?)");
        SQLitePreparedStatement state5 = this.database.executeFast("REPLACE INTO webpage_pending VALUES(?, ?)");
        for (a = 0; a < messages.size(); a++) {
            SQLiteCursor cursor;
            Message message = (Message) messages.get(a);
            long messageId = (long) message.id;
            if (message.dialog_id == 0) {
                if (message.to_id.user_id != 0) {
                    message.dialog_id = (long) message.to_id.user_id;
                } else if (message.to_id.chat_id != 0) {
                    message.dialog_id = (long) (-message.to_id.chat_id);
                } else {
                    message.dialog_id = (long) (-message.to_id.channel_id);
                }
            }
            if (message.to_id.channel_id != 0) {
                messageId |= ((long) message.to_id.channel_id) << 32;
            }
            if (((message.to_id.channel_id == 0 && MessageObject.isUnread(message)) || MessageObject.isContentUnread(message)) && !MessageObject.isOut(message)) {
                Integer currentMaxId = (Integer) dialogsReadMax.get(Long.valueOf(message.dialog_id));
                if (currentMaxId == null) {
                    cursor = this.database.queryFinalized("SELECT inbox_max FROM dialogs WHERE did = " + message.dialog_id, new Object[0]);
                    if (cursor.next()) {
                        currentMaxId = Integer.valueOf(cursor.intValue(0));
                    } else {
                        currentMaxId = Integer.valueOf(0);
                    }
                    cursor.dispose();
                    dialogsReadMax.put(Long.valueOf(message.dialog_id), currentMaxId);
                }
                if (message.id < 0 || currentMaxId.intValue() < message.id) {
                    if (messageIds.length() > 0) {
                        messageIds.append(",");
                    }
                    messageIds.append(messageId);
                    if (message.to_id.channel_id == 0 || MessageObject.isMegagroup(message) || MessageObject.isImportant(message)) {
                        messagesIdsMap.put(Long.valueOf(messageId), Long.valueOf(message.dialog_id));
                    } else if (message.to_id.channel_id != 0) {
                        messagesIdsMapNotImportant.put(Long.valueOf(messageId), Long.valueOf(message.dialog_id));
                    }
                }
            }
            if (SharedMediaQuery.canAddMessageToMedia(message)) {
                if (messageMediaIds == null) {
                    messageMediaIds = new StringBuilder();
                    messagesMediaIdsMap = new HashMap();
                    mediaTypes = new HashMap();
                }
                if (messageMediaIds.length() > 0) {
                    messageMediaIds.append(",");
                }
                messageMediaIds.append(messageId);
                messagesMediaIdsMap.put(Long.valueOf(messageId), Long.valueOf(message.dialog_id));
                mediaTypes.put(Long.valueOf(messageId), Integer.valueOf(SharedMediaQuery.getMediaType(message)));
            }
            if (message.reply_markup != null && (!message.reply_markup.selective || message.mentioned)) {
                Message oldMessage = (Message) botKeyboards.get(Long.valueOf(message.dialog_id));
                if (oldMessage == null || oldMessage.id < message.id) {
                    botKeyboards.put(Long.valueOf(message.dialog_id), message);
                }
            }
        }
        for (Entry<Long, Message> entry : botKeyboards.entrySet()) {
            BotQuery.putBotKeyboard(((Long) entry.getKey()).longValue(), (Message) entry.getValue());
        }
        if (messageMediaIds != null) {
            cursor = this.database.queryFinalized("SELECT mid FROM media_v2 WHERE mid IN(" + messageMediaIds.toString() + ")", new Object[0]);
            while (cursor.next()) {
                messagesMediaIdsMap.remove(Long.valueOf(cursor.longValue(0)));
            }
            cursor.dispose();
            mediaCounts = new HashMap();
            for (Entry<Long, Long> entry2 : messagesMediaIdsMap.entrySet()) {
                type = (Integer) mediaTypes.get(entry2.getKey());
                HashMap<Long, Integer> counts = (HashMap) mediaCounts.get(type);
                if (counts == null) {
                    counts = new HashMap();
                    count = Integer.valueOf(0);
                    mediaCounts.put(type, counts);
                } else {
                    count = (Integer) counts.get(entry2.getValue());
                }
                if (count == null) {
                    count = Integer.valueOf(0);
                }
                counts.put(entry2.getValue(), Integer.valueOf(count.intValue() + 1));
            }
        }
        if (messageIds.length() > 0) {
            cursor = this.database.queryFinalized("SELECT mid FROM messages WHERE mid IN(" + messageIds.toString() + ")", new Object[0]);
            while (cursor.next()) {
                messagesIdsMap.remove(Long.valueOf(cursor.longValue(0)));
                messagesIdsMapNotImportant.remove(Long.valueOf(cursor.longValue(0)));
            }
            cursor.dispose();
            for (Long dialog_id : messagesIdsMap.values()) {
                count = (Integer) messagesCounts.get(dialog_id);
                if (count == null) {
                    count = Integer.valueOf(0);
                }
                messagesCounts.put(dialog_id, Integer.valueOf(count.intValue() + 1));
            }
            for (Long dialog_id2 : messagesIdsMapNotImportant.values()) {
                count = (Integer) messagesCountsNotImportant.get(dialog_id2);
                if (count == null) {
                    count = Integer.valueOf(0);
                }
                messagesCountsNotImportant.put(dialog_id2, Integer.valueOf(count.intValue() + 1));
            }
        }
        int downloadMediaMask = 0;
        for (a = 0; a < messages.size(); a++) {
            message = (Message) messages.get(a);
            fixUnsupportedMedia(message);
            state.requery();
            messageId = (long) message.id;
            if (message.local_id != 0) {
                messageId = (long) message.local_id;
            }
            if (message.to_id.channel_id != 0) {
                messageId |= ((long) message.to_id.channel_id) << 32;
            }
            NativeByteBuffer data = new NativeByteBuffer(message.getObjectSize());
            message.serializeToStream(data);
            boolean updateDialog = true;
            if (!(message.action == null || !(message.action instanceof TL_messageEncryptedAction) || (message.action.encryptedAction instanceof TL_decryptedMessageActionSetMessageTTL) || (message.action.encryptedAction instanceof TL_decryptedMessageActionScreenshotMessages))) {
                updateDialog = false;
            }
            if (updateDialog) {
                Message lastMessage;
                if (message.to_id.channel_id == 0 || MessageObject.isMegagroup(message) || MessageObject.isImportant(message)) {
                    lastMessage = (Message) messagesMap.get(Long.valueOf(message.dialog_id));
                    if (lastMessage == null || message.date > lastMessage.date || ((message.id > 0 && lastMessage.id > 0 && message.id > lastMessage.id) || (message.id < 0 && lastMessage.id < 0 && message.id < lastMessage.id))) {
                        messagesMap.put(Long.valueOf(message.dialog_id), message);
                    }
                } else if (message.to_id.channel_id != 0) {
                    lastMessage = (Message) messagesMapNotImportant.get(Long.valueOf(message.dialog_id));
                    if (lastMessage == null || message.date > lastMessage.date || ((message.id > 0 && lastMessage.id > 0 && message.id > lastMessage.id) || (message.id < 0 && lastMessage.id < 0 && message.id < lastMessage.id))) {
                        messagesMapNotImportant.put(Long.valueOf(message.dialog_id), message);
                    }
                }
            }
            state.bindLong(1, messageId);
            state.bindLong(2, message.dialog_id);
            state.bindInteger(3, MessageObject.getUnreadFlags(message));
            state.bindInteger(4, message.send_state);
            state.bindInteger(5, message.date);
            state.bindByteBuffer(6, data);
            state.bindInteger(7, MessageObject.isOut(message) ? 1 : 0);
            state.bindInteger(8, message.ttl);
            if ((message.flags & 1024) != 0) {
                state.bindInteger(9, message.views);
            } else {
                state.bindInteger(9, getMessageMediaType(message));
            }
            state.bindInteger(10, MessageObject.isImportant(message) ? 1 : 0);
            state.step();
            if (message.random_id != 0) {
                state3.requery();
                state3.bindLong(1, message.random_id);
                state3.bindLong(2, messageId);
                state3.step();
            }
            if (SharedMediaQuery.canAddMessageToMedia(message)) {
                if (state2 == null) {
                    state2 = this.database.executeFast("REPLACE INTO media_v2 VALUES(?, ?, ?, ?, ?)");
                }
                state2.requery();
                state2.bindLong(1, messageId);
                state2.bindLong(2, message.dialog_id);
                state2.bindInteger(3, message.date);
                state2.bindInteger(4, SharedMediaQuery.getMediaType(message));
                state2.bindByteBuffer(5, data);
                state2.step();
            }
            if ((message.media instanceof TL_messageMediaWebPage) && (message.media.webpage instanceof TL_webPagePending)) {
                state5.requery();
                state5.bindLong(1, message.media.webpage.id);
                state5.bindLong(2, messageId);
                state5.step();
            }
            data.reuse();
            if ((message.to_id.channel_id == 0 || MessageObject.isImportant(message)) && message.date >= ConnectionsManager.getInstance().getCurrentTime() - 3600 && downloadMask != 0 && ((message.media instanceof TL_messageMediaAudio) || (message.media instanceof TL_messageMediaPhoto) || (message.media instanceof TL_messageMediaVideo) || (message.media instanceof TL_messageMediaDocument))) {
                int type2 = 0;
                long id = 0;
                TLObject object = null;
                if (message.media instanceof TL_messageMediaAudio) {
                    if ((downloadMask & 2) != 0 && message.media.audio.size < 5242880) {
                        id = message.media.audio.id;
                        type2 = 2;
                        object = message.media.audio;
                    }
                } else if (message.media instanceof TL_messageMediaPhoto) {
                    if ((downloadMask & 1) != 0) {
                        TLObject photoSize = FileLoader.getClosestPhotoSizeWithSize(message.media.photo.sizes, AndroidUtilities.getPhotoSize());
                        if (photoSize != null) {
                            id = message.media.photo.id;
                            type2 = 1;
                            object = photoSize;
                        }
                    }
                } else if (message.media instanceof TL_messageMediaVideo) {
                    if ((downloadMask & 4) != 0) {
                        id = message.media.video.id;
                        type2 = 4;
                        object = message.media.video;
                    }
                } else if ((message.media instanceof TL_messageMediaDocument) && (downloadMask & 8) != 0) {
                    id = message.media.document.id;
                    type2 = 8;
                    object = message.media.document;
                }
                if (object != null) {
                    downloadMediaMask |= type2;
                    state4.requery();
                    data = new NativeByteBuffer(object.getObjectSize());
                    object.serializeToStream(data);
                    state4.bindLong(1, id);
                    state4.bindInteger(2, type2);
                    state4.bindInteger(3, message.date);
                    state4.bindByteBuffer(4, data);
                    state4.step();
                    data.reuse();
                }
            }
        }
        state.dispose();
        if (state2 != null) {
            state2.dispose();
        }
        state3.dispose();
        state4.dispose();
        state5.dispose();
        state = this.database.executeFast("REPLACE INTO dialogs VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        HashMap<Long, Message> dids = new HashMap();
        dids.putAll(messagesMap);
        dids.putAll(messagesMapNotImportant);
        for (Entry<Long, Message> pair : dids.entrySet()) {
            Long key = (Long) pair.getKey();
            if (key.longValue() != 0) {
                long messageIdNotImportant;
                message = (Message) messagesMap.get(key);
                Message messageNotImportant = (Message) messagesMapNotImportant.get(key);
                int channelId = 0;
                if (message != null) {
                    channelId = message.to_id.channel_id;
                }
                if (messageNotImportant != null) {
                    channelId = messageNotImportant.to_id.channel_id;
                }
                cursor = this.database.queryFinalized("SELECT date, unread_count, last_mid_i, unread_count_i, pts, date_i, last_mid, inbox_max FROM dialogs WHERE did = " + key, new Object[0]);
                int dialog_date = 0;
                int last_mid = 0;
                int old_unread_count = 0;
                int last_mid_i = 0;
                int old_unread_count_i = 0;
                int pts = channelId != 0 ? 1 : 0;
                int dialog_date_i = 0;
                int inbox_max = 0;
                if (cursor.next()) {
                    dialog_date = cursor.intValue(0);
                    old_unread_count = cursor.intValue(1);
                    last_mid_i = cursor.intValue(2);
                    old_unread_count_i = cursor.intValue(3);
                    pts = cursor.intValue(4);
                    dialog_date_i = cursor.intValue(5);
                    last_mid = cursor.intValue(6);
                    inbox_max = cursor.intValue(7);
                } else if (channelId != 0) {
                    MessagesController.getInstance().checkChannelInviter(channelId);
                }
                cursor.dispose();
                Integer unread_count = (Integer) messagesCounts.get(key);
                if (unread_count == null) {
                    unread_count = Integer.valueOf(0);
                } else {
                    messagesCounts.put(key, Integer.valueOf(unread_count.intValue() + old_unread_count));
                }
                Integer unread_count_i = (Integer) messagesCountsNotImportant.get(key);
                if (unread_count_i == null) {
                    unread_count_i = Integer.valueOf(0);
                } else {
                    messagesCountsNotImportant.put(key, Integer.valueOf(unread_count_i.intValue() + old_unread_count_i));
                }
                messageId = message != null ? (long) message.id : (long) last_mid;
                if (!(message == null || message.local_id == 0)) {
                    messageId = (long) message.local_id;
                }
                if (messageNotImportant != null) {
                    messageIdNotImportant = (long) messageNotImportant.id;
                } else {
                    messageIdNotImportant = (long) last_mid_i;
                }
                if (!(messageNotImportant == null || messageNotImportant.local_id == 0)) {
                    messageIdNotImportant = (long) messageNotImportant.local_id;
                }
                if (channelId != 0) {
                    messageId |= ((long) channelId) << 32;
                    messageIdNotImportant |= ((long) channelId) << 32;
                }
                state.requery();
                state.bindLong(1, key.longValue());
                if (message == null || (doNotUpdateDialogDate && dialog_date != 0)) {
                    state.bindInteger(2, dialog_date);
                } else {
                    state.bindInteger(2, message.date);
                }
                state.bindInteger(3, unread_count.intValue() + old_unread_count);
                state.bindLong(4, messageId);
                state.bindInteger(5, inbox_max);
                state.bindInteger(6, 0);
                state.bindLong(7, messageIdNotImportant);
                state.bindInteger(8, unread_count_i.intValue() + old_unread_count_i);
                state.bindInteger(9, pts);
                if (messageNotImportant == null || (doNotUpdateDialogDate && dialog_date != 0)) {
                    state.bindInteger(10, dialog_date_i);
                } else {
                    state.bindInteger(10, messageNotImportant.date);
                }
                state.step();
            }
        }
        state.dispose();
        if (mediaCounts != null) {
            state3 = this.database.executeFast("REPLACE INTO media_counts_v2 VALUES(?, ?, ?)");
            for (Entry<Integer, HashMap<Long, Integer>> counts2 : mediaCounts.entrySet()) {
                type = (Integer) counts2.getKey();
                for (Entry<Long, Integer> pair2 : ((HashMap) counts2.getValue()).entrySet()) {
                    long uid = ((Long) pair2.getKey()).longValue();
                    int lower_part = (int) uid;
                    int count2 = -1;
                    cursor = this.database.queryFinalized(String.format(Locale.US, "SELECT count FROM media_counts_v2 WHERE uid = %d AND type = %d LIMIT 1", new Object[]{Long.valueOf(uid), type}), new Object[0]);
                    if (cursor.next()) {
                        count2 = cursor.intValue(0);
                    }
                    cursor.dispose();
                    if (count2 != -1) {
                        state3.requery();
                        count2 += ((Integer) pair2.getValue()).intValue();
                        state3.bindLong(1, uid);
                        state3.bindInteger(2, type.intValue());
                        state3.bindInteger(3, count2);
                        state3.step();
                    }
                }
            }
            state3.dispose();
        }
        if (withTransaction) {
            this.database.commitTransaction();
        }
        MessagesController.getInstance().processDialogsUpdateRead(messagesCounts);
        if (downloadMediaMask != 0) {
            final int i = downloadMediaMask;
            AndroidUtilities.runOnUIThread(new Runnable() {
                public void run() {
                    MediaController.getInstance().newDownloadObjectsAvailable(i);
                }
            });
        }
    }

    public void putMessages(ArrayList<Message> messages, boolean withTransaction, boolean useQueue, boolean doNotUpdateDialogDate, int downloadMask) {
        if (messages.size() != 0) {
            if (useQueue) {
                final ArrayList<Message> arrayList = messages;
                final boolean z = withTransaction;
                final boolean z2 = doNotUpdateDialogDate;
                final int i = downloadMask;
                this.storageQueue.postRunnable(new Runnable() {
                    public void run() {
                        MessagesStorage.this.putMessagesInternal(arrayList, z, z2, i);
                    }
                });
                return;
            }
            putMessagesInternal(messages, withTransaction, doNotUpdateDialogDate, downloadMask);
        }
    }

    public void markMessageAsSendError(final Message message) {
        this.storageQueue.postRunnable(new Runnable() {
            public void run() {
                try {
                    long messageId = (long) message.id;
                    if (message.to_id.channel_id != 0) {
                        messageId |= ((long) message.to_id.channel_id) << 32;
                    }
                    MessagesStorage.this.database.executeFast("UPDATE messages SET send_state = 2 WHERE mid = " + messageId).stepThis().dispose();
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
        });
    }

    public void setMessageSeq(final int mid, final int seq_in, final int seq_out) {
        this.storageQueue.postRunnable(new Runnable() {
            public void run() {
                try {
                    SQLitePreparedStatement state = MessagesStorage.this.database.executeFast("REPLACE INTO messages_seq VALUES(?, ?, ?)");
                    state.requery();
                    state.bindInteger(1, mid);
                    state.bindInteger(2, seq_in);
                    state.bindInteger(3, seq_out);
                    state.step();
                    state.dispose();
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
        });
    }

    private long[] updateMessageStateAndIdInternal(long random_id, Integer _oldId, int newId, int date, int channelId) {
        SQLitePreparedStatement state;
        SQLiteCursor cursor = null;
        long newMessageId = (long) newId;
        if (_oldId == null) {
            try {
                cursor = this.database.queryFinalized(String.format(Locale.US, "SELECT mid FROM randoms WHERE random_id = %d LIMIT 1", new Object[]{Long.valueOf(random_id)}), new Object[0]);
                if (cursor.next()) {
                    _oldId = Integer.valueOf(cursor.intValue(0));
                }
                if (cursor != null) {
                    cursor.dispose();
                }
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
                if (cursor != null) {
                    cursor.dispose();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.dispose();
                }
            }
            if (_oldId == null) {
                return null;
            }
        }
        long oldMessageId = (long) _oldId.intValue();
        if (channelId != 0) {
            oldMessageId |= ((long) channelId) << 32;
            newMessageId |= ((long) channelId) << 32;
        }
        long did = 0;
        try {
            cursor = this.database.queryFinalized(String.format(Locale.US, "SELECT uid FROM messages WHERE mid = %d LIMIT 1", new Object[]{Long.valueOf(oldMessageId)}), new Object[0]);
            if (cursor.next()) {
                did = cursor.longValue(0);
            }
            if (cursor != null) {
                cursor.dispose();
            }
        } catch (Throwable e2) {
            FileLog.m611e("tmessages", e2);
            if (cursor != null) {
                cursor.dispose();
            }
        } catch (Throwable th2) {
            if (cursor != null) {
                cursor.dispose();
            }
        }
        if (did == 0) {
            return null;
        }
        if (oldMessageId != newMessageId || date == 0) {
            state = null;
            try {
                state = this.database.executeFast("UPDATE messages SET mid = ?, send_state = 0 WHERE mid = ?");
                state.bindLong(1, newMessageId);
                state.bindLong(2, oldMessageId);
                state.step();
                if (state != null) {
                    state.dispose();
                    state = null;
                }
            } catch (Throwable e22) {
                try {
                    this.database.executeFast(String.format(Locale.US, "DELETE FROM messages WHERE mid = %d", new Object[]{Long.valueOf(oldMessageId)})).stepThis().dispose();
                    this.database.executeFast(String.format(Locale.US, "DELETE FROM messages_seq WHERE mid = %d", new Object[]{Long.valueOf(oldMessageId)})).stepThis().dispose();
                } catch (Throwable e23) {
                    FileLog.m611e("tmessages", e23);
                }
                FileLog.m611e("tmessages", e22);
                if (state != null) {
                    state.dispose();
                    state = null;
                }
            } catch (Throwable th3) {
                if (state != null) {
                    state.dispose();
                }
            }
            try {
                state = this.database.executeFast("UPDATE media_v2 SET mid = ? WHERE mid = ?");
                state.bindLong(1, newMessageId);
                state.bindLong(2, oldMessageId);
                state.step();
                if (state != null) {
                    state.dispose();
                    state = null;
                }
            } catch (Throwable e222) {
                try {
                    this.database.executeFast(String.format(Locale.US, "DELETE FROM media_v2 WHERE mid = %d", new Object[]{Long.valueOf(oldMessageId)})).stepThis().dispose();
                } catch (Throwable e232) {
                    FileLog.m611e("tmessages", e232);
                }
                FileLog.m611e("tmessages", e222);
                if (state != null) {
                    state.dispose();
                    state = null;
                }
            } catch (Throwable th4) {
                if (state != null) {
                    state.dispose();
                }
            }
            try {
                state = this.database.executeFast("UPDATE dialogs SET last_mid = ? WHERE last_mid = ?");
                state.bindLong(1, newMessageId);
                state.bindLong(2, oldMessageId);
                state.step();
                if (state != null) {
                    state.dispose();
                }
            } catch (Throwable e2222) {
                FileLog.m611e("tmessages", e2222);
                if (state != null) {
                    state.dispose();
                }
            } catch (Throwable th5) {
                if (state != null) {
                    state.dispose();
                }
            }
            return new long[]{did, (long) _oldId.intValue()};
        }
        state = null;
        try {
            state = this.database.executeFast("UPDATE messages SET send_state = 0, date = ? WHERE mid = ?");
            state.bindInteger(1, date);
            state.bindLong(2, newMessageId);
            state.step();
            if (state != null) {
                state.dispose();
            }
        } catch (Throwable e22222) {
            FileLog.m611e("tmessages", e22222);
            if (state != null) {
                state.dispose();
            }
        } catch (Throwable th6) {
            if (state != null) {
                state.dispose();
            }
        }
        return new long[]{did, (long) newId};
    }

    public long[] updateMessageStateAndId(long random_id, Integer _oldId, int newId, int date, boolean useQueue, int channelId) {
        if (!useQueue) {
            return updateMessageStateAndIdInternal(random_id, _oldId, newId, date, channelId);
        }
        final long j = random_id;
        final Integer num = _oldId;
        final int i = newId;
        final int i2 = date;
        final int i3 = channelId;
        this.storageQueue.postRunnable(new Runnable() {
            public void run() {
                MessagesStorage.this.updateMessageStateAndIdInternal(j, num, i, i2, i3);
            }
        });
        return null;
    }

    private void updateUsersInternal(ArrayList<User> users, boolean onlyStatus, boolean withTransaction) {
        if (Thread.currentThread().getId() != this.storageQueue.getId()) {
            throw new RuntimeException("wrong db thread");
        } else if (onlyStatus) {
            if (withTransaction) {
                try {
                    this.database.beginTransaction();
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                    return;
                }
            }
            SQLitePreparedStatement state = this.database.executeFast("UPDATE users SET status = ? WHERE uid = ?");
            i$ = users.iterator();
            while (i$.hasNext()) {
                user = (User) i$.next();
                state.requery();
                if (user.status != null) {
                    state.bindInteger(1, user.status.expires);
                } else {
                    state.bindInteger(1, 0);
                }
                state.bindInteger(2, user.id);
                state.step();
            }
            state.dispose();
            if (withTransaction) {
                this.database.commitTransaction();
            }
        } else {
            StringBuilder ids = new StringBuilder();
            HashMap<Integer, User> usersDict = new HashMap();
            i$ = users.iterator();
            while (i$.hasNext()) {
                user = (User) i$.next();
                if (ids.length() != 0) {
                    ids.append(",");
                }
                ids.append(user.id);
                usersDict.put(Integer.valueOf(user.id), user);
            }
            ArrayList<User> loadedUsers = new ArrayList();
            getUsersInternal(ids.toString(), loadedUsers);
            i$ = loadedUsers.iterator();
            while (i$.hasNext()) {
                user = (User) i$.next();
                User updateUser = (User) usersDict.get(Integer.valueOf(user.id));
                if (updateUser != null) {
                    if (updateUser.first_name != null && updateUser.last_name != null) {
                        if (!UserObject.isContact(user)) {
                            user.first_name = updateUser.first_name;
                            user.last_name = updateUser.last_name;
                        }
                        user.username = updateUser.username;
                    } else if (updateUser.photo != null) {
                        user.photo = updateUser.photo;
                    } else if (updateUser.phone != null) {
                        user.phone = updateUser.phone;
                    }
                }
            }
            if (!loadedUsers.isEmpty()) {
                if (withTransaction) {
                    this.database.beginTransaction();
                }
                putUsersInternal(loadedUsers);
                if (withTransaction) {
                    this.database.commitTransaction();
                }
            }
        }
    }

    public void updateUsers(final ArrayList<User> users, final boolean onlyStatus, final boolean withTransaction, boolean useQueue) {
        if (!users.isEmpty()) {
            if (useQueue) {
                this.storageQueue.postRunnable(new Runnable() {
                    public void run() {
                        MessagesStorage.this.updateUsersInternal(users, onlyStatus, withTransaction);
                    }
                });
            } else {
                updateUsersInternal(users, onlyStatus, withTransaction);
            }
        }
    }

    private void markMessagesAsReadInternal(SparseArray<Long> inbox, SparseIntArray outbox, HashMap<Integer, Integer> encryptedMessages) {
        int b;
        if (inbox != null) {
            b = 0;
            while (b < inbox.size()) {
                try {
                    long messageId = ((Long) inbox.get(inbox.keyAt(b))).longValue();
                    this.database.executeFast(String.format(Locale.US, "UPDATE messages SET read_state = read_state | 1 WHERE uid = %d AND mid > 0 AND mid <= %d AND read_state IN(0,2) AND out = 0", new Object[]{Integer.valueOf(key), Long.valueOf(messageId)})).stepThis().dispose();
                    b++;
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                    return;
                }
            }
        }
        if (outbox != null) {
            for (b = 0; b < outbox.size(); b++) {
                int messageId2 = outbox.get(outbox.keyAt(b));
                this.database.executeFast(String.format(Locale.US, "UPDATE messages SET read_state = read_state | 1 WHERE uid = %d AND mid > 0 AND mid <= %d AND read_state IN(0,2) AND out = 1", new Object[]{Integer.valueOf(key), Integer.valueOf(messageId2)})).stepThis().dispose();
            }
        }
        if (encryptedMessages != null && !encryptedMessages.isEmpty()) {
            for (Entry<Integer, Integer> entry : encryptedMessages.entrySet()) {
                long dialog_id = ((long) ((Integer) entry.getKey()).intValue()) << 32;
                int max_date = ((Integer) entry.getValue()).intValue();
                SQLitePreparedStatement state = this.database.executeFast("UPDATE messages SET read_state = read_state | 1 WHERE uid = ? AND date <= ? AND read_state IN(0,2) AND out = 1");
                state.requery();
                state.bindLong(1, dialog_id);
                state.bindInteger(2, max_date);
                state.step();
                state.dispose();
            }
        }
    }

    public void markMessagesContentAsRead(final ArrayList<Long> mids) {
        if (mids != null && !mids.isEmpty()) {
            this.storageQueue.postRunnable(new Runnable() {
                public void run() {
                    try {
                        MessagesStorage.this.database.executeFast(String.format(Locale.US, "UPDATE messages SET read_state = read_state | 2 WHERE mid IN (%s)", new Object[]{TextUtils.join(",", mids)})).stepThis().dispose();
                    } catch (Throwable e) {
                        FileLog.m611e("tmessages", e);
                    }
                }
            });
        }
    }

    public void markMessagesAsRead(final SparseArray<Long> inbox, final SparseIntArray outbox, final HashMap<Integer, Integer> encryptedMessages, boolean useQueue) {
        if (useQueue) {
            this.storageQueue.postRunnable(new Runnable() {
                public void run() {
                    MessagesStorage.this.markMessagesAsReadInternal(inbox, outbox, encryptedMessages);
                }
            });
        } else {
            markMessagesAsReadInternal(inbox, outbox, encryptedMessages);
        }
    }

    public void markMessagesAsDeletedByRandoms(final ArrayList<Long> messages) {
        if (!messages.isEmpty()) {
            this.storageQueue.postRunnable(new Runnable() {
                public void run() {
                    try {
                        String ids = TextUtils.join(",", messages);
                        SQLiteCursor cursor = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT mid FROM randoms WHERE random_id IN(%s)", new Object[]{ids}), new Object[0]);
                        final ArrayList<Integer> mids = new ArrayList();
                        while (cursor.next()) {
                            mids.add(Integer.valueOf(cursor.intValue(0)));
                        }
                        cursor.dispose();
                        if (!mids.isEmpty()) {
                            AndroidUtilities.runOnUIThread(new Runnable() {
                                public void run() {
                                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.messagesDeleted, mids, Integer.valueOf(0));
                                }
                            });
                            MessagesStorage.getInstance().updateDialogsWithReadMessagesInternal(mids, null);
                            MessagesStorage.getInstance().markMessagesAsDeletedInternal(mids, 0);
                            MessagesStorage.getInstance().updateDialogsWithDeletedMessagesInternal(mids, 0);
                        }
                    } catch (Throwable e) {
                        FileLog.m611e("tmessages", e);
                    }
                }
            });
        }
    }

    private void markMessagesAsDeletedInternal(ArrayList<Integer> messages, int channelId) {
        String ids;
        int unread_count = 0;
        if (channelId != 0) {
            try {
                StringBuilder builder = new StringBuilder(messages.size());
                for (int a = 0; a < messages.size(); a++) {
                    long messageId = ((long) ((Integer) messages.get(a)).intValue()) | (((long) channelId) << 32);
                    if (builder.length() > 0) {
                        builder.append(',');
                    }
                    builder.append(messageId);
                }
                ids = builder.toString();
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
                return;
            }
        }
        ids = TextUtils.join(",", messages);
        SQLiteCursor cursor = this.database.queryFinalized(String.format(Locale.US, "SELECT uid, data, read_state FROM messages WHERE mid IN(%s)", new Object[]{ids}), new Object[0]);
        ArrayList<File> filesToDelete = new ArrayList();
        while (cursor.next()) {
            long did;
            try {
                did = cursor.longValue(0);
                if (channelId != 0 && cursor.intValue(2) == 0) {
                    unread_count++;
                }
                if (((int) did) == 0) {
                    NativeByteBuffer data = new NativeByteBuffer(cursor.byteArrayLength(1));
                    if (!(data == null || cursor.byteBufferValue(1, data) == 0)) {
                        Message message = Message.TLdeserialize(data, data.readInt32(false), false);
                        if (!(message == null || message.media == null)) {
                            File file;
                            if (message.media instanceof TL_messageMediaAudio) {
                                file = FileLoader.getPathToAttach(message.media.audio);
                                if (file != null && file.toString().length() > 0) {
                                    filesToDelete.add(file);
                                }
                            } else if (message.media instanceof TL_messageMediaPhoto) {
                                Iterator i$ = message.media.photo.sizes.iterator();
                                while (i$.hasNext()) {
                                    file = FileLoader.getPathToAttach((PhotoSize) i$.next());
                                    if (file != null && file.toString().length() > 0) {
                                        filesToDelete.add(file);
                                    }
                                }
                            } else if (message.media instanceof TL_messageMediaVideo) {
                                file = FileLoader.getPathToAttach(message.media.video);
                                if (file != null && file.toString().length() > 0) {
                                    filesToDelete.add(file);
                                }
                                file = FileLoader.getPathToAttach(message.media.video.thumb);
                                if (file != null && file.toString().length() > 0) {
                                    filesToDelete.add(file);
                                }
                            } else if (message.media instanceof TL_messageMediaDocument) {
                                file = FileLoader.getPathToAttach(message.media.document);
                                if (file != null && file.toString().length() > 0) {
                                    filesToDelete.add(file);
                                }
                                file = FileLoader.getPathToAttach(message.media.document.thumb);
                                if (file != null && file.toString().length() > 0) {
                                    filesToDelete.add(file);
                                }
                            }
                        }
                    }
                    data.reuse();
                }
            } catch (Throwable e2) {
                FileLog.m611e("tmessages", e2);
            }
        }
        cursor.dispose();
        FileLoader.getInstance().deleteFiles(filesToDelete, 0);
        if (!(channelId == 0 || unread_count == 0)) {
            did = (long) (-channelId);
            SQLitePreparedStatement state = this.database.executeFast("UPDATE dialogs SET unread_count = ((SELECT unread_count FROM dialogs WHERE did = ?) - ?) WHERE did = ?");
            state.requery();
            state.bindLong(1, did);
            state.bindInteger(2, unread_count);
            state.bindLong(3, did);
            state.step();
            state.dispose();
        }
        this.database.executeFast(String.format(Locale.US, "DELETE FROM messages WHERE mid IN(%s)", new Object[]{ids})).stepThis().dispose();
        this.database.executeFast(String.format(Locale.US, "DELETE FROM bot_keyboard WHERE mid IN(%s)", new Object[]{ids})).stepThis().dispose();
        this.database.executeFast(String.format(Locale.US, "DELETE FROM messages_seq WHERE mid IN(%s)", new Object[]{ids})).stepThis().dispose();
        this.database.executeFast(String.format(Locale.US, "DELETE FROM media_v2 WHERE mid IN(%s)", new Object[]{ids})).stepThis().dispose();
        this.database.executeFast("DELETE FROM media_counts_v2 WHERE 1").stepThis().dispose();
        BotQuery.clearBotKeyboard(0, messages);
    }

    private void updateDialogsWithDeletedMessagesInternal(ArrayList<Integer> messages, int channelId) {
        if (Thread.currentThread().getId() != this.storageQueue.getId()) {
            throw new RuntimeException("wrong db thread");
        }
        try {
            String ids;
            SQLiteCursor cursor;
            if (messages.isEmpty()) {
                ids = "" + (-channelId);
            } else {
                SQLitePreparedStatement state;
                ArrayList<Long> dialogsToUpdate = new ArrayList();
                if (channelId != 0) {
                    dialogsToUpdate.add(Long.valueOf((long) (-channelId)));
                    state = this.database.executeFast("UPDATE dialogs SET last_mid = (SELECT mid FROM messages WHERE uid = ? AND date = (SELECT MAX(date) FROM messages WHERE uid = ? )) WHERE did = ?");
                } else {
                    ids = TextUtils.join(",", messages);
                    cursor = this.database.queryFinalized(String.format(Locale.US, "SELECT did FROM dialogs WHERE last_mid IN(%s)", new Object[]{ids}), new Object[0]);
                    while (cursor.next()) {
                        dialogsToUpdate.add(Long.valueOf(cursor.longValue(0)));
                    }
                    cursor.dispose();
                    state = this.database.executeFast("UPDATE dialogs SET unread_count = 0, unread_count_i = 0, last_mid = (SELECT mid FROM messages WHERE uid = ? AND date = (SELECT MAX(date) FROM messages WHERE uid = ? AND date != 0)) WHERE did = ?");
                }
                this.database.beginTransaction();
                for (int a = 0; a < dialogsToUpdate.size(); a++) {
                    long did = ((Long) dialogsToUpdate.get(a)).longValue();
                    state.requery();
                    state.bindLong(1, did);
                    state.bindLong(2, did);
                    state.bindLong(3, did);
                    state.step();
                }
                state.dispose();
                this.database.commitTransaction();
                ids = TextUtils.join(",", dialogsToUpdate);
            }
            messages_Dialogs dialogs = new messages_Dialogs();
            ArrayList<EncryptedChat> encryptedChats = new ArrayList();
            ArrayList<Integer> usersToLoad = new ArrayList();
            ArrayList<Integer> chatsToLoad = new ArrayList();
            ArrayList<Integer> encryptedToLoad = new ArrayList();
            cursor = this.database.queryFinalized(String.format(Locale.US, "SELECT d.did, d.last_mid, d.unread_count, d.date, m.data, m.read_state, m.mid, m.send_state, m.date, d.last_mid_i, d.unread_count_i, d.pts, d.inbox_max FROM dialogs as d LEFT JOIN messages as m ON d.last_mid = m.mid WHERE d.did IN(%s)", new Object[]{ids}), new Object[0]);
            while (cursor.next()) {
                Dialog dialog;
                if (channelId == 0) {
                    dialog = new TL_dialog();
                } else {
                    dialog = new TL_dialogChannel();
                }
                dialog.id = cursor.longValue(0);
                dialog.top_message = cursor.intValue(1);
                dialog.read_inbox_max_id = cursor.intValue(13);
                dialog.unread_count = cursor.intValue(2);
                dialog.last_message_date = cursor.intValue(3);
                dialog.pts = cursor.intValue(11);
                dialog.top_not_important_message = cursor.intValue(9);
                dialog.unread_not_important_count = cursor.intValue(10);
                dialogs.dialogs.add(dialog);
                NativeByteBuffer data = new NativeByteBuffer(cursor.byteArrayLength(4));
                if (!(data == null || cursor.byteBufferValue(4, data) == 0)) {
                    Message message = Message.TLdeserialize(data, data.readInt32(false), false);
                    MessageObject.setUnreadFlags(message, cursor.intValue(5));
                    message.id = cursor.intValue(6);
                    message.send_state = cursor.intValue(7);
                    int date = cursor.intValue(8);
                    if (date != 0) {
                        dialog.last_message_date = date;
                    }
                    message.dialog_id = dialog.id;
                    dialogs.messages.add(message);
                    addUsersAndChatsFromMessage(message, usersToLoad, chatsToLoad);
                }
                data.reuse();
                int lower_id = (int) dialog.id;
                int high_id = (int) (dialog.id >> 32);
                if (lower_id != 0) {
                    if (high_id == 1) {
                        if (!chatsToLoad.contains(Integer.valueOf(lower_id))) {
                            chatsToLoad.add(Integer.valueOf(lower_id));
                        }
                    } else if (lower_id > 0) {
                        if (!usersToLoad.contains(Integer.valueOf(lower_id))) {
                            usersToLoad.add(Integer.valueOf(lower_id));
                        }
                    } else if (!chatsToLoad.contains(Integer.valueOf(-lower_id))) {
                        chatsToLoad.add(Integer.valueOf(-lower_id));
                    }
                } else if (!encryptedToLoad.contains(Integer.valueOf(high_id))) {
                    encryptedToLoad.add(Integer.valueOf(high_id));
                }
            }
            cursor.dispose();
            if (!encryptedToLoad.isEmpty()) {
                getEncryptedChatsInternal(TextUtils.join(",", encryptedToLoad), encryptedChats, usersToLoad);
            }
            if (!chatsToLoad.isEmpty()) {
                getChatsInternal(TextUtils.join(",", chatsToLoad), dialogs.chats);
            }
            if (!usersToLoad.isEmpty()) {
                getUsersInternal(TextUtils.join(",", usersToLoad), dialogs.users);
            }
            if (!dialogs.dialogs.isEmpty() || !encryptedChats.isEmpty()) {
                MessagesController.getInstance().processDialogsUpdate(dialogs, encryptedChats);
            }
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
        }
    }

    public void updateDialogsWithDeletedMessages(final ArrayList<Integer> messages, boolean useQueue, final int channelId) {
        if (!messages.isEmpty() || channelId != 0) {
            if (useQueue) {
                this.storageQueue.postRunnable(new Runnable() {
                    public void run() {
                        MessagesStorage.this.updateDialogsWithDeletedMessagesInternal(messages, channelId);
                    }
                });
            } else {
                updateDialogsWithDeletedMessagesInternal(messages, channelId);
            }
        }
    }

    public void markMessagesAsDeleted(final ArrayList<Integer> messages, boolean useQueue, final int channelId) {
        if (!messages.isEmpty()) {
            if (useQueue) {
                this.storageQueue.postRunnable(new Runnable() {
                    public void run() {
                        MessagesStorage.this.markMessagesAsDeletedInternal(messages, channelId);
                    }
                });
            } else {
                markMessagesAsDeletedInternal(messages, channelId);
            }
        }
    }

    private void fixUnsupportedMedia(Message message) {
        if (message != null) {
            if (message.media instanceof TL_messageMediaUnsupported_old) {
                if (message.media.bytes.length == 0) {
                    message.media.bytes = new byte[1];
                    message.media.bytes[0] = (byte) 43;
                }
            } else if (message.media instanceof TL_messageMediaUnsupported) {
                message.media = new TL_messageMediaUnsupported_old();
                message.media.bytes = new byte[1];
                message.media.bytes[0] = (byte) 43;
                message.flags |= 512;
            }
        }
    }

    private void doneHolesInTable(String table, long did, int max_id) throws Exception {
        if (max_id == 0) {
            this.database.executeFast(String.format(Locale.US, "DELETE FROM " + table + " WHERE uid = %d", new Object[]{Long.valueOf(did)})).stepThis().dispose();
        } else {
            this.database.executeFast(String.format(Locale.US, "DELETE FROM " + table + " WHERE uid = %d AND start = 0", new Object[]{Long.valueOf(did)})).stepThis().dispose();
        }
        SQLitePreparedStatement state = this.database.executeFast("REPLACE INTO " + table + " VALUES(?, ?, ?)");
        state.requery();
        state.bindLong(1, did);
        state.bindInteger(2, 1);
        state.bindInteger(3, 1);
        state.step();
        state.dispose();
    }

    public void doneHolesInMedia(long did, int max_id, int type) throws Exception {
        SQLitePreparedStatement state;
        if (type == -1) {
            if (max_id == 0) {
                this.database.executeFast(String.format(Locale.US, "DELETE FROM media_holes_v2 WHERE uid = %d", new Object[]{Long.valueOf(did)})).stepThis().dispose();
            } else {
                this.database.executeFast(String.format(Locale.US, "DELETE FROM media_holes_v2 WHERE uid = %d AND start = 0", new Object[]{Long.valueOf(did)})).stepThis().dispose();
            }
            state = this.database.executeFast("REPLACE INTO media_holes_v2 VALUES(?, ?, ?, ?)");
            for (int a = 0; a < 5; a++) {
                state.requery();
                state.bindLong(1, did);
                state.bindInteger(2, a);
                state.bindInteger(3, 1);
                state.bindInteger(4, 1);
                state.step();
            }
            state.dispose();
            return;
        }
        if (max_id == 0) {
            this.database.executeFast(String.format(Locale.US, "DELETE FROM media_holes_v2 WHERE uid = %d AND type = %d", new Object[]{Long.valueOf(did), Integer.valueOf(type)})).stepThis().dispose();
        } else {
            this.database.executeFast(String.format(Locale.US, "DELETE FROM media_holes_v2 WHERE uid = %d AND type = %d AND start = 0", new Object[]{Long.valueOf(did), Integer.valueOf(type)})).stepThis().dispose();
        }
        state = this.database.executeFast("REPLACE INTO media_holes_v2 VALUES(?, ?, ?, ?)");
        state.requery();
        state.bindLong(1, did);
        state.bindInteger(2, type);
        state.bindInteger(3, 1);
        state.bindInteger(4, 1);
        state.step();
        state.dispose();
    }

    public void closeHolesInMedia(long did, int minId, int maxId, int type) throws Exception {
        SQLiteCursor cursor;
        if (type < 0) {
            cursor = this.database.queryFinalized(String.format(Locale.US, "SELECT type, start, end FROM media_holes_v2 WHERE uid = %d AND type >= 0 AND ((end >= %d AND end <= %d) OR (start >= %d AND start <= %d) OR (start >= %d AND end <= %d) OR (start <= %d AND end >= %d))", new Object[]{Long.valueOf(did), Integer.valueOf(minId), Integer.valueOf(maxId), Integer.valueOf(minId), Integer.valueOf(maxId), Integer.valueOf(minId), Integer.valueOf(maxId), Integer.valueOf(minId), Integer.valueOf(maxId)}), new Object[0]);
        } else {
            cursor = this.database.queryFinalized(String.format(Locale.US, "SELECT type, start, end FROM media_holes_v2 WHERE uid = %d AND type = %d AND ((end >= %d AND end <= %d) OR (start >= %d AND start <= %d) OR (start >= %d AND end <= %d) OR (start <= %d AND end >= %d))", new Object[]{Long.valueOf(did), Integer.valueOf(type), Integer.valueOf(minId), Integer.valueOf(maxId), Integer.valueOf(minId), Integer.valueOf(maxId), Integer.valueOf(minId), Integer.valueOf(maxId), Integer.valueOf(minId), Integer.valueOf(maxId)}), new Object[0]);
        }
        ArrayList<Hole> holes = null;
        while (cursor.next()) {
            if (holes == null) {
                holes = new ArrayList();
            }
            int holeType = cursor.intValue(0);
            int start = cursor.intValue(1);
            int end = cursor.intValue(2);
            if (start != end || start != 1) {
                holes.add(new Hole(holeType, start, end));
            }
        }
        cursor.dispose();
        if (holes != null) {
            for (int a = 0; a < holes.size(); a++) {
                Hole hole = (Hole) holes.get(a);
                if (maxId >= hole.end - 1 && minId <= hole.start + 1) {
                    this.database.executeFast(String.format(Locale.US, "DELETE FROM media_holes_v2 WHERE uid = %d AND type = %d AND start = %d AND end = %d", new Object[]{Long.valueOf(did), Integer.valueOf(hole.type), Integer.valueOf(hole.start), Integer.valueOf(hole.end)})).stepThis().dispose();
                } else if (maxId >= hole.end - 1) {
                    if (hole.end != minId) {
                        try {
                            this.database.executeFast(String.format(Locale.US, "UPDATE media_holes_v2 SET end = %d WHERE uid = %d AND type = %d AND start = %d AND end = %d", new Object[]{Integer.valueOf(minId), Long.valueOf(did), Integer.valueOf(hole.type), Integer.valueOf(hole.start), Integer.valueOf(hole.end)})).stepThis().dispose();
                        } catch (Throwable e) {
                            try {
                                FileLog.m611e("tmessages", e);
                            } catch (Throwable e2) {
                                FileLog.m611e("tmessages", e2);
                                return;
                            }
                        }
                    }
                    continue;
                } else if (minId > hole.start + 1) {
                    this.database.executeFast(String.format(Locale.US, "DELETE FROM media_holes_v2 WHERE uid = %d AND type = %d AND start = %d AND end = %d", new Object[]{Long.valueOf(did), Integer.valueOf(hole.type), Integer.valueOf(hole.start), Integer.valueOf(hole.end)})).stepThis().dispose();
                    SQLitePreparedStatement state = this.database.executeFast("REPLACE INTO media_holes_v2 VALUES(?, ?, ?, ?)");
                    state.requery();
                    state.bindLong(1, did);
                    state.bindInteger(2, hole.type);
                    state.bindInteger(3, hole.start);
                    state.bindInteger(4, minId);
                    state.step();
                    state.requery();
                    state.bindLong(1, did);
                    state.bindInteger(2, hole.type);
                    state.bindInteger(3, maxId);
                    state.bindInteger(4, hole.end);
                    state.step();
                    state.dispose();
                } else if (hole.start != maxId) {
                    try {
                        this.database.executeFast(String.format(Locale.US, "UPDATE media_holes_v2 SET start = %d WHERE uid = %d AND type = %d AND start = %d AND end = %d", new Object[]{Integer.valueOf(maxId), Long.valueOf(did), Integer.valueOf(hole.type), Integer.valueOf(hole.start), Integer.valueOf(hole.end)})).stepThis().dispose();
                    } catch (Throwable e22) {
                        FileLog.m611e("tmessages", e22);
                    }
                } else {
                    continue;
                }
            }
        }
    }

    private void closeHolesInTable(String table, long did, int minId, int maxId) throws Exception {
        SQLiteCursor cursor = this.database.queryFinalized(String.format(Locale.US, "SELECT start, end FROM " + table + " WHERE uid = %d AND ((end >= %d AND end <= %d) OR (start >= %d AND start <= %d) OR (start >= %d AND end <= %d) OR (start <= %d AND end >= %d))", new Object[]{Long.valueOf(did), Integer.valueOf(minId), Integer.valueOf(maxId), Integer.valueOf(minId), Integer.valueOf(maxId), Integer.valueOf(minId), Integer.valueOf(maxId), Integer.valueOf(minId), Integer.valueOf(maxId)}), new Object[0]);
        ArrayList<Hole> holes = null;
        while (cursor.next()) {
            if (holes == null) {
                holes = new ArrayList();
            }
            int start = cursor.intValue(0);
            int end = cursor.intValue(1);
            if (start != end || start != 1) {
                holes.add(new Hole(start, end));
            }
        }
        cursor.dispose();
        if (holes != null) {
            for (int a = 0; a < holes.size(); a++) {
                Hole hole = (Hole) holes.get(a);
                if (maxId >= hole.end - 1 && minId <= hole.start + 1) {
                    this.database.executeFast(String.format(Locale.US, "DELETE FROM " + table + " WHERE uid = %d AND start = %d AND end = %d", new Object[]{Long.valueOf(did), Integer.valueOf(hole.start), Integer.valueOf(hole.end)})).stepThis().dispose();
                } else if (maxId >= hole.end - 1) {
                    if (hole.end != minId) {
                        try {
                            this.database.executeFast(String.format(Locale.US, "UPDATE " + table + " SET end = %d WHERE uid = %d AND start = %d AND end = %d", new Object[]{Integer.valueOf(minId), Long.valueOf(did), Integer.valueOf(hole.start), Integer.valueOf(hole.end)})).stepThis().dispose();
                        } catch (Throwable e) {
                            FileLog.m611e("tmessages", e);
                        }
                    } else {
                        continue;
                    }
                } else if (minId > hole.start + 1) {
                    this.database.executeFast(String.format(Locale.US, "DELETE FROM " + table + " WHERE uid = %d AND start = %d AND end = %d", new Object[]{Long.valueOf(did), Integer.valueOf(hole.start), Integer.valueOf(hole.end)})).stepThis().dispose();
                    SQLitePreparedStatement state = this.database.executeFast("REPLACE INTO " + table + " VALUES(?, ?, ?)");
                    state.requery();
                    state.bindLong(1, did);
                    state.bindInteger(2, hole.start);
                    state.bindInteger(3, minId);
                    state.step();
                    state.requery();
                    state.bindLong(1, did);
                    state.bindInteger(2, maxId);
                    state.bindInteger(3, hole.end);
                    state.step();
                    state.dispose();
                } else if (hole.start != maxId) {
                    try {
                        this.database.executeFast(String.format(Locale.US, "UPDATE " + table + " SET start = %d WHERE uid = %d AND start = %d AND end = %d", new Object[]{Integer.valueOf(maxId), Long.valueOf(did), Integer.valueOf(hole.start), Integer.valueOf(hole.end)})).stepThis().dispose();
                    } catch (Throwable e2) {
                        try {
                            FileLog.m611e("tmessages", e2);
                        } catch (Throwable e22) {
                            FileLog.m611e("tmessages", e22);
                            return;
                        }
                    }
                } else {
                    continue;
                }
            }
        }
    }

    public void putMessages(messages_Messages messages, long dialog_id, int load_type, int max_id, int important, boolean createDialog) {
        final messages_Messages org_telegram_tgnet_TLRPC_messages_Messages = messages;
        final int i = load_type;
        final int i2 = important;
        final long j = dialog_id;
        final int i3 = max_id;
        final boolean z = createDialog;
        this.storageQueue.postRunnable(new Runnable() {
            public void run() {
                try {
                    if (!org_telegram_tgnet_TLRPC_messages_Messages.messages.isEmpty()) {
                        int count;
                        int a;
                        TL_messageGroup group;
                        int minId;
                        int maxId;
                        MessagesStorage.this.database.beginTransaction();
                        if (!org_telegram_tgnet_TLRPC_messages_Messages.collapsed.isEmpty() && i2 == 2) {
                            count = org_telegram_tgnet_TLRPC_messages_Messages.collapsed.size();
                            for (a = 0; a < count; a++) {
                                group = (TL_messageGroup) org_telegram_tgnet_TLRPC_messages_Messages.collapsed.get(a);
                                if (a < count - 1) {
                                    minId = group.max_id;
                                    maxId = ((TL_messageGroup) org_telegram_tgnet_TLRPC_messages_Messages.collapsed.get(a + 1)).min_id;
                                    MessagesStorage.this.closeHolesInTable("messages_holes", j, minId, maxId);
                                    MessagesStorage.this.closeHolesInMedia(j, minId, maxId, -1);
                                }
                                if (a == 0) {
                                    minId = ((Message) org_telegram_tgnet_TLRPC_messages_Messages.messages.get(org_telegram_tgnet_TLRPC_messages_Messages.messages.size() - 1)).id;
                                    maxId = minId > group.min_id ? group.max_id : group.min_id;
                                    MessagesStorage.this.closeHolesInTable("messages_holes", j, minId, maxId);
                                    MessagesStorage.this.closeHolesInMedia(j, minId, maxId, -1);
                                }
                                if (a == count - 1) {
                                    maxId = ((Message) org_telegram_tgnet_TLRPC_messages_Messages.messages.get(0)).id;
                                    minId = maxId < group.max_id ? group.min_id : group.max_id;
                                    MessagesStorage.this.closeHolesInTable("messages_holes", j, minId, maxId);
                                    MessagesStorage.this.closeHolesInMedia(j, minId, maxId, -1);
                                }
                            }
                        }
                        if (i == 0) {
                            minId = ((Message) org_telegram_tgnet_TLRPC_messages_Messages.messages.get(org_telegram_tgnet_TLRPC_messages_Messages.messages.size() - 1)).id;
                            if (i2 != 2 || org_telegram_tgnet_TLRPC_messages_Messages.collapsed.isEmpty()) {
                                MessagesStorage.this.closeHolesInTable("messages_holes", j, minId, i3);
                                MessagesStorage.this.closeHolesInMedia(j, minId, i3, -1);
                            }
                            if (i2 != 0) {
                                MessagesStorage.this.closeHolesInTable("messages_imp_holes", j, minId, i3);
                            }
                        } else if (i == 1) {
                            maxId = ((Message) org_telegram_tgnet_TLRPC_messages_Messages.messages.get(0)).id;
                            if (i2 != 2 || org_telegram_tgnet_TLRPC_messages_Messages.collapsed.isEmpty()) {
                                MessagesStorage.this.closeHolesInTable("messages_holes", j, i3, maxId);
                                MessagesStorage.this.closeHolesInMedia(j, i3, maxId, -1);
                            }
                            if (i2 != 0) {
                                MessagesStorage.this.closeHolesInTable("messages_imp_holes", j, i3, maxId);
                            }
                        } else if (i == 3 || i == 2) {
                            if (i3 == 0) {
                                maxId = ConnectionsManager.DEFAULT_DATACENTER_ID;
                            } else {
                                maxId = ((Message) org_telegram_tgnet_TLRPC_messages_Messages.messages.get(0)).id;
                            }
                            minId = ((Message) org_telegram_tgnet_TLRPC_messages_Messages.messages.get(org_telegram_tgnet_TLRPC_messages_Messages.messages.size() - 1)).id;
                            if (i2 != 2 || org_telegram_tgnet_TLRPC_messages_Messages.collapsed.isEmpty()) {
                                MessagesStorage.this.closeHolesInTable("messages_holes", j, minId, maxId);
                                MessagesStorage.this.closeHolesInMedia(j, minId, maxId, -1);
                            }
                            if (i2 != 0) {
                                MessagesStorage.this.closeHolesInTable("messages_imp_holes", j, minId, maxId);
                            }
                        }
                        count = org_telegram_tgnet_TLRPC_messages_Messages.messages.size();
                        SQLitePreparedStatement state = MessagesStorage.this.database.executeFast("REPLACE INTO messages VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, NULL, ?)");
                        SQLitePreparedStatement state2 = MessagesStorage.this.database.executeFast("REPLACE INTO media_v2 VALUES(?, ?, ?, ?, ?)");
                        Message botKeyboard = null;
                        int countAfterImportant = 0;
                        int minChannelMessageId = ConnectionsManager.DEFAULT_DATACENTER_ID;
                        int maxChannelMessageId = 0;
                        int lastChannelImportantId = -1;
                        int channelId = 0;
                        for (a = 0; a < count; a++) {
                            Message message = (Message) org_telegram_tgnet_TLRPC_messages_Messages.messages.get(a);
                            long messageId = (long) message.id;
                            if (channelId == 0) {
                                channelId = message.to_id.channel_id;
                            }
                            if (message.to_id.channel_id != 0) {
                                messageId |= ((long) channelId) << 32;
                            }
                            if (a == 0 && z) {
                                SQLitePreparedStatement state3 = MessagesStorage.this.database.executeFast("REPLACE INTO dialogs VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                                state3.bindLong(1, j);
                                state3.bindInteger(2, message.date);
                                state3.bindInteger(3, 0);
                                state3.bindLong(4, messageId);
                                state3.bindInteger(5, message.id);
                                state3.bindInteger(6, 0);
                                state3.bindLong(7, messageId);
                                state3.bindInteger(8, 0);
                                state3.bindInteger(9, org_telegram_tgnet_TLRPC_messages_Messages.pts);
                                state3.bindInteger(10, message.date);
                                state3.step();
                                state3.dispose();
                            }
                            boolean isImportant = MessageObject.isImportant(message);
                            if (i != -1 && i2 == 1) {
                                if (isImportant) {
                                    minChannelMessageId = Math.min(minChannelMessageId, message.id);
                                    maxChannelMessageId = Math.max(maxChannelMessageId, message.id);
                                    if (lastChannelImportantId == -1) {
                                        int countBeforeImportant = countAfterImportant;
                                    } else if (countAfterImportant != 0) {
                                        group = new TL_messageGroup();
                                        group.max_id = lastChannelImportantId;
                                        group.min_id = message.id;
                                        group.count = countAfterImportant;
                                        org_telegram_tgnet_TLRPC_messages_Messages.collapsed.add(group);
                                    }
                                    countAfterImportant = 0;
                                    lastChannelImportantId = message.id;
                                } else {
                                    countAfterImportant++;
                                }
                            }
                            MessagesStorage.this.fixUnsupportedMedia(message);
                            state.requery();
                            AbstractSerializedData nativeByteBuffer = new NativeByteBuffer(message.getObjectSize());
                            message.serializeToStream(nativeByteBuffer);
                            state.bindLong(1, messageId);
                            state.bindLong(2, j);
                            state.bindInteger(3, MessageObject.getUnreadFlags(message));
                            state.bindInteger(4, message.send_state);
                            state.bindInteger(5, message.date);
                            state.bindByteBuffer(6, (NativeByteBuffer) nativeByteBuffer);
                            state.bindInteger(7, MessageObject.isOut(message) ? 1 : 0);
                            state.bindInteger(8, 0);
                            if ((message.flags & 1024) != 0) {
                                state.bindInteger(9, message.views);
                            } else {
                                state.bindInteger(9, 0);
                            }
                            state.bindInteger(10, isImportant ? 1 : 0);
                            state.step();
                            if (SharedMediaQuery.canAddMessageToMedia(message)) {
                                state2.requery();
                                state2.bindLong(1, messageId);
                                state2.bindLong(2, j);
                                state2.bindInteger(3, message.date);
                                state2.bindInteger(4, SharedMediaQuery.getMediaType(message));
                                state2.bindByteBuffer(5, (NativeByteBuffer) nativeByteBuffer);
                                state2.step();
                            }
                            nativeByteBuffer.reuse();
                            if (i == 0 && message.reply_markup != null && ((!message.reply_markup.selective || message.mentioned) && (botKeyboard == null || botKeyboard.id < message.id))) {
                                botKeyboard = message;
                            }
                        }
                        state.dispose();
                        state2.dispose();
                        if (botKeyboard != null) {
                            BotQuery.putBotKeyboard(j, botKeyboard);
                        }
                        if (!(i == -1 || i2 == 0)) {
                            if (!org_telegram_tgnet_TLRPC_messages_Messages.collapsed.isEmpty()) {
                                state = MessagesStorage.this.database.executeFast("REPLACE INTO channel_group VALUES(?, ?, ?, ?)");
                                for (a = 0; a < org_telegram_tgnet_TLRPC_messages_Messages.collapsed.size(); a++) {
                                    group = (TL_messageGroup) org_telegram_tgnet_TLRPC_messages_Messages.collapsed.get(a);
                                    if (group.min_id > group.max_id) {
                                        int temp = group.min_id;
                                        group.min_id = group.max_id;
                                        group.max_id = temp;
                                    }
                                    state.requery();
                                    state.bindLong(1, j);
                                    state.bindInteger(2, group.min_id);
                                    state.bindInteger(3, group.max_id);
                                    state.bindInteger(4, group.count);
                                    state.step();
                                }
                                state.dispose();
                            }
                            if (i2 == 1) {
                                org_telegram_tgnet_TLRPC_messages_Messages.collapsed.clear();
                            }
                        }
                        MessagesStorage.this.putUsersInternal(org_telegram_tgnet_TLRPC_messages_Messages.users);
                        MessagesStorage.this.putChatsInternal(org_telegram_tgnet_TLRPC_messages_Messages.chats);
                        MessagesStorage.this.database.commitTransaction();
                        if (z) {
                            MessagesStorage.getInstance().updateDialogsWithDeletedMessages(new ArrayList(), false, channelId);
                        }
                    } else if (i == 0) {
                        if (i2 != 2) {
                            MessagesStorage.this.doneHolesInTable("messages_holes", j, i3);
                            MessagesStorage.this.doneHolesInMedia(j, i3, -1);
                        }
                        if (i2 != 0) {
                            MessagesStorage.this.doneHolesInTable("messages_imp_holes", j, i3);
                        }
                    }
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
        });
    }

    public static void addUsersAndChatsFromMessage(Message message, ArrayList<Integer> usersToLoad, ArrayList<Integer> chatsToLoad) {
        if (message.from_id != 0) {
            if (message.from_id > 0) {
                if (!usersToLoad.contains(Integer.valueOf(message.from_id))) {
                    usersToLoad.add(Integer.valueOf(message.from_id));
                }
            } else if (!chatsToLoad.contains(Integer.valueOf(-message.from_id))) {
                chatsToLoad.add(Integer.valueOf(-message.from_id));
            }
        }
        if (message.action != null) {
            if (!(message.action.user_id == 0 || usersToLoad.contains(Integer.valueOf(message.action.user_id)))) {
                usersToLoad.add(Integer.valueOf(message.action.user_id));
            }
            if (!(message.action.channel_id == 0 || chatsToLoad.contains(Integer.valueOf(message.action.channel_id)))) {
                chatsToLoad.add(Integer.valueOf(message.action.channel_id));
            }
            if (!(message.action.chat_id == 0 || chatsToLoad.contains(Integer.valueOf(message.action.chat_id)))) {
                chatsToLoad.add(Integer.valueOf(message.action.chat_id));
            }
            if (!message.action.users.isEmpty()) {
                for (int a = 0; a < message.action.users.size(); a++) {
                    Integer uid = (Integer) message.action.users.get(a);
                    if (!usersToLoad.contains(uid)) {
                        usersToLoad.add(uid);
                    }
                }
            }
        }
        if (message.media != null) {
            if (!(message.media.user_id == 0 || usersToLoad.contains(Integer.valueOf(message.media.user_id)))) {
                usersToLoad.add(Integer.valueOf(message.media.user_id));
            }
            if (!(message.media.audio == null || message.media.audio.user_id == 0 || usersToLoad.contains(Integer.valueOf(message.media.audio.user_id)))) {
                usersToLoad.add(Integer.valueOf(message.media.audio.user_id));
            }
        }
        if (message.fwd_from_id instanceof TL_peerUser) {
            if (!usersToLoad.contains(Integer.valueOf(message.fwd_from_id.user_id))) {
                usersToLoad.add(Integer.valueOf(message.fwd_from_id.user_id));
            }
        } else if ((message.fwd_from_id instanceof TL_peerChannel) && !chatsToLoad.contains(Integer.valueOf(message.fwd_from_id.channel_id))) {
            chatsToLoad.add(Integer.valueOf(message.fwd_from_id.channel_id));
        }
        if (message.ttl < 0 && !chatsToLoad.contains(Integer.valueOf(-message.ttl))) {
            chatsToLoad.add(Integer.valueOf(-message.ttl));
        }
    }

    public void getDialogs(final int offset, final int count) {
        this.storageQueue.postRunnable(new Runnable() {
            public void run() {
                messages_Dialogs dialogs = new messages_Dialogs();
                ArrayList<EncryptedChat> encryptedChats = new ArrayList();
                try {
                    ArrayList<Integer> usersToLoad = new ArrayList();
                    usersToLoad.add(Integer.valueOf(UserConfig.getClientUserId()));
                    ArrayList<Integer> chatsToLoad = new ArrayList();
                    ArrayList<Integer> encryptedToLoad = new ArrayList();
                    SQLiteCursor cursor = MessagesStorage.this.database.queryFinalized(String.format(Locale.US, "SELECT d.did, d.last_mid, d.unread_count, d.date, m.data, m.read_state, m.mid, m.send_state, s.flags, m.date, d.last_mid_i, d.unread_count_i, d.pts, d.inbox_max FROM dialogs as d LEFT JOIN messages as m ON d.last_mid = m.mid LEFT JOIN dialog_settings as s ON d.did = s.did ORDER BY d.date DESC LIMIT %d,%d", new Object[]{Integer.valueOf(offset), Integer.valueOf(count)}), new Object[0]);
                    while (cursor.next()) {
                        Dialog dialog;
                        int pts = cursor.intValue(12);
                        long id = cursor.longValue(0);
                        if (pts == 0 || ((int) id) > 0) {
                            dialog = new TL_dialog();
                        } else {
                            dialog = new TL_dialogChannel();
                        }
                        dialog.id = id;
                        dialog.top_message = cursor.intValue(1);
                        dialog.unread_count = cursor.intValue(2);
                        dialog.last_message_date = cursor.intValue(3);
                        dialog.pts = pts;
                        dialog.read_inbox_max_id = cursor.intValue(13);
                        dialog.top_not_important_message = cursor.intValue(10);
                        dialog.unread_not_important_count = cursor.intValue(11);
                        long flags = cursor.longValue(8);
                        int low_flags = (int) flags;
                        dialog.notify_settings = new TL_peerNotifySettings();
                        if ((low_flags & 1) != 0) {
                            dialog.notify_settings.mute_until = (int) (flags >> 32);
                            if (dialog.notify_settings.mute_until == 0) {
                                dialog.notify_settings.mute_until = ConnectionsManager.DEFAULT_DATACENTER_ID;
                            }
                        }
                        dialogs.dialogs.add(dialog);
                        NativeByteBuffer data = new NativeByteBuffer(cursor.byteArrayLength(4));
                        if (!(data == null || cursor.byteBufferValue(4, data) == 0)) {
                            Message message = Message.TLdeserialize(data, data.readInt32(false), false);
                            if (message != null) {
                                MessageObject.setUnreadFlags(message, cursor.intValue(5));
                                message.id = cursor.intValue(6);
                                int date = cursor.intValue(9);
                                if (date != 0) {
                                    dialog.last_message_date = date;
                                }
                                message.send_state = cursor.intValue(7);
                                message.dialog_id = dialog.id;
                                dialogs.messages.add(message);
                                MessagesStorage.addUsersAndChatsFromMessage(message, usersToLoad, chatsToLoad);
                            }
                        }
                        data.reuse();
                        int lower_id = (int) dialog.id;
                        int high_id = (int) (dialog.id >> 32);
                        if (lower_id == 0) {
                            if (!encryptedToLoad.contains(Integer.valueOf(high_id))) {
                                encryptedToLoad.add(Integer.valueOf(high_id));
                            }
                        } else if (high_id == 1) {
                            if (!chatsToLoad.contains(Integer.valueOf(lower_id))) {
                                chatsToLoad.add(Integer.valueOf(lower_id));
                            }
                        } else if (lower_id > 0) {
                            if (!usersToLoad.contains(Integer.valueOf(lower_id))) {
                                usersToLoad.add(Integer.valueOf(lower_id));
                            }
                        } else if (!chatsToLoad.contains(Integer.valueOf(-lower_id))) {
                            chatsToLoad.add(Integer.valueOf(-lower_id));
                        }
                    }
                    cursor.dispose();
                    if (!encryptedToLoad.isEmpty()) {
                        MessagesStorage.this.getEncryptedChatsInternal(TextUtils.join(",", encryptedToLoad), encryptedChats, usersToLoad);
                    }
                    if (!chatsToLoad.isEmpty()) {
                        MessagesStorage.this.getChatsInternal(TextUtils.join(",", chatsToLoad), dialogs.chats);
                    }
                    if (!usersToLoad.isEmpty()) {
                        MessagesStorage.this.getUsersInternal(TextUtils.join(",", usersToLoad), dialogs.users);
                    }
                    MessagesController.getInstance().processLoadedDialogs(dialogs, encryptedChats, offset, count, true, false, false);
                } catch (Throwable e) {
                    dialogs.dialogs.clear();
                    dialogs.users.clear();
                    dialogs.chats.clear();
                    encryptedChats.clear();
                    FileLog.m611e("tmessages", e);
                    MessagesController.getInstance().processLoadedDialogs(dialogs, encryptedChats, 0, 100, true, true, false);
                }
            }
        });
    }

    public static void createFirstHoles(long did, SQLitePreparedStatement state5, SQLitePreparedStatement state6, SQLitePreparedStatement state7, SQLitePreparedStatement state8, ArrayList<Message> arrayList) throws Exception {
        int impMessageId = 0;
        int notImpMessageId = 0;
        for (int a = 0; a < arrayList.size(); a++) {
            Message message = (Message) arrayList.get(a);
            if (MessageObject.isImportant(message)) {
                state7.requery();
                state7.bindLong(1, did);
                state7.bindInteger(2, message.id == 1 ? 1 : 0);
                state7.bindInteger(3, message.id);
                state7.step();
                impMessageId = Math.max(message.id, impMessageId);
            } else {
                notImpMessageId = Math.max(message.id, notImpMessageId);
            }
        }
        if (impMessageId != 0 && notImpMessageId == 0) {
            notImpMessageId = impMessageId;
            impMessageId = 0;
        }
        int b;
        if (arrayList.size() == 1) {
            int messageId = ((Message) arrayList.get(0)).id;
            state5.requery();
            state5.bindLong(1, did);
            state5.bindInteger(2, messageId == 1 ? 1 : 0);
            state5.bindInteger(3, messageId);
            state5.step();
            for (b = 0; b < 5; b++) {
                state6.requery();
                state6.bindLong(1, did);
                state6.bindInteger(2, b);
                state6.bindInteger(3, messageId == 1 ? 1 : 0);
                state6.bindInteger(4, messageId);
                state6.step();
            }
        } else if (arrayList.size() == 2) {
            int firstId = ((Message) arrayList.get(0)).id;
            int lastId = ((Message) arrayList.get(1)).id;
            if (firstId > lastId) {
                int temp = firstId;
                firstId = lastId;
                lastId = temp;
            }
            state5.requery();
            state5.bindLong(1, did);
            state5.bindInteger(2, firstId == 1 ? 1 : 0);
            state5.bindInteger(3, firstId);
            state5.step();
            state5.requery();
            state5.bindLong(1, did);
            state5.bindInteger(2, firstId);
            state5.bindInteger(3, lastId);
            state5.step();
            for (b = 0; b < 5; b++) {
                state6.requery();
                state6.bindLong(1, did);
                state6.bindInteger(2, b);
                state6.bindInteger(3, firstId == 1 ? 1 : 0);
                state6.bindInteger(4, firstId);
                state6.step();
                state6.requery();
                state6.bindLong(1, did);
                state6.bindInteger(2, b);
                state6.bindInteger(3, firstId);
                state6.bindInteger(4, lastId);
                state6.step();
            }
            if (impMessageId != 0 && impMessageId < notImpMessageId) {
                state8.requery();
                state8.bindLong(1, did);
                state8.bindInteger(2, impMessageId);
                state8.bindInteger(3, ConnectionsManager.DEFAULT_DATACENTER_ID);
                state8.bindInteger(4, notImpMessageId - impMessageId);
                state8.step();
            }
        }
    }

    private void putDialogsInternal(messages_Dialogs dialogs) {
        try {
            int a;
            Message message;
            ArrayList<Message> arrayList;
            this.database.beginTransaction();
            HashMap<Long, ArrayList<Message>> new_dialogMessage = new HashMap();
            for (a = 0; a < dialogs.messages.size(); a++) {
                message = (Message) dialogs.messages.get(a);
                arrayList = (ArrayList) new_dialogMessage.get(Long.valueOf(message.dialog_id));
                if (arrayList == null) {
                    arrayList = new ArrayList();
                    new_dialogMessage.put(Long.valueOf(message.dialog_id), arrayList);
                }
                arrayList.add(message);
            }
            if (!dialogs.dialogs.isEmpty()) {
                SQLitePreparedStatement state = this.database.executeFast("REPLACE INTO messages VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, NULL, ?)");
                SQLitePreparedStatement state2 = this.database.executeFast("REPLACE INTO dialogs VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                SQLitePreparedStatement state3 = this.database.executeFast("REPLACE INTO media_v2 VALUES(?, ?, ?, ?, ?)");
                SQLitePreparedStatement state4 = this.database.executeFast("REPLACE INTO dialog_settings VALUES(?, ?)");
                SQLitePreparedStatement state5 = this.database.executeFast("REPLACE INTO messages_holes VALUES(?, ?, ?)");
                SQLitePreparedStatement state6 = this.database.executeFast("REPLACE INTO media_holes_v2 VALUES(?, ?, ?, ?)");
                SQLitePreparedStatement state7 = this.database.executeFast("REPLACE INTO messages_imp_holes VALUES(?, ?, ?)");
                SQLitePreparedStatement state8 = this.database.executeFast("REPLACE INTO channel_group VALUES(?, ?, ?, ?)");
                for (a = 0; a < dialogs.dialogs.size(); a++) {
                    Dialog dialog = (Dialog) dialogs.dialogs.get(a);
                    if (dialog.id == 0) {
                        if (dialog.peer.user_id != 0) {
                            dialog.id = (long) dialog.peer.user_id;
                        } else if (dialog.peer.chat_id != 0) {
                            dialog.id = (long) (-dialog.peer.chat_id);
                        } else {
                            dialog.id = (long) (-dialog.peer.channel_id);
                        }
                    }
                    int messageDate = 0;
                    int messageDateI = 0;
                    boolean isMegagroup = false;
                    arrayList = (ArrayList) new_dialogMessage.get(Long.valueOf(dialog.id));
                    if (arrayList != null) {
                        for (int b = 0; b < arrayList.size(); b++) {
                            message = (Message) arrayList.get(b);
                            if (message.to_id.channel_id == 0 || MessageObject.isImportant(message)) {
                                messageDate = Math.max(message.date, messageDate);
                            } else {
                                messageDateI = Math.max(message.date, messageDateI);
                            }
                            isMegagroup = MessageObject.isMegagroup(message);
                            if (message.reply_markup != null && (!message.reply_markup.selective || message.mentioned)) {
                                BotQuery.putBotKeyboard(dialog.id, message);
                            }
                            fixUnsupportedMedia(message);
                            NativeByteBuffer data = new NativeByteBuffer(message.getObjectSize());
                            message.serializeToStream(data);
                            long messageId = (long) message.id;
                            if (message.to_id.channel_id != 0) {
                                messageId |= ((long) message.to_id.channel_id) << 32;
                            }
                            state.requery();
                            state.bindLong(1, messageId);
                            state.bindLong(2, dialog.id);
                            state.bindInteger(3, MessageObject.getUnreadFlags(message));
                            state.bindInteger(4, message.send_state);
                            state.bindInteger(5, message.date);
                            state.bindByteBuffer(6, data);
                            state.bindInteger(7, MessageObject.isOut(message) ? 1 : 0);
                            state.bindInteger(8, 0);
                            if ((message.flags & 1024) != 0) {
                                state.bindInteger(9, message.views);
                            } else {
                                state.bindInteger(9, 0);
                            }
                            state.bindInteger(10, MessageObject.isImportant(message) ? 1 : 0);
                            state.step();
                            if (SharedMediaQuery.canAddMessageToMedia(message)) {
                                state3.requery();
                                state3.bindLong(1, messageId);
                                state3.bindLong(2, dialog.id);
                                state3.bindInteger(3, message.date);
                                state3.bindInteger(4, SharedMediaQuery.getMediaType(message));
                                state3.bindByteBuffer(5, data);
                                state3.step();
                            }
                            data.reuse();
                        }
                        createFirstHoles(dialog.id, state5, state6, state7, state8, arrayList);
                    }
                    long topMessage = (long) dialog.top_message;
                    long topMessageI = (long) dialog.top_not_important_message;
                    if (dialog.peer.channel_id != 0) {
                        if (isMegagroup) {
                            topMessageI = Math.max(topMessage, topMessageI);
                            topMessage = topMessageI;
                            messageDateI = Math.max(messageDate, messageDateI);
                            messageDate = messageDateI;
                        }
                        topMessage |= ((long) dialog.peer.channel_id) << 32;
                        topMessageI |= ((long) dialog.peer.channel_id) << 32;
                    }
                    state2.requery();
                    state2.bindLong(1, dialog.id);
                    state2.bindInteger(2, messageDate);
                    state2.bindInteger(3, dialog.unread_count);
                    state2.bindLong(4, topMessage);
                    state2.bindInteger(5, dialog.read_inbox_max_id);
                    state2.bindInteger(6, 0);
                    state2.bindLong(7, topMessageI);
                    state2.bindInteger(8, dialog.unread_not_important_count);
                    state2.bindInteger(9, dialog.pts);
                    state2.bindInteger(10, messageDateI);
                    state2.step();
                    if (dialog.notify_settings != null) {
                        state4.requery();
                        state4.bindLong(1, dialog.id);
                        state4.bindInteger(2, dialog.notify_settings.mute_until != 0 ? 1 : 0);
                        state4.step();
                    }
                }
                state.dispose();
                state2.dispose();
                state3.dispose();
                state4.dispose();
                state5.dispose();
                state6.dispose();
                state7.dispose();
                state8.dispose();
            }
            putUsersInternal(dialogs.users);
            putChatsInternal(dialogs.chats);
            this.database.commitTransaction();
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
        }
    }

    public void putDialogs(final messages_Dialogs dialogs) {
        if (!dialogs.dialogs.isEmpty()) {
            this.storageQueue.postRunnable(new Runnable() {
                public void run() {
                    MessagesStorage.this.putDialogsInternal(dialogs);
                    MessagesStorage.this.loadUnreadMessages();
                }
            });
        }
    }

    public int getChannelReadInboxMax(final int channelId) {
        final Semaphore semaphore = new Semaphore(0);
        final Integer[] max = new Integer[]{Integer.valueOf(0)};
        getInstance().getStorageQueue().postRunnable(new Runnable() {
            public void run() {
                SQLiteCursor cursor = null;
                try {
                    cursor = MessagesStorage.this.database.queryFinalized("SELECT inbox_max FROM dialogs WHERE did = " + (-channelId), new Object[0]);
                    if (cursor.next()) {
                        max[0] = Integer.valueOf(cursor.intValue(0));
                    }
                    if (cursor != null) {
                        cursor.dispose();
                    }
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                    if (cursor != null) {
                        cursor.dispose();
                    }
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.dispose();
                    }
                }
                semaphore.release();
            }
        });
        try {
            semaphore.acquire();
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
        }
        return max[0].intValue();
    }

    public int getChannelPtsSync(final int channelId) {
        final Semaphore semaphore = new Semaphore(0);
        final Integer[] pts = new Integer[]{Integer.valueOf(0)};
        getInstance().getStorageQueue().postRunnable(new Runnable() {
            public void run() {
                SQLiteCursor cursor = null;
                try {
                    cursor = MessagesStorage.this.database.queryFinalized("SELECT pts FROM dialogs WHERE did = " + (-channelId), new Object[0]);
                    if (cursor.next()) {
                        pts[0] = Integer.valueOf(cursor.intValue(0));
                    }
                    if (cursor != null) {
                        cursor.dispose();
                    }
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                    if (cursor != null) {
                        cursor.dispose();
                    }
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.dispose();
                    }
                }
                semaphore.release();
            }
        });
        try {
            semaphore.acquire();
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
        }
        return pts[0].intValue();
    }

    public User getUserSync(final int user_id) {
        final Semaphore semaphore = new Semaphore(0);
        final User[] user = new User[1];
        getInstance().getStorageQueue().postRunnable(new Runnable() {
            public void run() {
                user[0] = MessagesStorage.this.getUser(user_id);
                semaphore.release();
            }
        });
        try {
            semaphore.acquire();
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
        }
        return user[0];
    }

    public Chat getChatSync(final int user_id) {
        final Semaphore semaphore = new Semaphore(0);
        final Chat[] chat = new Chat[1];
        getInstance().getStorageQueue().postRunnable(new Runnable() {
            public void run() {
                chat[0] = MessagesStorage.this.getChat(user_id);
                semaphore.release();
            }
        });
        try {
            semaphore.acquire();
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
        }
        return chat[0];
    }

    public User getUser(int user_id) {
        try {
            ArrayList<User> users = new ArrayList();
            getUsersInternal("" + user_id, users);
            if (users.isEmpty()) {
                return null;
            }
            return (User) users.get(0);
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
            return null;
        }
    }

    public ArrayList<User> getUsers(ArrayList<Integer> uids) {
        ArrayList<User> users = new ArrayList();
        try {
            getUsersInternal(TextUtils.join(",", uids), users);
        } catch (Throwable e) {
            users.clear();
            FileLog.m611e("tmessages", e);
        }
        return users;
    }

    public Chat getChat(int chat_id) {
        try {
            ArrayList<Chat> chats = new ArrayList();
            getChatsInternal("" + chat_id, chats);
            if (chats.isEmpty()) {
                return null;
            }
            return (Chat) chats.get(0);
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
            return null;
        }
    }

    public EncryptedChat getEncryptedChat(int chat_id) {
        try {
            ArrayList<EncryptedChat> encryptedChats = new ArrayList();
            getEncryptedChatsInternal("" + chat_id, encryptedChats, null);
            if (encryptedChats.isEmpty()) {
                return null;
            }
            return (EncryptedChat) encryptedChats.get(0);
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
            return null;
        }
    }
}
