package com.google.android.gms.maps.internal;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.google.android.gms.dynamic.C0146b;
import com.google.android.gms.dynamic.C0146b.C1296a;
import com.google.android.gms.maps.model.internal.C0228d;
import com.google.android.gms.maps.model.internal.C0228d.C1423a;

public interface C0202d extends IInterface {

    public static abstract class C1392a extends Binder implements C0202d {

        private static class C1391a implements C0202d {
            private IBinder f110a;

            C1391a(IBinder iBinder) {
                this.f110a = iBinder;
            }

            public IBinder asBinder() {
                return this.f110a;
            }

            public C0146b mo1453f(C0228d c0228d) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.maps.internal.IInfoWindowAdapter");
                    obtain.writeStrongBinder(c0228d != null ? c0228d.asBinder() : null);
                    this.f110a.transact(1, obtain, obtain2, 0);
                    obtain2.readException();
                    C0146b l = C1296a.m652l(obtain2.readStrongBinder());
                    return l;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public C0146b mo1454g(C0228d c0228d) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.maps.internal.IInfoWindowAdapter");
                    obtain.writeStrongBinder(c0228d != null ? c0228d.asBinder() : null);
                    this.f110a.transact(2, obtain, obtain2, 0);
                    obtain2.readException();
                    C0146b l = C1296a.m652l(obtain2.readStrongBinder());
                    return l;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }

        public C1392a() {
            attachInterface(this, "com.google.android.gms.maps.internal.IInfoWindowAdapter");
        }

        public static C0202d m1066x(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface("com.google.android.gms.maps.internal.IInfoWindowAdapter");
            return (queryLocalInterface == null || !(queryLocalInterface instanceof C0202d)) ? new C1391a(iBinder) : (C0202d) queryLocalInterface;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            IBinder iBinder = null;
            C0146b f;
            switch (code) {
                case 1:
                    data.enforceInterface("com.google.android.gms.maps.internal.IInfoWindowAdapter");
                    f = mo1453f(C1423a.m1120Q(data.readStrongBinder()));
                    reply.writeNoException();
                    if (f != null) {
                        iBinder = f.asBinder();
                    }
                    reply.writeStrongBinder(iBinder);
                    return true;
                case 2:
                    data.enforceInterface("com.google.android.gms.maps.internal.IInfoWindowAdapter");
                    f = mo1454g(C1423a.m1120Q(data.readStrongBinder()));
                    reply.writeNoException();
                    if (f != null) {
                        iBinder = f.asBinder();
                    }
                    reply.writeStrongBinder(iBinder);
                    return true;
                case 1598968902:
                    reply.writeString("com.google.android.gms.maps.internal.IInfoWindowAdapter");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    C0146b mo1453f(C0228d c0228d) throws RemoteException;

    C0146b mo1454g(C0228d c0228d) throws RemoteException;
}
