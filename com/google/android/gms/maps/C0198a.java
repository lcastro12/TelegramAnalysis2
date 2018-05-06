package com.google.android.gms.maps;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.C0142b;

public class C0198a {
    static void m538a(GoogleMapOptions googleMapOptions, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        C0142b.m129c(parcel, 1, googleMapOptions.m1045i());
        C0142b.m111a(parcel, 2, googleMapOptions.aZ());
        C0142b.m111a(parcel, 3, googleMapOptions.ba());
        C0142b.m129c(parcel, 4, googleMapOptions.getMapType());
        C0142b.m118a(parcel, 5, googleMapOptions.getCamera(), i, false);
        C0142b.m111a(parcel, 6, googleMapOptions.bb());
        C0142b.m111a(parcel, 7, googleMapOptions.bc());
        C0142b.m111a(parcel, 8, googleMapOptions.bd());
        C0142b.m111a(parcel, 9, googleMapOptions.be());
        C0142b.m111a(parcel, 10, googleMapOptions.bf());
        C0142b.m111a(parcel, 11, googleMapOptions.bg());
        C0142b.m110C(parcel, d);
    }
}
