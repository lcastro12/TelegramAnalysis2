package com.google.android.gms.internal;

import android.content.Context;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;
import android.util.Log;
import com.google.android.gms.appstate.AppState;
import com.google.android.gms.appstate.AppStateBuffer;
import com.google.android.gms.appstate.AppStateClient;
import com.google.android.gms.appstate.OnSignOutCompleteListener;
import com.google.android.gms.appstate.OnStateDeletedListener;
import com.google.android.gms.appstate.OnStateListLoadedListener;
import com.google.android.gms.appstate.OnStateLoadedListener;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.data.C1287d;
import com.google.android.gms.internal.C0169e.C1350a;
import com.google.android.gms.internal.C1354k.C0179b;
import com.google.android.gms.internal.C1354k.C1352c;
import com.google.android.gms.internal.C1354k.C1722d;

public final class C1712c extends C1354k<C0169e> {
    private final String f159g;

    final class C1345b extends C0179b<OnStateDeletedListener> {
        final /* synthetic */ C1712c f89o;
        private final int f90p;
        private final int f91q;

        public C1345b(C1712c c1712c, OnStateDeletedListener onStateDeletedListener, int i, int i2) {
            this.f89o = c1712c;
            super(c1712c, onStateDeletedListener);
            this.f90p = i;
            this.f91q = i2;
        }

        public void m947a(OnStateDeletedListener onStateDeletedListener) {
            onStateDeletedListener.onStateDeleted(this.f90p, this.f91q);
        }

        protected void mo1092d() {
        }
    }

    final class C1346h extends C0179b<OnSignOutCompleteListener> {
        final /* synthetic */ C1712c f92o;

        public C1346h(C1712c c1712c, OnSignOutCompleteListener onSignOutCompleteListener) {
            this.f92o = c1712c;
            super(c1712c, onSignOutCompleteListener);
        }

        public void m950a(OnSignOutCompleteListener onSignOutCompleteListener) {
            onSignOutCompleteListener.onSignOutComplete();
        }

        protected void mo1092d() {
        }
    }

    final class C1710d extends C1352c<OnStateListLoadedListener> {
        final /* synthetic */ C1712c f156o;

        public C1710d(C1712c c1712c, OnStateListLoadedListener onStateListLoadedListener, C1287d c1287d) {
            this.f156o = c1712c;
            super(c1712c, onStateListLoadedListener, c1287d);
        }

        public void m1277a(OnStateListLoadedListener onStateListLoadedListener, C1287d c1287d) {
            onStateListLoadedListener.onStateListLoaded(c1287d.getStatusCode(), new AppStateBuffer(c1287d));
        }
    }

    final class C1711f extends C1352c<OnStateLoadedListener> {
        final /* synthetic */ C1712c f157o;
        private final int f158q;

        public C1711f(C1712c c1712c, OnStateLoadedListener onStateLoadedListener, int i, C1287d c1287d) {
            this.f157o = c1712c;
            super(c1712c, onStateLoadedListener, c1287d);
            this.f158q = i;
        }

        public void m1279a(OnStateLoadedListener onStateLoadedListener, C1287d c1287d) {
            byte[] bArr = null;
            AppStateBuffer appStateBuffer = new AppStateBuffer(c1287d);
            try {
                String conflictVersion;
                byte[] localData;
                if (appStateBuffer.getCount() > 0) {
                    AppState appState = appStateBuffer.get(0);
                    conflictVersion = appState.getConflictVersion();
                    localData = appState.getLocalData();
                    bArr = appState.getConflictData();
                } else {
                    localData = null;
                    conflictVersion = null;
                }
                appStateBuffer.close();
                int statusCode = c1287d.getStatusCode();
                if (statusCode == AppStateClient.STATUS_WRITE_OUT_OF_DATE_VERSION) {
                    onStateLoadedListener.onStateConflict(this.f158q, conflictVersion, localData, bArr);
                } else {
                    onStateLoadedListener.onStateLoaded(statusCode, this.f158q, localData);
                }
            } catch (Throwable th) {
                appStateBuffer.close();
            }
        }
    }

    final class C1783a extends C1704b {
        private final OnStateDeletedListener f164n;
        final /* synthetic */ C1712c f165o;

        public C1783a(C1712c c1712c, OnStateDeletedListener onStateDeletedListener) {
            this.f165o = c1712c;
            this.f164n = (OnStateDeletedListener) C0192s.m518b((Object) onStateDeletedListener, (Object) "Listener must not be null");
        }

        public void onStateDeleted(int statusCode, int stateKey) {
            this.f165o.m993a(new C1345b(this.f165o, this.f164n, statusCode, stateKey));
        }
    }

    final class C1784c extends C1704b {
        final /* synthetic */ C1712c f166o;
        private final OnStateListLoadedListener f167r;

        public C1784c(C1712c c1712c, OnStateListLoadedListener onStateListLoadedListener) {
            this.f166o = c1712c;
            this.f167r = (OnStateListLoadedListener) C0192s.m518b((Object) onStateListLoadedListener, (Object) "Listener must not be null");
        }

