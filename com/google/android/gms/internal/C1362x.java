package com.google.android.gms.internal;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

public final class C1362x implements SafeParcelable {
    public static final C0196y CREATOR = new C0196y();
    private final int aJ;
    private final int ab;
    private final int ci;
    private final String cj;
    private final String ck;
    private final String cl;
    private final String cm;

    public C1362x(int i, int i2, int i3, String str, String str2, String str3, String str4) {
        this.ab = i;
        this.aJ = i2;
        this.ci = i3;
        this.cj = str;
        this.ck = str2;
        this.cl = str3;
        this.cm = str4;
    }

    public int m1025I() {
        return this.ci;
    }

    public String m1026J() {
        return this.cj;
    }

    public String m1027K() {
        return this.ck;
    }

    public String m1028L() {
        return this.cm;
    }

    public boolean m1029M() {
        return this.aJ == 1 && this.ci == -1;
    }

    public boolean m1030N() {
        return this.aJ == 2;
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof C1362x)) {
            return false;
        }
        C1362x c1362x = (C1362x) obj;
        return this.ab == c1362x.ab && this.aJ == c1362x.aJ && this.ci == c1362x.ci && C0191r.m513a(this.cj, c1362x.cj) && C0191r.m513a(this.ck, c1362x.ck);
    }

    public String getDisplayName() {
        return this.cl;
    }

    public int getType() {
        return this.aJ;
    }

    public int hashCode() {
        return C0191r.hashCode(Integer.valueOf(this.ab), Integer.valueOf(this.aJ), Integer.valueOf(this.ci), this.cj, this.ck);
    }

    public int m1031i() {
        return this.ab;
    }

    public String toString() {
        if (m1030N()) {
            return String.format("Person [%s] %s", new Object[]{m1027K(), getDisplayName()});
        } else if (m1029M()) {
            return String.format("Circle [%s] %s", new Object[]{m1026J(), getDisplayName()});
        } else {
            return String.format("Group [%s] %s", new Object[]{m1026J(), getDisplayName()});
        }
    }

    public void writeToParcel(Parcel out, int flags) {
        C0196y.m529a(this, out, flags);
    }
}
