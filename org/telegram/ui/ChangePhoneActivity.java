package org.telegram.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.SpannableStringBuilder;
import android.text.TextUtils.TruncateAt;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import com.google.android.gms.location.LocationStatusCodes;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.AnimationCompat.AnimatorListenerAdapterProxy;
import org.telegram.messenger.AnimationCompat.AnimatorSetProxy;
import org.telegram.messenger.AnimationCompat.ObjectAnimatorProxy;
import org.telegram.messenger.AnimationCompat.ViewProxy;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.C0553R;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.TL_account_changePhone;
import org.telegram.tgnet.TLRPC.TL_account_sendChangePhoneCode;
import org.telegram.tgnet.TLRPC.TL_account_sentChangePhoneCode;
import org.telegram.tgnet.TLRPC.TL_auth_sendCall;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.HintEditText;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.SlideView;
import org.telegram.ui.Components.TypefaceSpan;
import org.telegram.ui.CountrySelectActivity.CountrySelectActivityDelegate;

public class ChangePhoneActivity extends BaseFragment {
    private static final int done_button = 1;
    private int currentViewNum = 0;
    private ProgressDialog progressDialog;
    private SlideView[] views = new SlideView[2];

    class C15171 extends ActionBarMenuOnItemClick {
        C15171() {
        }

        public void onItemClick(int id) {
            if (id == 1) {
                ChangePhoneActivity.this.views[ChangePhoneActivity.this.currentViewNum].onNextPressed();
            } else if (id == -1) {
                ChangePhoneActivity.this.finishFragment();
            }
        }
    }

    public class LoginActivitySmsView extends SlideView implements NotificationCenterDelegate {
        private EditText codeField;
        private volatile int codeTime = 15000;
        private Timer codeTimer;
        private TextView confirmTextView;
        private Bundle currentParams;
        private boolean ignoreOnTextChange;
        private double lastCodeTime;
        private double lastCurrentTime;
        private String lastError = "";
        private boolean nextPressed = false;
        private String phoneHash;
        private String requestPhone;
        private volatile int time = 60000;
        private TextView timeText;
        private Timer timeTimer;
        private final Object timerSync = new Object();
        private boolean waitingForSms = false;

        class C08024 extends TimerTask {

            class C08011 implements Runnable {
                C08011() {
                }

                public void run() {
                    if (LoginActivitySmsView.this.codeTime <= LocationStatusCodes.GEOFENCE_NOT_AVAILABLE) {
                        LoginActivitySmsView.this.destroyCodeTimer();
                    }
                }
            }

            C08024() {
            }

            public void run() {
                double currentTime = (double) System.currentTimeMillis();
                LoginActivitySmsView.access$1626(LoginActivitySmsView.this, currentTime - LoginActivitySmsView.this.lastCodeTime);
                LoginActivitySmsView.this.lastCodeTime = currentTime;
                AndroidUtilities.runOnUIThread(new C08011());
            }
        }

        class C08055 extends TimerTask {

            class C08041 implements Runnable {

                class C15191 implements RequestDelegate {
                    C15191() {
                    }

                    public void run(TLObject response, final TL_error error) {
                        if (error != null && error.text != null) {
                            AndroidUtilities.runOnUIThread(new Runnable() {
                                public void run() {
                                    LoginActivitySmsView.this.lastError = error.text;
                                }
                            });
                        }
                    }
                }

                C08041() {
                }

                public void run() {
                    if (LoginActivitySmsView.this.time >= LocationStatusCodes.GEOFENCE_NOT_AVAILABLE) {
                        int seconds = (LoginActivitySmsView.this.time / LocationStatusCodes.GEOFENCE_NOT_AVAILABLE) - (((LoginActivitySmsView.this.time / LocationStatusCodes.GEOFENCE_NOT_AVAILABLE) / 60) * 60);
                        LoginActivitySmsView.this.timeText.setText(LocaleController.formatString("CallText", C0553R.string.CallText, Integer.valueOf(minutes), Integer.valueOf(seconds)));
                        return;
                    }
                    LoginActivitySmsView.this.timeText.setText(LocaleController.getString("Calling", C0553R.string.Calling));
                    LoginActivitySmsView.this.destroyTimer();
                    LoginActivitySmsView.this.createCodeTimer();
                    TL_auth_sendCall req = new TL_auth_sendCall();
                    req.phone_number = LoginActivitySmsView.this.requestPhone;
                    req.phone_code_hash = LoginActivitySmsView.this.phoneHash;
                    ConnectionsManager.getInstance().sendRequest(req, new C15191(), 2);
                }
            }

            C08055() {
            }

            public void run() {
                double currentTime = (double) System.currentTimeMillis();
                LoginActivitySmsView.access$1926(LoginActivitySmsView.this, currentTime - LoginActivitySmsView.this.lastCurrentTime);
                LoginActivitySmsView.this.lastCurrentTime = currentTime;
                AndroidUtilities.runOnUIThread(new C08041());
            }
        }

        class C15206 implements RequestDelegate {
            C15206() {
            }

