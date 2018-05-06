package com.google.android.gms.games.leaderboard;

import android.database.CharArrayBuffer;
import android.net.Uri;
import com.google.android.gms.common.data.C0130b;
import com.google.android.gms.common.data.C1287d;
import com.google.android.gms.games.C1691d;
import com.google.android.gms.games.Player;

public final class C1693d extends C0130b implements LeaderboardScore {
    private final C1691d eA;

    C1693d(C1287d c1287d, int i) {
        super(c1287d, i);
        this.eA = new C1691d(c1287d, i);
    }

    public LeaderboardScore aH() {
        return new C1692c(this);
    }

    public boolean equals(Object obj) {
        return C1692c.m1138a(this, obj);
    }

    public /* synthetic */ Object freeze() {
        return aH();
    }

    public String getDisplayRank() {
        return getString("display_rank");
    }

    public void getDisplayRank(CharArrayBuffer dataOut) {
        m34a("display_rank", dataOut);
    }

    public String getDisplayScore() {
        return getString("display_score");
    }

    public void getDisplayScore(CharArrayBuffer dataOut) {
        m34a("display_score", dataOut);
    }

    public long getRank() {
        return getLong("rank");
    }

    public long getRawScore() {
        return getLong("raw_score");
    }

    public Player getScoreHolder() {
        return m36e("external_player_id") ? null : this.eA;
    }

    public String getScoreHolderDisplayName() {
        return m36e("external_player_id") ? getString("default_display_name") : this.eA.getDisplayName();
    }

    public void getScoreHolderDisplayName(CharArrayBuffer dataOut) {
        if (m36e("external_player_id")) {
            m34a("default_display_name", dataOut);
        } else {
            this.eA.getDisplayName(dataOut);
        }
    }

    public Uri getScoreHolderHiResImageUri() {
        return m36e("external_player_id") ? null : this.eA.getHiResImageUri();
    }

    public Uri getScoreHolderIconImageUri() {
        return m36e("external_player_id") ? m35d("default_display_image_uri") : this.eA.getIconImageUri();
    }

    public long getTimestampMillis() {
        return getLong("achieved_timestamp");
    }

    public int hashCode() {
        return C1692c.m1137a(this);
    }

    public String toString() {
        return C1692c.m1139b(this);
    }
}
