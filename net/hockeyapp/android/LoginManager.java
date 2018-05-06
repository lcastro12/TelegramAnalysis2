package net.hockeyapp.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import com.google.android.gms.plus.PlusShare;
import java.util.HashMap;
import java.util.Map;
import net.hockeyapp.android.tasks.LoginTask;
import net.hockeyapp.android.utils.AsyncTaskUtils;
import net.hockeyapp.android.utils.PrefsUtil;
import net.hockeyapp.android.utils.Util;

public class LoginManager {
    static final String LOGIN_EXIT_KEY = "net.hockeyapp.android.EXIT";
    public static final int LOGIN_MODE_ANONYMOUS = 0;
    public static final int LOGIN_MODE_EMAIL_ONLY = 1;
    public static final int LOGIN_MODE_EMAIL_PASSWORD = 2;
    public static final int LOGIN_MODE_VALIDATE = 3;
    private static String identifier = null;
    static LoginManagerListener listener;
    static Class<?> mainActivity;
    private static int mode;
    private static String secret = null;
    private static String urlString = null;
    private static Handler validateHandler = null;

    public static void register(Context context, String appIdentifier, String appSecret, int mode, LoginManagerListener listener) {
        listener = listener;
        register(context, appIdentifier, appSecret, mode, (Class) null);
    }

    public static void register(Context context, String appIdentifier, String appSecret, int mode, Class<?> activity) {
        register(context, appIdentifier, appSecret, Constants.BASE_URL, mode, activity);
    }

    public static void register(final Context context, String appIdentifier, String appSecret, String urlString, int mode, Class<?> activity) {
        if (context != null) {
            identifier = Util.sanitizeAppIdentifier(appIdentifier);
            secret = appSecret;
            urlString = urlString;
            mode = mode;
            mainActivity = activity;
            if (validateHandler == null) {
                validateHandler = new Handler() {
                    public void handleMessage(Message msg) {
                        if (!msg.getData().getBoolean("success")) {
                            LoginManager.startLoginActivity(context);
                        }
                    }
                };
            }
            Constants.loadFromContext(context);
        }
    }

    public static void verifyLogin(Activity context, Intent intent) {
        if (intent != null && intent.getBooleanExtra(LOGIN_EXIT_KEY, false)) {
            context.finish();
        } else if (context != null && mode != 0 && mode != 3) {
            SharedPreferences prefs = context.getSharedPreferences("net.hockeyapp.android.login", 0);
            if (prefs.getInt("mode", -1) != mode) {
                PrefsUtil.applyChanges(prefs.edit().remove("auid").remove("iuid").putInt("mode", mode));
            }
            String auid = prefs.getString("auid", null);
            String iuid = prefs.getString("iuid", null);
            boolean notAuthenticated = auid == null && iuid == null;
            boolean auidMissing = auid == null && mode == 2;
            boolean iuidMissing = iuid == null && mode == 1;
            if (notAuthenticated || auidMissing || iuidMissing) {
                startLoginActivity(context);
                return;
            }
            Map<String, String> params = new HashMap();
            if (auid != null) {
                params.put("type", "auid");
                params.put("id", auid);
            } else if (iuid != null) {
                params.put("type", "iuid");
                params.put("id", iuid);
            }
            LoginTask verifyTask = new LoginTask(context, validateHandler, getURLString(3), 3, params);
            verifyTask.setShowProgressDialog(false);
            AsyncTaskUtils.execute(verifyTask);
        }
    }

    private static void startLoginActivity(Context context) {
        Intent intent = new Intent();
        intent.setFlags(1342177280);
        intent.setClass(context, LoginActivity.class);
        intent.putExtra(PlusShare.KEY_CALL_TO_ACTION_URL, getURLString(mode));
        intent.putExtra("mode", mode);
        intent.putExtra("secret", secret);
        context.startActivity(intent);
    }

    private static String getURLString(int mode) {
        String suffix = "";
        if (mode == 2) {
            suffix = "authorize";
        } else if (mode == 1) {
            suffix = "check";
        } else if (mode == 3) {
            suffix = "validate";
        }
        return urlString + "api/3/apps/" + identifier + "/identity/" + suffix;
    }
}
