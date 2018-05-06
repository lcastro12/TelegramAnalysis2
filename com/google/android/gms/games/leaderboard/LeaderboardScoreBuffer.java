package com.google.android.gms.games.leaderboard;

import com.google.android.gms.common.data.C1287d;
import com.google.android.gms.common.data.DataBuffer;

public final class LeaderboardScoreBuffer extends DataBuffer<LeaderboardScore> {
    private final C0153b ep;

    public LeaderboardScoreBuffer(C1287d dataHolder) {
        super(dataHolder);
        this.ep = new C0153b(dataHolder.m639l());
    }

    public C0153b aF() {
        return this.ep;
    }

    public LeaderboardScore get(int position) {
        return new C1693d(this.S, position);
    }
}
