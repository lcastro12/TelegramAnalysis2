package com.google.android.gms.games.leaderboard;

import com.google.android.gms.common.data.C1287d;
import com.google.android.gms.internal.C0191r;
import com.google.android.gms.internal.C0191r.C0190a;
import com.google.android.gms.internal.C0192s;
import com.google.android.gms.internal.bd;
import java.util.HashMap;

public final class SubmitScoreResult {
    private static final String[] eB = new String[]{"leaderboardId", "playerId", "timeSpan", "hasResult", "rawScore", "formattedScore", "newBest"};
    private String dx;
    private String eC;
    private HashMap<Integer, Result> eD;
    private int f39p;

    public static final class Result {
        public final String formattedScore;
        public final boolean newBest;
        public final long rawScore;

        public Result(long rawScore, String formattedScore, boolean newBest) {
            this.rawScore = rawScore;
            this.formattedScore = formattedScore;
            this.newBest = newBest;
        }

        public String toString() {
            return C0191r.m514c(this).m512a("RawScore", Long.valueOf(this.rawScore)).m512a("FormattedScore", this.formattedScore).m512a("NewBest", Boolean.valueOf(this.newBest)).toString();
        }
    }

    public SubmitScoreResult(int statusCode, String leaderboardId, String playerId) {
        this(statusCode, leaderboardId, playerId, new HashMap());
    }

    public SubmitScoreResult(int statusCode, String leaderboardId, String playerId, HashMap<Integer, Result> results) {
        this.f39p = statusCode;
        this.eC = leaderboardId;
        this.dx = playerId;
        this.eD = results;
    }

    public SubmitScoreResult(C1287d dataHolder) {
        int i = 0;
        this.f39p = dataHolder.getStatusCode();
        this.eD = new HashMap();
        int count = dataHolder.getCount();
        C0192s.m520c(count == 3);
        while (i < count) {
            int e = dataHolder.m631e(i);
            if (i == 0) {
                this.eC = dataHolder.m629c("leaderboardId", i, e);
                this.dx = dataHolder.m629c("playerId", i, e);
            }
            if (dataHolder.m630d("hasResult", i, e)) {
                m150a(new Result(dataHolder.m626a("rawScore", i, e), dataHolder.m629c("formattedScore", i, e), dataHolder.m630d("newBest", i, e)), dataHolder.m628b("timeSpan", i, e));
            }
            i++;
        }
    }

    private void m150a(Result result, int i) {
        this.eD.put(Integer.valueOf(i), result);
    }

    public String getLeaderboardId() {
        return this.eC;
    }

    public String getPlayerId() {
        return this.dx;
    }

    public Result getScoreResult(int timeSpan) {
        return (Result) this.eD.get(Integer.valueOf(timeSpan));
    }

    public int getStatusCode() {
        return this.f39p;
    }

    public String toString() {
        C0190a a = C0191r.m514c(this).m512a("PlayerId", this.dx).m512a("StatusCode", Integer.valueOf(this.f39p));
        for (int i = 0; i < 3; i++) {
            Result result = (Result) this.eD.get(Integer.valueOf(i));
            a.m512a("TimesSpan", bd.m338G(i));
            a.m512a("Result", result == null ? "null" : result.toString());
        }
        return a.toString();
    }
}
