package com.google.android.gms.common.data;

import android.database.CharArrayBuffer;
import android.net.Uri;
import com.google.android.gms.internal.C0191r;
import com.google.android.gms.internal.C0192s;

public abstract class C0130b {
    protected final C1287d f32S;
    protected final int f33V;
    private final int f34W;

    public C0130b(C1287d c1287d, int i) {
        this.f32S = (C1287d) C0192s.m521d(c1287d);
        boolean z = i >= 0 && i < c1287d.getCount();
        C0192s.m515a(z);
        this.f33V = i;
        this.f34W = c1287d.m631e(this.f33V);
    }

    protected void m34a(String str, CharArrayBuffer charArrayBuffer) {
        this.f32S.m627a(str, this.f33V, this.f34W, charArrayBuffer);
    }

    protected Uri m35d(String str) {
        return this.f32S.m633f(str, this.f33V, this.f34W);
    }

    protected boolean m36e(String str) {
        return this.f32S.m634g(str, this.f33V, this.f34W);
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof C0130b)) {
            return false;
        }
        C0130b c0130b = (C0130b) obj;
        return C0191r.m513a(Integer.valueOf(c0130b.f33V), Integer.valueOf(this.f33V)) && C0191r.m513a(Integer.valueOf(c0130b.f34W), Integer.valueOf(this.f34W)) && c0130b.f32S == this.f32S;
    }

    protected boolean getBoolean(String column) {
        return this.f32S.m630d(column, this.f33V, this.f34W);
    }

    protected byte[] getByteArray(String column) {
        return this.f32S.m632e(column, this.f33V, this.f34W);
    }

    protected int getInteger(String column) {
        return this.f32S.m628b(column, this.f33V, this.f34W);
    }

    protected long getLong(String column) {
        return this.f32S.m626a(column, this.f33V, this.f34W);
    }

    protected String getString(String column) {
        return this.f32S.m629c(column, this.f33V, this.f34W);
    }

    public int hashCode() {
        return C0191r.hashCode(Integer.valueOf(this.f33V), Integer.valueOf(this.f34W), this.f32S);
    }

    public boolean isDataValid() {
        return !this.f32S.isClosed();
    }
}
