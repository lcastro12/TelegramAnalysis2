package net.hockeyapp.android.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.provider.Settings.Secure;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Locale;
import net.hockeyapp.android.Constants;
import net.hockeyapp.android.Tracking;
import net.hockeyapp.android.UpdateManagerListener;
import net.hockeyapp.android.utils.HttpURLConnectionBuilder;
import net.hockeyapp.android.utils.VersionCache;
import net.hockeyapp.android.utils.VersionHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CheckUpdateTask extends AsyncTask<Void, String, JSONArray> {
    protected static final String APK = "apk";
    protected static final String INTENT_EXTRA_JSON = "json";
    protected static final String INTENT_EXTRA_URL = "url";
    private static final int MAX_NUMBER_OF_VERSIONS = 25;
    protected String appIdentifier;
    private Context context;
    protected UpdateManagerListener listener;
    protected Boolean mandatory;
    protected String urlString;
    private long usageTime;

    public CheckUpdateTask(WeakReference<? extends Context> weakContext, String urlString) {
        this(weakContext, urlString, null);
    }

    public CheckUpdateTask(WeakReference<? extends Context> weakContext, String urlString, String appIdentifier) {
        this(weakContext, urlString, appIdentifier, null);
    }

    public CheckUpdateTask(WeakReference<? extends Context> weakContext, String urlString, String appIdentifier, UpdateManagerListener listener) {
        this.urlString = null;
        this.appIdentifier = null;
        this.context = null;
        this.mandatory = Boolean.valueOf(false);
        this.usageTime = 0;
        this.appIdentifier = appIdentifier;
        this.urlString = urlString;
        this.listener = listener;
        Context ctx = null;
        if (weakContext != null) {
            ctx = (Context) weakContext.get();
        }
        if (ctx != null) {
            this.context = ctx.getApplicationContext();
            this.usageTime = Tracking.getUsageTime(ctx);
            Constants.loadFromContext(ctx);
        }
    }

    public void attach(WeakReference<? extends Context> weakContext) {
        Context ctx = null;
        if (weakContext != null) {
            ctx = (Context) weakContext.get();
        }
        if (ctx != null) {
            this.context = ctx.getApplicationContext();
            Constants.loadFromContext(ctx);
        }
    }

    public void detach() {
        this.context = null;
    }

    protected int getVersionCode() {
        return Integer.parseInt(Constants.APP_VERSION);
    }

    protected JSONArray doInBackground(Void... args) {
        try {
            int versionCode = getVersionCode();
            JSONArray json = new JSONArray(VersionCache.getVersionInfo(this.context));
            if (getCachingEnabled() && findNewVersion(json, versionCode)) {
                return json;
            }
            URLConnection connection = createConnection(new URL(getURLString(INTENT_EXTRA_JSON)));
            connection.connect();
            InputStream inputStream = new BufferedInputStream(connection.getInputStream());
            String jsonString = convertStreamToString(inputStream);
            inputStream.close();
            json = new JSONArray(jsonString);
            if (findNewVersion(json, versionCode)) {
                return limitResponseSize(json);
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected URLConnection createConnection(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        connection.addRequestProperty("User-Agent", "HockeySDK/Android");
        if (VERSION.SDK_INT <= 9) {
            connection.setRequestProperty("connection", "close");
        }
        return connection;
    }

    private boolean findNewVersion(JSONArray json, int versionCode) {
        boolean newerVersionFound = false;
        int index = 0;
        while (index < json.length()) {
            try {
                boolean largerVersionCode;
                JSONObject entry = json.getJSONObject(index);
                if (entry.getInt("version") > versionCode) {
                    largerVersionCode = true;
                } else {
                    largerVersionCode = false;
                }
                boolean newerApkFile;
                if (entry.getInt("version") == versionCode && VersionHelper.isNewerThanLastUpdateTime(this.context, entry.getLong("timestamp"))) {
                    newerApkFile = true;
                } else {
                    newerApkFile = false;
                }
                boolean minRequirementsMet;
                if (VersionHelper.compareVersionStrings(entry.getString("minimum_os_version"), VersionHelper.mapGoogleVersion(VERSION.RELEASE)) <= 0) {
                    minRequirementsMet = true;
                } else {
                    minRequirementsMet = false;
                }
                if ((largerVersionCode || newerApkFile) && minRequirementsMet) {
                    if (entry.has("mandatory")) {
                        this.mandatory = Boolean.valueOf(this.mandatory.booleanValue() | entry.getBoolean("mandatory"));
                    }
                    newerVersionFound = true;
                }
                index++;
            } catch (JSONException e) {
                return false;
            }
        }
        return newerVersionFound;
    }

    private JSONArray limitResponseSize(JSONArray json) {
        JSONArray result = new JSONArray();
        for (int index = 0; index < Math.min(json.length(), 25); index++) {
            try {
                result.put(json.get(index));
            } catch (JSONException e) {
            }
        }
        return result;
    }

    protected void onPostExecute(JSONArray updateInfo) {
        if (updateInfo != null) {
            if (this.listener != null) {
                this.listener.onUpdateAvailable(updateInfo, getURLString(APK));
            }
        } else if (this.listener != null) {
            this.listener.onNoUpdateAvailable();
        }
    }

    protected void cleanUp() {
        this.urlString = null;
        this.appIdentifier = null;
    }

    protected String getURLString(String format) {
        StringBuilder builder = new StringBuilder();
        builder.append(this.urlString);
        builder.append("api/2/apps/");
        builder.append(this.appIdentifier != null ? this.appIdentifier : this.context.getPackageName());
        builder.append("?format=" + format);
        if (Secure.getString(this.context.getContentResolver(), "android_id") != null) {
            builder.append("&udid=" + encodeParam(Secure.getString(this.context.getContentResolver(), "android_id")));
        }
        builder.append("&os=Android");
        builder.append("&os_version=" + encodeParam(Constants.ANDROID_VERSION));
        builder.append("&device=" + encodeParam(Constants.PHONE_MODEL));
        builder.append("&oem=" + encodeParam(Constants.PHONE_MANUFACTURER));
        builder.append("&app_version=" + encodeParam(Constants.APP_VERSION));
        builder.append("&sdk=" + encodeParam(Constants.SDK_NAME));
        builder.append("&sdk_version=" + encodeParam(Constants.SDK_VERSION));
        builder.append("&lang=" + encodeParam(Locale.getDefault().getLanguage()));
        builder.append("&usage_time=" + this.usageTime);
        return builder.toString();
    }

    private String encodeParam(String param) {
        try {
            return URLEncoder.encode(param, HttpURLConnectionBuilder.DEFAULT_CHARSET);
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    protected boolean getCachingEnabled() {
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static java.lang.String convertStreamToString(java.io.InputStream r6) {
        /*
        r2 = new java.io.BufferedReader;
        r4 = new java.io.InputStreamReader;
        r4.<init>(r6);
        r5 = 1024; // 0x400 float:1.435E-42 double:5.06E-321;
        r2.<init>(r4, r5);
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r1 = 0;
    L_0x0012:
        r1 = r2.readLine();	 Catch:{ IOException -> 0x002f }
        if (r1 == 0) goto L_0x003b;
    L_0x0018:
        r4 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x002f }
        r4.<init>();	 Catch:{ IOException -> 0x002f }
        r4 = r4.append(r1);	 Catch:{ IOException -> 0x002f }
        r5 = "\n";
        r4 = r4.append(r5);	 Catch:{ IOException -> 0x002f }
        r4 = r4.toString();	 Catch:{ IOException -> 0x002f }
        r3.append(r4);	 Catch:{ IOException -> 0x002f }
        goto L_0x0012;
    L_0x002f:
        r0 = move-exception;
        r0.printStackTrace();	 Catch:{ all -> 0x0049 }
        r6.close();	 Catch:{ IOException -> 0x0044 }
    L_0x0036:
        r4 = r3.toString();
        return r4;
    L_0x003b:
        r6.close();	 Catch:{ IOException -> 0x003f }
        goto L_0x0036;
    L_0x003f:
        r0 = move-exception;
        r0.printStackTrace();
        goto L_0x0036;
    L_0x0044:
        r0 = move-exception;
        r0.printStackTrace();
        goto L_0x0036;
    L_0x0049:
        r4 = move-exception;
        r6.close();	 Catch:{ IOException -> 0x004e }
    L_0x004d:
        throw r4;
    L_0x004e:
        r0 = move-exception;
        r0.printStackTrace();
        goto L_0x004d;
        */
        throw new UnsupportedOperationException("Method not decompiled: net.hockeyapp.android.tasks.CheckUpdateTask.convertStreamToString(java.io.InputStream):java.lang.String");
    }
}
