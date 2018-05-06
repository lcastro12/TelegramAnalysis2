package com.google.android.gms.internal;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.IBinder;
import android.os.Message;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.internal.C1354k.C0180e;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public final class C0183l implements Callback {
    private static final Object bJ = new Object();
    private static C0183l bK;
    private final Context bL;
    private final HashMap<String, C0182a> bM = new HashMap();
    private final Handler mHandler;

    final class C0182a {
        private final String bN;
        private final C0181a bO = new C0181a(this);
        private final HashSet<C0180e> bP = new HashSet();
        private boolean bQ;
        private IBinder bR;
        private ComponentName bS;
        final /* synthetic */ C0183l bT;
        private int mState = 0;

        public class C0181a implements ServiceConnection {
            final /* synthetic */ C0182a bU;

            public C0181a(C0182a c0182a) {
                this.bU = c0182a;
            }

            public void onServiceConnected(ComponentName component, IBinder binder) {
                synchronized (this.bU.bT.bM) {
                    this.bU.bR = binder;
                    this.bU.bS = component;
                    Iterator it = this.bU.bP.iterator();
                    while (it.hasNext()) {
                        ((C0180e) it.next()).onServiceConnected(component, binder);
                    }
                    this.bU.mState = 1;
                }
            }

            public void onServiceDisconnected(ComponentName component) {
                synchronized (this.bU.bT.bM) {
                    this.bU.bR = null;
                    this.bU.bS = component;
                    Iterator it = this.bU.bP.iterator();
                    while (it.hasNext()) {
                        ((C0180e) it.next()).onServiceDisconnected(component);
                    }
                    this.bU.mState = 2;
                }
            }
        }

        public C0182a(C0183l c0183l, String str) {
            this.bT = c0183l;
            this.bN = str;
        }

        public C0181a m476F() {
            return this.bO;
        }

        public String m477G() {
            return this.bN;
        }

        public boolean m478H() {
            return this.bP.isEmpty();
        }

        public void m479a(C0180e c0180e) {
            this.bP.add(c0180e);
        }

        public void m480b(C0180e c0180e) {
            this.bP.remove(c0180e);
        }

        public void m481b(boolean z) {
            this.bQ = z;
        }

        public boolean m482c(C0180e c0180e) {
            return this.bP.contains(c0180e);
        }

        public IBinder getBinder() {
            return this.bR;
        }

        public ComponentName getComponentName() {
            return this.bS;
        }

        public int getState() {
            return this.mState;
        }

        public boolean isBound() {
            return this.bQ;
        }
    }

    private C0183l(Context context) {
        this.mHandler = new Handler(context.getMainLooper(), this);
        this.bL = context.getApplicationContext();
    }

    public static C0183l m484g(Context context) {
        synchronized (bJ) {
            if (bK == null) {
                bK = new C0183l(context.getApplicationContext());
            }
        }
        return bK;
    }

    public boolean m485a(String str, C0180e c0180e) {
        boolean isBound;
        synchronized (this.bM) {
            C0182a c0182a = (C0182a) this.bM.get(str);
            if (c0182a != null) {
                this.mHandler.removeMessages(0, c0182a);
                if (!c0182a.m482c(c0180e)) {
                    c0182a.m479a((C0180e) c0180e);
                    switch (c0182a.getState()) {
                        case 1:
                            c0180e.onServiceConnected(c0182a.getComponentName(), c0182a.getBinder());
                            break;
                        case 2:
                            c0182a.m481b(this.bL.bindService(new Intent(str).setPackage(GooglePlayServicesUtil.GOOGLE_PLAY_SERVICES_PACKAGE), c0182a.m476F(), 129));
                            break;
                        default:
                            break;
                    }
                }
                throw new IllegalStateException("Trying to bind a GmsServiceConnection that was already connected before.  startServiceAction=" + str);
            }
            c0182a = new C0182a(this, str);
            c0182a.m479a((C0180e) c0180e);
            c0182a.m481b(this.bL.bindService(new Intent(str).setPackage(GooglePlayServicesUtil.GOOGLE_PLAY_SERVICES_PACKAGE), c0182a.m476F(), 129));
            this.bM.put(str, c0182a);
            isBound = c0182a.isBound();
        }
        return isBound;
    }

    public void m486b(String str, C0180e c0180e) {
        synchronized (this.bM) {
            C0182a c0182a = (C0182a) this.bM.get(str);
            if (c0182a == null) {
                throw new IllegalStateException("Nonexistent connection status for service action: " + str);
            } else if (c0182a.m482c(c0180e)) {
                c0182a.m480b((C0180e) c0180e);
                if (c0182a.m478H()) {
                    this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(0, c0182a), 5000);
                }
            } else {
                throw new IllegalStateException("Trying to unbind a GmsServiceConnection  that was not bound before.  startServiceAction=" + str);
            }
        }
    }

    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case 0:
                C0182a c0182a = (C0182a) msg.obj;
                synchronized (this.bM) {
                    if (c0182a.m478H()) {
                        this.bL.unbindService(c0182a.m476F());
                        this.bM.remove(c0182a.m477G());
                    }
                }
                return true;
            default:
                return false;
        }
    }
}
