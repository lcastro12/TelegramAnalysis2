package org.telegram.ui;

import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import java.util.ArrayList;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.C0553R;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.TL_account_checkUsername;
import org.telegram.tgnet.TLRPC.TL_account_updateUsername;
import org.telegram.tgnet.TLRPC.TL_boolTrue;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.LayoutHelper;

public class ChangeUsernameActivity extends BaseFragment {
    private static final int done_button = 1;
    private int checkReqId = 0;
    private Runnable checkRunnable = null;
    private TextView checkTextView;
    private View doneButton;
    private EditText firstNameField;
    private String lastCheckName = null;
    private boolean lastNameAvailable = false;

    class C08182 implements OnTouchListener {
        C08182() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            return true;
        }
    }

    class C08193 implements OnEditorActionListener {
        C08193() {
        }

        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
            if (i != 6 || ChangeUsernameActivity.this.doneButton == null) {
                return false;
            }
            ChangeUsernameActivity.this.doneButton.performClick();
            return true;
        }
    }

    class C08204 implements TextWatcher {
        C08204() {
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            ChangeUsernameActivity.this.checkUserName(ChangeUsernameActivity.this.firstNameField.getText().toString(), false);
        }

        public void afterTextChanged(Editable editable) {
        }
    }

    class C15241 extends ActionBarMenuOnItemClick {
        C15241() {
        }

        public void onItemClick(int id) {
            if (id == -1) {
                ChangeUsernameActivity.this.finishFragment();
            } else if (id == 1) {
                ChangeUsernameActivity.this.saveName();
            }
        }
    }

    public View createView(Context context) {
        this.actionBar.setBackButtonImage(C0553R.drawable.ic_ab_back);
        this.actionBar.setAllowOverlayTitle(true);
        this.actionBar.setTitle(LocaleController.getString("Username", C0553R.string.Username));
        this.actionBar.setActionBarMenuOnItemClick(new C15241());
        this.doneButton = this.actionBar.createMenu().addItemWithWidth(1, C0553R.drawable.ic_done, AndroidUtilities.dp(56.0f));
        User user = MessagesController.getInstance().getUser(Integer.valueOf(UserConfig.getClientUserId()));
        if (user == null) {
            user = UserConfig.getCurrentUser();
        }
        this.fragmentView = new LinearLayout(context);
        ((LinearLayout) this.fragmentView).setOrientation(1);
        this.fragmentView.setOnTouchListener(new C08182());
        this.firstNameField = new EditText(context);
        this.firstNameField.setTextSize(1, 18.0f);
        this.firstNameField.setHintTextColor(-6842473);
        this.firstNameField.setTextColor(-14606047);
        this.firstNameField.setMaxLines(1);
        this.firstNameField.setLines(1);
        this.firstNameField.setPadding(0, 0, 0, 0);
        this.firstNameField.setSingleLine(true);
        this.firstNameField.setGravity(LocaleController.isRTL ? 5 : 3);
        this.firstNameField.setInputType(180224);
        this.firstNameField.setImeOptions(6);
        this.firstNameField.setHint(LocaleController.getString("UsernamePlaceholder", C0553R.string.UsernamePlaceholder));
        AndroidUtilities.clearCursorDrawable(this.firstNameField);
        this.firstNameField.setOnEditorActionListener(new C08193());
        ((LinearLayout) this.fragmentView).addView(this.firstNameField, LayoutHelper.createLinear(-1, 36, 24.0f, 24.0f, 24.0f, 0.0f));
        if (!(user == null || user.username == null || user.username.length() <= 0)) {
            this.firstNameField.setText(user.username);
            this.firstNameField.setSelection(this.firstNameField.length());
        }
        this.checkTextView = new TextView(context);
        this.checkTextView.setTextSize(1, 15.0f);
        this.checkTextView.setGravity(LocaleController.isRTL ? 5 : 3);
        ((LinearLayout) this.fragmentView).addView(this.checkTextView, LayoutHelper.createLinear(-2, -2, LocaleController.isRTL ? 5 : 3, 24, 12, 24, 0));
        TextView helpTextView = new TextView(context);
        helpTextView.setTextSize(1, 15.0f);
        helpTextView.setTextColor(-9605774);
        helpTextView.setGravity(LocaleController.isRTL ? 5 : 3);
        helpTextView.setText(AndroidUtilities.replaceTags(LocaleController.getString("UsernameHelp", C0553R.string.UsernameHelp)));
        ((LinearLayout) this.fragmentView).addView(helpTextView, LayoutHelper.createLinear(-2, -2, LocaleController.isRTL ? 5 : 3, 24, 10, 24, 0));
        this.firstNameField.addTextChangedListener(new C08204());
        this.checkTextView.setVisibility(8);
        return this.fragmentView;
    }

    public void onResume() {
        super.onResume();
        if (!ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0).getBoolean("view_animations", true)) {
            this.firstNameField.requestFocus();
            AndroidUtilities.showKeyboard(this.firstNameField);
        }
    }

    private void showErrorAlert(String error) {
        if (getParentActivity() != null) {
            Builder builder = new Builder(getParentActivity());
            builder.setTitle(LocaleController.getString("AppName", C0553R.string.AppName));
            Object obj = -1;
            switch (error.hashCode()) {
                case -141887186:
                    if (error.equals("USERNAMES_UNAVAILABLE")) {
                        obj = 2;
                        break;
                    }
                    break;
                case 288843630:
                    if (error.equals("USERNAME_INVALID")) {
                        obj = null;
                        break;
                    }
                    break;
                case 533175271:
                    if (error.equals("USERNAME_OCCUPIED")) {
                        obj = 1;
                        break;
                    }
                    break;
            }
            switch (obj) {
                case null:
                    builder.setMessage(LocaleController.getString("UsernameInvalid", C0553R.string.UsernameInvalid));
                    break;
                case 1:
                    builder.setMessage(LocaleController.getString("UsernameInUse", C0553R.string.UsernameInUse));
                    break;
                case 2:
                    builder.setMessage(LocaleController.getString("FeatureUnavailable", C0553R.string.FeatureUnavailable));
                    break;
                default:
                    builder.setMessage(LocaleController.getString("ErrorOccurred", C0553R.string.ErrorOccurred));
                    break;
            }
            builder.setPositiveButton(LocaleController.getString("OK", C0553R.string.OK), null);
            showDialog(builder.create());
        }
    }

    private boolean checkUserName(final String name, boolean alert) {
        if (name == null || name.length() <= 0) {
            this.checkTextView.setVisibility(8);
        } else {
            this.checkTextView.setVisibility(0);
        }
        if (alert && name.length() == 0) {
            return true;
        }
        if (this.checkRunnable != null) {
            AndroidUtilities.cancelRunOnUIThread(this.checkRunnable);
            this.checkRunnable = null;
            this.lastCheckName = null;
            if (this.checkReqId != 0) {
                ConnectionsManager.getInstance().cancelRequest(this.checkReqId, true);
            }
        }
        this.lastNameAvailable = false;
        if (name != null) {
            if (name.startsWith("_") || name.endsWith("_")) {
                this.checkTextView.setText(LocaleController.getString("UsernameInvalid", C0553R.string.UsernameInvalid));
                this.checkTextView.setTextColor(-3198928);
                return false;
            }
            int a = 0;
            while (a < name.length()) {
                char ch = name.charAt(a);
                if (a == 0 && ch >= '0' && ch <= '9') {
                    if (alert) {
                        showErrorAlert(LocaleController.getString("UsernameInvalidStartNumber", C0553R.string.UsernameInvalidStartNumber));
                    } else {
                        this.checkTextView.setText(LocaleController.getString("UsernameInvalidStartNumber", C0553R.string.UsernameInvalidStartNumber));
                        this.checkTextView.setTextColor(-3198928);
                    }
                    return false;
                } else if ((ch < '0' || ch > '9') && ((ch < 'a' || ch > 'z') && ((ch < 'A' || ch > 'Z') && ch != '_'))) {
                    if (alert) {
                        showErrorAlert(LocaleController.getString("UsernameInvalid", C0553R.string.UsernameInvalid));
                    } else {
                        this.checkTextView.setText(LocaleController.getString("UsernameInvalid", C0553R.string.UsernameInvalid));
                        this.checkTextView.setTextColor(-3198928);
                    }
                    return false;
                } else {
                    a++;
                }
            }
        }
        if (name == null || name.length() < 5) {
            if (alert) {
                showErrorAlert(LocaleController.getString("UsernameInvalidShort", C0553R.string.UsernameInvalidShort));
            } else {
                this.checkTextView.setText(LocaleController.getString("UsernameInvalidShort", C0553R.string.UsernameInvalidShort));
                this.checkTextView.setTextColor(-3198928);
            }
            return false;
        } else if (name.length() > 32) {
            if (alert) {
                showErrorAlert(LocaleController.getString("UsernameInvalidLong", C0553R.string.UsernameInvalidLong));
            } else {
                this.checkTextView.setText(LocaleController.getString("UsernameInvalidLong", C0553R.string.UsernameInvalidLong));
                this.checkTextView.setTextColor(-3198928);
            }
            return false;
        } else if (alert) {
            return true;
        } else {
            String currentName = UserConfig.getCurrentUser().username;
            if (currentName == null) {
                currentName = "";
            }
            if (name.equals(currentName)) {
                this.checkTextView.setText(LocaleController.formatString("UsernameAvailable", C0553R.string.UsernameAvailable, name));
                this.checkTextView.setTextColor(-14248148);
                return true;
            }
            this.checkTextView.setText(LocaleController.getString("UsernameChecking", C0553R.string.UsernameChecking));
            this.checkTextView.setTextColor(-9605774);
            this.lastCheckName = name;
            this.checkRunnable = new Runnable() {

                class C15251 implements RequestDelegate {
                    C15251() {
                    }

                    public void run(final TLObject response, final TL_error error) {
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            public void run() {
                                ChangeUsernameActivity.this.checkReqId = 0;
                                if (ChangeUsernameActivity.this.lastCheckName != null && ChangeUsernameActivity.this.lastCheckName.equals(name)) {
                                    if (error == null && (response instanceof TL_boolTrue)) {
                                        ChangeUsernameActivity.this.checkTextView.setText(LocaleController.formatString("UsernameAvailable", C0553R.string.UsernameAvailable, name));
                                        ChangeUsernameActivity.this.checkTextView.setTextColor(-14248148);
                                        ChangeUsernameActivity.this.lastNameAvailable = true;
                                        return;
                                    }
                                    ChangeUsernameActivity.this.checkTextView.setText(LocaleController.getString("UsernameInUse", C0553R.string.UsernameInUse));
                                    ChangeUsernameActivity.this.checkTextView.setTextColor(-3198928);
                                    ChangeUsernameActivity.this.lastNameAvailable = false;
                                }
                            }
                        });
                    }
                }

                public void run() {
                    TL_account_checkUsername req = new TL_account_checkUsername();
                    req.username = name;
                    ChangeUsernameActivity.this.checkReqId = ConnectionsManager.getInstance().sendRequest(req, new C15251(), 2);
                }
            };
            AndroidUtilities.runOnUIThread(this.checkRunnable, 300);
            return true;
        }
    }

    private void saveName() {
        if (checkUserName(this.firstNameField.getText().toString(), true)) {
            User user = UserConfig.getCurrentUser();
            if (getParentActivity() != null && user != null) {
                String currentName = user.username;
                if (currentName == null) {
                    currentName = "";
                }
                String newName = this.firstNameField.getText().toString();
                if (currentName.equals(newName)) {
                    finishFragment();
                    return;
                }
                final ProgressDialog progressDialog = new ProgressDialog(getParentActivity());
                progressDialog.setMessage(LocaleController.getString("Loading", C0553R.string.Loading));
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.setCancelable(false);
                TL_account_updateUsername req = new TL_account_updateUsername();
                req.username = newName;
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateInterfaces, Integer.valueOf(1));
                final int reqId = ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                    public void run(TLObject response, final TL_error error) {
                        if (error == null) {
                            final User user = (User) response;
                            AndroidUtilities.runOnUIThread(new Runnable() {
                                public void run() {
                                    try {
                                        progressDialog.dismiss();
                                    } catch (Throwable e) {
                                        FileLog.m611e("tmessages", e);
                                    }
                                    ArrayList<User> users = new ArrayList();
                                    users.add(user);
                                    MessagesController.getInstance().putUsers(users, false);
                                    MessagesStorage.getInstance().putUsersAndChats(users, null, false, true);
                                    UserConfig.saveConfig(true);
                                    ChangeUsernameActivity.this.finishFragment();
                                }
                            });
                            return;
                        }
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            public void run() {
                                try {
                                    progressDialog.dismiss();
                                } catch (Throwable e) {
                                    FileLog.m611e("tmessages", e);
                                }
                                ChangeUsernameActivity.this.showErrorAlert(error.text);
                            }
                        });
                    }
                }, 2);
                ConnectionsManager.getInstance().bindRequestToGuid(reqId, this.classGuid);
                progressDialog.setButton(-2, LocaleController.getString("Cancel", C0553R.string.Cancel), new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ConnectionsManager.getInstance().cancelRequest(reqId, true);
                        try {
                            dialog.dismiss();
                        } catch (Throwable e) {
                            FileLog.m611e("tmessages", e);
                        }
                    }
                });
                progressDialog.show();
            }
        }
    }

    public void onTransitionAnimationEnd(boolean isOpen, boolean backward) {
        if (isOpen) {
            this.firstNameField.requestFocus();
            AndroidUtilities.showKeyboard(this.firstNameField);
        }
    }
}
