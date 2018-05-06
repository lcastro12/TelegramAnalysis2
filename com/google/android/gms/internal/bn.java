package com.google.android.gms.internal;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.internal.C1354k.C0179b;
import com.google.android.gms.internal.C1354k.C1722d;
import com.google.android.gms.internal.bl.C1331a;
import com.google.android.gms.internal.bm.C1333a;
import com.google.android.gms.panorama.PanoramaClient.C0234a;
import com.google.android.gms.panorama.PanoramaClient.OnPanoramaInfoLoadedListener;

public class bn extends C1354k<bm> {

    final class C1334a extends C0179b<C0234a> {
        public final ConnectionResult hO;
        public final Intent hP;
        final /* synthetic */ bn hQ;
        public final int type;

        public C1334a(bn bnVar, C0234a c0234a, ConnectionResult connectionResult, int i, Intent intent) {
            this.hQ = bnVar;
            super(bnVar, c0234a);
            this.hO = connectionResult;
            this.type = i;
            this.hP = intent;
        }

        protected void m881a(C0234a c0234a) {
            if (c0234a != null) {
                c0234a.m598a(this.hO, this.type, this.hP);
            }
        }

        protected void mo1092d() {
        }
    }

    final class C1335c extends C0179b<OnPanoramaInfoLoadedListener> {
        private final ConnectionResult hO;
        private final Intent hP;
        final /* synthetic */ bn hQ;

        public C1335c(bn bnVar, OnPanoramaInfoLoadedListener onPanoramaInfoLoadedListener, ConnectionResult connectionResult, Intent intent) {
            this.hQ = bnVar;
            super(bnVar, onPanoramaInfoLoadedListener);
            this.hO = connectionResult;
            this.hP = intent;
        }

        protected void m884a(OnPanoramaInfoLoadedListener onPanoramaInfoLoadedListener) {
            if (onPanoramaInfoLoadedListener != null) {
                onPanoramaInfoLoadedListener.onPanoramaInfoLoaded(this.hO, this.hP);
            }
        }

        protected void mo1092d() {
        }
    }

    final class C1707b extends C1331a {
        final /* synthetic */ bn hQ;
        private final C0234a hR = null;
        private final OnPanoramaInfoLoadedListener hS;
        private final Uri hT;

        public C1707b(bn bnVar, OnPanoramaInfoLoadedListener onPanoramaInfoLoadedListener, Uri uri) {
            this.hQ = bnVar;
            this.hS = onPanoramaInfoLoadedListener;
            this.hT = uri;
        }

        public void mo1243a(int i, Bundle bundle, int i2, Intent intent) {
            if (this.hT != null) {
                this.hQ.getContext().revokeUriPermission(this.hT, 1);
            }
            PendingIntent pendingIntent = null;
            if (bundle != null) {
                pendingIntent = (PendingIntent) bundle.getParcelable("pendingIntent");
            }
            ConnectionResult connectionResult = new ConnectionResult(i, pendingIntent);
            if (this.hR != null) {
                this.hQ.m993a(new C1334a(this.hQ, this.hR, connectionResult, i2, intent));
            } else {
                this.hQ.m993a(new C1335c(this.hQ, this.hS, connectionResult, intent));
            }
        }
    }

    public bn(Context context, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener) {
        super(context, connectionCallbacks, onConnectionFailedListener, (String[]) null);
    }

    public bm m1229X(IBinder iBinder) {
        return C1333a.m880W(iBinder);
    }

    public void m1230a(C1707b c1707b, Uri uri, Bundle bundle, boolean z) {
        m989B();
        if (z) {
            getContext().grantUriPermission(GooglePlayServicesUtil.GOOGLE_PLAY_SERVICES_PACKAGE, uri, 1);
        }
        try {
            ((bm) m990C()).mo1244a(c1707b, uri, bundle, z);
        } catch (RemoteException e) {
            c1707b.mo1243a(8, null, 0, null);
        }
    }

    protected void mo2347a(C0187p c0187p, C1722d c1722d) throws RemoteException {
        c0187p.mo1325a(c1722d, GooglePlayServicesUtil.GOOGLE_PLAY_SERVICES_VERSION_CODE, getContext().getPackageName(), new Bundle());
    }

    public void m1232a(OnPanoramaInfoLoadedListener onPanoramaInfoLoadedListener, Uri uri, boolean z) {
        m1230a(new C1707b(this, onPanoramaInfoLoadedListener, z ? uri : null), uri, null, z);
    }

    protected String mo2349b() {
        return "com.google.android.gms.panorama.service.START";
    }

    public /* synthetic */ IInterface mo2350c(IBinder iBinder) {
        return m1229X(iBinder);
    }

    protected String mo2351c() {
        return "com.google.android.gms.panorama.internal.IPanoramaService";
    }
}
