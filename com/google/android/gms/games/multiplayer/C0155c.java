package com.google.android.gms.games.multiplayer;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.C0141a;
import com.google.android.gms.common.internal.safeparcel.C0141a.C0140a;
import com.google.android.gms.common.internal.safeparcel.C0142b;
import com.google.android.gms.games.PlayerEntity;
import com.google.android.gms.location.LocationStatusCodes;

public class C0155c implements Creator<ParticipantEntity> {
    static void m155a(ParticipantEntity participantEntity, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        C0142b.m119a(parcel, 1, participantEntity.getParticipantId(), false);
        C0142b.m129c(parcel, LocationStatusCodes.GEOFENCE_NOT_AVAILABLE, participantEntity.m1381i());
        C0142b.m119a(parcel, 2, participantEntity.getDisplayName(), false);
        C0142b.m118a(parcel, 3, participantEntity.getIconImageUri(), i, false);
        C0142b.m118a(parcel, 4, participantEntity.getHiResImageUri(), i, false);
        C0142b.m129c(parcel, 5, participantEntity.getStatus());
        C0142b.m119a(parcel, 6, participantEntity.aM(), false);
        C0142b.m122a(parcel, 7, participantEntity.isConnectedToRoom());
        C0142b.m118a(parcel, 8, participantEntity.getPlayer(), i, false);
        C0142b.m129c(parcel, 9, participantEntity.aN());
        C0142b.m110C(parcel, d);
    }

    public ParticipantEntity[] m156I(int i) {
        return new ParticipantEntity[i];
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return mo1080q(x0);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return m156I(x0);
    }

    public ParticipantEntity mo1080q(Parcel parcel) {
        int i = 0;
        PlayerEntity playerEntity = null;
        int c = C0141a.m81c(parcel);
        boolean z = false;
        String str = null;
        int i2 = 0;
        Uri uri = null;
        Uri uri2 = null;
        String str2 = null;
        String str3 = null;
        int i3 = 0;
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
                    uri2 = (Uri) C0141a.m75a(parcel, b, Uri.CREATOR);
                    break;
                case 4:
                    uri = (Uri) C0141a.m75a(parcel, b, Uri.CREATOR);
                    break;
                case 5:
                    i2 = C0141a.m86f(parcel, b);
                    break;
                case 6:
                    str = C0141a.m92l(parcel, b);
                    break;
                case 7:
                    z = C0141a.m83c(parcel, b);
                    break;
                case 8:
                    playerEntity = (PlayerEntity) C0141a.m75a(parcel, b, PlayerEntity.CREATOR);
                    break;
                case 9:
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
            return new ParticipantEntity(i3, str3, str2, uri2, uri, i2, str, z, playerEntity, i);
        }
        throw new C0140a("Overread allowed size end=" + c, parcel);
    }
}
