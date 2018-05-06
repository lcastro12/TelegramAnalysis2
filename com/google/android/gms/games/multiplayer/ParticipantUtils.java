package com.google.android.gms.games.multiplayer;

import com.google.android.gms.games.Player;
import com.google.android.gms.internal.C0192s;
import java.util.ArrayList;

public final class ParticipantUtils {
    private ParticipantUtils() {
    }

    public static String getParticipantId(ArrayList<Participant> participants, String playerId) {
        int size = participants.size();
        for (int i = 0; i < size; i++) {
            Participant participant = (Participant) participants.get(i);
            Player player = participant.getPlayer();
            if (player != null && player.getPlayerId().equals(playerId)) {
                return participant.getParticipantId();
            }
        }
        return null;
    }

    public static boolean m151z(String str) {
        C0192s.m518b((Object) str, (Object) "Participant ID must not be null");
        return str.startsWith("p_");
    }
}
