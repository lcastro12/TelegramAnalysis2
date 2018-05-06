package org.telegram.ui.Adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.location.LocationStatusCodes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import org.telegram.SQLite.SQLiteCursor;
import org.telegram.SQLite.SQLitePreparedStatement;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.C0553R;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.NativeByteBuffer;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.EncryptedChat;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_inputPeerEmpty;
import org.telegram.tgnet.TLRPC.TL_messages_searchGlobal;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.messages_Messages;
import org.telegram.ui.Cells.DialogCell;
import org.telegram.ui.Cells.GreySectionCell;
import org.telegram.ui.Cells.HashtagSearchCell;
import org.telegram.ui.Cells.LoadingCell;
import org.telegram.ui.Cells.ProfileSearchCell;

public class DialogsSearchAdapter extends BaseSearchAdapterRecycler {
    private MessagesActivitySearchAdapterDelegate delegate;
    private int dialogsType;
    private String lastMessagesSearchString;
    private int lastReqId;
    private int lastSearchId = 0;
    private String lastSearchText;
    private Context mContext;
    private boolean messagesSearchEndReached;
    private int needMessagesSearch;
    private ArrayList<RecentSearchObject> recentSearchObjects = new ArrayList();
    private HashMap<Long, RecentSearchObject> recentSearchObjectsById = new HashMap();
    private int reqId = 0;
    private ArrayList<TLObject> searchResult = new ArrayList();
    private ArrayList<String> searchResultHashtags = new ArrayList();
    private ArrayList<MessageObject> searchResultMessages = new ArrayList();
    private ArrayList<CharSequence> searchResultNames = new ArrayList();
    private Timer searchTimer;

    class C07432 implements Runnable {

        class C07411 implements Comparator<RecentSearchObject> {
            C07411() {
            }

            public int compare(RecentSearchObject lhs, RecentSearchObject rhs) {
                if (lhs.date < rhs.date) {
                    return 1;
                }
                if (lhs.date > rhs.date) {
                    return -1;
                }
                return 0;
            }
        }

        C07432() {
        }

        public void run() {
            try {
                long did;
                RecentSearchObject recentSearchObject;
                int a;
                SQLiteCursor cursor = MessagesStorage.getInstance().getDatabase().queryFinalized("SELECT did, date FROM search_recent WHERE 1", new Object[0]);
                ArrayList<Integer> usersToLoad = new ArrayList();
                ArrayList<Integer> chatsToLoad = new ArrayList();
                ArrayList<Integer> encryptedToLoad = new ArrayList();
                ArrayList<User> encUsers = new ArrayList();
                final ArrayList<RecentSearchObject> arrayList = new ArrayList();
                HashMap<Long, RecentSearchObject> hashMap = new HashMap();
                while (cursor.next()) {
                    did = cursor.longValue(0);
                    boolean add = false;
                    int lower_id = (int) did;
                    int high_id = (int) (did >> 32);
                    if (lower_id != 0) {
                        if (high_id == 1) {
                            if (DialogsSearchAdapter.this.dialogsType == 0 && !chatsToLoad.contains(Integer.valueOf(lower_id))) {
                                chatsToLoad.add(Integer.valueOf(lower_id));
                                add = true;
                            }
                        } else if (lower_id > 0) {
                            if (!(DialogsSearchAdapter.this.dialogsType == 2 || usersToLoad.contains(Integer.valueOf(lower_id)))) {
                                usersToLoad.add(Integer.valueOf(lower_id));
                                add = true;
                            }
                        } else if (!chatsToLoad.contains(Integer.valueOf(-lower_id))) {
                            chatsToLoad.add(Integer.valueOf(-lower_id));
                            add = true;
                        }
                    } else if (DialogsSearchAdapter.this.dialogsType == 0 && !encryptedToLoad.contains(Integer.valueOf(high_id))) {
                        encryptedToLoad.add(Integer.valueOf(high_id));
                        add = true;
                    }
                    if (add) {
                        recentSearchObject = new RecentSearchObject();
                        recentSearchObject.did = did;
                        recentSearchObject.date = cursor.intValue(1);
                        arrayList.add(recentSearchObject);
                        hashMap.put(Long.valueOf(recentSearchObject.did), recentSearchObject);
                    }
                }
                cursor.dispose();
                ArrayList<User> users = new ArrayList();
                if (!encryptedToLoad.isEmpty()) {
                    ArrayList<EncryptedChat> encryptedChats = new ArrayList();
                    MessagesStorage.getInstance().getEncryptedChatsInternal(TextUtils.join(",", encryptedToLoad), encryptedChats, usersToLoad);
                    for (a = 0; a < encryptedChats.size(); a++) {
                        ((RecentSearchObject) hashMap.get(Long.valueOf(((long) ((EncryptedChat) encryptedChats.get(a)).id) << 32))).object = (TLObject) encryptedChats.get(a);
                    }
                }
                if (!chatsToLoad.isEmpty()) {
                    ArrayList<Chat> chats = new ArrayList();
                    MessagesStorage.getInstance().getChatsInternal(TextUtils.join(",", chatsToLoad), chats);
                    for (a = 0; a < chats.size(); a++) {
                        Chat chat = (Chat) chats.get(a);
                        if (chat.id > 0) {
                            did = (long) (-chat.id);
                        } else {
                            did = AndroidUtilities.makeBroadcastId(chat.id);
                        }
                        if (chat.migrated_to != null) {
                            recentSearchObject = (RecentSearchObject) hashMap.remove(Long.valueOf(did));
                            if (recentSearchObject != null) {
                                arrayList.remove(recentSearchObject);
                            }
                        } else {
                            ((RecentSearchObject) hashMap.get(Long.valueOf(did))).object = chat;
                        }
                    }
                }
                if (!usersToLoad.isEmpty()) {
                    MessagesStorage.getInstance().getUsersInternal(TextUtils.join(",", usersToLoad), users);
                    for (a = 0; a < users.size(); a++) {
                        TLObject user = (User) users.get(a);
                        recentSearchObject = (RecentSearchObject) hashMap.get(Long.valueOf((long) user.id));
                        if (recentSearchObject != null) {
                            recentSearchObject.object = user;
                        }
                    }
                }
                Collections.sort(arrayList, new C07411());
                final HashMap<Long, RecentSearchObject> hashMap2 = hashMap;
                AndroidUtilities.runOnUIThread(new Runnable() {
                    public void run() {
                        DialogsSearchAdapter.this.setRecentSearch(arrayList, hashMap2);
                    }
                });
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
        }
    }

