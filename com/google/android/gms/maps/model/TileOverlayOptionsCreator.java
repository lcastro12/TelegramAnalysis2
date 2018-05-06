package com.google.android.gms.maps.model;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.C0141a;
import com.google.android.gms.common.internal.safeparcel.C0141a.C0140a;
import com.google.android.gms.common.internal.safeparcel.C0142b;

public class TileOverlayOptionsCreator implements Creator<TileOverlayOptions> {
    public static final int CONTENT_DESCRIPTION = 0;

    static void m572a(TileOverlayOptions tileOverlayOptions, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        C0142b.m129c(parcel, 1, tileOverlayOptions.m1101i());
        C0142b.m116a(parcel, 2, tileOverlayOptions.bs(), false);
        C0142b.m122a(parcel, 3, tileOverlayOptions.isVisible());
        C0142b.m113a(parcel, 4, tileOverlayOptions.getZIndex());
        C0142b.m110C(parcel, d);
    }

    public TileOverlayOptions createFromParcel(Parcel parcel) {
        boolean z = false;
        int c = C0141a.m81c(parcel);
        IBinder iBinder = null;
        float f = 0.0f;
        int i = 0;
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
                    z = C0141a.m83c(parcel, b);
                    break;
                case 4:
                    f = C0141a.m89i(parcel, b);
                    break;
                default:
                    C0141a.m79b(parcel, b);
                    break;
            }
        }
        if (parcel.dataPosition() == c) {
            return new TileOverlayOptions(i, iBinder, z, f);
        }
        throw new C0140a("Overread allowed size end=" + c, parcel);
    }

    public TileOverlayOptions[] newArray(int size) {
        return new TileOverlayOptions[size];
    }
}
