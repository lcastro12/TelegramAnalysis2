package net.hockeyapp.android.tasks;

import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Environment;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;
import net.hockeyapp.android.Strings;
import net.hockeyapp.android.listeners.DownloadFileListener;

public class DownloadFileTask extends AsyncTask<Void, Integer, Long> {
    protected static final int MAX_REDIRECTS = 6;
    protected Context context;
    private String downloadErrorMessage;
    protected String filePath = (Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download");
    protected String filename = (UUID.randomUUID() + ".apk");
    protected DownloadFileListener notifier;
    protected ProgressDialog progressDialog;
    protected String urlString;

    class C02801 implements OnClickListener {
        C02801() {
        }

        public void onClick(DialogInterface dialog, int which) {
            DownloadFileTask.this.notifier.downloadFailed(DownloadFileTask.this, Boolean.valueOf(false));
        }
    }

    class C02812 implements OnClickListener {
        C02812() {
        }

        public void onClick(DialogInterface dialog, int which) {
            DownloadFileTask.this.notifier.downloadFailed(DownloadFileTask.this, Boolean.valueOf(true));
        }
    }

    public DownloadFileTask(Context context, String urlString, DownloadFileListener notifier) {
        this.context = context;
        this.urlString = urlString;
        this.notifier = notifier;
        this.downloadErrorMessage = null;
    }

    public void attach(Context context) {
        this.context = context;
    }

    public void detach() {
        this.context = null;
        this.progressDialog = null;
    }

    protected Long doInBackground(Void... args) {
        try {
            URLConnection connection = createConnection(new URL(getURLString()), 6);
            connection.connect();
            int lengthOfFile = connection.getContentLength();
            String contentType = connection.getContentType();
            if (contentType == null || !contentType.contains("text")) {
                File dir = new File(this.filePath);
                if (dir.mkdirs() || dir.exists()) {
                    File file = new File(dir, this.filename);
                    InputStream input = new BufferedInputStream(connection.getInputStream());
                    OutputStream output = new FileOutputStream(file);
                    byte[] data = new byte[1024];
                    long total = 0;
                    while (true) {
                        int count = input.read(data);
                        if (count != -1) {
                            total += (long) count;
                            publishProgress(new Integer[]{Integer.valueOf(Math.round((((float) total) * 100.0f) / ((float) lengthOfFile)))});
                            output.write(data, 0, count);
                        } else {
                            output.flush();
                            output.close();
                            input.close();
                            return Long.valueOf(total);
                        }
                    }
                }
                throw new IOException("Could not create the dir(s):" + dir.getAbsolutePath());
            }
            this.downloadErrorMessage = "The requested download does not appear to be a file.";
            return Long.valueOf(0);
        } catch (Exception e) {
            e.printStackTrace();
            return Long.valueOf(0);
        }
    }

    protected void setConnectionProperties(HttpURLConnection connection) {
        connection.addRequestProperty("User-Agent", "HockeySDK/Android");
        connection.setInstanceFollowRedirects(true);
        if (VERSION.SDK_INT <= 9) {
            connection.setRequestProperty("connection", "close");
        }
    }

    protected URLConnection createConnection(URL url, int remainingRedirects) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        setConnectionProperties(connection);
        int code = connection.getResponseCode();
        if ((code != 301 && code != 302 && code != 303) || remainingRedirects == 0) {
            return connection;
        }
        URL movedUrl = new URL(connection.getHeaderField("Location"));
        if (url.getProtocol().equals(movedUrl.getProtocol())) {
            return connection;
        }
        connection.disconnect();
        return createConnection(movedUrl, remainingRedirects - 1);
    }

    protected void onProgressUpdate(Integer... args) {
        try {
            if (this.progressDialog == null) {
                this.progressDialog = new ProgressDialog(this.context);
                this.progressDialog.setProgressStyle(1);
                this.progressDialog.setMessage("Loading...");
                this.progressDialog.setCancelable(false);
                this.progressDialog.show();
            }
            this.progressDialog.setProgress(args[0].intValue());
        } catch (Exception e) {
        }
    }

    protected void onPostExecute(Long result) {
        if (this.progressDialog != null) {
            try {
                this.progressDialog.dismiss();
            } catch (Exception e) {
            }
        }
        if (result.longValue() > 0) {
            this.notifier.downloadSuccessful(this);
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.setDataAndType(Uri.fromFile(new File(this.filePath, this.filename)), "application/vnd.android.package-archive");
            intent.setFlags(268435456);
            this.context.startActivity(intent);
            return;
        }
        try {
            String message;
            Builder builder = new Builder(this.context);
            builder.setTitle(Strings.get(this.notifier, 256));
            if (this.downloadErrorMessage == null) {
                message = Strings.get(this.notifier, 257);
            } else {
                message = this.downloadErrorMessage;
            }
            builder.setMessage(message);
            builder.setNegativeButton(Strings.get(this.notifier, Strings.DOWNLOAD_FAILED_DIALOG_NEGATIVE_BUTTON_ID), new C02801());
            builder.setPositiveButton(Strings.get(this.notifier, Strings.DOWNLOAD_FAILED_DIALOG_POSITIVE_BUTTON_ID), new C02812());
            builder.create().show();
        } catch (Exception e2) {
        }
    }

    protected String getURLString() {
        return this.urlString + "&type=apk";
    }
}
