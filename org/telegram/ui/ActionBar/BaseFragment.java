package org.telegram.ui.ActionBar;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import org.telegram.messenger.AnimationCompat.AnimatorSetProxy;
import org.telegram.messenger.C0553R;
import org.telegram.messenger.FileLog;
import org.telegram.tgnet.ConnectionsManager;

public class BaseFragment {
    protected ActionBar actionBar;
    protected Bundle arguments;
    protected int classGuid;
    protected View fragmentView;
    protected boolean hasOwnBackground;
    private boolean isFinished;
    protected ActionBarLayout parentLayout;
    protected boolean swipeBackEnabled;
    protected Dialog visibleDialog;

    class C07091 implements OnDismissListener {
        C07091() {
        }

        public void onDismiss(DialogInterface dialog) {
            BaseFragment.this.onDialogDismiss(BaseFragment.this.visibleDialog);
            BaseFragment.this.visibleDialog = null;
        }
    }

    public BaseFragment() {
        this.isFinished = false;
        this.visibleDialog = null;
        this.classGuid = 0;
        this.swipeBackEnabled = true;
        this.hasOwnBackground = false;
        this.classGuid = ConnectionsManager.getInstance().generateClassGuid();
    }

    public BaseFragment(Bundle args) {
        this.isFinished = false;
        this.visibleDialog = null;
        this.classGuid = 0;
        this.swipeBackEnabled = true;
        this.hasOwnBackground = false;
        this.arguments = args;
        this.classGuid = ConnectionsManager.getInstance().generateClassGuid();
    }

    public ActionBar getActionBar() {
        return this.actionBar;
    }

    public View getFragmentView() {
        return this.fragmentView;
    }

    public View createView(Context context) {
        return null;
    }

    public Bundle getArguments() {
        return this.arguments;
    }

