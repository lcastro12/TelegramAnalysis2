package com.google.android.gms.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.C0141a;
import com.google.android.gms.common.internal.safeparcel.C0141a.C0140a;
import com.google.android.gms.common.internal.safeparcel.C0142b;
import com.google.android.gms.internal.cc.C1720g;
import java.util.HashSet;
import java.util.Set;

public class cl implements Creator<C1720g> {
    static void m440a(C1720g c1720g, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        Set bH = c1720g.bH();
        if (bH.contains(Integer.valueOf(1))) {
            C0142b.m129c(parcel, 1, c1720g.m1333i());
        }
        if (bH.contains(Integer.valueOf(2))) {
            C0142b.m122a(parcel, 2, c1720g.isPrimary());
        }
        if (bH.contains(Integer.valueOf(3))) {
            C0142b.m119a(parcel, 3, c1720g.getValue(), true);
        }
        C0142b.m110C(parcel, d);
    }

    public C1720g m441G(Parcel parcel) {
        boolean z = false;
        int c = C0141a.m81c(parcel);
        Set hashSet = new HashSet();
        String str = null;
        int i = 0;
        while (parcel.dataPosition() < c) {
            int b = C0141a.m78b(parcel);
            switch (C0141a.m93m(b)) {
                case 1:
                    i = C0141a.m86f(parcel, b);
                    hashSet.add(Integer.valueOf(1));
                    break;
                case 2:
                    z = C0141a.m83c(parcel, b);
                    hashSet.add(Integer.valueOf(2));
                    break;
                case 3:
                    str = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(3));
                    break;
                default:
                    C0141a.m79b(parcel, b);
                    break;
            }
        }
        if (parcel.dataPosition() == c) {
            return new C1720g(hashSet, i, z, str);
        }
        throw new C0140a("Overread allowed size end=" + c, parcel);
    }

    public C1720g[] ag(int i) {
        return new C1720g[i];
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return m441G(x0);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return ag(x0);
    }
}
