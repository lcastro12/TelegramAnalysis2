package com.google.android.gms.internal;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.C0141a;
import com.google.android.gms.common.internal.safeparcel.C0141a.C0140a;
import com.google.android.gms.common.internal.safeparcel.C0142b;
import com.google.android.gms.location.LocationStatusCodes;
import java.util.ArrayList;

public class cr implements Creator<cq> {
    static void m446a(cq cqVar, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        C0142b.m129c(parcel, LocationStatusCodes.GEOFENCE_NOT_AVAILABLE, cqVar.m954i());
        C0142b.m128b(parcel, 2, cqVar.cK(), false);
        C0142b.m128b(parcel, 3, cqVar.cL(), false);
        C0142b.m115a(parcel, 4, cqVar.cM(), false);
        C0142b.m122a(parcel, 5, cqVar.cN());
        C0142b.m129c(parcel, 6, cqVar.cJ());
        C0142b.m110C(parcel, d);
    }

    public cq m447J(Parcel parcel) {
        Bundle bundle = null;
        int i = 0;
        int c = C0141a.m81c(parcel);
        boolean z = false;
        ArrayList arrayList = null;
        ArrayList arrayList2 = null;
        int i2 = 0;
        while (parcel.dataPosition() < c) {
            int b = C0141a.m78b(parcel);
            switch (C0141a.m93m(b)) {
                case 2:
                    arrayList2 = C0141a.m82c(parcel, b, C1362x.CREATOR);
                    break;
                case 3:
                    arrayList = C0141a.m82c(parcel, b, C1362x.CREATOR);
                    break;
                case 4:
                    bundle = C0141a.m95n(parcel, b);
                    break;
                case 5:
                    z = C0141a.m83c(parcel, b);
                    break;
                case 6:
                    i = C0141a.m86f(parcel, b);
                    break;
                case LocationStatusCodes.GEOFENCE_NOT_AVAILABLE /*1000*/:
                    i2 = C0141a.m86f(parcel, b);
                    break;
                default:
                    C0141a.m79b(parcel, b);
                    break;
            }
        }
        if (parcel.dataPosition() == c) {
            return new cq(i2, arrayList2, arrayList, bundle, z, i);
        }
        throw new C0140a("Overread allowed size end=" + c, parcel);
    }

    public cq[] aj(int i) {
        return new cq[i];
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return m447J(x0);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return aj(x0);
    }
}
