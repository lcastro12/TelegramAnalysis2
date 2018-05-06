package com.google.android.gms.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.C0141a;
import com.google.android.gms.common.internal.safeparcel.C0141a.C0140a;
import com.google.android.gms.common.internal.safeparcel.C0142b;
import java.util.HashSet;
import java.util.Set;

public class ca implements Creator<bz> {
    static void m418a(bz bzVar, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        Set bH = bzVar.bH();
        if (bH.contains(Integer.valueOf(1))) {
            C0142b.m129c(parcel, 1, bzVar.m1274i());
        }
        if (bH.contains(Integer.valueOf(2))) {
            C0142b.m119a(parcel, 2, bzVar.getId(), true);
        }
        if (bH.contains(Integer.valueOf(4))) {
            C0142b.m118a(parcel, 4, bzVar.bY(), i, true);
        }
        if (bH.contains(Integer.valueOf(5))) {
            C0142b.m119a(parcel, 5, bzVar.getStartDate(), true);
        }
        if (bH.contains(Integer.valueOf(6))) {
            C0142b.m118a(parcel, 6, bzVar.bZ(), i, true);
        }
        if (bH.contains(Integer.valueOf(7))) {
            C0142b.m119a(parcel, 7, bzVar.getType(), true);
        }
        C0142b.m110C(parcel, d);
    }

    public bz[] m419X(int i) {
        return new bz[i];
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return m420x(x0);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return m419X(x0);
    }

    public bz m420x(Parcel parcel) {
        String str = null;
        int c = C0141a.m81c(parcel);
        Set hashSet = new HashSet();
        int i = 0;
        bx bxVar = null;
        String str2 = null;
        bx bxVar2 = null;
        String str3 = null;
        while (parcel.dataPosition() < c) {
            int b = C0141a.m78b(parcel);
            bx bxVar3;
            switch (C0141a.m93m(b)) {
                case 1:
                    i = C0141a.m86f(parcel, b);
                    hashSet.add(Integer.valueOf(1));
                    break;
                case 2:
                    str3 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(2));
                    break;
                case 4:
                    bxVar3 = (bx) C0141a.m75a(parcel, b, bx.CREATOR);
                    hashSet.add(Integer.valueOf(4));
                    bxVar2 = bxVar3;
                    break;
                case 5:
                    str2 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(5));
                    break;
                case 6:
                    bxVar3 = (bx) C0141a.m75a(parcel, b, bx.CREATOR);
                    hashSet.add(Integer.valueOf(6));
                    bxVar = bxVar3;
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
            return new bz(hashSet, i, str3, bxVar2, str2, bxVar, str);
        }
        throw new C0140a("Overread allowed size end=" + c, parcel);
    }
}
