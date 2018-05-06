package com.google.android.gms.plus.model.moments;

import com.google.android.gms.common.data.C1287d;
import com.google.android.gms.common.data.DataBuffer;
import com.google.android.gms.internal.cb;

public final class MomentBuffer extends DataBuffer<Moment> {
    public MomentBuffer(C1287d dataHolder) {
        super(dataHolder);
    }

    public Moment get(int position) {
        return new cb(this.S, position);
    }
}
