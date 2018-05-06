package com.google.android.gms.games;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.C0141a;
import com.google.android.gms.common.internal.safeparcel.C0141a.C0140a;
import com.google.android.gms.common.internal.safeparcel.C0142b;
import com.google.android.gms.location.LocationStatusCodes;

public class C0152c implements Creator<PlayerEntity> {
    static void m147a(PlayerEntity playerEntity, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        C0142b.m119a(parcel, 1, playerEntity.getPlayerId(), false);
        C0142b.m129c(parcel, LocationStatusCodes.GEOFENCE_NOT_AVAILABLE, playerEntity.m1369i());
        C0142b.m119a(parcel, 2, playerEntity.getDisplayName(), false);
        C0142b.m118a(parcel, 3, playerEntity.getIconImageUri(), i, false);
        C0142b.m118a(parcel, 4, playerEntity.getHiResImageUri(), i, false);
        C0142b.m114a(parcel, 5, playerEntity.getRetrievedTimestamp());
        C0142b.m110C(parcel, d);
    }

    public PlayerEntity[] m148A(int i) {
        return new PlayerEntity[i];
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return mo1045o(x0);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return m148A(x0);
    }

    public PlayerEntity mo1045o(Parcel parcel) {
        Uri uri = null;
        int c = C0141a.m81c(parcel);
        int i = 0;
        long j = 0;
        Uri uri2 = null;
        String str = null;
        String str2 = null;
        while (parcel.dataPosition() < c) {
            int b = C0141a.m78b(parcel);
            switch (C0141a.m93m(b)) {
                case 1:
                    str2 = C0141a.m92l(parcel, b);
                    break;
                case 2:
                    str = C0141a.m92l(parcel, b);
                    break;
                case 3:
                    uri2 = (Uri) C0141a.m75a(parcel, b, Uri.CREATOR);
                    break;
                case 4:
                    uri = (Uri) C0141a.m75a(parcel, b, Uri.CREATOR);
                    break;
                case 5:
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
            return new PlayerEntity(i, str2, str, uri2, uri, j);
        }
        throw new C0140a("Overread allowed size end=" + c, parcel);
    }
}
