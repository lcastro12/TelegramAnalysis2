package com.google.android.gms.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.C0141a;
import com.google.android.gms.common.internal.safeparcel.C0141a.C0140a;
import com.google.android.gms.common.internal.safeparcel.C0142b;
import com.google.android.gms.internal.ah.C1309a;
import java.util.ArrayList;

public class ai implements Creator<ah> {
    static void m197a(ah ahVar, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        C0142b.m129c(parcel, 1, ahVar.m690i());
        C0142b.m128b(parcel, 2, ahVar.ai(), false);
        C0142b.m119a(parcel, 3, ahVar.aj(), false);
        C0142b.m110C(parcel, d);
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return m198k(x0);
    }

    public ah m198k(Parcel parcel) {
        String str = null;
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
                    arrayList = C0141a.m82c(parcel, b, C1309a.CREATOR);
                    break;
                case 3:
                    str = C0141a.m92l(parcel, b);
                    break;
                default:
                    C0141a.m79b(parcel, b);
                    break;
            }
        }
        if (parcel.dataPosition() == c) {
            return new ah(i, arrayList, str);
        }
        throw new C0140a("Overread allowed size end=" + c, parcel);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return m199t(x0);
    }

    public ah[] m199t(int i) {
        return new ah[i];
    }
}
