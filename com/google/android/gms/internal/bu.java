package com.google.android.gms.internal;

import android.content.Context;
import android.os.IBinder;
import android.view.View;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.dynamic.C1689c;
import com.google.android.gms.internal.bq.C1339a;
import com.google.android.gms.plus.PlusOneDummyView;

public final class bu {
    private static Context gN;
    private static bq iy;

    public static class C0166a extends Exception {
        public C0166a(String str) {
            super(str);
        }
    }

    public static View m409a(Context context, int i, int i2, String str, int i3) {
        if (str != null) {
            return (View) C1689c.m1134a(m411m(context).mo1262a(C1689c.m1135f(context), i, i2, str, i3));
        }
        try {
            throw new NullPointerException();
        } catch (Exception e) {
            return new PlusOneDummyView(context, i);
        }
    }

    public static View m410a(Context context, int i, int i2, String str, String str2) {
        if (str != null) {
            return (View) C1689c.m1134a(m411m(context).mo1263a(C1689c.m1135f(context), i, i2, str, str2));
        }
        try {
            throw new NullPointerException();
        } catch (Exception e) {
            return new PlusOneDummyView(context, i);
        }
    }

    private static bq m411m(Context context) throws C0166a {
        C0192s.m521d(context);
        if (iy == null) {
            if (gN == null) {
                gN = GooglePlayServicesUtil.getRemoteContext(context);
                if (gN == null) {
                    throw new C0166a("Could not get remote context.");
                }
            }
            try {
                iy = C1339a.m907Z((IBinder) gN.getClassLoader().loadClass("com.google.android.gms.plus.plusone.PlusOneButtonCreatorImpl").newInstance());
            } catch (ClassNotFoundException e) {
                throw new C0166a("Could not load creator class.");
            } catch (InstantiationException e2) {
                throw new C0166a("Could not instantiate creator.");
            } catch (IllegalAccessException e3) {
                throw new C0166a("Could not access creator.");
            }
        }
        return iy;
    }
}
