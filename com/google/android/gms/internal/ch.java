package com.google.android.gms.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.C0141a;
import com.google.android.gms.common.internal.safeparcel.C0141a.C0140a;
import com.google.android.gms.common.internal.safeparcel.C0142b;
import com.google.android.gms.internal.cc.C1716b.C1715b;
import java.util.HashSet;
import java.util.Set;

public class ch implements Creator<C1715b> {
    static void m432a(C1715b c1715b, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        Set bH = c1715b.bH();
        if (bH.contains(Integer.valueOf(1))) {
            C0142b.m129c(parcel, 1, c1715b.m1303i());
        }
        if (bH.contains(Integer.valueOf(2))) {
            C0142b.m129c(parcel, 2, c1715b.getHeight());
        }
        if (bH.contains(Integer.valueOf(3))) {
            C0142b.m119a(parcel, 3, c1715b.getUrl(), true);
        }
        if (bH.contains(Integer.valueOf(4))) {
            C0142b.m129c(parcel, 4, c1715b.getWidth());
        }
        C0142b.m110C(parcel, d);
    }

    public C1715b m433C(Parcel parcel) {
        int i = 0;
        int c = C0141a.m81c(parcel);
        Set hashSet = new HashSet();
        String str = null;
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
                    str = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(3));
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
            return new C1715b(hashSet, i3, i2, str, i);
        }
        throw new C0140a("Overread allowed size end=" + c, parcel);
    }

    public C1715b[] ac(int i) {
        return new C1715b[i];
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return m433C(x0);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return ac(x0);
    }
}
