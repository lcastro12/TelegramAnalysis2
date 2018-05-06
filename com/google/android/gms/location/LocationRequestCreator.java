package com.google.android.gms.location;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.C0141a;
import com.google.android.gms.common.internal.safeparcel.C0141a.C0140a;
import com.google.android.gms.common.internal.safeparcel.C0142b;
import org.telegram.tgnet.ConnectionsManager;

public class LocationRequestCreator implements Creator<LocationRequest> {
    public static final int CONTENT_DESCRIPTION = 0;

    static void m534a(LocationRequest locationRequest, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        C0142b.m129c(parcel, 1, locationRequest.mPriority);
        C0142b.m129c(parcel, LocationStatusCodes.GEOFENCE_NOT_AVAILABLE, locationRequest.m1043i());
        C0142b.m114a(parcel, 2, locationRequest.fB);
        C0142b.m114a(parcel, 3, locationRequest.fC);
        C0142b.m122a(parcel, 4, locationRequest.fD);
        C0142b.m114a(parcel, 5, locationRequest.fw);
        C0142b.m129c(parcel, 6, locationRequest.fE);
        C0142b.m113a(parcel, 7, locationRequest.fF);
        C0142b.m110C(parcel, d);
    }

    public LocationRequest createFromParcel(Parcel parcel) {
        boolean z = false;
        int c = C0141a.m81c(parcel);
        int i = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
        long j = 3600000;
        long j2 = 600000;
        long j3 = Long.MAX_VALUE;
        int i2 = ConnectionsManager.DEFAULT_DATACENTER_ID;
        float f = 0.0f;
        int i3 = 0;
        while (parcel.dataPosition() < c) {
            int b = C0141a.m78b(parcel);
            switch (C0141a.m93m(b)) {
                case 1:
                    i = C0141a.m86f(parcel, b);
                    break;
                case 2:
                    j = C0141a.m87g(parcel, b);
                    break;
                case 3:
                    j2 = C0141a.m87g(parcel, b);
                    break;
                case 4:
                    z = C0141a.m83c(parcel, b);
                    break;
                case 5:
                    j3 = C0141a.m87g(parcel, b);
                    break;
                case 6:
                    i2 = C0141a.m86f(parcel, b);
                    break;
                case 7:
                    f = C0141a.m89i(parcel, b);
                    break;
                case LocationStatusCodes.GEOFENCE_NOT_AVAILABLE /*1000*/:
                    i3 = C0141a.m86f(parcel, b);
                    break;
                default:
                    C0141a.m79b(parcel, b);
                    break;
            }
        }
        if (parcel.dataPosition() == c) {
            return new LocationRequest(i3, i, j, j2, z, j3, i2, f);
        }
        throw new C0140a("Overread allowed size end=" + c, parcel);
    }

    public LocationRequest[] newArray(int size) {
        return new LocationRequest[size];
    }
}
