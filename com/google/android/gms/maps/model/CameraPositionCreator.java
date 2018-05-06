package com.google.android.gms.maps.model;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.C0141a;
import com.google.android.gms.common.internal.safeparcel.C0141a.C0140a;
import com.google.android.gms.common.internal.safeparcel.C0142b;

public class CameraPositionCreator implements Creator<CameraPosition> {
    public static final int CONTENT_DESCRIPTION = 0;

    static void m562a(CameraPosition cameraPosition, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        C0142b.m129c(parcel, 1, cameraPosition.m1084i());
        C0142b.m118a(parcel, 2, cameraPosition.target, i, false);
        C0142b.m113a(parcel, 3, cameraPosition.zoom);
        C0142b.m113a(parcel, 4, cameraPosition.tilt);
        C0142b.m113a(parcel, 5, cameraPosition.bearing);
        C0142b.m110C(parcel, d);
    }

    public CameraPosition createFromParcel(Parcel parcel) {
        float f = 0.0f;
        int c = C0141a.m81c(parcel);
        int i = 0;
        LatLng latLng = null;
        float f2 = 0.0f;
        float f3 = 0.0f;
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
                    f3 = C0141a.m89i(parcel, b);
                    break;
                case 4:
                    f2 = C0141a.m89i(parcel, b);
                    break;
                case 5:
                    f = C0141a.m89i(parcel, b);
                    break;
                default:
                    C0141a.m79b(parcel, b);
                    break;
            }
        }
        if (parcel.dataPosition() == c) {
            return new CameraPosition(i, latLng, f3, f2, f);
        }
        throw new C0140a("Overread allowed size end=" + c, parcel);
    }

    public CameraPosition[] newArray(int size) {
        return new CameraPosition[size];
    }
}
