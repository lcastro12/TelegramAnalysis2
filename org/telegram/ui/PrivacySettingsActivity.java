package org.telegram.ui;

import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.support.v4.os.EnvironmentCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import java.util.ArrayList;
import java.util.Iterator;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.C0553R;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.PrivacyRule;
import org.telegram.tgnet.TLRPC.TL_accountDaysTTL;
import org.telegram.tgnet.TLRPC.TL_account_setAccountTTL;
import org.telegram.tgnet.TLRPC.TL_boolTrue;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_privacyValueAllowAll;
import org.telegram.tgnet.TLRPC.TL_privacyValueAllowUsers;
import org.telegram.tgnet.TLRPC.TL_privacyValueDisallowAll;
import org.telegram.tgnet.TLRPC.TL_privacyValueDisallowUsers;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Adapters.BaseFragmentAdapter;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.LayoutHelper;

public class PrivacySettingsActivity extends BaseFragment implements NotificationCenterDelegate {
    private int blockedRow;
    private int deleteAccountDetailRow;
    private int deleteAccountRow;
    private int deleteAccountSectionRow;
    private int lastSeenDetailRow;
    private int lastSeenRow;
    private ListAdapter listAdapter;
    private int passcodeRow;
    private int passwordRow;
    private int privacySectionRow;
    private int rowCount;
    private int securitySectionRow;
    private int sessionsDetailRow;
    private int sessionsRow;

    class C11642 implements OnItemClickListener {

        class C11631 implements OnClickListener {
            C11631() {
            }

