package com.google.android.gms.maps.model;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.C0142b;

public class C0222g {
    static void m580a(PolygonOptions polygonOptions, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        C0142b.m129c(parcel, 1, polygonOptions.m1097i());
        C0142b.m128b(parcel, 2, polygonOptions.getPoints(), false);
        C0142b.m130c(parcel, 3, polygonOptions.br(), false);
        C0142b.m113a(parcel, 4, polygonOptions.getStrokeWidth());
        C0142b.m129c(parcel, 5, polygonOptions.getStrokeColor());
        C0142b.m129c(parcel, 6, polygonOptions.getFillColor());
        C0142b.m113a(parcel, 7, polygonOptions.getZIndex());
        C0142b.m122a(parcel, 8, polygonOptions.isVisible());
        C0142b.m122a(parcel, 9, polygonOptions.isGeodesic());
        C0142b.m110C(parcel, d);
    }
}
