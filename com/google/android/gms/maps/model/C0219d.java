package com.google.android.gms.maps.model;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.C0142b;

public class C0219d {
    static void m577a(LatLngBounds latLngBounds, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        C0142b.m129c(parcel, 1, latLngBounds.m1095i());
        C0142b.m118a(parcel, 2, latLngBounds.southwest, i, false);
        C0142b.m118a(parcel, 3, latLngBounds.northeast, i, false);
        C0142b.m110C(parcel, d);
    }
}
