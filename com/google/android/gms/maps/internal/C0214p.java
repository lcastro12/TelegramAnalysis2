package com.google.android.gms.maps.internal;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.dynamic.C1689c;
import com.google.android.gms.internal.C0192s;
import com.google.android.gms.maps.internal.C0201c.C1390a;
import com.google.android.gms.maps.model.RuntimeRemoteException;

public class C0214p {
    private static Context gN;
    private static C0201c gO;

    private static <T> T m555a(ClassLoader classLoader, String str) {
        try {
            return C0214p.m556c(((ClassLoader) C0192s.m521d(classLoader)).loadClass(str));
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Unable to find dynamic class " + str);
        }
    }

    private static Class<?> bm() {
        try {
            return Class.forName("com.google.android.gms.maps.internal.CreatorImpl");
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private static <T> T m556c(Class<?> cls) {
        try {
            return cls.newInstance();
        } catch (InstantiationException e) {
            throw new IllegalStateException("Unable to instantiate the dynamic class " + cls.getName());
        } catch (IllegalAccessException e2) {
            throw new IllegalStateException("Unable to call the default constructor of " + cls.getName());
        }
    }

    private static Context getRemoteContext(Context context) {
        if (gN == null) {
            if (C0214p.bm() != null) {
                gN = context;
            } else {
                gN = GooglePlayServicesUtil.getRemoteContext(context);
            }
        }
        return gN;
    }

    public static C0201c m557i(Context context) throws GooglePlayServicesNotAvailableException {
        C0192s.m521d(context);
        C0214p.m559k(context);
        if (gO == null) {
            C0214p.m560l(context);
        }
        if (gO != null) {
            return gO;
        }
        gO = C1390a.m1063v((IBinder) C0214p.m555a(C0214p.getRemoteContext(context).getClassLoader(), "com.google.android.gms.maps.internal.CreatorImpl"));
        C0214p.m558j(context);
        return gO;
    }

    private static void m558j(Context context) {
        try {
            gO.mo1448a(C1689c.m1135f(C0214p.getRemoteContext(context).getResources()), (int) GooglePlayServicesUtil.GOOGLE_PLAY_SERVICES_VERSION_CODE);
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    public static void m559k(Context context) throws GooglePlayServicesNotAvailableException {
        int isGooglePlayServicesAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        if (isGooglePlayServicesAvailable != 0) {
            throw new GooglePlayServicesNotAvailableException(isGooglePlayServicesAvailable);
        }
    }

    private static void m560l(Context context) {
        Class bm = C0214p.bm();
        if (bm != null) {
            Log.i(C0214p.class.getSimpleName(), "Making Creator statically");
            gO = (C0201c) C0214p.m556c(bm);
            C0214p.m558j(context);
        }
    }
}
