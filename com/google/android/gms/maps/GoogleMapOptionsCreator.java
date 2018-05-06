package com.google.android.gms.maps;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.C0141a;
import com.google.android.gms.common.internal.safeparcel.C0141a.C0140a;
import com.google.android.gms.common.internal.safeparcel.C0142b;
import com.google.android.gms.maps.model.CameraPosition;

public class GoogleMapOptionsCreator implements Creator<GoogleMapOptions> {
    public static final int CONTENT_DESCRIPTION = 0;

    static void m537a(GoogleMapOptions googleMapOptions, Parcel parcel, int i) {
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

    public GoogleMapOptions createFromParcel(Parcel parcel) {
        byte b = (byte) 0;
        int c = C0141a.m81c(parcel);
        CameraPosition cameraPosition = null;
        byte b2 = (byte) 0;
        byte b3 = (byte) 0;
        byte b4 = (byte) 0;
        byte b5 = (byte) 0;
        byte b6 = (byte) 0;
        int i = 0;
        byte b7 = (byte) 0;
        byte b8 = (byte) 0;
        int i2 = 0;
        while (parcel.dataPosition() < c) {
            int b9 = C0141a.m78b(parcel);
            switch (C0141a.m93m(b9)) {
                case 1:
                    i2 = C0141a.m86f(parcel, b9);
                    break;
                case 2:
                    b8 = C0141a.m84d(parcel, b9);
                    break;
                case 3:
                    b7 = C0141a.m84d(parcel, b9);
                    break;
                case 4:
                    i = C0141a.m86f(parcel, b9);
                    break;
                case 5:
                    cameraPosition = (CameraPosition) C0141a.m75a(parcel, b9, CameraPosition.CREATOR);
                    break;
                case 6:
                    b6 = C0141a.m84d(parcel, b9);
                    break;
                case 7:
                    b5 = C0141a.m84d(parcel, b9);
                    break;
                case 8:
                    b4 = C0141a.m84d(parcel, b9);
                    break;
                case 9:
                    b3 = C0141a.m84d(parcel, b9);
                    break;
                case 10:
                    b2 = C0141a.m84d(parcel, b9);
                    break;
                case 11:
                    b = C0141a.m84d(parcel, b9);
                    break;
                default:
                    C0141a.m79b(parcel, b9);
                    break;
            }
        }
        if (parcel.dataPosition() == c) {
            return new GoogleMapOptions(i2, b8, b7, i, cameraPosition, b6, b5, b4, b3, b2, b);
        }
        throw new C0140a("Overread allowed size end=" + c, parcel);
    }

    public GoogleMapOptions[] newArray(int size) {
        return new GoogleMapOptions[size];
    }
}
