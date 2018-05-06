package com.google.android.gms.games;

import android.database.CharArrayBuffer;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.internal.C0191r;
import com.google.android.gms.internal.C1351j;
import com.google.android.gms.internal.ao;
import com.google.android.gms.internal.av;

public final class GameEntity extends av implements Game {
    public static final Creator<GameEntity> CREATOR = new C1297a();
    private final int ab;
    private final String cl;
    private final String df;
    private final String dg;
    private final String dh;
    private final String di;
    private final String dj;
    private final Uri dk;
    private final Uri dl;
    private final Uri dm;
    private final boolean dn;
    private final boolean f163do;
    private final String dp;
    private final int dq;
    private final int dr;
    private final int ds;

    static final class C1297a extends C0151a {
        C1297a() {
        }

        public /* synthetic */ Object createFromParcel(Parcel x0) {
            return mo1043n(x0);
        }

        public GameEntity mo1043n(Parcel parcel) {
            if (av.m1217c(C1351j.m970v()) || C1351j.m968h(GameEntity.class.getCanonicalName())) {
                return super.mo1043n(parcel);
            }
            String readString = parcel.readString();
            String readString2 = parcel.readString();
            String readString3 = parcel.readString();
            String readString4 = parcel.readString();
            String readString5 = parcel.readString();
            String readString6 = parcel.readString();
            String readString7 = parcel.readString();
            Uri parse = readString7 == null ? null : Uri.parse(readString7);
            readString7 = parcel.readString();
            Uri parse2 = readString7 == null ? null : Uri.parse(readString7);
            readString7 = parcel.readString();
            return new GameEntity(1, readString, readString2, readString3, readString4, readString5, readString6, parse, parse2, readString7 == null ? null : Uri.parse(readString7), parcel.readInt() > 0, parcel.readInt() > 0, parcel.readString(), parcel.readInt(), parcel.readInt(), parcel.readInt());
        }
    }

    GameEntity(int versionCode, String applicationId, String displayName, String primaryCategory, String secondaryCategory, String description, String developerName, Uri iconImageUri, Uri hiResImageUri, Uri featuredImageUri, boolean playEnabledGame, boolean instanceInstalled, String instancePackageName, int gameplayAclStatus, int achievementTotalCount, int leaderboardCount) {
        this.ab = versionCode;
        this.df = applicationId;
        this.cl = displayName;
        this.dg = primaryCategory;
        this.dh = secondaryCategory;
        this.di = description;
        this.dj = developerName;
        this.dk = iconImageUri;
        this.dl = hiResImageUri;
        this.dm = featuredImageUri;
        this.dn = playEnabledGame;
        this.f163do = instanceInstalled;
        this.dp = instancePackageName;
        this.dq = gameplayAclStatus;
        this.dr = achievementTotalCount;
        this.ds = leaderboardCount;
    }

    public GameEntity(Game game) {
        this.ab = 1;
        this.df = game.getApplicationId();
        this.dg = game.getPrimaryCategory();
        this.dh = game.getSecondaryCategory();
        this.di = game.getDescription();
        this.dj = game.getDeveloperName();
        this.cl = game.getDisplayName();
        this.dk = game.getIconImageUri();
        this.dl = game.getHiResImageUri();
        this.dm = game.getFeaturedImageUri();
        this.dn = game.isPlayEnabledGame();
        this.f163do = game.isInstanceInstalled();
        this.dp = game.getInstancePackageName();
        this.dq = game.getGameplayAclStatus();
        this.dr = game.getAchievementTotalCount();
        this.ds = game.getLeaderboardCount();
    }

    static int m1358a(Game game) {
        return C0191r.hashCode(game.getApplicationId(), game.getDisplayName(), game.getPrimaryCategory(), game.getSecondaryCategory(), game.getDescription(), game.getDeveloperName(), game.getIconImageUri(), game.getHiResImageUri(), game.getFeaturedImageUri(), Boolean.valueOf(game.isPlayEnabledGame()), Boolean.valueOf(game.isInstanceInstalled()), game.getInstancePackageName(), Integer.valueOf(game.getGameplayAclStatus()), Integer.valueOf(game.getAchievementTotalCount()), Integer.valueOf(game.getLeaderboardCount()));
    }

