package net.hockeyapp.android;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import net.hockeyapp.android.utils.PrefsUtil;

public class Tracking {
    private static final String START_TIME_KEY = "startTime";
    private static final String USAGE_TIME_KEY = "usageTime";

    public static void startUsage(Activity activity) {
        long now = System.currentTimeMillis();
        if (activity != null) {
            Editor editor = getPreferences(activity).edit();
            editor.putLong(START_TIME_KEY + activity.hashCode(), now);
            PrefsUtil.applyChanges(editor);
        }
    }

    public static void stopUsage(Activity activity) {
        long now = System.currentTimeMillis();
        if (activity != null && checkVersion(activity)) {
            SharedPreferences preferences = getPreferences(activity);
            long start = preferences.getLong(START_TIME_KEY + activity.hashCode(), 0);
            long sum = preferences.getLong(USAGE_TIME_KEY + Constants.APP_VERSION, 0);
            if (start > 0) {
                long duration = now - start;
                Editor editor = preferences.edit();
                editor.putLong(USAGE_TIME_KEY + Constants.APP_VERSION, sum + duration);
                PrefsUtil.applyChanges(editor);
            }
        }
    }

    public static long getUsageTime(Context context) {
        if (checkVersion(context)) {
            return getPreferences(context).getLong(USAGE_TIME_KEY + Constants.APP_VERSION, 0) / 1000;
        }
        return 0;
    }

    private static boolean checkVersion(Context context) {
        if (Constants.APP_VERSION == null) {
            Constants.loadFromContext(context);
            if (Constants.APP_VERSION == null) {
                return false;
            }
        }
        return true;
    }

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences("HockeyApp", 0);
    }
}
