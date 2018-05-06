package com.google.android.gms.games.leaderboard;

import com.google.android.gms.common.data.C1287d;
import com.google.android.gms.common.data.C1288f;

public final class LeaderboardBuffer extends C1288f<Leaderboard> {
    public LeaderboardBuffer(C1287d dataHolder) {
        super(dataHolder);
    }

    protected /* synthetic */ Object mo2301a(int i, int i2) {
        return getEntry(i, i2);
    }

    protected Leaderboard getEntry(int rowIndex, int numChildren) {
        return new C1300a(this.S, rowIndex, numChildren);
    }

    protected String getPrimaryDataMarkerColumn() {
        return "external_leaderboard_id";
    }
}
