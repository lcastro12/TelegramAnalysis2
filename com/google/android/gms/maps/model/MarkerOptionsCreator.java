package com.google.android.gms.maps.model;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.C0141a;
import com.google.android.gms.common.internal.safeparcel.C0141a.C0140a;
import com.google.android.gms.common.internal.safeparcel.C0142b;

public class MarkerOptionsCreator implements Creator<MarkerOptions> {
    public static final int CONTENT_DESCRIPTION = 0;

    static void m568a(MarkerOptions markerOptions, Parcel parcel, int i) {
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
        C0142b.m122a(parcel, 10, markerOptions.isFlat());
        C0142b.m113a(parcel, 11, markerOptions.getRotation());
        C0142b.m113a(parcel, 12, markerOptions.getInfoWindowAnchorU());
        C0142b.m113a(parcel, 13, markerOptions.getInfoWindowAnchorV());
        C0142b.m110C(parcel, d);
    }

    public MarkerOptions createFromParcel(Parcel parcel) {
        int c = C0141a.m81c(parcel);
        int i = 0;
        LatLng latLng = null;
        String str = null;
        String str2 = null;
        IBinder iBinder = null;
        float f = 0.0f;
        float f2 = 0.0f;
        boolean z = false;
        boolean z2 = false;
        boolean z3 = false;
        float f3 = 0.0f;
        float f4 = 0.5f;
        float f5 = 0.0f;
        while (parcel.dataPosition() < c) {
            int b = C0141a.m78b(parcel);
            switch (C0141a.m93m(b)) {
                case 1:
                    i = C0141a.m86f(parcel, b);
                    break;
                case 2:
                    latLng = (LatLng) C0141a.m75a(parcel, b, LatLng.CREATOR);
                    break;
                case 3:
                    str = C0141a.m92l(parcel, b);
                    break;
                case 4:
                    str2 = C0141a.m92l(parcel, b);
                    break;
                case 5:
                    iBinder = C0141a.m94m(parcel, b);
                    break;
                case 6:
                    f = C0141a.m89i(parcel, b);
                    break;
                case 7:
                    f2 = C0141a.m89i(parcel, b);
                    break;
                case 8:
                    z = C0141a.m83c(parcel, b);
                    break;
                case 9:
                    z2 = C0141a.m83c(parcel, b);
                    break;
                case 10:
                    z3 = C0141a.m83c(parcel, b);
                    break;
                case 11:
                    f3 = C0141a.m89i(parcel, b);
                    break;
                case 12:
                    f4 = C0141a.m89i(parcel, b);
                    break;
                case 13:
                    f5 = C0141a.m89i(parcel, b);
                    break;
                default:
                    C0141a.m79b(parcel, b);
                    break;
            }
        }
        if (parcel.dataPosition() == c) {
            return new MarkerOptions(i, latLng, str, str2, iBinder, f, f2, z, z2, z3, f3, f4, f5);
        }
        throw new C0140a("Overread allowed size end=" + c, parcel);
    }

    public MarkerOptions[] newArray(int size) {
        return new MarkerOptions[size];
    }
}
