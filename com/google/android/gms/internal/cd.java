package com.google.android.gms.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.C0141a;
import com.google.android.gms.common.internal.safeparcel.C0141a.C0140a;
import com.google.android.gms.common.internal.safeparcel.C0142b;
import com.google.android.gms.internal.cc.C1713a;
import com.google.android.gms.internal.cc.C1716b;
import com.google.android.gms.internal.cc.C1717c;
import com.google.android.gms.internal.cc.C1718d;
import com.google.android.gms.internal.cc.C1719f;
import com.google.android.gms.internal.cc.C1720g;
import com.google.android.gms.internal.cc.C1721h;
import com.googlecode.mp4parser.authoring.tracks.h265.NalUnitTypes;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class cd implements Creator<cc> {
    static void m422a(cc ccVar, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        Set bH = ccVar.bH();
        if (bH.contains(Integer.valueOf(1))) {
            C0142b.m129c(parcel, 1, ccVar.m1346i());
        }
        if (bH.contains(Integer.valueOf(2))) {
            C0142b.m119a(parcel, 2, ccVar.getAboutMe(), true);
        }
        if (bH.contains(Integer.valueOf(3))) {
            C0142b.m118a(parcel, 3, ccVar.cc(), i, true);
        }
        if (bH.contains(Integer.valueOf(4))) {
            C0142b.m119a(parcel, 4, ccVar.getBirthday(), true);
        }
        if (bH.contains(Integer.valueOf(5))) {
            C0142b.m119a(parcel, 5, ccVar.getBraggingRights(), true);
        }
        if (bH.contains(Integer.valueOf(6))) {
            C0142b.m129c(parcel, 6, ccVar.getCircledByCount());
        }
        if (bH.contains(Integer.valueOf(7))) {
            C0142b.m118a(parcel, 7, ccVar.cd(), i, true);
        }
        if (bH.contains(Integer.valueOf(8))) {
            C0142b.m119a(parcel, 8, ccVar.getCurrentLocation(), true);
        }
        if (bH.contains(Integer.valueOf(9))) {
            C0142b.m119a(parcel, 9, ccVar.getDisplayName(), true);
        }
        if (bH.contains(Integer.valueOf(12))) {
            C0142b.m129c(parcel, 12, ccVar.getGender());
        }
        if (bH.contains(Integer.valueOf(14))) {
            C0142b.m119a(parcel, 14, ccVar.getId(), true);
        }
        if (bH.contains(Integer.valueOf(15))) {
            C0142b.m118a(parcel, 15, ccVar.ce(), i, true);
        }
        if (bH.contains(Integer.valueOf(16))) {
            C0142b.m122a(parcel, 16, ccVar.isPlusUser());
        }
        if (bH.contains(Integer.valueOf(19))) {
            C0142b.m118a(parcel, 19, ccVar.cf(), i, true);
        }
        if (bH.contains(Integer.valueOf(18))) {
            C0142b.m119a(parcel, 18, ccVar.getLanguage(), true);
        }
        if (bH.contains(Integer.valueOf(21))) {
            C0142b.m129c(parcel, 21, ccVar.getObjectType());
        }
        if (bH.contains(Integer.valueOf(20))) {
            C0142b.m119a(parcel, 20, ccVar.getNickname(), true);
        }
        if (bH.contains(Integer.valueOf(23))) {
            C0142b.m128b(parcel, 23, ccVar.ch(), true);
        }
        if (bH.contains(Integer.valueOf(22))) {
            C0142b.m128b(parcel, 22, ccVar.cg(), true);
        }
        if (bH.contains(Integer.valueOf(25))) {
            C0142b.m129c(parcel, 25, ccVar.getRelationshipStatus());
        }
        if (bH.contains(Integer.valueOf(24))) {
            C0142b.m129c(parcel, 24, ccVar.getPlusOneCount());
        }
        if (bH.contains(Integer.valueOf(27))) {
            C0142b.m119a(parcel, 27, ccVar.getUrl(), true);
        }
        if (bH.contains(Integer.valueOf(26))) {
            C0142b.m119a(parcel, 26, ccVar.getTagline(), true);
        }
        if (bH.contains(Integer.valueOf(29))) {
            C0142b.m122a(parcel, 29, ccVar.isVerified());
        }
        if (bH.contains(Integer.valueOf(28))) {
            C0142b.m128b(parcel, 28, ccVar.ci(), true);
        }
        C0142b.m110C(parcel, d);
    }

    public cc[] m423Y(int i) {
        return new cc[i];
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return m424y(x0);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return m423Y(x0);
    }

    public cc m424y(Parcel parcel) {
        int c = C0141a.m81c(parcel);
        Set hashSet = new HashSet();
        int i = 0;
        String str = null;
        C1713a c1713a = null;
        String str2 = null;
        String str3 = null;
        int i2 = 0;
        C1716b c1716b = null;
        String str4 = null;
        String str5 = null;
        int i3 = 0;
        String str6 = null;
        C1717c c1717c = null;
        boolean z = false;
        String str7 = null;
        C1718d c1718d = null;
        String str8 = null;
        int i4 = 0;
        List list = null;
        List list2 = null;
        int i5 = 0;
        int i6 = 0;
        String str9 = null;
        String str10 = null;
        List list3 = null;
        boolean z2 = false;
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
                case 3:
                    C1713a c1713a2 = (C1713a) C0141a.m75a(parcel, b, C1713a.CREATOR);
                    hashSet.add(Integer.valueOf(3));
                    c1713a = c1713a2;
                    break;
                case 4:
                    str2 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(4));
                    break;
                case 5:
                    str3 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(5));
                    break;
                case 6:
                    i2 = C0141a.m86f(parcel, b);
                    hashSet.add(Integer.valueOf(6));
                    break;
                case 7:
                    C1716b c1716b2 = (C1716b) C0141a.m75a(parcel, b, C1716b.CREATOR);
                    hashSet.add(Integer.valueOf(7));
                    c1716b = c1716b2;
                    break;
                case 8:
                    str4 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(8));
                    break;
                case 9:
                    str5 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(9));
                    break;
                case 12:
                    i3 = C0141a.m86f(parcel, b);
                    hashSet.add(Integer.valueOf(12));
                    break;
                case 14:
                    str6 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(14));
                    break;
                case 15:
                    C1717c c1717c2 = (C1717c) C0141a.m75a(parcel, b, C1717c.CREATOR);
                    hashSet.add(Integer.valueOf(15));
                    c1717c = c1717c2;
                    break;
                case 16:
                    z = C0141a.m83c(parcel, b);
                    hashSet.add(Integer.valueOf(16));
                    break;
                case 18:
                    str7 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(18));
                    break;
                case 19:
                    C1718d c1718d2 = (C1718d) C0141a.m75a(parcel, b, (Creator) C1718d.CREATOR);
                    hashSet.add(Integer.valueOf(19));
                    c1718d = c1718d2;
                    break;
                case 20:
                    str8 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(20));
                    break;
                case 21:
                    i4 = C0141a.m86f(parcel, b);
                    hashSet.add(Integer.valueOf(21));
                    break;
                case 22:
                    list = C0141a.m82c(parcel, b, C1719f.CREATOR);
                    hashSet.add(Integer.valueOf(22));
                    break;
                case 23:
                    list2 = C0141a.m82c(parcel, b, C1720g.CREATOR);
                    hashSet.add(Integer.valueOf(23));
                    break;
                case 24:
                    i5 = C0141a.m86f(parcel, b);
                    hashSet.add(Integer.valueOf(24));
                    break;
                case 25:
                    i6 = C0141a.m86f(parcel, b);
                    hashSet.add(Integer.valueOf(25));
                    break;
                case NalUnitTypes.NAL_TYPE_RSV_VCL26 /*26*/:
                    str9 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(26));
                    break;
                case NalUnitTypes.NAL_TYPE_RSV_VCL27 /*27*/:
                    str10 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(27));
                    break;
                case NalUnitTypes.NAL_TYPE_RSV_VCL28 /*28*/:
                    list3 = C0141a.m82c(parcel, b, C1721h.CREATOR);
                    hashSet.add(Integer.valueOf(28));
                    break;
                case NalUnitTypes.NAL_TYPE_RSV_VCL29 /*29*/:
                    z2 = C0141a.m83c(parcel, b);
                    hashSet.add(Integer.valueOf(29));
                    break;
                default:
                    C0141a.m79b(parcel, b);
                    break;
            }
        }
        if (parcel.dataPosition() == c) {
            return new cc(hashSet, i, str, c1713a, str2, str3, i2, c1716b, str4, str5, i3, str6, c1717c, z, str7, c1718d, str8, i4, list, list2, i5, i6, str9, str10, list3, z2);
        }
        throw new C0140a("Overread allowed size end=" + c, parcel);
    }
}
