package org.telegram.ui;

import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Build.VERSION;
import android.os.Environment;
import android.os.StatFs;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.AnimationCompat.AnimatorSetProxy;
import org.telegram.messenger.AnimationCompat.ObjectAnimatorProxy;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.C0553R;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Adapters.BaseFragmentAdapter;
import org.telegram.ui.Cells.SharedDocumentCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.NumberTextView;

public class DocumentSelectActivity extends BaseFragment {
    private static final int done = 3;
    private ArrayList<View> actionModeViews = new ArrayList();
    private File currentDir;
    private DocumentSelectActivityDelegate delegate;
    private TextView emptyView;
    private ArrayList<HistoryEntry> history = new ArrayList();
    private ArrayList<ListItem> items = new ArrayList();
    private ListAdapter listAdapter;
    private ListView listView;
    private BroadcastReceiver receiver = new C10021();
    private boolean receiverRegistered = false;
    private boolean scrolling;
    private HashMap<String, ListItem> selectedFiles = new HashMap();
    private NumberTextView selectedMessagesCountTextView;
    private long sizeLimit = 1610612736;

    class C10021 extends BroadcastReceiver {

        class C10011 implements Runnable {
            C10011() {
            }

            public void run() {
                try {
                    if (DocumentSelectActivity.this.currentDir == null) {
                        DocumentSelectActivity.this.listRoots();
                    } else {
                        DocumentSelectActivity.this.listFiles(DocumentSelectActivity.this.currentDir);
                    }
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
        }

        C10021() {
        }

        public void onReceive(Context arg0, Intent intent) {
            Runnable r = new C10011();
            if ("android.intent.action.MEDIA_UNMOUNTED".equals(intent.getAction())) {
                DocumentSelectActivity.this.listView.postDelayed(r, 1000);
            } else {
                r.run();
            }
        }
    }

    class C10033 implements OnTouchListener {
        C10033() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            return true;
        }
    }

    class C10044 implements OnTouchListener {
        C10044() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            return true;
        }
    }

    class C10055 implements OnScrollListener {
        C10055() {
        }

