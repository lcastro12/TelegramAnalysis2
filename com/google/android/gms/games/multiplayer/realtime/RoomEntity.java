package com.google.android.gms.games.multiplayer.realtime;

import android.database.CharArrayBuffer;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.ParticipantEntity;
import com.google.android.gms.internal.C0191r;
import com.google.android.gms.internal.C1351j;
import com.google.android.gms.internal.ao;
import com.google.android.gms.internal.av;
import java.util.ArrayList;

public final class RoomEntity extends av implements Room {
    public static final Creator<RoomEntity> CREATOR = new C1304a();
    private final int ab;
    private final String dV;
    private final String di;
    private final long eG;
    private final ArrayList<ParticipantEntity> eJ;
    private final int eK;
    private final Bundle fa;
    private final String fe;
    private final int ff;
    private final int fg;

    static final class C1304a extends C0158b {
        C1304a() {
        }

        public /* synthetic */ Object createFromParcel(Parcel x0) {
            return mo1082s(x0);
        }

        public RoomEntity mo1082s(Parcel parcel) {
            if (av.m1217c(C1351j.m970v()) || C1351j.m968h(RoomEntity.class.getCanonicalName())) {
                return super.mo1082s(parcel);
            }
            String readString = parcel.readString();
            String readString2 = parcel.readString();
            long readLong = parcel.readLong();
            int readInt = parcel.readInt();
            String readString3 = parcel.readString();
            int readInt2 = parcel.readInt();
            Bundle readBundle = parcel.readBundle();
            int readInt3 = parcel.readInt();
            ArrayList arrayList = new ArrayList(readInt3);
            for (int i = 0; i < readInt3; i++) {
                arrayList.add(ParticipantEntity.CREATOR.createFromParcel(parcel));
            }
            return new RoomEntity(2, readString, readString2, readLong, readInt, readString3, readInt2, readBundle, arrayList, -1);
        }
    }

    RoomEntity(int versionCode, String roomId, String creatorId, long creationTimestamp, int roomStatus, String description, int variant, Bundle autoMatchCriteria, ArrayList<ParticipantEntity> participants, int autoMatchWaitEstimateSeconds) {
        this.ab = versionCode;
        this.dV = roomId;
        this.fe = creatorId;
        this.eG = creationTimestamp;
        this.ff = roomStatus;
        this.di = description;
        this.eK = variant;
        this.fa = autoMatchCriteria;
        this.eJ = participants;
        this.fg = autoMatchWaitEstimateSeconds;
    }

    public RoomEntity(Room room) {
        this.ab = 2;
        this.dV = room.getRoomId();
        this.fe = room.getCreatorId();
        this.eG = room.getCreationTimestamp();
        this.ff = room.getStatus();
        this.di = room.getDescription();
        this.eK = room.getVariant();
        this.fa = room.getAutoMatchCriteria();
        ArrayList participants = room.getParticipants();
        int size = participants.size();
        this.eJ = new ArrayList(size);
        for (int i = 0; i < size; i++) {
            this.eJ.add((ParticipantEntity) ((Participant) participants.get(i)).freeze());
        }
        this.fg = room.getAutoMatchWaitEstimateSeconds();
    }

    static int m1382a(Room room) {
        return C0191r.hashCode(room.getRoomId(), room.getCreatorId(), Long.valueOf(room.getCreationTimestamp()), Integer.valueOf(room.getStatus()), room.getDescription(), Integer.valueOf(room.getVariant()), room.getAutoMatchCriteria(), room.getParticipants(), Integer.valueOf(room.getAutoMatchWaitEstimateSeconds()));
    }

