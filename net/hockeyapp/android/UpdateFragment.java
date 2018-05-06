package net.hockeyapp.android;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.gms.plus.PlusShare;
import net.hockeyapp.android.listeners.DownloadFileListener;
import net.hockeyapp.android.tasks.DownloadFileTask;
import net.hockeyapp.android.tasks.GetFileSizeTask;
import net.hockeyapp.android.utils.AsyncTaskUtils;
import net.hockeyapp.android.utils.VersionHelper;
import net.hockeyapp.android.views.UpdateView;
import org.json.JSONArray;
import org.json.JSONException;

public class UpdateFragment extends DialogFragment implements OnClickListener, UpdateInfoListener {
    private DownloadFileTask downloadTask;
    private String urlString;
    private VersionHelper versionHelper;
    private JSONArray versionInfo;

    public static UpdateFragment newInstance(JSONArray versionInfo, String urlString) {
        Bundle state = new Bundle();
        state.putString(PlusShare.KEY_CALL_TO_ACTION_URL, urlString);
        state.putString("versionInfo", versionInfo.toString());
        UpdateFragment fragment = new UpdateFragment();
        fragment.setArguments(state);
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            this.urlString = getArguments().getString(PlusShare.KEY_CALL_TO_ACTION_URL);
            this.versionInfo = new JSONArray(getArguments().getString("versionInfo"));
            setStyle(1, 16973939);
        } catch (JSONException e) {
            dismiss();
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = getLayoutView();
        this.versionHelper = new VersionHelper(getActivity(), this.versionInfo.toString(), this);
        ((TextView) view.findViewById(4098)).setText(getAppName());
        final TextView versionLabel = (TextView) view.findViewById(4099);
        String versionString = "Version " + this.versionHelper.getVersionString();
        final String fileDate = this.versionHelper.getFileDateString();
        String appSizeString = "Unknown size";
        if (this.versionHelper.getFileSizeBytes() >= 0) {
            appSizeString = String.format("%.2f", new Object[]{Float.valueOf(((float) appSize) / 1048576.0f)}) + " MB";
        } else {
            final String str = versionString;
            AsyncTaskUtils.execute(new GetFileSizeTask(getActivity(), this.urlString, new DownloadFileListener() {
                public void downloadSuccessful(DownloadFileTask task) {
                    if (task instanceof GetFileSizeTask) {
                        long appSize = ((GetFileSizeTask) task).getSize();
                        versionLabel.setText(str + "\n" + fileDate + " - " + (String.format("%.2f", new Object[]{Float.valueOf(((float) appSize) / 1048576.0f)}) + " MB"));
                    }
                }
            }));
        }
        versionLabel.setText(versionString + "\n" + fileDate + " - " + appSizeString);
        ((Button) view.findViewById(UpdateView.UPDATE_BUTTON_ID)).setOnClickListener(this);
        WebView webView = (WebView) view.findViewById(UpdateView.WEB_VIEW_ID);
        webView.clearCache(true);
        webView.destroyDrawingCache();
        webView.loadDataWithBaseURL(Constants.BASE_URL, this.versionHelper.getReleaseNotes(false), "text/html", "utf-8", null);
        return view;
    }

    public void onClick(View view) {
        prepareDownload();
    }

    public void prepareDownload() {
        if (VERSION.SDK_INT < 23 || getActivity().checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") == 0) {
            startDownloadTask(getActivity());
            dismiss();
            return;
        }
        requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 1);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (permissions.length != 0 && grantResults.length != 0 && requestCode == 1) {
            if (grantResults[0] == 0) {
                startDownloadTask(getActivity());
                return;
            }
            Log.w("HockeyApp", "User denied write permission, can't continue with updater task.");
            UpdateManagerListener listener = UpdateManager.getLastListener();
            if (listener != null) {
                listener.onUpdatePermissionsNotGranted();
                return;
            }
            final UpdateFragment updateFragment = this;
            new Builder(getActivity()).setTitle(Strings.get(Strings.PERMISSION_UPDATE_TITLE_ID)).setMessage(Strings.get(Strings.PERMISSION_UPDATE_MESSAGE_ID)).setNegativeButton(Strings.get(Strings.PERMISSION_DIALOG_NEGATIVE_BUTTON_ID), null).setPositiveButton(Strings.get(Strings.PERMISSION_DIALOG_POSITIVE_BUTTON_ID), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    updateFragment.prepareDownload();
                }
            }).create().show();
        }
    }

    private void startDownloadTask(final Activity activity) {
        this.downloadTask = new DownloadFileTask(activity, this.urlString, new DownloadFileListener() {
            public void downloadFailed(DownloadFileTask task, Boolean userWantsRetry) {
                if (userWantsRetry.booleanValue()) {
                    UpdateFragment.this.startDownloadTask(activity);
                }
            }

            public void downloadSuccessful(DownloadFileTask task) {
            }

            public String getStringForResource(int resourceID) {
                UpdateManagerListener listener = UpdateManager.getLastListener();
                if (listener != null) {
                    return listener.getStringForResource(resourceID);
                }
                return null;
            }
        });
        AsyncTaskUtils.execute(this.downloadTask);
    }

    public int getCurrentVersionCode() {
        int currentVersionCode = -1;
        try {
            return getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 128).versionCode;
        } catch (NameNotFoundException e) {
            return currentVersionCode;
        } catch (NullPointerException e2) {
            return currentVersionCode;
        }
    }

    public String getAppName() {
        Activity activity = getActivity();
        try {
            PackageManager pm = activity.getPackageManager();
            return pm.getApplicationLabel(pm.getApplicationInfo(activity.getPackageName(), 0)).toString();
        } catch (NameNotFoundException e) {
            return "";
        }
    }

    public View getLayoutView() {
        return new UpdateView(getActivity(), false, true);
    }
}
