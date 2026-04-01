package rule;

import game.GameRoom;

import java.util.List;

public class PlayRuleChecker {
    public boolean canPlay(GameRoom room, int playerId, List<Integer> cards) {
        return room != null
                && room.findPlayerById(playerId) != null
                && cards != null
                && !cards.isEmpty();
    }
}