            public void run(final TLObject response, final TL_error error) {
                AndroidUtilities.runOnUIThread(new Runnable() {
                    public void run() {
                        ChangePhoneActivity.this.needHideProgress();
                        LoginActivitySmsView.this.nextPressed = false;
                        if (error == null) {
                            User user = response;
                            LoginActivitySmsView.this.destroyTimer();
                            LoginActivitySmsView.this.destroyCodeTimer();
                            UserConfig.setCurrentUser(user);
                            UserConfig.saveConfig(true);
                            ArrayList<User> users = new ArrayList();
                            users.add(user);
                            MessagesStorage.getInstance().putUsersAndChats(users, null, true, true);
                            MessagesController.getInstance().putUser(user, false);
                            ChangePhoneActivity.this.finishFragment();
                            return;
                        }
                        LoginActivitySmsView.this.lastError = error.text;
                        LoginActivitySmsView.this.createTimer();
                        if (error.text.contains("PHONE_NUMBER_INVALID")) {
                            ChangePhoneActivity.this.needShowAlert(LocaleController.getString("InvalidPhoneNumber", C0553R.string.InvalidPhoneNumber));
                        } else if (error.text.contains("PHONE_CODE_EMPTY") || error.text.contains("PHONE_CODE_INVALID")) {
                            ChangePhoneActivity.this.needShowAlert(LocaleController.getString("InvalidCode", C0553R.string.InvalidCode));
                        } else if (error.text.contains("PHONE_CODE_EXPIRED")) {
                            ChangePhoneActivity.this.needShowAlert(LocaleController.getString("CodeExpired", C0553R.string.CodeExpired));
                        } else if (error.text.startsWith("FLOOD_WAIT")) {
                            ChangePhoneActivity.this.needShowAlert(LocaleController.getString("FloodWait", C0553R.string.FloodWait));
                        } else {
                            ChangePhoneActivity.this.needShowAlert(error.text);
                        }
                    }
                });
            }
        }

        static /* synthetic */ int access$1626(LoginActivitySmsView x0, double x1) {
            int i = (int) (((double) x0.codeTime) - x1);
            x0.codeTime = i;
            return i;
        }

        static /* synthetic */ int access$1926(LoginActivitySmsView x0, double x1) {
            int i = (int) (((double) x0.time) - x1);
            x0.time = i;
            return i;
        }

        public LoginActivitySmsView(Context context) {
            super(context);
            setOrientation(1);
            this.confirmTextView = new TextView(context);
            this.confirmTextView.setTextColor(-9079435);
            this.confirmTextView.setTextSize(1, 14.0f);
            this.confirmTextView.setGravity(3);
            this.confirmTextView.setLineSpacing((float) AndroidUtilities.dp(2.0f), 1.0f);
            addView(this.confirmTextView);
            LayoutParams layoutParams = (LayoutParams) this.confirmTextView.getLayoutParams();
            layoutParams.width = -2;
            layoutParams.height = -2;
            layoutParams.gravity = 3;
            layoutParams.leftMargin = AndroidUtilities.dp(24.0f);
            layoutParams.rightMargin = AndroidUtilities.dp(24.0f);
            this.confirmTextView.setLayoutParams(layoutParams);
            this.codeField = new EditText(context);
            this.codeField.setTextColor(-14606047);
            this.codeField.setHint(LocaleController.getString("Code", C0553R.string.Code));
            AndroidUtilities.clearCursorDrawable(this.codeField);
            this.codeField.setHintTextColor(-6842473);
            this.codeField.setImeOptions(268435461);
            this.codeField.setTextSize(1, 18.0f);
            this.codeField.setInputType(3);
            this.codeField.setMaxLines(1);
            this.codeField.setPadding(0, 0, 0, 0);
            addView(this.codeField);
            layoutParams = (LayoutParams) this.codeField.getLayoutParams();
            layoutParams.width = -1;
            layoutParams.height = AndroidUtilities.dp(36.0f);
            layoutParams.gravity = 1;
            layoutParams.topMargin = AndroidUtilities.dp(20.0f);
            layoutParams.leftMargin = AndroidUtilities.dp(24.0f);
            layoutParams.rightMargin = AndroidUtilities.dp(24.0f);
            this.codeField.setLayoutParams(layoutParams);
            this.codeField.addTextChangedListener(new TextWatcher(ChangePhoneActivity.this) {
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                public void afterTextChanged(Editable s) {
                    if (!LoginActivitySmsView.this.ignoreOnTextChange && LoginActivitySmsView.this.codeField.length() == 5) {
                        LoginActivitySmsView.this.onNextPressed();
                    }
                }
            });
            this.codeField.setOnEditorActionListener(new OnEditorActionListener(ChangePhoneActivity.this) {
                public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                    if (i != 5) {
                        return false;
                    }
                    LoginActivitySmsView.this.onNextPressed();
                    return true;
                }
            });
            this.timeText = new TextView(context);
            this.timeText.setTextSize(1, 14.0f);
            this.timeText.setTextColor(-9079435);
            this.timeText.setLineSpacing((float) AndroidUtilities.dp(2.0f), 1.0f);
            this.timeText.setGravity(3);
            addView(this.timeText);
            layoutParams = (LayoutParams) this.timeText.getLayoutParams();
            layoutParams.width = -2;
            layoutParams.height = -2;
            layoutParams.gravity = 3;
            layoutParams.topMargin = AndroidUtilities.dp(BitmapDescriptorFactory.HUE_ORANGE);
            layoutParams.leftMargin = AndroidUtilities.dp(24.0f);
            layoutParams.rightMargin = AndroidUtilities.dp(24.0f);
            this.timeText.setLayoutParams(layoutParams);
            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setGravity(80);
            addView(linearLayout);
            layoutParams = (LayoutParams) linearLayout.getLayoutParams();
            layoutParams.width = -1;
            layoutParams.height = -1;
            linearLayout.setLayoutParams(layoutParams);
            TextView wrongNumber = new TextView(context);
            wrongNumber.setGravity(3);
            wrongNumber.setTextColor(-11697229);
            wrongNumber.setTextSize(1, 14.0f);
            wrongNumber.setLineSpacing((float) AndroidUtilities.dp(2.0f), 1.0f);
            wrongNumber.setPadding(0, AndroidUtilities.dp(24.0f), 0, 0);
            linearLayout.addView(wrongNumber);
            layoutParams = (LayoutParams) wrongNumber.getLayoutParams();
            layoutParams.width = -2;
            layoutParams.height = -2;
            layoutParams.gravity = 83;
            layoutParams.bottomMargin = AndroidUtilities.dp(10.0f);
            layoutParams.leftMargin = AndroidUtilities.dp(24.0f);
            layoutParams.rightMargin = AndroidUtilities.dp(24.0f);
            wrongNumber.setLayoutParams(layoutParams);
            wrongNumber.setText(LocaleController.getString("WrongNumber", C0553R.string.WrongNumber));
            wrongNumber.setOnClickListener(new OnClickListener(ChangePhoneActivity.this) {
                public void onClick(View view) {
                    LoginActivitySmsView.this.onBackPressed();
                    ChangePhoneActivity.this.setPage(0, true, null, true);
                }
            });
        }

