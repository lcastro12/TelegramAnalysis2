package com.google.android.gms.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.C0141a;
import com.google.android.gms.common.internal.safeparcel.C0141a.C0140a;
import com.google.android.gms.common.internal.safeparcel.C0142b;
import com.google.android.gms.internal.cc.C1716b;
import com.google.android.gms.internal.cc.C1716b.C1714a;
import com.google.android.gms.internal.cc.C1716b.C1715b;
import java.util.HashSet;
import java.util.Set;

public class cf implements Creator<C1716b> {
    static void m428a(C1716b c1716b, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        Set bH = c1716b.bH();
        if (bH.contains(Integer.valueOf(1))) {
            C0142b.m129c(parcel, 1, c1716b.m1309i());
        }
        if (bH.contains(Integer.valueOf(2))) {
            C0142b.m118a(parcel, 2, c1716b.cl(), i, true);
        }
        if (bH.contains(Integer.valueOf(3))) {
            C0142b.m118a(parcel, 3, c1716b.cm(), i, true);
        }
        if (bH.contains(Integer.valueOf(4))) {
            C0142b.m129c(parcel, 4, c1716b.getLayout());
        }
        C0142b.m110C(parcel, d);
    }

    public C1716b m429A(Parcel parcel) {
        C1715b c1715b = null;
        int i = 0;
        int c = C0141a.m81c(parcel);
        Set hashSet = new HashSet();
        C1714a c1714a = null;
        int i2 = 0;
        while (parcel.dataPosition() < c) {
            int b = C0141a.m78b(parcel);
            switch (C0141a.m93m(b)) {
                case 1:
                    i2 = C0141a.m86f(parcel, b);
                    hashSet.add(Integer.valueOf(1));
                    break;
                case 2:
                    C1714a c1714a2 = (C1714a) C0141a.m75a(parcel, b, C1714a.CREATOR);
                    hashSet.add(Integer.valueOf(2));
                    c1714a = c1714a2;
                    break;
                case 3:
                    C1715b c1715b2 = (C1715b) C0141a.m75a(parcel, b, C1715b.CREATOR);
                    hashSet.add(Integer.valueOf(3));
                    c1715b = c1715b2;
                    break;
                case 4:
                    i = C0141a.m86f(parcel, b);
                    hashSet.add(Integer.valueOf(4));
                    break;
                default:
                    C0141a.m79b(parcel, b);
                    break;
            }
        }
        if (parcel.dataPosition() == c) {
            return new C1716b(hashSet, i2, c1714a, c1715b, i);
        }
        throw new C0140a("Overread allowed size end=" + c, parcel);
    }

    public C1716b[] aa(int i) {
        return new C1716b[i];
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return m429A(x0);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return aa(x0);
    }
}
