package com.google.android.gms.games.leaderboard;

import android.database.CharArrayBuffer;
import android.net.Uri;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.PlayerEntity;
import com.google.android.gms.internal.C0191r;
import com.google.android.gms.internal.C0192s;
import com.google.android.gms.internal.ao;

public final class C1692c implements LeaderboardScore {
    private final long er;
    private final String es;
    private final String et;
    private final long eu;
    private final long ev;
    private final String ew;
    private final Uri ex;
    private final Uri ey;
    private final PlayerEntity ez;

    public C1692c(LeaderboardScore leaderboardScore) {
        this.er = leaderboardScore.getRank();
        this.es = (String) C0192s.m521d(leaderboardScore.getDisplayRank());
        this.et = (String) C0192s.m521d(leaderboardScore.getDisplayScore());
        this.eu = leaderboardScore.getRawScore();
        this.ev = leaderboardScore.getTimestampMillis();
        this.ew = leaderboardScore.getScoreHolderDisplayName();
        this.ex = leaderboardScore.getScoreHolderIconImageUri();
        this.ey = leaderboardScore.getScoreHolderHiResImageUri();
        Player scoreHolder = leaderboardScore.getScoreHolder();
        this.ez = scoreHolder == null ? null : (PlayerEntity) scoreHolder.freeze();
    }

    static int m1137a(LeaderboardScore leaderboardScore) {
        return C0191r.hashCode(Long.valueOf(leaderboardScore.getRank()), leaderboardScore.getDisplayRank(), Long.valueOf(leaderboardScore.getRawScore()), leaderboardScore.getDisplayScore(), Long.valueOf(leaderboardScore.getTimestampMillis()), leaderboardScore.getScoreHolderDisplayName(), leaderboardScore.getScoreHolderIconImageUri(), leaderboardScore.getScoreHolderHiResImageUri(), leaderboardScore.getScoreHolder());
    }

    static boolean m1138a(LeaderboardScore leaderboardScore, Object obj) {
        if (!(obj instanceof LeaderboardScore)) {
            return false;
        }
        if (leaderboardScore == obj) {
            return true;
        }
        LeaderboardScore leaderboardScore2 = (LeaderboardScore) obj;
        return C0191r.m513a(Long.valueOf(leaderboardScore2.getRank()), Long.valueOf(leaderboardScore.getRank())) && C0191r.m513a(leaderboardScore2.getDisplayRank(), leaderboardScore.getDisplayRank()) && C0191r.m513a(Long.valueOf(leaderboardScore2.getRawScore()), Long.valueOf(leaderboardScore.getRawScore())) && C0191r.m513a(leaderboardScore2.getDisplayScore(), leaderboardScore.getDisplayScore()) && C0191r.m513a(Long.valueOf(leaderboardScore2.getTimestampMillis()), Long.valueOf(leaderboardScore.getTimestampMillis())) && C0191r.m513a(leaderboardScore2.getScoreHolderDisplayName(), leaderboardScore.getScoreHolderDisplayName()) && C0191r.m513a(leaderboardScore2.getScoreHolderIconImageUri(), leaderboardScore.getScoreHolderIconImageUri()) && C0191r.m513a(leaderboardScore2.getScoreHolderHiResImageUri(), leaderboardScore.getScoreHolderHiResImageUri()) && C0191r.m513a(leaderboardScore2.getScoreHolder(), leaderboardScore.getScoreHolder());
    }

    static String m1139b(LeaderboardScore leaderboardScore) {
        return C0191r.m514c(leaderboardScore).m512a("Rank", Long.valueOf(leaderboardScore.getRank())).m512a("DisplayRank", leaderboardScore.getDisplayRank()).m512a("Score", Long.valueOf(leaderboardScore.getRawScore())).m512a("DisplayScore", leaderboardScore.getDisplayScore()).m512a("Timestamp", Long.valueOf(leaderboardScore.getTimestampMillis())).m512a("DisplayName", leaderboardScore.getScoreHolderDisplayName()).m512a("IconImageUri", leaderboardScore.getScoreHolderIconImageUri()).m512a("HiResImageUri", leaderboardScore.getScoreHolderHiResImageUri()).m512a("Player", leaderboardScore.getScoreHolder() == null ? null : leaderboardScore.getScoreHolder()).toString();
    }

    public LeaderboardScore aH() {
        return this;
    }

    public boolean equals(Object obj) {
        return C1692c.m1138a(this, obj);
    }

    public /* synthetic */ Object freeze() {
        return aH();
    }

    public String getDisplayRank() {
        return this.es;
    }

    public void getDisplayRank(CharArrayBuffer dataOut) {
        ao.m215b(this.es, dataOut);
    }

    public String getDisplayScore() {
        return this.et;
    }

    public void getDisplayScore(CharArrayBuffer dataOut) {
        ao.m215b(this.et, dataOut);
    }

    public long getRank() {
        return this.er;
    }

    public long getRawScore() {
        return this.eu;
    }

    public Player getScoreHolder() {
        return this.ez;
    }

    public String getScoreHolderDisplayName() {
        return this.ez == null ? this.ew : this.ez.getDisplayName();
    }

    public void getScoreHolderDisplayName(CharArrayBuffer dataOut) {
        if (this.ez == null) {
            ao.m215b(this.ew, dataOut);
        } else {
            this.ez.getDisplayName(dataOut);
        }
    }

    public Uri getScoreHolderHiResImageUri() {
        return this.ez == null ? this.ey : this.ez.getHiResImageUri();
    }

    public Uri getScoreHolderIconImageUri() {
        return this.ez == null ? this.ex : this.ez.getIconImageUri();
    }

    public long getTimestampMillis() {
        return this.ev;
    }

    public int hashCode() {
        return C1692c.m1137a(this);
    }

    public boolean isDataValid() {
        return true;
    }

    public String toString() {
        return C1692c.m1139b(this);
    }
}
