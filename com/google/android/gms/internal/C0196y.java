package com.google.android.gms.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.C0141a;
import com.google.android.gms.common.internal.safeparcel.C0141a.C0140a;
import com.google.android.gms.common.internal.safeparcel.C0142b;
import com.google.android.gms.location.LocationStatusCodes;

public class C0196y implements Creator<C1362x> {
    static void m529a(C1362x c1362x, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        C0142b.m129c(parcel, 1, c1362x.getType());
        C0142b.m129c(parcel, LocationStatusCodes.GEOFENCE_NOT_AVAILABLE, c1362x.m1031i());
        C0142b.m129c(parcel, 2, c1362x.m1025I());
        C0142b.m119a(parcel, 3, c1362x.m1026J(), false);
        C0142b.m119a(parcel, 4, c1362x.m1027K(), false);
        C0142b.m119a(parcel, 5, c1362x.getDisplayName(), false);
        C0142b.m119a(parcel, 6, c1362x.m1028L(), false);
        C0142b.m110C(parcel, d);
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return m530e(x0);
    }

    public C1362x m530e(Parcel parcel) {
        int i = 0;
        String str = null;
        int c = C0141a.m81c(parcel);
        String str2 = null;
        String str3 = null;
        String str4 = null;
        int i2 = 0;
        int i3 = 0;
        while (parcel.dataPosition() < c) {
            int b = C0141a.m78b(parcel);
            switch (C0141a.m93m(b)) {
                case 1:
                    i2 = C0141a.m86f(parcel, b);
                    break;
                case 2:
                    i = C0141a.m86f(parcel, b);
                    break;
                case 3:
                    str4 = C0141a.m92l(parcel, b);
                    break;
                case 4:
                    str3 = C0141a.m92l(parcel, b);
                    break;
                case 5:
                    str2 = C0141a.m92l(parcel, b);
                    break;
                case 6:
                    str = C0141a.m92l(parcel, b);
                    break;
                case LocationStatusCodes.GEOFENCE_NOT_AVAILABLE /*1000*/:
                    i3 = C0141a.m86f(parcel, b);
                    break;
                default:
                    C0141a.m79b(parcel, b);
                    break;
            }
        }
        if (parcel.dataPosition() == c) {
            return new C1362x(i3, i2, i, str4, str3, str2, str);
        }
        throw new C0140a("Overread allowed size end=" + c, parcel);
    }

    public C1362x[] m531n(int i) {
        return new C1362x[i];
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return m531n(x0);
    }
}
