package com.google.android.gms.games;

import com.google.android.gms.common.data.C1287d;
import com.google.android.gms.common.data.DataBuffer;

public final class GameBuffer extends DataBuffer<Game> {
    public GameBuffer(C1287d dataHolder) {
        super(dataHolder);
    }

    public Game get(int position) {
        return new C1690b(this.S, position);
    }
}
