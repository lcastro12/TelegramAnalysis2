package com.google.android.gms.internal;

import android.os.Bundle;
import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import java.util.ArrayList;

public class cq implements SafeParcelable {
    public static final cr CREATOR = new cr();
    private final int ab;
    private final ArrayList<C1362x> kA;
    private final Bundle kB;
    private final boolean kC;
    private final int ky;
    private final ArrayList<C1362x> kz;

    public cq(int i, ArrayList<C1362x> arrayList, ArrayList<C1362x> arrayList2, Bundle bundle, boolean z, int i2) {
        this.ab = i;
        this.kz = arrayList;
        this.kA = arrayList2;
        this.kB = bundle;
        this.kC = z;
        this.ky = i2;
    }

    public int cJ() {
        return this.ky;
    }

    public ArrayList<C1362x> cK() {
        return this.kz;
    }

    public ArrayList<C1362x> cL() {
        return this.kA;
    }

    public Bundle cM() {
        return this.kB;
    }

    public boolean cN() {
        return this.kC;
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof cq)) {
            return false;
        }
        cq cqVar = (cq) obj;
        return this.ab == cqVar.ab && C0191r.m513a(this.kz, cqVar.kz) && C0191r.m513a(this.kA, cqVar.kA) && C0191r.m513a(this.kB, cqVar.kB) && C0191r.m513a(Integer.valueOf(this.ky), Integer.valueOf(cqVar.ky));
    }

    public int hashCode() {
        return C0191r.hashCode(Integer.valueOf(this.ab), this.kz, this.kA, this.kB, Integer.valueOf(this.ky));
    }

    public int m954i() {
        return this.ab;
    }

    public void writeToParcel(Parcel out, int flags) {
        cr.m446a(this, out, flags);
    }
}
