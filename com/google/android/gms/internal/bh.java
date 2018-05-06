package com.google.android.gms.internal;

import android.app.PendingIntent;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.internal.C1354k.C0179b;
import com.google.android.gms.internal.C1354k.C1722d;
import com.google.android.gms.internal.be.C1324a;
import com.google.android.gms.internal.bf.C1326a;
import com.google.android.gms.location.LocationClient.OnAddGeofencesResultListener;
import com.google.android.gms.location.LocationClient.OnRemoveGeofencesResultListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationStatusCodes;
import java.util.List;

public class bh extends C1354k<bf> {
    private final bk<bf> fG = new C1328c();
    private final bg fM;
    private final String fN;

    private final class C1327a extends C0179b<OnAddGeofencesResultListener> {
        private final String[] fO;
        final /* synthetic */ bh fP;
        private final int f81p;

        public C1327a(bh bhVar, OnAddGeofencesResultListener onAddGeofencesResultListener, int i, String[] strArr) {
            this.fP = bhVar;
            super(bhVar, onAddGeofencesResultListener);
            this.f81p = LocationStatusCodes.m535O(i);
            this.fO = strArr;
        }

        protected void m862a(OnAddGeofencesResultListener onAddGeofencesResultListener) {
            if (onAddGeofencesResultListener != null) {
                onAddGeofencesResultListener.onAddGeofencesResult(this.f81p, this.fO);
            }
        }

        protected void mo1092d() {
        }
    }

    private final class C1328c implements bk<bf> {
        final /* synthetic */ bh fP;

        private C1328c(bh bhVar) {
            this.fP = bhVar;
        }

        public void mo1240B() {
            this.fP.m989B();
        }

        public /* synthetic */ IInterface mo1241C() {
            return aS();
        }

        public bf aS() {
            return (bf) this.fP.m990C();
        }
    }

    private final class C1329d extends C0179b<OnRemoveGeofencesResultListener> {
        private final String[] fO;
        final /* synthetic */ bh fP;
        private final int fT;
        private final PendingIntent mPendingIntent;
        private final int f82p;

        public C1329d(bh bhVar, int i, OnRemoveGeofencesResultListener onRemoveGeofencesResultListener, int i2, PendingIntent pendingIntent) {
            boolean z = true;
            this.fP = bhVar;
            super(bhVar, onRemoveGeofencesResultListener);
            if (i != 1) {
                z = false;
            }
            C0176h.m463a(z);
            this.fT = i;
            this.f82p = LocationStatusCodes.m535O(i2);
            this.mPendingIntent = pendingIntent;
            this.fO = null;
        }

        public C1329d(bh bhVar, int i, OnRemoveGeofencesResultListener onRemoveGeofencesResultListener, int i2, String[] strArr) {
            this.fP = bhVar;
            super(bhVar, onRemoveGeofencesResultListener);
            C0176h.m463a(i == 2);
            this.fT = i;
            this.f82p = LocationStatusCodes.m535O(i2);
            this.fO = strArr;
            this.mPendingIntent = null;
        }

        protected void m867a(OnRemoveGeofencesResultListener onRemoveGeofencesResultListener) {
            if (onRemoveGeofencesResultListener != null) {
                switch (this.fT) {
                    case 1:
                        onRemoveGeofencesResultListener.onRemoveGeofencesByPendingIntentResult(this.f82p, this.mPendingIntent);
                        return;
                    case 2:
                        onRemoveGeofencesResultListener.onRemoveGeofencesByRequestIdsResult(this.f82p, this.fO);
                        return;
                    default:
                        Log.wtf("LocationClientImpl", "Unsupported action: " + this.fT);
                        return;
                }
            }
        }

        protected void mo1092d() {
        }
    }

    private static final class C1706b extends C1324a {
        private OnAddGeofencesResultListener fQ;
        private OnRemoveGeofencesResultListener fR;
        private bh fS;

        public C1706b(OnAddGeofencesResultListener onAddGeofencesResultListener, bh bhVar) {
            this.fQ = onAddGeofencesResultListener;
            this.fR = null;
            this.fS = bhVar;
        }

        public C1706b(OnRemoveGeofencesResultListener onRemoveGeofencesResultListener, bh bhVar) {
            this.fR = onRemoveGeofencesResultListener;
            this.fQ = null;
            this.fS = bhVar;
        }

        public void onAddGeofencesResult(int statusCode, String[] geofenceRequestIds) throws RemoteException {
            if (this.fS == null) {
                Log.wtf("LocationClientImpl", "onAddGeofenceResult called multiple times");
                return;
            }
            bh bhVar = this.fS;
            bh bhVar2 = this.fS;
            bhVar2.getClass();
            bhVar.m993a(new C1327a(bhVar2, this.fQ, statusCode, geofenceRequestIds));
            this.fS = null;
            this.fQ = null;
            this.fR = null;
        }

        public void onRemoveGeofencesByPendingIntentResult(int statusCode, PendingIntent pendingIntent) {
            if (this.fS == null) {
                Log.wtf("LocationClientImpl", "onRemoveGeofencesByPendingIntentResult called multiple times");
                return;
            }
            bh bhVar = this.fS;
            bh bhVar2 = this.fS;
            bhVar2.getClass();
            bhVar.m993a(new C1329d(bhVar2, 1, this.fR, statusCode, pendingIntent));
            this.fS = null;
            this.fQ = null;
            this.fR = null;
        }

