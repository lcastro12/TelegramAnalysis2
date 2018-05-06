package com.google.android.gms.location;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.C0141a;
import com.google.android.gms.common.internal.safeparcel.C0141a.C0140a;
import com.google.android.gms.common.internal.safeparcel.C0142b;

public class DetectedActivityCreator implements Creator<DetectedActivity> {
    public static final int CONTENT_DESCRIPTION = 0;

    static void m533a(DetectedActivity detectedActivity, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        C0142b.m129c(parcel, 1, detectedActivity.fs);
        C0142b.m129c(parcel, LocationStatusCodes.GEOFENCE_NOT_AVAILABLE, detectedActivity.m1038i());
        C0142b.m129c(parcel, 2, detectedActivity.ft);
        C0142b.m110C(parcel, d);
    }

    public DetectedActivity createFromParcel(Parcel parcel) {
        int i = 0;
        int c = C0141a.m81c(parcel);
        int i2 = 0;
        int i3 = 0;
        while (parcel.dataPosition() < c) {
            int b = C0141a.m78b(parcel);
            switch (C0141a.m93m(b)) {
                case 1:
                    i2 = C0141a.m86f(parcel, b);
                    break;
                case 2:
                    i = C0141a.m86f(parcel, b);
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
            return new DetectedActivity(i3, i2, i);
        }
        throw new C0140a("Overread allowed size end=" + c, parcel);
    }

    public DetectedActivity[] newArray(int size) {
        return new DetectedActivity[size];
    }
}
