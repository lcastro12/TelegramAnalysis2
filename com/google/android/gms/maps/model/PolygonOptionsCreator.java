package com.google.android.gms.maps.model;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.C0141a;
import com.google.android.gms.common.internal.safeparcel.C0141a.C0140a;
import com.google.android.gms.common.internal.safeparcel.C0142b;
import java.util.ArrayList;
import java.util.List;

public class PolygonOptionsCreator implements Creator<PolygonOptions> {
    public static final int CONTENT_DESCRIPTION = 0;

    static void m569a(PolygonOptions polygonOptions, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        C0142b.m129c(parcel, 1, polygonOptions.m1097i());
        C0142b.m128b(parcel, 2, polygonOptions.getPoints(), false);
        C0142b.m130c(parcel, 3, polygonOptions.br(), false);
        C0142b.m113a(parcel, 4, polygonOptions.getStrokeWidth());
        C0142b.m129c(parcel, 5, polygonOptions.getStrokeColor());
        C0142b.m129c(parcel, 6, polygonOptions.getFillColor());
        C0142b.m113a(parcel, 7, polygonOptions.getZIndex());
        C0142b.m122a(parcel, 8, polygonOptions.isVisible());
        C0142b.m122a(parcel, 9, polygonOptions.isGeodesic());
        C0142b.m110C(parcel, d);
    }

    public PolygonOptions createFromParcel(Parcel parcel) {
        float f = 0.0f;
        boolean z = false;
        int c = C0141a.m81c(parcel);
        List list = null;
        List arrayList = new ArrayList();
        boolean z2 = false;
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
                    list = C0141a.m82c(parcel, b, LatLng.CREATOR);
                    break;
                case 3:
                    C0141a.m77a(parcel, b, arrayList, getClass().getClassLoader());
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
                    z2 = C0141a.m83c(parcel, b);
                    break;
                case 9:
                    z = C0141a.m83c(parcel, b);
                    break;
                default:
                    C0141a.m79b(parcel, b);
                    break;
            }
        }
        if (parcel.dataPosition() == c) {
            return new PolygonOptions(i3, list, arrayList, f2, i2, i, f, z2, z);
        }
        throw new C0140a("Overread allowed size end=" + c, parcel);
    }

    public PolygonOptions[] newArray(int size) {
        return new PolygonOptions[size];
    }
}