        public void mo1307a(C1287d c1287d) {
            this.f166o.m993a(new C1710d(this.f166o, this.f167r, c1287d));
        }
    }

    final class C1785e extends C1704b {
        final /* synthetic */ C1712c f168o;
        private final OnStateLoadedListener f169s;

        public C1785e(C1712c c1712c, OnStateLoadedListener onStateLoadedListener) {
            this.f168o = c1712c;
            this.f169s = (OnStateLoadedListener) C0192s.m518b((Object) onStateLoadedListener, (Object) "Listener must not be null");
        }

        public void mo1306a(int i, C1287d c1287d) {
            this.f168o.m993a(new C1711f(this.f168o, this.f169s, i, c1287d));
        }
    }

    final class C1786g extends C1704b {
        final /* synthetic */ C1712c f170o;
        private final OnSignOutCompleteListener f171t;

        public C1786g(C1712c c1712c, OnSignOutCompleteListener onSignOutCompleteListener) {
            this.f170o = c1712c;
            this.f171t = (OnSignOutCompleteListener) C0192s.m518b((Object) onSignOutCompleteListener, (Object) "Listener must not be null");
        }

        public void onSignOutComplete() {
            this.f170o.m993a(new C1346h(this.f170o, this.f171t));
        }
    }

    public C1712c(Context context, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener, String str, String[] strArr) {
        super(context, connectionCallbacks, onConnectionFailedListener, strArr);
        this.f159g = (String) C0192s.m521d(str);
    }

    public void m1281a(OnStateLoadedListener onStateLoadedListener, int i, byte[] bArr) {
        if (onStateLoadedListener == null) {
            C0168d c0168d = null;
        } else {
            Object c1785e = new C1785e(this, onStateLoadedListener);
        }
        try {
            ((C0169e) m990C()).mo1313a(c0168d, i, bArr);
        } catch (RemoteException e) {
            Log.w("AppStateClient", "service died");
        }
    }

    protected void mo2347a(C0187p c0187p, C1722d c1722d) throws RemoteException {
        c0187p.mo1326a(c1722d, GooglePlayServicesUtil.GOOGLE_PLAY_SERVICES_VERSION_CODE, getContext().getPackageName(), this.f159g, m1000x());
    }

    protected void mo2348a(String... strArr) {
        boolean z = false;
        for (String equals : strArr) {
            if (equals.equals(Scopes.APP_STATE)) {
                z = true;
            }
        }
        C0192s.m516a(z, String.format("AppStateClient requires %s to function.", new Object[]{Scopes.APP_STATE}));
    }

    protected C0169e m1284b(IBinder iBinder) {
        return C1350a.m966e(iBinder);
    }

    protected String mo2349b() {
        return "com.google.android.gms.appstate.service.START";
    }

    protected /* synthetic */ IInterface mo2350c(IBinder iBinder) {
        return m1284b(iBinder);
    }

    protected String mo2351c() {
        return "com.google.android.gms.appstate.internal.IAppStateService";
    }

    public void deleteState(OnStateDeletedListener listener, int stateKey) {
        try {
            ((C0169e) m990C()).mo1315b(new C1783a(this, listener), stateKey);
        } catch (RemoteException e) {
            Log.w("AppStateClient", "service died");
        }
    }

    public int getMaxNumKeys() {
        try {
            return ((C0169e) m990C()).getMaxNumKeys();
        } catch (RemoteException e) {
            Log.w("AppStateClient", "service died");
            return 2;
        }
    }

    public int getMaxStateSize() {
        try {
            return ((C0169e) m990C()).getMaxStateSize();
        } catch (RemoteException e) {
            Log.w("AppStateClient", "service died");
            return 2;
        }
    }

    public void listStates(OnStateListLoadedListener listener) {
        try {
            ((C0169e) m990C()).mo1310a(new C1784c(this, listener));
        } catch (RemoteException e) {
            Log.w("AppStateClient", "service died");
        }
    }

    public void loadState(OnStateLoadedListener listener, int stateKey) {
        try {
            ((C0169e) m990C()).mo1311a(new C1785e(this, listener), stateKey);
        } catch (RemoteException e) {
            Log.w("AppStateClient", "service died");
        }
    }

    public void resolveState(OnStateLoadedListener listener, int stateKey, String resolvedVersion, byte[] resolvedData) {
        try {
            ((C0169e) m990C()).mo1312a(new C1785e(this, listener), stateKey, resolvedVersion, resolvedData);
        } catch (RemoteException e) {
            Log.w("AppStateClient", "service died");
        }
    }

    public void signOut(OnSignOutCompleteListener listener) {
        if (listener == null) {
            C0168d c0168d = null;
        } else {
            Object c1786g = new C1786g(this, listener);
        }
        try {
            ((C0169e) m990C()).mo1314b(c0168d);
        } catch (RemoteException e) {
            Log.w("AppStateClient", "service died");
        }
    }
}
