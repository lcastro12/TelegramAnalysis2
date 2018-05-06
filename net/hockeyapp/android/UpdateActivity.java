package net.hockeyapp.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.gms.plus.PlusShare;
import net.hockeyapp.android.listeners.DownloadFileListener;
import net.hockeyapp.android.objects.ErrorObject;
import net.hockeyapp.android.tasks.DownloadFileTask;
import net.hockeyapp.android.tasks.GetFileSizeTask;
import net.hockeyapp.android.utils.AsyncTaskUtils;
import net.hockeyapp.android.utils.Util;
import net.hockeyapp.android.utils.VersionHelper;
import net.hockeyapp.android.views.UpdateView;

public class UpdateActivity extends Activity implements UpdateActivityInterface, UpdateInfoListener, OnClickListener {
    private final int DIALOG_ERROR_ID = 0;
    private Context context;
    protected DownloadFileTask downloadTask;
    private ErrorObject error;
    protected VersionHelper versionHelper;

    class C02704 implements Runnable {
        C02704() {
        }

        public void run() {
            UpdateActivity.this.showDialog(0);
        }
    }

    class C02715 implements Runnable {
        C02715() {
        }

        public void run() {
            UpdateActivity.this.showDialog(0);
        }
    }

    class C02726 implements Runnable {
        C02726() {
        }

        public void run() {
            UpdateActivity.this.showDialog(0);
        }
    }

    class C02737 implements DialogInterface.OnClickListener {
        C02737() {
        }

        public void onClick(DialogInterface dialog, int id) {
            UpdateActivity.this.error = null;
            dialog.cancel();
        }
    }

    class C17363 extends DownloadFileListener {
        C17363() {
        }

        public void downloadSuccessful(DownloadFileTask task) {
            UpdateActivity.this.enableUpdateButton();
        }

        public void downloadFailed(DownloadFileTask task, Boolean userWantsRetry) {
            if (userWantsRetry.booleanValue()) {
                UpdateActivity.this.startDownloadTask();
            } else {
                UpdateActivity.this.enableUpdateButton();
            }
        }

        public String getStringForResource(int resourceID) {
            UpdateManagerListener listener = UpdateManager.getLastListener();
            if (listener != null) {
                return listener.getStringForResource(resourceID);
            }
            return null;
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("App Update");
        setContentView(getLayoutView());
        this.context = this;
        this.versionHelper = new VersionHelper(this, getIntent().getStringExtra("json"), this);
        configureView();
        this.downloadTask = (DownloadFileTask) getLastNonConfigurationInstance();
        if (this.downloadTask != null) {
            this.downloadTask.attach(this);
        }
    }

    protected void configureView() {
        ((TextView) findViewById(4098)).setText(getAppName());
        final TextView versionLabel = (TextView) findViewById(4099);
        String versionString = "Version " + this.versionHelper.getVersionString();
        final String fileDate = this.versionHelper.getFileDateString();
        String appSizeString = "Unknown size";
        if (this.versionHelper.getFileSizeBytes() >= 0) {
            appSizeString = String.format("%.2f", new Object[]{Float.valueOf(((float) appSize) / 1048576.0f)}) + " MB";
        } else {
            final String str = versionString;
            AsyncTaskUtils.execute(new GetFileSizeTask(this, getIntent().getStringExtra(PlusShare.KEY_CALL_TO_ACTION_URL), new DownloadFileListener() {
                public void downloadSuccessful(DownloadFileTask task) {
                    if (task instanceof GetFileSizeTask) {
                        long appSize = ((GetFileSizeTask) task).getSize();
                        versionLabel.setText(str + "\n" + fileDate + " - " + (String.format("%.2f", new Object[]{Float.valueOf(((float) appSize) / 1048576.0f)}) + " MB"));
                    }
                }
            }));
        }
        versionLabel.setText(versionString + "\n" + fileDate + " - " + appSizeString);
        ((Button) findViewById(UpdateView.UPDATE_BUTTON_ID)).setOnClickListener(this);
        WebView webView = (WebView) findViewById(UpdateView.WEB_VIEW_ID);
        webView.clearCache(true);
        webView.destroyDrawingCache();
        webView.loadDataWithBaseURL(Constants.BASE_URL, getReleaseNotes(), "text/html", "utf-8", null);
    }

    protected String getReleaseNotes() {
        return this.versionHelper.getReleaseNotes(false);
    }

    public Object onRetainNonConfigurationInstance() {
        if (this.downloadTask != null) {
            this.downloadTask.detach();
        }
        return this.downloadTask;
    }

