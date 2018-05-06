package com.google.android.gms.games.achievement;

import android.database.CharArrayBuffer;
import android.net.Uri;
import com.google.android.gms.common.data.C0130b;
import com.google.android.gms.common.data.C1287d;
import com.google.android.gms.games.C1691d;
import com.google.android.gms.games.Player;
import com.google.android.gms.internal.C0176h;
import com.google.android.gms.internal.C0191r;
import com.google.android.gms.internal.C0191r.C0190a;
import com.google.android.gms.plus.PlusShare;

public final class C1299a extends C0130b implements Achievement {
    C1299a(C1287d c1287d, int i) {
        super(c1287d, i);
    }

    public String getAchievementId() {
        return getString("external_achievement_id");
    }

    public int getCurrentSteps() {
        boolean z = true;
        if (getType() != 1) {
            z = false;
        }
        C0176h.m463a(z);
        return getInteger("current_steps");
    }

    public String getDescription() {
        return getString(PlusShare.KEY_CONTENT_DEEP_LINK_METADATA_DESCRIPTION);
    }

    public void getDescription(CharArrayBuffer dataOut) {
        m34a(PlusShare.KEY_CONTENT_DEEP_LINK_METADATA_DESCRIPTION, dataOut);
    }

    public String getFormattedCurrentSteps() {
        boolean z = true;
        if (getType() != 1) {
            z = false;
        }
        C0176h.m463a(z);
        return getString("formatted_current_steps");
    }

    public void getFormattedCurrentSteps(CharArrayBuffer dataOut) {
        boolean z = true;
        if (getType() != 1) {
            z = false;
        }
        C0176h.m463a(z);
        m34a("formatted_current_steps", dataOut);
    }

    public String getFormattedTotalSteps() {
        boolean z = true;
        if (getType() != 1) {
            z = false;
        }
        C0176h.m463a(z);
        return getString("formatted_total_steps");
    }

    public void getFormattedTotalSteps(CharArrayBuffer dataOut) {
        boolean z = true;
        if (getType() != 1) {
            z = false;
        }
        C0176h.m463a(z);
        m34a("formatted_total_steps", dataOut);
    }

    public long getLastUpdatedTimestamp() {
        return getLong("last_updated_timestamp");
    }

    public String getName() {
        return getString("name");
    }

    public void getName(CharArrayBuffer dataOut) {
        m34a("name", dataOut);
    }

    public Player getPlayer() {
        return new C1691d(this.S, this.V);
    }

    public Uri getRevealedImageUri() {
        return m35d("revealed_icon_image_uri");
    }

    public int getState() {
        return getInteger("state");
    }

    public int getTotalSteps() {
        boolean z = true;
        if (getType() != 1) {
            z = false;
        }
        C0176h.m463a(z);
        return getInteger("total_steps");
    }

    public int getType() {
        return getInteger("type");
    }

    public Uri getUnlockedImageUri() {
        return m35d("unlocked_icon_image_uri");
    }

    public String toString() {
        C0190a a = C0191r.m514c(this).m512a("id", getAchievementId()).m512a("name", getName()).m512a("state", Integer.valueOf(getState())).m512a("type", Integer.valueOf(getType()));
        if (getType() == 1) {
            a.m512a("steps", getCurrentSteps() + "/" + getTotalSteps());
        }
        return a.toString();
    }
}
