package com.google.android.gms.maps.model;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.C0142b;

public class C0233k {
    static void m597a(VisibleRegion visibleRegion, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        C0142b.m129c(parcel, 1, visibleRegion.m1104i());
        C0142b.m118a(parcel, 2, visibleRegion.nearLeft, i, false);
        C0142b.m118a(parcel, 3, visibleRegion.nearRight, i, false);
        C0142b.m118a(parcel, 4, visibleRegion.farLeft, i, false);
        C0142b.m118a(parcel, 5, visibleRegion.farRight, i, false);
        C0142b.m118a(parcel, 6, visibleRegion.latLngBounds, i, false);
        C0142b.m110C(parcel, d);
    }
}
