package com.google.android.gms.maps.model;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.C0141a;
import com.google.android.gms.common.internal.safeparcel.C0141a.C0140a;
import com.google.android.gms.common.internal.safeparcel.C0142b;
import java.util.List;

public class PolylineOptionsCreator implements Creator<PolylineOptions> {
    public static final int CONTENT_DESCRIPTION = 0;

    static void m570a(PolylineOptions polylineOptions, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        C0142b.m129c(parcel, 1, polylineOptions.m1098i());
        C0142b.m128b(parcel, 2, polylineOptions.getPoints(), false);
        C0142b.m113a(parcel, 3, polylineOptions.getWidth());
        C0142b.m129c(parcel, 4, polylineOptions.getColor());
        C0142b.m113a(parcel, 5, polylineOptions.getZIndex());
        C0142b.m122a(parcel, 6, polylineOptions.isVisible());
        C0142b.m122a(parcel, 7, polylineOptions.isGeodesic());
        C0142b.m110C(parcel, d);
    }

    public PolylineOptions createFromParcel(Parcel parcel) {
        float f = 0.0f;
        boolean z = false;
        int c = C0141a.m81c(parcel);
        List list = null;
        boolean z2 = false;
        int i = 0;
        float f2 = 0.0f;
        int i2 = 0;
        while (parcel.dataPosition() < c) {
            int b = C0141a.m78b(parcel);
            switch (C0141a.m93m(b)) {
                case 1:
                    i2 = C0141a.m86f(parcel, b);
                    break;
                case 2:
                    list = C0141a.m82c(parcel, b, LatLng.CREATOR);
                    break;
                case 3:
                    f2 = C0141a.m89i(parcel, b);
                    break;
                case 4:
                    i = C0141a.m86f(parcel, b);
                    break;
                case 5:
                    f = C0141a.m89i(parcel, b);
                    break;
                case 6:
                    z2 = C0141a.m83c(parcel, b);
                    break;
                case 7:
                    z = C0141a.m83c(parcel, b);
                    break;
                default:
                    C0141a.m79b(parcel, b);
                    break;
            }
        }
        if (parcel.dataPosition() == c) {
            return new PolylineOptions(i2, list, f2, i, f, z2, z);
        }
        throw new C0140a("Overread allowed size end=" + c, parcel);
    }

    public PolylineOptions[] newArray(int size) {
        return new PolylineOptions[size];
    }
}
