package com.google.android.gms.internal;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class ae {

    public interface C0161b<I, O> {
        int mo1084R();

        int mo1085S();

        I mo1086e(O o);
    }

    public static class C1308a<I, O> implements SafeParcelable {
        public static final af CREATOR = new af();
        private final int ab;
        protected final String cA;
        private ah cB;
        private C0161b<I, O> cC;
        protected final int ct;
        protected final boolean cu;
        protected final int cv;
        protected final boolean cw;
        protected final String cx;
        protected final int cy;
        protected final Class<? extends ae> cz;

        C1308a(int i, int i2, boolean z, int i3, boolean z2, String str, int i4, String str2, C1363z c1363z) {
            this.ab = i;
            this.ct = i2;
            this.cu = z;
            this.cv = i3;
            this.cw = z2;
            this.cx = str;
            this.cy = i4;
            if (str2 == null) {
                this.cz = null;
                this.cA = null;
            } else {
                this.cz = ak.class;
                this.cA = str2;
            }
            if (c1363z == null) {
                this.cC = null;
            } else {
                this.cC = c1363z.m1034P();
            }
        }

        protected C1308a(int i, boolean z, int i2, boolean z2, String str, int i3, Class<? extends ae> cls, C0161b<I, O> c0161b) {
            this.ab = 1;
            this.ct = i;
            this.cu = z;
            this.cv = i2;
            this.cw = z2;
            this.cx = str;
            this.cy = i3;
            this.cz = cls;
            if (cls == null) {
                this.cA = null;
            } else {
                this.cA = cls.getCanonicalName();
            }
            this.cC = c0161b;
        }

        public static C1308a m668a(String str, int i, C0161b<?, ?> c0161b, boolean z) {
            return new C1308a(c0161b.mo1084R(), z, c0161b.mo1085S(), false, str, i, null, c0161b);
        }

        public static <T extends ae> C1308a<T, T> m669a(String str, int i, Class<T> cls) {
            return new C1308a(11, false, 11, false, str, i, cls, null);
        }

        public static <T extends ae> C1308a<ArrayList<T>, ArrayList<T>> m670b(String str, int i, Class<T> cls) {
            return new C1308a(11, true, 11, true, str, i, cls, null);
        }

        public static C1308a<Integer, Integer> m671c(String str, int i) {
            return new C1308a(0, false, 0, false, str, i, null, null);
        }

        public static C1308a<Double, Double> m673d(String str, int i) {
            return new C1308a(4, false, 4, false, str, i, null, null);
        }

        public static C1308a<Boolean, Boolean> m674e(String str, int i) {
            return new C1308a(6, false, 6, false, str, i, null, null);
        }

        public static C1308a<String, String> m675f(String str, int i) {
            return new C1308a(7, false, 7, false, str, i, null, null);
        }

        public static C1308a<ArrayList<String>, ArrayList<String>> m676g(String str, int i) {
            return new C1308a(7, true, 7, true, str, i, null, null);
        }

        public int m677R() {
            return this.ct;
        }

        public int m678S() {
            return this.cv;
        }

        public C1308a<I, O> m679W() {
            return new C1308a(this.ab, this.ct, this.cu, this.cv, this.cw, this.cx, this.cy, this.cA, ae());
        }

        public boolean m680X() {
            return this.cu;
        }

        public boolean m681Y() {
            return this.cw;
        }

        public String m682Z() {
            return this.cx;
        }

        public void m683a(ah ahVar) {
            this.cB = ahVar;
        }

        public int aa() {
            return this.cy;
        }

        public Class<? extends ae> ab() {
            return this.cz;
        }

        String ac() {
            return this.cA == null ? null : this.cA;
        }

        public boolean ad() {
            return this.cC != null;
        }

        C1363z ae() {
            return this.cC == null ? null : C1363z.m1032a(this.cC);
        }

        public HashMap<String, C1308a<?, ?>> af() {
            C0192s.m521d(this.cA);
            C0192s.m521d(this.cB);
            return this.cB.m691q(this.cA);
        }

        public int describeContents() {
            af afVar = CREATOR;
            return 0;
        }

        public I m684e(O o) {
            return this.cC.mo1086e(o);
        }

        public int m685i() {
            return this.ab;
        }

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Field\n");
            stringBuilder.append("            versionCode=").append(this.ab).append('\n');
            stringBuilder.append("                 typeIn=").append(this.ct).append('\n');
            stringBuilder.append("            typeInArray=").append(this.cu).append('\n');
            stringBuilder.append("                typeOut=").append(this.cv).append('\n');
            stringBuilder.append("           typeOutArray=").append(this.cw).append('\n');
            stringBuilder.append("        outputFieldName=").append(this.cx).append('\n');
            stringBuilder.append("      safeParcelFieldId=").append(this.cy).append('\n');
            stringBuilder.append("       concreteTypeName=").append(ac()).append('\n');
            if (ab() != null) {
                stringBuilder.append("     concreteType.class=").append(ab().getCanonicalName()).append('\n');
            }
            stringBuilder.append("          converterName=").append(this.cC == null ? "null" : this.cC.getClass().getCanonicalName()).append('\n');
            return stringBuilder.toString();
        }

        public void writeToParcel(Parcel out, int flags) {
            af afVar = CREATOR;
            af.m191a(this, out, flags);
        }
    }

    private void m179a(StringBuilder stringBuilder, C1308a c1308a, Object obj) {
        if (c1308a.m677R() == 11) {
            stringBuilder.append(((ae) c1308a.ab().cast(obj)).toString());
        } else if (c1308a.m677R() == 7) {
            stringBuilder.append("\"");
            stringBuilder.append(aq.m217r((String) obj));
            stringBuilder.append("\"");
        } else {
            stringBuilder.append(obj);
        }
    }

    private void m180a(StringBuilder stringBuilder, C1308a c1308a, ArrayList<Object> arrayList) {
        stringBuilder.append("[");
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                stringBuilder.append(",");
            }
            Object obj = arrayList.get(i);
            if (obj != null) {
                m179a(stringBuilder, c1308a, obj);
            }
        }
        stringBuilder.append("]");
    }

    public abstract HashMap<String, C1308a<?, ?>> mo1087T();

    public HashMap<String, Object> m182U() {
        return null;
    }

    public HashMap<String, Object> m183V() {
        return null;
    }

    protected <O, I> I m184a(C1308a<I, O> c1308a, Object obj) {
        return c1308a.cC != null ? c1308a.m684e(obj) : obj;
    }

    protected boolean mo2354a(C1308a c1308a) {
        return c1308a.m678S() == 11 ? c1308a.m681Y() ? m190p(c1308a.m682Z()) : m189o(c1308a.m682Z()) : mo1089n(c1308a.m682Z());
    }

    protected Object mo2355b(C1308a c1308a) {
        boolean z = true;
        String Z = c1308a.m682Z();
        if (c1308a.ab() == null) {
            return mo1088m(c1308a.m682Z());
        }
        if (mo1088m(c1308a.m682Z()) != null) {
            z = false;
        }
        C0192s.m516a(z, "Concrete field shouldn't be value object: " + c1308a.m682Z());
        Map V = c1308a.m681Y() ? m183V() : m182U();
        if (V != null) {
            return V.get(Z);
        }
        try {
            return getClass().getMethod("get" + Character.toUpperCase(Z.charAt(0)) + Z.substring(1), new Class[0]).invoke(this, new Object[0]);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract Object mo1088m(String str);

    protected abstract boolean mo1089n(String str);

    protected boolean m189o(String str) {
        throw new UnsupportedOperationException("Concrete types not supported");
    }

    protected boolean m190p(String str) {
        throw new UnsupportedOperationException("Concrete type arrays not supported");
    }

    public String toString() {
        HashMap T = mo1087T();
        StringBuilder stringBuilder = new StringBuilder(100);
        for (String str : T.keySet()) {
            C1308a c1308a = (C1308a) T.get(str);
            if (mo2354a(c1308a)) {
                Object a = m184a(c1308a, mo2355b(c1308a));
                if (stringBuilder.length() == 0) {
                    stringBuilder.append("{");
                } else {
                    stringBuilder.append(",");
                }
                stringBuilder.append("\"").append(str).append("\":");
                if (a != null) {
                    switch (c1308a.m678S()) {
                        case 8:
                            stringBuilder.append("\"").append(an.m213a((byte[]) a)).append("\"");
                            break;
                        case 9:
                            stringBuilder.append("\"").append(an.m214b((byte[]) a)).append("\"");
                            break;
                        case 10:
                            ar.m218a(stringBuilder, (HashMap) a);
                            break;
                        default:
                            if (!c1308a.m680X()) {
                                m179a(stringBuilder, c1308a, a);
                                break;
                            }
                            m180a(stringBuilder, c1308a, (ArrayList) a);
                            break;
                    }
                }
                stringBuilder.append("null");
            }
        }
        if (stringBuilder.length() > 0) {
            stringBuilder.append("}");
        } else {
            stringBuilder.append("{}");
        }
        return stringBuilder.toString();
    }
}
