package net.hockeyapp.android.tasks;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.plus.PlusShare;
import java.lang.ref.WeakReference;
import net.hockeyapp.android.Strings;
import net.hockeyapp.android.UpdateActivity;
import net.hockeyapp.android.UpdateFragment;
import net.hockeyapp.android.UpdateManagerListener;
import net.hockeyapp.android.utils.Util;
import net.hockeyapp.android.utils.VersionCache;
import org.json.JSONArray;

public class CheckUpdateTaskWithUI extends CheckUpdateTask {
    private Activity activity = null;
    private AlertDialog dialog = null;
    protected boolean isDialogRequired = false;

    class C02781 implements OnClickListener {
        C02781() {
        }

        public void onClick(DialogInterface dialog, int which) {
            CheckUpdateTaskWithUI.this.cleanUp();
            if (CheckUpdateTaskWithUI.this.listener != null) {
                CheckUpdateTaskWithUI.this.listener.onCancel();
            }
        }
    }

    public CheckUpdateTaskWithUI(WeakReference<Activity> weakActivity, String urlString, String appIdentifier, UpdateManagerListener listener, boolean isDialogRequired) {
        super(weakActivity, urlString, appIdentifier, listener);
        if (weakActivity != null) {
            this.activity = (Activity) weakActivity.get();
        }
        this.isDialogRequired = isDialogRequired;
    }

    public void detach() {
        super.detach();
        this.activity = null;
        if (this.dialog != null) {
            this.dialog.dismiss();
            this.dialog = null;
        }
    }

    protected void onPostExecute(JSONArray updateInfo) {
        super.onPostExecute(updateInfo);
        if (updateInfo != null && this.isDialogRequired) {
            showDialog(updateInfo);
        }
    }

    @TargetApi(11)
    private void showDialog(final JSONArray updateInfo) {
        if (getCachingEnabled()) {
            VersionCache.setVersionInfo(this.activity, updateInfo.toString());
        }
        if (this.activity != null && !this.activity.isFinishing()) {
            Builder builder = new Builder(this.activity);
            builder.setTitle(Strings.get(this.listener, 513));
            if (this.mandatory.booleanValue()) {
                Toast.makeText(this.activity, Strings.get(this.listener, 512), 1).show();
                startUpdateIntent(updateInfo, Boolean.valueOf(true));
                return;
            }
            builder.setMessage(Strings.get(this.listener, Strings.UPDATE_DIALOG_MESSAGE_ID));
            builder.setNegativeButton(Strings.get(this.listener, Strings.UPDATE_DIALOG_NEGATIVE_BUTTON_ID), new C02781());
            builder.setPositiveButton(Strings.get(this.listener, Strings.UPDATE_DIALOG_POSITIVE_BUTTON_ID), new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (CheckUpdateTaskWithUI.this.getCachingEnabled()) {
                        VersionCache.setVersionInfo(CheckUpdateTaskWithUI.this.activity, "[]");
                    }
                    WeakReference<Activity> weakActivity = new WeakReference(CheckUpdateTaskWithUI.this.activity);
                    if (Util.fragmentsSupported().booleanValue() && Util.runsOnTablet(weakActivity).booleanValue()) {
                        CheckUpdateTaskWithUI.this.showUpdateFragment(updateInfo);
                    } else {
                        CheckUpdateTaskWithUI.this.startUpdateIntent(updateInfo, Boolean.valueOf(false));
                    }
                }
            });
            this.dialog = builder.create();
            this.dialog.show();
        }
    }

    @TargetApi(11)
    private void showUpdateFragment(JSONArray updateInfo) {
        if (this.activity != null) {
            FragmentTransaction fragmentTransaction = this.activity.getFragmentManager().beginTransaction();
            fragmentTransaction.setTransition(4097);
            Fragment existingFragment = this.activity.getFragmentManager().findFragmentByTag("hockey_update_dialog");
            if (existingFragment != null) {
                fragmentTransaction.remove(existingFragment);
            }
            fragmentTransaction.addToBackStack(null);
            Class<? extends UpdateFragment> fragmentClass = UpdateFragment.class;
            if (this.listener != null) {
                fragmentClass = this.listener.getUpdateFragmentClass();
            }
            try {
                ((DialogFragment) fragmentClass.getMethod("newInstance", new Class[]{JSONArray.class, String.class}).invoke(null, new Object[]{updateInfo, getURLString("apk")})).show(fragmentTransaction, "hockey_update_dialog");
            } catch (Exception e) {
                Log.d("HockeyApp", "An exception happened while showing the update fragment:");
                e.printStackTrace();
                Log.d("HockeyApp", "Showing update activity instead.");
                startUpdateIntent(updateInfo, Boolean.valueOf(false));
            }
        }
    }

    @TargetApi(11)
    private void startUpdateIntent(JSONArray updateInfo, Boolean finish) {
        Class<?> activityClass = null;
        if (this.listener != null) {
            activityClass = this.listener.getUpdateActivityClass();
        }
        if (activityClass == null) {
            activityClass = UpdateActivity.class;
        }
        if (this.activity != null) {
            Intent intent = new Intent();
            intent.setClass(this.activity, activityClass);
            intent.putExtra("json", updateInfo.toString());
            intent.putExtra(PlusShare.KEY_CALL_TO_ACTION_URL, getURLString("apk"));
            this.activity.startActivity(intent);
            if (finish.booleanValue()) {
                this.activity.finish();
            }
        }
        cleanUp();
    }

    protected void cleanUp() {
        super.cleanUp();
        this.activity = null;
        this.dialog = null;
    }
}