    static boolean m1383a(Room room, Object obj) {
        if (!(obj instanceof Room)) {
            return false;
        }
        if (room == obj) {
            return true;
        }
        Room room2 = (Room) obj;
        return C0191r.m513a(room2.getRoomId(), room.getRoomId()) && C0191r.m513a(room2.getCreatorId(), room.getCreatorId()) && C0191r.m513a(Long.valueOf(room2.getCreationTimestamp()), Long.valueOf(room.getCreationTimestamp())) && C0191r.m513a(Integer.valueOf(room2.getStatus()), Integer.valueOf(room.getStatus())) && C0191r.m513a(room2.getDescription(), room.getDescription()) && C0191r.m513a(Integer.valueOf(room2.getVariant()), Integer.valueOf(room.getVariant())) && C0191r.m513a(room2.getAutoMatchCriteria(), room.getAutoMatchCriteria()) && C0191r.m513a(room2.getParticipants(), room.getParticipants()) && C0191r.m513a(Integer.valueOf(room2.getAutoMatchWaitEstimateSeconds()), Integer.valueOf(room.getAutoMatchWaitEstimateSeconds()));
    }

    static String m1384b(Room room) {
        return C0191r.m514c(room).m512a("RoomId", room.getRoomId()).m512a("CreatorId", room.getCreatorId()).m512a("CreationTimestamp", Long.valueOf(room.getCreationTimestamp())).m512a("RoomStatus", Integer.valueOf(room.getStatus())).m512a("Description", room.getDescription()).m512a("Variant", Integer.valueOf(room.getVariant())).m512a("AutoMatchCriteria", room.getAutoMatchCriteria()).m512a("Participants", room.getParticipants()).m512a("AutoMatchWaitEstimateSeconds", Integer.valueOf(room.getAutoMatchWaitEstimateSeconds())).toString();
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object obj) {
        return m1383a(this, obj);
    }

    public Room freeze() {
        return this;
    }

    public Bundle getAutoMatchCriteria() {
        return this.fa;
    }

    public int getAutoMatchWaitEstimateSeconds() {
        return this.fg;
    }

    public long getCreationTimestamp() {
        return this.eG;
    }

    public String getCreatorId() {
        return this.fe;
    }

    public String getDescription() {
        return this.di;
    }

    public void getDescription(CharArrayBuffer dataOut) {
        ao.m215b(this.di, dataOut);
    }

    public String getParticipantId(String playerId) {
        int size = this.eJ.size();
        for (int i = 0; i < size; i++) {
            Participant participant = (Participant) this.eJ.get(i);
            Player player = participant.getPlayer();
            if (player != null && player.getPlayerId().equals(playerId)) {
                return participant.getParticipantId();
            }
        }
        return null;
    }

    public ArrayList<String> getParticipantIds() {
        int size = this.eJ.size();
        ArrayList<String> arrayList = new ArrayList(size);
        for (int i = 0; i < size; i++) {
            arrayList.add(((ParticipantEntity) this.eJ.get(i)).getParticipantId());
        }
        return arrayList;
    }

    public int getParticipantStatus(String participantId) {
        int size = this.eJ.size();
        for (int i = 0; i < size; i++) {
            Participant participant = (Participant) this.eJ.get(i);
            if (participant.getParticipantId().equals(participantId)) {
                return participant.getStatus();
            }
        }
        throw new IllegalStateException("Participant " + participantId + " is not in room " + getRoomId());
    }

    public ArrayList<Participant> getParticipants() {
        return new ArrayList(this.eJ);
    }

    public String getRoomId() {
        return this.dV;
    }

    public int getStatus() {
        return this.ff;
    }

    public int getVariant() {
        return this.eK;
    }

    public int hashCode() {
        return m1382a(this);
    }

    public int m1387i() {
        return this.ab;
    }

    public boolean isDataValid() {
        return true;
    }

    public String toString() {
        return m1384b((Room) this);
    }

    public void writeToParcel(Parcel dest, int flags) {
        if (m971w()) {
            dest.writeString(this.dV);
            dest.writeString(this.fe);
            dest.writeLong(this.eG);
            dest.writeInt(this.ff);
            dest.writeString(this.di);
            dest.writeInt(this.eK);
            dest.writeBundle(this.fa);
            int size = this.eJ.size();
            dest.writeInt(size);
            for (int i = 0; i < size; i++) {
                ((ParticipantEntity) this.eJ.get(i)).writeToParcel(dest, flags);
            }
            return;
        }
        C0158b.m160a(this, dest, flags);
    }
}