    class C07454 implements Runnable {
        C07454() {
        }

        public void run() {
            try {
                MessagesStorage.getInstance().getDatabase().executeFast("DELETE FROM search_recent WHERE 1").stepThis().dispose();
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
        }
    }

    private class DialogSearchResult {
        public int date;
        public CharSequence name;
        public TLObject object;

        private DialogSearchResult() {
        }
    }

    public interface MessagesActivitySearchAdapterDelegate {
        void searchStateChanged(boolean z);
    }

    protected static class RecentSearchObject {
        int date;
        long did;
        TLObject object;

        protected RecentSearchObject() {
        }
    }

    private class Holder extends ViewHolder {
        public Holder(View itemView) {
            super(itemView);
        }
    }

    public DialogsSearchAdapter(Context context, int messagesSearch, int type) {
        this.mContext = context;
        this.needMessagesSearch = messagesSearch;
        this.dialogsType = type;
        loadRecentSearch();
    }

    public void setDelegate(MessagesActivitySearchAdapterDelegate delegate) {
        this.delegate = delegate;
    }

    public boolean isMessagesSearchEndReached() {
        return this.messagesSearchEndReached;
    }

    public void loadMoreSearchMessages() {
        searchMessagesInternal(this.lastMessagesSearchString);
    }

    public String getLastSearchString() {
        return this.lastMessagesSearchString;
    }

