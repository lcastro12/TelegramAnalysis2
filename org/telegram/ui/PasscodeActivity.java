package org.telegram.ui;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Build.VERSION;
import android.os.Vibrator;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.view.ViewCompat;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.TextUtils.TruncateAt;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.text.method.PasswordTransformationMethod;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import net.hockeyapp.android.utils.HttpURLConnectionBuilder;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.C0553R;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Adapters.BaseFragmentAdapter;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.NumberPicker;
import org.telegram.ui.Components.NumberPicker.Formatter;

public class PasscodeActivity extends BaseFragment implements NotificationCenterDelegate {
    private static final int done_button = 1;
    private static final int password_item = 3;
    private static final int pin_item = 2;
    private int autoLockDetailRow;
    private int autoLockRow;
    private int changePasscodeRow;
    private int currentPasswordType = 0;
    private TextView dropDown;
    private ActionBarMenuItem dropDownContainer;
    private int fingerprintRow;
    private String firstPassword;
    private ListAdapter listAdapter;
    private ListView listView;
    private int passcodeDetailRow;
    private int passcodeRow;
    private int passcodeSetStep = 0;
    private EditText passwordEditText;
    private int rowCount;
    private TextView titleTextView;
    private int type;

    class C11222 implements OnEditorActionListener {
        C11222() {
        }

        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
            if (PasscodeActivity.this.passcodeSetStep == 0) {
                PasscodeActivity.this.processNext();
                return true;
            } else if (PasscodeActivity.this.passcodeSetStep != 1) {
                return false;
            } else {
                PasscodeActivity.this.processDone();
                return true;
            }
        }
    }

    class C11233 implements TextWatcher {
        C11233() {
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        public void afterTextChanged(Editable s) {
            if (PasscodeActivity.this.passwordEditText.length() != 4) {
                return;
            }
            if (PasscodeActivity.this.type == 2 && UserConfig.passcodeType == 0) {
                PasscodeActivity.this.processDone();
            } else if (PasscodeActivity.this.type != 1 || PasscodeActivity.this.currentPasswordType != 0) {
            } else {
                if (PasscodeActivity.this.passcodeSetStep == 0) {
                    PasscodeActivity.this.processNext();
                } else if (PasscodeActivity.this.passcodeSetStep == 1) {
                    PasscodeActivity.this.processDone();
                }
            }
        }
    }

    class C11244 implements OnCreateContextMenuListener {
        C11244() {
        }

        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
            menu.clear();
        }
    }

    class C11255 implements Callback {
        C11255() {
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        public void onDestroyActionMode(ActionMode mode) {
        }

        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return false;
        }
    }

    class C11266 implements OnClickListener {
        C11266() {
        }

        public void onClick(View view) {
            PasscodeActivity.this.dropDownContainer.toggleSubMenu();
        }
    }

    class C11287 implements OnItemClickListener {

        class C16311 implements Formatter {
            C16311() {
            }

            public String format(int value) {
                if (value == 0) {
                    return LocaleController.getString("Disabled", C0553R.string.Disabled);
                }
                if (value == 1) {
                    return LocaleController.formatString("AutoLockInTime", C0553R.string.AutoLockInTime, LocaleController.formatPluralString("Minutes", 1));
                } else if (value == 2) {
                    return LocaleController.formatString("AutoLockInTime", C0553R.string.AutoLockInTime, LocaleController.formatPluralString("Minutes", 5));
                } else if (value == 3) {
                    return LocaleController.formatString("AutoLockInTime", C0553R.string.AutoLockInTime, LocaleController.formatPluralString("Hours", 1));
                } else if (value != 4) {
                    return "";
                } else {
                    return LocaleController.formatString("AutoLockInTime", C0553R.string.AutoLockInTime, LocaleController.formatPluralString("Hours", 5));
                }
            }
        }

        C11287() {
        }

        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            if (i == PasscodeActivity.this.changePasscodeRow) {
                PasscodeActivity.this.presentFragment(new PasscodeActivity(1));
            } else if (i == PasscodeActivity.this.passcodeRow) {
                TextCheckCell cell = (TextCheckCell) view;
                if (UserConfig.passcodeHash.length() != 0) {
                    UserConfig.passcodeHash = "";
                    UserConfig.appLocked = false;
                    UserConfig.saveConfig(false);
                    int count = PasscodeActivity.this.listView.getChildCount();
                    for (int a = 0; a < count; a++) {
                        View child = PasscodeActivity.this.listView.getChildAt(a);
                        if (child instanceof TextSettingsCell) {
                            ((TextSettingsCell) child).setTextColor(-3750202);
                            break;
                        }
                    }
                    cell.setChecked(UserConfig.passcodeHash.length() != 0);
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.didSetPasscode, new Object[0]);
                    return;
                }
                PasscodeActivity.this.presentFragment(new PasscodeActivity(1));
            } else if (i == PasscodeActivity.this.autoLockRow) {
                if (PasscodeActivity.this.getParentActivity() != null) {
                    Builder builder = new Builder(PasscodeActivity.this.getParentActivity());
                    builder.setTitle(LocaleController.getString("AutoLock", C0553R.string.AutoLock));
                    final NumberPicker numberPicker = new NumberPicker(PasscodeActivity.this.getParentActivity());
                    numberPicker.setMinValue(0);
                    numberPicker.setMaxValue(4);
                    if (UserConfig.autoLockIn == 0) {
                        numberPicker.setValue(0);
                    } else if (UserConfig.autoLockIn == 60) {
                        numberPicker.setValue(1);
                    } else if (UserConfig.autoLockIn == 300) {
                        numberPicker.setValue(2);
                    } else if (UserConfig.autoLockIn == 3600) {
                        numberPicker.setValue(3);
                    } else if (UserConfig.autoLockIn == 18000) {
                        numberPicker.setValue(4);
                    }
                    numberPicker.setFormatter(new C16311());
                    builder.setView(numberPicker);
                    builder.setNegativeButton(LocaleController.getString("Done", C0553R.string.Done), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            which = numberPicker.getValue();
                            if (which == 0) {
                                UserConfig.autoLockIn = 0;
                            } else if (which == 1) {
                                UserConfig.autoLockIn = 60;
                            } else if (which == 2) {
                                UserConfig.autoLockIn = 300;
                            } else if (which == 3) {
                                UserConfig.autoLockIn = 3600;
                            } else if (which == 4) {
                                UserConfig.autoLockIn = 18000;
                            }
                            PasscodeActivity.this.listView.invalidateViews();
                            UserConfig.saveConfig(false);
                        }
                    });
                    PasscodeActivity.this.showDialog(builder.create());
                }
            } else if (i == PasscodeActivity.this.fingerprintRow) {
                UserConfig.useFingerprint = !UserConfig.useFingerprint;
                UserConfig.saveConfig(false);
                ((TextCheckCell) view).setChecked(UserConfig.useFingerprint);
            }
        }
    }

    class C11298 implements Runnable {
        C11298() {
        }

        public void run() {
            if (PasscodeActivity.this.passwordEditText != null) {
                PasscodeActivity.this.passwordEditText.requestFocus();
                AndroidUtilities.showKeyboard(PasscodeActivity.this.passwordEditText);
            }
        }
    }

    class C11309 implements OnPreDrawListener {
        C11309() {
        }

        public boolean onPreDraw() {
            PasscodeActivity.this.listView.getViewTreeObserver().removeOnPreDrawListener(this);
            PasscodeActivity.this.fixLayoutInternal();
            return true;
        }
    }

    class C16301 extends ActionBarMenuOnItemClick {
        C16301() {
        }

        public void onItemClick(int id) {
            if (id == -1) {
                PasscodeActivity.this.finishFragment();
            } else if (id == 1) {
                if (PasscodeActivity.this.passcodeSetStep == 0) {
                    PasscodeActivity.this.processNext();
                } else if (PasscodeActivity.this.passcodeSetStep == 1) {
                    PasscodeActivity.this.processDone();
                }
            } else if (id == 2) {
                PasscodeActivity.this.currentPasswordType = 0;
                PasscodeActivity.this.updateDropDownTextView();
            } else if (id == 3) {
                PasscodeActivity.this.currentPasswordType = 1;
                PasscodeActivity.this.updateDropDownTextView();
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
            return i == PasscodeActivity.this.passcodeRow || i == PasscodeActivity.this.fingerprintRow || i == PasscodeActivity.this.autoLockRow || (UserConfig.passcodeHash.length() != 0 && i == PasscodeActivity.this.changePasscodeRow);
        }

        public int getCount() {
            return PasscodeActivity.this.rowCount;
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
            boolean z = false;
            int viewType = getItemViewType(i);
            if (viewType == 0) {
                if (view == null) {
                    view = new TextCheckCell(this.mContext);
                    view.setBackgroundColor(-1);
                }
                TextCheckCell textCell = (TextCheckCell) view;
                if (i == PasscodeActivity.this.passcodeRow) {
                    String string = LocaleController.getString("Passcode", C0553R.string.Passcode);
                    if (UserConfig.passcodeHash.length() > 0) {
                        z = true;
                    }
                    textCell.setTextAndCheck(string, z, true);
                } else if (i == PasscodeActivity.this.fingerprintRow) {
                    textCell.setTextAndCheck(LocaleController.getString("UnlockFingerprint", C0553R.string.UnlockFingerprint), UserConfig.useFingerprint, true);
                }
            } else if (viewType == 1) {
                if (view == null) {
                    view = new TextSettingsCell(this.mContext);
                    view.setBackgroundColor(-1);
                }
                TextSettingsCell textCell2 = (TextSettingsCell) view;
                if (i == PasscodeActivity.this.changePasscodeRow) {
                    textCell2.setText(LocaleController.getString("ChangePasscode", C0553R.string.ChangePasscode), false);
                    textCell2.setTextColor(UserConfig.passcodeHash.length() == 0 ? -3750202 : ViewCompat.MEASURED_STATE_MASK);
                } else if (i == PasscodeActivity.this.autoLockRow) {
                    String val;
                    if (UserConfig.autoLockIn == 0) {
                        val = LocaleController.formatString("AutoLockDisabled", C0553R.string.AutoLockDisabled, new Object[0]);
                    } else if (UserConfig.autoLockIn < 3600) {
                        val = LocaleController.formatString("AutoLockInTime", C0553R.string.AutoLockInTime, LocaleController.formatPluralString("Minutes", UserConfig.autoLockIn / 60));
                    } else if (UserConfig.autoLockIn < 86400) {
                        val = LocaleController.formatString("AutoLockInTime", C0553R.string.AutoLockInTime, LocaleController.formatPluralString("Hours", (int) Math.ceil((double) ((((float) UserConfig.autoLockIn) / BitmapDescriptorFactory.HUE_YELLOW) / BitmapDescriptorFactory.HUE_YELLOW))));
                    } else {
                        val = LocaleController.formatString("AutoLockInTime", C0553R.string.AutoLockInTime, LocaleController.formatPluralString("Days", (int) Math.ceil((double) (((((float) UserConfig.autoLockIn) / BitmapDescriptorFactory.HUE_YELLOW) / BitmapDescriptorFactory.HUE_YELLOW) / 24.0f))));
                    }
                    textCell2.setTextAndValue(LocaleController.getString("AutoLock", C0553R.string.AutoLock), val, true);
                    textCell2.setTextColor(ViewCompat.MEASURED_STATE_MASK);
                }
            } else if (viewType == 2) {
                if (view == null) {
                    view = new TextInfoPrivacyCell(this.mContext);
                }
                if (i == PasscodeActivity.this.passcodeDetailRow) {
                    ((TextInfoPrivacyCell) view).setText(LocaleController.getString("ChangePasscodeInfo", C0553R.string.ChangePasscodeInfo));
                    if (PasscodeActivity.this.autoLockDetailRow != -1) {
                        view.setBackgroundResource(C0553R.drawable.greydivider);
                    } else {
                        view.setBackgroundResource(C0553R.drawable.greydivider_bottom);
                    }
                } else if (i == PasscodeActivity.this.autoLockDetailRow) {
                    ((TextInfoPrivacyCell) view).setText(LocaleController.getString("AutoLockInfo", C0553R.string.AutoLockInfo));
                    view.setBackgroundResource(C0553R.drawable.greydivider_bottom);
                }
            }
            return view;
        }

        public int getItemViewType(int i) {
            if (i == PasscodeActivity.this.passcodeRow || i == PasscodeActivity.this.fingerprintRow) {
                return 0;
            }
            if (i == PasscodeActivity.this.changePasscodeRow || i == PasscodeActivity.this.autoLockRow) {
                return 1;
            }
            if (i == PasscodeActivity.this.passcodeDetailRow || i == PasscodeActivity.this.autoLockDetailRow) {
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

    public PasscodeActivity(int type) {
        this.type = type;
    }

    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        updateRows();
        if (this.type == 0) {
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.didSetPasscode);
        }
        return true;
    }

    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        if (this.type == 0) {
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.didSetPasscode);
        }
    }

    public View createView(Context context) {
        if (this.type != 3) {
            this.actionBar.setBackButtonImage(C0553R.drawable.ic_ab_back);
        }
        this.actionBar.setAllowOverlayTitle(false);
        this.actionBar.setActionBarMenuOnItemClick(new C16301());
        this.fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = this.fragmentView;
        LayoutParams layoutParams;
        if (this.type != 0) {
            ActionBarMenu menu = this.actionBar.createMenu();
            menu.addItemWithWidth(1, C0553R.drawable.ic_done, AndroidUtilities.dp(56.0f));
            this.titleTextView = new TextView(context);
            this.titleTextView.setTextColor(-9079435);
            if (this.type != 1) {
                this.titleTextView.setText(LocaleController.getString("EnterCurrentPasscode", C0553R.string.EnterCurrentPasscode));
            } else if (UserConfig.passcodeHash.length() != 0) {
                this.titleTextView.setText(LocaleController.getString("EnterNewPasscode", C0553R.string.EnterNewPasscode));
            } else {
                this.titleTextView.setText(LocaleController.getString("EnterNewFirstPasscode", C0553R.string.EnterNewFirstPasscode));
            }
            this.titleTextView.setTextSize(1, 18.0f);
            this.titleTextView.setGravity(1);
            frameLayout.addView(this.titleTextView);
            layoutParams = (LayoutParams) this.titleTextView.getLayoutParams();
            layoutParams.width = -2;
            layoutParams.height = -2;
            layoutParams.gravity = 1;
            layoutParams.topMargin = AndroidUtilities.dp(38.0f);
            this.titleTextView.setLayoutParams(layoutParams);
            this.passwordEditText = new EditText(context);
            this.passwordEditText.setTextSize(1, 20.0f);
            this.passwordEditText.setTextColor(ViewCompat.MEASURED_STATE_MASK);
            this.passwordEditText.setMaxLines(1);
            this.passwordEditText.setLines(1);
            this.passwordEditText.setGravity(1);
            this.passwordEditText.setSingleLine(true);
            if (this.type == 1) {
                this.passcodeSetStep = 0;
                this.passwordEditText.setImeOptions(5);
            } else {
                this.passcodeSetStep = 1;
                this.passwordEditText.setImeOptions(6);
            }
            this.passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            this.passwordEditText.setTypeface(Typeface.DEFAULT);
            AndroidUtilities.clearCursorDrawable(this.passwordEditText);
            frameLayout.addView(this.passwordEditText);
            layoutParams = (LayoutParams) this.passwordEditText.getLayoutParams();
            layoutParams.topMargin = AndroidUtilities.dp(90.0f);
            layoutParams.height = AndroidUtilities.dp(36.0f);
            layoutParams.leftMargin = AndroidUtilities.dp(40.0f);
            layoutParams.gravity = 51;
            layoutParams.rightMargin = AndroidUtilities.dp(40.0f);
            layoutParams.width = -1;
            this.passwordEditText.setLayoutParams(layoutParams);
            this.passwordEditText.setOnEditorActionListener(new C11222());
            this.passwordEditText.addTextChangedListener(new C11233());
            if (VERSION.SDK_INT < 11) {
                this.passwordEditText.setOnCreateContextMenuListener(new C11244());
            } else {
                this.passwordEditText.setCustomSelectionActionModeCallback(new C11255());
            }
            if (this.type == 1) {
                int dp;
                this.dropDownContainer = new ActionBarMenuItem(context, menu, C0553R.drawable.bar_selector);
                this.dropDownContainer.setSubMenuOpenSide(1);
                this.dropDownContainer.addSubItem(2, LocaleController.getString("PasscodePIN", C0553R.string.PasscodePIN), 0);
                this.dropDownContainer.addSubItem(3, LocaleController.getString("PasscodePassword", C0553R.string.PasscodePassword), 0);
                this.actionBar.addView(this.dropDownContainer);
                layoutParams = (LayoutParams) this.dropDownContainer.getLayoutParams();
                layoutParams.height = -1;
                layoutParams.width = -2;
                layoutParams.rightMargin = AndroidUtilities.dp(40.0f);
                if (AndroidUtilities.isTablet()) {
                    dp = AndroidUtilities.dp(64.0f);
                } else {
                    dp = AndroidUtilities.dp(56.0f);
                }
                layoutParams.leftMargin = dp;
                layoutParams.gravity = 51;
                this.dropDownContainer.setLayoutParams(layoutParams);
                this.dropDownContainer.setOnClickListener(new C11266());
                this.dropDown = new TextView(context);
                this.dropDown.setGravity(3);
                this.dropDown.setSingleLine(true);
                this.dropDown.setLines(1);
                this.dropDown.setMaxLines(1);
                this.dropDown.setEllipsize(TruncateAt.END);
                this.dropDown.setTextColor(-1);
                this.dropDown.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
                this.dropDown.setCompoundDrawablesWithIntrinsicBounds(0, 0, C0553R.drawable.ic_arrow_drop_down, 0);
                this.dropDown.setCompoundDrawablePadding(AndroidUtilities.dp(4.0f));
                this.dropDown.setPadding(0, 0, AndroidUtilities.dp(10.0f), 0);
                this.dropDownContainer.addView(this.dropDown);
                layoutParams = (LayoutParams) this.dropDown.getLayoutParams();
                layoutParams.width = -2;
                layoutParams.height = -2;
                layoutParams.leftMargin = AndroidUtilities.dp(16.0f);
                layoutParams.gravity = 16;
                layoutParams.bottomMargin = AndroidUtilities.dp(1.0f);
                this.dropDown.setLayoutParams(layoutParams);
            } else {
                this.actionBar.setTitle(LocaleController.getString("Passcode", C0553R.string.Passcode));
            }
            updateDropDownTextView();
        } else {
            this.actionBar.setTitle(LocaleController.getString("Passcode", C0553R.string.Passcode));
            frameLayout.setBackgroundColor(-986896);
            this.listView = new ListView(context);
            this.listView.setDivider(null);
            this.listView.setDividerHeight(0);
            this.listView.setVerticalScrollBarEnabled(false);
            this.listView.setDrawSelectorOnTop(true);
            frameLayout.addView(this.listView);
            layoutParams = (LayoutParams) this.listView.getLayoutParams();
            layoutParams.width = -1;
            layoutParams.height = -1;
            layoutParams.gravity = 48;
            this.listView.setLayoutParams(layoutParams);
            ListView listView = this.listView;
            android.widget.ListAdapter listAdapter = new ListAdapter(context);
            this.listAdapter = listAdapter;
            listView.setAdapter(listAdapter);
            this.listView.setOnItemClickListener(new C11287());
        }
        return this.fragmentView;
    }

    public void onResume() {
        super.onResume();
        if (this.listAdapter != null) {
            this.listAdapter.notifyDataSetChanged();
        }
        if (this.type != 0) {
            AndroidUtilities.runOnUIThread(new C11298(), 200);
        }
        fixLayoutInternal();
    }

    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.didSetPasscode && this.type == 0) {
            updateRows();
            if (this.listAdapter != null) {
                this.listAdapter.notifyDataSetChanged();
            }
        }
    }

    private void updateRows() {
        this.rowCount = 0;
        int i = this.rowCount;
        this.rowCount = i + 1;
        this.passcodeRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.changePasscodeRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.passcodeDetailRow = i;
        if (UserConfig.passcodeHash.length() > 0) {
            try {
                if (VERSION.SDK_INT >= 23 && FingerprintManagerCompat.from(ApplicationLoader.applicationContext).isHardwareDetected()) {
                    i = this.rowCount;
                    this.rowCount = i + 1;
                    this.fingerprintRow = i;
                }
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
            i = this.rowCount;
            this.rowCount = i + 1;
            this.autoLockRow = i;
            i = this.rowCount;
            this.rowCount = i + 1;
            this.autoLockDetailRow = i;
            return;
        }
        this.fingerprintRow = -1;
        this.autoLockRow = -1;
        this.autoLockDetailRow = -1;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.listView != null) {
            this.listView.getViewTreeObserver().addOnPreDrawListener(new C11309());
        }
    }

    public void onTransitionAnimationEnd(boolean isOpen, boolean backward) {
        if (isOpen && this.type != 0) {
            AndroidUtilities.showKeyboard(this.passwordEditText);
        }
    }

    private void updateDropDownTextView() {
        if (this.dropDown != null) {
            if (this.currentPasswordType == 0) {
                this.dropDown.setText(LocaleController.getString("PasscodePIN", C0553R.string.PasscodePIN));
            } else if (this.currentPasswordType == 1) {
                this.dropDown.setText(LocaleController.getString("PasscodePassword", C0553R.string.PasscodePassword));
            }
        }
        if ((this.type == 1 && this.currentPasswordType == 0) || (this.type == 2 && UserConfig.passcodeType == 0)) {
            this.passwordEditText.setFilters(new InputFilter[]{new LengthFilter(4)});
            this.passwordEditText.setInputType(3);
            this.passwordEditText.setKeyListener(DigitsKeyListener.getInstance("1234567890"));
        } else if ((this.type == 1 && this.currentPasswordType == 1) || (this.type == 2 && UserConfig.passcodeType == 1)) {
            this.passwordEditText.setFilters(new InputFilter[0]);
            this.passwordEditText.setKeyListener(null);
            this.passwordEditText.setInputType(129);
        }
        this.passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
    }

    private void processNext() {
        if (this.passwordEditText.getText().length() == 0 || (this.currentPasswordType == 0 && this.passwordEditText.getText().length() != 4)) {
            onPasscodeError();
            return;
        }
        if (this.currentPasswordType == 0) {
            this.actionBar.setTitle(LocaleController.getString("PasscodePIN", C0553R.string.PasscodePIN));
        } else {
            this.actionBar.setTitle(LocaleController.getString("PasscodePassword", C0553R.string.PasscodePassword));
        }
        this.dropDownContainer.setVisibility(8);
        this.titleTextView.setText(LocaleController.getString("ReEnterYourPasscode", C0553R.string.ReEnterYourPasscode));
        this.firstPassword = this.passwordEditText.getText().toString();
        this.passwordEditText.setText("");
        this.passcodeSetStep = 1;
    }

    private void processDone() {
        if (this.passwordEditText.getText().length() == 0) {
            onPasscodeError();
        } else if (this.type == 1) {
            if (this.firstPassword.equals(this.passwordEditText.getText().toString())) {
                try {
                    UserConfig.passcodeSalt = new byte[16];
                    Utilities.random.nextBytes(UserConfig.passcodeSalt);
                    byte[] passcodeBytes = this.firstPassword.getBytes(HttpURLConnectionBuilder.DEFAULT_CHARSET);
                    byte[] bytes = new byte[(passcodeBytes.length + 32)];
                    System.arraycopy(UserConfig.passcodeSalt, 0, bytes, 0, 16);
                    System.arraycopy(passcodeBytes, 0, bytes, 16, passcodeBytes.length);
                    System.arraycopy(UserConfig.passcodeSalt, 0, bytes, passcodeBytes.length + 16, 16);
                    UserConfig.passcodeHash = Utilities.bytesToHex(Utilities.computeSHA256(bytes, 0, bytes.length));
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
                UserConfig.passcodeType = this.currentPasswordType;
                UserConfig.saveConfig(false);
                finishFragment();
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.didSetPasscode, new Object[0]);
                this.passwordEditText.clearFocus();
                AndroidUtilities.hideKeyboard(this.passwordEditText);
                return;
            }
            try {
                Toast.makeText(getParentActivity(), LocaleController.getString("PasscodeDoNotMatch", C0553R.string.PasscodeDoNotMatch), 0).show();
            } catch (Throwable e2) {
                FileLog.m611e("tmessages", e2);
            }
            AndroidUtilities.shakeView(this.titleTextView, 2.0f, 0);
            this.passwordEditText.setText("");
        } else if (this.type != 2) {
        } else {
            if (UserConfig.checkPasscode(this.passwordEditText.getText().toString())) {
                this.passwordEditText.clearFocus();
                AndroidUtilities.hideKeyboard(this.passwordEditText);
                presentFragment(new PasscodeActivity(0), true);
                return;
            }
            this.passwordEditText.setText("");
            onPasscodeError();
        }
    }

    private void onPasscodeError() {
        if (getParentActivity() != null) {
            Vibrator v = (Vibrator) getParentActivity().getSystemService("vibrator");
            if (v != null) {
                v.vibrate(200);
            }
            AndroidUtilities.shakeView(this.titleTextView, 2.0f, 0);
        }
    }

    private void fixLayoutInternal() {
        if (this.dropDownContainer != null) {
            if (!AndroidUtilities.isTablet()) {
                LayoutParams layoutParams = (LayoutParams) this.dropDownContainer.getLayoutParams();
                layoutParams.topMargin = VERSION.SDK_INT >= 21 ? AndroidUtilities.statusBarHeight : 0;
                this.dropDownContainer.setLayoutParams(layoutParams);
            }
            if (AndroidUtilities.isTablet() || ApplicationLoader.applicationContext.getResources().getConfiguration().orientation != 2) {
                this.dropDown.setTextSize(20.0f);
            } else {
                this.dropDown.setTextSize(18.0f);
            }
        }
    }
}
