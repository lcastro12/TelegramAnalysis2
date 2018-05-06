package com.google.android.gms.games;

import android.database.CharArrayBuffer;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.internal.C0176h;
import com.google.android.gms.internal.C0191r;
import com.google.android.gms.internal.C1351j;
import com.google.android.gms.internal.ao;
import com.google.android.gms.internal.av;

public final class PlayerEntity extends av implements Player {
    public static final Creator<PlayerEntity> CREATOR = new C1298a();
    private final int ab;
    private final String cl;
    private final Uri dk;
    private final Uri dl;
    private final String dx;
    private final long dy;

    static final class C1298a extends C0152c {
        C1298a() {
        }

        public /* synthetic */ Object createFromParcel(Parcel x0) {
            return mo1045o(x0);
        }

        public PlayerEntity mo1045o(Parcel parcel) {
            Uri uri = null;
            if (av.m1217c(C1351j.m970v()) || C1351j.m968h(PlayerEntity.class.getCanonicalName())) {
                return super.mo1045o(parcel);
            }
            String readString = parcel.readString();
            String readString2 = parcel.readString();
            String readString3 = parcel.readString();
            String readString4 = parcel.readString();
            Uri parse = readString3 == null ? null : Uri.parse(readString3);
            if (readString4 != null) {
                uri = Uri.parse(readString4);
            }
            return new PlayerEntity(1, readString, readString2, parse, uri, parcel.readLong());
        }
    }

    PlayerEntity(int versionCode, String playerId, String displayName, Uri iconImageUri, Uri hiResImageUri, long retrievedTimestamp) {
        this.ab = versionCode;
        this.dx = playerId;
        this.cl = displayName;
        this.dk = iconImageUri;
        this.dl = hiResImageUri;
        this.dy = retrievedTimestamp;
    }

    public PlayerEntity(Player player) {
        boolean z = true;
        this.ab = 1;
        this.dx = player.getPlayerId();
        this.cl = player.getDisplayName();
        this.dk = player.getIconImageUri();
        this.dl = player.getHiResImageUri();
        this.dy = player.getRetrievedTimestamp();
        C0176h.m465b(this.dx);
        C0176h.m465b(this.cl);
        if (this.dy <= 0) {
            z = false;
        }
        C0176h.m463a(z);
    }

    static int m1364a(Player player) {
        return C0191r.hashCode(player.getPlayerId(), player.getDisplayName(), player.getIconImageUri(), player.getHiResImageUri(), Long.valueOf(player.getRetrievedTimestamp()));
    }

    static boolean m1365a(Player player, Object obj) {
        if (!(obj instanceof Player)) {
            return false;
        }
        if (player == obj) {
            return true;
        }
        Player player2 = (Player) obj;
        return C0191r.m513a(player2.getPlayerId(), player.getPlayerId()) && C0191r.m513a(player2.getDisplayName(), player.getDisplayName()) && C0191r.m513a(player2.getIconImageUri(), player.getIconImageUri()) && C0191r.m513a(player2.getHiResImageUri(), player.getHiResImageUri()) && C0191r.m513a(Long.valueOf(player2.getRetrievedTimestamp()), Long.valueOf(player.getRetrievedTimestamp()));
    }

    static String m1366b(Player player) {
        return C0191r.m514c(player).m512a("PlayerId", player.getPlayerId()).m512a("DisplayName", player.getDisplayName()).m512a("IconImageUri", player.getIconImageUri()).m512a("HiResImageUri", player.getHiResImageUri()).m512a("RetrievedTimestamp", Long.valueOf(player.getRetrievedTimestamp())).toString();
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object obj) {
        return m1365a(this, obj);
    }

    public Player freeze() {
        return this;
    }

    public String getDisplayName() {
        return this.cl;
    }

    public void getDisplayName(CharArrayBuffer dataOut) {
        ao.m215b(this.cl, dataOut);
    }

    public Uri getHiResImageUri() {
        return this.dl;
    }

    public Uri getIconImageUri() {
        return this.dk;
    }

    public String getPlayerId() {
        return this.dx;
    }

    public long getRetrievedTimestamp() {
        return this.dy;
    }

    public boolean hasHiResImage() {
        return getHiResImageUri() != null;
    }

    public boolean hasIconImage() {
        return getIconImageUri() != null;
    }

    public int hashCode() {
        return m1364a(this);
    }

    public int m1369i() {
        return this.ab;
    }

    public boolean isDataValid() {
        return true;
    }

    public String toString() {
        return m1366b((Player) this);
    }

    public void writeToParcel(Parcel dest, int flags) {
        String str = null;
        if (m971w()) {
            dest.writeString(this.dx);
            dest.writeString(this.cl);
            dest.writeString(this.dk == null ? null : this.dk.toString());
            if (this.dl != null) {
                str = this.dl.toString();
            }
            dest.writeString(str);
            dest.writeLong(this.dy);
            return;
        }
        C0152c.m147a(this, dest, flags);
    }
}
