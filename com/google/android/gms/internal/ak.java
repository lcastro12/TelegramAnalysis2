package com.google.android.gms.internal;

import android.os.Bundle;
import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.C0141a;
import com.google.android.gms.common.internal.safeparcel.C0141a.C0140a;
import com.google.android.gms.common.internal.safeparcel.C0142b;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.internal.ae.C1308a;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

public class ak extends ae implements SafeParcelable {
    public static final al CREATOR = new al();
    private final int ab;
    private final ah cB;
    private final Parcel cJ;
    private final int cK;
    private int cL;
    private int cM;
    private final String mClassName;

    ak(int i, Parcel parcel, ah ahVar) {
        this.ab = i;
        this.cJ = (Parcel) C0192s.m521d(parcel);
        this.cK = 2;
        this.cB = ahVar;
        if (this.cB == null) {
            this.mClassName = null;
        } else {
            this.mClassName = this.cB.aj();
        }
        this.cL = 2;
    }

    private ak(SafeParcelable safeParcelable, ah ahVar, String str) {
        this.ab = 1;
        this.cJ = Parcel.obtain();
        safeParcelable.writeToParcel(this.cJ, 0);
        this.cK = 1;
        this.cB = (ah) C0192s.m521d(ahVar);
        this.mClassName = (String) C0192s.m521d(str);
        this.cL = 2;
    }

    public static <T extends ae & SafeParcelable> ak m692a(T t) {
        String canonicalName = t.getClass().getCanonicalName();
        return new ak((SafeParcelable) t, m699b((ae) t), canonicalName);
    }

    public static HashMap<String, String> m693a(Bundle bundle) {
        HashMap<String, String> hashMap = new HashMap();
        for (String str : bundle.keySet()) {
            hashMap.put(str, bundle.getString(str));
        }
        return hashMap;
    }

    private static void m694a(ah ahVar, ae aeVar) {
        Class cls = aeVar.getClass();
        if (!ahVar.m689b(cls)) {
            HashMap T = aeVar.mo1087T();
            ahVar.m688a(cls, aeVar.mo1087T());
            for (String str : T.keySet()) {
                C1308a c1308a = (C1308a) T.get(str);
                Class ab = c1308a.ab();
                if (ab != null) {
                    try {
                        m694a(ahVar, (ae) ab.newInstance());
                    } catch (Throwable e) {
                        throw new IllegalStateException("Could not instantiate an object of type " + c1308a.ab().getCanonicalName(), e);
                    } catch (Throwable e2) {
                        throw new IllegalStateException("Could not access object of type " + c1308a.ab().getCanonicalName(), e2);
                    }
                }
            }
        }
    }

