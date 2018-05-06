package com.google.android.gms.games.multiplayer;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.games.Game;
import com.google.android.gms.games.GameEntity;
import com.google.android.gms.internal.C0191r;
import com.google.android.gms.internal.C0192s;
import com.google.android.gms.internal.C1351j;
import com.google.android.gms.internal.av;
import java.util.ArrayList;

public final class InvitationEntity extends av implements Invitation {
    public static final Creator<InvitationEntity> CREATOR = new C1302a();
    private final int ab;
    private final GameEntity eE;
    private final String eF;
    private final long eG;
    private final int eH;
    private final ParticipantEntity eI;
    private final ArrayList<ParticipantEntity> eJ;
    private final int eK;

    static final class C1302a extends C0154a {
        C1302a() {
        }

        public /* synthetic */ Object createFromParcel(Parcel x0) {
            return mo1078p(x0);
        }

        public InvitationEntity mo1078p(Parcel parcel) {
            if (av.m1217c(C1351j.m970v()) || C1351j.m968h(InvitationEntity.class.getCanonicalName())) {
                return super.mo1078p(parcel);
            }
            GameEntity gameEntity = (GameEntity) GameEntity.CREATOR.createFromParcel(parcel);
            String readString = parcel.readString();
            long readLong = parcel.readLong();
            int readInt = parcel.readInt();
            ParticipantEntity participantEntity = (ParticipantEntity) ParticipantEntity.CREATOR.createFromParcel(parcel);
            int readInt2 = parcel.readInt();
            ArrayList arrayList = new ArrayList(readInt2);
            for (int i = 0; i < readInt2; i++) {
                arrayList.add(ParticipantEntity.CREATOR.createFromParcel(parcel));
            }
            return new InvitationEntity(1, gameEntity, readString, readLong, readInt, participantEntity, arrayList, -1);
        }
    }

    InvitationEntity(int versionCode, GameEntity game, String invitationId, long creationTimestamp, int invitationType, ParticipantEntity inviter, ArrayList<ParticipantEntity> participants, int variant) {
        this.ab = versionCode;
        this.eE = game;
        this.eF = invitationId;
        this.eG = creationTimestamp;
        this.eH = invitationType;
        this.eI = inviter;
        this.eJ = participants;
        this.eK = variant;
    }

    InvitationEntity(Invitation invitation) {
        this.ab = 1;
        this.eE = new GameEntity(invitation.getGame());
        this.eF = invitation.getInvitationId();
        this.eG = invitation.getCreationTimestamp();
        this.eH = invitation.aL();
        this.eK = invitation.getVariant();
        String participantId = invitation.getInviter().getParticipantId();
        Object obj = null;
        ArrayList participants = invitation.getParticipants();
        int size = participants.size();
        this.eJ = new ArrayList(size);
        for (int i = 0; i < size; i++) {
            Participant participant = (Participant) participants.get(i);
            if (participant.getParticipantId().equals(participantId)) {
                obj = participant;
            }
            this.eJ.add((ParticipantEntity) participant.freeze());
        }
        C0192s.m518b(obj, (Object) "Must have a valid inviter!");
        this.eI = (ParticipantEntity) obj.freeze();
    }

    static int m1370a(Invitation invitation) {
        return C0191r.hashCode(invitation.getGame(), invitation.getInvitationId(), Long.valueOf(invitation.getCreationTimestamp()), Integer.valueOf(invitation.aL()), invitation.getInviter(), invitation.getParticipants(), Integer.valueOf(invitation.getVariant()));
    }

    static boolean m1371a(Invitation invitation, Object obj) {
        if (!(obj instanceof Invitation)) {
            return false;
        }
        if (invitation == obj) {
            return true;
        }
        Invitation invitation2 = (Invitation) obj;
        return C0191r.m513a(invitation2.getGame(), invitation.getGame()) && C0191r.m513a(invitation2.getInvitationId(), invitation.getInvitationId()) && C0191r.m513a(Long.valueOf(invitation2.getCreationTimestamp()), Long.valueOf(invitation.getCreationTimestamp())) && C0191r.m513a(Integer.valueOf(invitation2.aL()), Integer.valueOf(invitation.aL())) && C0191r.m513a(invitation2.getInviter(), invitation.getInviter()) && C0191r.m513a(invitation2.getParticipants(), invitation.getParticipants()) && C0191r.m513a(Integer.valueOf(invitation2.getVariant()), Integer.valueOf(invitation.getVariant()));
    }

    static String m1372b(Invitation invitation) {
        return C0191r.m514c(invitation).m512a("Game", invitation.getGame()).m512a("InvitationId", invitation.getInvitationId()).m512a("CreationTimestamp", Long.valueOf(invitation.getCreationTimestamp())).m512a("InvitationType", Integer.valueOf(invitation.aL())).m512a("Inviter", invitation.getInviter()).m512a("Participants", invitation.getParticipants()).m512a("Variant", Integer.valueOf(invitation.getVariant())).toString();
    }

    public int aL() {
        return this.eH;
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object obj) {
        return m1371a(this, obj);
    }

    public Invitation freeze() {
        return this;
    }

    public long getCreationTimestamp() {
        return this.eG;
    }

    public Game getGame() {
        return this.eE;
    }

    public String getInvitationId() {
        return this.eF;
    }

    public Participant getInviter() {
        return this.eI;
    }

    public ArrayList<Participant> getParticipants() {
        return new ArrayList(this.eJ);
    }

    public int getVariant() {
        return this.eK;
    }

    public int hashCode() {
        return m1370a(this);
    }

    public int m1375i() {
        return this.ab;
    }

    public boolean isDataValid() {
        return true;
    }

    public String toString() {
        return m1372b((Invitation) this);
    }

    public void writeToParcel(Parcel dest, int flags) {
        if (m971w()) {
            this.eE.writeToParcel(dest, flags);
            dest.writeString(this.eF);
            dest.writeLong(this.eG);
            dest.writeInt(this.eH);
            this.eI.writeToParcel(dest, flags);
            int size = this.eJ.size();
            dest.writeInt(size);
            for (int i = 0; i < size; i++) {
                ((ParticipantEntity) this.eJ.get(i)).writeToParcel(dest, flags);
            }
            return;
        }
        C0154a.m152a(this, dest, flags);
    }
}
