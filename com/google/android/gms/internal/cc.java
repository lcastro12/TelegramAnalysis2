package com.google.android.gms.internal;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.internal.ae.C1308a;
import com.google.android.gms.plus.PlusShare;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.Person.AgeRange;
import com.google.android.gms.plus.model.people.Person.Cover;
import com.google.android.gms.plus.model.people.Person.Cover.CoverInfo;
import com.google.android.gms.plus.model.people.Person.Cover.CoverPhoto;
import com.google.android.gms.plus.model.people.Person.Emails;
import com.google.android.gms.plus.model.people.Person.Image;
import com.google.android.gms.plus.model.people.Person.Name;
import com.google.android.gms.plus.model.people.Person.Organizations;
import com.google.android.gms.plus.model.people.Person.PlacesLived;
import com.google.android.gms.plus.model.people.Person.Urls;
import com.googlecode.mp4parser.authoring.tracks.h265.NalUnitTypes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class cc extends ae implements SafeParcelable, Person {
    public static final cd CREATOR = new cd();
    private static final HashMap<String, C1308a<?, ?>> iC = new HashMap();
    private final int ab;
    private String cl;
    private final Set<Integer> iD;
    private String ie;
    private String jE;
    private C1713a jF;
    private String jG;
    private String jH;
    private int jI;
    private C1716b jJ;
    private String jK;
    private int jL;
    private C1717c jM;
    private boolean jN;
    private String jO;
    private C1718d jP;
    private String jQ;
    private int jR;
    private List<C1719f> jS;
    private List<C1720g> jT;
    private int jU;
    private int jV;
    private String jW;
    private List<C1721h> jX;
    private boolean jY;
    private String jh;

    public static class C0167e {
        public static int m421G(String str) {
            if (str.equals("person")) {
                return 0;
            }
            if (str.equals("page")) {
                return 1;
            }
            throw new IllegalArgumentException("Unknown objectType string: " + str);
        }
    }

    public static final class C1713a extends ae implements SafeParcelable, AgeRange {
        public static final ce CREATOR = new ce();
        private static final HashMap<String, C1308a<?, ?>> iC = new HashMap();
        private final int ab;
        private final Set<Integer> iD;
        private int jZ;
        private int ka;

        static {
            iC.put("max", C1308a.m671c("max", 2));
            iC.put("min", C1308a.m671c("min", 3));
        }

        public C1713a() {
            this.ab = 1;
            this.iD = new HashSet();
        }

        C1713a(Set<Integer> set, int i, int i2, int i3) {
            this.iD = set;
            this.ab = i;
            this.jZ = i2;
            this.ka = i3;
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
                    return Integer.valueOf(this.jZ);
                case 3:
                    return Integer.valueOf(this.ka);
                default:
                    throw new IllegalStateException("Unknown safe parcelable id=" + c1308a.aa());
            }
        }

        Set<Integer> bH() {
            return this.iD;
        }

        public C1713a ck() {
            return this;
        }

        public int describeContents() {
            ce ceVar = CREATOR;
            return 0;
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof C1713a)) {
                return false;
            }
            if (this == obj) {
                return true;
            }
            C1713a c1713a = (C1713a) obj;
            for (C1308a c1308a : iC.values()) {
                if (mo2354a(c1308a)) {
                    if (!c1713a.mo2354a(c1308a)) {
                        return false;
                    }
                    if (!mo2355b(c1308a).equals(c1713a.mo2355b(c1308a))) {
                        return false;
                    }
                } else if (c1713a.mo2354a(c1308a)) {
                    return false;
                }
            }
            return true;
        }

        public /* synthetic */ Object freeze() {
            return ck();
        }

        public int getMax() {
            return this.jZ;
        }

        public int getMin() {
            return this.ka;
        }

        public boolean hasMax() {
            return this.iD.contains(Integer.valueOf(2));
        }

        public boolean hasMin() {
            return this.iD.contains(Integer.valueOf(3));
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

        int m1291i() {
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
            ce ceVar = CREATOR;
            ce.m425a(this, out, flags);
        }
    }

    public static final class C1716b extends ae implements SafeParcelable, Cover {
        public static final cf CREATOR = new cf();
        private static final HashMap<String, C1308a<?, ?>> iC = new HashMap();
        private final int ab;
        private final Set<Integer> iD;
        private C1714a kb;
        private C1715b kc;
        private int kd;

        public static final class C1714a extends ae implements SafeParcelable, CoverInfo {
            public static final cg CREATOR = new cg();
            private static final HashMap<String, C1308a<?, ?>> iC = new HashMap();
            private final int ab;
            private final Set<Integer> iD;
            private int ke;
            private int kf;

            static {
                iC.put("leftImageOffset", C1308a.m671c("leftImageOffset", 2));
                iC.put("topImageOffset", C1308a.m671c("topImageOffset", 3));
            }

            public C1714a() {
                this.ab = 1;
                this.iD = new HashSet();
            }

            C1714a(Set<Integer> set, int i, int i2, int i3) {
                this.iD = set;
                this.ab = i;
                this.ke = i2;
                this.kf = i3;
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
                        return Integer.valueOf(this.ke);
                    case 3:
                        return Integer.valueOf(this.kf);
                    default:
                        throw new IllegalStateException("Unknown safe parcelable id=" + c1308a.aa());
                }
            }

            Set<Integer> bH() {
                return this.iD;
            }

            public C1714a co() {
                return this;
            }

            public int describeContents() {
                cg cgVar = CREATOR;
                return 0;
            }

            public boolean equals(Object obj) {
                if (!(obj instanceof C1714a)) {
                    return false;
                }
                if (this == obj) {
                    return true;
                }
                C1714a c1714a = (C1714a) obj;
                for (C1308a c1308a : iC.values()) {
                    if (mo2354a(c1308a)) {
                        if (!c1714a.mo2354a(c1308a)) {
                            return false;
                        }
                        if (!mo2355b(c1308a).equals(c1714a.mo2355b(c1308a))) {
                            return false;
                        }
                    } else if (c1714a.mo2354a(c1308a)) {
                        return false;
                    }
                }
                return true;
            }

            public /* synthetic */ Object freeze() {
                return co();
            }

            public int getLeftImageOffset() {
                return this.ke;
            }

            public int getTopImageOffset() {
                return this.kf;
            }

            public boolean hasLeftImageOffset() {
                return this.iD.contains(Integer.valueOf(2));
            }

            public boolean hasTopImageOffset() {
                return this.iD.contains(Integer.valueOf(3));
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

            int m1297i() {
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
                cg cgVar = CREATOR;
                cg.m430a(this, out, flags);
            }
        }

        public static final class C1715b extends ae implements SafeParcelable, CoverPhoto {
            public static final ch CREATOR = new ch();
            private static final HashMap<String, C1308a<?, ?>> iC = new HashMap();
            private final int ab;
            private int hL;
            private int hM;
            private final Set<Integer> iD;
            private String ie;

            static {
                iC.put("height", C1308a.m671c("height", 2));
                iC.put(PlusShare.KEY_CALL_TO_ACTION_URL, C1308a.m675f(PlusShare.KEY_CALL_TO_ACTION_URL, 3));
                iC.put("width", C1308a.m671c("width", 4));
            }

            public C1715b() {
                this.ab = 1;
                this.iD = new HashSet();
            }

            C1715b(Set<Integer> set, int i, int i2, String str, int i3) {
                this.iD = set;
                this.ab = i;
                this.hM = i2;
                this.ie = str;
                this.hL = i3;
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
                        return Integer.valueOf(this.hM);
                    case 3:
                        return this.ie;
                    case 4:
                        return Integer.valueOf(this.hL);
                    default:
                        throw new IllegalStateException("Unknown safe parcelable id=" + c1308a.aa());
                }
            }

            Set<Integer> bH() {
                return this.iD;
            }

            public C1715b cp() {
                return this;
            }

            public int describeContents() {
                ch chVar = CREATOR;
                return 0;
            }

            public boolean equals(Object obj) {
                if (!(obj instanceof C1715b)) {
                    return false;
                }
                if (this == obj) {
                    return true;
                }
                C1715b c1715b = (C1715b) obj;
                for (C1308a c1308a : iC.values()) {
                    if (mo2354a(c1308a)) {
                        if (!c1715b.mo2354a(c1308a)) {
                            return false;
                        }
                        if (!mo2355b(c1308a).equals(c1715b.mo2355b(c1308a))) {
                            return false;
                        }
                    } else if (c1715b.mo2354a(c1308a)) {
                        return false;
                    }
                }
                return true;
            }

            public /* synthetic */ Object freeze() {
                return cp();
            }

            public int getHeight() {
                return this.hM;
            }

            public String getUrl() {
                return this.ie;
            }

            public int getWidth() {
                return this.hL;
            }

            public boolean hasHeight() {
                return this.iD.contains(Integer.valueOf(2));
            }

            public boolean hasUrl() {
                return this.iD.contains(Integer.valueOf(3));
            }

            public boolean hasWidth() {
                return this.iD.contains(Integer.valueOf(4));
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

            int m1303i() {
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
                ch chVar = CREATOR;
                ch.m432a(this, out, flags);
            }
        }

        static {
            iC.put("coverInfo", C1308a.m669a("coverInfo", 2, C1714a.class));
            iC.put("coverPhoto", C1308a.m669a("coverPhoto", 3, C1715b.class));
            iC.put("layout", C1308a.m668a("layout", 4, new ab().m665b("banner", 0), false));
        }

        public C1716b() {
            this.ab = 1;
            this.iD = new HashSet();
        }

        C1716b(Set<Integer> set, int i, C1714a c1714a, C1715b c1715b, int i2) {
            this.iD = set;
            this.ab = i;
            this.kb = c1714a;
            this.kc = c1715b;
            this.kd = i2;
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
                    return this.kb;
                case 3:
                    return this.kc;
                case 4:
                    return Integer.valueOf(this.kd);
                default:
                    throw new IllegalStateException("Unknown safe parcelable id=" + c1308a.aa());
            }
        }

        Set<Integer> bH() {
            return this.iD;
        }

        C1714a cl() {
            return this.kb;
        }

        C1715b cm() {
            return this.kc;
        }

        public C1716b cn() {
            return this;
        }

        public int describeContents() {
            cf cfVar = CREATOR;
            return 0;
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof C1716b)) {
                return false;
            }
            if (this == obj) {
                return true;
            }
            C1716b c1716b = (C1716b) obj;
            for (C1308a c1308a : iC.values()) {
                if (mo2354a(c1308a)) {
                    if (!c1716b.mo2354a(c1308a)) {
                        return false;
                    }
                    if (!mo2355b(c1308a).equals(c1716b.mo2355b(c1308a))) {
                        return false;
                    }
                } else if (c1716b.mo2354a(c1308a)) {
                    return false;
                }
            }
            return true;
        }

        public /* synthetic */ Object freeze() {
            return cn();
        }

        public CoverInfo getCoverInfo() {
            return this.kb;
        }

        public CoverPhoto getCoverPhoto() {
            return this.kc;
        }

        public int getLayout() {
            return this.kd;
        }

        public boolean hasCoverInfo() {
            return this.iD.contains(Integer.valueOf(2));
        }

        public boolean hasCoverPhoto() {
            return this.iD.contains(Integer.valueOf(3));
        }

        public boolean hasLayout() {
            return this.iD.contains(Integer.valueOf(4));
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

        int m1309i() {
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
            cf cfVar = CREATOR;
            cf.m428a(this, out, flags);
        }
    }

    public static final class C1717c extends ae implements SafeParcelable, Image {
        public static final ci CREATOR = new ci();
        private static final HashMap<String, C1308a<?, ?>> iC = new HashMap();
        private final int ab;
        private final Set<Integer> iD;
        private String ie;

        static {
            iC.put(PlusShare.KEY_CALL_TO_ACTION_URL, C1308a.m675f(PlusShare.KEY_CALL_TO_ACTION_URL, 2));
        }

        public C1717c() {
            this.ab = 1;
            this.iD = new HashSet();
        }

        public C1717c(String str) {
            this.iD = new HashSet();
            this.ab = 1;
            this.ie = str;
            this.iD.add(Integer.valueOf(2));
        }

        C1717c(Set<Integer> set, int i, String str) {
            this.iD = set;
            this.ab = i;
            this.ie = str;
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
                    return this.ie;
                default:
                    throw new IllegalStateException("Unknown safe parcelable id=" + c1308a.aa());
            }
        }

        Set<Integer> bH() {
            return this.iD;
        }

        public C1717c cq() {
            return this;
        }

        public int describeContents() {
            ci ciVar = CREATOR;
            return 0;
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof C1717c)) {
                return false;
            }
            if (this == obj) {
                return true;
            }
            C1717c c1717c = (C1717c) obj;
            for (C1308a c1308a : iC.values()) {
                if (mo2354a(c1308a)) {
                    if (!c1717c.mo2354a(c1308a)) {
                        return false;
                    }
                    if (!mo2355b(c1308a).equals(c1717c.mo2355b(c1308a))) {
                        return false;
                    }
                } else if (c1717c.mo2354a(c1308a)) {
                    return false;
                }
            }
            return true;
        }

        public /* synthetic */ Object freeze() {
            return cq();
        }

        public String getUrl() {
            return this.ie;
        }

        public boolean hasUrl() {
            return this.iD.contains(Integer.valueOf(2));
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

        int m1315i() {
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
            ci ciVar = CREATOR;
            ci.m434a(this, out, flags);
        }
    }

    public static final class C1718d extends ae implements SafeParcelable, Name {
        public static final cj CREATOR = new cj();
        private static final HashMap<String, C1308a<?, ?>> iC = new HashMap();
        private final int ab;
        private final Set<Integer> iD;
        private String jc;
        private String jf;
        private String kg;
        private String kh;
        private String ki;
        private String kj;

        static {
            iC.put("familyName", C1308a.m675f("familyName", 2));
            iC.put("formatted", C1308a.m675f("formatted", 3));
            iC.put("givenName", C1308a.m675f("givenName", 4));
            iC.put("honorificPrefix", C1308a.m675f("honorificPrefix", 5));
            iC.put("honorificSuffix", C1308a.m675f("honorificSuffix", 6));
            iC.put("middleName", C1308a.m675f("middleName", 7));
        }

        public C1718d() {
            this.ab = 1;
            this.iD = new HashSet();
        }

        C1718d(Set<Integer> set, int i, String str, String str2, String str3, String str4, String str5, String str6) {
            this.iD = set;
            this.ab = i;
            this.jc = str;
            this.kg = str2;
            this.jf = str3;
            this.kh = str4;
            this.ki = str5;
            this.kj = str6;
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
                    return this.jc;
                case 3:
                    return this.kg;
                case 4:
                    return this.jf;
                case 5:
                    return this.kh;
                case 6:
                    return this.ki;
                case 7:
                    return this.kj;
                default:
                    throw new IllegalStateException("Unknown safe parcelable id=" + c1308a.aa());
            }
        }

        Set<Integer> bH() {
            return this.iD;
        }

        public C1718d cr() {
            return this;
        }

        public int describeContents() {
            cj cjVar = CREATOR;
            return 0;
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof C1718d)) {
                return false;
            }
            if (this == obj) {
                return true;
            }
            C1718d c1718d = (C1718d) obj;
            for (C1308a c1308a : iC.values()) {
                if (mo2354a(c1308a)) {
                    if (!c1718d.mo2354a(c1308a)) {
                        return false;
                    }
                    if (!mo2355b(c1308a).equals(c1718d.mo2355b(c1308a))) {
                        return false;
                    }
                } else if (c1718d.mo2354a(c1308a)) {
                    return false;
                }
            }
            return true;
        }

        public /* synthetic */ Object freeze() {
            return cr();
        }

        public String getFamilyName() {
            return this.jc;
        }

        public String getFormatted() {
            return this.kg;
        }

        public String getGivenName() {
            return this.jf;
        }

        public String getHonorificPrefix() {
            return this.kh;
        }

        public String getHonorificSuffix() {
            return this.ki;
        }

        public String getMiddleName() {
            return this.kj;
        }

        public boolean hasFamilyName() {
            return this.iD.contains(Integer.valueOf(2));
        }

        public boolean hasFormatted() {
            return this.iD.contains(Integer.valueOf(3));
        }

        public boolean hasGivenName() {
            return this.iD.contains(Integer.valueOf(4));
        }

        public boolean hasHonorificPrefix() {
            return this.iD.contains(Integer.valueOf(5));
        }

        public boolean hasHonorificSuffix() {
            return this.iD.contains(Integer.valueOf(6));
        }

        public boolean hasMiddleName() {
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

        int m1321i() {
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
            cj cjVar = CREATOR;
            cj.m436a(this, out, flags);
        }
    }

    public static final class C1719f extends ae implements SafeParcelable, Organizations {
        public static final ck CREATOR = new ck();
        private static final HashMap<String, C1308a<?, ?>> iC = new HashMap();
        private int aJ;
        private final int ab;
        private String di;
        private String hs;
        private final Set<Integer> iD;
        private String jb;
        private String js;
        private String kk;
        private String kl;
        private boolean km;
        private String mName;

        static {
            iC.put("department", C1308a.m675f("department", 2));
            iC.put(PlusShare.KEY_CONTENT_DEEP_LINK_METADATA_DESCRIPTION, C1308a.m675f(PlusShare.KEY_CONTENT_DEEP_LINK_METADATA_DESCRIPTION, 3));
            iC.put("endDate", C1308a.m675f("endDate", 4));
            iC.put("location", C1308a.m675f("location", 5));
            iC.put("name", C1308a.m675f("name", 6));
            iC.put("primary", C1308a.m674e("primary", 7));
            iC.put("startDate", C1308a.m675f("startDate", 8));
            iC.put(PlusShare.KEY_CONTENT_DEEP_LINK_METADATA_TITLE, C1308a.m675f(PlusShare.KEY_CONTENT_DEEP_LINK_METADATA_TITLE, 9));
            iC.put("type", C1308a.m668a("type", 10, new ab().m665b("work", 0).m665b("school", 1), false));
        }

        public C1719f() {
            this.ab = 1;
            this.iD = new HashSet();
        }

        C1719f(Set<Integer> set, int i, String str, String str2, String str3, String str4, String str5, boolean z, String str6, String str7, int i2) {
            this.iD = set;
            this.ab = i;
            this.kk = str;
            this.di = str2;
            this.jb = str3;
            this.kl = str4;
            this.mName = str5;
            this.km = z;
            this.js = str6;
            this.hs = str7;
            this.aJ = i2;
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
                    return this.kk;
                case 3:
                    return this.di;
                case 4:
                    return this.jb;
                case 5:
                    return this.kl;
                case 6:
                    return this.mName;
                case 7:
                    return Boolean.valueOf(this.km);
                case 8:
                    return this.js;
                case 9:
                    return this.hs;
                case 10:
                    return Integer.valueOf(this.aJ);
                default:
                    throw new IllegalStateException("Unknown safe parcelable id=" + c1308a.aa());
            }
        }

        Set<Integer> bH() {
            return this.iD;
        }

        public C1719f cs() {
            return this;
        }

        public int describeContents() {
            ck ckVar = CREATOR;
            return 0;
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof C1719f)) {
                return false;
            }
            if (this == obj) {
                return true;
            }
            C1719f c1719f = (C1719f) obj;
            for (C1308a c1308a : iC.values()) {
                if (mo2354a(c1308a)) {
                    if (!c1719f.mo2354a(c1308a)) {
                        return false;
                    }
                    if (!mo2355b(c1308a).equals(c1719f.mo2355b(c1308a))) {
                        return false;
                    }
                } else if (c1719f.mo2354a(c1308a)) {
                    return false;
                }
            }
            return true;
        }

        public /* synthetic */ Object freeze() {
            return cs();
        }

        public String getDepartment() {
            return this.kk;
        }

        public String getDescription() {
            return this.di;
        }

        public String getEndDate() {
            return this.jb;
        }

        public String getLocation() {
            return this.kl;
        }

        public String getName() {
            return this.mName;
        }

        public String getStartDate() {
            return this.js;
        }

        public String getTitle() {
            return this.hs;
        }

        public int getType() {
            return this.aJ;
        }

        public boolean hasDepartment() {
            return this.iD.contains(Integer.valueOf(2));
        }

        public boolean hasDescription() {
            return this.iD.contains(Integer.valueOf(3));
        }

        public boolean hasEndDate() {
            return this.iD.contains(Integer.valueOf(4));
        }

        public boolean hasLocation() {
            return this.iD.contains(Integer.valueOf(5));
        }

        public boolean hasName() {
            return this.iD.contains(Integer.valueOf(6));
        }

        public boolean hasPrimary() {
            return this.iD.contains(Integer.valueOf(7));
        }

        public boolean hasStartDate() {
            return this.iD.contains(Integer.valueOf(8));
        }

        public boolean hasTitle() {
            return this.iD.contains(Integer.valueOf(9));
        }

        public boolean hasType() {
            return this.iD.contains(Integer.valueOf(10));
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

        int m1327i() {
            return this.ab;
        }

        public boolean isDataValid() {
            return true;
        }

        public boolean isPrimary() {
            return this.km;
        }

        protected Object mo1088m(String str) {
            return null;
        }

        protected boolean mo1089n(String str) {
            return false;
        }

        public void writeToParcel(Parcel out, int flags) {
            ck ckVar = CREATOR;
            ck.m438a(this, out, flags);
        }
    }

    public static final class C1720g extends ae implements SafeParcelable, PlacesLived {
        public static final cl CREATOR = new cl();
        private static final HashMap<String, C1308a<?, ?>> iC = new HashMap();
        private final int ab;
        private final Set<Integer> iD;
        private boolean km;
        private String mValue;

        static {
            iC.put("primary", C1308a.m674e("primary", 2));
            iC.put("value", C1308a.m675f("value", 3));
        }

        public C1720g() {
            this.ab = 1;
            this.iD = new HashSet();
        }

        C1720g(Set<Integer> set, int i, boolean z, String str) {
            this.iD = set;
            this.ab = i;
            this.km = z;
            this.mValue = str;
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
                    return Boolean.valueOf(this.km);
                case 3:
                    return this.mValue;
                default:
                    throw new IllegalStateException("Unknown safe parcelable id=" + c1308a.aa());
            }
        }

        Set<Integer> bH() {
            return this.iD;
        }

        public C1720g ct() {
            return this;
        }

        public int describeContents() {
            cl clVar = CREATOR;
            return 0;
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof C1720g)) {
                return false;
            }
            if (this == obj) {
                return true;
            }
            C1720g c1720g = (C1720g) obj;
            for (C1308a c1308a : iC.values()) {
                if (mo2354a(c1308a)) {
                    if (!c1720g.mo2354a(c1308a)) {
                        return false;
                    }
                    if (!mo2355b(c1308a).equals(c1720g.mo2355b(c1308a))) {
                        return false;
                    }
                } else if (c1720g.mo2354a(c1308a)) {
                    return false;
                }
            }
            return true;
        }

        public /* synthetic */ Object freeze() {
            return ct();
        }

        public String getValue() {
            return this.mValue;
        }

        public boolean hasPrimary() {
            return this.iD.contains(Integer.valueOf(2));
        }

        public boolean hasValue() {
            return this.iD.contains(Integer.valueOf(3));
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

        int m1333i() {
            return this.ab;
        }

        public boolean isDataValid() {
            return true;
        }

        public boolean isPrimary() {
            return this.km;
        }

        protected Object mo1088m(String str) {
            return null;
        }

        protected boolean mo1089n(String str) {
            return false;
        }

        public void writeToParcel(Parcel out, int flags) {
            cl clVar = CREATOR;
            cl.m440a(this, out, flags);
        }
    }

    public static final class C1721h extends ae implements SafeParcelable, Urls {
        public static final cm CREATOR = new cm();
        private static final HashMap<String, C1308a<?, ?>> iC = new HashMap();
        private int aJ;
        private final int ab;
        private final Set<Integer> iD;
        private String kn;
        private final int ko;
        private String mValue;

        static {
            iC.put(PlusShare.KEY_CALL_TO_ACTION_LABEL, C1308a.m675f(PlusShare.KEY_CALL_TO_ACTION_LABEL, 5));
            iC.put("type", C1308a.m668a("type", 6, new ab().m665b("home", 0).m665b("work", 1).m665b("blog", 2).m665b("profile", 3).m665b("other", 4).m665b("otherProfile", 5).m665b("contributor", 6).m665b("website", 7), false));
            iC.put("value", C1308a.m675f("value", 4));
        }

        public C1721h() {
            this.ko = 4;
            this.ab = 2;
            this.iD = new HashSet();
        }

        C1721h(Set<Integer> set, int i, String str, int i2, String str2, int i3) {
            this.ko = 4;
            this.iD = set;
            this.ab = i;
            this.kn = str;
            this.aJ = i2;
            this.mValue = str2;
        }

        public HashMap<String, C1308a<?, ?>> mo1087T() {
            return iC;
        }

        protected boolean mo2354a(C1308a c1308a) {
            return this.iD.contains(Integer.valueOf(c1308a.aa()));
        }

        protected Object mo2355b(C1308a c1308a) {
            switch (c1308a.aa()) {
                case 4:
                    return this.mValue;
                case 5:
                    return this.kn;
                case 6:
                    return Integer.valueOf(this.aJ);
                default:
                    throw new IllegalStateException("Unknown safe parcelable id=" + c1308a.aa());
            }
        }

        Set<Integer> bH() {
            return this.iD;
        }

        @Deprecated
        public int cu() {
            return 4;
        }

        public C1721h cv() {
            return this;
        }

        public int describeContents() {
            cm cmVar = CREATOR;
            return 0;
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof C1721h)) {
                return false;
            }
            if (this == obj) {
                return true;
            }
            C1721h c1721h = (C1721h) obj;
            for (C1308a c1308a : iC.values()) {
                if (mo2354a(c1308a)) {
                    if (!c1721h.mo2354a(c1308a)) {
                        return false;
                    }
                    if (!mo2355b(c1308a).equals(c1721h.mo2355b(c1308a))) {
                        return false;
                    }
                } else if (c1721h.mo2354a(c1308a)) {
                    return false;
                }
            }
            return true;
        }

        public /* synthetic */ Object freeze() {
            return cv();
        }

        public String getLabel() {
            return this.kn;
        }

        public int getType() {
            return this.aJ;
        }

        public String getValue() {
            return this.mValue;
        }

        public boolean hasLabel() {
            return this.iD.contains(Integer.valueOf(5));
        }

        public boolean hasType() {
            return this.iD.contains(Integer.valueOf(6));
        }

        public boolean hasValue() {
            return this.iD.contains(Integer.valueOf(4));
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

        int m1339i() {
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
            cm cmVar = CREATOR;
            cm.m442a(this, out, flags);
        }
    }

    static {
        iC.put("aboutMe", C1308a.m675f("aboutMe", 2));
        iC.put("ageRange", C1308a.m669a("ageRange", 3, C1713a.class));
        iC.put("birthday", C1308a.m675f("birthday", 4));
        iC.put("braggingRights", C1308a.m675f("braggingRights", 5));
        iC.put("circledByCount", C1308a.m671c("circledByCount", 6));
        iC.put("cover", C1308a.m669a("cover", 7, C1716b.class));
        iC.put("currentLocation", C1308a.m675f("currentLocation", 8));
        iC.put("displayName", C1308a.m675f("displayName", 9));
        iC.put("gender", C1308a.m668a("gender", 12, new ab().m665b("male", 0).m665b("female", 1).m665b("other", 2), false));
        iC.put("id", C1308a.m675f("id", 14));
        iC.put("image", C1308a.m669a("image", 15, C1717c.class));
        iC.put("isPlusUser", C1308a.m674e("isPlusUser", 16));
        iC.put("language", C1308a.m675f("language", 18));
        iC.put("name", C1308a.m669a("name", 19, C1718d.class));
        iC.put("nickname", C1308a.m675f("nickname", 20));
        iC.put("objectType", C1308a.m668a("objectType", 21, new ab().m665b("person", 0).m665b("page", 1), false));
        iC.put("organizations", C1308a.m670b("organizations", 22, C1719f.class));
        iC.put("placesLived", C1308a.m670b("placesLived", 23, C1720g.class));
        iC.put("plusOneCount", C1308a.m671c("plusOneCount", 24));
        iC.put("relationshipStatus", C1308a.m668a("relationshipStatus", 25, new ab().m665b("single", 0).m665b("in_a_relationship", 1).m665b("engaged", 2).m665b("married", 3).m665b("its_complicated", 4).m665b("open_relationship", 5).m665b("widowed", 6).m665b("in_domestic_partnership", 7).m665b("in_civil_union", 8), false));
        iC.put("tagline", C1308a.m675f("tagline", 26));
        iC.put(PlusShare.KEY_CALL_TO_ACTION_URL, C1308a.m675f(PlusShare.KEY_CALL_TO_ACTION_URL, 27));
        iC.put("urls", C1308a.m670b("urls", 28, C1721h.class));
        iC.put("verified", C1308a.m674e("verified", 29));
    }

    public cc() {
        this.ab = 2;
        this.iD = new HashSet();
    }

    public cc(String str, String str2, C1717c c1717c, int i, String str3) {
        this.ab = 2;
        this.iD = new HashSet();
        this.cl = str;
        this.iD.add(Integer.valueOf(9));
        this.jh = str2;
        this.iD.add(Integer.valueOf(14));
        this.jM = c1717c;
        this.iD.add(Integer.valueOf(15));
        this.jR = i;
        this.iD.add(Integer.valueOf(21));
        this.ie = str3;
        this.iD.add(Integer.valueOf(27));
    }

    cc(Set<Integer> set, int i, String str, C1713a c1713a, String str2, String str3, int i2, C1716b c1716b, String str4, String str5, int i3, String str6, C1717c c1717c, boolean z, String str7, C1718d c1718d, String str8, int i4, List<C1719f> list, List<C1720g> list2, int i5, int i6, String str9, String str10, List<C1721h> list3, boolean z2) {
        this.iD = set;
        this.ab = i;
        this.jE = str;
        this.jF = c1713a;
        this.jG = str2;
        this.jH = str3;
        this.jI = i2;
        this.jJ = c1716b;
        this.jK = str4;
        this.cl = str5;
        this.jL = i3;
        this.jh = str6;
        this.jM = c1717c;
        this.jN = z;
        this.jO = str7;
        this.jP = c1718d;
        this.jQ = str8;
        this.jR = i4;
        this.jS = list;
        this.jT = list2;
        this.jU = i5;
        this.jV = i6;
        this.jW = str9;
        this.ie = str10;
        this.jX = list3;
        this.jY = z2;
    }

    public static cc m1342d(byte[] bArr) {
        Parcel obtain = Parcel.obtain();
        obtain.unmarshall(bArr, 0, bArr.length);
        obtain.setDataPosition(0);
        cc y = CREATOR.m424y(obtain);
        obtain.recycle();
        return y;
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
                return this.jE;
            case 3:
                return this.jF;
            case 4:
                return this.jG;
            case 5:
                return this.jH;
            case 6:
                return Integer.valueOf(this.jI);
            case 7:
                return this.jJ;
            case 8:
                return this.jK;
            case 9:
                return this.cl;
            case 12:
                return Integer.valueOf(this.jL);
            case 14:
                return this.jh;
            case 15:
                return this.jM;
            case 16:
                return Boolean.valueOf(this.jN);
            case 18:
                return this.jO;
            case 19:
                return this.jP;
            case 20:
                return this.jQ;
            case 21:
                return Integer.valueOf(this.jR);
            case 22:
                return this.jS;
            case 23:
                return this.jT;
            case 24:
                return Integer.valueOf(this.jU);
            case 25:
                return Integer.valueOf(this.jV);
            case NalUnitTypes.NAL_TYPE_RSV_VCL26 /*26*/:
                return this.jW;
            case NalUnitTypes.NAL_TYPE_RSV_VCL27 /*27*/:
                return this.ie;
            case NalUnitTypes.NAL_TYPE_RSV_VCL28 /*28*/:
                return this.jX;
            case NalUnitTypes.NAL_TYPE_RSV_VCL29 /*29*/:
                return Boolean.valueOf(this.jY);
            default:
                throw new IllegalStateException("Unknown safe parcelable id=" + c1308a.aa());
        }
    }

    Set<Integer> bH() {
        return this.iD;
    }

    C1713a cc() {
        return this.jF;
    }

    C1716b cd() {
        return this.jJ;
    }

    C1717c ce() {
        return this.jM;
    }

    C1718d cf() {
        return this.jP;
    }

    List<C1719f> cg() {
        return this.jS;
    }

    List<C1720g> ch() {
        return this.jT;
    }

    List<C1721h> ci() {
        return this.jX;
    }

    public cc cj() {
        return this;
    }

    public int describeContents() {
        cd cdVar = CREATOR;
        return 0;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof cc)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        cc ccVar = (cc) obj;
        for (C1308a c1308a : iC.values()) {
            if (mo2354a(c1308a)) {
                if (!ccVar.mo2354a(c1308a)) {
                    return false;
                }
                if (!mo2355b(c1308a).equals(ccVar.mo2355b(c1308a))) {
                    return false;
                }
            } else if (ccVar.mo2354a(c1308a)) {
                return false;
            }
        }
        return true;
    }

    public /* synthetic */ Object freeze() {
        return cj();
    }

    public String getAboutMe() {
        return this.jE;
    }

    public AgeRange getAgeRange() {
        return this.jF;
    }

    public String getBirthday() {
        return this.jG;
    }

    public String getBraggingRights() {
        return this.jH;
    }

    public int getCircledByCount() {
        return this.jI;
    }

    public Cover getCover() {
        return this.jJ;
    }

    public String getCurrentLocation() {
        return this.jK;
    }

    public String getDisplayName() {
        return this.cl;
    }

    @Deprecated
    public List<Emails> getEmails() {
        return null;
    }

    public int getGender() {
        return this.jL;
    }

    public String getId() {
        return this.jh;
    }

    public Image getImage() {
        return this.jM;
    }

    public String getLanguage() {
        return this.jO;
    }

    public Name getName() {
        return this.jP;
    }

    public String getNickname() {
        return this.jQ;
    }

    public int getObjectType() {
        return this.jR;
    }

    public List<Organizations> getOrganizations() {
        return (ArrayList) this.jS;
    }

    public List<PlacesLived> getPlacesLived() {
        return (ArrayList) this.jT;
    }

    public int getPlusOneCount() {
        return this.jU;
    }

    public int getRelationshipStatus() {
        return this.jV;
    }

    public String getTagline() {
        return this.jW;
    }

    public String getUrl() {
        return this.ie;
    }

    public List<Urls> getUrls() {
        return (ArrayList) this.jX;
    }

    public boolean hasAboutMe() {
        return this.iD.contains(Integer.valueOf(2));
    }

    public boolean hasAgeRange() {
        return this.iD.contains(Integer.valueOf(3));
    }

    public boolean hasBirthday() {
        return this.iD.contains(Integer.valueOf(4));
    }

    public boolean hasBraggingRights() {
        return this.iD.contains(Integer.valueOf(5));
    }

    public boolean hasCircledByCount() {
        return this.iD.contains(Integer.valueOf(6));
    }

    public boolean hasCover() {
        return this.iD.contains(Integer.valueOf(7));
    }

    public boolean hasCurrentLocation() {
        return this.iD.contains(Integer.valueOf(8));
    }

    public boolean hasDisplayName() {
        return this.iD.contains(Integer.valueOf(9));
    }

    @Deprecated
    public boolean hasEmails() {
        return false;
    }

    public boolean hasGender() {
        return this.iD.contains(Integer.valueOf(12));
    }

    public boolean hasId() {
        return this.iD.contains(Integer.valueOf(14));
    }

    public boolean hasImage() {
        return this.iD.contains(Integer.valueOf(15));
    }

    public boolean hasIsPlusUser() {
        return this.iD.contains(Integer.valueOf(16));
    }

    public boolean hasLanguage() {
        return this.iD.contains(Integer.valueOf(18));
    }

    public boolean hasName() {
        return this.iD.contains(Integer.valueOf(19));
    }

    public boolean hasNickname() {
        return this.iD.contains(Integer.valueOf(20));
    }

    public boolean hasObjectType() {
        return this.iD.contains(Integer.valueOf(21));
    }

    public boolean hasOrganizations() {
        return this.iD.contains(Integer.valueOf(22));
    }

    public boolean hasPlacesLived() {
        return this.iD.contains(Integer.valueOf(23));
    }

    public boolean hasPlusOneCount() {
        return this.iD.contains(Integer.valueOf(24));
    }

    public boolean hasRelationshipStatus() {
        return this.iD.contains(Integer.valueOf(25));
    }

    public boolean hasTagline() {
        return this.iD.contains(Integer.valueOf(26));
    }

    public boolean hasUrl() {
        return this.iD.contains(Integer.valueOf(27));
    }

    public boolean hasUrls() {
        return this.iD.contains(Integer.valueOf(28));
    }

    public boolean hasVerified() {
        return this.iD.contains(Integer.valueOf(29));
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

    int m1346i() {
        return this.ab;
    }

    public boolean isDataValid() {
        return true;
    }

    public boolean isPlusUser() {
        return this.jN;
    }

    public boolean isVerified() {
        return this.jY;
    }

    protected Object mo1088m(String str) {
        return null;
    }

    protected boolean mo1089n(String str) {
        return false;
    }

    public void writeToParcel(Parcel out, int flags) {
        cd cdVar = CREATOR;
        cd.m422a(this, out, flags);
    }
}
