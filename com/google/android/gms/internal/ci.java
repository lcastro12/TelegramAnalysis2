package com.google.android.gms.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.C0141a;
import com.google.android.gms.common.internal.safeparcel.C0141a.C0140a;
import com.google.android.gms.common.internal.safeparcel.C0142b;
import com.google.android.gms.internal.cc.C1717c;
import java.util.HashSet;
import java.util.Set;

public class ci implements Creator<C1717c> {
    static void m434a(C1717c c1717c, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        Set bH = c1717c.bH();
        if (bH.contains(Integer.valueOf(1))) {
            C0142b.m129c(parcel, 1, c1717c.m1315i());
        }
        if (bH.contains(Integer.valueOf(2))) {
            C0142b.m119a(parcel, 2, c1717c.getUrl(), true);
        }
        C0142b.m110C(parcel, d);
    }

    public C1717c m435D(Parcel parcel) {
        int c = C0141a.m81c(parcel);
        Set hashSet = new HashSet();
        int i = 0;
        String str = null;
        while (parcel.dataPosition() < c) {
            int b = C0141a.m78b(parcel);
            switch (C0141a.m93m(b)) {
                case 1:
                    i = C0141a.m86f(parcel, b);
                    hashSet.add(Integer.valueOf(1));
                    break;
                case 2:
                    str = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(2));
                    break;
                default:
                    C0141a.m79b(parcel, b);
                    break;
            }
        }
        if (parcel.dataPosition() == c) {
            return new C1717c(hashSet, i, str);
        }
        throw new C0140a("Overread allowed size end=" + c, parcel);
    }

    public C1717c[] ad(int i) {
        return new C1717c[i];
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return m435D(x0);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return ad(x0);
    }
}
