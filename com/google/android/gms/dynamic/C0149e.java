package com.google.android.gms.dynamic;

import android.content.Context;
import android.os.IBinder;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.internal.C0192s;

public abstract class C0149e<T> {
    private final String dd;
    private T de;

    public static class C0148a extends Exception {
        public C0148a(String str) {
            super(str);
        }

        public C0148a(String str, Throwable th) {
            super(str, th);
        }
    }

    protected C0149e(String str) {
        this.dd = str;
    }

    protected final T m142h(Context context) throws C0148a {
        if (this.de == null) {
            C0192s.m521d(context);
            Context remoteContext = GooglePlayServicesUtil.getRemoteContext(context);
            if (remoteContext == null) {
                throw new C0148a("Could not get remote context.");
            }
            try {
                this.de = mo1337k((IBinder) remoteContext.getClassLoader().loadClass(this.dd).newInstance());
            } catch (ClassNotFoundException e) {
                throw new C0148a("Could not load creator class.");
            } catch (InstantiationException e2) {
                throw new C0148a("Could not instantiate creator.");
            } catch (IllegalAccessException e3) {
                throw new C0148a("Could not access creator.");
            }
        }
        return this.de;
    }

    protected abstract T mo1337k(IBinder iBinder);
}
