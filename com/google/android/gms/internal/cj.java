package com.google.android.gms.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.C0141a;
import com.google.android.gms.common.internal.safeparcel.C0141a.C0140a;
import com.google.android.gms.common.internal.safeparcel.C0142b;
import com.google.android.gms.internal.cc.C1718d;
import java.util.HashSet;
import java.util.Set;

public class cj implements Creator<C1718d> {
    static void m436a(C1718d c1718d, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        Set bH = c1718d.bH();
        if (bH.contains(Integer.valueOf(1))) {
            C0142b.m129c(parcel, 1, c1718d.m1321i());
        }
        if (bH.contains(Integer.valueOf(2))) {
            C0142b.m119a(parcel, 2, c1718d.getFamilyName(), true);
        }
        if (bH.contains(Integer.valueOf(3))) {
            C0142b.m119a(parcel, 3, c1718d.getFormatted(), true);
        }
        if (bH.contains(Integer.valueOf(4))) {
            C0142b.m119a(parcel, 4, c1718d.getGivenName(), true);
        }
        if (bH.contains(Integer.valueOf(5))) {
            C0142b.m119a(parcel, 5, c1718d.getHonorificPrefix(), true);
        }
        if (bH.contains(Integer.valueOf(6))) {
            C0142b.m119a(parcel, 6, c1718d.getHonorificSuffix(), true);
        }
        if (bH.contains(Integer.valueOf(7))) {
            C0142b.m119a(parcel, 7, c1718d.getMiddleName(), true);
        }
        C0142b.m110C(parcel, d);
    }

    public C1718d m437E(Parcel parcel) {
        String str = null;
        int c = C0141a.m81c(parcel);
        Set hashSet = new HashSet();
        int i = 0;
        String str2 = null;
        String str3 = null;
        String str4 = null;
        String str5 = null;
        String str6 = null;
        while (parcel.dataPosition() < c) {
            int b = C0141a.m78b(parcel);
            switch (C0141a.m93m(b)) {
                case 1:
                    i = C0141a.m86f(parcel, b);
                    hashSet.add(Integer.valueOf(1));
                    break;
                case 2:
                    str6 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(2));
                    break;
                case 3:
                    str5 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(3));
                    break;
                case 4:
                    str4 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(4));
                    break;
                case 5:
                    str3 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(5));
                    break;
                case 6:
                    str2 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(6));
                    break;
                case 7:
                    str = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(7));
                    break;
                default:
                    C0141a.m79b(parcel, b);
                    break;
            }
        }
        if (parcel.dataPosition() == c) {
            return new C1718d(hashSet, i, str6, str5, str4, str3, str2, str);
        }
        throw new C0140a("Overread allowed size end=" + c, parcel);
    }

    public C1718d[] ae(int i) {
        return new C1718d[i];
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return m437E(x0);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return ae(x0);
    }
}