        public String getHeaderName() {
            return LocaleController.getString("YourCode", C0553R.string.YourCode);
        }

        public void setParams(Bundle params) {
            if (params != null) {
                this.codeField.setText("");
                AndroidUtilities.setWaitingForSms(true);
                NotificationCenter.getInstance().addObserver(this, NotificationCenter.didReceiveSmsCode);
                this.currentParams = params;
                this.waitingForSms = true;
                String phone = params.getString("phone");
                this.requestPhone = params.getString("phoneFormated");
                this.phoneHash = params.getString("phoneHash");
                this.time = params.getInt("calltime");
                if (phone != null) {
                    String number = PhoneFormat.getInstance().format(phone);
                    String str = String.format(Locale.US, LocaleController.getString("SentSmsCode", C0553R.string.SentSmsCode) + " %s", new Object[]{number});
                    try {
                        SpannableStringBuilder stringBuilder = new SpannableStringBuilder(str);
                        TypefaceSpan span = new TypefaceSpan(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
                        int idx = str.indexOf(number);
                        stringBuilder.setSpan(span, idx, number.length() + idx, 18);
                        this.confirmTextView.setText(stringBuilder);
                    } catch (Throwable e) {
                        FileLog.m611e("tmessages", e);
                        this.confirmTextView.setText(str);
                    }
                    AndroidUtilities.showKeyboard(this.codeField);
                    this.codeField.requestFocus();
                    destroyTimer();
                    destroyCodeTimer();
                    this.timeText.setText(LocaleController.formatString("CallText", C0553R.string.CallText, Integer.valueOf(1), Integer.valueOf(0)));
                    this.lastCurrentTime = (double) System.currentTimeMillis();
                    createTimer();
                }
            }
        }

        private void createCodeTimer() {
            if (this.codeTimer == null) {
                this.codeTime = 15000;
                this.codeTimer = new Timer();
                this.lastCodeTime = (double) System.currentTimeMillis();
                this.codeTimer.schedule(new C08024(), 0, 1000);
            }
        }

        private void destroyCodeTimer() {
            try {
                synchronized (this.timerSync) {
                    if (this.codeTimer != null) {
                        this.codeTimer.cancel();
                        this.codeTimer = null;
                    }
                }
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
        }

        private void createTimer() {
            if (this.timeTimer == null) {
                this.timeTimer = new Timer();
                this.timeTimer.schedule(new C08055(), 0, 1000);
            }
        }

        private void destroyTimer() {
            try {
                synchronized (this.timerSync) {
                    if (this.timeTimer != null) {
                        this.timeTimer.cancel();
                        this.timeTimer = null;
                    }
                }
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
        }

        public void onNextPressed() {
            if (!this.nextPressed) {
                this.nextPressed = true;
                this.waitingForSms = false;
                AndroidUtilities.setWaitingForSms(false);
                NotificationCenter.getInstance().removeObserver(this, NotificationCenter.didReceiveSmsCode);
                TL_account_changePhone req = new TL_account_changePhone();
                req.phone_number = this.requestPhone;
                req.phone_code = this.codeField.getText().toString();
                req.phone_code_hash = this.phoneHash;
                destroyTimer();
                ChangePhoneActivity.this.needShowProgress();
                ConnectionsManager.getInstance().sendRequest(req, new C15206(), 2);
            }
        }

        public void onBackPressed() {
            destroyTimer();
            destroyCodeTimer();
            this.currentParams = null;
            AndroidUtilities.setWaitingForSms(false);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.didReceiveSmsCode);
            this.waitingForSms = false;
        }

        public void onDestroyActivity() {
            super.onDestroyActivity();
            AndroidUtilities.setWaitingForSms(false);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.didReceiveSmsCode);
            destroyTimer();
            destroyCodeTimer();
            this.waitingForSms = false;
        }

        public void onShow() {
            super.onShow();
            if (this.codeField != null) {
                this.codeField.requestFocus();
                this.codeField.setSelection(this.codeField.length());
            }
        }

        public void didReceivedNotification(int id, Object... args) {
            if (id == NotificationCenter.didReceiveSmsCode && this.waitingForSms && this.codeField != null) {
                this.ignoreOnTextChange = true;
                this.codeField.setText("" + args[0]);
                onNextPressed();
            }
        }
    }

