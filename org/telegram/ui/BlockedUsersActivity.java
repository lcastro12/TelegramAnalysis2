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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.android.gms.maps.model.GroundOverlayOptions;
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
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.ContactsActivity.ContactsActivityDelegate;

public class BlockedUsersActivity extends BaseFragment implements NotificationCenterDelegate, ContactsActivityDelegate {
    private static final int block_user = 1;
    private TextView emptyTextView;
    private ListView listView;
    private ListAdapter listViewAdapter;
    private FrameLayout progressView;
    private int selectedUserId;

    class C07682 implements OnTouchListener {
        C07682() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            return true;
        }
    }

    class C07693 implements OnItemClickListener {
        C07693() {
        }

        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            if (i < MessagesController.getInstance().blockedUsers.size()) {
                Bundle args = new Bundle();
                args.putInt("user_id", ((Integer) MessagesController.getInstance().blockedUsers.get(i)).intValue());
                BlockedUsersActivity.this.presentFragment(new ProfileActivity(args));
            }
        }
    }

    class C07714 implements OnItemLongClickListener {

        class C07701 implements OnClickListener {
            C07701() {
            }

            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    MessagesController.getInstance().unblockUser(BlockedUsersActivity.this.selectedUserId);
                }
            }
        }

        C07714() {
        }

        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
            if (i >= 0 && i < MessagesController.getInstance().blockedUsers.size() && BlockedUsersActivity.this.getParentActivity() != null) {
                BlockedUsersActivity.this.selectedUserId = ((Integer) MessagesController.getInstance().blockedUsers.get(i)).intValue();
                Builder builder = new Builder(BlockedUsersActivity.this.getParentActivity());
                builder.setItems(new CharSequence[]{LocaleController.getString("Unblock", C0553R.string.Unblock)}, new C07701());
                BlockedUsersActivity.this.showDialog(builder.create());
            }
            return true;
        }
    }

    class C15091 extends ActionBarMenuOnItemClick {
        C15091() {
        }

        public void onItemClick(int id) {
            if (id == -1) {
                BlockedUsersActivity.this.finishFragment();
            } else if (id == 1) {
                Bundle args = new Bundle();
                args.putBoolean("onlyUsers", true);
                args.putBoolean("destroyAfterSelect", true);
                args.putBoolean("returnAsResult", true);
                ContactsActivity fragment = new ContactsActivity(args);
                fragment.setDelegate(BlockedUsersActivity.this);
                BlockedUsersActivity.this.presentFragment(fragment);
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
            return i != MessagesController.getInstance().blockedUsers.size();
        }

        public int getCount() {
            if (MessagesController.getInstance().blockedUsers.isEmpty()) {
                return 0;
            }
            return MessagesController.getInstance().blockedUsers.size() + 1;
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
                User user = MessagesController.getInstance().getUser((Integer) MessagesController.getInstance().blockedUsers.get(i));
                if (user == null) {
                    return view;
                }
                UserCell userCell = (UserCell) view;
                CharSequence string = (user.phone == null || user.phone.length() == 0) ? LocaleController.getString("NumberUnknown", C0553R.string.NumberUnknown) : PhoneFormat.getInstance().format("+" + user.phone);
                userCell.setData(user, null, string, 0);
                return view;
            } else if (type != 1 || view != null) {
                return view;
            } else {
                view = new TextInfoCell(this.mContext);
                ((TextInfoCell) view).setText(LocaleController.getString("UnblockText", C0553R.string.UnblockText));
                return view;
            }
        }

        public int getItemViewType(int i) {
            if (i == MessagesController.getInstance().blockedUsers.size()) {
                return 1;
            }
            return 0;
        }

        public int getViewTypeCount() {
            return 2;
        }

        public boolean isEmpty() {
            return MessagesController.getInstance().blockedUsers.isEmpty();
        }
    }

    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.updateInterfaces);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.blockedUsersDidLoaded);
        MessagesController.getInstance().getBlockedUsers(false);
        return true;
    }

    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.updateInterfaces);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.blockedUsersDidLoaded);
    }

    public View createView(Context context) {
        int i = 1;
        this.actionBar.setBackButtonImage(C0553R.drawable.ic_ab_back);
        this.actionBar.setAllowOverlayTitle(true);
        this.actionBar.setTitle(LocaleController.getString("BlockedUsers", C0553R.string.BlockedUsers));
        this.actionBar.setActionBarMenuOnItemClick(new C15091());
        this.actionBar.createMenu().addItem(1, (int) C0553R.drawable.plus);
        this.fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = this.fragmentView;
        this.emptyTextView = new TextView(context);
        this.emptyTextView.setTextColor(-8355712);
        this.emptyTextView.setTextSize(20.0f);
        this.emptyTextView.setGravity(17);
        this.emptyTextView.setVisibility(4);
        this.emptyTextView.setText(LocaleController.getString("NoBlocked", C0553R.string.NoBlocked));
        frameLayout.addView(this.emptyTextView, LayoutHelper.createFrame(-1, -1, 51));
        this.emptyTextView.setOnTouchListener(new C07682());
        this.progressView = new FrameLayout(context);
        frameLayout.addView(this.progressView, LayoutHelper.createFrame(-1, GroundOverlayOptions.NO_DIMENSION));
        this.progressView.addView(new ProgressBar(context), LayoutHelper.createFrame(-2, -2, 17));
        this.listView = new ListView(context);
        this.listView.setEmptyView(this.emptyTextView);
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
        frameLayout.addView(this.listView, LayoutHelper.createFrame(-1, GroundOverlayOptions.NO_DIMENSION));
        this.listView.setOnItemClickListener(new C07693());
        this.listView.setOnItemLongClickListener(new C07714());
        if (MessagesController.getInstance().loadingBlockedUsers) {
            this.progressView.setVisibility(0);
            this.emptyTextView.setVisibility(8);
            this.listView.setEmptyView(null);
        } else {
            this.progressView.setVisibility(8);
            this.listView.setEmptyView(this.emptyTextView);
        }
        return this.fragmentView;
    }

    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.updateInterfaces) {
            int mask = ((Integer) args[0]).intValue();
            if ((mask & 2) != 0 || (mask & 1) != 0) {
                updateVisibleRows(mask);
            }
        } else if (id == NotificationCenter.blockedUsersDidLoaded) {
            if (this.progressView != null) {
                this.progressView.setVisibility(8);
            }
            if (this.listView != null && this.listView.getEmptyView() == null) {
                this.listView.setEmptyView(this.emptyTextView);
            }
            if (this.listViewAdapter != null) {
                this.listViewAdapter.notifyDataSetChanged();
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

    public void onResume() {
        super.onResume();
        if (this.listViewAdapter != null) {
            this.listViewAdapter.notifyDataSetChanged();
        }
    }

    public void didSelectContact(User user, String param) {
        if (user != null) {
            MessagesController.getInstance().blockUser(user.id);
        }
    }
}
