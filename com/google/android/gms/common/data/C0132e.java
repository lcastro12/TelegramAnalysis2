package com.google.android.gms.common.data;

import android.database.CursorWindow;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.C0141a;
import com.google.android.gms.common.internal.safeparcel.C0141a.C0140a;
import com.google.android.gms.common.internal.safeparcel.C0142b;
import com.google.android.gms.location.LocationStatusCodes;

public class C0132e implements Creator<C1287d> {
    static void m39a(C1287d c1287d, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        C0142b.m125a(parcel, 1, c1287d.m637j(), false);
        C0142b.m129c(parcel, LocationStatusCodes.GEOFENCE_NOT_AVAILABLE, c1287d.m636i());
        C0142b.m124a(parcel, 2, c1287d.m638k(), i, false);
        C0142b.m129c(parcel, 3, c1287d.getStatusCode());
        C0142b.m115a(parcel, 4, c1287d.m639l(), false);
        C0142b.m110C(parcel, d);
    }

    public C1287d m40a(Parcel parcel) {
        int i = 0;
        Bundle bundle = null;
        int c = C0141a.m81c(parcel);
        CursorWindow[] cursorWindowArr = null;
        String[] strArr = null;
        int i2 = 0;
        while (parcel.dataPosition() < c) {
            int b = C0141a.m78b(parcel);
            switch (C0141a.m93m(b)) {
                case 1:
                    strArr = C0141a.m104w(parcel, b);
                    break;
                case 2:
                    cursorWindowArr = (CursorWindow[]) C0141a.m80b(parcel, b, CursorWindow.CREATOR);
                    break;
                case 3:
                    i = C0141a.m86f(parcel, b);
                    break;
                case 4:
                    bundle = C0141a.m95n(parcel, b);
                    break;
                case LocationStatusCodes.GEOFENCE_NOT_AVAILABLE /*1000*/:
                    i2 = C0141a.m86f(parcel, b);
                    break;
                default:
                    C0141a.m79b(parcel, b);
                    break;
            }
        }
        if (parcel.dataPosition() != c) {
            throw new C0140a("Overread allowed size end=" + c, parcel);
        }
        C1287d c1287d = new C1287d(i2, strArr, cursorWindowArr, i, bundle);
        c1287d.m635h();
        return c1287d;
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return m40a(x0);
    }

    public C1287d[] m41g(int i) {
        return new C1287d[i];
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return m41g(x0);
    }
}
