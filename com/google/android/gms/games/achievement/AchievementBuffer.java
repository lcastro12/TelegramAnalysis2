package com.google.android.gms.games.achievement;

import com.google.android.gms.common.data.C1287d;
import com.google.android.gms.common.data.DataBuffer;

public final class AchievementBuffer extends DataBuffer<Achievement> {
    public AchievementBuffer(C1287d dataHolder) {
        super(dataHolder);
    }

    public Achievement get(int position) {
        return new C1299a(this.S, position);
    }
}
