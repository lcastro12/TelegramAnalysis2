package com.google.android.gms.internal;

import android.os.Build.VERSION;

public final class as {
    public static boolean an() {
        return m219x(11);
    }

    public static boolean ao() {
        return m219x(12);
    }

    public static boolean ap() {
        return m219x(13);
    }

    public static boolean aq() {
        return m219x(14);
    }

    public static boolean ar() {
        return m219x(16);
    }

    public static boolean as() {
        return m219x(17);
    }

    private static boolean m219x(int i) {
        return VERSION.SDK_INT >= i;
    }
}
