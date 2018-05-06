package org.telegram.messenger;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Environment;
import android.support.v4.internal.view.SupportMenu;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.StateSet;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.EdgeEffect;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import java.util.regex.Pattern;
import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.CrashManagerListener;
import net.hockeyapp.android.Strings;
import net.hockeyapp.android.UpdateManager;
import org.telegram.messenger.AnimationCompat.AnimatorListenerAdapterProxy;
import org.telegram.messenger.AnimationCompat.AnimatorSetProxy;
import org.telegram.messenger.AnimationCompat.ObjectAnimatorProxy;
import org.telegram.messenger.AnimationCompat.ViewProxy;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC.EncryptedChat;
import org.telegram.ui.Components.ForegroundDetector;
import org.telegram.ui.Components.NumberPicker;
import org.telegram.ui.Components.NumberPicker.Formatter;
import org.telegram.ui.Components.TypefaceSpan;

public class AndroidUtilities {
    public static final int FLAG_TAG_ALL = 7;
    public static final int FLAG_TAG_BOLD = 2;
    public static final int FLAG_TAG_BR = 1;
    public static final int FLAG_TAG_COLOR = 4;
    public static Pattern WEB_URL;
    private static int adjustOwnerClassGuid = 0;
    public static float density;
    public static DisplayMetrics displayMetrics = new DisplayMetrics();
    public static Point displaySize = new Point();
    private static Boolean isTablet = null;
    public static int leftBaseline = (isTablet() ? 80 : 72);
    public static Integer photoSize = null;
    private static int prevOrientation = -10;
    private static final Object smsLock = new Object();
    public static int statusBarHeight = 0;
    private static final Hashtable<String, Typeface> typefaceCache = new Hashtable();
    public static boolean usingHardwareInput;
    private static boolean waitingForSms = false;

    static class C14431 implements Formatter {
        C14431() {
        }

        public String format(int value) {
            if (value == 0) {
                return LocaleController.getString("ShortMessageLifetimeForever", C0553R.string.ShortMessageLifetimeForever);
            }
            if (value >= 1 && value < 16) {
                return AndroidUtilities.formatTTLString(value);
            }
            if (value == 16) {
                return AndroidUtilities.formatTTLString(30);
            }
            if (value == 17) {
                return AndroidUtilities.formatTTLString(60);
            }
            if (value == 18) {
                return AndroidUtilities.formatTTLString(3600);
            }
            if (value == 19) {
                return AndroidUtilities.formatTTLString(86400);
            }
            if (value == 20) {
                return AndroidUtilities.formatTTLString(604800);
            }
            return "";
        }
    }

    static class C17394 extends CrashManagerListener {
        C17394() {
        }

        public boolean includeDeviceData() {
            return true;
        }
    }

    static {
        density = 1.0f;
        WEB_URL = null;
        try {
            String GOOD_IRI_CHAR = "a-zA-Z0-9 -퟿豈-﷏ﷰ-￯";
            String IRI = "[a-zA-Z0-9 -퟿豈-﷏ﷰ-￯]([a-zA-Z0-9 -퟿豈-﷏ﷰ-￯\\-]{0,61}[a-zA-Z0-9 -퟿豈-﷏ﷰ-￯]){0,1}";
            String GOOD_GTLD_CHAR = "a-zA-Z -퟿豈-﷏ﷰ-￯";
            String GTLD = "[a-zA-Z -퟿豈-﷏ﷰ-￯]{2,63}";
            String HOST_NAME = "([a-zA-Z0-9 -퟿豈-﷏ﷰ-￯]([a-zA-Z0-9 -퟿豈-﷏ﷰ-￯\\-]{0,61}[a-zA-Z0-9 -퟿豈-﷏ﷰ-￯]){0,1}\\.)+[a-zA-Z -퟿豈-﷏ﷰ-￯]{2,63}";
            WEB_URL = Pattern.compile("((?:(http|https|Http|Https):\\/\\/(?:(?:[a-zA-Z0-9\\$\\-\\_\\.\\+\\!\\*\\'\\(\\)\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,64}(?:\\:(?:[a-zA-Z0-9\\$\\-\\_\\.\\+\\!\\*\\'\\(\\)\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,25})?\\@)?)?(?:" + Pattern.compile("(([a-zA-Z0-9 -퟿豈-﷏ﷰ-￯]([a-zA-Z0-9 -퟿豈-﷏ﷰ-￯\\-]{0,61}[a-zA-Z0-9 -퟿豈-﷏ﷰ-￯]){0,1}\\.)+[a-zA-Z -퟿豈-﷏ﷰ-￯]{2,63}|" + Pattern.compile("((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[0-9]))") + ")") + ")" + "(?:\\:\\d{1,5})?)" + "(\\/(?:(?:[" + "a-zA-Z0-9 -퟿豈-﷏ﷰ-￯" + "\\;\\/\\?\\:\\@\\&\\=\\#\\~" + "\\-\\.\\+\\!\\*\\'\\(\\)\\,\\_])|(?:\\%[a-fA-F0-9]{2}))*)?" + "(?:\\b|$)");
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
        }
        density = ApplicationLoader.applicationContext.getResources().getDisplayMetrics().density;
        checkDisplaySize();
    }

