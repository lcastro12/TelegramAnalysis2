package com.google.android.gms.internal;

import android.util.Log;

public final class C0185n {
    private final String bX;

    public C0185n(String str) {
        this.bX = (String) C0192s.m521d(str);
    }

    public void m491a(String str, String str2) {
        if (m496l(3)) {
            Log.d(str, str2);
        }
    }

    public void m492a(String str, String str2, Throwable th) {
        if (m496l(6)) {
            Log.e(str, str2, th);
        }
    }

    public void m493b(String str, String str2) {
        if (m496l(5)) {
            Log.w(str, str2);
        }
    }

    public void m494c(String str, String str2) {
        if (m496l(6)) {
            Log.e(str, str2);
        }
    }

    public void m495d(String str, String str2) {
        if (!m496l(4)) {
        }
    }

    public boolean m496l(int i) {
        return Log.isLoggable(this.bX, i);
    }
}
