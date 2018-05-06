package com.google.android.gms.maps.internal;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.google.android.gms.dynamic.C0146b;
import com.google.android.gms.dynamic.C0146b.C1296a;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.internal.ICameraUpdateFactoryDelegate.C1374a;
import com.google.android.gms.maps.internal.IMapFragmentDelegate.C1380a;
import com.google.android.gms.maps.internal.IMapViewDelegate.C1382a;
import com.google.android.gms.maps.model.internal.C0225a;
import com.google.android.gms.maps.model.internal.C0225a.C1417a;

public interface C0201c extends IInterface {

    public static abstract class C1390a extends Binder implements C0201c {

        private static class C1389a implements C0201c {
            private IBinder f109a;

            C1389a(IBinder iBinder) {
                this.f109a = iBinder;
            }

            public IMapViewDelegate mo1447a(C0146b c0146b, GoogleMapOptions googleMapOptions) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.maps.internal.ICreator");
                    obtain.writeStrongBinder(c0146b != null ? c0146b.asBinder() : null);
                    if (googleMapOptions != null) {
                        obtain.writeInt(1);
                        googleMapOptions.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    this.f109a.transact(3, obtain, obtain2, 0);
                    obtain2.readException();
                    IMapViewDelegate A = C1382a.m1055A(obtain2.readStrongBinder());
                    return A;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void mo1448a(C0146b c0146b, int i) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.maps.internal.ICreator");
                    obtain.writeStrongBinder(c0146b != null ? c0146b.asBinder() : null);
                    obtain.writeInt(i);
                    this.f109a.transact(6, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public IBinder asBinder() {
                return this.f109a;
            }

            public ICameraUpdateFactoryDelegate bk() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.maps.internal.ICreator");
                    this.f109a.transact(4, obtain, obtain2, 0);
                    obtain2.readException();
                    ICameraUpdateFactoryDelegate t = C1374a.m1051t(obtain2.readStrongBinder());
                    return t;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public C0225a bl() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.maps.internal.ICreator");
                    this.f109a.transact(5, obtain, obtain2, 0);
                    obtain2.readException();
                    C0225a N = C1417a.m1112N(obtain2.readStrongBinder());
                    return N;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void mo1451c(C0146b c0146b) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.maps.internal.ICreator");
                    obtain.writeStrongBinder(c0146b != null ? c0146b.asBinder() : null);
                    this.f109a.transact(1, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public IMapFragmentDelegate mo1452d(C0146b c0146b) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.maps.internal.ICreator");
                    obtain.writeStrongBinder(c0146b != null ? c0146b.asBinder() : null);
                    this.f109a.transact(2, obtain, obtain2, 0);
                    obtain2.readException();
                    IMapFragmentDelegate z = C1380a.m1054z(obtain2.readStrongBinder());
                    return z;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }

        public static C0201c m1063v(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface("com.google.android.gms.maps.internal.ICreator");
            return (queryLocalInterface == null || !(queryLocalInterface instanceof C0201c)) ? new C1389a(iBinder) : (C0201c) queryLocalInterface;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            IBinder iBinder = null;
            switch (code) {
                case 1:
                    data.enforceInterface("com.google.android.gms.maps.internal.ICreator");
                    mo1451c(C1296a.m652l(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 2:
                    data.enforceInterface("com.google.android.gms.maps.internal.ICreator");
                    IMapFragmentDelegate d = mo1452d(C1296a.m652l(data.readStrongBinder()));
                    reply.writeNoException();
                    if (d != null) {
                        iBinder = d.asBinder();
                    }
                    reply.writeStrongBinder(iBinder);
                    return true;
                case 3:
                    data.enforceInterface("com.google.android.gms.maps.internal.ICreator");
                    IMapViewDelegate a = mo1447a(C1296a.m652l(data.readStrongBinder()), data.readInt() != 0 ? GoogleMapOptions.CREATOR.createFromParcel(data) : null);
                    reply.writeNoException();
                    if (a != null) {
                        iBinder = a.asBinder();
                    }
                    reply.writeStrongBinder(iBinder);
                    return true;
                case 4:
                    data.enforceInterface("com.google.android.gms.maps.internal.ICreator");
                    ICameraUpdateFactoryDelegate bk = bk();
                    reply.writeNoException();
                    if (bk != null) {
                        iBinder = bk.asBinder();
                    }
                    reply.writeStrongBinder(iBinder);
                    return true;
                case 5:
                    data.enforceInterface("com.google.android.gms.maps.internal.ICreator");
                    C0225a bl = bl();
                    reply.writeNoException();
                    if (bl != null) {
                        iBinder = bl.asBinder();
                    }
                    reply.writeStrongBinder(iBinder);
                    return true;
                case 6:
                    data.enforceInterface("com.google.android.gms.maps.internal.ICreator");
                    mo1448a(C1296a.m652l(data.readStrongBinder()), data.readInt());
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString("com.google.android.gms.maps.internal.ICreator");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    IMapViewDelegate mo1447a(C0146b c0146b, GoogleMapOptions googleMapOptions) throws RemoteException;

    void mo1448a(C0146b c0146b, int i) throws RemoteException;

    ICameraUpdateFactoryDelegate bk() throws RemoteException;

    C0225a bl() throws RemoteException;

    void mo1451c(C0146b c0146b) throws RemoteException;

    IMapFragmentDelegate mo1452d(C0146b c0146b) throws RemoteException;
}
