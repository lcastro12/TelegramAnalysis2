package com.google.android.gms.games.multiplayer.realtime;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.google.android.gms.internal.C0192s;

public final class RealTimeMessage implements Parcelable {
    public static final Creator<RealTimeMessage> CREATOR = new C01561();
    public static final int RELIABLE = 1;
    public static final int UNRELIABLE = 0;
    private final String eT;
    private final byte[] eU;
    private final int eV;

    static class C01561 implements Creator<RealTimeMessage> {
        C01561() {
        }

        public RealTimeMessage[] m158J(int i) {
            return new RealTimeMessage[i];
        }

        public /* synthetic */ Object createFromParcel(Parcel x0) {
            return m159r(x0);
        }

        public /* synthetic */ Object[] newArray(int x0) {
            return m158J(x0);
        }

        public RealTimeMessage m159r(Parcel parcel) {
            return new RealTimeMessage(parcel);
        }
    }

    private RealTimeMessage(Parcel parcel) {
        this(parcel.readString(), parcel.createByteArray(), parcel.readInt());
    }

    public RealTimeMessage(String senderParticipantId, byte[] messageData, int isReliable) {
        this.eT = (String) C0192s.m521d(senderParticipantId);
        this.eU = (byte[]) ((byte[]) C0192s.m521d(messageData)).clone();
        this.eV = isReliable;
    }

    public int describeContents() {
        return 0;
    }

    public byte[] getMessageData() {
        return this.eU;
    }

    public String getSenderParticipantId() {
        return this.eT;
    }

    public boolean isReliable() {
        return this.eV == 1;
    }

    public void writeToParcel(Parcel parcel, int flag) {
        parcel.writeString(this.eT);
        parcel.writeByteArray(this.eU);
        parcel.writeInt(this.eV);
    }
}
