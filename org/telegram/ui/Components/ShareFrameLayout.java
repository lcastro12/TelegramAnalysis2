package org.telegram.ui.Components;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import org.telegram.SQLite.SQLiteCursor;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.C0553R;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.tgnet.NativeByteBuffer;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.Dialog;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.Adapters.BaseFragmentAdapter;
import org.telegram.ui.Cells.ShareDialogCell;

public class ShareFrameLayout extends FrameLayout {
    private LinearLayout doneButton;
    private TextView doneButtonBadgeTextView;
    private TextView doneButtonTextView;
    private GridView gridView;
    private ShareDialogsAdapter listAdapter;
    private EditText nameTextView;
    private BottomSheet parentBottomSheet;
    private ShareSearchAdapter searchAdapter;
    private EmptyTextProgressView searchEmptyView;
    private HashMap<Long, Dialog> selectedDialogs = new HashMap();
    private MessageObject sendingMessageObject;

    class C09611 implements OnClickListener {
        C09611() {
        }

        public void onClick(View v) {
            ArrayList<MessageObject> arrayList = new ArrayList();
            arrayList.add(ShareFrameLayout.this.sendingMessageObject);
            for (Entry<Long, Dialog> entry : ShareFrameLayout.this.selectedDialogs.entrySet()) {
                boolean asAdmin = true;
                int lower_id = (int) ((Dialog) entry.getValue()).id;
                if (lower_id < 0 && MessagesController.getInstance().getChat(Integer.valueOf(-lower_id)).megagroup) {
                    asAdmin = false;
                }
                SendMessagesHelper.getInstance().sendMessage(arrayList, ((Long) entry.getKey()).longValue(), asAdmin);
            }
            ShareFrameLayout.this.parentBottomSheet.dismiss();
        }
    }

    class C09622 implements TextWatcher {
        C09622() {
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        public void afterTextChanged(Editable s) {
            String text = ShareFrameLayout.this.nameTextView.getText().toString();
            if (text.length() != 0) {
                if (ShareFrameLayout.this.gridView.getAdapter() != ShareFrameLayout.this.searchAdapter) {
                    ShareFrameLayout.this.gridView.setAdapter(ShareFrameLayout.this.searchAdapter);
                    ShareFrameLayout.this.searchAdapter.notifyDataSetChanged();
                }
                if (ShareFrameLayout.this.searchEmptyView != null) {
                    ShareFrameLayout.this.searchEmptyView.setText(LocaleController.getString("NoResult", C0553R.string.NoResult));
                }
            } else if (ShareFrameLayout.this.gridView.getAdapter() != ShareFrameLayout.this.listAdapter) {
                ShareFrameLayout.this.searchEmptyView.setText(LocaleController.getString("NoChats", C0553R.string.NoChats));
                ShareFrameLayout.this.gridView.setAdapter(ShareFrameLayout.this.listAdapter);
                ShareFrameLayout.this.listAdapter.notifyDataSetChanged();
            }
            if (ShareFrameLayout.this.searchAdapter != null) {
                ShareFrameLayout.this.searchAdapter.searchDialogs(text);
            }
        }
    }

    class C09633 implements OnTouchListener {
        C09633() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            return true;
        }
    }