        public void onRemoveGeofencesByRequestIdsResult(int statusCode, String[] geofenceRequestIds) {
            if (this.fS == null) {
                Log.wtf("LocationClientImpl", "onRemoveGeofencesByRequestIdsResult called multiple times");
                return;
            }
            bh bhVar = this.fS;
            bh bhVar2 = this.fS;
            bhVar2.getClass();
            bhVar.m993a(new C1329d(bhVar2, 2, this.fR, statusCode, geofenceRequestIds));
            this.fS = null;
            this.fQ = null;
            this.fR = null;
        }
    }

    public bh(Context context, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener, String str) {
        super(context, connectionCallbacks, onConnectionFailedListener, new String[0]);
        this.fM = new bg(context, this.fG);
        this.fN = str;
    }

    protected void mo2347a(C0187p c0187p, C1722d c1722d) throws RemoteException {
        Bundle bundle = new Bundle();
        bundle.putString("client_name", this.fN);
        c0187p.mo1332e(c1722d, GooglePlayServicesUtil.GOOGLE_PLAY_SERVICES_VERSION_CODE, getContext().getPackageName(), bundle);
    }

    public void addGeofences(List<bi> geofences, PendingIntent pendingIntent, OnAddGeofencesResultListener listener) {
        m989B();
        boolean z = geofences != null && geofences.size() > 0;
        C0192s.m519b(z, (Object) "At least one geofence must be specified.");
        C0192s.m518b((Object) pendingIntent, (Object) "PendingIntent must be specified.");
        C0192s.m518b((Object) listener, (Object) "OnAddGeofencesResultListener not provided.");
        if (listener == null) {
            be beVar = null;
        } else {
            Object c1706b = new C1706b(listener, this);
        }
        try {
            ((bf) m990C()).mo1234a(geofences, pendingIntent, beVar, getContext().getPackageName());
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    protected String mo2349b() {
        return "com.google.android.location.internal.GoogleLocationManagerService.START";
    }

    protected /* synthetic */ IInterface mo2350c(IBinder iBinder) {
        return m1227s(iBinder);
    }

    protected String mo2351c() {
        return "com.google.android.gms.location.internal.IGoogleLocationManagerService";
    }

    public void disconnect() {
        synchronized (this.fM) {
            if (isConnected()) {
                this.fM.removeAllListeners();
                this.fM.aR();
            }
            super.disconnect();
        }
    }

    public Location getLastLocation() {
        return this.fM.getLastLocation();
    }

    public void removeActivityUpdates(PendingIntent callbackIntent) {
        m989B();
        C0192s.m521d(callbackIntent);
        try {
            ((bf) m990C()).removeActivityUpdates(callbackIntent);
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    public void removeGeofences(PendingIntent pendingIntent, OnRemoveGeofencesResultListener listener) {
        m989B();
        C0192s.m518b((Object) pendingIntent, (Object) "PendingIntent must be specified.");
        C0192s.m518b((Object) listener, (Object) "OnRemoveGeofencesResultListener not provided.");
        if (listener == null) {
            be beVar = null;
        } else {
            Object c1706b = new C1706b(listener, this);
        }
        try {
            ((bf) m990C()).mo1229a(pendingIntent, beVar, getContext().getPackageName());
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    public void removeGeofences(List<String> geofenceRequestIds, OnRemoveGeofencesResultListener listener) {
        m989B();
        boolean z = geofenceRequestIds != null && geofenceRequestIds.size() > 0;
        C0192s.m519b(z, (Object) "geofenceRequestIds can't be null nor empty.");
        C0192s.m518b((Object) listener, (Object) "OnRemoveGeofencesResultListener not provided.");
        String[] strArr = (String[]) geofenceRequestIds.toArray(new String[0]);
        if (listener == null) {
            be beVar = null;
        } else {
            Object c1706b = new C1706b(listener, this);
        }
        try {
            ((bf) m990C()).mo1235a(strArr, beVar, getContext().getPackageName());
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    public void removeLocationUpdates(PendingIntent callbackIntent) {
        this.fM.removeLocationUpdates(callbackIntent);
    }

    public void removeLocationUpdates(LocationListener listener) {
        this.fM.removeLocationUpdates(listener);
    }

    public void requestActivityUpdates(long detectionIntervalMillis, PendingIntent callbackIntent) {
        boolean z = true;
        m989B();
        C0192s.m521d(callbackIntent);
        if (detectionIntervalMillis < 0) {
            z = false;
        }
        C0192s.m519b(z, (Object) "detectionIntervalMillis must be >= 0");
        try {
            ((bf) m990C()).mo1227a(detectionIntervalMillis, true, callbackIntent);
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    public void requestLocationUpdates(LocationRequest request, PendingIntent callbackIntent) {
        this.fM.requestLocationUpdates(request, callbackIntent);
    }

    public void requestLocationUpdates(LocationRequest request, LocationListener listener) {
        requestLocationUpdates(request, listener, null);
    }

    public void requestLocationUpdates(LocationRequest request, LocationListener listener, Looper looper) {
        synchronized (this.fM) {
            this.fM.requestLocationUpdates(request, listener, looper);
        }
    }

    protected bf m1227s(IBinder iBinder) {
        return C1326a.m861r(iBinder);
    }

    public void setMockLocation(Location mockLocation) {
        this.fM.setMockLocation(mockLocation);
    }

    public void setMockMode(boolean isMockMode) {
        this.fM.setMockMode(isMockMode);
    }
}
