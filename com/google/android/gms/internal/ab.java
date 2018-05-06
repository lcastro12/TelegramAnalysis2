package com.google.android.gms.internal;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.internal.ae.C0161b;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public final class ab implements SafeParcelable, C0161b<String, Integer> {
    public static final ac CREATOR = new ac();
    private final int ab;
    private final HashMap<String, Integer> co;
    private final HashMap<Integer, String> cp;
    private final ArrayList<C1307a> cq;

    public static final class C1307a implements SafeParcelable {
        public static final ad CREATOR = new ad();
        final String cr;
        final int cs;
        final int versionCode;

        C1307a(int i, String str, int i2) {
            this.versionCode = i;
            this.cr = str;
            this.cs = i2;
        }

        C1307a(String str, int i) {
            this.versionCode = 1;
            this.cr = str;
            this.cs = i;
        }

        public int describeContents() {
            ad adVar = CREATOR;
            return 0;
        }

        public void writeToParcel(Parcel out, int flags) {
            ad adVar = CREATOR;
            ad.m173a(this, out, flags);
        }
    }

    public ab() {
        this.ab = 1;
        this.co = new HashMap();
        this.cp = new HashMap();
        this.cq = null;
    }

    ab(int i, ArrayList<C1307a> arrayList) {
        this.ab = i;
        this.co = new HashMap();
        this.cp = new HashMap();
        this.cq = null;
        m660a((ArrayList) arrayList);
    }

    private void m660a(ArrayList<C1307a> arrayList) {
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            C1307a c1307a = (C1307a) it.next();
            m665b(c1307a.cr, c1307a.cs);
        }
    }

    ArrayList<C1307a> m661Q() {
        ArrayList<C1307a> arrayList = new ArrayList();
        for (String str : this.co.keySet()) {
            arrayList.add(new C1307a(str, ((Integer) this.co.get(str)).intValue()));
        }
        return arrayList;
    }

    public int mo1084R() {
        return 7;
    }

    public int mo1085S() {
        return 0;
    }

    public String m664a(Integer num) {
        String str = (String) this.cp.get(num);
        return (str == null && this.co.containsKey("gms_unknown")) ? "gms_unknown" : str;
    }

    public ab m665b(String str, int i) {
        this.co.put(str, Integer.valueOf(i));
        this.cp.put(Integer.valueOf(i), str);
        return this;
    }

    public int describeContents() {
        ac acVar = CREATOR;
        return 0;
    }

    public /* synthetic */ Object mo1086e(Object obj) {
        return m664a((Integer) obj);
    }

    int m667i() {
        return this.ab;
    }

    public void writeToParcel(Parcel out, int flags) {
        ac acVar = CREATOR;
        ac.m170a(this, out, flags);
    }
}