            public void onClick(DialogInterface dialog, int which) {
                int value = 0;
                if (which == 0) {
                    value = 30;
                } else if (which == 1) {
                    value = 90;
                } else if (which == 2) {
                    value = 182;
                } else if (which == 3) {
                    value = 365;
                }
                final ProgressDialog progressDialog = new ProgressDialog(PrivacySettingsActivity.this.getParentActivity());
                progressDialog.setMessage(LocaleController.getString("Loading", C0553R.string.Loading));
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.setCancelable(false);
                progressDialog.show();
                final TL_account_setAccountTTL req = new TL_account_setAccountTTL();
                req.ttl = new TL_accountDaysTTL();
                req.ttl.days = value;
                ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                    public void run(final TLObject response, TL_error error) {
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            public void run() {
                                try {
                                    progressDialog.dismiss();
                                } catch (Throwable e) {
                                    FileLog.m611e("tmessages", e);
                                }
                                if (response instanceof TL_boolTrue) {
                                    ContactsController.getInstance().setDeleteAccountTTL(req.ttl.days);
                                    PrivacySettingsActivity.this.listAdapter.notifyDataSetChanged();
                                }
                            }
                        });
                    }
                });
            }
        }

        C11642() {
        }

        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            if (i == PrivacySettingsActivity.this.blockedRow) {
                PrivacySettingsActivity.this.presentFragment(new BlockedUsersActivity());
            } else if (i == PrivacySettingsActivity.this.sessionsRow) {
                PrivacySettingsActivity.this.presentFragment(new SessionsActivity());
            } else if (i == PrivacySettingsActivity.this.deleteAccountRow) {
                if (PrivacySettingsActivity.this.getParentActivity() != null) {
                    Builder builder = new Builder(PrivacySettingsActivity.this.getParentActivity());
                    builder.setTitle(LocaleController.getString("DeleteAccountTitle", C0553R.string.DeleteAccountTitle));
                    builder.setItems(new CharSequence[]{LocaleController.formatPluralString("Months", 1), LocaleController.formatPluralString("Months", 3), LocaleController.formatPluralString("Months", 6), LocaleController.formatPluralString("Years", 1)}, new C11631());
                    builder.setNegativeButton(LocaleController.getString("Cancel", C0553R.string.Cancel), null);
                    PrivacySettingsActivity.this.showDialog(builder.create());
                }
            } else if (i == PrivacySettingsActivity.this.lastSeenRow) {
                PrivacySettingsActivity.this.presentFragment(new LastSeenActivity());
            } else if (i == PrivacySettingsActivity.this.passwordRow) {
                PrivacySettingsActivity.this.presentFragment(new TwoStepVerificationActivity(0));
            } else if (i != PrivacySettingsActivity.this.passcodeRow) {
            } else {
                if (UserConfig.passcodeHash.length() > 0) {
                    PrivacySettingsActivity.this.presentFragment(new PasscodeActivity(2));
                } else {
                    PrivacySettingsActivity.this.presentFragment(new PasscodeActivity(0));
                }
            }
        }
    }

    class C16511 extends ActionBarMenuOnItemClick {
        C16511() {
        }

        public void onItemClick(int id) {
            if (id == -1) {
                PrivacySettingsActivity.this.finishFragment();
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
            return i == PrivacySettingsActivity.this.passcodeRow || i == PrivacySettingsActivity.this.passwordRow || i == PrivacySettingsActivity.this.blockedRow || i == PrivacySettingsActivity.this.sessionsRow || ((i == PrivacySettingsActivity.this.lastSeenRow && !ContactsController.getInstance().getLoadingLastSeenInfo()) || (i == PrivacySettingsActivity.this.deleteAccountRow && !ContactsController.getInstance().getLoadingDeleteInfo()));
        }

        public int getCount() {
            return PrivacySettingsActivity.this.rowCount;
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
                    view = new TextSettingsCell(this.mContext);
                    view.setBackgroundColor(-1);
                }
                TextSettingsCell textCell = (TextSettingsCell) view;
                if (i == PrivacySettingsActivity.this.blockedRow) {
                    textCell.setText(LocaleController.getString("BlockedUsers", C0553R.string.BlockedUsers), true);
                } else if (i == PrivacySettingsActivity.this.sessionsRow) {
                    textCell.setText(LocaleController.getString("SessionsTitle", C0553R.string.SessionsTitle), false);
                } else if (i == PrivacySettingsActivity.this.passwordRow) {
                    textCell.setText(LocaleController.getString("TwoStepVerification", C0553R.string.TwoStepVerification), true);
                } else if (i == PrivacySettingsActivity.this.passcodeRow) {
                    textCell.setText(LocaleController.getString("Passcode", C0553R.string.Passcode), true);
                } else if (i == PrivacySettingsActivity.this.lastSeenRow) {
                    if (ContactsController.getInstance().getLoadingLastSeenInfo()) {
                        value = LocaleController.getString("Loading", C0553R.string.Loading);
                    } else {
                        value = PrivacySettingsActivity.this.formatRulesString();
                    }
                    textCell.setTextAndValue(LocaleController.getString("PrivacyLastSeen", C0553R.string.PrivacyLastSeen), value, false);
                } else if (i == PrivacySettingsActivity.this.deleteAccountRow) {
                    if (ContactsController.getInstance().getLoadingDeleteInfo()) {
                        value = LocaleController.getString("Loading", C0553R.string.Loading);
                    } else {
                        int ttl = ContactsController.getInstance().getDeleteAccountTTL();
                        if (ttl <= 182) {
                            value = LocaleController.formatPluralString("Months", ttl / 30);
                        } else if (ttl == 365) {
                            value = LocaleController.formatPluralString("Years", ttl / 365);
                        } else {
                            value = LocaleController.formatPluralString("Days", ttl);
                        }
                    }
                    textCell.setTextAndValue(LocaleController.getString("DeleteAccountIfAwayFor", C0553R.string.DeleteAccountIfAwayFor), value, false);
                }
            } else if (type == 1) {
                if (view == null) {
                    view = new TextInfoPrivacyCell(this.mContext);
                }
                if (i == PrivacySettingsActivity.this.deleteAccountDetailRow) {
                    ((TextInfoPrivacyCell) view).setText(LocaleController.getString("DeleteAccountHelp", C0553R.string.DeleteAccountHelp));
                    view.setBackgroundResource(C0553R.drawable.greydivider_bottom);
                } else if (i == PrivacySettingsActivity.this.lastSeenDetailRow) {
                    ((TextInfoPrivacyCell) view).setText(LocaleController.getString("LastSeenHelp", C0553R.string.LastSeenHelp));
                    view.setBackgroundResource(C0553R.drawable.greydivider);
                } else if (i == PrivacySettingsActivity.this.sessionsDetailRow) {
                    ((TextInfoPrivacyCell) view).setText(LocaleController.getString("SessionsInfo", C0553R.string.SessionsInfo));
                    view.setBackgroundResource(C0553R.drawable.greydivider);
                }
            } else if (type == 2) {
                if (view == null) {
                    view = new HeaderCell(this.mContext);
                    view.setBackgroundColor(-1);
                }
                if (i == PrivacySettingsActivity.this.privacySectionRow) {
                    ((HeaderCell) view).setText(LocaleController.getString("PrivacyTitle", C0553R.string.PrivacyTitle));
                } else if (i == PrivacySettingsActivity.this.securitySectionRow) {
                    ((HeaderCell) view).setText(LocaleController.getString("SecurityTitle", C0553R.string.SecurityTitle));
                } else if (i == PrivacySettingsActivity.this.deleteAccountSectionRow) {
                    ((HeaderCell) view).setText(LocaleController.getString("DeleteAccountTitle", C0553R.string.DeleteAccountTitle));
                }
            }
            return view;
        }

        public int getItemViewType(int i) {
            if (i == PrivacySettingsActivity.this.lastSeenRow || i == PrivacySettingsActivity.this.blockedRow || i == PrivacySettingsActivity.this.deleteAccountRow || i == PrivacySettingsActivity.this.sessionsRow || i == PrivacySettingsActivity.this.passwordRow || i == PrivacySettingsActivity.this.passcodeRow) {
                return 0;
            }
            if (i == PrivacySettingsActivity.this.deleteAccountDetailRow || i == PrivacySettingsActivity.this.lastSeenDetailRow || i == PrivacySettingsActivity.this.sessionsDetailRow) {
                return 1;
            }
            if (i == PrivacySettingsActivity.this.securitySectionRow || i == PrivacySettingsActivity.this.deleteAccountSectionRow || i == PrivacySettingsActivity.this.privacySectionRow) {
                return 2;
            }
            return 0;
        }

        public int getViewTypeCount() {
            return 3;
        }

        public boolean isEmpty() {
            return false;
        }
    }

    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        ContactsController.getInstance().loadPrivacySettings();
        this.rowCount = 0;
        int i = this.rowCount;
        this.rowCount = i + 1;
        this.privacySectionRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.blockedRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.lastSeenRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.lastSeenDetailRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.securitySectionRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.passcodeRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.passwordRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.sessionsRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.sessionsDetailRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.deleteAccountSectionRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.deleteAccountRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.deleteAccountDetailRow = i;
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
        this.actionBar.setTitle(LocaleController.getString("PrivacySettings", C0553R.string.PrivacySettings));
        this.actionBar.setActionBarMenuOnItemClick(new C16511());
        this.listAdapter = new ListAdapter(context);
        this.fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = this.fragmentView;
        frameLayout.setBackgroundColor(-986896);
        ListView listView = new ListView(context);
        listView.setDivider(null);
        listView.setDividerHeight(0);
        listView.setVerticalScrollBarEnabled(false);
        listView.setDrawSelectorOnTop(true);
        frameLayout.addView(listView, LayoutHelper.createFrame(-1, GroundOverlayOptions.NO_DIMENSION));
        listView.setAdapter(this.listAdapter);
        listView.setOnItemClickListener(new C11642());
        return this.fragmentView;
    }

    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.privacyRulesUpdated && this.listAdapter != null) {
            this.listAdapter.notifyDataSetChanged();
        }
    }

    private String formatRulesString() {
        ArrayList<PrivacyRule> privacyRules = ContactsController.getInstance().getPrivacyRules();
        if (privacyRules.size() == 0) {
            return LocaleController.getString("LastSeenNobody", C0553R.string.LastSeenNobody);
        }
        int type = -1;
        int plus = 0;
        int minus = 0;
        Iterator i$ = privacyRules.iterator();
        while (i$.hasNext()) {
            PrivacyRule rule = (PrivacyRule) i$.next();
            if (rule instanceof TL_privacyValueAllowUsers) {
                plus += rule.users.size();
            } else if (rule instanceof TL_privacyValueDisallowUsers) {
                minus += rule.users.size();
            } else if (rule instanceof TL_privacyValueAllowAll) {
                type = 0;
            } else if (rule instanceof TL_privacyValueDisallowAll) {
                type = 1;
            } else {
                type = 2;
            }
        }
        if (type == 0 || (type == -1 && minus > 0)) {
            if (minus == 0) {
                return LocaleController.getString("LastSeenEverybody", C0553R.string.LastSeenEverybody);
            }
            return LocaleController.formatString("LastSeenEverybodyMinus", C0553R.string.LastSeenEverybodyMinus, Integer.valueOf(minus));
        } else if (type == 2 || (type == -1 && minus > 0 && plus > 0)) {
            if (plus == 0 && minus == 0) {
                return LocaleController.getString("LastSeenContacts", C0553R.string.LastSeenContacts);
            }
            if (plus != 0 && minus != 0) {
                return LocaleController.formatString("LastSeenContactsMinusPlus", C0553R.string.LastSeenContactsMinusPlus, Integer.valueOf(minus), Integer.valueOf(plus));
            } else if (minus != 0) {
                return LocaleController.formatString("LastSeenContactsMinus", C0553R.string.LastSeenContactsMinus, Integer.valueOf(minus));
            } else {
                return LocaleController.formatString("LastSeenContactsPlus", C0553R.string.LastSeenContactsPlus, Integer.valueOf(plus));
            }
        } else if (type != 1 && plus <= 0) {
            return EnvironmentCompat.MEDIA_UNKNOWN;
        } else {
            if (plus == 0) {
                return LocaleController.getString("LastSeenNobody", C0553R.string.LastSeenNobody);
            }
            return LocaleController.formatString("LastSeenNobodyPlus", C0553R.string.LastSeenNobodyPlus, Integer.valueOf(plus));
        }
    }

    public void onResume() {
        super.onResume();
        if (this.listAdapter != null) {
            this.listAdapter.notifyDataSetChanged();
        }
    }
}
