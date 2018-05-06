package com.google.android.gms.maps.model;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.C0141a;
import com.google.android.gms.common.internal.safeparcel.C0141a.C0140a;
import com.google.android.gms.common.internal.safeparcel.C0142b;

public class LatLngBoundsCreator implements Creator<LatLngBounds> {
    public static final int CONTENT_DESCRIPTION = 0;

    static void m566a(LatLngBounds latLngBounds, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        C0142b.m129c(parcel, 1, latLngBounds.m1095i());
        C0142b.m118a(parcel, 2, latLngBounds.southwest, i, false);
        C0142b.m118a(parcel, 3, latLngBounds.northeast, i, false);
        C0142b.m110C(parcel, d);
    }

    public LatLngBounds createFromParcel(Parcel parcel) {
        LatLng latLng = null;
        int c = C0141a.m81c(parcel);
        int i = 0;
        LatLng latLng2 = null;
        while (parcel.dataPosition() < c) {
            int f;
            LatLng latLng3;
            int b = C0141a.m78b(parcel);
            LatLng latLng4;
            switch (C0141a.m93m(b)) {
                case 1:
                    latLng4 = latLng;
                    latLng = latLng2;
                    f = C0141a.m86f(parcel, b);
                    latLng3 = latLng4;
                    break;
                case 2:
                    f = i;
                    latLng4 = (LatLng) C0141a.m75a(parcel, b, LatLng.CREATOR);
                    latLng3 = latLng;
                    latLng = latLng4;
                    break;
                case 3:
                    latLng3 = (LatLng) C0141a.m75a(parcel, b, LatLng.CREATOR);
                    latLng = latLng2;
                    f = i;
                    break;
                default:
                    C0141a.m79b(parcel, b);
                    latLng3 = latLng;
                    latLng = latLng2;
                    f = i;
                    break;
            }
            i = f;
            latLng2 = latLng;
            latLng = latLng3;
        }
        if (parcel.dataPosition() == c) {
            return new LatLngBounds(i, latLng2, latLng);
        }
        throw new C0140a("Overread allowed size end=" + c, parcel);
    }

    public LatLngBounds[] newArray(int size) {
        return new LatLngBounds[size];
    }
}
