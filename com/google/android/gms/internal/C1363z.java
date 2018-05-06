package com.google.android.gms.internal;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.internal.ae.C0161b;

public class C1363z implements SafeParcelable {
    public static final aa CREATOR = new aa();
    private final int ab;
    private final ab cn;

    C1363z(int i, ab abVar) {
        this.ab = i;
        this.cn = abVar;
    }

    private C1363z(ab abVar) {
        this.ab = 1;
        this.cn = abVar;
    }

    public static C1363z m1032a(C0161b<?, ?> c0161b) {
        if (c0161b instanceof ab) {
            return new C1363z((ab) c0161b);
        }
        throw new IllegalArgumentException("Unsupported safe parcelable field converter class.");
    }

    ab m1033O() {
        return this.cn;
    }

    public C0161b<?, ?> m1034P() {
        if (this.cn != null) {
            return this.cn;
        }
        throw new IllegalStateException("There was no converter wrapped in this ConverterWrapper.");
    }

    public int describeContents() {
        aa aaVar = CREATOR;
        return 0;
    }

    int m1035i() {
        return this.ab;
    }

    public void writeToParcel(Parcel out, int flags) {
        aa aaVar = CREATOR;
        aa.m167a(this, out, flags);
    }
}
