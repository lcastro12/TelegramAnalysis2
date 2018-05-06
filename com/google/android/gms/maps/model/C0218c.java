package com.google.android.gms.maps.model;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.C0142b;

public class C0218c {
    static void m576a(GroundOverlayOptions groundOverlayOptions, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        C0142b.m129c(parcel, 1, groundOverlayOptions.m1087i());
        C0142b.m116a(parcel, 2, groundOverlayOptions.bp(), false);
        C0142b.m118a(parcel, 3, groundOverlayOptions.getLocation(), i, false);
        C0142b.m113a(parcel, 4, groundOverlayOptions.getWidth());
        C0142b.m113a(parcel, 5, groundOverlayOptions.getHeight());
        C0142b.m118a(parcel, 6, groundOverlayOptions.getBounds(), i, false);
        C0142b.m113a(parcel, 7, groundOverlayOptions.getBearing());
        C0142b.m113a(parcel, 8, groundOverlayOptions.getZIndex());
        C0142b.m122a(parcel, 9, groundOverlayOptions.isVisible());
        C0142b.m113a(parcel, 10, groundOverlayOptions.getTransparency());
        C0142b.m113a(parcel, 11, groundOverlayOptions.getAnchorU());
        C0142b.m113a(parcel, 12, groundOverlayOptions.getAnchorV());
        C0142b.m110C(parcel, d);
    }
}
