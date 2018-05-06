package com.google.android.gms.maps.model;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.C0142b;

public class C0216a {
    static void m574a(CameraPosition cameraPosition, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        C0142b.m129c(parcel, 1, cameraPosition.m1084i());
        C0142b.m118a(parcel, 2, cameraPosition.target, i, false);
        C0142b.m113a(parcel, 3, cameraPosition.zoom);
        C0142b.m113a(parcel, 4, cameraPosition.tilt);
        C0142b.m113a(parcel, 5, cameraPosition.bearing);
        C0142b.m110C(parcel, d);
    }
}
