package org.telegram.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.C0553R;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Adapters.ContactsAdapter;
import org.telegram.ui.Adapters.SearchAdapter;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.Components.ChipSpan;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.LetterSectionsListView;

public class GroupCreateActivity extends BaseFragment implements NotificationCenterDelegate {
    private static final int done_button = 1;
    private ArrayList<ChipSpan> allSpans = new ArrayList();
    private int beforeChangeIndex;
    private CharSequence changeString;
    private int chatType = 0;
    private GroupCreateActivityDelegate delegate;
    private TextView emptyTextView;
    private boolean ignoreChange;
    private boolean isAlwaysShare;
    private boolean isNeverShare;
    private LetterSectionsListView listView;
    private ContactsAdapter listViewAdapter;
    private int maxCount = 199;
    private SearchAdapter searchListViewAdapter;
    private boolean searchWas;
    private boolean searching;
    private HashMap<Integer, ChipSpan> selectedContacts = new HashMap();
    private EditText userSelectEditText;

    class C10102 implements TextWatcher {
        C10102() {
        }

        public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            if (!GroupCreateActivity.this.ignoreChange) {
                GroupCreateActivity.this.beforeChangeIndex = GroupCreateActivity.this.userSelectEditText.getSelectionStart();
                GroupCreateActivity.this.changeString = new SpannableString(charSequence);
            }
        }

        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        public void afterTextChanged(Editable editable) {
            if (!GroupCreateActivity.this.ignoreChange) {
                boolean search = false;
                int afterChangeIndex = GroupCreateActivity.this.userSelectEditText.getSelectionEnd();
                if (editable.toString().length() < GroupCreateActivity.this.changeString.toString().length()) {
                    String deletedString = "";
                    try {
                        deletedString = GroupCreateActivity.this.changeString.toString().substring(afterChangeIndex, GroupCreateActivity.this.beforeChangeIndex);
                    } catch (Throwable e) {
                        FileLog.m611e("tmessages", e);
                    }
                    if (deletedString.length() > 0) {
                        if (GroupCreateActivity.this.searching && GroupCreateActivity.this.searchWas) {
                            search = true;
                        }
                        Spannable span = GroupCreateActivity.this.userSelectEditText.getText();
                        for (int a = 0; a < GroupCreateActivity.this.allSpans.size(); a++) {
                            ChipSpan sp = (ChipSpan) GroupCreateActivity.this.allSpans.get(a);
                            if (span.getSpanStart(sp) == -1) {
                                GroupCreateActivity.this.allSpans.remove(sp);
                                GroupCreateActivity.this.selectedContacts.remove(Integer.valueOf(sp.uid));
                            }
                        }
                        if (!(GroupCreateActivity.this.isAlwaysShare || GroupCreateActivity.this.isNeverShare)) {
                            GroupCreateActivity.this.actionBar.setSubtitle(LocaleController.formatString("MembersCount", C0553R.string.MembersCount, Integer.valueOf(GroupCreateActivity.this.selectedContacts.size()), Integer.valueOf(GroupCreateActivity.this.maxCount)));
                        }
                        GroupCreateActivity.this.listView.invalidateViews();
                    } else {
                        search = true;
                    }
                } else {
                    search = true;
                }
                if (search) {
                    String text = GroupCreateActivity.this.userSelectEditText.getText().toString().replace("<", "");
                    if (text.length() != 0) {
                        GroupCreateActivity.this.searching = true;
                        GroupCreateActivity.this.searchWas = true;
                        if (GroupCreateActivity.this.listView != null) {
                            GroupCreateActivity.this.listView.setAdapter(GroupCreateActivity.this.searchListViewAdapter);
                            GroupCreateActivity.this.searchListViewAdapter.notifyDataSetChanged();
                            if (VERSION.SDK_INT >= 11) {
                                GroupCreateActivity.this.listView.setFastScrollAlwaysVisible(false);
                            }
                            GroupCreateActivity.this.listView.setFastScrollEnabled(false);
                            GroupCreateActivity.this.listView.setVerticalScrollBarEnabled(true);
                        }
                        if (GroupCreateActivity.this.emptyTextView != null) {
                            GroupCreateActivity.this.emptyTextView.setText(LocaleController.getString("NoResult", C0553R.string.NoResult));
                        }
                        GroupCreateActivity.this.searchListViewAdapter.searchDialogs(text);
                        return;
                    }
                    GroupCreateActivity.this.searchListViewAdapter.searchDialogs(null);
                    GroupCreateActivity.this.searching = false;
                    GroupCreateActivity.this.searchWas = false;
                    GroupCreateActivity.this.listView.setAdapter(GroupCreateActivity.this.listViewAdapter);
                    GroupCreateActivity.this.listViewAdapter.notifyDataSetChanged();
                    if (VERSION.SDK_INT >= 11) {
                        GroupCreateActivity.this.listView.setFastScrollAlwaysVisible(true);
                    }
                    GroupCreateActivity.this.listView.setFastScrollEnabled(true);
                    GroupCreateActivity.this.listView.setVerticalScrollBarEnabled(false);
                    GroupCreateActivity.this.emptyTextView.setText(LocaleController.getString("NoContacts", C0553R.string.NoContacts));
                }
            }
        }
    }

    class C10113 implements OnTouchListener {
        C10113() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            return true;
        }
    }

    class C10124 implements OnItemClickListener {
        C10124() {
        }

        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            User user;
            if (GroupCreateActivity.this.searching && GroupCreateActivity.this.searchWas) {
                user = (User) GroupCreateActivity.this.searchListViewAdapter.getItem(i);
            } else {
                int section = GroupCreateActivity.this.listViewAdapter.getSectionForPosition(i);
                int row = GroupCreateActivity.this.listViewAdapter.getPositionInSectionForPosition(i);
                if (row >= 0 && section >= 0) {
                    user = (User) GroupCreateActivity.this.listViewAdapter.getItem(section, row);
                } else {
                    return;
                }
            }
            if (user != null) {
                boolean check = true;
                if (GroupCreateActivity.this.selectedContacts.containsKey(Integer.valueOf(user.id))) {
                    check = false;
                    try {
                        ChipSpan span = (ChipSpan) GroupCreateActivity.this.selectedContacts.get(Integer.valueOf(user.id));
                        GroupCreateActivity.this.selectedContacts.remove(Integer.valueOf(user.id));
                        SpannableStringBuilder text = new SpannableStringBuilder(GroupCreateActivity.this.userSelectEditText.getText());
                        text.delete(text.getSpanStart(span), text.getSpanEnd(span));
                        GroupCreateActivity.this.allSpans.remove(span);
                        GroupCreateActivity.this.ignoreChange = true;
                        GroupCreateActivity.this.userSelectEditText.setText(text);
                        GroupCreateActivity.this.userSelectEditText.setSelection(text.length());
                        GroupCreateActivity.this.ignoreChange = false;
                    } catch (Throwable e) {
                        FileLog.m611e("tmessages", e);
                    }
                } else if (GroupCreateActivity.this.maxCount == 0 || GroupCreateActivity.this.selectedContacts.size() != GroupCreateActivity.this.maxCount) {
                    GroupCreateActivity.this.ignoreChange = true;
                    GroupCreateActivity.this.createAndPutChipForUser(user).uid = user.id;
                    GroupCreateActivity.this.ignoreChange = false;
                } else {
                    return;
                }
                if (!(GroupCreateActivity.this.isAlwaysShare || GroupCreateActivity.this.isNeverShare)) {
                    GroupCreateActivity.this.actionBar.setSubtitle(LocaleController.formatString("MembersCount", C0553R.string.MembersCount, Integer.valueOf(GroupCreateActivity.this.selectedContacts.size()), Integer.valueOf(GroupCreateActivity.this.maxCount)));
                }
                if (GroupCreateActivity.this.searching || GroupCreateActivity.this.searchWas) {
                    GroupCreateActivity.this.ignoreChange = true;
                    SpannableStringBuilder ssb = new SpannableStringBuilder("");
                    Iterator i$ = GroupCreateActivity.this.allSpans.iterator();
                    while (i$.hasNext()) {
                        ImageSpan sp = (ImageSpan) i$.next();
                        ssb.append("<<");
                        ssb.setSpan(sp, ssb.length() - 2, ssb.length(), 33);
                    }
                    GroupCreateActivity.this.userSelectEditText.setText(ssb);
                    GroupCreateActivity.this.userSelectEditText.setSelection(ssb.length());
                    GroupCreateActivity.this.ignoreChange = false;
                    GroupCreateActivity.this.searchListViewAdapter.searchDialogs(null);
                    GroupCreateActivity.this.searching = false;
                    GroupCreateActivity.this.searchWas = false;
                    GroupCreateActivity.this.listView.setAdapter(GroupCreateActivity.this.listViewAdapter);
                    GroupCreateActivity.this.listViewAdapter.notifyDataSetChanged();
                    if (VERSION.SDK_INT >= 11) {
                        GroupCreateActivity.this.listView.setFastScrollAlwaysVisible(true);
                    }
                    GroupCreateActivity.this.listView.setFastScrollEnabled(true);
                    GroupCreateActivity.this.listView.setVerticalScrollBarEnabled(false);
                    GroupCreateActivity.this.emptyTextView.setText(LocaleController.getString("NoContacts", C0553R.string.NoContacts));
                } else if (view instanceof UserCell) {
                    ((UserCell) view).setChecked(check, true);
                }
            }
        }
    }

    class C10135 implements OnScrollListener {
        C10135() {
        }

        public void onScrollStateChanged(AbsListView absListView, int i) {
            boolean z = true;
            if (i == 1) {
                AndroidUtilities.hideKeyboard(GroupCreateActivity.this.userSelectEditText);
            }
            if (GroupCreateActivity.this.listViewAdapter != null) {
                ContactsAdapter access$1700 = GroupCreateActivity.this.listViewAdapter;
                if (i == 0) {
                    z = false;
                }
                access$1700.setIsScrolling(z);
            }
        }

        public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (absListView.isFastScrollEnabled()) {
                AndroidUtilities.clearDrawableAnimation(absListView);
            }
        }
    }

    public interface GroupCreateActivityDelegate {
        void didSelectUsers(ArrayList<Integer> arrayList);
    }

    class C15891 extends ActionBarMenuOnItemClick {
        C15891() {
        }

        public void onItemClick(int id) {
            if (id == -1) {
                GroupCreateActivity.this.finishFragment();
            } else if (id == 1 && !GroupCreateActivity.this.selectedContacts.isEmpty()) {
                ArrayList<Integer> result = new ArrayList();
                result.addAll(GroupCreateActivity.this.selectedContacts.keySet());
                if (GroupCreateActivity.this.isAlwaysShare || GroupCreateActivity.this.isNeverShare) {
                    if (GroupCreateActivity.this.delegate != null) {
                        GroupCreateActivity.this.delegate.didSelectUsers(result);
                    }
                    GroupCreateActivity.this.finishFragment();
                    return;
                }
                Bundle args = new Bundle();
                args.putIntegerArrayList("result", result);
                args.putInt("chatType", GroupCreateActivity.this.chatType);
                GroupCreateActivity.this.presentFragment(new GroupCreateFinalActivity(args));
            }
        }
    }

    public GroupCreateActivity(Bundle args) {
        super(args);
        this.chatType = args.getInt("chatType", 0);
        this.isAlwaysShare = args.getBoolean("isAlwaysShare", false);
        this.isNeverShare = args.getBoolean("isNeverShare", false);
        this.maxCount = this.chatType == 0 ? MessagesController.getInstance().maxGroupCount - 1 : MessagesController.getInstance().maxBroadcastCount;
    }

    public boolean onFragmentCreate() {
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.contactsDidLoaded);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.updateInterfaces);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.chatDidCreated);
        return super.onFragmentCreate();
    }

    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.contactsDidLoaded);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.updateInterfaces);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.chatDidCreated);
    }

    public View createView(Context context) {
        this.searching = false;
        this.searchWas = false;
        this.actionBar.setBackButtonImage(C0553R.drawable.ic_ab_back);
        this.actionBar.setAllowOverlayTitle(true);
        if (this.isAlwaysShare) {
            this.actionBar.setTitle(LocaleController.getString("AlwaysShareWithTitle", C0553R.string.AlwaysShareWithTitle));
        } else if (this.isNeverShare) {
            this.actionBar.setTitle(LocaleController.getString("NeverShareWithTitle", C0553R.string.NeverShareWithTitle));
        } else {
            CharSequence string;
            ActionBar actionBar = this.actionBar;
            if (this.chatType == 0) {
                string = LocaleController.getString("NewGroup", C0553R.string.NewGroup);
            } else {
                string = LocaleController.getString("NewBroadcastList", C0553R.string.NewBroadcastList);
            }
            actionBar.setTitle(string);
            this.actionBar.setSubtitle(LocaleController.formatString("MembersCount", C0553R.string.MembersCount, Integer.valueOf(this.selectedContacts.size()), Integer.valueOf(this.maxCount)));
        }
        this.actionBar.setActionBarMenuOnItemClick(new C15891());
        this.actionBar.createMenu().addItemWithWidth(1, C0553R.drawable.ic_done, AndroidUtilities.dp(56.0f));
        this.searchListViewAdapter = new SearchAdapter(context, null, false, false, false, false);
        this.searchListViewAdapter.setCheckedMap(this.selectedContacts);
        this.searchListViewAdapter.setUseUserCell(true);
        this.listViewAdapter = new ContactsAdapter(context, 1, false, null, false);
        this.listViewAdapter.setCheckedMap(this.selectedContacts);
        this.fragmentView = new LinearLayout(context);
        LinearLayout linearLayout = this.fragmentView;
        linearLayout.setOrientation(1);
        FrameLayout frameLayout = new FrameLayout(context);
        linearLayout.addView(frameLayout, LayoutHelper.createLinear(-1, -2));
        this.userSelectEditText = new EditText(context);
        this.userSelectEditText.setTextSize(1, 16.0f);
        this.userSelectEditText.setHintTextColor(-6842473);
        this.userSelectEditText.setTextColor(-14606047);
        this.userSelectEditText.setInputType(655536);
        this.userSelectEditText.setMinimumHeight(AndroidUtilities.dp(54.0f));
        this.userSelectEditText.setSingleLine(false);
        this.userSelectEditText.setLines(2);
        this.userSelectEditText.setMaxLines(2);
        this.userSelectEditText.setVerticalScrollBarEnabled(true);
        this.userSelectEditText.setHorizontalScrollBarEnabled(false);
        this.userSelectEditText.setPadding(0, 0, 0, 0);
        this.userSelectEditText.setImeOptions(268435462);
        this.userSelectEditText.setGravity((LocaleController.isRTL ? 5 : 3) | 16);
        AndroidUtilities.clearCursorDrawable(this.userSelectEditText);
        frameLayout.addView(this.userSelectEditText, LayoutHelper.createFrame(-1, -2.0f, 51, 10.0f, 0.0f, 10.0f, 0.0f));
        if (this.isAlwaysShare) {
            this.userSelectEditText.setHint(LocaleController.getString("AlwaysShareWithPlaceholder", C0553R.string.AlwaysShareWithPlaceholder));
        } else if (this.isNeverShare) {
            this.userSelectEditText.setHint(LocaleController.getString("NeverShareWithPlaceholder", C0553R.string.NeverShareWithPlaceholder));
        } else {
            this.userSelectEditText.setHint(LocaleController.getString("SendMessageTo", C0553R.string.SendMessageTo));
        }
        if (VERSION.SDK_INT >= 11) {
            this.userSelectEditText.setTextIsSelectable(false);
        }
        this.userSelectEditText.addTextChangedListener(new C10102());
        LinearLayout emptyTextLayout = new LinearLayout(context);
        emptyTextLayout.setVisibility(4);
        emptyTextLayout.setOrientation(1);
        linearLayout.addView(emptyTextLayout, LayoutHelper.createLinear(-1, -1));
        emptyTextLayout.setOnTouchListener(new C10113());
        this.emptyTextView = new TextView(context);
        this.emptyTextView.setTextColor(-8355712);
        this.emptyTextView.setTextSize(20.0f);
        this.emptyTextView.setGravity(17);
        this.emptyTextView.setText(LocaleController.getString("NoContacts", C0553R.string.NoContacts));
        emptyTextLayout.addView(this.emptyTextView, LayoutHelper.createLinear(-1, -1, 0.5f));
        emptyTextLayout.addView(new FrameLayout(context), LayoutHelper.createLinear(-1, -1, 0.5f));
        this.listView = new LetterSectionsListView(context);
        this.listView.setEmptyView(emptyTextLayout);
        this.listView.setVerticalScrollBarEnabled(false);
        this.listView.setDivider(null);
        this.listView.setDividerHeight(0);
        this.listView.setFastScrollEnabled(true);
        this.listView.setScrollBarStyle(33554432);
        this.listView.setAdapter(this.listViewAdapter);
        if (VERSION.SDK_INT >= 11) {
            this.listView.setFastScrollAlwaysVisible(true);
            this.listView.setVerticalScrollbarPosition(LocaleController.isRTL ? 1 : 2);
        }
        linearLayout.addView(this.listView, LayoutHelper.createLinear(-1, -1));
        this.listView.setOnItemClickListener(new C10124());
        this.listView.setOnScrollListener(new C10135());
        return this.fragmentView;
    }

    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.contactsDidLoaded) {
            if (this.listViewAdapter != null) {
                this.listViewAdapter.notifyDataSetChanged();
            }
        } else if (id == NotificationCenter.updateInterfaces) {
            int mask = ((Integer) args[0]).intValue();
            if ((mask & 2) != 0 || (mask & 1) != 0 || (mask & 4) != 0) {
                updateVisibleRows(mask);
            }
        } else if (id == NotificationCenter.chatDidCreated) {
            removeSelfFromStack();
        }
    }

    private void updateVisibleRows(int mask) {
        if (this.listView != null) {
            int count = this.listView.getChildCount();
            for (int a = 0; a < count; a++) {
                View child = this.listView.getChildAt(a);
                if (child instanceof UserCell) {
                    ((UserCell) child).update(mask);
                }
            }
        }
    }

    public void setDelegate(GroupCreateActivityDelegate delegate) {
        this.delegate = delegate;
    }

    private ChipSpan createAndPutChipForUser(User user) {
        View textView = ((LayoutInflater) ApplicationLoader.applicationContext.getSystemService("layout_inflater")).inflate(C0553R.layout.group_create_bubble, null);
        TextView text = (TextView) textView.findViewById(C0553R.id.bubble_text_view);
        String name = UserObject.getUserName(user);
        if (!(name.length() != 0 || user.phone == null || user.phone.length() == 0)) {
            name = PhoneFormat.getInstance().format("+" + user.phone);
        }
        text.setText(name + ", ");
        int spec = MeasureSpec.makeMeasureSpec(0, 0);
        textView.measure(spec, spec);
        textView.layout(0, 0, textView.getMeasuredWidth(), textView.getMeasuredHeight());
        Bitmap b = Bitmap.createBitmap(textView.getWidth(), textView.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(b);
        canvas.translate((float) (-textView.getScrollX()), (float) (-textView.getScrollY()));
        textView.draw(canvas);
        textView.setDrawingCacheEnabled(true);
        Bitmap viewBmp = textView.getDrawingCache().copy(Config.ARGB_8888, true);
        textView.destroyDrawingCache();
        BitmapDrawable bmpDrawable = new BitmapDrawable(b);
        bmpDrawable.setBounds(0, 0, b.getWidth(), b.getHeight());
        SpannableStringBuilder ssb = new SpannableStringBuilder("");
        ChipSpan span = new ChipSpan(bmpDrawable, 1);
        this.allSpans.add(span);
        this.selectedContacts.put(Integer.valueOf(user.id), span);
        Iterator i$ = this.allSpans.iterator();
        while (i$.hasNext()) {
            ImageSpan sp = (ImageSpan) i$.next();
            ssb.append("<<");
            ssb.setSpan(sp, ssb.length() - 2, ssb.length(), 33);
        }
        this.userSelectEditText.setText(ssb);
        this.userSelectEditText.setSelection(ssb.length());
        return span;
    }
}
