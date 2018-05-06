package com.google.android.gms.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import android.support.v4.view.MotionEventCompat;
import com.google.android.gms.common.internal.safeparcel.C0141a;
import com.google.android.gms.common.internal.safeparcel.C0141a.C0140a;
import com.google.android.gms.common.internal.safeparcel.C0142b;
import com.googlecode.mp4parser.authoring.tracks.h265.NalUnitTypes;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class by implements Creator<bx> {
    static void m415a(bx bxVar, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        Set bH = bxVar.bH();
        if (bH.contains(Integer.valueOf(1))) {
            C0142b.m129c(parcel, 1, bxVar.m1268i());
        }
        if (bH.contains(Integer.valueOf(2))) {
            C0142b.m118a(parcel, 2, bxVar.bI(), i, true);
        }
        if (bH.contains(Integer.valueOf(3))) {
            C0142b.m120a(parcel, 3, bxVar.getAdditionalName(), true);
        }
        if (bH.contains(Integer.valueOf(4))) {
            C0142b.m118a(parcel, 4, bxVar.bJ(), i, true);
        }
        if (bH.contains(Integer.valueOf(5))) {
            C0142b.m119a(parcel, 5, bxVar.getAddressCountry(), true);
        }
        if (bH.contains(Integer.valueOf(6))) {
            C0142b.m119a(parcel, 6, bxVar.getAddressLocality(), true);
        }
        if (bH.contains(Integer.valueOf(7))) {
            C0142b.m119a(parcel, 7, bxVar.getAddressRegion(), true);
        }
        if (bH.contains(Integer.valueOf(8))) {
            C0142b.m128b(parcel, 8, bxVar.bK(), true);
        }
        if (bH.contains(Integer.valueOf(9))) {
            C0142b.m129c(parcel, 9, bxVar.getAttendeeCount());
        }
        if (bH.contains(Integer.valueOf(10))) {
            C0142b.m128b(parcel, 10, bxVar.bL(), true);
        }
        if (bH.contains(Integer.valueOf(11))) {
            C0142b.m118a(parcel, 11, bxVar.bM(), i, true);
        }
        if (bH.contains(Integer.valueOf(12))) {
            C0142b.m128b(parcel, 12, bxVar.bN(), true);
        }
        if (bH.contains(Integer.valueOf(13))) {
            C0142b.m119a(parcel, 13, bxVar.getBestRating(), true);
        }
        if (bH.contains(Integer.valueOf(14))) {
            C0142b.m119a(parcel, 14, bxVar.getBirthDate(), true);
        }
        if (bH.contains(Integer.valueOf(15))) {
            C0142b.m118a(parcel, 15, bxVar.bO(), i, true);
        }
        if (bH.contains(Integer.valueOf(17))) {
            C0142b.m119a(parcel, 17, bxVar.getContentSize(), true);
        }
        if (bH.contains(Integer.valueOf(16))) {
            C0142b.m119a(parcel, 16, bxVar.getCaption(), true);
        }
        if (bH.contains(Integer.valueOf(19))) {
            C0142b.m128b(parcel, 19, bxVar.bP(), true);
        }
        if (bH.contains(Integer.valueOf(18))) {
            C0142b.m119a(parcel, 18, bxVar.getContentUrl(), true);
        }
        if (bH.contains(Integer.valueOf(21))) {
            C0142b.m119a(parcel, 21, bxVar.getDateModified(), true);
        }
        if (bH.contains(Integer.valueOf(20))) {
            C0142b.m119a(parcel, 20, bxVar.getDateCreated(), true);
        }
        if (bH.contains(Integer.valueOf(23))) {
            C0142b.m119a(parcel, 23, bxVar.getDescription(), true);
        }
        if (bH.contains(Integer.valueOf(22))) {
            C0142b.m119a(parcel, 22, bxVar.getDatePublished(), true);
        }
        if (bH.contains(Integer.valueOf(25))) {
            C0142b.m119a(parcel, 25, bxVar.getEmbedUrl(), true);
        }
        if (bH.contains(Integer.valueOf(24))) {
            C0142b.m119a(parcel, 24, bxVar.getDuration(), true);
        }
        if (bH.contains(Integer.valueOf(27))) {
            C0142b.m119a(parcel, 27, bxVar.getFamilyName(), true);
        }
        if (bH.contains(Integer.valueOf(26))) {
            C0142b.m119a(parcel, 26, bxVar.getEndDate(), true);
        }
        if (bH.contains(Integer.valueOf(29))) {
            C0142b.m118a(parcel, 29, bxVar.bQ(), i, true);
        }
        if (bH.contains(Integer.valueOf(28))) {
            C0142b.m119a(parcel, 28, bxVar.getGender(), true);
        }
        if (bH.contains(Integer.valueOf(31))) {
            C0142b.m119a(parcel, 31, bxVar.getHeight(), true);
        }
        if (bH.contains(Integer.valueOf(30))) {
            C0142b.m119a(parcel, 30, bxVar.getGivenName(), true);
        }
        if (bH.contains(Integer.valueOf(34))) {
            C0142b.m118a(parcel, 34, bxVar.bR(), i, true);
        }
        if (bH.contains(Integer.valueOf(32))) {
            C0142b.m119a(parcel, 32, bxVar.getId(), true);
        }
        if (bH.contains(Integer.valueOf(33))) {
            C0142b.m119a(parcel, 33, bxVar.getImage(), true);
        }
        if (bH.contains(Integer.valueOf(38))) {
            C0142b.m112a(parcel, 38, bxVar.getLongitude());
        }
        if (bH.contains(Integer.valueOf(39))) {
            C0142b.m119a(parcel, 39, bxVar.getName(), true);
        }
        if (bH.contains(Integer.valueOf(36))) {
            C0142b.m112a(parcel, 36, bxVar.getLatitude());
        }
        if (bH.contains(Integer.valueOf(37))) {
            C0142b.m118a(parcel, 37, bxVar.bS(), i, true);
        }
        if (bH.contains(Integer.valueOf(42))) {
            C0142b.m119a(parcel, 42, bxVar.getPlayerType(), true);
        }
        if (bH.contains(Integer.valueOf(43))) {
            C0142b.m119a(parcel, 43, bxVar.getPostOfficeBoxNumber(), true);
        }
        if (bH.contains(Integer.valueOf(40))) {
            C0142b.m118a(parcel, 40, bxVar.bT(), i, true);
        }
        if (bH.contains(Integer.valueOf(41))) {
            C0142b.m128b(parcel, 41, bxVar.bU(), true);
        }
        if (bH.contains(Integer.valueOf(46))) {
            C0142b.m118a(parcel, 46, bxVar.bV(), i, true);
        }
        if (bH.contains(Integer.valueOf(47))) {
            C0142b.m119a(parcel, 47, bxVar.getStartDate(), true);
        }
        if (bH.contains(Integer.valueOf(44))) {
            C0142b.m119a(parcel, 44, bxVar.getPostalCode(), true);
        }
        if (bH.contains(Integer.valueOf(45))) {
            C0142b.m119a(parcel, 45, bxVar.getRatingValue(), true);
        }
        if (bH.contains(Integer.valueOf(51))) {
            C0142b.m119a(parcel, 51, bxVar.getThumbnailUrl(), true);
        }
        if (bH.contains(Integer.valueOf(50))) {
            C0142b.m118a(parcel, 50, bxVar.bW(), i, true);
        }
        if (bH.contains(Integer.valueOf(49))) {
            C0142b.m119a(parcel, 49, bxVar.getText(), true);
        }
        if (bH.contains(Integer.valueOf(48))) {
            C0142b.m119a(parcel, 48, bxVar.getStreetAddress(), true);
        }
        if (bH.contains(Integer.valueOf(55))) {
            C0142b.m119a(parcel, 55, bxVar.getWidth(), true);
        }
        if (bH.contains(Integer.valueOf(54))) {
            C0142b.m119a(parcel, 54, bxVar.getUrl(), true);
        }
        if (bH.contains(Integer.valueOf(53))) {
            C0142b.m119a(parcel, 53, bxVar.getType(), true);
        }
        if (bH.contains(Integer.valueOf(52))) {
            C0142b.m119a(parcel, 52, bxVar.getTickerSymbol(), true);
        }
        if (bH.contains(Integer.valueOf(56))) {
            C0142b.m119a(parcel, 56, bxVar.getWorstRating(), true);
        }
        C0142b.m110C(parcel, d);
    }

    public bx[] m416W(int i) {
        return new bx[i];
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return m417w(x0);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return m416W(x0);
    }

    public bx m417w(Parcel parcel) {
        int c = C0141a.m81c(parcel);
        Set hashSet = new HashSet();
        int i = 0;
        bx bxVar = null;
        List list = null;
        bx bxVar2 = null;
        String str = null;
        String str2 = null;
        String str3 = null;
        List list2 = null;
        int i2 = 0;
        List list3 = null;
        bx bxVar3 = null;
        List list4 = null;
        String str4 = null;
        String str5 = null;
        bx bxVar4 = null;
        String str6 = null;
        String str7 = null;
        String str8 = null;
        List list5 = null;
        String str9 = null;
        String str10 = null;
        String str11 = null;
        String str12 = null;
        String str13 = null;
        String str14 = null;
        String str15 = null;
        String str16 = null;
        String str17 = null;
        bx bxVar5 = null;
        String str18 = null;
        String str19 = null;
        String str20 = null;
        String str21 = null;
        bx bxVar6 = null;
        double d = 0.0d;
        bx bxVar7 = null;
        double d2 = 0.0d;
        String str22 = null;
        bx bxVar8 = null;
        List list6 = null;
        String str23 = null;
        String str24 = null;
        String str25 = null;
        String str26 = null;
        bx bxVar9 = null;
        String str27 = null;
        String str28 = null;
        String str29 = null;
        bx bxVar10 = null;
        String str30 = null;
        String str31 = null;
        String str32 = null;
        String str33 = null;
        String str34 = null;
        String str35 = null;
        while (parcel.dataPosition() < c) {
            int b = C0141a.m78b(parcel);
            bx bxVar11;
            switch (C0141a.m93m(b)) {
                case 1:
                    i = C0141a.m86f(parcel, b);
                    hashSet.add(Integer.valueOf(1));
                    break;
                case 2:
                    bxVar11 = (bx) C0141a.m75a(parcel, b, bx.CREATOR);
                    hashSet.add(Integer.valueOf(2));
                    bxVar = bxVar11;
                    break;
                case 3:
                    list = C0141a.m105x(parcel, b);
                    hashSet.add(Integer.valueOf(3));
                    break;
                case 4:
                    bxVar11 = (bx) C0141a.m75a(parcel, b, bx.CREATOR);
                    hashSet.add(Integer.valueOf(4));
                    bxVar2 = bxVar11;
                    break;
                case 5:
                    str = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(5));
                    break;
                case 6:
                    str2 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(6));
                    break;
                case 7:
                    str3 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(7));
                    break;
                case 8:
                    list2 = C0141a.m82c(parcel, b, bx.CREATOR);
                    hashSet.add(Integer.valueOf(8));
                    break;
                case 9:
                    i2 = C0141a.m86f(parcel, b);
                    hashSet.add(Integer.valueOf(9));
                    break;
                case 10:
                    list3 = C0141a.m82c(parcel, b, bx.CREATOR);
                    hashSet.add(Integer.valueOf(10));
                    break;
                case 11:
                    bxVar11 = (bx) C0141a.m75a(parcel, b, bx.CREATOR);
                    hashSet.add(Integer.valueOf(11));
                    bxVar3 = bxVar11;
                    break;
                case 12:
                    list4 = C0141a.m82c(parcel, b, bx.CREATOR);
                    hashSet.add(Integer.valueOf(12));
                    break;
                case 13:
                    str4 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(13));
                    break;
                case 14:
                    str5 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(14));
                    break;
                case 15:
                    bxVar11 = (bx) C0141a.m75a(parcel, b, (Creator) bx.CREATOR);
                    hashSet.add(Integer.valueOf(15));
                    bxVar4 = bxVar11;
                    break;
                case 16:
                    str6 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(16));
                    break;
                case 17:
                    str7 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(17));
                    break;
                case 18:
                    str8 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(18));
                    break;
                case 19:
                    list5 = C0141a.m82c(parcel, b, bx.CREATOR);
                    hashSet.add(Integer.valueOf(19));
                    break;
                case 20:
                    str9 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(20));
                    break;
                case 21:
                    str10 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(21));
                    break;
                case 22:
                    str11 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(22));
                    break;
                case 23:
                    str12 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(23));
                    break;
                case 24:
                    str13 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(24));
                    break;
                case 25:
                    str14 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(25));
                    break;
                case NalUnitTypes.NAL_TYPE_RSV_VCL26 /*26*/:
                    str15 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(26));
                    break;
                case NalUnitTypes.NAL_TYPE_RSV_VCL27 /*27*/:
                    str16 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(27));
                    break;
                case NalUnitTypes.NAL_TYPE_RSV_VCL28 /*28*/:
                    str17 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(28));
                    break;
                case NalUnitTypes.NAL_TYPE_RSV_VCL29 /*29*/:
                    bxVar11 = (bx) C0141a.m75a(parcel, b, (Creator) bx.CREATOR);
                    hashSet.add(Integer.valueOf(29));
                    bxVar5 = bxVar11;
                    break;
                case NalUnitTypes.NAL_TYPE_RSV_VCL30 /*30*/:
                    str18 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(30));
                    break;
                case NalUnitTypes.NAL_TYPE_RSV_VCL31 /*31*/:
                    str19 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(31));
                    break;
                case 32:
                    str20 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(32));
                    break;
                case 33:
                    str21 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(33));
                    break;
                case 34:
                    bxVar11 = (bx) C0141a.m75a(parcel, b, (Creator) bx.CREATOR);
                    hashSet.add(Integer.valueOf(34));
                    bxVar6 = bxVar11;
                    break;
                case 36:
                    d = C0141a.m90j(parcel, b);
                    hashSet.add(Integer.valueOf(36));
                    break;
                case 37:
                    bxVar11 = (bx) C0141a.m75a(parcel, b, (Creator) bx.CREATOR);
                    hashSet.add(Integer.valueOf(37));
                    bxVar7 = bxVar11;
                    break;
                case 38:
                    d2 = C0141a.m90j(parcel, b);
                    hashSet.add(Integer.valueOf(38));
                    break;
                case 39:
                    str22 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(39));
                    break;
                case MotionEventCompat.AXIS_GENERIC_9 /*40*/:
                    bxVar11 = (bx) C0141a.m75a(parcel, b, (Creator) bx.CREATOR);
                    hashSet.add(Integer.valueOf(40));
                    bxVar8 = bxVar11;
                    break;
                case 41:
                    list6 = C0141a.m82c(parcel, b, bx.CREATOR);
                    hashSet.add(Integer.valueOf(41));
                    break;
                case 42:
                    str23 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(42));
                    break;
                case 43:
                    str24 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(43));
                    break;
                case 44:
                    str25 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(44));
                    break;
                case MotionEventCompat.AXIS_GENERIC_14 /*45*/:
                    str26 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(45));
                    break;
                case MotionEventCompat.AXIS_GENERIC_15 /*46*/:
                    bxVar11 = (bx) C0141a.m75a(parcel, b, (Creator) bx.CREATOR);
                    hashSet.add(Integer.valueOf(46));
                    bxVar9 = bxVar11;
                    break;
                case MotionEventCompat.AXIS_GENERIC_16 /*47*/:
                    str27 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(47));
                    break;
                case 48:
                    str28 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(48));
                    break;
                case 49:
                    str29 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(49));
                    break;
                case 50:
                    bxVar11 = (bx) C0141a.m75a(parcel, b, (Creator) bx.CREATOR);
                    hashSet.add(Integer.valueOf(50));
                    bxVar10 = bxVar11;
                    break;
                case 51:
                    str30 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(51));
                    break;
                case 52:
                    str31 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(52));
                    break;
                case 53:
                    str32 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(53));
                    break;
                case 54:
                    str33 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(54));
                    break;
                case 55:
                    str34 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(55));
                    break;
                case 56:
                    str35 = C0141a.m92l(parcel, b);
                    hashSet.add(Integer.valueOf(56));
                    break;
                default:
                    C0141a.m79b(parcel, b);
                    break;
            }
        }
        if (parcel.dataPosition() == c) {
            return new bx(hashSet, i, bxVar, list, bxVar2, str, str2, str3, list2, i2, list3, bxVar3, list4, str4, str5, bxVar4, str6, str7, str8, list5, str9, str10, str11, str12, str13, str14, str15, str16, str17, bxVar5, str18, str19, str20, str21, bxVar6, d, bxVar7, d2, str22, bxVar8, list6, str23, str24, str25, str26, bxVar9, str27, str28, str29, bxVar10, str30, str31, str32, str33, str34, str35);
        }
        throw new C0140a("Overread allowed size end=" + c, parcel);
    }
}
