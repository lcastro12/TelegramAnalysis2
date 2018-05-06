package com.google.android.gms.internal;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.internal.ae.C1308a;
import com.google.android.gms.plus.model.moments.ItemScope;
import com.google.android.gms.plus.model.moments.Moment;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public final class bz extends ae implements SafeParcelable, Moment {
    public static final ca CREATOR = new ca();
    private static final HashMap<String, C1308a<?, ?>> iC = new HashMap();
    private final int ab;
    private final Set<Integer> iD;
    private bx jB;
    private bx jC;
    private String jh;
    private String js;
    private String jy;

    static {
        iC.put("id", C1308a.m675f("id", 2));
        iC.put("result", C1308a.m669a("result", 4, bx.class));
        iC.put("startDate", C1308a.m675f("startDate", 5));
        iC.put("target", C1308a.m669a("target", 6, bx.class));
        iC.put("type", C1308a.m675f("type", 7));
    }

    public bz() {
        this.ab = 1;
        this.iD = new HashSet();
    }

    bz(Set<Integer> set, int i, String str, bx bxVar, String str2, bx bxVar2, String str3) {
        this.iD = set;
        this.ab = i;
        this.jh = str;
        this.jB = bxVar;
        this.js = str2;
        this.jC = bxVar2;
        this.jy = str3;
    }

    public bz(Set<Integer> set, String str, bx bxVar, String str2, bx bxVar2, String str3) {
        this.iD = set;
        this.ab = 1;
        this.jh = str;
        this.jB = bxVar;
        this.js = str2;
        this.jC = bxVar2;
        this.jy = str3;
    }

    public HashMap<String, C1308a<?, ?>> mo1087T() {
        return iC;
    }

    protected boolean mo2354a(C1308a c1308a) {
        return this.iD.contains(Integer.valueOf(c1308a.aa()));
    }

    protected Object mo2355b(C1308a c1308a) {
        switch (c1308a.aa()) {
            case 2:
                return this.jh;
            case 4:
                return this.jB;
            case 5:
                return this.js;
            case 6:
                return this.jC;
            case 7:
                return this.jy;
            default:
                throw new IllegalStateException("Unknown safe parcelable id=" + c1308a.aa());
        }
    }

    Set<Integer> bH() {
        return this.iD;
    }

    bx bY() {
        return this.jB;
    }

    bx bZ() {
        return this.jC;
    }

    public bz ca() {
        return this;
    }

    public int describeContents() {
        ca caVar = CREATOR;
        return 0;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof bz)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        bz bzVar = (bz) obj;
        for (C1308a c1308a : iC.values()) {
            if (mo2354a(c1308a)) {
                if (!bzVar.mo2354a(c1308a)) {
                    return false;
                }
                if (!mo2355b(c1308a).equals(bzVar.mo2355b(c1308a))) {
                    return false;
                }
            } else if (bzVar.mo2354a(c1308a)) {
                return false;
            }
        }
        return true;
    }

    public /* synthetic */ Object freeze() {
        return ca();
    }

    public String getId() {
        return this.jh;
    }

    public ItemScope getResult() {
        return this.jB;
    }

    public String getStartDate() {
        return this.js;
    }

    public ItemScope getTarget() {
        return this.jC;
    }

    public String getType() {
        return this.jy;
    }

    public boolean hasId() {
        return this.iD.contains(Integer.valueOf(2));
    }

    public boolean hasResult() {
        return this.iD.contains(Integer.valueOf(4));
    }

    public boolean hasStartDate() {
        return this.iD.contains(Integer.valueOf(5));
    }

    public boolean hasTarget() {
        return this.iD.contains(Integer.valueOf(6));
    }

    public boolean hasType() {
        return this.iD.contains(Integer.valueOf(7));
    }

    public int hashCode() {
        int i = 0;
        for (C1308a c1308a : iC.values()) {
            int hashCode;
            if (mo2354a(c1308a)) {
                hashCode = mo2355b(c1308a).hashCode() + (i + c1308a.aa());
            } else {
                hashCode = i;
            }
            i = hashCode;
        }
        return i;
    }

    int m1274i() {
        return this.ab;
    }

    public boolean isDataValid() {
        return true;
    }

    protected Object mo1088m(String str) {
        return null;
    }

    protected boolean mo1089n(String str) {
        return false;
    }

    public void writeToParcel(Parcel out, int flags) {
        ca caVar = CREATOR;
        ca.m418a(this, out, flags);
    }
}
