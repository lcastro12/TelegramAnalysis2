package com.google.android.gms.games.multiplayer.realtime;

import android.os.Bundle;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.internal.C0192s;
import java.util.ArrayList;
import java.util.Arrays;

public final class RoomConfig {
    private final String eF;
    private final int eK;
    private final RoomUpdateListener eW;
    private final RoomStatusUpdateListener eX;
    private final RealTimeMessageReceivedListener eY;
    private final String[] eZ;
    private final Bundle fa;
    private final boolean fb;

    public static final class Builder {
        int eK;
        final RoomUpdateListener eW;
        RoomStatusUpdateListener eX;
        RealTimeMessageReceivedListener eY;
        Bundle fa;
        boolean fb;
        String fc;
        ArrayList<String> fd;

        private Builder(RoomUpdateListener updateListener) {
            this.fc = null;
            this.eK = -1;
            this.fd = new ArrayList();
            this.fb = false;
            this.eW = (RoomUpdateListener) C0192s.m518b((Object) updateListener, (Object) "Must provide a RoomUpdateListener");
        }

        public Builder addPlayersToInvite(ArrayList<String> playerIds) {
            C0192s.m521d(playerIds);
            this.fd.addAll(playerIds);
            return this;
        }

        public Builder addPlayersToInvite(String... playerIds) {
            C0192s.m521d(playerIds);
            this.fd.addAll(Arrays.asList(playerIds));
            return this;
        }

        public RoomConfig build() {
            return new RoomConfig();
        }

        public Builder setAutoMatchCriteria(Bundle autoMatchCriteria) {
            this.fa = autoMatchCriteria;
            return this;
        }

        public Builder setInvitationIdToAccept(String invitationId) {
            C0192s.m521d(invitationId);
            this.fc = invitationId;
            return this;
        }

        public Builder setMessageReceivedListener(RealTimeMessageReceivedListener listener) {
            this.eY = listener;
            return this;
        }

        public Builder setRoomStatusUpdateListener(RoomStatusUpdateListener listener) {
            this.eX = listener;
            return this;
        }

        public Builder setSocketCommunicationEnabled(boolean enableSockets) {
            this.fb = enableSockets;
            return this;
        }

        public Builder setVariant(int variant) {
            this.eK = variant;
            return this;
        }
    }

    private RoomConfig(Builder builder) {
        this.eW = builder.eW;
        this.eX = builder.eX;
        this.eY = builder.eY;
        this.eF = builder.fc;
        this.eK = builder.eK;
        this.fa = builder.fa;
        this.fb = builder.fb;
        this.eZ = (String[]) builder.fd.toArray(new String[builder.fd.size()]);
        if (this.eY == null) {
            C0192s.m516a(this.fb, "Must either enable sockets OR specify a message listener");
        }
    }

    public static Builder builder(RoomUpdateListener listener) {
        return new Builder(listener);
    }

    public static Bundle createAutoMatchCriteria(int minAutoMatchPlayers, int maxAutoMatchPlayers, long exclusiveBitMask) {
        Bundle bundle = new Bundle();
        bundle.putInt(GamesClient.EXTRA_MIN_AUTOMATCH_PLAYERS, minAutoMatchPlayers);
        bundle.putInt(GamesClient.EXTRA_MAX_AUTOMATCH_PLAYERS, maxAutoMatchPlayers);
        bundle.putLong(GamesClient.EXTRA_EXCLUSIVE_BIT_MASK, exclusiveBitMask);
        return bundle;
    }

    public Bundle getAutoMatchCriteria() {
        return this.fa;
    }

    public String getInvitationId() {
        return this.eF;
    }

    public String[] getInvitedPlayerIds() {
        return this.eZ;
    }

    public RealTimeMessageReceivedListener getMessageReceivedListener() {
        return this.eY;
    }

    public RoomStatusUpdateListener getRoomStatusUpdateListener() {
        return this.eX;
    }

    public RoomUpdateListener getRoomUpdateListener() {
        return this.eW;
    }

    public int getVariant() {
        return this.eK;
    }

    public boolean isSocketEnabled() {
        return this.fb;
    }
}
