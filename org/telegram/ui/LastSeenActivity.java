package org.telegram.ui;

import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Iterator;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.C0553R;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.InputUser;
import org.telegram.tgnet.TLRPC.PrivacyRule;
import org.telegram.tgnet.TLRPC.TL_account_privacyRules;
import org.telegram.tgnet.TLRPC.TL_account_setPrivacy;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_inputPrivacyKeyStatusTimestamp;
import org.telegram.tgnet.TLRPC.TL_inputPrivacyValueAllowAll;
import org.telegram.tgnet.TLRPC.TL_inputPrivacyValueAllowContacts;
import org.telegram.tgnet.TLRPC.TL_inputPrivacyValueAllowUsers;
import org.telegram.tgnet.TLRPC.TL_inputPrivacyValueDisallowAll;
import org.telegram.tgnet.TLRPC.TL_inputPrivacyValueDisallowUsers;
import org.telegram.tgnet.TLRPC.TL_privacyValueAllowAll;
import org.telegram.tgnet.TLRPC.TL_privacyValueAllowUsers;
import org.telegram.tgnet.TLRPC.TL_privacyValueDisallowAll;
import org.telegram.tgnet.TLRPC.TL_privacyValueDisallowUsers;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Adapters.BaseFragmentAdapter;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.LastSeenRadioCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.GroupCreateActivity.GroupCreateActivityDelegate;
import org.telegram.ui.LastSeenUsersActivity.LastSeenUsersActivityDelegate;

public class LastSeenActivity extends BaseFragment implements NotificationCenterDelegate {
    private static final int done_button = 1;
    private int alwaysShareRow;
    private ArrayList<Integer> currentMinus;
    private ArrayList<Integer> currentPlus;
    private int currentType = 0;
    private View doneButton;
    private int everybodyRow;
    private int lastCheckedType = -1;
    private int lastSeenDetailRow;
    private int lastSeenSectionRow;
    private ListAdapter listAdapter;
    private int myContactsRow;
    private int neverShareRow;
    private int nobodyRow;
    private int rowCount;
    private int shareDetailRow;
    private int shareSectionRow;

    class C10372 implements OnItemClickListener {
        C10372() {
        }

