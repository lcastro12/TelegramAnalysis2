package com.google.android.gms.maps.model;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.C0142b;

public class C0217b {
    static void m575a(CircleOptions circleOptions, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        C0142b.m129c(parcel, 1, circleOptions.m1085i());
        C0142b.m118a(parcel, 2, circleOptions.getCenter(), i, false);
        C0142b.m112a(parcel, 3, circleOptions.getRadius());
        C0142b.m113a(parcel, 4, circleOptions.getStrokeWidth());
        C0142b.m129c(parcel, 5, circleOptions.getStrokeColor());
        C0142b.m129c(parcel, 6, circleOptions.getFillColor());
        C0142b.m113a(parcel, 7, circleOptions.getZIndex());
        C0142b.m122a(parcel, 8, circleOptions.isVisible());
        C0142b.m110C(parcel, d);
    }
}
