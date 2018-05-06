package org.telegram.ui;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Iterator;
import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.C0553R;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Adapters.BaseFragmentAdapter;
import org.telegram.ui.Cells.TextInfoCell;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.GroupCreateActivity.GroupCreateActivityDelegate;

public class LastSeenUsersActivity extends BaseFragment implements NotificationCenterDelegate {
    private static final int block_user = 1;
    private LastSeenUsersActivityDelegate delegate;
    private boolean isAlwaysShare;
    private ListView listView;
    private ListAdapter listViewAdapter;
    private int selectedUserId;
    private ArrayList<Integer> uidArray;

    class C10392 implements OnTouchListener {
        C10392() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            return true;
        }
    }

    class C10403 implements OnItemClickListener {
        C10403() {
        }

        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            if (i < LastSeenUsersActivity.this.uidArray.size()) {
                Bundle args = new Bundle();
                args.putInt("user_id", ((Integer) LastSeenUsersActivity.this.uidArray.get(i)).intValue());
                LastSeenUsersActivity.this.presentFragment(new ProfileActivity(args));
            }
        }
    }

    class C10424 implements OnItemLongClickListener {

        class C10411 implements OnClickListener {
            C10411() {
            }

            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    LastSeenUsersActivity.this.uidArray.remove(Integer.valueOf(LastSeenUsersActivity.this.selectedUserId));
                    LastSeenUsersActivity.this.listViewAdapter.notifyDataSetChanged();
                    if (LastSeenUsersActivity.this.delegate != null) {
                        LastSeenUsersActivity.this.delegate.didUpdatedUserList(LastSeenUsersActivity.this.uidArray, false);
                    }
                }
            }
        }

        C10424() {
        }

        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
            if (i >= 0 && i < LastSeenUsersActivity.this.uidArray.size() && LastSeenUsersActivity.this.getParentActivity() != null) {
                LastSeenUsersActivity.this.selectedUserId = ((Integer) LastSeenUsersActivity.this.uidArray.get(i)).intValue();
                Builder builder = new Builder(LastSeenUsersActivity.this.getParentActivity());
                builder.setItems(new CharSequence[]{LocaleController.getString("Delete", C0553R.string.Delete)}, new C10411());
                LastSeenUsersActivity.this.showDialog(builder.create());
            }
            return true;
        }
    }

    public interface LastSeenUsersActivityDelegate {
        void didUpdatedUserList(ArrayList<Integer> arrayList, boolean z);
    }

    class C16021 extends ActionBarMenuOnItemClick {

        class C16011 implements GroupCreateActivityDelegate {
            C16011() {
            }

            public void didSelectUsers(ArrayList<Integer> ids) {
                Iterator i$ = ids.iterator();
                while (i$.hasNext()) {
                    Integer id = (Integer) i$.next();
                    if (!LastSeenUsersActivity.this.uidArray.contains(id)) {
                        LastSeenUsersActivity.this.uidArray.add(id);
                    }
                }
                LastSeenUsersActivity.this.listViewAdapter.notifyDataSetChanged();
                if (LastSeenUsersActivity.this.delegate != null) {
                    LastSeenUsersActivity.this.delegate.didUpdatedUserList(LastSeenUsersActivity.this.uidArray, true);
                }
            }
        }

        C16021() {
        }

        public void onItemClick(int id) {
            if (id == -1) {
                LastSeenUsersActivity.this.finishFragment();
            } else if (id == 1) {
                Bundle args = new Bundle();
                args.putBoolean(LastSeenUsersActivity.this.isAlwaysShare ? "isAlwaysShare" : "isNeverShare", true);
                GroupCreateActivity fragment = new GroupCreateActivity(args);
                fragment.setDelegate(new C16011());
                LastSeenUsersActivity.this.presentFragment(fragment);
            }
        }
    }

    private class ListAdapter extends BaseFragmentAdapter {
        private Context mContext;

        public ListAdapter(Context context) {
            this.mContext = context;
        }

        public boolean areAllItemsEnabled() {
            return false;
        }

        public boolean isEnabled(int i) {
            return i != LastSeenUsersActivity.this.uidArray.size();
        }

        public int getCount() {
            if (LastSeenUsersActivity.this.uidArray.isEmpty()) {
                return 0;
            }
            return LastSeenUsersActivity.this.uidArray.size() + 1;
        }

        public Object getItem(int i) {
            return null;
        }

        public long getItemId(int i) {
            return (long) i;
        }

        public boolean hasStableIds() {
            return false;
        }

        public View getView(int i, View view, ViewGroup viewGroup) {
            int type = getItemViewType(i);
            if (type == 0) {
                if (view == null) {
                    view = new UserCell(this.mContext, 1, 0);
                }
                User user = MessagesController.getInstance().getUser((Integer) LastSeenUsersActivity.this.uidArray.get(i));
                UserCell userCell = (UserCell) view;
                CharSequence string = (user.phone == null || user.phone.length() == 0) ? LocaleController.getString("NumberUnknown", C0553R.string.NumberUnknown) : PhoneFormat.getInstance().format("+" + user.phone);
                userCell.setData(user, null, string, 0);
                return view;
            } else if (type != 1 || view != null) {
                return view;
            } else {
                view = new TextInfoCell(this.mContext);
                ((TextInfoCell) view).setText(LocaleController.getString("RemoveFromListText", C0553R.string.RemoveFromListText));
                return view;
            }
        }

        public int getItemViewType(int i) {
            if (i == LastSeenUsersActivity.this.uidArray.size()) {
                return 1;
            }
            return 0;
        }

        public int getViewTypeCount() {
            return 2;
        }

        public boolean isEmpty() {
            return LastSeenUsersActivity.this.uidArray.isEmpty();
        }
    }

    public LastSeenUsersActivity(ArrayList<Integer> users, boolean always) {
        this.uidArray = users;
        this.isAlwaysShare = always;
    }

    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.updateInterfaces);
        return true;
    }

    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.updateInterfaces);
    }

    public View createView(Context context) {
        int i = 1;
        this.actionBar.setBackButtonImage(C0553R.drawable.ic_ab_back);
        this.actionBar.setAllowOverlayTitle(true);
        if (this.isAlwaysShare) {
            this.actionBar.setTitle(LocaleController.getString("AlwaysShareWithTitle", C0553R.string.AlwaysShareWithTitle));
        } else {
            this.actionBar.setTitle(LocaleController.getString("NeverShareWithTitle", C0553R.string.NeverShareWithTitle));
        }
        this.actionBar.setActionBarMenuOnItemClick(new C16021());
        this.actionBar.createMenu().addItem(1, (int) C0553R.drawable.plus);
        this.fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = this.fragmentView;
        TextView emptyTextView = new TextView(context);
        emptyTextView.setTextColor(-8355712);
        emptyTextView.setTextSize(20.0f);
        emptyTextView.setGravity(17);
        emptyTextView.setVisibility(4);
        emptyTextView.setText(LocaleController.getString("NoContacts", C0553R.string.NoContacts));
        frameLayout.addView(emptyTextView);
        LayoutParams layoutParams = (LayoutParams) emptyTextView.getLayoutParams();
        layoutParams.width = -1;
        layoutParams.height = -1;
        layoutParams.gravity = 48;
        emptyTextView.setLayoutParams(layoutParams);
        emptyTextView.setOnTouchListener(new C10392());
        this.listView = new ListView(context);
        this.listView.setEmptyView(emptyTextView);
        this.listView.setVerticalScrollBarEnabled(false);
        this.listView.setDivider(null);
        this.listView.setDividerHeight(0);
        ListView listView = this.listView;
        android.widget.ListAdapter listAdapter = new ListAdapter(context);
        this.listViewAdapter = listAdapter;
        listView.setAdapter(listAdapter);
        if (VERSION.SDK_INT >= 11) {
            listView = this.listView;
            if (!LocaleController.isRTL) {
                i = 2;
            }
            listView.setVerticalScrollbarPosition(i);
        }
        frameLayout.addView(this.listView);
        layoutParams = (LayoutParams) this.listView.getLayoutParams();
        layoutParams.width = -1;
        layoutParams.height = -1;
        this.listView.setLayoutParams(layoutParams);
        this.listView.setOnItemClickListener(new C10403());
        this.listView.setOnItemLongClickListener(new C10424());
        return this.fragmentView;
    }

    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.updateInterfaces) {
            int mask = ((Integer) args[0]).intValue();
            if ((mask & 2) != 0 || (mask & 1) != 0) {
                updateVisibleRows(mask);
            }
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

    public void setDelegate(LastSeenUsersActivityDelegate delegate) {
        this.delegate = delegate;
    }

    public void onResume() {
        super.onResume();
        if (this.listViewAdapter != null) {
            this.listViewAdapter.notifyDataSetChanged();
        }
    }
}
