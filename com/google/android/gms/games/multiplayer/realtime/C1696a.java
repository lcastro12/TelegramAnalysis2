package com.google.android.gms.games.multiplayer.realtime;

import com.google.android.gms.common.data.C1287d;
import com.google.android.gms.common.data.C1288f;

public final class C1696a extends C1288f<Room> {
    public C1696a(C1287d c1287d) {
        super(c1287d);
    }

    protected /* synthetic */ Object mo2301a(int i, int i2) {
        return m1142b(i, i2);
    }

    protected Room m1142b(int i, int i2) {
        return new C1697c(this.S, i, i2);
    }

    protected String getPrimaryDataMarkerColumn() {
        return "external_match_id";
    }
}
