package server.rule;

import server.model.GameSession;

import java.util.List;

public class PlayRuleChecker {
    public boolean canPlay(GameSession session, int playerId, List<Integer> cards) {
        return session != null && session.findPlayerById(playerId) != null && cards != null && !cards.isEmpty();
    }
}
