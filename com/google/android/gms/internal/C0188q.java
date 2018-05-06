package com.google.android.gms.internal;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.google.android.gms.dynamic.C0146b;
import com.google.android.gms.dynamic.C0146b.C1296a;

public interface C0188q extends IInterface {

    public static abstract class C1360a extends Binder implements C0188q {

        private static class C1359a implements C0188q {
            private IBinder f99a;

            C1359a(IBinder iBinder) {
                this.f99a = iBinder;
            }

            public C0146b mo1336a(C0146b c0146b, int i, int i2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.common.internal.ISignInButtonCreator");
                    obtain.writeStrongBinder(c0146b != null ? c0146b.asBinder() : null);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    this.f99a.transact(1, obtain, obtain2, 0);
                    obtain2.readException();
                    C0146b l = C1296a.m652l(obtain2.readStrongBinder());
                    return l;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public IBinder asBinder() {
                return this.f99a;
            }
        }

        public static C0188q m1020i(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface("com.google.android.gms.common.internal.ISignInButtonCreator");
            return (queryLocalInterface == null || !(queryLocalInterface instanceof C0188q)) ? new C1359a(iBinder) : (C0188q) queryLocalInterface;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface("com.google.android.gms.common.internal.ISignInButtonCreator");
                    C0146b a = mo1336a(C1296a.m652l(data.readStrongBinder()), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeStrongBinder(a != null ? a.asBinder() : null);
                    return true;
                case 1598968902:
                    reply.writeString("com.google.android.gms.common.internal.ISignInButtonCreator");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    C0146b mo1336a(C0146b c0146b, int i, int i2) throws RemoteException;
}
