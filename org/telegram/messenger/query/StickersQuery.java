package org.telegram.messenger.query;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Message;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import org.telegram.SQLite.SQLiteCursor;
import org.telegram.SQLite.SQLitePreparedStatement;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.C0553R;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.NativeByteBuffer;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.DocumentAttribute;
import org.telegram.tgnet.TLRPC.InputStickerSet;
import org.telegram.tgnet.TLRPC.StickerSet;
import org.telegram.tgnet.TLRPC.TL_documentAttributeSticker;
import org.telegram.tgnet.TLRPC.TL_documentEmpty;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_inputStickerSetID;
import org.telegram.tgnet.TLRPC.TL_messages_allStickers;
import org.telegram.tgnet.TLRPC.TL_messages_getAllStickers;
import org.telegram.tgnet.TLRPC.TL_messages_getStickerSet;
import org.telegram.tgnet.TLRPC.TL_messages_installStickerSet;
import org.telegram.tgnet.TLRPC.TL_messages_stickerSet;
import org.telegram.tgnet.TLRPC.TL_messages_uninstallStickerSet;
import org.telegram.tgnet.TLRPC.TL_stickerPack;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.StickersAlert;

public class StickersQuery {
    private static HashMap<String, ArrayList<Document>> allStickers = new HashMap();
    private static int loadDate;
    private static int loadHash;
    private static boolean loadingStickers;
    private static ArrayList<TL_messages_stickerSet> stickerSets = new ArrayList();
    private static HashMap<Long, TL_messages_stickerSet> stickerSetsById = new HashMap();
    private static HashMap<Long, String> stickersByEmoji = new HashMap();
    private static HashMap<Long, Document> stickersById = new HashMap();
    private static boolean stickersLoaded;

    static class C06312 implements Runnable {

        class C06301 implements Runnable {
            final /* synthetic */ TL_error val$error;
            final /* synthetic */ TLObject val$response;

            C06301(TLObject tLObject, TL_error tL_error) {
                this.val$response = tLObject;
                this.val$error = tL_error;
            }

