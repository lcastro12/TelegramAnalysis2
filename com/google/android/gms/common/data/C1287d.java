package com.google.android.gms.common.data;

import android.database.CharArrayBuffer;
import android.database.CursorIndexOutOfBoundsException;
import android.database.CursorWindow;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.internal.C0192s;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class C1287d implements SafeParcelable {
    public static final C0132e CREATOR = new C0132e();
    private static final HashMap<CursorWindow, Throwable> f70Z = ((HashMap) null);
    private static final Object aa = new Object();
    private static final C0131a ai = new C0131a(new String[0], null) {
    };
    private final int ab;
    private final String[] ac;
    Bundle ad;
    private final CursorWindow[] ae;
    private final Bundle af;
    int[] ag;
    int ah;
    boolean mClosed;
    private final int f71p;

    public static class C0131a {
        private final String[] ac;
        private final ArrayList<HashMap<String, Object>> aj;
        private final String ak;
        private final HashMap<Object, Integer> al;
        private boolean am;
        private String an;

        private C0131a(String[] strArr, String str) {
            this.ac = (String[]) C0192s.m521d(strArr);
            this.aj = new ArrayList();
            this.ak = str;
            this.al = new HashMap();
            this.am = false;
            this.an = null;
        }
    }

    C1287d(int i, String[] strArr, CursorWindow[] cursorWindowArr, int i2, Bundle bundle) {
        this.mClosed = false;
        this.ab = i;
        this.ac = strArr;
        this.ae = cursorWindowArr;
        this.f71p = i2;
        this.af = bundle;
    }

    private C1287d(C0131a c0131a, int i, Bundle bundle) {
        this(c0131a.ac, C1287d.m624a(c0131a), i, bundle);
    }

    public C1287d(String[] strArr, CursorWindow[] cursorWindowArr, int i, Bundle bundle) {
        this.mClosed = false;
        this.ab = 1;
        this.ac = (String[]) C0192s.m521d(strArr);
        this.ae = (CursorWindow[]) C0192s.m521d(cursorWindowArr);
        this.f71p = i;
        this.af = bundle;
        m635h();
    }

    public static C1287d m621a(int i, Bundle bundle) {
        return new C1287d(ai, i, bundle);
    }

    private static void m622a(CursorWindow cursorWindow) {
    }

    private void m623a(String str, int i) {
        if (this.ad == null || !this.ad.containsKey(str)) {
            throw new IllegalArgumentException("No such column: " + str);
        } else if (isClosed()) {
            throw new IllegalArgumentException("Buffer is closed.");
        } else if (i < 0 || i >= this.ah) {
            throw new CursorIndexOutOfBoundsException(i, this.ah);
        }
    }

    private static CursorWindow[] m624a(C0131a c0131a) {
        if (c0131a.ac.length == 0) {
            return new CursorWindow[0];
        }
        ArrayList c = c0131a.aj;
        int size = c.size();
        CursorWindow cursorWindow = new CursorWindow(false);
        CursorWindow[] cursorWindowArr = new CursorWindow[]{cursorWindow};
        cursorWindow.setNumColumns(c0131a.ac.length);
        int i = 0;
        while (i < size) {
            try {
                if (cursorWindow.allocRow()) {
                    Map map = (Map) c.get(i);
                    for (int i2 = 0; i2 < c0131a.ac.length; i2++) {
                        String str = c0131a.ac[i2];
                        Object obj = map.get(str);
                        if (obj == null) {
                            cursorWindow.putNull(i, i2);
                        } else if (obj instanceof String) {
                            cursorWindow.putString((String) obj, i, i2);
                        } else if (obj instanceof Long) {
                            cursorWindow.putLong(((Long) obj).longValue(), i, i2);
                        } else if (obj instanceof Integer) {
                            cursorWindow.putLong((long) ((Integer) obj).intValue(), i, i2);
                        } else if (obj instanceof Boolean) {
                            cursorWindow.putLong(((Boolean) obj).booleanValue() ? 1 : 0, i, i2);
                        } else if (obj instanceof byte[]) {
                            cursorWindow.putBlob((byte[]) obj, i, i2);
                        } else {
                            throw new IllegalArgumentException("Unsupported object for column " + str + ": " + obj);
                        }
                    }
                    i++;
                } else {
                    throw new RuntimeException("Cursor window out of memory");
                }
            } catch (RuntimeException e) {
                cursorWindow.close();
                throw e;
            }
        }
        return cursorWindowArr;
    }

    public static C1287d m625f(int i) {
        return C1287d.m621a(i, null);
    }

    public long m626a(String str, int i, int i2) {
        m623a(str, i);
        return this.ae[i2].getLong(i - this.ag[i2], this.ad.getInt(str));
    }

    public void m627a(String str, int i, int i2, CharArrayBuffer charArrayBuffer) {
        m623a(str, i);
        this.ae[i2].copyStringToBuffer(i - this.ag[i2], this.ad.getInt(str), charArrayBuffer);
    }

    public int m628b(String str, int i, int i2) {
        m623a(str, i);
        return this.ae[i2].getInt(i - this.ag[i2], this.ad.getInt(str));
    }

    public String m629c(String str, int i, int i2) {
        m623a(str, i);
        return this.ae[i2].getString(i - this.ag[i2], this.ad.getInt(str));
    }

    public void close() {
        synchronized (this) {
            if (!this.mClosed) {
                this.mClosed = true;
                for (int i = 0; i < this.ae.length; i++) {
                    this.ae[i].close();
                    C1287d.m622a(this.ae[i]);
                }
            }
        }
    }

    public boolean m630d(String str, int i, int i2) {
        m623a(str, i);
        return Long.valueOf(this.ae[i2].getLong(i - this.ag[i2], this.ad.getInt(str))).longValue() == 1;
    }

    public int describeContents() {
        return 0;
    }

    public int m631e(int i) {
        int i2 = 0;
        boolean z = i >= 0 && i < this.ah;
        C0192s.m515a(z);
        while (i2 < this.ag.length) {
            if (i < this.ag[i2]) {
                i2--;
                break;
            }
            i2++;
        }
        return i2 == this.ag.length ? i2 - 1 : i2;
    }

    public byte[] m632e(String str, int i, int i2) {
        m623a(str, i);
        return this.ae[i2].getBlob(i - this.ag[i2], this.ad.getInt(str));
    }

    public Uri m633f(String str, int i, int i2) {
        String c = m629c(str, i, i2);
        return c == null ? null : Uri.parse(c);
    }

    public boolean m634g(String str, int i, int i2) {
        m623a(str, i);
        return this.ae[i2].isNull(i - this.ag[i2], this.ad.getInt(str));
    }

    public int getCount() {
        return this.ah;
    }

    public int getStatusCode() {
        return this.f71p;
    }

    public void m635h() {
        int i;
        int i2 = 0;
        this.ad = new Bundle();
        for (i = 0; i < this.ac.length; i++) {
            this.ad.putInt(this.ac[i], i);
        }
        this.ag = new int[this.ae.length];
        i = 0;
        while (i2 < this.ae.length) {
            this.ag[i2] = i;
            i += this.ae[i2].getNumRows();
            i2++;
        }
        this.ah = i;
    }

    int m636i() {
        return this.ab;
    }

    public boolean isClosed() {
        boolean z;
        synchronized (this) {
            z = this.mClosed;
        }
        return z;
    }

    String[] m637j() {
        return this.ac;
    }

    CursorWindow[] m638k() {
        return this.ae;
    }

    public Bundle m639l() {
        return this.af;
    }

    public void writeToParcel(Parcel dest, int flags) {
        C0132e.m39a(this, dest, flags);
    }
}
