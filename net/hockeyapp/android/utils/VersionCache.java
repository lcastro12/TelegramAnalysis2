package net.hockeyapp.android.utils;

import android.content.Context;
import android.content.SharedPreferences.Editor;

public class VersionCache {
    private static String VERSION_INFO_KEY = "versionInfo";

    public static void setVersionInfo(Context context, String json) {
        if (context != null) {
            Editor editor = context.getSharedPreferences("HockeyApp", 0).edit();
            editor.putString(VERSION_INFO_KEY, json);
            PrefsUtil.applyChanges(editor);
        }
    }

    public static String getVersionInfo(Context context) {
        if (context != null) {
            return context.getSharedPreferences("HockeyApp", 0).getString(VERSION_INFO_KEY, "[]");
        }
        return "[]";
    }
}
