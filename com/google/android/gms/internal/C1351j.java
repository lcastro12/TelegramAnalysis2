package com.google.android.gms.internal;

import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

public abstract class C1351j implements SafeParcelable {
    private static final Object bo = new Object();
    private static ClassLoader bp = null;
    private static Integer bq = null;
    private boolean br = false;

    private static boolean m967a(Class<?> cls) {
        boolean z = false;
        try {
            z = SafeParcelable.NULL.equals(cls.getField("NULL").get(null));
        } catch (NoSuchFieldException e) {
        } catch (IllegalAccessException e2) {
        }
        return z;
    }

    protected static boolean m968h(String str) {
        ClassLoader u = C1351j.m969u();
        if (u == null) {
            return true;
        }
        try {
            return C1351j.m967a(u.loadClass(str));
        } catch (Exception e) {
            return false;
        }
    }

    protected static ClassLoader m969u() {
        ClassLoader classLoader;
        synchronized (bo) {
            classLoader = bp;
        }
        return classLoader;
    }

    protected static Integer m970v() {
        Integer num;
        synchronized (bo) {
            num = bq;
        }
        return num;
    }

    protected boolean m971w() {
        return this.br;
    }
}
