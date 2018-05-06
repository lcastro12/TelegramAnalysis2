package com.google.android.gms.games.multiplayer;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.C0141a;
import com.google.android.gms.common.internal.safeparcel.C0141a.C0140a;
import com.google.android.gms.common.internal.safeparcel.C0142b;
import com.google.android.gms.games.GameEntity;
import com.google.android.gms.location.LocationStatusCodes;
import java.util.ArrayList;

public class C0154a implements Creator<InvitationEntity> {
    static void m152a(InvitationEntity invitationEntity, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        C0142b.m118a(parcel, 1, invitationEntity.getGame(), i, false);
        C0142b.m129c(parcel, LocationStatusCodes.GEOFENCE_NOT_AVAILABLE, invitationEntity.m1375i());
        C0142b.m119a(parcel, 2, invitationEntity.getInvitationId(), false);
        C0142b.m114a(parcel, 3, invitationEntity.getCreationTimestamp());
        C0142b.m129c(parcel, 4, invitationEntity.aL());
        C0142b.m118a(parcel, 5, invitationEntity.getInviter(), i, false);
        C0142b.m128b(parcel, 6, invitationEntity.getParticipants(), false);
        C0142b.m129c(parcel, 7, invitationEntity.getVariant());
        C0142b.m110C(parcel, d);
    }

    public InvitationEntity[] m153H(int i) {
        return new InvitationEntity[i];
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return mo1078p(x0);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return m153H(x0);
    }

    public InvitationEntity mo1078p(Parcel parcel) {
        int i = 0;
        ArrayList arrayList = null;
        int c = C0141a.m81c(parcel);
        long j = 0;
        ParticipantEntity participantEntity = null;
        int i2 = 0;
        String str = null;
        GameEntity gameEntity = null;
        int i3 = 0;
        while (parcel.dataPosition() < c) {
            int b = C0141a.m78b(parcel);
            switch (C0141a.m93m(b)) {
                case 1:
                    gameEntity = (GameEntity) C0141a.m75a(parcel, b, GameEntity.CREATOR);
                    break;
                case 2:
                    str = C0141a.m92l(parcel, b);
                    break;
                case 3:
                    j = C0141a.m87g(parcel, b);
                    break;
                case 4:
                    i2 = C0141a.m86f(parcel, b);
                    break;
                case 5:
                    participantEntity = (ParticipantEntity) C0141a.m75a(parcel, b, ParticipantEntity.CREATOR);
                    break;
                case 6:
                    arrayList = C0141a.m82c(parcel, b, ParticipantEntity.CREATOR);
                    break;
                case 7:
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
            return new InvitationEntity(i3, gameEntity, str, j, i2, participantEntity, arrayList, i);
        }
        throw new C0140a("Overread allowed size end=" + c, parcel);
    }
}
