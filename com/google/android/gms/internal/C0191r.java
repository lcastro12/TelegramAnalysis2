package com.google.android.gms.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class C0191r {

    public static final class C0190a {
        private final List<String> bY;
        private final Object bZ;

        private C0190a(Object obj) {
            this.bZ = C0192s.m521d(obj);
            this.bY = new ArrayList();
        }

        public C0190a m512a(String str, Object obj) {
            this.bY.add(((String) C0192s.m521d(str)) + "=" + String.valueOf(obj));
            return this;
        }

        public String toString() {
            StringBuilder append = new StringBuilder(100).append(this.bZ.getClass().getSimpleName()).append('{');
            int size = this.bY.size();
            for (int i = 0; i < size; i++) {
                append.append((String) this.bY.get(i));
                if (i < size - 1) {
                    append.append(", ");
                }
            }
            return append.append('}').toString();
        }
    }

    public static boolean m513a(Object obj, Object obj2) {
        return obj == obj2 || (obj != null && obj.equals(obj2));
    }

    public static C0190a m514c(Object obj) {
        return new C0190a(obj);
    }

    public static int hashCode(Object... objects) {
        return Arrays.hashCode(objects);
    }
}
