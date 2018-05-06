package org.telegram.messenger;

import android.app.Activity;
import android.net.Uri;
import android.util.Log;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.UUID;
import net.hockeyapp.android.Constants;
import net.hockeyapp.android.utils.SimpleMultipartEntity;

public class NativeCrashManager {

    static class C05332 implements FilenameFilter {
        C05332() {
        }

        public boolean accept(File dir, String name) {
            return name.endsWith(".dmp");
        }
    }

    public static void handleDumpFiles(Activity activity) {
        for (String dumpFilename : searchForDumpFiles()) {
            String logFilename = createLogFile();
            if (logFilename != null) {
                uploadDumpAndLog(activity, BuildVars.DEBUG_VERSION ? BuildVars.HOCKEY_APP_HASH_DEBUG : BuildVars.HOCKEY_APP_HASH, dumpFilename, logFilename);
            }
        }
    }

    public static String createLogFile() {
        Date now = new Date();
        try {
            String filename = UUID.randomUUID().toString();
            String path = Constants.FILES_PATH + "/" + filename + ".faketrace";
            Log.d("HockeyApp", "Writing unhandled exception to: " + path);
            BufferedWriter write = new BufferedWriter(new FileWriter(path));
            write.write("Package: " + Constants.APP_PACKAGE + "\n");
            write.write("Version Code: " + Constants.APP_VERSION + "\n");
            write.write("Version Name: " + Constants.APP_VERSION_NAME + "\n");
            write.write("Android: " + Constants.ANDROID_VERSION + "\n");
            write.write("Manufacturer: " + Constants.PHONE_MANUFACTURER + "\n");
            write.write("Model: " + Constants.PHONE_MODEL + "\n");
            write.write("Date: " + now + "\n");
            write.write("\n");
            write.write("MinidumpContainer");
            write.flush();
            write.close();
            return filename + ".faketrace";
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
            return null;
        }
    }

    public static void uploadDumpAndLog(final Activity activity, final String identifier, final String dumpFilename, final String logFilename) {
        new Thread() {
            public void run() {
                try {
                    SimpleMultipartEntity entity = new SimpleMultipartEntity();
                    entity.writeFirstBoundaryIfNeeds();
                    Uri attachmentUri = Uri.fromFile(new File(Constants.FILES_PATH, dumpFilename));
                    entity.addPart("attachment0", attachmentUri.getLastPathSegment(), activity.getContentResolver().openInputStream(attachmentUri), false);
                    attachmentUri = Uri.fromFile(new File(Constants.FILES_PATH, logFilename));
                    entity.addPart("log", attachmentUri.getLastPathSegment(), activity.getContentResolver().openInputStream(attachmentUri), false);
                    entity.writeLastBoundaryIfNeeds();
                    HttpURLConnection urlConnection = (HttpURLConnection) new URL("https://rink.hockeyapp.net/api/2/apps/" + identifier + "/crashes/upload").openConnection();
                    urlConnection.setDoOutput(true);
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setRequestProperty("Content-Type", entity.getContentType());
                    urlConnection.setRequestProperty("Content-Length", String.valueOf(entity.getContentLength()));
                    urlConnection.getOutputStream().write(entity.getOutputStream().toByteArray());
                    urlConnection.connect();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    activity.deleteFile(logFilename);
                    activity.deleteFile(dumpFilename);
                }
            }
        }.start();
    }

    private static String[] searchForDumpFiles() {
        if (Constants.FILES_PATH != null) {
            File dir = new File(Constants.FILES_PATH + "/");
            if (dir.mkdir() || dir.exists()) {
                return dir.list(new C05332());
            }
            return new String[0];
        }
        FileLog.m608d("HockeyApp", "Can't search for exception as file path is null.");
        return new String[0];
    }
}