    public class PhoneView extends SlideView implements OnItemSelectedListener {
        private EditText codeField;
        private HashMap<String, String> codesMap = new HashMap();
        private ArrayList<String> countriesArray = new ArrayList();
        private HashMap<String, String> countriesMap = new HashMap();
        private TextView countryButton;
        private int countryState = 0;
        private boolean ignoreOnPhoneChange = false;
        private boolean ignoreOnTextChange = false;
        private boolean ignoreSelection = false;
        private boolean nextPressed = false;
        private HintEditText phoneField;
        private HashMap<String, String> phoneFormatMap = new HashMap();

        public PhoneView(Context context) {
            super(context);
            setOrientation(1);
            this.countryButton = new TextView(context);
            this.countryButton.setTextSize(1, 18.0f);
            this.countryButton.setPadding(AndroidUtilities.dp(12.0f), AndroidUtilities.dp(10.0f), AndroidUtilities.dp(12.0f), 0);
            this.countryButton.setTextColor(-14606047);
            this.countryButton.setMaxLines(1);
            this.countryButton.setSingleLine(true);
            this.countryButton.setEllipsize(TruncateAt.END);
            this.countryButton.setGravity(3);
            this.countryButton.setBackgroundResource(C0553R.drawable.spinner_states);
            addView(this.countryButton, LayoutHelper.createLinear(-1, 36, 20.0f, 0.0f, 20.0f, 14.0f));
            final ChangePhoneActivity changePhoneActivity = ChangePhoneActivity.this;
            this.countryButton.setOnClickListener(new OnClickListener() {

                class C15211 implements CountrySelectActivityDelegate {

                    class C08071 implements Runnable {
                        C08071() {
                        }

                        public void run() {
                            AndroidUtilities.showKeyboard(PhoneView.this.phoneField);
                        }
                    }

                    C15211() {
                    }

                    public void didSelectCountry(String name) {
                        PhoneView.this.selectCountry(name);
                        AndroidUtilities.runOnUIThread(new C08071(), 300);
                        PhoneView.this.phoneField.requestFocus();
                        PhoneView.this.phoneField.setSelection(PhoneView.this.phoneField.length());
                    }
                }

                public void onClick(View view) {
                    CountrySelectActivity fragment = new CountrySelectActivity();
                    fragment.setCountrySelectActivityDelegate(new C15211());
                    ChangePhoneActivity.this.presentFragment(fragment);
                }
            });
            View view = new View(context);
            view.setPadding(AndroidUtilities.dp(12.0f), 0, AndroidUtilities.dp(12.0f), 0);
            view.setBackgroundColor(-2368549);
            addView(view, LayoutHelper.createLinear(-1, 1, 24.0f, -17.5f, 24.0f, 0.0f));
            view = new LinearLayout(context);
            view.setOrientation(0);
            addView(view, LayoutHelper.createLinear(-1, -2, 0.0f, 20.0f, 0.0f, 0.0f));
            view = new TextView(context);
            view.setText("+");
            view.setTextColor(-14606047);
            view.setTextSize(1, 18.0f);
            view.addView(view, LayoutHelper.createLinear(-2, -2, 24.0f, 0.0f, 0.0f, 0.0f));
            this.codeField = new EditText(context);
            this.codeField.setInputType(3);
            this.codeField.setTextColor(-14606047);
            AndroidUtilities.clearCursorDrawable(this.codeField);
            this.codeField.setPadding(AndroidUtilities.dp(10.0f), 0, 0, 0);
            this.codeField.setTextSize(1, 18.0f);
            this.codeField.setMaxLines(1);
            this.codeField.setGravity(19);
            this.codeField.setImeOptions(268435461);
            this.codeField.setFilters(new InputFilter[]{new LengthFilter(5)});
            view.addView(this.codeField, LayoutHelper.createLinear(55, 36, -9.0f, 0.0f, 16.0f, 0.0f));
            final ChangePhoneActivity changePhoneActivity2 = ChangePhoneActivity.this;
            this.codeField.addTextChangedListener(new TextWatcher() {
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                }

                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                }

                public void afterTextChanged(Editable editable) {
                    if (PhoneView.this.ignoreOnTextChange) {
                        PhoneView.this.ignoreOnTextChange = false;
                        return;
                    }
                    PhoneView.this.ignoreOnTextChange = true;
                    String text = PhoneFormat.stripExceptNumbers(PhoneView.this.codeField.getText().toString());
                    PhoneView.this.codeField.setText(text);
                    if (text.length() == 0) {
                        PhoneView.this.countryButton.setText(LocaleController.getString("ChooseCountry", C0553R.string.ChooseCountry));
                        PhoneView.this.phoneField.setHintText(null);
                        PhoneView.this.countryState = 1;
                        return;
                    }
                    boolean ok = false;
                    String textToSet = null;
                    if (text.length() > 4) {
                        PhoneView.this.ignoreOnTextChange = true;
                        for (int a = 4; a >= 1; a--) {
                            String sub = text.substring(0, a);
                            if (((String) PhoneView.this.codesMap.get(sub)) != null) {
                                ok = true;
                                textToSet = text.substring(a, text.length()) + PhoneView.this.phoneField.getText().toString();
                                text = sub;
                                PhoneView.this.codeField.setText(sub);
                                break;
                            }
                        }
                        if (!ok) {
                            PhoneView.this.ignoreOnTextChange = true;
                            textToSet = text.substring(1, text.length()) + PhoneView.this.phoneField.getText().toString();
                            EditText access$400 = PhoneView.this.codeField;
                            text = text.substring(0, 1);
                            access$400.setText(text);
                        }
                    }
                    String country = (String) PhoneView.this.codesMap.get(text);
                    if (country != null) {
                        int index = PhoneView.this.countriesArray.indexOf(country);
                        if (index != -1) {
                            PhoneView.this.ignoreSelection = true;
                            PhoneView.this.countryButton.setText((CharSequence) PhoneView.this.countriesArray.get(index));
                            String hint = (String) PhoneView.this.phoneFormatMap.get(text);
                            PhoneView.this.phoneField.setHintText(hint != null ? hint.replace('X', '–') : null);
                            PhoneView.this.countryState = 0;
                        } else {
                            PhoneView.this.countryButton.setText(LocaleController.getString("WrongCountry", C0553R.string.WrongCountry));
                            PhoneView.this.phoneField.setHintText(null);
                            PhoneView.this.countryState = 2;
                        }
                    } else {
                        PhoneView.this.countryButton.setText(LocaleController.getString("WrongCountry", C0553R.string.WrongCountry));
                        PhoneView.this.phoneField.setHintText(null);
                        PhoneView.this.countryState = 2;
                    }
                    if (!ok) {
                        PhoneView.this.codeField.setSelection(PhoneView.this.codeField.getText().length());
                    }
                    if (textToSet != null) {
                        PhoneView.this.phoneField.requestFocus();
                        PhoneView.this.phoneField.setText(textToSet);
                        PhoneView.this.phoneField.setSelection(PhoneView.this.phoneField.length());
                    }
                }
            });
            final ChangePhoneActivity changePhoneActivity22 = ChangePhoneActivity.this;
            this.codeField.setOnEditorActionListener(new OnEditorActionListener() {
                public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                    if (i != 5) {
                        return false;
                    }
                    PhoneView.this.phoneField.requestFocus();
                    PhoneView.this.phoneField.setSelection(PhoneView.this.phoneField.length());
                    return true;
                }
            });
            this.phoneField = new HintEditText(context);
            this.phoneField.setInputType(3);
            this.phoneField.setTextColor(-14606047);
            this.phoneField.setHintTextColor(-6842473);
            this.phoneField.setPadding(0, 0, 0, 0);
            AndroidUtilities.clearCursorDrawable(this.phoneField);
            this.phoneField.setTextSize(1, 18.0f);
            this.phoneField.setMaxLines(1);
            this.phoneField.setGravity(19);
            this.phoneField.setImeOptions(268435461);
            view.addView(this.phoneField, LayoutHelper.createLinear(-1, 36, 0.0f, 0.0f, 24.0f, 0.0f));
            final ChangePhoneActivity changePhoneActivity222 = ChangePhoneActivity.this;
            this.phoneField.addTextChangedListener(new TextWatcher() {
                private int actionPosition;
                private int characterAction = -1;

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    if (count == 0 && after == 1) {
                        this.characterAction = 1;
                    } else if (count != 1 || after != 0) {
                        this.characterAction = -1;
                    } else if (s.charAt(start) != ' ' || start <= 0) {
                        this.characterAction = 2;
                    } else {
                        this.characterAction = 3;
                        this.actionPosition = start - 1;
                    }
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                public void afterTextChanged(Editable s) {
                    if (!PhoneView.this.ignoreOnPhoneChange) {
                        int a;
                        int start = PhoneView.this.phoneField.getSelectionStart();
                        String phoneChars = "0123456789";
                        String str = PhoneView.this.phoneField.getText().toString();
                        if (this.characterAction == 3) {
                            str = str.substring(0, this.actionPosition) + str.substring(this.actionPosition + 1, str.length());
                            start--;
                        }
                        StringBuilder builder = new StringBuilder(str.length());
                        for (a = 0; a < str.length(); a++) {
                            String ch = str.substring(a, a + 1);
                            if (phoneChars.contains(ch)) {
                                builder.append(ch);
                            }
                        }
                        PhoneView.this.ignoreOnPhoneChange = true;
                        String hint = PhoneView.this.phoneField.getHintText();
                        if (hint != null) {
                            a = 0;
                            while (a < builder.length()) {
                                if (a < hint.length()) {
                                    if (hint.charAt(a) == ' ') {
                                        builder.insert(a, ' ');
                                        a++;
                                        if (!(start != a || this.characterAction == 2 || this.characterAction == 3)) {
                                            start++;
                                        }
                                    }
                                    a++;
                                } else {
                                    builder.insert(a, ' ');
                                    if (!(start != a + 1 || this.characterAction == 2 || this.characterAction == 3)) {
                                        start++;
                                    }
                                }
                            }
                        }
                        PhoneView.this.phoneField.setText(builder);
                        if (start >= 0) {
                            HintEditText access$200 = PhoneView.this.phoneField;
                            if (start > PhoneView.this.phoneField.length()) {
                                start = PhoneView.this.phoneField.length();
                            }
                            access$200.setSelection(start);
                        }
                        PhoneView.this.phoneField.onTextChange();
                        PhoneView.this.ignoreOnPhoneChange = false;
                    }
                }
            });
            final ChangePhoneActivity changePhoneActivity2222 = ChangePhoneActivity.this;
            this.phoneField.setOnEditorActionListener(new OnEditorActionListener() {
                public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                    if (i != 5) {
                        return false;
                    }
                    PhoneView.this.onNextPressed();
                    return true;
                }
            });
            view = new TextView(context);
            view.setText(LocaleController.getString("ChangePhoneHelp", C0553R.string.ChangePhoneHelp));
            view.setTextColor(-9079435);
            view.setTextSize(1, 14.0f);
            view.setGravity(3);
            view.setLineSpacing((float) AndroidUtilities.dp(2.0f), 1.0f);
            addView(view, LayoutHelper.createLinear(-2, -2, 3, 24, 28, 24, 10));
            HashMap<String, String> languageMap = new HashMap();
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getResources().getAssets().open("countries.txt")));
                while (true) {
                    String line = bufferedReader.readLine();
                    if (line == null) {
                        break;
                    }
                    String[] args = line.split(";");
                    this.countriesArray.add(0, args[2]);
                    this.countriesMap.put(args[2], args[0]);
                    this.codesMap.put(args[0], args[2]);
                    if (args.length > 3) {
                        this.phoneFormatMap.put(args[0], args[3]);
                    }
                    languageMap.put(args[1], args[2]);
                }
                bufferedReader.close();
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
            final ChangePhoneActivity changePhoneActivity22222 = ChangePhoneActivity.this;
            Collections.sort(this.countriesArray, new Comparator<String>() {
                public int compare(String lhs, String rhs) {
                    return lhs.compareTo(rhs);
                }
            });
            String country = null;
            try {
                TelephonyManager telephonyManager = (TelephonyManager) ApplicationLoader.applicationContext.getSystemService("phone");
                if (telephonyManager != null) {
                    country = telephonyManager.getSimCountryIso().toUpperCase();
                }
            } catch (Throwable e2) {
                FileLog.m611e("tmessages", e2);
            }
            if (country != null) {
                String countryName = (String) languageMap.get(country);
                if (!(countryName == null || this.countriesArray.indexOf(countryName) == -1)) {
                    this.codeField.setText((CharSequence) this.countriesMap.get(countryName));
                    this.countryState = 0;
                }
            }
            if (this.codeField.length() == 0) {
                this.countryButton.setText(LocaleController.getString("ChooseCountry", C0553R.string.ChooseCountry));
                this.phoneField.setHintText(null);
                this.countryState = 1;
            }
            if (this.codeField.length() != 0) {
                AndroidUtilities.showKeyboard(this.phoneField);
                this.phoneField.requestFocus();
                this.phoneField.setSelection(this.phoneField.length());
                return;
            }
            AndroidUtilities.showKeyboard(this.codeField);
            this.codeField.requestFocus();
        }

        public void selectCountry(String name) {
            if (this.countriesArray.indexOf(name) != -1) {
                this.ignoreOnTextChange = true;
                String code = (String) this.countriesMap.get(name);
                this.codeField.setText(code);
                this.countryButton.setText(name);
                String hint = (String) this.phoneFormatMap.get(code);
                this.phoneField.setHintText(hint != null ? hint.replace('X', '–') : null);
                this.countryState = 0;
            }
        }

        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if (this.ignoreSelection) {
                this.ignoreSelection = false;
                return;
            }
            this.ignoreOnTextChange = true;
            this.codeField.setText((CharSequence) this.countriesMap.get((String) this.countriesArray.get(i)));
        }

        public void onNothingSelected(AdapterView<?> adapterView) {
        }

        public void onNextPressed() {
            if (!this.nextPressed) {
                if (this.countryState == 1) {
                    ChangePhoneActivity.this.needShowAlert(LocaleController.getString("ChooseCountry", C0553R.string.ChooseCountry));
                } else if (this.countryState == 2 && !BuildVars.DEBUG_VERSION) {
                    ChangePhoneActivity.this.needShowAlert(LocaleController.getString("WrongCountry", C0553R.string.WrongCountry));
                } else if (this.codeField.length() == 0) {
                    ChangePhoneActivity.this.needShowAlert(LocaleController.getString("InvalidPhoneNumber", C0553R.string.InvalidPhoneNumber));
                } else {
                    TL_account_sendChangePhoneCode req = new TL_account_sendChangePhoneCode();
                    String phone = PhoneFormat.stripExceptNumbers("" + this.codeField.getText() + this.phoneField.getText());
                    req.phone_number = phone;
                    final String phone2 = "+" + this.codeField.getText() + " " + this.phoneField.getText();
                    final Bundle params = new Bundle();
                    params.putString("phone", phone2);
                    params.putString("phoneFormated", phone);
                    this.nextPressed = true;
                    ChangePhoneActivity.this.needShowProgress();
                    ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                        public void run(final TLObject response, final TL_error error) {
                            AndroidUtilities.runOnUIThread(new Runnable() {
                                public void run() {
                                    PhoneView.this.nextPressed = false;
                                    if (error == null) {
                                        TL_account_sentChangePhoneCode res = response;
                                        params.putString("phoneHash", res.phone_code_hash);
                                        params.putInt("calltime", res.send_call_timeout * LocationStatusCodes.GEOFENCE_NOT_AVAILABLE);
                                        ChangePhoneActivity.this.setPage(1, true, params, false);
                                    } else if (error.text != null) {
                                        if (error.text.contains("PHONE_NUMBER_INVALID")) {
                                            ChangePhoneActivity.this.needShowAlert(LocaleController.getString("InvalidPhoneNumber", C0553R.string.InvalidPhoneNumber));
                                        } else if (error.text.contains("PHONE_CODE_EMPTY") || error.text.contains("PHONE_CODE_INVALID")) {
                                            ChangePhoneActivity.this.needShowAlert(LocaleController.getString("InvalidCode", C0553R.string.InvalidCode));
                                        } else if (error.text.contains("PHONE_CODE_EXPIRED")) {
                                            ChangePhoneActivity.this.needShowAlert(LocaleController.getString("CodeExpired", C0553R.string.CodeExpired));
                                        } else if (error.text.startsWith("FLOOD_WAIT")) {
                                            ChangePhoneActivity.this.needShowAlert(LocaleController.getString("FloodWait", C0553R.string.FloodWait));
                                        } else if (error.text.startsWith("PHONE_NUMBER_OCCUPIED")) {
                                            ChangePhoneActivity.this.needShowAlert(LocaleController.formatString("ChangePhoneNumberOccupied", C0553R.string.ChangePhoneNumberOccupied, phone2));
                                        } else {
                                            ChangePhoneActivity.this.needShowAlert(LocaleController.getString("ErrorOccurred", C0553R.string.ErrorOccurred));
                                        }
                                    }
                                    ChangePhoneActivity.this.needHideProgress();
                                }
                            });
                        }
                    }, 2);
                }
            }
        }

        public void onShow() {
            super.onShow();
            if (this.phoneField == null) {
                return;
            }
            if (this.codeField.length() != 0) {
                AndroidUtilities.showKeyboard(this.phoneField);
                this.phoneField.requestFocus();
                this.phoneField.setSelection(this.phoneField.length());
                return;
            }
            AndroidUtilities.showKeyboard(this.codeField);
            this.codeField.requestFocus();
        }

        public String getHeaderName() {
            return LocaleController.getString("ChangePhoneNewNumber", C0553R.string.ChangePhoneNewNumber);
        }
    }

    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        for (SlideView v : this.views) {
            if (v != null) {
                v.onDestroyActivity();
            }
        }
        if (this.progressDialog != null) {
            try {
                this.progressDialog.dismiss();
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
            this.progressDialog = null;
        }
        AndroidUtilities.removeAdjustResize(getParentActivity(), this.classGuid);
    }

    public View createView(Context context) {
        this.actionBar.setTitle(LocaleController.getString("AppName", C0553R.string.AppName));
        this.actionBar.setBackButtonImage(C0553R.drawable.ic_ab_back);
        this.actionBar.setActionBarMenuOnItemClick(new C15171());
        this.actionBar.createMenu().addItemWithWidth(1, C0553R.drawable.ic_done, AndroidUtilities.dp(56.0f));
        this.fragmentView = new ScrollView(context);
        ScrollView scrollView = this.fragmentView;
        scrollView.setFillViewport(true);
        FrameLayout frameLayout = new FrameLayout(context);
        scrollView.addView(frameLayout);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) frameLayout.getLayoutParams();
        layoutParams.width = -1;
        layoutParams.height = -2;
        layoutParams.gravity = 51;
        frameLayout.setLayoutParams(layoutParams);
        this.views[0] = new PhoneView(context);
        this.views[0].setVisibility(0);
        frameLayout.addView(this.views[0], LayoutHelper.createFrame(-1, -2.0f, 51, 16.0f, BitmapDescriptorFactory.HUE_ORANGE, 16.0f, 0.0f));
        this.views[1] = new LoginActivitySmsView(context);
        this.views[1].setVisibility(8);
        frameLayout.addView(this.views[1], LayoutHelper.createFrame(-1, GroundOverlayOptions.NO_DIMENSION, 51, 16.0f, BitmapDescriptorFactory.HUE_ORANGE, 16.0f, 0.0f));
        try {
            if (this.views[0] == null || this.views[1] == null) {
                FrameLayout parent = (FrameLayout) ((ScrollView) this.fragmentView).getChildAt(0);
                for (int a = 0; a < this.views.length; a++) {
                    if (this.views[a] == null) {
                        this.views[a] = (SlideView) parent.getChildAt(a);
                    }
                }
            }
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
        }
        this.actionBar.setTitle(this.views[0].getHeaderName());
        return this.fragmentView;
    }

    public void onResume() {
        super.onResume();
        AndroidUtilities.requestAdjustResize(getParentActivity(), this.classGuid);
    }

    public boolean onBackPressed() {
        if (this.currentViewNum == 0) {
            for (SlideView v : this.views) {
                if (v != null) {
                    v.onDestroyActivity();
                }
            }
            return true;
        }
        if (this.currentViewNum == 1) {
            setPage(0, true, null, true);
        }
        return false;
    }

    public void onTransitionAnimationEnd(boolean isOpen, boolean backward) {
        if (isOpen) {
            this.views[this.currentViewNum].onShow();
        }
    }

    public void needShowAlert(String text) {
        if (text != null && getParentActivity() != null) {
            Builder builder = new Builder(getParentActivity());
            builder.setTitle(LocaleController.getString("AppName", C0553R.string.AppName));
            builder.setMessage(text);
            builder.setPositiveButton(LocaleController.getString("OK", C0553R.string.OK), null);
            showDialog(builder.create());
        }
    }

    public void needShowProgress() {
        if (getParentActivity() != null && !getParentActivity().isFinishing() && this.progressDialog == null) {
            this.progressDialog = new ProgressDialog(getParentActivity());
            this.progressDialog.setMessage(LocaleController.getString("Loading", C0553R.string.Loading));
            this.progressDialog.setCanceledOnTouchOutside(false);
            this.progressDialog.setCancelable(false);
            this.progressDialog.show();
        }
    }

    public void needHideProgress() {
        if (this.progressDialog != null) {
            try {
                this.progressDialog.dismiss();
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
            this.progressDialog = null;
        }
    }

    public void setPage(int page, boolean animated, Bundle params, boolean back) {
        if (VERSION.SDK_INT > 10) {
            float f;
            final SlideView outView = this.views[this.currentViewNum];
            final SlideView newView = this.views[page];
            this.currentViewNum = page;
            newView.setParams(params);
            this.actionBar.setTitle(newView.getHeaderName());
            newView.onShow();
            ViewProxy.setX(newView, back ? (float) (-AndroidUtilities.displaySize.x) : (float) AndroidUtilities.displaySize.x);
            AnimatorSetProxy animatorSet = new AnimatorSetProxy();
            animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
            animatorSet.setDuration(300);
            Object[] objArr = new Object[2];
            String str = "translationX";
            float[] fArr = new float[1];
            if (back) {
                f = (float) AndroidUtilities.displaySize.x;
            } else {
                f = (float) (-AndroidUtilities.displaySize.x);
            }
            fArr[0] = f;
            objArr[0] = ObjectAnimatorProxy.ofFloat(outView, str, fArr);
            objArr[1] = ObjectAnimatorProxy.ofFloat(newView, "translationX", 0.0f);
            animatorSet.playTogether(objArr);
            animatorSet.addListener(new AnimatorListenerAdapterProxy() {
                public void onAnimationStart(Object animation) {
                    newView.setVisibility(0);
                }

                @SuppressLint({"NewApi"})
                public void onAnimationEnd(Object animation) {
                    outView.setVisibility(8);
                    outView.setX(0.0f);
                }
            });
            animatorSet.start();
            return;
        }
        this.views[this.currentViewNum].setVisibility(8);
        this.currentViewNum = page;
        this.views[page].setParams(params);
        this.views[page].setVisibility(0);
        this.actionBar.setTitle(this.views[page].getHeaderName());
        this.views[page].onShow();
    }
}
