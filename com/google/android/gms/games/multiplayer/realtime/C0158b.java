package com.google.android.gms.games.multiplayer.realtime;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.C0141a;
import com.google.android.gms.common.internal.safeparcel.C0141a.C0140a;
import com.google.android.gms.common.internal.safeparcel.C0142b;
import com.google.android.gms.games.multiplayer.ParticipantEntity;
import com.google.android.gms.location.LocationStatusCodes;
import java.util.ArrayList;

public class C0158b implements Creator<RoomEntity> {
    static void m160a(RoomEntity roomEntity, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        C0142b.m119a(parcel, 1, roomEntity.getRoomId(), false);
        C0142b.m129c(parcel, LocationStatusCodes.GEOFENCE_NOT_AVAILABLE, roomEntity.m1387i());
        C0142b.m119a(parcel, 2, roomEntity.getCreatorId(), false);
        C0142b.m114a(parcel, 3, roomEntity.getCreationTimestamp());
        C0142b.m129c(parcel, 4, roomEntity.getStatus());
        C0142b.m119a(parcel, 5, roomEntity.getDescription(), false);
        C0142b.m129c(parcel, 6, roomEntity.getVariant());
        C0142b.m115a(parcel, 7, roomEntity.getAutoMatchCriteria(), false);
        C0142b.m128b(parcel, 8, roomEntity.getParticipants(), false);
        C0142b.m129c(parcel, 9, roomEntity.getAutoMatchWaitEstimateSeconds());
        C0142b.m110C(parcel, d);
    }

    public RoomEntity[] m161K(int i) {
        return new RoomEntity[i];
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return mo1082s(x0);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return m161K(x0);
    }

    public RoomEntity mo1082s(Parcel parcel) {
        int i = 0;
        ArrayList arrayList = null;
        int c = C0141a.m81c(parcel);
        long j = 0;
        Bundle bundle = null;
        int i2 = 0;
        String str = null;
        int i3 = 0;
        String str2 = null;
        String str3 = null;
        int i4 = 0;
        while (parcel.dataPosition() < c) {
            int b = C0141a.m78b(parcel);
            switch (C0141a.m93m(b)) {
                case 1:
                    str3 = C0141a.m92l(parcel, b);
                    break;
                case 2:
                    str2 = C0141a.m92l(parcel, b);
                    break;
                case 3:
                    j = C0141a.m87g(parcel, b);
                    break;
                case 4:
                    i3 = C0141a.m86f(parcel, b);
                    break;
                case 5:
                    str = C0141a.m92l(parcel, b);
                    break;
                case 6:
                    i2 = C0141a.m86f(parcel, b);
                    break;
                case 7:
                    bundle = C0141a.m95n(parcel, b);
                    break;
                case 8:
                    arrayList = C0141a.m82c(parcel, b, ParticipantEntity.CREATOR);
                    break;
                case 9:
                    i = C0141a.m86f(parcel, b);
                    break;
                case LocationStatusCodes.GEOFENCE_NOT_AVAILABLE /*1000*/:
                    i4 = C0141a.m86f(parcel, b);
                    break;
                default:
                    C0141a.m79b(parcel, b);
                    break;
            }
        }
        if (parcel.dataPosition() == c) {
            return new RoomEntity(i4, str3, str2, j, i3, str, i2, bundle, arrayList, i);
        }
        throw new C0140a("Overread allowed size end=" + c, parcel);
    }
}
