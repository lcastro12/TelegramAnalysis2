package com.google.android.gms.common.data;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

public class C1285c<T extends SafeParcelable> extends DataBuffer<T> {
    private static final String[] f68X = new String[]{"data"};
    private final Creator<T> f69Y;

    public C1285c(C1287d c1287d, Creator<T> creator) {
        super(c1287d);
        this.f69Y = creator;
    }

    public T m620d(int i) {
        byte[] e = this.S.m632e("data", i, 0);
        Parcel obtain = Parcel.obtain();
        obtain.unmarshall(e, 0, e.length);
        obtain.setDataPosition(0);
        SafeParcelable safeParcelable = (SafeParcelable) this.f69Y.createFromParcel(obtain);
        obtain.recycle();
        return safeParcelable;
    }

    public /* synthetic */ Object get(int x0) {
        return m620d(x0);
    }
}
