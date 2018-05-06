package com.google.android.gms.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.C0141a;
import com.google.android.gms.common.internal.safeparcel.C0141a.C0140a;
import com.google.android.gms.common.internal.safeparcel.C0142b;
import com.google.android.gms.internal.ab.C1307a;
import java.util.ArrayList;

public class ac implements Creator<ab> {
    static void m170a(ab abVar, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        C0142b.m129c(parcel, 1, abVar.m667i());
        C0142b.m128b(parcel, 2, abVar.m661Q(), false);
        C0142b.m110C(parcel, d);
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return m171g(x0);
    }

    public ab m171g(Parcel parcel) {
        int c = C0141a.m81c(parcel);
        int i = 0;
        ArrayList arrayList = null;
        while (parcel.dataPosition() < c) {
            int b = C0141a.m78b(parcel);
            switch (C0141a.m93m(b)) {
                case 1:
                    i = C0141a.m86f(parcel, b);
                    break;
                case 2:
                    arrayList = C0141a.m82c(parcel, b, C1307a.CREATOR);
                    break;
                default:
                    C0141a.m79b(parcel, b);
                    break;
            }
        }
        if (parcel.dataPosition() == c) {
            return new ab(i, arrayList);
        }
        throw new C0140a("Overread allowed size end=" + c, parcel);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return m172p(x0);
    }

    public ab[] m172p(int i) {
        return new ab[i];
    }
}
