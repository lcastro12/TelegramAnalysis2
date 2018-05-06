package com.google.android.gms.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.C0141a;
import com.google.android.gms.common.internal.safeparcel.C0141a.C0140a;
import com.google.android.gms.common.internal.safeparcel.C0142b;
import com.google.android.gms.internal.ae.C1308a;

public class af implements Creator<C1308a> {
    static void m191a(C1308a c1308a, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        C0142b.m129c(parcel, 1, c1308a.m685i());
        C0142b.m129c(parcel, 2, c1308a.m677R());
        C0142b.m122a(parcel, 3, c1308a.m680X());
        C0142b.m129c(parcel, 4, c1308a.m678S());
        C0142b.m122a(parcel, 5, c1308a.m681Y());
        C0142b.m119a(parcel, 6, c1308a.m682Z(), false);
        C0142b.m129c(parcel, 7, c1308a.aa());
        C0142b.m119a(parcel, 8, c1308a.ac(), false);
        C0142b.m118a(parcel, 9, c1308a.ae(), i, false);
        C0142b.m110C(parcel, d);
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return m192i(x0);
    }

    public C1308a m192i(Parcel parcel) {
        C1363z c1363z = null;
        int i = 0;
        int c = C0141a.m81c(parcel);
        String str = null;
        String str2 = null;
        boolean z = false;
        int i2 = 0;
        boolean z2 = false;
        int i3 = 0;
        int i4 = 0;
        while (parcel.dataPosition() < c) {
            int b = C0141a.m78b(parcel);
            switch (C0141a.m93m(b)) {
                case 1:
                    i4 = C0141a.m86f(parcel, b);
                    break;
                case 2:
                    i3 = C0141a.m86f(parcel, b);
                    break;
                case 3:
                    z2 = C0141a.m83c(parcel, b);
                    break;
                case 4:
                    i2 = C0141a.m86f(parcel, b);
                    break;
                case 5:
                    z = C0141a.m83c(parcel, b);
                    break;
                case 6:
                    str2 = C0141a.m92l(parcel, b);
                    break;
                case 7:
                    i = C0141a.m86f(parcel, b);
                    break;
                case 8:
                    str = C0141a.m92l(parcel, b);
                    break;
                case 9:
                    c1363z = (C1363z) C0141a.m75a(parcel, b, C1363z.CREATOR);
                    break;
                default:
                    C0141a.m79b(parcel, b);
                    break;
            }
        }
        if (parcel.dataPosition() == c) {
            return new C1308a(i4, i3, z2, i2, z, str2, i, str, c1363z);
        }
        throw new C0140a("Overread allowed size end=" + c, parcel);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return m193r(x0);
    }

    public C1308a[] m193r(int i) {
        return new C1308a[i];
    }
}
