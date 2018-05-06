package com.google.android.gms.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.C0141a;
import com.google.android.gms.common.internal.safeparcel.C0141a.C0140a;
import com.google.android.gms.common.internal.safeparcel.C0142b;
import com.google.android.gms.internal.cc.C1713a;
import java.util.HashSet;
import java.util.Set;

public class ce implements Creator<C1713a> {
    static void m425a(C1713a c1713a, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        Set bH = c1713a.bH();
        if (bH.contains(Integer.valueOf(1))) {
            C0142b.m129c(parcel, 1, c1713a.m1291i());
        }
        if (bH.contains(Integer.valueOf(2))) {
            C0142b.m129c(parcel, 2, c1713a.getMax());
        }
        if (bH.contains(Integer.valueOf(3))) {
            C0142b.m129c(parcel, 3, c1713a.getMin());
        }
        C0142b.m110C(parcel, d);
    }

    public C1713a[] m426Z(int i) {
        return new C1713a[i];
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return m427z(x0);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return m426Z(x0);
    }

    public C1713a m427z(Parcel parcel) {
        int i = 0;
        int c = C0141a.m81c(parcel);
        Set hashSet = new HashSet();
        int i2 = 0;
        int i3 = 0;
        while (parcel.dataPosition() < c) {
            int b = C0141a.m78b(parcel);
            switch (C0141a.m93m(b)) {
                case 1:
                    i3 = C0141a.m86f(parcel, b);
                    hashSet.add(Integer.valueOf(1));
                    break;
                case 2:
                    i2 = C0141a.m86f(parcel, b);
                    hashSet.add(Integer.valueOf(2));
                    break;
                case 3:
                    i = C0141a.m86f(parcel, b);
                    hashSet.add(Integer.valueOf(3));
                    break;
                default:
                    C0141a.m79b(parcel, b);
                    break;
            }
        }
        if (parcel.dataPosition() == c) {
            return new C1713a(hashSet, i3, i2, i);
        }
        throw new C0140a("Overread allowed size end=" + c, parcel);
    }
}
