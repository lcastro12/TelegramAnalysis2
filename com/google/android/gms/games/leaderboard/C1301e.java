package com.google.android.gms.games.leaderboard;

import com.google.android.gms.common.data.C0130b;
import com.google.android.gms.common.data.C1287d;
import com.google.android.gms.internal.C0191r;
import com.google.android.gms.internal.bc;
import com.google.android.gms.internal.bd;

public final class C1301e extends C0130b implements LeaderboardVariant {
    C1301e(C1287d c1287d, int i) {
        super(c1287d, i);
    }

    public String aI() {
        return getString("top_page_token_next");
    }

    public String aJ() {
        return getString("window_page_token_prev");
    }

    public String aK() {
        return getString("window_page_token_next");
    }

    public int getCollection() {
        return getInteger("collection");
    }

    public String getDisplayPlayerRank() {
        return getString("player_display_rank");
    }

    public String getDisplayPlayerScore() {
        return getString("player_display_score");
    }

    public long getNumScores() {
        return m36e("total_scores") ? -1 : getLong("total_scores");
    }

    public long getPlayerRank() {
        return m36e("player_rank") ? -1 : getLong("player_rank");
    }

    public long getRawPlayerScore() {
        return m36e("player_raw_score") ? -1 : getLong("player_raw_score");
    }

    public int getTimeSpan() {
        return getInteger("timespan");
    }

    public boolean hasPlayerInfo() {
        return !m36e("player_raw_score");
    }

    public String toString() {
        return C0191r.m514c(this).m512a("TimeSpan", bd.m338G(getTimeSpan())).m512a("Collection", bc.m337G(getCollection())).m512a("RawPlayerScore", hasPlayerInfo() ? Long.valueOf(getRawPlayerScore()) : "none").m512a("DisplayPlayerScore", hasPlayerInfo() ? getDisplayPlayerScore() : "none").m512a("PlayerRank", hasPlayerInfo() ? Long.valueOf(getPlayerRank()) : "none").m512a("DisplayPlayerRank", hasPlayerInfo() ? getDisplayPlayerRank() : "none").m512a("NumScores", Long.valueOf(getNumScores())).m512a("TopPageNextToken", aI()).m512a("WindowPageNextToken", aK()).m512a("WindowPagePrevToken", aJ()).toString();
    }
}
