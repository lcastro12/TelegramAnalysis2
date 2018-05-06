package com.google.android.gms.internal;

import android.content.Context;
import android.content.Intent;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcelable;
import android.os.RemoteException;
import android.view.View;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.data.C1287d;
import com.google.android.gms.games.Game;
import com.google.android.gms.games.GameBuffer;
import com.google.android.gms.games.GameEntity;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.OnGamesLoadedListener;
import com.google.android.gms.games.OnPlayersLoadedListener;
import com.google.android.gms.games.OnSignOutCompleteListener;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.PlayerBuffer;
import com.google.android.gms.games.PlayerEntity;
import com.google.android.gms.games.RealTimeSocket;
import com.google.android.gms.games.achievement.AchievementBuffer;
import com.google.android.gms.games.achievement.OnAchievementUpdatedListener;
import com.google.android.gms.games.achievement.OnAchievementsLoadedListener;
import com.google.android.gms.games.leaderboard.LeaderboardBuffer;
import com.google.android.gms.games.leaderboard.LeaderboardScoreBuffer;
import com.google.android.gms.games.leaderboard.OnLeaderboardMetadataLoadedListener;
import com.google.android.gms.games.leaderboard.OnLeaderboardScoresLoadedListener;
import com.google.android.gms.games.leaderboard.OnScoreSubmittedListener;
import com.google.android.gms.games.leaderboard.SubmitScoreResult;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.InvitationBuffer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.OnInvitationsLoadedListener;
import com.google.android.gms.games.multiplayer.ParticipantUtils;
import com.google.android.gms.games.multiplayer.realtime.C1696a;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeReliableMessageSentListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;
import com.google.android.gms.internal.C1354k.C0179b;
import com.google.android.gms.internal.C1354k.C1352c;
import com.google.android.gms.internal.C1354k.C1722d;
import com.google.android.gms.internal.az.C1321a;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class au extends C1354k<az> {
    private final Map<String, bb> dA;
    private PlayerEntity dB;
    private GameEntity dC;
    private final ba dD;
    private boolean dE = false;
    private final Binder dF;
    private final long dG;
    private final boolean dH;
    private final String dz;
    private final String f155g;

    final class ag extends C0179b<RealTimeReliableMessageSentListener> {
        final /* synthetic */ au dJ;
        private final String dZ;
        private final int ea;
        private final int f74p;

        ag(au auVar, RealTimeReliableMessageSentListener realTimeReliableMessageSentListener, int i, int i2, String str) {
            this.dJ = auVar;
            super(auVar, realTimeReliableMessageSentListener);
            this.f74p = i;
            this.ea = i2;
            this.dZ = str;
        }

        public void m708a(RealTimeReliableMessageSentListener realTimeReliableMessageSentListener) {
            if (realTimeReliableMessageSentListener != null) {
                realTimeReliableMessageSentListener.onRealTimeMessageSent(this.f74p, this.ea, this.dZ);
            }
        }

        protected void mo1092d() {
        }
    }

    final class ao extends C0179b<OnSignOutCompleteListener> {
        final /* synthetic */ au dJ;

        public ao(au auVar, OnSignOutCompleteListener onSignOutCompleteListener) {
            this.dJ = auVar;
            super(auVar, onSignOutCompleteListener);
        }

        public void m711a(OnSignOutCompleteListener onSignOutCompleteListener) {
            onSignOutCompleteListener.onSignOutComplete();
        }

        protected void mo1092d() {
        }
    }

    final class aq extends C0179b<OnScoreSubmittedListener> {
        final /* synthetic */ au dJ;
        private final SubmitScoreResult eh;

        public aq(au auVar, OnScoreSubmittedListener onScoreSubmittedListener, SubmitScoreResult submitScoreResult) {
            this.dJ = auVar;
            super(auVar, onScoreSubmittedListener);
            this.eh = submitScoreResult;
        }

        public void m714a(OnScoreSubmittedListener onScoreSubmittedListener) {
            onScoreSubmittedListener.onScoreSubmitted(this.eh.getStatusCode(), this.eh);
        }

        protected void mo1092d() {
        }
    }

    final class C1311e extends C0179b<OnAchievementUpdatedListener> {
        final /* synthetic */ au dJ;
        private final String dL;
        private final int f75p;

        C1311e(au auVar, OnAchievementUpdatedListener onAchievementUpdatedListener, int i, String str) {
            this.dJ = auVar;
            super(auVar, onAchievementUpdatedListener);
            this.f75p = i;
            this.dL = str;
        }

        protected void m717a(OnAchievementUpdatedListener onAchievementUpdatedListener) {
            onAchievementUpdatedListener.onAchievementUpdated(this.f75p, this.dL);
        }

        protected void mo1092d() {
        }
    }

    final class C1312m extends C0179b<OnInvitationReceivedListener> {
        final /* synthetic */ au dJ;
        private final Invitation dP;

        C1312m(au auVar, OnInvitationReceivedListener onInvitationReceivedListener, Invitation invitation) {
            this.dJ = auVar;
            super(auVar, onInvitationReceivedListener);
            this.dP = invitation;
        }

        protected void m720a(OnInvitationReceivedListener onInvitationReceivedListener) {
            onInvitationReceivedListener.onInvitationReceived(this.dP);
        }

        protected void mo1092d() {
        }
    }

    final class C1313r extends C0179b<OnLeaderboardScoresLoadedListener> {
        final /* synthetic */ au dJ;
        private final C1287d dS;
        private final C1287d dT;

        C1313r(au auVar, OnLeaderboardScoresLoadedListener onLeaderboardScoresLoadedListener, C1287d c1287d, C1287d c1287d2) {
            this.dJ = auVar;
            super(auVar, onLeaderboardScoresLoadedListener);
            this.dS = c1287d;
            this.dT = c1287d2;
        }

        protected void m723a(OnLeaderboardScoresLoadedListener onLeaderboardScoresLoadedListener) {
            C1287d c1287d = null;
            C1287d c1287d2 = this.dS;
            C1287d c1287d3 = this.dT;
            if (onLeaderboardScoresLoadedListener != null) {
                try {
                    onLeaderboardScoresLoadedListener.onLeaderboardScoresLoaded(c1287d3.getStatusCode(), new LeaderboardBuffer(c1287d2), new LeaderboardScoreBuffer(c1287d3));
                    c1287d3 = null;
                } catch (Throwable th) {
                    if (c1287d2 != null) {
                        c1287d2.close();
                    }
                    if (c1287d3 != null) {
                        c1287d3.close();
                    }
                }
            } else {
                c1287d = c1287d3;
                c1287d3 = c1287d2;
            }
            if (c1287d3 != null) {
                c1287d3.close();
            }
            if (c1287d != null) {
                c1287d.close();
            }
        }

        protected void mo1092d() {
            if (this.dS != null) {
                this.dS.close();
            }
            if (this.dT != null) {
                this.dT.close();
            }
        }
    }

    final class C1314u extends C0179b<RoomUpdateListener> {
        final /* synthetic */ au dJ;
        private final String dV;
        private final int f76p;

        C1314u(au auVar, RoomUpdateListener roomUpdateListener, int i, String str) {
            this.dJ = auVar;
            super(auVar, roomUpdateListener);
            this.f76p = i;
            this.dV = str;
        }

        public void m726a(RoomUpdateListener roomUpdateListener) {
            roomUpdateListener.onLeftRoom(this.f76p, this.dV);
        }

        protected void mo1092d() {
        }
    }

    final class C1315v extends C0179b<RealTimeMessageReceivedListener> {
        final /* synthetic */ au dJ;
        private final RealTimeMessage dW;

        C1315v(au auVar, RealTimeMessageReceivedListener realTimeMessageReceivedListener, RealTimeMessage realTimeMessage) {
            this.dJ = auVar;
            super(auVar, realTimeMessageReceivedListener);
            this.dW = realTimeMessage;
        }

        public void m729a(RealTimeMessageReceivedListener realTimeMessageReceivedListener) {
            ax.m221a("GamesClient", "Deliver Message received callback");
            if (realTimeMessageReceivedListener != null) {
                realTimeMessageReceivedListener.onRealTimeMessageReceived(this.dW);
            }
        }

        protected void mo1092d() {
        }
    }

    final class C1316w extends C0179b<RoomStatusUpdateListener> {
        final /* synthetic */ au dJ;
        private final String dX;

        C1316w(au auVar, RoomStatusUpdateListener roomStatusUpdateListener, String str) {
            this.dJ = auVar;
            super(auVar, roomStatusUpdateListener);
            this.dX = str;
        }

        public void m732a(RoomStatusUpdateListener roomStatusUpdateListener) {
            if (roomStatusUpdateListener != null) {
                roomStatusUpdateListener.onP2PConnected(this.dX);
            }
        }

        protected void mo1092d() {
        }
    }

    final class C1317x extends C0179b<RoomStatusUpdateListener> {
        final /* synthetic */ au dJ;
        private final String dX;

        C1317x(au auVar, RoomStatusUpdateListener roomStatusUpdateListener, String str) {
            this.dJ = auVar;
            super(auVar, roomStatusUpdateListener);
            this.dX = str;
        }

        public void m735a(RoomStatusUpdateListener roomStatusUpdateListener) {
            if (roomStatusUpdateListener != null) {
                roomStatusUpdateListener.onP2PDisconnected(this.dX);
            }
        }

        protected void mo1092d() {
        }
    }

    final class af extends C1352c<OnPlayersLoadedListener> {
        final /* synthetic */ au dJ;

        af(au auVar, OnPlayersLoadedListener onPlayersLoadedListener, C1287d c1287d) {
            this.dJ = auVar;
            super(auVar, onPlayersLoadedListener, c1287d);
        }

        protected void m1179a(OnPlayersLoadedListener onPlayersLoadedListener, C1287d c1287d) {
            onPlayersLoadedListener.onPlayersLoaded(c1287d.getStatusCode(), new PlayerBuffer(c1287d));
        }
    }

    abstract class C1698b extends C1352c<RoomUpdateListener> {
        final /* synthetic */ au dJ;

        C1698b(au auVar, RoomUpdateListener roomUpdateListener, C1287d c1287d) {
            this.dJ = auVar;
            super(auVar, roomUpdateListener, c1287d);
        }

        protected void m1181a(RoomUpdateListener roomUpdateListener, C1287d c1287d) {
            mo2677a(roomUpdateListener, this.dJ.m1197x(c1287d), c1287d.getStatusCode());
        }

        protected abstract void mo2677a(RoomUpdateListener roomUpdateListener, Room room, int i);
    }

    abstract class C1699c extends C1352c<RoomStatusUpdateListener> {
        final /* synthetic */ au dJ;

        C1699c(au auVar, RoomStatusUpdateListener roomStatusUpdateListener, C1287d c1287d) {
            this.dJ = auVar;
            super(auVar, roomStatusUpdateListener, c1287d);
        }

        protected void m1184a(RoomStatusUpdateListener roomStatusUpdateListener, C1287d c1287d) {
            mo2676a(roomStatusUpdateListener, this.dJ.m1197x(c1287d));
        }

        protected abstract void mo2676a(RoomStatusUpdateListener roomStatusUpdateListener, Room room);
    }

    final class C1700g extends C1352c<OnAchievementsLoadedListener> {
        final /* synthetic */ au dJ;

        C1700g(au auVar, OnAchievementsLoadedListener onAchievementsLoadedListener, C1287d c1287d) {
            this.dJ = auVar;
            super(auVar, onAchievementsLoadedListener, c1287d);
        }

        protected void m1187a(OnAchievementsLoadedListener onAchievementsLoadedListener, C1287d c1287d) {
            onAchievementsLoadedListener.onAchievementsLoaded(c1287d.getStatusCode(), new AchievementBuffer(c1287d));
        }
    }

    final class C1701k extends C1352c<OnGamesLoadedListener> {
        final /* synthetic */ au dJ;

        C1701k(au auVar, OnGamesLoadedListener onGamesLoadedListener, C1287d c1287d) {
            this.dJ = auVar;
            super(auVar, onGamesLoadedListener, c1287d);
        }

        protected void m1189a(OnGamesLoadedListener onGamesLoadedListener, C1287d c1287d) {
            onGamesLoadedListener.onGamesLoaded(c1287d.getStatusCode(), new GameBuffer(c1287d));
        }
    }

    final class C1702o extends C1352c<OnInvitationsLoadedListener> {
        final /* synthetic */ au dJ;

        C1702o(au auVar, OnInvitationsLoadedListener onInvitationsLoadedListener, C1287d c1287d) {
            this.dJ = auVar;
            super(auVar, onInvitationsLoadedListener, c1287d);
        }

        protected void m1191a(OnInvitationsLoadedListener onInvitationsLoadedListener, C1287d c1287d) {
            onInvitationsLoadedListener.onInvitationsLoaded(c1287d.getStatusCode(), new InvitationBuffer(c1287d));
        }
    }

    final class C1703t extends C1352c<OnLeaderboardMetadataLoadedListener> {
        final /* synthetic */ au dJ;

        C1703t(au auVar, OnLeaderboardMetadataLoadedListener onLeaderboardMetadataLoadedListener, C1287d c1287d) {
            this.dJ = auVar;
            super(auVar, onLeaderboardMetadataLoadedListener, c1287d);
        }

        protected void m1193a(OnLeaderboardMetadataLoadedListener onLeaderboardMetadataLoadedListener, C1287d c1287d) {
            onLeaderboardMetadataLoadedListener.onLeaderboardMetadataLoaded(c1287d.getStatusCode(), new LeaderboardBuffer(c1287d));
        }
    }

    abstract class C1769a extends C1699c {
        private final ArrayList<String> dI = new ArrayList();
        final /* synthetic */ au dJ;

        C1769a(au auVar, RoomStatusUpdateListener roomStatusUpdateListener, C1287d c1287d, String[] strArr) {
            this.dJ = auVar;
            super(auVar, roomStatusUpdateListener, c1287d);
            for (Object add : strArr) {
                this.dI.add(add);
            }
        }

        protected void mo2676a(RoomStatusUpdateListener roomStatusUpdateListener, Room room) {
            mo2693a(roomStatusUpdateListener, room, this.dI);
        }

        protected abstract void mo2693a(RoomStatusUpdateListener roomStatusUpdateListener, Room room, ArrayList<String> arrayList);
    }

    final class ae extends at {
        final /* synthetic */ au dJ;
        private final OnPlayersLoadedListener dY;

        ae(au auVar, OnPlayersLoadedListener onPlayersLoadedListener) {
            this.dJ = auVar;
            this.dY = (OnPlayersLoadedListener) C0192s.m518b((Object) onPlayersLoadedListener, (Object) "Listener must not be null");
        }

        public void mo1108e(C1287d c1287d) {
            this.dJ.m993a(new af(this.dJ, this.dY, c1287d));
        }
    }

    final class ah extends at {
        final /* synthetic */ au dJ;
        final RealTimeReliableMessageSentListener eb;

        public ah(au auVar, RealTimeReliableMessageSentListener realTimeReliableMessageSentListener) {
            this.dJ = auVar;
            this.eb = realTimeReliableMessageSentListener;
        }

        public void mo1097a(int i, int i2, String str) {
            this.dJ.m993a(new ag(this.dJ, this.eb, i, i2, str));
        }
    }

    final class ai extends C1699c {
        final /* synthetic */ au dJ;

        ai(au auVar, RoomStatusUpdateListener roomStatusUpdateListener, C1287d c1287d) {
            this.dJ = auVar;
            super(auVar, roomStatusUpdateListener, c1287d);
        }

        public void mo2676a(RoomStatusUpdateListener roomStatusUpdateListener, Room room) {
            roomStatusUpdateListener.onRoomAutoMatching(room);
        }
    }

    final class aj extends at {
        final /* synthetic */ au dJ;
        private final RoomUpdateListener ec;
        private final RoomStatusUpdateListener ed;
        private final RealTimeMessageReceivedListener ee;

        public aj(au auVar, RoomUpdateListener roomUpdateListener) {
            this.dJ = auVar;
            this.ec = (RoomUpdateListener) C0192s.m518b((Object) roomUpdateListener, (Object) "Callbacks must not be null");
            this.ed = null;
            this.ee = null;
        }

        public aj(au auVar, RoomUpdateListener roomUpdateListener, RoomStatusUpdateListener roomStatusUpdateListener, RealTimeMessageReceivedListener realTimeMessageReceivedListener) {
            this.dJ = auVar;
            this.ec = (RoomUpdateListener) C0192s.m518b((Object) roomUpdateListener, (Object) "Callbacks must not be null");
            this.ed = roomStatusUpdateListener;
            this.ee = realTimeMessageReceivedListener;
        }

        public void mo1101a(C1287d c1287d, String[] strArr) {
            this.dJ.m993a(new ab(this.dJ, this.ed, c1287d, strArr));
        }

        public void mo1103b(C1287d c1287d, String[] strArr) {
            this.dJ.m993a(new ac(this.dJ, this.ed, c1287d, strArr));
        }

        public void mo1105c(C1287d c1287d, String[] strArr) {
            this.dJ.m993a(new ad(this.dJ, this.ed, c1287d, strArr));
        }

        public void mo1107d(C1287d c1287d, String[] strArr) {
            this.dJ.m993a(new C1789z(this.dJ, this.ed, c1287d, strArr));
        }

        public void mo1109e(C1287d c1287d, String[] strArr) {
            this.dJ.m993a(new C1788y(this.dJ, this.ed, c1287d, strArr));
        }

        public void mo1111f(C1287d c1287d, String[] strArr) {
            this.dJ.m993a(new aa(this.dJ, this.ed, c1287d, strArr));
        }

        public void mo1119n(C1287d c1287d) {
            this.dJ.m993a(new am(this.dJ, this.ec, c1287d));
        }

        public void mo1120o(C1287d c1287d) {
            this.dJ.m993a(new C1777p(this.dJ, this.ec, c1287d));
        }

        public void onLeftRoom(int statusCode, String externalRoomId) {
            this.dJ.m993a(new C1314u(this.dJ, this.ec, statusCode, externalRoomId));
        }

        public void onP2PConnected(String participantId) {
            this.dJ.m993a(new C1316w(this.dJ, this.ed, participantId));
        }

        public void onP2PDisconnected(String participantId) {
            this.dJ.m993a(new C1317x(this.dJ, this.ed, participantId));
        }

        public void onRealTimeMessageReceived(RealTimeMessage message) {
            ax.m221a("GamesClient", "RoomBinderCallbacks: onRealTimeMessageReceived");
            this.dJ.m993a(new C1315v(this.dJ, this.ee, message));
        }

        public void mo1127p(C1287d c1287d) {
            this.dJ.m993a(new al(this.dJ, this.ed, c1287d));
        }

        public void mo1128q(C1287d c1287d) {
            this.dJ.m993a(new ai(this.dJ, this.ed, c1287d));
        }

        public void mo1129r(C1287d c1287d) {
            this.dJ.m993a(new ak(this.dJ, this.ec, c1287d));
        }

        public void mo1130s(C1287d c1287d) {
            this.dJ.m993a(new C1772h(this.dJ, this.ed, c1287d));
        }

        public void mo1131t(C1287d c1287d) {
            this.dJ.m993a(new C1773i(this.dJ, this.ed, c1287d));
        }
    }

    final class ak extends C1698b {
        final /* synthetic */ au dJ;

        ak(au auVar, RoomUpdateListener roomUpdateListener, C1287d c1287d) {
            this.dJ = auVar;
            super(auVar, roomUpdateListener, c1287d);
        }

        public void mo2677a(RoomUpdateListener roomUpdateListener, Room room, int i) {
            roomUpdateListener.onRoomConnected(i, room);
        }
    }

    final class al extends C1699c {
        final /* synthetic */ au dJ;

        al(au auVar, RoomStatusUpdateListener roomStatusUpdateListener, C1287d c1287d) {
            this.dJ = auVar;
            super(auVar, roomStatusUpdateListener, c1287d);
        }

        public void mo2676a(RoomStatusUpdateListener roomStatusUpdateListener, Room room) {
            roomStatusUpdateListener.onRoomConnecting(room);
        }
    }

    final class am extends C1698b {
        final /* synthetic */ au dJ;

        public am(au auVar, RoomUpdateListener roomUpdateListener, C1287d c1287d) {
            this.dJ = auVar;
            super(auVar, roomUpdateListener, c1287d);
        }

        public void mo2677a(RoomUpdateListener roomUpdateListener, Room room, int i) {
            roomUpdateListener.onRoomCreated(i, room);
        }
    }

    final class an extends at {
        final /* synthetic */ au dJ;
        private final OnSignOutCompleteListener ef;

        public an(au auVar, OnSignOutCompleteListener onSignOutCompleteListener) {
            this.dJ = auVar;
            this.ef = (OnSignOutCompleteListener) C0192s.m518b((Object) onSignOutCompleteListener, (Object) "Listener must not be null");
        }

        public void onSignOutComplete() {
            this.dJ.m993a(new ao(this.dJ, this.ef));
        }
    }

    final class ap extends at {
        final /* synthetic */ au dJ;
        private final OnScoreSubmittedListener eg;

        public ap(au auVar, OnScoreSubmittedListener onScoreSubmittedListener) {
            this.dJ = auVar;
            this.eg = (OnScoreSubmittedListener) C0192s.m518b((Object) onScoreSubmittedListener, (Object) "Listener must not be null");
        }

        public void mo1106d(C1287d c1287d) {
            this.dJ.m993a(new aq(this.dJ, this.eg, new SubmitScoreResult(c1287d)));
        }
    }

    final class C1770d extends at {
        final /* synthetic */ au dJ;
        private final OnAchievementUpdatedListener dK;

        C1770d(au auVar, OnAchievementUpdatedListener onAchievementUpdatedListener) {
            this.dJ = auVar;
            this.dK = (OnAchievementUpdatedListener) C0192s.m518b((Object) onAchievementUpdatedListener, (Object) "Listener must not be null");
        }

        public void onAchievementUpdated(int statusCode, String achievementId) {
            this.dJ.m993a(new C1311e(this.dJ, this.dK, statusCode, achievementId));
        }
    }

    final class C1771f extends at {
        final /* synthetic */ au dJ;
        private final OnAchievementsLoadedListener dM;

        C1771f(au auVar, OnAchievementsLoadedListener onAchievementsLoadedListener) {
            this.dJ = auVar;
            this.dM = (OnAchievementsLoadedListener) C0192s.m518b((Object) onAchievementsLoadedListener, (Object) "Listener must not be null");
        }

        public void mo1102b(C1287d c1287d) {
            this.dJ.m993a(new C1700g(this.dJ, this.dM, c1287d));
        }
    }

    final class C1772h extends C1699c {
        final /* synthetic */ au dJ;

        C1772h(au auVar, RoomStatusUpdateListener roomStatusUpdateListener, C1287d c1287d) {
            this.dJ = auVar;
            super(auVar, roomStatusUpdateListener, c1287d);
        }

        public void mo2676a(RoomStatusUpdateListener roomStatusUpdateListener, Room room) {
            roomStatusUpdateListener.onConnectedToRoom(room);
        }
    }

    final class C1773i extends C1699c {
        final /* synthetic */ au dJ;

        C1773i(au auVar, RoomStatusUpdateListener roomStatusUpdateListener, C1287d c1287d) {
            this.dJ = auVar;
            super(auVar, roomStatusUpdateListener, c1287d);
        }

        public void mo2676a(RoomStatusUpdateListener roomStatusUpdateListener, Room room) {
            roomStatusUpdateListener.onDisconnectedFromRoom(room);
        }
    }

    final class C1774j extends at {
        final /* synthetic */ au dJ;
        private final OnGamesLoadedListener dN;

        C1774j(au auVar, OnGamesLoadedListener onGamesLoadedListener) {
            this.dJ = auVar;
            this.dN = (OnGamesLoadedListener) C0192s.m518b((Object) onGamesLoadedListener, (Object) "Listener must not be null");
        }

        public void mo1112g(C1287d c1287d) {
            this.dJ.m993a(new C1701k(this.dJ, this.dN, c1287d));
        }
    }

    final class C1775l extends at {
        final /* synthetic */ au dJ;
        private final OnInvitationReceivedListener dO;

        C1775l(au auVar, OnInvitationReceivedListener onInvitationReceivedListener) {
            this.dJ = auVar;
            this.dO = onInvitationReceivedListener;
        }

        public void mo1116k(C1287d c1287d) {
            InvitationBuffer invitationBuffer = new InvitationBuffer(c1287d);
            Invitation invitation = null;
            try {
                if (invitationBuffer.getCount() > 0) {
                    invitation = (Invitation) ((Invitation) invitationBuffer.get(0)).freeze();
                }
                invitationBuffer.close();
                if (invitation != null) {
                    this.dJ.m993a(new C1312m(this.dJ, this.dO, invitation));
                }
            } catch (Throwable th) {
                invitationBuffer.close();
            }
        }
    }

    final class C1776n extends at {
        final /* synthetic */ au dJ;
        private final OnInvitationsLoadedListener dQ;

        C1776n(au auVar, OnInvitationsLoadedListener onInvitationsLoadedListener) {
            this.dJ = auVar;
            this.dQ = onInvitationsLoadedListener;
        }

        public void mo1115j(C1287d c1287d) {
            this.dJ.m993a(new C1702o(this.dJ, this.dQ, c1287d));
        }
    }

    final class C1777p extends C1698b {
        final /* synthetic */ au dJ;

        public C1777p(au auVar, RoomUpdateListener roomUpdateListener, C1287d c1287d) {
            this.dJ = auVar;
            super(auVar, roomUpdateListener, c1287d);
        }

        public void mo2677a(RoomUpdateListener roomUpdateListener, Room room, int i) {
            roomUpdateListener.onJoinedRoom(i, room);
        }
    }

    final class C1778q extends at {
        final /* synthetic */ au dJ;
        private final OnLeaderboardScoresLoadedListener dR;

        C1778q(au auVar, OnLeaderboardScoresLoadedListener onLeaderboardScoresLoadedListener) {
            this.dJ = auVar;
            this.dR = (OnLeaderboardScoresLoadedListener) C0192s.m518b((Object) onLeaderboardScoresLoadedListener, (Object) "Listener must not be null");
        }

        public void mo1100a(C1287d c1287d, C1287d c1287d2) {
            this.dJ.m993a(new C1313r(this.dJ, this.dR, c1287d, c1287d2));
        }
    }

    final class C1779s extends at {
        final /* synthetic */ au dJ;
        private final OnLeaderboardMetadataLoadedListener dU;

        C1779s(au auVar, OnLeaderboardMetadataLoadedListener onLeaderboardMetadataLoadedListener) {
            this.dJ = auVar;
            this.dU = (OnLeaderboardMetadataLoadedListener) C0192s.m518b((Object) onLeaderboardMetadataLoadedListener, (Object) "Listener must not be null");
        }

        public void mo1104c(C1287d c1287d) {
            this.dJ.m993a(new C1703t(this.dJ, this.dU, c1287d));
        }
    }

    final class aa extends C1769a {
        final /* synthetic */ au dJ;

        aa(au auVar, RoomStatusUpdateListener roomStatusUpdateListener, C1287d c1287d, String[] strArr) {
            this.dJ = auVar;
            super(auVar, roomStatusUpdateListener, c1287d, strArr);
        }

        protected void mo2693a(RoomStatusUpdateListener roomStatusUpdateListener, Room room, ArrayList<String> arrayList) {
            roomStatusUpdateListener.onPeersDisconnected(room, arrayList);
        }
    }

    final class ab extends C1769a {
        final /* synthetic */ au dJ;

        ab(au auVar, RoomStatusUpdateListener roomStatusUpdateListener, C1287d c1287d, String[] strArr) {
            this.dJ = auVar;
            super(auVar, roomStatusUpdateListener, c1287d, strArr);
        }

        protected void mo2693a(RoomStatusUpdateListener roomStatusUpdateListener, Room room, ArrayList<String> arrayList) {
            roomStatusUpdateListener.onPeerInvitedToRoom(room, arrayList);
        }
    }

    final class ac extends C1769a {
        final /* synthetic */ au dJ;

        ac(au auVar, RoomStatusUpdateListener roomStatusUpdateListener, C1287d c1287d, String[] strArr) {
            this.dJ = auVar;
            super(auVar, roomStatusUpdateListener, c1287d, strArr);
        }

        protected void mo2693a(RoomStatusUpdateListener roomStatusUpdateListener, Room room, ArrayList<String> arrayList) {
            roomStatusUpdateListener.onPeerJoined(room, arrayList);
        }
    }

    final class ad extends C1769a {
        final /* synthetic */ au dJ;

        ad(au auVar, RoomStatusUpdateListener roomStatusUpdateListener, C1287d c1287d, String[] strArr) {
            this.dJ = auVar;
            super(auVar, roomStatusUpdateListener, c1287d, strArr);
        }

        protected void mo2693a(RoomStatusUpdateListener roomStatusUpdateListener, Room room, ArrayList<String> arrayList) {
            roomStatusUpdateListener.onPeerLeft(room, arrayList);
        }
    }

    final class C1788y extends C1769a {
        final /* synthetic */ au dJ;

        C1788y(au auVar, RoomStatusUpdateListener roomStatusUpdateListener, C1287d c1287d, String[] strArr) {
            this.dJ = auVar;
            super(auVar, roomStatusUpdateListener, c1287d, strArr);
        }

        protected void mo2693a(RoomStatusUpdateListener roomStatusUpdateListener, Room room, ArrayList<String> arrayList) {
            roomStatusUpdateListener.onPeersConnected(room, arrayList);
        }
    }

    final class C1789z extends C1769a {
        final /* synthetic */ au dJ;

        C1789z(au auVar, RoomStatusUpdateListener roomStatusUpdateListener, C1287d c1287d, String[] strArr) {
            this.dJ = auVar;
            super(auVar, roomStatusUpdateListener, c1287d, strArr);
        }

        protected void mo2693a(RoomStatusUpdateListener roomStatusUpdateListener, Room room, ArrayList<String> arrayList) {
            roomStatusUpdateListener.onPeerDeclined(room, arrayList);
        }
    }

    public au(Context context, String str, String str2, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener, String[] strArr, int i, View view, boolean z) {
        super(context, connectionCallbacks, onConnectionFailedListener, strArr);
        this.dz = str;
        this.f155g = (String) C0192s.m521d(str2);
        this.dF = new Binder();
        this.dA = new HashMap();
        this.dD = ba.m334a(this, i);
        setViewForPopups(view);
        this.dG = (long) hashCode();
        this.dH = z;
    }

    private void av() {
        this.dB = null;
    }

    private void aw() {
        for (bb close : this.dA.values()) {
            try {
                close.close();
            } catch (Throwable e) {
                ax.m222a("GamesClient", "IOException:", e);
            }
        }
        this.dA.clear();
    }

    private bb m1196t(String str) {
        try {
            String v = ((az) m990C()).mo1211v(str);
            if (v == null) {
                return null;
            }
            ax.m225d("GamesClient", "Creating a socket to bind to:" + v);
            LocalSocket localSocket = new LocalSocket();
            try {
                localSocket.connect(new LocalSocketAddress(v));
                bb bbVar = new bb(localSocket, str);
                this.dA.put(str, bbVar);
                return bbVar;
            } catch (IOException e) {
                ax.m224c("GamesClient", "connect() call failed on socket: " + e.getMessage());
                return null;
            }
        } catch (RemoteException e2) {
            ax.m224c("GamesClient", "Unable to create socket. Service died.");
            return null;
        }
    }

    private Room m1197x(C1287d c1287d) {
        C1696a c1696a = new C1696a(c1287d);
        Room room = null;
        try {
            if (c1696a.getCount() > 0) {
                room = (Room) ((Room) c1696a.get(0)).freeze();
            }
            c1696a.close();
            return room;
        } catch (Throwable th) {
            c1696a.close();
        }
    }

    public int m1198a(byte[] bArr, String str, String[] strArr) {
        C0192s.m518b((Object) strArr, (Object) "Participant IDs must not be null");
        try {
            return ((az) m990C()).mo1163b(bArr, str, strArr);
        } catch (RemoteException e) {
            ax.m223b("GamesClient", "service died");
            return -1;
        }
    }

    protected void mo2345a(int i, IBinder iBinder, Bundle bundle) {
        if (i == 0 && bundle != null) {
            this.dE = bundle.getBoolean("show_welcome_popup");
        }
        super.mo2345a(i, iBinder, bundle);
    }

    public void m1200a(IBinder iBinder, Bundle bundle) {
        if (isConnected()) {
            try {
                ((az) m990C()).mo1137a(iBinder, bundle);
            } catch (RemoteException e) {
                ax.m223b("GamesClient", "service died");
            }
        }
    }

    protected void mo2346a(ConnectionResult connectionResult) {
        super.mo2346a(connectionResult);
        this.dE = false;
    }

    public void m1202a(OnPlayersLoadedListener onPlayersLoadedListener, int i, boolean z, boolean z2) {
        try {
            ((az) m990C()).mo1140a(new ae(this, onPlayersLoadedListener), i, z, z2);
        } catch (RemoteException e) {
            ax.m223b("GamesClient", "service died");
        }
    }

    public void m1203a(OnAchievementUpdatedListener onAchievementUpdatedListener, String str) {
        if (onAchievementUpdatedListener == null) {
            ay ayVar = null;
        } else {
            Object c1770d = new C1770d(this, onAchievementUpdatedListener);
        }
        try {
            ((az) m990C()).mo1151a(ayVar, str, this.dD.aD(), this.dD.aC());
        } catch (RemoteException e) {
            ax.m223b("GamesClient", "service died");
        }
    }

    public void m1204a(OnAchievementUpdatedListener onAchievementUpdatedListener, String str, int i) {
        try {
            ((az) m990C()).mo1147a(onAchievementUpdatedListener == null ? null : new C1770d(this, onAchievementUpdatedListener), str, i, this.dD.aD(), this.dD.aC());
        } catch (RemoteException e) {
            ax.m223b("GamesClient", "service died");
        }
    }

    public void m1205a(OnScoreSubmittedListener onScoreSubmittedListener, String str, long j) {
        if (onScoreSubmittedListener == null) {
            ay ayVar = null;
        } else {
            Object apVar = new ap(this, onScoreSubmittedListener);
        }
        try {
            ((az) m990C()).mo1150a(ayVar, str, j);
        } catch (RemoteException e) {
            ax.m223b("GamesClient", "service died");
        }
    }

    protected void mo2347a(C0187p c0187p, C1722d c1722d) throws RemoteException {
        String locale = getContext().getResources().getConfiguration().locale.toString();
        Bundle bundle = new Bundle();
        bundle.putBoolean("com.google.android.gms.games.key.isHeadless", this.dH);
        c0187p.mo1328a(c1722d, GooglePlayServicesUtil.GOOGLE_PLAY_SERVICES_VERSION_CODE, getContext().getPackageName(), this.f155g, m1000x(), this.dz, this.dD.aD(), locale, bundle);
    }

    protected void mo2348a(String... strArr) {
        int i = 0;
        boolean z = false;
        for (String str : strArr) {
            if (str.equals(Scopes.GAMES)) {
                z = true;
            } else if (str.equals("https://www.googleapis.com/auth/games.firstparty")) {
                i = 1;
            }
        }
        if (i != 0) {
            C0192s.m516a(!z, String.format("Cannot have both %s and %s!", new Object[]{Scopes.GAMES, "https://www.googleapis.com/auth/games.firstparty"}));
            return;
        }
        C0192s.m516a(z, String.format("GamesClient requires %s to function.", new Object[]{Scopes.GAMES}));
    }

    public void ax() {
        if (isConnected()) {
            try {
                ((az) m990C()).ax();
            } catch (RemoteException e) {
                ax.m223b("GamesClient", "service died");
            }
        }
    }

    protected String mo2349b() {
        return "com.google.android.gms.games.service.START";
    }

    public void m1209b(OnAchievementUpdatedListener onAchievementUpdatedListener, String str) {
        if (onAchievementUpdatedListener == null) {
            ay ayVar = null;
        } else {
            Object c1770d = new C1770d(this, onAchievementUpdatedListener);
        }
        try {
            ((az) m990C()).mo1171b(ayVar, str, this.dD.aD(), this.dD.aC());
        } catch (RemoteException e) {
            ax.m223b("GamesClient", "service died");
        }
    }

    protected /* synthetic */ IInterface mo2350c(IBinder iBinder) {
        return m1214m(iBinder);
    }

    protected String mo2351c() {
        return "com.google.android.gms.games.internal.IGamesService";
    }

    public void clearNotifications(int notificationTypes) {
        try {
            ((az) m990C()).clearNotifications(notificationTypes);
        } catch (RemoteException e) {
            ax.m223b("GamesClient", "service died");
        }
    }

    public void connect() {
        av();
        super.connect();
    }

    public void createRoom(RoomConfig config) {
        try {
            ((az) m990C()).mo1143a(new aj(this, config.getRoomUpdateListener(), config.getRoomStatusUpdateListener(), config.getMessageReceivedListener()), this.dF, config.getVariant(), config.getInvitedPlayerIds(), config.getAutoMatchCriteria(), config.isSocketEnabled(), this.dG);
        } catch (RemoteException e) {
            ax.m223b("GamesClient", "service died");
        }
    }

    public void disconnect() {
        this.dE = false;
        if (isConnected()) {
            try {
                az azVar = (az) m990C();
                azVar.ax();
                azVar.mo1165b(this.dG);
                azVar.mo1136a(this.dG);
            } catch (RemoteException e) {
                ax.m223b("GamesClient", "Failed to notify client disconnect.");
            }
        }
        aw();
        super.disconnect();
    }

    public Intent getAchievementsIntent() {
        m989B();
        Intent intent = new Intent("com.google.android.gms.games.VIEW_ACHIEVEMENTS");
        intent.addFlags(67108864);
        return aw.m220b(intent);
    }

    public Intent getAllLeaderboardsIntent() {
        m989B();
        Intent intent = new Intent("com.google.android.gms.games.VIEW_LEADERBOARDS");
        intent.putExtra("com.google.android.gms.games.GAME_PACKAGE_NAME", this.dz);
        intent.addFlags(67108864);
        return aw.m220b(intent);
    }

    public String getAppId() {
        try {
            return ((az) m990C()).getAppId();
        } catch (RemoteException e) {
            ax.m223b("GamesClient", "service died");
            return null;
        }
    }

    public String getCurrentAccountName() {
        try {
            return ((az) m990C()).getCurrentAccountName();
        } catch (RemoteException e) {
            ax.m223b("GamesClient", "service died");
            return null;
        }
    }

    public Game getCurrentGame() {
        m989B();
        synchronized (this) {
            if (this.dC == null) {
                GameBuffer gameBuffer;
                try {
                    gameBuffer = new GameBuffer(((az) m990C()).aA());
                    if (gameBuffer.getCount() > 0) {
                        this.dC = (GameEntity) gameBuffer.get(0).freeze();
                    }
                    gameBuffer.close();
                } catch (RemoteException e) {
                    ax.m223b("GamesClient", "service died");
                } catch (Throwable th) {
                    gameBuffer.close();
                }
            }
        }
        return this.dC;
    }

    public Player getCurrentPlayer() {
        m989B();
        synchronized (this) {
            if (this.dB == null) {
                PlayerBuffer playerBuffer;
                try {
                    playerBuffer = new PlayerBuffer(((az) m990C()).ay());
                    if (playerBuffer.getCount() > 0) {
                        this.dB = (PlayerEntity) playerBuffer.get(0).freeze();
                    }
                    playerBuffer.close();
                } catch (RemoteException e) {
                    ax.m223b("GamesClient", "service died");
                } catch (Throwable th) {
                    playerBuffer.close();
                }
            }
        }
        return this.dB;
    }

    public String getCurrentPlayerId() {
        try {
            return ((az) m990C()).getCurrentPlayerId();
        } catch (RemoteException e) {
            ax.m223b("GamesClient", "service died");
            return null;
        }
    }

    public Intent getInvitationInboxIntent() {
        m989B();
        Intent intent = new Intent("com.google.android.gms.games.SHOW_INVITATIONS");
        intent.putExtra("com.google.android.gms.games.GAME_PACKAGE_NAME", this.dz);
        return aw.m220b(intent);
    }

    public Intent getLeaderboardIntent(String leaderboardId) {
        m989B();
        Intent intent = new Intent("com.google.android.gms.games.VIEW_LEADERBOARD_SCORES");
        intent.putExtra("com.google.android.gms.games.LEADERBOARD_ID", leaderboardId);
        intent.addFlags(67108864);
        return aw.m220b(intent);
    }

    public RealTimeSocket getRealTimeSocketForParticipant(String roomId, String participantId) {
        if (participantId == null || !ParticipantUtils.m151z(participantId)) {
            throw new IllegalArgumentException("Bad participant ID");
        }
        bb bbVar = (bb) this.dA.get(participantId);
        return (bbVar == null || bbVar.isClosed()) ? m1196t(participantId) : bbVar;
    }

    public Intent getRealTimeWaitingRoomIntent(Room room, int minParticipantsToStart) {
        m989B();
        Intent intent = new Intent("com.google.android.gms.games.SHOW_REAL_TIME_WAITING_ROOM");
        C0192s.m518b((Object) room, (Object) "Room parameter must not be null");
        intent.putExtra(GamesClient.EXTRA_ROOM, (Parcelable) room.freeze());
        C0192s.m516a(minParticipantsToStart >= 0, "minParticipantsToStart must be >= 0");
        intent.putExtra("com.google.android.gms.games.MIN_PARTICIPANTS_TO_START", minParticipantsToStart);
        return aw.m220b(intent);
    }

    public Intent getSelectPlayersIntent(int minPlayers, int maxPlayers) {
        m989B();
        Intent intent = new Intent("com.google.android.gms.games.SELECT_PLAYERS");
        intent.putExtra("com.google.android.gms.games.MIN_SELECTIONS", minPlayers);
        intent.putExtra("com.google.android.gms.games.MAX_SELECTIONS", maxPlayers);
        return aw.m220b(intent);
    }

    public Intent getSettingsIntent() {
        m989B();
        Intent intent = new Intent("com.google.android.gms.games.SHOW_SETTINGS");
        intent.putExtra("com.google.android.gms.games.GAME_PACKAGE_NAME", this.dz);
        intent.addFlags(67108864);
        return aw.m220b(intent);
    }

    public void m1212h(String str, int i) {
        try {
            ((az) m990C()).mo1201h(str, i);
        } catch (RemoteException e) {
            ax.m223b("GamesClient", "service died");
        }
    }

    public void m1213i(String str, int i) {
        try {
            ((az) m990C()).mo1204i(str, i);
        } catch (RemoteException e) {
            ax.m223b("GamesClient", "service died");
        }
    }

    public void joinRoom(RoomConfig config) {
        try {
            ((az) m990C()).mo1144a(new aj(this, config.getRoomUpdateListener(), config.getRoomStatusUpdateListener(), config.getMessageReceivedListener()), this.dF, config.getInvitationId(), config.isSocketEnabled(), this.dG);
        } catch (RemoteException e) {
            ax.m223b("GamesClient", "service died");
        }
    }

    public void leaveRoom(RoomUpdateListener listener, String roomId) {
        try {
            ((az) m990C()).mo1190e(new aj(this, listener), roomId);
            aw();
        } catch (RemoteException e) {
            ax.m223b("GamesClient", "service died");
        }
    }

    public void loadAchievements(OnAchievementsLoadedListener listener, boolean forceReload) {
        try {
            ((az) m990C()).mo1176b(new C1771f(this, listener), forceReload);
        } catch (RemoteException e) {
            ax.m223b("GamesClient", "service died");
        }
    }

    public void loadGame(OnGamesLoadedListener listener) {
        try {
            ((az) m990C()).mo1184d(new C1774j(this, listener));
        } catch (RemoteException e) {
            ax.m223b("GamesClient", "service died");
        }
    }

    public void loadInvitations(OnInvitationsLoadedListener listener) {
        try {
            ((az) m990C()).mo1188e(new C1776n(this, listener));
        } catch (RemoteException e) {
            ax.m223b("GamesClient", "service died");
        }
    }

    public void loadLeaderboardMetadata(OnLeaderboardMetadataLoadedListener listener, String leaderboardId, boolean forceReload) {
        try {
            ((az) m990C()).mo1181c(new C1779s(this, listener), leaderboardId, forceReload);
        } catch (RemoteException e) {
            ax.m223b("GamesClient", "service died");
        }
    }

    public void loadLeaderboardMetadata(OnLeaderboardMetadataLoadedListener listener, boolean forceReload) {
        try {
            ((az) m990C()).mo1182c(new C1779s(this, listener), forceReload);
        } catch (RemoteException e) {
            ax.m223b("GamesClient", "service died");
        }
    }

    public void loadMoreScores(OnLeaderboardScoresLoadedListener listener, LeaderboardScoreBuffer buffer, int maxResults, int pageDirection) {
        try {
            ((az) m990C()).mo1142a(new C1778q(this, listener), buffer.aF().aG(), maxResults, pageDirection);
        } catch (RemoteException e) {
            ax.m223b("GamesClient", "service died");
        }
    }

    public void loadPlayer(OnPlayersLoadedListener listener, String playerId) {
        try {
            ((az) m990C()).mo1179c(new ae(this, listener), playerId);
        } catch (RemoteException e) {
            ax.m223b("GamesClient", "service died");
        }
    }

    public void loadPlayerCenteredScores(OnLeaderboardScoresLoadedListener listener, String leaderboardId, int span, int leaderboardCollection, int maxResults, boolean forceReload) {
        try {
            ((az) m990C()).mo1169b(new C1778q(this, listener), leaderboardId, span, leaderboardCollection, maxResults, forceReload);
        } catch (RemoteException e) {
            ax.m223b("GamesClient", "service died");
        }
    }

    public void loadTopScores(OnLeaderboardScoresLoadedListener listener, String leaderboardId, int span, int leaderboardCollection, int maxResults, boolean forceReload) {
        try {
            ((az) m990C()).mo1146a(new C1778q(this, listener), leaderboardId, span, leaderboardCollection, maxResults, forceReload);
        } catch (RemoteException e) {
            ax.m223b("GamesClient", "service died");
        }
    }

    protected az m1214m(IBinder iBinder) {
        return C1321a.m847o(iBinder);
    }

    public void registerInvitationListener(OnInvitationReceivedListener listener) {
        try {
            ((az) m990C()).mo1141a(new C1775l(this, listener), this.dG);
        } catch (RemoteException e) {
            ax.m223b("GamesClient", "service died");
        }
    }

    public int sendReliableRealTimeMessage(RealTimeReliableMessageSentListener listener, byte[] messageData, String roomId, String recipientParticipantId) {
        try {
            return ((az) m990C()).mo1135a(new ah(this, listener), messageData, roomId, recipientParticipantId);
        } catch (RemoteException e) {
            ax.m223b("GamesClient", "service died");
            return -1;
        }
    }

    public int sendUnreliableRealTimeMessageToAll(byte[] messageData, String roomId) {
        try {
            return ((az) m990C()).mo1163b(messageData, roomId, null);
        } catch (RemoteException e) {
            ax.m223b("GamesClient", "service died");
            return -1;
        }
    }

    public void setGravityForPopups(int gravity) {
        this.dD.setGravity(gravity);
    }

    public void setUseNewPlayerNotificationsFirstParty(boolean newPlayerStyle) {
        try {
            ((az) m990C()).setUseNewPlayerNotificationsFirstParty(newPlayerStyle);
        } catch (RemoteException e) {
            ax.m223b("GamesClient", "service died");
        }
    }

    public void setViewForPopups(View gamesContentView) {
        this.dD.mo1217a(gamesContentView);
    }

    public void signOut(OnSignOutCompleteListener listener) {
        if (listener == null) {
            ay ayVar = null;
        } else {
            Object anVar = new an(this, listener);
        }
        try {
            ((az) m990C()).mo1138a(ayVar);
        } catch (RemoteException e) {
            ax.m223b("GamesClient", "service died");
        }
    }

    public void unregisterInvitationListener() {
        try {
            ((az) m990C()).mo1165b(this.dG);
        } catch (RemoteException e) {
            ax.m223b("GamesClient", "service died");
        }
    }

    protected void mo2352y() {
        super.mo2352y();
        if (this.dE) {
            this.dD.aB();
            this.dE = false;
        }
    }

    protected Bundle mo2353z() {
        try {
            Bundle z = ((az) m990C()).mo1215z();
            if (z == null) {
                return z;
            }
            z.setClassLoader(au.class.getClassLoader());
            return z;
        } catch (RemoteException e) {
            ax.m223b("GamesClient", "service died");
            return null;
        }
    }
}
