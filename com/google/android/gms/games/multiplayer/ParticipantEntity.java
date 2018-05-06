package com.google.android.gms.games.multiplayer;

import android.database.CharArrayBuffer;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.PlayerEntity;
import com.google.android.gms.internal.C0191r;
import com.google.android.gms.internal.C1351j;
import com.google.android.gms.internal.ao;
import com.google.android.gms.internal.av;

public final class ParticipantEntity extends av implements Participant {
    public static final Creator<ParticipantEntity> CREATOR = new C1303a();
    private final int ab;
    private final String cl;
    private final String dX;
    private final Uri dk;
    private final Uri dl;
    private final int eN;
    private final String eO;
    private final boolean eP;
    private final PlayerEntity eQ;
    private final int eR;

    static final class C1303a extends C0155c {
        C1303a() {
        }

        public /* synthetic */ Object createFromParcel(Parcel x0) {
            return mo1080q(x0);
        }

        public ParticipantEntity mo1080q(Parcel parcel) {
            int i = 0;
            if (av.m1217c(C1351j.m970v()) || C1351j.m968h(ParticipantEntity.class.getCanonicalName())) {
                return super.mo1080q(parcel);
            }
            String readString = parcel.readString();
            String readString2 = parcel.readString();
            String readString3 = parcel.readString();
            Uri parse = readString3 == null ? null : Uri.parse(readString3);
            String readString4 = parcel.readString();
            Uri parse2 = readString4 == null ? null : Uri.parse(readString4);
            int readInt = parcel.readInt();
            String readString5 = parcel.readString();
            boolean z = parcel.readInt() > 0;
            if (parcel.readInt() > 0) {
                i = 1;
            }
            return new ParticipantEntity(1, readString, readString2, parse, parse2, readInt, readString5, z, i != 0 ? (PlayerEntity) PlayerEntity.CREATOR.createFromParcel(parcel) : null, 7);
        }
    }

    ParticipantEntity(int versionCode, String participantId, String displayName, Uri iconImageUri, Uri hiResImageUri, int status, String clientAddress, boolean connectedToRoom, PlayerEntity player, int capabilities) {
        this.ab = versionCode;
        this.dX = participantId;
        this.cl = displayName;
        this.dk = iconImageUri;
        this.dl = hiResImageUri;
        this.eN = status;
        this.eO = clientAddress;
        this.eP = connectedToRoom;
        this.eQ = player;
        this.eR = capabilities;
    }

    public ParticipantEntity(Participant participant) {
        this.ab = 1;
        this.dX = participant.getParticipantId();
        this.cl = participant.getDisplayName();
        this.dk = participant.getIconImageUri();
        this.dl = participant.getHiResImageUri();
        this.eN = participant.getStatus();
        this.eO = participant.aM();
        this.eP = participant.isConnectedToRoom();
        Player player = participant.getPlayer();
        this.eQ = player == null ? null : new PlayerEntity(player);
        this.eR = participant.aN();
    }

    static int m1376a(Participant participant) {
        return C0191r.hashCode(participant.getPlayer(), Integer.valueOf(participant.getStatus()), participant.aM(), Boolean.valueOf(participant.isConnectedToRoom()), participant.getDisplayName(), participant.getIconImageUri(), participant.getHiResImageUri(), Integer.valueOf(participant.aN()));
    }

    static boolean m1377a(Participant participant, Object obj) {
        if (!(obj instanceof Participant)) {
            return false;
        }
        if (participant == obj) {
            return true;
        }
        Participant participant2 = (Participant) obj;
        return C0191r.m513a(participant2.getPlayer(), participant.getPlayer()) && C0191r.m513a(Integer.valueOf(participant2.getStatus()), Integer.valueOf(participant.getStatus())) && C0191r.m513a(participant2.aM(), participant.aM()) && C0191r.m513a(Boolean.valueOf(participant2.isConnectedToRoom()), Boolean.valueOf(participant.isConnectedToRoom())) && C0191r.m513a(participant2.getDisplayName(), participant.getDisplayName()) && C0191r.m513a(participant2.getIconImageUri(), participant.getIconImageUri()) && C0191r.m513a(participant2.getHiResImageUri(), participant.getHiResImageUri()) && C0191r.m513a(Integer.valueOf(participant2.aN()), Integer.valueOf(participant.aN()));
    }

    static String m1378b(Participant participant) {
        return C0191r.m514c(participant).m512a("Player", participant.getPlayer()).m512a("Status", Integer.valueOf(participant.getStatus())).m512a("ClientAddress", participant.aM()).m512a("ConnectedToRoom", Boolean.valueOf(participant.isConnectedToRoom())).m512a("DisplayName", participant.getDisplayName()).m512a("IconImage", participant.getIconImageUri()).m512a("HiResImage", participant.getHiResImageUri()).m512a("Capabilities", Integer.valueOf(participant.aN())).toString();
    }

    public String aM() {
        return this.eO;
    }

    public int aN() {
        return this.eR;
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object obj) {
        return m1377a(this, obj);
    }

    public Participant freeze() {
        return this;
    }

    public String getDisplayName() {
        return this.eQ == null ? this.cl : this.eQ.getDisplayName();
    }

    public void getDisplayName(CharArrayBuffer dataOut) {
        if (this.eQ == null) {
            ao.m215b(this.cl, dataOut);
        } else {
            this.eQ.getDisplayName(dataOut);
        }
    }

    public Uri getHiResImageUri() {
        return this.eQ == null ? this.dl : this.eQ.getHiResImageUri();
    }

    public Uri getIconImageUri() {
        return this.eQ == null ? this.dk : this.eQ.getIconImageUri();
    }

    public String getParticipantId() {
        return this.dX;
    }

    public Player getPlayer() {
        return this.eQ;
    }

    public int getStatus() {
        return this.eN;
    }

    public int hashCode() {
        return m1376a(this);
    }

    public int m1381i() {
        return this.ab;
    }

    public boolean isConnectedToRoom() {
        return this.eP;
    }

    public boolean isDataValid() {
        return true;
    }

    public String toString() {
        return m1378b((Participant) this);
    }

    public void writeToParcel(Parcel dest, int flags) {
        String str = null;
        int i = 0;
        if (m971w()) {
            dest.writeString(this.dX);
            dest.writeString(this.cl);
            dest.writeString(this.dk == null ? null : this.dk.toString());
            if (this.dl != null) {
                str = this.dl.toString();
            }
            dest.writeString(str);
            dest.writeInt(this.eN);
            dest.writeString(this.eO);
            dest.writeInt(this.eP ? 1 : 0);
            if (this.eQ != null) {
                i = 1;
            }
            dest.writeInt(i);
            if (this.eQ != null) {
                this.eQ.writeToParcel(dest, flags);
                return;
            }
            return;
        }
        C0155c.m155a(this, dest, flags);
    }
}
