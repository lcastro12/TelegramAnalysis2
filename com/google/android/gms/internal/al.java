package com.google.android.gms.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.C0141a;
import com.google.android.gms.common.internal.safeparcel.C0141a.C0140a;
import com.google.android.gms.common.internal.safeparcel.C0142b;

public class al implements Creator<ak> {
    static void m203a(ak akVar, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        C0142b.m129c(parcel, 1, akVar.m705i());
        C0142b.m117a(parcel, 2, akVar.al(), false);
        C0142b.m118a(parcel, 3, akVar.am(), i, false);
        C0142b.m110C(parcel, d);
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return m204m(x0);
    }

    public ak m204m(Parcel parcel) {
        ah ahVar = null;
        int c = C0141a.m81c(parcel);
        int i = 0;
        Parcel parcel2 = null;
        while (parcel.dataPosition() < c) {
            int b = C0141a.m78b(parcel);
            switch (C0141a.m93m(b)) {
                case 1:
                    i = C0141a.m86f(parcel, b);
                    break;
                case 2:
                    parcel2 = C0141a.m106y(parcel, b);
                    break;
                case 3:
                    ahVar = (ah) C0141a.m75a(parcel, b, ah.CREATOR);
                    break;
                default:
                    C0141a.m79b(parcel, b);
                    break;
            }
        }
        if (parcel.dataPosition() == c) {
            return new ak(i, parcel2, ahVar);
        }
        throw new C0140a("Overread allowed size end=" + c, parcel);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return m205v(x0);
    }

    public ak[] m205v(int i) {
        return new ak[i];
    }
}
