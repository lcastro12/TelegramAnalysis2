package com.google.android.gms.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.C0141a;
import com.google.android.gms.common.internal.safeparcel.C0141a.C0140a;
import com.google.android.gms.common.internal.safeparcel.C0142b;
import com.google.android.gms.location.LocationStatusCodes;
import java.util.ArrayList;

public class bw implements Creator<bv> {
    static void m412a(bv bvVar, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        C0142b.m119a(parcel, 1, bvVar.getDescription(), false);
        C0142b.m129c(parcel, LocationStatusCodes.GEOFENCE_NOT_AVAILABLE, bvVar.m946i());
        C0142b.m128b(parcel, 2, bvVar.bE(), false);
        C0142b.m128b(parcel, 3, bvVar.bF(), false);
        C0142b.m122a(parcel, 4, bvVar.bG());
        C0142b.m110C(parcel, d);
    }

    public bv[] m413V(int i) {
        return new bv[i];
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return m414v(x0);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return m413V(x0);
    }

    public bv m414v(Parcel parcel) {
        boolean z = false;
        ArrayList arrayList = null;
        int c = C0141a.m81c(parcel);
        ArrayList arrayList2 = null;
        String str = null;
        int i = 0;
        while (parcel.dataPosition() < c) {
            int b = C0141a.m78b(parcel);
            switch (C0141a.m93m(b)) {
                case 1:
                    str = C0141a.m92l(parcel, b);
                    break;
                case 2:
                    arrayList2 = C0141a.m82c(parcel, b, C1362x.CREATOR);
                    break;
                case 3:
                    arrayList = C0141a.m82c(parcel, b, C1362x.CREATOR);
                    break;
                case 4:
                    z = C0141a.m83c(parcel, b);
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
            return new bv(i, str, arrayList2, arrayList, z);
        }
        throw new C0140a("Overread allowed size end=" + c, parcel);
    }
}
