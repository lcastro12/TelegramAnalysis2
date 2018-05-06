package com.google.android.gms.maps.model;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.C0141a;
import com.google.android.gms.common.internal.safeparcel.C0141a.C0140a;
import com.google.android.gms.common.internal.safeparcel.C0142b;

public class GroundOverlayOptionsCreator implements Creator<GroundOverlayOptions> {
    public static final int CONTENT_DESCRIPTION = 0;

    static void m564a(GroundOverlayOptions groundOverlayOptions, Parcel parcel, int i) {
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

    public GroundOverlayOptions createFromParcel(Parcel parcel) {
        int c = C0141a.m81c(parcel);
        int i = 0;
        IBinder iBinder = null;
        LatLng latLng = null;
        float f = 0.0f;
        float f2 = 0.0f;
        LatLngBounds latLngBounds = null;
        float f3 = 0.0f;
        float f4 = 0.0f;
        boolean z = false;
        float f5 = 0.0f;
        float f6 = 0.0f;
        float f7 = 0.0f;
        while (parcel.dataPosition() < c) {
            int b = C0141a.m78b(parcel);
            switch (C0141a.m93m(b)) {
                case 1:
                    i = C0141a.m86f(parcel, b);
                    break;
                case 2:
                    iBinder = C0141a.m94m(parcel, b);
                    break;
                case 3:
                    latLng = (LatLng) C0141a.m75a(parcel, b, LatLng.CREATOR);
                    break;
                case 4:
                    f = C0141a.m89i(parcel, b);
                    break;
                case 5:
                    f2 = C0141a.m89i(parcel, b);
                    break;
                case 6:
                    latLngBounds = (LatLngBounds) C0141a.m75a(parcel, b, LatLngBounds.CREATOR);
                    break;
                case 7:
                    f3 = C0141a.m89i(parcel, b);
                    break;
                case 8:
                    f4 = C0141a.m89i(parcel, b);
                    break;
                case 9:
                    z = C0141a.m83c(parcel, b);
                    break;
                case 10:
                    f5 = C0141a.m89i(parcel, b);
                    break;
                case 11:
                    f6 = C0141a.m89i(parcel, b);
                    break;
                case 12:
                    f7 = C0141a.m89i(parcel, b);
                    break;
                default:
                    C0141a.m79b(parcel, b);
                    break;
            }
        }
        if (parcel.dataPosition() == c) {
            return new GroundOverlayOptions(i, iBinder, latLng, f, f2, latLngBounds, f3, f4, z, f5, f6, f7);
        }
        throw new C0140a("Overread allowed size end=" + c, parcel);
    }

    public GroundOverlayOptions[] newArray(int size) {
        return new GroundOverlayOptions[size];
    }
}
