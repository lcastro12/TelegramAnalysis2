package com.google.android.gms.maps.model;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.C0142b;

public class C0232j {
    static void m596a(TileOverlayOptions tileOverlayOptions, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        C0142b.m129c(parcel, 1, tileOverlayOptions.m1101i());
        C0142b.m116a(parcel, 2, tileOverlayOptions.bs(), false);
        C0142b.m122a(parcel, 3, tileOverlayOptions.isVisible());
        C0142b.m113a(parcel, 4, tileOverlayOptions.getZIndex());
        C0142b.m110C(parcel, d);
    }
}
