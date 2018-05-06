package com.google.android.gms.maps.model;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.C0141a;
import com.google.android.gms.common.internal.safeparcel.C0141a.C0140a;
import com.google.android.gms.common.internal.safeparcel.C0142b;

public class CircleOptionsCreator implements Creator<CircleOptions> {
    public static final int CONTENT_DESCRIPTION = 0;

    static void m563a(CircleOptions circleOptions, Parcel parcel, int i) {
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

    public CircleOptions createFromParcel(Parcel parcel) {
        float f = 0.0f;
        boolean z = false;
        int c = C0141a.m81c(parcel);
        LatLng latLng = null;
        double d = 0.0d;
        int i = 0;
        int i2 = 0;
        float f2 = 0.0f;
        int i3 = 0;
        while (parcel.dataPosition() < c) {
            int b = C0141a.m78b(parcel);
            switch (C0141a.m93m(b)) {
                case 1:
                    i3 = C0141a.m86f(parcel, b);
                    break;
                case 2:
                    latLng = (LatLng) C0141a.m75a(parcel, b, LatLng.CREATOR);
                    break;
                case 3:
                    d = C0141a.m90j(parcel, b);
                    break;
                case 4:
                    f2 = C0141a.m89i(parcel, b);
                    break;
                case 5:
                    i2 = C0141a.m86f(parcel, b);
                    break;
                case 6:
                    i = C0141a.m86f(parcel, b);
                    break;
                case 7:
                    f = C0141a.m89i(parcel, b);
                    break;
                case 8:
                    z = C0141a.m83c(parcel, b);
                    break;
                default:
                    C0141a.m79b(parcel, b);
                    break;
            }
        }
        if (parcel.dataPosition() == c) {
            return new CircleOptions(i3, latLng, d, f2, i2, i, f, z);
        }
        throw new C0140a("Overread allowed size end=" + c, parcel);
    }

    public CircleOptions[] newArray(int size) {
        return new CircleOptions[size];
    }
}
