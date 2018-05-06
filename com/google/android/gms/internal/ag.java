package com.google.android.gms.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.C0141a;
import com.google.android.gms.common.internal.safeparcel.C0141a.C0140a;
import com.google.android.gms.common.internal.safeparcel.C0142b;
import com.google.android.gms.internal.ae.C1308a;
import com.google.android.gms.internal.ah.C1310b;

public class ag implements Creator<C1310b> {
    static void m194a(C1310b c1310b, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        C0142b.m129c(parcel, 1, c1310b.versionCode);
        C0142b.m119a(parcel, 2, c1310b.cH, false);
        C0142b.m118a(parcel, 3, c1310b.cI, i, false);
        C0142b.m110C(parcel, d);
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return m195j(x0);
    }

    public C1310b m195j(Parcel parcel) {
        C1308a c1308a = null;
        int c = C0141a.m81c(parcel);
        int i = 0;
        String str = null;
        while (parcel.dataPosition() < c) {
            int b = C0141a.m78b(parcel);
            switch (C0141a.m93m(b)) {
                case 1:
                    i = C0141a.m86f(parcel, b);
                    break;
                case 2:
                    str = C0141a.m92l(parcel, b);
                    break;
                case 3:
                    c1308a = (C1308a) C0141a.m75a(parcel, b, C1308a.CREATOR);
                    break;
                default:
                    C0141a.m79b(parcel, b);
                    break;
            }
        }
        if (parcel.dataPosition() == c) {
            return new C1310b(i, str, c1308a);
        }
        throw new C0140a("Overread allowed size end=" + c, parcel);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return m196s(x0);
    }

    public C1310b[] m196s(int i) {
        return new C1310b[i];
    }
}
