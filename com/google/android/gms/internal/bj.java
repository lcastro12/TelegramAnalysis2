package com.google.android.gms.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.C0141a;
import com.google.android.gms.common.internal.safeparcel.C0141a.C0140a;
import com.google.android.gms.common.internal.safeparcel.C0142b;
import com.google.android.gms.location.LocationStatusCodes;

public class bj implements Creator<bi> {
    static void m348a(bi biVar, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        C0142b.m119a(parcel, 1, biVar.getRequestId(), false);
        C0142b.m129c(parcel, LocationStatusCodes.GEOFENCE_NOT_AVAILABLE, biVar.m876i());
        C0142b.m114a(parcel, 2, biVar.getExpirationTime());
        C0142b.m121a(parcel, 3, biVar.aT());
        C0142b.m112a(parcel, 4, biVar.getLatitude());
        C0142b.m112a(parcel, 5, biVar.getLongitude());
        C0142b.m113a(parcel, 6, biVar.aU());
        C0142b.m129c(parcel, 7, biVar.aV());
        C0142b.m110C(parcel, d);
    }

    public bi[] m349R(int i) {
        return new bi[i];
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return m350t(x0);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return m349R(x0);
    }

    public bi m350t(Parcel parcel) {
        double d = 0.0d;
        short s = (short) 0;
        int c = C0141a.m81c(parcel);
        String str = null;
        float f = 0.0f;
        long j = 0;
        double d2 = 0.0d;
        int i = 0;
        int i2 = 0;
        while (parcel.dataPosition() < c) {
            int b = C0141a.m78b(parcel);
            switch (C0141a.m93m(b)) {
                case 1:
                    str = C0141a.m92l(parcel, b);
                    break;
                case 2:
                    j = C0141a.m87g(parcel, b);
                    break;
                case 3:
                    s = C0141a.m85e(parcel, b);
                    break;
                case 4:
                    d2 = C0141a.m90j(parcel, b);
                    break;
                case 5:
                    d = C0141a.m90j(parcel, b);
                    break;
                case 6:
                    f = C0141a.m89i(parcel, b);
                    break;
                case 7:
                    i = C0141a.m86f(parcel, b);
                    break;
                case LocationStatusCodes.GEOFENCE_NOT_AVAILABLE /*1000*/:
                    i2 = C0141a.m86f(parcel, b);
                    break;
                default:
                    C0141a.m79b(parcel, b);
                    break;
            }
        }
        if (parcel.dataPosition() == c) {
            return new bi(i2, str, i, s, d2, d, f, j);
        }
        throw new C0140a("Overread allowed size end=" + c, parcel);
    }
}