        public void onScrollStateChanged(AbsListView view, int scrollState) {
            DocumentSelectActivity.this.scrolling = scrollState != 0;
        }

        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        }
    }

    class C10066 implements OnItemLongClickListener {
        C10066() {
        }

        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long id) {
            if (DocumentSelectActivity.this.actionBar.isActionModeShowed() || i < 0 || i >= DocumentSelectActivity.this.items.size()) {
                return false;
            }
            ListItem item = (ListItem) DocumentSelectActivity.this.items.get(i);
            File file = item.file;
            if (!(file == null || file.isDirectory())) {
                if (!file.canRead()) {
                    DocumentSelectActivity.this.showErrorBox(LocaleController.getString("AccessError", C0553R.string.AccessError));
                    return false;
                } else if (DocumentSelectActivity.this.sizeLimit != 0 && file.length() > DocumentSelectActivity.this.sizeLimit) {
                    DocumentSelectActivity.this.showErrorBox(LocaleController.formatString("FileUploadLimit", C0553R.string.FileUploadLimit, AndroidUtilities.formatFileSize(DocumentSelectActivity.this.sizeLimit)));
                    return false;
                } else if (file.length() == 0) {
                    return false;
                } else {
                    DocumentSelectActivity.this.selectedFiles.put(file.toString(), item);
                    DocumentSelectActivity.this.selectedMessagesCountTextView.setNumber(1, false);
                    if (VERSION.SDK_INT >= 11) {
                        AnimatorSetProxy animatorSet = new AnimatorSetProxy();
                        ArrayList animators = new ArrayList();
                        for (int a = 0; a < DocumentSelectActivity.this.actionModeViews.size(); a++) {
                            View view2 = (View) DocumentSelectActivity.this.actionModeViews.get(a);
                            AndroidUtilities.clearDrawableAnimation(view2);
                            animators.add(ObjectAnimatorProxy.ofFloat(view2, "scaleY", 0.1f, 1.0f));
                        }
                        animatorSet.playTogether(animators);
                        animatorSet.setDuration(250);
                        animatorSet.start();
                    }
                    DocumentSelectActivity.this.scrolling = false;
                    if (view instanceof SharedDocumentCell) {
                        ((SharedDocumentCell) view).setChecked(true, true);
                    }
                    DocumentSelectActivity.this.actionBar.showActionMode();
                }
            }
            return true;
        }
    }

    class C10077 implements OnItemClickListener {
        C10077() {
        }

        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            if (i >= 0 && i < DocumentSelectActivity.this.items.size()) {
                ListItem item = (ListItem) DocumentSelectActivity.this.items.get(i);
                File file = item.file;
                HistoryEntry he;
                if (file == null) {
                    if (item.icon == C0553R.drawable.ic_storage_gallery) {
                        if (DocumentSelectActivity.this.delegate != null) {
                            DocumentSelectActivity.this.delegate.startDocumentSelectActivity();
                        }
                        DocumentSelectActivity.this.finishFragment(false);
                        return;
                    }
                    he = (HistoryEntry) DocumentSelectActivity.this.history.remove(DocumentSelectActivity.this.history.size() - 1);
                    DocumentSelectActivity.this.actionBar.setTitle(he.title);
                    if (he.dir != null) {
                        DocumentSelectActivity.this.listFiles(he.dir);
                    } else {
                        DocumentSelectActivity.this.listRoots();
                    }
                    DocumentSelectActivity.this.listView.setSelectionFromTop(he.scrollItem, he.scrollOffset);
                } else if (file.isDirectory()) {
                    he = new HistoryEntry();
                    he.scrollItem = DocumentSelectActivity.this.listView.getFirstVisiblePosition();
                    he.scrollOffset = DocumentSelectActivity.this.listView.getChildAt(0).getTop();
                    he.dir = DocumentSelectActivity.this.currentDir;
                    he.title = DocumentSelectActivity.this.actionBar.getTitle();
                    DocumentSelectActivity.this.history.add(he);
                    if (DocumentSelectActivity.this.listFiles(file)) {
                        DocumentSelectActivity.this.actionBar.setTitle(item.title);
                        DocumentSelectActivity.this.listView.setSelection(0);
                        return;
                    }
                    DocumentSelectActivity.this.history.remove(he);
                } else {
                    if (!file.canRead()) {
                        DocumentSelectActivity.this.showErrorBox(LocaleController.getString("AccessError", C0553R.string.AccessError));
                        file = new File("/mnt/sdcard");
                    }
                    if (DocumentSelectActivity.this.sizeLimit != 0 && file.length() > DocumentSelectActivity.this.sizeLimit) {
                        DocumentSelectActivity.this.showErrorBox(LocaleController.formatString("FileUploadLimit", C0553R.string.FileUploadLimit, AndroidUtilities.formatFileSize(DocumentSelectActivity.this.sizeLimit)));
                    } else if (file.length() == 0) {
                    } else {
                        if (DocumentSelectActivity.this.actionBar.isActionModeShowed()) {
                            if (DocumentSelectActivity.this.selectedFiles.containsKey(file.toString())) {
                                DocumentSelectActivity.this.selectedFiles.remove(file.toString());
                            } else {
                                DocumentSelectActivity.this.selectedFiles.put(file.toString(), item);
                            }
                            if (DocumentSelectActivity.this.selectedFiles.isEmpty()) {
                                DocumentSelectActivity.this.actionBar.hideActionMode();
                            } else {
                                DocumentSelectActivity.this.selectedMessagesCountTextView.setNumber(DocumentSelectActivity.this.selectedFiles.size(), true);
                            }
                            DocumentSelectActivity.this.scrolling = false;
                            if (view instanceof SharedDocumentCell) {
                                ((SharedDocumentCell) view).setChecked(DocumentSelectActivity.this.selectedFiles.containsKey(item.file.toString()), true);
                            }
                        } else if (DocumentSelectActivity.this.delegate != null) {
                            ArrayList<String> files = new ArrayList();
                            files.add(file.getAbsolutePath());
                            DocumentSelectActivity.this.delegate.didSelectFiles(DocumentSelectActivity.this, files);
                        }
                    }
                }
            }
        }
    }

    class C10088 implements OnPreDrawListener {
        C10088() {
        }

        public boolean onPreDraw() {
            DocumentSelectActivity.this.listView.getViewTreeObserver().removeOnPreDrawListener(this);
            DocumentSelectActivity.this.fixLayoutInternal();
            return true;
        }
    }

    class C10099 implements Comparator<File> {
        C10099() {
        }

        public int compare(File lhs, File rhs) {
            if (lhs.isDirectory() != rhs.isDirectory()) {
                return lhs.isDirectory() ? -1 : 1;
            } else {
                return lhs.getName().compareToIgnoreCase(rhs.getName());
            }
        }
    }

    public interface DocumentSelectActivityDelegate {
        void didSelectFiles(DocumentSelectActivity documentSelectActivity, ArrayList<String> arrayList);

        void startDocumentSelectActivity();
    }

    private class HistoryEntry {
        File dir;
        int scrollItem;
        int scrollOffset;
        String title;

        private HistoryEntry() {
        }
    }

    private class ListItem {
        String ext;
        File file;
        int icon;
        String subtitle;
        String thumb;
        String title;

        private ListItem() {
            this.subtitle = "";
            this.ext = "";
        }
    }

    class C15882 extends ActionBarMenuOnItemClick {
        C15882() {
        }

        public void onItemClick(int id) {
            if (id == -1) {
                if (DocumentSelectActivity.this.actionBar.isActionModeShowed()) {
                    DocumentSelectActivity.this.selectedFiles.clear();
                    DocumentSelectActivity.this.actionBar.hideActionMode();
                    DocumentSelectActivity.this.listView.invalidateViews();
                    return;
                }
                DocumentSelectActivity.this.finishFragment();
            } else if (id == 3 && DocumentSelectActivity.this.delegate != null) {
                ArrayList<String> files = new ArrayList();
                files.addAll(DocumentSelectActivity.this.selectedFiles.keySet());
                DocumentSelectActivity.this.delegate.didSelectFiles(DocumentSelectActivity.this, files);
            }
        }
    }

    private class ListAdapter extends BaseFragmentAdapter {
        private Context mContext;

        public ListAdapter(Context context) {
            this.mContext = context;
        }

        public int getCount() {
            return DocumentSelectActivity.this.items.size();
        }

        public Object getItem(int position) {
            return DocumentSelectActivity.this.items.get(position);
        }

        public long getItemId(int position) {
            return 0;
        }

        public int getViewTypeCount() {
            return 2;
        }

        public int getItemViewType(int pos) {
            return ((ListItem) DocumentSelectActivity.this.items.get(pos)).subtitle.length() > 0 ? 0 : 1;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            boolean z = true;
            if (convertView == null) {
                convertView = new SharedDocumentCell(this.mContext);
            }
            SharedDocumentCell textDetailCell = (SharedDocumentCell) convertView;
            ListItem item = (ListItem) DocumentSelectActivity.this.items.get(position);
            if (item.icon != 0) {
                ((SharedDocumentCell) convertView).setTextAndValueAndTypeAndThumb(item.title, item.subtitle, null, null, item.icon);
            } else {
                ((SharedDocumentCell) convertView).setTextAndValueAndTypeAndThumb(item.title, item.subtitle, item.ext.toUpperCase().substring(0, Math.min(item.ext.length(), 4)), item.thumb, 0);
            }
            if (item.file == null || !DocumentSelectActivity.this.actionBar.isActionModeShowed()) {
                if (DocumentSelectActivity.this.scrolling) {
                    z = false;
                }
                textDetailCell.setChecked(false, z);
            } else {
                boolean z2;
                boolean containsKey = DocumentSelectActivity.this.selectedFiles.containsKey(item.file.toString());
                if (DocumentSelectActivity.this.scrolling) {
                    z2 = false;
                } else {
                    z2 = true;
                }
                textDetailCell.setChecked(containsKey, z2);
            }
            return convertView;
        }
    }

    public void onFragmentDestroy() {
        try {
            if (this.receiverRegistered) {
                ApplicationLoader.applicationContext.unregisterReceiver(this.receiver);
            }
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
        }
        super.onFragmentDestroy();
    }

    public View createView(Context context) {
        if (!this.receiverRegistered) {
            this.receiverRegistered = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.MEDIA_BAD_REMOVAL");
            filter.addAction("android.intent.action.MEDIA_CHECKING");
            filter.addAction("android.intent.action.MEDIA_EJECT");
            filter.addAction("android.intent.action.MEDIA_MOUNTED");
            filter.addAction("android.intent.action.MEDIA_NOFS");
            filter.addAction("android.intent.action.MEDIA_REMOVED");
            filter.addAction("android.intent.action.MEDIA_SHARED");
            filter.addAction("android.intent.action.MEDIA_UNMOUNTABLE");
            filter.addAction("android.intent.action.MEDIA_UNMOUNTED");
            filter.addDataScheme("file");
            ApplicationLoader.applicationContext.registerReceiver(this.receiver, filter);
        }
        this.actionBar.setBackButtonDrawable(new BackDrawable(false));
        this.actionBar.setAllowOverlayTitle(true);
        this.actionBar.setTitle(LocaleController.getString("SelectFile", C0553R.string.SelectFile));
        this.actionBar.setActionBarMenuOnItemClick(new C15882());
        this.selectedFiles.clear();
        this.actionModeViews.clear();
        ActionBarMenu actionMode = this.actionBar.createActionMode();
        this.selectedMessagesCountTextView = new NumberTextView(actionMode.getContext());
        this.selectedMessagesCountTextView.setTextSize(18);
        this.selectedMessagesCountTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        this.selectedMessagesCountTextView.setTextColor(-9211021);
        this.selectedMessagesCountTextView.setOnTouchListener(new C10033());
        actionMode.addView(this.selectedMessagesCountTextView, LayoutHelper.createLinear(0, -1, 1.0f, 65, 0, 0, 0));
        this.actionModeViews.add(actionMode.addItem(3, C0553R.drawable.ic_ab_done_gray, C0553R.drawable.bar_selector_mode, null, AndroidUtilities.dp(54.0f)));
        this.fragmentView = getParentActivity().getLayoutInflater().inflate(C0553R.layout.document_select_layout, null, false);
        this.listAdapter = new ListAdapter(context);
        this.emptyView = (TextView) this.fragmentView.findViewById(C0553R.id.searchEmptyView);
        this.emptyView.setOnTouchListener(new C10044());
        this.listView = (ListView) this.fragmentView.findViewById(C0553R.id.listView);
        this.listView.setEmptyView(this.emptyView);
        this.listView.setAdapter(this.listAdapter);
        this.listView.setOnScrollListener(new C10055());
        this.listView.setOnItemLongClickListener(new C10066());
        this.listView.setOnItemClickListener(new C10077());
        listRoots();
        return this.fragmentView;
    }

    public void onResume() {
        super.onResume();
        if (this.listAdapter != null) {
            this.listAdapter.notifyDataSetChanged();
        }
        fixLayoutInternal();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.listView != null) {
            this.listView.getViewTreeObserver().addOnPreDrawListener(new C10088());
        }
    }

    private void fixLayoutInternal() {
        if (this.selectedMessagesCountTextView != null) {
            if (AndroidUtilities.isTablet() || ApplicationLoader.applicationContext.getResources().getConfiguration().orientation != 2) {
                this.selectedMessagesCountTextView.setTextSize(20);
            } else {
                this.selectedMessagesCountTextView.setTextSize(18);
            }
        }
    }

    public boolean onBackPressed() {
        if (this.history.size() <= 0) {
            return super.onBackPressed();
        }
        HistoryEntry he = (HistoryEntry) this.history.remove(this.history.size() - 1);
        this.actionBar.setTitle(he.title);
        if (he.dir != null) {
            listFiles(he.dir);
        } else {
            listRoots();
        }
        this.listView.setSelectionFromTop(he.scrollItem, he.scrollOffset);
        return false;
    }

    public void setDelegate(DocumentSelectActivityDelegate delegate) {
        this.delegate = delegate;
    }

    private boolean listFiles(File dir) {
        if (dir.canRead()) {
            this.emptyView.setText(LocaleController.getString("NoFiles", C0553R.string.NoFiles));
            try {
                File[] files = dir.listFiles();
                if (files == null) {
                    showErrorBox(LocaleController.getString("UnknownError", C0553R.string.UnknownError));
                    return false;
                }
                ListItem item;
                this.currentDir = dir;
                this.items.clear();
                Arrays.sort(files, new C10099());
                for (File file : files) {
                    if (!file.getName().startsWith(".")) {
                        item = new ListItem();
                        item.title = file.getName();
                        item.file = file;
                        if (file.isDirectory()) {
                            item.icon = C0553R.drawable.ic_directory;
                            item.subtitle = LocaleController.getString("Folder", C0553R.string.Folder);
                        } else {
                            String fname = file.getName();
                            String[] sp = fname.split("\\.");
                            item.ext = sp.length > 1 ? sp[sp.length - 1] : "?";
                            item.subtitle = AndroidUtilities.formatFileSize(file.length());
                            fname = fname.toLowerCase();
                            if (fname.endsWith(".jpg") || fname.endsWith(".png") || fname.endsWith(".gif") || fname.endsWith(".jpeg")) {
                                item.thumb = file.getAbsolutePath();
                            }
                        }
                        this.items.add(item);
                    }
                }
                item = new ListItem();
                item.title = "..";
                if (this.history.size() > 0) {
                    HistoryEntry entry = (HistoryEntry) this.history.get(this.history.size() - 1);
                    if (entry.dir == null) {
                        item.subtitle = LocaleController.getString("Folder", C0553R.string.Folder);
                    } else {
                        item.subtitle = entry.dir.toString();
                    }
                } else {
                    item.subtitle = LocaleController.getString("Folder", C0553R.string.Folder);
                }
                item.icon = C0553R.drawable.ic_directory;
                item.file = null;
                this.items.add(0, item);
                AndroidUtilities.clearDrawableAnimation(this.listView);
                this.scrolling = true;
                this.listAdapter.notifyDataSetChanged();
                return true;
            } catch (Exception e) {
                showErrorBox(e.getLocalizedMessage());
                return false;
            }
        } else if ((!dir.getAbsolutePath().startsWith(Environment.getExternalStorageDirectory().toString()) && !dir.getAbsolutePath().startsWith("/sdcard") && !dir.getAbsolutePath().startsWith("/mnt/sdcard")) || Environment.getExternalStorageState().equals("mounted") || Environment.getExternalStorageState().equals("mounted_ro")) {
            showErrorBox(LocaleController.getString("AccessError", C0553R.string.AccessError));
            return false;
        } else {
            this.currentDir = dir;
            this.items.clear();
            if ("shared".equals(Environment.getExternalStorageState())) {
                this.emptyView.setText(LocaleController.getString("UsbActive", C0553R.string.UsbActive));
            } else {
                this.emptyView.setText(LocaleController.getString("NotMounted", C0553R.string.NotMounted));
            }
            AndroidUtilities.clearDrawableAnimation(this.listView);
            this.scrolling = true;
            this.listAdapter.notifyDataSetChanged();
            return true;
        }
    }

    private void showErrorBox(String error) {
        if (getParentActivity() != null) {
            new Builder(getParentActivity()).setTitle(LocaleController.getString("AppName", C0553R.string.AppName)).setMessage(error).setPositiveButton(LocaleController.getString("OK", C0553R.string.OK), null).show();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @android.annotation.SuppressLint({"NewApi"})
    private void listRoots() {
        /*
        r23 = this;
        r21 = 0;
        r0 = r21;
        r1 = r23;
        r1.currentDir = r0;
        r0 = r23;
        r0 = r0.items;
        r21 = r0;
        r21.clear();
        r17 = new java.util.HashSet;
        r17.<init>();
        r21 = android.os.Environment.getExternalStorageDirectory();
        r5 = r21.getPath();
        r21 = android.os.Build.VERSION.SDK_INT;
        r22 = 9;
        r0 = r21;
        r1 = r22;
        if (r0 < r1) goto L_0x02b6;
    L_0x0028:
        r21 = android.os.Environment.isExternalStorageRemovable();
        if (r21 == 0) goto L_0x02b6;
    L_0x002e:
        r12 = 1;
    L_0x002f:
        r6 = android.os.Environment.getExternalStorageState();
        r21 = "mounted";
        r0 = r21;
        r21 = r6.equals(r0);
        if (r21 != 0) goto L_0x0047;
    L_0x003d:
        r21 = "mounted_ro";
        r0 = r21;
        r21 = r6.equals(r0);
        if (r21 == 0) goto L_0x0098;
    L_0x0047:
        r8 = new org.telegram.ui.DocumentSelectActivity$ListItem;
        r21 = 0;
        r0 = r23;
        r1 = r21;
        r8.<init>();
        r21 = android.os.Build.VERSION.SDK_INT;
        r22 = 9;
        r0 = r21;
        r1 = r22;
        if (r0 < r1) goto L_0x0062;
    L_0x005c:
        r21 = android.os.Environment.isExternalStorageRemovable();
        if (r21 == 0) goto L_0x02b9;
    L_0x0062:
        r21 = "SdCard";
        r22 = 2131165981; // 0x7f07031d float:1.7946194E38 double:1.052935897E-314;
        r21 = org.telegram.messenger.LocaleController.getString(r21, r22);
        r0 = r21;
        r8.title = r0;
        r21 = 2130837696; // 0x7f0200c0 float:1.7280353E38 double:1.0527737025E-314;
        r0 = r21;
        r8.icon = r0;
    L_0x0076:
        r0 = r23;
        r21 = r0.getRootSubtitle(r5);
        r0 = r21;
        r8.subtitle = r0;
        r21 = android.os.Environment.getExternalStorageDirectory();
        r0 = r21;
        r8.file = r0;
        r0 = r23;
        r0 = r0.items;
        r21 = r0;
        r0 = r21;
        r0.add(r8);
        r0 = r17;
        r0.add(r5);
    L_0x0098:
        r3 = 0;
        r4 = new java.io.BufferedReader;	 Catch:{ Exception -> 0x031b }
        r21 = new java.io.FileReader;	 Catch:{ Exception -> 0x031b }
        r22 = "/proc/mounts";
        r21.<init>(r22);	 Catch:{ Exception -> 0x031b }
        r0 = r21;
        r4.<init>(r0);	 Catch:{ Exception -> 0x031b }
    L_0x00a7:
        r14 = r4.readLine();	 Catch:{ Exception -> 0x01ce, all -> 0x02de }
        if (r14 == 0) goto L_0x02e6;
    L_0x00ad:
        r21 = "vfat";
        r0 = r21;
        r21 = r14.contains(r0);	 Catch:{ Exception -> 0x01ce, all -> 0x02de }
        if (r21 != 0) goto L_0x00c1;
    L_0x00b7:
        r21 = "/mnt";
        r0 = r21;
        r21 = r14.contains(r0);	 Catch:{ Exception -> 0x01ce, all -> 0x02de }
        if (r21 == 0) goto L_0x00a7;
    L_0x00c1:
        r21 = "tmessages";
        r0 = r21;
        org.telegram.messenger.FileLog.m609e(r0, r14);	 Catch:{ Exception -> 0x01ce, all -> 0x02de }
        r19 = new java.util.StringTokenizer;	 Catch:{ Exception -> 0x01ce, all -> 0x02de }
        r21 = " ";
        r0 = r19;
        r1 = r21;
        r0.<init>(r14, r1);	 Catch:{ Exception -> 0x01ce, all -> 0x02de }
        r20 = r19.nextToken();	 Catch:{ Exception -> 0x01ce, all -> 0x02de }
        r16 = r19.nextToken();	 Catch:{ Exception -> 0x01ce, all -> 0x02de }
        r0 = r17;
        r1 = r16;
        r21 = r0.contains(r1);	 Catch:{ Exception -> 0x01ce, all -> 0x02de }
        if (r21 != 0) goto L_0x00a7;
    L_0x00e5:
        r21 = "/dev/block/vold";
        r0 = r21;
        r21 = r14.contains(r0);	 Catch:{ Exception -> 0x01ce, all -> 0x02de }
        if (r21 == 0) goto L_0x00a7;
    L_0x00ef:
        r21 = "/mnt/secure";
        r0 = r21;
        r21 = r14.contains(r0);	 Catch:{ Exception -> 0x01ce, all -> 0x02de }
        if (r21 != 0) goto L_0x00a7;
    L_0x00f9:
        r21 = "/mnt/asec";
        r0 = r21;
        r21 = r14.contains(r0);	 Catch:{ Exception -> 0x01ce, all -> 0x02de }
        if (r21 != 0) goto L_0x00a7;
    L_0x0103:
        r21 = "/mnt/obb";
        r0 = r21;
        r21 = r14.contains(r0);	 Catch:{ Exception -> 0x01ce, all -> 0x02de }
        if (r21 != 0) goto L_0x00a7;
    L_0x010d:
        r21 = "/dev/mapper";
        r0 = r21;
        r21 = r14.contains(r0);	 Catch:{ Exception -> 0x01ce, all -> 0x02de }
        if (r21 != 0) goto L_0x00a7;
    L_0x0117:
        r21 = "tmpfs";
        r0 = r21;
        r21 = r14.contains(r0);	 Catch:{ Exception -> 0x01ce, all -> 0x02de }
        if (r21 != 0) goto L_0x00a7;
    L_0x0121:
        r21 = new java.io.File;	 Catch:{ Exception -> 0x01ce, all -> 0x02de }
        r0 = r21;
        r1 = r16;
        r0.<init>(r1);	 Catch:{ Exception -> 0x01ce, all -> 0x02de }
        r21 = r21.isDirectory();	 Catch:{ Exception -> 0x01ce, all -> 0x02de }
        if (r21 != 0) goto L_0x016c;
    L_0x0130:
        r21 = 47;
        r0 = r16;
        r1 = r21;
        r11 = r0.lastIndexOf(r1);	 Catch:{ Exception -> 0x01ce, all -> 0x02de }
        r21 = -1;
        r0 = r21;
        if (r11 == r0) goto L_0x016c;
    L_0x0140:
        r21 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x01ce, all -> 0x02de }
        r21.<init>();	 Catch:{ Exception -> 0x01ce, all -> 0x02de }
        r22 = "/storage/";
        r21 = r21.append(r22);	 Catch:{ Exception -> 0x01ce, all -> 0x02de }
        r22 = r11 + 1;
        r0 = r16;
        r1 = r22;
        r22 = r0.substring(r1);	 Catch:{ Exception -> 0x01ce, all -> 0x02de }
        r21 = r21.append(r22);	 Catch:{ Exception -> 0x01ce, all -> 0x02de }
        r15 = r21.toString();	 Catch:{ Exception -> 0x01ce, all -> 0x02de }
        r21 = new java.io.File;	 Catch:{ Exception -> 0x01ce, all -> 0x02de }
        r0 = r21;
        r0.<init>(r15);	 Catch:{ Exception -> 0x01ce, all -> 0x02de }
        r21 = r21.isDirectory();	 Catch:{ Exception -> 0x01ce, all -> 0x02de }
        if (r21 == 0) goto L_0x016c;
    L_0x016a:
        r16 = r15;
    L_0x016c:
        r0 = r17;
        r1 = r16;
        r0.add(r1);	 Catch:{ Exception -> 0x01ce, all -> 0x02de }
        r13 = new org.telegram.ui.DocumentSelectActivity$ListItem;	 Catch:{ Exception -> 0x01c4, all -> 0x02de }
        r21 = 0;
        r0 = r23;
        r1 = r21;
        r13.<init>();	 Catch:{ Exception -> 0x01c4, all -> 0x02de }
        r21 = r16.toLowerCase();	 Catch:{ Exception -> 0x01c4, all -> 0x02de }
        r22 = "sd";
        r21 = r21.contains(r22);	 Catch:{ Exception -> 0x01c4, all -> 0x02de }
        if (r21 == 0) goto L_0x02cf;
    L_0x018a:
        r21 = "SdCard";
        r22 = 2131165981; // 0x7f07031d float:1.7946194E38 double:1.052935897E-314;
        r21 = org.telegram.messenger.LocaleController.getString(r21, r22);	 Catch:{ Exception -> 0x01c4, all -> 0x02de }
        r0 = r21;
        r13.title = r0;	 Catch:{ Exception -> 0x01c4, all -> 0x02de }
    L_0x0197:
        r21 = 2130837696; // 0x7f0200c0 float:1.7280353E38 double:1.0527737025E-314;
        r0 = r21;
        r13.icon = r0;	 Catch:{ Exception -> 0x01c4, all -> 0x02de }
        r0 = r23;
        r1 = r16;
        r21 = r0.getRootSubtitle(r1);	 Catch:{ Exception -> 0x01c4, all -> 0x02de }
        r0 = r21;
        r13.subtitle = r0;	 Catch:{ Exception -> 0x01c4, all -> 0x02de }
        r21 = new java.io.File;	 Catch:{ Exception -> 0x01c4, all -> 0x02de }
        r0 = r21;
        r1 = r16;
        r0.<init>(r1);	 Catch:{ Exception -> 0x01c4, all -> 0x02de }
        r0 = r21;
        r13.file = r0;	 Catch:{ Exception -> 0x01c4, all -> 0x02de }
        r0 = r23;
        r0 = r0.items;	 Catch:{ Exception -> 0x01c4, all -> 0x02de }
        r21 = r0;
        r0 = r21;
        r0.add(r13);	 Catch:{ Exception -> 0x01c4, all -> 0x02de }
        goto L_0x00a7;
    L_0x01c4:
        r7 = move-exception;
        r21 = "tmessages";
        r0 = r21;
        org.telegram.messenger.FileLog.m611e(r0, r7);	 Catch:{ Exception -> 0x01ce, all -> 0x02de }
        goto L_0x00a7;
    L_0x01ce:
        r7 = move-exception;
        r3 = r4;
    L_0x01d0:
        r21 = "tmessages";
        r0 = r21;
        org.telegram.messenger.FileLog.m611e(r0, r7);	 Catch:{ all -> 0x0319 }
        if (r3 == 0) goto L_0x01dc;
    L_0x01d9:
        r3.close();	 Catch:{ Exception -> 0x02f9 }
    L_0x01dc:
        r9 = new org.telegram.ui.DocumentSelectActivity$ListItem;
        r21 = 0;
        r0 = r23;
        r1 = r21;
        r9.<init>();
        r21 = "/";
        r0 = r21;
        r9.title = r0;
        r21 = "SystemRoot";
        r22 = 2131166076; // 0x7f07037c float:1.7946387E38 double:1.0529359437E-314;
        r21 = org.telegram.messenger.LocaleController.getString(r21, r22);
        r0 = r21;
        r9.subtitle = r0;
        r21 = 2130837686; // 0x7f0200b6 float:1.7280333E38 double:1.0527736975E-314;
        r0 = r21;
        r9.icon = r0;
        r21 = new java.io.File;
        r22 = "/";
        r21.<init>(r22);
        r0 = r21;
        r9.file = r0;
        r0 = r23;
        r0 = r0.items;
        r21 = r0;
        r0 = r21;
        r0.add(r9);
        r18 = new java.io.File;	 Catch:{ Exception -> 0x030c }
        r21 = android.os.Environment.getExternalStorageDirectory();	 Catch:{ Exception -> 0x030c }
        r22 = "Telegram";
        r0 = r18;
        r1 = r21;
        r2 = r22;
        r0.<init>(r1, r2);	 Catch:{ Exception -> 0x030c }
        r21 = r18.exists();	 Catch:{ Exception -> 0x030c }
        if (r21 == 0) goto L_0x025e;
    L_0x022e:
        r10 = new org.telegram.ui.DocumentSelectActivity$ListItem;	 Catch:{ Exception -> 0x030c }
        r21 = 0;
        r0 = r23;
        r1 = r21;
        r10.<init>();	 Catch:{ Exception -> 0x030c }
        r21 = "Telegram";
        r0 = r21;
        r10.title = r0;	 Catch:{ Exception -> 0x0316 }
        r21 = r18.toString();	 Catch:{ Exception -> 0x0316 }
        r0 = r21;
        r10.subtitle = r0;	 Catch:{ Exception -> 0x0316 }
        r21 = 2130837686; // 0x7f0200b6 float:1.7280333E38 double:1.0527736975E-314;
        r0 = r21;
        r10.icon = r0;	 Catch:{ Exception -> 0x0316 }
        r0 = r18;
        r10.file = r0;	 Catch:{ Exception -> 0x0316 }
        r0 = r23;
        r0 = r0.items;	 Catch:{ Exception -> 0x0316 }
        r21 = r0;
        r0 = r21;
        r0.add(r10);	 Catch:{ Exception -> 0x0316 }
        r9 = r10;
    L_0x025e:
        r9 = new org.telegram.ui.DocumentSelectActivity$ListItem;
        r21 = 0;
        r0 = r23;
        r1 = r21;
        r9.<init>();
        r21 = "Gallery";
        r22 = 2131165606; // 0x7f0701a6 float:1.7945434E38 double:1.0529357115E-314;
        r21 = org.telegram.messenger.LocaleController.getString(r21, r22);
        r0 = r21;
        r9.title = r0;
        r21 = "GalleryInfo";
        r22 = 2131165607; // 0x7f0701a7 float:1.7945436E38 double:1.052935712E-314;
        r21 = org.telegram.messenger.LocaleController.getString(r21, r22);
        r0 = r21;
        r9.subtitle = r0;
        r21 = 2130837731; // 0x7f0200e3 float:1.7280424E38 double:1.0527737197E-314;
        r0 = r21;
        r9.icon = r0;
        r21 = 0;
        r0 = r21;
        r9.file = r0;
        r0 = r23;
        r0 = r0.items;
        r21 = r0;
        r0 = r21;
        r0.add(r9);
        r0 = r23;
        r0 = r0.listView;
        r21 = r0;
        org.telegram.messenger.AndroidUtilities.clearDrawableAnimation(r21);
        r21 = 1;
        r0 = r21;
        r1 = r23;
        r1.scrolling = r0;
        r0 = r23;
        r0 = r0.listAdapter;
        r21 = r0;
        r21.notifyDataSetChanged();
        return;
    L_0x02b6:
        r12 = 0;
        goto L_0x002f;
    L_0x02b9:
        r21 = "InternalStorage";
        r22 = 2131165637; // 0x7f0701c5 float:1.7945497E38 double:1.052935727E-314;
        r21 = org.telegram.messenger.LocaleController.getString(r21, r22);
        r0 = r21;
        r8.title = r0;
        r21 = 2130837730; // 0x7f0200e2 float:1.7280422E38 double:1.0527737193E-314;
        r0 = r21;
        r8.icon = r0;
        goto L_0x0076;
    L_0x02cf:
        r21 = "ExternalStorage";
        r22 = 2131165532; // 0x7f07015c float:1.7945284E38 double:1.052935675E-314;
        r21 = org.telegram.messenger.LocaleController.getString(r21, r22);	 Catch:{ Exception -> 0x01c4, all -> 0x02de }
        r0 = r21;
        r13.title = r0;	 Catch:{ Exception -> 0x01c4, all -> 0x02de }
        goto L_0x0197;
    L_0x02de:
        r21 = move-exception;
        r3 = r4;
    L_0x02e0:
        if (r3 == 0) goto L_0x02e5;
    L_0x02e2:
        r3.close();	 Catch:{ Exception -> 0x0303 }
    L_0x02e5:
        throw r21;
    L_0x02e6:
        if (r4 == 0) goto L_0x031e;
    L_0x02e8:
        r4.close();	 Catch:{ Exception -> 0x02ee }
        r3 = r4;
        goto L_0x01dc;
    L_0x02ee:
        r7 = move-exception;
        r21 = "tmessages";
        r0 = r21;
        org.telegram.messenger.FileLog.m611e(r0, r7);
        r3 = r4;
        goto L_0x01dc;
    L_0x02f9:
        r7 = move-exception;
        r21 = "tmessages";
        r0 = r21;
        org.telegram.messenger.FileLog.m611e(r0, r7);
        goto L_0x01dc;
    L_0x0303:
        r7 = move-exception;
        r22 = "tmessages";
        r0 = r22;
        org.telegram.messenger.FileLog.m611e(r0, r7);
        goto L_0x02e5;
    L_0x030c:
        r7 = move-exception;
    L_0x030d:
        r21 = "tmessages";
        r0 = r21;
        org.telegram.messenger.FileLog.m611e(r0, r7);
        goto L_0x025e;
    L_0x0316:
        r7 = move-exception;
        r9 = r10;
        goto L_0x030d;
    L_0x0319:
        r21 = move-exception;
        goto L_0x02e0;
    L_0x031b:
        r7 = move-exception;
        goto L_0x01d0;
    L_0x031e:
        r3 = r4;
        goto L_0x01dc;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.DocumentSelectActivity.listRoots():void");
    }

    private String getRootSubtitle(String path) {
        try {
            StatFs stat = new StatFs(path);
            long free = ((long) stat.getAvailableBlocks()) * ((long) stat.getBlockSize());
            if (((long) stat.getBlockCount()) * ((long) stat.getBlockSize()) == 0) {
                return "";
            }
            return LocaleController.formatString("FreeOfTotal", C0553R.string.FreeOfTotal, AndroidUtilities.formatFileSize(free), AndroidUtilities.formatFileSize(((long) stat.getBlockCount()) * ((long) stat.getBlockSize())));
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
            return path;
        }
    }
}
