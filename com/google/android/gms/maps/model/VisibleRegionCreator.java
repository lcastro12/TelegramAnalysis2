package com.google.android.gms.maps.model;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.C0141a;
import com.google.android.gms.common.internal.safeparcel.C0141a.C0140a;
import com.google.android.gms.common.internal.safeparcel.C0142b;

public class VisibleRegionCreator implements Creator<VisibleRegion> {
    public static final int CONTENT_DESCRIPTION = 0;

    static void m573a(VisibleRegion visibleRegion, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        C0142b.m129c(parcel, 1, visibleRegion.m1104i());
        C0142b.m118a(parcel, 2, visibleRegion.nearLeft, i, false);
        C0142b.m118a(parcel, 3, visibleRegion.nearRight, i, false);
        C0142b.m118a(parcel, 4, visibleRegion.farLeft, i, false);
        C0142b.m118a(parcel, 5, visibleRegion.farRight, i, false);
        C0142b.m118a(parcel, 6, visibleRegion.latLngBounds, i, false);
        C0142b.m110C(parcel, d);
    }

    public VisibleRegion createFromParcel(Parcel parcel) {
        LatLngBounds latLngBounds = null;
        int c = C0141a.m81c(parcel);
        int i = 0;
        LatLng latLng = null;
        LatLng latLng2 = null;
        LatLng latLng3 = null;
        LatLng latLng4 = null;
        while (parcel.dataPosition() < c) {
            int b = C0141a.m78b(parcel);
            switch (C0141a.m93m(b)) {
                case 1:
                    i = C0141a.m86f(parcel, b);
                    break;
                case 2:
                    latLng4 = (LatLng) C0141a.m75a(parcel, b, LatLng.CREATOR);
                    break;
                case 3:
                    latLng3 = (LatLng) C0141a.m75a(parcel, b, LatLng.CREATOR);
                    break;
                case 4:
                    latLng2 = (LatLng) C0141a.m75a(parcel, b, LatLng.CREATOR);
                    break;
                case 5:
                    latLng = (LatLng) C0141a.m75a(parcel, b, LatLng.CREATOR);
                    break;
                case 6:
                    latLngBounds = (LatLngBounds) C0141a.m75a(parcel, b, LatLngBounds.CREATOR);
                    break;
                default:
                    C0141a.m79b(parcel, b);
                    break;
            }
        }
        if (parcel.dataPosition() == c) {
            return new VisibleRegion(i, latLng4, latLng3, latLng2, latLng, latLngBounds);
        }
        throw new C0140a("Overread allowed size end=" + c, parcel);
    }

    public VisibleRegion[] newArray(int size) {
        return new VisibleRegion[size];
    }
}
