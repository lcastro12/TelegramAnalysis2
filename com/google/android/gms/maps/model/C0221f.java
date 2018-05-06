package com.google.android.gms.maps.model;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.C0142b;

public class C0221f {
    static void m579a(MarkerOptions markerOptions, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        C0142b.m129c(parcel, 1, markerOptions.m1096i());
        C0142b.m118a(parcel, 2, markerOptions.getPosition(), i, false);
        C0142b.m119a(parcel, 3, markerOptions.getTitle(), false);
        C0142b.m119a(parcel, 4, markerOptions.getSnippet(), false);
        C0142b.m116a(parcel, 5, markerOptions.bq(), false);
        C0142b.m113a(parcel, 6, markerOptions.getAnchorU());
        C0142b.m113a(parcel, 7, markerOptions.getAnchorV());
        C0142b.m122a(parcel, 8, markerOptions.isDraggable());
        C0142b.m122a(parcel, 9, markerOptions.isVisible());
        C0142b.m110C(parcel, d);
    }
}
