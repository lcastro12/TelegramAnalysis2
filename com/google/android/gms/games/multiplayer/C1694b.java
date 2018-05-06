package com.google.android.gms.games.multiplayer;

import android.os.Parcel;
import com.google.android.gms.common.data.C0130b;
import com.google.android.gms.common.data.C1287d;
import com.google.android.gms.games.C1690b;
import com.google.android.gms.games.Game;
import com.google.android.gms.internal.C0192s;
import java.util.ArrayList;

public final class C1694b extends C0130b implements Invitation {
    private final ArrayList<Participant> eJ;
    private final Game eL;
    private final C1695d eM;

    C1694b(C1287d c1287d, int i, int i2) {
        super(c1287d, i);
        this.eL = new C1690b(c1287d, i);
        this.eJ = new ArrayList(i2);
        String string = getString("external_inviter_id");
        Object obj = null;
        for (int i3 = 0; i3 < i2; i3++) {
            C1695d c1695d = new C1695d(this.S, this.V + i3);
            if (c1695d.getParticipantId().equals(string)) {
                obj = c1695d;
            }
            this.eJ.add(c1695d);
        }
        this.eM = (C1695d) C0192s.m518b(obj, (Object) "Must have a valid inviter!");
    }

    public int aL() {
        return getInteger("type");
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object obj) {
        return InvitationEntity.m1371a(this, obj);
    }

    public Invitation freeze() {
        return new InvitationEntity(this);
    }

    public long getCreationTimestamp() {
        return getLong("creation_timestamp");
    }

    public Game getGame() {
        return this.eL;
    }

    public String getInvitationId() {
        return getString("external_invitation_id");
    }

    public Participant getInviter() {
        return this.eM;
    }

    public ArrayList<Participant> getParticipants() {
        return this.eJ;
    }

    public int getVariant() {
        return getInteger("variant");
    }

    public int hashCode() {
        return InvitationEntity.m1370a(this);
    }

    public String toString() {
        return InvitationEntity.m1372b((Invitation) this);
    }

    public void writeToParcel(Parcel dest, int flags) {
        ((InvitationEntity) freeze()).writeToParcel(dest, flags);
    }
}