    class C09644 implements OnItemClickListener {
        C09644() {
        }

        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Dialog dialog;
            if (ShareFrameLayout.this.gridView.getAdapter() == ShareFrameLayout.this.listAdapter) {
                dialog = ShareFrameLayout.this.listAdapter.getItem(i);
            } else {
                dialog = ShareFrameLayout.this.searchAdapter.getItem(i);
            }
            ShareDialogCell cell = (ShareDialogCell) view;
            if (ShareFrameLayout.this.selectedDialogs.containsKey(Long.valueOf(dialog.id))) {
                ShareFrameLayout.this.selectedDialogs.remove(Long.valueOf(dialog.id));
                cell.setChecked(false, true);
            } else {
                ShareFrameLayout.this.selectedDialogs.put(Long.valueOf(dialog.id), dialog);
                cell.setChecked(true, true);
            }
            ShareFrameLayout.this.updateSelectedCount(ShareFrameLayout.this.selectedDialogs.size(), true);
        }
    }

    private class ShareDialogsAdapter extends BaseFragmentAdapter {
        private int currentCount;
        private ArrayList<Dialog> dialogs = new ArrayList();
        private Context mContext;

        public ShareDialogsAdapter(Context context) {
            this.mContext = context;
            for (int a = 0; a < MessagesController.getInstance().dialogsServerOnly.size(); a++) {
                Dialog dialog = (Dialog) MessagesController.getInstance().dialogsServerOnly.get(a);
                int lower_id = (int) dialog.id;
                int high_id = (int) (dialog.id >> 32);
                if (!(lower_id == 0 || high_id == 1)) {
                    if (lower_id > 0) {
                        this.dialogs.add(dialog);
                    } else {
                        Chat chat = MessagesController.getInstance().getChat(Integer.valueOf(-lower_id));
                        if (!(chat == null || ChatObject.isNotInChat(chat) || (ChatObject.isChannel(chat) && !chat.creator && !chat.editor && !chat.megagroup))) {
                            this.dialogs.add(dialog);
                        }
                    }
                }
            }
        }

        public int getCount() {
            return this.dialogs.size();
        }

        public Dialog getItem(int i) {
            if (i < 0 || i >= this.dialogs.size()) {
                return null;
            }
            return (Dialog) this.dialogs.get(i);
        }

        public long getItemId(int i) {
            return (long) i;
        }

        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = new ShareDialogCell(this.mContext);
            }
            ShareDialogCell cell = (ShareDialogCell) view;
            Dialog dialog = getItem(i);
            cell.setDialog(dialog, ShareFrameLayout.this.selectedDialogs.containsKey(Long.valueOf(dialog.id)), null);
            return view;
        }

        public int getItemViewType(int i) {
            return 0;
        }

        public int getViewTypeCount() {
            return 1;
        }

        public boolean isEmpty() {
            return getCount() == 0;
        }
    }

    public class ShareSearchAdapter extends BaseFragmentAdapter {
        private int lastReqId;
        private int lastSearchId = 0;
        private String lastSearchText;
        private Context mContext;
        private int reqId = 0;
        private ArrayList<DialogSearchResult> searchResult = new ArrayList();
        private Timer searchTimer;

        private class DialogSearchResult {
            public int date;
            public Dialog dialog;
            public CharSequence name;
            public TLObject object;

            private DialogSearchResult() {
                this.dialog = new Dialog();
            }
        }

        public ShareSearchAdapter(Context context) {
            this.mContext = context;
        }

        private void searchDialogsInternal(final String query, final int searchId) {
            MessagesStorage.getInstance().getStorageQueue().postRunnable(new Runnable() {

                class C09651 implements Comparator<DialogSearchResult> {
                    C09651() {
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
                            ShareSearchAdapter.this.lastSearchId = -1;
                            ShareSearchAdapter.this.updateSearchResults(new ArrayList(), ShareSearchAdapter.this.lastSearchId);
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
                        TLObject user;
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
                            if (!(lower_id == 0 || high_id == 1)) {
                                if (lower_id > 0) {
                                    if (!usersToLoad.contains(Integer.valueOf(lower_id))) {
                                        usersToLoad.add(Integer.valueOf(lower_id));
                                    }
                                } else if (!chatsToLoad.contains(Integer.valueOf(-lower_id))) {
                                    chatsToLoad.add(Integer.valueOf(-lower_id));
                                }
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
                                            user = User.TLdeserialize(data, data.readInt32(false), false);
                                            dialogSearchResult = (DialogSearchResult) dialogsResult.get(Long.valueOf((long) user.id));
                                            if (user.status != null) {
                                                user.status.expires = cursor.intValue(1);
                                            }
                                            if (found == 1) {
                                                dialogSearchResult.name = AndroidUtilities.generateSearchName(user.first_name, user.last_name, q);
                                            } else {
                                                dialogSearchResult.name = AndroidUtilities.generateSearchName("@" + user.username, null, "@" + q);
                                            }
                                            dialogSearchResult.object = user;
                                            dialogSearchResult.dialog.id = (long) user.id;
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
                                int a = 0;
                                while (a < search.length) {
                                    q = search[a];
                                    if (name.startsWith(q) || name.contains(" " + q) || (tName != null && (tName.startsWith(q) || tName.contains(" " + q)))) {
                                        data = new NativeByteBuffer(cursor.byteArrayLength(0));
                                        if (!(data == null || cursor.byteBufferValue(0, data) == 0)) {
                                            Chat chat = Chat.TLdeserialize(data, data.readInt32(false), false);
                                            if (!(chat == null || ChatObject.isNotInChat(chat) || (ChatObject.isChannel(chat) && !chat.creator && !chat.editor && !chat.megagroup))) {
                                                dialogSearchResult = (DialogSearchResult) dialogsResult.get(Long.valueOf(-((long) chat.id)));
                                                dialogSearchResult.name = AndroidUtilities.generateSearchName(chat.title, null, q);
                                                dialogSearchResult.object = chat;
                                                dialogSearchResult.dialog.id = (long) (-chat.id);
                                                resultCount++;
                                            }
                                        }
                                        data.reuse();
                                    } else {
                                        a++;
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
                                            dialogSearchResult2 = new DialogSearchResult();
                                            if (user.status != null) {
                                                user.status.expires = cursor.intValue(1);
                                            }
                                            dialogSearchResult2.dialog.id = (long) user.id;
                                            dialogSearchResult2.object = user;
                                            if (found == 1) {
                                                dialogSearchResult2.name = AndroidUtilities.generateSearchName(user.first_name, user.last_name, q);
                                            } else {
                                                dialogSearchResult2.name = AndroidUtilities.generateSearchName("@" + user.username, null, "@" + q);
                                            }
                                            arrayList.add(dialogSearchResult2);
                                        }
                                        data.reuse();
                                    } else {
                                        i$++;
                                    }
                                }
                            }
                        }
                        cursor.dispose();
                        Collections.sort(arrayList, new C09651());
                        ShareSearchAdapter.this.updateSearchResults(arrayList, searchId);
                    } catch (Throwable e) {
                        FileLog.m611e("tmessages", e);
                    }
                }
            });
        }

        private void updateSearchResults(final ArrayList<DialogSearchResult> result, final int searchId) {
            AndroidUtilities.runOnUIThread(new Runnable() {
                public void run() {
                    if (searchId == ShareSearchAdapter.this.lastSearchId) {
                        for (int a = 0; a < result.size(); a++) {
                            DialogSearchResult obj = (DialogSearchResult) result.get(a);
                            if (obj.object instanceof User) {
                                MessagesController.getInstance().putUser(obj.object, true);
                            } else if (obj.object instanceof Chat) {
                                MessagesController.getInstance().putChat(obj.object, true);
                            }
                        }
                        ShareSearchAdapter.this.searchResult = result;
                        ShareSearchAdapter.this.notifyDataSetChanged();
                    }
                }
            });
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
                    this.searchResult.clear();
                    notifyDataSetChanged();
                    return;
                }
                final int searchId = this.lastSearchId + 1;
                this.lastSearchId = searchId;
                this.searchTimer = new Timer();
                this.searchTimer.schedule(new TimerTask() {
                    public void run() {
                        try {
                            cancel();
                            ShareSearchAdapter.this.searchTimer.cancel();
                            ShareSearchAdapter.this.searchTimer = null;
                        } catch (Throwable e) {
                            FileLog.m611e("tmessages", e);
                        }
                        ShareSearchAdapter.this.searchDialogsInternal(query, searchId);
                    }
                }, 200, 300);
            }
        }

        public int getCount() {
            return this.searchResult.size();
        }

        public Dialog getItem(int i) {
            return ((DialogSearchResult) this.searchResult.get(i)).dialog;
        }

        public long getItemId(int i) {
            return (long) i;
        }

        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = new ShareDialogCell(this.mContext);
            }
            DialogSearchResult result = (DialogSearchResult) this.searchResult.get(i);
            ((ShareDialogCell) view).setDialog(result.dialog, ShareFrameLayout.this.selectedDialogs.containsKey(Long.valueOf(result.dialog.id)), result.name);
            return view;
        }

        public int getItemViewType(int i) {
            return 0;
        }

        public boolean isEmpty() {
            return getCount() == 0;
        }
    }

    public ShareFrameLayout(Context context, BottomSheet bottomSheet, MessageObject messageObject) {
        super(context);
        this.parentBottomSheet = bottomSheet;
        this.sendingMessageObject = messageObject;
        this.searchAdapter = new ShareSearchAdapter(context);
        FrameLayout frameLayout = new FrameLayout(context);
        frameLayout.setBackgroundColor(-1);
        addView(frameLayout, LayoutHelper.createFrame(-1, 48, 51));
        this.doneButton = new LinearLayout(context);
        this.doneButton.setOrientation(0);
        this.doneButton.setBackgroundResource(C0553R.drawable.bar_selector_audio);
        this.doneButton.setPadding(AndroidUtilities.dp(21.0f), 0, AndroidUtilities.dp(21.0f), 0);
        frameLayout.addView(this.doneButton, LayoutHelper.createFrame(-2, -1, 53));
        this.doneButton.setOnClickListener(new C09611());
        this.doneButtonBadgeTextView = new TextView(context);
        this.doneButtonBadgeTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        this.doneButtonBadgeTextView.setTextSize(1, 13.0f);
        this.doneButtonBadgeTextView.setTextColor(-1);
        this.doneButtonBadgeTextView.setGravity(17);
        this.doneButtonBadgeTextView.setBackgroundResource(C0553R.drawable.bluecounter);
        this.doneButtonBadgeTextView.setMinWidth(AndroidUtilities.dp(23.0f));
        this.doneButtonBadgeTextView.setPadding(AndroidUtilities.dp(8.0f), 0, AndroidUtilities.dp(8.0f), AndroidUtilities.dp(1.0f));
        this.doneButton.addView(this.doneButtonBadgeTextView, LayoutHelper.createLinear(-2, 23, 16, 0, 0, 10, 0));
        this.doneButtonTextView = new TextView(context);
        this.doneButtonTextView.setTextSize(1, 14.0f);
        this.doneButtonTextView.setTextColor(-15095832);
        this.doneButtonTextView.setGravity(17);
        this.doneButtonTextView.setCompoundDrawablePadding(AndroidUtilities.dp(8.0f));
        this.doneButtonTextView.setText(LocaleController.getString("Send", C0553R.string.Send).toUpperCase());
        this.doneButtonTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        this.doneButton.addView(this.doneButtonTextView, LayoutHelper.createLinear(-2, -2, 16));
        ImageView imageView = new ImageView(context);
        imageView.setImageResource(C0553R.drawable.search_share);
        imageView.setScaleType(ScaleType.CENTER);
        imageView.setPadding(0, AndroidUtilities.dp(2.0f), 0, 0);
        frameLayout.addView(imageView, LayoutHelper.createFrame(48, 48, 19));
        this.nameTextView = new EditText(context);
        this.nameTextView.setHint(LocaleController.getString("ShareSendTo", C0553R.string.ShareSendTo));
        this.nameTextView.setMaxLines(1);
        this.nameTextView.setSingleLine(true);
        this.nameTextView.setGravity(19);
        this.nameTextView.setTextSize(1, 16.0f);
        this.nameTextView.setBackgroundDrawable(null);
        this.nameTextView.setHintTextColor(-6842473);
        this.nameTextView.setImeOptions(268435456);
        this.nameTextView.setInputType(16385);
        AndroidUtilities.clearCursorDrawable(this.nameTextView);
        this.nameTextView.setTextColor(-14606047);
        frameLayout.addView(this.nameTextView, LayoutHelper.createFrame(-1, GroundOverlayOptions.NO_DIMENSION, 51, 48.0f, 2.0f, 96.0f, 0.0f));
        this.nameTextView.addTextChangedListener(new C09622());
        View lineView = new View(context);
        lineView.setBackgroundResource(C0553R.drawable.header_shadow);
        addView(lineView, LayoutHelper.createFrame(-1, 3.0f, 51, 0.0f, 48.0f, 0.0f, 0.0f));
        setOnTouchListener(new C09633());
        this.gridView = new GridView(context);
        this.gridView.setDrawSelectorOnTop(true);
        this.gridView.setPadding(0, AndroidUtilities.dp(8.0f), 0, AndroidUtilities.dp(8.0f));
        this.gridView.setClipToPadding(false);
        this.gridView.setStretchMode(2);
        this.gridView.setHorizontalScrollBarEnabled(false);
        this.gridView.setVerticalScrollBarEnabled(false);
        this.gridView.setNumColumns(4);
        this.gridView.setVerticalSpacing(AndroidUtilities.dp(4.0f));
        this.gridView.setHorizontalSpacing(AndroidUtilities.dp(4.0f));
        this.gridView.setSelector(C0553R.drawable.list_selector);
        addView(this.gridView, LayoutHelper.createFrame(-1, GroundOverlayOptions.NO_DIMENSION, 51, 0.0f, 48.0f, 0.0f, 0.0f));
        GridView gridView = this.gridView;
        ListAdapter shareDialogsAdapter = new ShareDialogsAdapter(context);
        this.listAdapter = shareDialogsAdapter;
        gridView.setAdapter(shareDialogsAdapter);
        AndroidUtilities.setListViewEdgeEffectColor(this.gridView, -657673);
        this.gridView.setOnItemClickListener(new C09644());
        this.searchEmptyView = new EmptyTextProgressView(context);
        this.searchEmptyView.setShowAtCenter(true);
        this.searchEmptyView.showTextView();
        this.searchEmptyView.setText(LocaleController.getString("NoChats", C0553R.string.NoChats));
        this.gridView.setEmptyView(this.searchEmptyView);
        addView(this.searchEmptyView, LayoutHelper.createFrame(-1, GroundOverlayOptions.NO_DIMENSION, 51, 0.0f, 48.0f, 0.0f, 0.0f));
        updateSelectedCount(this.selectedDialogs.size(), true);
    }

    public void updateSelectedCount(int count, boolean disable) {
        if (count == 0) {
            this.doneButtonBadgeTextView.setVisibility(8);
            this.doneButtonTextView.setTextColor(-5000269);
            this.doneButton.setEnabled(false);
            return;
        }
        this.doneButtonTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        this.doneButtonBadgeTextView.setVisibility(0);
        this.doneButtonBadgeTextView.setText(String.format("%d", new Object[]{Integer.valueOf(count)}));
        this.doneButtonTextView.setTextColor(-12664327);
        this.doneButton.setEnabled(true);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(310.0f), 1073741824));
    }
}
