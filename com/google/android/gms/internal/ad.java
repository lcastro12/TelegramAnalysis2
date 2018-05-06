package com.google.android.gms.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.C0141a;
import com.google.android.gms.common.internal.safeparcel.C0141a.C0140a;
import com.google.android.gms.common.internal.safeparcel.C0142b;
import com.google.android.gms.internal.ab.C1307a;

public class ad implements Creator<C1307a> {
    static void m173a(C1307a c1307a, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        C0142b.m129c(parcel, 1, c1307a.versionCode);
        C0142b.m119a(parcel, 2, c1307a.cr, false);
        C0142b.m129c(parcel, 3, c1307a.cs);
        C0142b.m110C(parcel, d);
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return m174h(x0);
    }

    public C1307a m174h(Parcel parcel) {
        int i = 0;
        int c = C0141a.m81c(parcel);
        String str = null;
        int i2 = 0;
        while (parcel.dataPosition() < c) {
            int b = C0141a.m78b(parcel);
            switch (C0141a.m93m(b)) {
                case 1:
                    i2 = C0141a.m86f(parcel, b);
                    break;
                case 2:
                    str = C0141a.m92l(parcel, b);
                    break;
                case 3:
                    i = C0141a.m86f(parcel, b);
                    break;
                default:
                    C0141a.m79b(parcel, b);
                    break;
            }
        }
        if (parcel.dataPosition() == c) {
            return new C1307a(i2, str, i);
        }
        throw new C0140a("Overread allowed size end=" + c, parcel);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return m175q(x0);
    }

    public C1307a[] m175q(int i) {
        return new C1307a[i];
    }
}