    private void searchMessagesInternal(String query) {
        if (this.needMessagesSearch == 0) {
            return;
        }
        if ((this.lastMessagesSearchString != null && this.lastMessagesSearchString.length() != 0) || (query != null && query.length() != 0)) {
            if (this.reqId != 0) {
                ConnectionsManager.getInstance().cancelRequest(this.reqId, true);
                this.reqId = 0;
            }
            if (query == null || query.length() == 0) {
                this.searchResultMessages.clear();
                this.lastReqId = 0;
                this.lastMessagesSearchString = null;
                notifyDataSetChanged();
                if (this.delegate != null) {
                    this.delegate.searchStateChanged(false);
                    return;
                }
                return;
            }
            final TL_messages_searchGlobal req = new TL_messages_searchGlobal();
            req.limit = 20;
            req.f143q = query;
            if (this.lastMessagesSearchString == null || !query.equals(this.lastMessagesSearchString) || this.searchResultMessages.isEmpty()) {
                req.offset_date = 0;
                req.offset_id = 0;
                req.offset_peer = new TL_inputPeerEmpty();
            } else {
                int id;
                MessageObject lastMessage = (MessageObject) this.searchResultMessages.get(this.searchResultMessages.size() - 1);
                req.offset_id = lastMessage.getId();
                req.offset_date = lastMessage.messageOwner.date;
                if (lastMessage.messageOwner.to_id.channel_id != 0) {
                    id = -lastMessage.messageOwner.to_id.channel_id;
                } else if (lastMessage.messageOwner.to_id.chat_id != 0) {
                    id = -lastMessage.messageOwner.to_id.chat_id;
                } else {
                    id = lastMessage.messageOwner.to_id.user_id;
                }
                req.offset_peer = MessagesController.getInputPeer(id);
            }
            this.lastMessagesSearchString = query;
            final int currentReqId = this.lastReqId + 1;
            this.lastReqId = currentReqId;
            if (this.delegate != null) {
                this.delegate.searchStateChanged(true);
            }
            this.reqId = ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                public void run(final TLObject response, final TL_error error) {
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        public void run() {
                            boolean z = true;
                            if (currentReqId == DialogsSearchAdapter.this.lastReqId && error == null) {
                                messages_Messages res = response;
                                MessagesStorage.getInstance().putUsersAndChats(res.users, res.chats, true, true);
                                MessagesController.getInstance().putUsers(res.users, false);
                                MessagesController.getInstance().putChats(res.chats, false);
                                if (req.offset_id == 0) {
                                    DialogsSearchAdapter.this.searchResultMessages.clear();
                                }
                                Iterator i$ = res.messages.iterator();
                                while (i$.hasNext()) {
                                    DialogsSearchAdapter.this.searchResultMessages.add(new MessageObject((Message) i$.next(), null, false));
                                }
                                DialogsSearchAdapter dialogsSearchAdapter = DialogsSearchAdapter.this;
                                if (res.messages.size() == 20) {
                                    z = false;
                                }
                                dialogsSearchAdapter.messagesSearchEndReached = z;
                                DialogsSearchAdapter.this.notifyDataSetChanged();
                            }
                            if (DialogsSearchAdapter.this.delegate != null) {
                                DialogsSearchAdapter.this.delegate.searchStateChanged(false);
                            }
                            DialogsSearchAdapter.this.reqId = 0;
                        }
                    });
                }
            }, 2);
        }
    }

    public boolean hasRecentRearch() {
        return !this.recentSearchObjects.isEmpty();
    }

    public boolean isRecentSearchDisplayed() {
        return this.needMessagesSearch != 2 && ((this.lastSearchText == null || this.lastSearchText.length() == 0) && !this.recentSearchObjects.isEmpty());
    }

    public void loadRecentSearch() {
        MessagesStorage.getInstance().getStorageQueue().postRunnable(new C07432());
    }

    public void putRecentSearch(final long did, TLObject object) {
        RecentSearchObject recentSearchObject = (RecentSearchObject) this.recentSearchObjectsById.get(Long.valueOf(did));
        if (recentSearchObject == null) {
            recentSearchObject = new RecentSearchObject();
            this.recentSearchObjectsById.put(Long.valueOf(did), recentSearchObject);
        } else {
            this.recentSearchObjects.remove(recentSearchObject);
        }
        this.recentSearchObjects.add(0, recentSearchObject);
        recentSearchObject.did = did;
        recentSearchObject.object = object;
        recentSearchObject.date = ((int) System.currentTimeMillis()) / LocationStatusCodes.GEOFENCE_NOT_AVAILABLE;
        notifyDataSetChanged();
        MessagesStorage.getInstance().getStorageQueue().postRunnable(new Runnable() {
            public void run() {
                try {
                    SQLitePreparedStatement state = MessagesStorage.getInstance().getDatabase().executeFast("REPLACE INTO search_recent VALUES(?, ?)");
                    state.requery();
                    state.bindLong(1, did);
                    state.bindInteger(2, ((int) System.currentTimeMillis()) / LocationStatusCodes.GEOFENCE_NOT_AVAILABLE);
                    state.step();
                    state.dispose();
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
        });
    }

    public void clearRecentSearch() {
        this.recentSearchObjectsById = new HashMap();
        this.recentSearchObjects = new ArrayList();
        notifyDataSetChanged();
        MessagesStorage.getInstance().getStorageQueue().postRunnable(new C07454());
    }

    private void setRecentSearch(ArrayList<RecentSearchObject> arrayList, HashMap<Long, RecentSearchObject> hashMap) {
        this.recentSearchObjects = arrayList;
        this.recentSearchObjectsById = hashMap;
        for (int a = 0; a < this.recentSearchObjects.size(); a++) {
            RecentSearchObject recentSearchObject = (RecentSearchObject) this.recentSearchObjects.get(a);
            if (recentSearchObject.object instanceof User) {
                MessagesController.getInstance().putUser((User) recentSearchObject.object, true);
            } else if (recentSearchObject.object instanceof Chat) {
                MessagesController.getInstance().putChat((Chat) recentSearchObject.object, true);
            } else if (recentSearchObject.object instanceof EncryptedChat) {
                MessagesController.getInstance().putEncryptedChat((EncryptedChat) recentSearchObject.object, true);
            }
        }
        notifyDataSetChanged();
    }

    private void searchDialogsInternal(final String query, final int searchId) {
        if (this.needMessagesSearch != 2) {
            MessagesStorage.getInstance().getStorageQueue().postRunnable(new Runnable() {

                class C07461 implements Comparator<DialogSearchResult> {
                    C07461() {
                    }

                    public int compare(DialogSearchResult lhs, DialogSearchResult rhs) {
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
                        String search1 = query.trim().toLowerCase();
                        if (search1.length() == 0) {
                            DialogsSearchAdapter.this.lastSearchId = -1;
                            DialogsSearchAdapter.this.updateSearchResults(new ArrayList(), new ArrayList(), new ArrayList(), DialogsSearchAdapter.this.lastSearchId);
                            return;
                        }
                        DialogSearchResult dialogSearchResult;
                        String name;
                        String tName;
                        String username;
                        int usernamePos;
                        int found;
                        String[] arr$;
                        int len$;
                        int i$;
                        String q;
                        NativeByteBuffer data;
                        User user;
                        String search2 = LocaleController.getInstance().getTranslitString(search1);
                        if (search1.equals(search2) || search2.length() == 0) {
                            search2 = null;
                        }
                        String[] search = new String[((search2 != null ? 1 : 0) + 1)];
                        search[0] = search1;
                        if (search2 != null) {
                            search[1] = search2;
                        }
                        ArrayList<Integer> usersToLoad = new ArrayList();
                        ArrayList<Integer> chatsToLoad = new ArrayList();
                        ArrayList<Integer> encryptedToLoad = new ArrayList();
                        ArrayList<User> encUsers = new ArrayList();
                        int resultCount = 0;
                        HashMap<Long, DialogSearchResult> dialogsResult = new HashMap();
                        SQLiteCursor cursor = MessagesStorage.getInstance().getDatabase().queryFinalized("SELECT did, date FROM dialogs ORDER BY date DESC LIMIT 400", new Object[0]);
                        while (cursor.next()) {
                            long id = cursor.longValue(0);
                            dialogSearchResult = new DialogSearchResult();
                            dialogSearchResult.date = cursor.intValue(1);
                            dialogsResult.put(Long.valueOf(id), dialogSearchResult);
                            int lower_id = (int) id;
                            int high_id = (int) (id >> 32);
                            if (lower_id != 0) {
                                if (high_id == 1) {
                                    if (DialogsSearchAdapter.this.dialogsType == 0 && !chatsToLoad.contains(Integer.valueOf(lower_id))) {
                                        chatsToLoad.add(Integer.valueOf(lower_id));
                                    }
                                } else if (lower_id > 0) {
                                    if (!(DialogsSearchAdapter.this.dialogsType == 2 || usersToLoad.contains(Integer.valueOf(lower_id)))) {
                                        usersToLoad.add(Integer.valueOf(lower_id));
                                    }
                                } else if (!chatsToLoad.contains(Integer.valueOf(-lower_id))) {
                                    chatsToLoad.add(Integer.valueOf(-lower_id));
                                }
                            } else if (DialogsSearchAdapter.this.dialogsType == 0 && !encryptedToLoad.contains(Integer.valueOf(high_id))) {
                                encryptedToLoad.add(Integer.valueOf(high_id));
                            }
                        }
                        cursor.dispose();
                        if (!usersToLoad.isEmpty()) {
                            cursor = MessagesStorage.getInstance().getDatabase().queryFinalized(String.format(Locale.US, "SELECT data, status, name FROM users WHERE uid IN(%s)", new Object[]{TextUtils.join(",", usersToLoad)}), new Object[0]);
                            while (cursor.next()) {
                                name = cursor.stringValue(2);
                                tName = LocaleController.getInstance().getTranslitString(name);
                                if (name.equals(tName)) {
                                    tName = null;
                                }
                                username = null;
                                usernamePos = name.lastIndexOf(";;;");
                                if (usernamePos != -1) {
                                    username = name.substring(usernamePos + 3);
                                }
                                found = 0;
                                arr$ = search;
                                len$ = arr$.length;
                                i$ = 0;
                                while (i$ < len$) {
                                    q = arr$[i$];
                                    if (name.startsWith(q) || name.contains(" " + q) || (tName != null && (tName.startsWith(q) || tName.contains(" " + q)))) {
                                        found = 1;
                                    } else if (username != null && username.startsWith(q)) {
                                        found = 2;
                                    }
                                    if (found != 0) {
                                        data = new NativeByteBuffer(cursor.byteArrayLength(0));
                                        if (!(data == null || cursor.byteBufferValue(0, data) == 0)) {
                                            TLObject user2 = User.TLdeserialize(data, data.readInt32(false), false);
                                            dialogSearchResult = (DialogSearchResult) dialogsResult.get(Long.valueOf((long) user2.id));
                                            if (user2.status != null) {
                                                user2.status.expires = cursor.intValue(1);
                                            }
                                            if (found == 1) {
                                                dialogSearchResult.name = AndroidUtilities.generateSearchName(user2.first_name, user2.last_name, q);
                                            } else {
                                                dialogSearchResult.name = AndroidUtilities.generateSearchName("@" + user2.username, null, "@" + q);
                                            }
                                            dialogSearchResult.object = user2;
                                            resultCount++;
                                        }
                                        data.reuse();
                                    } else {
                                        i$++;
                                    }
                                }
                            }
                            cursor.dispose();
                        }
                        if (!chatsToLoad.isEmpty()) {
                            cursor = MessagesStorage.getInstance().getDatabase().queryFinalized(String.format(Locale.US, "SELECT data, name FROM chats WHERE uid IN(%s)", new Object[]{TextUtils.join(",", chatsToLoad)}), new Object[0]);
                            while (cursor.next()) {
                                name = cursor.stringValue(1);
                                tName = LocaleController.getInstance().getTranslitString(name);
                                if (name.equals(tName)) {
                                    tName = null;
                                }
                                arr$ = search;
                                len$ = arr$.length;
                                i$ = 0;
                                while (i$ < len$) {
                                    q = arr$[i$];
                                    if (name.startsWith(q) || name.contains(" " + q) || (tName != null && (tName.startsWith(q) || tName.contains(" " + q)))) {
                                        data = new NativeByteBuffer(cursor.byteArrayLength(0));
                                        if (!(data == null || cursor.byteBufferValue(0, data) == 0)) {
                                            Chat chat = Chat.TLdeserialize(data, data.readInt32(false), false);
                                            if (!(chat == null || chat.deactivated || (ChatObject.isChannel(chat) && ChatObject.isNotInChat(chat)))) {
                                                long dialog_id;
                                                if (chat.id > 0) {
                                                    dialog_id = (long) (-chat.id);
                                                } else {
                                                    dialog_id = AndroidUtilities.makeBroadcastId(chat.id);
                                                }
                                                dialogSearchResult = (DialogSearchResult) dialogsResult.get(Long.valueOf(dialog_id));
                                                dialogSearchResult.name = AndroidUtilities.generateSearchName(chat.title, null, q);
                                                dialogSearchResult.object = chat;
                                                resultCount++;
                                            }
                                        }
                                        data.reuse();
                                    } else {
                                        i$++;
                                    }
                                }
                            }
                            cursor.dispose();
                        }
                        if (!encryptedToLoad.isEmpty()) {
                            cursor = MessagesStorage.getInstance().getDatabase().queryFinalized(String.format(Locale.US, "SELECT q.data, u.name, q.user, q.g, q.authkey, q.ttl, u.data, u.status, q.layer, q.seq_in, q.seq_out, q.use_count, q.exchange_id, q.key_date, q.fprint, q.fauthkey, q.khash FROM enc_chats as q INNER JOIN users as u ON q.user = u.uid WHERE q.uid IN(%s)", new Object[]{TextUtils.join(",", encryptedToLoad)}), new Object[0]);
                            while (cursor.next()) {
                                name = cursor.stringValue(1);
                                tName = LocaleController.getInstance().getTranslitString(name);
                                if (name.equals(tName)) {
                                    tName = null;
                                }
                                username = null;
                                usernamePos = name.lastIndexOf(";;;");
                                if (usernamePos != -1) {
                                    username = name.substring(usernamePos + 2);
                                }
                                found = 0;
                                arr$ = search;
                                len$ = arr$.length;
                                i$ = 0;
                                while (i$ < len$) {
                                    q = arr$[i$];
                                    if (name.startsWith(q) || name.contains(" " + q) || (tName != null && (tName.startsWith(q) || tName.contains(" " + q)))) {
                                        found = 1;
                                    } else if (username != null && username.startsWith(q)) {
                                        found = 2;
                                    }
                                    if (found != 0) {
                                        data = new NativeByteBuffer(cursor.byteArrayLength(0));
                                        NativeByteBuffer data2 = new NativeByteBuffer(cursor.byteArrayLength(6));
                                        if (!(data == null || cursor.byteBufferValue(0, data) == 0 || cursor.byteBufferValue(6, data2) == 0)) {
                                            EncryptedChat chat2 = EncryptedChat.TLdeserialize(data, data.readInt32(false), false);
                                            dialogSearchResult = (DialogSearchResult) dialogsResult.get(Long.valueOf(((long) chat2.id) << 32));
                                            chat2.user_id = cursor.intValue(2);
                                            chat2.a_or_b = cursor.byteArrayValue(3);
                                            chat2.auth_key = cursor.byteArrayValue(4);
                                            chat2.ttl = cursor.intValue(5);
                                            chat2.layer = cursor.intValue(8);
                                            chat2.seq_in = cursor.intValue(9);
                                            chat2.seq_out = cursor.intValue(10);
                                            int use_count = cursor.intValue(11);
                                            chat2.key_use_count_in = (short) (use_count >> 16);
                                            chat2.key_use_count_out = (short) use_count;
                                            chat2.exchange_id = cursor.longValue(12);
                                            chat2.key_create_date = cursor.intValue(13);
                                            chat2.future_key_fingerprint = cursor.longValue(14);
                                            chat2.future_auth_key = cursor.byteArrayValue(15);
                                            chat2.key_hash = cursor.byteArrayValue(16);
                                            user = User.TLdeserialize(data2, data2.readInt32(false), false);
                                            if (user.status != null) {
                                                user.status.expires = cursor.intValue(7);
                                            }
                                            if (found == 1) {
                                                dialogSearchResult.name = AndroidUtilities.replaceTags("<c#ff00a60e>" + ContactsController.formatName(user.first_name, user.last_name) + "</c>");
                                            } else {
                                                dialogSearchResult.name = AndroidUtilities.generateSearchName("@" + user.username, null, "@" + q);
                                            }
                                            dialogSearchResult.object = chat2;
                                            encUsers.add(user);
                                            resultCount++;
                                        }
                                        data.reuse();
                                        data2.reuse();
                                    } else {
                                        i$++;
                                    }
                                }
                            }
                            cursor.dispose();
                        }
                        ArrayList<DialogSearchResult> arrayList = new ArrayList(resultCount);
                        for (DialogSearchResult dialogSearchResult2 : dialogsResult.values()) {
                            if (!(dialogSearchResult2.object == null || dialogSearchResult2.name == null)) {
                                arrayList.add(dialogSearchResult2);
                            }
                        }
                        Collections.sort(arrayList, new C07461());
                        ArrayList<TLObject> resultArray = new ArrayList();
                        ArrayList<CharSequence> resultArrayNames = new ArrayList();
                        for (int a = 0; a < arrayList.size(); a++) {
                            dialogSearchResult2 = (DialogSearchResult) arrayList.get(a);
                            resultArray.add(dialogSearchResult2.object);
                            resultArrayNames.add(dialogSearchResult2.name);
                        }
                        if (DialogsSearchAdapter.this.dialogsType != 2) {
                            cursor = MessagesStorage.getInstance().getDatabase().queryFinalized("SELECT u.data, u.status, u.name, u.uid FROM users as u INNER JOIN contacts as c ON u.uid = c.uid", new Object[0]);
                            while (cursor.next()) {
                                if (!dialogsResult.containsKey(Long.valueOf((long) cursor.intValue(3)))) {
                                    name = cursor.stringValue(2);
                                    tName = LocaleController.getInstance().getTranslitString(name);
                                    if (name.equals(tName)) {
                                        tName = null;
                                    }
                                    username = null;
                                    usernamePos = name.lastIndexOf(";;;");
                                    if (usernamePos != -1) {
                                        username = name.substring(usernamePos + 3);
                                    }
                                    found = 0;
                                    arr$ = search;
                                    len$ = arr$.length;
                                    i$ = 0;
                                    while (i$ < len$) {
                                        q = arr$[i$];
                                        if (name.startsWith(q) || name.contains(" " + q) || (tName != null && (tName.startsWith(q) || tName.contains(" " + q)))) {
                                            found = 1;
                                        } else if (username != null && username.startsWith(q)) {
                                            found = 2;
                                        }
                                        if (found != 0) {
                                            data = new NativeByteBuffer(cursor.byteArrayLength(0));
                                            if (!(data == null || cursor.byteBufferValue(0, data) == 0)) {
                                                user = User.TLdeserialize(data, data.readInt32(false), false);
                                                if (user.status != null) {
                                                    user.status.expires = cursor.intValue(1);
                                                }
                                                if (found == 1) {
                                                    resultArrayNames.add(AndroidUtilities.generateSearchName(user.first_name, user.last_name, q));
                                                } else {
                                                    resultArrayNames.add(AndroidUtilities.generateSearchName("@" + user.username, null, "@" + q));
                                                }
                                                resultArray.add(user);
                                            }
                                            data.reuse();
                                        } else {
                                            i$++;
                                        }
                                    }
                                }
                            }
                            cursor.dispose();
                        }
                        DialogsSearchAdapter.this.updateSearchResults(resultArray, resultArrayNames, encUsers, searchId);
                    } catch (Throwable e) {
                        FileLog.m611e("tmessages", e);
                    }
                }
            });
        }
    }

    private void updateSearchResults(ArrayList<TLObject> result, ArrayList<CharSequence> names, ArrayList<User> encUsers, int searchId) {
        final int i = searchId;
        final ArrayList<TLObject> arrayList = result;
        final ArrayList<User> arrayList2 = encUsers;
        final ArrayList<CharSequence> arrayList3 = names;
        AndroidUtilities.runOnUIThread(new Runnable() {
            public void run() {
                if (i == DialogsSearchAdapter.this.lastSearchId) {
                    Iterator i$ = arrayList.iterator();
                    while (i$.hasNext()) {
                        TLObject obj = (TLObject) i$.next();
                        if (obj instanceof User) {
                            MessagesController.getInstance().putUser((User) obj, true);
                        } else if (obj instanceof Chat) {
                            MessagesController.getInstance().putChat((Chat) obj, true);
                        } else if (obj instanceof EncryptedChat) {
                            MessagesController.getInstance().putEncryptedChat((EncryptedChat) obj, true);
                        }
                    }
                    i$ = arrayList2.iterator();
                    while (i$.hasNext()) {
                        MessagesController.getInstance().putUser((User) i$.next(), true);
                    }
                    DialogsSearchAdapter.this.searchResult = arrayList;
                    DialogsSearchAdapter.this.searchResultNames = arrayList3;
                    DialogsSearchAdapter.this.notifyDataSetChanged();
                }
            }
        });
    }

    public boolean isGlobalSearch(int i) {
        return i > this.searchResult.size() && i <= this.globalSearch.size() + this.searchResult.size();
    }

    public void clearRecentHashtags() {
        super.clearRecentHashtags();
        this.searchResultHashtags.clear();
        notifyDataSetChanged();
    }

    protected void setHashtags(ArrayList<HashtagObject> arrayList, HashMap<String, HashtagObject> hashMap) {
        super.setHashtags(arrayList, hashMap);
        Iterator i$ = arrayList.iterator();
        while (i$.hasNext()) {
            this.searchResultHashtags.add(((HashtagObject) i$.next()).hashtag);
        }
        if (this.delegate != null) {
            this.delegate.searchStateChanged(false);
        }
        notifyDataSetChanged();
    }

    public void searchDialogs(final String query) {
        if (query == null || this.lastSearchText == null || !query.equals(this.lastSearchText)) {
            this.lastSearchText = query;
            try {
                if (this.searchTimer != null) {
                    this.searchTimer.cancel();
                    this.searchTimer = null;
                }
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
            if (query == null || query.length() == 0) {
                this.hashtagsLoadedFromDb = false;
                this.searchResult.clear();
                this.searchResultNames.clear();
                this.searchResultHashtags.clear();
                if (this.needMessagesSearch != 2) {
                    queryServerSearch(null, true);
                }
                searchMessagesInternal(null);
                notifyDataSetChanged();
            } else if (this.needMessagesSearch != 2 && query.startsWith("#") && query.length() == 1) {
                this.messagesSearchEndReached = true;
                if (this.hashtagsLoadedFromDb) {
                    this.searchResultMessages.clear();
                    this.searchResultHashtags.clear();
                    Iterator i$ = this.hashtags.iterator();
                    while (i$.hasNext()) {
                        this.searchResultHashtags.add(((HashtagObject) i$.next()).hashtag);
                    }
                    if (this.delegate != null) {
                        this.delegate.searchStateChanged(false);
                    }
                    notifyDataSetChanged();
                    return;
                }
                loadRecentHashtags();
                if (this.delegate != null) {
                    this.delegate.searchStateChanged(true);
                }
                notifyDataSetChanged();
            } else {
                this.searchResultHashtags.clear();
                final int searchId = this.lastSearchId + 1;
                this.lastSearchId = searchId;
                this.searchTimer = new Timer();
                this.searchTimer.schedule(new TimerTask() {

                    class C07491 implements Runnable {
                        C07491() {
                        }

                        public void run() {
                            if (DialogsSearchAdapter.this.needMessagesSearch != 2) {
                                DialogsSearchAdapter.this.queryServerSearch(query, true);
                            }
                            DialogsSearchAdapter.this.searchMessagesInternal(query);
                        }
                    }

                    public void run() {
                        try {
                            cancel();
                            DialogsSearchAdapter.this.searchTimer.cancel();
                            DialogsSearchAdapter.this.searchTimer = null;
                        } catch (Throwable e) {
                            FileLog.m611e("tmessages", e);
                        }
                        DialogsSearchAdapter.this.searchDialogsInternal(query, searchId);
                        AndroidUtilities.runOnUIThread(new C07491());
                    }
                }, 200, 300);
            }
        }
    }

    public int getItemCount() {
        if (this.needMessagesSearch != 2 && ((this.lastSearchText == null || this.lastSearchText.length() == 0) && !this.recentSearchObjects.isEmpty())) {
            return this.recentSearchObjects.size() + 1;
        }
        if (!this.searchResultHashtags.isEmpty()) {
            return this.searchResultHashtags.size() + 1;
        }
        int count = this.searchResult.size();
        int globalCount = this.globalSearch.size();
        int messagesCount = this.searchResultMessages.size();
        if (globalCount != 0) {
            count += globalCount + 1;
        }
        if (messagesCount == 0) {
            return count;
        }
        return count + ((this.messagesSearchEndReached ? 0 : 1) + (messagesCount + 1));
    }

    public Object getItem(int i) {
        if (this.needMessagesSearch == 2 || (!(this.lastSearchText == null || this.lastSearchText.length() == 0) || this.recentSearchObjects.isEmpty())) {
            if (this.searchResultHashtags.isEmpty()) {
                int localCount = this.searchResult.size();
                int globalCount = this.globalSearch.isEmpty() ? 0 : this.globalSearch.size() + 1;
                int messagesCount = this.searchResultMessages.isEmpty() ? 0 : this.searchResultMessages.size() + 1;
                if (i >= 0 && i < localCount) {
                    return this.searchResult.get(i);
                }
                if (i > localCount && i < globalCount + localCount) {
                    return this.globalSearch.get((i - localCount) - 1);
                }
                if (i <= globalCount + localCount || i >= (globalCount + localCount) + messagesCount) {
                    return null;
                }
                return this.searchResultMessages.get(((i - localCount) - globalCount) - 1);
            } else if (i > 0) {
                return this.searchResultHashtags.get(i - 1);
            } else {
                return null;
            }
        } else if (i <= 0 || i - 1 >= this.recentSearchObjects.size()) {
            return null;
        } else {
            TLObject object = ((RecentSearchObject) this.recentSearchObjects.get(i - 1)).object;
            if (object instanceof User) {
                TLObject user = MessagesController.getInstance().getUser(Integer.valueOf(((User) object).id));
                if (user != null) {
                    return user;
                }
                return object;
            } else if (!(object instanceof Chat)) {
                return object;
            } else {
                TLObject chat = MessagesController.getInstance().getChat(Integer.valueOf(((Chat) object).id));
                if (chat != null) {
                    return chat;
                }
                return object;
            }
        }
    }

    public long getItemId(int i) {
        return (long) i;
    }

    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        switch (viewType) {
            case 0:
                view = new ProfileSearchCell(this.mContext);
                view.setBackgroundResource(C0553R.drawable.list_selector);
                break;
            case 1:
                view = new GreySectionCell(this.mContext);
                break;
            case 2:
                view = new DialogCell(this.mContext);
                break;
            case 3:
                view = new LoadingCell(this.mContext);
                break;
            case 4:
                view = new HashtagSearchCell(this.mContext);
                break;
        }
        return new Holder(view);
    }

    public void onBindViewHolder(ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case 0:
                TLObject tLObject;
                ProfileSearchCell cell = holder.itemView;
                TLObject user = null;
                TLObject chat = null;
                EncryptedChat encryptedChat = null;
                CharSequence username = null;
                CharSequence name = null;
                boolean isRecent = false;
                String un = null;
                TLObject obj = getItem(position);
                if (obj instanceof User) {
                    user = (User) obj;
                    un = user.username;
                } else if (obj instanceof Chat) {
                    chat = MessagesController.getInstance().getChat(Integer.valueOf(((Chat) obj).id));
                    if (chat == null) {
                        chat = (Chat) obj;
                    }
                    un = chat.username;
                } else if (obj instanceof EncryptedChat) {
                    encryptedChat = MessagesController.getInstance().getEncryptedChat(Integer.valueOf(((EncryptedChat) obj).id));
                    user = MessagesController.getInstance().getUser(Integer.valueOf(encryptedChat.user_id));
                }
                boolean z;
                if (this.needMessagesSearch == 2 || (!(this.lastSearchText == null || this.lastSearchText.length() == 0) || this.recentSearchObjects.isEmpty())) {
                    int localCount = this.searchResult.size();
                    z = (position == getItemCount() + -1 || position == localCount - 1 || position == (localCount + (this.globalSearch.isEmpty() ? 0 : this.globalSearch.size() + 1)) - 1) ? false : true;
                    cell.useSeparator = z;
                    if (position < this.searchResult.size()) {
                        name = (CharSequence) this.searchResultNames.get(position);
                        if (!(name == null || user == null || user.username == null || user.username.length() <= 0 || !name.toString().startsWith("@" + user.username))) {
                            username = name;
                            name = null;
                        }
                    } else if (position > this.searchResult.size() && un != null) {
                        String foundUserName = this.lastFoundUsername;
                        if (foundUserName.startsWith("@")) {
                            foundUserName = foundUserName.substring(1);
                        }
                        try {
                            username = AndroidUtilities.replaceTags(String.format("<c#ff4d83b3>@%s</c>%s", new Object[]{un.substring(0, foundUserName.length()), un.substring(foundUserName.length())}));
                        } catch (Throwable e) {
                            Object username2 = un;
                            FileLog.m611e("tmessages", e);
                        }
                    }
                } else {
                    isRecent = true;
                    if (position != getItemCount() - 1) {
                        z = true;
                    } else {
                        z = false;
                    }
                    cell.useSeparator = z;
                }
                if (user != null) {
                    tLObject = user;
                } else {
                    tLObject = chat;
                }
                cell.setData(tLObject, encryptedChat, name, username, isRecent);
                return;
            case 1:
                GreySectionCell cell2 = holder.itemView;
                if (this.needMessagesSearch != 2 && ((this.lastSearchText == null || this.lastSearchText.length() == 0) && !this.recentSearchObjects.isEmpty())) {
                    cell2.setText(LocaleController.getString("Recent", C0553R.string.Recent).toUpperCase());
                    return;
                } else if (!this.searchResultHashtags.isEmpty()) {
                    cell2.setText(LocaleController.getString("Hashtags", C0553R.string.Hashtags).toUpperCase());
                    return;
                } else if (this.globalSearch.isEmpty() || position != this.searchResult.size()) {
                    cell2.setText(LocaleController.getString("SearchMessages", C0553R.string.SearchMessages));
                    return;
                } else {
                    cell2.setText(LocaleController.getString("GlobalSearch", C0553R.string.GlobalSearch));
                    return;
                }
            case 2:
                DialogCell cell3 = holder.itemView;
                cell3.useSeparator = position != getItemCount() + -1;
                MessageObject messageObject = (MessageObject) getItem(position);
                cell3.setDialog(messageObject.getDialogId(), messageObject, messageObject.messageOwner.date);
                return;
            case 4:
                HashtagSearchCell cell4 = holder.itemView;
                cell4.setText((CharSequence) this.searchResultHashtags.get(position - 1));
                cell4.setNeedDivider(position != this.searchResultHashtags.size());
                return;
            default:
                return;
        }
    }

    public int getItemViewType(int i) {
        if (this.needMessagesSearch == 2 || (!(this.lastSearchText == null || this.lastSearchText.length() == 0) || this.recentSearchObjects.isEmpty())) {
            if (this.searchResultHashtags.isEmpty()) {
                int localCount = this.searchResult.size();
                int globalCount = this.globalSearch.isEmpty() ? 0 : this.globalSearch.size() + 1;
                int messagesCount = this.searchResultMessages.isEmpty() ? 0 : this.searchResultMessages.size() + 1;
                if ((i >= 0 && i < localCount) || (i > localCount && i < globalCount + localCount)) {
                    return 0;
                }
                if (i > globalCount + localCount && i < (globalCount + localCount) + messagesCount) {
                    return 2;
                }
                if (messagesCount == 0 || i != (globalCount + localCount) + messagesCount) {
                    return 1;
                }
                return 3;
            } else if (i != 0) {
                return 4;
            } else {
                return 1;
            }
        } else if (i == 0) {
            return 1;
        } else {
            return 0;
        }
    }
}
