package com.google.android.gms.maps.model;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.C0142b;

public class C0224i {
    static void m582a(Tile tile, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        C0142b.m129c(parcel, 1, tile.m1099i());
        C0142b.m129c(parcel, 2, tile.width);
        C0142b.m129c(parcel, 3, tile.height);
        C0142b.m123a(parcel, 4, tile.data, false);
        C0142b.m110C(parcel, d);
    }
}
