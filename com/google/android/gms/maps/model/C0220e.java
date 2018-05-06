package com.google.android.gms.maps.model;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.C0142b;

public class C0220e {
    static void m578a(LatLng latLng, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        C0142b.m129c(parcel, 1, latLng.m1088i());
        C0142b.m112a(parcel, 2, latLng.latitude);
        C0142b.m112a(parcel, 3, latLng.longitude);
        C0142b.m110C(parcel, d);
    }
}
