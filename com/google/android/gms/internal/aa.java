package com.google.android.gms.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.C0141a;
import com.google.android.gms.common.internal.safeparcel.C0141a.C0140a;
import com.google.android.gms.common.internal.safeparcel.C0142b;

public class aa implements Creator<C1363z> {
    static void m167a(C1363z c1363z, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        C0142b.m129c(parcel, 1, c1363z.m1035i());
        C0142b.m118a(parcel, 2, c1363z.m1033O(), i, false);
        C0142b.m110C(parcel, d);
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return m168f(x0);
    }

    public C1363z m168f(Parcel parcel) {
        int c = C0141a.m81c(parcel);
        int i = 0;
        ab abVar = null;
        while (parcel.dataPosition() < c) {
            int b = C0141a.m78b(parcel);
            switch (C0141a.m93m(b)) {
                case 1:
                    i = C0141a.m86f(parcel, b);
                    break;
                case 2:
                    abVar = (ab) C0141a.m75a(parcel, b, ab.CREATOR);
                    break;
                default:
                    C0141a.m79b(parcel, b);
                    break;
            }
        }
        if (parcel.dataPosition() == c) {
            return new C1363z(i, abVar);
        }
        throw new C0140a("Overread allowed size end=" + c, parcel);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return m169o(x0);
    }

    public C1363z[] m169o(int i) {
        return new C1363z[i];
    }
}
