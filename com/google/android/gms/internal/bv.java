package com.google.android.gms.internal;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import java.util.ArrayList;

public class bv implements SafeParcelable {
    public static final bw CREATOR = new bw();
    private final int ab;
    private final String di;
    private final ArrayList<C1362x> iA;
    private final boolean iB;
    private final ArrayList<C1362x> iz;

    public bv(int i, String str, ArrayList<C1362x> arrayList, ArrayList<C1362x> arrayList2, boolean z) {
        this.ab = i;
        this.di = str;
        this.iz = arrayList;
        this.iA = arrayList2;
        this.iB = z;
    }

    public ArrayList<C1362x> bE() {
        return this.iz;
    }

    public ArrayList<C1362x> bF() {
        return this.iA;
    }

    public boolean bG() {
        return this.iB;
    }

    public int describeContents() {
        return 0;
    }

    public String getDescription() {
        return this.di;
    }

    public int m946i() {
        return this.ab;
    }

    public void writeToParcel(Parcel out, int flags) {
        bw.m412a(this, out, flags);
    }
}
