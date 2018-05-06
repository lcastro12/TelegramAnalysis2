package com.google.android.gms.internal;

public final class C0192s {
    public static void m515a(boolean z) {
        if (!z) {
            throw new IllegalStateException();
        }
    }

    public static void m516a(boolean z, Object obj) {
        if (!z) {
            throw new IllegalStateException(String.valueOf(obj));
        }
    }

    public static void m517a(boolean z, String str, Object... objArr) {
        if (!z) {
            throw new IllegalArgumentException(String.format(str, objArr));
        }
    }

    public static <T> T m518b(T t, Object obj) {
        if (t != null) {
            return t;
        }
        throw new NullPointerException(String.valueOf(obj));
    }

    public static void m519b(boolean z, Object obj) {
        if (!z) {
            throw new IllegalArgumentException(String.valueOf(obj));
        }
    }

    public static void m520c(boolean z) {
        if (!z) {
            throw new IllegalArgumentException();
        }
    }

    public static <T> T m521d(T t) {
        if (t != null) {
            return t;
        }
        throw new NullPointerException("null reference");
    }
}