            public void run() {
                if (this.val$response instanceof TL_messages_allStickers) {
                    final HashMap<Long, TL_messages_stickerSet> newStickerSets = new HashMap();
                    final ArrayList<TL_messages_stickerSet> newStickerArray = new ArrayList();
                    final TL_messages_allStickers res = this.val$response;
                    for (int a = 0; a < res.sets.size(); a++) {
                        final StickerSet stickerSet = (StickerSet) res.sets.get(a);
                        TL_messages_stickerSet oldSet = (TL_messages_stickerSet) StickersQuery.access$100().get(Long.valueOf(stickerSet.id));
                        if (oldSet == null || oldSet.set.hash != stickerSet.hash) {
                            newStickerArray.add(null);
                            final int index = a;
                            TL_messages_getStickerSet req = new TL_messages_getStickerSet();
                            req.stickerset = new TL_inputStickerSetID();
                            req.stickerset.id = stickerSet.id;
                            req.stickerset.access_hash = stickerSet.access_hash;
                            ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                                public void run(final TLObject response, TL_error error) {
                                    AndroidUtilities.runOnUIThread(new Runnable() {
                                        public void run() {
                                            TL_messages_stickerSet res1 = response;
                                            newStickerArray.set(index, res1);
                                            newStickerSets.put(Long.valueOf(stickerSet.id), res1);
                                            if (newStickerSets.size() == res.sets.size()) {
                                                StickersQuery.access$000(newStickerArray, false, (int) (System.currentTimeMillis() / 1000), res.hash);
                                            }
                                        }
                                    });
                                }
                            });
                        } else {
                            oldSet.set.disabled = stickerSet.disabled;
                            oldSet.set.installed = stickerSet.installed;
                            oldSet.set.official = stickerSet.official;
                            newStickerSets.put(Long.valueOf(oldSet.set.id), oldSet);
                            newStickerArray.add(oldSet);
                            if (newStickerSets.size() == res.sets.size()) {
                                StickersQuery.access$000(newStickerArray, false, (int) (System.currentTimeMillis() / 1000), res.hash);
                            }
                        }
                    }
                    return;
                }
                StickersQuery.access$000(null, false, (int) (System.currentTimeMillis() / 1000), this.val$error == null ? "" : null);
            }
        }

        C06312() {
        }

        public void run() {
            Throwable e;
            Throwable th;
            ArrayList<TL_messages_stickerSet> newStickerArray = null;
            int date = 0;
            int hash = 0;
            SQLiteCursor cursor = null;
            try {
                cursor = MessagesStorage.getInstance().getDatabase().queryFinalized("SELECT data, date, hash FROM stickers_v2 WHERE 1", new Object[0]);
                if (cursor.next()) {
                    NativeByteBuffer data = new NativeByteBuffer(cursor.byteArrayLength(0));
                    if (!(data == null || cursor.byteBufferValue(0, data) == 0)) {
                        ArrayList<TL_messages_stickerSet> newStickerArray2 = new ArrayList();
                        try {
                            int count = data.readInt32(false);
                            for (int a = 0; a < count; a++) {
                                newStickerArray2.add(TL_messages_stickerSet.TLdeserialize(data, data.readInt32(false), false));
                            }
                            newStickerArray = newStickerArray2;
                        } catch (Throwable th2) {
                            th = th2;
                            newStickerArray = newStickerArray2;
                            if (cursor != null) {
                                cursor.dispose();
                            }
                            throw th;
                        }
                    }
                    date = cursor.intValue(1);
                    hash = StickersQuery.calcStickersHash(newStickerArray);
                    data.reuse();
                }
                if (cursor != null) {
                    cursor.dispose();
                }
            } catch (Throwable th3) {
                e = th3;
                FileLog.m611e("tmessages", e);
                if (cursor != null) {
                    cursor.dispose();
                }
                StickersQuery.processLoadedStickers(newStickerArray, true, date, hash);
            }
            StickersQuery.processLoadedStickers(newStickerArray, true, date, hash);
        }
    }

    static class C06385 implements Runnable {

        class C06351 implements Runnable {
            C06351() {
            }

            public void run() {
                if (!(C06385.this.val$res == null || C06385.this.val$hash == null)) {
                    StickersQuery.access$402(C06385.this.val$hash);
                }
                StickersQuery.loadStickers(false, false);
            }
        }

        class C06362 implements Runnable {
            final /* synthetic */ HashMap val$allStickersNew;
            final /* synthetic */ HashMap val$stickerSetsByIdNew;
            final /* synthetic */ ArrayList val$stickerSetsNew;
            final /* synthetic */ HashMap val$stickersByEmojiNew;
            final /* synthetic */ HashMap val$stickersByIdNew;

            C06362(HashMap hashMap, HashMap hashMap2, ArrayList arrayList, HashMap hashMap3, HashMap hashMap4) {
                this.val$stickersByIdNew = hashMap;
                this.val$stickerSetsByIdNew = hashMap2;
                this.val$stickerSetsNew = arrayList;
                this.val$allStickersNew = hashMap3;
                this.val$stickersByEmojiNew = hashMap4;
            }

            public void run() {
                StickersQuery.access$602(this.val$stickersByIdNew);
                StickersQuery.access$102(this.val$stickerSetsByIdNew);
                StickersQuery.access$702(this.val$stickerSetsNew);
                StickersQuery.access$802(this.val$allStickersNew);
                StickersQuery.allStickers = this.val$stickersByEmojiNew;
                StickersQuery.access$402(C06385.this.val$hash);
                StickersQuery.access$1002(C06385.this.val$date);
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.stickersDidLoaded, new Object[0]);
            }
        }

        class C06373 implements Runnable {
            C06373() {
            }

            public void run() {
                StickersQuery.access$1002(C06385.this.val$date);
            }
        }

        C06385() {
        }

        public void run() {
            StickersQuery.loadingStickers = false;
            StickersQuery.stickersLoaded = true;
        }
    }

    static class C14779 implements RequestDelegate {

        class C06521 implements Runnable {
            C06521() {
            }

            public void run() {
                StickersQuery.loadStickers(false, false);
            }
        }

        C14779() {
        }

        public void run(TLObject response, TL_error error) {
            AndroidUtilities.runOnUIThread(new C06521(), 1000);
        }
    }

    public static void cleanup() {
        loadHash = 0;
        loadDate = 0;
        allStickers.clear();
        stickerSets.clear();
        stickersByEmoji.clear();
        stickerSetsById.clear();
        loadingStickers = false;
        stickersLoaded = false;
    }

    public static void checkStickers() {
        if (!loadingStickers) {
            if (!stickersLoaded || ((long) loadDate) < (System.currentTimeMillis() / 1000) - 3600) {
                loadStickers(true, false);
            }
        }
    }

    public static boolean isLoadingStickers() {
        return loadingStickers;
    }

    public static Document getStickerById(long id) {
        Document document = (Document) stickersById.get(Long.valueOf(id));
        if (document == null) {
            return document;
        }
        TL_messages_stickerSet stickerSet = (TL_messages_stickerSet) stickerSetsById.get(Long.valueOf(getStickerSetId(document)));
        if (stickerSet == null || !stickerSet.set.disabled) {
            return document;
        }
        return null;
    }

    public static HashMap<String, ArrayList<Document>> getAllStickers() {
        return allStickers;
    }

    public static ArrayList<TL_messages_stickerSet> getStickerSets() {
        return stickerSets;
    }

    public static boolean isStickerPackInstalled(long id) {
        return stickerSetsById.containsKey(Long.valueOf(id));
    }

    public static String getEmojiForSticker(long id) {
        String value = (String) stickersByEmoji.get(Long.valueOf(id));
        return value != null ? value : "";
    }

    public static void reorderStickers(final ArrayList<Long> order) {
        Collections.sort(stickerSets, new Comparator<TL_messages_stickerSet>() {
            public int compare(TL_messages_stickerSet lhs, TL_messages_stickerSet rhs) {
                int index1 = order.indexOf(Long.valueOf(lhs.set.id));
                int index2 = order.indexOf(Long.valueOf(rhs.set.id));
                if (index1 > index2) {
                    return 1;
                }
                if (index1 < index2) {
                    return -1;
                }
                return 0;
            }
        });
        loadHash = calcStickersHash(stickerSets);
        NotificationCenter.getInstance().postNotificationName(NotificationCenter.stickersDidLoaded, new Object[0]);
        loadStickers(false, true);
    }

    public static void calcNewHash() {
        loadHash = calcStickersHash(stickerSets);
    }

    public static void addNewStickerSet(TL_messages_stickerSet set) {
        if (!stickerSetsById.containsKey(Long.valueOf(set.set.id))) {
            int a;
            stickerSets.add(0, set);
            stickerSetsById.put(Long.valueOf(set.set.id), set);
            for (a = 0; a < set.documents.size(); a++) {
                Document document = (Document) set.documents.get(a);
                stickersById.put(Long.valueOf(document.id), document);
            }
            for (a = 0; a < set.packs.size(); a++) {
                TL_stickerPack stickerPack = (TL_stickerPack) set.packs.get(a);
                stickerPack.emoticon = stickerPack.emoticon.replace("️", "");
                ArrayList<Document> arrayList = (ArrayList) allStickers.get(stickerPack.emoticon);
                if (arrayList == null) {
                    arrayList = new ArrayList();
                    allStickers.put(stickerPack.emoticon, arrayList);
                }
                for (int c = 0; c < stickerPack.documents.size(); c++) {
                    Long id = (Long) stickerPack.documents.get(c);
                    if (!stickersByEmoji.containsKey(id)) {
                        stickersByEmoji.put(id, stickerPack.emoticon);
                    }
                    arrayList.add(stickersById.get(id));
                }
            }
            loadHash = calcStickersHash(stickerSets);
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.stickersDidLoaded, new Object[0]);
        }
        loadStickers(false, true);
    }

    public static void loadStickers(boolean cache, boolean force) {
        if (!loadingStickers) {
            loadingStickers = true;
            if (cache) {
                MessagesStorage.getInstance().getStorageQueue().postRunnable(new C06312());
                return;
            }
            final TL_messages_getAllStickers req = new TL_messages_getAllStickers();
            req.hash = force ? 0 : loadHash;
            ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                public void run(final TLObject response, TL_error error) {
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        public void run() {
                            if (response instanceof TL_messages_allStickers) {
                                final HashMap<Long, TL_messages_stickerSet> newStickerSets = new HashMap();
                                final ArrayList<TL_messages_stickerSet> newStickerArray = new ArrayList();
                                final TL_messages_allStickers res = response;
                                for (int a = 0; a < res.sets.size(); a++) {
                                    final StickerSet stickerSet = (StickerSet) res.sets.get(a);
                                    TL_messages_stickerSet oldSet = (TL_messages_stickerSet) StickersQuery.stickerSetsById.get(Long.valueOf(stickerSet.id));
                                    if (oldSet == null || oldSet.set.hash != stickerSet.hash) {
                                        newStickerArray.add(null);
                                        final int index = a;
                                        TL_messages_getStickerSet req = new TL_messages_getStickerSet();
                                        req.stickerset = new TL_inputStickerSetID();
                                        req.stickerset.id = stickerSet.id;
                                        req.stickerset.access_hash = stickerSet.access_hash;
                                        ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                                            public void run(final TLObject response, TL_error error) {
                                                AndroidUtilities.runOnUIThread(new Runnable() {
                                                    public void run() {
                                                        TL_messages_stickerSet res1 = response;
                                                        newStickerArray.set(index, res1);
                                                        newStickerSets.put(Long.valueOf(stickerSet.id), res1);
                                                        if (newStickerSets.size() == res.sets.size()) {
                                                            StickersQuery.processLoadedStickers(newStickerArray, false, (int) (System.currentTimeMillis() / 1000), res.hash);
                                                        }
                                                    }
                                                });
                                            }
                                        });
                                    } else {
                                        oldSet.set.disabled = stickerSet.disabled;
                                        oldSet.set.installed = stickerSet.installed;
                                        oldSet.set.official = stickerSet.official;
                                        newStickerSets.put(Long.valueOf(oldSet.set.id), oldSet);
                                        newStickerArray.add(oldSet);
                                        if (newStickerSets.size() == res.sets.size()) {
                                            StickersQuery.processLoadedStickers(newStickerArray, false, (int) (System.currentTimeMillis() / 1000), res.hash);
                                        }
                                    }
                                }
                                return;
                            }
                            StickersQuery.processLoadedStickers(null, false, (int) (System.currentTimeMillis() / 1000), req.hash);
                        }
                    });
                }
            });
        }
    }

    private static void putStickersToCache(final ArrayList<TL_messages_stickerSet> stickers, final int date, final int hash) {
        MessagesStorage.getInstance().getStorageQueue().postRunnable(new Runnable() {
            public void run() {
                try {
                    SQLitePreparedStatement state;
                    if (stickers != null) {
                        int a;
                        state = MessagesStorage.getInstance().getDatabase().executeFast("REPLACE INTO stickers_v2 VALUES(?, ?, ?, ?)");
                        state.requery();
                        int size = 4;
                        for (a = 0; a < stickers.size(); a++) {
                            size += ((TL_messages_stickerSet) stickers.get(a)).getObjectSize();
                        }
                        NativeByteBuffer data = new NativeByteBuffer(size);
                        data.writeInt32(stickers.size());
                        for (a = 0; a < stickers.size(); a++) {
                            ((TL_messages_stickerSet) stickers.get(a)).serializeToStream(data);
                        }
                        state.bindInteger(1, 1);
                        state.bindByteBuffer(2, data);
                        state.bindInteger(3, date);
                        state.bindInteger(4, hash);
                        state.step();
                        data.reuse();
                        state.dispose();
                        return;
                    }
                    state = MessagesStorage.getInstance().getDatabase().executeFast("UPDATE stickers_v2 SET date = ?");
                    state.requery();
                    state.bindInteger(1, date);
                    state.step();
                    state.dispose();
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
        });
    }

    public static long getStickerSetId(Document document) {
        for (int a = 0; a < document.attributes.size(); a++) {
            DocumentAttribute attribute = (DocumentAttribute) document.attributes.get(a);
            if (attribute instanceof TL_documentAttributeSticker) {
                if (attribute.stickerset instanceof TL_inputStickerSetID) {
                    return attribute.stickerset.id;
                }
                return -1;
            }
        }
        return -1;
    }

    private static int calcStickersHash(ArrayList<TL_messages_stickerSet> sets) {
        long acc = 0;
        for (int a = 0; a < sets.size(); a++) {
            StickerSet set = ((TL_messages_stickerSet) sets.get(a)).set;
            if (!set.disabled) {
                acc = (((20261 * acc) + 2147483648L) + ((long) set.hash)) % 2147483648L;
            }
        }
        return (int) acc;
    }

    private static void processLoadedStickers(final ArrayList<TL_messages_stickerSet> res, final boolean cache, final int date, final int hash) {
        AndroidUtilities.runOnUIThread(new C06385());
        Utilities.stageQueue.postRunnable(new Runnable() {

            class C06421 implements Runnable {

                class C06401 implements OnClickListener {

                    class C14741 implements RequestDelegate {
                        C14741() {
                        }

                        public void run(TLObject response, final TL_error error) {
                            AndroidUtilities.runOnUIThread(new Runnable() {
                                public void run() {
                                    if (C06456.this.val$fragment.getParentActivity() != null) {
                                        if (error == null) {
                                            Toast.makeText(C06456.this.val$fragment.getParentActivity(), LocaleController.getString("AddStickersInstalled", C0553R.string.AddStickersInstalled), 0).show();
                                        } else {
                                            Toast.makeText(C06456.this.val$fragment.getParentActivity(), LocaleController.getString("ErrorOccurred", C0553R.string.Enhance), 0).show();
                                        }
                                    }
                                    StickersQuery.loadStickers(false, true);
                                }
                            });
                        }
                    }

                    C06401() {
                    }

                    public void onClick(DialogInterface dialog, int which) {
                        TL_messages_installStickerSet req = new TL_messages_installStickerSet();
                        req.stickerset = C06456.this.val$stickerSet;
                        ConnectionsManager.getInstance().sendRequest(req, new C14741());
                    }
                }

                class C06412 implements OnClickListener {
                    final /* synthetic */ TL_messages_stickerSet val$res;

                    C06412(TL_messages_stickerSet tL_messages_stickerSet) {
                        this.val$res = tL_messages_stickerSet;
                    }

                    public void onClick(DialogInterface dialog, int which) {
                        StickersQuery.removeStickersSet(C06456.this.val$fragment.getParentActivity(), this.val$res.set, 0);
                    }
                }

                C06421() {
                }

                public void run() {
                    if (!(res == null || hash == 0)) {
                        StickersQuery.loadHash = hash;
                    }
                    StickersQuery.loadStickers(false, false);
                }
            }

            class C06443 implements Runnable {
                C06443() {
                }

                public void run() {
                    StickersQuery.loadDate = date;
                }
            }

            public void run() {
                if ((cache && (res == null || date < ((int) ((System.currentTimeMillis() / 1000) - 3600)))) || (!cache && res == null && hash == 0)) {
                    C06421 c06421 = new C06421();
                    long j = (res != null || cache) ? 0 : 1000;
                    AndroidUtilities.runOnUIThread(c06421, j);
                    if (res == null) {
                        return;
                    }
                }
                if (res != null) {
                    try {
                        final ArrayList<TL_messages_stickerSet> stickerSetsNew = new ArrayList();
                        final HashMap<Long, TL_messages_stickerSet> stickerSetsByIdNew = new HashMap();
                        final HashMap<Long, String> stickersByEmojiNew = new HashMap();
                        final HashMap<Long, Document> stickersByIdNew = new HashMap();
                        final HashMap<String, ArrayList<Document>> allStickersNew = new HashMap();
                        for (int a = 0; a < res.size(); a++) {
                            TL_messages_stickerSet stickerSet = (TL_messages_stickerSet) res.get(a);
                            if (stickerSet != null) {
                                int b;
                                stickerSetsNew.add(stickerSet);
                                stickerSetsByIdNew.put(Long.valueOf(stickerSet.set.id), stickerSet);
                                for (b = 0; b < stickerSet.documents.size(); b++) {
                                    Document document = (Document) stickerSet.documents.get(b);
                                    if (!(document == null || (document instanceof TL_documentEmpty))) {
                                        stickersByIdNew.put(Long.valueOf(document.id), document);
                                    }
                                }
                                if (!stickerSet.set.disabled) {
                                    for (b = 0; b < stickerSet.packs.size(); b++) {
                                        TL_stickerPack stickerPack = (TL_stickerPack) stickerSet.packs.get(b);
                                        if (!(stickerPack == null || stickerPack.emoticon == null)) {
                                            stickerPack.emoticon = stickerPack.emoticon.replace("️", "");
                                            ArrayList<Document> arrayList = (ArrayList) allStickersNew.get(stickerPack.emoticon);
                                            if (arrayList == null) {
                                                arrayList = new ArrayList();
                                                allStickersNew.put(stickerPack.emoticon, arrayList);
                                            }
                                            for (int c = 0; c < stickerPack.documents.size(); c++) {
                                                Long id = (Long) stickerPack.documents.get(c);
                                                if (!stickersByEmojiNew.containsKey(id)) {
                                                    stickersByEmojiNew.put(id, stickerPack.emoticon);
                                                }
                                                arrayList.add(stickersByIdNew.get(id));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (!cache) {
                            StickersQuery.putStickersToCache(stickerSetsNew, date, hash);
                        }
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            public void run() {
                                StickersQuery.stickersById = stickersByIdNew;
                                StickersQuery.stickerSetsById = stickerSetsByIdNew;
                                StickersQuery.stickerSets = stickerSetsNew;
                                StickersQuery.allStickers = allStickersNew;
                                StickersQuery.stickersByEmoji = stickersByEmojiNew;
                                StickersQuery.loadHash = hash;
                                StickersQuery.loadDate = date;
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.stickersDidLoaded, new Object[0]);
                            }
                        });
                    } catch (Throwable e) {
                        FileLog.m611e("tmessages", e);
                    }
                } else if (!cache) {
                    AndroidUtilities.runOnUIThread(new C06443());
                    StickersQuery.putStickersToCache(null, date, 0);
                }
            }
        });
    }

    public static void loadStickers(final BaseFragment fragment, final InputStickerSet stickerSet) {
        if (fragment != null && stickerSet != null) {
            final ProgressDialog progressDialog = new ProgressDialog(fragment.getParentActivity());
            progressDialog.setMessage(LocaleController.getString("Loading", C0553R.string.Loading));
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setCancelable(false);
            TL_messages_getStickerSet req = new TL_messages_getStickerSet();
            req.stickerset = stickerSet;
            final int reqId = ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                public void run(final TLObject response, final TL_error error) {
                    AndroidUtilities.runOnUIThread(new Runnable() {

                        class C06471 implements OnClickListener {

                            class C14751 implements RequestDelegate {
                                C14751() {
                                }

                                public void run(TLObject response, final TL_error error) {
                                    AndroidUtilities.runOnUIThread(new Runnable() {
                                        public void run() {
                                            if (fragment.getParentActivity() != null) {
                                                if (error == null) {
                                                    Toast.makeText(fragment.getParentActivity(), LocaleController.getString("AddStickersInstalled", C0553R.string.AddStickersInstalled), 0).show();
                                                } else {
                                                    Toast.makeText(fragment.getParentActivity(), LocaleController.getString("ErrorOccurred", C0553R.string.ErrorOccurred), 0).show();
                                                }
                                            }
                                            StickersQuery.loadStickers(false, true);
                                        }
                                    });
                                }
                            }

                            C06471() {
                            }

                            public void onClick(DialogInterface dialog, int which) {
                                TL_messages_installStickerSet req = new TL_messages_installStickerSet();
                                req.stickerset = stickerSet;
                                ConnectionsManager.getInstance().sendRequest(req, new C14751());
                            }
                        }

                        public void run() {
                            try {
                                progressDialog.dismiss();
                            } catch (Throwable e) {
                                FileLog.m611e("tmessages", e);
                            }
                            if (fragment.getParentActivity() != null && !fragment.getParentActivity().isFinishing()) {
                                if (error == null) {
                                    final TL_messages_stickerSet res = response;
                                    StickersAlert alert = new StickersAlert(fragment.getParentActivity(), res);
                                    if (res.set == null || !StickersQuery.isStickerPackInstalled(res.set.id)) {
                                        alert.setButton(-1, LocaleController.getString("AddStickers", C0553R.string.AddStickers), new C06471());
                                    } else {
                                        alert.setButton(-3, LocaleController.getString("StickersRemove", C0553R.string.StickersRemove), new OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                StickersQuery.removeStickersSet(fragment.getParentActivity(), res.set, 0);
                                            }
                                        });
                                    }
                                    alert.setButton(-2, LocaleController.getString("Close", C0553R.string.Close), (Message) null);
                                    fragment.setVisibleDialog(alert);
                                    alert.show();
                                    return;
                                }
                                Toast.makeText(fragment.getParentActivity(), LocaleController.getString("AddStickersNotFound", C0553R.string.AddStickersNotFound), 0).show();
                            }
                        }
                    });
                }
            });
            progressDialog.setButton(-2, LocaleController.getString("Cancel", C0553R.string.Cancel), new OnClickListener() {

                class C06501 implements Runnable {
                    C06501() {
                    }

                    public void run() {
                        StickersQuery.loadStickers(false, true);
                    }
                }

                public void onClick(DialogInterface dialog, int which) {
                    ConnectionsManager.getInstance().cancelRequest(reqId, true);
                    try {
                        dialog.dismiss();
                    } catch (Throwable e) {
                        FileLog.m611e("tmessages", e);
                    }
                }
            });
            fragment.setVisibleDialog(progressDialog);
            progressDialog.show();
        }
    }

    public static void removeStickersSet(final Context context, StickerSet stickerSet, int hide) {
        boolean z = true;
        TL_inputStickerSetID stickerSetID = new TL_inputStickerSetID();
        stickerSetID.access_hash = stickerSet.access_hash;
        stickerSetID.id = stickerSet.id;
        if (hide != 0) {
            TL_messages_installStickerSet req;
            stickerSet.disabled = hide == 1;
            for (int a = 0; a < stickerSets.size(); a++) {
                TL_messages_stickerSet set = (TL_messages_stickerSet) stickerSets.get(a);
                if (set.set.id == stickerSet.id) {
                    stickerSets.remove(a);
                    if (hide != 2) {
                        for (int b = stickerSets.size() - 1; b >= 0; b--) {
                            if (!((TL_messages_stickerSet) stickerSets.get(b)).set.disabled) {
                                stickerSets.add(b + 1, set);
                                break;
                            }
                        }
                    } else {
                        stickerSets.add(0, set);
                    }
                    loadHash = calcStickersHash(stickerSets);
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.stickersDidLoaded, new Object[0]);
                    req = new TL_messages_installStickerSet();
                    req.stickerset = stickerSetID;
                    if (hide != 1) {
                        z = false;
                    }
                    req.disabled = z;
                    ConnectionsManager.getInstance().sendRequest(req, new C14779());
                    return;
                }
            }
            loadHash = calcStickersHash(stickerSets);
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.stickersDidLoaded, new Object[0]);
            req = new TL_messages_installStickerSet();
            req.stickerset = stickerSetID;
            if (hide != 1) {
                z = false;
            }
            req.disabled = z;
            ConnectionsManager.getInstance().sendRequest(req, new C14779());
            return;
        }
        TL_messages_uninstallStickerSet req2 = new TL_messages_uninstallStickerSet();
        req2.stickerset = stickerSetID;
        ConnectionsManager.getInstance().sendRequest(req2, new RequestDelegate() {
            public void run(TLObject response, final TL_error error) {
                AndroidUtilities.runOnUIThread(new Runnable() {
                    public void run() {
                        try {
                            if (error == null) {
                                Toast.makeText(context, LocaleController.getString("StickersRemoved", C0553R.string.StickersRemoved), 0).show();
                            } else {
                                Toast.makeText(context, LocaleController.getString("ErrorOccurred", C0553R.string.ErrorOccurred), 0).show();
                            }
                        } catch (Throwable e) {
                            FileLog.m611e("tmessages", e);
                        }
                        StickersQuery.loadStickers(false, true);
                    }
                });
            }
        });
    }
}
