package com.google.android.gms.location;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.C0141a;
import com.google.android.gms.common.internal.safeparcel.C0141a.C0140a;
import com.google.android.gms.common.internal.safeparcel.C0142b;
import java.util.List;

public class ActivityRecognitionResultCreator implements Creator<ActivityRecognitionResult> {
    public static final int CONTENT_DESCRIPTION = 0;

    static void m532a(ActivityRecognitionResult activityRecognitionResult, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        C0142b.m128b(parcel, 1, activityRecognitionResult.fp, false);
        C0142b.m129c(parcel, LocationStatusCodes.GEOFENCE_NOT_AVAILABLE, activityRecognitionResult.m1036i());
        C0142b.m114a(parcel, 2, activityRecognitionResult.fq);
        C0142b.m114a(parcel, 3, activityRecognitionResult.fr);
        C0142b.m110C(parcel, d);
    }

    public ActivityRecognitionResult createFromParcel(Parcel parcel) {
        long j = 0;
        int c = C0141a.m81c(parcel);
        int i = 0;
        List list = null;
        long j2 = 0;
        while (parcel.dataPosition() < c) {
            int b = C0141a.m78b(parcel);
            switch (C0141a.m93m(b)) {
                case 1:
                    list = C0141a.m82c(parcel, b, DetectedActivity.CREATOR);
                    break;
                case 2:
                    j2 = C0141a.m87g(parcel, b);
                    break;
                case 3:
                    j = C0141a.m87g(parcel, b);
                    break;
                case LocationStatusCodes.GEOFENCE_NOT_AVAILABLE /*1000*/:
                    i = C0141a.m86f(parcel, b);
                    break;
                default:
                    C0141a.m79b(parcel, b);
                    break;
            }
        }
        if (parcel.dataPosition() == c) {
            return new ActivityRecognitionResult(i, list, j2, j);
        }
        throw new C0140a("Overread allowed size end=" + c, parcel);
    }

    public ActivityRecognitionResult[] newArray(int size) {
        return new ActivityRecognitionResult[size];
    }
}
