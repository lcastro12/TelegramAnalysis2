package com.google.android.gms.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.C0141a;
import com.google.android.gms.common.internal.safeparcel.C0141a.C0140a;
import com.google.android.gms.common.internal.safeparcel.C0142b;
import com.google.android.gms.internal.ah.C1309a;
import com.google.android.gms.internal.ah.C1310b;
import java.util.ArrayList;

public class aj implements Creator<C1309a> {
    static void m200a(C1309a c1309a, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        C0142b.m129c(parcel, 1, c1309a.versionCode);
        C0142b.m119a(parcel, 2, c1309a.className, false);
        C0142b.m128b(parcel, 3, c1309a.cG, false);
        C0142b.m110C(parcel, d);
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return m201l(x0);
    }

    public C1309a m201l(Parcel parcel) {
        ArrayList arrayList = null;
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
                    arrayList = C0141a.m82c(parcel, b, C1310b.CREATOR);
                    break;
                default:
                    C0141a.m79b(parcel, b);
                    break;
            }
        }
        if (parcel.dataPosition() == c) {
            return new C1309a(i, str, arrayList);
        }
        throw new C0140a("Overread allowed size end=" + c, parcel);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return m202u(x0);
    }

    public C1309a[] m202u(int i) {
        return new C1309a[i];
    }
}