    protected void startDownloadTask() {
        startDownloadTask(getIntent().getStringExtra(PlusShare.KEY_CALL_TO_ACTION_URL));
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        enableUpdateButton();
        if (permissions.length != 0 && grantResults.length != 0 && requestCode == 1) {
            if (grantResults[0] == 0) {
                prepareDownload();
                return;
            }
            Log.w("HockeyApp", "User denied write permission, can't continue with updater task.");
            UpdateManagerListener listener = UpdateManager.getLastListener();
            if (listener != null) {
                listener.onUpdatePermissionsNotGranted();
                return;
            }
            final UpdateActivity updateActivity = this;
            new Builder(this.context).setTitle(Strings.get(Strings.PERMISSION_UPDATE_TITLE_ID)).setMessage(Strings.get(Strings.PERMISSION_UPDATE_MESSAGE_ID)).setNegativeButton(Strings.get(Strings.PERMISSION_DIALOG_NEGATIVE_BUTTON_ID), null).setPositiveButton(Strings.get(Strings.PERMISSION_DIALOG_POSITIVE_BUTTON_ID), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    updateActivity.prepareDownload();
                }
            }).create().show();
        }
    }

    protected void startDownloadTask(String url) {
        createDownloadTask(url, new C17363());
        AsyncTaskUtils.execute(this.downloadTask);
    }

    protected void createDownloadTask(String url, DownloadFileListener listener) {
        this.downloadTask = new DownloadFileTask(this, url, listener);
    }

    public void enableUpdateButton() {
        findViewById(UpdateView.UPDATE_BUTTON_ID).setEnabled(true);
    }

    public int getCurrentVersionCode() {
        int currentVersionCode = -1;
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 128).versionCode;
        } catch (NameNotFoundException e) {
            return currentVersionCode;
        }
    }

    public ViewGroup getLayoutView() {
        return new UpdateView(this);
    }

    public String getAppName() {
        try {
            PackageManager pm = getPackageManager();
            return pm.getApplicationLabel(pm.getApplicationInfo(getPackageName(), 0)).toString();
        } catch (NameNotFoundException e) {
            return "";
        }
    }

    private boolean isWriteExternalStorageSet(Context context) {
        return context.checkCallingOrSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") == 0;
    }

    private boolean isUnknownSourcesChecked() {
        try {
            if (VERSION.SDK_INT < 17 || VERSION.SDK_INT >= 21) {
                if (Secure.getInt(getContentResolver(), "install_non_market_apps") != 1) {
                    return false;
                }
                return true;
            } else if (Global.getInt(getContentResolver(), "install_non_market_apps") == 1) {
                return true;
            } else {
                return false;
            }
        } catch (SettingNotFoundException e) {
            return true;
        }
    }

    public void onClick(View v) {
        prepareDownload();
        v.setEnabled(false);
    }

    protected void prepareDownload() {
        if (!Util.isConnectedToNetwork(this.context)) {
            this.error = new ErrorObject();
            this.error.setMessage(Strings.get(Strings.ERROR_NO_NETWORK_MESSAGE_ID));
            runOnUiThread(new C02704());
        } else if (isWriteExternalStorageSet(this.context)) {
            if (isUnknownSourcesChecked()) {
                startDownloadTask();
                return;
            }
            this.error = new ErrorObject();
            this.error.setMessage("The installation from unknown sources is not enabled. Please check the device settings.");
            runOnUiThread(new C02726());
        } else if (VERSION.SDK_INT >= 23) {
            requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 1);
        } else {
            this.error = new ErrorObject();
            this.error.setMessage("The permission to access the external storage permission is not set. Please contact the developer.");
            runOnUiThread(new C02715());
        }
    }

    protected Dialog onCreateDialog(int id) {
        return onCreateDialog(id, null);
    }

    protected Dialog onCreateDialog(int id, Bundle args) {
        switch (id) {
            case 0:
                return new Builder(this).setMessage("An error has occured").setCancelable(false).setTitle("Error").setIcon(17301543).setPositiveButton("OK", new C02737()).create();
            default:
                return null;
        }
    }

    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case 0:
                AlertDialog messageDialogError = (AlertDialog) dialog;
                if (this.error != null) {
                    messageDialogError.setMessage(this.error.getMessage());
                    return;
                } else {
                    messageDialogError.setMessage("An unknown error has occured.");
                    return;
                }
            default:
                return;
        }
    }
}
