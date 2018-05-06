package com.google.android.gms.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.C0141a;
import com.google.android.gms.common.internal.safeparcel.C0141a.C0140a;
import com.google.android.gms.common.internal.safeparcel.C0142b;
import com.google.android.gms.internal.cc.C1721h;
import java.util.HashSet;
import java.util.Set;

public class cm implements Creator<C1721h> {
    static void m442a(C1721h c1721h, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        Set bH = c1721h.bH();
        if (bH.contains(Integer.valueOf(1))) {
            C0142b.m129c(parcel, 1, c1721h.m1339i());
        }
        if (bH.contains(Integer.valueOf(3))) {
            C0142b.m129c(parcel, 3, c1721h.cu());
        }
        if (bH.contains(Integer.valueOf(4))) {
            C0142b.m119a(parcel, 4, c1721h.getValue(), true);
        }
        if (bH.contains(Integer.valueOf(5))) {
            C0142b.m119a(parcel, 5, c1721h.getLabel(), true);
        }
        if (bH.contains(Integer.valueOf(6))) {
            C0142b.m129c(parcel, 6, c1721h.getType());
        }
        C0142b.m110C(parcel, d);
    }

    public C1721h m443H(Parcel parcel) {
        String str = null;
        int i = 0;
        int c = C0141a.m81c(parcel);
        Set hashSet = new HashSet();
        int i2 = 0;
        String str2 = null;
        int i3 = 0;
        while (parcel.dataPosition() < c) {
            int b = C0141a.m78b(parcel);
            switch (C0141a.m93m(b)) {
                case 1:
                    i3 = C0141a.m86f(parcel, b);
                    hashSet.add(Integer.valueOf(1));
                    break;
                case 3:
                    i = C0141a.m86f(parcel, b);
                    hashSet.add(Integer.valueOf(3));
                    break;
                case 4:
                    str = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(4));
                    break;
                case 5:
                    str2 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(5));
                    break;
                case 6:
                    i2 = C0141a.m86f(parcel, b);
                    hashSet.add(Integer.valueOf(6));
                    break;
                default:
                    C0141a.m79b(parcel, b);
                    break;
            }
        }
        if (parcel.dataPosition() == c) {
            return new C1721h(hashSet, i3, str2, i2, str, i);
        }
        throw new C0140a("Overread allowed size end=" + c, parcel);
    }

    public C1721h[] ah(int i) {
        return new C1721h[i];
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return m443H(x0);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return ah(x0);
    }
}
