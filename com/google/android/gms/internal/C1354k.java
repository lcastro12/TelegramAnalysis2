package com.google.android.gms.internal;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.data.C1287d;
import com.google.android.gms.internal.C0186o.C1356a;
import com.google.android.gms.internal.C0187p.C1358a;
import java.util.ArrayList;

public abstract class C1354k<T extends IInterface> implements GooglePlayServicesClient {
    public static final String[] bD = new String[]{"service_esmobile", "service_googleme"};
    boolean bA = false;
    boolean bB = false;
    private final Object bC = new Object();
    private T bs;
    private ArrayList<ConnectionCallbacks> bt;
    final ArrayList<ConnectionCallbacks> bu = new ArrayList();
    private boolean bv = false;
    private ArrayList<OnConnectionFailedListener> bw;
    private boolean bx = false;
    private final ArrayList<C0179b<?>> by = new ArrayList();
    private C0180e bz;
    private final String[] f96f;
    private final Context mContext;
    final Handler mHandler;

    final class C0178a extends Handler {
        final /* synthetic */ C1354k bE;

        public C0178a(C1354k c1354k, Looper looper) {
            this.bE = c1354k;
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg.what != 1 || this.bE.isConnecting()) {
                synchronized (this.bE.bC) {
                    this.bE.bB = false;
                }
                if (msg.what == 3) {
                    this.bE.mo2346a(new ConnectionResult(((Integer) msg.obj).intValue(), null));
                    return;
                } else if (msg.what == 4) {
                    synchronized (this.bE.bt) {
                        if (this.bE.bA && this.bE.isConnected() && this.bE.bt.contains(msg.obj)) {
                            ((ConnectionCallbacks) msg.obj).onConnected(this.bE.mo2353z());
                        }
                    }
                    return;
                } else if (msg.what == 2 && !this.bE.isConnected()) {
                    C0179b c0179b = (C0179b) msg.obj;
                    c0179b.mo1092d();
                    c0179b.unregister();
                    return;
                } else if (msg.what == 2 || msg.what == 1) {
                    ((C0179b) msg.obj).mo1319D();
                    return;
                } else {
                    Log.wtf("GmsClient", "Don't know how to handle this message.");
                    return;
                }
            }
            c0179b = (C0179b) msg.obj;
            c0179b.mo1092d();
            c0179b.unregister();
        }
    }

    protected abstract class C0179b<TListener> {
        final /* synthetic */ C1354k bE;
        private boolean bF = false;
        private TListener mListener;

        public C0179b(C1354k c1354k, TListener tListener) {
            this.bE = c1354k;
            this.mListener = tListener;
        }

        public void mo1319D() {
            synchronized (this) {
                Object obj = this.mListener;
                if (this.bF) {
                    Log.w("GmsClient", "Callback proxy " + this + " being reused. This is not safe.");
                }
            }
            if (obj != null) {
                try {
                    mo1091a(obj);
                } catch (RuntimeException e) {
                    mo1092d();
                    throw e;
                }
            }
            mo1092d();
            synchronized (this) {
                this.bF = true;
            }
            unregister();
        }

        public void mo1320E() {
            synchronized (this) {
                this.mListener = null;
            }
        }

        protected abstract void mo1091a(TListener tListener);

        protected abstract void mo1092d();

        public void unregister() {
            mo1320E();
            synchronized (this.bE.by) {
                this.bE.by.remove(this);
            }
        }
    }

    final class C0180e implements ServiceConnection {
        final /* synthetic */ C1354k bE;

        C0180e(C1354k c1354k) {
            this.bE = c1354k;
        }

        public void onServiceConnected(ComponentName component, IBinder binder) {
            this.bE.m999f(binder);
        }

        public void onServiceDisconnected(ComponentName component) {
            this.bE.bs = null;
            this.bE.m988A();
        }
    }

    public abstract class C1352c<TListener> extends C0179b<TListener> {
        private final C1287d f95S;
        final /* synthetic */ C1354k bE;

        public C1352c(C1354k c1354k, TListener tListener, C1287d c1287d) {
            this.bE = c1354k;
            super(c1354k, tListener);
            this.f95S = c1287d;
        }

        public /* bridge */ /* synthetic */ void mo1319D() {
            super.mo1319D();
        }

        public /* bridge */ /* synthetic */ void mo1320E() {
            super.mo1320E();
        }

        protected final void mo1091a(TListener tListener) {
            mo2344a(tListener, this.f95S);
        }

        protected abstract void mo2344a(TListener tListener, C1287d c1287d);

        protected void mo1092d() {
            if (this.f95S != null) {
                this.f95S.close();
            }
        }

        public /* bridge */ /* synthetic */ void unregister() {
            super.unregister();
        }
    }

    protected final class C1353f extends C0179b<Boolean> {
        final /* synthetic */ C1354k bE;
        public final Bundle bH;
        public final IBinder bI;
        public final int statusCode;

        public C1353f(C1354k c1354k, int i, IBinder iBinder, Bundle bundle) {
            this.bE = c1354k;
            super(c1354k, Boolean.valueOf(true));
            this.statusCode = i;
            this.bI = iBinder;
            this.bH = bundle;
        }

        protected void m977a(Boolean bool) {
            if (bool != null) {
                switch (this.statusCode) {
                    case 0:
                        try {
                            if (this.bE.mo2351c().equals(this.bI.getInterfaceDescriptor())) {
                                this.bE.bs = this.bE.mo2350c(this.bI);
                                if (this.bE.bs != null) {
                                    this.bE.mo2352y();
                                    return;
                                }
                            }
                        } catch (RemoteException e) {
                        }
                        C0183l.m484g(this.bE.mContext).m486b(this.bE.mo2349b(), this.bE.bz);
                        this.bE.bz = null;
                        this.bE.bs = null;
                        this.bE.mo2346a(new ConnectionResult(8, null));
                        return;
                    case 10:
                        throw new IllegalStateException("A fatal developer error has occurred. Check the logs for further information.");
                    default:
                        PendingIntent pendingIntent = this.bH != null ? (PendingIntent) this.bH.getParcelable("pendingIntent") : null;
                        if (this.bE.bz != null) {
                            C0183l.m484g(this.bE.mContext).m486b(this.bE.mo2349b(), this.bE.bz);
                            this.bE.bz = null;
                        }
                        this.bE.bs = null;
                        this.bE.mo2346a(new ConnectionResult(this.statusCode, pendingIntent));
                        return;
                }
            }
        }

        protected void mo1092d() {
        }
    }

    public static final class C1722d extends C1356a {
        private C1354k bG;

        public C1722d(C1354k c1354k) {
            this.bG = c1354k;
        }

        public void mo1322b(int i, IBinder iBinder, Bundle bundle) {
            C0192s.m518b((Object) "onPostInitComplete can be called only once per call to getServiceFromBroker", this.bG);
            this.bG.mo2345a(i, iBinder, bundle);
            this.bG = null;
        }
    }

    protected C1354k(Context context, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener, String... strArr) {
        this.mContext = (Context) C0192s.m521d(context);
        this.bt = new ArrayList();
        this.bt.add(C0192s.m521d(connectionCallbacks));
        this.bw = new ArrayList();
        this.bw.add(C0192s.m521d(onConnectionFailedListener));
        this.mHandler = new C0178a(this, context.getMainLooper());
        mo2348a(strArr);
        this.f96f = strArr;
    }

    protected final void m988A() {
        this.mHandler.removeMessages(4);
        synchronized (this.bt) {
            this.bv = true;
            ArrayList arrayList = this.bt;
            int size = arrayList.size();
            for (int i = 0; i < size && this.bA; i++) {
                if (this.bt.contains(arrayList.get(i))) {
                    ((ConnectionCallbacks) arrayList.get(i)).onDisconnected();
                }
            }
            this.bv = false;
        }
    }

    protected final void m989B() {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected. Call connect() and wait for onConnected() to be called.");
        }
    }

    protected final T m990C() {
        m989B();
        return this.bs;
    }

    protected void mo2345a(int i, IBinder iBinder, Bundle bundle) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(1, new C1353f(this, i, iBinder, bundle)));
    }

    protected void mo2346a(ConnectionResult connectionResult) {
        this.mHandler.removeMessages(4);
        synchronized (this.bw) {
            this.bx = true;
            ArrayList arrayList = this.bw;
            int size = arrayList.size();
            int i = 0;
            while (i < size) {
                if (this.bA) {
                    if (this.bw.contains(arrayList.get(i))) {
                        ((OnConnectionFailedListener) arrayList.get(i)).onConnectionFailed(connectionResult);
                    }
                    i++;
                } else {
                    return;
                }
            }
            this.bx = false;
        }
    }

    public final void m993a(C0179b<?> c0179b) {
        synchronized (this.by) {
            this.by.add(c0179b);
        }
        this.mHandler.sendMessage(this.mHandler.obtainMessage(2, c0179b));
    }

    protected abstract void mo2347a(C0187p c0187p, C1722d c1722d) throws RemoteException;

    protected void mo2348a(String... strArr) {
    }

    protected abstract String mo2349b();

    protected abstract T mo2350c(IBinder iBinder);

    protected abstract String mo2351c();

    public void connect() {
        this.bA = true;
        synchronized (this.bC) {
            this.bB = true;
        }
        int isGooglePlayServicesAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.mContext);
        if (isGooglePlayServicesAvailable != 0) {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(3, Integer.valueOf(isGooglePlayServicesAvailable)));
            return;
        }
        if (this.bz != null) {
            Log.e("GmsClient", "Calling connect() while still connected, missing disconnect().");
            this.bs = null;
            C0183l.m484g(this.mContext).m486b(mo2349b(), this.bz);
        }
        this.bz = new C0180e(this);
        if (!C0183l.m484g(this.mContext).m485a(mo2349b(), this.bz)) {
            Log.e("GmsClient", "unable to connect to service: " + mo2349b());
            this.mHandler.sendMessage(this.mHandler.obtainMessage(3, Integer.valueOf(9)));
        }
    }

    public void disconnect() {
        this.bA = false;
        synchronized (this.bC) {
            this.bB = false;
        }
        synchronized (this.by) {
            int size = this.by.size();
            for (int i = 0; i < size; i++) {
                ((C0179b) this.by.get(i)).mo1320E();
            }
            this.by.clear();
        }
        this.bs = null;
        if (this.bz != null) {
            C0183l.m484g(this.mContext).m486b(mo2349b(), this.bz);
            this.bz = null;
        }
    }

    protected final void m999f(IBinder iBinder) {
        try {
            mo2347a(C1358a.m1018h(iBinder), new C1722d(this));
        } catch (RemoteException e) {
            Log.w("GmsClient", "service died");
        }
    }

    public final Context getContext() {
        return this.mContext;
    }

    public boolean isConnected() {
        return this.bs != null;
    }

    public boolean isConnecting() {
        boolean z;
        synchronized (this.bC) {
            z = this.bB;
        }
        return z;
    }

    public boolean isConnectionCallbacksRegistered(ConnectionCallbacks listener) {
        boolean contains;
        C0192s.m521d(listener);
        synchronized (this.bt) {
            contains = this.bt.contains(listener);
        }
        return contains;
    }

    public boolean isConnectionFailedListenerRegistered(OnConnectionFailedListener listener) {
        boolean contains;
        C0192s.m521d(listener);
        synchronized (this.bw) {
            contains = this.bw.contains(listener);
        }
        return contains;
    }

    public void registerConnectionCallbacks(ConnectionCallbacks listener) {
        C0192s.m521d(listener);
        synchronized (this.bt) {
            if (this.bt.contains(listener)) {
                Log.w("GmsClient", "registerConnectionCallbacks(): listener " + listener + " is already registered");
            } else {
                if (this.bv) {
                    this.bt = new ArrayList(this.bt);
                }
                this.bt.add(listener);
            }
        }
        if (isConnected()) {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(4, listener));
        }
    }

    public void registerConnectionFailedListener(OnConnectionFailedListener listener) {
        C0192s.m521d(listener);
        synchronized (this.bw) {
            if (this.bw.contains(listener)) {
                Log.w("GmsClient", "registerConnectionFailedListener(): listener " + listener + " is already registered");
            } else {
                if (this.bx) {
                    this.bw = new ArrayList(this.bw);
                }
                this.bw.add(listener);
            }
        }
    }

    public void unregisterConnectionCallbacks(ConnectionCallbacks listener) {
        C0192s.m521d(listener);
        synchronized (this.bt) {
            if (this.bt != null) {
                if (this.bv) {
                    this.bt = new ArrayList(this.bt);
                }
                if (!this.bt.remove(listener)) {
                    Log.w("GmsClient", "unregisterConnectionCallbacks(): listener " + listener + " not found");
                } else if (this.bv && !this.bu.contains(listener)) {
                    this.bu.add(listener);
                }
            }
        }
    }

    public void unregisterConnectionFailedListener(OnConnectionFailedListener listener) {
        C0192s.m521d(listener);
        synchronized (this.bw) {
            if (this.bw != null) {
                if (this.bx) {
                    this.bw = new ArrayList(this.bw);
                }
                if (!this.bw.remove(listener)) {
                    Log.w("GmsClient", "unregisterConnectionFailedListener(): listener " + listener + " not found");
                }
            }
        }
    }

    public final String[] m1000x() {
        return this.f96f;
    }

    protected void mo2352y() {
        boolean z = true;
        synchronized (this.bt) {
            C0192s.m515a(!this.bv);
            this.mHandler.removeMessages(4);
            this.bv = true;
            if (this.bu.size() != 0) {
                z = false;
            }
            C0192s.m515a(z);
            Bundle z2 = mo2353z();
            ArrayList arrayList = this.bt;
            int size = arrayList.size();
            for (int i = 0; i < size && this.bA && isConnected(); i++) {
                this.bu.size();
                if (!this.bu.contains(arrayList.get(i))) {
                    ((ConnectionCallbacks) arrayList.get(i)).onConnected(z2);
                }
            }
            this.bu.clear();
            this.bv = false;
        }
    }

    protected Bundle mo2353z() {
        return null;
    }
}