    static boolean m1359a(Game game, Object obj) {
        if (!(obj instanceof Game)) {
            return false;
        }
        if (game == obj) {
            return true;
        }
        Game game2 = (Game) obj;
        return C0191r.m513a(game2.getApplicationId(), game.getApplicationId()) && C0191r.m513a(game2.getDisplayName(), game.getDisplayName()) && C0191r.m513a(game2.getPrimaryCategory(), game.getPrimaryCategory()) && C0191r.m513a(game2.getSecondaryCategory(), game.getSecondaryCategory()) && C0191r.m513a(game2.getDescription(), game.getDescription()) && C0191r.m513a(game2.getDeveloperName(), game.getDeveloperName()) && C0191r.m513a(game2.getIconImageUri(), game.getIconImageUri()) && C0191r.m513a(game2.getHiResImageUri(), game.getHiResImageUri()) && C0191r.m513a(game2.getFeaturedImageUri(), game.getFeaturedImageUri()) && C0191r.m513a(Boolean.valueOf(game2.isPlayEnabledGame()), Boolean.valueOf(game.isPlayEnabledGame())) && C0191r.m513a(Boolean.valueOf(game2.isInstanceInstalled()), Boolean.valueOf(game.isInstanceInstalled())) && C0191r.m513a(game2.getInstancePackageName(), game.getInstancePackageName()) && C0191r.m513a(Integer.valueOf(game2.getGameplayAclStatus()), Integer.valueOf(game.getGameplayAclStatus())) && C0191r.m513a(Integer.valueOf(game2.getAchievementTotalCount()), Integer.valueOf(game.getAchievementTotalCount())) && C0191r.m513a(Integer.valueOf(game2.getLeaderboardCount()), Integer.valueOf(game.getLeaderboardCount()));
    }

    static String m1360b(Game game) {
        return C0191r.m514c(game).m512a("ApplicationId", game.getApplicationId()).m512a("DisplayName", game.getDisplayName()).m512a("PrimaryCategory", game.getPrimaryCategory()).m512a("SecondaryCategory", game.getSecondaryCategory()).m512a("Description", game.getDescription()).m512a("DeveloperName", game.getDeveloperName()).m512a("IconImageUri", game.getIconImageUri()).m512a("HiResImageUri", game.getHiResImageUri()).m512a("FeaturedImageUri", game.getFeaturedImageUri()).m512a("PlayEnabledGame", Boolean.valueOf(game.isPlayEnabledGame())).m512a("InstanceInstalled", Boolean.valueOf(game.isInstanceInstalled())).m512a("InstancePackageName", game.getInstancePackageName()).m512a("GameplayAclStatus", Integer.valueOf(game.getGameplayAclStatus())).m512a("AchievementTotalCount", Integer.valueOf(game.getAchievementTotalCount())).m512a("LeaderboardCount", Integer.valueOf(game.getLeaderboardCount())).toString();
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object obj) {
        return m1359a(this, obj);
    }

    public Game freeze() {
        return this;
    }

    public int getAchievementTotalCount() {
        return this.dr;
    }

    public String getApplicationId() {
        return this.df;
    }

    public String getDescription() {
        return this.di;
    }

    public void getDescription(CharArrayBuffer dataOut) {
        ao.m215b(this.di, dataOut);
    }

    public String getDeveloperName() {
        return this.dj;
    }

    public void getDeveloperName(CharArrayBuffer dataOut) {
        ao.m215b(this.dj, dataOut);
    }

    public String getDisplayName() {
        return this.cl;
    }

    public void getDisplayName(CharArrayBuffer dataOut) {
        ao.m215b(this.cl, dataOut);
    }

    public Uri getFeaturedImageUri() {
        return this.dm;
    }

    public int getGameplayAclStatus() {
        return this.dq;
    }

    public Uri getHiResImageUri() {
        return this.dl;
    }

    public Uri getIconImageUri() {
        return this.dk;
    }

    public String getInstancePackageName() {
        return this.dp;
    }

    public int getLeaderboardCount() {
        return this.ds;
    }

    public String getPrimaryCategory() {
        return this.dg;
    }

    public String getSecondaryCategory() {
        return this.dh;
    }

    public int hashCode() {
        return m1358a(this);
    }

    public int m1363i() {
        return this.ab;
    }

    public boolean isDataValid() {
        return true;
    }

    public boolean isInstanceInstalled() {
        return this.f163do;
    }

    public boolean isPlayEnabledGame() {
        return this.dn;
    }

    public String toString() {
        return m1360b((Game) this);
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i = 1;
        String str = null;
        if (m971w()) {
            dest.writeString(this.df);
            dest.writeString(this.cl);
            dest.writeString(this.dg);
            dest.writeString(this.dh);
            dest.writeString(this.di);
            dest.writeString(this.dj);
            dest.writeString(this.dk == null ? null : this.dk.toString());
            dest.writeString(this.dl == null ? null : this.dl.toString());
            if (this.dm != null) {
                str = this.dm.toString();
            }
            dest.writeString(str);
            dest.writeInt(this.dn ? 1 : 0);
            if (!this.f163do) {
                i = 0;
            }
            dest.writeInt(i);
            dest.writeString(this.dp);
            dest.writeInt(this.dq);
            dest.writeInt(this.dr);
            dest.writeInt(this.ds);
            return;
        }
        C0151a.m144a(this, dest, flags);
    }
}
