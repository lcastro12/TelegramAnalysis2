package com.google.android.gms.games.leaderboard;

import android.database.CharArrayBuffer;
import android.net.Uri;
import com.google.android.gms.common.data.C0130b;
import com.google.android.gms.common.data.C1287d;
import com.google.android.gms.internal.C0191r;
import java.util.ArrayList;

public final class C1300a extends C0130b implements Leaderboard {
    private final int eo;

    C1300a(C1287d c1287d, int i, int i2) {
        super(c1287d, i);
        this.eo = i2;
    }

    public String getDisplayName() {
        return getString("name");
    }

    public void getDisplayName(CharArrayBuffer dataOut) {
        m34a("name", dataOut);
    }

    public Uri getIconImageUri() {
        return m35d("board_icon_image_uri");
    }

    public String getLeaderboardId() {
        return getString("external_leaderboard_id");
    }

    public int getScoreOrder() {
        return getInteger("score_order");
    }

    public ArrayList<LeaderboardVariant> getVariants() {
        ArrayList<LeaderboardVariant> arrayList = new ArrayList(this.eo);
        for (int i = 0; i < this.eo; i++) {
            arrayList.add(new C1301e(this.S, this.V + i));
        }
        return arrayList;
    }

    public String toString() {
        return C0191r.m514c(this).m512a("ID", getLeaderboardId()).m512a("DisplayName", getDisplayName()).m512a("IconImageURI", getIconImageUri()).m512a("ScoreOrder", Integer.valueOf(getScoreOrder())).toString();
    }
}
