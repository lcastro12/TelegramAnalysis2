package com.google.android.gms.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.C0141a;
import com.google.android.gms.common.internal.safeparcel.C0141a.C0140a;
import com.google.android.gms.common.internal.safeparcel.C0142b;
import com.google.android.gms.internal.cc.C1716b.C1714a;
import java.util.HashSet;
import java.util.Set;

public class cg implements Creator<C1714a> {
    static void m430a(C1714a c1714a, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        Set bH = c1714a.bH();
        if (bH.contains(Integer.valueOf(1))) {
            C0142b.m129c(parcel, 1, c1714a.m1297i());
        }
        if (bH.contains(Integer.valueOf(2))) {
            C0142b.m129c(parcel, 2, c1714a.getLeftImageOffset());
        }
        if (bH.contains(Integer.valueOf(3))) {
            C0142b.m129c(parcel, 3, c1714a.getTopImageOffset());
        }
        C0142b.m110C(parcel, d);
    }

    public C1714a m431B(Parcel parcel) {
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
            return new C1714a(hashSet, i3, i2, i);
        }
        throw new C0140a("Overread allowed size end=" + c, parcel);
    }

    public C1714a[] ab(int i) {
        return new C1714a[i];
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return m431B(x0);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return ab(x0);
    }
}
