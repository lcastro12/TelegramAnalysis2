package com.google.android.gms.internal;

import android.content.Context;
import android.os.IBinder;
import android.view.View;
import com.google.android.gms.dynamic.C0149e;
import com.google.android.gms.dynamic.C0149e.C0148a;
import com.google.android.gms.dynamic.C1689c;
import com.google.android.gms.internal.C0188q.C1360a;

public final class C1361t extends C0149e<C0188q> {
    private static final C1361t ca = new C1361t();

    private C1361t() {
        super("com.google.android.gms.common.ui.SignInButtonCreatorImpl");
    }

    public static View m1021d(Context context, int i, int i2) throws C0148a {
        return ca.m1022e(context, i, i2);
    }

    private View m1022e(Context context, int i, int i2) throws C0148a {
        try {
            return (View) C1689c.m1134a(((C0188q) m142h(context)).mo1336a(C1689c.m1135f(context), i, i2));
        } catch (Throwable e) {
            throw new C0148a("Could not get button with size " + i + " and color " + i2, e);
        }
    }

    public C0188q m1023j(IBinder iBinder) {
        return C1360a.m1020i(iBinder);
    }

    public /* synthetic */ Object mo1337k(IBinder iBinder) {
        return m1023j(iBinder);
    }
}
