package com.google.android.gms.plus;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.internal.C0191r;

public class C1433a implements SafeParcelable {
    public static final C0237b CREATOR = new C0237b();
    private final int ab;
    private final String f130g;
    private final String[] hY;
    private final String hZ;
    private final String ia;
    private final String ib;
    private final String[] ik;
    private final String[] il;

    public C1433a(int i, String str, String[] strArr, String[] strArr2, String[] strArr3, String str2, String str3, String str4) {
        this.ab = i;
        this.f130g = str;
        this.ik = strArr;
        this.il = strArr2;
        this.hY = strArr3;
        this.hZ = str2;
        this.ia = str3;
        this.ib = str4;
    }

    public C1433a(String str, String[] strArr, String[] strArr2, String[] strArr3, String str2, String str3, String str4) {
        this.ab = 1;
        this.f130g = str;
        this.ik = strArr;
        this.il = strArr2;
        this.hY = strArr3;
        this.hZ = str2;
        this.ia = str3;
        this.ib = str4;
    }

    public String[] bA() {
        return this.hY;
    }

    public String bB() {
        return this.hZ;
    }

    public String bC() {
        return this.ia;
    }

    public String bD() {
        return this.ib;
    }

    public String[] by() {
        return this.ik;
    }

    public String[] bz() {
        return this.il;
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof C1433a)) {
            return false;
        }
        C1433a c1433a = (C1433a) obj;
        return this.ab == c1433a.ab && C0191r.m513a(this.f130g, c1433a.f130g) && C0191r.m513a(this.ik, c1433a.ik) && C0191r.m513a(this.il, c1433a.il) && C0191r.m513a(this.hY, c1433a.hY) && C0191r.m513a(this.hZ, c1433a.hZ) && C0191r.m513a(this.ia, c1433a.ia) && C0191r.m513a(this.ib, c1433a.ib);
    }

    public String getAccountName() {
        return this.f130g;
    }

    public int hashCode() {
        return C0191r.hashCode(Integer.valueOf(this.ab), this.f130g, this.ik, this.il, this.hY, this.hZ, this.ia, this.ib);
    }

    public int m1126i() {
        return this.ab;
    }

    public String toString() {
        return C0191r.m514c(this).m512a("versionCode", Integer.valueOf(this.ab)).m512a("accountName", this.f130g).m512a("requestedScopes", this.ik).m512a("visibleActivities", this.il).m512a("requiredFeatures", this.hY).m512a("packageNameForAuth", this.hZ).m512a("callingPackageName", this.ia).m512a("applicationName", this.ib).toString();
    }

    public void writeToParcel(Parcel out, int flags) {
        C0237b.m605a(this, out, flags);
    }
}