    protected void clearViews() {
        ViewGroup parent;
        if (this.fragmentView != null) {
            parent = (ViewGroup) this.fragmentView.getParent();
            if (parent != null) {
                try {
                    parent.removeView(this.fragmentView);
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
            this.fragmentView = null;
        }
        if (this.actionBar != null) {
            parent = (ViewGroup) this.actionBar.getParent();
            if (parent != null) {
                try {
                    parent.removeView(this.actionBar);
                } catch (Throwable e2) {
                    FileLog.m611e("tmessages", e2);
                }
            }
            this.actionBar = null;
        }
        this.parentLayout = null;
    }

    protected void setParentLayout(ActionBarLayout layout) {
        if (this.parentLayout != layout) {
            ViewGroup parent;
            this.parentLayout = layout;
            if (this.fragmentView != null) {
                parent = (ViewGroup) this.fragmentView.getParent();
                if (parent != null) {
                    try {
                        parent.removeView(this.fragmentView);
                    } catch (Throwable e) {
                        FileLog.m611e("tmessages", e);
                    }
                }
                if (!(this.parentLayout == null || this.parentLayout.getContext() == this.fragmentView.getContext())) {
                    this.fragmentView = null;
                }
            }
            if (this.actionBar != null) {
                parent = (ViewGroup) this.actionBar.getParent();
                if (parent != null) {
                    try {
                        parent.removeView(this.actionBar);
                    } catch (Throwable e2) {
                        FileLog.m611e("tmessages", e2);
                    }
                }
                if (!(this.parentLayout == null || this.parentLayout.getContext() == this.actionBar.getContext())) {
                    this.actionBar = null;
                }
            }
            if (this.parentLayout != null && this.actionBar == null) {
                this.actionBar = new ActionBar(this.parentLayout.getContext());
                this.actionBar.parentFragment = this;
                this.actionBar.setBackgroundColor(-11242082);
                this.actionBar.setItemsBackground(C0553R.drawable.bar_selector);
            }
        }
    }

    public void finishFragment() {
        finishFragment(true);
    }

    public void finishFragment(boolean animated) {
        if (!this.isFinished && this.parentLayout != null) {
            this.parentLayout.closeLastFragment(animated);
        }
    }

    public void removeSelfFromStack() {
        if (!this.isFinished && this.parentLayout != null) {
            this.parentLayout.removeFragmentFromStack(this);
        }
    }

    public boolean onFragmentCreate() {
        return true;
    }

    public void onFragmentDestroy() {
        ConnectionsManager.getInstance().cancelRequestsForGuid(this.classGuid);
        this.isFinished = true;
        if (this.actionBar != null) {
            this.actionBar.setEnabled(false);
        }
    }

    public void onResume() {
    }

    public void onPause() {
        if (this.actionBar != null) {
            this.actionBar.onPause();
        }
        try {
            if (this.visibleDialog != null && this.visibleDialog.isShowing() && dismissDialogOnPause(this.visibleDialog)) {
                this.visibleDialog.dismiss();
                this.visibleDialog = null;
            }
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
    }

    public boolean onBackPressed() {
        return true;
    }

    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
    }

    public void onRequestPermissionsResultFragment(int requestCode, String[] permissions, int[] grantResults) {
    }

    public void saveSelfArgs(Bundle args) {
    }

    public void restoreSelfArgs(Bundle args) {
    }

    public boolean presentFragment(BaseFragment fragment) {
        return this.parentLayout != null && this.parentLayout.presentFragment(fragment);
    }

    public boolean presentFragment(BaseFragment fragment, boolean removeLast) {
        return this.parentLayout != null && this.parentLayout.presentFragment(fragment, removeLast);
    }

    public boolean presentFragment(BaseFragment fragment, boolean removeLast, boolean forceWithoutAnimation) {
        return this.parentLayout != null && this.parentLayout.presentFragment(fragment, removeLast, forceWithoutAnimation, true);
    }

    public Activity getParentActivity() {
        if (this.parentLayout != null) {
            return this.parentLayout.parentActivity;
        }
        return null;
    }

    public void startActivityForResult(Intent intent, int requestCode) {
        if (this.parentLayout != null) {
            this.parentLayout.startActivityForResult(intent, requestCode);
        }
    }

    public boolean dismissDialogOnPause(Dialog dialog) {
        return true;
    }

    public void onBeginSlide() {
        try {
            if (this.visibleDialog != null && this.visibleDialog.isShowing()) {
                this.visibleDialog.dismiss();
                this.visibleDialog = null;
            }
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
        }
        if (this.actionBar != null) {
            this.actionBar.onPause();
        }
    }

    protected void onTransitionAnimationStart(boolean isOpen, boolean backward) {
    }

    protected void onTransitionAnimationEnd(boolean isOpen, boolean backward) {
    }

    protected void onBecomeFullyVisible() {
    }

    protected AnimatorSetProxy onCustomTransitionAnimation(boolean isOpen, Runnable callback) {
        return null;
    }

    public void onLowMemory() {
    }

    public Dialog showDialog(Dialog dialog) {
        return showDialog(dialog, false);
    }

    public Dialog showDialog(Dialog dialog, boolean allowInTransition) {
        Dialog dialog2 = null;
        if (!(dialog == null || this.parentLayout == null || this.parentLayout.animationInProgress || this.parentLayout.startedTracking || (!allowInTransition && this.parentLayout.checkTransitionAnimation()))) {
            try {
                if (this.visibleDialog != null) {
                    this.visibleDialog.dismiss();
                    this.visibleDialog = null;
                }
            } catch (Throwable e) {
                FileLog.m611e("tmessages", e);
            }
            try {
                this.visibleDialog = dialog;
                this.visibleDialog.setCanceledOnTouchOutside(true);
                this.visibleDialog.setOnDismissListener(new C07091());
                this.visibleDialog.show();
                dialog2 = this.visibleDialog;
            } catch (Throwable e2) {
                FileLog.m611e("tmessages", e2);
            }
        }
        return dialog2;
    }

    protected void onDialogDismiss(Dialog dialog) {
    }

    public Dialog getVisibleDialog() {
        return this.visibleDialog;
    }

    public void setVisibleDialog(Dialog dialog) {
        this.visibleDialog = dialog;
    }
}
