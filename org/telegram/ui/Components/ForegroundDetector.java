package org.telegram.ui.Components;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.os.Build.VERSION;
import android.os.Bundle;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import org.telegram.messenger.FileLog;

@SuppressLint({"NewApi"})
public class ForegroundDetector implements ActivityLifecycleCallbacks {
    private static ForegroundDetector Instance = null;
    private long enterBackgroundTime = 0;
    private CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList();
    private int refs;
    private boolean wasInBackground = true;

    public interface Listener {
        void onBecameBackground();

        void onBecameForeground();
    }

    public static ForegroundDetector getInstance() {
        return Instance;
    }

    public ForegroundDetector(Application application) {
        Instance = this;
        application.registerActivityLifecycleCallbacks(this);
    }

    public boolean isForeground() {
        return this.refs > 0;
    }

    public boolean isBackground() {
        return this.refs == 0;
    }

    public void addListener(Listener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        this.listeners.remove(listener);
    }

    public void onActivityStarted(Activity activity) {
        int i = this.refs + 1;
        this.refs = i;
        if (i == 1) {
            if (System.currentTimeMillis() - this.enterBackgroundTime < 200) {
                this.wasInBackground = false;
            }
            FileLog.m609e("tmessages", "switch to foreground");
            Iterator i$ = this.listeners.iterator();
            while (i$.hasNext()) {
                try {
                    ((Listener) i$.next()).onBecameForeground();
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
        }
    }

    public boolean isWasInBackground(boolean reset) {
        if (reset && VERSION.SDK_INT >= 21 && System.currentTimeMillis() - this.enterBackgroundTime < 200) {
            this.wasInBackground = false;
        }
        return this.wasInBackground;
    }

    public void resetBackgroundVar() {
        this.wasInBackground = false;
    }

    public void onActivityStopped(Activity activity) {
        int i = this.refs - 1;
        this.refs = i;
        if (i == 0) {
            this.enterBackgroundTime = System.currentTimeMillis();
            this.wasInBackground = true;
            FileLog.m609e("tmessages", "switch to background");
            Iterator i$ = this.listeners.iterator();
            while (i$.hasNext()) {
                try {
                    ((Listener) i$.next()).onBecameBackground();
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
            }
        }
    }

    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    public void onActivityResumed(Activity activity) {
    }

    public void onActivityPaused(Activity activity) {
    }

    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    public void onActivityDestroyed(Activity activity) {
    }
}
