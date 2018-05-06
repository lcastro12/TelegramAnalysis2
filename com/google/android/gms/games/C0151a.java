package com.google.android.gms.games;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.C0141a;
import com.google.android.gms.common.internal.safeparcel.C0141a.C0140a;
import com.google.android.gms.common.internal.safeparcel.C0142b;
import com.google.android.gms.location.LocationStatusCodes;

public class C0151a implements Creator<GameEntity> {
    static void m144a(GameEntity gameEntity, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        C0142b.m119a(parcel, 1, gameEntity.getApplicationId(), false);
        C0142b.m119a(parcel, 2, gameEntity.getDisplayName(), false);
        C0142b.m119a(parcel, 3, gameEntity.getPrimaryCategory(), false);
        C0142b.m119a(parcel, 4, gameEntity.getSecondaryCategory(), false);
        C0142b.m119a(parcel, 5, gameEntity.getDescription(), false);
        C0142b.m119a(parcel, 6, gameEntity.getDeveloperName(), false);
        C0142b.m118a(parcel, 7, gameEntity.getIconImageUri(), i, false);
        C0142b.m118a(parcel, 8, gameEntity.getHiResImageUri(), i, false);
        C0142b.m118a(parcel, 9, gameEntity.getFeaturedImageUri(), i, false);
        C0142b.m122a(parcel, 10, gameEntity.isPlayEnabledGame());
        C0142b.m122a(parcel, 11, gameEntity.isInstanceInstalled());
        C0142b.m119a(parcel, 12, gameEntity.getInstancePackageName(), false);
        C0142b.m129c(parcel, 13, gameEntity.getGameplayAclStatus());
        C0142b.m129c(parcel, 14, gameEntity.getAchievementTotalCount());
        C0142b.m129c(parcel, 15, gameEntity.getLeaderboardCount());
        C0142b.m129c(parcel, LocationStatusCodes.GEOFENCE_NOT_AVAILABLE, gameEntity.m1363i());
        C0142b.m110C(parcel, d);
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return mo1043n(x0);
    }

    public GameEntity mo1043n(Parcel parcel) {
        int c = C0141a.m81c(parcel);
        int i = 0;
        String str = null;
        String str2 = null;
        String str3 = null;
        String str4 = null;
        String str5 = null;
        String str6 = null;
        Uri uri = null;
        Uri uri2 = null;
        Uri uri3 = null;
        boolean z = false;
        boolean z2 = false;
        String str7 = null;
        int i2 = 0;
        int i3 = 0;
        int i4 = 0;
        while (parcel.dataPosition() < c) {
            int b = C0141a.m78b(parcel);
            switch (C0141a.m93m(b)) {
                case 1:
                    str = C0141a.m92l(parcel, b);
                    break;
                case 2:
                    str2 = C0141a.m92l(parcel, b);
                    break;
                case 3:
                    str3 = C0141a.m92l(parcel, b);
                    break;
                case 4:
                    str4 = C0141a.m92l(parcel, b);
                    break;
                case 5:
                    str5 = C0141a.m92l(parcel, b);
                    break;
                case 6:
                    str6 = C0141a.m92l(parcel, b);
                    break;
                case 7:
                    uri = (Uri) C0141a.m75a(parcel, b, Uri.CREATOR);
                    break;
                case 8:
                    uri2 = (Uri) C0141a.m75a(parcel, b, Uri.CREATOR);
                    break;
                case 9:
                    uri3 = (Uri) C0141a.m75a(parcel, b, Uri.CREATOR);
                    break;
                case 10:
                    z = C0141a.m83c(parcel, b);
                    break;
                case 11:
                    z2 = C0141a.m83c(parcel, b);
                    break;
                case 12:
                    str7 = C0141a.m92l(parcel, b);
                    break;
                case 13:
                    i2 = C0141a.m86f(parcel, b);
                    break;
                case 14:
                    i3 = C0141a.m86f(parcel, b);
                    break;
                case 15:
                    i4 = C0141a.m86f(parcel, b);
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
            return new GameEntity(i, str, str2, str3, str4, str5, str6, uri, uri2, uri3, z, z2, str7, i2, i3, i4);
        }
        throw new C0140a("Overread allowed size end=" + c, parcel);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return m146z(x0);
    }

    public GameEntity[] m146z(int i) {
        return new GameEntity[i];
    }
}
