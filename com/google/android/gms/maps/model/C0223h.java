package com.google.android.gms.maps.model;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.C0142b;

public class C0223h {
    static void m581a(PolylineOptions polylineOptions, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        C0142b.m129c(parcel, 1, polylineOptions.m1098i());
        C0142b.m128b(parcel, 2, polylineOptions.getPoints(), false);
        C0142b.m113a(parcel, 3, polylineOptions.getWidth());
        C0142b.m129c(parcel, 4, polylineOptions.getColor());
        C0142b.m113a(parcel, 5, polylineOptions.getZIndex());
        C0142b.m122a(parcel, 6, polylineOptions.isVisible());
        C0142b.m122a(parcel, 7, polylineOptions.isGeodesic());
        C0142b.m110C(parcel, d);
    }
}
