package com.google.android.gms.internal;

import android.app.Activity;
import android.content.Context;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Display;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import java.lang.ref.WeakReference;

public class ba {
    protected au dt;
    protected C0163a ej;

    public static final class C0163a {
        public int bottom;
        public IBinder ek;
        public int el;
        public int gravity;
        public int left;
        public int right;
        public int top;

        private C0163a(int i, IBinder iBinder) {
            this.el = -1;
            this.left = 0;
            this.top = 0;
            this.right = 0;
            this.bottom = 0;
            this.gravity = i;
            this.ek = iBinder;
        }

        public Bundle aE() {
            Bundle bundle = new Bundle();
            bundle.putInt("popupLocationInfo.gravity", this.gravity);
            bundle.putInt("popupLocationInfo.displayId", this.el);
            bundle.putInt("popupLocationInfo.left", this.left);
            bundle.putInt("popupLocationInfo.top", this.top);
            bundle.putInt("popupLocationInfo.right", this.right);
            bundle.putInt("popupLocationInfo.bottom", this.bottom);
            return bundle;
        }
    }

    private static final class C1322b extends ba implements OnAttachStateChangeListener, OnGlobalLayoutListener {
        private boolean dE = false;
        private WeakReference<View> em;

        protected C1322b(au auVar, int i) {
            super(auVar, i);
        }

        private void m848b(View view) {
            int i = -1;
            if (as.as()) {
                Display display = view.getDisplay();
                if (display != null) {
                    i = display.getDisplayId();
                }
            }
            IBinder windowToken = view.getWindowToken();
            int[] iArr = new int[2];
            view.getLocationInWindow(iArr);
            int width = view.getWidth();
            int height = view.getHeight();
            this.ej.el = i;
            this.ej.ek = windowToken;
            this.ej.left = iArr[0];
            this.ej.top = iArr[1];
            this.ej.right = iArr[0] + width;
            this.ej.bottom = iArr[1] + height;
            if (this.dE) {
                aB();
                this.dE = false;
            }
        }

        protected void mo1216F(int i) {
            this.ej = new C0163a(i, null);
        }

        public void mo1217a(View view) {
            View view2;
            Context context;
            this.dt.ax();
            if (this.em != null) {
                view2 = (View) this.em.get();
                context = this.dt.getContext();
                if (view2 == null && (context instanceof Activity)) {
                    view2 = ((Activity) context).getWindow().getDecorView();
                }
                if (view2 != null) {
                    view2.removeOnAttachStateChangeListener(this);
                    ViewTreeObserver viewTreeObserver = view2.getViewTreeObserver();
                    if (as.ar()) {
                        viewTreeObserver.removeOnGlobalLayoutListener(this);
                    } else {
                        viewTreeObserver.removeGlobalOnLayoutListener(this);
                    }
                }
            }
            this.em = null;
            context = this.dt.getContext();
            if (view == null && (context instanceof Activity)) {
                view2 = ((Activity) context).findViewById(16908290);
                if (view2 == null) {
                    view2 = ((Activity) context).getWindow().getDecorView();
                }
                ax.m223b("PopupManager", "You have not specified a View to use as content view for popups. Falling back to the Activity content view which may not work properly in future versions of the API. Use setViewForPopups() to set your content view.");
                view = view2;
            }
            if (view != null) {
                m848b(view);
                this.em = new WeakReference(view);
                view.addOnAttachStateChangeListener(this);
                view.getViewTreeObserver().addOnGlobalLayoutListener(this);
                return;
            }
            ax.m224c("PopupManager", "No content view usable to display popups. Popups will not be displayed in response to this client's calls. Use setViewForPopups() to set your content view.");
        }

        public void aB() {
            if (this.ej.ek != null) {
                super.aB();
            } else {
                this.dE = this.em != null;
            }
        }

        public void onGlobalLayout() {
            if (this.em != null) {
                View view = (View) this.em.get();
                if (view != null) {
                    m848b(view);
                }
            }
        }

        public void onViewAttachedToWindow(View v) {
            m848b(v);
        }

        public void onViewDetachedFromWindow(View v) {
            this.dt.ax();
            v.removeOnAttachStateChangeListener(this);
        }
    }

    private ba(au auVar, int i) {
        this.dt = auVar;
        mo1216F(i);
    }

    public static ba m334a(au auVar, int i) {
        return as.ao() ? new C1322b(auVar, i) : new ba(auVar, i);
    }

    protected void mo1216F(int i) {
        this.ej = new C0163a(i, new Binder());
    }

    public void mo1217a(View view) {
    }

    public void aB() {
        this.dt.m1200a(this.ej.ek, this.ej.aE());
    }

    public Bundle aC() {
        return this.ej.aE();
    }

    public IBinder aD() {
        return this.ej.ek;
    }

    public void setGravity(int gravity) {
        this.ej.gravity = gravity;
    }
}