    private void m695a(StringBuilder stringBuilder, int i, Object obj) {
        switch (i) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                stringBuilder.append(obj);
                return;
            case 7:
                stringBuilder.append("\"").append(aq.m217r(obj.toString())).append("\"");
                return;
            case 8:
                stringBuilder.append("\"").append(an.m213a((byte[]) obj)).append("\"");
                return;
            case 9:
                stringBuilder.append("\"").append(an.m214b((byte[]) obj));
                stringBuilder.append("\"");
                return;
            case 10:
                ar.m218a(stringBuilder, (HashMap) obj);
                return;
            case 11:
                throw new IllegalArgumentException("Method does not accept concrete type.");
            default:
                throw new IllegalArgumentException("Unknown type = " + i);
        }
    }

    private void m696a(StringBuilder stringBuilder, C1308a<?, ?> c1308a, Parcel parcel, int i) {
        switch (c1308a.m678S()) {
            case 0:
                m702b(stringBuilder, (C1308a) c1308a, m184a(c1308a, Integer.valueOf(C0141a.m86f(parcel, i))));
                return;
            case 1:
                m702b(stringBuilder, (C1308a) c1308a, m184a(c1308a, C0141a.m88h(parcel, i)));
                return;
            case 2:
                m702b(stringBuilder, (C1308a) c1308a, m184a(c1308a, Long.valueOf(C0141a.m87g(parcel, i))));
                return;
            case 3:
                m702b(stringBuilder, (C1308a) c1308a, m184a(c1308a, Float.valueOf(C0141a.m89i(parcel, i))));
                return;
            case 4:
                m702b(stringBuilder, (C1308a) c1308a, m184a(c1308a, Double.valueOf(C0141a.m90j(parcel, i))));
                return;
            case 5:
                m702b(stringBuilder, (C1308a) c1308a, m184a(c1308a, C0141a.m91k(parcel, i)));
                return;
            case 6:
                m702b(stringBuilder, (C1308a) c1308a, m184a(c1308a, Boolean.valueOf(C0141a.m83c(parcel, i))));
                return;
            case 7:
                m702b(stringBuilder, (C1308a) c1308a, m184a(c1308a, C0141a.m92l(parcel, i)));
                return;
            case 8:
            case 9:
                m702b(stringBuilder, (C1308a) c1308a, m184a(c1308a, C0141a.m96o(parcel, i)));
                return;
            case 10:
                m702b(stringBuilder, (C1308a) c1308a, m184a(c1308a, m693a(C0141a.m95n(parcel, i))));
                return;
            case 11:
                throw new IllegalArgumentException("Method does not accept concrete type.");
            default:
                throw new IllegalArgumentException("Unknown field out type = " + c1308a.m678S());
        }
    }

    private void m697a(StringBuilder stringBuilder, String str, C1308a<?, ?> c1308a, Parcel parcel, int i) {
        stringBuilder.append("\"").append(str).append("\":");
        if (c1308a.ad()) {
            m696a(stringBuilder, c1308a, parcel, i);
        } else {
            m701b(stringBuilder, c1308a, parcel, i);
        }
    }

    private void m698a(StringBuilder stringBuilder, HashMap<String, C1308a<?, ?>> hashMap, Parcel parcel) {
        HashMap b = m700b((HashMap) hashMap);
        stringBuilder.append('{');
        int c = C0141a.m81c(parcel);
        Object obj = null;
        while (parcel.dataPosition() < c) {
            int b2 = C0141a.m78b(parcel);
            Entry entry = (Entry) b.get(Integer.valueOf(C0141a.m93m(b2)));
            if (entry != null) {
                if (obj != null) {
                    stringBuilder.append(",");
                }
                m697a(stringBuilder, (String) entry.getKey(), (C1308a) entry.getValue(), parcel, b2);
                obj = 1;
            }
        }
        if (parcel.dataPosition() != c) {
            throw new C0140a("Overread allowed size end=" + c, parcel);
        }
        stringBuilder.append('}');
    }

    private static ah m699b(ae aeVar) {
        ah ahVar = new ah(aeVar.getClass());
        m694a(ahVar, aeVar);
        ahVar.ah();
        ahVar.ag();
        return ahVar;
    }

    private static HashMap<Integer, Entry<String, C1308a<?, ?>>> m700b(HashMap<String, C1308a<?, ?>> hashMap) {
        HashMap<Integer, Entry<String, C1308a<?, ?>>> hashMap2 = new HashMap();
        for (Entry entry : hashMap.entrySet()) {
            hashMap2.put(Integer.valueOf(((C1308a) entry.getValue()).aa()), entry);
        }
        return hashMap2;
    }

    private void m701b(StringBuilder stringBuilder, C1308a<?, ?> c1308a, Parcel parcel, int i) {
        if (c1308a.m681Y()) {
            stringBuilder.append("[");
            switch (c1308a.m678S()) {
                case 0:
                    am.m208a(stringBuilder, C0141a.m98q(parcel, i));
                    break;
                case 1:
                    am.m210a(stringBuilder, C0141a.m100s(parcel, i));
                    break;
                case 2:
                    am.m209a(stringBuilder, C0141a.m99r(parcel, i));
                    break;
                case 3:
                    am.m207a(stringBuilder, C0141a.m101t(parcel, i));
                    break;
                case 4:
                    am.m206a(stringBuilder, C0141a.m102u(parcel, i));
                    break;
                case 5:
                    am.m210a(stringBuilder, C0141a.m103v(parcel, i));
                    break;
                case 6:
                    am.m212a(stringBuilder, C0141a.m97p(parcel, i));
                    break;
                case 7:
                    am.m211a(stringBuilder, C0141a.m104w(parcel, i));
                    break;
                case 8:
                case 9:
                case 10:
                    throw new UnsupportedOperationException("List of type BASE64, BASE64_URL_SAFE, or STRING_MAP is not supported");
                case 11:
                    Parcel[] z = C0141a.m107z(parcel, i);
                    int length = z.length;
                    for (int i2 = 0; i2 < length; i2++) {
                        if (i2 > 0) {
                            stringBuilder.append(",");
                        }
                        z[i2].setDataPosition(0);
                        m698a(stringBuilder, c1308a.af(), z[i2]);
                    }
                    break;
                default:
                    throw new IllegalStateException("Unknown field type out.");
            }
            stringBuilder.append("]");
            return;
        }
        switch (c1308a.m678S()) {
            case 0:
                stringBuilder.append(C0141a.m86f(parcel, i));
                return;
            case 1:
                stringBuilder.append(C0141a.m88h(parcel, i));
                return;
            case 2:
                stringBuilder.append(C0141a.m87g(parcel, i));
                return;
            case 3:
                stringBuilder.append(C0141a.m89i(parcel, i));
                return;
            case 4:
                stringBuilder.append(C0141a.m90j(parcel, i));
                return;
            case 5:
                stringBuilder.append(C0141a.m91k(parcel, i));
                return;
            case 6:
                stringBuilder.append(C0141a.m83c(parcel, i));
                return;
            case 7:
                stringBuilder.append("\"").append(aq.m217r(C0141a.m92l(parcel, i))).append("\"");
                return;
            case 8:
                stringBuilder.append("\"").append(an.m213a(C0141a.m96o(parcel, i))).append("\"");
                return;
            case 9:
                stringBuilder.append("\"").append(an.m214b(C0141a.m96o(parcel, i)));
                stringBuilder.append("\"");
                return;
            case 10:
                Bundle n = C0141a.m95n(parcel, i);
                Set<String> keySet = n.keySet();
                keySet.size();
                stringBuilder.append("{");
                int i3 = 1;
                for (String str : keySet) {
                    if (i3 == 0) {
                        stringBuilder.append(",");
                    }
                    stringBuilder.append("\"").append(str).append("\"");
                    stringBuilder.append(":");
                    stringBuilder.append("\"").append(aq.m217r(n.getString(str))).append("\"");
                    i3 = 0;
                }
                stringBuilder.append("}");
                return;
            case 11:
                Parcel y = C0141a.m106y(parcel, i);
                y.setDataPosition(0);
                m698a(stringBuilder, c1308a.af(), y);
                return;
            default:
                throw new IllegalStateException("Unknown field type out");
        }
    }

    private void m702b(StringBuilder stringBuilder, C1308a<?, ?> c1308a, Object obj) {
        if (c1308a.m680X()) {
            m703b(stringBuilder, (C1308a) c1308a, (ArrayList) obj);
        } else {
            m695a(stringBuilder, c1308a.m677R(), obj);
        }
    }

    private void m703b(StringBuilder stringBuilder, C1308a<?, ?> c1308a, ArrayList<?> arrayList) {
        stringBuilder.append("[");
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            if (i != 0) {
                stringBuilder.append(",");
            }
            m695a(stringBuilder, c1308a.m677R(), arrayList.get(i));
        }
        stringBuilder.append("]");
    }

    public HashMap<String, C1308a<?, ?>> mo1087T() {
        return this.cB == null ? null : this.cB.m691q(this.mClassName);
    }

    public Parcel al() {
        switch (this.cL) {
            case 0:
                this.cM = C0142b.m131d(this.cJ);
                C0142b.m110C(this.cJ, this.cM);
                this.cL = 2;
                break;
            case 1:
                C0142b.m110C(this.cJ, this.cM);
                this.cL = 2;
                break;
        }
        return this.cJ;
    }

    ah am() {
        switch (this.cK) {
            case 0:
                return null;
            case 1:
                return this.cB;
            case 2:
                return this.cB;
            default:
                throw new IllegalStateException("Invalid creation type: " + this.cK);
        }
    }

    public int describeContents() {
        al alVar = CREATOR;
        return 0;
    }

    public int m705i() {
        return this.ab;
    }

    protected Object mo1088m(String str) {
        throw new UnsupportedOperationException("Converting to JSON does not require this method.");
    }

    protected boolean mo1089n(String str) {
        throw new UnsupportedOperationException("Converting to JSON does not require this method.");
    }

    public String toString() {
        C0192s.m518b(this.cB, (Object) "Cannot convert to JSON on client side.");
        Parcel al = al();
        al.setDataPosition(0);
        StringBuilder stringBuilder = new StringBuilder(100);
        m698a(stringBuilder, this.cB.m691q(this.mClassName), al);
        return stringBuilder.toString();
    }

    public void writeToParcel(Parcel out, int flags) {
        al alVar = CREATOR;
        al.m203a(this, out, flags);
    }
}
