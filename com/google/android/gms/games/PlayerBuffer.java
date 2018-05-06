package com.google.android.gms.games;

import com.google.android.gms.common.data.C1287d;
import com.google.android.gms.common.data.DataBuffer;

public final class PlayerBuffer extends DataBuffer<Player> {
    public PlayerBuffer(C1287d dataHolder) {
        super(dataHolder);
    }

    public Player get(int position) {
        return new C1691d(this.S, position);
    }
}