    public static void requestAdjustResize(Activity activity, int classGuid) {
        if (activity != null && !isTablet()) {
            activity.getWindow().setSoftInputMode(16);
            adjustOwnerClassGuid = classGuid;
        }
    }

    public static void removeAdjustResize(Activity activity, int classGuid) {
        if (activity != null && !isTablet() && adjustOwnerClassGuid == classGuid) {
            activity.getWindow().setSoftInputMode(32);
        }
    }

    public static void lockOrientation(Activity activity) {
        if (activity != null && prevOrientation == -10 && VERSION.SDK_INT >= 9) {
            try {
                prevOrientation = activity.getRequestedOrientation();
                WindowManager manager = (WindowManager) activity.getSystemService("window");
                if (manager != null && manager.getDefaultDisplay() != null) {
                    int rotation = manager.getDefaultDisplay().getRotation();
                    int orientation = activity.getResources().getConfiguration().orientation;
                    int SCREEN_ORIENTATION_REVERSE_LANDSCAPE = 8;
                    int SCREEN_ORIENTATION_REVERSE_PORTRAIT = 9;
                    if (VERSION.SDK_INT < 9) {
                        SCREEN_ORIENTATION_REVERSE_LANDSCAPE = 0;
                        SCREEN_ORIENTATION_REVERSE_PORTRAIT = 1;
                    }
                    if (rotation == 3) {
                        if (orientation == 1) {
                            activity.setRequestedOrientation(1);
                        } else {
                            activity.setRequestedOrientation(SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                        }
                    } else if (rotation == 1) {
                        if (orientation == 1) {
                            activity.setRequestedOrientation(SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                        } else {
                            activity.setRequestedOrientation(0);
                        }
                    } else if (rotation == 0) {
                        if (orientation == 2) {
                            activity.setRequestedOrientation(0);
                        } else {
                            activity.setRequestedOrientation(1);
                        }
                    } else if (orientation == 2) {
                        activity.setRequestedOrientation(SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                    } else {
                        activity.setRequestedOrientation(SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                    }
                }
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
        }
    }

    public static void unlockOrientation(Activity activity) {
        if (activity != null && VERSION.SDK_INT >= 9) {
            try {
                if (prevOrientation != -10) {
                    activity.setRequestedOrientation(prevOrientation);
                    prevOrientation = -10;
                }
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
        }
    }

    public static Typeface getTypeface(String assetPath) {
        Typeface typeface;
        synchronized (typefaceCache) {
            if (!typefaceCache.containsKey(assetPath)) {
                try {
                    typefaceCache.put(assetPath, Typeface.createFromAsset(ApplicationLoader.applicationContext.getAssets(), assetPath));
                } catch (Exception e) {
                    FileLog.m609e("Typefaces", "Could not get typeface '" + assetPath + "' because " + e.getMessage());
                    typeface = null;
                }
            }
            typeface = (Typeface) typefaceCache.get(assetPath);
        }
        return typeface;
    }

    public static boolean isWaitingForSms() {
        boolean value;
        synchronized (smsLock) {
            value = waitingForSms;
        }
        return value;
    }

    public static void setWaitingForSms(boolean value) {
        synchronized (smsLock) {
            waitingForSms = value;
        }
    }

    public static void showKeyboard(View view) {
        if (view != null) {
            ((InputMethodManager) view.getContext().getSystemService("input_method")).showSoftInput(view, 1);
        }
    }

    public static boolean isKeyboardShowed(View view) {
        if (view == null) {
            return false;
        }
        return ((InputMethodManager) view.getContext().getSystemService("input_method")).isActive(view);
    }

    public static void hideKeyboard(View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService("input_method");
            if (imm.isActive()) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    public static File getCacheDir() {
        File file;
        String state = null;
        try {
            state = Environment.getExternalStorageState();
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
        }
        if (state == null || state.startsWith("mounted")) {
            try {
                file = ApplicationLoader.applicationContext.getExternalCacheDir();
                if (file != null) {
                    return file;
                }
            } catch (Throwable e2) {
                FileLog.m611e("tmessages", e2);
            }
        }
        try {
            file = ApplicationLoader.applicationContext.getCacheDir();
            if (file != null) {
                return file;
            }
        } catch (Throwable e22) {
            FileLog.m611e("tmessages", e22);
        }
        return new File("");
    }

    public static int dp(float value) {
        if (value == 0.0f) {
            return 0;
        }
        return (int) Math.ceil((double) (density * value));
    }

    public static int compare(int lhs, int rhs) {
        if (lhs == rhs) {
            return 0;
        }
        if (lhs > rhs) {
            return 1;
        }
        return -1;
    }

    public static float dpf2(float value) {
        if (value == 0.0f) {
            return 0.0f;
        }
        return density * value;
    }

    public static void checkDisplaySize() {
        boolean z = true;
        try {
            Configuration configuration = ApplicationLoader.applicationContext.getResources().getConfiguration();
            if (configuration.keyboard == 1 || configuration.hardKeyboardHidden != 1) {
                z = false;
            }
            usingHardwareInput = z;
            WindowManager manager = (WindowManager) ApplicationLoader.applicationContext.getSystemService("window");
            if (manager != null) {
                Display display = manager.getDefaultDisplay();
                if (display != null) {
                    display.getMetrics(displayMetrics);
                    if (VERSION.SDK_INT < 13) {
                        displaySize.set(display.getWidth(), display.getHeight());
                    } else {
                        display.getSize(displaySize);
                    }
                    FileLog.m609e("tmessages", "display size = " + displaySize.x + " " + displaySize.y + " " + displayMetrics.xdpi + "x" + displayMetrics.ydpi);
                }
            }
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
        }
    }

    public static float getPixelsInCM(float cm, boolean isX) {
        return (isX ? displayMetrics.xdpi : displayMetrics.ydpi) * (cm / 2.54f);
    }

    public static long makeBroadcastId(int id) {
        return 4294967296L | (((long) id) & 4294967295L);
    }

    public static int getMyLayerVersion(int layer) {
        return SupportMenu.USER_MASK & layer;
    }

    public static int getPeerLayerVersion(int layer) {
        return (layer >> 16) & SupportMenu.USER_MASK;
    }

    public static int setMyLayerVersion(int layer, int version) {
        return (SupportMenu.CATEGORY_MASK & layer) | version;
    }

    public static int setPeerLayerVersion(int layer, int version) {
        return (SupportMenu.USER_MASK & layer) | (version << 16);
    }

    public static void runOnUIThread(Runnable runnable) {
        runOnUIThread(runnable, 0);
    }

    public static void runOnUIThread(Runnable runnable, long delay) {
        if (delay == 0) {
            ApplicationLoader.applicationHandler.post(runnable);
        } else {
            ApplicationLoader.applicationHandler.postDelayed(runnable, delay);
        }
    }

    public static void cancelRunOnUIThread(Runnable runnable) {
        ApplicationLoader.applicationHandler.removeCallbacks(runnable);
    }

    public static boolean isTablet() {
        if (isTablet == null) {
            isTablet = Boolean.valueOf(ApplicationLoader.applicationContext.getResources().getBoolean(C0553R.bool.isTablet));
        }
        return isTablet.booleanValue();
    }

    public static boolean isSmallTablet() {
        return ((float) Math.min(displaySize.x, displaySize.y)) / density <= 700.0f;
    }

    public static int getMinTabletSide() {
        int leftSide;
        if (isSmallTablet()) {
            int smallSide = Math.min(displaySize.x, displaySize.y);
            int maxSide = Math.max(displaySize.x, displaySize.y);
            leftSide = (maxSide * 35) / 100;
            if (leftSide < dp(320.0f)) {
                leftSide = dp(320.0f);
            }
            return Math.min(smallSide, maxSide - leftSide);
        }
        smallSide = Math.min(displaySize.x, displaySize.y);
        leftSide = (smallSide * 35) / 100;
        if (leftSide < dp(320.0f)) {
            leftSide = dp(320.0f);
        }
        return smallSide - leftSide;
    }

    public static int getPhotoSize() {
        if (photoSize == null) {
            if (VERSION.SDK_INT >= 16) {
                photoSize = Integer.valueOf(Strings.LOGIN_HEADLINE_TEXT_ID);
            } else {
                photoSize = Integer.valueOf(800);
            }
        }
        return photoSize.intValue();
    }

    public static String formatTTLString(int ttl) {
        if (ttl < 60) {
            return LocaleController.formatPluralString("Seconds", ttl);
        }
        if (ttl < 3600) {
            return LocaleController.formatPluralString("Minutes", ttl / 60);
        }
        if (ttl < 86400) {
            return LocaleController.formatPluralString("Hours", (ttl / 60) / 60);
        }
        if (ttl < 604800) {
            return LocaleController.formatPluralString("Days", ((ttl / 60) / 60) / 24);
        }
        int days = ((ttl / 60) / 60) / 24;
        if (ttl % 7 == 0) {
            return LocaleController.formatPluralString("Weeks", days / 7);
        }
        return String.format("%s %s", new Object[]{LocaleController.formatPluralString("Weeks", days / 7), LocaleController.formatPluralString("Days", days % 7)});
    }

    public static Builder buildTTLAlert(Context context, final EncryptedChat encryptedChat) {
        Builder builder = new Builder(context);
        builder.setTitle(LocaleController.getString("MessageLifetime", C0553R.string.MessageLifetime));
        final NumberPicker numberPicker = new NumberPicker(context);
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(20);
        if (encryptedChat.ttl > 0 && encryptedChat.ttl < 16) {
            numberPicker.setValue(encryptedChat.ttl);
        } else if (encryptedChat.ttl == 30) {
            numberPicker.setValue(16);
        } else if (encryptedChat.ttl == 60) {
            numberPicker.setValue(17);
        } else if (encryptedChat.ttl == 3600) {
            numberPicker.setValue(18);
        } else if (encryptedChat.ttl == 86400) {
            numberPicker.setValue(19);
        } else if (encryptedChat.ttl == 604800) {
            numberPicker.setValue(20);
        } else if (encryptedChat.ttl == 0) {
            numberPicker.setValue(0);
        }
        numberPicker.setFormatter(new C14431());
        builder.setView(numberPicker);
        builder.setNegativeButton(LocaleController.getString("Done", C0553R.string.Done), new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                int oldValue = encryptedChat.ttl;
                which = numberPicker.getValue();
                if (which >= 0 && which < 16) {
                    encryptedChat.ttl = which;
                } else if (which == 16) {
                    encryptedChat.ttl = 30;
                } else if (which == 17) {
                    encryptedChat.ttl = 60;
                } else if (which == 18) {
                    encryptedChat.ttl = 3600;
                } else if (which == 19) {
                    encryptedChat.ttl = 86400;
                } else if (which == 20) {
                    encryptedChat.ttl = 604800;
                }
                if (oldValue != encryptedChat.ttl) {
                    SecretChatHelper.getInstance().sendTTLMessage(encryptedChat, null);
                    MessagesStorage.getInstance().updateEncryptedChatTTL(encryptedChat);
                }
            }
        });
        return builder;
    }

    public static void clearCursorDrawable(EditText editText) {
        if (editText != null && VERSION.SDK_INT >= 12) {
            try {
                Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
                mCursorDrawableRes.setAccessible(true);
                mCursorDrawableRes.setInt(editText, 0);
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
        }
    }

    public static void setProgressBarAnimationDuration(ProgressBar progressBar, int duration) {
        if (progressBar != null) {
            try {
                Field mCursorDrawableRes = ProgressBar.class.getDeclaredField("mDuration");
                mCursorDrawableRes.setAccessible(true);
                mCursorDrawableRes.setInt(progressBar, duration);
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
        }
    }

    public static int getViewInset(View view) {
        int i = 0;
        if (!(view == null || VERSION.SDK_INT < 21 || view.getHeight() == displaySize.y || view.getHeight() == displaySize.y - statusBarHeight)) {
            try {
                Field mAttachInfoField = View.class.getDeclaredField("mAttachInfo");
                mAttachInfoField.setAccessible(true);
                Object mAttachInfo = mAttachInfoField.get(view);
                if (mAttachInfo != null) {
                    Field mStableInsetsField = mAttachInfo.getClass().getDeclaredField("mStableInsets");
                    mStableInsetsField.setAccessible(true);
                    i = ((Rect) mStableInsetsField.get(mAttachInfo)).bottom;
                }
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
        }
        return i;
    }

    public static Point getRealScreenSize() {
        Point size = new Point();
        try {
            WindowManager windowManager = (WindowManager) ApplicationLoader.applicationContext.getSystemService("window");
            if (VERSION.SDK_INT >= 17) {
                windowManager.getDefaultDisplay().getRealSize(size);
            } else {
                try {
                    size.set(((Integer) Display.class.getMethod("getRawWidth", new Class[0]).invoke(windowManager.getDefaultDisplay(), new Object[0])).intValue(), ((Integer) Display.class.getMethod("getRawHeight", new Class[0]).invoke(windowManager.getDefaultDisplay(), new Object[0])).intValue());
                } catch (Throwable e) {
                    size.set(windowManager.getDefaultDisplay().getWidth(), windowManager.getDefaultDisplay().getHeight());
                    FileLog.m611e("tmessages", e);
                }
            }
        } catch (Throwable e2) {
            FileLog.m611e("tmessages", e2);
        }
        return size;
    }

    public static void setListViewEdgeEffectColor(AbsListView listView, int color) {
        if (VERSION.SDK_INT >= 21) {
            try {
                Field field = AbsListView.class.getDeclaredField("mEdgeGlowTop");
                field.setAccessible(true);
                EdgeEffect mEdgeGlowTop = (EdgeEffect) field.get(listView);
                if (mEdgeGlowTop != null) {
                    mEdgeGlowTop.setColor(color);
                }
                field = AbsListView.class.getDeclaredField("mEdgeGlowBottom");
                field.setAccessible(true);
                EdgeEffect mEdgeGlowBottom = (EdgeEffect) field.get(listView);
                if (mEdgeGlowBottom != null) {
                    mEdgeGlowBottom.setColor(color);
                }
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
        }
    }

    @SuppressLint({"NewApi"})
    public static void clearDrawableAnimation(View view) {
        if (VERSION.SDK_INT >= 21 && view != null) {
            Drawable drawable;
            if (view instanceof ListView) {
                drawable = ((ListView) view).getSelector();
                if (drawable != null) {
                    drawable.setState(StateSet.NOTHING);
                    return;
                }
                return;
            }
            drawable = view.getBackground();
            if (drawable != null) {
                drawable.setState(StateSet.NOTHING);
                drawable.jumpToCurrentState();
            }
        }
    }

    public static Spannable replaceTags(String str) {
        return replaceTags(str, 7);
    }

    public static Spannable replaceTags(String str, int flag) {
        try {
            int start;
            int end;
            int a;
            StringBuilder stringBuilder = new StringBuilder(str);
            if ((flag & 1) != 0) {
                while (true) {
                    start = stringBuilder.indexOf("<br>");
                    if (start != -1) {
                        stringBuilder.replace(start, start + 4, "\n");
                    } else {
                        while (true) {
                            stringBuilder.replace(start, start + 5, "\n");
                        }
                    }
                }
                start = stringBuilder.indexOf("<br/>");
                if (start == -1) {
                    break;
                }
                stringBuilder.replace(start, start + 5, "\n");
            }
            ArrayList<Integer> bolds = new ArrayList();
            if ((flag & 2) != 0) {
                while (true) {
                    start = stringBuilder.indexOf("<b>");
                    if (start == -1) {
                        break;
                    }
                    stringBuilder.replace(start, start + 3, "");
                    end = stringBuilder.indexOf("</b>");
                    if (end == -1) {
                        end = stringBuilder.indexOf("<b>");
                    }
                    stringBuilder.replace(end, end + 4, "");
                    bolds.add(Integer.valueOf(start));
                    bolds.add(Integer.valueOf(end));
                }
            }
            ArrayList<Integer> colors = new ArrayList();
            if ((flag & 4) != 0) {
                while (true) {
                    start = stringBuilder.indexOf("<c#");
                    if (start == -1) {
                        break;
                    }
                    stringBuilder.replace(start, start + 2, "");
                    end = stringBuilder.indexOf(">", start);
                    int color = Color.parseColor(stringBuilder.substring(start, end));
                    stringBuilder.replace(start, end + 1, "");
                    end = stringBuilder.indexOf("</c>");
                    stringBuilder.replace(end, end + 4, "");
                    colors.add(Integer.valueOf(start));
                    colors.add(Integer.valueOf(end));
                    colors.add(Integer.valueOf(color));
                }
            }
            Spannable spannableStringBuilder = new SpannableStringBuilder(stringBuilder);
            for (a = 0; a < bolds.size() / 2; a++) {
                spannableStringBuilder.setSpan(new TypefaceSpan(getTypeface("fonts/rmedium.ttf")), ((Integer) bolds.get(a * 2)).intValue(), ((Integer) bolds.get((a * 2) + 1)).intValue(), 33);
            }
            for (a = 0; a < colors.size() / 3; a++) {
                spannableStringBuilder.setSpan(new ForegroundColorSpan(((Integer) colors.get((a * 3) + 2)).intValue()), ((Integer) colors.get(a * 3)).intValue(), ((Integer) colors.get((a * 3) + 1)).intValue(), 33);
            }
            return spannableStringBuilder;
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
            return new SpannableStringBuilder(str);
        }
    }

    public static boolean needShowPasscode(boolean reset) {
        boolean wasInBackground;
        if (VERSION.SDK_INT >= 14) {
            wasInBackground = ForegroundDetector.getInstance().isWasInBackground(reset);
            if (reset) {
                ForegroundDetector.getInstance().resetBackgroundVar();
            }
        } else {
            wasInBackground = UserConfig.lastPauseTime != 0;
        }
        if (UserConfig.passcodeHash.length() > 0 && wasInBackground) {
            if (UserConfig.appLocked) {
                return true;
            }
            if (!(UserConfig.autoLockIn == 0 || UserConfig.lastPauseTime == 0 || UserConfig.appLocked || UserConfig.lastPauseTime + UserConfig.autoLockIn > ConnectionsManager.getInstance().getCurrentTime())) {
                return true;
            }
        }
        return false;
    }

    public static void shakeView(final View view, final float x, final int num) {
        if (num == 6) {
            ViewProxy.setTranslationX(view, 0.0f);
            view.clearAnimation();
            return;
        }
        AnimatorSetProxy animatorSetProxy = new AnimatorSetProxy();
        Object[] objArr = new Object[1];
        objArr[0] = ObjectAnimatorProxy.ofFloat(view, "translationX", (float) dp(x));
        animatorSetProxy.playTogether(objArr);
        animatorSetProxy.setDuration(50);
        animatorSetProxy.addListener(new AnimatorListenerAdapterProxy() {
            public void onAnimationEnd(Object animation) {
                AndroidUtilities.shakeView(view, num == 5 ? 0.0f : -x, num + 1);
            }
        });
        animatorSetProxy.start();
    }

    public static void checkForCrashes(Activity context) {
        CrashManager.register(context, BuildVars.DEBUG_VERSION ? BuildVars.HOCKEY_APP_HASH_DEBUG : BuildVars.HOCKEY_APP_HASH, new C17394());
    }

    public static void checkForUpdates(Activity context) {
        if (BuildVars.DEBUG_VERSION) {
            UpdateManager.register(context, BuildVars.DEBUG_VERSION ? BuildVars.HOCKEY_APP_HASH_DEBUG : BuildVars.HOCKEY_APP_HASH);
        }
    }

    public static void unregisterUpdates() {
        if (BuildVars.DEBUG_VERSION) {
            UpdateManager.unregister();
        }
    }

    public static void addMediaToGallery(String fromPath) {
        if (fromPath != null) {
            addMediaToGallery(Uri.fromFile(new File(fromPath)));
        }
    }

    public static void addMediaToGallery(Uri uri) {
        if (uri != null) {
            try {
                Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
                mediaScanIntent.setData(uri);
                ApplicationLoader.applicationContext.sendBroadcast(mediaScanIntent);
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
        }
    }

    private static File getAlbumDir() {
        if (VERSION.SDK_INT >= 23 && ApplicationLoader.applicationContext.checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") != 0) {
            return FileLoader.getInstance().getDirectory(4);
        }
        if ("mounted".equals(Environment.getExternalStorageState())) {
            File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Telegram");
            if (storageDir.mkdirs() || storageDir.exists()) {
                return storageDir;
            }
            FileLog.m608d("tmessages", "failed to create directory");
            return null;
        }
        FileLog.m608d("tmessages", "External storage is not mounted READ/WRITE.");
        return null;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @android.annotation.SuppressLint({"NewApi"})
    public static java.lang.String getPath(android.net.Uri r14) {
        /*
        r9 = 0;
        r12 = 1;
        r10 = 0;
        r11 = android.os.Build.VERSION.SDK_INT;	 Catch:{ Exception -> 0x00f7 }
        r13 = 19;
        if (r11 < r13) goto L_0x004e;
    L_0x0009:
        r4 = r12;
    L_0x000a:
        if (r4 == 0) goto L_0x00cf;
    L_0x000c:
        r11 = org.telegram.messenger.ApplicationLoader.applicationContext;	 Catch:{ Exception -> 0x00f7 }
        r11 = android.provider.DocumentsContract.isDocumentUri(r11, r14);	 Catch:{ Exception -> 0x00f7 }
        if (r11 == 0) goto L_0x00cf;
    L_0x0014:
        r11 = isExternalStorageDocument(r14);	 Catch:{ Exception -> 0x00f7 }
        if (r11 == 0) goto L_0x0050;
    L_0x001a:
        r1 = android.provider.DocumentsContract.getDocumentId(r14);	 Catch:{ Exception -> 0x00f7 }
        r10 = ":";
        r7 = r1.split(r10);	 Catch:{ Exception -> 0x00f7 }
        r10 = 0;
        r8 = r7[r10];	 Catch:{ Exception -> 0x00f7 }
        r10 = "primary";
        r10 = r10.equalsIgnoreCase(r8);	 Catch:{ Exception -> 0x00f7 }
        if (r10 == 0) goto L_0x004d;
    L_0x002f:
        r10 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x00f7 }
        r10.<init>();	 Catch:{ Exception -> 0x00f7 }
        r11 = android.os.Environment.getExternalStorageDirectory();	 Catch:{ Exception -> 0x00f7 }
        r10 = r10.append(r11);	 Catch:{ Exception -> 0x00f7 }
        r11 = "/";
        r10 = r10.append(r11);	 Catch:{ Exception -> 0x00f7 }
        r11 = 1;
        r11 = r7[r11];	 Catch:{ Exception -> 0x00f7 }
        r10 = r10.append(r11);	 Catch:{ Exception -> 0x00f7 }
        r9 = r10.toString();	 Catch:{ Exception -> 0x00f7 }
    L_0x004d:
        return r9;
    L_0x004e:
        r4 = r10;
        goto L_0x000a;
    L_0x0050:
        r11 = isDownloadsDocument(r14);	 Catch:{ Exception -> 0x00f7 }
        if (r11 == 0) goto L_0x0075;
    L_0x0056:
        r3 = android.provider.DocumentsContract.getDocumentId(r14);	 Catch:{ Exception -> 0x00f7 }
        r10 = "content://downloads/public_downloads";
        r10 = android.net.Uri.parse(r10);	 Catch:{ Exception -> 0x00f7 }
        r11 = java.lang.Long.valueOf(r3);	 Catch:{ Exception -> 0x00f7 }
        r12 = r11.longValue();	 Catch:{ Exception -> 0x00f7 }
        r0 = android.content.ContentUris.withAppendedId(r10, r12);	 Catch:{ Exception -> 0x00f7 }
        r10 = org.telegram.messenger.ApplicationLoader.applicationContext;	 Catch:{ Exception -> 0x00f7 }
        r11 = 0;
        r12 = 0;
        r9 = getDataColumn(r10, r0, r11, r12);	 Catch:{ Exception -> 0x00f7 }
        goto L_0x004d;
    L_0x0075:
        r11 = isMediaDocument(r14);	 Catch:{ Exception -> 0x00f7 }
        if (r11 == 0) goto L_0x004d;
    L_0x007b:
        r1 = android.provider.DocumentsContract.getDocumentId(r14);	 Catch:{ Exception -> 0x00f7 }
        r11 = ":";
        r7 = r1.split(r11);	 Catch:{ Exception -> 0x00f7 }
        r11 = 0;
        r8 = r7[r11];	 Catch:{ Exception -> 0x00f7 }
        r0 = 0;
        r11 = -1;
        r13 = r8.hashCode();	 Catch:{ Exception -> 0x00f7 }
        switch(r13) {
            case 93166550: goto L_0x00bc;
            case 100313435: goto L_0x00a9;
            case 112202875: goto L_0x00b2;
            default: goto L_0x0091;
        };	 Catch:{ Exception -> 0x00f7 }
    L_0x0091:
        r10 = r11;
    L_0x0092:
        switch(r10) {
            case 0: goto L_0x00c6;
            case 1: goto L_0x00c9;
            case 2: goto L_0x00cc;
            default: goto L_0x0095;
        };	 Catch:{ Exception -> 0x00f7 }
    L_0x0095:
        r5 = "_id=?";
        r10 = 1;
        r6 = new java.lang.String[r10];	 Catch:{ Exception -> 0x00f7 }
        r10 = 0;
        r11 = 1;
        r11 = r7[r11];	 Catch:{ Exception -> 0x00f7 }
        r6[r10] = r11;	 Catch:{ Exception -> 0x00f7 }
        r10 = org.telegram.messenger.ApplicationLoader.applicationContext;	 Catch:{ Exception -> 0x00f7 }
        r11 = "_id=?";
        r9 = getDataColumn(r10, r0, r11, r6);	 Catch:{ Exception -> 0x00f7 }
        goto L_0x004d;
    L_0x00a9:
        r12 = "image";
        r12 = r8.equals(r12);	 Catch:{ Exception -> 0x00f7 }
        if (r12 == 0) goto L_0x0091;
    L_0x00b1:
        goto L_0x0092;
    L_0x00b2:
        r10 = "video";
        r10 = r8.equals(r10);	 Catch:{ Exception -> 0x00f7 }
        if (r10 == 0) goto L_0x0091;
    L_0x00ba:
        r10 = r12;
        goto L_0x0092;
    L_0x00bc:
        r10 = "audio";
        r10 = r8.equals(r10);	 Catch:{ Exception -> 0x00f7 }
        if (r10 == 0) goto L_0x0091;
    L_0x00c4:
        r10 = 2;
        goto L_0x0092;
    L_0x00c6:
        r0 = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;	 Catch:{ Exception -> 0x00f7 }
        goto L_0x0095;
    L_0x00c9:
        r0 = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI;	 Catch:{ Exception -> 0x00f7 }
        goto L_0x0095;
    L_0x00cc:
        r0 = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;	 Catch:{ Exception -> 0x00f7 }
        goto L_0x0095;
    L_0x00cf:
        r10 = "content";
        r11 = r14.getScheme();	 Catch:{ Exception -> 0x00f7 }
        r10 = r10.equalsIgnoreCase(r11);	 Catch:{ Exception -> 0x00f7 }
        if (r10 == 0) goto L_0x00e5;
    L_0x00db:
        r10 = org.telegram.messenger.ApplicationLoader.applicationContext;	 Catch:{ Exception -> 0x00f7 }
        r11 = 0;
        r12 = 0;
        r9 = getDataColumn(r10, r14, r11, r12);	 Catch:{ Exception -> 0x00f7 }
        goto L_0x004d;
    L_0x00e5:
        r10 = "file";
        r11 = r14.getScheme();	 Catch:{ Exception -> 0x00f7 }
        r10 = r10.equalsIgnoreCase(r11);	 Catch:{ Exception -> 0x00f7 }
        if (r10 == 0) goto L_0x004d;
    L_0x00f1:
        r9 = r14.getPath();	 Catch:{ Exception -> 0x00f7 }
        goto L_0x004d;
    L_0x00f7:
        r2 = move-exception;
        r10 = "tmessages";
        org.telegram.messenger.FileLog.m611e(r10, r2);
        goto L_0x004d;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.telegram.messenger.AndroidUtilities.getPath(android.net.Uri):java.lang.String");
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String column = "_data";
        try {
            cursor = context.getContentResolver().query(uri, new String[]{"_data"}, selection, selectionArgs, null);
            if (cursor == null || !cursor.moveToFirst()) {
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }
            String string = cursor.getString(cursor.getColumnIndexOrThrow("_data"));
            if (cursor == null) {
                return string;
            }
            cursor.close();
            return string;
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static File generatePicturePath() {
        try {
            return new File(getAlbumDir(), "IMG_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".jpg");
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
            return null;
        }
    }

    public static CharSequence generateSearchName(String name, String name2, String q) {
        if (name == null && name2 == null) {
            return "";
        }
        CharSequence builder = new SpannableStringBuilder();
        String wholeString = name;
        if (wholeString == null || wholeString.length() == 0) {
            wholeString = name2;
        } else if (!(name2 == null || name2.length() == 0)) {
            wholeString = wholeString + " " + name2;
        }
        wholeString = wholeString.trim();
        String lower = " " + wholeString.toLowerCase();
        int lastIndex = 0;
        while (true) {
            int index = lower.indexOf(" " + q, lastIndex);
            if (index == -1) {
                break;
            }
            int i;
            if (index == 0) {
                i = 0;
            } else {
                i = 1;
            }
            int idx = index - i;
            int length = q.length();
            if (index == 0) {
                i = 0;
            } else {
                i = 1;
            }
            int end = (i + length) + idx;
            if (lastIndex != 0 && lastIndex != idx + 1) {
                builder.append(wholeString.substring(lastIndex, idx));
            } else if (lastIndex == 0 && idx != 0) {
                builder.append(wholeString.substring(0, idx));
            }
            String query = wholeString.substring(idx, end);
            if (query.startsWith(" ")) {
                builder.append(" ");
            }
            builder.append(replaceTags("<c#ff4d83b3>" + query.trim() + "</c>"));
            lastIndex = end;
        }
        if (lastIndex == -1 || lastIndex == wholeString.length()) {
            return builder;
        }
        builder.append(wholeString.substring(lastIndex, wholeString.length()));
        return builder;
    }

    public static File generateVideoPath() {
        try {
            return new File(getAlbumDir(), "VID_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".mp4");
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
            return null;
        }
    }

    public static String formatFileSize(long size) {
        if (size < PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID) {
            return String.format("%d B", new Object[]{Long.valueOf(size)});
        } else if (size < 1048576) {
            return String.format("%.1f KB", new Object[]{Float.valueOf(((float) size) / 1024.0f)});
        } else if (size < 1073741824) {
            return String.format("%.1f MB", new Object[]{Float.valueOf((((float) size) / 1024.0f) / 1024.0f)});
        } else {
            return String.format("%.1f GB", new Object[]{Float.valueOf(((((float) size) / 1024.0f) / 1024.0f) / 1024.0f)});
        }
    }

    public static byte[] decodeQuotedPrintable(byte[] bytes) {
        byte[] bArr = null;
        if (bytes != null) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int i = 0;
            while (i < bytes.length) {
                int b = bytes[i];
                if (b == 61) {
                    i++;
                    try {
                        int u = Character.digit((char) bytes[i], 16);
                        i++;
                        buffer.write((char) ((u << 4) + Character.digit((char) bytes[i], 16)));
                    } catch (Throwable e) {
                        FileLog.m611e("tmessages", e);
                    }
                } else {
                    buffer.write(b);
                }
                i++;
            }
            bArr = buffer.toByteArray();
            try {
                buffer.close();
            } catch (Throwable e2) {
                FileLog.m611e("tmessages", e2);
            }
        }
        return bArr;
    }

    public static boolean copyFile(InputStream sourceFile, File destFile) throws IOException {
        OutputStream out = new FileOutputStream(destFile);
        byte[] buf = new byte[4096];
        while (true) {
            int len = sourceFile.read(buf);
            if (len > 0) {
                Thread.yield();
                out.write(buf, 0, len);
            } else {
                out.close();
                return true;
            }
        }
    }

    public static boolean copyFile(File sourceFile, File destFile) throws IOException {
        Throwable e;
        Throwable th;
        if (!destFile.exists()) {
            destFile.createNewFile();
        }
        FileInputStream source = null;
        FileOutputStream destination = null;
        try {
            FileInputStream source2 = new FileInputStream(sourceFile);
            try {
                FileOutputStream destination2 = new FileOutputStream(destFile);
                try {
                    destination2.getChannel().transferFrom(source2.getChannel(), 0, source2.getChannel().size());
                    if (source2 != null) {
                        source2.close();
                    }
                    if (destination2 != null) {
                        destination2.close();
                    }
                    destination = destination2;
                    source = source2;
                    return true;
                } catch (Exception e2) {
                    e = e2;
                    destination = destination2;
                    source = source2;
                    try {
                        FileLog.m611e("tmessages", e);
                        if (source != null) {
                            source.close();
                        }
                        if (destination != null) {
                            return false;
                        }
                        destination.close();
                        return false;
                    } catch (Throwable th2) {
                        th = th2;
                        if (source != null) {
                            source.close();
                        }
                        if (destination != null) {
                            destination.close();
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    destination = destination2;
                    source = source2;
                    if (source != null) {
                        source.close();
                    }
                    if (destination != null) {
                        destination.close();
                    }
                    throw th;
                }
            } catch (Exception e3) {
                e = e3;
                source = source2;
                FileLog.m611e("tmessages", e);
                if (source != null) {
                    source.close();
                }
                if (destination != null) {
                    return false;
                }
                destination.close();
                return false;
            } catch (Throwable th4) {
                th = th4;
                source = source2;
                if (source != null) {
                    source.close();
                }
                if (destination != null) {
                    destination.close();
                }
                throw th;
            }
        } catch (Exception e4) {
            e = e4;
            FileLog.m611e("tmessages", e);
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                return false;
            }
            destination.close();
            return false;
        }
    }
}
