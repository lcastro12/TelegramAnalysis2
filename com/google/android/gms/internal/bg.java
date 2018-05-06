package com.google.android.gms.internal;

import android.app.PendingIntent;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.google.android.gms.location.C0197a;
import com.google.android.gms.location.C0197a.C1365a;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import java.util.HashMap;

public class bg {
    private final bk<bf> fG;
    private ContentProviderClient fH = null;
    private boolean fI = false;
    private HashMap<LocationListener, C1705b> fJ = new HashMap();
    private final ContentResolver mContentResolver;

    private static class C0164a extends Handler {
        private final LocationListener fK;

        public C0164a(LocationListener locationListener) {
            this.fK = locationListener;
        }

        public C0164a(LocationListener locationListener, Looper looper) {
            super(looper);
            this.fK = locationListener;
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    this.fK.onLocationChanged(new Location((Location) msg.obj));
                    return;
                default:
                    Log.e("LocationClientHelper", "unknown message in LocationHandler.handleMessage");
                    return;
            }
        }
    }

    private static class C1705b extends C1365a {
        private Handler fL;

        C1705b(LocationListener locationListener, Looper looper) {
            this.fL = looper == null ? new C0164a(locationListener) : new C0164a(locationListener, looper);
        }

        public void onLocationChanged(Location location) {
            if (this.fL == null) {
                Log.e("LocationClientHelper", "Received a location in client after calling removeLocationUpdates.");
                return;
            }
            Message obtain = Message.obtain();
            obtain.what = 1;
            obtain.obj = location;
            this.fL.sendMessage(obtain);
        }

        public void release() {
            this.fL = null;
        }
    }

    public bg(Context context, bk<bf> bkVar) {
        this.fG = bkVar;
        this.mContentResolver = context.getContentResolver();
    }

    public void aR() {
        if (this.fI) {
            setMockMode(false);
        }
    }

    public Location getLastLocation() {
        this.fG.mo1240B();
        try {
            return ((bf) this.fG.mo1241C()).aQ();
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    public void removeAllListeners() {
        try {
            synchronized (this.fJ) {
                for (C0197a c0197a : this.fJ.values()) {
                    if (c0197a != null) {
                        ((bf) this.fG.mo1241C()).mo1233a(c0197a);
                    }
                }
                this.fJ.clear();
            }
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    public void removeLocationUpdates(PendingIntent callbackIntent) {
        this.fG.mo1240B();
        try {
            ((bf) this.fG.mo1241C()).mo1228a(callbackIntent);
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    public void removeLocationUpdates(LocationListener listener) {
        this.fG.mo1240B();
        C0192s.m518b((Object) listener, (Object) "Invalid null listener");
        synchronized (this.fJ) {
            C0197a c0197a = (C1705b) this.fJ.remove(listener);
            if (this.fH != null && this.fJ.isEmpty()) {
                this.fH.release();
                this.fH = null;
            }
            if (c0197a != null) {
                c0197a.release();
                try {
                    ((bf) this.fG.mo1241C()).mo1233a(c0197a);
                } catch (Throwable e) {
                    throw new IllegalStateException(e);
                }
            }
        }
    }

    public void requestLocationUpdates(LocationRequest request, PendingIntent callbackIntent) {
        this.fG.mo1240B();
        try {
            ((bf) this.fG.mo1241C()).mo1231a(request, callbackIntent);
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    public void requestLocationUpdates(LocationRequest request, LocationListener listener, Looper looper) {
        this.fG.mo1240B();
        if (looper == null) {
            C0192s.m518b(Looper.myLooper(), (Object) "Can't create handler inside thread that has not called Looper.prepare()");
        }
        synchronized (this.fJ) {
            C0197a c1705b;
            C1705b c1705b2 = (C1705b) this.fJ.get(listener);
            if (c1705b2 == null) {
                c1705b = new C1705b(listener, looper);
            } else {
                Object obj = c1705b2;
            }
            this.fJ.put(listener, c1705b);
            try {
                ((bf) this.fG.mo1241C()).mo1232a(request, c1705b);
            } catch (Throwable e) {
                throw new IllegalStateException(e);
            }
        }
    }

    public void setMockLocation(Location mockLocation) {
        this.fG.mo1240B();
        try {
            ((bf) this.fG.mo1241C()).setMockLocation(mockLocation);
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    public void setMockMode(boolean isMockMode) {
        this.fG.mo1240B();
        try {
            ((bf) this.fG.mo1241C()).setMockMode(isMockMode);
            this.fI = isMockMode;
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }
}