        public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
            boolean z = false;
            if (i == LastSeenActivity.this.nobodyRow || i == LastSeenActivity.this.everybodyRow || i == LastSeenActivity.this.myContactsRow) {
                int newType = LastSeenActivity.this.currentType;
                if (i == LastSeenActivity.this.nobodyRow) {
                    newType = 1;
                } else if (i == LastSeenActivity.this.everybodyRow) {
                    newType = 0;
                } else if (i == LastSeenActivity.this.myContactsRow) {
                    newType = 2;
                }
                if (newType != LastSeenActivity.this.currentType) {
                    LastSeenActivity.this.doneButton.setVisibility(0);
                    LastSeenActivity.this.lastCheckedType = LastSeenActivity.this.currentType;
                    LastSeenActivity.this.currentType = newType;
                    LastSeenActivity.this.updateRows();
                }
            } else if (i == LastSeenActivity.this.neverShareRow || i == LastSeenActivity.this.alwaysShareRow) {
                ArrayList<Integer> createFromArray;
                if (i == LastSeenActivity.this.neverShareRow) {
                    createFromArray = LastSeenActivity.this.currentMinus;
                } else {
                    createFromArray = LastSeenActivity.this.currentPlus;
                }
                if (createFromArray.isEmpty()) {
                    Bundle args = new Bundle();
                    args.putBoolean(i == LastSeenActivity.this.neverShareRow ? "isNeverShare" : "isAlwaysShare", true);
                    GroupCreateActivity fragment = new GroupCreateActivity(args);
                    fragment.setDelegate(new GroupCreateActivityDelegate() {
                        public void didSelectUsers(ArrayList<Integer> ids) {
                            Iterator i$;
                            if (i == LastSeenActivity.this.neverShareRow) {
                                LastSeenActivity.this.currentMinus = ids;
                                i$ = LastSeenActivity.this.currentMinus.iterator();
                                while (i$.hasNext()) {
                                    LastSeenActivity.this.currentPlus.remove((Integer) i$.next());
                                }
                            } else {
                                LastSeenActivity.this.currentPlus = ids;
                                i$ = LastSeenActivity.this.currentPlus.iterator();
                                while (i$.hasNext()) {
                                    LastSeenActivity.this.currentMinus.remove((Integer) i$.next());
                                }
                            }
                            LastSeenActivity.this.doneButton.setVisibility(0);
                            LastSeenActivity.this.lastCheckedType = -1;
                            LastSeenActivity.this.listAdapter.notifyDataSetChanged();
                        }
                    });
                    LastSeenActivity.this.presentFragment(fragment);
                    return;
                }
                if (i == LastSeenActivity.this.alwaysShareRow) {
                    z = true;
                }
                LastSeenUsersActivity fragment2 = new LastSeenUsersActivity(createFromArray, z);
                fragment2.setDelegate(new LastSeenUsersActivityDelegate() {
                    public void didUpdatedUserList(ArrayList<Integer> ids, boolean added) {
                        Iterator i$;
                        if (i == LastSeenActivity.this.neverShareRow) {
                            LastSeenActivity.this.currentMinus = ids;
                            if (added) {
                                i$ = LastSeenActivity.this.currentMinus.iterator();
                                while (i$.hasNext()) {
                                    LastSeenActivity.this.currentPlus.remove((Integer) i$.next());
                                }
                            }
                        } else {
                            LastSeenActivity.this.currentPlus = ids;
                            if (added) {
                                i$ = LastSeenActivity.this.currentPlus.iterator();
                                while (i$.hasNext()) {
                                    LastSeenActivity.this.currentMinus.remove((Integer) i$.next());
                                }
                            }
                        }
                        LastSeenActivity.this.doneButton.setVisibility(0);
                        LastSeenActivity.this.listAdapter.notifyDataSetChanged();
                    }
                });
                LastSeenActivity.this.presentFragment(fragment2);
            }
        }
    }

    private static class LinkMovementMethodMy extends LinkMovementMethod {
        private LinkMovementMethodMy() {
        }

        public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
            try {
                return super.onTouchEvent(widget, buffer, event);
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
                return false;
            }
        }
    }

    class C15971 extends ActionBarMenuOnItemClick {
        C15971() {
        }

        public void onItemClick(int id) {
            if (id == -1) {
                LastSeenActivity.this.finishFragment();
            } else if (id == 1 && LastSeenActivity.this.getParentActivity() != null) {
                if (LastSeenActivity.this.currentType != 0) {
                    final SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0);
                    if (!preferences.getBoolean("privacyAlertShowed", false)) {
                        Builder builder = new Builder(LastSeenActivity.this.getParentActivity());
                        builder.setMessage(LocaleController.getString("CustomHelp", C0553R.string.CustomHelp));
                        builder.setTitle(LocaleController.getString("AppName", C0553R.string.AppName));
                        builder.setPositiveButton(LocaleController.getString("OK", C0553R.string.OK), new OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                LastSeenActivity.this.applyCurrentPrivacySettings();
                                preferences.edit().putBoolean("privacyAlertShowed", true).commit();
                            }
                        });
                        builder.setNegativeButton(LocaleController.getString("Cancel", C0553R.string.Cancel), null);
                        LastSeenActivity.this.showDialog(builder.create());
                        return;
                    }
                }
                LastSeenActivity.this.applyCurrentPrivacySettings();
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
            return i == LastSeenActivity.this.nobodyRow || i == LastSeenActivity.this.everybodyRow || i == LastSeenActivity.this.myContactsRow || i == LastSeenActivity.this.neverShareRow || i == LastSeenActivity.this.alwaysShareRow;
        }

        public int getCount() {
            return LastSeenActivity.this.rowCount;
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
            boolean z = true;
            int type = getItemViewType(i);
            if (type == 0) {
                if (view == null) {
                    view = new TextSettingsCell(this.mContext);
                    view.setBackgroundColor(-1);
                }
                TextSettingsCell textCell = (TextSettingsCell) view;
                String value;
                if (i == LastSeenActivity.this.alwaysShareRow) {
                    if (LastSeenActivity.this.currentPlus.size() != 0) {
                        value = LocaleController.formatPluralString("Users", LastSeenActivity.this.currentPlus.size());
                    } else {
                        value = LocaleController.getString("EmpryUsersPlaceholder", C0553R.string.EmpryUsersPlaceholder);
                    }
                    String string = LocaleController.getString("AlwaysShareWith", C0553R.string.AlwaysShareWith);
                    if (LastSeenActivity.this.neverShareRow == -1) {
                        z = false;
                    }
                    textCell.setTextAndValue(string, value, z);
                } else if (i == LastSeenActivity.this.neverShareRow) {
                    if (LastSeenActivity.this.currentMinus.size() != 0) {
                        value = LocaleController.formatPluralString("Users", LastSeenActivity.this.currentMinus.size());
                    } else {
                        value = LocaleController.getString("EmpryUsersPlaceholder", C0553R.string.EmpryUsersPlaceholder);
                    }
                    textCell.setTextAndValue(LocaleController.getString("NeverShareWith", C0553R.string.NeverShareWith), value, false);
                }
            } else if (type == 1) {
                if (view == null) {
                    view = new TextInfoPrivacyCell(this.mContext);
                    view.setBackgroundColor(-1);
                }
                if (i == LastSeenActivity.this.lastSeenDetailRow) {
                    ((TextInfoPrivacyCell) view).setText(LocaleController.getString("CustomHelp", C0553R.string.CustomHelp));
                    view.setBackgroundResource(C0553R.drawable.greydivider);
                } else if (i == LastSeenActivity.this.shareDetailRow) {
                    ((TextInfoPrivacyCell) view).setText(LocaleController.getString("CustomShareSettingsHelp", C0553R.string.CustomShareSettingsHelp));
                    view.setBackgroundResource(C0553R.drawable.greydivider_bottom);
                }
            } else if (type == 2) {
                if (view == null) {
                    view = new HeaderCell(this.mContext);
                    view.setBackgroundColor(-1);
                }
                if (i == LastSeenActivity.this.lastSeenSectionRow) {
                    ((HeaderCell) view).setText(LocaleController.getString("LastSeenTitle", C0553R.string.LastSeenTitle));
                } else if (i == LastSeenActivity.this.shareSectionRow) {
                    ((HeaderCell) view).setText(LocaleController.getString("AddExceptions", C0553R.string.AddExceptions));
                }
            } else if (type == 3) {
                if (view == null) {
                    view = new LastSeenRadioCell(this.mContext);
                    view.setBackgroundColor(-1);
                }
                LastSeenRadioCell textCell2 = (LastSeenRadioCell) view;
                int checkedType = 0;
                if (i == LastSeenActivity.this.everybodyRow) {
                    textCell2.setText(LocaleController.getString("LastSeenEverybody", C0553R.string.LastSeenEverybody), LastSeenActivity.this.lastCheckedType == 0, true);
                    checkedType = 0;
                } else if (i == LastSeenActivity.this.myContactsRow) {
                    r7 = LocaleController.getString("LastSeenContacts", C0553R.string.LastSeenContacts);
                    if (LastSeenActivity.this.lastCheckedType == 2) {
                        r6 = true;
                    } else {
                        r6 = false;
                    }
                    textCell2.setText(r7, r6, true);
                    checkedType = 2;
                } else if (i == LastSeenActivity.this.nobodyRow) {
                    r7 = LocaleController.getString("LastSeenNobody", C0553R.string.LastSeenNobody);
                    if (LastSeenActivity.this.lastCheckedType == 1) {
                        r6 = true;
                    } else {
                        r6 = false;
                    }
                    textCell2.setText(r7, r6, false);
                    checkedType = 1;
                }
                if (LastSeenActivity.this.lastCheckedType == checkedType) {
                    textCell2.setChecked(false, true);
                } else if (LastSeenActivity.this.currentType == checkedType) {
                    textCell2.setChecked(true, true);
                }
            }
            return view;
        }

        public int getItemViewType(int i) {
            if (i == LastSeenActivity.this.alwaysShareRow || i == LastSeenActivity.this.neverShareRow) {
                return 0;
            }
            if (i == LastSeenActivity.this.shareDetailRow || i == LastSeenActivity.this.lastSeenDetailRow) {
                return 1;
            }
            if (i == LastSeenActivity.this.lastSeenSectionRow || i == LastSeenActivity.this.shareSectionRow) {
                return 2;
            }
            if (i == LastSeenActivity.this.everybodyRow || i == LastSeenActivity.this.myContactsRow || i == LastSeenActivity.this.nobodyRow) {
                return 3;
            }
            return 0;
        }

        public int getViewTypeCount() {
            return 4;
        }

        public boolean isEmpty() {
            return false;
        }
    }

    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        checkPrivacy();
        updateRows();
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.privacyRulesUpdated);
        return true;
    }

    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.privacyRulesUpdated);
    }

    public View createView(Context context) {
        this.actionBar.setBackButtonImage(C0553R.drawable.ic_ab_back);
        this.actionBar.setAllowOverlayTitle(true);
        this.actionBar.setTitle(LocaleController.getString("PrivacyLastSeen", C0553R.string.PrivacyLastSeen));
        this.actionBar.setActionBarMenuOnItemClick(new C15971());
        this.doneButton = this.actionBar.createMenu().addItemWithWidth(1, C0553R.drawable.ic_done, AndroidUtilities.dp(56.0f));
        this.doneButton.setVisibility(8);
        this.listAdapter = new ListAdapter(context);
        this.fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = this.fragmentView;
        frameLayout.setBackgroundColor(-986896);
        ListView listView = new ListView(context);
        listView.setDivider(null);
        listView.setDividerHeight(0);
        listView.setVerticalScrollBarEnabled(false);
        listView.setDrawSelectorOnTop(true);
        frameLayout.addView(listView);
        LayoutParams layoutParams = (LayoutParams) listView.getLayoutParams();
        layoutParams.width = -1;
        layoutParams.height = -1;
        layoutParams.gravity = 48;
        listView.setLayoutParams(layoutParams);
        listView.setAdapter(this.listAdapter);
        listView.setOnItemClickListener(new C10372());
        return this.fragmentView;
    }

    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.privacyRulesUpdated) {
            checkPrivacy();
        }
    }

    private void applyCurrentPrivacySettings() {
        Iterator i$;
        User user;
        InputUser inputUser;
        TL_account_setPrivacy req = new TL_account_setPrivacy();
        req.key = new TL_inputPrivacyKeyStatusTimestamp();
        if (this.currentType != 0 && this.currentPlus.size() > 0) {
            TL_inputPrivacyValueAllowUsers rule = new TL_inputPrivacyValueAllowUsers();
            i$ = this.currentPlus.iterator();
            while (i$.hasNext()) {
                user = MessagesController.getInstance().getUser((Integer) i$.next());
                if (user != null) {
                    inputUser = MessagesController.getInputUser(user);
                    if (inputUser != null) {
                        rule.users.add(inputUser);
                    }
                }
            }
            req.rules.add(rule);
        }
        if (this.currentType != 1 && this.currentMinus.size() > 0) {
            TL_inputPrivacyValueDisallowUsers rule2 = new TL_inputPrivacyValueDisallowUsers();
            i$ = this.currentMinus.iterator();
            while (i$.hasNext()) {
                user = MessagesController.getInstance().getUser((Integer) i$.next());
                if (user != null) {
                    inputUser = MessagesController.getInputUser(user);
                    if (inputUser != null) {
                        rule2.users.add(inputUser);
                    }
                }
            }
            req.rules.add(rule2);
        }
        if (this.currentType == 0) {
            req.rules.add(new TL_inputPrivacyValueAllowAll());
        } else if (this.currentType == 1) {
            req.rules.add(new TL_inputPrivacyValueDisallowAll());
        } else if (this.currentType == 2) {
            req.rules.add(new TL_inputPrivacyValueAllowContacts());
        }
        final ProgressDialog progressDialog = new ProgressDialog(getParentActivity());
        progressDialog.setMessage(LocaleController.getString("Loading", C0553R.string.Loading));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.show();
        ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
            public void run(final TLObject response, final TL_error error) {
                AndroidUtilities.runOnUIThread(new Runnable() {
                    public void run() {
                        try {
                            progressDialog.dismiss();
                        } catch (Throwable e) {
                            FileLog.m611e("tmessages", e);
                        }
                        if (error == null) {
                            LastSeenActivity.this.finishFragment();
                            TL_account_privacyRules rules = response;
                            MessagesController.getInstance().putUsers(rules.users, false);
                            ContactsController.getInstance().setPrivacyRules(rules.rules);
                            return;
                        }
                        LastSeenActivity.this.showErrorAlert();
                    }
                });
            }
        }, 2);
    }

    private void showErrorAlert() {
        if (getParentActivity() != null) {
            Builder builder = new Builder(getParentActivity());
            builder.setTitle(LocaleController.getString("AppName", C0553R.string.AppName));
            builder.setMessage(LocaleController.getString("PrivacyFloodControlError", C0553R.string.PrivacyFloodControlError));
            builder.setPositiveButton(LocaleController.getString("OK", C0553R.string.OK), null);
            showDialog(builder.create());
        }
    }

    private void checkPrivacy() {
        this.currentPlus = new ArrayList();
        this.currentMinus = new ArrayList();
        ArrayList<PrivacyRule> privacyRules = ContactsController.getInstance().getPrivacyRules();
        if (privacyRules.size() == 0) {
            this.currentType = 1;
            return;
        }
        int type = -1;
        Iterator i$ = privacyRules.iterator();
        while (i$.hasNext()) {
            PrivacyRule rule = (PrivacyRule) i$.next();
            if (rule instanceof TL_privacyValueAllowUsers) {
                this.currentPlus.addAll(rule.users);
            } else if (rule instanceof TL_privacyValueDisallowUsers) {
                this.currentMinus.addAll(rule.users);
            } else if (rule instanceof TL_privacyValueAllowAll) {
                type = 0;
            } else if (rule instanceof TL_privacyValueDisallowAll) {
                type = 1;
            } else {
                type = 2;
            }
        }
        if (type == 0 || (type == -1 && this.currentMinus.size() > 0)) {
            this.currentType = 0;
        } else if (type == 2 || (type == -1 && this.currentMinus.size() > 0 && this.currentPlus.size() > 0)) {
            this.currentType = 2;
        } else if (type == 1 || (type == -1 && this.currentPlus.size() > 0)) {
            this.currentType = 1;
        }
        if (this.doneButton != null) {
            this.doneButton.setVisibility(8);
        }
        updateRows();
    }

    private void updateRows() {
        this.rowCount = 0;
        int i = this.rowCount;
        this.rowCount = i + 1;
        this.lastSeenSectionRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.everybodyRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.myContactsRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.nobodyRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.lastSeenDetailRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.shareSectionRow = i;
        if (this.currentType == 1 || this.currentType == 2) {
            i = this.rowCount;
            this.rowCount = i + 1;
            this.alwaysShareRow = i;
        } else {
            this.alwaysShareRow = -1;
        }
        if (this.currentType == 0 || this.currentType == 2) {
            i = this.rowCount;
            this.rowCount = i + 1;
            this.neverShareRow = i;
        } else {
            this.neverShareRow = -1;
        }
        i = this.rowCount;
        this.rowCount = i + 1;
        this.shareDetailRow = i;
        if (this.listAdapter != null) {
            this.listAdapter.notifyDataSetChanged();
        }
    }

    public void onResume() {
        super.onResume();
        this.lastCheckedType = -1;
        if (this.listAdapter != null) {
            this.listAdapter.notifyDataSetChanged();
        }
    }
}
