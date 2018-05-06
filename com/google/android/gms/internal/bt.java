package com.google.android.gms.internal;

import android.app.PendingIntent;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.data.C1287d;
import com.google.android.gms.internal.C1354k.C0179b;
import com.google.android.gms.internal.C1354k.C1352c;
import com.google.android.gms.internal.C1354k.C1722d;
import com.google.android.gms.internal.bs.C1343a;
import com.google.android.gms.plus.C1433a;
import com.google.android.gms.plus.PlusClient.OnAccessRevokedListener;
import com.google.android.gms.plus.PlusClient.OnMomentsLoadedListener;
import com.google.android.gms.plus.PlusClient.OnPeopleLoadedListener;
import com.google.android.gms.plus.model.moments.Moment;
import com.google.android.gms.plus.model.moments.MomentBuffer;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.PersonBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class bt extends C1354k<bs> {
    private Person ip;
    private C1433a iq;

    final class C1344f extends C0179b<OnAccessRevokedListener> {
        final /* synthetic */ bt is;
        private final ConnectionResult it;

        public C1344f(bt btVar, OnAccessRevokedListener onAccessRevokedListener, ConnectionResult connectionResult) {
            this.is = btVar;
            super(btVar, onAccessRevokedListener);
            this.it = connectionResult;
        }

        protected void m943a(OnAccessRevokedListener onAccessRevokedListener) {
            this.is.disconnect();
            if (onAccessRevokedListener != null) {
                onAccessRevokedListener.onAccessRevoked(this.it);
            }
        }

        protected void mo1092d() {
        }
    }

    final class C1708b extends C1352c<OnMomentsLoadedListener> {
        final /* synthetic */ bt is;
        private final ConnectionResult it;
        private final String iu;
        private final String iv;

        public C1708b(bt btVar, OnMomentsLoadedListener onMomentsLoadedListener, ConnectionResult connectionResult, C1287d c1287d, String str, String str2) {
            this.is = btVar;
            super(btVar, onMomentsLoadedListener, c1287d);
            this.it = connectionResult;
            this.iu = str;
            this.iv = str2;
        }

        protected void m1253a(OnMomentsLoadedListener onMomentsLoadedListener, C1287d c1287d) {
            onMomentsLoadedListener.onMomentsLoaded(this.it, c1287d != null ? new MomentBuffer(c1287d) : null, this.iu, this.iv);
        }
    }

    final class C1709d extends C1352c<OnPeopleLoadedListener> {
        final /* synthetic */ bt is;
        private final ConnectionResult it;
        private final String iu;

        public C1709d(bt btVar, OnPeopleLoadedListener onPeopleLoadedListener, ConnectionResult connectionResult, C1287d c1287d, String str) {
            this.is = btVar;
            super(btVar, onPeopleLoadedListener, c1287d);
            this.it = connectionResult;
            this.iu = str;
        }

        protected void m1255a(OnPeopleLoadedListener onPeopleLoadedListener, C1287d c1287d) {
            onPeopleLoadedListener.onPeopleLoaded(this.it, c1287d != null ? new PersonBuffer(c1287d) : null, this.iu);
        }
    }

    final class C1780a extends bo {
        private final OnMomentsLoadedListener ir;
        final /* synthetic */ bt is;

        public C1780a(bt btVar, OnMomentsLoadedListener onMomentsLoadedListener) {
            this.is = btVar;
            this.ir = onMomentsLoadedListener;
        }

        public void mo1257a(C1287d c1287d, String str, String str2) {
            C1287d c1287d2;
            ConnectionResult connectionResult = new ConnectionResult(c1287d.getStatusCode(), c1287d.m639l() != null ? (PendingIntent) c1287d.m639l().getParcelable("pendingIntent") : null);
            if (connectionResult.isSuccess() || c1287d == null) {
                c1287d2 = c1287d;
            } else {
                if (!c1287d.isClosed()) {
                    c1287d.close();
                }
                c1287d2 = null;
            }
            this.is.m993a(new C1708b(this.is, this.ir, connectionResult, c1287d2, str, str2));
        }
    }

    final class C1781c extends bo {
        final /* synthetic */ bt is;
        private final OnPeopleLoadedListener iw;

        public C1781c(bt btVar, OnPeopleLoadedListener onPeopleLoadedListener) {
            this.is = btVar;
            this.iw = onPeopleLoadedListener;
        }

        public void mo1256a(C1287d c1287d, String str) {
            C1287d c1287d2;
            ConnectionResult connectionResult = new ConnectionResult(c1287d.getStatusCode(), c1287d.m639l() != null ? (PendingIntent) c1287d.m639l().getParcelable("pendingIntent") : null);
            if (connectionResult.isSuccess() || c1287d == null) {
                c1287d2 = c1287d;
            } else {
                if (!c1287d.isClosed()) {
                    c1287d.close();
                }
                c1287d2 = null;
            }
            this.is.m993a(new C1709d(this.is, this.iw, connectionResult, c1287d2, str));
        }
    }

    final class C1782e extends bo {
        final /* synthetic */ bt is;
        private final OnAccessRevokedListener ix;

        public C1782e(bt btVar, OnAccessRevokedListener onAccessRevokedListener) {
            this.is = btVar;
            this.ix = onAccessRevokedListener;
        }

        public void mo1258b(int i, Bundle bundle) {
            PendingIntent pendingIntent = null;
            if (bundle != null) {
                pendingIntent = (PendingIntent) bundle.getParcelable("pendingIntent");
            }
            this.is.m993a(new C1344f(this.is, this.ix, new ConnectionResult(i, pendingIntent)));
        }
    }

    public bt(Context context, C1433a c1433a, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener) {
        super(context, connectionCallbacks, onConnectionFailedListener, c1433a.by());
        this.iq = c1433a;
    }

    public boolean m1257F(String str) {
        return Arrays.asList(m1000x()).contains(str);
    }

    protected void mo2345a(int i, IBinder iBinder, Bundle bundle) {
        if (i == 0 && bundle != null && bundle.containsKey("loaded_person")) {
            this.ip = cc.m1342d(bundle.getByteArray("loaded_person"));
        }
        super.mo2345a(i, iBinder, bundle);
    }

    protected void mo2347a(C0187p c0187p, C1722d c1722d) throws RemoteException {
        Bundle bundle = new Bundle();
        bundle.putBoolean("skip_oob", false);
        bundle.putStringArray("request_visible_actions", this.iq.bz());
        if (this.iq.bA() != null) {
            bundle.putStringArray("required_features", this.iq.bA());
        }
        if (this.iq.bD() != null) {
            bundle.putString("application_name", this.iq.bD());
        }
        c0187p.mo1327a(c1722d, GooglePlayServicesUtil.GOOGLE_PLAY_SERVICES_VERSION_CODE, this.iq.bC(), this.iq.bB(), m1000x(), this.iq.getAccountName(), bundle);
    }

    public void m1260a(OnPeopleLoadedListener onPeopleLoadedListener, Collection<String> collection) {
        m989B();
        bp c1781c = new C1781c(this, onPeopleLoadedListener);
        try {
            ((bs) m990C()).mo1288a(c1781c, new ArrayList(collection));
        } catch (RemoteException e) {
            c1781c.mo1256a(C1287d.m625f(8), null);
        }
    }

    public void m1261a(OnPeopleLoadedListener onPeopleLoadedListener, String[] strArr) {
        m1260a(onPeopleLoadedListener, Arrays.asList(strArr));
    }

    protected bs ac(IBinder iBinder) {
        return C1343a.ab(iBinder);
    }

    protected String mo2349b() {
        return "com.google.android.gms.plus.service.START";
    }

    protected /* synthetic */ IInterface mo2350c(IBinder iBinder) {
        return ac(iBinder);
    }

    protected String mo2351c() {
        return "com.google.android.gms.plus.internal.IPlusService";
    }

    public void clearDefaultAccount() {
        m989B();
        try {
            this.ip = null;
            ((bs) m990C()).clearDefaultAccount();
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    public String getAccountName() {
        m989B();
        try {
            return ((bs) m990C()).getAccountName();
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    public Person getCurrentPerson() {
        m989B();
        return this.ip;
    }

    public void loadMoments(OnMomentsLoadedListener listener) {
        loadMoments(listener, 20, null, null, null, "me");
    }

    public void loadMoments(OnMomentsLoadedListener listener, int maxResults, String pageToken, Uri targetUrl, String type, String userId) {
        m989B();
        Object c1780a = listener != null ? new C1780a(this, listener) : null;
        try {
            ((bs) m990C()).mo1273a(c1780a, maxResults, pageToken, targetUrl, type, userId);
        } catch (RemoteException e) {
            c1780a.mo1257a(C1287d.m625f(8), null, null);
        }
    }

    public void loadVisiblePeople(OnPeopleLoadedListener listener, int orderBy, String pageToken) {
        m989B();
        bp c1781c = new C1781c(this, listener);
        try {
            ((bs) m990C()).mo1269a(c1781c, 1, orderBy, -1, pageToken);
        } catch (RemoteException e) {
            c1781c.mo1256a(C1287d.m625f(8), null);
        }
    }

    public void loadVisiblePeople(OnPeopleLoadedListener listener, String pageToken) {
        loadVisiblePeople(listener, 0, pageToken);
    }

    public void removeMoment(String momentId) {
        m989B();
        try {
            ((bs) m990C()).removeMoment(momentId);
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    public void revokeAccessAndDisconnect(OnAccessRevokedListener listener) {
        m989B();
        clearDefaultAccount();
        Object c1782e = new C1782e(this, listener);
        try {
            ((bs) m990C()).mo1292c(c1782e);
        } catch (RemoteException e) {
            c1782e.mo1258b(8, null);
        }
    }

    public void writeMoment(Moment moment) {
        m989B();
        try {
            ((bs) m990C()).mo1267a(ak.m692a((bz) moment));
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }
}
