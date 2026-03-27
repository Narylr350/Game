package server.rule;

import server.model.GameSession;

public class LandlordRule {
    public boolean canCallLandlord(GameSession session, int playerId) {
        return session != null && session.findPlayerById(playerId) != null;
    }
}
