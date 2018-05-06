package com.google.android.gms.internal;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.internal.ae.C1308a;
import java.util.ArrayList;
import java.util.HashMap;

public class ah implements SafeParcelable {
    public static final ai CREATOR = new ai();
    private final int ab;
    private final HashMap<String, HashMap<String, C1308a<?, ?>>> cD;
    private final ArrayList<C1309a> cE;
    private final String cF;

    public static class C1309a implements SafeParcelable {
        public static final aj CREATOR = new aj();
        final ArrayList<C1310b> cG;
        final String className;
        final int versionCode;

        C1309a(int i, String str, ArrayList<C1310b> arrayList) {
            this.versionCode = i;
            this.className = str;
            this.cG = arrayList;
        }

        C1309a(String str, HashMap<String, C1308a<?, ?>> hashMap) {
            this.versionCode = 1;
            this.className = str;
            this.cG = C1309a.m686a(hashMap);
        }

        private static ArrayList<C1310b> m686a(HashMap<String, C1308a<?, ?>> hashMap) {
            if (hashMap == null) {
                return null;
            }
            ArrayList<C1310b> arrayList = new ArrayList();
            for (String str : hashMap.keySet()) {
                arrayList.add(new C1310b(str, (C1308a) hashMap.get(str)));
            }
            return arrayList;
        }

        HashMap<String, C1308a<?, ?>> ak() {
            HashMap<String, C1308a<?, ?>> hashMap = new HashMap();
            int size = this.cG.size();
            for (int i = 0; i < size; i++) {
                C1310b c1310b = (C1310b) this.cG.get(i);
                hashMap.put(c1310b.cH, c1310b.cI);
            }
            return hashMap;
        }

        public int describeContents() {
            aj ajVar = CREATOR;
            return 0;
        }

        public void writeToParcel(Parcel out, int flags) {
            aj ajVar = CREATOR;
            aj.m200a(this, out, flags);
        }
    }

    public static class C1310b implements SafeParcelable {
        public static final ag CREATOR = new ag();
        final String cH;
        final C1308a<?, ?> cI;
        final int versionCode;

        C1310b(int i, String str, C1308a<?, ?> c1308a) {
            this.versionCode = i;
            this.cH = str;
            this.cI = c1308a;
        }

        C1310b(String str, C1308a<?, ?> c1308a) {
            this.versionCode = 1;
            this.cH = str;
            this.cI = c1308a;
        }

        public int describeContents() {
            ag agVar = CREATOR;
            return 0;
        }

        public void writeToParcel(Parcel out, int flags) {
            ag agVar = CREATOR;
            ag.m194a(this, out, flags);
        }
    }

    ah(int i, ArrayList<C1309a> arrayList, String str) {
        this.ab = i;
        this.cE = null;
        this.cD = m687b((ArrayList) arrayList);
        this.cF = (String) C0192s.m521d(str);
        ag();
    }

    public ah(Class<? extends ae> cls) {
        this.ab = 1;
        this.cE = null;
        this.cD = new HashMap();
        this.cF = cls.getCanonicalName();
    }

    private static HashMap<String, HashMap<String, C1308a<?, ?>>> m687b(ArrayList<C1309a> arrayList) {
        HashMap<String, HashMap<String, C1308a<?, ?>>> hashMap = new HashMap();
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            C1309a c1309a = (C1309a) arrayList.get(i);
            hashMap.put(c1309a.className, c1309a.ak());
        }
        return hashMap;
    }

    public void m688a(Class<? extends ae> cls, HashMap<String, C1308a<?, ?>> hashMap) {
        this.cD.put(cls.getCanonicalName(), hashMap);
    }

    public void ag() {
        for (String str : this.cD.keySet()) {
            HashMap hashMap = (HashMap) this.cD.get(str);
            for (String str2 : hashMap.keySet()) {
                ((C1308a) hashMap.get(str2)).m683a(this);
            }
        }
    }

    public void ah() {
        for (String str : this.cD.keySet()) {
            HashMap hashMap = (HashMap) this.cD.get(str);
            HashMap hashMap2 = new HashMap();
            for (String str2 : hashMap.keySet()) {
                hashMap2.put(str2, ((C1308a) hashMap.get(str2)).m679W());
            }
            this.cD.put(str, hashMap2);
        }
    }

    ArrayList<C1309a> ai() {
        ArrayList<C1309a> arrayList = new ArrayList();
        for (String str : this.cD.keySet()) {
            arrayList.add(new C1309a(str, (HashMap) this.cD.get(str)));
        }
        return arrayList;
    }

    public String aj() {
        return this.cF;
    }

    public boolean m689b(Class<? extends ae> cls) {
        return this.cD.containsKey(cls.getCanonicalName());
    }

    public int describeContents() {
        ai aiVar = CREATOR;
        return 0;
    }

    int m690i() {
        return this.ab;
    }

    public HashMap<String, C1308a<?, ?>> m691q(String str) {
        return (HashMap) this.cD.get(str);
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String str : this.cD.keySet()) {
            stringBuilder.append(str).append(":\n");
            HashMap hashMap = (HashMap) this.cD.get(str);
            for (String str2 : hashMap.keySet()) {
                stringBuilder.append("  ").append(str2).append(": ");
                stringBuilder.append(hashMap.get(str2));
            }
        }
        return stringBuilder.toString();
    }

    public void writeToParcel(Parcel out, int flags) {
        ai aiVar = CREATOR;
        ai.m197a(this, out, flags);
    }
}
