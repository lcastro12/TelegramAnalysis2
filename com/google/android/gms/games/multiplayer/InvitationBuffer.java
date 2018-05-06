package com.google.android.gms.games.multiplayer;

import com.google.android.gms.common.data.C1287d;
import com.google.android.gms.common.data.C1288f;

public final class InvitationBuffer extends C1288f<Invitation> {
    public InvitationBuffer(C1287d dataHolder) {
        super(dataHolder);
    }

    protected /* synthetic */ Object mo2301a(int i, int i2) {
        return getEntry(i, i2);
    }

    protected Invitation getEntry(int rowIndex, int numChildren) {
        return new C1694b(this.S, rowIndex, numChildren);
    }

    protected String getPrimaryDataMarkerColumn() {
        return "external_invitation_id";
    }
}
