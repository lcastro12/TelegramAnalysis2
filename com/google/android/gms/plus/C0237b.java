package com.google.android.gms.plus;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.C0141a;
import com.google.android.gms.common.internal.safeparcel.C0141a.C0140a;
import com.google.android.gms.common.internal.safeparcel.C0142b;
import com.google.android.gms.location.LocationStatusCodes;

public class C0237b implements Creator<C1433a> {
    static void m605a(C1433a c1433a, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        C0142b.m119a(parcel, 1, c1433a.getAccountName(), false);
        C0142b.m129c(parcel, LocationStatusCodes.GEOFENCE_NOT_AVAILABLE, c1433a.m1126i());
        C0142b.m125a(parcel, 2, c1433a.by(), false);
        C0142b.m125a(parcel, 3, c1433a.bz(), false);
        C0142b.m125a(parcel, 4, c1433a.bA(), false);
        C0142b.m119a(parcel, 5, c1433a.bB(), false);
        C0142b.m119a(parcel, 6, c1433a.bC(), false);
        C0142b.m119a(parcel, 7, c1433a.bD(), false);
        C0142b.m110C(parcel, d);
    }

    public C1433a[] m606U(int i) {
        return new C1433a[i];
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return m607u(x0);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return m606U(x0);
    }

    public C1433a m607u(Parcel parcel) {
        String str = null;
        int c = C0141a.m81c(parcel);
        int i = 0;
        String str2 = null;
        String str3 = null;
        String[] strArr = null;
        String[] strArr2 = null;
        String[] strArr3 = null;
        String str4 = null;
        while (parcel.dataPosition() < c) {
            int b = C0141a.m78b(parcel);
            switch (C0141a.m93m(b)) {
                case 1:
                    str4 = C0141a.m92l(parcel, b);
                    break;
                case 2:
                    strArr3 = C0141a.m104w(parcel, b);
                    break;
                case 3:
                    strArr2 = C0141a.m104w(parcel, b);
                    break;
                case 4:
                    strArr = C0141a.m104w(parcel, b);
                    break;
                case 5:
                    str3 = C0141a.m92l(parcel, b);
                    break;
                case 6:
                    str2 = C0141a.m92l(parcel, b);
                    break;
                case 7:
                    str = C0141a.m92l(parcel, b);
                    break;
                case LocationStatusCodes.GEOFENCE_NOT_AVAILABLE /*1000*/:
                    i = C0141a.m86f(parcel, b);
                    break;
                default:
                    C0141a.m79b(parcel, b);
                    break;
            }
        }
        if (parcel.dataPosition() == c) {
            return new C1433a(i, str4, strArr3, strArr2, strArr, str3, str2, str);
        }
        throw new C0140a("Overread allowed size end=" + c, parcel);
    }
}
