package com.google.android.gms.internal;

import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.C0141a;
import com.google.android.gms.common.internal.safeparcel.C0141a.C0140a;
import com.google.android.gms.common.internal.safeparcel.C0142b;
import com.google.android.gms.location.LocationStatusCodes;
import java.util.List;

public class cp implements Creator<co> {
    static void m444a(co coVar, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        C0142b.m119a(parcel, 1, coVar.getId(), false);
        C0142b.m129c(parcel, LocationStatusCodes.GEOFENCE_NOT_AVAILABLE, coVar.m953i());
        C0142b.m128b(parcel, 2, coVar.cB(), false);
        C0142b.m128b(parcel, 3, coVar.cC(), false);
        C0142b.m118a(parcel, 4, coVar.cD(), i, false);
        C0142b.m119a(parcel, 5, coVar.cE(), false);
        C0142b.m119a(parcel, 6, coVar.cF(), false);
        C0142b.m119a(parcel, 7, coVar.cG(), false);
        C0142b.m115a(parcel, 8, coVar.cH(), false);
        C0142b.m115a(parcel, 9, coVar.cI(), false);
        C0142b.m129c(parcel, 10, coVar.cJ());
        C0142b.m110C(parcel, d);
    }

    public co m445I(Parcel parcel) {
        int i = 0;
        Bundle bundle = null;
        int c = C0141a.m81c(parcel);
        Bundle bundle2 = null;
        String str = null;
        String str2 = null;
        String str3 = null;
        Uri uri = null;
        List list = null;
        List list2 = null;
        String str4 = null;
        int i2 = 0;
        while (parcel.dataPosition() < c) {
            int b = C0141a.m78b(parcel);
            switch (C0141a.m93m(b)) {
                case 1:
                    str4 = C0141a.m92l(parcel, b);
                    break;
                case 2:
                    list2 = C0141a.m82c(parcel, b, C1362x.CREATOR);
                    break;
                case 3:
                    list = C0141a.m82c(parcel, b, Uri.CREATOR);
                    break;
                case 4:
                    uri = (Uri) C0141a.m75a(parcel, b, Uri.CREATOR);
                    break;
                case 5:
                    str3 = C0141a.m92l(parcel, b);
                    break;
                case 6:
                    str2 = C0141a.m92l(parcel, b);
                    break;
                case 7:
                    str = C0141a.m92l(parcel, b);
                    break;
                case 8:
                    bundle2 = C0141a.m95n(parcel, b);
                    break;
                case 9:
                    bundle = C0141a.m95n(parcel, b);
                    break;
                case 10:
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
            return new co(i2, str4, list2, list, uri, str3, str2, str, bundle2, bundle, i);
        }
        throw new C0140a("Overread allowed size end=" + c, parcel);
    }

    public co[] ai(int i) {
        return new co[i];
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return m445I(x0);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return ai(x0);
    }
}
