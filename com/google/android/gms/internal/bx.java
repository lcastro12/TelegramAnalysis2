package com.google.android.gms.internal;

import android.os.Parcel;
import android.support.v4.view.MotionEventCompat;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.internal.ae.C1308a;
import com.google.android.gms.plus.PlusShare;
import com.google.android.gms.plus.model.moments.ItemScope;
import com.googlecode.mp4parser.authoring.tracks.h265.NalUnitTypes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class bx extends ae implements SafeParcelable, ItemScope {
    public static final by CREATOR = new by();
    private static final HashMap<String, C1308a<?, ?>> iC = new HashMap();
    private final int ab;
    private String di;
    private double fy;
    private double fz;
    private final Set<Integer> iD;
    private bx iE;
    private List<String> iF;
    private bx iG;
    private String iH;
    private String iI;
    private String iJ;
    private List<bx> iK;
    private int iL;
    private List<bx> iM;
    private bx iN;
    private List<bx> iO;
    private String iP;
    private String iQ;
    private bx iR;
    private String iS;
    private String iT;
    private String iU;
    private List<bx> iV;
    private String iW;
    private String iX;
    private String iY;
    private String iZ;
    private String ie;
    private String jA;
    private String ja;
    private String jb;
    private String jc;
    private String jd;
    private bx je;
    private String jf;
    private String jg;
    private String jh;
    private String ji;
    private bx jj;
    private bx jk;
    private bx jl;
    private List<bx> jm;
    private String jn;
    private String jo;
    private String jp;
    private String jq;
    private bx jr;
    private String js;
    private String jt;
    private String ju;
    private bx jv;
    private String jw;
    private String jx;
    private String jy;
    private String jz;
    private String mName;

    static {
        iC.put("about", C1308a.m669a("about", 2, bx.class));
        iC.put("additionalName", C1308a.m676g("additionalName", 3));
        iC.put("address", C1308a.m669a("address", 4, bx.class));
        iC.put("addressCountry", C1308a.m675f("addressCountry", 5));
        iC.put("addressLocality", C1308a.m675f("addressLocality", 6));
        iC.put("addressRegion", C1308a.m675f("addressRegion", 7));
        iC.put("associated_media", C1308a.m670b("associated_media", 8, bx.class));
        iC.put("attendeeCount", C1308a.m671c("attendeeCount", 9));
        iC.put("attendees", C1308a.m670b("attendees", 10, bx.class));
        iC.put("audio", C1308a.m669a("audio", 11, bx.class));
        iC.put("author", C1308a.m670b("author", 12, bx.class));
        iC.put("bestRating", C1308a.m675f("bestRating", 13));
        iC.put("birthDate", C1308a.m675f("birthDate", 14));
        iC.put("byArtist", C1308a.m669a("byArtist", 15, bx.class));
        iC.put("caption", C1308a.m675f("caption", 16));
        iC.put("contentSize", C1308a.m675f("contentSize", 17));
        iC.put("contentUrl", C1308a.m675f("contentUrl", 18));
        iC.put("contributor", C1308a.m670b("contributor", 19, bx.class));
        iC.put("dateCreated", C1308a.m675f("dateCreated", 20));
        iC.put("dateModified", C1308a.m675f("dateModified", 21));
        iC.put("datePublished", C1308a.m675f("datePublished", 22));
        iC.put(PlusShare.KEY_CONTENT_DEEP_LINK_METADATA_DESCRIPTION, C1308a.m675f(PlusShare.KEY_CONTENT_DEEP_LINK_METADATA_DESCRIPTION, 23));
        iC.put("duration", C1308a.m675f("duration", 24));
        iC.put("embedUrl", C1308a.m675f("embedUrl", 25));
        iC.put("endDate", C1308a.m675f("endDate", 26));
        iC.put("familyName", C1308a.m675f("familyName", 27));
        iC.put("gender", C1308a.m675f("gender", 28));
        iC.put("geo", C1308a.m669a("geo", 29, bx.class));
        iC.put("givenName", C1308a.m675f("givenName", 30));
        iC.put("height", C1308a.m675f("height", 31));
        iC.put("id", C1308a.m675f("id", 32));
        iC.put("image", C1308a.m675f("image", 33));
        iC.put("inAlbum", C1308a.m669a("inAlbum", 34, bx.class));
        iC.put("latitude", C1308a.m673d("latitude", 36));
        iC.put("location", C1308a.m669a("location", 37, bx.class));
        iC.put("longitude", C1308a.m673d("longitude", 38));
        iC.put("name", C1308a.m675f("name", 39));
        iC.put("partOfTVSeries", C1308a.m669a("partOfTVSeries", 40, bx.class));
        iC.put("performers", C1308a.m670b("performers", 41, bx.class));
        iC.put("playerType", C1308a.m675f("playerType", 42));
        iC.put("postOfficeBoxNumber", C1308a.m675f("postOfficeBoxNumber", 43));
        iC.put("postalCode", C1308a.m675f("postalCode", 44));
        iC.put("ratingValue", C1308a.m675f("ratingValue", 45));
        iC.put("reviewRating", C1308a.m669a("reviewRating", 46, bx.class));
        iC.put("startDate", C1308a.m675f("startDate", 47));
        iC.put("streetAddress", C1308a.m675f("streetAddress", 48));
        iC.put("text", C1308a.m675f("text", 49));
        iC.put("thumbnail", C1308a.m669a("thumbnail", 50, bx.class));
        iC.put(PlusShare.KEY_CONTENT_DEEP_LINK_METADATA_THUMBNAIL_URL, C1308a.m675f(PlusShare.KEY_CONTENT_DEEP_LINK_METADATA_THUMBNAIL_URL, 51));
        iC.put("tickerSymbol", C1308a.m675f("tickerSymbol", 52));
        iC.put("type", C1308a.m675f("type", 53));
        iC.put(PlusShare.KEY_CALL_TO_ACTION_URL, C1308a.m675f(PlusShare.KEY_CALL_TO_ACTION_URL, 54));
        iC.put("width", C1308a.m675f("width", 55));
        iC.put("worstRating", C1308a.m675f("worstRating", 56));
    }

    public bx() {
        this.ab = 1;
        this.iD = new HashSet();
    }

    bx(Set<Integer> set, int i, bx bxVar, List<String> list, bx bxVar2, String str, String str2, String str3, List<bx> list2, int i2, List<bx> list3, bx bxVar3, List<bx> list4, String str4, String str5, bx bxVar4, String str6, String str7, String str8, List<bx> list5, String str9, String str10, String str11, String str12, String str13, String str14, String str15, String str16, String str17, bx bxVar5, String str18, String str19, String str20, String str21, bx bxVar6, double d, bx bxVar7, double d2, String str22, bx bxVar8, List<bx> list6, String str23, String str24, String str25, String str26, bx bxVar9, String str27, String str28, String str29, bx bxVar10, String str30, String str31, String str32, String str33, String str34, String str35) {
        this.iD = set;
        this.ab = i;
        this.iE = bxVar;
        this.iF = list;
        this.iG = bxVar2;
        this.iH = str;
        this.iI = str2;
        this.iJ = str3;
        this.iK = list2;
        this.iL = i2;
        this.iM = list3;
        this.iN = bxVar3;
        this.iO = list4;
        this.iP = str4;
        this.iQ = str5;
        this.iR = bxVar4;
        this.iS = str6;
        this.iT = str7;
        this.iU = str8;
        this.iV = list5;
        this.iW = str9;
        this.iX = str10;
        this.iY = str11;
        this.di = str12;
        this.iZ = str13;
        this.ja = str14;
        this.jb = str15;
        this.jc = str16;
        this.jd = str17;
        this.je = bxVar5;
        this.jf = str18;
        this.jg = str19;
        this.jh = str20;
        this.ji = str21;
        this.jj = bxVar6;
        this.fy = d;
        this.jk = bxVar7;
        this.fz = d2;
        this.mName = str22;
        this.jl = bxVar8;
        this.jm = list6;
        this.jn = str23;
        this.jo = str24;
        this.jp = str25;
        this.jq = str26;
        this.jr = bxVar9;
        this.js = str27;
        this.jt = str28;
        this.ju = str29;
        this.jv = bxVar10;
        this.jw = str30;
        this.jx = str31;
        this.jy = str32;
        this.ie = str33;
        this.jz = str34;
        this.jA = str35;
    }

    public bx(Set<Integer> set, bx bxVar, List<String> list, bx bxVar2, String str, String str2, String str3, List<bx> list2, int i, List<bx> list3, bx bxVar3, List<bx> list4, String str4, String str5, bx bxVar4, String str6, String str7, String str8, List<bx> list5, String str9, String str10, String str11, String str12, String str13, String str14, String str15, String str16, String str17, bx bxVar5, String str18, String str19, String str20, String str21, bx bxVar6, double d, bx bxVar7, double d2, String str22, bx bxVar8, List<bx> list6, String str23, String str24, String str25, String str26, bx bxVar9, String str27, String str28, String str29, bx bxVar10, String str30, String str31, String str32, String str33, String str34, String str35) {
        this.iD = set;
        this.ab = 1;
        this.iE = bxVar;
        this.iF = list;
        this.iG = bxVar2;
        this.iH = str;
        this.iI = str2;
        this.iJ = str3;
        this.iK = list2;
        this.iL = i;
        this.iM = list3;
        this.iN = bxVar3;
        this.iO = list4;
        this.iP = str4;
        this.iQ = str5;
        this.iR = bxVar4;
        this.iS = str6;
        this.iT = str7;
        this.iU = str8;
        this.iV = list5;
        this.iW = str9;
        this.iX = str10;
        this.iY = str11;
        this.di = str12;
        this.iZ = str13;
        this.ja = str14;
        this.jb = str15;
        this.jc = str16;
        this.jd = str17;
        this.je = bxVar5;
        this.jf = str18;
        this.jg = str19;
        this.jh = str20;
        this.ji = str21;
        this.jj = bxVar6;
        this.fy = d;
        this.jk = bxVar7;
        this.fz = d2;
        this.mName = str22;
        this.jl = bxVar8;
        this.jm = list6;
        this.jn = str23;
        this.jo = str24;
        this.jp = str25;
        this.jq = str26;
        this.jr = bxVar9;
        this.js = str27;
        this.jt = str28;
        this.ju = str29;
        this.jv = bxVar10;
        this.jw = str30;
        this.jx = str31;
        this.jy = str32;
        this.ie = str33;
        this.jz = str34;
        this.jA = str35;
    }

    public HashMap<String, C1308a<?, ?>> mo1087T() {
        return iC;
    }

    protected boolean mo2354a(C1308a c1308a) {
        return this.iD.contains(Integer.valueOf(c1308a.aa()));
    }

    protected Object mo2355b(C1308a c1308a) {
        switch (c1308a.aa()) {
            case 2:
                return this.iE;
            case 3:
                return this.iF;
            case 4:
                return this.iG;
            case 5:
                return this.iH;
            case 6:
                return this.iI;
            case 7:
                return this.iJ;
            case 8:
                return this.iK;
            case 9:
                return Integer.valueOf(this.iL);
            case 10:
                return this.iM;
            case 11:
                return this.iN;
            case 12:
                return this.iO;
            case 13:
                return this.iP;
            case 14:
                return this.iQ;
            case 15:
                return this.iR;
            case 16:
                return this.iS;
            case 17:
                return this.iT;
            case 18:
                return this.iU;
            case 19:
                return this.iV;
            case 20:
                return this.iW;
            case 21:
                return this.iX;
            case 22:
                return this.iY;
            case 23:
                return this.di;
            case 24:
                return this.iZ;
            case 25:
                return this.ja;
            case NalUnitTypes.NAL_TYPE_RSV_VCL26 /*26*/:
                return this.jb;
            case NalUnitTypes.NAL_TYPE_RSV_VCL27 /*27*/:
                return this.jc;
            case NalUnitTypes.NAL_TYPE_RSV_VCL28 /*28*/:
                return this.jd;
            case NalUnitTypes.NAL_TYPE_RSV_VCL29 /*29*/:
                return this.je;
            case NalUnitTypes.NAL_TYPE_RSV_VCL30 /*30*/:
                return this.jf;
            case NalUnitTypes.NAL_TYPE_RSV_VCL31 /*31*/:
                return this.jg;
            case 32:
                return this.jh;
            case 33:
                return this.ji;
            case 34:
                return this.jj;
            case 36:
                return Double.valueOf(this.fy);
            case 37:
                return this.jk;
            case 38:
                return Double.valueOf(this.fz);
            case 39:
                return this.mName;
            case MotionEventCompat.AXIS_GENERIC_9 /*40*/:
                return this.jl;
            case 41:
                return this.jm;
            case 42:
                return this.jn;
            case 43:
                return this.jo;
            case 44:
                return this.jp;
            case MotionEventCompat.AXIS_GENERIC_14 /*45*/:
                return this.jq;
            case MotionEventCompat.AXIS_GENERIC_15 /*46*/:
                return this.jr;
            case MotionEventCompat.AXIS_GENERIC_16 /*47*/:
                return this.js;
            case 48:
                return this.jt;
            case 49:
                return this.ju;
            case 50:
                return this.jv;
            case 51:
                return this.jw;
            case 52:
                return this.jx;
            case 53:
                return this.jy;
            case 54:
                return this.ie;
            case 55:
                return this.jz;
            case 56:
                return this.jA;
            default:
                throw new IllegalStateException("Unknown safe parcelable id=" + c1308a.aa());
        }
    }

    Set<Integer> bH() {
        return this.iD;
    }

    bx bI() {
        return this.iE;
    }

    bx bJ() {
        return this.iG;
    }

    List<bx> bK() {
        return this.iK;
    }

    List<bx> bL() {
        return this.iM;
    }

    bx bM() {
        return this.iN;
    }

    List<bx> bN() {
        return this.iO;
    }

    bx bO() {
        return this.iR;
    }

    List<bx> bP() {
        return this.iV;
    }

    bx bQ() {
        return this.je;
    }

    bx bR() {
        return this.jj;
    }

    bx bS() {
        return this.jk;
    }

    bx bT() {
        return this.jl;
    }

    List<bx> bU() {
        return this.jm;
    }

    bx bV() {
        return this.jr;
    }

    bx bW() {
        return this.jv;
    }

    public bx bX() {
        return this;
    }

    public int describeContents() {
        by byVar = CREATOR;
        return 0;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof bx)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        bx bxVar = (bx) obj;
        for (C1308a c1308a : iC.values()) {
            if (mo2354a(c1308a)) {
                if (!bxVar.mo2354a(c1308a)) {
                    return false;
                }
                if (!mo2355b(c1308a).equals(bxVar.mo2355b(c1308a))) {
                    return false;
                }
            } else if (bxVar.mo2354a(c1308a)) {
                return false;
            }
        }
        return true;
    }

    public /* synthetic */ Object freeze() {
        return bX();
    }

    public ItemScope getAbout() {
        return this.iE;
    }

    public List<String> getAdditionalName() {
        return this.iF;
    }

    public ItemScope getAddress() {
        return this.iG;
    }

    public String getAddressCountry() {
        return this.iH;
    }

    public String getAddressLocality() {
        return this.iI;
    }

    public String getAddressRegion() {
        return this.iJ;
    }

    public List<ItemScope> getAssociated_media() {
        return (ArrayList) this.iK;
    }

    public int getAttendeeCount() {
        return this.iL;
    }

    public List<ItemScope> getAttendees() {
        return (ArrayList) this.iM;
    }

    public ItemScope getAudio() {
        return this.iN;
    }

    public List<ItemScope> getAuthor() {
        return (ArrayList) this.iO;
    }

    public String getBestRating() {
        return this.iP;
    }

    public String getBirthDate() {
        return this.iQ;
    }

    public ItemScope getByArtist() {
        return this.iR;
    }

    public String getCaption() {
        return this.iS;
    }

    public String getContentSize() {
        return this.iT;
    }

    public String getContentUrl() {
        return this.iU;
    }

    public List<ItemScope> getContributor() {
        return (ArrayList) this.iV;
    }

    public String getDateCreated() {
        return this.iW;
    }

    public String getDateModified() {
        return this.iX;
    }

    public String getDatePublished() {
        return this.iY;
    }

    public String getDescription() {
        return this.di;
    }

    public String getDuration() {
        return this.iZ;
    }

    public String getEmbedUrl() {
        return this.ja;
    }

    public String getEndDate() {
        return this.jb;
    }

    public String getFamilyName() {
        return this.jc;
    }

    public String getGender() {
        return this.jd;
    }

    public ItemScope getGeo() {
        return this.je;
    }

    public String getGivenName() {
        return this.jf;
    }

    public String getHeight() {
        return this.jg;
    }

    public String getId() {
        return this.jh;
    }

    public String getImage() {
        return this.ji;
    }

    public ItemScope getInAlbum() {
        return this.jj;
    }

    public double getLatitude() {
        return this.fy;
    }

    public ItemScope getLocation() {
        return this.jk;
    }

    public double getLongitude() {
        return this.fz;
    }

    public String getName() {
        return this.mName;
    }

    public ItemScope getPartOfTVSeries() {
        return this.jl;
    }

    public List<ItemScope> getPerformers() {
        return (ArrayList) this.jm;
    }

    public String getPlayerType() {
        return this.jn;
    }

    public String getPostOfficeBoxNumber() {
        return this.jo;
    }

    public String getPostalCode() {
        return this.jp;
    }

    public String getRatingValue() {
        return this.jq;
    }

    public ItemScope getReviewRating() {
        return this.jr;
    }

    public String getStartDate() {
        return this.js;
    }

    public String getStreetAddress() {
        return this.jt;
    }

    public String getText() {
        return this.ju;
    }

    public ItemScope getThumbnail() {
        return this.jv;
    }

    public String getThumbnailUrl() {
        return this.jw;
    }

    public String getTickerSymbol() {
        return this.jx;
    }

    public String getType() {
        return this.jy;
    }

    public String getUrl() {
        return this.ie;
    }

    public String getWidth() {
        return this.jz;
    }

    public String getWorstRating() {
        return this.jA;
    }

    public boolean hasAbout() {
        return this.iD.contains(Integer.valueOf(2));
    }

    public boolean hasAdditionalName() {
        return this.iD.contains(Integer.valueOf(3));
    }

    public boolean hasAddress() {
        return this.iD.contains(Integer.valueOf(4));
    }

    public boolean hasAddressCountry() {
        return this.iD.contains(Integer.valueOf(5));
    }

    public boolean hasAddressLocality() {
        return this.iD.contains(Integer.valueOf(6));
    }

    public boolean hasAddressRegion() {
        return this.iD.contains(Integer.valueOf(7));
    }

    public boolean hasAssociated_media() {
        return this.iD.contains(Integer.valueOf(8));
    }

    public boolean hasAttendeeCount() {
        return this.iD.contains(Integer.valueOf(9));
    }

    public boolean hasAttendees() {
        return this.iD.contains(Integer.valueOf(10));
    }

    public boolean hasAudio() {
        return this.iD.contains(Integer.valueOf(11));
    }

    public boolean hasAuthor() {
        return this.iD.contains(Integer.valueOf(12));
    }

    public boolean hasBestRating() {
        return this.iD.contains(Integer.valueOf(13));
    }

    public boolean hasBirthDate() {
        return this.iD.contains(Integer.valueOf(14));
    }

    public boolean hasByArtist() {
        return this.iD.contains(Integer.valueOf(15));
    }

    public boolean hasCaption() {
        return this.iD.contains(Integer.valueOf(16));
    }

    public boolean hasContentSize() {
        return this.iD.contains(Integer.valueOf(17));
    }

    public boolean hasContentUrl() {
        return this.iD.contains(Integer.valueOf(18));
    }

    public boolean hasContributor() {
        return this.iD.contains(Integer.valueOf(19));
    }

    public boolean hasDateCreated() {
        return this.iD.contains(Integer.valueOf(20));
    }

    public boolean hasDateModified() {
        return this.iD.contains(Integer.valueOf(21));
    }

    public boolean hasDatePublished() {
        return this.iD.contains(Integer.valueOf(22));
    }

    public boolean hasDescription() {
        return this.iD.contains(Integer.valueOf(23));
    }

    public boolean hasDuration() {
        return this.iD.contains(Integer.valueOf(24));
    }

    public boolean hasEmbedUrl() {
        return this.iD.contains(Integer.valueOf(25));
    }

    public boolean hasEndDate() {
        return this.iD.contains(Integer.valueOf(26));
    }

    public boolean hasFamilyName() {
        return this.iD.contains(Integer.valueOf(27));
    }

    public boolean hasGender() {
        return this.iD.contains(Integer.valueOf(28));
    }

    public boolean hasGeo() {
        return this.iD.contains(Integer.valueOf(29));
    }

    public boolean hasGivenName() {
        return this.iD.contains(Integer.valueOf(30));
    }

    public boolean hasHeight() {
        return this.iD.contains(Integer.valueOf(31));
    }

    public boolean hasId() {
        return this.iD.contains(Integer.valueOf(32));
    }

    public boolean hasImage() {
        return this.iD.contains(Integer.valueOf(33));
    }

    public boolean hasInAlbum() {
        return this.iD.contains(Integer.valueOf(34));
    }

    public boolean hasLatitude() {
        return this.iD.contains(Integer.valueOf(36));
    }

    public boolean hasLocation() {
        return this.iD.contains(Integer.valueOf(37));
    }

    public boolean hasLongitude() {
        return this.iD.contains(Integer.valueOf(38));
    }

    public boolean hasName() {
        return this.iD.contains(Integer.valueOf(39));
    }

    public boolean hasPartOfTVSeries() {
        return this.iD.contains(Integer.valueOf(40));
    }

    public boolean hasPerformers() {
        return this.iD.contains(Integer.valueOf(41));
    }

    public boolean hasPlayerType() {
        return this.iD.contains(Integer.valueOf(42));
    }

    public boolean hasPostOfficeBoxNumber() {
        return this.iD.contains(Integer.valueOf(43));
    }

    public boolean hasPostalCode() {
        return this.iD.contains(Integer.valueOf(44));
    }

    public boolean hasRatingValue() {
        return this.iD.contains(Integer.valueOf(45));
    }

    public boolean hasReviewRating() {
        return this.iD.contains(Integer.valueOf(46));
    }

    public boolean hasStartDate() {
        return this.iD.contains(Integer.valueOf(47));
    }

    public boolean hasStreetAddress() {
        return this.iD.contains(Integer.valueOf(48));
    }

    public boolean hasText() {
        return this.iD.contains(Integer.valueOf(49));
    }

    public boolean hasThumbnail() {
        return this.iD.contains(Integer.valueOf(50));
    }

    public boolean hasThumbnailUrl() {
        return this.iD.contains(Integer.valueOf(51));
    }

    public boolean hasTickerSymbol() {
        return this.iD.contains(Integer.valueOf(52));
    }

    public boolean hasType() {
        return this.iD.contains(Integer.valueOf(53));
    }

    public boolean hasUrl() {
        return this.iD.contains(Integer.valueOf(54));
    }

    public boolean hasWidth() {
        return this.iD.contains(Integer.valueOf(55));
    }

    public boolean hasWorstRating() {
        return this.iD.contains(Integer.valueOf(56));
    }

    public int hashCode() {
        int i = 0;
        for (C1308a c1308a : iC.values()) {
            int hashCode;
            if (mo2354a(c1308a)) {
                hashCode = mo2355b(c1308a).hashCode() + (i + c1308a.aa());
            } else {
                hashCode = i;
            }
            i = hashCode;
        }
        return i;
    }

    int m1268i() {
        return this.ab;
    }

    public boolean isDataValid() {
        return true;
    }

    protected Object mo1088m(String str) {
        return null;
    }

    protected boolean mo1089n(String str) {
        return false;
    }

    public void writeToParcel(Parcel out, int flags) {
        by byVar = CREATOR;
        by.m415a(this, out, flags);
    }
}
