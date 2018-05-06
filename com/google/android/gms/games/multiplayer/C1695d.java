package com.google.android.gms.games.multiplayer;

import android.database.CharArrayBuffer;
import android.net.Uri;
import android.os.Parcel;
import com.google.android.gms.common.data.C0130b;
import com.google.android.gms.common.data.C1287d;
import com.google.android.gms.games.C1691d;
import com.google.android.gms.games.Player;

public final class C1695d extends C0130b implements Participant {
    private final C1691d eS;

    public C1695d(C1287d c1287d, int i) {
        super(c1287d, i);
        this.eS = new C1691d(c1287d, i);
    }

    public String aM() {
        return getString("client_address");
    }

    public int aN() {
        return getInteger("capabilities");
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object obj) {
        return ParticipantEntity.m1377a(this, obj);
    }

    public Participant freeze() {
        return new ParticipantEntity(this);
    }

    public String getDisplayName() {
        return m36e("external_player_id") ? getString("default_display_name") : this.eS.getDisplayName();
    }

    public void getDisplayName(CharArrayBuffer dataOut) {
        if (m36e("external_player_id")) {
            m34a("default_display_name", dataOut);
        } else {
            this.eS.getDisplayName(dataOut);
        }
    }

    public Uri getHiResImageUri() {
        return m36e("external_player_id") ? null : this.eS.getHiResImageUri();
    }

    public Uri getIconImageUri() {
        return m36e("external_player_id") ? m35d("default_display_image_uri") : this.eS.getIconImageUri();
    }

    public String getParticipantId() {
        return getString("external_participant_id");
    }

    public Player getPlayer() {
        return m36e("external_player_id") ? null : this.eS;
    }

    public int getStatus() {
        return getInteger("player_status");
    }

    public int hashCode() {
        return ParticipantEntity.m1376a(this);
    }

    public boolean isConnectedToRoom() {
        return getInteger("connected") > 0;
    }

    public String toString() {
        return ParticipantEntity.m1378b((Participant) this);
    }

    public void writeToParcel(Parcel dest, int flags) {
        ((ParticipantEntity) freeze()).writeToParcel(dest, flags);
    }
}
