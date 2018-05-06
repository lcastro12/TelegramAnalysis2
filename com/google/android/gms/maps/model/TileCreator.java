package com.google.android.gms.maps.model;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.C0141a;
import com.google.android.gms.common.internal.safeparcel.C0141a.C0140a;
import com.google.android.gms.common.internal.safeparcel.C0142b;

public class TileCreator implements Creator<Tile> {
    public static final int CONTENT_DESCRIPTION = 0;

    static void m571a(Tile tile, Parcel parcel, int i) {
        int d = C0142b.m131d(parcel);
        C0142b.m129c(parcel, 1, tile.m1099i());
        C0142b.m129c(parcel, 2, tile.width);
        C0142b.m129c(parcel, 3, tile.height);
        C0142b.m123a(parcel, 4, tile.data, false);
        C0142b.m110C(parcel, d);
    }

    public Tile createFromParcel(Parcel parcel) {
        int i = 0;
        int c = C0141a.m81c(parcel);
        byte[] bArr = null;
        int i2 = 0;
        int i3 = 0;
        while (parcel.dataPosition() < c) {
            int b = C0141a.m78b(parcel);
            switch (C0141a.m93m(b)) {
                case 1:
                    i3 = C0141a.m86f(parcel, b);
                    break;
                case 2:
                    i2 = C0141a.m86f(parcel, b);
                    break;
                case 3:
                    i = C0141a.m86f(parcel, b);
                    break;
                case 4:
                    bArr = C0141a.m96o(parcel, b);
                    break;
                default:
                    C0141a.m79b(parcel, b);
                    break;
            }
        }
        if (parcel.dataPosition() == c) {
            return new Tile(i3, i2, i, bArr);
        }
        throw new C0140a("Overread allowed size end=" + c, parcel);
    }

    public Tile[] newArray(int size) {
        return new Tile[size];
    }
}
