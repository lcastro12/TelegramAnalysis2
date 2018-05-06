package com.google.android.gms.maps;

import android.content.Context;
import android.os.RemoteException;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.internal.C0192s;
import com.google.android.gms.maps.internal.C0201c;
import com.google.android.gms.maps.internal.C0214p;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.RuntimeRemoteException;

public final class MapsInitializer {
    private MapsInitializer() {
    }

    public static void initialize(Context context) throws GooglePlayServicesNotAvailableException {
        C0192s.m521d(context);
        C0201c i = C0214p.m557i(context);
        try {
            CameraUpdateFactory.m536a(i.bk());
            BitmapDescriptorFactory.m561a(i.bl());
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }
}
