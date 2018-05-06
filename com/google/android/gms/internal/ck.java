package com.google.android.gms.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.C0141a;
import com.google.android.gms.common.internal.safeparcel.C0141a.C0140a;
import com.google.android.gms.common.internal.safeparcel.C0142b;
import com.google.android.gms.internal.cc.C1719f;
import java.util.HashSet;
import java.util.Set;

public class ck implements Creator<C1719f> {
    static void m438a(C1719f c1719f, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        Set bH = c1719f.bH();
        if (bH.contains(Integer.valueOf(1))) {
            C0142b.m129c(parcel, 1, c1719f.m1327i());
        }
        if (bH.contains(Integer.valueOf(2))) {
            C0142b.m119a(parcel, 2, c1719f.getDepartment(), true);
        }
        if (bH.contains(Integer.valueOf(3))) {
            C0142b.m119a(parcel, 3, c1719f.getDescription(), true);
        }
        if (bH.contains(Integer.valueOf(4))) {
            C0142b.m119a(parcel, 4, c1719f.getEndDate(), true);
        }
        if (bH.contains(Integer.valueOf(5))) {
            C0142b.m119a(parcel, 5, c1719f.getLocation(), true);
        }
        if (bH.contains(Integer.valueOf(6))) {
            C0142b.m119a(parcel, 6, c1719f.getName(), true);
        }
        if (bH.contains(Integer.valueOf(7))) {
            C0142b.m122a(parcel, 7, c1719f.isPrimary());
        }
        if (bH.contains(Integer.valueOf(8))) {
            C0142b.m119a(parcel, 8, c1719f.getStartDate(), true);
        }
        if (bH.contains(Integer.valueOf(9))) {
            C0142b.m119a(parcel, 9, c1719f.getTitle(), true);
        }
        if (bH.contains(Integer.valueOf(10))) {
            C0142b.m129c(parcel, 10, c1719f.getType());
        }
        C0142b.m110C(parcel, d);
    }

    public C1719f m439F(Parcel parcel) {
        int i = 0;
        String str = null;
        int c = C0141a.m81c(parcel);
        Set hashSet = new HashSet();
        String str2 = null;
        boolean z = false;
        String str3 = null;
        String str4 = null;
        String str5 = null;
        String str6 = null;
        String str7 = null;
        int i2 = 0;
        while (parcel.dataPosition() < c) {
            int b = C0141a.m78b(parcel);
            switch (C0141a.m93m(b)) {
                case 1:
                    i2 = C0141a.m86f(parcel, b);
                    hashSet.add(Integer.valueOf(1));
                    break;
                case 2:
                    str7 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(2));
                    break;
                case 3:
                    str6 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(3));
                    break;
                case 4:
                    str5 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(4));
                    break;
                case 5:
                    str4 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(5));
                    break;
                case 6:
                    str3 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(6));
                    break;
                case 7:
                    z = C0141a.m83c(parcel, b);
                    hashSet.add(Integer.valueOf(7));
                    break;
                case 8:
                    str2 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(8));
                    break;
                case 9:
                    str = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(9));
                    break;
                case 10:
                    i = C0141a.m86f(parcel, b);
                    hashSet.add(Integer.valueOf(10));
                    break;
                default:
                    C0141a.m79b(parcel, b);
                    break;
            }
        }
        if (parcel.dataPosition() == c) {
            return new C1719f(hashSet, i2, str7, str6, str5, str4, str3, z, str2, str, i);
        }
        throw new C0140a("Overread allowed size end=" + c, parcel);
    }

    public C1719f[] af(int i) {
        return new C1719f[i];
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return m439F(x0);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return af(x0);
    }
}
