package com.google.android.gms.appstate;

import com.google.android.gms.common.data.C0130b;
import com.google.android.gms.common.data.C1287d;

public final class C1688b extends C0130b implements AppState {
    C1688b(C1287d c1287d, int i) {
        super(c1287d, i);
    }

    public AppState m1133a() {
        return new C1687a(this);
    }

    public boolean equals(Object obj) {
        return C1687a.m1130a(this, obj);
    }

    public /* synthetic */ Object freeze() {
        return m1133a();
    }

    public byte[] getConflictData() {
        return getByteArray("conflict_data");
    }

    public String getConflictVersion() {
        return getString("conflict_version");
    }

    public int getKey() {
        return getInteger("key");
    }

    public byte[] getLocalData() {
        return getByteArray("local_data");
    }

    public String getLocalVersion() {
        return getString("local_version");
    }

    public boolean hasConflict() {
        return !m36e("conflict_version");
    }

    public int hashCode() {
        return C1687a.m1129a(this);
    }

    public String toString() {
        return C1687a.m1131b(this);
    }
}
