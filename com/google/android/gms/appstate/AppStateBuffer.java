package com.google.android.gms.appstate;

import com.google.android.gms.common.data.C1287d;
import com.google.android.gms.common.data.DataBuffer;

public final class AppStateBuffer extends DataBuffer<AppState> {
    public AppStateBuffer(C1287d dataHolder) {
        super(dataHolder);
    }

    public AppState get(int position) {
        return new C1688b(this.S, position);
    }
}
